package org.esmartpoint.genesis.plugins;

import java.util.HashMap;
import java.util.Map;

import org.esmartpoint.genesis.util.XMLProccesorContext;

public class CommandDispatcher {
	Map<String, IXMLCommandUnit> plugins;
	
	public CommandDispatcher() {
		plugins = new HashMap<String, IXMLCommandUnit>();
	}
	
	public void register(String namespace, Class<?> plugin) throws InstantiationException, IllegalAccessException {
		plugins.put(namespace, (IXMLCommandUnit)plugin.newInstance());
	}
	
	public void setupPlugins(XMLProccesorContext context)  throws Exception {
		for (String namespace : plugins.keySet()) {
			plugins.get(namespace).setup(context);
		}
	}

	public void cleanupPlugins(XMLProccesorContext context)  throws Exception {
		for (String namespace : plugins.keySet()) {
			plugins.get(namespace).cleanup(context);
		}
	}
	
	public IXMLCommandUnit getPlugin(String namespace) {
		return plugins.get(namespace);
	}
}
