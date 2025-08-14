package io.openlineage.client.transports;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * A hostname verifier that accepts all hostnames without verification.
 * WARNING: This should only be used for testing environments and not in production.
 */
public class NoopHostnameVerifierCompat implements HostnameVerifier {
    
    /**
     * Singleton instance of NoopHostnameVerifierCompat.
     */
    public static final NoopHostnameVerifierCompat INSTANCE = new NoopHostnameVerifierCompat();
    
    /**
     * Private constructor to prevent direct instantiation.
     */
    private NoopHostnameVerifierCompat() {
        // Private constructor to enforce singleton pattern
    }
    
    /**
     * Accepts all hostnames without verification.
     *
     * @param hostname the hostname to verify
     * @param session the SSL session
     * @return always returns true, indicating that any hostname is acceptable
     */
    @Override
    public boolean verify(String hostname, SSLSession session) {
        // Accept all hostnames without verification
        return true;
    }
}
