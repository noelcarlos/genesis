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
import java.util.HashMap;
import java.util.List;

import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = false)
public class DataService extends BaseService<Object> implements Serializable {
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	@Transactional(readOnly=false)
	List<String> readKeysForLinkedin() {
		List<String> keyList;
		keyList = getSession().createSQLQuery(
			//"SELECT nombre FROM test_data.src_nombres WHERE id>=352").list();				
			//"SELECT apellido FROM test_data.src_apellidos WHERE id>=1").list();
			"SELECT firstname FROM test_data.src_firstnames WHERE id>=3414").list(); // For linkedin
		return keyList;
	}
	
	@SuppressWarnings("unchecked")
	@Transactional(readOnly=false)
	List<String> readKeysForImdb() {
		List<String> keyList;
		keyList = getSession().createSQLQuery(
			//"SELECT nombre FROM test_data.src_nombres WHERE id>=352").list();				
			//"SELECT apellido FROM test_data.src_apellidos WHERE id>=1").list();
			//"SELECT firstname FROM test_data.src_firstnames WHERE id>=2286").list(); // For linkedin
			"SELECT firstname FROM test_data.src_firstnames WHERE id>=2249").list(); // For imdb
		return keyList;
	}
	
	@SuppressWarnings("unchecked")
	@Transactional(readOnly=false)
	List<HashMap<String, Object>> getUnproccesedUserList(int max) {
		List<HashMap<String, Object>> keyList = getSession().createSQLQuery(
			"SELECT * FROM test_data.src_linkedin WHERE processed_date is null")
			.setMaxResults(max)
			.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
			.list();
		return keyList;
	}

	@Transactional(readOnly=false)
	boolean saveData(String keyId, String profileLink, String name, String thumbSrc,
			String largeSrc, String title, String location, String industry, String locale) {
		Number n = (Number)getSession().createSQLQuery("SELECT COUNT(*) FROM test_data.src_linkedin WHERE key_id=:key_id")
				.setParameter("key_id", keyId)
				.uniqueResult();
		
		if (n.intValue() == 0) {
			getSession().createSQLQuery("INSERT INTO test_data.src_linkedin ("
				+ "key_id, profile_link, image_link, thumb_link, name, title, location, industry, locale) "
				+ "VALUES (:key_id, :profile_link, :image_link, :thumb_link, :name, :title, :location, :industry, :locale)")
				.setParameter("key_id", keyId)
				.setParameter("profile_link", profileLink)
				.setParameter("image_link", largeSrc)
				.setParameter("thumb_link", thumbSrc)
				.setParameter("name", name)
				.setParameter("title", title)
				.setParameter("location", location)
				.setParameter("industry", industry)
				.setParameter("locale", locale)
				.executeUpdate();
			return true;
		}
		
		return false;
	}
	
	@Transactional(readOnly=false)
	boolean saveIMDBData(String keyId, String profileLink, String name, String thumbSrc,
			String largeSrc, String title, String film, String bio, String locale) {
		Number n = (Number)getSession().createSQLQuery("SELECT COUNT(*) FROM test_data.src_imdb WHERE key_id=:key_id")
				.setParameter("key_id", keyId)
				.uniqueResult();
		
		if (n.intValue() == 0) {
			getSession().createSQLQuery("INSERT INTO test_data.src_imdb ("
				+ "key_id, profile_link, image_link, thumb_link, name, title, film, bio, locale) "
				+ "VALUES (:key_id, :profile_link, :image_link, :thumb_link, :name, :title, :film, :bio, :locale)")
				.setParameter("key_id", keyId)
				.setParameter("profile_link", profileLink)
				.setParameter("image_link", largeSrc)
				.setParameter("thumb_link", thumbSrc)
				.setParameter("name", name)
				.setParameter("title", title)
				.setParameter("film", film)
				.setParameter("bio", bio)
				.setParameter("locale", locale)
				.executeUpdate();
			return true;
		}
		
		return false;
	}
}
