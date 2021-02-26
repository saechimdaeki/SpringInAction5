# 🥇 13장 서비스 탐구하기

### 이 장에서 배우는 내용

- 마이크로서비스 알아보기
- 서비스 레포지토리 생성하기
- 서비스 등록 및 발견하기

하나의 완전한 애플리케이션 기능을 제공하기 위해 함께 동작하는 자고 독립적인 애플리케이션인 마이크로서비스를

개발하고 등록 및사용하는 방법을 알아보자. 또한, 스프링 클라우드의 가장 유용한 컴포넌트인 유레카와 리본도 알아보자.

## 🍰 마이크로 서비스 이해하기

작고 간단한 애플리케이션을 개발할때 단일파일로 애플리케이션을 배포하는것은 좋은방법이다. 그러나 작은 애플리케이션이 점점 더

커지게 된다는 것이 문제다. 결국 새로운 기능이 필요할 때마다 더 많은 코드가 추가되어야 하므로 주체하기 어렵고

복잡한 단일 애플리케이션이 된다.

---

#### 단일 애플리케이션은 언 뜻 보기에 간단하지만 다음과 같은 문제가 따른다.

### [1] 전체를 파악하기 어렵다: 코드가 점점 더 많아질수록 애플리케이션에 있는 각 컴포넌트 역할을 알기 어려워진다.

### [2] 테스트가 더 어렵다: 애플리케이션이 커지면서 통합과 테스트가 더복잡해진다

### [3] 라이브러리 간 충돌생기기쉽다: 애플리케이션의 한 기능에서 필요한 라이브러리 의존성이 다른

### 기능에서 필요한 라이브러리 의존성과 호환되지 않을 수 있다.

### [4] 확장시에 비효율적이다: 시스템 확장을 목적으로 더 많은 서버에 애플리케이션을 배포해야

### 할때는 애플리케이션의 이룹가 아닌 전체를 배포해야한다. 애플리케이션 기능의 일부만 확장하더라도 마찬가지

### [5] 적용할 테크놀로지를 결정할때도 애플리케이션 전체를 고려해야한다: 애플리케이션에 사용할 프로그래미언어

### 런타임 플랫폼, 프레임워크, 라이브러리를 선택할 때 애플리케이션 전체를 고려하여 선택해야함

### [6] 프로덕션으로 이양하기 위해 많은 노력이 필요: 애플리케이션을 한 덩어리로 배포하므로 프로덕션

### 으로 이양하는 것이 더 쉬운 것처럼 보일 수 있다. 그러나 일반적으로 단일 애플리케이션은 크기와

### 복잡도 때문에 더 엄격한 개발 프로세스와 더욱 철두철미한 테스트가 필요하다. 고품질과 무결함을 보장하기 위해서다.

이렇게 단일 애플리케이션의 문제를 해결하기 위해 `마이크로서비스 아키텍쳐`가 발전하였다. 간단히 말해 마이크로 서비스

아키텍쳐는 개별적으로 개발되고 배포되는 소규모의 작은 애플리케이션들로 애플리케이션을 만드는 방법이다.

마이크로서비스는 상호 협력하여 더 큰 애플리케이션의 기능을 가지며 다음과 같은특성을 가진다.

### [1] `마이크로서비스는 쉽게 이해할 수 있다`: 다른 마이크로 서비스와 협력할 때 각 마이크로서비스는

### 작으면서 한정된 처리를 수행한다. 따라서 마이크로서비스는 자신의 목적에만 집중하므로 이해하기쉽다.

### [2] `마이크로서비스는 테스트가 쉽다`: 크기가 작을수록 테스트가 쉬워지는 것은 분명한 사실이다.

### [3] `마이크로서비스는 라이브러리 비호환성 문제가 생기지 않는다`: 각 마이크로서비스는 다른

### 마이크로서비스와 공유되지 않는 빌드 의존성을 가지므로 라이브러리 충돌문제가 생기지않는다.

### [4] `마이크로서비스는 독자적으로 규모를 조정할 수 있다`: 만일 특정 마이크로 서비스의 규모가 더 커야한다면

