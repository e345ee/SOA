package se.ifmo.ru.utils;
import se.ifmo.ru.error.exceptions.RequestValidationException;
import java.time.LocalDate;
public final class ApiValidation {
 private ApiValidation() {}
 public static long id(String raw) {
  try { if(raw==null || !raw.matches("[0-9]+"))throw new IllegalArgumentException(); long id=Long.parseLong(raw);if(id<1)throw new IllegalArgumentException();return id; }
  catch(IllegalArgumentException e){throw new RequestValidationException("id","Expected a positive int64 identifier");}
 }
 public static long page(String raw){return integerParameter(raw,0,Long.MAX_VALUE,"page");}
 public static int size(String raw){return (int)integerParameter(raw,1,100,"size");}
 private static long integerParameter(String raw,long min,long max,String field){
  try{if(raw==null||!raw.matches("[0-9]+"))throw new IllegalArgumentException();long value=Long.parseLong(raw);if(value<min||value>max)throw new IllegalArgumentException();return value;}
  catch(IllegalArgumentException e){throw new RequestValidationException(field,"Invalid pagination integer or allowed range");}
 }
 public static void birthday(LocalDate date) {if(date==null||date.getYear()<1||date.getYear()>9999)throw new RequestValidationException("birthday","Expected a calendar date with year 1..9999");}
 public static void numbers(Double x, Float price) {if(x==null||!Double.isFinite(x)||price==null||!Float.isFinite(price))throw new RequestValidationException("ticket","Coordinates and price must be finite numbers");}
}
