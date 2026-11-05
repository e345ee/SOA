package se.ifmo.ru.utils;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import se.ifmo.ru.dto.request.TicketSearchFilterDto;
import se.ifmo.ru.model.Ticket;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static se.ifmo.ru.utils.CriteriaPredicates.add;
import static se.ifmo.ru.utils.CriteriaPredicates.contains;

public final class TicketCriteriaSearch {
    private TicketCriteriaSearch() {}

    public static Predicate[] predicates(CriteriaBuilder cb, Root<Ticket> ticket,
                                         TicketSearchFilterDto filter) {
        List<Predicate> result = new ArrayList<>();
        if (filter == null) return new Predicate[0];
        addTicketFields(cb, ticket, filter, result);
        addCoordinates(cb, ticket, filter, result);
        addCreationDate(cb, ticket, filter, result);
        addPersonFields(cb, ticket, filter, result);
        return result.toArray(Predicate[]::new);
    }

    private static void addTicketFields(CriteriaBuilder cb, Root<Ticket> ticket,
                                         TicketSearchFilterDto filter, List<Predicate> result) {
        add(result, filter.getId(),
                value -> cb.equal(ticket.get("id"), value));
        add(result, filter.getNameContains(),
                value -> cb.like(cb.lower(ticket.get("name")), contains(value), '\\'));
        add(result, filter.getPriceFrom(),
                value -> cb.ge(ticket.get("price"), value));
        add(result, filter.getPriceTo(),
                value -> cb.le(ticket.get("price"), value));
        if (filter.isTypeSpecified()) {
            result.add(filter.getType() == null
                    ? cb.isNull(ticket.get("type"))
                    : cb.equal(ticket.get("type"), filter.getType()));
        }
        add(result, filter.getEventId(),
                value -> cb.equal(ticket.get("eventId"), value));
    }

    private static void addCoordinates(CriteriaBuilder cb, Root<Ticket> ticket,
                                         TicketSearchFilterDto filter, List<Predicate> result) {
        add(result, filter.getCoordinatesXFrom(),
                value -> cb.ge(ticket.get("coordinates").get("x"), value));
        add(result, filter.getCoordinatesXTo(),
                value -> cb.le(ticket.get("coordinates").get("x"), value));
        add(result, filter.getCoordinatesYFrom(),
                value -> cb.ge(ticket.get("coordinates").get("y"), value));
        add(result, filter.getCoordinatesYTo(),
                value -> cb.le(ticket.get("coordinates").get("y"), value));
    }

    private static void addCreationDate(CriteriaBuilder cb, Root<Ticket> ticket,
                                         TicketSearchFilterDto filter, List<Predicate> result) {
        add(result, filter.getCreationDateFrom(),
                value -> cb.greaterThanOrEqualTo(ticket.<Date>get("creationDate"), Date.from(value.toInstant())));
        add(result, filter.getCreationDateTo(),
                value -> cb.lessThanOrEqualTo(ticket.<Date>get("creationDate"), Date.from(value.toInstant())));
    }

    private static void addPersonFields(CriteriaBuilder cb, Root<Ticket> ticket,
                                         TicketSearchFilterDto filter, List<Predicate> result) {
        add(result, filter.getPersonBirthday(),
                value -> cb.equal(ticket.get("person").get("birthday"), value));
        add(result, filter.getPersonBirthdayFrom(),
                value -> cb.greaterThanOrEqualTo(ticket.get("person").get("birthday"), value));
        add(result, filter.getPersonBirthdayTo(),
                value -> cb.lessThanOrEqualTo(ticket.get("person").get("birthday"), value));
        add(result, filter.getPersonWeight(),
                value -> cb.equal(ticket.get("person").get("weight"), value));
        add(result, filter.getPersonWeightFrom(),
                value -> cb.ge(ticket.get("person").get("weight"), value));
        add(result, filter.getPersonWeightTo(),
                value -> cb.le(ticket.get("person").get("weight"), value));
        add(result, filter.getPersonLocationX(),
                value -> cb.equal(ticket.get("person").get("location").get("x"), value));
        add(result, filter.getPersonLocationY(),
                value -> cb.equal(ticket.get("person").get("location").get("y"), value));
        add(result, filter.getPersonLocationNameContains(),
                value -> cb.like(cb.lower(ticket.get("person").get("location").get("name")), contains(value), '\\'));
    }
}
