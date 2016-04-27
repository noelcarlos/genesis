package org.esmartpoint.genesis.selectors;

public class WeightedItem<T>{
    private final int weight;
    private final T item;
    
    public WeightedItem(int weight, T item) {
        this.item = item;
        this.weight = weight;
    }

    public T getItem() {
        return item;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"_class\": WeightedItem {\"weight\":\"").append(weight).append("\", \"item\":\"")
                .append(item).append("}");
        return builder.toString();
    }
}
