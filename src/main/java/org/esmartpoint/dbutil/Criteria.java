package org.esmartpoint.dbutil;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.dom4j.Document;
import org.dom4j.Node;
import org.esmartpoint.dbutil.db.metadata.FieldInfo;
import org.esmartpoint.dbutil.db.metadata.ForeingKeyInfo;
import org.esmartpoint.dbutil.db.metadata.TableInfo;

public class Criteria {
	StringBuilder selectPart;
	StringBuilder selectCountPart;
	StringBuilder fromPart;
	StringBuilder fromCountPart;
	private String rootEntity;
	ArrayList<String> criteriaList = new ArrayList<String>();
	ArrayList<Criteria> unionList = new ArrayList<Criteria>();
	Record parameters = new Record();
	Record collectMap = new Record();
	String orderPart = "";
	String groupPart = "";
	Connection con;
	Integer firstResult;
	Integer maxResults;
	
	public Criteria(String selectPart, String fromPart) throws SQLException {
		this.fromPart = new StringBuilder(fromPart);
		this.setRootEntity(fromPart);
		this.selectPart = new StringBuilder(selectPart);
		this.con = DAOBase.openConnection();
	}
	
	Criteria(String entityName) throws SQLException {
		selectPart = new StringBuilder("this.*");
		setRootEntity(entityName);
		fromPart = new StringBuilder(entityName + " this");
		this.con = DAOBase.openConnection();
	}
	
	static Document doc = null;
	
	public Criteria(Connection con, String namedCriteria) throws SQLException {
		doc = DAOBase.getNamedQueriesDocument();
		Node n = doc.selectSingleNode("//queries/criteria[@name='" + namedCriteria + "']");
		if (n == null)
			throw new SQLException("Can't find namedCriteria:" + namedCriteria);
		this.selectPart = new StringBuilder(n.valueOf("select"));
		this.fromPart = new StringBuilder(n.valueOf("from"));
		this.setRootEntity(fromPart.toString());
		this.selectCountPart = new StringBuilder(n.valueOf("selectCount"));
		this.fromCountPart = new StringBuilder(n.valueOf("fromCount"));
		this.con = con;
	}
	
	static public Criteria load(String namedCriteria) throws SQLException {
		return new Criteria(DAOBase.openConnection(), namedCriteria);
	}
	
	public String getSelectPart() {
		return selectPart.toString();
	}

	public void setSelectPart(String selectPart) {
		this.selectPart = new StringBuilder(selectPart);
	}

	public String getSelectCountPart() {
		return selectCountPart.toString();
	}

	public void setSelectCountPart(String selectCountPart) {
		this.selectCountPart = new StringBuilder(selectCountPart);
	}

	public String getFromPart() {
		return fromPart.toString();
	}

	public void setFromPart(String fromPart) {
		this.fromPart = new StringBuilder(fromPart);
	}

	public String getFromCountPart() {
		return fromCountPart.toString();
	}

	public void setFromCountPart(String fromCountPart) {
		this.fromCountPart = new StringBuilder(fromCountPart);
	}

	public ArrayList<Criteria> getUnionList() {
		return unionList;
	}

	public String getOrderPart() {
		return orderPart;
	}
	
	public String getGroupPart() {
		return groupPart;
	}

	public Criteria collect(String alias) {
		collectMap.put(alias, "*");
		return this;
	}
	
	public Criteria addCondition(String condition) {
		criteriaList.add(condition);
		return this;
	}

	public Criteria addUnion(Criteria c) {
		unionList.add(c);
		return this;
	}
	
	public Criteria addOrder(String sortField, boolean ascending) {
		if (orderPart.length() != 0)
			orderPart += ",";
		orderPart += sortField + ((ascending) ? " ASC" : " DESC");
		return this;
	}
	
	public Criteria addGroup(String groupField) {
		if (groupPart.length() != 0)
			groupPart += ",";
		groupPart += groupField;
		return this;
	}
	
	public Criteria setParameter(String name, Object value) {
		parameters.put(name, value);
		return this;
	}
	
	public Criteria setFirstResult(Integer firstResult) {
		this.firstResult = firstResult;
		return this;
	}
	
	public Criteria setMaxResults(Integer maxResults) {
		this.maxResults = maxResults;
		return this;
	}
	
	public Criteria leftJoin(String field) throws SQLException {
		return buildJoin(null, "LEFT JOIN", "this", field, null, false);
	}
	