### 애플리케이션의 다른 마이크로서비스에 영향을 주지않고 메모리할당이나 인스턴스의 수를 크게조정할수있다.

### [5] `각 마이크로서비스에 적용할 테크놀로지를 다르게 선택할수있다`: 각 마이크로서비스에 사용할

### 프로그래밍 언어, 플랫폼, 프레임워크,라이브러리를 서로 다르게 선택할 수 있다.

### [6] `마이크로서비스는 언제든 프로덕션으로 이양할 수 있다`s: 마이크로서비스 아키텍처 기반으로

### 개발된 애플리케이션이 여러개의 마이크로서비스로 구성되었더라도 각 마이크로서비스를 따로 배포할수있다.

## 🏓 서비스 레지스트리 설정하기

스프링 클라우드는 큰 프로젝트이며 마이크로서비스 개발을 하는데 필요한 여러 개의 부속 프로젝트로 구성된다.

이 중 하나가 스프링 넷플렉스이며 이것은 넷플렉스 오픈소스로부터 다수의 컴포넌트를 제공한다.

이 컴포넌트 중에 넷플렉스 서비스 레지스트리인 유레카가있다.

### 🌟 유레카란????

유레카는 마이크로서비스 애플리케이션에 있는 모든 서비스의 중앙 집중 레지스트리로 작동한다. 유레카 자체도 마이크로서비스로

생각할 수 있으며 더 큰 애플리케이션에서 서로다른 서비스들이 서로를 찾는데 도움을 주는것이 목적이다

#### 다른 서비스가 찾아서 사용할ㅊ 수 있도록 각 서비스는 유레카 서비스 레지스트리에 자신을 등록한다.

