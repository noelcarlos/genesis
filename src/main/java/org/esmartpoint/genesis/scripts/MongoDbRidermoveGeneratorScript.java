package org.esmartpoint.genesis.scripts;

import java.util.HashMap;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.esmartpoint.genesis.helpers.DbHelper;
import org.esmartpoint.genesis.helpers.GeneratorHelper;
import org.esmartpoint.genesis.helpers.MongoDbHelper;
import org.esmartpoint.genesis.selectors.WeightedEntitySelector;
import org.esmartpoint.genesis.selectors.WeightedItemSelector;
import org.esmartpoint.genesis.selectors.WeightedMapSelector;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MongoDbRidermoveGeneratorScript {
	@Autowired GeneratorHelper generator;
	@Autowired DbHelper dbHelper;
	@Autowired MongoDbHelper mongoDbHelper;
	
	int MAX_USERS = 200000000;
	int MAX_LUGARES = 100;
	
	public MongoDbRidermoveGeneratorScript() {
		
	}
	
	public void run() throws Exception {
		generateMain();
	}
	
	private void generateMain() throws Exception {
//		dbHelper.createConnection(null, "org.postgresql.Driver", 
//			"jdbc:postgresql://localhost:5432/ridermove?charSet=UTF8", 
//			"wwwwww", "postgres", "org.hibernate.dialect.PostgreSQLDialect");
		dbHelper.createConnection(null, "com.mysql.jdbc.Driver", 
				"jdbc:mysql://localhost:3306/ridermove-test?charSet=UTF8", 
				"wwwwww", "root", "org.hibernate.dialect.MySQL5InnoDBDialect");
		
		mongoDbHelper.openSession("192.168.1.11", 27017, "allianz");

		//mongoDbHelper.dropCollection("users");
		
		generateUsers();

		mongoDbHelper.closeSession();
		
		dbHelper.closeConnection(null);
	}
	
	private void generateUsers() throws Exception {
		WeightedItemSelector<Boolean> isDeleted =  new WeightedItemSelector<Boolean>()
			.add(10, true)
			.add(90, false)
			.build();
		WeightedItemSelector<Boolean> isActive =  new WeightedItemSelector<Boolean>()
			.add(10, true)
			.add(90, false)
			.build();
		WeightedItemSelector<String> browserType =  new WeightedItemSelector<String>()
			.add(30, "Firefox")
			.add(40, "Chrome")
			.add(25, "IE")
			.add(5, "Other")
			.build();
		WeightedItemSelector<String> fileExtension =  new WeightedItemSelector<String>()
				.add(75, "jpg")
				.add(20, "png")
				.add(5, "bmp")
				.build();
		WeightedItemSelector<String> emailDomain =  new WeightedItemSelector<String>()
			.add(10, "ridermove.com")
			.add(30, "hotmail.com")
			.add(40, "gmail.com")
			.add(5, "yahoo.com")
			.add(5, "aol.com")
			.build();
		WeightedItemSelector<Integer> rank =  new WeightedItemSelector<Integer>()
			.add(10, 1)
			.add(20, 2)
			.add(30, 3)
			.add(50, 4)
			.add(10, 5)
			.build();
		WeightedMapSelector<Object> lastNameSelector = new WeightedMapSelector<Object>(
			WeightedMapSelector.readFromXMLResource("data/resources/src_apellidos.xml"), 0, "apellido", "frecuencia")
			.build();
		WeightedMapSelector<Object> firstNameSelector = new WeightedMapSelector<Object>(
			WeightedMapSelector.readFromXMLResource("data/resources/src_nombres.xml"), 0, "nombre", "frecuencia")
			.build();		
		WeightedEntitySelector<Object> deporteSelector = new WeightedEntitySelector<Object>(
			dbHelper.listOneField(null, "SELECT t.* FROM snm_categories t INNER JOIN snm_contexts c ON t.context_id = c.context_id WHERE c.name = 'deporte'", null), 
			0, 100)
			.build();
		
		WeightedEntitySelector<Object> tagSelector = new WeightedEntitySelector<Object>(
			dbHelper.listOneField(null, "SELECT t.* FROM snm_tags t INNER JOIN snm_contexts c ON t.context_id = c.context_id WHERE c.name = 'lugar.tag'", null), 
			0, 100)
			.build();

		WeightedEntitySelector<Object> ciudadSelector = new WeightedEntitySelector<Object>(
			dbHelper.listOneField(null, "SELECT t.* FROM snm_categories t INNER JOIN snm_contexts c ON t.context_id = c.context_id WHERE c.name = 'ciudad'", null), 
			0, 100)
			.build();
		
		WeightedEntitySelector<Object> servicioSelector = new WeightedEntitySelector<Object>(
			dbHelper.listOneField(null, "SELECT t.* FROM snm_tags t INNER JOIN snm_contexts c ON t.context_id = c.context_id WHERE c.name = 'lugar.servicio'", null), 
			0, 100)
			.build();
	
		WeightedEntitySelector<Object> requisitoSelector = new WeightedEntitySelector<Object>(
			dbHelper.listOneField(null, "SELECT t.* FROM snm_tags t INNER JOIN snm_contexts c ON t.context_id = c.context_id WHERE c.name = 'lugar.requisito'", null), 
			0, 100)
			.build();
		
		for(int i=0; i<MAX_USERS; i++) {
			DateTime createAt = generator.randomDate(new org.joda.time.DateTime(2000, 1,  1, 0, 0), new org.joda.time.DateTime(2014, 1,  1, 0, 0));
			DateTime modifiedAt = generator.randomDate(new org.joda.time.DateTime(2000, 1,  1, 0, 0), new org.joda.time.DateTime(2014, 1,  1, 0, 0));

			String firstName = firstNameSelector.getNext().toString();
			String lastName1 = lastNameSelector.getNext().toString();
			String lastName2 = lastNameSelector.getNext().toString();
			DateTime lastLogonAt = generator.randomDate(createAt, modifiedAt);

			JSONObject body = new JSONObject();
			
			body.put("id", i+1);

			JSONObject entity = new JSONObject();
			entity.put("name", generator.randomTitle(10, 64, 2, 8));
			entity.put("entityClassName", "lugar");
			entity.put("entityClassPackageName", "org.rider.model");
			entity.put("entityClassClassName", "Lugar");
			entity.put("entityClassId", 100);
			entity.put("createdById", 1);
			entity.put("createdByAvatarLocation", "avatars/" + generator.randomFilename(10,28,3,8,fileExtension.getNext()));
			entity.put("createdByFirstName", "John");
			entity.put("createdOn", createAt.toDate());
			entity.put("modifiedOn", modifiedAt.toDate());
			entity.put("isDeleted", true);
			entity.put("humanUrlName", generator.toHumanURL(firstName, 128));
			entity.put("numLikes", generator.randomInt(0, 1000));
			entity.put("numVisits", generator.randomInt(0, 1000));
			entity.put("numComments", generator.randomInt(0, 1000));
			entity.put("numPosts", generator.randomInt(0, 1000));
			entity.put("numMembers", generator.randomInt(0, 1000));
			entity.put("numEvaluations", generator.randomInt(0, 1000));
			entity.put("numDocuments", generator.randomInt(0, 1000));
			entity.put("numNolikes", generator.randomInt(0, 1000));
			entity.put("numTasks", generator.randomInt(0, 1000));
			entity.put("numFollowers", generator.randomInt(0, 1000));
			entity.put("numQuestions", generator.randomInt(0, 1000));
			entity.put("numFollowers", generator.randomInt(0, 1000));
			entity.put("numSuggests", generator.randomInt(0, 1000));
			entity.put("rank", generator.randomInt(0, 5));

			JSONArray categories = new JSONArray();
			deporteSelector.restart();
			for(int j = 0; j< generator.randomInt(1, 5); j++) {
			  	HashMap<String, Object> value = (HashMap<String, Object>)deporteSelector.nextUnique();
				categories.put(new JSONObject()
					.put("categoryId", value.get("category_id"))
					.put("name", value.get("name"))
					.put("contextId", 104)
					.put("contextName", "deporte"));
			}
			entity.put("categories", categories);
			
			JSONArray tags = new JSONArray();
			tagSelector.restart();
			for(int j = 0; j< generator.randomInt(1, 5); j++) {
			  	HashMap<String, Object> value = (HashMap<String, Object>)tagSelector.nextUnique();
			  	tags.put(new JSONObject()
					.put("tagId", value.get("id"))
					.put("name", value.get("name"))
					.put("contextId", 130)
					.put("contextName", "lugar.tag"));
			}
			entity.put("tags", tags);
			
			body.put("entity", entity);
			
			body.put("description", generator.randomParagraph(128, 500, 10, 64, 2, 8));
			body.put("firstName", firstName);
			body.put("lastName", lastName1 + " " + lastName2);
			body.put("isDeleted", generator.randomBoolean(90, 10));
			body.put("isActive", generator.randomBoolean(90, 10));
			body.put("lastLogonAt", lastLogonAt.toDate());
			body.put("lastLogonIp", generator.randomIP());
			body.put("browserType", browserType.getNext());
			body.put("email", firstName.toLowerCase() + "." + lastName1.toLowerCase() + "@" + emailDomain.getNext());
			body.put("password", "secret");
			body.put("resumen", generator.randomParagraph(128, 500, 10, 64, 2, 8));
			body.put("presentacionPersonal", generator.randomTitle(10, 50, 2, 8));
			body.put("actionbarBackgroundColor", generator.randomHexNumeric(6));
			
			JSONArray servicios = new JSONArray();
			servicioSelector.restart();
			for(int j = 0; j< generator.randomInt(1, 5); j++) {
			  	HashMap<String, Object> value = (HashMap<String, Object>)servicioSelector.nextUnique();
			  	servicios.put(new JSONObject()
					.put("tagId", value.get("id"))
					.put("name", value.get("name"))
					.put("contextId", 127)
					.put("contextName", "lugar.servicio"));
			}
			body.put("servicios", servicios);
			
			requisitoSelector.restart();
			JSONArray requisitos = new JSONArray();
			for(int j = 0; j< generator.randomInt(1, 5); j++) {
			  	HashMap<String, Object> value = (HashMap<String, Object>)requisitoSelector.nextUnique();
			  	requisitos.put(new JSONObject()
					.put("tagId", value.get("id"))
					.put("name", value.get("name"))
					.put("contextId", 128)
					.put("contextName", "lugar.requisito"));
			}
			body.put("requisitos", requisitos);
			
			ciudadSelector.restart();
			JSONArray locations = new JSONArray();
			for(int j = 0; j< generator.randomInt(1, 5); j++) {
			  	HashMap<String, Object> value = (HashMap<String, Object>)ciudadSelector.nextUnique();
			  	locations.put(new JSONObject()
					.put("categoryId", value.get("category_id"))
					.put("name", value.get("name"))
					.put("contextId", 126)
					.put("contextName", "ciudad"));
			}
			body.put("locations", locations);
			
			mongoDbHelper.insert("users", body.toString());
		}
	}
}
