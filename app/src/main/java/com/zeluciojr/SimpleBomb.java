package com.zeluciojr;

import java.time.LocalDateTime;

public class SimpleBomb {

    public void run(){
        var now = LocalDateTime.now();
        System.out.println("Time of SimpleBomb execution: " + now.getHour() + ":" + now.getMinute() + ":" + now.getSecond());
        throw new RuntimeException("I'm a bomb!");
    }

}
