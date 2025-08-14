package io.openlineage.client.transports;

import io.openlineage.client.Transport;
import io.openlineage.client.TransportFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * A factory for creating HTTP transports that disable SSL certificate validation.
 * This factory overrides the standard HttpTransportFactory to create insecure transports.
 */
@Slf4j
public class HttpTransportFactory implements TransportFactory {

    @Override
    public String getType() {
        // Must match the standard HttpTransportFactory type to override it
        return "http";
    }

    @Override
    public Transport create(Map<String, Object> config) {
        // Parse the config into an HttpConfig object
        HttpConfig httpConfig = HttpConfig.fromConfig(config);
        
        // Log a warning about using insecure transport
        log.warn("Creating InsecureHttpTransport with SSL certificate validation disabled. " +
                "This should only be used in testing environments!");
        
        // Create and return our insecure transport instead of the standard one
        return new InsecureHttpTransport(httpConfig);
    }
}
