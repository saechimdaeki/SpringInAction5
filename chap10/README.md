# 🥇 10장 리액터 개요

## 💕 이 장에서 배우는 내용
- 리액티브 프로그래밍 이해하기
- 프로젝트 리액터
- 리액티브 데이터 오퍼레이션

애플리케이션 코드를 개발할 때는 명령형(imperative)과 리액티브(reactive , 반응형)의 두가지형태로 코드를 작성할수있다.

#### 🐸 [1] `명령형` 코드는 순차적으로 연속되는 작업이며, 각 작업은 한 번에 하나씩 그리고 이전 작업 다음에 실행된다.

#### 데이터는 모아서 처리되고 이전 작업이 데이터 처리를 끝낸 후에 다음작업으로 넘어갈 수 있다.

#### 🐸  [2] `리액티브` 코드는 데이터 처리를 위해 일련의 작업들이 정의 되지만, 이 작업들은 병렬로 실행될 수 있다.

#### 그리고 각 작업은 부분 집합의 데이터를 처리할 수 있으며, 처리가 끝난 데이터를 다음 작업에 넘겨주고

#### 다른 부분 집합의 데이터로 계속 작업할 수 있다.

---

리액터는 스프링 프로젝트의 일부분인 리액티브 프로그래밍 라이브러리다. 그리고 리액터는 스프링5 에서

리액티브 프로그래밍을 지원하는 데 필요한 기반이므로 먼저 리액터를 파악한 다음에 스프링으로 리액티브

컨트롤러와 레포지토리를 빌드하는것이 중요하다 따라서 리액티브 프로그래밍의 핵심을 간단히 알아보자

## 🍰 리액티브 프로그래밍 이해하기

리액티브 프로그래밍은 명령형 프로그래밍의 대안이 되는 패러다임이다. 명령형 프로그래밍의 한계를 해결할 수 있기 때문이다.

이런 한계를 이해하면 리액티브 모델의 장점을 더 확실하게 이해할 수 있다.

명령형 프로그래밍의 발상은 간단하다. 즉 , 한번에 하나씩 만나는 순서대로 실행되는 명령어들로 코드를 작성하면 된다.

그리고 프로그램에서는 하나의 작업이 완전히 끝나기를 기다렸다가 그다음 작업을 수행한다.

각 단계마다 처리되는 데이터는 전체를 처리할 수 있도록 사용할 수 있어야한다.

그러나 작업이 수행되는 동안 특히 이 작업이 원격지 서버로부터 데이터베이스에 데이터를 쓰거나 가져오는 것이라면

이작업이 완료될 때까지 아무것도 할수없다. 따라서 이작업을 수행하는 스레드는 차단된다. 이렇게 차단되는 스레드는 낭비다.

자바를 비롯해 대부분의 프로그래밍 언어는 동시 프로그래밍을 지원한다. 자바에서는 스레드가 어떤 작업을 계속 수행하는 동안

이 스레드에서 다른스레드를 시작시키고 작업을 수행하게 하는것은 매우쉽다. 하지만 스레드를 생성하는 것은

쉬울지 몰라도 생성된 스레드는 어떤이유로든 차단되며 다중스레드로 동시성을 관리하는 것은 쉽지않다.

이에 반해 `리액티브 프로그래밍`은 본질적으로 함수적이면서 선언적이다. 즉, 순차적으로 수행되는 작업단계를 나타낸것이 아니라

데이터가 흘러가는 파이프라인이나 스트림을 포함한다. 그리고 이런 리액티브 스트림은 데이터 전체를 사용할 수 있을때까지 기다리지 않고 

사용가능한 데이터가 있을 때마다 처리되므로 사실상 입력되는 데이터는 무한할 수 잇다.

## 🏓 리액티브 스트림 정의하기

`리액티브 스트림`은 넷플릭스,라이트벤드, 피보탈의 엔지니어들에 의해 2013년 말 시작되었다.

`리액티브 스트림`은 차단되지 않는 백 프레셔를 갖는 비동기 스트림 처리의 표준을 제공하는 것이 목적이다.

즉, 동시에 여러 작업을 수행하여 더 큰 확장성을 얻게 해주며, 백 프레셔는 데이터를 소비하는(읽는) 컨슈머가

처리할 수 있는 만큼으로 전달 데이터를 제한함으로써 지나치게 빠른 데이터 소스로부터의 데이터 전달 폭주를 피할수있는수단이다.

```
자바스트림 vs 리액티브 스트림

자바 스트림과 리액티브 스트림은 많은 유사성이있다. 우선 둘 다 Streams라는 단어가 이름에 포함된다. 
또한, 데이터로 작업하기 위한 API를 제공한다. 실제로 리액터를 살펴볼때 알겠지만 다수의 똑같은
오퍼레이션을 공유한다.

그러나 자바 스트림은 대개 동기화되어 있고 한정된 데이터로 작업을 수행한다.
리액티브 스트림은 무한 데이터셋을 비롯해서 어떤 크기의 데이터셋이건 비동기 처리를 지원한다. 그리고
실시간으로 데이터를 처리하며, 백 프레셔를 사용해서 데이터 전달 폭주를 막는다.
```

### 리액티브 스트림은 4개의 인터페이스인 Publisher,Subscriber,Subscription,Processor로 요약할 수 있다.

