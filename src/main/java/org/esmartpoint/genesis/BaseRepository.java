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
import java.lang.reflect.ParameterizedType;
import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Component
@Repository
public class BaseRepository<T extends Object> implements Serializable {
	private static final long serialVersionUID = 1L;

	private EntityManager em;

	@SuppressWarnings("unchecked")
	protected Class<T> getTypeClass() {
		return (Class<T>) ((ParameterizedType) getClass()
			.getGenericSuperclass()).getActualTypeArguments()[0];
	}

	public Session getSession() {
		return (Session) em.getDelegate();
	}

	@SuppressWarnings("unchecked")
	public List<T> list() {
		return getSession().createQuery("FROM " + getTypeClass().getSimpleName()).list();
	}

	@SuppressWarnings("unchecked")
	public T load(Serializable id) {
		return (T) getSession().load(getTypeClass(), id);
	}

	@SuppressWarnings("unchecked")
	public T get(Serializable id) {
		return (T) getSession().get(getTypeClass(), id);
	}

	public void evict(T obj) {
		getSession().evict(obj);
	}
	
	public void save(T a) {
		getSession().saveOrUpdate(a);
	}
	
	@SuppressWarnings("unchecked")
	public T merge(T a) {
		return (T)getSession().merge(a);
	}	

	public void delete(T p) {
		getSession().delete(p);
		getSession().flush();
	}

	public boolean delete(Serializable id) {
		try {
			getSession().delete(load(id));
			getSession().flush();
		} catch (ConstraintViolationException e) {
			return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public static <T> T initializeAndUnproxy(T entity) {
	    if (entity == null) {
	        throw new NullPointerException("Entity passed for initialization is null");
	    }
	    Hibernate.initialize(entity);
	    if (entity instanceof HibernateProxy) {
	        entity = (T) ((HibernateProxy) entity).getHibernateLazyInitializer().getImplementation();
	    }
	    return entity;
	}
}
