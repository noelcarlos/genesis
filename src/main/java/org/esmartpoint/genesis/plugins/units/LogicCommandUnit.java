package org.esmartpoint.genesis.plugins.units;

import java.util.List;

import org.dom4j.Element;
import org.esmartpoint.genesis.plugins.IXMLCommandUnit;
import org.esmartpoint.genesis.util.GenesisRuntimeException;
import org.esmartpoint.genesis.util.XMLProccesorContext;

public class LogicCommandUnit implements IXMLCommandUnit {
	
	@Override
	public void setup(XMLProccesorContext context) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean dispatch(XMLProccesorContext context, Element node) throws GenesisRuntimeException {
		if (node.getName().equals("if")) {
			commandIf(context, node, node.selectNodes("*"));
			return true;
		}
		if (node.getName().equals("for")) {
			commandFor(context, node, node.selectNodes("*"));
			return true;
		}
		return false;
	}

	@Override
	public void cleanup(XMLProccesorContext context) {
		// TODO Auto-generated method stub

	}
	
	private void commandIf(XMLProccesorContext context, final Element node, List<Element> nodes) throws GenesisRuntimeException {
		context.setVariables(context.getVariables());
		Boolean testExpr = (Boolean)context.evaluateExpression(node, node.valueOf("@test"));
		
		if (testExpr) {
			for(Element n: nodes) {
				context.executeNode(n);
			}
		}
	}

	private void commandFor(XMLProccesorContext context, final Element node, List<Element> nodes) throws GenesisRuntimeException {
		context.setVariables(context.getVariables());
		String var = (String)node.valueOf("@var");
		Integer from = (Integer)context.evaluateExpression(node, node.valueOf("@from"));
		Integer to = (Integer)context.evaluateExpression(node, node.valueOf("@to"));
		
		for(int i=from; i <= to; i++) {
			context.getVariables().put(var, i);
			for(Element n: nodes) {
				context.executeNode(n);
			}
		}
	}	
	
}
