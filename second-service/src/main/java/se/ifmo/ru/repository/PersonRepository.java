package se.ifmo.ru.repository;

import jakarta.data.Sort;
import jakarta.data.page.PageRequest;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Repository;
import jakarta.persistence.criteria.*;
import org.hibernate.StatelessSession;
import se.ifmo.ru.model.Person;
import se.ifmo.ru.dto.request.PersonSearchFilterDto;
import se.ifmo.ru.utils.PersonCriteriaSearch;

import java.util.ArrayList;
import java.util.List;

@Repository(provider = "Hibernate", dataStore = "default")
public interface PersonRepository extends CrudRepository<Person, Long> {
    StatelessSession session();

    default List<Person> search(PersonSearchFilterDto filter, PageRequest pageRequest, List<Sort<Person>> sorts) {
        StatelessSession session = session();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Person> query = cb.createQuery(Person.class);
        Root<Person> root = query.from(Person.class);
        List<Order> orderBy = new ArrayList<>();
        if (sorts != null) {
            for (Sort<Person> sort : sorts) {
                Path<?> column = root;
                for (String part : sort.property().split("\\.")) {
                    column = column.get(part);
                }
                orderBy.add(sort.isAscending() ? cb.asc(column) : cb.desc(column));
            }
        }
        query.select(root)
                .where(PersonCriteriaSearch.predicates(cb, root, filter))
                .orderBy(orderBy);

        int offset = Math.toIntExact(Math.multiplyExact(pageRequest.page() - 1, (long) pageRequest.size()));
        return session.createQuery(query)
                .setFirstResult(offset)
                .setMaxResults(pageRequest.size())
                .getResultList();
    }

    default Long count(PersonSearchFilterDto filter) {
        StatelessSession session = session();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Person> root = query.from(Person.class);

        query.select(cb.count(root))
                .where(PersonCriteriaSearch.predicates(cb, root, filter));
        return session.createQuery(query).getSingleResult();
    }

}
