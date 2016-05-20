package org.esmartpoint.genesis.output.provider;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

public class CouchbaseRepositorySettings {

	List<String> nodes;
	String bucketName;
	int port;
	String password;
	private String key;

	CouchbaseRepositorySettings() {
	}

	public static CouchbaseRepositorySettings builder() {
		return new CouchbaseRepositorySettings();
	}

	public List<String> nodes() {
		return nodes;
	}

	public CouchbaseRepositorySettings nodes(List<String> nodes) {
		this.nodes = nodes;
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public CouchbaseRepositorySettings nodes(String ... nodes) {
		this.nodes = (List<String>)CollectionUtils.arrayToList(nodes);
		return this;
	}

	public String bucketName() {
		return bucketName;
	}

	public CouchbaseRepositorySettings bucketName(String bucketName) {
		this.bucketName = bucketName;
		return this;
	}

	public int port() {
		return port;
	}

	public CouchbaseRepositorySettings port(int port) {
		this.port = port;
		return this;
	}

	public String password() {
		return password;
	}

	public CouchbaseRepositorySettings password(String password) {
		this.password = password;
		return this;
	}

	public String key() {
		return key;
	}

	public CouchbaseRepositorySettings key(String key) {
		this.key = key;
		return this;
	}
}
