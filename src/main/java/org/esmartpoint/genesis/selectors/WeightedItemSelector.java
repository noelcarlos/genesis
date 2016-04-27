package org.esmartpoint.genesis.selectors;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WeightedItemSelector<T> {
    private final Random rnd = new Random();
    
    List<WeightedItem<T>> weightingOptions = new ArrayList<WeightedItem<T>>();
    int total;
    
    public WeightedItemSelector() {
    }
    
    public WeightedItemSelector<T> add(int weight, T element) {
    	weightingOptions.add(new WeightedItem<T>(weight, element));
        return this;
    }
    
    public WeightedItemSelector<T> build() {
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
