package se.ifmo.ru.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import se.ifmo.ru.repository.TicketRepository;
import se.ifmo.ru.dto.request.PersonInputDto;
import java.util.LinkedHashMap;
import java.util.Map;

@ApplicationScoped
public class TicketStatisticsService {
    @Inject
    TicketRepository ticketRepository;
    public TicketStatisticsService() {
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public Double getSum(){
        Double value=ticketRepository.getPriceSum();return value==null?0.0:value;
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public Double getAvg(){
        Double value=ticketRepository.averagePrice();return value==null?0.0:value;
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public Long countByPerson(PersonInputDto person) {
        Map<String,Object> filters=new LinkedHashMap<>();
        if(person.getId()!=null)filters.put("person.id",person.getId());
        filters.put("person.birthday",person.getBirthday());filters.put("person.weight",person.getWeight());
        filters.put("person.location.x",person.getLocation().x());filters.put("person.location.y",person.getLocation().y());filters.put("person.location.name",person.getLocation().name());
        return ticketRepository.count(filters);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public Long countByPerson(Long personId) {
        return ticketRepository.countByPerson(personId);
    }
}
