package org.esmartpoint.genesis.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
 
@Component
public class ApplicationContextHolder implements ApplicationContextAware{
 
    private static ApplicationContext context;
     
    public static ApplicationContext getApplicationContext() {
        return context;
    }
    
    public static void init(ApplicationContext context) {
    	ApplicationContextHolder.context = context;
    }
    
    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
    	ApplicationContextHolder.context = context;
    }
}
