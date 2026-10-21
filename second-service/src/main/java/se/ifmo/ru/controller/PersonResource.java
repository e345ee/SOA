package se.ifmo.ru.controller;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import se.ifmo.ru.dto.request.*;
import se.ifmo.ru.dto.response.PersonResponseDto;
import se.ifmo.ru.service.PersonService;
import java.net.URI;

@Path("/persons")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PersonResource {
    @Inject
    private PersonService personService;

    @GET
    public Response getPersons(@QueryParam("page") @DefaultValue("0") @Min(0) Long page,
                                @QueryParam("size") @DefaultValue("20") @Min(1) @Max(100) Integer size) {
        return Response.ok(personService.getPersons(page, size)).build();
    }

    @GET
    @Path("/{id}")
    public Response getPersonById(@PathParam("id") @Min(1) Long id) {
        return Response.ok(personService.findById(id)).build();
    }

    @POST
    public Response createPerson(@NotNull @Valid PersonCreateDto dto, @Context UriInfo uriInfo) {
        PersonResponseDto response = personService.savePerson(dto);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(response.id())).build();
        return Response.created(location).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    public Response updatePerson(@PathParam("id") @Min(1) Long id, @NotNull @Valid PersonInputDto dto) {
        return Response.ok(personService.updatePerson(id, dto.toCreate())).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deletePerson(@PathParam("id") @Min(1) Long id) {
        personService.deletePersonById(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/search")
    public Response searchPersons(@NotNull @Valid SearchDto<PersonSearchFilter> dto) {
        return Response.ok(personService.search(dto)).build();
    }
}
