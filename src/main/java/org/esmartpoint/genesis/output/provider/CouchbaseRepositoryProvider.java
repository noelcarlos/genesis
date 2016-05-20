package org.esmartpoint.genesis.output.provider;

import org.codehaus.jettison.json.JSONObject;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;

public class CouchbaseRepositoryProvider implements IDataRepository  {

	Cluster cluster;
	Bucket bucket;
	CouchbaseRepositorySettings settings;
	
	public CouchbaseRepositoryProvider(CouchbaseRepositorySettings settings) {
		this.settings = settings;
	}
	
	@Override
	public void init() {
		cluster = CouchbaseCluster.create(settings.nodes());
		bucket = cluster.openBucket(settings.bucketName());
	}
	
	public void done() {
		bucket.close();
		cluster.disconnect();
	}
	
	@Override
	public <S extends JSONObject> S save(S entity) {
		String key;
		try {
			key = "" + entity.getInt(settings.key());
			while (true) {
				try {
					bucket.upsert(JsonDocument.create(key, JsonObject.fromJson(entity.toString())));
					break;
				} catch (com.couchbase.client.java.error.TemporaryFailureException toofast) {
					System.out.println("Retrying element:" + key);
					Thread.sleep(100);
				} catch (RuntimeException tooslow) {
					if (tooslow.getCause() instanceof java.util.concurrent.TimeoutException) {
						System.out.println("Time out retrying element:" + key);
						Thread.sleep(100);
					} else {
						
					}
				}
			}
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
