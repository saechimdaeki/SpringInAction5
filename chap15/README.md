# 15장 실패와 지연 처리하기

## 🏓 이 장에서 배우는 내용
- 서킷 브레이커 패턴 개요
- Hystrix로 실패와 지연 처리하기
- 서킷 브레이커 모니터링
- 서킷 브레이커 메트릭 종합하기

### 🌟 서킷 브레이커 이해하기

서킷 브레이커 패턴은 우리가 작성한 코드가 실행에 실패하는 경우에 안전하게 처리되도록 해준다. 이 강력한 패턴은 마이크로서비스의

컨텍스트에서 훨씬 더 중요하다. 한 마이크로서비스의 실패가 다른 마이크로서비스의 연쇄적인 실패로 확산되는 것을 방지해야하기 때문이다.

서킷 브레이커는 메서드의 호출을 허용하며, 서킷은 닫힘상태에서 시작된다. 그리고 어떤 이유로든 메소드의 실행이 실패하면 

서킷브레이커가 개방되고 실패한 메소드에 대해 더이상 호출이 수행되지 앟는다. 그러나 서킷브레이커는 폴백을 제공하여

자체적으로 실패를 처리한다. 다음그림은 서킷브레이커의 처리흐름을 보여준다.

![image](https://user-images.githubusercontent.com/40031858/109584732-73c23780-7b45-11eb-97db-33b02d64c66a.png)

서킷 브레이커로 보호되는 메소드가 실행에 성공하면 서킷은 닫힘상태가 유지되고 이후에도 해당 메소드가 실행된다.

그러나 서킷 브레이커로 보호되는 메소드가 실행에 실패하면, 서킷은 열림 상태가 되고 이후에는 실패한 메소드 대신 폴백 메소드가 호출된다.

그러다가 때떄로 서킷이 절반-열림상태로 바뀌면서 실패했던 메소드의 호출을 서킷 브레이커가 다시 시도한다.

그러나 여전히 실패하면 서킷은 다시 열림상태가 되고, 이후에는 다시 폴백 메소드가 호출된다. 하지만 성공하면 문제가 해결된 것으로 간주해 서킷은 닫힘상태가된다.

서킷 브레이커를 더 강력한 형태의 `try/catch`라고 생각하면 이해하는데 도움이될수있다. 닫힘상태는 `try`블록과 유사한 반면 폴백 메소드는 `catch`블록과 유사하다.

그러나 `try/catch`와 다르게 서킷브레이커는 원래 호출하려던 메소드가 너무 자주실패하면 폴백 메소드를 호출한다.

서킷 브레이커는 메소드에 적용된다. 따라서 하나의 마이크로서비스에 많은 서킷브레이커가 있을 수 있다. 그러므로 코드의 어디에 서킷브레이커를

선언할지 결정할 때는 실패의 대상이 되는 메소드를 식별하는것이 중요하다. 대개는 다음 유형의 메소드들이 서킷브레이커를 선언할 후보들이다.
- `REST를 호출하는 메소드`: 사용할 수 없거나 HTTP 500응답을 반환하는 원격서비스로 인해 실패할수있는메소드
- `데이터베이스 쿼리를수행하는 메소드`:어떤이유로든 데이터베이스가 무반응 샅애가 되거나, 애플리케이션을 중단시킬 수 있는 스키마의 변경이 생기면 실패할수있는메소드
- `느리게 실행될 가능성있는 메소드`: 이것은 반드시 실패하는 메소드가아니라 너무 오랫동안 실행된다면 비정상적 상태를 고려할수있다.

첫번째와 두번째 유형의 메소드는 서킷브레이커의 실패처리로 해결할 수 있지만 마지막 유형의 메소드는 실패보다는 지연이 문제된다.

이경우에도 서킷브레이커의 또 다른 장점을 살릴수 있는데 지나치게 느린 메소드가 상위 서비스에 연쇄적인 지연을 유발하여

마이크로서비스의 성능을 저하하지않게 하는게 중요하기때문이다.  서킷브레이커 패턴은 코드의 실패와 지연을 처리하는 강력한 수단이다.

어떻게 우리코드에 서킷브레이커를 적용할 수 있을까? 다행히 Netflix오픈소스프로젝트가 `Hystrix`라이브러리를 사용해 답을제공한다


`Netflix Hystrix`는 서킷브레이커 패턴을 자바로 구현한 라이브러리다. 간단히 말해 `Hystrix`서킷 브레이커는 대상 메소드가

실패할 때 폴백 메소드를 호출하는 어스펙트로 구현된다. 그리고 서킷 브레이커 패턴을 제대로 구현하기 위해

어스펙트는 대상 메소드가 얼마나 자주실패하는지 추적한다 그다음 실패율이 한계값을 초과하면 모든 대상 메소드 호출을 폴백메소드호출로 전달한다

## 🍰 서킷 브레이커 선언하기

서킷 브레이커를 선언하기 앞서 스프링 클라우드 Netflix Hystrix스타터를 각 서비스의 빌드에 추가해야한다
```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
</dependency>
```

이제 Hystrix 스타터 의존성이 추가되었으므로 다음은 Hystrix를 활성화햐애한다. 이때는 각 애플리케이션의 

메인 구성 클래스에 `@EnableHyhstrix` 애노테이션을 지정하면 된다. 예를들어, IngredientServiceApplication에 다음과같이 지정한다

```
@SpringBootApplication
@EnableHystrix
public class IngredientServiceApplication{
    ...
}
```

이제 Hystrix가 애플리케이션에 활성화되었다. 그러나 아직 어떤 메소드에도 서킷 브레이커가 선언되지않았다. 이때 `@HystrixCommand`애노테이션이 필요하다.

어떤 메소드이건 `@HystrixCommand`가 지정되면 서킷브레이커가 적용된다. 예를들어 다음메소드를보자

이 메소드는 RestTemplate을 사용해서 식자재 서비스로부터 Ingredient객체들이 저장된 컬렉션을 가져온다

```
public Iterable<Ingredient> getAllIngredients(){
    ParameterizedTypeReference<List<Ingredient>> stringList=new ParameterizedTypeReference<List<Ingredient>>(){};

    return rest.exhange(
        "http://ingredient-service/ingredients",HttpMethod.GET,
        HttpEntity.EMPTY,stringList).getBody();
}
```

여기서 exchange()의 호출이 문제를 유발할수있는 잠재적 원인이다. exchange()메소드 내부를 보면 유레카에 ingredient-service로

등록된 서비스가 없거나 요청이 실패하면 Exception이 발생한다. 이 메소드는 try/catch 블록으로

예외를 처리하지 않으므로 exchange()를 호출한 호출자에서 예외를 처리해야한다. 그러나 호출자에서도 이 예외를 처리하지않는다면 

계속 예외가전파될것이다. 이처럼 처리되지않은 예외는 어떤 애플리케이션에서도 골칫거리이며 특히 마이크로서비스의 경우에그렇다.

장애가 생기면 마이크로서비스는 베가스규칙을 적용해야한다. 즉, 에러는 다른곳에 전파하지않고 마이크로서비스에 남는다는 애기다.

getAllIngredients()메소드에 서킷 브레이커를 선언하면 이런 규칙을 충족시킨다. 서킷브레이커를 선언할 떄는

`@HystrixCommand`를 메소드에 지정하고 폴백메소드를 제공하면된다. 우선 `@HystrixCommand`를 추가하자
```
@HystrixCommand(fallbackMethod="getDefaultIngredients")
public Iterable<Ingredient> getAllIngredients(){
    ...
}
```

이제 서킷브레이커가 실패로부터 보호해주므로 getAllIngredients()메소드에는 안전장치가 되었다. 따라서 어떤 이유로든 예외가 발생하여

getAllIngredients()로부터 벗어나면 서킷브레이커가 해당 예외를 잡아서 폴백메소드인 getDefaultIngredients()를 호출해준다

따라서 getDefaultIngredients()메소드에는 매개변수가 없고 List< Ingredient>를 반환해야한다.
```
private Iterable<Ingredient> getDefaultIngredients(){
    List<Ingredient> ingredients=new ArrayList<>();
    ingredients.add(new Ingredient("FLTO","FLour Tortilla", Ingredient.Type.WRAP));
    ingredients.add(new Ingredient("GRBF","Ground Beef", Ingredient.Type.PROTEIN));
    ingredients.add(new Ingredient("CHED","Shredded Cheddar",Ingredient.Type.CHEESE));

    return ingredients;
}
```

이제는 어떤 이유로든 getAllIngredients()가 실행에 실패하면 서킷 브레이커가 getDefaultIngredients()를 호출해 주며,

getAllIngredients()의 호출자는 기본식자재 세개가 저장된 List를받는다

그런데 폴백 메소드 자신도 서킷 브레이커를 가질 수 있는지 궁금할것이다. 여기서 getDefaultIngredients()는 실행에 실패할 일 없다 그러나

getDefaultIngredients()를 다르게 구현한다면 잠재적 장애점이 될 수 있다. 이경우 getDefaultIngredients()에 @HystrixCommand

를 지정하여 또 다른 폴백 메소드를 제공할 수 있다. 필요하다면 이런식으로 폴백메소드를 연쇄적으로 지정할 수 있다. 

단, 한가지 제약이 있는데 폴백 스택의 제일 밑에는 실행에 실패하지 않아 서킷브레이커가 필요없는 메소드가 있어야한다

### 🌟 지연시간 줄이기

서킷 브레이커는 메소드의 실행이 끝나고 복귀하는 시간이 너무 오래 걸릴 경우 타임아웃을 사용해 지연시간을 줄일 수 있다.

기본적으로 `@HystrixCommand`가 지정된 모든 메소드는 1초 후에 타임아웃되고 이 메소드의 폴백 메소드가 호출된다.

즉 어떤이유로든 서비스 응답이 느려져 getAllIngredients()에 대한 호출이 1초 후에 타임아웃되면 getDefaultIngredients()가 대신호출된다.

타임아웃을 변경하려면 `@HystrixCommand`애노테이션의 `commandProperties` 속성을통해 Hystrix명령 속성을 설정할 수 있다.

`commandProperties`속성은 설정될 속성의 이름과 값을 지정하는 하나이상의 `@Hystrix Proeprty`애노테이션을 저장한 배열이다.

예를들어 getAllIngredients()메소드의 타임아웃을 0.5초로 줄일때는 다음과같이한다.
```
@HystrixCommand(
    fallbackMethod="getDefaultIngredients",
    commandProperties={
        @HystrixProperty(
            name="execution.isolation.thread.timeoutInMilliseconds",
            value="500")
})
public Iterable<Ingredient> getAllIngredients(){
    ...
}
```

타임아웃으로 지정되는 값의 단위느 1/1000초이며 시간을 늘리려면 더 큰값으로 설정하면된다. 또 타임아웃이 필요없을 때는

명령 속성인 execution.timeout.enabled를 false로 설정해 타임아웃을 없앨수있다.
```
@HystrixCommand(
    fallbackMethod="getDefaultIngredients",
    commandProperties={
        @HystrixProperty(
            name="execution.timeout.enabled",
            value="false")
    })
public Iterable<Ingredient> getAllIngredients(){
    ...
}
```


execution.timeout.enabled 속성이 false로 설정되면 보호되지 않으므로, getAllIngredients()메소드의 실행시간이

1초, 10초,30분이걸려도 타임아웃되지않는다. 따라서 연쇄지연효과가 발생할수있으므로 조심해야한다.

### 🌟 서킷 브레이커 한계값 관리하기

만일 서킷 브레이커로 보호되는 메소드가 10초 동안에 20번이상 호출되고 이 중 50%이상 실패한다면 이 서킷은 열림상태가된다.

또한 이후의 모든 호출은 폴백 메소드에 의해 처리된다. 그리고 5초후 이서킷은 절반-열림 상태가 되어 원래의 메소드 호출이 다시시도된다.

Hystrix명령 속성을 설정하면 실패와 재시도 한계값을 변경할 수 있다. 서킷 브레이커의 상태 변화를 초래하는 조건에

영향을 주는 명령 속성들은 다음과 같다.
- `circuitBreaker.requestVolumeThreshold`: 지정된 시간내에 메소드가 호출되어야 하는 횟수
- `circuitBreaker.errorThresholdPercentage`: 지정된 시간 내에 실패한 메소드 호출의 비율(%)
- `metrics.rollingStats.timeInMilliseconds`: 요청 횟수와 에러 비율이 고려되는 시간
- `circuitBreaker.sleepWindowInMilliseconds`: 절반-열림 상태로 진입하여 실패한 메소드가 다시 시도되기 전에 열림상태의 서킷이 유지되는 시간

예를들어 , 실패 설정을 다음과같이 조정하고 싶다고 하자. 즉, 20초 이내에 메소드가 30번 이상 호출되어 이 중에서

25%이상이 실패일경우다. 이때는 다음과 같이 속성을 설정한다

```
@HystrixCommand(
    fallbackMethod="getDefaultIngredients",
    commandProperties={
        @HystrixProperty(
            name="circuitBreaker.requestVolumeThreshold",
            value="30"),
        @HystrixProeperty(
            name="circuitBreaker.errorThresholdPercentage",
            value="25"),
        @HystrixProperty(
            name="metrics.rollingStats.timeInMilliseconds",
            value="20000")
})
public List<Ingredient> getAllIngredients(){
    //...
}
```

또한, 서킷 브레이커가 절반-열림 상태가 되기 전에 1분까지 열림상태로 머물러야한다면 다음속성을 설정할 수도있다.
```
@HystrixCommand(
    fallbackMethod="getDefaultIngredients",
    commandProperties={
        ...
        @HystrixProeprty(
            name="circuitBreaker.sleepWindowInMilliseconds",
            value="60000")
})
```

## 🏓 실패 모니터링 하기
서킷 브레이커로 보호되는 메소드가 매번 호출할때마다 해당 호출에 관한 여러 데이터가 수집되어 Hystrix스트림으로 발행된다.

그리고 이 Hystrix스트림은 실행중인 애플리케이션의 건강 상태를 실시간으로 모니털이하는데 사용할 수 있다.

각 서킷브레이커로부터 수집한 데이터 중에서 Hystrix스트림은 다음을포함한다.
- 메소드가 몇 번 호출되는지
- 성공적으로 몇 번 호출되는지
- 폴백 메소드가 몇 번 호출되는지
- 메소드가 몇 번 타임아웃되는지

이 Hystrix스트림은 엑추에이터 엔드포인트로 제공된다. 모든서비스들이 Hystrix스트림을 활성화하려면 엑추에이터

의존성을 빌드에 추가해야한다 다음과같다

```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

Hystrix스트림 엔드포인트는 /actuator/hystrix.stream 경로로 노출되어 있다. 대부분의 엑추에이터 엔드포인트는 기본적으로

비활성화 되어 있다. 그러나 각애플리케이션의 application.yml파일에 다음 구성을 추가하면 Hystrix 스트림 엔드포인트를 활성화할수있다

```
management:
  endpoints:
    web:
      exposure:
        include:hystrix.stream
```

애플리케이션이 시작되면 Hystrix 스트림이 노출된다. 따라서 어떤 REST 클라이언트를 사용해도 Hystrix 스트림을 소비할 수 있다.

하지만 커스텀 Hystrix스트림 클라이언트의 작성을 시작하기전 알아둘 것이있다. Hystrix 스트림의 각 항목은 온갖 JSON데이터로

가득 차 있으므로 이 데이터를 해석하기 위해 클라이언트 측의 작업이 많이필요하다 .

### 🌟 Hystrix대시보드 개요

Hystrix대시보드를 사용하려면 우선 Hystrix 대시보드 의존성을 다음과 같이 추가하면된다.
```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-hystrix-dashboard</artifactId>
</dependency>
```

그다음 Hystrix대시보드를 활성화 하기위해 메인 구성 클래스에 `@EnableHystrixDashboard`애노테이션을 지정해야한다

```
@SpringBootApplication
@EnableHystricxDashboard
public class HystrixDashboardApplication{
    public static void main(String[] args){
        SpringApplication.run(HystrixDashboardApplication.class,args);
    }
}
```

이제 이를 7979포트로 설정해놨다면 http://localhost:7979/hystrix에 접속하면 대시보드 홈페이지가 나타난다.

이제 여기서 서킷브레이커 모니터를 보면  말로만설명하자면 다음과같다.

모니터의 왼쪽 위 모서리의 그래프는 지정된 메소드의 지난 2분동안의 트래픽을 나타내며, 메소드가 얼마나

바쁘게 실행되었는지 간략하게 보여준다. 또, 그래프의 배경에는 크기와 색상이 수시로 변동되는 원이있다. 원의 크기는

현재의 트래픽 수량을 나타내며, 트래픽 수량이 증가하면 원이 커진다. 원의 색상은 해당 서킷브레이커의 건강 상태를 나타낸다.

모니터의 오른쪽 위에는 다양한 카운터를 세 열로 보여준다. 왼쪽 열의 위에서부터 첫번째 번호는 성공한 호출횟수,

두번째 번호는 숏-서킷요청횟수, 마지막번호는 잘못된 요청의 횟수를 나타낸다. 중간열의 제일 위 번호는 타임아웃된

요청횟수, 그 아래번호는 스레드 풀이 거부한 횟수, 제일 아래 번호는 실패한 요청횟수를 나타낸다. 그리고 제일 오른ㅉ고 열은

지난 10초간 에러 비율을나타낸다.

## 🍰 다수의 Hystrix 스트림 종합하기
Hystrix 대시보드는 한번에 하나의 Hystrix 스트림만 모니터링 할 수 있다. 애플리케이션에 있는 마이크로서비스 인스턴스는 자신의

Hystrix스트림만을 발행하므로 애플리케이션 전체의 건강 상태 정보를 얻는것은 불가능하다.

그러나 다행히도 또다른 Netflix프로젝트인 Turbine이 모든 마이크로서비스로부터 모든 Hystrix스트림을 Hystrix대시보드가 모니터링할

수있는 하나의 스트림으로 종합하는 방법을 제공한다. 스프링 클라우드 Netflix는 다른 스프링 클라우드 서비스 생성과

유사한 방법을 사용해서 Turbine 서비스의 생성을 지원한다 Turbine서비스를 생성하려면 새로운 스프링 부트 프로젝트를

생성하고 Turbine스타터 의존성을 빌드에 포함시켜야한다.
```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-turbine</artifactId>
</dependency>
```

프로젝트가 생성되었으면 Turbine을 활성화해야한다. 이때는 애플리케이션의 메인 구성 클래스에 `@EnableTurbine`애노테이션을 지정한다.

```
@SpringBootApplication
@EnableTurbine
public class TurbineServerApplication{
    public static void main(String[] args){
        SprintApplication.run(TurbineServerApplication.class,args);
    }
}
```

이제 다수의 마이크로서비스로부터 Hystrix스트림이 소비되면 서킷 브레이커 메트릭들이 Turbine에 의해 하나의 Hystrix스트림으로 종합될 것이다.

Turbine은 유레카 클라이언트에 의해 작동하므로 Hystrix스트림을 종합할 서비스들을 유레카에서 차즌ㄴ다. 

그러나 유레카에 등록된 모든 서비스의 Hystrix스트림을 종합하지는 않는다. 따라서 Hystrix스트림을 종합할 서비스들을 알 수 있게

Turbine을 구성해야한다. 이때 turbine.app-config속성을 설정한다.

turbine.app-config속성에는 Hystrix스트림을 종합하기 위해 유레카에서 찾을 서비스 이름들을 설정한다. 
```
turbine:
  app-config: ingredient-service,taco-service,order-service,user-service
  cluster-name-expression: "'default'"
```

turbine.app-config 속성에 추가하여 turbine.cluster-nameexpression속성도 'default'로 해야한다.

이것은 이름이 default인 클러스터에 있는 모든 종합될 스트림을 Turbine이 수집해야한다는것을 나타낸다. 이 클러스터 이름을

설정하는 것은 중요하다 만일 설정하지않으면 지정된 애플리케이션들로부터 종합될 어떤 스트림 데이터도 

Turbine스트림에 포함되지 않기때문이다. 이제는 Hystrix대시보드가 마이크로서비스에 있는 모든 서킷 브레이커의 건강상태 정보를 보여준다

## 프로젝트 실행방법(순서대로)
```
./mvnw clean package (service-registry)
./mvnw clean package (config-server)
./mvnw clean package (hystrix-dashboard)
./mvnw clean package (turbine-server)
./mvnw clean package (ingredient-service)
./mvnw clean package (ingredient-client)


java -jar service-registry-0.0.1-SNAPSHOT.jar
java -jar config-server-0.0.1-SNAPSHOT.jar
java -jar hystrix-dashboard-0.0.1-SNAPSHOT.jar
java -jar turbine-server-0.0.1-SNAPSHOT.jar
java -jar ingredient-service-0.0.1-SNAPSHOT.jar
java -jar ingredient-client-0.0.1-SNAPSHOT.jar
```

# 15장 요약
### [1] 서킷 브레이커 패턴은 유연한 실패 처리를 할 수 있다.
### [2] Hystrix메소드가 실행에 실패하거나 너무 느릴 때 폴백 처리를 활성화하는 서킷 브레이커패턴을 구현한다
### [3] Hystrix가 제공하는 각 서킷 브레이커는 애플리케이션의 건강상태를 모니터링할 목적

### 으로 Hystrix스트림의 메트릭을 발행한다

### [4] Hystrix스트림은 Hystrix 대시보드가 소비할 수 있다. Hystrix대시보드는 서킷 브레이커

### 메트릭을 보여주는 웹 애플리케이션ㅇ니다

### [5] Turbine은 여러 애플리케이션의 Hystix스트림들을 하나의 Hystrix 스트림으로 종합하며

### 종합된 Hystrix스트림은 Hystrix대시보드에서 볼 수 있다.