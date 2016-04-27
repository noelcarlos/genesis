package org.esmartpoint.genesis.helpers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.esmartpoint.dbutil.Cronometro;
import org.esmartpoint.dbutil.DAOBase;
import org.esmartpoint.dbutil.db.metadata.FieldInfo;
import org.esmartpoint.dbutil.db.metadata.TableInfo;
import org.esmartpoint.genesis.util.Stats;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.ejb.HibernatePersistence;
import org.hibernate.jdbc.Work;
import org.hibernate.transform.AliasToEntityMapResultTransformer;
import org.hibernate.type.BooleanType;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import httl.util.StringUtils;

@Service
@Component
public class DbHelper {
	class ConnectionInfo {
		private EntityManager entityManager;
		private EntityManagerFactory entityManagerFactory;
		private Transaction transaction;
		private String dialect;
		
		public ConnectionInfo(EntityManagerFactory entityManagerFactory, EntityManager entityManager, Transaction transaction, String dialect) {
			super();
			this.entityManagerFactory = entityManagerFactory;
			this.entityManager = entityManager;
			this.transaction = transaction;
			this.setDialect(dialect);
		}
		public EntityManager getEntityManager() {
			return entityManager;
		}
		public void setEntityManager(EntityManager entityManager) {
			this.entityManager = entityManager;
		}
		public EntityManagerFactory getEntityManagerFactory() {
			return entityManagerFactory;
		}
		public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
			this.entityManagerFactory = entityManagerFactory;
		}
		public Transaction getTransaction() {
			return transaction;
		}
		public void setTransaction(Transaction transaction) {
			this.transaction = transaction;
		}
		public String getDialect() {
			return dialect;
		}
		public void setDialect(String dialect) {
			this.dialect = dialect;
		}
	};
	
	HashMap<String, ConnectionInfo> connectionMap = new HashMap<String, ConnectionInfo>();
	
	public Session getSession(String name) {
		if (StringUtils.isEmpty(name)) {
			name = "default";
		}
		return (Session) connectionMap.get(name).getEntityManager().getDelegate();
	}
	
	@SuppressWarnings("unchecked")
	public HashMap<String, Object> load(String name, String str, HashMap<String, Object> params) {
		SQLQuery query = getSession(name).createSQLQuery(str);
		query.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE);
		return (HashMap<String, Object>)query.uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	public List<HashMap<String, Object>> list(String name, String str, HashMap<String, Object> params) {
		SQLQuery query = getSession(name).createSQLQuery(str);
		query.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE);
		return (List<HashMap<String, Object>>)query.list();
	}

	@SuppressWarnings("unchecked")
	public List<Object> listOneField(String name, String str, HashMap<String, Object> params) {
		SQLQuery query = getSession(name).createSQLQuery(str);
		query.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE);
		List<Object> res = new ArrayList<Object>();
		String keyName = null;
		for (HashMap<String, Object> record : (List<HashMap<String, Object>>)query.list()) {
			if (record.size() == 1) {
				if (keyName == null) {
					keyName = record.keySet().iterator().next();
				}
				res.add(record.get(keyName));
			} else {
				res.add(record);
			}
		}
		return res;
	}
	
	protected void readSchemaDefinitions(String name) {
		getSession(name).doWork(new Work() {
		    @Override
		    public void execute(Connection connection) throws SQLException {
		        //connection, finally!
		    	DAOBase.setDialect(DAOBase.DIALECT_POSTGRES);
		    	DAOBase.setCacheFullMetadata(true);
		    	DAOBase.initConnection(connection);
		    }
		});
	}
	
	public void insert(String name, String entityName, HashMap<String, Object> values, String keyOutput) throws Exception {
		StringBuffer buf = new StringBuffer("INSERT INTO ");
		if (StringUtils.isEmpty(name)) {
			name = "default";
		}
		
    	TableInfo ti = DAOBase.getMetadata().getTables().get(entityName);
		
		buf.append(entityName);
		buf.append("(");
		boolean startStr = true; 
		for (String key : values.keySet()) {
			if (!startStr)
				buf.append(",");
			buf.append(key);
			startStr = false;
		}
		buf.append(") VALUES (");
		startStr = true; 
		for (String key : values.keySet()) {
			if (!startStr)
				buf.append(",");
			FieldInfo fieldInfo = ti.getFieldsMap().get(key);
			if (fieldInfo == null)
				throw new Exception("Field [" + key + "] not found in [" + entityName + "] table.");
			String typeName = fieldInfo.getTypeName();
			if (typeName.equals("bit")) {
				buf.append(":" + key);
			} else {
				buf.append(":" + key);
			}
			startStr = false;
		}
		buf.append(")");
		SQLQuery query = getSession(name).createSQLQuery(buf.toString());
		
		for (String key : values.keySet()) {
			Object v = values.get(key);
//			if (v != null) {
//				query.setParameter(key, v);
//			} 
			String typeName = ti.getFieldsMap().get(key).getTypeName();
			if (typeName.equals("timestamp")) {
				if (v == null)
					query.setDate(key, null);
				else if (v instanceof Date)
					query.setDate(key, (Date)v);
				else
					query.setDate(key, ((DateTime)v).toDate());
			} else if (typeName.equals("int8") || typeName.equals("int4")) {
				if (v == null)
					query.setBigInteger(key, null);
				else {
					if (v instanceof java.lang.Boolean) {
						query.setInteger(key, ((Boolean)v) ? 1 : 0);
					} else
						query.setInteger(key, (int)v);
				}
			} else if (typeName.equals("bit")) {
				if (v == null)
					query.setBigInteger(key, null);
				else {
					if (v instanceof java.lang.Boolean) {
						query.setParameter(key, ((Boolean)v) ? true : false, BooleanType.INSTANCE);
					} else
						query.setInteger(key, (int)v);
				}
			} else {
				query.setParameter(key, v);
			}
		}
		query.executeUpdate();
		
		if (keyOutput != null && keyOutput.length() > 0) {
			String dialect = connectionMap.get(name).getDialect();
			Object res;
			if (dialect.equals("org.hibernate.dialect.PostgreSQLDialect")) {
				res = getSession(name).createSQLQuery("SELECT LASTVAL()").uniqueResult();
			} else if (dialect.equals("org.hibernate.dialect.MySQL5InnoDBDialect")) {
				res = getSession(name).createSQLQuery("SELECT LAST_INSERT_ID()").uniqueResult();
			}  else {
				return;
			}
			values.put(keyOutput, ((Number)res).intValue());
		}
		
		if (Stats.iterate()) {
			Cronometro.start("DATABASE");
			commit(name);
			begin(name);
			Cronometro.stop("DATABASE");
		}
		
		Stats.printSpeed();
	}
	
	public void script(String name, String text) {
		String[] cmd = text.split(";\\s*\n");
		for (int i = 0; i < cmd.length; i++) {
			if (cmd[i].trim().length() > 0) {
				getSession(name).createSQLQuery(cmd[i].trim()).executeUpdate();
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ConnectionInfo createConnection(String name, String driverClassName, String url, String password, String username, String dialect) {
		Map properties = new HashMap();
		properties.put("hibernate.connection.driver_class", driverClassName);
		properties.put("hibernate.connection.url", url);
		properties.put("hibernate.connection.username", username);
		properties.put("hibernate.connection.password", password);
		//properties.put("javax.persistence.query.timeout", 10*60*1000);
		//properties.put("javax.persistence.lock.timeout", 10*60*1000);
		//properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL5InnoDBDialect");
		properties.put("hibernate.dialect", dialect);
		properties.put("hibernate.show-sql", "true");
		//emf = Persistence.createEntityManagerFactory("jpablogPUnit");
		EntityManagerFactory emf = new HibernatePersistence().createEntityManagerFactory("main", properties);
		EntityManager em = (EntityManager) emf.createEntityManager();
		
		ConnectionInfo con = new ConnectionInfo(emf, em, null, dialect);
		if (StringUtils.isEmpty(name)) {
			name = "default";
		}
		
		connectionMap.put(name, con);
		readSchemaDefinitions(null);
		
    	return con;
	}
	
	public void closeConnection(String name) {
		getSession(name).close();
	}

	public void begin(String name) {
		if (StringUtils.isEmpty(name)) {
			name = "default";
		}
		connectionMap.get(name).setTransaction(getSession(name).beginTransaction());
	}

	public void commit(String name) {
		if (StringUtils.isEmpty(name)) {
			name = "default";
		}
		connectionMap.get(name).getTransaction().commit();;
	}
	
	public void rollback(String name) {
		if (StringUtils.isEmpty(name)) {
			name = "default";
		}
		connectionMap.get(name).getTransaction().rollback();
	}
}
