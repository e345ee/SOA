package io.github.timur0o31.soa_booking_service.Client;

import io.github.timur0o31.soa_booking_service.dto.TicketCreateDto;
import io.github.timur0o31.soa_booking_service.dto.TicketPageResponseDto;
import io.github.timur0o31.soa_booking_service.model.Ticket;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class TicketClient {
    private Client client;
    private final String URI= "https://localhost:14321/api/v1";

    @PostConstruct
    public void init(){
        client = ClientBuilder.newClient();
    }

    public Ticket getTicketById(Long ticketId){
        WebTarget target = client.target(URI).path("tickets/"+ticketId);
        try {
            Response response = target.request(MediaType.APPLICATION_JSON).get();
            if (response.getStatus() == 404) {
                throw new NotFoundException("Билет с id=" + ticketId + " не найден");
            }
            if (response.getStatus() != 200) {
                throw new WebApplicationException("Первый сервис вернул HTTP "
                                + response.getStatus(), Response.Status.BAD_GATEWAY);
            }
            return response.readEntity(Ticket.class);
        }catch (ProcessingException e) {
            throw new WebApplicationException("Ошибка обращения к первому сервису",
                    e, Response.Status.BAD_GATEWAY);
        }
    }

    public Ticket createTicket(TicketCreateDto ticketCreateDto){
        WebTarget target= client.target(URI).path("tickets/");
        try{
            try (Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(ticketCreateDto))) {
                if (response.getStatus() == 400) {
                    throw new RuntimeException("переданы некорректные данные");
                }

                if (response.getStatus() != 200) {
                    throw new WebApplicationException("Первый сервис вернул HTTP "
                            + response.getStatus(), Response.Status.BAD_GATEWAY);
                }
                return response.readEntity(Ticket.class);
            }
        }catch (ProcessingException e) {
            throw new WebApplicationException("Ошибка обращения к первому сервису",
                    e, Response.Status.BAD_GATEWAY);
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
            WebTarget target = client.target(URI).path("tickets/search");
            try (Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(body))) {
                if (response.getStatus() != 200) {
                    throw new WebApplicationException("Ошибка поиска: первый сервис вернул HTTP " + response.getStatus(), Response.Status.BAD_GATEWAY);
                }
                TicketPageResponseDto result = response.readEntity(TicketPageResponseDto.class);

                if (result == null || result.getContent() == null) {
                    throw new WebApplicationException("Некорректный ответ поиска", Response.Status.BAD_GATEWAY);
                }

                for (Ticket ticket : result.getContent()) {
                    ticketIds.add(ticket.getId());
                }
                hasNext = result.getHasNext() != null ? result.getHasNext() : result.getContent().size() == size;
            } catch (ProcessingException e) {
                throw new WebApplicationException("Ошибка обращения к первому сервису", e, Response.Status.BAD_GATEWAY);
            }
            page++;
        }

        return ticketIds;
    }

    public void deleteTicketById(Long ticketId) {
        WebTarget target = client.target(URI).path("tickets/" + ticketId);
        try (Response response = target.request().delete()) {
            if (response.getStatus() == 404) {
                throw new NotFoundException("Билет с id=" + ticketId + " не найден");
            }
            if (response.getStatus() != 204) {
                throw new WebApplicationException("Ошибка удаления: первый сервис вернул HTTP "
                                + response.getStatus(), Response.Status.BAD_GATEWAY);
            }
        } catch (ProcessingException e) {
            throw new WebApplicationException(
                    "Ошибка обращения к первому сервису", e, Response.Status.BAD_GATEWAY);
        }
    }

    @PreDestroy
    public void destroy(){
        client.close();
    }
}
