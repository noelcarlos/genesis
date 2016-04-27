package org.esmartpoint.dbutil.db.metadata;


public class TableInfoPK {
	private String columnName;
	private Integer keySeq;
	
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setKeySeq(Integer keySeq) {
		this.keySeq = keySeq;
	}
	public Integer getKeySeq() {
		return keySeq;
	}
	
}
