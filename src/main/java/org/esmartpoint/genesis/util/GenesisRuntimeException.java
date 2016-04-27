package org.esmartpoint.genesis.util;

import org.esmartpoint.genesis.generator.XMLProccesor;

public class GenesisRuntimeException extends Exception {
	private static final long serialVersionUID = 1L;

	private XMLProccesor xmlProccesor;
	private GokuElement element;

	public GenesisRuntimeException(XMLProccesor xmlProccesor, GokuElement element, String message, Exception cause) {
		super(message, cause);
		
		this.setXmlProccesor(xmlProccesor);
		this.setElement(element);
	}

	public XMLProccesor getXmlProccesor() {
		return xmlProccesor;
	}

	public void setXmlProccesor(XMLProccesor xmlProccesor) {
		this.xmlProccesor = xmlProccesor;
	}

	public GokuElement getElement() {
		return element;
	}

	public void setElement(GokuElement element) {
		this.element = element;
	}
}
