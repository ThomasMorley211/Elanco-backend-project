package com.example.ticktracker.ingest;

import com.example.ticktracker.client.SightingDto;
import com.example.ticktracker.domain.TickSighting;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

@Component 
public class SightingValidator{
    private static final int MAX_ID = 64;
    private static final int MAX_LOCATION = 100;
    private static final int MAX_SPECIES = 100;
    private static final int MAX_LATIN_NAME = 150;

    public ValidationResult validate(SightingDto dto){
        if (dto == null ){
            return ValidationResult.rejected("Record is empty");
        }
        String id = clean(dto.id());
        if (id == null){
            return ValidationResult.rejected("missing id");
        }
        if (id.length() > MAX_ID){
            return ValidationResult.rejected("id is too long");
        }
        String dateText = clean(dto.date());
        if (dateText == null){
            return ValidationResult.rejected("Missing date");
        }
        LocalDateTime date = parseDate(dateText);
        if (date == null) {
            return ValidationResult.rejected("unreadable date:  "+dateText);

        }
        if (date.isAfter(LocalDateTime.now())){
            return ValidationResult.rejected("date is in the future:  "+dateText);
        }
        String location = clean(dto.location());
        if (location == null){
            if (location == null){
                return ValidationResult.rejected("Missing location");
            }
        if (location.length() > MAX_LOCATION){
            return ValidationResult.rejected("Location is too long");
        }
        location = titleCase(location);
        }
        String species = clean(dto.species());
        if (species == null){
            return ValidationResult.rejected("missing species");
        }
        if (species.length() > MAX_SPECIES){
            return ValidationResult.rejected("species is too long");
        }
        String latinName = clean(dto.latinName());
        if (latinName == null){
            return ValidationResult.rejected("Missing latin name");
        }
        if (latinName.length()>MAX_LATIN_NAME){
            return ValidationResult.rejected("Latin Name is too long");
        }
        return ValidationResult.accepted(new TickSighting(id,date,location,species,latinName));
    }
    private String clean(String text){
        if (text == null){
            return null;
        }
        String cleaned = text.trim().replaceAll("\\s+"," ");
        return cleaned.isEmpty() ? null : cleaned;

    }
    private LocalDateTime parseDate(String text){
        try {
            return LocalDateTime.parse(text);
        } catch (DateTimeParseException ignored){
        }
        try {
            return LocalDate.parse(text).atStartOfDay();
        }catch (DateTimeParseException ignored){
            // program gives up
        }
        return null;
    }
    private String titleCase(String text){
        StringBuilder result = new StringBuilder();
        boolean upperNext = true;
        for (char c : text.toCharArray()){
            if (Character.isLetter(c)){
                result.append(upperNext ? Character.toUpperCase(c) : Character.toLowerCase(c));
                upperNext = false;
            }else {
                result.append(c);
                upperNext = (c == ' ' || c == '-');
            }
        }
        return result.toString();
    }
}
