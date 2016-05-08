package org.esmartpoint.genesis.plugins.units;

import org.dom4j.Element;
import org.dom4j.Node;
import org.esmartpoint.dbutil.Cronometro;
import org.esmartpoint.genesis.helpers.MongoDbHelper;
import org.esmartpoint.genesis.plugins.IXMLCommandUnit;
import org.esmartpoint.genesis.util.ApplicationContextHolder;
import org.esmartpoint.genesis.util.GenesisRuntimeException;
import org.esmartpoint.genesis.util.XMLProccesorContext;
import org.springframework.context.ApplicationContext;

public class MongoDbCommandUnit implements IXMLCommandUnit {
	MongoDbHelper mongoDbHelper;
	
	@Override
	public void setup(XMLProccesorContext context)  throws Exception {
		ApplicationContext springContext = ApplicationContextHolder.getApplicationContext();
		
		mongoDbHelper = springContext.getBean(MongoDbHelper.class);
	}

	@Override
	public boolean dispatch(XMLProccesorContext context, Element node) throws GenesisRuntimeException {
		if (node.getName().equals("open")) {
			commandOpen(context, node);
			return true;
		}
		if (node.getName().equals("close")) {
			commandClose(context, node);
			return true;
		}
		if (node.getName().equals("insert")) {
			commandInsert(context, node);
			return true;
		}
		if (node.getName().equals("dropCollection")) {
			commandDropCollection(context, node);
			return true;
		}
		return false;
	}

	@Override
	public void cleanup(XMLProccesorContext context) {
		// TODO Auto-generated method stub
		if (mongoDbHelper != null) {
			mongoDbHelper.closeSession();
		}
	}
	
	private void commandInsert(XMLProccesorContext context, Element node) throws GenesisRuntimeException {
		String collectionName = node.valueOf("@collection").toString();
		Object data = context.getVariables().get(node.valueOf("@data"));

		try {
			Cronometro.start("DATABASE");
			mongoDbHelper.insert(collectionName, data.toString());
		} catch (Exception evalExc) {
			evalExc.printStackTrace();
			throw context.createExceptionForNode(evalExc, node, evalExc.getMessage() + " [" + data + "]"); 
		} finally {
			Cronometro.stop("DATABASE");
		}
	}
	
	private void commandOpen(XMLProccesorContext context, Element node) throws GenesisRuntimeException {
		String host = node.valueOf("@host").toString();
		int port = Integer.parseInt(node.valueOf("@port").toString());
		String database = node.valueOf("@database").toString();

		try {
			Cronometro.start("DATABASE");
			mongoDbHelper.openSession(host, port, database);
		} catch (Exception evalExc) {
			evalExc.printStackTrace();
			throw context.createExceptionForNode(evalExc, node, evalExc.getMessage()); 
		} finally {
			Cronometro.stop("DATABASE");
		}
	}
	
	private void commandClose(XMLProccesorContext context, Element node) throws GenesisRuntimeException {

		try {
			Cronometro.start("DATABASE");
			mongoDbHelper.closeSession();
		} catch (Exception evalExc) {
			evalExc.printStackTrace();
			throw context.createExceptionForNode(evalExc, node, evalExc.getMessage()); 
		} finally {
			Cronometro.stop("DATABASE");
		}
	}	
	
	private void commandDropCollection(XMLProccesorContext context, Node node) throws GenesisRuntimeException {
		String collectionName = node.valueOf("@collection").toString();
		
		try {
			Cronometro.start("DATABASE");
			mongoDbHelper.dropCollection(collectionName);
		} catch (Exception evalExc) {
			evalExc.printStackTrace();
			throw context.createExceptionForNode(evalExc, node, evalExc.getMessage()); 
		} finally {
			Cronometro.stop("DATABASE");
		}
	}
	
}
