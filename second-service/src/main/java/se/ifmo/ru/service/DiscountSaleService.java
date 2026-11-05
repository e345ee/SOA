package se.ifmo.ru.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import se.ifmo.ru.dto.request.TicketCreateDto;
import se.ifmo.ru.dto.response.TicketResponseDto;

import java.util.UUID;

@ApplicationScoped
public class DiscountSaleService {
    @PersistenceContext(unitName = "default")
    private EntityManager entityManager;

    @Inject
    private TicketService tickets;

    private final Jsonb json = JsonbBuilder.create();

    @PreDestroy
    void close() {
        try {
            json.close();
        } catch (Exception failure) {
            throw new IllegalStateException(failure);
        }
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public String sell(UUID key, long sourceId, long personId, float discount) {
        int claimed = entityManager.createNativeQuery("""
                INSERT INTO discount_sales(operation_key, source_id, person_id, discount)
                VALUES (?1, ?2, ?3, ?4) ON CONFLICT (operation_key) DO NOTHING
                """).setParameter(1, key).setParameter(2, sourceId)
                .setParameter(3, personId).setParameter(4, discount).executeUpdate();
        if (claimed == 0) {
            var previous = entityManager.createNativeQuery("""
                    SELECT response FROM discount_sales
                    WHERE operation_key = ?1 AND source_id = ?2 AND person_id = ?3 AND discount = ?4
                    """, String.class).setParameter(1, key).setParameter(2, sourceId)
                    .setParameter(3, personId).setParameter(4, discount).getResultList();
            if (previous.isEmpty()) {
                throw new WebApplicationException("Этот ключ уже использован для другой продажи", 409);
            }
            return (String) previous.get(0);
        }
        TicketResponseDto source = tickets.findById(sourceId);
        float price = (float) Math.max(1.0, source.price() * (1.0 - discount / 100.0));
        var sale = new TicketCreateDto(source.name(), source.coordinates(), price, source.type(),
                personId, null, source.eventId());
        String response = json.toJson(tickets.saveTicket(sale));
        entityManager.createNativeQuery("UPDATE discount_sales SET response = ?1 WHERE operation_key = ?2")
                .setParameter(1, response).setParameter(2, key).executeUpdate();
        return response;
    }
}
