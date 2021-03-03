# 🥇 16장 스프링 부트 액추에이터 사용하기

## 🍰 이 장에서 배우는 내용
- 스프링 부트 프로젝트에 액추에이터 활성화하기
- 액추에이터 엔드포인트 살펴보기
- 액추에이터 커스터마이징
- 액추에이터 보안 처리하기

`액추에이터`는 스프링 부트 애플리케이션의 모니터링이나 메트릭과 같은 기능을 HTTP와 JMX엔드포인트를 통해 제공한다.

## 🏓 액추에이터 개요
기계장치에서 액추에이터는 메커니즘을 제어하고 작동시키는 부품이다. 스프링 부트 애플리케이션에서는 스프링 부트 액추에이터가 그와

같은 역할을 수행한다. 즉, 실행 중인 애플리케이션의 내부를 볼 수 있게 하고, 어느 정도까지는 애플리케이션의 작동방법을 제어할 수 있게한다

액추에이터가 노출하는 엔드포인트를 사용하면 실행중인 스프링 부트 애플리케이션의 내부상태에 관한것을 알 수 있다. 예를들면 다음과같다.
- 애플리케이션 환경에서 사용할 수 있는 구성속성들
- 애플리케이션에 포함된 다양한 패키지의 로깅 레벨
- 애플리케이션이 사용 중인 메모리
- 지정된 엔드포인트가 받은 요청 횟수
- 애플리케이션의 건강 상태 정보

스프링 부트 애플리케이션에 액추에이터를 활성화 하려면 액추에이터의 스타터 의존성을 빌드에 추가해야한다
```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

이제 여러가지 액추에이터 엔드포인트를 사용할 수 있다.

#### 실행중인 스프링 부트 애플리케이션의 내부 상태를 볼 수있는 액추에이터 엔드포인트

|HTTP메소드|경로|설명|기본적으로 활성화되는가?|
|:--:|:--:|:--:|:--:|
|GET|/auditevents|호출된 감사(audit) 이벤트 리포트를 생성한다|No|
|GET|/beans|스프링 애플리케이션 컨텍스트의 모든 빈을 알려준다|No|
|GET|/conditions|성공 또는 실패했던 자동-구성 조건의 내역을 생성한다|No|
|GET|/configprops|모든 구성 속성들을 현재 값과 같이 알려준다|No|
|GET,POST,DELETE|/env|스프링 애플리케이션에 사용할 수 있는 모든속성근원과 이 근원들의 속성을알려준다|No|
|GET|/env/{toMatch}|특정 환경 속성의 값을 알려준다|No|
|GET|/health|애플리케이션의 건강 상태 정보를 반환한다|YES|
|GET|/heapdump|힙(heap)덤프를 다운로드한다|No|
|GET|/httptrace|가장 최근의 100개 요청에 대한 추적기록을 생성한다|No|
|GET|/info|개발자가 정의한 애플리케이션에 관한 정보를 반환한다|Yes|
|GET|/loggers|애플리케이션의 패키지리스트를 생성한다|No|
|GET,POST|/loggers/{name}|지정된 로거의 로깅 레벨을 반환한다. 유효 로깅레벨은 HTTP POST요청으로 설정될 수 있다.|No|
|GET|/mappings|모든 HTTP매핑과 이 매핑들을 처리하는 핸들러 메소드들의 내역을 제공한다|No|
|GET|/metrics|모든 메트릭 리스트를 반환한다|No|
|GET|/metrics/{name}|지정된 메트릭의 값을 반환한다|No|
|GET|/scheduledtasks|스케쥴링된 모든 태스크의 내역을 제공한다|No|
|GET|/threaddump|모든 애플리케이션 스레드의 내역을 반환한다|No|

### 🌟 액추에이터의 기본 경로 구성하기
기본적으로 위 표에있는 모든 엔드포인트의 경로에는 /actuator가 앞에 붙는다. 액추에이터의 기본 경로는

`management.endpoint.web.base-path`속성을 설정해 변경할 수 있다. 예를 들어, 기본 경로를 /management로 변경하고 싶다면

다음과 같이 application.yml파일에 설정하면 된다.
```
management:
  endpoints:
    web:
      base-path: /management
