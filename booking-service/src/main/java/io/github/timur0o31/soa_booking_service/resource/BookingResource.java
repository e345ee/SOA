package io.github.timur0o31.soa_booking_service.resource;

import io.github.timur0o31.soa_booking_service.dto.TicketResponseDto;
import io.github.timur0o31.soa_booking_service.service.BookingService;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import io.github.timur0o31.soa_booking_service.error.exceptions.RequestValidationException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/reservation")
public class BookingResource {

    @Inject
    private BookingService bookingService;

    private static long id(String value){try{if(!value.matches("[0-9]+"))throw new IllegalArgumentException();long id=Long.parseLong(value);if(id<1)throw new IllegalArgumentException();return id;}catch(RuntimeException e){throw new RequestValidationException("id","Expected a positive int64 identifier");}}

    @POST
    @Path("/sell/discount/{ticket-id}/{person-id}/{discount}")
    public Response createTicketWithDiscount(@PathParam("ticket-id") String ticketIdRaw,
                                             @PathParam("person-id") String personIdRaw,
                                             @PathParam("discount") String discountRaw){
        Long ticketId=id(ticketIdRaw),personId=id(personIdRaw);
        Float discount;try{BigDecimal n=new BigDecimal(discountRaw);discount=n.floatValue();if(!Float.isFinite(discount)||(discount==0&&n.signum()!=0))throw new IllegalArgumentException();}catch(RuntimeException e){throw new RequestValidationException("discount","Expected a finite float percentage");}
        TicketResponseDto response = bookingService.createTicket(ticketId, personId, discount);
        return Response.status(Response.Status.CREATED)
                .type(MediaType.APPLICATION_JSON)
                .entity(response).build();
    }

    @POST
    @Path("/event/{event-id}/cancel")
    public Response cancelEvent(@PathParam("event-id") String eventIdRaw) {
        Long eventId=id(eventIdRaw);
        bookingService.cancelEvent(eventId);
        return Response.noContent().build();
    }
}
