package se.ifmo.ru.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import se.ifmo.ru.repository.TicketRepository;
import se.ifmo.ru.dto.request.PersonInputDto;


@ApplicationScoped
public class TicketStatisticsService {
    @Inject
    TicketRepository ticketRepository;
    public TicketStatisticsService() {
    }
    public Double getSum(){
        Double value=ticketRepository.getPriceSum();
        if (value==null){
            return 0.0;
        }
        return value;
    }
    public Double getAvg(){
        Double value=ticketRepository.averagePrice();
        if (value==null) return 0.0;
        return value;
    }
    public Long countByPerson(PersonInputDto person) {
        return ticketRepository.countByPerson(person);
    }

    public Long countByPerson(Long personId) {
        return ticketRepository.countByPerson(personId);
    }
}
