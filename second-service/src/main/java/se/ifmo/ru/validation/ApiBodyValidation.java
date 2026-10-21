package se.ifmo.ru.validation;
import jakarta.annotation.Priority;
import jakarta.json.*;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.ext.Provider;
import se.ifmo.ru.error.exceptions.RequestValidationException;
import java.io.*;
import java.math.*;
import java.time.*;
import java.util.*;
@Provider
@Priority(Priorities.ENTITY_CODER-100)
public final class ApiBodyValidation implements ContainerRequestFilter {
 private static final JsonObject schemas;
 static { try(InputStream in=ApiBodyValidation.class.getResourceAsStream("/api-schemas.json");JsonReader reader=Json.createReader(in)){schemas=reader.readObject();}catch(IOException e){throw new ExceptionInInitializerError(e);} }
 @Override public void filter(ContainerRequestContext context) throws IOException {
  String path=context.getUriInfo().getPath().replaceAll("^/+|/+$","");String method=context.getMethod(), schema=null;
  if(method.equals("POST"))schema=switch(path){case "tickets"->"TicketCreate";case "persons"->"PersonCreate";case "tickets/search"->"TicketSearchRequest";case "persons/search"->"PersonSearchRequest";case "tickets/count-by-person"->"Person";default->null;};
  if(method.equals("PUT")) {if(path.matches("tickets/[^/]+"))schema="Ticket";if(path.matches("persons/[^/]+"))schema="Person";}
  if(schema==null)return;
  byte[] bytes=context.getEntityStream().readAllBytes();context.setEntityStream(new ByteArrayInputStream(bytes));
  JsonValue body;
  try(JsonReader reader=Json.createReader(new ByteArrayInputStream(bytes))){body=reader.readValue();}catch(RuntimeException e){throw invalid("$","Expected a valid JSON request body");}
  validate(body,schemas.getJsonObject(schema),"$");
 }
 private static RequestValidationException invalid(String field,String detail){return new RequestValidationException(field,detail);}
 private static JsonObject resolve(JsonObject s){return s.containsKey("$ref")?schemas.getJsonObject(s.getString("$ref").replaceAll("^.*/","")):s;}
 private static void validate(JsonValue v,JsonObject original,String path) {
  JsonObject s=resolve(original);
  if(v==JsonValue.NULL){if(s.getBoolean("nullable",false))return;throw invalid(path,"Null is not allowed; omit optional fields instead");}
  String type=s.getString("type","");
  if(type.equals("object")){
   if(!(v instanceof JsonObject o))throw invalid(path,"Expected an object");
   JsonObject props=s.getJsonObject("properties");
   for(JsonValue name:s.getJsonArray("required")==null?Json.createArrayBuilder().build():s.getJsonArray("required")){
    String key=((JsonString)name).getString();JsonObject prop=props==null?null:props.getJsonObject(key);
    if(prop!=null&&resolve(prop).getBoolean("readOnly",false))continue;
    if(!o.containsKey(key))throw invalid(path+"."+key,"Required field is missing");
   }
   if(props!=null)for(String key:o.keySet())if(props.containsKey(key)&&!resolve(props.getJsonObject(key)).getBoolean("readOnly",false))validate(o.get(key),props.getJsonObject(key),path+"."+key);
   JsonArray alternatives=s.getJsonArray("oneOf");
   if(alternatives!=null){int matches=0;for(JsonValue alt:alternatives){boolean ok=true;JsonArray required=((JsonObject)alt).getJsonArray("required");if(required!=null)for(JsonValue key:required)ok &= o.containsKey(((JsonString)key).getString());if(ok)matches++;}if(matches!=1)throw invalid(path,"Specify exactly one of personId or person");}
  } else if(type.equals("array")) {
   if(!(v instanceof JsonArray a))throw invalid(path,"Expected an array");for(int i=0;i<a.size();i++)validate(a.get(i),s.getJsonObject("items"),path+"["+i+"]");
  } else if(type.equals("string")) {
   if(!(v instanceof JsonString text))throw invalid(path,"Expected a string");String value=text.getString();
   if(value.codePointCount(0,value.length())<s.getInt("minLength",0))throw invalid(path,"String is too short");
   String format=s.getString("format","");
   try {if(format.equals("date")){if(!value.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")||LocalDate.parse(value).getYear()<1)throw new IllegalArgumentException();}
    if(format.equals("date-time")){if(!value.matches("[0-9]{4}-.*")||OffsetDateTime.parse(value).getYear()<1)throw new IllegalArgumentException();}}
   catch(RuntimeException e){throw invalid(path,"Invalid calendar date or RFC 3339 timestamp");}
  } else if(type.equals("integer")||type.equals("number")) {
   if(!(v instanceof JsonNumber number))throw invalid(path,"Expected a JSON number");BigDecimal n=number.bigDecimalValue();String format=s.getString("format","");
   try {if(type.equals("integer")){BigInteger i=n.toBigIntegerExact();if(format.equals("int32"))i.intValueExact();if(format.equals("int64"))i.longValueExact();}
    if(type.equals("number")){double d=n.doubleValue();if(!Double.isFinite(d)||(d==0&&n.signum()!=0))throw new ArithmeticException();if(format.equals("float")){float f=n.floatValue();if(!Float.isFinite(f)||(f==0&&n.signum()!=0))throw new ArithmeticException();}}}
   catch(ArithmeticException e){throw invalid(path,"Number is outside the API type range");}
   if(s.containsKey("minimum")){int c=n.compareTo(s.getJsonNumber("minimum").bigDecimalValue());if(c<0||(c==0&&s.getBoolean("exclusiveMinimum",false)))throw invalid(path,"Number is below the allowed minimum");}
   if(s.containsKey("maximum")){int c=n.compareTo(s.getJsonNumber("maximum").bigDecimalValue());if(c>0||(c==0&&s.getBoolean("exclusiveMaximum",false)))throw invalid(path,"Number is above the allowed maximum");}
  } else if(type.equals("boolean")&&v!=JsonValue.TRUE&&v!=JsonValue.FALSE)throw invalid(path,"Expected a boolean");
  JsonArray allowed=s.getJsonArray("enum");if(allowed!=null&&!allowed.contains(v))throw invalid(path,"Value is not in the allowed enumeration");
 }
}