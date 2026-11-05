package se.ifmo.ru.service;

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

@ApplicationScoped
public class PersonService {
    @Inject
    private PersonRepository personRepository;
    @Inject
    private PersonMapper personMapper;
    @Inject
    private TicketStatisticsService ticketStatisticsService;

    public PersonResponseDto findById(Long personId) {
        Person person = personRepository.findById(personId).orElseThrow(
                () -> new ResourceNotFoundException("person with id " + personId + " not found"));
        return personMapper.toPersonResponseDto(person);
    }
    @Transactional
    public PersonResponseDto savePerson(PersonCreateDto personCreateDto) {
        Person person = savePersonEntity(personCreateDto);
        return personMapper.toPersonResponseDto(person);
    }

    @Transactional
    public Person savePersonEntity(PersonCreateDto personCreateDto) {
        Person person = personMapper.toPerson(personCreateDto);
        return personRepository.insert(person);
    }
    @Transactional
    public void deletePersonById(Long personId){
        if (personRepository.findById(personId).isPresent()) {
            long count = ticketStatisticsService.countByPerson(personId);
            if (count > 0) throw new ResourceConflictException("Person with id " + personId + " is used by "+ count + " ticket(s)");
            personRepository.deleteById(personId);
        }else{
            throw new ResourceNotFoundException("person with id " + personId + " not found");
        }
    }
    @Transactional
    public PersonResponseDto updatePerson(Long personId, PersonCreateDto personCreateDto) {
        Person person = personRepository.findById(personId).orElseThrow(
                () -> new ResourceNotFoundException("person with id " + personId + " not found"));
        personMapper.updatePersonFromDto(personCreateDto, person);
        Person newPerson = personRepository.update(person);
        return personMapper.toPersonResponseDto(newPerson);
    }

    public ResponsePaginationDto<PersonResponseDto> search(SearchDto<PersonSearchFilterDto> requestDto) {
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
                () -> new ResourceNotFoundException("person with id " + personId + " not found"));
    }
}
