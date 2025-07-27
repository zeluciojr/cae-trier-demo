package com.zeluciojr;

import com.cae.mapped_exceptions.MappedException;
import com.cae.trier.Trier;
import com.cae.trier.retry.NoRetriesLeftException;
import com.cae.trier.retry.OnExhaustion;
import com.cae.trier.retry.RetryNotifier;

import java.util.concurrent.TimeUnit;

import static com.zeluciojr.OddBomb.OddNumberMappedException;

public class Demo {

    public static void main(String[] args) {
        Demo.subscribeRetrySubscribers();
        Demo.runExampleOfAHealthyExecution();
        Demo.runExampleOfAnUnhealthyExecution();
        Demo.runExampleOfIntermittentActionWithRetry();
        Demo.runExampleOfAnUnhealthyExecutionWithRetry();
    }

    /**
     * It will provide the instance of CustomRetrySubscriber as an interested in receiving
     * notifications everytime a new one happens. Internally it will simply print the notification content
     * to the console.
     */
    private static void subscribeRetrySubscribers() {
        RetryNotifier.SINGLETON.subscribe(new CustomRetrySubscriber());
    }

    /**
     * It will wrap the SumCalculator::sum action and safely run it.
     * It will be prepared to handle unexpected exceptions, but since it will
     * not throw, the handler will never be called.
     * <p>
     * Whatever the action returns will be returned by the Trier instance.
     * </p>
     */
    public static void runExampleOfAHealthyExecution(){
        var result = Trier.of(() -> SumCalculator.sum(22, 44, 88))
            .onUnexpectedExceptions(Demo::handleUnexpectedException)
            .execute();
        System.out.println("Yay: " + result);
    }


    /**
     * It will wrap the SumCalculator::sum action and safely run it.
     * It will be prepared to handle unexpected exceptions, and since it will
     * throw NullPointerException, the handler will be called and map the raw exception
     * into a MappedException instance.
     * <p>
     * Since the action will throw, no output will actually be returned but instead the
     * provided MappedException will be thrown.
     * </p>
     */
    public static void runExampleOfAnUnhealthyExecution(){
        try{
            var result = Trier.of(() -> SumCalculator.sum(22, null, 88)) //bad input
                .onUnexpectedExceptions(Demo::handleUnexpectedException)
                .execute();
            System.out.println("Yay: " + result);
        } catch (DemoMappedException exception){
            System.out.println("Ouch! Threw: " + exception);
        }
    }

    /**
     * It will wrap the OddBomb::run action and safely run it.
     * It will be prepared to handle unexpected exceptions, but since it will
     * not throw, the handler will never be called.
     * It will also be prepared to retry up to 1 time after 3 seconds if a OddNumberMappedException is thrown
     * during the action execution.
     * <p>
     * The OddBomb instance will throw a MappedException if
     * it reaches an odd amount of invocations.
     * </p>
     * <p>
     * Since the first invocation will count as an odd number, it will throw, which will trigger a retry after 3 seconds.
     * The second execution will count as 2, an even number, so it won't throw, thus finishing the execution smoothly.
     * </p>
     */
    private static void runExampleOfIntermittentActionWithRetry() {
        var intermittentActionRunner = new OddBomb();
        Trier.of(intermittentActionRunner::run)
            .retryOn(OddNumberMappedException.class, 1, 3, TimeUnit.SECONDS)
            .onUnexpectedExceptions(Demo::handleUnexpectedException)
            .execute();
    }

    /**
     * It will wrap the SimpleBomb::run action and safely run it.
     * The action will deterministically throw a RuntimeException, which is set to be retried up to 5 times.
     * <p>
     * The first retry will happen after 500ms.
     * The second will happen after 1s.
     * The third will happen after 2s.
     * The fourth, after 4s.
     * The fifth, after 8s.
     * </p>
     * <p>
     * Since the SimpleBomb instance won't stop throwing, the Trier instance will exhaust its retries for that kind
     * of exception. This behavior will trigger the execution of the OnExhaustion handler and as it doesn't throw, the default
     * NoRetriesLeftException will be thrown by the Trier itself.
     * </p>
     */
    private static void runExampleOfAnUnhealthyExecutionWithRetry() {
        try{
            var bombActionRunner = new SimpleBomb();
            Trier.of(bombActionRunner::run)
                .retryOn(RuntimeException.class, 5, 500, TimeUnit.MILLISECONDS)
                .onExhaustion(Demo::handleExhaustion)
                .onUnexpectedExceptions(Demo::handleUnexpectedException)
                .execute();
        } catch (NoRetriesLeftException noRetriesLeftException){
            System.out.println("Ouch... " + noRetriesLeftException);
        }
    }


    private static MappedException handleUnexpectedException(Exception exception) {
        return new DemoMappedException(exception);
    }

    public static class DemoMappedException extends MappedException {

        public DemoMappedException(Exception exception) {
            super(
                    "Something threw a raw exception",
                    "More details: " + exception.toString(),
                    exception
            );
        }

    }

    private static void handleExhaustion(OnExhaustion.FailureStatus failureStatus) {
        System.out.println("Exhausted retries");
    }

}