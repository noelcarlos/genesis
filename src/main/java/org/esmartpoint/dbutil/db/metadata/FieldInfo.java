package org.esmartpoint.dbutil.db.metadata;


public class FieldInfo {
	private String name;
	private String propertyName;
	private int type;
	private String typeName;
	private String remarks;
	private String defaultValue;
	private int size;
	private int decimals;
	private boolean isNullable;
	private boolean isPK;
	private boolean isFK;
	private ForeingKeyInfo foreingKey;
	private int sequencePK;
	private boolean index;
	private IndexInfo indexInfo;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPropertyName() {
		return propertyName;
	}
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getTypeName() {
		return typeName;
	}
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public int getDecimals() {
		return decimals;
	}
	public void setDecimals(int decimals) {
		this.decimals = decimals;
	}
	public boolean isNullable() {
		return isNullable;
	}
	public void setNullable(boolean isNullable) {
		this.isNullable = isNullable;
	}
	public boolean isPK() {
		return isPK;
	}
	public void setPK(boolean isPK) {
		this.isPK = isPK;
	}
	public boolean isFK() {
		return isFK;
	}
	public void setFK(boolean isFK) {
		this.isFK = isFK;
	}
	public ForeingKeyInfo getForeingKey() {
		return foreingKey;
	}
	public void setForeingKey(ForeingKeyInfo fk) {
		this.foreingKey = fk;
	}
	public void setSequencePK(int sequencePK) {
		this.sequencePK = sequencePK;
	}
	public int getSequencePK() {
		return sequencePK;
	}
	public void setIndexInfo(IndexInfo indexInfo) {
		this.indexInfo = indexInfo;
	}
	public IndexInfo getIndexInfo() {
		return indexInfo;
	}
	public void setIndex(boolean index) {
		this.index = index;
	}
	public boolean isIndex() {
		return index;
	}
}