	public Criteria leftJoin(String field, String alias) throws SQLException {
		return buildJoin(null, "LEFT JOIN", "this", field, alias, false);
	}
	
	public Criteria customJoin(String joinType, String source, String field, String alias) throws SQLException {
		return buildJoin(null, joinType + " JOIN", source, field, alias, false);
	}
	
	public Criteria rightJoin(String field) throws SQLException {
		return buildJoin(null, "RIGHT JOIN", "this", field, null, false);
	}
	
	public Criteria rightJoin(String field, String alias) throws SQLException {
		return buildJoin(null, "RIGHT JOIN", "this", field, alias, false);
	}	
	
	public Criteria innerJoin(String field) throws SQLException {
		return buildJoin(null, "INNER JOIN", "this", field, null, false);
	}
	
	public Criteria innerJoin(String field, String alias) throws SQLException {
		return buildJoin(null, "INNER JOIN", "this", field, alias, false);
	}
	
	public Criteria leftJoinMap(String field) throws SQLException {
		return buildJoin(null, "LEFT JOIN", "this", field, null, true);
	}
	
	public Criteria leftJoinMap(String field, String alias) throws SQLException {
		return buildJoin(null, "LEFT JOIN", "this", field, alias, true);
	}
	
	public Criteria customJoinMap(String joinType, String source, String field, String alias) throws SQLException {
		return buildJoin(null, joinType + " JOIN", source, field, alias, true);
	}
	
	public Criteria rightJoinMap(String field) throws SQLException {
		return buildJoin(null, "RIGHT JOIN", "this", field, null, true);
	}
	
	public Criteria rightJoinMap(String field, String alias) throws SQLException {
		return buildJoin(null, "RIGHT JOIN", "this", field, alias, true);
	}	
	
	public Criteria innerJoinMap(String field) throws SQLException {
		return buildJoin(null, "INNER JOIN", "this", field, null, true);
	}
	
	public Criteria innerJoinMap(String field, String alias) throws SQLException {
		return buildJoin(null, "INNER JOIN", "this", field, alias, true);
	}
	
	public Criteria joinAll() throws SQLException {
		return buildJoin(null, "LEFT JOIN", "this", null, null, false);
	}
	
	public Criteria joinAllMap() throws SQLException {
		return buildJoin(null, "LEFT JOIN", "this", null, null, true);
	}
	
	public Criteria buildJoin(TableInfo rootInfoTable, String joinType, String sourceAlias, 
			String field, String alias, boolean map) throws SQLException {
		TableInfo info = (rootInfoTable == null) ? DAOBase.getTableInfo(getRootEntity()) : rootInfoTable;
		List<FieldInfo> fields = info.getFields();
		for(int i = 0; i < fields.size(); i++) {
			if (fields.get(i).isFK() && (field == null || field.equals(fields.get(i).getName()))) {
				ForeingKeyInfo fkInfo = fields.get(i).getForeingKey();
				TableInfo frgnInfo = DAOBase.getTableInfo(fkInfo.getTable());
				List<FieldInfo> frgnFields = frgnInfo.getFields();
				String frgnTableName = alias;
				if (alias == null)
					frgnTableName = fields.get(i).getName();
				if (rootInfoTable == null && frgnTableName.equals(sourceAlias)) {
					if (field.endsWith("_id"))
						field = field.substring(0, field.length() - 3); 
					return buildJoin(frgnInfo, joinType, field, sourceAlias, alias, map);
				}
				if (frgnTableName.endsWith("_id"))
					frgnTableName = frgnTableName.substring(0, frgnTableName.length() - 3); 
				if (map) {
					for (int j = 0; j < frgnFields.size(); j++) {
						if (!frgnFields.get(j).isPK()) {
							String fName = frgnFields.get(j).getName();
							selectPart.append(",")
								.append(frgnTableName)
								.append(".")
								.append(frgnFields.get(j).getName())
								.append(" AS ");
							selectPart.append(frgnTableName);
							selectPart.append('_');
							selectPart.append(fName);
						}
					}
				}
				fromPart.append(" ").append(joinType).append(" ")
					.append(fkInfo.getTable())
					.append(" ")
					.append(frgnTableName)
					.append(" ON (");
				if (sourceAlias != null)
					fromPart.append(sourceAlias).append('.');
				else
					fromPart.append("this.");
				fromPart.append(fields.get(i).getName())
					.append(" = ")
					.append(frgnTableName + "." + frgnInfo.getPrimaryKey())
					.append(")");
				if (field != null)
					return this;
			}
		}
		if (field != null)
			throw new SQLException("join path not found:" + field + " on table:" + getRootEntity());
		return this;
	}
	
