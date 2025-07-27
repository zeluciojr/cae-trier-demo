package com.zeluciojr;

import com.cae.mapped_exceptions.MappedException;

public class OddBomb {

    private Integer invocationCounter = 0;

    public void run(){
        this.invocationCounter ++;
        if ((this.invocationCounter % 2) != 0)
            throw new OddNumberMappedException(this.invocationCounter);

    }

    public static class OddNumberMappedException extends MappedException {
        public OddNumberMappedException(Integer invocationCounter) {
            super("Odd number of invocations reached: " + invocationCounter);
        }
    }
}
