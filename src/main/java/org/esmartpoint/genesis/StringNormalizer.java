package org.esmartpoint.genesis;

import java.util.HashMap;
import java.util.Map;

public class StringNormalizer {
	private static Map<Character, Character> MAP_NORM;
	
	public static String normalizeToEnglish(String value)
	{
	    if (MAP_NORM == null || MAP_NORM.size() == 0)
	    {
	        MAP_NORM = new HashMap<Character, Character>();
	        MAP_NORM.put('À', 'A');
	        MAP_NORM.put('Á', 'A');
	        MAP_NORM.put('Â', 'A');
	        MAP_NORM.put('Ã', 'A');
	        MAP_NORM.put('Ä', 'A');
	        MAP_NORM.put('È', 'E');
	        MAP_NORM.put('É', 'E');
	        MAP_NORM.put('Ê', 'E');
	        MAP_NORM.put('Ë', 'E');
	        MAP_NORM.put('Í', 'I');
	        MAP_NORM.put('Ì', 'I');
	        MAP_NORM.put('Î', 'I');
	        MAP_NORM.put('Ï', 'I');
	        MAP_NORM.put('Ù', 'U');
	        MAP_NORM.put('Ú', 'U');
	        MAP_NORM.put('Û', 'U');
	        MAP_NORM.put('Ü', 'U');
	        MAP_NORM.put('Ò', 'O');
	        MAP_NORM.put('Ó', 'O');
	        MAP_NORM.put('Ô', 'O');
	        MAP_NORM.put('Õ', 'O');
	        MAP_NORM.put('Ö', 'O');
	        MAP_NORM.put('Ñ', 'N');
	        MAP_NORM.put('Ç', 'C');
	        MAP_NORM.put('ª', 'A');
	        MAP_NORM.put('º', 'O');
	        MAP_NORM.put('§', 'S');
	        MAP_NORM.put('³', '3');
	        MAP_NORM.put('²', '2');
	        MAP_NORM.put('¹', '1');
	        MAP_NORM.put('à', 'a');
	        MAP_NORM.put('á', 'a');
	        MAP_NORM.put('â', 'a');
	        MAP_NORM.put('ã', 'a');
	        MAP_NORM.put('ä', 'a');
	        MAP_NORM.put('è', 'e');
	        MAP_NORM.put('é', 'e');
	        MAP_NORM.put('ê', 'e');
	        MAP_NORM.put('ë', 'e');
	        MAP_NORM.put('í', 'i');
	        MAP_NORM.put('ì', 'i');
	        MAP_NORM.put('î', 'i');
	        MAP_NORM.put('ï', 'i');
	        MAP_NORM.put('ù', 'u');
	        MAP_NORM.put('ú', 'u');
	        MAP_NORM.put('û', 'u');
	        MAP_NORM.put('ü', 'u');
	        MAP_NORM.put('ò', 'o');
	        MAP_NORM.put('ó', 'o');
	        MAP_NORM.put('ô', 'o');
	        MAP_NORM.put('õ', 'o');
	        MAP_NORM.put('ö', 'o');
	        MAP_NORM.put('ñ', 'n');
	        MAP_NORM.put('ç', 'c');
	    }

	    if (value == null) {
	        return "";
	    }

	    StringBuilder sb = new StringBuilder(value);

	    for(int i = 0; i < value.length(); i++) {
	        Character c = MAP_NORM.get(sb.charAt(i));
	        if(c != null) {
	            sb.setCharAt(i, c.charValue());
	        }
	    }
	    return sb.toString();
	}
	
	static public String stringToURL(String url) {
		String res = normalizeToEnglish(url.toLowerCase());
		res = res.replaceAll("\\s", "-");
		res = res.replaceAll("[^a-z0-9-]", "");
		res = res.replaceAll("[-]+", "-");
		return res;
	}

	static public String stringToURLParam(String url) {
		String res = normalizeToEnglish(url.toLowerCase());
		res = res.replaceAll("\\s", "+");
		res = res.replaceAll("[^a-z0-9+]", "");
		res = res.replaceAll("[-]+", "+");
		return res;
	}
	
	static public String stringToEmail(String url, String domain) {
		String res = normalizeToEnglish(url.toLowerCase());
		res = res.replaceAll("\\s", ".");
		res = res.replaceAll("[^a-z0-9\\.]", "");
		res = res.replaceAll("[-]\\.", ".");
		return res + "@" + domain;
	}
}