```

### 🌟 액추에이터 엔드포인트의 활성화와 비활성화

위 표를보면 /health와 /info 엔드포인트만 기본적으로 활성화되는 것을 알 수 있다. 대부분의 액추에이터 엔드포인트는

민감한 정보를 제공하므로 보안 처리가 되어야 하기 때문이다. 물론 스프링 시큐리티를 사용해서 액추에이터를 보안처리할수있다.

그러나 액추에이터 자체로는 보안 처리가 되어있지 않으므로 대부분의 엔드포인트가 기본적으로 비활성화 되어 있다.

엔드포인트의 노출 여부를 제어할때는 `management.endpoints.web.exposure.incoude`와 `management.endpoints.web.exposure.exclude`

구성 속성을 사용할 수 있다. 예를들어, health,/info,/beans,/conditions엔드포인트만 노출하고 싶다면 다음과같다
```
management:
  endpoints:
    web:
      exposure:
        include: health, info, beans, conditions
```

management.endpoints.web.exposure.include 속성은 와일드 카드인 *도 허용한다. 이것은 모든 액추에이터 엔드포인트가 노출되어야 한다는것을 나타낸다
```
management:
  endpoints:
    web:
      exposure:
        include: '*'
```

만일 일부 엔드포인트를 제외한 모든 엔드포인트를 노출하고 싶다면, 와일드카드로 모든 엔드포인트를 포함시킨 후 일부만

제외하면 쉽게 할 수 있다. 예를들어, /threaddump와 /heapdump를 제외한 모든 액추에이터 엔드포인트를 노출한다면 다음과같다

```
management:
  endpoints:
    web:
      exposure:
        include: '*'
        exclude: threaddump, headdump
```

## 🍰 액추에이터 엔드포인트 소비하기 

액추에이터는 실행 중인 애플리케이션의 흥미롭고 유용한 정보를 HTTP 엔드포인트를 통해 제공한다. 그리고 HTTP 엔드포인트이므로

다른 REST API처럼 브라우저 기반의 자바스크립트 애플리케이션 또는 curl을 클라이너트로 사용해 소비할 수 있다.

### 🌟 애플리케이션 기본 정보 가져오기

#### `애플리케이션에 관한 정보 요구하기`
실행 중인 스프링 부트 애플리케이션에 관한 정보를 알려면 /info 엔드포인트에 요구하면 된다. /info 엔드포인트가

기본으로 제공하는 정보는 그리 유용하지 않지만, curl을 사용해 알아보면 다음과 같다.
```
$ curl localhost:8081/actuator/info
{}
```

괄호 속의 응답을 보면 아무것도없다. 우리가 제공한 정보가 없기때문이다. /info 엔드포인트가 반환하는 정보를 제공하는

방법은 몇가지 있다. 이름이 info.로 시작하는 하나 이상의 구성 속성을 생성하는 것이 가장 쉬운 방법이다.

예를들어, /info엔드포인트의 응답에 이메일과 전화번호를 포함하는 연락처 정보를 포함시키고 싶다면 다음과같다

```
info:
  contact:
    email:support@tacocloud.com
    phone: 822-625-6831
```

위의 속성들은 빈에 특별한 의미를 주는 것은 아니다 그러나 이제는 /info엔드포인트가 다음과 같이 응답한다
```
{
    "contact":{
        "email":"support@tacocloud.com",
        "phone:"822-625-6831"
    }
}
```

#### `애플리케이션의 건강 상태 살펴보기`
/health 엔드포인트에 HTTP GET 요청을 하면 애플리케이션의 건강 상태 정보를 갖는 간단한 JSON응답을 받는다.
```
$ curl localhost:8080/actuator/health
{"status":"UP"}
```
여기에 나타난 상태는 하나이상의 건강지표를 종합한 상태다. 건강 지표는 애플리케이션이 상호 작용하는 외부시스템의 건강상태를 나타낸다.

각 지표의 건강상태는 다음중 하나가 될 수 있다.
- UP: 외부 시스템이 작동 중이고 접근가능하다
- DOWN: 외부 시스템이 작동하지 않거나 접근할 수없다
- UNKNOWN: 외부 시스템의 상태가 분명하지 않다
- OUT_OF_SERVICE: 외부 시스템에 접근할 수 있지만, 현재는 사용할 수없다

모든 건강 지표의 건강 상태는 다음 규칙에 따라 애플리케이션의 전체 건강 상태로 종합된다.
- 모든 건강 지표가 UP이면 애플리케이션의 건강상태도 UP
- 하나 이상의 건강 지표가 DOWN이면 애플리케이션의 건강 상태도 DOWN
- 하나 이상의 건강 지표가 OUT_OF_SERVICE이면 애플리케이션의 건강 상태도 OUT_OF_SERVICE
- UNKNOWN건강상태는 무시되며, 애플리케이션의 종합된 건강상태만 반환된다.

기본적으로 /health 엔드포인트의 요청 응답으로는 종합된 건강 상태만 반환된다. 그러나 management.endpoint.health.show-details

속성을 구성해 모든 건강 지표를 자세히 볼수있다 
```
management:
  endpoint:
    health:
      show-details: always
