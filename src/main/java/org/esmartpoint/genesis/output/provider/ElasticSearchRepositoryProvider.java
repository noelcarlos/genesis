package org.esmartpoint.genesis.output.provider;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONObject;
import org.esmartpoint.genesis.helpers.HttpHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ElasticSearchRepositoryProvider implements IDataRepository  {

	@Autowired HttpHelper httpHelper;
	ElasticSearchRepositorySettings settings;
	
	List<JSONObject> objects = null;
	
	public ElasticSearchRepositoryProvider() {
		
	}
	
	public void setSettings(ElasticSearchRepositorySettings settings) {
		this.settings = settings;
	}
	
	@Override
	public void init() {
//		httpHelper.createConnection("target", "org.postgresql.Driver", 
//			"jdbc:postgresql://localhost:5432/ridermove?charSet=UTF8", 
//			"wwwwww", "postgres", "org.hibernate.dialect.PostgreSQLDialect");
	}
	
	public void done() {
//		dbHelper.closeConnection("target");
	}
	
	@Override
	public <S extends JSONObject> S save(S entity) {
		if (objects == null) {
			try {
				Integer key = entity.getInt(settings.key());
				JSONObject res = (JSONObject) httpHelper.post(settings.url() + "/" + settings.index() + "/" 
						+ settings.type() + "/" + key + "/_create" , entity.toString(), "JSON");
				return (S)res;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else {
			objects.add(entity);
			return entity;
		}
	}

	@Override
	public <S extends JSONObject> Iterable<S> save(Iterable<S> entities) {
		try {
			StringBuffer sb = new StringBuffer();
			for (S entity : entities) {
				Integer key = entity.getInt(settings.key());
				sb.append("{ \"create\" : { \"_id\" : ").append(key).append(" } }\r\n");
				sb.append(entity.toString() + "\r\n").append("\r\n");
			}
			
			JSONObject res = (JSONObject) httpHelper.post(settings.url() + "/" + settings.index() + "/" 
					+ settings.type() + "/_bulk" , sb.toString(), "JSON");

			return null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public JSONObject findOne(Long id) {
		try {
			return null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public Iterable<JSONObject> findAll(Long from, Long size) {
		try {
			
			return null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean exists(Long id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Iterable<JSONObject> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<JSONObject> findAll(Iterable<Long> ids) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long count() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void delete(Long id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(JSONObject entity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(Iterable<? extends JSONObject> entities) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteAll() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void commit() {
		if (objects != null) {
			save(objects);
		}
		objects = null;
	}

	@Override
	public void begin() {
		objects = new ArrayList<JSONObject>();
	}
	
}
