package se.ifmo.ru.service;

import jakarta.data.Sort;
import jakarta.data.page.PageRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import se.ifmo.ru.error.exceptions.ResourceConflictException;
import se.ifmo.ru.error.exceptions.ResourceNotFoundException;
import se.ifmo.ru.dto.request.PersonCreateDto;
import se.ifmo.ru.dto.request.SearchDto;
import se.ifmo.ru.dto.request.PersonSearchFilter;
import se.ifmo.ru.dto.response.PersonResponseDto;
import se.ifmo.ru.dto.response.ResponsePaginationDto;
import se.ifmo.ru.mapper.PersonMapper;
import se.ifmo.ru.model.Person;
import se.ifmo.ru.repository.PersonRepository;
import se.ifmo.ru.utils.PersonSearchValidator;

import java.util.*;
import java.util.logging.Logger;

@ApplicationScoped
public class PersonService {
    @Inject
    private PersonRepository personRepository;
    @Inject
    private PersonMapper personMapper;
    @Inject
    private TicketStatisticsService ticketStatisticsService;

    @Transactional(Transactional.TxType.REQUIRED)
    public PersonResponseDto findById(Long personId) {
        Person person = personRepository.findById(personId).orElseThrow(
                () -> new ResourceNotFoundException("person with id " + personId + " not found"));
        return personMapper.toPersonResponseDto(person);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public PersonResponseDto savePerson(PersonCreateDto personCreateDto) {
        Person person = savePersonEntity(personCreateDto);
        return personMapper.toPersonResponseDto(person);
    }

    @Transactional(Transactional.TxType.MANDATORY)
    public Person savePersonEntity(PersonCreateDto personCreateDto) {
        Person person = personMapper.toPerson(personCreateDto);
        return personRepository.insert(person);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void deletePersonById(Long personId){
        if (personRepository.findById(personId).isPresent()) {
            long count = ticketStatisticsService.countByPerson(personId);
            if (count > 0) throw new ResourceConflictException("Person with id " + personId + " is used by "+ count + " ticket(s)");
            personRepository.deleteById(personId);
        }else{
            throw new ResourceNotFoundException("person with id " + personId + " not found");
        }
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public PersonResponseDto updatePerson(Long personId, PersonCreateDto personCreateDto) {
        Person person = personRepository.findById(personId).orElseThrow(
                () -> new ResourceNotFoundException("person with id " + personId + " not found"));
        personMapper.updatePersonFromDto(personCreateDto, person);
        Person newPerson = personRepository.update(person);
        return personMapper.toPersonResponseDto(newPerson);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public ResponsePaginationDto<PersonResponseDto> search(SearchDto<PersonSearchFilter> requestDto) {
        Map<String, Object> filters = PersonSearchValidator.validateSearch(requestDto);
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
        Long total = personRepository.count(filters);
        List<Person> people = requestDto.getPage() > total / requestDto.getSize() ? List.of() : personRepository.search(filters,PageRequest.ofPage(requestDto.getPage()+1).size(Math.toIntExact(requestDto.getSize())),sorts);
        List<PersonResponseDto> content = people.stream().map(person -> personMapper.toPersonResponseDto(person)).toList();
        return ResponsePaginationDto.ans(content,requestDto.getPage(),requestDto.getSize(),total);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public ResponsePaginationDto<PersonResponseDto> getPersons(long page, int size) {
        return search(new SearchDto<>(null, page, (long) size, null));
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public Person getPersonById(Long personId) {
        return personRepository.findById(personId).orElseThrow(
                () -> new ResourceNotFoundException("person with id " + personId + " not found"));
    }
}
