/*******************************************************************************
 * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
 ******************************************************************************/ 
package org.jboss.tools.common.model.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.jboss.tools.common.model.XModelObject;

public class NamespaceMapping {
	public static String ATTR_NAMESPACE_MAPPING = "namespaceMapping"; //$NON-NLS-1$

	Map<String, String> defaultToActual = new HashMap<String, String>();
	Map<String, String> actualToDefault = new HashMap<String, String>();
	Map<String, String> defaultToURI = new HashMap<String, String>();

	Map<String, String> defaultToActualCache = new HashMap<String, String>();
	Map<String, String> actualToDefaultCache = new HashMap<String, String>();

	public NamespaceMapping() {}
	
	public void addNamespace(String defaultNamespace, String actualNamespace, String uri) {
		defaultToActual.put(defaultNamespace, actualNamespace);
		actualToDefault.put(actualNamespace, defaultNamespace);
		defaultToURI.put(defaultNamespace, uri);
	}

	public String getActualNamespace(String defaultNamespace) {
		return defaultToActual.get(defaultNamespace);
	}
	
	public String getDefaultNamespace(String actualNamespace) {
		return actualToDefault.get(actualNamespace);
	}

	public String getURIForDefaultNamespace(String defaultNamespace) {
		return defaultNamespace == null ? null : defaultToURI.get(defaultNamespace);
	}
	
	public String getURIForActualNamespace(String actualNamespace) {
		String defaultNamespace = getDefaultNamespace(actualNamespace);
		return getURIForDefaultNamespace(defaultNamespace);
	}
	
	public String convertToDefault(String name) {
		if(name.indexOf(':') < 0) return name;
		String value = actualToDefaultCache.get(name);
		if(value != null) return value;
		int i = name.indexOf(':');
		String a = name.substring(0, i);
		String d = getDefaultNamespace(a);
		if(d == null || a.equals(d)) {
			value = name;
		} else {
			value = d + name.substring(i);
		}
		defaultToActualCache.put(name, value);
		return value;
	}

	public String convertToActual(String name) {
		if(name.indexOf(':') < 0) return name;
		String value = defaultToActualCache.get(name);
		if(value != null) return value;
		StringBuffer res = new StringBuffer();
		if(name.indexOf('|') >= 0) {
			StringTokenizer st = new StringTokenizer(name, "|"); //$NON-NLS-1$
			int i = 0;
			while(st.hasMoreTokens()) {
				String t = st.nextToken();
				if(i > 0) res.append('|');
				t = convertToActual(t);
				res.append(t);
				i++;
			}
		} else if(name.indexOf('.') >= 0) {
			StringTokenizer st = new StringTokenizer(name, "."); //$NON-NLS-1$
			int i = 0;
			while(st.hasMoreTokens()) {
				String t = st.nextToken();
				if(i > 0) res.append('.');
				t = convertToActual(t);
				res.append(t);
				i++;
			}
		} else {
			int i = name.indexOf(':');
			String d = name.substring(0, i);
			String a = getActualNamespace(d);
			if(a == null || a.equals(name)) {
				res.append(name);
			} else {
				res.append(a).append(name.substring(i));
			}
		}
		value = res.toString();
		defaultToActualCache.put(name, value);
		return value;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		Iterator<String> i = defaultToActual.keySet().iterator();
		while(i.hasNext()) {
			String d = i.next();
			String a = defaultToActual.get(d);
			String u = defaultToURI.get(d);
			sb.append(d).append(' ').append(a).append(' ').append(u).append(' ');
		}
		
		return sb.toString();
	}

	public static NamespaceMapping load(XModelObject file) {
		String nms = file.get(ATTR_NAMESPACE_MAPPING);
		return nms == null ? null : load(nms);
	}

	public static NamespaceMapping load(String s) {
		if(s == null || s.length() == 0) return null;
		NamespaceMapping m = new NamespaceMapping();
		StringTokenizer st = new StringTokenizer(s, " "); //$NON-NLS-1$
		while(st.hasMoreTokens()) {
			String d = st.nextToken();
			String a = st.hasMoreTokens() ? st.nextToken() : d;
			String u = st.hasMoreTokens() ? st.nextToken() : "http://"; //$NON-NLS-1$
			m.addNamespace(d, a, u);
		}		
		return m;
	}

}
