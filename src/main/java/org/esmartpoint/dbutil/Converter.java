package org.esmartpoint.dbutil;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
public class Converter {
	
	static public Boolean toBool(Object obj) {
		if (obj == null)
			return null;
		String str = obj.toString();
		if (str.trim().equals(""))
			return null;
		else
			return new Boolean(str.trim());
	}
	
	static public Boolean toBool(Object obj, boolean def) {
		if (obj == null)
			return def;
		String str = obj.toString();
		if (str.trim().equals(""))
			return def;
		else
			return new Boolean(str.trim());
	}
	
	static public Integer toInt(Object str) {
		if (str == null || str.toString().trim().equals("") || str.toString().trim().equals("undefined"))
			return null;
		else
			return new Integer(str.toString().trim());
	}
	
	static public Integer toInt(Object str, int def) {
		if (str == null || str.toString().trim().equals("") || str.toString().trim().equals("undefined"))
			return def;
		else
			return new Integer(str.toString().trim());
	}

	static public Date toDate(Object dt) throws ParseException {
		if (dt == null || dt.toString().trim().equals("") || dt.toString().trim().equals("undefined"))
			return null;
		else {
			String date = dt.toString().replaceAll("\\s", "");
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			sdf.setLenient(false);
			return sdf.parse(date);
		}
	}
	
	static public DecimalFormat nf;
	
	static {
		nf = (DecimalFormat)NumberFormat.getInstance(new Locale("es"));
		nf.applyLocalizedPattern("#.##0,##");
	}

	static public Double toDouble(Object str) throws ParseException {
		if (str == null || str.toString().trim().equals("") || str.toString().trim().equals("undefined"))
			return null;
		else
			return new Double(nf.parse(str.toString().trim()).doubleValue());
	}
	
	static public Double toDouble(Object str, Double def) throws ParseException {
		if (str == null || str.toString().trim().equals(""))
			return def;
		else
			return new Double(nf.parse(str.toString().trim()).doubleValue());
	}
	
	static public String toStr(Object obj) {
		if (obj ==  null)
			return "";
		if (obj instanceof Date) {
			GregorianCalendar gc = new GregorianCalendar();
			gc.setTime((Date)obj);
			if (gc.get(Calendar.HOUR_OF_DAY) > 0 || 
				gc.get(Calendar.MINUTE) > 0 ||
				gc.get(Calendar.SECOND) > 0)
				return new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format((Date)obj);
			else
				return new SimpleDateFormat("dd/MM/yyyy").format((Date)obj);
		} else if (obj instanceof Double) {
			return nf.format((Double)obj);
		} else 
			return obj.toString();
	}
	
	protected boolean isValidDate(String date, String format) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			sdf.setLenient(false);
			date = date.replaceAll("\\s", "");
			Date dateSimple = sdf.parse(date);
			if (!date.equals(sdf.format(dateSimple)))
				  return false;
			if (dateSimple.before(new GregorianCalendar(1900, 1, 1).getTime()))
				return false;
			if (dateSimple.after(new GregorianCalendar(2100, 1, 1).getTime()))
				return false;
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static boolean isValidInt(String str) {
		try {
			str = str.replaceAll("\\s", "");
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	protected boolean isValidNumber(String str) {
		try {
			if (str == null)
				return false;
			str = str.replaceAll("\\s", "");
			nf.parse(str);
			return true;
		} catch (ParseException e) {
			return false;
		}
	}
	
	public static boolean esCif(String cif){
		Pattern cifPattern =
			Pattern.compile("([ABCDEFGHKLMNPQSabcdefghklmnpqs])" +
					"(\\d)(\\d)(\\d)(\\d)(\\d)(\\d)(\\d)([abcdefghijABCDEFGHIJ0123456789])");
	 
		Matcher m = cifPattern.matcher(cif);
		if(m.matches()){
			//Sumamos las posiciones pares de los números centrales (en realidad posiciones 3,5,7 generales)
			int sumaPar = Integer.parseInt(m.group(3))+Integer.parseInt(m.group(5))+Integer.parseInt(m.group(7));
			//Multiplicamos por 2 las posiciones impares de los números centrales (en realidad posiciones 2,4,6,8 generales)
			//Y sumamos ambos digitos: el primer digito sale al dividir por 10 (es un entero y quedará 0 o 1)
			//El segundo dígito sale de modulo 10
			int sumaDigito2 = ((Integer.parseInt(m.group(2))*2)% 10)+((Integer.parseInt(m.group(2))*2)/ 10);
			int sumaDigito4 = ((Integer.parseInt(m.group(4))*2)% 10)+((Integer.parseInt(m.group(4))*2)/ 10);
			int sumaDigito6 = ((Integer.parseInt(m.group(6))*2)% 10)+((Integer.parseInt(m.group(6))*2)/ 10);
			int sumaDigito8 = ((Integer.parseInt(m.group(8))*2)% 10)+((Integer.parseInt(m.group(8))*2)/ 10);
			int sumaImpar = sumaDigito2 +sumaDigito4 +sumaDigito6 +sumaDigito8 ;
			int suma = sumaPar +sumaImpar;
			int control = 10 - (suma%10);
			//La cadena comienza en el caracter 0, J es 0, no 10
			if (control==10)
				control=0;
			String letras = "JABCDEFGHI";
			//El dígito de control es una letra
			if (m.group(1).equalsIgnoreCase("K") || m.group(1).equalsIgnoreCase("P") ||
				m.group(1).equalsIgnoreCase("Q") || m.group(1).equalsIgnoreCase("S")){
				if (m.group(9).equalsIgnoreCase(letras.substring(control,control+1)))
					return true;
				else
					return false;
			}
			//El dígito de control es un número
			else if (m.group(1).equalsIgnoreCase("A") || m.group(1).equalsIgnoreCase("B") ||
				m.group(1).equalsIgnoreCase("E") || m.group(1).equalsIgnoreCase("H")){
				if (m.group(9).equalsIgnoreCase(""+control))
					return true;
				else
					return false;
			}
			//El dígito de control puede ser un número o una letra
			else{
				if (m.group(9).equalsIgnoreCase(letras.substring(control,control+1))||
					m.group(9).equalsIgnoreCase(""+control))
					return true;
				else
					return false;
			}
		}
		else
			return false;
	}
}
