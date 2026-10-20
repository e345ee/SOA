package se.ifmo.ru.repository;

import jakarta.data.Sort;
import jakarta.data.page.PageRequest;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Repository;
import jakarta.persistence.criteria.*;
import org.hibernate.StatelessSession;
import se.ifmo.ru.model.Person;
import se.ifmo.ru.utils.CriteriaSearch;

import java.util.List;
import java.util.Map;

@Repository
public interface PersonRepository extends CrudRepository<Person, Long> {
    StatelessSession session();

    default List<Person> search(Map<String, Object> filters, PageRequest pageRequest, List<Sort<Person>> sorts) {
        return CriteriaSearch.search(session(), Person.class, filters, pageRequest, sorts);
    }

    default Long count(Map<String, Object> filters) {
        return CriteriaSearch.count(session(), Person.class, filters);
    }
}
