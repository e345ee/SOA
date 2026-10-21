package io.github.timur0o31.soa_booking_service.Client;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Arrays;


final class TicketServiceTls {
    private TicketServiceTls() {
    }

    static URI requireHttpsBaseUri(String value) {
        URI uri = URI.create(value);
        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                || uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null
                || uri.getPort() == 0 || uri.getPort() > 65535) {
            throw new IllegalArgumentException("ticket.service.url must be an HTTPS base URI without credentials, query or fragment");
        }
        return uri;
    }

    static SSLContext loadTrustStore(Path storePath, Path passwordFile, String type)
            throws IOException, GeneralSecurityException {
        String secret = Files.readString(passwordFile);

        if (secret.endsWith("\n")) secret = secret.substring(0, secret.length() - 1);
        if (secret.endsWith("\r")) secret = secret.substring(0, secret.length() - 1);
        char[] password = secret.toCharArray();
        KeyStore store = KeyStore.getInstance(type);
        try (InputStream input = Files.newInputStream(storePath)) {
            store.load(input, password);
        } finally {
            Arrays.fill(password, '\0');
        }
        if (store.size() == 0) {
            throw new GeneralSecurityException("Ticket Service truststore must contain a trusted certificate");
        }
        TrustManagerFactory managers = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        managers.init(store);
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, managers.getTrustManagers(), null);
        return context;
    }
}
