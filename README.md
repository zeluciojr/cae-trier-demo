# cae-trier-demo
This repository holds a ``Java`` project with the purpose of showing how to use the ``Trier`` component.

Examples in this project:

<br>

### Providing A Retry Subscriber
Providing to the ``Trier`` ecosystem a custom instance of ``RetrySubscriber`` so everytime a retry happens it will be triggered.

```java
private static void subscribeRetrySubscribers() {
    RetryNotifier.SINGLETON.subscribe(new CustomRetrySubscriber());
}
```

<br>

### A Healthy Action Execution
A normal action that doesn't throw being wrapped and executed by the ``Trier`` object.

```java
public static void runExampleOfAHealthyExecution(){
    var result = Trier.of(() -> SumCalculator.sum(22, 44, 88))
        .onUnexpectedExceptions(Demo::handleUnexpectedException)
        .execute();
    System.out.println("Yay: " + result);
}
```

<br>

### An Unhealthy Action Execution
The same normal action being executed but this time with a bad input which makes it throw ``NullPointerException``, triggering the ``Trier`` to call the ``UnexpectedExceptionHandler`` provided.

```java
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
```

<br>

### An Intermittent Action Execution
An action the intermittently throws an exception to showcase how the retry mechanism works with the ``Trier`` component.

```java
private static void runExampleOfIntermittentActionWithRetry() {
    var intermittentActionRunner = new OddBomb();
    Trier.of(intermittentActionRunner::run)
        .retryOn(OddNumberMappedException.class, 1, 3, TimeUnit.SECONDS)
        .onUnexpectedExceptions(Demo::handleUnexpectedException)
        .execute();
}
```

<br>

### An Unhealthy Action Execution With Retries
An action that will always throw to showcase how the retry mechanism works when it reaches an exhaustion state.

```java
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
```

