package org.esmartpoint.genesis.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.esmartpoint.genesis.helpers.DbHelper;
import org.esmartpoint.genesis.helpers.GeneratorHelper;
import org.esmartpoint.genesis.plugins.CommandDispatcher;
import org.esmartpoint.genesis.util.ApplicationContextHolder;
import org.esmartpoint.genesis.util.DocumentFactoryWithLocator;
import org.esmartpoint.genesis.util.GenesisRuntimeException;
import org.esmartpoint.genesis.util.GokuSAXReader;
import org.esmartpoint.genesis.util.XMLProccesorContext;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.expression.Expression;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.LocatorImpl;
//import org.ridermove.util.ElasticSearchService;

/**
 * Hello world!
 *
 */
public class XMLProccesor {
	static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XMLProccesor.class);
	
	Document doc = null;

	private FileSystemResource currentFile;
	private Element currentNode;
	private XMLProccesor parent;
	
	DbHelper dbHelper;
	GeneratorHelper generatorHelper;
	XMLProccesorContext context;
	
	private CommandDispatcher commandDispatcher;

	static HashMap<String, Expression> expressionMapCache = new HashMap<String, Expression>();

//	@Autowired private ElasticSearchService elasticSearchService;
	public XMLProccesor(XMLProccesor parent, CommandDispatcher commandDispatcher) {
		this.parent = parent;

		ApplicationContext springContext = ApplicationContextHolder.getApplicationContext();
		
		this.generatorHelper = springContext.getBean(GeneratorHelper.class);
		this.dbHelper = springContext.getBean(DbHelper.class);
		this.setCommandDispatcher(commandDispatcher);
	}
	
	public void setup() {
//    	dbHelper.createConnection("org.postgresql.Driver","wwwwww",
//   			"jdbc:postgresql://localhost:5432/ridermove?charSet=UTF8","postgres");
//    	dbHelper.begin();
//    	HashMap<String, Object> values = new HashMap<String, Object>();
//    	values.put("modified_on", generatorHelper.randomDate(DateTime.now(), DateTime.now().plusYears(10)));
//    	values.put("num_nolikes", 827);
//    	values.put("num_documents", 584);
//    	values.put("entity_class_id", 1);
//    	values.put("human_url_name", "jose-luis-gallardo-blanco");
//    	values.put("num_suggests", 357);
//    	values.put("num_posts", 470);
//    	values.put("created_by", null);
//    	values.put("num_tasks", 724);
//    	values.put("num_comments", 963);
//    	values.put("num_evaluations", 67);
//    	values.put("is_deleted", true);
//    	values.put("created_on", generatorHelper.randomDate(DateTime.now(), DateTime.now().plusYears(10)));
//    	values.put("num_members", 343);
//    	values.put("num_questions", 953);
//    	values.put("name", "José Luis Gallardo Blanco");
//    	values.put("modified_by", null);
//    	values.put("rank", 4);
//    	values.put("num_followers", 981);
//    	values.put("num_likes", 324);
//    	values.put("num_visits", 2344);
//    	values.put("num_notes", 866);
//    	
//    	dbHelper.insert("snm_entities", values, null);
//    	dbHelper.commit();
//    	dbHelper.closeConnection();
	}

	public void load(File file) throws FileNotFoundException, DocumentException, IOException, SAXException {
		Locator locator = new LocatorImpl();
		DocumentFactory docFactory = new DocumentFactoryWithLocator(locator);
		SAXReader reader = new GokuSAXReader(docFactory, locator);
		doc = reader.read(file);
	}
	
	public void init() {
		context = new XMLProccesorContext(this, generatorHelper, dbHelper);
		context.init();
	}
	
	public void done() {
		context.done();
	}
	
	public void executeNode(Element node) throws GenesisRuntimeException {
		currentNode = node;

		if (node.getName().equals("input")) {
			//doInput(node);
		}
		if (node.getName().equals("output")) {
			//doInput(node);
		}
		
		String namespace = node.getNamespacePrefix();
		boolean res;
		if (StringUtils.isEmpty(namespace))
			res = commandDispatcher.getPlugin("<DEFAULT>").dispatch(context, node);
		else
			res = commandDispatcher.getPlugin(namespace).dispatch(context, node);
		if (!res) {
			throw context.createExceptionForNode(null, node, "node executor not found [" + node.getName() + "]");
		}
			
	}
	
	@SuppressWarnings("unchecked")
	public HashMap<String, Object> execute(String file, HashMap<String, Object> input) throws GenesisRuntimeException {
		context.logInfo("Executing: " + file);
		
		try {
			if (parent == null) { 
				setCurrentFile(new FileSystemResource(file));
				load(new FileSystemResource(file).getFile());
			} else {
				setCurrentFile((FileSystemResource)parent.getCurrentFile().createRelative(file));
				load(parent.getCurrentFile().createRelative(file).getFile());
			}
		} catch (Exception exp) {
			throw context.createExceptionForNode(exp, null);
		}
		
		List<Element> res = doc.selectNodes("/module/*");
		for (Element key : res) {
			if (key.getName().equals("input")) {
				context.getVariables().put(key.valueOf("@name"), input.get(key.valueOf("@name")));
			}
		}
		
		for(Element node: res) {
			executeNode(node);
		}
		
		HashMap<String, Object> resOutput = new HashMap<String, Object>();
		for (Element key : res) {
			if (key.getName().equals("output")) { 
				resOutput.put(key.valueOf("@name"), context.getVariables().get(key.valueOf("@name")));
			}
		}
		
		return resOutput;
	}


