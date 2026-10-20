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

    public ResponsePaginationDto<PersonResponseDto> search(SearchDto requestDto) {
        Map<String, Object> filters = PersonSearchValidator.validateSearch(requestDto);
        List<Sort<Person>> sorts = new ArrayList<>();
        if (requestDto.getSort() != null) {
            for (var item : requestDto.getSort()) {
                String field = PersonSearchValidator.field(item.field());
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
        PageRequest page = PageRequest.ofPage(requestDto.getPage()).size(Math.toIntExact(requestDto.getSize()));
        List<Person> people = personRepository.search(filters,page, sorts);
        List<PersonResponseDto> content = people.stream().map(person -> personMapper.toPersonResponseDto(person)).toList();
        Long total = personRepository.count(filters);
        return ResponsePaginationDto.ans(content,requestDto.getPage(),requestDto.getSize(),total);
    }

    public Person getPersonById(Long personId) {
        return personRepository.findById(personId).orElseThrow(
                () -> new ResourceNotFoundException("person with id " + personId + " not found"));
    }
}
