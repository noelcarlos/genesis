package org.esmartpoint.genesis.output.provider;

public class FileSystemRepositorySettings {

	String directory;
	int levels = 1;
	private String key;

	FileSystemRepositorySettings() {
	}

	public static FileSystemRepositorySettings builder() {
		return new FileSystemRepositorySettings();
	}

	public String directory() {
		return directory;
	}

	public FileSystemRepositorySettings directory(String directory) {
		this.directory = directory;
		return this;
	}

	public int levels() {
		return levels;
	}

	public FileSystemRepositorySettings levels(int levels) {
		this.levels = levels;
		return this;
	}

	public String key() {
		return key;
	}

	public FileSystemRepositorySettings key(String key) {
		this.key = key;
		return this;
	}
}
