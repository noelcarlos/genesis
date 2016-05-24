package org.esmartpoint.genesis.output.provider;

public class ElasticSearchRepositorySettings {

	private String index;
	private String type;
	private String url;

	private int port;
	private String password;
	private String key;

	ElasticSearchRepositorySettings() {
	}

	public static ElasticSearchRepositorySettings builder() {
		return new ElasticSearchRepositorySettings();
	}

	public String url() {
		return url;
	}

	public ElasticSearchRepositorySettings url(String url) {
		this.url = url;
		return this;
	}
	
	public String index() {
		return index;
	}

	public ElasticSearchRepositorySettings index(String index) {
		this.index = index;
		return this;
	}	
	
	public String type() {
		return type;
	}

	public ElasticSearchRepositorySettings type(String type) {
		this.type = type;
		return this;
	}

	public int port() {
		return port;
	}

	public ElasticSearchRepositorySettings port(int port) {
		this.port = port;
		return this;
	}

	public String password() {
		return password;
	}

	public ElasticSearchRepositorySettings password(String password) {
		this.password = password;
		return this;
	}

	public String key() {
		return key;
	}

	public ElasticSearchRepositorySettings key(String key) {
		this.key = key;
		return this;
	}
}
