package se.ifmo.ru.utils;
import se.ifmo.ru.error.exceptions.RequestValidationException;
import java.time.LocalDate;
public final class ApiValidation {
 private ApiValidation() {}
 public static long id(String raw) {
  try { if(raw==null || !raw.matches("[0-9]+"))throw new IllegalArgumentException(); long id=Long.parseLong(raw);if(id<1)throw new IllegalArgumentException();return id; }
  catch(IllegalArgumentException e){throw new RequestValidationException("id","Expected a positive int64 identifier");}
 }
 public static void birthday(LocalDate date) {if(date==null||date.getYear()<1||date.getYear()>9999)throw new RequestValidationException("birthday","Expected a calendar date with year 1..9999");}
 public static void numbers(Double x, Float price) {if(x==null||!Double.isFinite(x)||price==null||!Float.isFinite(price))throw new RequestValidationException("ticket","Coordinates and price must be finite numbers");}
}