	protected StringBuilder buildQuery() throws SQLException {
		StringBuilder query = new StringBuilder("SELECT ");
		query.append(selectPart);
		query.append(" FROM ");
		query.append(fromPart);
		query.append(buildQuery(criteriaList));
		if (groupPart.length() != 0)
			query.append(" GROUP BY " + groupPart);
		
		for(Iterator<Criteria> iter = unionList.iterator(); iter.hasNext();) {
			Criteria c = iter.next();
			query.append(" UNION " + c.buildQuery());
		}
			
		if (orderPart.length() != 0)
			query.append(" ORDER BY " + orderPart);
		if (maxResults != null)
			query.append(" LIMIT " + maxResults);
		if (firstResult != null)
			query.append(" OFFSET " + firstResult);
		
		return query;
	}
	
	protected StringBuilder buildAggregateQuery(String aggregateFunction, String argument) throws SQLException {
		StringBuilder query;
		
		if (unionList.size() > 0)
			query = new StringBuilder("SELECT " + aggregateFunction + "(" + argument + ") FROM (SELECT ");
		else
			query = new StringBuilder("SELECT ");

		if (selectCountPart != null && selectCountPart.length() > 0) 
			query.append(selectCountPart);
		else {
			if (unionList.size() > 0) 
				query.append(selectPart);
			else {
				if (groupPart.length() != 0) {
					if (DAOBase.getDialect().equals("mysql"))
						query.append(aggregateFunction + "(DISTINCT " + groupPart + ")");
					else
						query.append(aggregateFunction + "(DISTINCT (" + groupPart + "))");
				} else 
					query.append(aggregateFunction + "(" + argument + ")");
			}
		} 
		query.append(" FROM ");
		if (fromCountPart != null && fromCountPart.length() > 0)
			query.append(fromCountPart);
		else
			query.append(fromPart);
		query.append(buildQuery(criteriaList));
		
		for(Iterator<Criteria> iter = unionList.iterator(); iter.hasNext();) {
			Criteria c = iter.next();
			query.append(" UNION " + c.buildQuery());
		}
			
		if (unionList.size() > 0)
			query.append(" ) todo ");
		
		return query;
	}
	
	protected StringBuilder buildHasMoreThanQuery(int boundary) throws SQLException {
		StringBuilder query = new StringBuilder("SELECT COUNT(1) FROM (SELECT 1 FROM ");
		if (fromCountPart != null && fromCountPart.length() > 0)
			query.append(fromCountPart);
		else
			query.append(fromPart);
		query.append(buildQuery(criteriaList));
		query.append(" LIMIT " + 1 + " OFFSET " + boundary + " ) todo ");
		return query;
	}
	
	public List<Record> list() throws SQLException {
		StringBuilder query = buildQuery();
		//System.out.println(query.toString());
		NamedParameterStatement nps = new NamedParameterStatement(con, query.toString());
		setAllParameters(nps);
		try {
			return nps.list();
		} finally {
			nps.close();
		}
	}
	
	private List<Record> sublistAsTree(List<Record> original, Integer id) {
		List<Record> res = new ArrayList<Record>();
		for(int i = 0; i < original.size(); i++) {
			Record r = original.get(i);
			if (r.getInt("parentId") == id) {
				r.put("children", sublistAsTree(original, r.getInt("id")));
				res.add(r);			
			}
		}
		return res;
	}
	
	public List<Record> listAsTree(Integer id) throws SQLException {
		List<Record> original = list();
		return sublistAsTree(original, id);
	}
	
	public List<Record> listAsTree() throws SQLException {
		List<Record> original = list();
		return sublistAsTree(original, null);
	}
	
	public Record uniqueResult() throws SQLException {
		List<Record> res = list();
		if (res.size() == 0)
			return null;
		else
			return res.get(0);
	}
	
	private void setAllParameters(NamedParameterStatement nps) throws SQLException {
		for (Iterator<Entry<Object, Object>> iterator = parameters.entrySet().iterator(); iterator.hasNext();) {
			Entry<Object, Object> elemento = iterator.next();
			nps.setParameter(elemento.getKey().toString(), elemento.getValue());
		}

		for(Iterator<Criteria> iter = unionList.iterator(); iter.hasNext();) {
			Criteria c = iter.next();
			c.setAllParameters(nps);
		}
	}
	
