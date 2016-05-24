package org.esmartpoint.genesis.generator;

import java.util.HashMap;

import org.esmartpoint.dbutil.Cronometro;
import org.esmartpoint.genesis.output.provider.CouchbaseRepositoryProvider;
import org.esmartpoint.genesis.output.provider.CouchbaseRepositorySettings;
import org.esmartpoint.genesis.output.provider.ElasticSearchRepositoryProvider;
import org.esmartpoint.genesis.output.provider.ElasticSearchRepositorySettings;
import org.esmartpoint.genesis.output.provider.FileSystemRepositoryProvider;
import org.esmartpoint.genesis.output.provider.FileSystemRepositorySettings;
import org.esmartpoint.genesis.output.provider.IDataRepository;
import org.esmartpoint.genesis.output.provider.PostgresqlRepositoryProvider;
import org.esmartpoint.genesis.output.provider.PostgresqlRepositorySettings;
import org.esmartpoint.genesis.plugins.CommandDispatcher;
import org.esmartpoint.genesis.plugins.units.DbCommandUnit;
import org.esmartpoint.genesis.plugins.units.DefaultCommandUnit;
import org.esmartpoint.genesis.plugins.units.HttpCommandUnit;
import org.esmartpoint.genesis.plugins.units.LogicCommandUnit;
import org.esmartpoint.genesis.plugins.units.MongoDbCommandUnit;
import org.esmartpoint.genesis.scripts.CouchbaseGeneratorScript;
import org.esmartpoint.genesis.scripts.DBRidermoveGeneratorScript;
import org.esmartpoint.genesis.scripts.ElasticRidermoveGeneratorScript;
import org.esmartpoint.genesis.scripts.MongoDbRidermoveGeneratorScript;
import org.esmartpoint.genesis.scripts.PostgresqlGeneratorScript;
import org.esmartpoint.genesis.scripts.UserGeneratorScript;
import org.esmartpoint.genesis.util.ApplicationContextHolder;
import org.esmartpoint.genesis.util.GenesisRuntimeException;
import org.esmartpoint.genesis.util.GokuElement;
import org.esmartpoint.genesis.util.Stats;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Hello world!
 *
 */
public class GenesisApp 
{
	static int MAX_USERS = 100;
	static int MAX_GROUPS = 50;
	static int MAX_ENTITIES = 50;
	static public DateTime start;
	
	public static void main( String[] args )
    {
        ApplicationContext springContext = new ClassPathXmlApplicationContext("application-config.xml");
    	ApplicationContextHolder.init(springContext);

        try {
        	start = DateTime.now();
        	Cronometro.inicializar();
        	CommandDispatcher commandDispatcher = new CommandDispatcher();

        	commandDispatcher.register("<DEFAULT>", DefaultCommandUnit.class);
        	commandDispatcher.register("db", DbCommandUnit.class);
        	commandDispatcher.register("logic", LogicCommandUnit.class);
        	commandDispatcher.register("http", HttpCommandUnit.class);
        	commandDispatcher.register("mongodb", MongoDbCommandUnit.class);
        	
    		XMLProccesor xmlProccesor = new XMLProccesor(null, commandDispatcher);
        	xmlProccesor.init();
        	xmlProccesor.setup();
        	commandDispatcher.setupPlugins(null);

        	//HashMap<String, Object> input = new HashMap<String, Object>();
        	//xmlProccesor.execute("data/test-followfun/main.xml", input);

        	//xmlProccesor.execute("data/test-elasticsearch/main.xml", input);
        	//xmlProccesor.execute("data/test-mongodb/main.xml", input);
        	//Cronometro.trace();
        	
        	//ElasticRidermoveGeneratorScript script = springContext.getBean(ElasticRidermoveGeneratorScript.class);
        	//DBRidermoveGeneratorScript script = springContext.getBean(DBRidermoveGeneratorScript.class);
        	//MongoDbRidermoveGeneratorScript script = springContext.getBean(MongoDbRidermoveGeneratorScript.class);
        	//CouchbaseGeneratorScript script = springContext.getBean(CouchbaseGeneratorScript.class);
        	//PostgresqlGeneratorScript script = springContext.getBean(PostgresqlGeneratorScript.class);
        	
        	CouchbaseRepositoryProvider repository = new CouchbaseRepositoryProvider();
        	
        	repository.setSettings(CouchbaseRepositorySettings.builder()
        		.nodes("192.168.1.14")
        		.key("id")
        		.bucketName("allianz"));
        	
//        	IDataRepository repository = new FileSystemRepositoryProvider(FileSystemRepositorySettings.builder()
//        		.directory("d:/data/users")
//        		.key("id")
//        		.levels(1));
//        	PostgresqlRepositoryProvider repository = springContext.getBean(PostgresqlRepositoryProvider.class);
//        	repository.setSettings(PostgresqlRepositorySettings.builder()
//        		.table("users")
//        		.key("id"));
        	
//        	ElasticSearchRepositoryProvider repository = springContext.getBean(ElasticSearchRepositoryProvider.class);
//        	repository.setSettings(ElasticSearchRepositorySettings.builder()
//        		.url("http://localhost:9200")
//        		.index("ridermove")
//        		.type("users")
//        		.key("id"));
        	
        	repository.init();

        	UserGeneratorScript script = springContext.getBean(UserGeneratorScript.class);
        	script.setDataRepository(repository);
        	
        	Stats.init();
        	script.run();
        	
        	repository.done();
        	Stats.done();

//        	System.out.println("EVALUATIONS operations took, " +  PeriodFormat.getDefault().print(new Period(Cronometro.get("EVALUATIONS").sum)));
//        	System.out.println("DATABASE operations took, " +  PeriodFormat.getDefault().print(new Period(Cronometro.get("DATABASE").sum)));
//        	System.out.println("ELASTICSEARCH operations took, " +  PeriodFormat.getDefault().print(new Period(Cronometro.get("ELASTICSEARCH").sum)));
        	//System.out.println("operations took, " +  PeriodFormat.getDefault().print(new Period(Cronometro.get("DATABASE").sum)));
        	System.out.println("Terminado en, " + PeriodFormat.getDefault().print(new Period(GenesisApp.start, DateTime.now())));
        	
        	commandDispatcher.cleanupPlugins(null);
        	xmlProccesor.done();
		} catch (GenesisRuntimeException e) {
			if (e.getCause() != null)
				e.getCause().printStackTrace();
			else
				System.err.println("Genesis runtime error:" + e.getMessage());
			System.err.println("XML STACK TRACE:");
//			System.err.println("at org.esmartpoint.genesis.generator.XMLProccesor.evaluate(XMLProccesor.java:259)");
//			System.err.println("at data.test.generate-likes.Generate(generate-likes.xml:259)");
//			System.err.println("at data.test.generateLikes.Generate(generateLikes.java:259)");
			XMLProccesor xmlProccesor = e.getXmlProccesor();
			GokuElement node = e.getElement();
			while (xmlProccesor != null) {
				String fileName = xmlProccesor.getCurrentFile().getFile().toString().replaceAll("\\\\", "/");
				System.err.println("\tExecutionException: " + "/" + fileName    
					+ " @" +  node.getLineNumber() + "," + node.getColumnNumber());
				xmlProccesor = xmlProccesor.getParent();
				if (xmlProccesor != null) {
					node = (GokuElement)xmlProccesor.getCurrentNode();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
