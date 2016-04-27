package org.esmartpoint.dbutil.db.metadata;


public class ForeingKeyInfo {
	private String catalog;
	private String schema;
	private String table;
	private String column;
	private String sequence;
	private int updateRule;
	private int deleteRule;
	private String foreignKeyName;
	private String primaryKeyName;
	private String foreignKeyColumnName;
	
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
	public String getTable() {
		return table;
	}
	public void setTable(String table) {
		this.table = table;
	}
	public String getColumn() {
		return column;
	}
	public void setColumn(String column) {
		this.column = column;
	}
	public String getSequence() {
		return sequence;
	}
	public void setSequence(String sequence) {
		this.sequence = sequence;
	}
	public int getUpdateRule() {
		return updateRule;
	}
	public void setUpdateRule(int updateRule) {
		this.updateRule = updateRule;
	}
	public int getDeleteRule() {
		return deleteRule;
	}
	public void setDeleteRule(int deleteRule) {
		this.deleteRule = deleteRule;
	}
	public String getForeignKeyName() {
		return foreignKeyName;
	}
	public void setForeignKeyName(String foreignKeyName) {
		this.foreignKeyName = foreignKeyName;
	}
	public String getPrimaryKeyName() {
		return primaryKeyName;
	}
	public void setPrimaryKeyName(String primaryKeyName) {
		this.primaryKeyName = primaryKeyName;
	}
	public void setForeignKeyColumnName(String foreignKeyColumnName) {
		this.foreignKeyColumnName = foreignKeyColumnName;
	}
	public String getForeignKeyColumnName() {
		return foreignKeyColumnName;
	}
}
