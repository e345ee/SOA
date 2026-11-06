package se.ifmo.ru.service;

import se.ifmo.ru.utils.SearchValidation;
import java.util.Set;
import jakarta.data.Sort;
import jakarta.data.page.PageRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import se.ifmo.ru.dto.request.*;
import se.ifmo.ru.error.exceptions.RequestValidationException;
import se.ifmo.ru.error.exceptions.ResourceNotFoundException;
import se.ifmo.ru.dto.response.ResponsePaginationDto;
import se.ifmo.ru.dto.response.TicketResponseDto;
import se.ifmo.ru.mapper.TicketMapper;
import se.ifmo.ru.model.Person;
import se.ifmo.ru.model.Ticket;
import se.ifmo.ru.repository.TicketRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class TicketService {
    @Inject
    private TicketStatisticsService ticketStatisticsService;
    @Inject
    private PersonService personService;
    @Inject
    private TicketRepository ticketRepository;
    @Inject
    private TicketMapper ticketMapper;

    public TicketResponseDto findById(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(
                () -> new ResourceNotFoundException("Ticket with id " + ticketId + " not found"));
        return ticketMapper.toTicketResponseDto(ticket);
    }

    @Transactional
    public TicketResponseDto saveTicket(TicketCreateDto ticketCreateDto) {
        Person person = parsePerson(ticketCreateDto.personId(), ticketCreateDto.person());
        Ticket ticket = ticketMapper.toTicket(ticketCreateDto);
        ticket.setPerson(person);
        return ticketMapper.toTicketResponseDto(ticketRepository.insert(ticket));
    }

    @Transactional
    public void deleteTicketById(Long ticketId){
        if (ticketRepository.findById(ticketId).isPresent()) {
            ticketRepository.deleteById(ticketId);
        }else{
            throw new ResourceNotFoundException("Ticket with id " + ticketId + " not found");
        }
    }

    @Transactional
    public TicketResponseDto updateTicket(Long ticketId, TicketUpdateDto ticketCreateDto) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(
                () -> new ResourceNotFoundException("Ticket with id " + ticketId + " not found"));
        Person person = parsePerson(null, ticketCreateDto.person());
        ticketMapper.updateTicket(ticketCreateDto, ticket);
        ticket.setPerson(person);
        return ticketMapper.toTicketResponseDto(ticketRepository.update(ticket));
    }

    private Person parsePerson(Long personId, PersonInputDto personDto) {
        if (personId == null && personDto == null)
            throw new RequestValidationException("person", "Укажите personId или person");
        if (personId != null && personDto != null)
            throw new RequestValidationException("person", "Нельзя одновременно указывать personId и person");
        if (personId != null) return personService.getPersonById(personId);
        if(personDto.id()!=null)return personService.getPersonById(personDto.id());
        return personService.savePersonEntity(personDto.toCreate());
    }

    public Long countByPerson(PersonInputDto person) {
        return ticketStatisticsService.countByPerson(person);
    }

    public Double getSum(){
        return ticketStatisticsService.getSum();
    }

    public Double getAvg(){
        return ticketStatisticsService.getAvg();
    }

    public ResponsePaginationDto<TicketResponseDto> search(SearchDto<TicketSearchFilterDto> dto) {
        SearchValidation.validateSorts(dto.getSort(), Set.of("id", "name", "coordinates.x", "coordinates.y", "creationDate", "price", "type", "person.birthday", "person.weight", "person.location.x", "person.location.y", "person.location.name", "eventId"));
        List<Sort<Ticket>> sorts = new ArrayList<>();
        if (dto.getSort() != null) {
            for (SortDto s : dto.getSort()) {
                String path = s.field();
                if ("DESC".equalsIgnoreCase(s.direction())) {
                    sorts.add(Sort.desc(path));
                } else {
                    sorts.add(Sort.asc(path));
                }
            }
        }
        if (sorts.stream().noneMatch(sort -> sort.property().equals("id"))) {
            sorts.add(Sort.asc("id"));
        }
        TicketSearchFilterDto filter = dto.getFilter();
        long totalCount = ticketRepository.count(filter);
        long totalPages = totalCount / dto.getSize();
        if (totalCount % dto.getSize() != 0) {
            totalPages++;
        }
        List<TicketResponseDto> content;
        if (dto.getPage() >= totalPages) {
            content = List.of();
        } else {
            PageRequest pageRequest = PageRequest.ofPage(dto.getPage() + 1).size(dto.getSize().intValue());
            content = ticketRepository.search(filter, pageRequest, sorts)
                    .stream()
                    .map(ticketMapper::toTicketResponseDto)
                    .toList();
        }
        return ResponsePaginationDto.ans(content, dto.getPage(), dto.getSize(), totalCount);
    }

    public ResponsePaginationDto<TicketResponseDto> getTickets(long page, int size) {
        return search(new SearchDto<TicketSearchFilterDto>(null, page, (long) size, null));
    }

}