	public Integer count(String field) throws SQLException {
		StringBuilder query = buildAggregateQuery("COUNT", field);
		NamedParameterStatement nps = new NamedParameterStatement(con, query.toString());
		setAllParameters(nps);
		try {
			return (nps.scalar()).intValue();
		} finally {
			nps.close();
		}
	}
	
	public Integer count() throws SQLException {
		return count("*");
	}
	
	public boolean hasMoreThan(int boundary) throws SQLException {
		StringBuilder query = buildHasMoreThanQuery(boundary);
		NamedParameterStatement nps = new NamedParameterStatement(con, query.toString());
		setAllParameters(nps);
		try {
			return (nps.scalar()).intValue() == 1;
		} finally {
			nps.close();
		}
	} 
			
	public Number sum(String field) throws SQLException {
		StringBuilder query = buildAggregateQuery("SUM", field);
		NamedParameterStatement nps = new NamedParameterStatement(con, query.toString());
		setAllParameters(nps);
		try {
			return nps.scalar();
		} finally {
			nps.close();
		}
	}	
	
	public Object min(String field) throws SQLException {
		StringBuilder query = buildAggregateQuery("MIN", field);
		NamedParameterStatement nps = new NamedParameterStatement(con, query.toString());
		setAllParameters(nps);
		try {
			return nps.singleElement();
		} finally {
			nps.close();
		}
	}	
	
	public Object max(String field) throws SQLException {
		StringBuilder query = buildAggregateQuery("MAX", field);
		NamedParameterStatement nps = new NamedParameterStatement(con, query.toString());
		setAllParameters(nps);
		try {
			return nps.singleElement();
		} finally {
			nps.close();
		}
	}
	
	public Integer delete() throws SQLException {
		StringBuilder query = new StringBuilder("DELETE FROM ");
		query.append(getRootEntity());
		query.append(buildQuery(criteriaList));
		NamedParameterStatement nps = new NamedParameterStatement(con, query.toString());
		setAllParameters(nps);
		try {
			return nps.executeUpdate();
		} finally {
			nps.close();
		}
	}
	
	public Integer save(Record rec) throws SQLException {
		TableInfo info = DAOBase.getTableInfo(getRootEntity());
		Integer id = null;
		if (info.getPrimaryKey() != null && rec.containsKey(info.getPrimaryKey()))
			id = (Integer)rec.get(info.getPrimaryKey());
		List<FieldInfo> fields = info.getFields();
		StringBuilder query = null;
		
		if (id == null && parameters.size() == 0) {
			return DAOBase.save(getRootEntity(), rec);
		} 
		
		Record record = rec.mapFromCamelCase();
		query = new StringBuilder("UPDATE ");
		query.append(getRootEntity());
		query.append(" SET ");
		
		boolean isFirst = true;
		for(int i = 0; i < fields.size(); i++) {
			if (record.containsKey(fields.get(i).getName())) {
				if (!isFirst)
					query.append(",");
				query.append(fields.get(i).getName() + "=:" + fields.get(i).getName());
				isFirst = false;
			}
		}
		
		query.append(buildQuery(criteriaList));			
		
		NamedParameterStatement nps = new NamedParameterStatement(con, query.toString());
		try {
			nps = DAOBase.createQuery(query.toString());
			for(int i = 0; i < fields.size(); i++) {
				String fn = fields.get(i).getName();
				if (id != null || !fields.get(i).isPK()) 
					if (record.containsKey(fn))
						if (!parameters.containsKey(fn))
							nps.setParameter(fn, record.get(fn));
			}
			setAllParameters(nps);
			Integer numreg = nps.executeUpdate();
			if (numreg == 0) {
				for (Iterator<?> iterator = parameters.keySet().iterator(); iterator.hasNext();) {
					String key = (String) iterator.next();
					rec.put(key, parameters.get(key));
				} 
				return DAOBase.save(getRootEntity(), rec);
			}
			if (id == null && parameters.size() == 0) 
				return nps.getGeneratedKey();
			else
				return id;
		} finally {
			nps.close();
		}
	}
	
	public Criteria addFilter(String fieldName, String operation, Criteria c) throws SQLException {
		addCondition(fieldName + " " + operation + " (" + c.buildQuery() + ")");
		for (Iterator<Entry<Object, Object>> iterator = c.parameters.entrySet().iterator(); iterator.hasNext();) {
			Entry<Object, Object> elemento = iterator.next();
			setParameter(elemento.getKey().toString(), elemento.getValue());
		}
		return this;
	}
	
