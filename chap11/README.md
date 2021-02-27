# 🥇 11장 리액티브 API 개발하기

### 🌟 이 장에서 배우는 내용
- 스프링 WebFlux 사용하기
- 리액티브 컨트롤러와 클라이언트 작성하고 테스트하기
- REST API 소비하기
- 리액티브 웹 애플리케이션의 보안
---
## 🍰 스프링 WebFlux 사용하기
매 연결마다 하나의 스레드를 사용하는 스프링 MVC같은 전형적인 서블릿 기반의 웹 프레임워크는 스레드 블로킹과 다중스레드로수행된다.

즉, 요청이 처리될 때 스레드 풀에서 작업 스레드를 가져와 해당 요청을 처리하며, 작업 스레드가 종료될 때까지 요청 스레드는 블로킹된다.

따라서 블로킹 웹 프레임워크는 요청량의 증가에 따른 확장이 사실상 어렵다. 게다가 처리가 느린 작업스레드로 인해 심각한상황이 발생한다.

해당 작업 스레드가 풀로 반환되어 또 다른 요청 처리를 준비하는 데 더 많은 시간이 걸리기 때문이다. 상황에 따라서는 이런 방식이

받아들일만 하지만 시대가 바뀌고있다. 이런 웹애플리케이션 클라이언트는 가끔 웹사이트를 보는 사람들로부터 빈번하게 콘텐츠를

소비하고 HTTP API와 연동하는 애플리케이션을 사용하는 사람들로 변모하고 있다. 요즘은 사물인터넷, 비동기적 클라이언트

등 웹어플리케이션을 사용하는 클라이언트 수가 증가함에 따라 그 어느때보다 확장성이 중요해졌다.

이에 반해 비동기 웹프레임워크는 더 적은 스레드로 더 높은 확장성을 성취한다. `이벤트 루핑`이라는 기법을 적용한 프레임워크는

한 스레드당 많은 요청을 처리할 수 있어서 한 연결당 소요 비용이 경제적이다. 결과적으로 비동기 웹 프레임

워크는 소수의 스레드로 많은 요청을 처리할 수 있어서 스레드 관리부담이 줄어들고 확장이 용이하다.

#### 비동기 웹 프레임워크는 이벤트 루핑을 적용해 더 적은수의 스레드로 더 많은 요청을 처리한다

