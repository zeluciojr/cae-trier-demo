package com.zeluciojr;

import com.cae.trier.retry.RetryNotification;
import com.cae.trier.retry.RetrySubscriber;

public class CustomRetrySubscriber implements RetrySubscriber {

    @Override
    public void receiveRetryNotification(RetryNotification retryNotification) {
        System.out.println("New retry notification. " + retryNotification);
    }

}