`Publisher`는 하나의 `Subscription`당 하나의 `Subscriber`에 발행하는 데이터를 생성한다.

`Publisher`인터페이스에는 `Subscriber`가 `Publisher`를 구독신청할 수 있는 `subscribe()`메소드 한개가 선언되어있다.

```
public interface Publisher<T>{
    void subscribe(Subscribe<? super T> subscriber);
}
```
그리고 Subscriber가 구독신청되면 Publisher로부터 이벤트를 수신할 수 있다. 이 이벤트들은 Subscriber인터페이스의 메소드를 통해 전송된다
```
public interface Subscriber<T>{
    void onSubscribe(Subcription sub);
    void onNext(T item);
    void onError(Throwable ex);
    void onComplete();
}
```

Subscriber가 수신할 첫 번째 이벤트는 onSubscribe()의 호출을 통해 이루어진다. Publisher가 

onSubscribe()를 호출할 때 이 메소드의 인저로 Subscription객체를 Subscriber에 전달한다.

Subscriber는 Subscription 객체를 통해서 구독을 관리할 수 있다.
```
public interface Subscription{
    void request(long n);
    void cancel();
}
```

Subscriber는 request()를 호출하여 전송되는 데이터를 요청하거나, 또는 더 이상 데이터를 수신하지 않고

구독을 취소한다는 것을 나타내기 위해 cancel()을 호출할 수 있다. request()를 호출할 때 Subscriber는 

받고자 하는 데이터 항목 수를 나타내는 long타입의 값을 인자로 전달한다. 바로 이것이 `백 프레셔`이며 

Subscriber가 처리할 수 있는 것보다 더 많은 데이터를 Publisher가 전송하는 것을 막아준다. 요청된 수의 데이터를

Publisher가 전송한 후에 Subscriber는 다시 request()를 호출하여 더 많은 요청을 할 수 있다.

Subscriber의 데이터 요청이 완료되면 데이터가 스트림을 통해 전달되기 시작한다. 이때 onNext() 메소드가

호출되어 Publsher가 전송하는 데이터가 Subscriber에게 전달되며, 만일 에러가 생길 때는 onError()가 호출된다.

그리고 Publisher에서 전송할 데이터가 없고 더 이상의 데이터를 생성하지 않는다면 Publisher가 onComplete()

를 호출하여 작업이 끝났다고 Subscriber에게 알려준다. Processor인터페이스는 다음과 같이 Subscriber

인터페이스와 Publisher인터페이스를 결합한것이다.
```
public interface Processor<T, R>
            extends Subscriber<T>, Publisher<R>{}
```
Subscriber역할로 Processor는 데이터를 수신하고 처리한다. 그다음에 역할을 바꾸어 Publisher 역할로

처리 결과를 자신의 Subscriber들에게 발행한다. 보면 알 수 있듯이, 리액티브 스트림은 직관적이라 데이터 처리 

파이프라인을 개발하는 방법을 쉽게 알 수 있다. 즉 Publisher로부터 0 또는 그 이상의 Processor를

통해 데이터를 끌어온 다음 최종 결과를 Subscriber에 전달한다. 그러나 리액티브 스트림 인터페이스는 스트림을

구성하는 기능이 없다. 이에 따라 프로젝트 리액터에서는 리액티브 스트림을 구성하는 API를 제공하여

리액티브 스트림 인터페이스를 구현하였다. 다른장에서 소개하겠지만 리액터는 스프링 5의 리액티브

프로그래밍 모델의 기반이다.

---

## 🍰 리액터 시작하기
리액티브 프로그래밍은 명령형 프로그래밍과 매우 다른 방식으로 접근해야 한다. 즉, 일련의 작업 단계를 기술하는 것이

아니라 데이터가 전달될 파이프라인을 구성하는 것이다. 그리고 이 파이프라인을 통해 데이터가 전달되는

동안 어떤 형태로든 변경 또는 사용 될 수 있다. 예를들어, 사람의 이름을 가져와서 모두 대문자로 변경 후 이것으로

인사말 메시지를 만들어 출력한다면 명령형 프로그래밍 모델에서는 다음과 같이할것이다.
```
String name= "Craig";
String capitalName= name.toUpperCase();
String greeting="Hello, "+ capitalName+"!";
System.out.println(greeting);
```

이 경우는 각 줄의 코드가 같은 스레드에서 한 단계씩 차례대로 실행된다. 그리고 각 단계가 완료될 때까지

다음 단계로 이동하지 못하게 실행중인 스레드를막는다. 이와는 다르게 리액티브 코드에서는 다음과같이 할수있다.
```
Mono.just("Craig")
    .map(n -> n.toUpperCase())
    .map(cn -> "Hello, " + cn + "!)
    .subscribe(System.out::println);
```
이 예의 리액티브 코드가 단계별로 실행되는 것처럼 보이겠지만, 실제로는 데이터가 전달되는 파이프라인을 구성하는 것이다.

그리고 파이프라인의 각 단계에서는 어떻게 하든 데이터가 변경된다. 또한, 각 오퍼레이션은 같은 스레드로 실행되거나 다른메소드로 실행될 수 있다.

이 예의 Mono는 리액터의 두 가지 핵심 타입 중 하나이며, 다른 하나로는 Flux가 있다. 두 개 모두 리액티브

