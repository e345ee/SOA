package se.ifmo.ru.service;

import lombok.extern.slf4j.Slf4j;
import se.ifmo.ru.utils.SearchValidation;
import java.util.Set;
import jakarta.data.Sort;
import jakarta.data.page.PageRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import se.ifmo.ru.dto.request.PersonSearchFilterDto;
import se.ifmo.ru.error.exceptions.ResourceConflictException;
import se.ifmo.ru.error.exceptions.ResourceNotFoundException;
import se.ifmo.ru.dto.request.PersonCreateDto;
import se.ifmo.ru.dto.request.SearchDto;
import se.ifmo.ru.dto.response.PersonResponseDto;
import se.ifmo.ru.dto.response.ResponsePaginationDto;
import se.ifmo.ru.mapper.PersonMapper;
import se.ifmo.ru.model.Person;
import se.ifmo.ru.repository.PersonRepository;

import java.util.*;

@Slf4j
@ApplicationScoped
public class PersonService {
    @Inject
    private PersonRepository personRepository;
    @Inject
    private PersonMapper personMapper;
    @Inject
    private TicketStatisticsService ticketStatisticsService;

    public PersonResponseDto findById(Long personId) {
        log.debug("Поиск человека: personId={}", personId);
        Person person = personRepository.findById(personId).orElseThrow(
                () -> new ResourceNotFoundException("Человек с id " + personId + " не найден"));
        return personMapper.toPersonResponseDto(person);
    }
    @Transactional
    public PersonResponseDto savePerson(PersonCreateDto personCreateDto) {
        Person person = savePersonEntity(personCreateDto);
        return personMapper.toPersonResponseDto(person);
    }

    @Transactional
    public Person savePersonEntity(PersonCreateDto personCreateDto) {
        log.info("Создание человека");
        Person person = personMapper.toPerson(personCreateDto);
        Person saved = personRepository.insert(person);
        log.info("Запрос на создание человека выполнен: personId={}", saved.getId());
        return saved;
    }
    @Transactional
    public void deletePersonById(Long personId){
        log.info("Удаление человека: personId={}", personId);
        if (personRepository.findById(personId).isPresent()) {
            long count = ticketStatisticsService.countByPerson(personId);
            if (count > 0) throw new ResourceConflictException("Человек с id " + personId + " используется в билетах; количество: " + count);
            personRepository.deleteById(personId);
            log.info("Запрос на удаление человека выполнен: personId={}", personId);
        }else{
            throw new ResourceNotFoundException("Человек с id " + personId + " не найден");
        }
    }
    @Transactional
    public PersonResponseDto updatePerson(Long personId, PersonCreateDto personCreateDto) {
        log.info("Обновление человека: personId={}", personId);
        Person person = personRepository.findById(personId).orElseThrow(
                () -> new ResourceNotFoundException("Человек с id " + personId + " не найден"));
        personMapper.updatePersonFromDto(personCreateDto, person);
        Person newPerson = personRepository.update(person);
        log.info("Запрос на обновление человека выполнен: personId={}", personId);
        return personMapper.toPersonResponseDto(newPerson);
    }

    public ResponsePaginationDto<PersonResponseDto> search(SearchDto<PersonSearchFilterDto> requestDto) {
        log.debug("Поиск людей: страница={}, размер={}", requestDto.getPage(), requestDto.getSize());
        SearchValidation.validateSorts(requestDto.getSort(), Set.of("id", "birthday", "weight", "location.x", "location.y", "location.name"));
        List<Sort<Person>> sorts = new ArrayList<>();
        if (requestDto.getSort() != null) {
            for (var item : requestDto.getSort()) {
                String field = item.field();
                String direction = item.direction();
                if ("DESC".equalsIgnoreCase(direction)) {
                    sorts.add(Sort.desc(field));
                }else{
                    sorts.add(Sort.asc(field));
                }
            }
        }
        if (sorts.stream().noneMatch(s -> s.property().equals("id")))
            sorts.add(Sort.asc("id"));
        PersonSearchFilterDto filter = requestDto.getFilter();
        Long total = personRepository.count(filter);
        List<Person> people = requestDto.getPage() > total / requestDto.getSize() ? List.of() : personRepository.search(filter, PageRequest.ofPage(requestDto.getPage()+1).size(Math.toIntExact(requestDto.getSize())), sorts);
        List<PersonResponseDto> content = people.stream().map(person -> personMapper.toPersonResponseDto(person)).toList();
        return ResponsePaginationDto.ans(content,requestDto.getPage(),requestDto.getSize(),total);
    }

    public ResponsePaginationDto<PersonResponseDto> getPersons(long page, int size) {
        return search(new SearchDto<PersonSearchFilterDto>(null, page, (long) size, null));
    }

    public Person getPersonById(Long personId) {
        return personRepository.findById(personId).orElseThrow(
                () -> new ResourceNotFoundException("Человек с id " + personId + " не найден"));
    }
}
