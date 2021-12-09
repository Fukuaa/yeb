package com.xxxx.server.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class DateConverter implements Converter<String,LocalDate> {

    @Override
    public LocalDate convert(String s) {
        try{
            return LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
