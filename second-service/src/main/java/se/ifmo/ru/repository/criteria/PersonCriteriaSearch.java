package se.ifmo.ru.repository.criteria;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import se.ifmo.ru.dto.request.PersonSearchFilterDto;
import se.ifmo.ru.model.Person;
import se.ifmo.ru.model.Person_;
import se.ifmo.ru.model.Location_;

import java.util.ArrayList;
import java.util.List;

import static se.ifmo.ru.repository.criteria.CriteriaPredicates.add;
import static se.ifmo.ru.repository.criteria.CriteriaPredicates.contains;

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
                value -> cb.equal(person.get(Person_.id), value));
        add(result, filter.getBirthday(),
                value -> cb.equal(person.get(Person_.birthday), value));
        add(result, filter.getBirthdayFrom(),
                value -> cb.greaterThanOrEqualTo(person.get(Person_.birthday), value));
        add(result, filter.getBirthdayTo(),
                value -> cb.lessThanOrEqualTo(person.get(Person_.birthday), value));
        add(result, filter.getWeight(),
                value -> cb.equal(person.get(Person_.weight), value));
        add(result, filter.getWeightFrom(),
                value -> cb.ge(person.get(Person_.weight), value));
        add(result, filter.getWeightTo(),
                value -> cb.le(person.get(Person_.weight), value));
    }

    private static void addLocation(CriteriaBuilder cb, Root<Person> person,
                                         PersonSearchFilterDto filter, List<Predicate> result) {
        add(result, filter.getLocationX(),
                value -> cb.equal(person.get(Person_.location).get(Location_.x), value));
        add(result, filter.getLocationY(),
                value -> cb.equal(person.get(Person_.location).get(Location_.y), value));
        add(result, filter.getLocationNameContains(),
                value -> cb.like(cb.lower(person.get(Person_.location).get(Location_.name)), contains(value), '\\'));
    }
}
