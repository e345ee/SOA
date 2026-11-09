package io.github.timur0o31.soa_booking_service.Client;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ProcessingException;
import io.github.timur0o31.soa_booking_service.error.exceptions.RequestValidationException;
import io.github.timur0o31.soa_booking_service.error.exceptions.TicketServiceException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@ApplicationScoped
public class TicketClient {
    private static final Logger LOG = Logger.getLogger(TicketClient.class.getName());
    private Client client;
    private URI ticketServiceUri;

    @Inject
    @ConfigProperty(name = "ticket.service.url")
    private String baseUrl;

    @Inject
    @ConfigProperty(name = "ticket.service.truststore.path")
    private String trustStorePath;

    @Inject
    @ConfigProperty(name = "ticket.service.truststore.password-file")
    private String trustStorePasswordFile;

    @Inject
    @ConfigProperty(name = "ticket.service.truststore.type", defaultValue = "PKCS12")
    private String trustStoreType;

    @Inject
    @ConfigProperty(name = "ticket.service.connect-timeout-ms", defaultValue = "5000")
    private long connectTimeoutMs;

    @Inject
    @ConfigProperty(name = "ticket.service.read-timeout-ms", defaultValue = "15000")
    private long readTimeoutMs;

    @PostConstruct
    public void init(){
        ticketServiceUri = TicketServiceTls.requireHttpsBaseUri(baseUrl);
        if (connectTimeoutMs <= 0 || readTimeoutMs <= 0) {
            throw new IllegalStateException("Тайм-ауты Ticket Service должны быть положительными");
        }
        try {
            client = ClientBuilder.newBuilder()
                    .sslContext(TicketServiceTls.loadTrustStore(Path.of(trustStorePath),
                            Path.of(trustStorePasswordFile), trustStoreType))
                    .connectTimeout(connectTimeoutMs, TimeUnit.MILLISECONDS)
                    .readTimeout(readTimeoutMs, TimeUnit.MILLISECONDS)
                    .property("jersey.config.client.followRedirects", false)
                    .build();
        } catch (java.io.IOException | java.security.GeneralSecurityException e) {
            throw new IllegalStateException("Не удалось настроить доверенные сертификаты Ticket Service; проверьте файлы конфигурации", e);
        }
    }


    void onApplicationStarted(@Observes @Initialized(ApplicationScoped.class) Object event) {
    }

    public String sell(long ticketId, long personId, float discount) {
        WebTarget target = client.target(ticketServiceUri).path("tickets/discount")
                .path(Long.toString(ticketId)).path(Long.toString(personId)).path(Float.toString(discount));
        return post(target, "продажа", 201);
    }

    public void cancelEvent(Long eventId) {
        post(client.target(ticketServiceUri).path("tickets/cancel-event/" + eventId), "отмена события", 204);
    }

    private String post(WebTarget target, String operation, int expected) {
        long start = System.nanoTime();
        int status = 0;
        var request = target.request(MediaType.APPLICATION_JSON);
        try (Response response = request.post(null)) {
            status = response.getStatus();
            if (status == 404) throw new NotFoundException("Билет, человек или событие не найдены");
            if (status == 400) throw new RequestValidationException("request", "Сервис билетов отклонил параметры");
            if (status != expected) throw new TicketServiceException("Сервис билетов вернул HTTP " + status);
            if (expected == 204) return null;
            String body = response.readEntity(String.class);
            if (body == null || body.isBlank()) throw new TicketServiceException("Пустой ответ сервиса билетов");
            return body;
        } catch (ProcessingException exception) {
            throw new TicketServiceException("Не удалось получить ответ сервиса билетов", exception);
        } finally {
            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            LOG.info("Обращение к Ticket Service: операция=" + operation + ", статус=" + status + ", длительность, мс=" + elapsedMs);
        }
    }

    @PreDestroy
    public void destroy(){
        if(client != null) client.close();
    }
}
