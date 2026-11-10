package se.ifmo.ru.controller;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
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

    @POST
    @Path("/discount/{ticket-id}/{person-id}/{discount}")
    public Response sellWithDiscount(
            @PathParam("ticket-id") @Min(1) Long ticketId,
            @PathParam("person-id") @Min(1) Long personId,
            @PathParam("discount") @DecimalMin("0") @DecimalMax("100") Float discount
    ) {
        TicketResponseDto response = ticketService.sellWithDiscount(ticketId, personId, discount);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    public Response getTickets(@QueryParam("page") @DefaultValue("0") @Min(0) Long page,
            @QueryParam("size") @DefaultValue("20") @Min(1) @Max(100) Integer size) {
        return Response.ok(ticketService.getTickets(page, size)).build();
    }
    @POST
    @Path("/search")
    public Response searchTickets(@NotNull @Valid SearchDto<TicketSearchFilterDto> dto) {
        return Response.ok(ticketService.search(dto)).build();
    }

    @POST
    public Response createTicket(@NotNull @Valid TicketCreateDto dto, @Context UriInfo uriInfo){
        TicketResponseDto responseDto = ticketService.saveTicket(dto);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(responseDto.id())).build();
        return Response.created(location).entity(responseDto).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteTicket(@PathParam("id") Long ticketId){
        ticketService.deleteTicketById(ticketId);
        return Response.noContent().build();
    }
    @GET
    @Path("/{id}")
    public Response getTicketById(@PathParam("id") Long id){
        TicketResponseDto responseDto =  ticketService.findById(id);
        return Response.ok(responseDto).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateTicketById(@PathParam("id") Long id, @NotNull @Valid TicketUpdateDto dto){
        TicketResponseDto responseDto = ticketService.updateTicket(id, dto);
        return Response.ok(responseDto).build();
    }

    @GET
    @Path("/price/sum")
    public Response getPriceSum(){
        Double response = ticketService.getSum();
        return Response.ok(response).build();
    }

    @GET
    @Path("/price/average")
    public Response getPriceAvg(){
        Double response = ticketService.getAvg();
        return Response.ok(response).build();
    }

    @POST
    @Path("/count-by-person")
    public Response getCountByPerson(@NotNull @Valid PersonInputDto dto){
        return Response.ok(ticketService.countByPerson(dto)).build();
    }

    @GET
    @Path("/count-by-person/{person-id}")
    public Response getCountByPersonId(@PathParam("person-id") @Min(1) Long personId) {
        return Response.ok(ticketService.countByPerson(personId)).build();
    }

}
