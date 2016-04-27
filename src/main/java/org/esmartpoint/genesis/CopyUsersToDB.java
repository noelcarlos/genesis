package org.esmartpoint.genesis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.esmartpoint.dbutil.DBRecordService;
import org.esmartpoint.dbutil.Record;
import org.hibernate.transform.Transformers;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = false)
public class CopyUsersToDB {
    static final Logger log = Logger.getLogger(CopyUsersToDB.class);

	protected DateTime start;
	@Autowired DataService dataService;
	@Autowired DBRecordService dbRecordService;
	
	public CopyUsersToDB() {
		
	}

	public void setUp() throws Exception {
		start = DateTime.now();
	}
	
	@SuppressWarnings("unchecked")
	@Transactional(readOnly=false)
	public void doItAll() throws Exception {
		int cantidad = ((Number)dataService.getSession()
			.createSQLQuery("SELECT COUNT(*) FROM `ridermove-dev`.snm_users")
			.uniqueResult())
			.intValue();
		List<Record> userList = dataService.getSession()
			.createSQLQuery("SELECT * FROM `ridermove-dev`.snm_users")
			.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
			.list();
		
		List<Record> linkedinUserList = dataService.getSession()
			.createSQLQuery("select * from test_data.src_linkedin l where l.image_link <> "
				+ "'https://static.licdn.com/scds/common/u/images/themes/katy/ghosts/person/ghost_person_60x60_v1.png' limit " + cantidad + " offset 15000")
			.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
			.list();
		
		for (int i = 0; i < userList.size(); i++) {
			Record srcUser = new Record(linkedinUserList.get(i));
			Record trgUser = new Record(userList.get(i));

			trgUser.put("first_name", srcUser.get("name"));
			trgUser.put("last_name", srcUser.get(""));
			trgUser.put("email", StringNormalizer.stringToEmail(srcUser.get("name").toString() + " " + trgUser.get("user_id"), "ridermove.com"));

			Record archive = dbRecordService.load("snm_archives", trgUser.getInt("avatar_id"));

			archive.put("name", srcUser.get("key_id"));
			archive.put("type", "avatar");
			
			String inpFilePath = "C:\\tmp\\linkedin\\";
			String outPath = "C:\\JavaAppData\\ridermove\\repository\\resources\\avatars\\";
			archive.put("file_size", new File(inpFilePath + srcUser.get("key_id") + "-l.jpg").length());
			archive.put("location", "avatars/" + srcUser.get("key_id") + "-l.jpg"); 
			archive.put("created_at", new Date());
			archive.put("created_by_id", 1);
			
			try {
				IOUtils.copy(new FileInputStream(inpFilePath + srcUser.get("key_id") + "-l.jpg"), new FileOutputStream(outPath + srcUser.get("key_id") + "-l.jpg"));
				IOUtils.copy(new FileInputStream(inpFilePath + srcUser.get("key_id") + "-m.jpg"), new FileOutputStream(outPath + srcUser.get("key_id") + "-m.jpg"));
				IOUtils.copy(new FileInputStream(inpFilePath + srcUser.get("key_id") + "-s.jpg"), new FileOutputStream(outPath + srcUser.get("key_id") + "-s.jpg"));
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			dbRecordService.save("snm_archives", archive);
			dbRecordService.save("snm_users", trgUser);

			Record upData = dbRecordService.load("user_profile", (Integer)trgUser.get("user_id"));
			
			if (upData != null) {
				if (srcUser.get("title") != null)
					upData.put("presentacion_personal", srcUser.get("title"));
				else
					upData.put("presentacion_personal", "No está informado");
				upData.put("resumen", "No está informado");
				if (srcUser.get("title") != null)
					upData.put("industry", srcUser.get("industry"));
				else
					upData.put("industry", "No está informado");
				dbRecordService.save("user_profile", upData);
			}
			
			Record snmUserData = dbRecordService.load("snm_entities", (Integer)trgUser.get("user_id"));
			snmUserData.put("name", srcUser.get("name"));
			snmUserData.put("categories", srcUser.get("location"));
			dbRecordService.save("snm_entities", snmUserData);
			
			//upData.put("presentacion_personal", srcUser.get("location"));
		}
	}

	@SuppressWarnings("resource")
	public static void main( String[] args )
    {	
		//ElasticSearchService elasticSearchService = (ElasticSearchService) context.getBean("elasticSearchService");
		
        try {
            ApplicationContext context = new ClassPathXmlApplicationContext("application-config.xml");
            
        	CopyUsersToDB app = context.getBean(CopyUsersToDB.class);

        	app.setUp();
        	app.doItAll();
        	
			System.out.println("Terminado en, " + PeriodFormat.getDefault().print(new Period(app.start, DateTime.now())));
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
	
}