```

management.endpoint.health.show-details 속성의 기본값은 never다. 모든 건강지표의 상세 내역을 항상볼때는 always로 설정하면된다.

또한, when-authorized로 설정하면 요청하는 클라이언트가 완벽하게 인가된 경우에 한해서 상세내역을 보여준다.

이제 always로 한후 /health엔드포인트의 GET요청을 하면 몽고문서데이터베이스를 사용하는 경우 다음과 같은 응답이 반환될것이다.
```
{
    "status":"UP",
    "details":{
        "mongo":{
            "status":"UP",
            "details":{
                "version":"3.2.2"
            }
    },
    "diskSpace":{
        "status":"UP",
        "details":{
            "total":499963170816,
            "free":177284784128,
            "threshold":10485760
        }
    }
    }
}
```
다른 외부 의존성과 무관하게 모든 애플리케이션은 diskSpace라는 이름의 파일 시스템 건강지표를 갖는다.

diskSPace건강 지표는 파일시스템의 건강상태를 나타내며, 빈공간이 얼마나 남아있는지에 따라 결정된다.

만일 사용가능한 디스크 공간이 한계치 밑으로 떨어지면 DOWN상태로 알려준다.

자동-구성에서는 애플리케이션과 관련된 건강 지표만 /health 엔드포인트의 응답에 나타낸다. mongo와 diskSpace건강

지표에 추가하여 스프링 부트는 다른 외부 데이터베이스와 시스템의 건강 지표들도 제공한다 예를들어 다음과같다
- 카산드라
- 구성서버
- Couchbase
- 유레카
- Hystrix
- JDBC 데이터 소스
- Elasticsearch
- InfluxDB
- JMS 메시지 브로커
- LDAP
- 이메일 서버
- Neo4j
- Rabbit메시지 브로커
- Redis
- Solr

### 🌟 구성 상세 정보 보기
애플리케이션에 관한 일반 정보를 받는것은 기본적으로 필요하지만, 이보다 더 유용한 정보를 알아야 할 필요가 있다. 예를들어,

애플리케이션이 어떻게 구성되었는지, 애플리케이션 컨텍스트에 어떤 빈이 있는지, 어떤 자동-구성이 성공 또는 실패인지 등.

우선 /beans엔드포인트부터 보자

#### `빈(Bean)연결 정보 얻기`
스프링 애플리케이션 컨텍스트를 살펴보는 데 가장 중요한 엔드포인트가 /beans 엔드포인트다. 이 엔드포인트는 애플리케이션

컨텍스트의 모든 빈을 나타내는 JSON문서를 반환한다. /beans 엔드포인트의 Get요청에 대한 결과는 너무많으므로 일부만 보자.

```
{
    "contexts":{
        "application-1":{
            "beans":{
                ...
                "ingredientController":{
                    "aliases":[],
                    "scope":"singleton",
                    "type":"tacos.ingredients.IngredientsController",
                    "resource":file[/Users/kimjunseong/DeskTop/SpringInAction5/chap16/TacoCloud/ingredient-service/target/classes/tacos/
                    ingredients/Ingredients.Controller.class]",
                  "dependencies":[
                      "ingredientRepository"
                  ]
                },
                ...
            },
            "parentId":null
        }
    }
}
```
응답의 최상위 요소는 contexts이며 이것은 애플리케이션에 있는 각 스프링 애플리케이션 컨텍스트의 하위 요소 하나를 포함한다.

그리고 각 스프링 애플리케이션 컨텍스트에는 beans요소가 있으며, 이것은 해당 애플리케이션 컨텍스트에 있는 모든 빈의 상세정보를 갖는다.

#### `자동-구성 내역 알아보기`
자동-구성은 스프링 부트가 제공하는 가장 강력한 기능 중 하나다. 그러나 때로는 왜 자동-구성이 되었는지 궁금할 것이다.

이런경우 /conditions엔드포인트의 GET요청을 하여 자동-구성에서 무슨일 이생겼는지 알아볼 수있다.

/conditions 엔드포인트에서 반환된 자동-구성 내역은 세 부분으로 나뉜다. 긍정일치, 부정일치, 조건없는 클래스다. 

예를들어 /conditions요청의 응답 일부를 보면 다음과같다

```
{
    "contexts":{
        "application-1":{
            "positiveMatches":{
                ...
                "MongoDataAutoConfiguration#mongoTemplate":[
                    {
                        "condition":"OnBeanCondition",
                        "message":"@ConditionalOnMissingBean (types:
                        org.springframework.data.mongodb.core.MongoTemplate;
                        SearchStrategy: all) did not find any beans"
                    }
                ],
                ...
            },
            "negativeMatches":{
                ...
                "DispatcherServletAutoConfiguration":{
                    "notMatched":[
                        {
                            "condition":"OnClassCondition",
                            "message":"@ConditionalOnClass did not find required
                            class 'org.springframework.web.servlet.DispatcherServlet'"
                        }
                    ],
                    "matched":[]
                },
                ...
            },
            "unconditionalClasses"[
                ...
                "org.springframework.boot.autoconfigure.context.
                ConfigurationPropertiesAutoConfiguration",
                ...
            ]
        }
    }
}
```
`positiveMathces`에서는 MongoTemplate 빈이자동-구성되었음을 보여준다. 이 자동-구성에는 `@ConditionalOnMissingBean`애노테이션이 포함되어 있다.

이 애노테이션은 해당 빈이 구성되지 않았다면 구성되게한다. 여기서는 MongoTemplate타입의 빈이 없으므로 자동-구성이 수행되어 하나를 구성한것이다.

`negativeMathces`에서는 스프링 부트 자동-구성이 DispatcherServlet을 구성하려 했지만, DispatcherServlet

을 찾을 수 없어서 조건부 애노테이션인 `@ConditionalOnClass`가 실패하였다는 것을 보여준다.

마지막으로 `unconditionalClasses`아래에 있는 ConfigurationPropertiesAutoConfiguration빈은 조건없이 구성되었다.

구성 속성들은 스프링 부트의 작동에 기본이 되는 것이므로 구성 속성에 관련된 모든 구성은 조건 없이 자동-구성되어야하기 때문이다.

#### `환경 속성과 구성 속성 살펴보기`

애플리케이션의 빈들이 어떻게 연결되어 있는지 아는 것에 추가하여 어떤 환경속성들이 사용가능하고 어떤 구성속성들이 각 빈에 주입되었는지 파악하는 것도 중요하다

/env 엔드포인트에 GET요청을 하면 스프링 애플리케이션에 적용 중인 모든 속성 근원의 속성들을 포함하는 다소 긴 응답을 받는다.

여기에는 환경 변수, JVM 시스템 속성, application.properties와 application.yml파일, 

그리고 스프링 클라우드 구성서버의 속성까지도 포함된다.

#### `HTTP 요청-매핑 내역보기`
스프링 MVC의 프로그래밍 모델은 HTTP 요청을 쉽게 처리한다. 요청-매핑을 해주는 애노테이션을 메소드에 지정만 하면되기때ㅜㅁㄴ이다.

그러나 애플리케이션이 처리할 수 있는 모든 종류의 HTTP요청, 그리고 이런 요청들을 어떤 종류의 컴포넌트가 처리하는지를 전체적으로 파악하는 게

어려울 수 있다. 액추에이터의 /mappings 엔드포인트는 애플리케이션의 모든 HTTP요청핸들러 내역을 제공한다.

#### `로깅 레벨 관리하기`

어떤 애플리케이션이든 로깅은 중요한 기능이다. 로깅은 감사는 물론 디버깅의 수단을 제공할 수 있다.

로깅 레벨의 설정은 균형을 잡는 작업이 될 수 있다. 만일 로깅 레벨을 너무 장황한것으로 설정하면 로그에

너무 많은 메시지가 나타나서 유용한 정보를 찾기 어려울 수 있다. 반면에 로깅 레벨을 너무 느슨한 것으로 설정하면 

에플리케이션이 처리하는 것을 이해하는데 로그가 도움이 되지않을 수 있다.

일반적으로 로깅 레벨은 패키지 단위로 적용된다. 다음은 /loggers엔드포인트의 json응답을 발췌한것이다

```
{
    "levels":["OFF","ERROR","WARN","INFO","DEBUG","TRACE"],
    "loggers"{
        "ROOT":{
            "configuredLevel":"INFO","effectiveLevel":"INFO"
        },
    ...
    "org.springframework.web":{
        "configuredLevel":null,"effectiveLevel":"INFO"
    },
    ...
    "tacos":{
        "configuredLevel":null, "effectiveLevel":"INFO"
    },
    "tacos.ingredients":{
        "configuredLevel": null, "effectiveLevel":"INFO"
    },
    ...
    }
}
```

응답의 맨앞에는 모든 로깅레벨의 내역이 나타난다. 그다음에 loggers요소에는 애플리케이션의 각 패키지에 대한 로깅레벨의 상세내역이 포함된다.

configuredLevel 속성은 명시적으로 구성된 로깅레벨을 보여준다. effectiveLevel속성은 ㅂ ㅜ모 패키지나

루트 로거로부터 상속받을 수 있는 유효 로깅 레벨을 제공한다.

만일 tacos.ingredients패키지에 설정된 로깅레벨을 알고싶을때는 /loggers/tacos/ingredients GET요청을하면된다
```
{
    "configuredLevel":null,
    "effectiveLevel":"INFO"
}
```

애플리케이션 패키지의 로깅 레벨을 반환하는 것외에도 /loggers엔드포인트는 POST요청을 통해 configured로깅 레벨을 변경할 수 있게해준다.

예를들어 tacos.ingredients패키지의 로깅레벨을 DEBUG로 설정하고 싶다면 다음과같이한다
```
$ curl localhost:8081/actuator/loggers/tacos/ingredients \
    -d'{"configuredLevel":"DEBUG"}' \
    -H"Content-type: application/json"
