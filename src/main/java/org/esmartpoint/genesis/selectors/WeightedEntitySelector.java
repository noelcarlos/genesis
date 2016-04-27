package org.esmartpoint.genesis.selectors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.springframework.beans.BeansException;

/*
 * This selector allows to create an entity or not
 */
public class WeightedEntitySelector<T> {
	private final Random rnd = new Random();
    List<WeightedItem<Integer>> weightingOptions = new ArrayList<WeightedItem<Integer>>();
    List<T> entitites = new ArrayList<T>();
    Object previousKey;
    int total, position = 0;
    
    public WeightedEntitySelector(List<T> entitites, int weightNull, int weightValue) throws Exception {
    	for (T entity : entitites) {
			this.entitites.add(entity);
		}
    	
    	weightingOptions.add(new WeightedItem<Integer>(weightNull, 0));
    	weightingOptions.add(new WeightedItem<Integer>(weightValue, 1));
    }
    
    public WeightedEntitySelector<T> build() {
    	total = 0;
    	
        for (WeightedItem<Integer> w : weightingOptions) {
            total += w.getWeight();
        }
        return this;
    }
    
    public T getNext() {
        int random = rnd.nextInt(total);
     
        //loop thru our weightings until we arrive at the correct one
        int current = 0;
        for (WeightedItem<Integer> w : weightingOptions) {
            current += w.getWeight();
            if (random < current) {
            	if (w.getItem() == 1) {
            		return entitites.get(rnd.nextInt(entitites.size()));
            	} else {
					return null;
				}
            }
                
        }
        //shouldn't happen.
        return null;
    }
    
    public void restart() {
		Collections.shuffle(entitites);
		position = 0;
		previousKey = null;
    }
    
    public <T> T nextUnique(Class<T> requiredType) {
		return (T)entitites.get(position++ % entitites.size());
    }
    
    public T nextUnique() {
		return (T)entitites.get(position++ % entitites.size());
    }
    
    public T getUnique(Object key) {
    	if (key != previousKey) {
    		Collections.shuffle(entitites);
    		position = 0;
    		previousKey = key;
    	}
    	
        int random = rnd.nextInt(total);
     
        //loop thru our weightings until we arrive at the correct one
        int current = 0;
        for (WeightedItem<Integer> w : weightingOptions) {
            current += w.getWeight();
            if (random < current) {
            	if (w.getItem() == 1) {
            		return entitites.get(position++);
            	} else {
					return null;
				}
            }
                
        }
        //shouldn't happen.
        return null;
    }
}
