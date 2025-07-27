package com.zeluciojr;

import java.util.stream.Stream;

public class SumCalculator {

    private SumCalculator(){}

    public static Integer sum(Integer... numbers){
        return Stream.of(numbers).reduce(0, Integer::sum);
    }

}
