package se.ifmo.ru.controller;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import se.ifmo.ru.dto.request.PersonCreateDto;
import se.ifmo.ru.dto.request.SearchDto;
import se.ifmo.ru.dto.response.PersonResponseDto;
import se.ifmo.ru.service.PersonService;

@Path("/persons")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PersonResource {
    @Inject
    private PersonService personService;
    @GET
    public Response getPerson() {
        return Response.ok().build();
    }

    @POST
    public Response createPerson(@NotNull @Valid PersonCreateDto person) {
        return Response.ok(personService.savePerson(person)).build();
    }
    @DELETE
    @Path("/{id}")
    public Response deletePerson(@PathParam("id") Long id) {
        personService.deletePersonById(id);
        return Response.ok().build();
    }
    @GET
    @Path("/{id}")
    public Response getTicketById(@PathParam("id") Long id){
        PersonResponseDto dto = personService.findById(id);
        return Response.accepted(dto).build();
    }
    @PUT
    @Path("/{id}")
    public Response updatePerson(@PathParam("id") Long id, @NotNull @Valid PersonCreateDto dto) {
        PersonResponseDto responseDto = personService.updatePerson(id, dto);
        return Response.accepted(responseDto).build();
    }

    @POST
    @Path("/search")
    public Response searchPerson(SearchDto searchDto) {
        personService.search(searchDto);
        return Response.ok().build();
    }


}
