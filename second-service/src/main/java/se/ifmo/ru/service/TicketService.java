package se.ifmo.ru.service;

import se.ifmo.ru.utils.SearchValidation;
import java.util.Set;
import jakarta.data.Sort;
import jakarta.data.page.PageRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
        log.info("Поиск билета: ticketId={}", ticketId);
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(
                () -> new ResourceNotFoundException("Билет с id " + ticketId + " не найден"));
        return ticketMapper.toTicketResponseDto(ticket);
    }

    @Transactional
    public TicketResponseDto saveTicket(TicketCreateDto ticketCreateDto) {
        log.info("Создание билета: eventId={}", ticketCreateDto.eventId());
        Person person = parsePerson(ticketCreateDto.personId(), ticketCreateDto.person());
        Ticket ticket = ticketMapper.toTicket(ticketCreateDto);
        ticket.setPerson(person);
        Ticket saved = ticketRepository.insert(ticket);
        log.info("Запрос на создание билета выполнен: ticketId={}, eventId={}", saved.getId(), saved.getEventId());
        return ticketMapper.toTicketResponseDto(saved);
    }

    @Transactional
    public TicketResponseDto sellWithDiscount(long ticketId, long personId, float discount) {
        log.info("Продажа билета со скидкой: ticketId={}, personId={}, скидка={}", ticketId, personId, discount);
        TicketResponseDto source = findById(ticketId);
        float price = (float) Math.max(1.0, source.price() * (1.0 - discount / 100.0));
        TicketCreateDto sale = new TicketCreateDto(source.name(), source.coordinates(), price,
                source.type(), personId, null, source.eventId());
        return saveTicket(sale);
    }

    @Transactional
    public void deleteTicketById(Long ticketId){
        log.info("Удаление билета: ticketId={}", ticketId);
        if (ticketRepository.findById(ticketId).isPresent()) {
            ticketRepository.deleteById(ticketId);
            log.info("Запрос на удаление билета выполнен: ticketId={}", ticketId);
        }else{
            throw new ResourceNotFoundException("Билет с id " + ticketId + " не найден");
        }
    }

    @Transactional
    public TicketResponseDto updateTicket(Long ticketId, TicketUpdateDto ticketCreateDto) {
        log.info("Обновление билета: ticketId={}", ticketId);
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(
                () -> new ResourceNotFoundException("Билет с id " + ticketId + " не найден"));
        Person person = parsePerson(null, ticketCreateDto.person());
        ticketMapper.updateTicket(ticketCreateDto, ticket);
        ticket.setPerson(person);
        Ticket updated = ticketRepository.update(ticket);
        log.info("Запрос на обновление билета выполнен: ticketId={}", ticketId);
        return ticketMapper.toTicketResponseDto(updated);
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
        log.debug("Поиск билетов: страница={}, размер={}", dto.getPage(), dto.getSize());
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
