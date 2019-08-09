package org.liws.framework.util.context;


import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 上下文
 */
public class Context implements Serializable {
	private static final long serialVersionUID = 1L;

	private Map<String, Serializable> m_attribute = new ConcurrentHashMap<>();

	public Context() {
	}

	public Context(Context context) {
		if (context != null) {
			putAll(context.m_attribute);
		}
	}

	public void putAll(Context context) {
		if (context == null) {
			return;
		}
		putAll(context.m_attribute);
	}

	private void putAll(Map<String, Serializable> map) {
		for (String key : map.keySet()) {
			setAttribute(key, map.get(key));
		}
	}

	/**
	 * 获取上下文属性
	 * 
	 * @param key
	 * @return
	 */
	public Object getAttribute(String key) {
		if (key == null) {
			return null;
		}
		return m_attribute.get(key.toLowerCase());
	}

	/**
	 * 设置上下文属性
	 * 
	 * @param key
	 * @param value
	 */
	public void setAttribute(String key, Object value) {
		if (key == null) {
			return;
		}
		if(value instanceof Serializable || value == null){
			m_attribute.put(key.toLowerCase(), (Serializable)value);
		}
		

	}

	/**
	 * 获取上下文属性列表
	 * 
	 * @return
	 */
	public String[] getAttributeNames() {
		return m_attribute.keySet().toArray(new String[0]);
	}

	/**
	 * 删除上下文属性
	 * 
	 * @param key
	 */
	public Object removeAttribute(String key) {
		if (key == null) {
			return null;
		}
		return m_attribute.remove(key.toLowerCase());
	}

	@Override
    public String toString() {
		StringBuffer bf = new StringBuffer();
		String[] keys = getAttributeNames();
		for (String key : keys) {
			bf.append(key).append("=").append(getAttribute(key)).append(";");
		}
		return bf.toString();
	}
}
