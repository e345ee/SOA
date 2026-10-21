package io.github.timur0o31.soa_booking_service.Client;

import io.github.timur0o31.soa_booking_service.dto.TicketCreateDto;
import io.github.timur0o31.soa_booking_service.model.Ticket;
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
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
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
            throw new IllegalStateException("Ticket Service timeouts must be positive");
        }
        try {
            client = ClientBuilder.newBuilder()
                    .sslContext(TicketServiceTls.loadTrustStore(Path.of(trustStorePath),
                            Path.of(trustStorePasswordFile), trustStoreType))
                    .connectTimeout(connectTimeoutMs, TimeUnit.MILLISECONDS)
                    .readTimeout(readTimeoutMs, TimeUnit.MILLISECONDS)
                    .property("jersey.config.client.followRedirects", false)
                    .register((ClientResponseFilter) (request, response) -> {
                        if (response.getStatus() >= 400) {
                            LOG.log(Level.WARNING, "Ticket Service returned HTTP {0} for {1} {2}",
                                    new Object[]{response.getStatus(), request.getMethod(), request.getUri().getPath()});
                        }
                    })
                    .build();
        } catch (java.io.IOException | java.security.GeneralSecurityException e) {
            throw new IllegalStateException("Cannot initialize Ticket Service TLS trust; check configured files", e);
        }
    }


    void onApplicationStarted(@Observes @Initialized(ApplicationScoped.class) Object event) {
    }

    public Ticket getTicketById(Long ticketId){
        WebTarget target = client.target(ticketServiceUri).path("tickets/"+ticketId);
        try (Response response = target.request(MediaType.APPLICATION_JSON).get()) {
            if (response.getStatus() == 404) {
                throw new NotFoundException("Билет с id=" + ticketId + " не найден");
            }
            if (response.getStatus() != 200) {
                throw new TicketServiceException("Сервис билетов вернул HTTP " + response.getStatus());
            }
            return readTicket(response);
        }catch (ProcessingException e) {
            throw new TicketServiceException("Не удалось обратиться к сервису билетов", e);
        }
    }

    public Ticket createTicket(TicketCreateDto ticketCreateDto){
        WebTarget target = client.target(ticketServiceUri).path("tickets/");
        try (Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(ticketCreateDto))) {
            if (response.getStatus() == 400) {
                throw new RequestValidationException("ticket", "Сервис билетов отклонил данные нового билета");
            }
            if (response.getStatus() == 404) {
                throw new NotFoundException("Человек с id=" + ticketCreateDto.getPersonId() + " не найден");
            }
            if (response.getStatus() != 201) {
                throw new TicketServiceException("Сервис билетов вернул HTTP " + response.getStatus());
            }
            return readTicket(response);
        } catch (ProcessingException e) {
            throw new TicketServiceException("Не удалось обратиться к сервису билетов", e);
        }
    }

    public void cancelEvent(Long eventId) {
        WebTarget target = client.target(ticketServiceUri).path("tickets/cancel-event/" + eventId);
        try (Response response = target.request().post(null)) {
            if (response.getStatus() == 404) {
                throw new NotFoundException("Билеты на событие не найдены");
            }
            if (response.getStatus() != 204) {
                throw new TicketServiceException("Ошибка отмены: сервис билетов вернул HTTP " + response.getStatus());
            }
        } catch (ProcessingException exception) {
            throw new TicketServiceException("Не удалось обратиться к сервису билетов", exception);
        }
    }

    private Ticket readTicket(Response response) {
        Ticket ticket = response.readEntity(Ticket.class);
        if (ticket == null) {
            throw new TicketServiceException("Сервис билетов вернул пустой ответ");
        }
        return ticket;
    }

    @PreDestroy
    public void destroy(){
        if(client != null) client.close();
    }
}
