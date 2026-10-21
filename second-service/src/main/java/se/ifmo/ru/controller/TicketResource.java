package se.ifmo.ru.controller;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import lombok.Getter;
import se.ifmo.ru.dto.request.PersonCreateDto;
import se.ifmo.ru.dto.request.PersonInputDto;
import se.ifmo.ru.dto.request.TicketUpdateDto;
import static se.ifmo.ru.utils.ApiValidation.id;
import se.ifmo.ru.dto.request.SearchDto;
import se.ifmo.ru.dto.request.TicketCreateDto;
import se.ifmo.ru.dto.response.TicketResponseDto;
import se.ifmo.ru.service.TicketService;

import java.net.URI;

@Path("/tickets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TicketResource {
    @Inject
    private TicketService ticketService;

    @GET
    public Response getTickets(@QueryParam("page") @DefaultValue("0") String pageRaw,
            @QueryParam("size") @DefaultValue("20") String sizeRaw) {
        long page=se.ifmo.ru.utils.ApiValidation.page(pageRaw);int size=se.ifmo.ru.utils.ApiValidation.size(sizeRaw);
        return Response.ok(ticketService.getTickets(page, size)).build();
    }
    @POST
    @Path("/search")
    public Response searchTickets(SearchDto dto) {
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
    public Response deleteTicket(@PathParam("id") String ticketIdRaw){
        Long ticketId = id(ticketIdRaw);
        ticketService.deleteTicketById(ticketId);
        return Response.noContent().build();
    }
    @GET
    @Path("/{id}")
    public Response getTicketById(@PathParam("id") String idRaw){
        Long id = id(idRaw);
        TicketResponseDto responseDto =  ticketService.findById(id);
        return Response.ok(responseDto).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateTicketById(@PathParam("id") String idRaw, @NotNull @Valid TicketUpdateDto dto){
        Long id = id(idRaw);
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

}
