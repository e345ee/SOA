package se.ifmo.ru.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import se.ifmo.ru.repository.TicketRepository;

@ApplicationScoped
public class TicketStatisticsService {
    @Inject
    TicketRepository ticketRepository;
    public TicketStatisticsService() {
    }
    public Double getSum(){
        return ticketRepository.getPriceSum();
    }
    public Double getAvg(){
        return ticketRepository.averagePrice();
    }
    public Long countByPerson(Long personId) {
        return ticketRepository.countByPerson(personId);
    }
}
