package org.esmartpoint.genesis.selectors;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.esmartpoint.genesis.util.DocumentFactoryWithLocator;
import org.esmartpoint.genesis.util.GokuSAXReader;
import org.springframework.core.io.FileSystemResource;
import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

public class WeightedMapSelector<T> {
	private final Random rnd = new Random();
    List<WeightedItem<T>> weightingOptions = new ArrayList<WeightedItem<T>>();
    List<T> entitites = new ArrayList<T>();
    int total;
    
    @SuppressWarnings("unchecked")
	public WeightedMapSelector(List<HashMap<String, Object>> entitites, int weightNull, String keyValue, String keyWeight) throws Exception {
    	weightingOptions.add(new WeightedItem<T>(weightNull, null));

    	for (Map<String, Object> entity : entitites) {
    		Integer value;
    		Object objValue = entity.get(keyWeight);
    		if (objValue instanceof Number)
    			value = ((Number)objValue).intValue();
    		else
    			value = Integer.parseInt(objValue.toString());
	    	weightingOptions.add(new WeightedItem<T>(value, (T)entity.get(keyValue)));
		}
    	
    }
    
    public static List<HashMap<String, Object>> readFromXMLResource(String resourceName) throws DocumentException {
    	File f = new FileSystemResource(resourceName).getFile();
    	
		Locator locator = new LocatorImpl();
		DocumentFactory docFactory = new DocumentFactoryWithLocator(locator);
		SAXReader reader = new GokuSAXReader(docFactory, locator);
		Document doc = reader.read(f);
		
		List<HashMap<String, Object>> res = new ArrayList<HashMap<String, Object>>();
		List<Element> rowList = doc.selectNodes("/table_data/row");
		for (Element elementRow : rowList) {
			List<Element> fieldList = elementRow.selectNodes("field");
			HashMap<String, Object> fields = new HashMap<String, Object>();
			for (Element elementField : fieldList) {
				fields.put(elementField.valueOf("@name"), elementField.getStringValue());
			}
			res.add(fields);
		}

    	return res;
    }

	public WeightedMapSelector<T> build() {
    	total = 0;
    	
        for (WeightedItem<T> w : weightingOptions) {
            total += w.getWeight();
        }
        return this;
    }
    
    public T getNext() {
    	int random = rnd.nextInt(total);
        
        //loop thru our weightings until we arrive at the correct one
        int current = 0;
        for (WeightedItem<T> w : weightingOptions) {
            current += w.getWeight();
            if (random < current)
                return w.getItem();
        }
        //shouldn't happen.
        return null;
    }    
}
