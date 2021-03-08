# ⚡ 17장 스프링 관리하기

### 🌟 이 장에서 배우는 내용
- 스프링 부트 Admin 설정하기
- 클라이언트 애플리케이션 등록하기
- 액추에이터 엔드포인트 소비하기
- Admin 서버의 보안

이전 장에서는 스프링 부트 엑추에이터가 노출시킨 모든 HTTP 엔드포인트를 살펴보았다. 이것은 JSON응답을

반환하는 HTTP엔드포인트이며, 사용될 수 있는 방법에 제한은 없다. 이번 장에서는 이런 엔드포인트를 더 쉽게

사용할 수 있도록 엑추에이터의 상위 계층에 프론트엔드 사용자 인터페이스를 생성하고 엑추에이터로부터 직접 사용학기

어려운 실시간 데이터를 캡쳐하는 방법을 알아본다.

## 🍰 스프링 부트 Admin 사용하기

`스프링 부트 Admin`은 관리용 프론트엔드 웹 애플리케이션이며 엑추에이터 엔드포인트를 사람들이 더 많이 소비할 수 

있게 한다. 엑추에이터 엔드포인트는 두 개의 주요 구성 요소로 나뉜다. 스프링부트 Admin과 이것의 클라이언트 들이다.

### 🌟 Admin서버 생성하기

Admin서버를 활성화 하려면 새로운 스프링 부트 애플리케이션을 생성하고 Admin 서버 의존성을 프로젝트의 빌드에 추가해야한다.

일반적으로 Admin서버는 독립 실행형애플리케이션으로 사용된다.


```
<dependency>
    <groupId>de.codecentric</groupId>
    <artifactId>spring-boot-admin-starter-server</artifactId>
</dependency>
```
그리고 구성클래스에 @EnableAdminServer 애노테이션을 지정해 Admin서버를 활성화 해야한다
```
@SrpingBootApplication
@EnableAdminServer
public class BootAdminServerApplication{
    public static void main(String[] args){}{
        SpringApplication.run(BootAdminServerApplcation.class,args);
    }
}
```

이제 실행하면 spring boot admin을볼수있다. 하지만 Admin서버가 유용하게 쓰이려면 클라이언트 애플리케이션을 등록해야 한다.

### 🌟 Admin 클라이언트 등록하기

Admin서버는 다른 스프링 부트 애플리케이션의 엑추에이터 데이터를 보여주는 별개의 애플리케이션이므로 다른 애플리케이션을 

Admin서버가 알 수 있도록 클라이언트로 등록해야한다. Admin클라이언트를 Admin서버에 등록하는 방법은 다음 두가지가있다.
- 각 애플리케이션이 자신을 Admin서버에 등록한다
- Admin 서버가 유레카 서비스 레지스트리를 통해서 서비스를 찾는다.

#### `Admin 클라이언트 애플리케이션 구성하기`
스프링 부트 애플리케이션이 자신을 Admin서버의 클라이언트로 등록하려면 해당 애플리케이션의 빌드에 스프링 부트 Admin

클라이언트 스타터를 포함시켜야한다.
```
<dependency>
    <groupId>de.codecentric</groupId>
    <artifactId>spring-boot-admin-starter-client</artifactId>
</dependency>
```
의존성을 추가해 클라이언트 측의 라이브러리가 준비되었으므로 클라이언트가 자신을 등록할 수 있는

Admin서버의 위치도 구성해야한다. 이대는 spring.boot.admin.client.url속성을 Admin서버의 루트 URL로 설정하면된다.

#### `Admin클라이언트 찾기`
서비스들을 찾을 수 있게 Admin서버를 활성화 할때는 Admin서버 프로젝트의 빌드에 스프링 클라우드 Netflix

유레카 클라이언트만 추가하면된다.

```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```
이렇게하면 유레카에 등록된 모든 애플리케이션을 Admin 서버가 자동으로 찾아서 그것들의 엑추에이터 데이터를 보여준다.