```

이제는 로깅 레벨이 변경되었으므로 /loggers/tacos/ingredients에 GET요청을 하면 변경되었는지 확인할 수 있다.

```
{
    "configuredLevel":"DEBUG",
    "effectiveLevel":"DEBUG"
}
```


이처럼 configuredLevel을 변경하면 effectiveLevel도 같이 변경된다는 것에 유의하자.

### 🌟 애플리케이션 활동 지켜보기

애플리케이션이 처리하는 HTTP요청이나 애플리케이션에 있는 모든 스레드의 작동을 포함해서 실행 중인 애플리케이션의 활동을

지켜보는 것은 유용하다. 이것을 위해 액추에이터는 /httptrace, /threaddump, /heapdump엔드포인트를 제공한다.

/heapdump엔드포인트는 상세하게 나타내기 가장 어려운 액추에이터 엔드포인트일것이다. 이 엔드포인트는 메모리나 스레드 문제를

찾는데 사용할 수 있는 gzip압축형태의 HPROF힙 덤프 파일을 다운로드한다. 이부분은 생략한다.

#### `HTTP 요청 추적하기`
/httptrace 엔드포인트는 애플리케이션이 처리한 가장 최근의 100개 요청을 알려주며, 다음내용이 포함된다.

HTTP 요청 메소드와 경로, 요청이 처리된 시점을 나타내는 타임스탬프, 요청과 응답 모두의 헤더들, 요청처리 소요시간등이다.

#### `스레드 모니터링`
HTTP 요청 추적에 추가하여 실행 중인 애플리케이션에서 무슨 일이 생기는지 결정하는데 스레드의 활동이 유용할 수있다.

/threaddump 엔드포인트는 현재 실행중인 스레드에 관한 스냅샷을제공한다.  이 정보에는 스레드의 블로킹과

록킹 상태의 관련 상세정보와 스택 기록등이 포함된다. 하지만 요청 시점의 스레드 활동에 대한 스냅샷만 제공하므로

스냅샷의 지속적인 모든활동을 알기 어렵다. 다음장에서 실시간뷰로 엔드포인트 모니터링방법을 알아보자.

### 🌟 런타임 메트릭 활용하기

/metrics 엔드포인트는 실행 중인 애플리케이션에서 생성되는 온갖 종류의 메트릭을 제공할 수 있으며 여기에는

메모리, 프로세스, 가비지컬렉션, HTTP요청 관련 메트릭 등이 포함된다. 

## 💕 액추에이터 커스터마이징
액추에이터의 가장 큰 특징 중 하나는 애플리케이션의 특정 요구를 충족하기 위해 커스터마이징 할 수 있다느 것이다.

즉 커스텀 엔드포인트를 생성할 수 있다.

### 🌟 /info 엔드포인트에 정보 제공하기
이전에 보앗듯 /info엔드포인트는 처음에 아무 정보도 제공하지 않지만 `info.`로 시작하는 속성을 생성하면

쉽게 커스텀 데이터를 추가할 수 있다. 하지만 이외에도 다른 방법이 있는데 스프링 부트는 `InfoContributor`라는 인터페이스를 제공하며,

이 인터페이스는 우리가 원하는 어떤정보도 /info엔드포인트 응답에 추가할 수 있게한다.

#### `커스텀 정보 제공자 생성하기`
타코 클라우드와 관련된 간단한 통계치를 /info 엔드포인트에 추가하고 싶다고하자. 예를들어, 생성된 타코의 개수에 관한

정보를 포함시킨다면 다음과 같이 할 수 있다. 우선, `InfoContributor`를 구현하는 클래스를 생성해 TacoRepository에 주입후 

TacoRepository가 제공하는 타코 개수를 /info엔드포인트에 추가한다. 이런일을 처리하는 InfoContributor구현클래스는 다음과같다

```
@Component
public class TacoCountInfoContributor implements InfoContributor{
    private TacoRepository tacoRepo;
    public TacoCountInfoContributor(TacoRepository tacoRepo){
        this.tacoRep=tacoRepo;
    }

