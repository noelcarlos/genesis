package org.esmartpoint.genesis.util;

import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.tree.DefaultElement;

public class GokuElement extends DefaultElement {
	private static final long serialVersionUID = -9195183496262393267L;
	private int lineNum = 0, colNum = 0;

	public GokuElement(QName qname) {
		super(qname);
	}

	public GokuElement(QName qname, int attrCount) {
		super(qname, attrCount);
	}

	public GokuElement(String name) {
		super(name);
	}

	public GokuElement(String name, Namespace namespace) {
		super(name, namespace);
	}

	public int getColumnNumber() {
		return this.colNum;
	}

	public int getLineNumber() {
		return this.lineNum;
	}

	public void setLocation(int lineNum, int colNum) {
		this.lineNum = lineNum;
		this.colNum = colNum;
	}
}
