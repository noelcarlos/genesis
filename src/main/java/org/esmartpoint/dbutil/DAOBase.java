package org.esmartpoint.dbutil;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.esmartpoint.dbutil.db.metadata.FieldInfo;
import org.esmartpoint.dbutil.db.metadata.ForeingKeyInfo;
import org.esmartpoint.dbutil.db.metadata.IndexInfo;
import org.esmartpoint.dbutil.db.metadata.MetaDataInfo;
import org.esmartpoint.dbutil.db.metadata.TableInfo;
import org.esmartpoint.dbutil.db.metadata.TableInfoPK;

public class DAOBase {
	private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(DAOBase.class);
	private static ThreadLocal<Connection> conn = new ThreadLocal<Connection>();
	private static boolean cacheFullMetadata = false;
	private static String dialect = "";
	public final static String DIALECT_POSTGRES = "postgres";
	public final static String DIALECT_MYSQL = "mysql";
	private static String schemaName = null;
	
	private DAOBase() {
	}
	
	static public void beginTransaction(String db) throws SQLException {
		Context init;
		try {
			init = new InitialContext();
			DataSource ds = (DataSource) init.lookup(db);
			conn.set(ds.getConnection());
			getConnection().setAutoCommit(false);
			init.close();
			if (getMetadata().getTables().size() == 0 && isCacheFullMetadata()) {
				if (dialect.equals(DIALECT_MYSQL))
					cacheAllMetadata(null);
				else
					cacheAllMetadata("public");
			}
		} catch (NamingException e) {
			throw new SQLException("error.namingcontext", e);
		}
	}
	
	static public void initConnection(Connection con) throws SQLException {
		conn.set(con);
		if (getMetadata().getTables().size() == 0 && isCacheFullMetadata()) {
			if (dialect.equals(DIALECT_MYSQL))
				cacheAllMetadata(null);
			else
				cacheAllMetadata("public");
		}
	}
	
	static public void cacheAllMetadata(String schemaPattern) throws SQLException {
		if (getMetadata().getTables().size() == 0)
			leerMetaData(getMetadata(), schemaPattern, "%");
	}
	
	static public void beginTransaction() throws SQLException {
		if (getMetadata().getTables().size() == 0 && isCacheFullMetadata()) {
			if (dialect.equals(DIALECT_MYSQL))
				cacheAllMetadata(null);
			else
				cacheAllMetadata("public");
		}
		getConnection().setAutoCommit(false);
	}
	
	static public void commit() throws SQLException {
		getConnection().commit();
	}
	
	static public void rollback() {
		try {
			getConnection().rollback();
		} catch (SQLException e) {
			logger.error("Error during rollback", e);
		}
	}
	
	static public Connection openConnection() throws SQLException {
		return conn.get();
	}
	
	public static NamedParameterStatement createQuery(String sql) throws SQLException {
		openConnection();
		NamedParameterStatement nps = new NamedParameterStatement(getConnection(), sql);
		return nps;
	}

	static Document doc = null;
	
	static public Document getNamedQueriesDocument() {
		return doc;
	}
	
	static public Document initializeFromResource(String resourceName) throws SQLException {
		if (doc == null) {
			try {
				doc = DocumentHelper.parseText(Utility.loadTextResource(resourceName));
			} catch (DocumentException e) {
				throw new SQLException("Document Exception:" + e);
			} catch (IOException e) {
				throw new SQLException("IO Exception:" + e);
			}
		}
		return doc;
	}
	
	static public Document loadNamedQueryResources(String resourceName) throws SQLException {
		try {
			if (ResourceLoaderService.reloadingRequired(resourceName)) {
				doc = DocumentHelper.parseText(ResourceLoaderService.loadTextFile(resourceName));
			}
			return doc;
		} catch (DocumentException e) {
			throw new SQLException("Document Exception:" + e);
		} catch (IOException e) {
			throw new SQLException("IO Exception:" + e);
		}
	}

	public static String loadNamedQuery(String queryName) throws SQLException {
		Node n = doc.selectSingleNode("//queries/query[@name='" + queryName + 
				"' and (not (@dialect) or @dialect='" + dialect + "')]");
		if (n == null)
			throw new SQLException("Can't find namedQuery:" + queryName);
		return n.getText();
	}
	
	public static String loadNamedESMapping(String mappingName) throws SQLException {
		Node n = doc.selectSingleNode("//queries/esMapping[@name='" + mappingName + 
				"' and (not (@dialect) or @dialect='" + dialect + "')]");
		if (n == null)
			throw new SQLException("Can't find esMapping:" + mappingName);
		return n.getText();
	}
	
