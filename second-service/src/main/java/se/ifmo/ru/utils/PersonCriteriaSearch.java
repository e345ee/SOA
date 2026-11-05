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
        add(result, filter.getId(),
                value -> cb.equal(person.get("id"), value));
        add(result, filter.getBirthday(),
                value -> cb.equal(person.get("birthday"), value));
        add(result, filter.getBirthdayFrom(),
                value -> cb.greaterThanOrEqualTo(person.get("birthday"), value));
        add(result, filter.getBirthdayTo(),
                value -> cb.lessThanOrEqualTo(person.get("birthday"), value));
        add(result, filter.getWeight(),
                value -> cb.equal(person.get("weight"), value));
        add(result, filter.getWeightFrom(),
                value -> cb.ge(person.get("weight"), value));
        add(result, filter.getWeightTo(),
                value -> cb.le(person.get("weight"), value));
    }

    private static void addLocation(CriteriaBuilder cb, Root<Person> person,
                                         PersonSearchFilterDto filter, List<Predicate> result) {
        add(result, filter.getLocationX(),
                value -> cb.equal(person.get("location").get("x"), value));
        add(result, filter.getLocationY(),
                value -> cb.equal(person.get("location").get("y"), value));
        add(result, filter.getLocationNameContains(),
                value -> cb.like(cb.lower(person.get("location").get("name")), contains(value), '\\'));
    }
}
