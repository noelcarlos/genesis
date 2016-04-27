package org.esmartpoint.genesis.builders;

import java.util.ArrayList;
import java.util.List;

import org.esmartpoint.genesis.generator.Range;
import org.esmartpoint.genesis.helpers.GeneratorHelper;
import org.esmartpoint.genesis.selectors.WeightedItemSelector;

public class ChildListBuilder<T,P> extends AbstractBuilder<T>{
	int maxElements;
	int pos = 0;
	int internalPos = 0;
	List<P> elements;
	List<Integer> childNumbers;
	
	public ChildListBuilder(List<P> elements) {
		this(elements, null);
	}
	
	public ChildListBuilder(List<P> elements, WeightedItemSelector<Range> numItems) {
		if (numItems != null) {
			this.elements = new ArrayList<P>();
			this.childNumbers = new ArrayList<Integer>();
			for (P element : elements) {
				Range range = numItems.getNext();
				long nItems = 0;
				if (range == null)
					continue;
				if (range.getStep() != null)
					nItems = GeneratorHelper.randomNumberWithStep(range.getFrom(), range.getTo(), range.getStep());
				else if (range.getFrom() != null)
					nItems = GeneratorHelper.randomNumber(range.getFrom(), range.getTo());
				//for (int i=0; i < nItems; i++)
				this.elements.add(element);
				this.childNumbers.add(new Long(nItems).intValue());
				this.maxElements = this.elements.size();
			}
		} else {
			this.maxElements = elements.size();
			this.elements = elements;
		}
		System.out.println("child builder finish:" + this.maxElements);
	}
	
	public boolean hasNext() {
		if (childNumbers != null) {
			return pos < maxElements -1 || (pos == maxElements -1 && internalPos < childNumbers.get(pos));
		} else {
			return pos < maxElements;
		}
	}
	
	public void next() throws InstantiationException, IllegalAccessException {
//		T res = rootClass.newInstance();
//		params.put("parent", elements.get(pos));
		
		if (childNumbers != null) {
			internalPos++;
			if (internalPos >= childNumbers.get(pos)) {
				internalPos = 0;
				pos ++;
			}
		} else {
			pos++;
		}
//		
//		return res;
	}
	
	public void setParams() {
	}
	
	public Integer getPosition() {
		return pos;
	}	
}
