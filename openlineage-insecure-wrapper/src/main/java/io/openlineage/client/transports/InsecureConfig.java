package io.openlineage.client.transports;

import java.net.URI;
import java.util.Map;

/**
 * Configuration class for InsecureTransport.
 */
public final class InsecureConfig implements TransportConfig {
    private URI url;
    private String endpoint;
    private Double timeout;
    private TokenProvider auth;
    private Map<String, String> urlParams;
    private Map<String, String> headers;

    public InsecureConfig() {}

    public InsecureConfig(URI url, String endpoint, Double timeout, TokenProvider auth, 
                         Map<String, String> urlParams, Map<String, String> headers) {
        this.url = url;
        this.endpoint = endpoint;
        this.timeout = timeout;
        this.auth = auth;
        this.urlParams = urlParams;
        this.headers = headers;
    }

    public URI getUrl() { return url; }
    public void setUrl(URI url) { this.url = url; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public Double getTimeout() { return timeout; }
    public void setTimeout(Double timeout) { this.timeout = timeout; }

    public TokenProvider getAuth() { return auth; }
    public void setAuth(TokenProvider auth) { this.auth = auth; }

    public Map<String, String> getUrlParams() { return urlParams; }
    public void setUrlParams(Map<String, String> urlParams) { this.urlParams = urlParams; }

    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }
}
