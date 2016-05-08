package org.esmartpoint.genesis.plugins.units;

import java.util.HashMap;
import java.util.List;

import org.dom4j.Element;
import org.esmartpoint.dbutil.Cronometro;
import org.esmartpoint.genesis.helpers.DbHelper;
import org.esmartpoint.genesis.plugins.IXMLCommandUnit;
import org.esmartpoint.genesis.util.ApplicationContextHolder;
import org.esmartpoint.genesis.util.GenesisRuntimeException;
import org.esmartpoint.genesis.util.XMLProccesorContext;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

public class DbCommandUnit implements IXMLCommandUnit {
	DbHelper dbHelper;

	@Override
	public void setup(XMLProccesorContext context) {
		ApplicationContext springContext = ApplicationContextHolder.getApplicationContext();
		
		dbHelper = springContext.getBean(DbHelper.class);
	}

	@Override
	public boolean dispatch(XMLProccesorContext context, Element node) throws GenesisRuntimeException {
		if (node.getName().equals("load")) {
			commandLoad(context, node);
			return true;
		}
		if (node.getName().equals("list")) {
			commandList(context, node);
			return true;
		}
		if (node.getName().equals("insert")) {
			commandInsert(context, node);
			return true;
		}
		if (node.getName().equals("script")) {
			commandScript(context, node);
			return true;
		}
		if (node.getName().equals("open")) {
			commandOpen(context, node);
			return true;
		}
		if (node.getName().equals("close")) {
			commandClose(context, node);
			return true;
		}
		return false;
	}

	@Override
	public void cleanup(XMLProccesorContext context) {
		// TODO Auto-generated method stub

	}
	
	@Transactional(readOnly = false)
	private void commandLoad(XMLProccesorContext context, Element node) throws GenesisRuntimeException {
		//context.setVariables(variables);
		try {
			String varName = node.valueOf("@var");
			String name = node.valueOf("@name");
			String query = context.evaluateTemplate(node, node.getText());
			Cronometro.start("DATABASE");
			HashMap<String, Object> res = dbHelper.load(name, query, null);
			Cronometro.stop("DATABASE");
			context.getVariables().put(varName, res);
		} catch (Exception e) {
			throw context.createExceptionForNode(e, node);
		}
		//context.setVariable(varName, res);
	}
	
	@Transactional(readOnly = false)
	private void commandList(XMLProccesorContext context, Element node) throws GenesisRuntimeException {
		//context.setVariables(variables);
		String varName = node.valueOf("@var");
		String query = context.evaluateTemplate(node, node.getText());
		String name = node.valueOf("@name");
		String projectToMapString = node.valueOf("@projectToMap");
		Boolean projectToMap = (projectToMapString == null) ? true : Boolean.parseBoolean(projectToMapString);
		
		List<?> res;
		
		Cronometro.start("DATABASE");
		if (projectToMap)
			res = dbHelper.list(name, query, null);
		else
			res = dbHelper.listOneField(name, query, null);
		Cronometro.stop("DATABASE");

		context.getVariables().put(varName, res);
		//context.setVariable(varName, res);
	}
	
	@SuppressWarnings("unchecked")
	@Transactional(readOnly = false)
	private void commandInsert(XMLProccesorContext context, Element node) throws GenesisRuntimeException {
		String varName = null;
		HashMap<String, Object> res = null;
		String name = null;
		Cronometro.start("DATABASE");
		try {
			//context.setVariables(variables);
			varName = node.valueOf("@value");
			name = node.valueOf("@name");
			res = (HashMap<String, Object>)context.getVariables().get(node.valueOf("@value"));
			dbHelper.insert(name, node.valueOf("@table"), res, node.valueOf("@key"));
			context.getVariables().put(varName, res);
			//context.setVariable(varName, res);
		} catch (Exception exp) {
			context.logError("DBInsert Error:");
			context.logError("\tTable:" + node.valueOf("@table"));
			context.logError("\tParams: "+ varName + "=" + res);
			dbHelper.rollback(name);
			throw context.createExceptionForNode(exp, node); 
		} finally {
			Cronometro.stop("DATABASE");
		}
	}
	
	@Transactional(readOnly = false)
	private void commandScript(XMLProccesorContext context, Element node) throws GenesisRuntimeException {
		String query = context.evaluateTemplate(node, node.getText());
		try {
			Cronometro.start("DATABASE");
			String name = node.valueOf("@name");
			dbHelper.script(name, query);
		} catch (Exception exp) {
			context.logError("DBScript Error:");
			context.logError("\tQuery: "+ query);
			throw context.createExceptionForNode(exp, node); 
		} finally {
			Cronometro.stop("DATABASE");
		}
	}
	
	private void commandOpen(XMLProccesorContext context, Element node) throws GenesisRuntimeException {
		try {
			String driverClassName = node.valueOf("@driverClassName");
			String password = node.valueOf("@password");
			String url = node.valueOf("@url");
			String username = node.valueOf("@username");
			String dialect = node.valueOf("@dialect");
			String name = node.valueOf("@name");
			
			dbHelper.createConnection(name, driverClassName, url, password, username, dialect);
			dbHelper.begin(name);
		} catch (Exception exp) {
			context.logError("DBOpen Error");
			exp.printStackTrace();
			throw context.createExceptionForNode(exp, node); 
		}
	}
	
	private void commandClose(XMLProccesorContext context, Element node) throws GenesisRuntimeException {
		String varName = null;
		HashMap<String, Object> res = null;
		try {
			String name = node.valueOf("@name");
			dbHelper.commit(name);
			dbHelper.closeConnection(name);
		} catch (Exception exp) {
			context.logError("DBClose Error, params: "+ varName + "=" + res);
			throw context.createExceptionForNode(exp, node); 
		}
	}

}
