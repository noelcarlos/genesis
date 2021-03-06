package org.esmartpoint.genesis.output.provider;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class FileSystemRepositoryProvider implements IDataRepository  {

	FileSystemRepositorySettings settings;
	
	public void setSettings(FileSystemRepositorySettings settings) {
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
		String keyStr;
		try {
			int key = entity.getInt(settings.key());
			keyStr = "" + key;
			String path = "";
			if (key < 1000000) {
				path = "b" + (key % 1000) +"/";
			}
			String filename = settings.directory + "/" + path + keyStr + ".json";
			String contents = entity.toString();
			FileUtils.writeStringToFile(new File(filename), contents, "UTF-8");
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

	@Override
	public Iterable<JSONObject> findAll(Long from, Long size) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void commit() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void begin() {
		// TODO Auto-generated method stub
		
	}
}
