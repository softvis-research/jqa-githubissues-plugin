package org.jqassistant.contrib.plugin.githubissues.toolbox;

/**
 * Gets thrown if the client retrieves any {@link com.sun.jersey.api.client.ClientResponse} with a status unequal
 * to 200.
 * <p>
 * <ul>
 * <li>If the error is thrown on the pagination level, the plugin tries to use the objects it retrieved so far.</li>
 * <li>If the error is thrown on the issue level, the whole issue will be ignored and the plugin tries to import
 * the next one.</li>
 * <li>If the error is thrown on the markdown level, it won't resolve references but doesn't cancel the issue or
 * comment.</li>
 * </ul>
 */
public class RequestFailedException extends Exception {

    RequestFailedException(String message) {
        super(message);
    }
}
