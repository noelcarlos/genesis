package org.esmartpoint.genesis.helpers;

import org.bson.Document;
import org.esmartpoint.dbutil.Cronometro;
import org.esmartpoint.genesis.util.Stats;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

@Service
@Component
public class MongoDbHelper {
	MongoClient client;
	MongoDatabase database;
	
	public void openSession(String host, int port, String databaseName) {
		client = new MongoClient( host , port );
		database = client.getDatabase(databaseName);
	}
	
	public void closeSession() {
		if (client != null) {
			client.close();
		}
	}

	public void insert(String collectionName, String body) throws Exception {
		Cronometro.start("DATABASE");
	 
		try {
			database.getCollection(collectionName).insertOne(Document.parse(body));
			
			if (Stats.iterate(1000)) {
				Stats.printSpeed();
			}
		} finally {
			Cronometro.stop("DATABASE");
		}
	}

	public void dropCollection(String collectionName) throws Exception {
		Cronometro.start("DATABASE");
		
		try {
			database.getCollection(collectionName).drop();
		} finally {
			Cronometro.stop("DATABASE");
		}
	}
}
