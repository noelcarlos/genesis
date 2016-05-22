package org.esmartpoint.genesis.helpers;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import org.apache.commons.lang3.StringUtils;

@Component
public class GeneratorHelper {
	private static final String ALPHA_NUMERIC_STRING = "abcdefghijklmnopqrstuvwxyz0123456789";
	private static final String ALPHA_STRING = "abcdefghijklmnopqrstuvwxyz";
	private static final String VOWELS = "aeiou";
	private static final String WORD_CONSONANTS = "bcdfghjklmnprstvwz"; // consonants except hard to speak ones
	private static final String WORD_LETTERS = "aeioubcdfghjklmnprstvwz"; // consonants except hard to speak ones
	private static final String HEX_STRING = "0123456789abcdef";

	public static long randomNumber(long from, long to) {
		return from + (long)(Math.random() * (to - from + 1));
	}
	
	public static int randomNumber(int from, int to) {
		return from + (int)(Math.random() * (to - from + 1));
	}
	
	public static int randomNumberWithStep(int from, int to, int step) {
		return from + (int)(Math.random() * (to - from + 1) / step) * step;
	}
	
	public int randomInt(int from, int to) {
		return (int)(from + (int)(Math.random() * (to - from + 1)));
	}
	
	public boolean randomBoolean(int trueWeigth, int falseWeigth) {
		return (randomInt(0, trueWeigth + falseWeigth) < trueWeigth);
	}
	
	public static int randomIntWithStep(int from, int to, int step) {
		return (int)(from + (int)(Math.random() * (to - from + 1) / step) * step);
	}
	
	public String randomAlphaNumeric(int minLength, int maxLength) {
		StringBuilder builder = new StringBuilder(maxLength);
		int count = (int)randomNumber(minLength, maxLength);
		while (count-- != 0) {
			int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
			builder.append(ALPHA_NUMERIC_STRING.charAt(character));
		}
		return builder.toString();
	}
	
	public String randomHexNumeric(int count) {
		StringBuilder builder = new StringBuilder(count);
		while (count-- != 0) {
			int character = (int)(Math.random()*HEX_STRING.length());
			builder.append(HEX_STRING.charAt(character));
		}
		return builder.toString();
	}
	
	public String randomAlpha(int minLength, int maxLength) {
		StringBuilder builder = new StringBuilder(maxLength);
		int count = randomNumber(minLength, maxLength);
		while (count-- != 0) {
			int character = (int)(Math.random()*ALPHA_STRING.length());
			builder.append(ALPHA_STRING.charAt(character));
		}
		return builder.toString();
	}
	
	public String randomWordRaw(int minLength, int maxLength) {
		StringBuilder builder = new StringBuilder(maxLength);
		int count = randomNumber(minLength, maxLength);
		char chr = 0;
		for (int i = 0; i < count; i++) {
	        if (i == 0) {
	            // First character can be anything
				int character = (int)(Math.random()*WORD_LETTERS.length());
				chr = WORD_LETTERS.charAt(character);
	        } else if (WORD_CONSONANTS.indexOf(chr) == -1) {
	            // Last character was a vowel, now we want a consonant
				int character = (int)(Math.random()*WORD_CONSONANTS.length());
				chr = WORD_CONSONANTS.charAt(character);
	        } else {
	            // Last character was a consonant, now we want a vowel
				int character = (int)(Math.random()*VOWELS.length());
				chr = VOWELS.charAt(character);
	        }
			builder.append(chr);
	    }
		return builder.toString();
	}

	static HashMap<String, List<String>> wordCache = new HashMap<String, List<String>>();
	int MAX_DICTIONARY_LENGTH = 1000;

	public String randomWord(int minLength, int maxLength) {
		String key = minLength + ":" + maxLength;
		List<String> wordCollection = wordCache.get(key);
		if (wordCollection == null) {
			wordCollection = new ArrayList<>();
			wordCache.put(key, wordCollection);
		}
		if (wordCollection.size() < MAX_DICTIONARY_LENGTH) {
			String word = randomWordRaw(minLength, maxLength);
			wordCollection.add(word);
			return word;
		}
		int index = randomNumber(0, MAX_DICTIONARY_LENGTH - 1);
		return wordCollection.get(index);
	}
	
	public String randomSentence(int minLength, int maxLength, int minWordLength, int maxWordLength) {
		StringBuilder builder = new StringBuilder(maxLength);
		int count = randomNumber(minLength, maxLength);
		while (builder.length() < count) {
			if (builder.length() != 0)
				builder.append(' ');
			builder.append(randomWord(minWordLength, maxWordLength));
		}
		String res = builder.substring(0, 1).toUpperCase() + builder.substring(1);
		if (res.length() >= maxLength - 1)
			return res.substring(0, (int)maxLength - 1).trim() + ".";
		else
			return res.trim() + ".";
	}
	
	public String randomTitle(int minLength, int maxLength, int minWordLength, int maxWordLength) {
		StringBuilder builder = new StringBuilder(maxLength);
		int count = randomNumber(minLength, maxLength);
		while (builder.length() < count) {
			if (builder.length() != 0)
				builder.append(' ');
			builder.append(randomWord(minWordLength, maxWordLength));
		}
		String res = builder.substring(0, 1).toUpperCase() + builder.substring(1);
		if (res.length() >= maxLength - 1)
			return res.substring(0, maxLength - 1).trim();
		else
			return res.trim();
	}
	
