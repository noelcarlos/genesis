package org.esmartpoint.dbutil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * Simple message service class. Inspired from Spring's
 * SpringValidatorAdapter This class should be delete when WebFlow will be
 * compatible with Bean Validation.
 */
public class ResourceLoaderService implements Serializable {
	private static final long serialVersionUID = -1;

	private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ResourceLoaderService.class);

	private static String webInfRealPath;
	private static HashMap<String, String> contentCache = new HashMap<String, String>();
	private static HashMap<String, FileChangedReloadingStrategy> reloadingCache = new HashMap<String, FileChangedReloadingStrategy>();
	
	public static String loadTextFile(InputStream is) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		StringBuilder sb = new StringBuilder();
		String s;
		while ((s = br.readLine()) != null)
			sb.append(s + "\n");
		br.close();
		return new String(sb.toString().getBytes(), "UTF-8");
	}
	
	static boolean reloadingRequired(String filename) throws MalformedURLException, IOException {
		if (filename.startsWith("CLASSPATH://")) {
			return false;
		} else if (filename.startsWith("HTTP://") || filename.startsWith("HTTPS://") || filename.startsWith("FTP://")) { 
			return false;
		} else {
			FileChangedReloadingStrategy reloadingStrategy = reloadingCache.get(getFile(filename));
			return reloadingStrategy == null || reloadingStrategy.reloadingRequired();
		}
	}
	
	static File getFile(String filename) {
		File f; 
		if (filename.startsWith("WEB://")) {
			logger.debug("Loading resource:" + filename + "from:" + webInfRealPath);
			f = new File(webInfRealPath + "../" + filename.substring(6));
		} else if (filename.startsWith("WEB-INF://")) {
			logger.debug("Loading resource:" + filename + "from:" + webInfRealPath);
			f = new File(webInfRealPath + filename.substring(10));
		} else {
			logger.debug("Loading resource:" + filename);
			f = new File(filename);
		}
		return f;
	}
	
	static public String loadTextFile(String filename) throws MalformedURLException, IOException {
		if (filename.startsWith("CLASSPATH://")) {
			String content = contentCache.get(filename); 
			if (content == null) {
				content = loadTextFile(Thread.currentThread().getContextClassLoader().getResourceAsStream(filename.substring(12)));
				contentCache.put(filename, content);
			}
			return content;
		} else if (filename.startsWith("HTTP://") || filename.startsWith("HTTPS://") || filename.startsWith("FTP://")) { 
			String content = contentCache.get(filename); 
			if (content == null) {
				content = loadTextFile(new URL(filename).openStream());
				contentCache.put(filename, content);
			}
			return content;
		}
		else {
			FileChangedReloadingStrategy reloadingStrategy = reloadingCache.get(filename);

			if (reloadingStrategy == null || reloadingStrategy.reloadingRequired()) {
				File f = getFile(filename);
				if (reloadingStrategy == null) {
					reloadingStrategy = new FileChangedReloadingStrategy();
					reloadingStrategy.setFile(f);
					reloadingCache.put(filename, reloadingStrategy);
				}
				reloadingStrategy.reloadingPerformed();
				String content = loadTextFile(new FileInputStream(f));
				contentCache.put(filename, content);
				return content;
			} else {
				return contentCache.get(filename);
			}
		}
	}

	public static String getWebInfRealPath() {
		return webInfRealPath;
	}

	public static void setWebInfRealPath(String webInfRealPath) {
		ResourceLoaderService.webInfRealPath = webInfRealPath;
	}
}
