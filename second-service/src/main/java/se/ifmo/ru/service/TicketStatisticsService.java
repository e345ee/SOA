package se.ifmo.ru.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import se.ifmo.ru.repository.TicketRepository;


@Slf4j
@ApplicationScoped
public class TicketStatisticsService {
    @Inject
    TicketRepository ticketRepository;
    public TicketStatisticsService() {
    }
    @Transactional(Transactional.TxType.SUPPORTS)
    public Double getSum(){
        log.info("Расчёт суммы цен билетов");
        Double value=ticketRepository.getPriceSum();
        if (value==null){
            return 0.0;
        }
        return value;
    }
    @Transactional(Transactional.TxType.SUPPORTS)
    public Double getAvg(){
        log.info("Расчёт средней цены билетов");
        Double value=ticketRepository.averagePrice();
        if (value==null) return 0.0;
        return value;
    }
    @Transactional(Transactional.TxType.SUPPORTS)
    public Long countByPerson(Long personId) {
        log.info("Подсчёт билетов: personId={}", personId);
        return ticketRepository.countByPerson(personId);
    }
}
