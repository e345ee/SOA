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
        add(result, filter.id(),
                value -> cb.equal(ticket.get("id"), value));
        add(result, filter.name(),
                value -> cb.like(cb.lower(ticket.get("name")), contains(value), '\\'));
        add(result, filter.priceFrom(),
                value -> cb.ge(ticket.get("price"), value));
        add(result, filter.priceTo(),
                value -> cb.le(ticket.get("price"), value));
        add(result, filter.type(),
                value -> cb.equal(ticket.get("type"), value));
        add(result, filter.eventId(),
                value -> cb.equal(ticket.get("eventId"), value));
    }

    private static void addCoordinates(CriteriaBuilder cb, Root<Ticket> ticket,
                                         TicketSearchFilterDto filter, List<Predicate> result) {
        add(result, filter.coordinatesXFrom(),
                value -> cb.ge(ticket.get("coordinates").get("x"), value.doubleValue()));
        add(result, filter.coordinatesXTo(),
                value -> cb.le(ticket.get("coordinates").get("x"), value.doubleValue()));
        add(result, filter.coordinatesYFrom(),
                value -> cb.ge(ticket.get("coordinates").get("y"), value));
        add(result, filter.coordinatesYTo(),
                value -> cb.le(ticket.get("coordinates").get("y"), value));
    }

    private static void addCreationDate(CriteriaBuilder cb, Root<Ticket> ticket,
                                         TicketSearchFilterDto filter, List<Predicate> result) {
        add(result, filter.creationDateFrom(),
                value -> cb.greaterThanOrEqualTo(ticket.<Date>get("creationDate"), Date.from(value)));
        add(result, filter.creationDateTo(),
                value -> cb.lessThanOrEqualTo(ticket.<Date>get("creationDate"), Date.from(value)));
    }

    private static void addPersonFields(CriteriaBuilder cb, Root<Ticket> ticket,
                                         TicketSearchFilterDto filter, List<Predicate> result) {
        add(result, filter.personBirthday(),
                value -> cb.equal(ticket.get("person").get("birthday"), value));
        add(result, filter.personBirthdayFrom(),
                value -> cb.greaterThanOrEqualTo(ticket.get("person").get("birthday"), value));
        add(result, filter.personBirthdayTo(),
                value -> cb.lessThanOrEqualTo(ticket.get("person").get("birthday"), value));
        add(result, filter.personWeight(),
                value -> cb.equal(ticket.get("person").get("weight"), value));
        add(result, filter.personWeightFrom(),
                value -> cb.ge(ticket.get("person").get("weight"), value));
        add(result, filter.personWeightTo(),
                value -> cb.le(ticket.get("person").get("weight"), value));
        add(result, filter.personLocationX(),
                value -> cb.equal(ticket.get("person").get("location").get("x"), value));
        add(result, filter.personLocationY(),
                value -> cb.equal(ticket.get("person").get("location").get("y"), value));
        add(result, filter.personLocationName(),
                value -> cb.like(cb.lower(ticket.get("person").get("location").get("name")), contains(value), '\\'));
    }
}
