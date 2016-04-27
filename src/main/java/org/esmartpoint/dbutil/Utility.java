package org.esmartpoint.dbutil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Map.Entry;

public class Utility {
	static public String fileSizeToString(long size) {
		int kb = (int) size / 1024;
		int megas = kb / 1000;
		return megas + "," + kb;
	}

	static public String secondsToString(long seconds) {
		long min = seconds / 60;
		seconds = seconds % 60;
		return min + ":" + ((seconds < 10) ? "0" + seconds : "" + seconds);
	}

	static public long parseTimeToSeconds(String time) {
		String[] parts = time.split("[\\:]");
		long seconds = 0;
		int fac = 1;
		for (int i = parts.length - 1; i >= 0; i--, fac *= 60)
			seconds += Integer.parseInt(parts[i]) * fac;
		return seconds;
	}

	static public String getFileExtension(String filename) {
		int startpos = Math.max(0, filename.lastIndexOf('\\'));
		startpos = Math.max(startpos, filename.lastIndexOf('/'));
		startpos = Math.max(startpos, filename.lastIndexOf(':'));
		int extpos = filename.indexOf('.', startpos);
		if (extpos == -1)
			return "";
		return filename.substring(extpos + 1);
	}

	static public boolean extensionMatchFilter(String ext, String filter) {
		String f = filter.toLowerCase();
		String e = ext.toLowerCase();
		int p1 = f.indexOf(e);
		// No esta en el filtro
		if (p1 == -1)
			return false;
		// Validar de que este completamente
		int p2 = p1 + e.length();
		if ((p1 == 0 || ".|;".indexOf(f.charAt(p1 - 1)) != -1)
				&& (p2 == f.length() || ".|;".indexOf(f.charAt(p2)) != -1))
			return true;
		return false;
	}

