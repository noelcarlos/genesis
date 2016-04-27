package org.esmartpoint.genesis.util;

import java.io.StringReader;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.esmartpoint.dbutil.Cronometro;
import org.esmartpoint.genesis.generator.XMLProccesor;
import org.esmartpoint.genesis.helpers.DbHelper;
import org.esmartpoint.genesis.helpers.GeneratorHelper;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.SpelParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import freemarker.template.Configuration;
import httl.Engine;
import httl.Template;

/**
 * Hello world!
 *
 */
public class XMLProccesorContext {
	static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XMLProccesorContext.class);
	
	private static Engine engine;
	Configuration cfg = new Configuration();
	
	private static HashMap<String, Object> globals = new HashMap<String, Object>();
	private HashMap<String, Object> variables = new HashMap<String, Object>();
	private HashMap<String, Element> references = new HashMap<String, Element>();
	Document doc = null;
	ExpressionParser parser;
	StandardEvaluationContext evaluationContext;
	TemplateParserContext templateParserContext;
	private XMLProccesor xmlProccesor;
	
	DbHelper dbHelper;
	GeneratorHelper generatorHelper;

	static HashMap<String, Expression> expressionMapCache = new HashMap<String, Expression>();
	static HashMap<String, Template> templateMapCache = new HashMap<String, Template>();

	DecimalFormat longIntegerFormat = (DecimalFormat)NumberFormat.getNumberInstance(new Locale("es", "es"));
	PeriodFormatter formatter = new PeriodFormatterBuilder()
	    .appendDays().appendSuffix(" days ")
	    .appendHours().appendSuffix(" hours ")
	    .appendMinutes().appendSuffix(" minutes ")
	    .appendSeconds().appendSuffix(" seconds ")
	    .appendWeeks().appendSuffix(" weeks ")
	    .printZeroNever()
	    .toFormatter();

