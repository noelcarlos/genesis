/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.esmartpoint.genesis;

import java.io.Serializable;
import java.util.List;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.springframework.stereotype.Component;

@Component
public class BaseService<T extends Object> implements Serializable {
	private static final long serialVersionUID = 1L;

	private EntityManager em;

//	@PersistenceContext
//	public void setEntityManager(EntityManager emNew) {
//		this.em = emNew;
//	}

	public Session getSession() {
		return (Session) em.getDelegate();
	}

	@SuppressWarnings("unchecked")
	protected List<T> find(final Criteria c, final Integer firstResult, final Integer maxResults,
			final String orderBy, final Boolean isAscending) {
		if (orderBy != null && isAscending != null) {
			if (isAscending) {
				c.addOrder(Order.asc(orderBy));
			} else {
				c.addOrder(Order.desc(orderBy));
			}
		}
		if (firstResult != null) {
			c.setFirstResult(firstResult);
		}
		if (maxResults != null) {
			c.setMaxResults(maxResults);
		}
		return c.list();
	}
	
	protected List<T> find(final Criteria c) {
		return find(c, null, null, null, null);
	}

	protected int getCount(final Criteria c) {
		c.setProjection(Projections.rowCount());
		Number count = (Number) c.uniqueResult();
		return count.intValue();
	}

	public void evict(T obj) {
		getSession().evict(obj);
	}
	
	private static Pattern doublePattern = Pattern.compile("-?\\d+(\\.\\d*)?");

	public boolean isDouble(String string) {
	    return doublePattern.matcher(string).matches();
	}
	
	private static Pattern integerPattern = Pattern.compile("-?\\d+(\\.\\d*)?");

	public boolean isInteger(String string) {
	    return integerPattern.matcher(string).matches();
	}	
}