    @Override
    public void contribute(Builder builder){
        long tacoCount=tacoRepo.count();
        Map<String,Object> tacoMap=new HashMap<String,Object>();
        tacoMap.put("count",tacoCount);
        builder.withDetail("taco-stats",tacoMap);
    }
}
```

TacoCountInfoContributor 클래스는 InfoContributor의 contribute()메소드를 구현해야한다. 이 메소드에는

TacoRepository로부터 타코 개수를 알아낸 후 인자로 받은 Builder객체의 withDetail()을 호출하여

타코 개수 정보를 /info 엔드포인트에 추가한다. 생성된 타코 개수는 TacoRepository의 count()메소드를 호출하여 알 수있으며, 이것을 Map에저장한다.

그리고 이 Map과 이것의 라벨인 taco-stats를 withDetail()메소드의 인자로 전달하여 /info엔드포인트에 추가한다.

따라서 /info엔드포인트에 GET요청을 하면 생성된 타코 개수가 포함된 다음 응답을 반환한다
```
{
    "taco-stats":{
        "count":44
    }
}
```
이처럼 info.으로 시작하는 속성생성방법과 달리 원하는 정보를 동적으로 추가할 수 있다.

#### `Git 커밋 정보 노출하기`

Git 소스 코드 제어 시스템에 프로젝트를 유지관리한다고 해보자. 이 경우 Git커밋 정보를 /info 엔드포인트에 포함하고 싶을 수 있다.

이때는 메이븐 프로젝트의 pom.xml에 다음의 플러그인을 추가해야한다

```
<build>
    <pulgins>
        <plugin>
            <groupId>pl.project13.maven</groupId>
            <artifactId>git-commit-id-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```
gradle의 경우 build.gradle에 아래와 같이 추가하면 된다

```
plugins{
    id "com.gorylenko.gradle-git-properties" version "1.4.17"
}
```

두 가지 플러그인 모두 기본적으로 같은 일을 수행한다 즉, 프로젝트의 모든 Git메타데이터를 포함하는 git.properties라는 이름의

파일을 빌드 시점에 생성한다. 그리고 애플리케이션이 실행될 때 스프링 부트에 특별히 구현된 InfoContributor에서 

해당 파일을 찾아서 /info엔드포인트 응답의 일부로 파일내용을 노출시킨다.

### 🌟 커스텀 건강 지표 정의하기

스프링 부트에는 몇 가지 건강 지표가 포함되어 있으며, 이 건강 지표들은 스프링 애플리케이션에 통합할 수 있는 많은 외부 시스템의

건강 상태 정보를 제공한다. 그러나 때로는 스프링 부트에서 지원하지 않거나 건강 지표를 제공하지 않는 외부 시스템을

사용하는 경우가 생길 수 있다. 예를들어, 우리 애플리케이션이 레거시 메인프레임 애플리케이션과 통합될 수있으며,

이 경우 우리 애플리케이션의 건강 상태는 레거시 시스템의 건강상태에 의해 영향받을 수 있다. 커스텀 건강 지표를 생성할 때는 

`HealthIndicator`인터페이스를 구현하는 빈만 생성하면된다. 시간에 따라 무작위로 건강상태가 결정되는 `HealthIndicator`간단한 예를 보자

```
@Component
public class WackoHealthIndicator implements HealthIndicator{
    @Override
    public Health health(){
        int hour=Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if(hour>12){
            return Health
                .outOfService()
                .withDetail("reason",
                    "I'm out of service after lunchtime")
                .withDetail("hour",hour)
                .build();
        }
        if(Math.random< 0.1){
            return Health
                    .down()
                    .withDetail("reason", "I break 10% of the time")
                    .build();
        }
        return Health
            .up()
            .withDetail("reason","All is good!")
            .build();
    }
}
```

야기서 우선 현재 시간을 검사해 오후면 OUT_OF_SERVICE 건강상태와 이상태를 설명하는 메시지르 ㄹ반환한다.

그리고 오전일지라도 10%의 확률로 DOWN건강상태를 ㄱ반환한다. 

### 🌟 커스텀 메트릭 등록하기
궁극적으로 액추에이터 메트릭은 Micrometer에 의해 구현된다. 이것은 벤더 중립적인 메트릭이며, 애플리케이션이

원하는 어떤 메트릭도 발행하여 서드파티 모니터링 시스템에서 보여줄 수 있게한다.

Micrometer로 메트릭을 발행하는 가장 기본적인 방법은 Micrometer의 `MeterRegistry`를 사용하는 것이다.

스프링 부트 애플리케이션에서 메트릭을 발행할 때는 어디든 필요한곳에 `MeterRegistry`만 주입하면 된다.

커스텀 메트릭을 발행하는 예로 서로 다른 식자재를 사용해 생성된 타코의 개수를 세는 카운터를 유지하고싶다면 다음과같다
```
@Component
public class TacoMetrics extends AbstractRepositoryEventListener<Taco>{
    private MeterRegistry meterRegistry;
    public TacoMetrics(MeterRegistry meterRegistry){
        this.meterRegistry=meterRegistry;
    }