	public static NamedParameterStatement createNamedQuery(String queryName) throws SQLException {
		openConnection();
		String sql = loadNamedQuery(queryName); 
		logger.debug("NamedQuery [" + queryName + "]");
		NamedParameterStatement nps = new NamedParameterStatement(getConnection(), sql);
		return nps;
	}
	
	static public void closeTransaction() {
		try {
			if (getConnection() != null) {
				getConnection().close();
			}
			conn.remove();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	static public Connection getConnection() {
		return conn.get();
	}
	
	private static MetaDataInfo metadata = new MetaDataInfo();
	
	static public TableInfo getTableInfo(String tableName) throws SQLException {
		if (getMetadata().getTables().get(tableName) == null)
			leerMetaData(getMetadata(), getSchemaName(), tableName);
		return getMetadata().getTables().get(tableName);
	}	
	
	// tableNamePattern = "%"
	static public void leerMetaData(MetaDataInfo tables, String schemaPattern, String tableNamePattern) throws SQLException {
    	Cronometro.start("es.afu.util.NamedParameterStatement.DBQUERY");
		DatabaseMetaData md = openConnection().getMetaData();
		
		ResultSet rs = md.getTables(null, schemaPattern, tableNamePattern, new String[] {"TABLE"});
		
		while (rs.next()) {
			TableInfo hm = new TableInfo();
			hm.setCatalog(rs.getString("TABLE_CAT"));
			hm.setSchema(rs.getString("TABLE_SCHEM"));
			hm.setName(rs.getString("TABLE_NAME"));
			hm.setType(rs.getString("TABLE_TYPE"));
			hm.setRemarks(rs.getString("REMARKS"));
		
			ResultSet rsPks = md.getPrimaryKeys(rs.getString("TABLE_CAT"), 
					rs.getString("TABLE_SCHEM"), rs.getString("TABLE_NAME"));
			HashMap<String, Integer> hmPks = new HashMap<String, Integer>();
			while (rsPks.next()) {
				hmPks.put(rsPks.getString("COLUMN_NAME"), rsPks.getInt("KEY_SEQ"));
				TableInfoPK e = new TableInfoPK();
				e.setColumnName(rsPks.getString("COLUMN_NAME"));
				e.setKeySeq(rsPks.getInt("KEY_SEQ"));
				hm.getPkFields().add(e );
			}
			rsPks.close();
			
			ResultSet rsNdxs = md.getIndexInfo(rs.getString("TABLE_CAT"), 
					rs.getString("TABLE_SCHEM"), rs.getString("TABLE_NAME"), false, true);
			
			HashMap<String, IndexInfo> ndxsMap = new HashMap<String, IndexInfo>();
			HashMap<String, List<IndexInfo>> indexByName = new HashMap<String, List<IndexInfo>>();
			
			while (rsNdxs.next()) {
				IndexInfo hmFks = new IndexInfo();
				hmFks.setCatalog(rsNdxs.getString("TABLE_CAT"));
				hmFks.setSchema(rsNdxs.getString("TABLE_SCHEM"));
				hmFks.setTable(rsNdxs.getString("TABLE_NAME"));
				hmFks.setNonUnique(rsNdxs.getBoolean("NON_UNIQUE"));
				hmFks.setIndexQualifier(rsNdxs.getString("INDEX_QUALIFIER"));
				hmFks.setIndexName(rsNdxs.getString("INDEX_NAME"));
				hmFks.setType(rsNdxs.getInt("TYPE"));
				hmFks.setOrdinalPosition(rsNdxs.getInt("ORDINAL_POSITION"));
				hmFks.setColumnName(rsNdxs.getString("COLUMN_NAME"));
				hmFks.setAscending((rsNdxs.getString("ASC_OR_DESC") == null) ? null : 
					(rsNdxs.getString("ASC_OR_DESC").equals("A") ? true : false));
				hmFks.setCardinality(rsNdxs.getInt("CARDINALITY"));
				hmFks.setPages(rsNdxs.getInt("PAGES"));
				hmFks.setFilterCondition(rsNdxs.getString("FILTER_CONDITION"));
				
				ndxsMap.put(rsNdxs.getString("COLUMN_NAME"), hmFks);
				
				List<IndexInfo> fkList;
				String ndxName = rsNdxs.getString("INDEX_NAME");
				if (indexByName.containsKey(ndxName)) 
					fkList = (List<IndexInfo>)indexByName.get(ndxName);
				else {
					fkList = new ArrayList<IndexInfo>();
					indexByName.put(ndxName, fkList);		
				}
				fkList.add(hmFks);
			}
			rsNdxs.close();
			hm.setIndexNameMap(indexByName);
			
			ResultSet rsFks = md.getImportedKeys(rs.getString("TABLE_CAT"), 
				rs.getString("TABLE_SCHEM"), rs.getString("TABLE_NAME"));
			
			HashMap<String, ForeingKeyInfo> fksFieldMap = new HashMap<String, ForeingKeyInfo>();
			HashMap<String, List<ForeingKeyInfo>> foreignKeyByNameMap = new HashMap<String, List<ForeingKeyInfo>>();
			HashMap<String, List<ForeingKeyInfo>> foreignKeyByTableNameMap = new HashMap<String, List<ForeingKeyInfo>>();
			while (rsFks.next()) {
				ForeingKeyInfo hmFks = new ForeingKeyInfo();
				hmFks.setCatalog(rsFks.getString("PKTABLE_CAT"));
				hmFks.setSchema(rsFks.getString("PKTABLE_SCHEM"));
				hmFks.setTable(rsFks.getString("PKTABLE_NAME"));
				hmFks.setColumn(rsFks.getString("PKCOLUMN_NAME"));
				hmFks.setSequence(rsFks.getString("KEY_SEQ"));
				hmFks.setUpdateRule(rsFks.getInt("UPDATE_RULE"));
				hmFks.setDeleteRule(rsFks.getInt("DELETE_RULE"));
				hmFks.setForeignKeyName(rsFks.getString("FK_NAME"));
				hmFks.setPrimaryKeyName(rsFks.getString("PK_NAME"));
				hmFks.setForeignKeyColumnName(rsFks.getString("FKCOLUMN_NAME"));
				
				fksFieldMap.put(rsFks.getString("FKCOLUMN_NAME"), hmFks);
				
				List<ForeingKeyInfo> fkList;
				String keyFKName = rsFks.getString("FK_NAME");
				if (foreignKeyByNameMap.containsKey(keyFKName)) 
					fkList = (List<ForeingKeyInfo>)foreignKeyByNameMap.get(keyFKName);
				else {
					fkList = new ArrayList<ForeingKeyInfo>();
					foreignKeyByNameMap.put(keyFKName, fkList);
				}
				fkList.add(hmFks);
				
				keyFKName = rsFks.getString("PKTABLE_NAME");
				if (foreignKeyByTableNameMap.containsKey(keyFKName)) 
					fkList = (List<ForeingKeyInfo>)foreignKeyByTableNameMap.get(keyFKName);
				else {
					fkList = new ArrayList<ForeingKeyInfo>();
					foreignKeyByTableNameMap.put(keyFKName, fkList);
				}
				fkList.add(hmFks);
			}
			rsFks.close();
			hm.setForeingKeyByNameMap(foreignKeyByNameMap);
			hm.setForeingKeyByTableNameMap(foreignKeyByTableNameMap);
			
//			ArrayList<Record> fields = new ArrayList<Record>();
//			Record fieldsMap = new Record();
//			
			ResultSet rsCol = md.getColumns(rs.getString("TABLE_CAT"), 
					rs.getString("TABLE_SCHEM"), rs.getString("TABLE_NAME"), "%");
			while (rsCol.next()) {
				FieldInfo hmCol = new FieldInfo();
				String colName = rsCol.getString("COLUMN_NAME");
				hmCol.setName(colName);
				hmCol.setPropertyName(Record.mapToCamelCase(colName));
				hmCol.setType(rsCol.getInt("DATA_TYPE"));
				hmCol.setTypeName(rsCol.getString("TYPE_NAME"));
				hmCol.setRemarks(rsCol.getString("REMARKS"));
				hmCol.setDefaultValue(rsCol.getString("COLUMN_DEF"));
				hmCol.setSize(rsCol.getInt("COLUMN_SIZE"));
				hmCol.setDecimals(rsCol.getInt("DECIMAL_DIGITS"));
				hmCol.setNullable(rsCol.getString("IS_NULLABLE").equals("YES"));
				if (hmPks.containsKey(rsCol.getString("COLUMN_NAME"))) {
					hmCol.setPK(true);
					hmCol.setSequencePK(hmPks.get(rsCol.getString("COLUMN_NAME")));
					if (hm.getPrimaryKey() != null)
						hm.setPrimaryKey(null);
					else
						hm.setPrimaryKey(rsCol.getString("COLUMN_NAME"));
				} else {
					hmCol.setPK(false);
				}
				if (fksFieldMap.containsKey(colName)) {
					hmCol.setFK(true);
					hmCol.setForeingKey(fksFieldMap.get(colName));
				} else {
					hmCol.setFK(false);
				}
				if (ndxsMap.containsKey(colName)) {
					hmCol.setIndex(true);
					hmCol.setIndexInfo(ndxsMap.get(colName));
				} else {
					hmCol.setIndex(false);
				}
				hm.getFields().add(hmCol);
				hm.getFieldsMap().put(colName, hmCol);
			}
			rsCol.close();
			tables.getTables().put(hm.getName(), hm);
		}
		rs.close();
		Cronometro.stop("es.afu.util.NamedParameterStatement.DBQUERY");
	}
	
	static public Record load(String entityName, Integer id) throws SQLException {
		TableInfo info = getTableInfo(entityName);
		return DAOBase.createQuery("SELECT * FROM " + entityName + 
				" WHERE " + info.getPrimaryKey() + "=:" + info.getPrimaryKey())
			.setParameter(info.getPrimaryKey().toString(), id)
			.uniqueResult();
	}
	
	static public List<Record> list(String entityName) throws SQLException {
		return DAOBase.createQuery("SELECT * FROM " + entityName)
			.list();
	}
	
	static public List<Record> list(String entityName, String key, Integer id) throws SQLException {
		return DAOBase.createQuery("SELECT * FROM " + entityName + " WHERE " + key + "=:id")
			.setParameter("id", id)
			.list();
	}
	
	static public void delete(String entityName, Integer id) throws SQLException {
		TableInfo info = getTableInfo(entityName);
		DAOBase.createQuery("DELETE FROM " + entityName + 
				" WHERE " + info.getPrimaryKey() + "=:" + info.getPrimaryKey())
			.setParameter(info.getPrimaryKey().toString(), id)
			.executeUpdate();
	}
	
	//TODO: Al guardar un registro con llave primaria multiple o que no sea autonumerica, intenta hacer un UPDATE en lugar de insert
	//Hay que detectar si el campo llave es autonumerico o no, en caso de que no sea autonumerico no se puede generar, por lo que es 
	//Obligatorio como campo de entrada. ¿Como se detecta entonces si es insert o update si antes se hacia cuando eran nulls las 
	//llaves y ahora no pueden serlo?
	static public Integer save(String entityName, Record r) throws SQLException {
		NamedParameterStatement q;
		StringBuilder sql;
		
		Record record = r.mapFromCamelCase();
		
		TableInfo info = getTableInfo(entityName);
		Integer id = null;
		if (info.getPrimaryKey() != null && record.containsKey(info.getPrimaryKey()))
			id = (Integer)record.get(info.getPrimaryKey());
		List<FieldInfo> fields = (List<FieldInfo>)info.getFields();
		
		if (id == null) {
			sql = new StringBuilder("INSERT INTO " + entityName + "(");
			boolean isFirst = true;
			for(int i = 0; i < fields.size(); i++) { 
				//if (fields.get(i).get("isPK").equals(false)) {
					if (record.containsKey(fields.get(i).getName().toString())) {
						if (!isFirst)
							sql.append(",");
						sql.append(fields.get(i).getName());
						isFirst = false;
					}
				//}
			}
			sql.append(") VALUES (");
			isFirst = true;
			for(int i = 0; i < fields.size(); i++) {
				//if (fields.get(i).get("isPK").equals(false)) {
					if (record.containsKey(fields.get(i).getName())) {
						if (!isFirst)
							sql.append(",");
						sql.append(":" + fields.get(i).getName());
						isFirst = false;
					} 
				//}
			}
			sql.append(")");
		} else {
			sql = new StringBuilder("UPDATE " + entityName + " SET ");
			boolean isFirst = true;
			for(int i = 0; i < fields.size(); i++) {
				if (!fields.get(i).isPK()) {
					if (record.containsKey(fields.get(i).getName())) {
						if (!isFirst)
							sql.append(",");
						sql.append(fields.get(i).getName() + "=:" + fields.get(i).getName());
						isFirst = false;
					}
				}
			}
			sql.append(" WHERE " + info.getPrimaryKey() + "=:" + info.getPrimaryKey());
		}
		q = DAOBase.createQuery(sql.toString());
		for(int i = 0; i < fields.size(); i++) 
			//if (id == null || (id != null && fields.get(i).get("isPK").equals(false))) 
				if (record.containsKey(fields.get(i).getName()))
					q.setParameter(fields.get(i).getName(), 
						record.get(fields.get(i).getName()));
		q.executeUpdate();
		if (id == null) 
			return q.getGeneratedKey();
		else
			return id;
	}
	
	static private String buildDetail(String entityName) throws SQLException {
		StringBuilder selectPart = new StringBuilder();
		StringBuilder fromPart = new StringBuilder();
		return buildDetail(entityName, selectPart, fromPart);
	}
	
	static private String buildDetail(String entityName, StringBuilder selectPart, StringBuilder fromPart) throws SQLException {
		TableInfo info = getTableInfo(entityName);
		selectPart.append("this.*");
		fromPart.append(entityName + " this");
		
		List<FieldInfo> fields = info.getFields();
		for(int i = 0; i < fields.size(); i++) 
			if (fields.get(i).isFK()) {
				ForeingKeyInfo fkInfo = fields.get(i).getForeingKey();
				TableInfo frgnInfo = getTableInfo(fkInfo.getTable());
				List<FieldInfo> frgnFields = frgnInfo.getFields();
				//String frgnTableName = "fk" + i;
				String frgnTableName = (String)fields.get(i).getName();
				if (frgnTableName.endsWith("_id"))
					frgnTableName = frgnTableName.substring(0, frgnTableName.length() - 3); 
				for (int j = 0; j < frgnFields.size(); j++)
					if (!frgnFields.get(j).isPK()) {
						String fName = frgnFields.get(j).getName();
						selectPart.append(",")
							.append(frgnTableName)
							.append(".")
							.append(frgnFields.get(j).getName())
							.append(" AS ")
							.append(frgnTableName)
							.append('_')
							.append(fName);
					}
				
				fromPart.append(" LEFT JOIN ").append(fkInfo.getTable())
					.append(" ")
					.append(frgnTableName)
					.append(" ON (")
					.append("this." + fields.get(i).getName())
					.append(" = ")
					.append(frgnTableName + "." + frgnInfo.getPrimaryKey())
					.append(")");
			}
		return "SELECT " + selectPart + " FROM " + fromPart;
	}

	static public Record loadDetail(String entityName, Integer id) throws SQLException {
		TableInfo info = getTableInfo(entityName);
		return DAOBase.createQuery(buildDetail(entityName) + 
				" WHERE this." + info.getPrimaryKey() + "=:" + info.getPrimaryKey())
			.setParameter(info.getPrimaryKey(), id)
			.uniqueResult();
	}

	static public List<Record> listDetail(String entityName, Integer id) throws SQLException {
		TableInfo info = getTableInfo(entityName);
		return DAOBase.createQuery(buildDetail(entityName) + 
				" WHERE this." + info.getPrimaryKey() + "=:" + info.getPrimaryKey())
				.setParameter(info.getPrimaryKey(), id)
			.list();
	}
	
	static public Criteria createCriteriaDetail(String entityName) throws SQLException {
		StringBuilder selectPart = new StringBuilder();
		StringBuilder fromPart = new StringBuilder();
		buildDetail(entityName, selectPart, fromPart);
		Criteria c = new Criteria(selectPart.toString(), fromPart.toString());
		c.setRootEntity(entityName);
		return c;
	}
	
	static public Criteria createCriteria(String entityName) throws SQLException {
		return new Criteria(entityName);
	}

	static public Criteria createNamedCriteria(String namedCriteria) throws SQLException {
		return new Criteria(DAOBase.getConnection(), namedCriteria);
	}

	public static void setConnection(Connection conn) {
		DAOBase.conn.set(conn); 
	}

	public static void setCacheFullMetadata(boolean cacheFullMetadata) {
		DAOBase.cacheFullMetadata = cacheFullMetadata;
	}

	public static boolean isCacheFullMetadata() {
		return cacheFullMetadata;
	}

	public static void setDialect(String dialect) {
		DAOBase.dialect = dialect;
	}

	public static String getDialect() {
		return dialect;
	}

	public static void setMetadata(MetaDataInfo metadata) {
		DAOBase.metadata = metadata;
	}

	public static MetaDataInfo getMetadata() {
		return metadata;
	}

	public static void setSchemaName(String schemaName) {
		DAOBase.schemaName = schemaName;
	}

	public static String getSchemaName() {
		return schemaName;
	}
	
}