//	@Autowired private ElasticSearchService elasticSearchService;
	public XMLProccesorContext(XMLProccesor xmlProccesor, GeneratorHelper generatorHelper, DbHelper dbHelper) {
		this.xmlProccesor = xmlProccesor;
		this.generatorHelper = generatorHelper;
		this.dbHelper = dbHelper;
		
    	Properties config = new Properties();
    	//config.put("import.variables", "Map user, List books");
    	config.put("interpreted", "true");
    	config.put("compiled", "false");

    	engine = Engine.getEngine(config);

    	longIntegerFormat.applyPattern("#,##0");
	}
	
	public void setup() {
		// start timer after initialization
	}

	public void init() {
    	parser = new SpelExpressionParser();
		evaluationContext = new StandardEvaluationContext();
		templateParserContext = new TemplateParserContext("${", "}");
		
		getVariables().put("generator", generatorHelper);
		getVariables().put("globals", getGlobals());
		getVariables().put("db", dbHelper);
		
		evaluationContext.setRootObject(getVariables());
		
		evaluationContext.addPropertyAccessor(new PropertyAccessor() {
			@Override @SuppressWarnings({ "rawtypes", "unchecked" })
			public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
				if (target instanceof Map) {
					((Map)target).put(name, newValue);
				} else {
					try {
						((JSONObject)target).put(name, newValue);
					} catch (JSONException e) {
						throw new AccessException(e.getMessage(), e);
					}
				}
			}

			@Override @SuppressWarnings("rawtypes")
			public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
				if (target instanceof Map) {
					return new TypedValue(((Map)target).get(name));
				} else {
					try {
						return new TypedValue(((JSONObject)target).get(name));
					} catch (JSONException e) {
						throw new AccessException(e.getMessage(), e);
					}
				}
			}
			
			@Override
			public Class<?>[] getSpecificTargetClasses() {
				return new Class[] {HashMap.class, JSONObject.class};
			}
			
			@Override
			public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
				return true;
			}
			
			@Override
			public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
				return true;
			}
			
		});
	}
	
	public void done() {
	}
	
	public Object evaluateExpression(Node node, String str) throws GenesisRuntimeException {
		GokuElement e = (GokuElement)node;
		try {
			Cronometro.start("EVALUATIONS");
			Expression expression = getEvaluateExpressionFromCache(e.getLineNumber(), e.getColumnNumber(), 
				getXmlProccesor().getCurrentFile().getFile().getAbsolutePath(), str);
			Object value = expression.getValue(evaluationContext);
			return value;
		} catch (Exception evalExc) {
			evalExc.printStackTrace();
			throw new GenesisRuntimeException(xmlProccesor, e, evalExc.getMessage() + " [" + str + "]", evalExc); 
		} finally {
			Cronometro.stop("EVALUATIONS");
		}
	}
	
	public <T> T evaluateExpression(Node node, String str, Class<T> desiredResultType) throws GenesisRuntimeException {
		GokuElement e = (GokuElement)node;
		try {
			Cronometro.start("EVALUATIONS");
			Expression expression = getEvaluateExpressionFromCache(e.getLineNumber(), e.getColumnNumber(), 
				getXmlProccesor().getCurrentFile().getFile().getAbsolutePath(), str);
			T value = expression.getValue(evaluationContext, desiredResultType);
			return value;
		} catch (SpelParseException parseExc) {
			throw new GenesisRuntimeException(xmlProccesor, e, parseExc.getMessage() + " [" + str + "]", parseExc);
		} catch (EvaluationException evalExc) {
			throw new GenesisRuntimeException(xmlProccesor, e, evalExc.getMessage() + " [" + str + "]", evalExc); 
		} catch (Exception evalExc) {
			evalExc.printStackTrace();
			throw new GenesisRuntimeException(xmlProccesor, e, evalExc.getMessage() + " [" + str + "]", evalExc); 
		} finally {
			Cronometro.stop("EVALUATIONS");
		}
	}
	
	private Expression getEvaluateExpressionFromCache(int lineNumber, int columnNumber, String absoluteTemplatePath, String str) {
		String key = str;
		Expression res = expressionMapCache.get(key); 
		if (res == null) {
			res = parser.parseExpression(str); 
			expressionMapCache.put(key, res);
		}
		return res;
	}

	private Template getTemplateFromCache(String str) throws ParseException {
		String key = str;
		Template res = templateMapCache.get(key); 
		if (res == null) {
			res = engine.parseTemplate(str);
			templateMapCache.put(key, res);
		}
		return res;
	}
	
	public String evaluateTemplate(Node node, String str) throws GenesisRuntimeException {
		try {
			Cronometro.start("EVALUATIONS");
			StringWriter writer = new StringWriter(64*1024);
    		Template template = getTemplateFromCache(str);
    		template.render(getVariables(), writer);
	        writer.flush();
	        writer.close();
			return writer.toString();
		} catch (Exception ext) {
			GokuElement e = (GokuElement)node;
			throw new GenesisRuntimeException(xmlProccesor, e, ext.getMessage() + " [" + str + "]", ext); 
		} finally {
			Cronometro.stop("EVALUATIONS");
		}
	}

	@SuppressWarnings("unused")
	private String evaluateTemplateVelocity(Node node, String str) throws GenesisRuntimeException {
		try {
			VelocityContext context = new VelocityContext();

			Iterator<Entry<String, Object>> it = getVariables().entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry<String, Object> pair = (Map.Entry<String, Object>)it.next();
		        context.put(pair.getKey(), pair.getValue());
		    }

			StringWriter writer = new StringWriter();
			
			StringReader reader = new StringReader(str);
	        Velocity.evaluate(context, writer, "noname.vm", reader);
	        writer.flush();
	        writer.close();
	        
			String value = writer.toString();
			return value;
		} catch (Exception ext) {
			GokuElement e = (GokuElement)node;
			throw new GenesisRuntimeException(xmlProccesor, e, ext.getMessage() + " [" + str + "]", ext); 
		} 
	}

	public HashMap<String, Object> getVariables() {
		return variables;
	}

	public void setVariables(HashMap<String, Object> variables) {
		this.variables = variables;
		evaluationContext.setVariables(variables);
	}
	
	public void logError(String msg) {
		logger.error(msg);
	}
	
	public void logInfo(String msg) {
		logger.info(msg);
	}
	
	public void logDebug(String msg) {
		logger.debug(msg);
	}
	
	public GenesisRuntimeException createExceptionForNode(Exception exp, Node node) {
		GokuElement e = (GokuElement)node;
		return new GenesisRuntimeException(xmlProccesor, e, exp.getMessage(), exp);
	}
	
	public GenesisRuntimeException createExceptionForNode(Exception exp, Node node, String message) {
		GokuElement e = (GokuElement)node;
		return new GenesisRuntimeException(xmlProccesor, e, message, exp);
	}

	public HashMap<String, Element> getReferences() {
		return references;
	}

	public void setReferences(HashMap<String, Element> references) {
		this.references = references;
	}

	public void executeNode(Element n) throws GenesisRuntimeException {
		xmlProccesor.executeNode(n);
	}

	public HashMap<String, Object> getGlobals() {
		return globals;
	}

	public void setGlobals(HashMap<String, Object> globals) {
		XMLProccesorContext.globals = globals;
	}

	public XMLProccesor getXmlProccesor() {
		return xmlProccesor;
	}

	public void setXmlProccesor(XMLProccesor xmlProccesor) {
		this.xmlProccesor = xmlProccesor;
	}

}
