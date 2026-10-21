package se.ifmo.ru.service;

import jakarta.data.Sort;
import jakarta.data.page.PageRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import se.ifmo.ru.dto.request.PersonCreateDto;
import se.ifmo.ru.dto.request.PersonInputDto;
import se.ifmo.ru.dto.request.TicketUpdateDto;
import se.ifmo.ru.error.exceptions.RequestValidationException;
import se.ifmo.ru.error.exceptions.ResourceNotFoundException;
import se.ifmo.ru.dto.request.SearchDto;
import se.ifmo.ru.dto.request.TicketSearchFilter;
import se.ifmo.ru.dto.request.SortDto;
import se.ifmo.ru.dto.request.TicketCreateDto;
import se.ifmo.ru.dto.response.ResponsePaginationDto;
import se.ifmo.ru.dto.response.TicketResponseDto;
import se.ifmo.ru.mapper.TicketMapper;
import se.ifmo.ru.model.Person;
import se.ifmo.ru.model.Ticket;
import se.ifmo.ru.repository.TicketRepository;
import se.ifmo.ru.utils.TicketSearchValidator;

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

    @Transactional(Transactional.TxType.REQUIRED)
    public TicketResponseDto findById(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(
                () -> new ResourceNotFoundException("Ticket with id " + ticketId + " not found"));
        return ticketMapper.toTicketResponseDto(ticket);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public TicketResponseDto saveTicket(TicketCreateDto ticketCreateDto) {
        Person person = parsePerson(ticketCreateDto.getPersonId(), ticketCreateDto.getPerson());
        Ticket ticket = ticketMapper.toTicket(ticketCreateDto);
        ticket.setPerson(person);
        return ticketMapper.toTicketResponseDto(ticketRepository.insert(ticket));
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void deleteTicketById(Long ticketId){
        if (ticketRepository.findById(ticketId).isPresent()) {
            ticketRepository.deleteById(ticketId);
        }else{
            throw new ResourceNotFoundException("Ticket with id " + ticketId + " not found");
        }
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public TicketResponseDto updateTicket(Long ticketId, TicketUpdateDto ticketCreateDto) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(
                () -> new ResourceNotFoundException("Ticket with id " + ticketId + " not found"));
        Person person = parsePerson(null, ticketCreateDto.getPerson());
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
        if(personDto.getId()!=null)return personService.getPersonById(personDto.getId());
        return personService.savePersonEntity(personDto.toCreate());
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public Long countByPerson(PersonInputDto person) {
        return ticketStatisticsService.countByPerson(person);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public Double getSum(){
        return ticketStatisticsService.getSum();
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public Double getAvg(){
        return ticketStatisticsService.getAvg();
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public ResponsePaginationDto<TicketResponseDto> search(SearchDto<TicketSearchFilter> dto) {
        Map<String, Object> filters = TicketSearchValidator.validateSearch(dto);
        List<Sort<Ticket>> sorts = new ArrayList<>();
        if (dto.getSort() != null) {
            for (SortDto s : dto.getSort()) {
                String path = s.field();
                sorts.add("DESC".equalsIgnoreCase(s.direction()) ? Sort.desc(path) : Sort.asc(path));
            }
        }
        if (sorts.stream().noneMatch(sort -> sort.property().equals("id"))) {
            sorts.add(Sort.asc("id"));
        }
        long total = ticketRepository.count(filters);
        List<TicketResponseDto> content = dto.getPage() > total / dto.getSize() ? List.of() : ticketRepository
                .search(filters, PageRequest.ofPage(dto.getPage() + 1).size(dto.getSize().intValue()), sorts)
                .stream()
                .map(ticketMapper::toTicketResponseDto)
                .toList();
        return ResponsePaginationDto.ans(content, dto.getPage(), dto.getSize(), total);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public ResponsePaginationDto<TicketResponseDto> getTickets(long page, int size) {
        return search(new SearchDto<>(null, page, (long) size, null));
    }

}
