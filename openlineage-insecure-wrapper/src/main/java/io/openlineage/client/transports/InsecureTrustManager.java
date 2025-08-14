package io.openlineage.client.transports;

import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

/**
 * A trust manager that accepts all X.509 certificates without verification.
 * WARNING: This should only be used for testing environments and not in production.
 */
public class InsecureTrustManager implements X509TrustManager {
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) {
        // No-op, trust everything
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) {
        // No-op, trust everything
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}
