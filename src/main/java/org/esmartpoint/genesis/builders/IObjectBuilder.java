package org.esmartpoint.genesis.builders;

public interface IObjectBuilder<T> {
//	public Integer getPosition();
	public boolean hasNext();
	public void next() throws InstantiationException, IllegalAccessException;
//	public void setParams();
}
