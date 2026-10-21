package io.github.timur0o31.soa_booking_service.Client;

import io.github.timur0o31.soa_booking_service.dto.TicketCreateDto;
import io.github.timur0o31.soa_booking_service.dto.TicketPageResponseDto;
import io.github.timur0o31.soa_booking_service.model.Ticket;
import io.github.timur0o31.soa_booking_service.error.exceptions.RequestValidationException;
import io.github.timur0o31.soa_booking_service.error.exceptions.TicketServiceException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class TicketClient {
    private Client client;
    @Inject
    @ConfigProperty(name = "ticket.service.url")
    String baseUrl;
    @PostConstruct
    public void init(){
        client = ClientBuilder.newClient();
    }

    public Ticket getTicketById(Long ticketId){
        WebTarget target = client.target(baseUrl).path("tickets/"+ticketId);
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
        WebTarget target = client.target(baseUrl).path("tickets/");
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

    public List<Long> getAllTicketIdsByEventId(Long eventId) {
        List<Long> ticketIds = new ArrayList<>();
        int page = 0;
        int size = 100;
        boolean hasNext = true;
        while (hasNext) {
            Map<String, Object> body = Map.of(
                    "filter", Map.of("eventId", eventId),
                    "page", page,
                    "size", size,
                    "sort", List.of(Map.of("field", "id", "direction", "ASC")));
            WebTarget target = client.target(baseUrl).path("tickets/search");
            try (Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(body))) {
                if (response.getStatus() != 200) {
                    throw new TicketServiceException("Ошибка поиска: сервис билетов вернул HTTP " + response.getStatus());
                }
                TicketPageResponseDto result = response.readEntity(TicketPageResponseDto.class);

                if (result == null || result.getContent() == null) {
                    throw new TicketServiceException("Сервис билетов вернул некорректный ответ поиска");
                }

                for (Ticket ticket : result.getContent()) {
                    if (ticket == null || ticket.getId() == null) {
                        throw new TicketServiceException("Сервис билетов вернул билет без идентификатора");
                    }
                    ticketIds.add(ticket.getId());
                }
                hasNext = result.getHasNext() != null ? result.getHasNext() : result.getContent().size() == size;
            } catch (ProcessingException e) {
                throw new TicketServiceException("Не удалось обратиться к сервису билетов", e);
            }
            page++;
        }

        return ticketIds;
    }

    public void deleteTicketById(Long ticketId) {
        WebTarget target = client.target(baseUrl).path("tickets/" + ticketId);
        try (Response response = target.request().delete()) {
            if (response.getStatus() == 404) {
                throw new NotFoundException("Билет с id=" + ticketId + " не найден");
            }
            if (response.getStatus() != 204) {
                throw new TicketServiceException("Ошибка удаления: сервис билетов вернул HTTP " + response.getStatus());
            }
        } catch (ProcessingException e) {
            throw new TicketServiceException("Не удалось обратиться к сервису билетов", e);
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
        client.close();
    }
}
