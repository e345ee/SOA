package se.ifmo.ru.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import se.ifmo.ru.repository.TicketRepository;
import se.ifmo.ru.dto.request.PersonCreateDto;

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
    public Long countByPerson(PersonCreateDto person) {
        var location = person.location();
        return ticketRepository.countByPersonValues(
                person.birthday(), person.weight(),
                location.x(), location.y(), location.name());
    }

    public Long countByPerson(Long personId) {
        return ticketRepository.countByPerson(personId);
    }
}
