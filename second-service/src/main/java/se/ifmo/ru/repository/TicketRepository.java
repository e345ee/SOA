package se.ifmo.ru.repository;

import jakarta.data.Sort;
import jakarta.data.page.PageRequest;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Repository;
import org.hibernate.StatelessSession;
import se.ifmo.ru.model.Ticket;
import se.ifmo.ru.utils.CriteriaSearch;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository(provider = "Hibernate", dataStore = "default")
public interface TicketRepository extends CrudRepository<Ticket, Long> {
    StatelessSession session();
    default List<Ticket> search(Map<String, Object> filters, PageRequest pageRequest, List<Sort<Ticket>> sorts) {
        return CriteriaSearch.search(session(), Ticket.class, filters, pageRequest, sorts);
    }

    default Long count(Map<String, Object> filters) {
        return CriteriaSearch.count(session(), Ticket.class, filters);
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

    default Long countByPersonValues(LocalDate birthday, Integer weight,
                                     Integer x, Integer y, String name) {
        return session()
                .createNativeQuery("""
                    SELECT COUNT(*) FROM tickets
                    WHERE person_id IN (
                        SELECT id FROM persons
                        WHERE birthday = :birthday
                          AND weight = :weight
                          AND x = :x
                          AND y = :y
                          AND name = :name)
                """, Long.class)
                .setParameter("birthday", birthday)
                .setParameter("weight", weight)
                .setParameter("x", x)
                .setParameter("y", y)
                .setParameter("name", name)
                .getSingleResult();
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
