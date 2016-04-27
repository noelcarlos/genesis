package org.esmartpoint.genesis.generator;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Hello world!
 *
 */
public class TestApp 
{
	static public DateTime start;
	
    public static void main( String[] args )
    {
    	ExpressionParser parser;
    	HashMap<String, Object> variables = new HashMap<String, Object>();
    	StandardEvaluationContext context;
    	TemplateParserContext templateParserContext;
    	
        try {
        	start = DateTime.now();
        	
    		parser = new SpelExpressionParser();
    		context = new StandardEvaluationContext();
    		templateParserContext = new TemplateParserContext("${", "}");
    		
    		HashMap<String, Object> usuario = new HashMap<String, Object>();
    		usuario.put("nombre", "juan");
    		usuario.put("telefono", "9338484392");
    		
    		variables.put("field", "telefono");
    		variables.put("user", usuario);
    		
    		context.setVariables(variables);
    		
    		String varValue = "${user.nombre} ${user.telefono} ${field}, "
    				+ "static= ${user['telefono']} "
    				+ "corchetes= ${user[ field ]} "
    				+ "get= ${user.get(field)}";
    		
    		context.setRootObject(variables);
    		
    		context.addPropertyAccessor(new PropertyAccessor() {
				
    			@Override @SuppressWarnings({ "rawtypes", "unchecked" })
				public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
					((Map)target).put(name, newValue);
				}
				
				@Override @SuppressWarnings("rawtypes")
				public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
					return new TypedValue(((Map)target).get(name));
				}
				
				@Override
				public Class<?>[] getSpecificTargetClasses() {
					return new Class[] {HashMap.class};
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
    		
    		Object result = parser.parseExpression(varValue, templateParserContext).getValue(context);
    		System.out.println(result);
    		
    		result = parser.parseExpression("user.get(field)").getValue(context);
    		System.out.println(result);
    		
			System.out.println("Terminado en, " + PeriodFormat.getDefault().print(new Period(TestApp.start, DateTime.now())));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
