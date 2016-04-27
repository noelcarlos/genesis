package org.esmartpoint.genesis.builders;

public class FixedBuilder<T> extends AbstractBuilder<T>{
	int maxElements;
	int pos = 0;
	Class<T> rootClass;
	
	public FixedBuilder(Class<T> rootClass, int maxElements) {
		this.maxElements = maxElements;
		this.rootClass = rootClass;
	}
	
	public boolean hasNext() {
		return pos < maxElements;
	}
	
	public void next() throws InstantiationException, IllegalAccessException {
		pos ++;
	}
	
	public void setParams() {
		
	}
	
	public Integer getPosition() {
		return pos;
	}	
}
