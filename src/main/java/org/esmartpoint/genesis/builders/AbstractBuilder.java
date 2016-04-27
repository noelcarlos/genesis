package org.esmartpoint.genesis.builders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class AbstractBuilder<T> implements IObjectBuilder<T> {
	HashMap<String, Object> params = new HashMap<String, Object>();
	private Integer numIterations;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<T> build(IObjectCreator om, boolean discardResult) throws Exception {
		numIterations = 0;
		final List<T> res;
		
		if (!discardResult) {
			res = new ArrayList<T>();
		} else {
			res = null;
		}
		
		while (hasNext()) {
			T val = (T)om.create(null);
			if (!discardResult)
				res.add(val);
			numIterations++;
			next();
		} 
		
		return res;
	}

	public Integer getNumIterations() {
		return numIterations;
	}

	public void setNumIterations(Integer numIterations) {
		this.numIterations = numIterations;
	}
}