	protected StringBuilder buildQuery(ArrayList<String> criteriaList) {
		StringBuilder res = new StringBuilder();
		for (Iterator<String> iter = criteriaList.iterator(); iter.hasNext();) {
			String str = iter.next();
			if (res.length() == 0) 
				res.append(" WHERE (").append(str).append(")");
			else
				res.append(" AND (").append(str).append(")");
		}
		return res;
	}
	
	public Criteria addFilter(String fieldName, Integer value) {
		if (value != null) {
			int p = fieldName.lastIndexOf('.');
			String fieldNamePart = (p == -1) ? fieldName : fieldName.substring(p + 1);
			fieldNamePart = fieldNamePart.replaceAll("\"", "");
			if (parameters.containsKey(fieldNamePart)) 
				fieldNamePart = fieldName.replace('.', '_');
			addCondition(fieldName + "=:" + fieldNamePart);
			setParameter(fieldNamePart, value);
		}
		return this;
	}
	
	public Criteria addFilter(String fieldName, String value) {
		if (value != null && value.trim().length() > 0) {
			int p = fieldName.lastIndexOf('.');
			String fieldNamePart = (p == -1) ? fieldName : fieldName.substring(p + 1);
			fieldNamePart = fieldNamePart.replaceAll("\"", "");
			if (parameters.containsKey(fieldNamePart)) 
				fieldNamePart = fieldName.replace('.', '_');
			addCondition(fieldName + " like concat('%', :" + fieldNamePart + ",'%')");
			setParameter(fieldNamePart, value.trim());
		}
		return this;
	}
	
	public Criteria addFilterExpression(String fieldName, String value) throws ParseException {
		if (value != null && value.trim().length() > 0) {
			int p = fieldName.lastIndexOf('.');
			String fieldNamePart = (p == -1) ? fieldName : fieldName.substring(p + 1);
			fieldNamePart = fieldNamePart.replaceAll("\"", "");
			if (parameters.containsKey(fieldNamePart)) 
				fieldNamePart = fieldName.replace('.', '_');

			value = value.trim();
			if (value.startsWith("=")) {
				addCondition(fieldName + " = :" + fieldNamePart);
				setParameter(fieldNamePart, Converter.toDouble(value.substring(1)));
			} else if (value.startsWith(">=")) {
				addCondition(fieldName + " >= :" + fieldNamePart);
				setParameter(fieldNamePart, Converter.toDouble(value.substring(2)));
			} else if (value.startsWith("<=")) {
				addCondition(fieldName + " <= :" + fieldNamePart);
				setParameter(fieldNamePart, Converter.toDouble(value.substring(2)));
			} else if (value.startsWith(">")) {
				addCondition(fieldName + " > :" + fieldNamePart);
				setParameter(fieldNamePart, Converter.toDouble(value.substring(1)));
			} else if (value.startsWith("<")) {
				addCondition(fieldName + " < :" + fieldNamePart);
				setParameter(fieldNamePart, Converter.toDouble(value.substring(1)));
			} else if (value.startsWith(">=")) {
				addCondition(fieldName + " >= :" + fieldNamePart);
				setParameter(fieldNamePart, Converter.toDouble(value.substring(2)));
			} else if (value.indexOf(' ') != -1) {
				addCondition(fieldName + " in :" + fieldNamePart);
				setParameter(fieldNamePart, Converter.toDouble(value.substring(2)));
			} else if (value.indexOf('-') > 0) {
				int p1 = value.indexOf('-', 1);
				String from = value.substring(0, p1);
				String to = value.substring(p1 + 1);
				addCondition(fieldName + " >= :" + fieldNamePart + "Desde");
				setParameter(fieldNamePart + "Desde", Converter.toDouble(from));
				addCondition(fieldName + " <= :" + fieldNamePart + "Hasta");
				setParameter(fieldNamePart + "Hasta", Converter.toDouble(to));
			} else {
				addCondition(fieldName + " = :" + fieldNamePart);
				setParameter(fieldNamePart, Converter.toDouble(value));
			}
		}
		return this;
	}

