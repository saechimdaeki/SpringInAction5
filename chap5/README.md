# 5장 구성 속성 사용하기
### 이 장에서 배우는 내용
- 자동-구성되는 빈 조정하기
- 구성 속성을 애플리케이션 컴포넌트에 적용하기
- 스프링 프로파일 사용하기

스프링 부트는 `구성속성(configuration property)`을 사용하는 방법을 제공한다

스프링 애플리케이션 컨텍스트에서 구성 속성은 빈의 속성이다. 그리고 JVM시스템 속성, 명령행 인자, 환경변수

등의 여러 가지 원천 속성중에서 설정할 수 있다.

## 자동-구성 세부 조정하기

#### 구성 속성을 더 자세히 알아보기에 앞서, 스프링에는 다음 두가지 형태의 서로다르면서도 관련이 있는 구성이있다.
- 빈연결: 스프링 애플리케이션 컨텍스트에서 빈으로 생성되는 애플리케이션 컴포넌트 및 상호간주입되는 방법을 선언하는구성
- 속성주입: 스프링 애플리케이션 컨텍스트에서 빈의 속성값을 설정하는 구성

자바 기반 구성에서 `@Bean`애노테이션이 지정된 메소드는 사용하는 빈의 인스턴스를 생성하고 속성값도 설정한다.

예를들어 스프링에 내장된 H2 데이터베이스를 DataSource로 선언하는 메소드를보자
```
@Bean
public DataSource dataSource(){
    return enw EmbeddedDatabaseBuilder()
    .setType(EmbeddedDatabaseType.H2)
    .addScript("schema.sql")
    .addScripts("user_data.sql", "ingredient_data.sql")
    .build();
}
```

여기서 EmbeddedDatabaseBuilder는 내장 데이터베이스를 구성하는 클래스이며 addScript()와 addScripts 메소드

는 하나 또는 여러개의 속성을 SQL 스크립트 파일의 이름으로 설정한다. 이 경우 해당 DataSource가 

준비되면 각 스크립트의 SQL이 실행되어 데이터베이스에 적용된다. 만일 스프링부트 사용중이 아니라면

이 메소드는 DataSource빈을 구성할 수 있는 방법이된다.

그러나 스프링부트를 사용할때는 자동-구성이 DataSource빈을 구성해주므로 메소드가 필요없다.


### 스프링 환경 추상화 이해하기

`스프링 환경 추상화`는 구성 가능한 모든 속성을 한 곳에서 관리하는 개념이다. 

즉 속성의 근원을 추상화하여 각 속성을 필요로 하는 빈이 스프링 자체에서 해당 속성을 사용할수있게해준다.

스프링 환경에서는 당므과 같은 속성의 근원으로부터 원천속성을가져온다
- JVM시스템속성
- 운영체제의 환경변수
- 명령행 인자
- 애플리케이션의 속성 구성파일

그런다음 스프링환경에서는 이 속성들을 한 군데로 모은 후 각 속성이 주입되는 스프링 빈을 사용할수있게해준다.

