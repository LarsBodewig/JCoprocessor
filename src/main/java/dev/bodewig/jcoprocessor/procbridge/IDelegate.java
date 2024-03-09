package dev.bodewig.jcoprocessor.procbridge;

/**
 * Defines how the Server handles requests
 *
 * @author Gong Zhang
 * @author Lars Bodewig
 */
public interface IDelegate {

	/**
	 * Defines how the Server handles requests. The Client can send different types
	 * of payloads, it is up to the server to identify and cast the payload
	 * according to the method.
	 * <p>
	 * Payload types are: Boolean, Double, Integer, JSONArray, JSONObject, Long,
	 * String
	 *
	 * @param method  the requested method
	 * @param payload the requested payload
	 * @return the response payload
	 */
	Object handleRequest(String method, Object payload);
}