	public Criteria addFilter(String fieldName, Date desdeCreacion, Date hastaCreacion) {
		if (desdeCreacion != null) {
			int p = fieldName.lastIndexOf('.');
			String fieldNamePart = (p == -1) ? fieldName : fieldName.substring(p + 1);
			fieldNamePart = fieldNamePart.replaceAll("\"", "");
			if (parameters.containsKey(fieldNamePart)) 
				fieldNamePart = fieldName.replace('.', '_');
			addCondition(fieldName + " >= :" + fieldNamePart + "Desde");
			setParameter(fieldNamePart + "Desde", desdeCreacion);
		}
		if (hastaCreacion != null) {
			int p = fieldName.lastIndexOf('.');
			String fieldNamePart = (p == -1) ? fieldName : fieldName.substring(p + 1);
			fieldNamePart = fieldNamePart.replaceAll("\"", "");
			if (parameters.containsKey(fieldNamePart)) 
				fieldNamePart = fieldName.replace('.', '_');
			addCondition(" DATE("+fieldName + ") <= :" + fieldNamePart + "Hasta");
			setParameter(fieldNamePart + "Hasta", hastaCreacion);
		}
		return this;
	}
	
	public Criteria addFilter(String fieldName, Integer desde, Integer hasta) {
		if (desde != null) {
			int p = fieldName.lastIndexOf('.');
			String fieldNamePart = (p == -1) ? fieldName : fieldName.substring(p + 1);
			fieldNamePart = fieldNamePart.replaceAll("\"", "");
			if (parameters.containsKey(fieldNamePart)) 
				fieldNamePart = fieldName.replace('.', '_');
			addCondition(fieldName + " >= :" + fieldNamePart + "Desde");
			setParameter(fieldNamePart + "Desde", desde);
		}
		if (hasta != null) {
			int p = fieldName.lastIndexOf('.');
			String fieldNamePart = (p == -1) ? fieldName : fieldName.substring(p + 1);
			fieldNamePart = fieldNamePart.replaceAll("\"", "");
			if (parameters.containsKey(fieldNamePart)) 
				fieldNamePart = fieldName.replace('.', '_');
			addCondition(fieldName + " <= :" + fieldNamePart + "Hasta");
			setParameter(fieldNamePart + "Hasta", hasta);
		}
		return this;
	}
	
	public Criteria addRestriction(String fieldName, Integer value) {
		int p = fieldName.lastIndexOf('.');
		String fieldNamePart = (p == -1) ? fieldName : fieldName.substring(p + 1);
		fieldNamePart = fieldNamePart.replaceAll("\"", "");
		if (parameters.containsKey(fieldNamePart)) 
			fieldNamePart = fieldName.replace('.', '_');
		addCondition(fieldName + "=:" + fieldNamePart);
		setParameter(fieldNamePart, value);
		return this;
	}
	
	public Criteria addRestriction(String fieldName, String value) {
		int p = fieldName.lastIndexOf('.');
		String fieldNamePart = (p == -1) ? fieldName : fieldName.substring(p + 1);
		fieldNamePart = fieldNamePart.replaceAll("\"", "");
		if (parameters.containsKey(fieldNamePart)) 
			fieldNamePart = fieldName.replace('.', '_');
		addCondition(fieldName + " like concat('%', :" + fieldNamePart + ",'%')");
		setParameter(fieldNamePart, value.trim());
		return this;
	}

	public Criteria addRestriction(String fieldName, Date desdeCreacion, Date hastaCreacion) {
		int p = fieldName.lastIndexOf('.');
		String fieldNamePart = (p == -1) ? fieldName : fieldName.substring(p + 1);
		fieldNamePart = fieldNamePart.replaceAll("\"", "");
		if (parameters.containsKey(fieldNamePart)) 
			fieldNamePart = fieldName.replace('.', '_');
		addCondition(fieldName + " >= :" + fieldNamePart + "Desde");
		setParameter(fieldNamePart + "Desde", desdeCreacion);

		p = fieldName.lastIndexOf('.');
		fieldNamePart = (p == -1) ? fieldName : fieldName.substring(p + 1);
		fieldNamePart = fieldNamePart.replaceAll("\"", "");
		if (parameters.containsKey(fieldNamePart)) 
			fieldNamePart = fieldName.replace('.', '_');
		addCondition(fieldName + " <= :" + fieldNamePart + "Hasta");
		setParameter(fieldNamePart + "Hasta", hastaCreacion);
		return this;
	}

	public void setRootEntity(String rootEntity) {
		this.rootEntity = rootEntity;
	}

	public String getRootEntity() {
		return rootEntity;
	}
}
