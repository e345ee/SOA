package se.ifmo.ru.utils;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import se.ifmo.ru.dto.request.PersonSearchFilterDto;
import se.ifmo.ru.model.Person;

import java.util.ArrayList;
import java.util.List;

import static se.ifmo.ru.utils.CriteriaPredicates.add;
import static se.ifmo.ru.utils.CriteriaPredicates.contains;

public final class PersonCriteriaSearch {
    private PersonCriteriaSearch() {}

    public static Predicate[] predicates(CriteriaBuilder cb, Root<Person> person,
                                         PersonSearchFilterDto filter) {
        List<Predicate> result = new ArrayList<>();
        if (filter == null) return new Predicate[0];
        addPersonFields(cb, person, filter, result);
        addLocation(cb, person, filter, result);
        return result.toArray(Predicate[]::new);
    }

    private static void addPersonFields(CriteriaBuilder cb, Root<Person> person,
                                         PersonSearchFilterDto filter, List<Predicate> result) {
        add(result, filter.id(),
                value -> cb.equal(person.get("id"), value));
        add(result, filter.birthday(),
                value -> cb.equal(person.get("birthday"), value));
        add(result, filter.birthdayFrom(),
                value -> cb.greaterThanOrEqualTo(person.get("birthday"), value));
        add(result, filter.birthdayTo(),
                value -> cb.lessThanOrEqualTo(person.get("birthday"), value));
        add(result, filter.weight(),
                value -> cb.equal(person.get("weight"), value));
        add(result, filter.weightFrom(),
                value -> cb.ge(person.get("weight"), value));
        add(result, filter.weightTo(),
                value -> cb.le(person.get("weight"), value));
    }

    private static void addLocation(CriteriaBuilder cb, Root<Person> person,
                                         PersonSearchFilterDto filter, List<Predicate> result) {
        add(result, filter.locationX(),
                value -> cb.equal(person.get("location").get("x"), value));
        add(result, filter.locationY(),
                value -> cb.equal(person.get("location").get("y"), value));
        add(result, filter.locationName(),
                value -> cb.like(cb.lower(person.get("location").get("name")), contains(value), '\\'));
    }
}
