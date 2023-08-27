package org.example;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public class DateTime {

    public static String currentdatetime(){
        Timestamp tp= Timestamp.from(Instant.now());
        LocalDateTime dt= tp.toLocalDateTime();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss ");
        String formatteddate = dt.format(myFormatObj);
//        System.out.println(formatteddate);
        return formatteddate;
    }

    public static String expdatetime(){
        Timestamp rt= Timestamp.from(Instant.parse(Instant.now().plusSeconds(TimeUnit.MINUTES.toSeconds( 15 ) )
                .toString())) ;
        LocalDateTime dtr= rt.toLocalDateTime();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss ");
        String formatteddate = dtr.format(myFormatObj);
        System.out.println(formatteddate);
        return formatteddate;
    }

}
