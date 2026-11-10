package se.ifmo.ru.repository.criteria;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import se.ifmo.ru.dto.request.TicketSearchFilterDto;
import se.ifmo.ru.model.Ticket;
import se.ifmo.ru.model.Ticket_;
import se.ifmo.ru.model.Person_;
import se.ifmo.ru.model.Coordinates_;
import se.ifmo.ru.model.Location_;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static se.ifmo.ru.repository.criteria.CriteriaPredicates.add;
import static se.ifmo.ru.repository.criteria.CriteriaPredicates.contains;

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
                value -> cb.equal(ticket.get(Ticket_.id), value));
        add(result, filter.getNameContains(),
                value -> cb.like(cb.lower(ticket.get(Ticket_.name)), contains(value), '\\'));
        add(result, filter.getPriceFrom(),
                value -> cb.ge(ticket.get(Ticket_.price), value));
        add(result, filter.getPriceTo(),
                value -> cb.le(ticket.get(Ticket_.price), value));
        if (filter.isTypeSpecified()) {
            result.add(filter.getType() == null
                    ? cb.isNull(ticket.get(Ticket_.type))
                    : cb.equal(ticket.get(Ticket_.type), filter.getType()));
        }
        add(result, filter.getEventId(),
                value -> cb.equal(ticket.get(Ticket_.eventId), value));
    }

    private static void addCoordinates(CriteriaBuilder cb, Root<Ticket> ticket,
                                         TicketSearchFilterDto filter, List<Predicate> result) {
        add(result, filter.getCoordinatesXFrom(),
                value -> cb.ge(ticket.get(Ticket_.coordinates).get(Coordinates_.x), value));
        add(result, filter.getCoordinatesXTo(),
                value -> cb.le(ticket.get(Ticket_.coordinates).get(Coordinates_.x), value));
        add(result, filter.getCoordinatesYFrom(),
                value -> cb.ge(ticket.get(Ticket_.coordinates).get(Coordinates_.y), value));
        add(result, filter.getCoordinatesYTo(),
                value -> cb.le(ticket.get(Ticket_.coordinates).get(Coordinates_.y), value));
    }

    private static void addCreationDate(CriteriaBuilder cb, Root<Ticket> ticket,
                                         TicketSearchFilterDto filter, List<Predicate> result) {
        add(result, filter.getCreationDateFrom(),
                value -> cb.greaterThanOrEqualTo(ticket.get(Ticket_.creationDate), Date.from(value.toInstant())));
        add(result, filter.getCreationDateTo(),
                value -> cb.lessThanOrEqualTo(ticket.get(Ticket_.creationDate), Date.from(value.toInstant())));
    }

    private static void addPersonFields(CriteriaBuilder cb, Root<Ticket> ticket,
                                         TicketSearchFilterDto filter, List<Predicate> result) {
        add(result, filter.getPersonBirthday(),
                value -> cb.equal(ticket.get(Ticket_.person).get(Person_.birthday), value));
        add(result, filter.getPersonBirthdayFrom(),
                value -> cb.greaterThanOrEqualTo(ticket.get(Ticket_.person).get(Person_.birthday), value));
        add(result, filter.getPersonBirthdayTo(),
                value -> cb.lessThanOrEqualTo(ticket.get(Ticket_.person).get(Person_.birthday), value));
        add(result, filter.getPersonWeight(),
                value -> cb.equal(ticket.get(Ticket_.person).get(Person_.weight), value));
        add(result, filter.getPersonWeightFrom(),
                value -> cb.ge(ticket.get(Ticket_.person).get(Person_.weight), value));
        add(result, filter.getPersonWeightTo(),
                value -> cb.le(ticket.get(Ticket_.person).get(Person_.weight), value));
        add(result, filter.getPersonLocationX(),
                value -> cb.equal(ticket.get(Ticket_.person).get(Person_.location).get(Location_.x), value));
        add(result, filter.getPersonLocationY(),
                value -> cb.equal(ticket.get(Ticket_.person).get(Person_.location).get(Location_.y), value));
        add(result, filter.getPersonLocationNameContains(),
                value -> cb.like(cb.lower(ticket.get(Ticket_.person).get(Person_.location).get(Location_.name)), contains(value), '\\'));
    }
}
