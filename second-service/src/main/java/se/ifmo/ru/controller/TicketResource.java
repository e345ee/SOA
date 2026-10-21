package se.ifmo.ru.controller;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import se.ifmo.ru.dto.request.*;
import se.ifmo.ru.dto.response.TicketResponseDto;
import se.ifmo.ru.service.TicketService;
import se.ifmo.ru.service.EventCancellationService;
import java.net.URI;

@Path("/tickets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TicketResource {
    @Inject
    private TicketService ticketService;

    @Inject
    private EventCancellationService eventCancellationService;

    @POST
    @Path("/cancel-event/{event-id}")
    public Response cancelEvent(@PathParam("event-id") @Min(1) Long eventId) {
        eventCancellationService.cancelEvent(eventId);
        return Response.noContent().build();
    }

    @GET
    public Response getTickets(@QueryParam("page") @DefaultValue("0") @Min(0) Long page,
                                @QueryParam("size") @DefaultValue("20") @Min(1) @Max(100) Integer size) {
        return Response.ok(ticketService.getTickets(page, size)).build();
    }

    @GET
    @Path("/{id}")
    public Response getTicketById(@PathParam("id") @Min(1) Long id) {
        return Response.ok(ticketService.findById(id)).build();
    }

    @POST
    public Response createTicket(@NotNull @Valid TicketCreateDto dto, @Context UriInfo uriInfo) {
        TicketResponseDto response = ticketService.saveTicket(dto);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(response.id())).build();
        return Response.created(location).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateTicket(@PathParam("id") @Min(1) Long id, @NotNull @Valid TicketUpdateDto dto) {
        return Response.ok(ticketService.updateTicket(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteTicket(@PathParam("id") @Min(1) Long id) {
        ticketService.deleteTicketById(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/search")
    public Response searchTickets(@NotNull @Valid SearchDto<TicketSearchFilter> dto) {
        return Response.ok(ticketService.search(dto)).build();
    }

    @GET
    @Path("/price/sum")
    public Response getPriceSum() {
        return Response.ok(ticketService.getSum()).build();
    }

    @GET
    @Path("/price/average")
    public Response getPriceAverage() {
        return Response.ok(ticketService.getAvg()).build();
    }

    @POST
    @Path("/count-by-person")
    public Response countByPerson(@NotNull @Valid PersonInputDto dto) {
        return Response.ok(ticketService.countByPerson(dto)).build();
    }
}
