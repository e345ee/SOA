package se.ifmo.ru.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import se.ifmo.ru.repository.TicketRepository;
import se.ifmo.ru.dto.request.PersonInputDto;
import se.ifmo.ru.utils.ApiValidation;
import java.util.LinkedHashMap;
import java.util.Map;

@ApplicationScoped
public class TicketStatisticsService {
    @Inject
    TicketRepository ticketRepository;
    public TicketStatisticsService() {
    }
    public Double getSum(){
        Double value=ticketRepository.getPriceSum();return value==null?0.0:value;
    }
    public Double getAvg(){
        Double value=ticketRepository.averagePrice();return value==null?0.0:value;
    }
    public Long countByPerson(PersonInputDto person) {
        ApiValidation.birthday(person.birthday());
        Map<String,Object> filters=new LinkedHashMap<>();
        if(person.id()!=null)filters.put("person.id",person.id());
        filters.put("person.birthday",person.birthday());filters.put("person.weight",person.weight());
        filters.put("person.location.x",person.location().x());filters.put("person.location.y",person.location().y());filters.put("person.location.name",person.location().name());
        return ticketRepository.count(filters);
    }

    public Long countByPerson(Long personId) {
        return ticketRepository.countByPerson(personId);
    }
}