![image](https://user-images.githubusercontent.com/40031858/108790181-66460400-75bf-11eb-92e7-d93026aca7d5.png)


### 🌟 스프링 WebFlux 개요


#### 스프링 5는 WebFlux라는 새로운 웹 프레임워크로 리액티브 웹 애플리케이션을 지원한다.

#### Web-Flux는 스프링 MVC의 많은 핵심 컴포넌트를 공유한다

![image](https://user-images.githubusercontent.com/40031858/108790437-0a2faf80-75c0-11eb-9532-9615f6e8feec.png)

위 그림에서 왼쪽 부분은 스프링 MVC스택이다. 스프링 MVC는 실행 시에 톰캣과 같은 서블릿 컨테이너가 필요한 자바 서블릿 API상위계층에 위치한다.

이에 반해서 오른쪽 스프링 WebFlux는 서블릿 API와 연계되지 않는다. 따라서 서블릿 API가 제공하는 것과

동일한 기능의 리액티브 HTTP API의 상위 계층에 위치한다. 그리고 스프링 WebFlux는 서블릿 API에 연결되지 않으므로

실행하기 위해 서블릿 컨테이너를 필요로 하지않는다. 대신에 블로킹이 없는 어떤 웹 컨테이너에서도 실행 될 수 있으며,

이에는 Netty,Undertow, 톰캣,Jetty또는 다른 서블릿 3.1이상의 컨테이너가 포함된다.


먼저 가장 주목할 만한것은 제일 위의 왼쪽네모에 있다. 이것은 스프링 MVC와 스프링 WebFlux간의 공통적인 컴포넌트들을 나타내며,

주로 컨트롤러를 정의하는데 사용되는 애노테이션들이다. 스프링 MVC와 스프링 WebFlux는 같은 애노테이션을 공유한다.

제일 위의 오른쪽 네모는 애노테이션을 사용하는 대신 함수형 프로그래밍 패러다임으로 컨트롤러를 정의하는 대안 프로그래밍 모델을 나타낸다.

스프링 MVC와 스프링 WebFlux간의 가장 중요한 차이는 빌드에 추가하는 의존성이다.  스프링 WebFlux를 사용할 때는 표준 웹스타터 대신

스프링 부트 WebFlux스타터 의존성을 추가해야한다
```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

또한 스프링 MVC대신 WebFlux를 사용할 때는 기본적인 내장서버가 톰캣 대신 Netty가 된다. Netty는 몇 안되는 비동기적 이벤트 

중심의 서버 중 하나이며, 스프링 WebFlux와 같은 리액티브 웹 프레임워크에 잘맞는다. 다른 스타터 의존성을 사용하는 것

외에도 스프링 WebFlux의 컨트롤러 메소드는 대개 도메인 타입이나 컬렉션 대신 `Mono`나 `Flux`같은

리액티브 타입을 인자로 받거나 반환한다. 또한, 스프링 WebFlux컨트롤러는 Obseravable, Single, Completable과 같은 RxJava타입도 처리할수있다

## 🏓  리액티브 스프링 MVC???

스프링 WebFlux 컨트롤러가 Mono나 Flux같은 리액티브 타입을 반환하지만, 그렇다고 해서 스프링 MVC가

리액티브 타입을 전혀 사용하지 못하는 것은 아니다. 스프링 MVC의 컨트롤러 메소드도 Mono나 Flux를 반환할 수 있다.

단지 차이점은 타입들이 사용되는 방법에 있다. 즉, 스프링 WebFlux는 요청이 이벤트 루프로 처리되는 진정한

리액티브 웹 프레임워크인 반면, 스프링 MVC는 다중 스레드에 의존하여 다수의 요청을 처리하는 서블릿 기반웹프레임워크다.

### 🌟 리액티브 컨트롤러 작성하기
이전에 작성했던 DesignTacoController의 코드를 봐보자
```
@RequestController
@RequestMapping(path="/design",produces="application/json")
@CrossOrigin(origins="*")
public class DesignTacoController{
    ...

    @GetMapping("/recent")
    public Iterable<Taco> recentTacos(){
        PageRequest page=PageRequest.of(
            0,12,Sort.by("createdAt").descending());
        return tacoRepo.findAll(page).getContent();
    }
    ...
}
```
여기서 recentTacos()컨트롤러 메소드는 /design/recent의 HTTP GET요청을 처리하여 최근 생성된

타코들의 리스트를 반환한다. 이코드는 잘 작동하지만 Interable은 리액티브 타입이 아니다. 따라서 Iterable에는

어떤 리액티브 오퍼레이션도 적용할 수 없으며, 또한 프레임워크가 Iterable타입을 리액티브 타입으로 사용하여 여러스레드에

걸쳐 작업을 분할하게 할 수도 없다. 이제 Iterable타입을 Flux타입으로 변환하기 위해 다시작성하는 것은 간단하지만 

이렇게 페이징도 Flux의 take()호출로 교체할 수 있다.
```
@GetMapping("/recent")
public Flux<Taco> recentTacos(){
    return Flux.fromIterable(tacoRepo.findAll()).take(12);
}
```

이렇게 사용하면 Iterable< Taco>가 Flux< Taco>로 변환된다. 또한 타입을 변환할 필요가 없도록

아예 해당 리포지토리에서 Flux타입을 반환하면 더좋을 것인데 그렇다면 이렇게 작성할 수 있다.
```
@GetMapping("/recent")
public Flux<Taco> recentTacos(){
    return tacoRepo.findAll().take(12);
}
```
이 코드가 훨씬 더좋아 보이며 이상적으로는 리액티브 컨트롤러가 리액티브 엔드-to-엔드 스택의 제일 끝에 위치하며, 

이 스택에는 컨트롤러, 레포지토리, 데이터베이스, 그리고 여타 서비스가 포함된다.

![image](https://user-images.githubusercontent.com/40031858/108801484-33106e80-75d9-11eb-83bc-098c020d5f1e.png)

이런 엔드- to -엔드 스택에서는 Iterable 대신 Flux를 반환하도록 레포지토리가 작성되어야 한다.

리액티브 레포지토리의 작성에 관해서는 다음장에서 자세히 알아보자 다만 다음과 같이 작성될 수 있다는것만 알아두자
```
public interface TacoRepository extends ReactiveCrudRepository<Taco, Long>{

}
```

Iterable 대신 Flux를 사용하는 것 외에, WebFlux 컨트롤러에 관해 알아 둘 중요한것이 있다.

리액티브 WebFlux 컨트롤러를 정의하기 위한 프로그래밍 모델은 `리액티브가 아닌 스프링 MVC컨트롤러와 크게 다르지않다.`

두 가지 모두 @RestController와 @RequestMapping 애노테이션이 클래스에 지정되며, 메소드 수준의 

@GetMapping애노테이션이 지정된 요청 처리 메소드들을 갖는다. 이외에 중요한 것은 레포지토리로부터

`Flux<Taco>`와 같은 리액티브 타입을 받을때 `subscribe()`를 호출할 필요가 없다는 것이다. 프레임워크가 호출해주기때문!!

### 🌟 단일 값 반환하기

이전에 작성한 tacoById()메소드를 보자
```
@GetMapping("/{id}")
public Taco tacoById(@PathVariable("id") Long id){
    Optional<Taco> optTaco=tacoRepo.findById(id);
    if(opTaco.isPresent())
        return optTaco.get();
    return null;
}
```

여기서는 메소드가 /design/{id}의 GET 요청을 처리하고 하나의 Taco객체를 반환한다.

그러나 findById()가 Optional< Taco> 대신 Mono< Taco>를 반환한다고하면 다음과 같이 작성할 수있다.

```
@GetMapping("/id")
public Mono<Taco> tacoById(@PathVariable("id") Long id){
    return tacoRepo.findById(id);
}
```

Mono< Taco> 리액티브 타입 객체를 반환하므로 스프링 WebFlux가 리액티브 방식으로 응답을 처리할 수 있다는것.

이에 따라 많은 요청에 대한 응답 처리 시에 API의 확장성이 더 좋아진다.

### 🌟 RxJava 타입 사용하기 

스프링 WebFlux를 사용할 때 Flux나 Mono와 같은 리액티브 타입이 자연스럽지만 Observable이나 Single같은

RxJava타입을 사용할 수도 있다는 것을 알아두자. 예를들어 다음과 같이 작성할 수 있다.
```
@GetMapping("/recent")
public Observable<Taco> recentTacos(){
    return tacoService.getRecentTacos();
}
```

이와 유사하게 Mono가아닌 RxJava의 Single타입을 처리하기 위해 다음과 같이 작성할 수 있다.
```
@GetMapping("/{id}")
public Single<Taco> tacoById(@PathVariable("id") Long id){
    return tacoService.lookupTaco(id);
}
```

이와 더불어 스프링 WebFlux 컨트롤러의 메소드는 리액터의 Mono< Void> 타입과 동일한 RxJava의 Completable타입을

반환할 수 도있다. WebFlux는 또한 Observable이나 리액터 Flux타입의 대안으로 Flowable타입을 반환할수도있다.

### 🌟 리액티브하게 입력하기

스프링 WebFlux를 사용할 때 요청을 처리하는 핸들러 메소드의 입력으로도 Mono나 Flux를 받을 수 있다. 예를들어,
```
@PostMapping(consumes="application/json")
@ResponseStatus(HttpStatus.CREATED)
public Taco postTaco(@RequestBody Taco taco){
    return tacoRepo.save(taco);
}
```

여기서는 postTaco()가 간단한 Taco 객체를 반환하는 것은 물론이고, 요청 몸체의 콘텐츠와 결합된 Taco객체를 입력으로 받는다

이것은 요청 페이로드(요청의 헤더와 같은 메타데이터가 아닌 실제데이터)가 완전하게 분석되어 Taco 객체를

생성하는데 사용될 수 있어야 postTaco()가 호출될 수 있다는 것을 의미한다. 또한, 레포지토리의 save()메소드의 블로킹

되는 호출이 끝나고 복귀되어야 postTaco()가 끝나고 복귀할 수 있다는 것을 의미한다. 간략히 말해, 

요청은 두 번 블로킹된다. postTaco()로 진입할 때와 postTaco()의 내부에서.

하지만 postTaco()에 리액티브 코드를 적용하면 완전히 블로킹되지 않게 다음과같이 요청처리메소드를 만들수있다.
```
@PostMapping(consumes="application/json")
@ResponseStatus(HttpStatus.CREATED)
public Mono<Taco> postTaco(@RequestBody Mono<Taco> tacoMono){
    return tacoRepo.saveAll(tacoMono).next();
}
```
여기서 postTaco()는 Mono< Taco>를인자로 받아 레포지토리의 saveAll()메소드를 호출한다.

saveAll()메소드는 Mono나 Flux를 포함해서 리액티브 스트림의 Publisher인터페이스를 구현한 어떤 타입도 인자로 받을 수 있다.

saveAll()메소드는 Flux< Taco>를 반환한다. 그러나 postT채()의 인자로 전달된 Mono를 saveAll()에서 인자로

받았으므로 saveAll()이 반환하는 Flux가 하나의 Taco객체만 포함한다는 것을 알고있다. 

따라서 next()를 호출하여 Mono< Taco>로 받을 수 있으며 이것을 postTaco()가 반환한다.

    Flux는 0,1,또는 다수의 데이터를 갖는 파이프라인. Mono는 하나의 데이터 항목만 갖는 데이터셋에 최적화 된 리액티브타입

saveAll()메소드는 Mono< Taco>를 입력으로 받으므로 요청 몸체로부터 Taco객체가 분석되는 것을 기다리지 않고 즉시호출된다.

그리고 레포지토리 또한 리액티브이므로 Mono를 받아 즉시 Flux< Taco>를 반환한다. 이 Flux< Taco>를 next()

호출에서 Mono< Taco>로 반환한다. `스프링 WebFlux`는 스프링 MVC의 환상적인 대안이며, 스프링 MVC와

동일한 개발모델을 사용해 리액티브 웹 애플리케이션을 작성할 수 있는 선택의 기회를 준다.

## 👟 함수형 요청 핸들러 정의하기

스프링 MVC의 애노테이션 기반 프로그래밍 모델은 스프링 2.5붙너 있었고 지금도 널리 사용되고 있다. 하지만 몇가지 단점이있다.

우선 어떤 애노테이션 기반 프로그래밍이건 애노테이션이 `무엇`을 하는지와 `어떻게`해야 하는지를 정의하는데 괴리가있다.

애노테이션 자체는 `무엇`을 정의하며 `어떻게`는 프레임워크 코드의 어딘가에 정의되어 있다. 이로 인해

프로그래밍 모델을 커스터마이징하거나 확장할 때 복잡해진다. 이런 변경을 하려면 애노테이션 외부에 있는 코드로 작업해야하기 때문.

게다가 이런 코드의 디버깅은 까다롭다. 애노테이션에 중단점을 설정할 수 없기 때문이다.

또한 스프링이 처음인 개발자들은 애노테이션기반의 스프링MVC,WebFlux가 이미 알던것과 매우다르다는 것을 발견할수있다.

따라서 WebFlux의 대안으로 스프링 5에는 리액티브 API를 정의하기 위해 새로운 함수형 프로그래밍 모델이 소개되었다.

이런 새로운 프로그래밍 모델은 프레임워크보다는 라이브러리 형태로 사용되므로 애노테이션을 사용하지 않고 요청을 핸들러 코드에 연관시킨다.

스프링의 함수형 프로그래밍 모델을 사용한 API작성에는 다음 네 가지 기본타입이 수반된다
- `RequestPredicate`: 처리될 요청의 종류를 선언한다
- `RouteFunction`: 일치하는 요청이 어떻게 핸들러에게 전달되어야 하는지를 선언한다
- `ServerRequest`: HTTP요청을 나타내며, 헤더와 몸체 정보를 사용할 수 있다
- `ServerResponse`: HTTP응답을 나타내며, 헤더와 몸체 정보를 포함한다

이 타입 모두를 사용하는 다음의 간단한 예를 살펴보자
```
import static.org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static.org.springframework.web.reactive.function.server.RouterFunctions.route;
import static.org.springframework.web.reactive.function.server.ServerResponse.ok
import static reactor.core.publisher.Mono.just;
@Configuration
public class RouterFunctionConfig{
    @Bean
    public RouterFunction<?> helloRouterFunction(){
        return route(GET("/hello"),
            request-> ok().body(just("Hello world!"),String.class));
    }
}
```
`@Configuration`이 지정된 RouterFunctionConfig클래스에는 `RouterFunction<?>`타입의 @Bean메소드가 하나 있다.

이 `RouterFunction`은 요청을 나타내는 `RequestPredicate`객체가 어떤 요청 처리함수와 연관되는지를 선언한다.

`RouterFunction`의 `route()`메소드는 두개의 인자를 받는다. 하나는 `RequestPrdicate`객체이고 다른하나는 일치하는 요청을 처리하는 함수다.

여기서는 /hello 경로의 HTTP GET요청과 일치하는 RequestPredicate을 RequestPredicates의 GET()메소드가 선언된다.

두 번째 인자로 전달된 핸들러 함수는 메소드 참조가 될 수도있지만, 여기서는 람다로 작성하였다. 

요청 처리 람다에서는 `ServerRequest`를 인자로 받으며, `ServerResponse`의 ok()메소드와 이 메소드에서 반환된

BodyBuilder의 body()를 사용해 `ServerResponse`를 반환한다. 그리고 실행이 완료되면 

HTTP 200상태코드를 갖는 응답과 'Hello World!'를 갖는 몸체 페이로드가 생성된다.

이 코드에서 helloRouterFunction()메소드는 한 종류의 요청만 처리하는 RouterFunction을 반환타입으로 선언한다.

그러나 다른 종류의 요청을 처리해야 하더라도 또다른 `@Bean`메소드를 작성할 필요없다

대신 `addRoute()`를 호출하여 또다른 `RequestPredicate`객체가 어떤 요청 처리 함수와 연관되는지 선언하면된다.

예를들어 /bye의 GET요청을 처리하는 또다른 핸들러를 다음과같이 추가할수있다.

```
@Bean
public RouterFunction<?> helloRouterFunction(){
    return route(GET("/hello"),
        request-> ok().body(just("Hello World!"),String.class))
        .andRoute(GET("/bye"),
        request-> ok().body(just("See ya!"),String.class));
}
```

이제 기존 작성했었던 DesignTacoController의 동일한기능을 함수형 방식으로 작성해보자.
```
@Configuration
public class RouterFunctionConfig{
    @Autowired
    private TacoRepository tacoRep;

    @Bean
    public RoouterFunction<?> routerFunction(){
        return route(GET("/design/taco"), this::recents)
            .andRoute(POST("/design"),this::postTaco);
    }

    public Mono<ServerResponse> recents(ServerRequest request){
        return ServerResponse.ok()
            .body(tacoRepo.findAll().take(12),Taco.class);
    }

    public Mono<ServerResponse> postTaco(ServerRequest request){
        Mono<Taco> taco=request.bodyToMono(Taco.class);
        Mono<Taco> savedTaco=tacoRepo.save(taco);
        return ServerResponse
            .create(URI.create(
                "http://localhost:8080/design/taco" +
                savedTaco.getId()))
            .body(savedTaco,Taco.class);
    }
}
```

여기서 routerFunction()메소드는 앞의 예와 같이 RouterFunction<?>빈을 선언한다.

그러나 이것은 요청을 처리하는 타입과 방법이 다르다. 이 경우 RouterFunction은 /design.taco의 

GET요청과 /design의 POST요청을 처리하기 위해 생성된다.

또 눈에 띄는 것은 람다가 아닌 메소드 참조로 경로가 처리된다는 것이다. RouterFunction의 내부 기능이 간단할 때는 람다가 아주좋다.

그러나 여러 경우에서 해당 기능을 별도의 메소드로 추출하고 메소드 참조를 사용하는 것이 코드파악에 더 좋다.

따라서 여기서는 /design/taco의 GET요청이 recents()메소드에서 처리되도록 하였고 이 메소드에서 주입된 TacoRepository를

사용해서 12개까지의 Mono< Taco>를 가져온다. 또한 /design의 POST요청은 postTaco()메소드에서 처리되며,

이 메소드에서는 인자로 전달된 ServerRequest로부터 하나의 Mono< Taco>를 추출한다 그 다음에

TacoRepository를 사용해서 레포지토리에 저장한후 save()
메소드로부터 반환되는 Mono< Taco>를 응답에 포함시켜 반환한다.

---

## 🍰 리액티브 컨트롤러 테스트하기

스프링 5는 `WebTestClient`를 소개하였는데  이것이 바로 스프링`WebFlux`를 사용하는 리액티브

컨트롤러의 테스트를 쉽게 작성하게 해주는 새로운 테스트 유틸리티다. 

#### 🐸 GET요청 테스트하기
```
public class DesignTacoControllerTest{
    @Test
    public void shouldReturnRecentTacos(){
        Taco[] tacos={
            testTaco(1L),testTaco(2L),
            testTaco(3L),testTaco(4L),
            testTaco(5L),testTaco(6L),
            testTaco(7L),testTaco(8L),
            testTaco(9L),testTaco(10L),
            testTaco(11L),testTaco(12L),
            testTaco(13L),testTaco(14L),
            testTaco(15L),testTaco(16L)
        }
        Flux<Taco> tacoFlux=Flux.just(tacos);

        TacoRepository tacoRepo=Mockito.mock(TacoRepository.class);

        when(tacoRepo.findAll()).thenReturn(tacoFlux);

        WebTestClient testClient=WebTestClient.bindToController(
            new DesignTacoController(tacoRepo))
            .build();
        
        testClient.get().uri("/design/recent")
            .exchange() 
            .expectStatus().isOk()
            .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$[0].id").isEqualTo(tacos[0].getId().toString())
                .jsonPath("$[0].name").isEqualTo("Taco 1").jsonPath("$[1].id")
                .isEqualTo(tacos[1].getId().toString()).jsonPath("$[1].name")
                .isEqualTo("Taco 2").jsonPath("$[11].id")
                .isEqualTo(tacos[11].getId().toString())
            ...
                .jsonPath("$[11].name").isEqualTo("Taco 12").jsonPath("$[12]")
                .doesNotExist();
                .jsonPath("$[12]").doesNotExist();    
    }
    ...
}
```

shouldReturnRecentTacos()메소드에는 제일 먼저 Flux< Taco>타입의 테스트 데이터를 생성한다. 

그리고 모의 TacoRepository의 findAll()메소드의 반환값으로 이 Flux가 제공된다.

Flux가 발행되는 Taco객체는 testTaco()라는 이름의 유틸리티 메소드에서 생성되며, 이 메소드에서는 인자로 받은 숫자로

ID와 이름을 갖는 Taco객첼르 생성한다. testTaco()메소드는 다음과 같이 구현된다.
```
private Taco testTaco(Long number){
    Taco taco=new Taco();
    taco.setId(UUID.randomUUID());
    taco.setName("Taco " + number);
    List<IngredientUDT> ingredients=new ArrayList<>();
    ingredients.add(
        new IngredientUDT("INGA","Ingredient A", "Type.WRAP));
    ingredients.add(
        new IngredientUDT("INGB","Ingredient B", Type.PROTEIN));

    taco.seetIngredients(ingredients);
    return taco;        
}
```

get().uri("/design/recent")의 호출은 submit요청을 나타내며, 그 다음에 exchange()를 호출하면 해당 요청을 제출한다.

그리고 이 요청은 WebTestClient와 연결된 컨트롤러인 DesignTacoController에 의해 처리된다.

마지막으로 요청 응답이 기대한것인지 검사하는데 우선, expectStatus()를 호출하여 응답이 HTTP 200상태코드를 갖는지 확인한다.

그 다음에는 jsonPath()를 여러번 호출하여 응답몸체의 JSON이 기대한 값을 갖는지 검사한다. 

제일 끝의 어서션(.jsonPath("$[12]").doesNotExist)에서는 인덱스 값이 12인 요소의 존재 여부를 검사한다.

왜냐하면 배열의 첫 번째 요소는 인덱스 값이 0부터 시작하므로 인덱스 값이 12인 요소는 응답의 JSON에 존재하면 안되기때문이다.

응답의 JSON 데이터가 많거나 중첩이 심해서 복잡할 경우에는 jsonPath()를 사용하기 번거로울 수 있다.

이런 경우를 위해 WebTestClient는 json()메소드를 제공한다. json()은 JSON을 포함하는 String을 인자로 받아 이것을 응답의 것과 비교한다.

예를들어, recent-tacos.json이라는 파일에 완벽한 응답 JSON을 생성해 /tacos경로의 classpath에 저장했다고 해보자

이 경우 WebTestClient의 어서션을 다음과 같이 다시 작성할 수 있다.
```
ClassPathResource recentsResource=
    new ClassPathResource("/tacos/recent-tacos.json");

String recentsJson=Streamutils.copyToString(
    recentsResource.getInputStream(),Charset.defaultCharset());

    testClient.get().uri("/design/recent")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .json(recentJson);
```

`json()`메소드는 String 타입의 인자를 받으므로 우선 classpath의 리소스를 String타입으로 로드해야한다. 

이때 스프링에서 제공하는 StreamUtils의 copyToString()메소드를 사용하면 쉽다. copyToString()이

반환하는 String값은 우리가 요청 응답에 기대하는 전체 JSON을 포함한다. 따라서 이 String 값을

json()메소드의 인자로 전달하여 컨트롤러가 올바른 응답을 생성하는지 확인할 수도있다.

WebTestClient는 리스트 형태로 여러 개의 값을 갖는 응답 몸체를 비교할 수 있는 `expectBodyList()`메소드도 제공한다.

이 메소드는 리스트에 있는 요소의 타입을 나타내는 Class나 ParameterizedTypeReference를 인자로 받아

assertion을 수행할 ListBodySpec객체를 반환한다.
```
testClient.get().uri("/design/recent")
    .accept(MediaType.APPLICATION_JSON)
    .exchange()
    .expectStatus().isOk()
    .expectBodyList(Taco.class)
    .contains(Arrays.copyOf(tacos,12));
```
이 코드는 응답 몸체가 List를 포함하는지 검사하는 어서션을 수행하는 예시이다.
---

#### 🐸 POST요청 테스트하기

### WebTestClient는 스프링 WebFlux컨트롤러에 대해 어떤종류의 요청도 테스트가 가능하다
|HTTP 메소드|WebTestClient메소드|
|:--|:--|
|GET|.get()|
|POST|.post()|
|PUT|.put()|
|PATCH|.patch()|
|DELETE|.delete()|
|HEAD|.head()|

스프링 WebFlux 컨트롤러에 대한 또 다른 HTTP메소드를 테스트하는 예를 살펴보자.

여기서는 /design의 POST요청을 제출하여 타코 클라우드 API의 타코 생성 엔드포인트를 테스트한다
```
@Test
public void shoudSaveATaco(){
    TacoRepository tacoRepo=Mockito.mock(
        TacoRepository.class); //테스트 데이터 설정
    
    Mono<Taco> unsavedTacoMono=Mono.just(testTaco(null));
    Taco savedTaco=testTaco(null);
    savedTaco.setId(1L);
    Mono<Taco> savedTacoMono=Mono.just(savedTaco);

    when(tacoRepo.save(any())).thenReturn(savedTacoMono);

    WebTestClient testClient=WebTestClient.bindToController(
        new DesignTacoController(tacoRepo)).build();

    testClient.post()
        .uri("/design")
        .contentType(MediaType.APPLICATION_JSON)
        .body(unsavedTacoMono,Taco.class)
        .exchange()
        .expectStatus().isCreated()
        .expectBody(Taco.class)
        .isEqualTo(savedTaco);    
}
```

shouldSaveATaco()는 모의 TacoRepository에 테스트 데이터를 설정하는 것부터 시작한 후 컨트롤러와 연관되는 WebTestClient를 생성한다.

그다음에 WebTestClient를 사용해서 /design의 POST요청을 제출한다.

이때 이 요청에는 application/json 타입의 몸체와 페이로드(JSON으로 직렬화된 형태의 Taco를 갖는 저장되지않은 Mono)가 포함된다.

그 다음 exchange()를 실행후 응답이 HTTP 201 상태 코드를 갖는지, 저장된 Taco객체와 동일한페이로드를 

응답 몸체가 갖는지 어서션으로 검사한다,

#### 🐸 실행중인 서버로 테스트하기

테스트에 있어서 Netty나 톰캣과 같은 서버 환경에서 레포지토리나 다른 의존성 모듈을 사용해서 WebFlux컨트롤러를

테스트할 필요가 있을 수있다. 다시말해 통합테스트를 작성할 수 있다.
```
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
public class DesignTacoControllerTest{
    @Autowired
    private WebTestClient testClient;

    @Test
    public void shouldReturnRecentTacos() throws IOException{
        testClient.get().uri("/design/recent")
            .accept(MediaType.APPLICATION_JSON).exchange()
            .expectStatus().isOk()
            .expectBody()
                .jsonPath("$[?(@.id=='TACO1')].name")
                    .isEqualTo("Carnivore")
                .jsonPath("$[?(@.id=='TACO2')].name")
                    .isEqualTo("Bovine Bounty")
                .jsonPath("$[?(@.id=='TACO3')].name")
                    .isEqualTo("Veg-Out");
    }
}
```

이렇게 보면 알 수 있듯 새버전의 shouldReturnRecentTacos()는 코드가 훨씬 적다. 그리고 더이상 WebTestClient인스턴스

를 생성할 필요가 없다. 자동 연결되는 인스턴스를 사용하기 때문. 또한 스프링이 DesignTacoController의 인스턴스를

생성하고 실제 TacoRepository를 주입하기 까지 한다. 

테스트 하는 동안 WebFlux컨트롤러가 노출하는 API를 사용할 때도 WebTestClient가 유용하다.

그러나 애플리케이션 자체에서 다른 API는 어떨까...?

---

## 🍰 REST API를 리액티브하게 사용하기

이전 7장에서는 RestTemplate을 사용해 타코클라우드 API의 클라이언트 요청을 했었다. 하지만 스프링 3.0버전에

소개된 RestTemplate은 구세대가 되었다. 그러나 RestTemplate이 제공하는 모든 메소드는 리액티브가 아닌 도메인 

타입이나 컬렉션을 처리한다. 따라서 리액티브 방식으로 응답데이터를 사용하고자 한다면 이것을 Flux나 Mono타입

으로 래핑해야한다. 이미 Flux나 Mono타입이 있으면서 POST나 PUT요청으로 전송하고 싶다면 요청을 하기 전

Flux나 Mono데이터를 리액티브가 아닌 타입으로 추출해야한다.

따라서 RestTemplate을 리액티브 타입으로 사용하는 방법이 있으면 좋았을거라는 생각이든다.

놀랍게도 스프링5가 RestTemplate의 리액티브 대안으로 `WebClient`를 제공한다.

`WebClient`의 사용은 `RestTemplate`을 사용하는 것과 많이 다르다. 다수의 메소드로 서로 다른 종류의 요청을 처리하는

대신 WebClient는 요청을 나타내고 전송하게 해주는 빌더 방식의 인터페이스를 사용한다. WebClient를 사용하는 일반적 패턴은 다음과 같다.

- WebClient의 인스턴스를 생성한다(또는 WebClient빈을 중비한다)
- 요청을 전송할 HTTP 메소드를 지정한다
- 요청에 필요한 URI와 헤더를 지정한다
- 요청을 제출한다
- 응답을 소비한다

### 🌟 리소스 얻기(GET)

WebClient의 사용예로 타코클라우드 API로부터 식자재를 나타내는 특정 Ingredient객체를 이것의 ID를 

사용해 가져와야 한다고 해보자 RestTemplate의 경우는 getForObject()메소드를 사용할 수 있다.

그러나 WebClient를 사용할 때는 요청을 생성하고 응답을 받은 다음에 Ingredient객체를 발행하는 Mono를 추출한다.

```
Mono<Ingredient> ingredient = WebClient.create()
    .get()
    .uri("http://localhost:8080/ingredients/{id}",ingredientId)
    .retrieve()
    .bodyToMono(Ingredient.class);

ingredient.subscribe(i -> {...})
```

위의 예시에서는 `create()` 메소드로 새로운 `WebClient`인스턴스를 생성한다. 그리고 get()과 uri()를 사용해

http://localhost:8080/ingredients/{id}에 대한 GET요청을 정의한다. 여기서 {id}
플레이스 홀더는 ingredientId의 값으로 대체될것이다.

retrive()메소드는 해당 요청을 실행한다. 마지막으로 bodyToMono()호출에서는 응답 몸체의 페이로드를

Mono< Ingredient>로 추출한다. 따라서 이 코드 다음에는 계속해서 Mono의 다른 오퍼레이션들을 연쇄 호출할수있다.

bodyToMono()로부터 반환되는 Mono에 추가로 오퍼레이션을 적용하려면 해당 요청이 전송되기 전에 구독을해야한다. 

따라서 이 예의 제일 끝에는 subscribe()메소드를 호출한다.

컬렉션에 저장된 값들을 반환하는 요청도 매우쉽다 예를 들어 다음코드는 모든 식자재를가져온다
```
Flux<Ingredient> ingredient = WebClient.create()
    .get()
    .uri("http://localhost:8080/ingredients")
    .retrieve()
    .bodyToFlux(Ingredient.class);

ingredient.subscribe(i -> {...})
```

대체로 다수의 항목을 가져오는건 단일 항목을 요청하는 것과 동일하다. 단지 차이점이라면 `bodyToMono()`를 사용해 응답 몸체를

Mono로 추출하는 대신 `bodyToFlux()`를 사용해 Flux로 추출하는 것이다.

#### 🏭 기본 URI로 요청하기

기본 URI는 서로 다른 많은 요청에서 사용할 수 있다. 이경우 기본 URI를 갖는 WebClient빈을 생성하고 어디든지 필요한곳에서 주입하는 것이 유용하다.

```
@Bean
public WebClient webClient(){
    return Webclient.create("http://localhost:8080");
}
```
그다음 이 기본 URI를 사용하는 요청을 하면 이 WebClient빈이 주입되어 다음과 같이 사용할 수 있다.
```
@Autowired
WebClient webClient;

public Mono<Ingredient> getIngredientById(String ingredientId){
    Mono<Ingredient> ingredient=webClient
        .get()
        .uri("/ingredients/{id}",ingredientId)
        .retrieve()
        .bodyToMono(Ingredient.class);
    ingredient.subscribe(i -> {...})
}
```

#### 🏭 오래 실행되는 요청 타임아웃시키기

느려 터진 네트워크나 서비스 때문에 클라이언트의 요청이 지체되는 것을 방지하기 위해 Flux나 Mono의 timeout()메소드를 사용해

데이터를 기다리는 시간을 제한할 수 있다.

```
Flux<Ingredient> ingredients=WebClient.create()
    .get()
    .uri("http://localhost:8080/ingredients")
    .retrieve()
    .bodyToFlux(Ingredient.class);

ingredients
    .timeout(Duration.ofSeconds(1))
    .subscribe(
        i -> {...},
        e->{
            //handle timeout error 
        }
    )
```

이렇게 timeout()으로 1초로 지정하면 해당 요청이 1초 미만으로 수행될 수 있다면 아무문제없다.

그러나 1초보다 더 오래걸리면 타임아웃되어 subscribe()의 두번째 인자로 지정된 에러 핸들러가 호출된다.

#### 🏭 리소스 전송하기

WebClient로 데이터를 전송하는 것은 데이터 수신과 별로 다르지않다. 예를들어 Mono< Ingredient>를 갖고있고, 

Ingredient객체를 포함하는 POST요청을 전송하고 싶다면 get()대신 post(), body()를 호출해 Mono를사용해 해당요청몸체에 넣는다는것만 지정하면된다.

```
Mono<Ingredient> ingredientMono=...;
Mono<Ingredient> result=webClient
    .post()
    .uri("/ingredients")
    .body(ingredientMono, Ingredient.class)
    .retrieve()
    .bodyToMono(Ingredient.class);

result.subscribe(i ->{...})
```

만일 전송할 Mono나 Flux가 없는 대신 도메인 객체가 있다면 `syncBody()`를 사용할 수 있다.

예를들어, Mono< Ingredient>대신 Ingredient객체를 요청 몸체에 포함시켜 전송하고 싶다면 다음과같다.
```
Ingredeint ingredient=...;
Mono<Ingredient> result=webClient
    .post()
    .uri("/ingredients")
    .syncBody(ingredient)
    .retrieve()
    .bodyToMono(Ingredient.class);
result.subscribe(i -> {...})
```
만일 POST요청 대신 PUT요청을하고싶다면 post()대신 put()을 하면된다. 일반적으로 PUT요청은 비어있는

응답 페이로드를 갖기때문에 따라서 Void타입의 Mono를 반환하도록 bodyToMono()에 Void.class를 인자로 전달하면 된다.

#### 🏭 리소스 삭제하기

WebClient는 또한 `delete()`메소드를 통해 리소스의 삭제를 허용한다. 예를들어 다음과같다
```
Mono<Void> result=webClient
    .delete()
    .uri("/ingredients/{id}", ingredientId)
    .retrieve()
    .bodyToMono(Void.class)
    .subscribe();
```
PUT요청처럼 DELETE요청도 응답 페이로드를 갖지않는다.

다시한번 중요해 말하지만 요청을 전송하려면 bodyToMono()에서 Mono< void>를 반환하고 subscribe()로 구독해야한다.

#### 🏭 에러 처리하기

WebClient는 또한 에러를 처리할때 `onStatus()`메소드로 상태코드를 지정할 수 있다.

`onStatus()`는 두 개의 함수를 인자로 받는다. 처리해야 할 HTTP 상태와 일치시키는데 사용되는 조건 함수와

`Mono<Throwable>`을 반환하는 함수다.
```
Mono<Ingredient> ingreedientMono= webClient
    .get()
    .uri("http://localhost:8080/ingredients/{id}",ingredientId)
    .retrieve()
    .bodyToMono(Ingredient.class);
```
### 만약 위의 코드에서 일치하는 식자재가 없다면 어떻게 될까??

에러가 생길 수 있는 Mono나 Flux를 구독할 때는 subscribe()메소드를 호출핧 때 데이터 컨슈머는 물론 에러 컨슈머도 등록하는 것이 중요하다
```
ingredientMono.subscribe(
    ingredient ->{
        //식자재 데이터를 처리한다
        ...
    }
    error ->{
        //에러를 처리한다
        ...
    });
```
이 경우 지정된 ID와 일치하는 식자재 리소스를 찾으면 subscribe()의 첫 번째 인자로 전달된 람다 상태코드를 갖게 되고,

두번 째 인자로 전달된 람다가 실행되어 기본적으로 `WebClientResponseException`을 발생시킨다

그러나 WebClientResponseException는 구체적인 예외를 나타내는 것이 아니므로 무엇이 잘못되었는지 정확히 알 수 없다.

이때 커스텀 에러 핸들러를 추가하면 HTTP 상태코드를 우리가 선택한 Throwable로 변환하는 실행 코드를

제공할수 있다. 예를들어 식자재 리소스의 요청에 실패했을 때 UnknownIngredientException 에러를

포함하는 Mono로 생성하고 싶다면 다음과 같이 retrieve()호출 다음에 onStatus()를 호출하면된다.
```
Mono<Ingredient> ingredientMono=webClient
    .get()
    .uri("http://localhost:8080/ingredients/{id}",ingredientId)
    .retrieve()
    .onStatus(HttpStatus::is4xxClientError,
        response -> Mono.just(new UnknownIngredientException()))
    .bodyToMono(Ingredient.class);
```

onStatus()의 첫 번째 인자는 HttpStatus를 지정하는 조건식이며, 우리가 처리를 원하는 HTTP상태 코드라면 true를 반환한다.

그리고 상태코드가 일치하면 두 번째 인자의 함수로 응답이 반환되고 이 함수에서는 Throwable타입의 Mono를 반환한다.

이 경우 HTTP 상태코드가 400 수준의 상태코드이면 UnknownIngredientException을 포함하는 Mono가 반환된다.

또한 다음과같이 HTTP 404 상태코드를 검사하도록 onStatus()호출을 변경할 수 있다.
```
Mono<Ingredient> ingredientMono = webClient
    .get()
    .uri("http://localhost:8080/ingredients/{id}",ingredientId)
    .retrieve()
    .onStatus(status -> status == HttpStatus.NOT_FOUND,
        response -> Mono.just(new UnknownIngredientException()))
    .bodyToMono(Ingredient.class);
```
응답으로 반환될 수 있는 다양한 HTTP상태 코드를 처리할 필요가 있을때는 onStatus()호출을 여러번 할 수 있다는것을 알아두자

#### 🏭 요청교환하기

지금껏 WebClient를 사용할때 retrieve()메소드를 사용해 요청의 전송을 나타냈다. 이때 retrieve()apthemsms ResponseSpec

타입의 객체를 반환하였으며, 이 객체를 통해서 onStatus(),bodyToFlux(), bodyToMono()와 같은 메소드를 호출하여

응답을 처리할 수 있었다. 간단한 상황에서는 ResponseSpec을 사용하는 것이 좋다 그러나 이경우는 몇가지 면에서 제한된다.!!!!

예를들어, 응답의 헤더나 쿠키값을 사용할 필요가 있을때는 ResponseSpec으로 처리할수없다

ResponseSpec이 기대에 미치지 못할 때는 retrieve()대신 exchange()를 호출할 수 있다.

exchane()메소드는 ClientResponse타입의 Mono를 반환한다. ClientResponse타입은 리액티브

오퍼레이션을 적용할 수있고 응답의 모든부분에서 데이터를 사용할 수 있다.
다음 두가지 코드를보자
```
//1...
Mono<Ingredient> ingredientMono=webClient
    .get()
    .uri("http://localhost:8080/ingredients/{id}",ingredientId)
    .exchange()
    .flatMap(cr -> cr.bodyToMono(Ingredient.class));

//2..
Mono<Ingredient> ingredientMono=webClient
    .get()
    .uri("http://localhost:8080/ingredients/{id}",ingredientId)
    .retrieve()
    .bodyToMono(Ingredient.class);
````

두코드의 차이점은 exchange()예시에서는 ResponseSpec객체의 bodyToMono()를 사용해
Mono< Ingredient>를 가져오는 대신,

매핑함수 중 하나인 flatMap()을 사용해서 ClientResponse를 Mono< Ingredient>와 연관시킬 수 있는 Mono< ClientResponse>를 가져온다

exchange()의 다른점을 알아보면 요청의 응답에 true값을 나타내는 X_UNAVAILABLE이라는 이름의 헤더가 포함될 수있다해보자.

그리고 X_UNAVAILABLE 헤더가 존재한다면 결과 Mono는 빈것이어야한다고 가정해보자 그러면 코드는 다음과같다
```
Mono<Ingredient> ingredientMono= webClient
    .get()
    .uri("http://localhost:8080/ingredient/{id}", ingredientId)
    .exchange()
    .flatMap(cr -> {
        if(cr.headers().header("X_UNAVAILABLE").contains("true)){
            return Mono.empty();
        }
        return Mono.just(cr);
    })
    .flatMap(cr -> cr.bodyToMono(Ingredient.class));
```

새로 추가된 flatMap()호출에서는 true값을 갖는 X_UNAVAILABLE헤더를 찾으면서 지정된 ClientRequest객체의 

헤더를 검사한다. 그리고 비어 있는 Mono를 반환하며 못찾으면 ClientResponse를 포함하는 새로운 Mono를 반환한다.

어떤 경우든 반환되는 Mono는 그다음의 flatMap()이 처리할 Mono가된다.

---

## 🏓 리액티브 웹 API 보안

스프링시큐리티의 웹 보안 모델은 서블릿 필터를 중심으로 만들어졌다.

만일 요청자가 올바른 권한을 갖고있는지 확인하기 위해 서블릿 기반 웹프레임워크의 요청바운드를 가로채야한다면

서블릿 필터가 확실한선택이다. 하지만 스프링 WebFlux에서는 이런방법이 곤란하다.

스프링 WebFlux애플리케이션의 보안에 서블릿 필터를 사용할수없다. 그러나 5버전부터 스프링 시큐리티는 서블릿기반의

스프링 MVC와 리액티브 스프링 WebFlux애플리케이션의 보안에 모두 사용될 수있다.

스프링의 `WebFilter`가 이일을 해준다. `WebFilter`는 서블릿 API에 의존하지않는 스프링 특유의 서블릿 필터같은것이다.

하지만 더 놀라운 사실은 리액티브 스프링 시큐리티 구성모델이 일반 스프링 시큐리티와 크게 다르지않다는것이다.

실제로 스프링 MVC와 다른 의존성을 갖는 스프링WebFlux와 다르게, 스프링 시큐리티는 스프링 MVC와 동일한

스프링 부트 보안 스타터를 사용한다.

### 🌟 리액티브 웹 보안 구성하기

스프링 MVC 웹 애플리케이션의 보안을 구성할 때는 `WebSecurityConfigurerAdapter`의 서브 클래스로 새로운 구성 클래스를

생성하며 이 클래스에는 `@EnableWebSecurity`애노테이션을 지정한다. 그리고 이 구성 클래스에는

`configuration()` 메소드를 오버라이딩하여 요청 경로에 필요한 권한 등과 같은 웹 보안 명세를 지정한다.

스프링 MVC보안의 예시이다
```
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter{

    @Override
    protected void configure(HttpSecurity http) throws Exception{
        http
            .authorizeRequests()
            .antMatchers("/design","/orders").hasAuthority("USER")
            .antMatchers("/**").permitAll();
    }
}
```

다음은 스프링 WebFlux애플리케이션에서의 스프링시큐리티 구성이다.
```
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig{
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http){
        return http
            .authorizeExchange()
            .pathMatchers("/design","/orders").hasAuthority("USER")
            .anyExchange().permitAll()
            .and()
            .build();
    }
}
```

보면 알 수 있듯이, `@EnableWebSecurity`대신 `@EnableWebFluxSecurity`가 지정되어 있다.

게다가 구성 클래스가 WebSecurityConfigurerAdpater의 서브클래스도 아니며, 다른 베이스 클래스로부터 

상속 받지도 않는다. 따라서 configure()메소드도 오버라이딩 하지 않는다.

그리고 `configure()`메소드를 대신해서 `securityWebFilterChain()`메소드를 갖는 SecurityWebFilterChain 

타입의 빈을 선언한다. SecurityWebFilterChain()메소드 내부의 실행코드는 configure()메소드와

크게 다르지않지만 변경된 것이 있다. 우선, `HttpSecurity`객체 대신 `ServerHttpSecurity`객체를 사용해 구성을 선언한다.

그리고 인자로 전달된 `ServerHttpSecurity`를 사용해 `authorizeExchange()`를 호출할 수 있다.

이 메소드는 요청 수준의 보안을 선언하는 `authorizeRequest()`와 거의같다.

경로 일치 확인의 경우에 여전히 Ant 방식의 와일드카드 경로를 사용할 수 있지만, 메소드는 `antMatchers()`대신 `pathMathcers()`

를 사용한다. 그리고 모든 경로를 의미하는 Ant방식의 /**을 더이상 지정할 필요 없다.

`anyExchange()`메소드가 /**를 반환하기 때문이다. 끝으로 프레임워크 메소드를 오버라이딩하는 대신

`SecurityWebFilterChain`을 빈으로 선언하므로 반드시 build()메소드를 호출해 모든 보안규칙을

`SecurityWebFilterChain`으로조립하고 반환해야한다.

### 🌟 리액티브 사용자 명세 서비스 구성하기
`WebSecurityConfigurerAdpater`의 서브클래스로 구성 클래스를 작성할 때는 하나의 configure()메소드를

오버라이딩하여 웹 보안 규칙을 선언하며, 또 다른 configure()메소드를 오버라이딩 하여 UserDetails객체로

정의하는 인증로직을 구성한다. 어떻게하는지 다시 알아볼 겸 다음의 오버라이딩된 configure()메소드를 보자.

이 메소드 내부에서는 주입된 UserRepository객체를 UserDetailsService에서 사용하여 사용자 이름으로 사용자를 찾는다.
```
@Autowired
UserRepository userRepo;

@Override
protected void configure(AuthenticationManagerBuilder auth) throws Exception{
    auth
        .userDetailsService(new UserDetailsService(){
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
                User user=userRepo.findByUsername(username);
                if(user==null){
                    throw new UsernameNotFOundException(username + "not found);
                }
                return user.toUserDetails();
            }
        });
}
```
위와 같이 리액티브가 아닌 구성에서는 UserDetailsService에서 필요한 loadUserByUsername()메소드만 오버라이딩한다.

그리고 이 메소드 내부에서는 지정된 UserRepository를 사용해서 인자로 전달된 사용자 이름으로 사용자를 찾는다.

만일 해당 이름을 못찾으면 UsernameNotFoundException을 발생시킨다. 그러나 찾으면 toUser Details()를 호출하여

UserDetails 객체를 반환한다. 

그러나 리액티브 보안 구성에서는 configure()메소드를 오버라이딩하지 않고 대신에 `ReactiveUserDetailsService`빈을

선언한다. 이것은 UserDetialsService의 리액티브 버전이며 UserDetailsService처럼 하나의 메소드만

구현하면 된다. 특히 findByUsername()메소드는 UserDetails 객체 대신 Mono< userDetails>를 반환한다.

다음 예에서는 인자로 전달된 UserRepository를 사용하기 위해 ReactiveUserDetialsService 빈이 선언되었다.
```
@Service
public ReactiveUserDetailsService userDetailsService(UserRepository userRepo){
    return new ReactiveUserDetailsService(){
        @Override
        public Mono<UserDetails> findByUsername(String username){
            return userRepo.findByUsername(username)
                .map(user ->{
                    return user.toUserDetails();
                });
        }
    };
}
```

여기서 UserRepository의 findByUsername()메소드는 Mono< User>를 반환한다.

따라서 Mono타입에 사용 가능한 오퍼레이션들을 연쇄적으로 호출할 수있다.

여기서는 map()오퍼레이션의 인자로 람다를 전달하여 호출하며, 이 람다에서는 UserRepository.findByUsername()

에서 반환된 Mono가 발행하는 User객체의 toUserDetails()메소드를 호출한다. 

그리고 이 메소드는 User객체를 UserDetails객체로 변환한다. 따라서 map()오퍼레이션에서

반환하는 타입은 Mono< UserDetails>가 된다. 이것이 Reactive UserDetailsService.findByUsername() 에서 요구하는 반환타입이다.

# 10장 요약
#### [1] 스프링 WebFlux는 리액티브 웹 프레임워크를 제공한다. 이 프레임워크의 프로그래밍 모델은
#### 스프링 MVC가 많이 반영되었다. 심지어는 애노테이션도 많은것을 공유한다.

#### [2] 스프링5는 또한 스프링 WebFlux의 대안으로 함수형 프로그래밍 모델을 제공한다
#### [3] 리액티브 컨트롤러는 WebTestClient를 사용해 테스트할 수 있다
#### [4] 클라이언트 측에는 스프링5가 스프링 RestTemplate의 리액티브 버전인 WebClient를 제공한다
#### [5] 스프링 시큐리티 5는 리액티브 보안을 지원하며 이것의 프로그래밍 모델은 리액티브가 아닌
#### 스프링 MVC애플리케이션의 것과 크게 다르지 않다





