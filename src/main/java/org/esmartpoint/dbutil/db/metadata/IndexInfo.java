package org.esmartpoint.dbutil.db.metadata;


/*
TABLE_CAT String => table catalog (may be null) 
TABLE_SCHEM String => table schema (may be null) 
TABLE_NAME String => table name 
NON_UNIQUE boolean => Can index values be non-unique. false when TYPE is tableIndexStatistic 
INDEX_QUALIFIER String => index catalog (may be null); null when TYPE is tableIndexStatistic 
INDEX_NAME String => index name; null when TYPE is tableIndexStatistic 
TYPE short => index type: 
tableIndexStatistic - this identifies table statistics that are returned in conjuction with a table's index descriptions 
tableIndexClustered - this is a clustered index 
tableIndexHashed - this is a hashed index 
tableIndexOther - this is some other style of index 
ORDINAL_POSITION short => column sequence number within index; zero when TYPE is tableIndexStatistic 
COLUMN_NAME String => column name; null when TYPE is tableIndexStatistic 
ASC_OR_DESC String => column sort sequence, "A" => ascending, "D" => descending, may be null if sort sequence is not supported; null when TYPE is tableIndexStatistic 
CARDINALITY int => When TYPE is tableIndexStatistic, then this is the number of rows in the table; otherwise, it is the number of unique values in the index. 
PAGES int => When TYPE is tableIndexStatisic then this is the number of pages used for the table, otherwise it is the number of pages used for the current index. 
FILTER_CONDITION String => Filter condition, if any. (may be null) 
*/

public class IndexInfo {
	String catalog;
	String schema;
	String table;
	
	Boolean nonUnique;
	String indexQualifier;
	String indexName;
	Integer type;
	Integer ordinalPosition;
	String columnName;
	Boolean ascending;
	Integer cardinality;
	Integer pages;
	String filterCondition;
	
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
	public Boolean getNonUnique() {
		return nonUnique;
	}
	public void setNonUnique(Boolean nonUnique) {
		this.nonUnique = nonUnique;
	}
	public String getIndexQualifier() {
		return indexQualifier;
	}
	public void setIndexQualifier(String indexQualifier) {
		this.indexQualifier = indexQualifier;
	}
	public String getIndexName() {
		return indexName;
	}
	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public Integer getOrdinalPosition() {
		return ordinalPosition;
	}
	public void setOrdinalPosition(Integer ordinalPosition) {
		this.ordinalPosition = ordinalPosition;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public Boolean getAscending() {
		return ascending;
	}
	public void setAscending(Boolean ascending) {
		this.ascending = ascending;
	}
	public Integer getCardinality() {
		return cardinality;
	}
	public void setCardinality(Integer cardinality) {
		this.cardinality = cardinality;
	}
	public Integer getPages() {
		return pages;
	}
	public void setPages(Integer pages) {
		this.pages = pages;
	}
	public String getFilterCondition() {
		return filterCondition;
	}
	public void setFilterCondition(String filterCondition) {
		this.filterCondition = filterCondition;
	}
	
}
