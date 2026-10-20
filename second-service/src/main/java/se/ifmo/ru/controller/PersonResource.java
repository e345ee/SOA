package se.ifmo.ru.controller;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import se.ifmo.ru.dto.request.PersonCreateDto;
import se.ifmo.ru.dto.request.SearchDto;
import se.ifmo.ru.dto.response.PersonResponseDto;
import se.ifmo.ru.dto.response.TicketResponseDto;
import se.ifmo.ru.service.PersonService;

import java.net.URI;

@Path("/persons")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PersonResource {
    @Inject
    private PersonService personService;
    @GET
    public Response getPersons(@QueryParam("page") @DefaultValue("0") @Min(0) long page,
            @QueryParam("size") @DefaultValue("20") @Min(1) @Max(100) int size) {
        return Response.ok(personService.getPersons(page, size)).build();
    }

    @POST
    public Response createPerson(@NotNull @Valid PersonCreateDto personDto,@Context UriInfo uriInfo) {
        PersonResponseDto responseDto = personService.savePerson(personDto);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(responseDto.id())).build();
        return Response.created(location).entity(responseDto).build();
    }
    @DELETE
    @Path("/{id}")
    public Response deletePerson(@PathParam("id") Long id) {
        personService.deletePersonById(id);
        return Response.noContent().build();
    }
    @GET
    @Path("/{id}")
    public Response getPersonById(@PathParam("id") Long id){
        PersonResponseDto dto = personService.findById(id);
        return Response.ok(dto).build();
    }
    @PUT
    @Path("/{id}")
    public Response updatePerson(@PathParam("id") Long id, @NotNull @Valid PersonCreateDto dto) {
        PersonResponseDto responseDto = personService.updatePerson(id, dto);
        return Response.ok(responseDto).build();
    }

    @POST
    @Path("/search")
    public Response searchPerson(SearchDto searchDto) {
        return Response.ok(personService.search(searchDto)).build();
    }


}
