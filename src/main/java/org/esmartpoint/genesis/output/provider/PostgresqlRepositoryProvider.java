package org.esmartpoint.genesis.output.provider;

import java.util.HashMap;

import org.codehaus.jettison.json.JSONObject;
import org.esmartpoint.genesis.helpers.DbHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PostgresqlRepositoryProvider implements IDataRepository  {

	@Autowired DbHelper dbHelper;
	PostgresqlRepositorySettings settings;
	
	public PostgresqlRepositoryProvider() {
		
	}
	
	public void setSettings(PostgresqlRepositorySettings settings) {
		this.settings = settings;
	}
	
	@Override
	public void init() {
		dbHelper.createConnection("target", "org.postgresql.Driver", 
			"jdbc:postgresql://localhost:5432/ridermove?charSet=UTF8", 
			"wwwwww", "postgres", "org.hibernate.dialect.PostgreSQLDialect");
	}
	
	public void done() {
		dbHelper.closeConnection("target");
	}
	
	@Override
	public <S extends JSONObject> S save(S entity) {
		try {
			HashMap<String, Object> record = new HashMap<String, Object>();
			record.put("document", entity.toString());
			dbHelper.insert("target", settings.table(), record, settings.key());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return entity;
	}

	@Override
	public <S extends JSONObject> Iterable<S> save(Iterable<S> entities) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject findOne(Long id) {
		try {
			HashMap<String, Object> record = dbHelper.findOne("target", settings.table(), "id", id.intValue());
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
		dbHelper.commit("target");		
	}

	@Override
	public void begin() {
		dbHelper.begin("target");
	}
	
}
