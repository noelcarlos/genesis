package org.esmartpoint.genesis.output.provider;

public class PostgresqlRepositorySettings {

	private String table;
	private int port;
	private String password;
	private String key;

	PostgresqlRepositorySettings() {
	}

	public static PostgresqlRepositorySettings builder() {
		return new PostgresqlRepositorySettings();
	}

	public String table() {
		return table;
	}

	public PostgresqlRepositorySettings table(String table) {
		this.table = table;
		return this;
	}

	public int port() {
		return port;
	}

	public PostgresqlRepositorySettings port(int port) {
		this.port = port;
		return this;
	}

	public String password() {
		return password;
	}

	public PostgresqlRepositorySettings password(String password) {
		this.password = password;
		return this;
	}

	public String key() {
		return key;
	}

	public PostgresqlRepositorySettings key(String key) {
		this.key = key;
		return this;
	}
}