![image](https://user-images.githubusercontent.com/40031858/107725236-25313280-6d29-11eb-9d33-487be52ae047.png)

스프링 부트에 의해 자동으로 구성되는 빈들은 스프링 환경으로부터 가져온 속성들을 사용해서 구성될 수있다.

간단한예로 서블릿컨테이너가 8080기본 포트가 아닌 다른포트로 작동하게 한다면 다음과 같이 다른 포트값을

갖는 server,port속성을 application.properties 파일에 지정하면된다.

```
server.port=9090


//ymaml사용시
server:
    port: 9090
```

또한 애플리케이션 실행시 명령형 인자로 속성을 지정할 수 도있다

    $ java -jar tacocloud-0.0.1-SNAPSHOT.jar --server.port=9090


### 데이터소스 구성하기

데이터소스의 경우는 우리 나름의 DataSource 빈을 명시적으로 구성할 수 있다. 그러나 스프링 부트 사용

시 그럴 필요는없으며 대신 구성속성을 통해해당 데이터베이스의 URL과 인증을 구성하는것이 간단하다.

예를들어 mysql을사용한다면 다음과 같이 application.yml에 추가하면된다.

```
spring:
    datasource:
        url: jdbc:mysql://localhost/tacocloud
        username: tacodb
        password: tacopassword
```

그다음 적합한 JDBC드라이버를 추가해야하지만 구체적인 JDBC드라이버 클래스를 지정할 필요는없다

스프링 부트가 데이터베이스 URL로부터 찾을 수 있기 때문이다. 그러나 문제가 생긴다면

    spring.datasource.driver-class-name속성을 설정하면된다.

그러면 이 DataSource 빈을 자동-구성할때 스프링 부트가 이런 속성설정을 연결데이터로 사용한다.

또한 톰캣의 JDBC커넥션 풀을 classpath에서 자동으로 찾을 수 있다면 DataSource빈이 그것을 사용한다.

그러나 그렇지 않다면 스프링 부트는 당므 중 하나의 다른 컬렉션 풀을 classpath에서 찾아사용한다
- HikariCP
- Commons DBCP 2

이것이 스프링 부트의 자동-구성을 통해서 사용가능한 커넥션 풀이다. 

그러나 우리가 원하는 DataSource빈을 명시적으로 구성하면 어떤 커넥션 풀도 사용할 수 있다.

애플리케이션이 시작될 때 데이터베이스를 초기화하는 SQL스크립트의 실행방법을 이야기했는데 다음과같이

spring.datasource.schema와 spring.datasource.data속성을 사용하면 더 간단하게 지정할수있다.

```
spring:
    datasource:
        schema:
            - order-schema.sql
            - ingredient-schema.sql
            - taco-schema.sql
            - user-schema.sql
        data:
            - ingredient.sql
```

또는 명시적인 데이터 소스 구성대신 JNDI에 구성하는 것을 원할수도있다. 이때는 다음과같이 구성하면된다.
```
spring:
    datasource:
        jndi-name: java:/comp/env/jdbc/tacoCloudDS
```
단, spring.datasource.jndi-name 속성을 설정하면 기존에 설정된 다른 데이터 소스구성속성은 무시된다.

### 내장 서버 구성하기

server.port를 0으로 설정하면 어떻게될까?
```
server:
    port: 0
```
이처럼 우리가 server.port를 0으로 설정하더라도 서버는 0번 포트로 시작하지않고

대신 사용 가능한 포트를 무작위로 선택하여 시작된다. 이것은 자동화된 통합테스트를 수행할때 유용하다.

동시적으로 실행되는 어떤 테스트도 같은 포트번호로 인한 충돌이 생기지 않기 때문이다. 이것은 

또한 마이크로서비스와 같이 애플리케이션이 시작되는 포트가 중요하지않을때도 ㅇ퓨용하다.

서버와 관련해서 포트외에 중요한것이 더있는데 그중 하나가 `HTTPS요청` 처리를 위한 컨테이너 관련설정이다

이때는 JDK의 keytool 명령행 유틸리티를 사용해서 키스토어를 생성하는것이 가장 먼저 할일이다

```
$ keytool -keystore mykeys.jks -genkey -alias tomcat -keyalg RSA
```

keytool이 실행되면 저장 위치 등의 여러정보를 입력받는데, 무엇보다 우리가 입력한 비밀번홀르

잘 기억해두는 것이 중요하다. 키스토어 생성이 끝난후 내장서버의 HTTPS를 활성화하기 위해 몇가지 속성을

설정해야한다. 이 속성들은 모두 명령행에 지정할 수 있다. 하지만 불편하기에 다음과같이 설정하는게 좋다

```
server:
    port: 8443
    ssl:
        key-store: file://path/to/mykeys.jks
        key-store:password: letmein
        key-password: letmein
```

여기서 server.port 속성은 8443으로 설정되었다. 이값은 개발용 HTTPS서버에 많이 사용된다.

server.ssl.key-store 속성은 키스토어 파일이 생성된 경로로 설정되어야한다.

### 로깅 구성하기

기본적으로 스프링 부트는 INFO수준으로 로그메시지를 쓰기위해 Logback을 통해 로깅을 구성한다.

로깅 구성을 제어할 때는 classpath의 루트에 logback.xml 파일을 생성할 수 있다. 간단히 사용할 수 있는

logback.xml파일의 예는 다음과같다

```
<configuration>
    <appender name="STDOUT" class="ch.qos.core.ConsoleAppender">
        <encoder>
            <pattern>
                %d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
            </pattern>
        </encoder>
    </appender>
    <logger name="root" level="INFO"/>
    <root level="INFO">
        <appender-ref ref="STDOUT"/>
    </root> 
</configuration>
```

로깅에 사용되는 패턴을 제외하면 이 구성은 logback.xml 파일이 없을때의 기본구성과 동일하다.

그러나 logback.xml파일을 수정하면 우리가 원하는 형태로 애플리케이션 로그 파일을 제어할 수 있다.

스프링부트의 구성을 사용하면 logback.xml 파일을 `생성하지 않고` 그것을 변경할수있다.

예를들어 루트의 로깅수준을 WARN으로하되 시큐리티의 로그는 DEBUG수준으로 하고 로그항목을

/var/logs/경로의 TacoCloud.log파일에 수록하고 싶다면 이렇게하면된다

```
logging:
    path: /var/logs/
    file: TacoCloud.log
    level:
        root: WARN
        org:
            springframework:
                security: DEBUG
```

## 우리의 구성 속성 생성하기

구성속성의 올바른 주입을 지원하기 위해 스프링부트는 `@ConfigurationProperties` 애노테이션을 제공한다. 

그리고 어떤 스프링 빈이건 이 애노테이션이 지정되면, 해당 빈의 속성들이 스프링 환경의 속성으로부터 주입될수있다.

그리고 어떤 스프링 빈이건 이 애노테이션이 지정되면 해당 빈의 속성들이 스프링 환경의 속성으로부터주입될수있다.

OrderController에 다음을 추가해보자
```
@GetMapping
	public String ordersForUser(
			@AuthenticationPrincipal User user, Model model
			) {
		model.addAttribute("orders", orderRepo.findByUserOrderByPlacedAtDesc(user));
		return "orderList";
	}
```

orderForUser()메소드는 사용자가 여러번 주문했을 때 유용하게 사용할 수 있다. 그러나

최근 몇개의 주문이 브라우저에 나타나는 것은 유용하지만, 수백개의 주문을 여러 페이지에 걸쳐봐야 한다면

피곤할것이다. 최근의 20개 주문만 나타나도록 제한하면 다음과같이 변경할 수 있다.

```
@GetMapping
	public String ordersForUser(
			@AuthenticationPrincipal User user, Model model
			) {
		Pageable pageable= PageRequest.of(0, 20);
		model.addAttribute("orders", orderRepo.findByUserOrderByPlacedAtDesc(user,pageable));
		return "orderList";
	}
```

이 코드는 잘 동작하지만 페이지 크기를 하드 코딩했다는것이 거슬린다.  만일 한페이지 20개가 많아서

10개로 줄이려한다면?? 다시 빌드 및 배포해야할것이다.

이때는 커스텀 구성 속성을 사용해 페이지 크기를 설정할 수 있다. 우선 pageSize라는 새로운 속성을

OrderController에 추가한후 `@ConfigurationProperties`애노테이션을 지정하면된다.


```
@Slf4j
@Controller
@RequestMapping("/orders")
@SessionAttributes("order")
@ConfigurationProperties(prefix="taco.orders")
public class OrderController {
	
	private int pageSize=20;
	
	public void setPageSize(int pageSize) {
		this.pageSize=pageSize;
	}
@GetMapping
	public String ordersForUser(
			@AuthenticationPrincipal User user, Model model
			) {
		Pageable pageable= PageRequest.of(0, pageSize);
		model.addAttribute("orders", orderRepo.findByUserOrderByPlacedAtDesc(user,pageable));
		return "orderList";
	}
}
```

가장 중요한 변화는 `@ConfigurationProperties`이며 이 애노테이션의 지정된 접두어는 taco.orders다.

따라서 pageSize 구성 속성 값을 설정할때는 taco.orders.pageSize라는 이름을 사용해야한다.

새로운 pageSize속성의 기본값은 20이다. 그러나 taco.orders.pageSize를 사용해 어떤값으로도

쉽게 변경할 수 있다. 예를들어 다음과 같이 application.yml에 속성을  설정하면된다

```
taco:
    orders:
        pageSize: 10
```

또는 애플리케이션 프로덕션에서 사용중에 빠르게 변경해야한다면 다음과 같이 환경 변수에 

taco.orders.pageSize속성을 설정할 수 있다. 이때는 다시 빌드 및 배포하지않아도 된다

    $ export TACO_ORDERS_PAGESIZE=10

### 구성 속성 홀더 정의하기

`@ConfigurationProperties`가 반드시 컨트롤러나 특정 빈에만 사용될 수 있는것은아니다.

실제로 @ConfigurationProperties는 구성 데이터의 홀더로 사용되는 빈에 지정되는 경우가 많다.

그리고 이렇게 하면 컨트롤러와 이외의 다른 애플리케이션 클래스 외부에 구성 관련 정보를 따로 유지할 수 있다. 

또한 여러 빈에 공통적인 구성 속성을 쉽게 공유할 수 있다.

OrderController의 pageSize 속성의 경우는 이속성을 별개의 홀더 클래스로 추출할 수 있다.
```
@Component @ConfigurationProperties(prefix="taco.orders")
@Data
public class OrderProps {
	private int pageSize=20;
}

```

이 클래스는 접두어로 taco.orders를 갖는 @ConfigurationProperties가 지정되었다.

또한 @Component가 지정되었으므로 스프링 컴포넌트 검색에서 OrderProps를 자동으로 찾은 후 

어플리케이션 컨텍스트의 빈으로 생성해준다. 따라서 OrderProps빈을 OrderController에 주입하자
```
@Slf4j
@Controller
@RequestMapping("/orders")
@SessionAttributes("order")
public class OrderController {
	
	
	private OrderRepository orderRepo;
	
	private OrderProps props;
	
	public OrderController(OrderRepository orderRepo, OrderProps props) {
		this.orderRepo = orderRepo;
		this.props=props;
	}
	
	@GetMapping
	public String ordersForUser(
			@AuthenticationPrincipal User user, Model model
			) {
		Pageable pageable= PageRequest.of(0, props.getPageSize());
		model.addAttribute("orders", orderRepo.findByUserOrderByPlacedAtDesc(user,pageable));
		return "orderList";
	}
}
```

이처럼 구성 속성 홀더 빈을 사용하면 구성 속성 관련 코드를 한군데에 모아둘수있으므로 해당속성을

사용하는 클래스들의 코드가 더 깔끔해진다.

### 프로파일 사용해서 구성하기

애플리케이션이 서로 다른 런타임 환경에 배포, 설치될 때는 대게 구성 명세가 달라진다.

예를들어, 데이터베이스 연결 명세가 개발환경과 다를 것이고, 프로덕션 환경과도 여전히 다를 것이다.

이때는 application.properties나 application.yml에 정의하는 대신 운영체제의 환경변수를 사용해 구성하는것도 한가지 방법이다.

예를들어 개발 시점에서는 자동-구성된 내장 H2데이터베이스를 사용할수있다.

그러나 프로덕션 환경에서는 다음과 같이 환경변수로 데이터베이스 구성 속성을 설정해야한다

```
% export SPRING_DATASOURCE_URL=jdbc:mysql://localhost/tacocloud
% export SPRING_DATASOURCE_USERNAME=tacouser
% export SPRING_DATASOURCE_PASSWORD=tacopassword
```

하지만 하나이상의 구성속성을 환경변수로 지정하는것은 번거롭다. 게다가 환경변수의 변경을 추적 관리

하거나 오류가 있을경우 변경전으로 바로 되돌릴수 있는방법이 마땅치 않다.

### 따라서 스프링 프로파일의 사용을 선호한다.

런타임시에 활성화 되는 프로파일에 따라 서로 다른 빈, 구성클래스, 구성속성들이 적용 또는 무시되도록 하는 것이 프로파일이다.


### 프로파일 특정 속성 정의하기

프로파일에 특정한 속성을 정의하는 한가지 방법은 프로덕션 환경의 속성들만 포함하는 또다른 .yml이나

.properties 파일을 생성하는것이다. 이때 파일이름은 다음 규칙을 따라야한다

즉 application-{프로파일 이름}.yml 또는 application-{프로파일이름}.properties다.

그다음 해당프로파일에 적합한 구성속성들을 지정할수있다.예를들어, application-prod.yyml이라는 파일을생성할수있다.
```
spring:
    datasource:
        url: jdbc:mysql://localhost/tacocloud
        username: tacouser
        password: tacopassword
    logging:
        level:
            tacos: WARN
```

또한 YAML 구성에서만 가능한 또 다른 방법으로 프로파일 특정 속성을 정의할 수도있다.

이때는 프로파일에 특정되지 않고 공통으로 적용되는 기본 속성과 함께 프로파일 특정 속성을 application.yml에 지정할수있다.

즉 프로파일에 특정되지 않는 기본 속성 다음에 --- 을 추가하고 그다음에 해당 프로파일 이름을 나타내는

spring.profiles속성을 지정하면된다. 이방법으로 application.yml에 프로덕션 환경속성을 지정한예를보자

```
loggin:
    level:
        taos: DEBGUG


---
spring:
    prifiles: prod
    datasource:
        url: jdbc:mysql://localhost/tacocloud
        username: tacouser
        password: tacopassword
logging:
    level:
    tacos: WARN
```
이 application.yml 파일은 3개의 하이픈(---)을 기준으로 두부분으로 구분된다.

두번째 부분에서는 spring.profiles의 값을 지정하고 있으며, 이후의 속성 설정은 prod 프로파일에만 

적용됨을 나타낸다 이와는 달리 첫 번째 부분에서는 spring.profiles의 값을 지정하지 않았다.

따라서 이 부분의 속성 설정은 모든 프로파일에 공통으로 적용되며, 만일 이부분의 속성과 같은 속성을 활성화된 

프로파일에서 설정하지 않으면 해당 속성은 기본설정이된다.

이렇게 application-{프로파일 이름}.yml또는 application-{프로파일이름}.properties 형식의 이름을

갖는 파일들을 추가로 생성하면, 우리가 필요로하는 만큼 얼마든지 많은 프로파일로 속성을 정의할 수 있다.

또는 우리가 원한다면 application.yml에 3개의 하이픈(---)을 입력한후 프로파일 이름을 나타내는

또다른 spring.profiles 속성을 지정하여 우리가 필요한 모든 프로파일 특정 속성을 추가할 수 있다.

### 프로파일 활성화하기

프로파일을 활성화 하려면 spring.profiels.active속성에 지정하면된다. 예를들어 다음과 같다
```
spring:
    profiels:
        active:
        - prod    
```

그러나 이것은 가장 좋지 않은 프로파일 활성화 방법이다. 만일 application.yml에서 활성화 프로파일을

설정하면 해당 프로파일이 기본 프로파일이된다. 따라서 프로덕션 환경 특정 속성을 개발 속성과 

분리시키기위해 프로파일을 사용하는 장점을 전혀 살릴수없게 된다.

그러므로 이 방법대신 환경 변수를 사용해서 활성화 프로파일을 설정할 것을 권한다.

이때는 다음과 같이 프로덕션 환경의 SPRING_PROFILES_ACTIVE를 설정할수있다
    % export SPRING_PROFILES_ACTIVE=prod

이렇게하면 해당 컴퓨터에 배포되는 어떤 애플리케이션에서도 prod 프로파일이 활성화된다.

만일 실행가능한 JAR파일로 애플리케이션을 실행한다면, 다음과 같이 명령행 인자로 활성화 프로파일을 설정할수도있다.

    % java -jar taco-cloud.jar --spring.profiles.active=prod

spring.profiles.active 속성에는 여러개의 프로파일이 포함될 수 있다. 이런경우 환경변수를 사용하는 경우

    % export SPRING_PROFILES_ACTIVE=prod,audit,ha

YAML에서는
```
spring:
    profiles:
    active:
        - prod
        - audit
        - ha
```

만일 스프링 애플리케이션을 클라우드 파운드리에 배포할때는 cloud라는 이름의 프로파일이 자동으로 활성화 된다는것을 알아두자.

따라서 클라우드 파운드리가 우리의 프로덕션 환경이라면 cloud프로파일 아래에 프로덕션 환경의 특정속성들을 지정해야한다

### 프로파일을 사용해 조건별로 빈생성하기

서로 다른 프로파일 각각에 적합한 빈들을 제공하는 것이 유용할때가있다.

일반적으로 자바 구성클래스에 선언된 빈은 활성화되는 프로파일과는 무관하게 생성된다. 

그러나 특정 프로파일이 활성화될 때만 생성되어야 하는 빈들이 있다고 해보자.  이경우 @Profile 애노테이션을

사용하면 지정된 프로파일에만 적합한 빈들을 나타낼 수 있다.

예를들어, TacoColudApplication에는 CommandLineRunner 빈이 선언되어 있다.

그리고 애플리케이션이 시작될 때마다 식자재 데이터를 내장 데이터베이스에 로드하기 위해 CommandLineRunner 빈이 사용된다. 

이것은 개발시점에는 좋지만 프로덕션환경에는 불필요하고 원하는것도아니다.

이경우 다음과같이 빈 메소드에 @Profile을 지정하면 프로덕션 환경에서 애플리케이션이 시작될 때마다 식자재

데이터가 로드되는 것을 방지할 수 있다.

```
@Bean
@Profile("dev")
public CommandLineRunner detaLoader(IngredientRepository repo,
    UserRepository userRepo, PasswordEncoder encoder){
        ...
    }
```

또는 dev 프로파일이나 qa 프로파일 중 하나가 활성화 될 때 CommandLineRunner빈이 생서오디어야 한다고 해보자. 

이때 이 빈이 생성되어야 하는 프로파일들을 함께 지정할 수 있다.
```
@Bean
@Profile({"dev","qa"})
public CommandLineRunner detaLoader(IngredientRepository repo,
    UserRepository userRepo, PasswordEncoder encoder){
        ...
    }
```

이렇게 하면 dev프로파일이나 qa프로파일중 하나가 활성화 될때만 식자재 데이터가 로드된다.

또한 prod프로파일이 활성화 되지 않을때는 CommandLineRunner빈이 항상 생성되도록 한다면 다음과같다

```
@Bean
@Profile("!prod")
public CommandLineRunner detaLoader(IngredientRepository repo,
    UserRepository userRepo, PasswordEncoder encoder){
        ...
    }
```

이때 느낌표는 부정의 의미이므로 prod프로파일이 활성화 되자 않을 경우 CommandLineRunner빈이 생성됨을 나타낸다.

@Profile은 @Configuration이 지정된 클래스 전체에 대해 사용할 수도 있다.

예를들어, DevelopmentConfig라는 이름의 구성클래스로 CommandLineRunner빈을 주입할때다음과 같이 지정할수있다.

```
@Profile({"!prod","!qa"})
@Configuration
public class DevelopmentConfig{
    @Bean
    public CommandLineRunner dataLoader(IngredientRepository repo,
        UserRepository userRepo, PasswordEncoder encoder
    )
}

```

이경우는 prod프로파일과 qa프로파일 모두 활성화 되지 않을때만  CommandLineRunner빈이 생성된다

# 5장 요약 
### [1] 스프링 빈에 @ConfigurationProperties를 지정하면 여러가지 원천 속성으로부터 

### 구성 속성 값의 주입을 활성화 할 수 있다.

### [2] 구성 속성은 명령행 인자, 환경변수, JVM시스템속성, 속성파일 ,YAML파일, 커스텀 

### 속성 등에서 설정할 수 있다.

### [3] 데이터소스 URL과 로깅수준의 지정을 포함해서 구성 속성은 스프링의 자동-구성 설정을

### 변경하는데 사용할 수 있다.

### [4] 스프링 프로파일은 활성화된 프로파일을 기반으로 구성속성을 설정하기 위해 사용할수있다.