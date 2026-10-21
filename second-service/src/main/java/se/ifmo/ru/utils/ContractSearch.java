package se.ifmo.ru.utils;
import se.ifmo.ru.dto.request.SearchDto;
import se.ifmo.ru.dto.request.SortDto;
import se.ifmo.ru.enums.TicketType;
import se.ifmo.ru.error.exceptions.RequestValidationException;
import java.math.*;
import java.time.*;
import java.util.*;
public final class ContractSearch {
 private record Rule(String path,String kind,double minimum,boolean exclusive,int minLength) {}
 private static final Map<String,Rule> ticket=new LinkedHashMap<>(), person=new LinkedHashMap<>();
 private static final Set<String> ticketSort=Set.of("id","name","coordinates.x","coordinates.y","creationDate","price","type","person.birthday","person.weight","person.location.x","person.location.y","person.location.name","eventId"), personSort=Set.of("id","birthday","weight","location.x","location.y","location.name");
 static {
  ticket.put("id", new Rule("id","int64",1,false,0));
  ticket.put("nameContains", new Rule("name~","string",Double.NEGATIVE_INFINITY,false,1));
  ticket.put("coordinatesXFrom", new Rule("coordinates.x>=","double",-495,true,0));
  ticket.put("coordinatesXTo", new Rule("coordinates.x<=","double",Double.NEGATIVE_INFINITY,false,0));
  ticket.put("coordinatesYFrom", new Rule("coordinates.y>=","int32",-262,false,0));
  ticket.put("coordinatesYTo", new Rule("coordinates.y<=","int32",Double.NEGATIVE_INFINITY,false,0));
  ticket.put("creationDateFrom", new Rule("creationDate>=","datetime",Double.NEGATIVE_INFINITY,false,0));
  ticket.put("creationDateTo", new Rule("creationDate<=","datetime",Double.NEGATIVE_INFINITY,false,0));
  ticket.put("priceFrom", new Rule("price>=","float",0,true,0));
  ticket.put("priceTo", new Rule("price<=","float",0,true,0));
  ticket.put("type", new Rule("type","enum",Double.NEGATIVE_INFINITY,false,0));
  ticket.put("personBirthday", new Rule("person.birthday","date",Double.NEGATIVE_INFINITY,false,0));
  ticket.put("personBirthdayFrom", new Rule("person.birthday>=","date",Double.NEGATIVE_INFINITY,false,0));
  ticket.put("personBirthdayTo", new Rule("person.birthday<=","date",Double.NEGATIVE_INFINITY,false,0));
  ticket.put("personWeight", new Rule("person.weight","int32",1,false,0));
  ticket.put("personWeightFrom", new Rule("person.weight>=","int32",1,false,0));
  ticket.put("personWeightTo", new Rule("person.weight<=","int32",1,false,0));
  ticket.put("personLocationX", new Rule("person.location.x","int32",Double.NEGATIVE_INFINITY,false,0));
  ticket.put("personLocationY", new Rule("person.location.y","int32",Double.NEGATIVE_INFINITY,false,0));
  ticket.put("personLocationNameContains", new Rule("person.location.name~","string",Double.NEGATIVE_INFINITY,false,0));
  ticket.put("eventId", new Rule("eventId","int64",1,false,0));
  person.put("id", new Rule("id","int64",1,false,0));
  person.put("birthday", new Rule("birthday","date",Double.NEGATIVE_INFINITY,false,0));
  person.put("birthdayFrom", new Rule("birthday>=","date",Double.NEGATIVE_INFINITY,false,0));
  person.put("birthdayTo", new Rule("birthday<=","date",Double.NEGATIVE_INFINITY,false,0));
  person.put("weight", new Rule("weight","int32",1,false,0));
  person.put("weightFrom", new Rule("weight>=","int32",1,false,0));
  person.put("weightTo", new Rule("weight<=","int32",1,false,0));
  person.put("locationX", new Rule("location.x","int32",Double.NEGATIVE_INFINITY,false,0));
  person.put("locationY", new Rule("location.y","int32",Double.NEGATIVE_INFINITY,false,0));
  person.put("locationNameContains", new Rule("location.name~","string",Double.NEGATIVE_INFINITY,false,0));
 }
 private static RequestValidationException invalid(String field,String detail){return new RequestValidationException(field,detail);}
 public static Map<String,Object> validate(SearchDto request, boolean tickets) {
  if(request==null)throw invalid("$","Search body is required");
  if(request.getPage()==null||request.getPage()<0)throw invalid("page","Expected an integer >= 0");
  if(request.getSize()==null||request.getSize()<1||request.getSize()>100)throw invalid("size","Expected an integer 1..100");
  Map<String,Rule> rules=tickets?ticket:person;Map<String,Object> output=new LinkedHashMap<>();
  if(request.getFilter()!=null)for(var e:request.getFilter().entrySet()) {
   Rule rule=rules.get(e.getKey());if(rule==null)throw invalid("filter."+e.getKey(),"Unknown filter");
   output.put(rule.path(),value(e.getKey(),e.getValue(),rule));
  }
  if(request.getFilter()!=null)for(String key:request.getFilter().keySet())if(key.endsWith("From")) {
   String to=key.substring(0,key.length()-4)+"To";
   if(request.getFilter().containsKey(to)) {
    Object lo=output.get(rules.get(key).path()), hi=output.get(rules.get(to).path());
    @SuppressWarnings("unchecked") Comparable<Object> comparable=(Comparable<Object>)lo;
    if(comparable.compareTo(hi)>0)throw invalid("filter."+key,"Range start must not exceed its end");
   }
  }
  if(request.getSort()!=null)for(SortDto sort:request.getSort()) {
   if(sort==null||sort.field()==null||!(tickets?ticketSort:personSort).contains(sort.field()))throw invalid("sort","Unknown sort field");
   if(!"ASC".equals(sort.direction())&&!"DESC".equals(sort.direction()))throw invalid("sort","Direction must be ASC or DESC");
  }
  return output;
 }
 private static Object value(String key,Object raw,Rule r) {
  try {
   if(raw==null){if(r.kind().equals("enum"))return null;throw new IllegalArgumentException();}
   Object parsed;
   switch(r.kind()) {
    case "int32", "int64" -> {
     if(!(raw instanceof Number))throw new IllegalArgumentException();
     BigDecimal n=new BigDecimal(raw.toString());
     parsed=r.kind().equals("int32")?(Object)n.intValueExact():n.longValueExact();
    }
    case "float", "double", "number" -> {
     if(!(raw instanceof Number))throw new IllegalArgumentException();
     BigDecimal n=new BigDecimal(raw.toString());double d=n.doubleValue();
     if(!Double.isFinite(d)||(d==0&&n.signum()!=0))throw new IllegalArgumentException();
     if(r.kind().equals("float")){float f=n.floatValue();if(!Float.isFinite(f)||(f==0&&n.signum()!=0))throw new IllegalArgumentException();parsed=f;}else parsed=d;
    }
    case "date" -> {if(!(raw instanceof String s)||!s.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}"))throw new IllegalArgumentException();LocalDate d=LocalDate.parse(s);ApiValidation.birthday(d);parsed=d;}
    case "datetime" -> {if(!(raw instanceof String s)||!s.matches("[0-9]{4}-.*"))throw new IllegalArgumentException();OffsetDateTime d=OffsetDateTime.parse(s);if(d.getYear()<1)throw new IllegalArgumentException();parsed=Date.from(d.toInstant());}
    case "enum" -> {if(!(raw instanceof String s))throw new IllegalArgumentException();parsed=TicketType.valueOf(s);}
    default -> {if(!(raw instanceof String s)||s.codePointCount(0,s.length())<r.minLength())throw new IllegalArgumentException();parsed=s;}
   }
   if(parsed instanceof Number n && (r.exclusive()?n.doubleValue()<=r.minimum():n.doubleValue()<r.minimum()))throw new IllegalArgumentException();
   return parsed;
  }catch(RuntimeException e){throw invalid("filter."+key,"Value does not match the API type, format or allowed range");}
 }
}
