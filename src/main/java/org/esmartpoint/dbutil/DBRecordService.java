/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.esmartpoint.dbutil;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.esmartpoint.dbutil.db.metadata.FieldInfo;
import org.esmartpoint.dbutil.db.metadata.TableInfo;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.internal.SessionImpl;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Component;

@Component
public class DBRecordService implements Serializable {
	private static final long serialVersionUID = 1L;

	private EntityManager em;

	public Session getSession() {
		return (Session) em.getDelegate();
	}

	@SuppressWarnings("unchecked")
	public Record load(String entity, Integer id) throws SQLException {
		DAOBase.initConnection(((SessionImpl)getSession()).connection());
		TableInfo ti = DAOBase.getTableInfo(entity);
		Map<Object, Object> res = (Map<Object, Object>)getSession().createSQLQuery(
				"SELECT * FROM " + entity + " WHERE " + ti.getPrimaryKey() + "=:" + ti.getPrimaryKey())
			.setParameter(ti.getPrimaryKey(), id)
			.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
			.uniqueResult();
		if (res == null)
			return null;
		return new Record(res);
	}
	
	public Integer save(String entityName, Record record) throws SQLException {
		DAOBase.initConnection(((SessionImpl)getSession()).connection());

		StringBuilder sql;
		
		TableInfo info = DAOBase.getTableInfo(entityName);
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
		SQLQuery nq = getSession().createSQLQuery(sql.toString());
		for(int i = 0; i < fields.size(); i++) 
			//if (id == null || (id != null && fields.get(i).get("isPK").equals(false))) 
				if (record.containsKey(fields.get(i).getName()))
					nq.setParameter(fields.get(i).getName(), 
						record.get(fields.get(i).getName()));
		
		nq.executeUpdate();
		return ((Number)getSession().createSQLQuery("SELECT LAST_INSERT_ID()").uniqueResult()).intValue();
	}
}
