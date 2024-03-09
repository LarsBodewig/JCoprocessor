package dev.bodewig.jcoprocessor.procbridge;

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
	 * @param payload the requested payload
	 * @return the result
	 */
	Object handleRequest(String method, Object payload);
}
