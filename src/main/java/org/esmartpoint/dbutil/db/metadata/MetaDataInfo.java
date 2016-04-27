package org.esmartpoint.dbutil.db.metadata;

import java.util.HashMap;
import java.util.Map;

public class MetaDataInfo {
	private Map<String, TableInfo> tables;

	public MetaDataInfo() {
		tables = new HashMap<String, TableInfo>();
	}
	
	public void setTables(Map<String, TableInfo> tables) {
		this.tables = tables;
	}

	public Map<String, TableInfo> getTables() {
		return tables;
	}
}
