package org.esmartpoint.genesis.builders;


public abstract class IObjectCreator<T, P> {
//	private T parent;
//	private Integer position;
//	
//	public T getParent() {
//		return parent;
//	}
//
//	public void setParent(T parent) {
//		this.parent = parent;
//	}
//
//	public Integer getPosition() {
//		return position;
//	}
//
//	public void setPosition(Integer position) {
//		this.position = position;
//	}
	
	public abstract T create(P parent) throws Exception;
}
