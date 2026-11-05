package io.github.timur0o31.soa_booking_service.resource;

import jakarta.ws.rs.HeaderParam;
import java.util.UUID;
import io.github.timur0o31.soa_booking_service.service.BookingService;
import jakarta.inject.Inject;
import jakarta.validation.constraints.*;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/reservation")
public class BookingResource {
    @Inject
    private BookingService bookingService;

    @POST
    @Path("/sell/discount/{ticket-id}/{person-id}/{discount}")
    public Response createTicketWithDiscount(@PathParam("ticket-id") @Min(1) Long ticketId,
                                             @PathParam("person-id") @Min(1) Long personId,
                                             @PathParam("discount") @DecimalMin("0") @DecimalMax("100") Float discount,
                                             @HeaderParam("Idempotency-Key") @Pattern(regexp = "[0-9a-fA-F]{8}(?:-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}") String key) {
        String operationKey = key == null ? UUID.randomUUID().toString() : key;
        String response = bookingService.createTicket(ticketId, personId, discount, operationKey);
        return Response.status(Response.Status.CREATED)
                .type(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", operationKey)
                .entity(response)
                .build();
    }

    @POST
    @Path("/event/{event-id}/cancel")
    public Response cancelEvent(@PathParam("event-id") @Min(1) Long eventId) {
        bookingService.cancelEvent(eventId);
        return Response.noContent().build();
    }
}
