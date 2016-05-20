package org.esmartpoint.genesis.output.provider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;

import org.apache.commons.io.FileUtils;
import org.codehaus.jettison.json.JSONObject;

public class FileSystemRepositoryProvider implements IDataRepository  {

	FileSystemRepositorySettings settings;
	
	public FileSystemRepositoryProvider(FileSystemRepositorySettings settings) {
		this.settings = settings;
	}
	
	@Override
	public void init() {
		new File(settings.directory).mkdirs();
	}
	
	public void done() {
	}
	
	@Override
	public <S extends JSONObject> S save(S entity) {
		String key;
		try {
			key = "" + entity.getInt(settings.key());
			String filename = settings.directory + "/" + key + ".json";
			//String x = entity.toString();
			FileUtils.writeStringToFile(new File(filename), entity.toString(), "UTF-8");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	@Override
	public <S extends JSONObject> Iterable<S> save(Iterable<S> entities) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject findOne(Long id) {
		// TODO Auto-generated method stub
		return null;
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

	
}