스트림의 Publisher 인터페이스를 구현한 것이다. Flux는 0,1 또는 다수의 데이터를 갖는 파이프라인을 나타낸다.

반면에 Mono는 하나의 데이터 항목만 갖는 데이터셋에 최적화 된 리액티브 타입이다.

앞의 예에서는 세 개의 Mono가 있으며 `just()`오퍼레이션은 첫 번째 것을 생성한다. 그리고 첫 번째

Mono가 값을 방출하면 이 값이 첫 번째 `map()` 오퍼레이션에 전달되어 대문자로 변경되고 다른 Mono를 생성하는데 사용된다.

이렇게 생성된 두 번째 Mono가 데이터를 방출하면 이 데이터가 두번째 map() 오퍼레이션에 전달되어 문자열 결합이 

수행되며, 이 결과는 세번째 Mono를 생성하는데 사용된다. 그리고 `subscribe()`호출에서 세번째 Mono를 구독하여 데이터를 수신하고 출력한다.

---

### 🌟 리액티브 플로우의 다이어그램

리액티브 플로우는 마블다이어그램으로 나타내곤한다. 마블 다이어그램의 제일 위에는 Flux나 Mono를 통해 전달되는 

데이터 타임라인을 나타내고, 중앙에는 오퍼레이션을, 제일밑에는 결과로 생성되는 Flux나 Mono의 타임라인을 나타낸다.

#### Flux의 기본적인 플로우를 보여주는 마블 다이어그램

