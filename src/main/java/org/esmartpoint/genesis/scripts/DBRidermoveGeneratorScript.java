package org.esmartpoint.genesis.scripts;

import java.util.HashMap;

import org.esmartpoint.genesis.helpers.DbHelper;
import org.esmartpoint.genesis.helpers.GeneratorHelper;
import org.esmartpoint.genesis.helpers.HttpHelper;
import org.esmartpoint.genesis.selectors.WeightedEntitySelector;
import org.esmartpoint.genesis.selectors.WeightedItemSelector;
import org.esmartpoint.genesis.selectors.WeightedMapSelector;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DBRidermoveGeneratorScript {
	@Autowired GeneratorHelper generator;
	@Autowired HttpHelper httpHelper;
	@Autowired DbHelper dbHelper;
	
	int MAX_USERS = 200000000;
	int MAX_LUGARES = 100;
	String DIALECT = "postgresql";
	
	public DBRidermoveGeneratorScript() {
		
	}
	
	public void run() throws Exception {
		generateMain();
	}
	
	private void generateMain() throws Exception {
		dbHelper.createConnection(null, "org.postgresql.Driver", 
			"jdbc:postgresql://localhost:5432/ridermove?charSet=UTF8", 
			"wwwwww", "postgres", "org.hibernate.dialect.PostgreSQLDialect");
		dbHelper.begin(null);

		if (DIALECT.equals("postgresql")) {
			dbHelper.script(null, 
				"delete from snm_entities;" + 
				"delete from snm_users;" + 
				"delete from snm_user_roles;" + 
				"delete from user_profile;" + 
				"delete from snm_archives;" + 
				"delete from snm_entity_followers;" + 
				"delete from snm_entity_tasks;" + 
				"delete from lugares;" + 
				"delete from snm_activities;");
		}
		if (DIALECT.equals("mysql")) {
			dbHelper.script(null, 
				"delete from snm_entities;" + 
				"delete from snm_users;" + 
				"delete from snm_user_roles;" + 
				"delete from user_profile;" + 
				"delete from snm_archives;" + 
				"delete from snm_entity_followers;" + 
				"delete from snm_entity_tasks;" + 
				"delete from lugares;" + 
				"delete from snm_activities;" +
				"ALTER TABLE snm_entities AUTO_INCREMENT=1;" +
				"ALTER TABLE snm_users AUTO_INCREMENT=1;" +
				"ALTER TABLE snm_archives AUTO_INCREMENT=1;" +
				"ALTER TABLE snm_user_roles AUTO_INCREMENT=1;" +
				"ALTER TABLE lugares AUTO_INCREMENT=1;" +
				"ALTER TABLE snm_activities AUTO_INCREMENT=1;");
		}
		
		generateUsers();
		
		dbHelper.commit(null);
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
		
		HashMap<String, Object> entityClass = dbHelper.load(null, "select * from snm_classes where package_name='org.snm.core' and class_name='User'", null);
		
		for(int i=0; i<MAX_USERS; i++) {
			DateTime createAt = generator.randomDate(new org.joda.time.DateTime(2000, 1,  1, 0, 0), new org.joda.time.DateTime(2014, 1,  1, 0, 0));
			DateTime modifiedAt = generator.randomDate(new org.joda.time.DateTime(2000, 1,  1, 0, 0), new org.joda.time.DateTime(2014, 1,  1, 0, 0));

			String firstName = firstNameSelector.getNext().toString();
			String lastName1 = lastNameSelector.getNext().toString();
			String lastName2 = lastNameSelector.getNext().toString();
			DateTime lastLogonAt = generator.randomDate(createAt, modifiedAt);
			
			HashMap<String, Object> entity = new HashMap<String, Object>();
			entity.put("name", firstName + " " + lastName1 + " " + lastName2);
			entity.put("created_by", null);
			entity.put("created_on", createAt.toDate());
			entity.put("entity_class_id", entityClass.get("id"));
			entity.put("rank", rank.getNext());
			entity.put("human_url_name", generator.toHumanURL(entity.get("name").toString(), 128));
			entity.put("is_deleted", generator.randomBoolean(90, 10));
			entity.put("modified_by", null);
			entity.put("modified_on", createAt.toDate());
			
			entity.put("num_comments", generator.randomInt(0, 1000));
			entity.put("num_documents", generator.randomInt(0, 1000));
			entity.put("num_evaluations", generator.randomInt(0, 1000));
			entity.put("num_likes", generator.randomInt(0, 1000));
			entity.put("num_nolikes", generator.randomInt(0, 1000));
			entity.put("num_members", generator.randomInt(0, 1000));
			entity.put("num_posts", generator.randomInt(0, 1000));
			entity.put("num_tasks", generator.randomInt(0, 1000));
			entity.put("num_visits", generator.randomInt(0, 1000));
			entity.put("num_notes", generator.randomInt(0, 1000));
			entity.put("num_followers", generator.randomInt(0, 1000));
			entity.put("num_questions", generator.randomInt(0, 1000));
			entity.put("num_suggests", generator.randomInt(0, 1000));
			
			dbHelper.insert(null, "snm_entities", entity, "entity_id");

		}
	}
}
