package org.esmartpoint.genesis.selectors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.commons.beanutils.BeanUtils;

public class WeightedEnumSelector<T> {
    private final Random rnd = new Random();
    
    List<WeightedItem<T>> weightingOptions = new ArrayList<WeightedItem<T>>();
    HashMap<Integer, T> entititesMap = new HashMap<Integer, T>();
    int total;
    
    public WeightedEnumSelector(List<T> entitites, String keyName) throws Exception {
    	for (T t : entitites) {
			Integer key = Integer.parseInt(BeanUtils.getProperty(t, keyName));
    		entititesMap.put(key, t);
		}
    }
    
    public WeightedEnumSelector<T> add(int weight, Integer element) {
    	weightingOptions.add(new WeightedItem<T>(weight, entititesMap.get(element)));
        return this;
    }
    
    public WeightedEnumSelector<T> build() {
    	total = 0;
    	
        for (WeightedItem<T> w : weightingOptions) {
            total += w.getWeight();
        }
        return this;
    }
    
    public T getNext() {
        int random = rnd.nextInt(total);
     
        //loop thru our weightings until we arrive at the correct one
        int current = 0;
        for (WeightedItem<T> w : weightingOptions) {
            current += w.getWeight();
            if (random < current)
                return w.getItem();
        }
        //shouldn't happen.
        return null;
    }    
}
