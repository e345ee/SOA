package se.ifmo.ru.controller;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import se.ifmo.ru.dto.request.SearchDto;
import se.ifmo.ru.dto.request.TicketCreateDto;
import se.ifmo.ru.dto.response.TicketResponseDto;
import se.ifmo.ru.service.TicketService;

@Path("/tickets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TicketResource {
    @Inject
    private TicketService ticketService;

    @GET
    public Response getTickets(){
        return Response.accepted().build();
    }
    @POST
    @Path("/search")
    public Response searchTickets(SearchDto dto) {
        return Response.ok(ticketService.search(dto)).build();
    }

    @POST
    public Response createTicket(@NotNull @Valid TicketCreateDto dto){
        TicketResponseDto responseDto = ticketService.saveTicket(dto);
        return Response.ok(responseDto).build();
    }
    @DELETE
    @Path("/{id}")
    public Response deleteTicket(@PathParam("id") Long ticketId){
        ticketService.deleteTicketById(ticketId);
        return Response.accepted().build();
    }
    @GET
    @Path("/{id}")
    public Response getTicketById(@PathParam("id") Long id){
        TicketResponseDto responseDto =  ticketService.findById(id);
        return Response.accepted(responseDto).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateTicketById(@PathParam("id") Long id, @NotNull @Valid TicketCreateDto dto){
        TicketResponseDto responseDto = ticketService.updateTicket(id, dto);
        return Response.ok(responseDto).build();
    }

    @GET
    @Path("/price/sum")
    public Response getPriceSum(){
        Double response = ticketService.getSum();
        return Response.accepted(response).build();
    }

    @GET
    @Path("/price/average")
    public Response getPriceAvg(){
        Double response = ticketService.getAvg();
        return Response.accepted(response).build();
    }

    @POST
    @Path("/count-by-person")
    public Response getCountByPerson(){
        return Response.accepted().build();
    }

}
