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
import se.ifmo.ru.utils.ApiValidation;

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
        ApiValidation.birthday(personCreateDto.birthday());
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
        ApiValidation.birthday(personCreateDto.birthday());
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
        Long total = personRepository.count(filters);
        List<Person> people = requestDto.getPage() > total / requestDto.getSize() ? List.of() : personRepository.search(filters,PageRequest.ofPage(requestDto.getPage()+1).size(Math.toIntExact(requestDto.getSize())),sorts);
        List<PersonResponseDto> content = people.stream().map(person -> personMapper.toPersonResponseDto(person)).toList();
        return ResponsePaginationDto.ans(content,requestDto.getPage(),requestDto.getSize(),total);
    }

    public ResponsePaginationDto<PersonResponseDto> getPersons(long page, int size) {
        return search(new SearchDto(Map.of(),page,(long)size,null));
    }

    public Person getPersonById(Long personId) {
        return personRepository.findById(personId).orElseThrow(
                () -> new ResourceNotFoundException("person with id " + personId + " not found"));
    }
}