![image](https://user-images.githubusercontent.com/40031858/109242774-136a8780-781f-11eb-852a-c02b6673f91a.png)

유레카는 마이크로서비스 애플리케이션에 있는 모든 서비스의 중앙 집중 레지스트리로 작동한다.

유레카 자체도 마이크로서비스로 생각할 수 있으며, 더 큰 애플리케이션에서 서로 다른 서비스들이 서로를 찾는데 도움을 주는게 목적이다.

위의 그림대로 서비스 인스턴스가 시작될 때 해당 서비스는 자신의 이름을 유레카에 등록한다. some-service가

서비스 이름이며 some-service의 인스턴스는 여러개 생성될 수 있다. 그러나 이것들 모두 같은 이름으로 유레카에 등록된다.

어느 순간에는 다른서비스가 some-service를 사용해야한다. 이때 some-service의 특정 호스트 이름과

포트 정보를 other-service 코드에 하드코딩하지 않는다. 대신에 other-service는 some-service라는

이름을 유레카에서 찾으면된다. 그러면 유레카는 모든 some-service인스턴스의 정보를 알려준다.

다음으로 other-service는 some-service의 어떤 인스턴스를 사용할지 결정해야한다. 이때 특정 인스턴스를

매번 선택하는 것을 피하기 위해 클라이언트 측에서 동작하는 로드 밸런싱 알고리즘을 적용하는 것이 가장좋다.

바로 이때 사용될 수 있는 또 다른 넷플릭스 프로젝트인 리본이다.

some-service의 인스턴스를 찾고 선택하는 것은 other-service가 해야할 일이지만, 이것을 리본에게 맡길 수 있다.

리본은 other-service를 대신하여 some-service인스턴스를 선택하는 클라이언트 측의 로드 밸런서이다.

그리고 other-service는 리본이 선택하는 인스턴스에 대해 필요한 요청을 하면 된다.

### 🌟 클라이언트 측의 로드 밸런서를 사용하는 이유

로드밸런서로는 주로 단일 중앙 집중화된 서비스가 서버측에서 사용되었다. 그러나 이와 반대로 리본은

각 클라이언트에서 실행되는 클라이언트 측의 로드밸런서이다. 클라이언트 측의 로드 밸런서인 `리본`은 중앙 집중화된 로드 밸런서

에 비해 몇가지 장점을 갖는다. 각 클라이언트에 하나의 로컬 로드 밸런서가 있으므로 클라이언트의 수에 비례하여 자연스럽게 로드 밸런서의

크기가 조정된다. 또한, 서버에 연결된 모든 서비스에 획일적으로 같은 구성을 사용하는 대신, 로드 밸런서는

각 클라이언트에 가장 적합한 로드 밸런싱 알고리즘을 사용하도록 구성할 수 있다.

이런과정은 복잡하지만 걱정하지 않아도된다. 이런일의 대부분은 자동으로 처리되기 때문이다. 그러나 서비스를 등록하고 사용하려면

우선 유레카 서버를 활성화 해야한다.

#### 유레카 서버 스타터 의존성

```
<dependencies>
    ...
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflux-eureka-serverß</artifactId>
    </dependency>
</dependencies>
...
```

#### 스프링 클라우드 버전 의존성

```
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupdId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>${spring-cloud.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

또한 spring-cloud.version 속성의 값은 < properties>에 자동 설정되어 있을것이다

```
<properties>
...
    <spring-cloud.version>Hoxton.SR3</spring-cloud.version>
</properties>
```

만일 다른버전의 스프링 클라우드를 사용하고 싶을때는 spring.cloud.version 속성의 값만 원하는 것으로 변경하면된다.

이제 유레카 스타터 의존성이 지정되었으므로 유레카 서버를 활성화시키면된다

```
@SpringBootApplication
@EnableEurekaServer
public class ServiceRegistryApplication{
    public static void main(String[] args){
        SpringApplication.run(ServiceRegistryApplicastion.class,args);
    }
}
```

### 🌟 유레카 구성하기
하나보다는 여러 개의 유레카 서버가 함께 동작하는 것이 안전하므로 유레카 서버들이 클러스터로 구성되는 것이 좋다

왜냐하면 여러개의 유레카 서버가 있을 경우 그중 하나에 문제가 발생해도 단일문제점은 생기지 않기때문이다.

따라서 기본적으로 유레카는 다른 유레카 서버로부터 서비스 레지스트리를 가져오거나 다른 유레카 서버의 서비스로 자신을 등록하기도한다.

실무환경 설정에서는 유레카의 고가용성이 바람직하다. 그러나 개발 시에 두 개 이상의 유레카 서버를 실행

하는 것은 불편하기도 하고 불필요하다. 개발 목적으로는 하나의 유레카 서버면 충분하기 때문. 그러나 유레카 서버를 올바르게

구성하지 않으면 30초마다 예외형태로 로그메시지를 출력한다. 왜냐하면 유레카는 30초마자 다른 유레카 서버와

통신하면서 자신이 작동 중임을 알리고 레지스트리 정보를 공유하기때문이다. 따라서 유라케 서버가 혼자임을 알도록 구성하자

```
server:
  port: 8761
eureka:
  instance:
    hostname: localhost
  client:
    fetchRegistry: false
    registerWithEureka: false
    serviceUrl:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
```

여기서 server.port 속성을 8761로 eureka.instance.hostname 속성을 localhostfh 설정하였다.

이것은 유레카가 실행되는 호스트 이름과 포트를 나타내며 생략가능하다.

eureka.clien.fetchRegistry와 eureka.client.registerWithEureka는 유레카와 상호 작용하는 

방법을 알려주기 위해 다른 마이크로서비스에 설정할 수 있는 속성들이다. 그러나 유레카 역시 마이크로

서비스이므로 이 두속성은 해당 유레카 서버가 다른유레카 서버와 상호작용하는 방법을 알려주기위해 사용할 수 있다.

두 속성의 기본값은 true이며 해당 유레카 서버가 다른 유레카 서버로부터 레지스트리 정보를 가져오며, 다른 유레카 서버의

서비스로 자신을 등록헤야 한다는 것을 나타낸다. 위의예에서는 다른유레카 서버들이 필요없어서 false로 설정하였다.

마지막으로 eureka.client.serverUrl속성을 설정하였다. 이 속성은 영역이름과 이 영역에 해당하는 하나이상의

유레카 서버 URL을 포함하며, 이 값은 Map에 저장된다. Map의 키인 defaultZone은 클라이언트가 자신이

원하는 영역을 지정하지않았을때 사용된다. 여기서는 유레카가 하나만 있으므로 defaultZone에 해당하는 URL이 유레카

자신의 URL을 나타내며 중괄호 안에 지정된 다른 속성의 값으로 대체된다.

### 🌟 자체-보존 모드를 비활성화시키기
설정을 고려할 수 있는 다른 속성으로 eureka.server.enableSelfPreservation이 있다.

유레카 서버는 서비스 인스턴스가 자신을 등록하고 등록 갱신요청을 30초마다 전송하기를 기대한다. 일반적으로 

세번의 갱신기간동안 서비스 인스턴스로부터 등록갱신요청을 받지못하면 해당 서비스 인스턴스의 등록을 취소하게 된다.

그리고 만일 이렇게 중단되는 서비스의 수가 임계값을 초과하면 유레카 서버는 문제가 생긴 것으로 간주하고

레지스트리에 등록된 나머지 서비스 데이터를 보존하기 위해 자체-보존모드가 된다. 따라서 추가적인 서비스 인스턴스 등록취소가 방지된다.

## 🍰 유레카 확장하기

개발 시에 단일 유레카 인스턴스가 더 편리하지만, 애플리케이션을 프로덕션으로 이양할 때는 고가용성을

위해 최소한 두 개의 유레카 인스턴스를 가져야한다.

### 🌟 프로덕션 환경의 클라우드 서비스
마이크로서비스를 프로덕션 환경으로 배포할때는 고려할 것이 많다. 유레카의 고가용성과 보안은 개발 시에는

중요하지 않은 관점들이지만 프로덕션에서는 매우 중요하기 때문이다. 만일 피보탈클라우드 파운드리나

피보탈 웹 서비스의 고객이라면 해당 사항에 대한 기술지원을 받을 수 있을것이므로 걱정할필욘없다.

스프링클라우드 서비스에서는 구성서버와 서킷브레이커 대시보드는 물론 서비스 레지스트리인 유레카의

프로덕션버전또한 제공한다(https://docs.pivotal.io/spring-cloud-services/2-0/common/index.html)

두 개 이상의 유레카 인스턴스를 구성하는 가장 쉽고 간단한 방법은 application파일에 스프링 프로파일을 지정하는 것이다.

그리고 그다음 한번에 하나씩 프로파일을 사용해 유레카를 두번 시작하면된다.

#### 스프링 프로파일을 사용해 두개의 유레카 구성하기
```
eureka:
  client:
    service-url:
      defaultZone:http://${othoer.eureka.host}:${other.eureka.port}/eureka
---
spring:
  profiles: eureka-1
  application:
    name: eureka-1
server:
  port: 8761

eureka:
  instance:
    hostname: eureka1.tacocloud.com

other:
  instance:
    hostname: eureka2.tacocloud.com
    port: 8761

---
spring:
  profiles: eureka-2
  application:
    name: eureka-2

server:
  port:8762

eureka:
  instance:
    hostname: eureka2.tacocloud.com

other:
  eureka:
    host: eureka1.tacocloud.com
    port: 8762
```

제일 앞의 기본 프로파일에는 eureka.client.serviceurl.defaultZone을 설정했으며 여기에 지정된

other.eureka.host와 other.eureka.port 변수의 값은 그다음에 있는 각 프로파일 구성에서 설정된 값으로 대체된다.

기본 프로파일 다음에는 두 개의 프로파일인 eureka-1과 eureka-2가 구성되어 있으며, 각 프로파일에는 자신의 포트와

eureka.instance.hostname이 설정되어있다. 그리고 각 프로파일에 설정된 다른 유레카 인스턴스를

참조하기 위해 other.eureka.host와 other.eureka.port속성도 설정되어있다. 이속성들은 프레임워크와

관련없으며 기본프로파일에 지정된 other.eureka.host와 other.eureka.port변수의 값을 대체하기 위해필요하다.

eureka.client.fetchRegistry나 eureka.client.registerWithEureka를 설정하지않았는데 이 속성들을 설정하지 않으면

기본값인 true가된다. 따라서 `각 유레카 서버가 다른 유레카 서버에 자신을 등록하고 레지스트리의 등록정보를 가져온다`

## 🏓 서비스 등록하고 찾기

서비스가 등록되지 않으면 유레카 서비스 레지스트리는 쓸모가없다. 우리 서비스는 다른 서비스에서 찾아 사용하게 하려면 

유레카 서비스 레지스트리의 클라이언트로 활성화 시켜야한다.

#### 유레카 클라이언트 스타터 의존성 추가하기
```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

이렇게 의존성을 지정하면 유레카를 이용해 서비스를 찾는데 필요한 모든것이 자동으로 추가된다. 예를들어,

유레카의 클라이언트 라이브러리, 리본 로드밸런서 등이다. 따라서 우리 애플리케이션을 유레카 서비스 레지스트리의

클라이언트로 활성화시킬 수 있다. 즉, 애플리케이션이 시작된 8761포트로 서비스하는 유레카 서버에 연결하고

UNKNOWN이라는 이름으로 유레카에 애플리케이션 자신을 등록한다

### 🌟 유레카 클라이언트 속성 구성하기

서비스의 기본이름인 UNKNOWN을 그대로 두면 유레카 서버에 등록되는 모든 서비스 이름이 같게 되므로 변경해야한다.

이때 spring.application.name 속성을 설정하면 된다. 예를들어, 타코 식자재를 처리하는 서비스의 

경우에는 Ingredient-service라는 이름으로 유레카 서버에 등록할수있다. 이경우 application.yml에 다음과같이 설정한다
```
spring:
  application:
    name: ingredient-service
```

이렇게 하면 이서비스를 사용하는 서비스에는 ingredient-service라는 이름으로 이 서비스를 찾을 수 있다.

스프링 클라우드를 계속 사용하는 동안 spring.application.name이 우리가 설정하는 가장 중요한 속성 중 하나라는것을 알게될것이다.

다음 장에서는 애플리케이션에 특정된 구성을 관리하기 위해 이 속성으로 애플리케이션을 식별한다는것을 알게될 것이다.

스프링 클라우드 태스크와 스프링 클라우드 슬루스같은 다른 스프링 클라우드 프로젝트에서도 서비스를

식별하기 위해 spring.application.name 속성을 사용한다.

모든 스프링MVC와 스프링 WebFlux애플리케이션은 기본적으로 8080포트를 리스닝하지만 서비스는 유레카를 통해서만

찾게 될 것이므로, 애플리케이션이 리스닝하는 포트는 상관없다. 서비스가 리스닝하는 포트를 유레카가 알고있기때문이다.

따라서 localhost에서 실행될 때 생길 수 있는 서비스의 포트 충돌을 막기위해 각 서비스 애플리케이션의 포트번호를 0으로설정할수있다.
```
server:
  port: 0
```
이처럼 포트를 0으로 설정하면 서비스 애플리케이션이 시작될 때 포트번호가 무작위로 선택된다.

또 기본적으로 유레카 클라이언트는 유레카 서버가 localhost의 8761포트로 리스닝한다고 간주한다.

이것은 개발시에 좋지만 프로덕션에서는 적합하지 않으므로 유레카서버의 위치를 지정해야한다. 다음과같이 지정할수있다.
```
eureka:
  client:
    service-url:
      defaultZone: http://eureka1.tacocloud.com:8761/eureka/
```

이렇게하면 eureka1.tacocloud.com의 8761포트로 리스닝하는 유레카 서버에 등록되도록 클라이언트가 구성된다.

이 경우 해당 유레카 서버가 제대로 작동중이라면 문제가 없다. 그러나 만일 어떤 이유로든 해당 유레카 서버가

중단된다면 클라이언트 서비스가 등록되지않을것이다. 따라서 이것을 방지하기 위해 두 개이상의

유레카 서버를 사용하도록 클라이언트 서비스를 구성하는게좋다

```
eureka:
  client:
    service-url:
      defaultZone: http://eureka1.tacocloud.com:8761/eureka/,
                   http://eureka2.tacocloud.com:8762/eureka/
```
이렇게하면 해당 서비스가 첫 번째 유레카 서버에 등록을 시도한다. 그러나 만일 어떤이유로든 등록에 실패하면

두번 째에 피어로 지정된 유레카 서버의 레지스트리에 등록을 시도하게 된다. 그리고 이후에 등록에 실패했던 유레카

서버가 다시 온라인 상태가 되면, 해당 서비스의 등록 정보가 포함된 피어 서버 레지스트리가 복제된다.

### 🌟 서비스 사용하기

서비스를 사용하는 컨슈머코드에 해당 서비스 인스턴스의 URL을 하드코딩하는 것은 좋지않다.

이 경우 사용되는 서비스의 특정 인스턴스와 해당 컨슈머가 밀접하게 결합되는 것은 물론이고 사용되는 서비스의

호스트나 포트가 변경될 경우 해당 컨슈머의 실행 중단을 초래할 수 있기 때문이다.

유레카 서버에서 서비스를 찾을 때 컨슈머 애플리케이션이 할 일이 있다. 즉, 같은 서비스의 인스턴스가 여러 개일때도

유레카 서버는 서비스 검색에 응답할 수 있다. 만ㅁ일 컨슈거가 ingredient-service라는 서비스를 요청 했는데

6개 정도의 서비스 인스턴스가 반환된다면 어떻게 처리할 수 있을까???

이 경우 컨슈머 애플리케이션은 `자신이 서비스 인스턴스를 선택하지 않아도 되며, 특정 서비스 인스턴스를 명시적으로 찾을 필요도없다`

스프링 클라우드의 유레카 클라이언트 지원에 포함된 리본클라이언트 로드밸런서를 사용하여 서비스 인스턴스를 쉽게 찾아

선택하고 사용할 수 있기 때문이다. 유레카 서버에서 찾은 서비스를 선택 및 사용하는방법은 두가지가있다.

- 로드 밸런싱된 RestTemplate
- Feign에서 생성된 클라이언트 인터페이스

### 🌟  RestTemplate 사용해서 서비스 사용하기
RestTemplate이 생성되거나 주입되면 HTTP요청을 수행하여 원하는 응답을 받을 수 있다.

식자재 ID로 특정 식자재를 가져오기위해 HTTP GET요청을 수행할때는 다음의 코드를 사용할 수 있다

```
public Ingredient getIngredientById(String ingredientId){
    return rest.getForObject("http://localhost:8080/ingredients/{id}", Ingredient.class,ingredientId);
}
```
이 코드에는 문제점이 있는데 getForObject()의 인자로 전달되는 URL이 특정 호스트와 포트로 하드코딩되었다는것이다.

일단 유레카 클라이언트로 애플리케이션을 활성화했다면 로드밸런싱된 `RestTemplate`빈을 선언할 수 있다.

이때는 기존대로 `RestTemplate`빈을 선언하되, `@Bean`과 `LoadBalanced`애노테이션을 메소드에 같이지정하면된다.

```
@Bean
@LoadBalanced
public RestTemplate restTemplate(){
    return new RestTemplate();
}
```

`@LoadBalanced` 애노테이션은 다음 두가지 목적을 갖는다. 첫 번째이면서 가장 중요한 것으로 현재의 

`RestTemplate`이 리본을 통해서만 서비스를 찾는다는 것을 스프링 클라우드에 알려준다.

두번째로 `주입식별자`로 동작한다. `주입식별자`는 서비스이름이며 getForObject()메소드의 HTTP요청에서

호스트와 포트 대신 사용할 수있다. 예를들어, 식자재를 찾기위해 로드 밸런싱된 `RestTemplate`을 사용하고싶다고하자.

이때는 우선, 로드밸런싱된 RestTemplate을 필요로 하는 빈에 주입해야한다

```
@Component
public class IngredientServiceClient{
    private RestTemplate rest;

    public IngredientServiceClient(@LoadBalanced RestTemplate rest){
        this.rest=rest;
    }
    ...
}
```

그 다음에 getIngredienById() 메소드에서 호스트와 포트 대신 해당 서비스의 등록된 이름을 사용하도록 변경한다.

```
public Ingredient getIngredeintById(String ingredientId){
    return rest.getForObject(
        "http://ingredient-service/ingredients/{id}",
        Ingredient.class, ingredeintId);
}
```
여기서는 `getForObject()`의 인자로 전달되는 URL에 특정 호스트 이름과 포트를 사용하지않는다.

즉 호스트 이름과 포트 대신 서비스 이름인 ingredient-service가 사용되었다. 내부적으로는 ingredient-service라는

서비스 이름을 찾아 인스턴스를 선택하도록 `RestTemplate`이 리본에 요청한다. 그리고 선택된 서비스

인스턴스의 호스트와 포트정보를 포함하도록 리본이 URL을 변경한 후 원래대로 RestTemplate이 사용된다.

이렇게 로드밸런싱된 RestTemplate을 사용하는 방법은 보통의 RestTemplate을 사용하는 방법과 별반 다르지않다.

단지 `차이점이라면 클라이언트 코드에서 호스트 이름과 포트대신서비스이름으로 처리할 수 있다는 것뿐이다.`

### 🌟 WebClient로 서비스 사용하기

`WebClient`를 사용한다면 RestTemplate과 같은방법으로 로드밸런싱된 클라이언트로 사용할 수 있다.

이때 제일 먼저 할 일은 `@LoadBalanced`애노테이션이 지정된 `WebClient.Builder` 빈메서드를 선언하는것이다.

```
@Bean
@LoadBalanced
public WebClient.Builder webClientBuilder(){
    return WebClient.builder();
}
```

그리고 `WebClient.Builder`빈이 선언되었으므로 이제는 로드 밸런싱된 `WebClientBuilder`를 필요로 하는 어떤빈에도 주입할수있다.

예를들어 다음과같이 IngredientServiceClient의 생성자로 주입할 수 있다.

```
@Component
public class IngredientServiceCLient{
    private WebClient.Builder wcBuilder;

    public IngredientServiceClient(
        @LoadBalanced WebClient.Builder wcBuilder){
            this.wcBuilder=wcBuilder;
        }
    ...
}
```

그다음에 WebClient.Builder를 사용해서 WebClient를 빌드한 후 유레카에 등록된 서비스이름을 사용해 요청을 수행할수있다.

```
public Mono<Ingredient> getIngredientById(String ingredientId){
    return wcBuilder.build()
            .get()
            .uri("http://ingredient-service/ingredients/{id}",ingredientId)
        .retrieve().bodyToMono(Ingredient.class);
}
```

이 경우 로드 밸런싱된 RestTemplate처럼 호스트나 포트를 지정할 필요가없다. 즉, 해당 서비스 이름이 URL에서

추출되어 유레카에서 서비스를 찾는데 사용된다. 그리고 리본이 해당 서비스의 인스턴스를 선택한 후 선택된

인스턴스의 호스트와 포트로 URL이 변경되어 요청이 수행된다.

### 🌟 Feign 클라이언트 인터페이스 정의하기
`Feign`은 REST클라이언트 라이브러리이며, 인터페이스를 기반으로 하는 방법을 사용해서 REST 클라이언트를 정의한다. 

간단히 말해 스프링 데이터가 레포지토리 인터페이스를 자동으로 구현하는 것과 유사한 방법을 사용한다.

Feign은 원래 넷플릭스 프로젝트였지만, 나중에 OpenFeign(https://github.com/OpenFeign)이라는 독립된 오픈소스프로젝트가되었다.

일단 다음과 같은 의존성을 추가해야 사용할 수 있다.
```
...
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
...
```

의존성을 추가해도 자동-구성으로 Feign이 활성화되지는 않는데 다음과 같이 구성클래스중하나에
`@EnableFeignClients`애노테이션을 추가해야한다

```
@Configuration
@EnableFeignClients
public RestClientConfiguration{
}
```

이제 Feign을 사용할 때가 되었다. 예를들어 ingredient-service라는 이름으로 유레카에 등록된 서비스를

사용해서 식자재를 가져오는 클라이언트를 작성하고 싶다면 다음과같이 인터페이스만 정의하면된다

```
@FeignClient("ingredient-servcice")
public interface IngredientClient{
    @GetMapping("/ingredients/{id}")
    Ingredient getIngredient(@PathVariable("id") String id);
}
```
구현코드가 없는 간단한 인터페이스지만 런타임시에 Feign이 이인터페이스를 찾으므로 아무 문제가없다.

그리고 Feign이 자동으로 구현 클래스를 생성한 후 스프링 애플리케이션 컨텍스트에 빈으로 노출된다.

우선 IngredientCLient 인터페이스에 선언된 모든 메소드는 서비스 `@FeignClient`애노테이션이다.

내부적으로 ingredient-service는 리본을 통해 찾게된다. 그리고 getIngredient()메소드에는 @GetMapping이 지정되었다.

이것은 스프링 MVC에서 사용했던 것과 같은 애노테이션이며 여기서는 컨트롤러 대신 클라이언트에 저장되어 있다.

이제 정의한 Feign이 구현한 IngredientClient인터페이스를 필요로하는곳에 주입하고 사용해보자

```
@Controller
@RequestMapping("/ingredients")
public class IngredientController{
    private IngredientClient client;

    @Autowired
    public IngredientController(IngredientClient client){
        this.client=client;
    }

    @GetMapping("/{id}")
    public String ingredientDetailPage(@PathVarible("id") String id,
        Model model){
            model.addAttribute("ingredient",client.getIngredient(id));
            return "ingredientDetail";
        }
}
```

또한 Feign에는 자신의 애노테이션인 `@RequestLine`과 `@Param`이 있다. 이 애노테이션들은 스프링 MVC의

`@RequestMapping`및 `@PathVariable`과 거의 유사하지만 용도는 약간다르다.

---


# 프로젝트 실행 방법
### 🌟 springboot최신버전에 맞춰서 클라우드버전도 변경하여 동작하게끔 코드를 변경하였습니다.
```
1. cd service-registry
2. ./mvnw clean package
3. cd ingredient-service
4. ./mvnw clean package
5. cd ingredient-client
6. ./mvnw clean package

7. java -jar target/demo-0.0.1-SNAPSHOT.jar
8. java -jar target/demo-0.0.1-SNAPSHOT.jar
9. java -jar -Dspring.profiles.active=webclient target/demo-0.0.1-SNAPSHOT.jar

^__^
```

# 13장 요약
#### [1]스프링 클라우드 넷플렉스는 자동-구성과 @EnableEurekaServer 애노테이션을 사용해서 넷플렉스 유레카

#### 서비스 레지스트리를 쉽게 생성할 수 있다

#### [2] 다른 서비스가 찾을 수 있도록 마이크로서비스는 이름을 사용해서 자신을 유레카 서버에 등록한다

#### [3] 리본은 클라이언트 측의 로드 밸런서로 동작하면서 서비스 이름으로 서비스 인스턴스를 찾아 선택한다

#### [4] 리본 로드 밸런싱으로 처리되는 RestTemplate, 또는 Feign에 의해 자동으로 구현되는 인터페이스를 사용해서

#### 클라이언트 코드는 자신의 REST클라이언트를 정의할 수 있다

#### [5] 로드 밸런싱된 RestTemplate, WebClient 또는 Feign 클라이언트 인터페이스 중 어느 것을 사용하더라도

#### 서비스의 위치(호스트 이름과 포트)가 클라이언트 코드에 하드코딩되지않는다