    @Override
    protected void onAfterCreate(Taco taco){
        List<Ingredient> ingredients=taco.getIngredients();
        for(Ingredient ingredient: ingredients){
            meterRegistry.counter("tacocloud",
            "ingredient", ingredient.getId()).increment();
        }
    }
}
```
여기서는 MeterRegistry가 TacoMetrics의 생성자를 통해 주입된다. 또한 TacoMetrics는 레포지토리 이벤트를 가로챌 수 있는 스프링

데이터 클래스인 `AbstractRepositoryEventListener`의 서브클래스이며 새로운 Taco객체가 저장될 때마다 호출 되도록 

onAfterCreate()메소드를 오버라이딩한다.

그리고 onAfterCreate()내부에서는 각 식자재에 대한 카운터가 선언되며, 이때 카운터의 태그 이름은 ingredient이고 ,

태그 값은 식자재 ID와 동일하다. 만일 해당 태그가 이미 존재하면 재사용된다. 그리고 각 식자재를 갖는

타코가 생성되면 해당 카운터 값이 증가한다.

### 🌟 커스텀 엔드포인트 생성하기

액추에이터의 엔드포인트는 스프링 컨트롤러로 구현된 것에 불과하다고 생각할 수 잇다. 그러나 엔드포인트는 HTTP요청을 처리하는 것은 물론이고

JMXMBeans로도 노출되어 사용될 수 있다. 따라서 엔드포인트는 컨트롤러 클래스 이상의 것임이 분명하다.

실제로 액추에이터 엔드포인트는 컨트롤러와 매우 다르게 정의된다. @Controller나 @RestController 애노테이션으로 지정되는

클래스 대신, 액추에이터 엔드포인트는 @Endpoint로 지정되는 클래스로 정의된다. 

게다가 @GetMapping,@PostMapping과 같은 HTTP애노테이션을 사용하는 대신, 액추에이터 엔드포인트 오퍼레이션은 

@ReadOperation, @WriteOperation,@DeleteOperation 애노테이션이 지정된 메소드로 정의된다. 

또한 이 애노테이션들은 어떤 특정한 통신메커니즘도 수반하지 않으므로 액추에이터 대신 다양한 통신매커니즘으로 통신할 수 있게한다.

#### 메모 처리 커스텀 엔드포인트
```
@Component
@Endpoint(id="notes",enableByDefault=true)
public class NotesEndpoint{
    private List<Note> notes=new ArrayList<>();

