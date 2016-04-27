package org.esmartpoint.genesis.plugins;

import org.dom4j.Element;
import org.esmartpoint.genesis.util.GenesisRuntimeException;
import org.esmartpoint.genesis.util.XMLProccesorContext;

public interface IXMLCommandUnit {
	public void setup(XMLProccesorContext context) throws Exception;
	public boolean dispatch(XMLProccesorContext context, Element node) throws GenesisRuntimeException;
	public void cleanup(XMLProccesorContext context)  throws Exception;
}