	static public void getFileList(String baseFolder, Vector<String> list, String filter) {
		File folder = new File(baseFolder);
		File files[] = folder.listFiles();

		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory())
				getFileList(files[i].getAbsolutePath(), list, filter);
			else {
				String path = files[i].getAbsolutePath();
				String ext = getFileExtension(path);
				if (filter == null || extensionMatchFilter(ext, filter))
					list.add(path);
			}
		}
	}

	static public boolean isValidInteger(String str) {
		if (str.length() == 0 || str == null)
			return false;
		int i = 0;
		// Parse spaces
		while (i < str.length() && " ".indexOf(str.charAt(i)) != -1)
			i++;
		// Parse sign
		if (i < str.length() && "+-".indexOf(str.charAt(i)) != -1)
			i++;
		// Parse digits
		while (i < str.length() && "0123456789".indexOf(str.charAt(i)) != -1)
			i++;
		// Parse spaces
		while (i < str.length() && " ".indexOf(str.charAt(i)) != -1)
			i++;
		return i == str.length();
	}

	public static String encryptString(String str) {
		byte[] originalByteArray = str.getBytes();
		String returnString = "";

		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.reset();
			digest.update(originalByteArray);
			byte[] arr = digest.digest();
			returnString = arr.toString();

		} catch (Exception e) {
			System.out.println("Error:encryptString():" + e.getMessage());
			return returnString;
		}

		return returnString;
	}

	public static String hex(byte[] array) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < array.length; ++i) {
			sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100)
					.toLowerCase().substring(1, 3));
		}
		return sb.toString();
	}

	public static String toHex(String str) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < str.length(); ++i) {
			char c = str.charAt(i);
			sb.append(Integer.toHexString((c & 0xFF) | 0x100).toLowerCase()
					.substring(1, 3));
		}
		return sb.toString();
	}

	static final String hexTbl = "0123456789abcdef";

	public static String fromHex(String hexStr) {
		if (hexStr == null || hexStr.length() % 2 != 0)
			return null;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < hexStr.length(); i++) {
			char c1 = hexStr.charAt(i++);
			char c2 = hexStr.charAt(i);
			int v = 16 * hexTbl.indexOf(c1) + hexTbl.indexOf(c2);
			sb.append((char) v);
		}
		return sb.toString();
	}

	public static String md5(String message) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			return hex(md.digest(message.getBytes("CP1252")));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void saveTextFile(OutputStream os, String text) throws IOException {
		BufferedWriter br = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
		br.write(text);
		br.close();
	}
	
	public static String loadTextFile(InputStream is) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		StringBuilder sb = new StringBuilder();
		String s;
		while ((s = br.readLine()) != null)
			sb.append(s + "\n");
		br.close();
		return new String(sb.toString().getBytes(), "UTF-8");
	}	
	
	public static String loadTextFile(String filename) throws IOException {
		return loadTextFile(new FileInputStream(filename));
	}
	
	public static void saveTextFile(String filename, String text) throws IOException {
		saveTextFile(new FileOutputStream(filename), text);
	}
	
	public static ArrayList<String> stringToVector(String str, String sep) {
		ArrayList<String> res = new ArrayList<String>();
		if (str != null) {
			StringTokenizer st = new StringTokenizer(str, sep);
			while (st.hasMoreElements()) 
				res.add(st.nextToken().trim());
		}
		return res;
	}
	
	public static Record stringToHashMap(String str, String sep) {
		Record res = new Record();
		if (str != null) {
			StringTokenizer st = new StringTokenizer(str, sep);
			while (st.hasMoreElements()) {
				res.put(st.nextToken(), new Boolean(true));
			}
		}
		return res;
	}
	
	public static Integer formStrToInt(String str) {
		return formStrToInt(str, new Integer(0));
	}
	
	public static Integer formStrToInt(String str, Integer def) {
		if (str == null || str.equals("") || str.equals("*"))
			return def;
		return new Integer(str);
	}

	public static String formStrToStr(String str) {
		if (str == null || str.equals(""))
			return "";
		return str;
	}
	
	public static String formStrToStr(String str, String def) {
		if (str == null || str.equals(""))
			return def;
		return str;
	}
	
	public static boolean isNullOrEmpty(String str) {
		return str == null || str.equals(""); 
	}
	
	public static long streamCopy(InputStream is, OutputStream os) throws IOException {
		return streamCopy(is, os, 8*1024);
	}
	
	public static long fileCopy(String source, String target) throws IOException {
		return streamCopy(new FileInputStream(source), new FileOutputStream(target));
	}
	
	public static boolean fileMove(String source, String target) throws IOException {
		FileInputStream src = new FileInputStream(source);
		FileOutputStream trg = new FileOutputStream(target);
		streamCopy(src, trg);
		trg.close();
		src.close();
		return new File(source).delete();
	}
	
	public static long streamCopy(InputStream is, OutputStream os, int bufferSize) throws IOException {
		byte data[] = new byte[bufferSize];
		long size = 0;
		while (true) {
			int cb = is.read(data, 0, bufferSize);
			if (cb == -1)
				break;
			os.write(data, 0, cb);
			size += cb;
		}
		return size;
	}
	
	public static String getFileExt(String filename) {
		String res = new File(filename).getName();
		int pos = res.lastIndexOf(".");
		if (pos != -1)
			return res.substring(pos);
		else
			return "";
	}

	public static String loadTextResource(String resourceName) throws IOException {
		return loadTextFile(Utility.class.getResourceAsStream(resourceName));
	}
	
	
	public static String variableSubstitution(String strText, Record variables) {
	    //strText = strText.replaceAll("\\{INPUTDATA\\}", xmlDoc);
		for (Iterator<Entry<Object, Object>> iterator = variables.entrySet().iterator(); iterator.hasNext();) {
			Entry<Object, Object> entry = iterator.next();
			Object value = entry.getValue();
			String strValue = value == null ? "" : value.toString();
			strText = strText.replaceAll("\\$\\{" + entry.getKey() + "\\}", strValue);
		}
		return strText;
	}
}