	public String randomFilename(int minLength, int maxLength, int minWordLength, int maxWordLength, String ext) {
		StringBuilder builder = new StringBuilder(maxLength);
		int count = randomNumber(minLength, maxLength);
		while (builder.length() < count) {
			if (builder.length() != 0)
				builder.append('-');
			builder.append(randomWord(minWordLength, maxWordLength));
		}
		String res = builder.substring(0, 1).toUpperCase() + builder.substring(1);
		if (res.length() >= maxLength - 1)
			return res.substring(0, (int)maxLength - 1).trim() + "." + ext;
		else
			return res.trim() + "." + ext;
	}
	
	public String randomParagraph(int minLength, int maxLength, int minSentenceLength, int maxSentenceLength, int minWordLength, int maxWordLength) {
		StringBuilder builder = new StringBuilder(maxLength);
		int count = randomNumber(minLength, maxLength);
		while (builder.length() < count) {
			if (builder.length() != 0)
				builder.append(' ');
			builder.append(randomSentence(minSentenceLength, maxSentenceLength, minWordLength, maxWordLength));
		}
		String res = builder.substring(0, 1).toUpperCase() + builder.substring(1);
		if (res.length() >= maxLength - 1)
			return res.substring(0, (int)maxLength - 1).trim() + ".";
		else
			return res.trim();
	}
	
	public String randomText(int minLength, int maxLength, String breakChars, int minParagraphLength, int maxParagraphLength,
			int minSentenceLength, int maxSentenceLength, int minWordLength, int maxWordLength) {
		StringBuilder builder = new StringBuilder(maxLength);
		int count = randomNumber(minLength, maxLength);
		breakChars = "\r\n";
		while (builder.length() < count) {
			if (builder.length() != 0)
				builder.append(breakChars);
			builder.append(randomParagraph(minParagraphLength, maxParagraphLength, minSentenceLength, maxSentenceLength, minWordLength, maxWordLength));
		}
		String res = builder.substring(0, 1).toUpperCase() + builder.substring(1);
		if (res.length() >= maxLength - 1)
			return res.substring(0, (int)maxLength - 1).trim() + ".";
		else
			return res.trim();
	}
	
	public String randomAlphaWithSpaces(int minLength, int maxLength, int minWordLength, int maxWordLength) {
		StringBuilder builder = new StringBuilder(maxLength);
		int count = randomNumber(minLength, maxLength);
		int wordLength = randomNumber(minWordLength, maxWordLength);
		for (int i = 0; i < count; i++) {
			if (i != wordLength) {
				int character = (int)(Math.random()*ALPHA_STRING.length());
				builder.append(ALPHA_STRING.charAt(character));
			} else {
				wordLength = i + randomNumber(minWordLength, maxWordLength);
				builder.append(' ');
			}
		}
		return builder.toString();
	}
	
	public DateTime randomDate(DateTime from, DateTime to) {
		long count = randomNumber(from.getMillis(), to.getMillis());
		return new DateTime(count);
	}

	public String randomIP() {
		int a = randomNumber(10, 255);
		int b = randomNumber(0, 255);
		int c = randomNumber(0, 255);
		int d = randomNumber(2, 255);
		return a + "." + b + "." + "." + c + "." + d;
	}

	public String randomFirstName() {
		return StringUtils.capitalize(randomAlpha(4,12).toLowerCase());
	}
	
	public String randomLastName() {
		return StringUtils.capitalize(randomAlpha(5,12).toLowerCase());
	}
	
	public String randomFullName() {
		return randomFirstName() + " " + randomLastName();
	}
	
	public String randomEnumerateElement(List<?> elements) {
		return randomFirstName() + " " + randomLastName();
	}
	
	public String toHumanURL(String str, int max) {
		String url = StringUtils.stripAccents(str.toLowerCase().replaceAll("\\s", "-"));
		if (url.length() > max)
			return url.substring(0, max);
		else
			return url;
	}
	
	public String createUsername(String firstName, String lastName, Integer id) {
		firstName = StringUtils.stripAccents(firstName.toLowerCase().replaceAll("\\s", ""));
		lastName = StringUtils.stripAccents(lastName.toLowerCase().replaceAll("\\s", ""));
		return firstName + '.' + lastName + '.' + id;
	}
	
	public String randomObjectId() {
		//TODO: Missing implentation
		return "55d4a28a9c9cc583415bb8d1";
	}
	
	public String randomGUID() {
		//TODO: Missing implentation
		return "58798caf-6850-466d-9549-d16b8e769dc8";
	}

	/**
	 * Returns a randomPhone based on pattern 9# ### ## ## 6## ### ###
	 * @param pattern
	 * @return
	 */
	public String randomPhone(String pattern) {
		int count = StringUtils.countMatches(pattern, "#");
		String res = pattern;
		for (int i = 0; i <count; i++) {
			res = StringUtils.replaceOnce(res, "#", "" + randomNumber(0, 9));
		}
		return res;
	}

	public String randomAddress() {
		//{integer(100, 999)}} {{street()}}, {{city()}}, {{state()}
		//TODO: Missing implentation
		return "463 Empire Boulevard, Highland, Virginia, 8787";
	}

	public double randomLatitude() {
		//floating(-90.000001, 90)
		//TODO: Missing implentation
		return -86.297376;
	}

	public double randomLongitude() {
		//'{{floating(-180.000001, 180)
		//TODO: Missing implentation
		return 132.149959;
	}
	
	public int randomAge() {
		//'{{floating(-180.000001, 180)
		//TODO: Missing implentation
		return randomNumber(18, 90);
	}
	
	public int randomDni() {
		//'{{floating(-180.000001, 180)
		//TODO: Missing implentation
		return randomNumber(18, 90);
	}
	
	public int randomIBAN() {
		//'{{floating(-180.000001, 180)
		//TODO: Missing implentation
		return randomNumber(18, 90);
	}
}
