package org.esmartpoint.genesis.generator;

import httl.Engine;
import httl.Template;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.joda.time.DateTime;

/**
 * Hello world!
 *
 */
public class TestHttlTemplateApp 
{
	static public DateTime start;
	
    public static void main( String[] args )
    {
        try {
        	Properties config = new Properties();
        	//config.put("import.variables", "Map user, List books");
        	config.put("interpreted", "true");
        	config.put("compiled", "false");

        	Engine engine = Engine.getEngine(config);

    		Map<String, Object> variables = new HashMap<String, Object>();
    		Map<String, Object> usuario = new HashMap<String, Object>();
    		usuario.put("nombre", "juan");
    		usuario.put("telefono", "9338484392");
    		variables.put("field", "telefono");
    		variables.put("user", usuario);
    		variables.put("books", Arrays.asList("juan", "pedro", "jose", "alberto"));

    		String varValue = "${books.size}\r\n"
    				+ "static= ${user.telefono}\r\n "
    				+ "#for(name: books) ${name}\r\n#break(for.index==2) #end \r\n #for(i: 1..15) kk ${i}\r\n  #end";

    		Template template = engine.parseTemplate(varValue);
    		StringWriter writer = new StringWriter();
    		template.render(variables, writer);
    		
    		System.out.println(writer.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
