package org.esmartpoint.genesis.plugins.units;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.codehaus.jettison.json.JSONObject;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.Node;
import org.esmartpoint.genesis.builders.AbstractBuilder;
import org.esmartpoint.genesis.builders.ChildBuilder;
import org.esmartpoint.genesis.builders.FixedBuilder;
import org.esmartpoint.genesis.builders.IObjectCreator;
import org.esmartpoint.genesis.generator.Range;
import org.esmartpoint.genesis.generator.XMLProccesor;
import org.esmartpoint.genesis.helpers.DbHelper;
import org.esmartpoint.genesis.plugins.IXMLCommandUnit;
import org.esmartpoint.genesis.selectors.WeightedEntitySelector;
import org.esmartpoint.genesis.selectors.WeightedItemSelector;
import org.esmartpoint.genesis.selectors.WeightedMapSelector;
import org.esmartpoint.genesis.util.ApplicationContextHolder;
import org.esmartpoint.genesis.util.GenesisRuntimeException;
import org.esmartpoint.genesis.util.XMLProccesorContext;
import org.springframework.context.ApplicationContext;

import httl.util.StringUtils;

public class DefaultCommandUnit implements IXMLCommandUnit {
	DbHelper dbHelper;
	
	@Override
	public void setup(XMLProccesorContext context) {
		ApplicationContext springContext = ApplicationContextHolder.getApplicationContext();
		
		dbHelper = springContext.getBean(DbHelper.class);
	}

	@Override
	public boolean dispatch(XMLProccesorContext context, Element node) throws GenesisRuntimeException {
		if (node.getName().equals("output")) {
			return true;
		}
		if (node.getName().equals("input")) {
			return true;
		}
		if (node.getName().equals("debug")) {
			doDebug(context, node);
			return true;
		}
		if (node.getName().equals("script")) {
			doScript(context, node);
			return true;
		}
		if (node.getName().equals("assert")) {
			doAssert(context, node);
			return true;
		}
		if (node.getName().equals("selector")) {
			doSelector(context, node);
			return true;
		}
		if (node.getName().equals("execute")) {
			doExecute(context, node);
			return true;
		}
		if (node.getName().equals("set")) {
			doSet(context, node);
			return true;
		}
		if (node.getName().equals("def")) {
			doDef(context, node);
			return true;
		}
		if (node.getName().equals("generate") && node.valueOf("@type").equals("fixed")) {
			doGenerateFixed(context, node, node.selectNodes("*"), false);
			return true;
		}
		if (node.getName().equals("generate") && node.valueOf("@type").equals("child")) {
			doGenerateChild(context, node, node.selectNodes("*"), false);
			return true;
		}
		if (node.getName().equals("call")) {
			doGenerateCall(context, node, node.selectNodes("*"));
			return true;
		}
		return false;
	}

	@Override
	public void cleanup(XMLProccesorContext context) {
		// TODO Auto-generated method stub

	}
	