![image](https://user-images.githubusercontent.com/40031858/108649501-d3469480-7500-11eb-9c99-d7daa5031598.png)

#### Mono의 기본적인 플로우를 보여주는 마블 다이어그램

![image](https://user-images.githubusercontent.com/40031858/108649560-f2452680-7500-11eb-92fe-4c08a5d7af0c.png)


리액터를 시작하려면 다음 의존성을 프로젝트 빌더에 추가해야한다
```
<dependency>
    <groupId>io.projectreactor</groupId>
    <artifactId>reactor-core</artifactId>
</dependency>
```

이제 Mono와 Flux를 사용해서 리액티브 파이프라인의 생성을 시작할 수 있다.

## 🏓 리액티브 오퍼레이션 적용하기
Flux와 Mono는 리액터가 제공하는 가장 핵심적인 구성요소(리액티브 타입)다. 그리고 Flux와 Mono가 제공하는

오퍼레이션들은 두 타입을 함께 결합하여 데이터가 전달될 수 있는 파이프라인을 생성한다. Flux와 Mono에는 500개 이상의

오퍼레이션이 있으며, 각 오퍼레이션은 다음과 같이 분류될 수 있다.
- 생성(creation)오퍼레이션
- 조합(combination)오퍼레이션
- 변환(tranformation) 오퍼레이션
- 로직(logic)오퍼레이션

이것들이 이책에서 정한 가장 유용한 몇가지 오퍼레이션이다.

---

## 👟 리액티브 타입 생성하기

스프링에서 리액티브 타입을 사용할 때는 레포지토리나 서비스로부터 Flux나 Mono가 제공되므로 리액티브 타입을 생성할 필요가없다.

그러나 발행하는 새로운 리액티브 발행자(publisher)를 생성할 때가 있다.

### 🌟 객체로부터 생성하기
Flux나 Mono로 생성하려는 하나 이상의 객체가 있다면 Flux나 Mono의 just()메소드를 사용해 리액티브 타입을 생성할 수 있다.

예를들어, 다음의 테스트 메소드는 다섯개의 String객체로부터 Flux를 생성한다
```
@Test
public void createAFlus_just(){
    Flux<String> fruitFlux=Flux
    .just("Apple","Orange","Grape","Banana","Strawberry");
}
```

이 경우 Flux는 생성되지만, 구독자가 없다. 구독자 없이는 데이터가 전달되지 않을것이다. 구독자를 추가할때는 Flux의 subscribe()를 호출한다.
```
fruitFlux.subscribe(
    f-> System.out.println("Here's some fruit: " +f)
);
```

여기서 subscribe()에 지정된 람다는 실제로 java.util.Consumer이며 이것은 리액티브

스트림의 Subscriber객체를 생성하기 위해 사용된다. subscribe()를 호출하는 즉시 데이터가 전달되기 시작.

이처럼 Flux나 Mono의 항목들을 출력해보면 리액티브 타입이 실제 작동하는 것을 파악하는데 좋지만

리액터의 `StepVerifier`를 사용하는 것이 Flux나 Mono를 테스트하는 더 좋은 방법이다.

Flux나 Mono가 지정되면 StepVerifier는 해당 리액티브 타입을 구독한 다음에 스트림을 통해 전달되는

데이터에 대해 `assertion`을 적용한다. 그리고 해당 스트림이 기대한 대로 완전하게 작동하는지 검사한다.


```
StepVerifier.create(fruitFlux)
    .expectNext("Apple")
    .expectNext("Orange")
    .expectNext("Grape")
    .expectNext("Banana")
    .expectNext("Strawberry")
    .verifyComplete();
```

### 🌟 컬렉션으로부터 생성하기
Flux는 또한 배열, Iterable 객체, 자바 Stream 객체로부터 생성될 수도 있다.

배열로부터 Flux를 생성하려면 static메소드인 `fromArray()`를 호출하며, 소스배열을 인자로 전달한다.
```
@Test
public void createAFlux_fromArray(){
    String[] fruits=new String[]{
        "Apple","Orange","Grape","Banana","Strawberry"};
    Flux<String> fruitFlux=Flux.fromArray(fruits);

    StepVerifier.create(fruitFlux)
        .expectNext("Apple")
        .expectNext("Orange")
        .expectNext("Grape")
        .expectNext("Banana")
        .expectNext("Strawberry")
        .verifyComplete();
}
```

또한 java.util.List, java.util.Set 또는 java.lang.Iterable의 다른 구현 컬렉션으로부터 

Flux를 생성해야한다면 해당 컬렉션을 인자로 전달하여 static메소드인 `fromIterable()`을 호출하면 된다.
```
@Test
public void createAFlux_fromIterable(){
    List<String> fruitList=new ArrayList<>();
    fruitList.add("Apple");
    fruitList.add("Orange");
    fruitList.add("Grape");
    fruitList.add("Banana");
    fruitList.add("Strawberry");

    Flux<String> fruitFlux=Flux.fromIterable(fruitList);

    //...
}
```
또는 Flux를 생성하는 소스로 자바 Stream객체를 사용해야 한다면 static메소드인 `fromStream()`을 호출하면된다.

```
@Test
public void createAFlux_fromStream(){
    Stream<String> fruitStream=
        Stream.of("Apple","Orange","Grape","Banana","Strawberry");

    Flux<String> fruitFlux=Flux.fromStream(fruitStream);

    //...
}
```

### 🌟 Flux데이터 생성하기

때로는 데이터 없이 매번 새값으로 증가하는 숫자를 방출하는 카운터역할의 Flux만 필요한 경우가 있다.

이와 같은 Flux를 생성할 때는 static 메소드인 `range()`를 사용할 수 있다.

```
@Test
public void createAFlux_range(){
    Flux<Integer> intervalFlux=
        Flux.range(1,5);

    StepVerifier.create(intervalFlux)
        .expectNext(1)
        .expectNext(2)
        .expectNext(3)
        .expectNext(4)
        .expectNext(5)
        .verifyComplete();
}
```

`range()`와 유사한 또 다른 Flux 생성 메소드로 `interval()`이 있다. range()처럼 interval()도 증가값을 방출하는 Flux()를 생성한다.

그러나 시작 값과 종료 값대신 값이 방출되는 시간 간격이나 주기를 지정한다. 매초 값을 방출하는 Flux를생성하는 코드를보자
```
@Test
public void createAFlux_interval(){
    Flux<Long> intervalFlux=
        Flux.interval(Duration.ofSeconds(1))
        .taoke(5);

    StepVerifier.create(intervalFlux)
        .expectNext(0L)
        .expectNext(1L)
        .expectNext(2L)
        .expectNext(3L)
        .expectNext(4L)
        .verifyComplete();
}
```
이런 Flux가 방출하는 값은 0부터 시작하여 값이 증가한다는것에 유의하자. 또한 interval()에는

최대값이 지정되지 않으므로 무한정 실행된다. 따라서 이 경우 `take()`를 사용해 첫번째 5개 항목으로 결과를 제한할수있다.

---

## 👟 리액티브 타입 조합하기
두 개의 리액티브 타입을 결합해야하거나 하나의 Flux를 두 개이상의 리액티브 타입으로 분할해야 하는경우가있다.

### 🌟 리액티브 타입 결합하기
두 개의 Flux 스트림이 있는데 이것을 하나의 결과 Flux로 생성해야한다고해보자. 이처럼 하나의 Flux를 다른것과 결합하려면 `mergeWith()`를 사용한다
```
@Test
public void mergeFluxes(){
    Flux<String> characterFlux= Flux
        .just("Garfield","Kojak","Barbossa")
        .delayElements(Duration.ofMillis(500));
    Flux<String> foodFlux=Flux
        .just("Lasagna", "Lollipops", "Apples")
        .delaySubscription(Duration.ofMillis(250))
        .delayElements(Duration.ofMillis(500));
    
    Flux<String> mergedFlux=characterFlux.mergeWith(foodFlux);

    StepVerifier.create(mergedFlux)
        .expectNext("Garfield")
        .expectNext("Lasagna")
        .expectNext("Kojak")
        .expectNext("Lollipops")
        .expectNext("Barbossa")
        .expectNext("Apples")
        .verifyComplete();
}
```

일반적으로 Flux는 가능한 빨리 데이터를 방출한다. 여기서 mergedFlux로부터 방출되는 항목의 순서는 두 개의 소스 Flux

로부터 방출되는 시간에 맞춰결정된다. 여기서는 두 Flux객체 모두 일정한 속도로 방춢되게 설정했으므로 두 Flux가 번갈아 끼워진다.

`megeWith()`는 소스 Flux들의 값이 완벽하게 번갈아 방출되게 보장할 수 없으므로 필요하다면 `zip()`

오퍼레이션을 대신 사용할 수 있다. 이 오퍼레이션은 각 Flux소스로부터 한 항목씩 번갈아 가져와 새로운 Flux를 생성한다.

```
@Test
public void zipFluxes(){
    Flux<String> characterFlux= Flux
        .just("Garfield","Kojak","Barbossa");
    Flux<String> foodFlux=Flux
        .just("Lasagna", "Lollipops", "Apples");
    Flux<Tuple2<String,String>> zippedFlux=
        Flux.zip(characterFlux,foodFlux);
    
    StepVerifier.create(zippedFlux)
        .expectNextMatches(p ->
            p.getT1().equals("Garfield") &&
            p.getT2().equals("Lasagna"))
        .expectNextMatches(p ->
            p.getT1().equals("Kojak") &&
            p.getT2().equals("Lollipops"))
        .expectNextMatches(p ->
            p.getT1().equals("Barbossa") &&
            p.getT2().equals("Apples"))
        .verifyComplete();
}
```

mergeWith()와 다르게 zip() 오퍼레이션은 정적인 생성 오퍼레이션이다 . 따라서 여기서 생성되는 Flux

는 캐릭터와 이캐릭터가 좋아하는 식품을 완벽하게 조합한다. zippedFlux로부터 방출되는 

각 항목은 Tuple2(두 개의 객체를 전달하는 컨테이너 객체)이며, 각 소스 Flux가 순서대로 방출하는 항목을 포함한다.

만일 Tuple2가 아닌 다른 타입을 사용하고 싶다면 우리가 원하는 객체를 생성하는 함수를 zip()에 제공하면 된다.

```
@Test
public void zipFluxesToObject(){
    Flux<String> characterFlux= Flux
        .just("Garfield","Kojak","Barbossa");
    Flux<String> foodFlux=Flux
        .just("Lasagna", "Lollipops", "Apples");

    Flux<String> zippedFlux=
        Flux.zip(characterFlux,foodFlux,(c,f) -> c+ " eats " +f);

    StepVerifier.create(zippedFlux)
        .expectNext("Garfield eats Lasagna")
        .expectNext("Kojak eats Lollipops")
        .expectNext("Barbossa eats Apples")
        .verifyComplete();
}
```

### 🌟 먼저 값을 방출하는 리액티브 타입 선택하기

두개의 Flux객체가 있는데, 이것을 결합하는 대신 먼저 값을 방출하는 소스 Flux의 값을 발행하는 새로운 Flux

를 생성하고 싶다고 해보자 `first()`오퍼레이션은 두 Flux 객체 중 먼저 값을 방출하는 Flux값을 선택해서 이값을 발행한다.

```
@Test
public void firstFlux(){
    Flux<String> slowFlux=Flux.just("tortoise", "snail", "Sloth")
        .delaySubscription(Duration.ofMillis(100));
    
    Flux<String> fastFlux=Flux.just("hare","cheetah","squirrel");

    Flux<String> firstFlux=Flux.first(slowFlux,fastFlux);

    StepVerifier.create(firstFlux)
        .expectNext("hare")
        .expectNext("cheetah")
        .expectNext("squirrel")
        .verifyComplete();
}
```

이 경우 느린 Flux(slowFlux)는 100밀리초가 경과한후 구독신청과 발행을 시작하므로 새로생성되는 Flux(firstFlux)는

느린 Flux를 무시하고 빠른 Flux(fastFlux)의 값만 발행하게된다.

## 👟 리액티브 스트림의 변환과 필터링
데이터가 스트림을 통해 흐르는 동안 일부 값을 필터링하거나 다른값으로 변경해야 할 경우가있다.

### 🌟 리액티브 타입으로부터 데이터 필터링하기
Flux로부터 데이터가 전달될 때 이것을 필터링하는 가장 기본적인 방법은 맨 앞부터 원하는 개수의 항목을 무시하는 것이다.

`skip()`오퍼레이션을 사용한다. 다수의 항목을 갖는 소스 Flux가 지정되었을 때 skip()오퍼레이션은 소스 Flux의

항목에서 지정된 수만큼 건너뛴 후 나머지 항목을 방출하는 새로운 Flux를 생성한다.

```
@Test
public void skipAFew(){
    Flux<String> skipFlux=Flux.just(
        "one","two","skip a few", "ninety nine", "one hundred")
        .skil(3);
    StepVerifier.create(skipFlux)
        .expectNext("ninety nine","one hundred")
        .verifyComplete();
}
```
여기서는 다섯 개의 항목을 갖는 Flux가 있다. 이 Flux에 대해 skip(3)을 호출하면 처음 세 개의 항목을

건너뛰고 마지막 두 항목만 발행하는 새로운 Flux(skipFlux)를 생성한다. 그러나 특정 수의 항목을

건너뛰는 대신, 일정 시간이 경과할 때까지 처음의 여러 항목을 건너뛰어야 하는 경우가 있다.

이런 형태의 skip() 오퍼레이션은 지정된 시간이 경과할 때까지 기다렸다가 소스 Flux의 항목을 방출하는 Flux를 생성한다.

다음 테스트는 skip()을 사용해 4초 동안 기다렸다가 값을 방출하는 결과 Flux를 생성한다.
```
@Test
public void skipAFewSeconds(){
    Flux<String> skipFlux=Flux.just(
        "one","two","skip a few", "ninety nine", "one hundred")
        .delayElements(Duration.ofSeconds(1))
        .skip(Duration.ofSeconds(4));

    StepVerifier.create(skipFlux)
        .expectNext("ninety nine", "one hundred")
        .verifyComplete();
}
여기서는 항목간에 1초동안 지연되는 Flux로부터 결과 Flux가 생성되었으므로 마지막 두개의 항목만 방출된다
```

`skip()`오퍼레이션의 반대 기능이 필요할 때는 `take()`를 고려할 수 있다. skip()이 처음의 여러개 항목을 건너뛰는 반면,

take()는 처음부터 지정된 수의 항목만을 방출한다.
```
@Test
public void take(){
    Flux<String> nationParkFlux=Flux.just(
        "Yellowstone","Yosemite","Grand Canyon",
        "Zion", "Grand Teton")
        .take(3);
    
    StepVerifier.create(nationalParkFlux)
        .expectNext("Yellowstone","Yosemite","Grand Canyon")
        .verifyComplete();
}
```

skip()처럼 take()도 항목 수가 아닌 경과 시간을 기준으로 하는 다른 형태를 갖는다. 이경우 소스 Flux로부터 전달되는 항목이 일정시간 경과될동안만 방출된다


```
@Test
public void take(){
    Flux<String> nationalParkFlux=Flux.just(
        "Yellowstone","Yosemite","Grand Canyon",
        "Zion", "Grand Teton")
        .delayElements(Duration.ofSeconds(1))
        .take(Duration.ofMillis(3500));

    StepVerifier.create(nationParkFlux)
        .expectNext("Yellowstone", "Yosemite","Grand Canyon")
        .verifyComplete();
}
```
skip()과 take()오퍼레이션은 카운트나 경과시간을 필터 조건으로하는 일종의 필터 오퍼레이션이라고 생각 할 수 있다.

그러나 Flux값의 더 범용적인 필터링을 할때는 `filter()`오퍼레이션이 매우 유용하다.

Flux를 통해 항목을 전달할 것인가의 여부를 결정하는 조건식(Predicate)이 지정되면 filter()오퍼레이션에서 원하는 조건을 기반으로 선택적 발행을 할수있다.

```
@Test
public void filter(){
    Flux<String> nationParkFlux=Flux.just(
        "Yellowstone","Yosemite","Grand Canyon",
        "Zion","Grand Teton")
    .filter(np -> !np.contains(" ));

    StepVerifier.create(nationalParkFlux)
        .expectNext("Yellowstone","Yosemite","Zion")
        .verifyComplete();
}
```

여기서 `filter()` 에는 람다로 조건식이 지정되었으며 이 람다에서는 공백이 없는 문자열 값만 받는다.

따라서 "Grand Canyon"과 "Grand Teton"은 결과 Flux에서 제외된다. 경우에 따라서는 이미 발행되어 수신된 항목을

필터링으로 걸러낼 필요가있는데 이때 `distinct()`오퍼레이션을 사용하면 발행한 적이 없는 소스 Flux의 항목만 발행하는 결과 Flux를생성한다

## 👟 리액티브 데이터 매핑하기

Flux나 Mono에 가장 많이 사용하는 오퍼레이션 중 하나는 발행된 항목을 다른형태의 타입으로 매핑하는 것이다.

리액터의 타입은 이런 목적의 `map()`과 `flatMap()`오퍼레이션을 제공한다.

`map()`오퍼레이션은 변환을 수행하는 Flux를 생성한다. 다음 테스트는 농구 선수 이름을 나타내는 문자열

값을 전달하는 소스 Flux가 Player 객체를 발행하는 새로운 Flux로 변환된다
```
@Test
public void map(){
    Flux<Player> playerFlux=Flux
        .just("Michael Jordan", "Scottie Pippen", "Steve Kerr")
        .map(n ->{
            String[] split=n.split("\\s");
            return new Player(split[0],split[1]);
        });
    StepVerifier.create(playerFlux)
        .expectNext(new Player("Michael", "Jordan"))
        .expectNext(new Player("Scottie","Pippen"))
        .expectNext(new Player("Steve", "Kerr"))
        .verifyComplete();
}
```

map()에 지정된 함수에서는 공백을 기준으로 입력 문자열을 분리하여 배열에 넣고 이 배열을 사용해서 Player객체를 생성한다.

여기서 just()로 생성된 Flux는 String객체를 발행하지만 , map()의 결과로 생성된 Flux는 Player객체를 발행한다.

`map()`에서 알아 둘 중요한 것은, 각 항목이 소스 Flux로부터 발행될 때 동기적으로 매핑이 수행된다는 것이다. 

따라서 비동기적으로 매핑을 수행하고 싶다면 `flatMap()`오퍼레이션을 사용해야한다.

```
@Test
public void flatMap(){
    Flux<Player> playerFlux = Flux
        .just("Michael Jordan" , "Scottie Pippen", "Steve Kerr")
        .flatMap(n -> Mono.just(n)
            .map(p -> {
                String[] split=p.split("\\s");
                return new Player(split[0],split[1]);
            })
            .subscribeOn(Schedulers.parallel())
            );
    List<Player> playerList= Arrays.asList(
        new Player("Michael", "Jordan"),
        new Player("Scottie", "Pippen"),
        new Player("Steve", "Kerr"));

    StepVerifier.create(playerFlux)
        .expectNextMatches(p -> playerList.contains(p))
        .expectNextMatches(p -> playerList.contains(p))
        .expectNextMatches(p -> playerList.contains(p))
        .verifyComplete();
}

```

여기서는 String 타입의 입력 문자열을 String 타입의 Mono변환하는 람다가 flatMap()에 지정되었다.

그다음에 map()오퍼레이션이 해당 Mono에 적용되어 String 객체를 Player객체로 변환한다.

만일 여기서 멈춘다면 결과 Flux는 Player객체를 전달할 것이며, Player객체는 바로 전의 map() 예와 동일한 순서로 생성된다.

그러나 마지막에 `subscribeOn()`을 호출하였다. 이것은 각 구독이 병렬 스레드로 수행되어야 한다는 것을 나타낸다.

따라서 다수의 입력 객체들의 map()오퍼레이션이 비동기적으로 병헹 수행될 수있다.

`subscribeOn()`의 이름은 `subscribe()`와 유사하지만 두 오퍼레이션은 매우 다르다.

`subscribe()`는 이름이 동사형이면서 리액티브 플로우를 구독 요청하고 실제로 구독하는 반면,

`subscribeOn()`은 이름이 더 서술적이면서 구독이 동시적으로 처리되어 햔다는 것을 지정한다.

리액터는 어떤 특정 동시성 모델도 강요하지 않으며, 우리가 사용하기 원하는 동시성 모델을 `subscribeOn()`의 인자로 지정할 수 있다.

이때 Schedulers의 static 메소드 중 하나를 사용한다. 위의 예에서는 고정된 크기의스레드 풀의 작업스레드로 실행되는 parallel()을사용했다.

그러나 Schedulers는 아래와 같은 몇가지 동시성 모델을 지원한다
|Schedulers 메소드| 개요|
|:--:|:--:|
|`.immediate()`|현재 스레드에서 구독을 실행한다|
|`.single()`|단일의 재사용 가능한 스레드에서 구독을 실행한다. 모든 호출자에 대한 동일한 스레드를 재사용한다|
|`.newSingle()`|매 호출마다 전용 스레드에서 구독을 실행한다|
|`.elastic()`|무한하고 신축성있는 풀에서 가져온 작업 스레드에서 구독을 실행한다. 필요시 새로운작업 스레드가 생성되며, 유휴 스레드는 제거된다.(기본적으로 60초)|
|`.parallel()`|고정된 크기의 풀에서 가져온 작업 스레드에서 구독을 실행하며, CPU코어의 개수가 크기가된다|

`flatMap()`이나 `subscribeOn()`을 사용할 때의 장점은 다수의 병행 스레드에 작업을 분할 하여 스트림의 처리량을 증가시킬수있다는것이다.

그러나 작업이 병행으로 수행되므로 어떤 작업이 먼저 끝날지 보장이 안되어 결과 Flux에서 방출되는 항목의 순서를

알방법이 없다. 따라서 방출되는 각 항목이 우리가 기대하는 Player객체 리스트에 존재하는지,

그리고 3개의 항목이 있는지만 StepVerifier가 검사할 수 있다.

### 🌟 리액티브 스트림의 데이터 버퍼링하기

Flux를 통해 전달되는 데이터를 처리하는 동안 데이터 스트림을 작은 덩어리로 분할하면 도움이 될 수 있다. 이 때 `buffer()`를 사용한다

문자열 값을 갖는 Flux가 지정되었을때 Flux로부터 List컬렉션들을 포함하는 새로운 Flux를 생성할 수 있다, 이때 각 List는 지정된 수 이내의 요소를갖는다
```
Flux<String> fruitFlux=Flux.just(
    "apple","orange","banana","kiwi","strawberry");

    Flux<List<String>> bufferedFlux=fruitFlux.buffer(3);

    StepVerifier
        .create(bufferedFlux)
        .expectNext(Arrays.asList("apple","orange","banana"))
        .expectNext(Arrays.asList("kiwi","strawberry"))
        .verifyComplete();
```

이경우 String 요소의 Flux는 List컬렉션을 포함하는 새로운 Flux로 버퍼링한다. 따라서 5개의 String

값을 방출하는 원래의 Flux는 두개의 컬렉션을 방출하는 Flux로 변환된다. 이처럼 리액티브 Flux로부터 리액티브가

아닌 List컬렉션으로 버퍼링되는 값은 비생산적인 것처럼 보인다. 그러나 buffer()를 flatMap()과 같이

사용하면 각 List컬렉션을 병행으로 처리할 수 있다.
```
Flux.just("apple","orange","banana","kiwi","strawberry")
    .buffer(3)
    .flatMap( x -> 
        Flux.fromIterable(x)
            .map(y -> y.toUpperCase())
            .subsribeOn(Schedulers.parallel())
            .log()
            ).subscribe();
```
여기서는 5개의 값으로 된 Flux를 새로운 Flux로 버퍼링하지만, 이Flux는 여전히 List 컬렉션을 포함한다.

그러나 그다음에 List컬렉션의 Flux에 flatMap()을 적용한다. 이경우 flatMap()에서는 각 List 버퍼를 가져와서 해당 List

의 요소로부터 새로운 Flux를 생성하고 map()오퍼레이션을 적용한다. 따라서 버퍼링된 각 List는 

별도의 스레드에서 병행으로 계속 처리될 수 있다.

만일 어떤 이유로든 Flux가 방출되는 모든 항목을 List로 모을 필요가 있다면 인자를 전달하지 않고 buffer()를 호출하면된다

    Flux<List<String>> bufferedFlux=fruitFlux.buffer();

이 경우 소스 Flux가 발행한 모든 항목을 포함하는 List를 방출하는 새로운 Flux가 생성된다. `collectList()`오퍼레이션

을 사용해도 같은 결과를 얻을 수 있다. collectList()는 List를 발행하는 Flux대신 Mono를 생성한다.

```
@Test
public void collectList(){
    Flux<String> fruitFlux=Flux.just(
        "apple","orange","banana","kiwi","strawberry");
    
    Mono<List<String>> fruitListMono=fruitFlux.collectList();

    StepVerifier
        .create(fruitListMono)
        .expectNext(Arrays.asList(
            "apple","orange","banana","kiwi","strawberry"
        )).verifyComplete();
}
```

Flux가 방출하는 항목들을 모으는 훨씬 더 흥미로운 방법으로 `collectMap()`이 있다. collectMap()오퍼레이션은 

Map을 포함하는 Mono를 생성한다. 이 때 해당하는 Map에는 지정된 함수로 산출된 키를 갖는 항목이 저장된다.

```
@Test
public void collectMap(){
    Flux<String> animalFlux=Flux.just(
        "aardvark","elephant","koala","eagle","kangaroo");
    
    Mono<Map<Character,String>> animalMapMono=
        animalFlux.collectMap(a -> a.charAt(0));
    
    StepVerifier
        .create(animalMapMono)
        .expectNextMatches(map -> {
                return 
                    map.size()==3 &&
                    map.get('a').equals("aardvark") &&
                    map.get('e').equals("eagle") &&
                    map.get('k').equals("kangaroo")
        })
        .verifyComplete();
}
```
여기서 소스 Flux(animalFlux)는 소수의 동물 이름을 방출한다. 그리고 이 Flux로부터 collectMap()을 사용해서 Map을 방출

하는 새로운 Mono를 생성한다. 이 때 Map의 키는 동물 이름의 첫 번째 문자로 결정되며, 키의 항목 값은 동물 이름 자체가 된다.

두 동물 이름이 같은 문자로 시작하는 경우는 Map의 키가 같아진다. 따라서 스트림의 동물이름이 Map에 저장될 때 

앞에있는 항목값이 나중에 나온 항목값으로 변경된다.

## 👟 리액티브 타입에 로직 오퍼레이션 수행하기

Mono나 Flux가 발행한 항목이 어떤 조건과 일치하는지만 알아야 할 경우가있다. 이때는 `all()`이나 `any()`오퍼레이션이 그런로직을 수행한다.

Flux가 발행하는 모든 문자열이 문자 a나 k를 포함하는지 알고싶을때 다음 테스트에서 all()을 사용하여 검사할수있다.
```
@Test
public void all(){
    Flux<String> animalFlux=Flux.just(
        "aardvark","elephant","koala","eagle","kangaroo");
        Mono<Boolean> hasAMono=animalFlux.all(a -> a.contains("a"));
        StepVerifier.create(hasAMono)
            .expectNext(true)
            .verifyComplete();

        Mono<Boolean> hasKMono= animalFlux.all(a -> a.contains("k"));
        StepVerifier.create(hasKMono)
            .expectNext(false)
            .verifyComplete();
}
```

첫번째 StepVerifier에서는 문자 a를검사한다. all오퍼레이션이 소스Flux에 적용되었고 결과는 Boolean타입의 Mono로 생성된다.

예에서는 모든 동물이름에 a가포함되어있어 true가 방출된다. 그러나 두번 째 StepVerifier에는 결과가 false를반환한다.

이처럼 '모 아니면 도'와같은 검사를 수행하지 않고 하나의 항목이 일치하는지 검사할경우가있다.

이때는 `any()`오퍼레이션을 사용한다. 다음테스트는 이 any()를사용해 t와z를검사한다.
```
@Test
public void any(){
    Flux<String> animalFlux=Flux.just(
        "aardvark","elephant","koala","eagle","kangaroo");
    
    Mono<Boolean> hasTMono= animalFlux.any(a -> a.contains("t"));

    StepVerifier.create(hasTMono)
        .expectNext(true)
        .verifyComplete();
    
    Mono<Boolean> hasZMono=animalFlux.any(a -> a.contains("z"));
    StepVerifier.create(hasZMono)
        .expectNext(false)
        .verifyComplete();
}
```

첫 번째 StepVerifier에서는 결과 Mono가 true을 방출한다 . 최소한 하나의 동물이름에 t가포함되어있기 때문이다.

# 10장 요약
#### [1]리액티브 프로그래밍에서는 데이터가 흘러가는 파이프라인을 생성한다
#### [2]리액티브 스트림은 Publisher,Subscriber,Subscription,Transformer의 네가지 타입을 정의한다
#### [3]프로젝트 리액터는 리액티브 스트림을 구현하며, 수많은 오퍼레이션을 제공하는 Flux와 Mono두가지타입으로 스트림을정의한다
#### [4] 스프링5는 리액터를 사용하여 리액티브 컨트롤러, 레포지토리,Rest클라이언트를 생성하고 다른 리액티브 프레임워크를 지원한다
















