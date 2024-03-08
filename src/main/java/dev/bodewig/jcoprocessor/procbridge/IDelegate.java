package dev.bodewig.jcoprocessor.procbridge;

import org.json.JSONObject;

/**
 * 
 * @author Gong Zhang
 * @author Lars Bodewig
 */
public interface IDelegate {

	/**
	 * An interface that defines how server handles requests.
	 *
	 * @param method  the requested method
	 * @param payload the requested payload, must be a JSON value
	 * @return the result, must be a JSON value
	 */
	JSONObject handleRequest(String method, JSONObject payload);
}
