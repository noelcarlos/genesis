package org.esmartpoint.genesis.builders;

import java.util.ArrayList;
import java.util.List;

import org.esmartpoint.genesis.generator.Range;
import org.esmartpoint.genesis.helpers.GeneratorHelper;
import org.esmartpoint.genesis.selectors.WeightedItemSelector;

/*
 
  Query query = session.createQuery(query);
    query.setReadOnly(true);
    // MIN_VALUE gives hint to JDBC driver to stream results
    query.setFetchSize(Integer.MIN_VALUE);
    ScrollableResults results = query.scroll(ScrollMode.FORWARD_ONLY);
    // iterate over results
    while (results.next()) {
        Object row = results.get();
        // process row then release reference
        // you may need to flush() as well
    }
    results.close();
  
 */
public class ChildBuilder<T,P> extends AbstractBuilder<T>{
	int maxElements;
	int pos = 0;
	int internalPos = 0;
	List<P> elements;
	List<Integer> childNumbers;
	
	public ChildBuilder(List<P> elements) {
		this.maxElements = elements.size();
		this.elements = elements;
	}
	
	public ChildBuilder(List<P> elements, WeightedItemSelector<?> numItemsSelector) {
		this.elements = new ArrayList<P>();
		this.childNumbers = new ArrayList<Integer>();
		
		for (P element : elements) {
			Object wItem = numItemsSelector.getNext();
			long nItems = 0;
			if (wItem == null)
				continue;
			if (wItem instanceof Range) {
				Range range = (Range)wItem;
				if (range.getStep() != null)
					nItems = GeneratorHelper.randomNumberWithStep(range.getFrom(), range.getTo(), range.getStep());
				else if (range.getFrom() != null)
					nItems = GeneratorHelper.randomNumber(range.getFrom(), range.getTo());
			} else if (wItem instanceof Number) {
				nItems = ((Number)wItem).intValue();
			}
			//for (int i=0; i < nItems; i++)
			this.elements.add(element);
			this.childNumbers.add(new Long(nItems).intValue());
		}

		this.maxElements = this.elements.size();
		//System.out.println("child builder finish:" + this.maxElements);
	}
	
	public boolean hasNext() {
		if (childNumbers != null) {
			return pos < maxElements -1 || (pos == maxElements -1 && internalPos < childNumbers.get(pos));
		} else {
			return pos < maxElements;
		}
	}
	
	public void next() throws InstantiationException, IllegalAccessException {
		if (childNumbers != null) {
			internalPos++;
			if (internalPos >= childNumbers.get(pos)) {
				internalPos = 0;
				pos ++;
			}
		} else {
			pos++;
		}
	}
	
	public void setParams() {
	}
	
	public Integer getPosition() {
		return pos;
	}	
	
	public Integer getInternalPos() {
		return internalPos;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<T> build(final IObjectCreator om, final boolean discardResult) throws Exception {
		setNumIterations(0);
		final List<T> res;
		
		if (!discardResult) {
			res = new ArrayList<T>();
		} else {
			res = null;
		}
		
    	while (hasNext()) {
			T val = (T)om.create(elements.get(pos));
			if (!discardResult)
				res.add(val);
			setNumIterations(getNumIterations() + 1);
			next();
		}	
		
		return res;
	}
}
