package org.esmartpoint.genesis.output.provider;

import org.codehaus.jettison.json.JSONObject;
import org.springframework.data.repository.CrudRepository;

public interface IDataRepository extends CrudRepository<JSONObject, Long>  {

	void init();
	
	void done();
	
	public Iterable<JSONObject> findAll(Long from, Long size);

	void commit();

	void begin();
}
