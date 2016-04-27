package org.esmartpoint.dbutil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeSet;

@SuppressWarnings("unchecked")
public class Cronometro {
	private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Cronometro.class);
	
	public static class Data {
		public long current;
		public long sum;
		public long count;
		public Long min;
		public Long max;
		
		public Data() {
			sum = 0;
			count = 0;
			min = max = null;
		}
	}
	
	private static ThreadLocal<HashMap<String, Long>> clocks = new ThreadLocal<HashMap<String, Long>>();
	
	private static ThreadLocal<HashMap<String, Data>> tiempo = new ThreadLocal<HashMap<String, Data>>();

	static public void start(String nombre) {
//		if (clocks.get(nombre) == null) {
//			clocks.put(nombre, -1L);
//			return;
//		}
		if (clocks.get() == null)
			inicializar();
		clocks.get().put(nombre, System.currentTimeMillis());
		//limpiar(nombre);
	}

	static public void stop(String nombre) {
		Long ck = clocks.get().get(nombre);
		if (ck == null)
			return;
//		if (ck < 0) {
//			ck++;
//			clocks.put(nombre, ck);
//			return;
//		}
		Long val = System.currentTimeMillis() - clocks.get().get(nombre);
		clocks.get().put(nombre, System.currentTimeMillis());
		clocks.get().remove(nombre);
		Data d = tiempo.get().get(nombre);
		if (d == null) {
			d = new Data();
			d.current = val;
			d.sum = val;
			d.count = 1;
			d.min = val;
			d.max = val;
			tiempo.get().put(nombre, d);
		} else {
			d.current = val;
			d.sum = d.sum + val;
			d.count = d.count + 1;
			d.min = Math.min(d.min, val);
			d.max = Math.max(d.max, val);
			//tiempo.put(nombre, d);
		}
	}
	
	static public void limpiar(String nombre) {
		tiempo.get().put(nombre, new Data());
	}
	
	static public Data get(String nombre) {
		return tiempo.get().get(nombre);
	}
	
	@SuppressWarnings("rawtypes")
	static public void trace() {
		// to hold the result
		HashMap map = new LinkedHashMap<String, Data>();

		List<String> yourMapKeys = new ArrayList<String>(tiempo.get().keySet());
		List<Data> yourMapValues = new ArrayList<Data>(tiempo.get().values());
		
		TreeSet<Data> sortedSet = new TreeSet<Data>(new Comparator<Data>(){
            public int compare(Data entry, Data entry1) {
            	long dif = (entry.sum - entry1.sum);
                //return dif == 0 ? 0 : (dif > 0) ? 1 : -1;
            	return dif == 0 ? 1 : (dif > 0) ? 1 : -1;
            }
		});

		sortedSet.addAll(yourMapValues);
		Object[] sortedArray = sortedSet.toArray();
		int size = sortedArray.length;

		for (int i=0; i<size; i++) {
		   map.put(yourMapKeys.get(yourMapValues.indexOf(sortedArray[i])), sortedArray[i]);
		}

		for (Iterator<String> iter = map.keySet().iterator(); iter.hasNext();) {
			String key = iter.next();
			Data d = tiempo.get().get(key); 
			System.out.println(key + " [ total=" + ((int)(d.sum))/1000.0 + 
					" current=" + ((int)(d.current))/1000.0 +
					" media=" + ((int)(d.sum/d.count))/1000.0 +
					" min=" + ((long)d.min)/1000.0 +
					" max=" + ((long)d.max)/1000.0 +" ]");
		}
	}
	
	static public void inicializar() {
		clocks.set(new HashMap<String, Long>());
		tiempo.set(new HashMap<String, Data>());
	}
}