## 💕 Admin 서버 살펴보기
모든 스프링 부트 애플리케이션이 Admin서버 클라이언트로 등록되면 각 애플리케이션 내부에서 생기는 풍부한

정보를 Admin서버가 볼수있다. 이러한 정보에는 다음 사항이 포함된다.
- 애플리케이션의 건강상태 정보와 일반정보
- Micrometer를 통해 발행되는 메트릭과 /metrics 엔드포인트
- 환경 속성
- 패키지와 클래스의 로깅 레벨
- 스레드 추적 기록 정보
- HTTP 요청의 추적 기록
- 감사 로그

사실상 액추에이터가 노출하는 거의 모든것을 훨씬 더 인간 친화적인 형태로 Admin서버에서 볼 수 있다.

여기에는 정보 추출과 파악에 도움을 주는 그래프와 필터가 포함된다.

#### `애플리케이션의 건강 상태 정보와 일반 정보 보기`
액추에이터가 제공하는 가장 기본적인 정보 중에 건강상태와 일반정보가 있으며 이정보들은 /health와 /info엔드포인트를

통해 제공된다.

Admin 서버는 Details 탭에서 이정보를 보여준다
![image](https://user-images.githubusercontent.com/40031858/110307300-1a28a400-8042-11eb-9401-361fe27dbe1d.png)

메모리와 스레드를 보여주는 그래프들과 프로세스 정보가 포함되며 각종 그래프 및 프로세스와

가비지 컬렉션의 메트릭에 보여지는 정보는 애플리케이션이 JVM리소스를 어떻게 사용하는지 살표보는데 유용하다.


#### `핵심 메트릭 살펴보기`

/metrics 엔드포인트로부터 제공되는 정보는 애플리케이션에서 생성되는 메트릭이며, 모든 액추에이터 엔드포인트 중 가장 

덜 인간 친화적인 형태일것이다. 그러나 Admin서버가 Metrics 탭의 UI를 사용해서 일반인이 알아보기 쉽게한다.

처음에는 Metrics 탭에서 어떤 메트릭 정보도 보여주지 않는다. 그러나 계속 지켜볼 메트릭에 대해 하나 이상의 관찰점을 설정하면

이것에 대한 정보를 보여준다.

#### `환경 속성 살펴보기`
액추에이터의 /env 엔드포인트는 스프링 부트 애플리케이션의 모든 속성 근원으로부터 해당 애플리케이션에 사용할 수 있는

모든 환경속성을 반환한다. 이렇게 반환되는 JSON 응답이 알아보기 어려운 것은 아니다. 그러나 Admin서버는

Environment 탭에서 훨씬 더 보기 좋은 형태로 응답을 보여준다.

#### `로깅 레벨을 보거나 설정하기`
액추에이터의 /loggers엔드포인트는 실행 중인 애플리케이션의 로깅 레벨을 파악하거나 변경하는데 도움이 된다.

Admin서버의 Loggeers탭에는 애플리케이션의 로깅 레벨 관리 작업을 쉽게할수있도록 사용이 쉬운 UI가추가되어있다.

기본적으로 Admin서버는 모든 패키지와 클래스의 로깅레벨을 보여주지만, 이름이나 로깅 레벨로 필터링 할 수 있다.

#### `스레드 모니터링`

어떤 애플리케이션이든 많은 스레드가 동시에 실행될 수 있다. /threaddump엔드포인트는 애플리케이션에서 실행 중인

스레드의 상태 스냅샷을 제공한다. 스프링 부트 Admin UI는 애플리케이션의 모든 스레드에 대해 실시간으로 감시한다.

#### `HTTP요청 추적하기`

스프링 부트 Admin UI의 HTTP Traces 탭에서는 액추에이터의 /httptrace 엔드포인트로부터 받은 데이터를 보여준다.

그러나 요청 시점에 100개의 가장 최근 HTTP추적 기록을 반환하는 /httptrace엔드포인트와 다르게,

HTTP Traces 탭은 HTTP요청들의 전체 이력 데이터를 보여준다. 그리고 이 탭에 머무는 동안 이력 데이터가 계속 변경된다. 만일 이 탭을

떠낫다가 다시 돌아오면 처음 100개의 가장 최근 요청들만 보여주지만, 이후로는 추적이 계속된다.

## 🏓 Admin서버의 보안

이전 장에서 이야기했듯 액추에이터의 엔드포인트에서 노출한 정보는 일반적인 사용을 위해 생성된 것이 아니다.

여기에는 애플리케이션 관리자만이 봐야하는 애플리케이션 상세 내역을 노출하는 정보가 포함된다. 더욱이

엔드포인트에는 아무에게나 함부로 노출되면 안되는 정보의 변경을 허용한다. 액추에이터에 보안이 중요하듯 Admin서버에도 보안은 중요하다

게다가 만일 액추에이터 엔드포인트에서 인증을 요구한다면 Admin서버가 해당 엔트포인트에 접근하기 위해 인증정보를 알아야한다.

### 🌟 Admin 서버에 로그인 활성화하기

Admin서버는 기본적으로 보안이 되지않으므로 보안을 추가하는 것이 좋다. Admin서버는 스프링 부트 애플리케이션이므로

다른 스프링 부트 애플리케이션과 마찬가지로 스프링 시큐리티를 사용해 처리할 수 있다. 그러므로 아래와 같이 하면된다

```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

그리고 무작위로 생성되는 비밀번호를 Admin 서버의 로그에서 계속 찾을 필요 없으므로 간단한관리자의 이름과 비밀번호를 아래와같이 설정하자

```
spring:
  securitry:
    user:
      name: admin
      password: 53cr3t
```

### 🌟 액추에이터로 인증하기
Admin 서버의 클라이언트 애플리케이션은 자신을 직접 Admin 서버에 등록하거나 유레카를 통해 발견되게 함으로써

자신의 인증 정보를 Admin 서버에 제공할 수 있다. 만일 Admin서버의 클라이언트 애플리케이션이 직접 Admin 서버에

등록한다면 등록할 때 자신의 인증정보를 Admin서버에 전송할 수 있다. 이렇게하려면 몇가지 속성을 구성해야한다.

Admin서버가 애플리케이션의 액추에이터 엔드포인트에 접근하는데 사용할 수 있는 인증정보는 다음과 같이 각 클라이언트의 

application.yml에 `spring.boot.admin.client.instance.metadata.user.name`과 `spring.boot.admin.client.instance.metadata.user.password`속성을 지정한다


```
spring:
  boot:
    admin:
      client:
        url:http://localhost:8080
        instance:
          metadata:
            user.name: ${spring-security.user.name}
            user.password: ${spring.security.user.password}
```

이처럼 인증정보는 Admin서버에 자신을 등록하는 각 클라이언트 애플리케이션에 반드시 설정되어야한다.

그리고 지정된 값은 액추에이터 엔드포인트에 대한 HTTP 기본 인증 헤더에 필요한 인증정보와 반드시 일치해야한다.

# 17장 요약

### [1] 스프링 부트 Admin 서버는 하나 이상의 스프링 부트 애플리케이션으로부터 액추에이터 엔드포인트

### 를 소비하고 사용자 친화적인 웹 애플리케이션에서 데이터를 보여준다

### [2] 스프링부트 애플리케이션은 자신을 클라이언트로 Admin서버에 등록할 수 있다.

### 또는 Admin서버가 유레카를 통해 클라이언트 애플리케이션을 찾게 할 수 있다.

### [3] 애플리케이션 상태의 스냅샷을 캡쳐하는 액추에이터 엔드포인트와는 다르게, Admin

### 서버는 애플리케이션의 내부 작업에 관한 실시간 뷰를 보여줄 수 있다.

### [4] Admin서버는 액추에이터 엔드포인트의 결과를 쉽게 필터링해주며, 경우에따라서는 그래프로 데이터를 보여준다.

### [5] Admin서버는 스프링 부트 애플리케이션이므로 스프링 시큐리티를 통해 사용할 수 있는

### 어떤 방법으로도 보안을 처리할 수 있다.