	private void doDef(XMLProccesorContext context, Element node) throws GenesisRuntimeException {
		context.setVariables(context.getVariables());
		String varName = node.valueOf("@var");
		String varField = node.valueOf("@field");
		String varValue = node.valueOf("@value");
		String varType = node.valueOf("@type");
		String scope = node.valueOf("@scope");
		
		if (StringUtils.isEmpty(scope)) {
			scope = "local";
		}
		
		try {
			Object result;
			if (!StringUtils.isEmpty(varValue)) {
				if (!StringUtils.isEmpty(varField)) {
					varType = getFullClassName(varType);
					result = Class.forName(varType).newInstance();
					BeanUtils.setProperty(result, varField, context.evaluateExpression(node, varValue));
				} else
					result = context.evaluateExpression(node, varValue);
			} else {
				varType = getFullClassName(varType);
				result = Class.forName(varType).newInstance();
			}
			if (scope.equals("local"))
				context.getVariables().put(varName, result);
			else
				context.getGlobals().put(varName, result);
		} catch (Exception e) {
			throw context.createExceptionForNode(e, node);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void doSet(XMLProccesorContext context, Element node) throws GenesisRuntimeException {
		context.setVariables(context.getVariables());
		String varName = node.valueOf("@var");
		String varField = node.valueOf("@field");
		String varValue = node.valueOf("@value");
		String varType = node.valueOf("@type");
		String scope = node.valueOf("@scope");
		
		if (StringUtils.isEmpty(scope)) {
			scope = "local";
		}

		try {
			if (!StringUtils.isEmpty(varValue)) {
				Object result; 
				if (!StringUtils.isEmpty(varField)) {
					if (scope.equals("local"))
						result = context.getVariables().get(varName);
					else
						result = context.getGlobals().get(varName);
					BeanUtils.setProperty(result, varField, context.evaluateExpression(node, varValue));
				} else
					result = context.evaluateExpression(node, varValue);
				if (scope.equals("local"))
					context.getVariables().put(varName, result);
				else
					context.getGlobals().put(varName, result);
			} else {
				if (!StringUtils.isEmpty(varType)) {
					varType = getFullClassName(varType);
					if (scope.equals("local"))
						context.getVariables().put(varName, Class.forName(varType).newInstance());
					else
						context.getGlobals().put(varName, Class.forName(varType).newInstance());
				}
				List<Element> nodes = node.selectNodes("*");
				
				Object record;
				
				if (scope.equals("local"))
					record = context.getVariables().get(varName);
				else
					record = context.getGlobals().get(varName);
				
				for(Element n: nodes) {
					context.getXmlProccesor().setCurrentNode(n);
					Object value = context.evaluateExpression(node, n.valueOf("@value"));
					String property;
					if (n.getName().equals("field")) 
						property = n.valueOf("@name");
					else 
						property = n.valueOf("@field");

					if (record instanceof Map)
						((Map)record).put(property, value);
					if (record instanceof JSONObject)
						((JSONObject)record).put(property, value);

				}
				if (scope.equals("local"))
					context.getVariables().put(varName, record);
				else
					context.getGlobals().put(varName, record);
				//context.setVariable(varName, record);
			}
		} catch (Exception e) {
			throw context.createExceptionForNode(e, context.getXmlProccesor().getCurrentNode());
		}
	}

	private String getFullClassName(String varType) {
		// Translate alias
		if (varType.equals("JSONObject"))
			varType = "org.codehaus.jettison.json.JSONObject";
		if (varType.equals("JSONArray"))
			varType = "org.codehaus.jettison.json.JSONArray";
		if (varType.equals("HashMap"))
			varType = "java.util.HashMap";
		return varType;
	}

	private void doDebug(XMLProccesorContext context, Element node) throws GenesisRuntimeException {
		//context.setVariables(variables);
		String debugText = context.evaluateTemplate(node, node.getText());
		context.logInfo(debugText);
	}
	
	private void doScript(XMLProccesorContext context, Element node) throws GenesisRuntimeException {
		context.evaluateTemplate(node, node.getText());
	}
	
	private void doAssert(XMLProccesorContext context, Element node) throws GenesisRuntimeException {
		String condition = node.valueOf("@condition");
		if (!(Boolean)context.evaluateExpression(node, condition)) {
			throw context.createExceptionForNode(null, node, "Assert fail [" + condition + "]"); 
		}
	}

	@SuppressWarnings("unchecked")
	private void doSelector(XMLProccesorContext context, Node node) throws GenesisRuntimeException {
		context.setVariables(context.getVariables());
		String varName = node.valueOf("@var");
		String name = node.valueOf("@name");
		
		context.logDebug(MessageFormat.format("Assign selector {0}", varName));
		//node.valueOf("*[local-name(.) = 'sql']")
		try {
			if (node.valueOf("@items").length() > 0) {
				Object items = context.getVariables().get(node.valueOf("@items"));
				WeightedEntitySelector<Object> selector = new WeightedEntitySelector<Object>((List<Object>)items, 
					Integer.parseInt(node.valueOf("@weightNull")), Integer.parseInt(node.valueOf("@weightValue")));
				selector.build();
				context.getVariables().put(varName, selector);
			} else if (node.valueOf("*[local-name(.) = 'sql']").length() > 0) {
				String query = context.evaluateTemplate(node, node.valueOf("*[local-name(.) = 'sql']"));
				if (node.valueOf("@weightValue").length() > 0) {
					List<Object> items = dbHelper.listOneField(name, query, null);
					WeightedEntitySelector<Object> selector = new WeightedEntitySelector<Object>((List<Object>)items, 
						Integer.parseInt(node.valueOf("@weightNull")), Integer.parseInt(node.valueOf("@weightValue")));
					selector.build();
					context.getVariables().put(varName, selector);
				} else {
					List<HashMap<String, Object>> items = dbHelper.list(name, query, null);
					WeightedMapSelector<Object> selector = new WeightedMapSelector<Object>((List<HashMap<String, Object>>)items, 
						Integer.parseInt(node.valueOf("@weightNull")), node.valueOf("@keyValue"), node.valueOf("@keyWeight"));
					selector.build();
					context.getVariables().put(varName, selector);
				}
			} else {
				WeightedItemSelector<Object> items = new WeightedItemSelector<Object>();
				List<Element> nodes = node.selectNodes("*");
				
				for(Element n: nodes) {
					if (n.getName().equals("item")) {
						if (n.valueOf("@value").length() > 0) {
							Object value = context.evaluateExpression(node, n.valueOf("@value"));
							items.add(Integer.parseInt(n.valueOf("@weight")), value);
						} else {
							Integer from = context.evaluateExpression(node, n.valueOf("@from"), Integer.class);
							Integer to = context.evaluateExpression(node, n.valueOf("@to"), Integer.class);
							items.add(Integer.parseInt(n.valueOf("@weight")), new Range(from, to));
						}
					}
				}
				items.build();
				context.getVariables().put(varName, items);
			}
		} catch(Exception exp) {
			throw context.createExceptionForNode(exp, node);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void doGenerateFixed(final XMLProccesorContext context, final Element node, List<Element> nodes, boolean isReferencedCall) throws GenesisRuntimeException {
		context.logInfo("Generating: " + node.valueOf("@title"));
		
		if (node.valueOf("@name").length() > 0 && !isReferencedCall) {
			context.getReferences().put(node.valueOf("@name"), node);
		}
		
		if (isReferencedCall) {
			Element nodeOrig = (Element)context.getReferences().get(node.valueOf("@name"));
			nodes = (List<Element>)nodeOrig.selectNodes("*");
		}
		
		context.setVariables(context.getVariables());
		
		Object sizeExpr = context.evaluateExpression(node, node.valueOf("@size"));
		
		int size = ((Number)sizeExpr).intValue();
		String varName = node.valueOf("@var");
		final String varEntity = node.valueOf("@element");
		
		AbstractBuilder<Object> builder = new FixedBuilder<Object>(Object.class, size);
		
		List<Object> userList;
		try {
			userList = builder.build(new IObjectCreator<Object, Object>() {
					private List<Element> nodes;
					
					@Override
					public Object create(Object parent) throws Exception {
						for(Element n: nodes) {
							context.executeNode(n);
						}
						if (varEntity != null && varEntity.length() > 0)
							return context.evaluateExpression(node, varEntity);
						return
							null;
					}
				
					public IObjectCreator<Object, Object> setNodes(List<Element> nodes) {
						this.nodes = nodes;
						return this;
					}
				}.setNodes(nodes)
			, varName.length() == 0);
			
			if (varName != null) {
				context.getVariables().put(varName, userList);
			}
		} catch (Exception exp) {
			throw context.createExceptionForNode(exp, node);
		}
	}
	
	private void doGenerateCall(XMLProccesorContext context, Element node, List<Element> selectNodes) throws GenesisRuntimeException {
		Element ref = (Element)context.getReferences().get(node.valueOf("@name"));
		
		// Assign parameters
		for(Object attrObj: ref.attributes()) {
			Attribute attr = (Attribute)attrObj;
			if (node.attribute(attr.getName()) == null) {
				node.addAttribute(attr.getName(), attr.getText());
			}
		}
		
		if (ref.getName().equals("generate") && ref.valueOf("@type").equals("fixed")) {
			doGenerateFixed(context, node, selectNodes, true);
		}
		if (ref.getName().equals("generate") && ref.valueOf("@type").equals("child")) {
			doGenerateChild(context, node, selectNodes, true);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void doExecute(XMLProccesorContext context, Element node) throws GenesisRuntimeException {
		String file = node.valueOf("@file").trim();
		
		XMLProccesor xmlProccesor = new XMLProccesor(context.getXmlProccesor(), context.getXmlProccesor().getCommandDispatcher());
		
		List<Element> nodes = node.selectNodes("*");
		HashMap<String, Object> input = new HashMap<String, Object>();
		
		for(Element n: nodes) {
			if (n.getName().equals("input")) {
				if (n.valueOf("@value").trim().length() == 0) {
					input.put(n.valueOf("@name"), context.getVariables().get(n.valueOf("@name")));
				} else {
					Object value = context.evaluateExpression(n, n.valueOf("@value"));
					input.put(n.valueOf("@name"), value);
				}
			}
		}
		
		xmlProccesor.init();
		HashMap<String, Object> output = xmlProccesor.execute(file, input);
		context.getVariables().putAll(output);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void doGenerateChild(final XMLProccesorContext context, final Element node, List<Element> nodes, boolean isReferencedCall) throws GenesisRuntimeException {
		context.logInfo("Generating childs for: " + node.valueOf("@title"));
		
		if (node.valueOf("@name").length() > 0 && !isReferencedCall) {
			context.getReferences().put(node.valueOf("@name"), node);
		}
		
		if (isReferencedCall) {
			Element nodeOrig = (Element)context.getReferences().get(node.valueOf("@name"));
			nodes = (List<Element>)nodeOrig.selectNodes("*");
		}
		
		context.setVariables(context.getVariables());
	
		String varName = node.valueOf("@var");
		
		final String varEntity = node.valueOf("@element");
		List source = (List)context.getVariables().get(node.valueOf("@source"));

		context.logInfo("Source items: " + source.size());
		
		final ChildBuilder<Object, Object> builder;
		
		if (node.valueOf("@parentFrecuency").length() == 0) {
			builder = new ChildBuilder<Object, Object>(source);
		} else {
			WeightedItemSelector<Range> parentFrecuency = (WeightedItemSelector<Range>)
				context.getVariables().get(node.valueOf("@parentFrecuency"));
			builder = new ChildBuilder<Object, Object>(source, parentFrecuency);
		}
		
		List<Object> userList;
		try {
			userList = builder.build(new IObjectCreator<Object, Object>() {
					private List<Element> nodes;
					
					@Override
					public Object create(Object parent) throws Exception {
						context.getVariables().put("parent", parent);
						context.getVariables().put("childIndex", builder.getInternalPos());
						for(Element n: nodes) {
							context.executeNode(n);
						}
						if (varEntity.length() > 0)
							return context.evaluateExpression(node, varEntity);
						else
							return null;
					}

					public IObjectCreator<Object, Object> setNodes(List<Element> nodes) {
						this.nodes = nodes;
						return this;
					}
				}.setNodes(nodes)
			, varName.length() == 0);
			
			context.logInfo("Generated: " + builder.getNumIterations() + " items");
			
			if (varName != null) {
				context.getVariables().put(varName, userList);
			}
		} catch (GenesisRuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw context.createExceptionForNode(e, node);
		}
	}
}
