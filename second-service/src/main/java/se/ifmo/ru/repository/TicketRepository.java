package se.ifmo.ru.repository;

import jakarta.data.Sort;
import jakarta.data.page.PageRequest;
import jakarta.data.repository.By;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Repository;
import jakarta.persistence.criteria.*;
import org.hibernate.StatelessSession;
import se.ifmo.ru.dto.request.TicketSearchFilterDto;
import se.ifmo.ru.model.Ticket;
import se.ifmo.ru.repository.criteria.CriteriaPredicates;
import se.ifmo.ru.repository.criteria.TicketCriteriaSearch;

import java.util.ArrayList;
import java.util.List;

@Repository(provider = "Hibernate", dataStore = "default")
public interface    TicketRepository extends CrudRepository<Ticket, Long> {

     @Delete
     int deleteTicketById(@By("id") Long ticketId);

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


    default Long countByPerson(Long personId) {
        StatelessSession session = session();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Ticket> ticket = query.from(Ticket.class);
        query.select(cb.count(ticket))
                .where(cb.equal(ticket.get("person").get("id"), personId));
        return session.createQuery(query).getSingleResult();
    }
}