    @ReadOperation
    public List<Note> notes(){
        return notes;
    }

    @WriteOperation
    public List<Note> addNote(String text){
        notes.add(new Note(text));
        return notes
    }

    @DeleteOperation
    public List<Note> deleteNote(int index){
        if(index<notes.size())
        notes.remove(index);
        return notes;
    }

    @RequiredArgsConstructor
    private class Note{
        @Getter
        private Date time=new Date();
        @Getter
        private final String text;
    }
}
```

이 엔드포인트는 간단한 메모처리 엔드포인트이다. 따라서 쓰기 오퍼레이션으로 메모 제출등을 할 수있다.

NotesEndpoint 클래스에는 @Component가 지정되었으므로, 스프링의 컴포넌트 검색으로 시작되고 스프링 애플리케이션

컨텍스트의 빈으로 생성된다. 이 클래스에는 또한 @Endpoint가 지정되었다 따라서 ID가 notes인 액추에이터 엔드포인트가 된다.

그리고 기본적으로 활성화되었으므로 management.web.endpoints.web.exposure.include 구성속성에 포함해 활성화 하지않아도된다.

## 🏓 액추에이터 보안 처리하기

액추에이터는 환경속성과 로깅레벨을 변경할 수 있는 몇가지 오퍼레이션을 제공하므로 유효한 접근 권한을 갖는 클라이언트만이

엔드포인트를 소비할 수 있도록 액추에이터를 보안처리하는것이 좋은 생각이다. 이는 스프링시큐리티에서 처리하면된다.

예를들어 ROLE_ADMIN권한을 갖는 사용자가 액추에이터 엔드포인트를 사용하게 하려면 WebSecurityConfigurerAdapter 클래스의

configure()메소드를 다음과 같이 오버라이딩하면 된다
```
@Override
protected void configure(HttpSecurity http) throws Exception{
    http
        .authorizeRequests()
        .antMatchers("/actuator/**).hasRole("ADMIN")
        .and()
        .httpBasic();
}
```

이 경우 액추에이터 엔드포인트를 사용하려면 ROLE_ADMIN권한을 갖도록 인가한 사용자로부터 요청되어야 한다.

여기서는 또한 클라이언트 애플리케이션이 요청의 Authorization헤더에 암호화된 인증 정보를 제출할 수 있도록 HTTP기본 인증도 구성하였다

이처럼 액추에이터 보안을 처리할 때 유일한 문제점은 엔드포인트의 경로가 /actuator/**로 하드코딩되었다는 것이다. 

따라서 만일 management.endpoints.web.base-path속성이 변경된다면 엔드포인트의 기본경로가 바뀌므로

보안이 처리되지 않을 것이다. 이런 경우를 고려해 스프링 부트는 `EndpointRequest`클래스도 제공한다. 이것은 지정된 문자열

경로에 종속되지 않으면서 보다 더 쉽게 사용할 수 있는 요청 일치 클래스다.`EndpointRequest`를 사용하면 다음과같이 적용할 수 잏ㅆ다.

```
@Override
protected void configure(HttpSecurity http) throws Exception{
    http
        .requestMatcher(EndpointRequest.toAnyEndpoint())
            .authorizeRequests()
                .anyRequest().hasRole("ADMIN")
            .and()
            .httpBasic();
}
```

`EndpointRequest.toAnyEndpoint()`메소드는 어떤 액추에이터 엔드포인트와도 일치하는 요청 matcher를 반환한다.

그리고 요청 matcher로부터 일부 엔드포인트를 제외하고 싶다면 해당 엔드포인트의 이름을 인자로 전달해 `excluding()`메소드를 호출하면된다
```
@Override
protected void configure(HttpSecurity http) throws Exception{
    http
        .requestMatcher(
            EndpointReuqest.toAnyEndpoint()
                    .excluding("health","info"))
            .authorizeRequests()
                .anyRequest().hasRole("ADMIN")
            .and()
            .httpBasic();
}

```
이와는 달리 일부 액추에이터 엔드포인트에만 보안을 적용하고 싶다면 `toAnyEndpoint()`대신 `to()`를 호출하면된다.
```
@Override
protected void configure(HttpSecurity http) throws Exception{
    http
        .requestMatcher(EndpointRequest.to(
                "beans","threaddump","loggers"))
                .authorizeRequests()
                    .anyRequest().hasRole("ADMIN")
                .and()
                .httpBasic();
}
```
이 경우는 /beans,/threaddump,/logger 엔드포인트에만 보안이 적용되고 이외의 다른 모든 액추에이터 엔드포인트는 보안처리되지 않는다.

# 16장 요약

### [1] 스프링 부트 액추에이터는 HTTP와 JMX MBeans 모두의 엔드포인트를 제공한다.

### 엔드포인트는 스프링 부트 애플리케이션의 내부 활동을 볼 수 있게한다.

### [2] 대부분의 액추에이터 엔드포인트는 기본적으로 비활성화된다. 그러나 

### management.endpoints.web.exposure.include속성과 management.endpoints.web.exposure.exclude

### 속성을 설정하여 선택적으로 노출 시킬 수 있다.

### [3] /loggers와 /env 같은 엔드포인트는 실행 중인 애플리케이션의 구성을 실시간으로 변경하는 쓰기오퍼레이션을 허용한다

### [4] 애플리케이션의 빌드와 Git커밋에 관한 상세정보는 /info엔드포인트에서 노출될 수 있다.

### [5] 애플리케이션의 건강 상태는 외부에 통합된 애플리케이션의 건강 상태를 추적하는 커스텀 건강지표에 의해 영향받을수 있다

### [6] 커스텀 애플리케이션 매트릭은 Micrometer를 통해 등록할 수 있다. Micrometer는 벤더 중립적인 메트릭이며

### 애플리케이션이 원하는 어떤 메트릭도 발행하여 서드파티 모니터링 시스템에서 보일 수 있게한다

### [7] 스프링 웹 애플리케이션의 다른 엔드포인트와 마찬가지로 액추에이터 엔드포인트는 스프링 시큐리티를 사용해 보안처리할수있다.





