package org.esmartpoint.dbutil.db.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableInfo {
	private String catalog;
	private String schema;
	private String name;
	private String type;
	private String remarks;
	private String primaryKey;
	private String sequencePK;
	
	private List<FieldInfo> fields = new ArrayList<FieldInfo>();
	private List<TableInfoPK> pkFields = new ArrayList<TableInfoPK>();
	private Map<String, FieldInfo> fieldsMap = new HashMap<String, FieldInfo>();
	private HashMap<String, List<ForeingKeyInfo>> foreingKeyByNameMap;
	private HashMap<String, List<ForeingKeyInfo>> foreingKeyByTableNameMap;
	private HashMap<String, List<IndexInfo>> indexNameMap;
	
	public String getCatalog() {
		return catalog;
	}
	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}
	public String getSchema() {
		return schema;
	}
	public void setSchema(String schema) {
		this.schema = schema;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public List<FieldInfo> getFields() {
		return fields;
	}
	public void setFields(List<FieldInfo> fields) {
		this.fields = fields;
	}
	public Map<String, FieldInfo> getFieldsMap() {
		return fieldsMap;
	}
	public void setFieldsMap(Map<String, FieldInfo> fieldsMap) {
		this.fieldsMap = fieldsMap;
	}
	public String getPrimaryKey() {
		return primaryKey;
	}
	public void setPrimaryKey(String primaryKey) {
		this.primaryKey = primaryKey;
	}
	public String getSequencePK() {
		return sequencePK;
	}
	public void setSequencePK(String sequencePK) {
		this.sequencePK = sequencePK;
	}
	public void setPkFields(List<TableInfoPK> pkFields) {
		this.pkFields = pkFields;
	}
	public List<TableInfoPK> getPkFields() {
		return pkFields;
	}
	public void setForeingKeyByNameMap(HashMap<String, List<ForeingKeyInfo>> foreingKeyByNameMap) {
		this.foreingKeyByNameMap = foreingKeyByNameMap;
	}
	public HashMap<String, List<ForeingKeyInfo>> getForeingKeyByNameMap() {
		return foreingKeyByNameMap;
	}
	public void setIndexNameMap(HashMap<String, List<IndexInfo>> indexNameMap) {
		this.indexNameMap = indexNameMap;
	}
	public HashMap<String, List<IndexInfo>> getIndexNameMap() {
		return indexNameMap;
	}
	public void setForeingKeyByTableNameMap(HashMap<String, List<ForeingKeyInfo>> foreingKeyByTableNameMap) {
		this.foreingKeyByTableNameMap = foreingKeyByTableNameMap;
	}
	public HashMap<String, List<ForeingKeyInfo>> getForeingKeyByTableNameMap() {
		return foreingKeyByTableNameMap;
	}
}
