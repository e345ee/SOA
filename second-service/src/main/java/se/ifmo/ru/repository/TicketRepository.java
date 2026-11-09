package se.ifmo.ru.repository;

import jakarta.data.Sort;
import jakarta.data.page.PageRequest;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Repository;
import jakarta.persistence.criteria.*;
import org.hibernate.StatelessSession;
import se.ifmo.ru.model.Ticket;
import se.ifmo.ru.dto.request.TicketSearchFilterDto;
import se.ifmo.ru.dto.request.PersonInputDto;
import se.ifmo.ru.utils.TicketCriteriaSearch;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository(provider = "Hibernate", dataStore = "default")
public interface    TicketRepository extends CrudRepository<Ticket, Long> {
    StatelessSession session();

    default List<Ticket> search(TicketSearchFilterDto filter, PageRequest pageRequest, List<Sort<Ticket>> sorts) {
        StatelessSession session = session();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Ticket> query = cb.createQuery(Ticket.class);
        Root<Ticket> root = query.from(Ticket.class);
        List<Order> orderBy = new ArrayList<>();
        if (sorts != null) {
            for (Sort<Ticket> sort : sorts) {
                Path<?> column = root;
                for (String part : sort.property().split("\\.")) {
                    column = column.get(part);
                }
                orderBy.add(sort.isAscending() ? cb.asc(column) : cb.desc(column));
            }
        }
        root.fetch("person", JoinType.LEFT);
        query.select(root)
                .where(TicketCriteriaSearch.predicates(cb, root, filter))
                .orderBy(orderBy);
        int offset = Math.toIntExact(Math.multiplyExact(pageRequest.page() - 1, (long) pageRequest.size()));
        return session.createQuery(query)
                .setFirstResult(offset)
                .setMaxResults(pageRequest.size())
                .getResultList();
    }

    default Long count(TicketSearchFilterDto filter) {
        StatelessSession session = session();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Ticket> root = query.from(Ticket.class);

        query.select(cb.count(root))
                .where(TicketCriteriaSearch.predicates(cb, root, filter));
        return session.createQuery(query).getSingleResult();
    }

    default Double getPriceSum(){
        return session()
                .createQuery("SELECT sum(t.price) FROM Ticket t", Double.class)
                .getSingleResult();
    }

    default Double averagePrice(){
        return session()
                .createQuery("SELECT avg(t.price) FROM Ticket t", Double.class)
                .getSingleResult();
    }

    default int deleteByEventId(long eventId) {
        return session()
                .createQuery("DELETE FROM Ticket t WHERE t.eventId = :eventId")
                .setParameter("eventId", eventId)
                .executeUpdate();
    }

    default Long countByPerson(PersonInputDto person) {
        StatelessSession session = session();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Ticket> ticket = query.from(Ticket.class);
        Path<?> personPath = ticket.get("person");
        Path<?> location = personPath.get("location");
        List<Predicate> conditions = new ArrayList<>();

        if (person.id() != null) {
            conditions.add(cb.equal(personPath.get("id"), person.id()));
        }
        conditions.add(equalOrNull(cb, personPath.get("birthday"), person.birthday()));
        conditions.add(equalOrNull(cb, personPath.get("weight"), person.weight()));
        conditions.add(equalOrNull(cb, location.get("x"), person.location().x()));
        conditions.add(equalOrNull(cb, location.get("y"), person.location().y()));
        conditions.add(equalOrNull(cb, location.get("name"), person.location().name()));

        query.select(cb.count(ticket)).where(conditions.toArray(Predicate[]::new));
        return session.createQuery(query).getSingleResult();
    }

    private static Predicate equalOrNull(CriteriaBuilder cb, Path<?> path, Object value) {
        return value == null ? cb.isNull(path) : cb.equal(path, value);
    }

    default Long countByPerson(Long personId) {
        return session()
                .createNativeQuery("""
                    SELECT COUNT(*)
                    FROM tickets
                    WHERE person_id = :personId
                """, Long.class)
                .setParameter("personId", personId)
                .getSingleResult();
    }
}