//	@SuppressWarnings("unchecked")
//	@Transactional(readOnly = false)
//	private void executeEsInsert(Element node) throws Exception {
//		String varName = null;
//		HashMap<String, Object> res = null;
//		try {
//			//context.setVariables(variables);
//			varName = node.valueOf("@value");
//			res = (HashMap<String, Object>)variables.get(node.valueOf("@value"));
//			elasticSearchService.setIndexName(node.valueOf("@index"));
//			res.put(node.valueOf("@key"), elasticSearchService.save(node.valueOf("@type"), null, res, null));
//			//insert(node.valueOf("@table"), res, node.valueOf("@key"));
//			variables.put(varName, res);
//			//context.setVariable(varName, res);
//		} catch (Exception exp) {
//			logger.error("DBInsert Error, params: "+ varName + "=" + res);
//			throw exp;
//		}
//	}
	
	/*
	 	FIXED EXAMPLE
	 
	  	<selector var="fileExtension">
			<item weight="10" value="null"/>
	 		<item weight="70" value="'jpg'"/>
	 		<item weight="20" value="'png'"/>
	 		<item weight="5" value="'bmp'"/>
	 	</selector>
	 
		ENTITY SELECTOR EXAMPLE

		<selector var="entitySelector" items="entityList" weightNull="0" weightValue="100" />
		
	 	SQL BASED SELECTOR EXAMPLE 
	 	
 		<selector var="lastNameSelector" weightNull="0" keyValue="apellido" keyWeight="frecuencia" >
			<sql>
				SELECT * FROM datagen.src_apellidos
			</sql>
		</selector>

		SQL BASED SELECTOR EXAMPLE

 		<selector var="userSelector" weightNull="10" weightValue="90" >
			<sql>
				SELECT * FROM snm_users
			</sql>
		</selector>
	 */
	
	public FileSystemResource getCurrentFile() {
		return currentFile;
	}

	public void setCurrentFile(FileSystemResource currentFile) {
		this.currentFile = currentFile;
	}

	public Element getCurrentNode() {
		return currentNode;
	}

	public void setCurrentNode(Element currentNode) {
		this.currentNode = currentNode;
	}

	public XMLProccesor getParent() {
		return parent;
	}

	public void setParent(XMLProccesor parent) {
		this.parent = parent;
	}

	public CommandDispatcher getCommandDispatcher() {
		return commandDispatcher;
	}

	public void setCommandDispatcher(CommandDispatcher commandDispatcher) {
		this.commandDispatcher = commandDispatcher;
	}
}
