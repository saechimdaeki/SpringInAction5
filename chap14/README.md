# 14장 클라우드 구성 관리

### 🌟 이 장에서 배우는 내용
- 스프링 클라우드 구성서버 실행하기
- 구성 서버 클라이언트 생성하기
- 보안에 민감한 구성 정보 저장하기
- 구성을 자동으로 리프레쉬하기


우리는 이전 챕터5에서 구성속성을 설정하여 스프링 부트 애플리케이션을 구성할 수 있는 방법을 알아보았다.

구성 속성들은 각 애플리케이션에 맞게 구성될 수 있으며, 이때는 배포되는 애플리케이션 패키지에 있는 

application.properties나 application.yml 파일에 구성속성을 지정하면 된다. 그러나 마이크로 서비스 애플리케이션을

구축할 때는 여러 마이크로서비스에 걸쳐 동일한 구성 속성이 적용되므로 문제가 될 수있다. 이번 장은 스프링 클라우드의

구성 서버에 관해 알아보자. 구성서버는 애플리케이션의 모든 마이크로서비스에 대해 중앙 집중식의

구성을 제공한다. 따라서 구성서버를 사용하면 애플리케이션의 모든 구성을 한 곳에서 관리할수있다.


## 🍰 구성 공유하기

만일 구성 속성이 런타임 환경을 변경하거나 런타임 환경에 고유한 것이어야 한다면, 자바 시스템 속성이나 운영체제의

환경 변수를 구성 속성으로 사용하는 것이 좋다. 그러나 값이 변경될 가능성이 거의 없고 애플리케이션에 특정되는

속성의 경우는 애플리케이션 패키지에 포함되어 배포되는 application.yml이나 application.properties 파일에

구성속성을 지정하는 것이 좋은 선택이다.

이런 선택은 간단한 애플리케이션에는 문제가 없다. 그러나 자바 시스템 속성이나 운영체제의 환경 변수에 구성 속성을

설정하는 경우는 해당 속성의 변경으로 인해 애플리케이션이 다시 시작되어야 한다는 것을 감안해야한다.

그리고 배포되는 JAR나 WAR파일 내부에 구성 속성을 포함시키는 경우는 해당 속성을 변경하거나 원래 값으로

되돌릴 때 애플리케이션을 다시 빌드하여 배포해야한다. 또한, 데이터베이스 비밀번호와 같은 일부 속성들은 

보안에 민감한 값을 갖는다. 이러한 어떤 구성 속성들은 애플리케이션 개발자조차도 접근할 수 없도록 해야하므로 

이런 속성들은 운영체제의 환경변수에 설정하는 것은 바람직하지 않다.

이에 반해 `중앙집중식으로 구성을 관리`할 때는 어떻게 되는지 생각해보자

#### [1] 구성이 더이상 애플리케이션 코드에 패키징되어 배포되지 않는다. 따라서 애플리케이션을 다시 빌드하거나 배포하지

#### 않고 구성을 변경하거나 원래 값으로 환원할 수 있다. 또한, 애플리케이션을 다시 시작하지 않아도 실행중에 구성을변경할수있다.

#### [2] 공통적인 구성을 공유하는 마이크로서비스가 자신의 속성 설정으로 유지,관리하지 않고도 동일한 속성들을 공유할수있다.

#### 그리고 속성 변경이 필요하면 한곳에서 한번만 변경해도 모든 마이크로서비스에 적용할수있다.

#### [3] 보안에 민감한 구성 속성은 애플리케이션 코드와는 별도로 암호화하고 유지,관리할 수 있다. 그리고 복호화된

#### 속성 값을 언제든지 애플리케이션에서 사용할 수 있으므로 복호화를 하는 코드가 애플리케이션에 없어도된다.

스프링 클라우드 구성서버는 애플리케이션의 모든 마이크로서비스가 구성에 의존할 수 있는 서버를 사용해서

중앙 집중식 구성을 제공한다. 따라서 모든서비스에 공통된 구성은 물론이고, 특정서비스에 국한된 구성도 한곳에서 관리할수있다.

구성 서버를 사용하는 첫 번째 단계는 서버를 생성하고 실행하는 것이다.

## 🍰 구성 서버 실행하기

스프링 클라우드 구성 서버는 집중화된 구성 데이터 소스를 제공한다. 구성서버는 유레카처럼 더 큰 애플리케이션의

마이크로서비스로 생각할 수 있으며, 같은 애플리케이션에 있는 다른 서비스들의 구성 데이터를 제공하는 역할을 수행한다.

구성서버는 클라이언트가 되는 다른 서비스들이 구성 속성을 사용할 수 있도록 REST API를 제공한다.

### 스프링 클라우드 구성서버는 Git이나 Valut를 백엔드로 사용해 구성속성을 제공한다

![image](https://user-images.githubusercontent.com/40031858/109371415-31e88580-78e8-11eb-88fa-253fb22d46b2.png)

### 🌟 구성 서버 활성화하기

더 큰 애플리케이션 시스템 내부의 또 다른 마이크로서비스인 구성 서버는 별개의 애플리케이션으로 개발되어 배포된다.

따라서 새소룽ㄴ 구성서버 프로젝트를 생성 후 구성서버 스타터의존성을 추가한후에 구성서버를 활성화하자

애플리케이션이 시작되는 부트스트랩 클래스에 `@EnableConfigServer`애노테이션을 추가하자.

이름이 암시하듯 이 애노테이션은 애플리케이션이 실행될 때 구성서버를 활성화하여 자동-구성한다.

```
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication{
    public static void main(String[] args){
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
```
애플리케이션을 실행하고 구성 서버가 작동하는 것을 알아보기 전에 한가지 더할 것이있는데

구성 서버가 처리할 구성속성들이 있는곳을 알려주어야 한다. 여기서는 Git레포지토링니 github를 사용할것이다.

따라서 application.yml파일의 spring.cloud.config.server.git.uri 속성에 github구성 레포지토리의

URL을 설정해야한다.

```
spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/habuma/tacocloud-config
```
(책 저자의 깃허브레포지토리)

하지만 로컬에서 애플리케이션을 개발하는 경우 추가로 설정할 속성이 있다. 로컬에서 서비스를 테스트할ㄷ 떄는 다수의 서비스들이

실행되면서 localhost의 서로다른 포트를 낭비하게된다. 그러나 스프링 부트 웹 애플리케이션인 구성서버는 기본적으로 8080

포트를 리스닝한다. 따라서 server.port를 고유한 다른값으로 설정해야한다
```
server:
  port:8888
```
포트번호는 원하는 것으로 지정해도 되지만, 구성 서버의 클라이언트 서비스에서도 같은 번호를 사용해야한다

조금 전까지 설정한 두 개의 속성은 구성 서버 자체의 구성에 필요한 속성이며 구성 서버가 클라이언트에 제공하는 구성

속성은 Git이나 Valut에서 가져온다.

지금은 구성 서버의 클라이언트가 작성되지 안항ㅆ다. 그러나 다음과 같이 명령행에서 curl 명령을 사용해서 

구성 서버의 클라이언트인 것처럼 실행해 볼 ㅅ후 있다.

    curl localhost:8888/application/default

또는 모든 운영체제의 웹 브라우저에서 http://localhost:8888/application/default/master 에 접속하면 결과가 응답할 것이다.

여기서는 구성서버의 /application/default 경로에 대한 HTTP GET요청을 수행한다.

![image](https://user-images.githubusercontent.com/40031858/109372458-05833800-78ed-11eb-826f-489c139844ff.png)

경로의 첫 번째 부분인 'application'은 구성 서버에 요청하는 애플리케이션 이름이다.

요청 경로의 두 번째 부분은 요청하는 애플리케이션에 활성화된 스프링 프로파일의 이름이다. 

요청 경로의 세 번째 부분은 생략가능하며, 구성속성을 가져올 Git레포지토리의 라벨이나 분기를 지정한다.

만일 지정하지 않으면 'main'분기가 기본값이 된다 (2021년기준)

요청 응답에는 구성 서버가 제공하는 것에 관한 몇 가지 기본정보가 포함된다. 예를 들어, 구성 서버가 구성서버를 가져오는 

Git 커밋의 버전과 라벨등이다. 그리고 구성 속성들은 propertySources 속성에 포함된다. 여기서는 github.com/habuma/tacocloud-config

에 필요한 미리 추가했던 'name'과 'source'등의 몇가지 구성 속성이 포함되어 있다. 그러나 다른 Git

레포지토를 사용할 떄는 각자 추가해야한다.

### 🌟 Git레포지토리에 구성 속성 지정하기

구성 서버가 가져올 속성을 준비하는 방법은 여러가지가 있다. 가장 기본적이고 쉬운 방법은 Git레포지토리의 루트경로로

`application.properties`나`application.yml`파일을 커밋하는 것이다. localhost의 gogs를 Git레포지토리로 사용한다고 가정하자

gogs의 포트번호는 10080이며 구성 속성은 localhost:10080/tacocloud/tacocloud-confg에 저장한다고하자

gogs에 저장할 구성속성을 아래와 같이 application.yml에 설정했다고 가정하자.
```
server:
  port: 0

eureka:
  client:
    service-url:
      defaultZone: http://eureka1:8761/eureka/
```

여기서 설정된 구성 속성은 몇개 없지만 중요하다. 우선, 포트를 0으로 설정하였고 따라서 이 구성속성을 사용하는 애플리케이션의 모든 서비스는

무작위로 선택된 포트를 사용하며, 유레카의 클라이언트로 등록할 수 있다.

### 🌟 Git 하위 경로로 구성 속성 저장하기

필요하다면 Git 레포지토리의 루트 경로 대신 하위 경로에 구성 속성을 저장할 수도 있다. 예를 들어, 앞의 Git레포지토리의

'config'라는 서브 디렉토리에 구성 속성을 저장한다고 가정해보자 이때는 구성서버 자체의 속성으로 

`spring.config.server.git.search-paths`를 추가하면 된다.
```
spring:
  cloud:
    config:
      server:
        git:
          url: http//localhost:10080/tacocloud/tacocloud-config
          search-paths: config
```

`spring.config.server.git.search-paths`은 복수형이므로 ,를 사용해 각 경로또한 구분할 수 있다.

### 🌟 Git레포지토리의 분기나 라벨에 구성 속성 저장하고 제공하기

기본적으로 구성 서버는 Git레포지토리의 main분기에서 구성 속성을 가져온다. 그리고 클라이언트에서는 구성 서버에 대한

요청 경로의 세 번째 부분에 Git 레포지토리의 분기나 라벨을 지정할 수 있다. 이때 main 분기 대신 특정 라벨이나

분기를 구성 서버 자체의 속성으로 지정하면 유용하다. 이때 `spring.cloud.config.server.git.default-label`

속성을 지정하면 기본 라벨이나 분기가 변경된다. 예를들어, 'sidework'라는 이름의 분기에 저장된 구성 속성을

구성 서버가 가져올때는 다음과 같이 지정한다
```
spring:
  cloud:
    config:
      server:
        git:
          uri: http://localhost:10080/tacocloud/tacocloud-config
          default-label: sidework
```
이 경우 구성 서버 클라이언트에서 특정 분기나 라벨을 지정하지 않고 구성 서버에 요청하면 'sidework'분기의 구성 속성을 구성서버가 가져온다.

### 🌟 Git 백엔드를 사용한 인증

구성 서버가 읽는 백엔드 Git 레포지토리는 사용자 이름과 비밀번호로 인증될 수 있다. 이때는 구성 서버 자체의 

속성으로 Git 레포지토리의 사용자 이름과 비밀번호를 설ㅊ정해야한다.

Git레포지토리의 사용자 이름은 `spring.cloud.config.server.git.username` 속성으로 설정하며,

비밀번호는 `spring.cloud.config.server.git.password`속성으로 설정한다

```
spring:
  cloud:
    config:
      server:
        git:
          uri: http://localhost:10080.tacocloud/tacocloud-config
          username: kjs
          password: 1234
```

## 👟 공유되는 구성 데이터 사용하기

중앙집중식 구성서버를 제공하는것에 추가하여, 스프링 클라우드 구성 서버는 클라이언트 라이브러리도 제공한다. 

스프링부트 애플리케이션의 빌드에 포함하면 애플리케이션이 구성서버의 클라이언트가 될 수 있다.

```
<dependency>
    <groupId><org.springframework.cloud/groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
```

이처럼 의존성을 추가한 후 애플리케이션을 실행하면 자동-구성이 실행되어 구성 서버로부터 속성들을 가져오는 

속성 소스를 등록한다. 기본적으로 자동-구성은 구성 서버가 localhost의 8888포트에서 실행중인것으로 간주한다.

그러나 spring.cloud.config.uri 속성을 설정하면 구성서버의 위치를 알려줄 수 있다.
```
spring:
  cloud:
    config:
      uri: http://config.tacocloud.com:8888
```

이 속성은 구성 서버의 클라이언트가 되는 애플리케이션자체에 설정되어야 한다.

그러나 중앙 집중식 구성 서버가 있을떄는 대부분의 모든 구성이 이서버에서 제공된다. 따라서 각 마이크로서비스가 자신의

구성을 가질 필요가 없으며, 구성 서버의 위치를 지정하는 `spring.cloud.config.uri`와 구성서버에 애플리케이션을 알려주는

`spring.application.name` 속성만 각 마이크로서비스에 설정하면 된다.

애플리케이션이 시작되면 구성 서버 클라이언트가 제공하는 속성 소스가 구성 서버에 속성 값을 요청한 후 받으면 애플리케이션의

환경에서 이 속성들을 사용할 수 있다. 게다가 이 속성들은 효과적으로 캐싱되므로 구성서버의 실행이 중단되더라도 사용가능.

## 👟 애플리케이션이나 프로파일에 특정된 속성 제공하기
구성 서버 클라이언트가 시작될 때는 애플리케이션의 이름과 활성 프로파일 모두를 포함하는 요청 경로를

사용해서 구성 서버에 속성을 요청한다. 그리고 구성서버가 구성 속성을 제공할 때는 클라이언트가 요청한

값을 고려해서 애플리케이션과 프로파일에 특정된 구성속성을 클라이언트에 반환한다.

### 🌟 애플리케이션에 특정된 속성 제공하기
한 애플리케이션의 모든 마이크로 서비스들이 공통 구성 속성을 공유할 수 있다는 것이 구성 서버 사용의 장점 중하나다.

그렇지만 하나의 마이크로 서비스에만 공유하면서 모든 마이크로서비스가 공유할 필요 없는 속성들이 있을때가 있다.

공유하는 구성과 더불어 구성서버는 특정 애플리케이션을 대상으로 하는 구성 속성을 관리할 수 있다.

이 경우 해당 애플리케이션의 spring.application.name 속성 값과 동일하게 구성파일의 이름을 지정하는 것이

좋은 방법이다. 13장에서 spring.application.name 속성을 사용해 유레카에 등록하는 마이크로서비스의 이름을 지정하였다.

구성 서버에서 구성 클라이언트를 식별할 때도 같은 속성이 사용된다. 구성서버가 해당 애플리케이션에 특정된

구성 데이터를 제공할 수 있게 하기 위함이다. 예를들어, 4개의 마이크로서비스로 분할했던 타코 클라우드 애플리케이션에서는

이 서비스 이름을 각 서비스 애플리케이션의 spring.application.name속성에 지정할 수 있다.

그 다음에 구성서버의 Git백엔드에 ingredient-service.yml, order-service.yml, taco-service.yml, user-service.yml이름의 YAML구성파일을 생성하면된다.

애플리케이션 이름과 상관없이 모든 애플리케이션은 application.yml파일의 구성 속성을 받는다. 그러나 각 서비스 

애플리케이션의 spring.application.name속성이 구성서버에 요청할때 전송된다. 그리고 이 속성 값과 일치하는 이름의 구성

파일이 있으면 이 파일에 저장된 속성들이 반환된다. 만일 application.yml의 공통속성과 애플리케이션에 특정한

구성 파일의 속성이 중복될 떄는 애플리케이션에 특정된 속성들이 우선한다.

### 🌟 프로파일로부터 속성 제공하기

스프링 클라우드 구성 서버는 각 스프링 부트 애플리케이션에 사용했던 것과 똑같은 방법으로 프로파일에 특정된 속성들을 지원한다.

```
- 프로파일에 특정된 .properties 파일이나 YAML파일들을 제공한다. 예를들면, 
application-production.yml이라는 이름의 구성파일이 해당된다
- 하나의 YAML파일 내부에 여러개의 프로파일 구성그룹을 포함한다. 이 경우 3개의
하이폰(---)을 추가하고 그아음에 해당 프로파일의 이름을 나타내는 spring.profiles속성을 지정한다

```

예를들어, 구성 서버를 통해서 애플리케이션의 모든 마이크로서비스들이 공유하는 유레카 구성을 생각해보면

이 경우 개발환경에서는 하나의 유레카 개발 인스턴스만 참조해도 충분하다. 그러나 마이크로서비스들이 프로덕션에서 실행된다면

다수의 유레카 노드를 참조하도록 구성해야할것이다. 또한 개발 환경의 구성에서는 server.port 속성을 0으로 설정했지만,

서비스들이 프로덕션으로 이양되면 8080포트를 외부 포트로 전환하여 연결하는 별개의 컨테이너에서 각 서비스를 실행해야한다.

모든 애플리케이션이 8080포트를 리스닝하기 때문이다. 이때 프로파일을 사용하면 이런 구성을 선언할 수 있다.

즉 구성서버의 Git 백엔뜨에 저장했던 기본 application.yml 파일에 추가하여 application-production.yml이라는 이름의 또

다른 YAML파일을 저장하면된다.

```
server:
  port:8080
eureka:
  client:
    service-url:
      defaultZone: http://eureka1:8761/eureka/, http://eureka2:8761/eureka/
```

## 🍰 구성 속성들의 보안 유지하기 

구성서버에서 민감한 정보를 포함하는 속성들을 제공해야할 경우가 있다. 보안구성속성을 사용할 때 구성서버는 다음 두가지옵션을 제공한다

- Git 백엔드 레포지토리에 저장된 구성파일에 암호화된 값쓰기
- Git 백엔드 레포지토리에 추가하여 구성서버의 백엔드 저장소로 해시코프의 Vault사용하기

### 🌟 Git백엔드의 속성들 암호화하기
암호화되지 않은 값들을 제공하는 것에 추가하여 구성 서버는 Git레포지토리에 저장된 구성 파일에 쓰는 암호화된 

값들도 제공할 수 있다. Git레포지토리에 저장되는 암호화된 데이터를 사용하는 핵심은 `암호화 키`다.

암호화된 속성을 사용하려면 암호화 키를 사용해서 구성서버를 구성해야 하며, 암호화 키는 속성 값을 클라이언트 애플리케이션에

제공하기 전에 복호화하는데 사용된다. 구성서버는 대칭 키와 비대칭키를 모두 지원한다. 우선 대칭키를 설정하려면 구성서버

자체 구성의 encrypt.key 속성에 암호화 키와 복호화 키와 같이 사용할 값을 설정하면된다.
```
encrypt:
  key: s3cr3t
```

이 속성은 부트스트랩 구성에 설정되어야 한다. 그래야만 자동-구성이 구성서버를 활성화 시키기 전에 로드되어 사용할수있기때문.

더 강력한 보안을 위해서는 구성서버가 한 쌍의 비대칭 RSA키나 키스토어의 참조를 사용하도록 구성할 수 있다. 이때는 

다음과 같이 keytool 명령행 도구를 사용하여 키를 생성할 수 있다.
```
keytool -genkeypair -alias tacokey -keyalg RSA \
-dname "CN=Web Server, OU=Unit, O=Organization,L=City,S=State,C=US" \
-keypass s3cr3t -keystore keystore.jks -storepass l3tm3in
```

결과로 생성되는 키스토어는 keystore.jks라는 이름의 파일로 저장되며, 파일시스템의 키스토어 파일로 유지하거나

애플리케이션 자체에 둘 수 잇따. 그리고 둘 중 어떤 경우든 해딩 키스토어의 위치와 인증 정보를 구성 서버의

bootstrap.yml 파일에 구성해야한다.
```
encrypt:
  key-store:
    alias: tacokey
    location: classpath:/keystore.jks
    password: l3tm31n
    secret: s3cr3t
```

이처럼 키나 키스토어가 준비된 후에는 데이터를 암호화해야한다. 구성 서버는 /encrypt 엔드포인트를 제공한다.

따라서 암호화될 데이터를 갖는 POST 요청을 /encrypt 엔드포인트에 하면된다. 예를들어, 몽고 DB 데이터베이스의

비밀번호를 암호화하고 싶다고하자. curl을 사용할때는 다음과 같이 해당 비밀번호를 암호화할수있다.
```
$ curl localhost:8888/encrypt -d "s3cr3tP455w0rd"
93912a660a7f3c04e811b5df9a3cf6e1f63850cdcd4aa092cf5a3f7e1662fab7
```

Post 요청이 제출된후 암호화된 값을 응답으로 받는다. 그다음 이값을 복사하여 Git레포지토리에 저장된 구성파일에 붙여넣으면된다.

몽고 DB의 비밀번호를 설정할 때는 Git레포지토리에 저장된 application.yml파일에 spring.data.mongodb.password 속성을 추가한다

```
spring:
  data:
    mongodb:
      password: '{ciper}93912a660a7f3c04e811b5df...'
```
지정된 값이 작은 따음표로 둘러싸여 있고 맨앞에 `{cipher}`가 붙어 있다는것에 유의하자. 이것은 해당 값이 암호화된

값이라는 것을 구성서버에 알려주는것이다. 이렇게 변경된 application.yml파일을 Git레포지토리에 커밋하고 푸쉬하면

암호화된 속성들을 구성서버가 제공할 준비가 된것이다. 따라서 해당 구성을 사용하는 클라이언트 애플리케이션은 Git

레포지토리의 암호화된 속성들을 받기 위해 어떤 특별한 코드나 구성도 가질 필요는없다.

만일 구성서버가 암호화된 속성의 값을 복호화하지않고 제공하기 원한다면 spring.cloud.config.server.encrypt.enable속성을 false로하면된다.
```
spring:
  cloud:
    config:
      server:
        git:
          uri: http://localhost:10080/tacocloud/tacocloud-config
        encrypt:
          enabled: false
```

이렇게 지금까지 보았듯 암호화된 속성 값을 구성서버가 제공하도록 Git레포지토리에 저장할 수 있다.

그러나 암호화는 Git레포지토리의 본래 기능이 아니므로 데이터를 암호화해서 저장하려면 별도의 노력이 필요하다.

게다가 보안이 필요한 속성은 누가 요청하든 구성서버 API를 통해서 복호화되어 제공된다.

### 🌟 Vault에 보안 속성 저장하기

해시코프의 Vault는 보안 관리 도구다. 이것은 Git서버와 다르게 Vault가 보안정보를 자체적으로 처리한다는 의미다.

따라서 보안에 민감한 구성 데이터의 경우 구성서버의 백엔드로 Vault가 훨씬 더 매력적인 선택이 된다.

Vault를 시작하기전에 https://learn.hashicorp.com/tutorials/vault/getting-started-install 의 지침대로 명령행도구를 다운로드하자.

### 🌟 Vault서버 시작시키기

구성서버로 보안 속성을 저장하고 제공하기에 앞서 Vault서버를 시작시켜야한다. 여기서는 다음과 같이 개발모드로 시작시킨다

```
$ vault server -dev -dev-root-token-id=roottoken
$ export VAULT_ADDR='http://127.0.0.1:8200'
$ vault status
```

첫 번째 명령은 Vault 서버를 개발모드로 시작시키며, 이것의 루트토큰ID는 roottoken이다.

이름이 암시하듯, 개발 모드는 간단하지만 아직 보안이 되지않는 Vault런타임이다. 따라서 프로덕션 설정에서 사용하면 안된다.

Vault서버를 사용하려면 토큰을 제공해야한다. 특히 루트 토큰은 관리용 토큰이며, 더 많은 토큰을 생성할 수 있게 한다.

또한, 루트 토큰은 보안 정보를 읽거나 쓰는데도 사용할 수 있따. 만일 개발 모드로 Vault서버를 시작시킬때

루트 토큰을 지정하지 않으면 Vault가 자동으로 하나를 생성하고 로그에 기록한다. 일단 개발 모드 서버가 시작되면

앞에서 지정한 대로 로컬 컴퓨터의 8200 포트를 리스닝한다. 중요한것이 있는데 vault명령에서 Vault서버의 위치를 알 수 있도록

VAULT_ADDR 환경변수를 설정해야한다.

마지막으로 vault status 명령에서는 이전의 두 명령이 제대루 수행되어 Vault서버가 실행중인지 검사한다.

이 명령이 실행되면 Vault 서버의 구성을 나타내는 속성들의 내역을 받을것이다. 여기에는 sealed 상태인지

나타내는 속성이 포함된다. Vault 0.10.0이상 버전에는 구성 서버와 연계되도록 Vault를 사전 준비시키기 위해 수행해야 할 명령들이있다.

다음의 두 명령은 구성서버와 호환되는 secret이라는 이름의 Vault백엔드를 다시 생성한다
```
$ vault secrets disable secret
$ vault secrets enable -path=secret kv
```

### 🌟 보안 데이터를 VAULT에 쓰기
vault 명령은 보안 데이터를 Vault 서버에 쓰기 쉽게 해준다. 예를들어 spring.data.mongodb.password 속성으로 

몽고 DB의 비밀번호를 Vault 서버에 저장하고 싶다고 하자. 이때는 다음과같이 명령을 사용하면 된다

  $ vault write secret/application spring.data.mongodb.password=s3cr3t

![image](https://user-images.githubusercontent.com/40031858/109385224-a0076980-7935-11eb-9b0c-ca4f79e9c656.png)

여기서 가장 중요한 부분은 `보안데이터 경로`, `보안키`, `보안처리될 값`이다. 파일시스템 경로와

매우 흡사한 보안 데이터 경로는 연관된 보안 데이터를 지정된 경로에 모아둘 수 있게 한다. 보안 데이터

경로 앞의 secret/는 Vault 백엔드 서버를 나타내며 여기서는 이름이 'secret'이다.

보안키와 보안 처리될 값은 Vault서버에 쓰려는 실제 보안 데이터다 이처럼 구성 서버가 제공하는 보안데이터를

Vault서버에 쓸 때 구성 속성과 동일한 보안키를 사용하는것이 중요하다. 저장된 구성 데이터는 vault read명령을 통해 확인할 수있다

  $ vault read secret/application

지정된 경로에 보안 데이터를 쓸 때는 이전에 해당 경로에 썻던 데이터를 덮어쓰기한다는것을 알아두자

예를 들어 앞의 예와 같은경로로 몽고DB의 사용자 이름을 Vault서버에 쓴다고하면 이경우 spring.data.mongodb.username

보안속성만 쓰면 안된다. 왜냐하면 이미 썻던 spring.data.mongodb.password 보안속성이 없어지기 때문이다.

따라서 이때는 다음과 같이 두 속성 모두를 같이써야한다.

```
$ vault write secret/application \
              spring.data.mongodb.password=s3cr3t \
              spring.data.mongodb.username=tacocloud
```

### 🌟 구성 서버에서 Vault 백엔드 활성화하기

구성서버의 백엔드로 Vault서버를 추가할때 최소한으로 해야할것이 바로 활성화 ㅍ ㅡ로파일로 vault를 추가하는것이다. 

다음과 같이 구성서버의 application.yml파일에 추가하면된다
```
spring:
  profiles:
    active:
      - vault
      - git
```
여기서는 vault와 git 프로파일 모두 활성화되었다. 따라서 구성 서버가 Vault와 Git모두의 구성을 제공할 수 있다.

이 경우 보안에 민감한 구성 속성은 Vault에만 쓰고 그렇지 않은 구성 속성은 Git백엔드를 쓰면된다.

만일 모든 구성 속성을 Vault에 쓰고 Git백엔드는 사용하고 싶지않다면, spring.profiles.active에 vault만 설정하면된다.

### 🌟 구성 서버 클라이언트에 Vault 토큰 설정하기

당연하지만 구성 서버로부터 속성을 가져올 때 각 마이크로서비스에서 curl을 사용해 토큰을 지정할수는 없을것이다.

대신에 각 서비스 애플리케이션의 로컬구성에 다음과 같이 추가하면 된다
```
spring:
  cloud:
    config:
      token: roottoken
```
spring.cloud.config.token 속성은 지정된 토큰 값을 구성 서버에 대해 모든요청을 포함하라고 구성 서버 클라이언트에 알려준다.

이 속성은 구성 서버의 Git이나 Vault 백엔드에 저장되지 않고 애플리케이션의 로컬구성에 설정되어야한다. 

그래야만 구성 서버가 Vault에 전달하고 구성속성을 제공할 수 있기 떄문이다.

### 🌟 애플리케이션과 프로파일에 특정된 보안 속성 쓰기

application 경로에 저장되는 보안 속성은 구성 서버가 이름과 상관없이 모든애플리케이션에 제공한다.

그러나 만일 지정된 애플리케이션에 특정된 보안 속성을 저장해야 한다면, 요청 경로의 application 부분을 해당 애플리케이션

이름으로 교체하면된다. 예를들어 다음의 vault write명령어에서는 이름이 ingredieng-service인 애플리케이션에 특정된 보안속성을쓴다
```
$ vault write secret/ingredient-service \
              spring.data.mongodb.password=s3cr3t
```

이와 유사하게, 프로파일을 지정하지 않으면 Vault에 저장된 보안 속성은 기본 프로파일의 일부로 제공된다. 

즉 클라이언트는 자신의 활성화 프로파일이 무엇이건 관계없이 해당 속성을 받는다. 그러나 다음과 같이 프로파일에 관련된 보안속성을 쓸수있다.

```
$ vault write secret/application.production \
              spring.data.mongodb.password=s3cr3t \
              spring.data.mongodb.username=tacocloud
```
이 경우 활성 프로파일이 production인 애플리케이션에만 제공되는 보안속성을쓴다.

## 🍰 실시간으로 구성 속성 리프레시하기

일반적으로 구성 변경을 포함해서 애플리케이션을 유지보수할때는 애플리케이션을 다시 배포하거나 최소한 다시 시작해야한다.

그러나 이것은 클라우드 기반의 애플리케이션에서는 용납할 수 없으므로 애플리케이션을 중단시키지 않고 실시간으로 구성속성을

변경할수 있어야한다. 다행히도 스프링 클라우드 구성서버는 실행 중인 애플리케이션을 중단시키지 않고 구성속성을 리프레시 하는 기능을 제공한다.

백엔드 Git레포지토리나 Vault 보안 서버에 변경 데이터가 푸쉬되면 애플리케이션의 각 마이크로서비스는 새로운

구성으로 즉시 리프레시되며 다음 중 한가지 방법을 사용한다

`수동식`: 구성 서버 클라이언트는 /actuator/refresh의 특별한 액추에이터 엔드포인트를 활성화한다.

그리고 각 서비스에서 이 엔드포인트로 HTTP POST요청을 하면 구성 클라이언트가 가장 최근의 구성을 백엔드로부터 가져온다

`자동식`: 레포지토리의 커밋 후크가 모든 서비스의 리프레쉬를 촉발할 수 있다. 이때는 구성 서버와 이것의 클라이언트

간의 통신을 위해 스프링 클라우드 버스라는 스프링 클라우드 프로젝트가 개입한다.

---

각 방법은 장점과 단점이 있다. 수동식 리프레쉬는 서비스가 리프레쉬되는 구성으로 업데이트 시점을 더 정확하게 제어할 수 있다.

그러나 마이크로서비스의 인스턴스에 대해 개별적인 HTTP 요청이 수행되어야한다. 반면에 자동식 리프레쉬는 

애플리케이션의 모든 마이크로서비스에 대해 즉시로 변경 구성을 적용한다. 그러나 이것은 구성 레포지토리에

커밋을 할 때 수행되므로 프로젝트에 따라서는 큰 부담이 될 수 있다.

### 🌟 구성 속성을 수동으로 리프레쉬하기

이후에 배울 스프링 부트의 기본 요소 중 하나인 스프링 부트 액추에이터를 알아보자. 이것은 런타임 파악 및 로깅 수준과

같은 런타임 상태의 일부 제한적인 제어를 가능학게 한다. 그러나 지금은 스프링 클라우드 구성서버 클라이언트로 구성된

애플리케이션에서만 사용할 수 있는 액추에이터의 특정기능을 알아보자.

구성서버의 클라이언트로 애플리케이션을 활성화하면, 구성 속성들을 리프레시하기 위해 자동-구성이 액추에이터 엔드포인트를 구성한다.
```
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

이제 실행중인 구성 클라이언트 애플리케이션에 액추에이터가 활성화 되므로 /actuator/refresh에 대한 

HTTP POST요청을 제출하여 언제든 우리가 원할 때 백엔드 레포지토리로부터 구성 속성을 리프레쉬할수있다.

`@ConfigurationProperties` 애노테이션이 지정된 GreetingProps라는 이름의 클래스가 있다고 하자

```
@ConfigurationProperties(prefix="greeting")
@Component
public class GreetingProps{
  private String message;

  public String getMessage(){
    return message;
  }

  public void setMessage(String message){
    this.message=message;
  }
}
```

여기에 추가하여 GreetingProps가 주입되고 GET요처을 처리할 때 message속성의 값을 반환하는 다음의 컨트롤러도있다.

```
@RestController
public class GreetingController{
  private final GreetingProps props;

  public GreetingController(GreetingProps props){
    this.props=props;
  }

  @GetMapping("/hello")
  public String message(){
    return props.getMessage();
  }
}
```

이 두 클래스를 갖는 애플리케이션 이름을 hello-world라고하자. 그리고 Git레포지토리에는 다음 속성들이 정의된 application.yml이있다고하자.

```
greeting:
  message: Hello World!
```

이제 curl을 사용해서 /hello에 대한 HTTP GET요청을 하면 'Hello World!'라는 응답이 출력된다

```
$ curl localhost:8080/hello
Hello World!
```

그 다음 hello-world 애플리케이션이나 구성 서버를 다시 시작시키지 않고 구성 서버 Git레포지토리의 application.yml

파일에 있는 greeting.message 속성을 다음과 같이 변경한 후 Git 레포지토리에 푸쉬한다고하자

```
greeting:
  message: Hiya folks!
```
Git 레포지토리의 greeting.message 속성 값이 변경되었는데도 바로 전과 같이 hello-world 애플리케이션에

Get요청을 다시하면 여전히 동일한 'Hello World!'응답을 받을것이다. hello-world애플리케이션의 구성

서버에서는 Git레포지토리의 greeting.message 속성이 변경된 것을 모르기 때문이다. 그러나 다음과 같이 hello-world 애플리케이션

서버의 액추에이터 리프레쉬 엔드포인트로 POST 요청을 하면 greeting.message 속성이 리프레쉬된다


    $ curl localhost:8080/actuator/refresh -X POST

그리고 다음의 응답을 반환한다

    ["config.client.version", "greeting.message"]

이 응답을 보면 변경된 속성 이름을 저장한 JSON 배열이 포함되어 있고 이 배열에는 greeting.message속성이

포함된 것을 알 수 있따. 그리고 또한 config.client.version속성도 포함되어있다. 이속성은 현재의 구성이 생성된

Git 커밋의 해시값을 갖는다 이는 해당구성이 새로운 Git커밋을 기반으로 하므로 이속성은 백엔드 구성 레포지토리에 변경이 생길때마다 변경된다.

`/actuator/refresh`엔드포인트는 구성 속성의 변경이 생기는 시점을 완전하게 제어하기 원할 때 아주 좋다.

그러나 만일 우리의 애플리케이션이 다수의 마이크로서비스로 구성된다면 그것들 모두의 구성을 리프레쉬하는것은 매우번거롭다..

### 🌟 구성속성을 자동으로 리프레쉬하기.
한 애플리케이션의 모든 구성 서버 클라이언트들의 속성을 수동으로 리프레쉬하는 방법의 대안으로 구성서버는 모든

클라이언트에게 자동으로 구성 변경을 알려줄 수 있다. 이떄 또다른 스프링 클라우드 프로젝트인 스프링 클라우드 버스를 사용.

![image](https://user-images.githubusercontent.com/40031858/109391866-96452c80-795c-11eb-85fb-16026c61acd3.png)

위의 그림의 속성 리프레시 절차는 다음과 같이 요약할 수 있다.

#### [1] 웹훅이 Git 레포지토리에 생성되어 Git레포지토리에 대한 변경이 생겼음을 구성 서버에 알려준다. 웹훅은 

#### GitGub,GitLab, Bitbucket, Gogs를 비롯한 많은 레포지토리에서 지원된다

#### [2] 구성서버는 RabbitMQ나 카프카와 같은 메시지 브로커를 통하여 변경 관련 메시지를 전파함으로써 웹훅의 POST요청에 반응한다

#### [3] 알림을구독하는 구성서버 클라이언트 애플리케이션은 구성 서버로부터 받은 새로운 속성 값으로

#### 자신의 속성을 리프레쉬하여 알림 메시지에 반응한다

따라서 모든 구성 서버 클라이언트 애플리케이션은 변경 속성이 백엔드 Git 레포지토리에 푸쉬되는 즉시

구성 서버로부터 받은 최신의 구성 속성 값을 갖는다. 구성 서버를 통해 자동리프레쉬를 사용할때 몇가지를 고려해야한다.

#### [1] 구성 서버에 이것의 클라이언트 간의 메시지 처리에 사용할 수 있는 메시지 브로커가 있어야하며

#### RabbitMQ나 카프카 중 하나를 선택할 수 있다.

#### [2] 구성 서버에 변경을 알려주기 위해 웹훅이 백엔드 Git레포지토리에 생성되어야 한다

#### [3] 구성 서버는 구성서버 모니터 의존성 및 RabbitMQ나 카프카 스프링 클라우드 스트림 의존성과 함께 활성화되어야한다

#### [4] 메시지 브로커가 기본 설정으로 로컬에서 실행되는 것이 아니라면, 브로커에 연결하기 위해 세부 정보를

#### 구성 서버와 이것의 모든 클라이언트에 구성해야한다

#### [5] 각 구성 서버 클라이언트 애플리케이션에 스프링 클라우드 버스 의존성이 추가되어야한다

### 🌟 웹훅 생성하기

많은 종류의 Git서버들이 Git레포지토리의 푸쉬를 비롯한 변경을 애플리케이션에 알리기 위해 웹훅의 생성을 지원한다.

웹훅의 설정 명세는 Git서버마다 다르므로 모두설명하기는 어려우며 Gogs 레포지토리의 웹훅을 설정하는 방법을 보자

Gogs는 로컬에서 실행하면서 로컬로 실행하는 애플리케이션에 대해 POST요청을 쉽게하는 웹훅을 가지므로 선택한것이다.

또한 Gogs로 웹훅을 설정하는 절차는 Github와 거의 동일하다.

우선 웹 브라우저에서 구성 레포지토리에 접속하고 setting을 클릭하면 레포지토리의 설정 페이지가 나타나며, 왼쪽에는

설정 관련 메뉴가 보이는데 여기서 Webhooks를 클릭후 Add Webhook을누르면된다. 이 폼에는 여러 필드가 있지만,

가장 중요한 것이 페이로드 URL과 컨텐트 타입이다. 구성서버는 /monitor 경로에 대한 웹훅의 POST 요청을 처리할 수

있어야 한다. 따라서 페이로드 URL필드에는 구성 서버의 /monitor엔드포인트를 참조하는 URL을 입력해야한다. 

이후 콘텐트 타입필드 등을 지정한 후 레포지토리에 대한 푸시 요청에서만 웹훅이 작동하도록 Just the Push Event라디오

버튼을 선택하고 Active 체크박스도 선택한다. 그리고 폼의 끝에 있는 Add Webhook 버튼을 클릭하면

웹훅이 생성되며, 이후로 레포지토리에 푸쉬가 발생할때마다 구성 서버에 POST요청을 전송한다


### 🌟 구성서버에서 웹훅 처리하기

구성서버의 /monitor 엔드포인트를 활성화하는 것은 간단하다. 스프링 클라우드 구성모니터 의존성을 추가하면된다

```
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-config-monitor</artifactId>
</dependency>
```

이처럼 의존성이 추가되면 자동-구성이 /monitor 엔드포인트를 활성화한다. 그러나 구성 서버가 변경 알림을

전파하는 수단도 가져야하므로 스프링 클라우드 스트림 의존성도 추가해야한다. 스프링 클라우드 스트림은

또다른 스프링 클라우드 프로젝트 중 하나며 RabbitMQ나 카프카를 통해 통신하는 서비스들을 생성할 수 있다.

이 서비스들은 스트림으로부터 처리할 데이터를 받으며, 하위 스트림 서비스가 처리하도록 스트림으로 데이터를 반환한다.

/monitor 엔드포인트는 스프링 클라우드 스트림을 사용해서 구성 서버 클라이언트에 알림 메시지를 전송한다.

```
//RabbitMQ 사용 시
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-stream-rabbit</artifactId>
<dependecny>

//카프카 사용 시
<dependency>
  <groupId>org.springframework.org</groupId>
  <artifactId>spring-cloud-starter-stream-kafka</artifactId>
<dependecny>
```

이처럼 필요한 의존성이 추가되면 구성 서버가 속성의 자동 리프레쉬에 참여할 준비가 된 것이다.

실제로 RabbitMQ나 카프카 메시지 브로커가 기본설정으로 로컬에서 실행되면 구성서버가 실행되는데 아무 문제없다.

그러나 메시지 브로커가 localhost가 아닌 다른곳에서 기본포트가 아닌 다른포트로 실행중이거나 해당브로커에

접근하기 위한 인증정보를 변경했다면 속성을 설정해야한다.

#### RabbitMQ의경우
```
spring:
  rabbitmq:
    host: rabbit.tacocloud.com
    port: 5672
    username: tacocloud
    password: s3cr3t
```

#### 카프카의 경우
```
spring:
  kafka:
    bootstrap-servers:
    - kafka.tacocloud.com:9092
    - kafka.tacocloud.com:9093
    - kafka.tacocloud.com:9094
```

### 🌟 Gogs알림 추출기 생성하기

서로 다른 종류의 Git서버마다 웹훅의 POST요청을 처리하는 방법이다르다. 따라서 웹훅의 POST요청을 처리할 때 

서로 다른 데이터 형식을 /monitor 엔드포인트가 알수 있어야 한다. 내부적으로 /monitor 엔드포인트는

일련의 컴포넌트들로 구성되어 있다. 그리고 이 컴포넌트들은 POST 요청을 조사하고, 어떤 종류의 Git서버로부터 온 

요청인지 판단한 후 각 클라이언트에 전송될 알림 타입으로 요청 데이터를 변환한다.

기본적으로 구성서버에는 Github, GitLab, Bitbucket등의 Git서버 지원 기능이 포함되어있다.

따라서 이것들 중 하나를 사용한다면 특별히 필요한게 없다. 하지만 Gogs는 공식적으로 지원되지 않아서 다음과 같은

알림 추출기 클래스를 프로젝트 빌드에 포함시켜야한다.

```
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 300)
public class GogsPropertyPathNotificationExtractor implements PropertyPathNotificationExtractor{

  @Override
  public PropertyPathNotification extract(
    MultiValueMap<String,String> headers,Map<String, Object> request){
      if("push".equals(headers.getFirst("X-Gogs-Event"))){
        if(request.get("commits") instanceof Collection){
          Set<String> paths=new HashSet<>();
          @SuppressWarning("unchecked")
          Collection<Map<String, Object>> commits=
            (Collection<Map<String, Object>>) request.get("commits");
          for(Map<String, Object> commit:commits){
            addAllPaths(paths,commit,"added");
            addAllPaths(paths,commit, "removed");
            addAllPaths(paths, commit, "modified");
          }
          if(!paths.isEmpty()){
            return new PropertyPathNotification(
              paths.toArray(new String[0]);
            )
          }
        }
        return null;
      }
    }
  private void addAllPaths(Set<String> paths, Map<String,Object> commit, String name){
    @SuppressWarning("unchecked")
    Collection<String> files= (Collection<String>) commit.get(name);
    if(files!=null){
      paths.addAll(files);
    }
  }
}
```

### 🌟 구성 서버클라이언트에 속성의 자동 리프레쉬 활성화하기

구성서버 클라이언트에 자동리프레쉬를 추가하는것또한 쉽다. 의존성을 추가하면된다.

```
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-bus-acmp</artifactId>
</dependency>
```
이것은 AMQP 스프링 클라우드 버스 스타터를 빌드에 추가한다 만약 카프카를 사용하면 다음의 의존성도 추가해야한다

```
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-bus-kafka</artifactId>
</dependency>
```

# 14장 요약

### [1] 스프링 클라우드 구성서버는 중앙 집중화된 구성 데이터 소스를 마이크로 서비스 기반의

### 더 큰 애플리케이션을 구성하는 모든 마이크로서비스에 제공한다

### [2] 구성 서버가 제공하는 속성들은 백엔드 Git이나 Vault레포지토리에서 유지 관리된다

### [3] 모든 구성 서버 클라이언트에 제공되는 전역적인 속성들에 추가하여 구성 서버는 프로파일에

### 특정된 속성과 애플리케이션에 특ㄹ정된 속성도 제공할 수 있다.

### [4] 보안에 민감한 속성들은 백엔드 Git 레포지토리에 암호화하여 저장하거나 Vault백엔드의

### 보안 속성으로 저장하여 보안을 유지할 수 있다.

### [5] 구성 서버 클라이언트는 새로운 속성으로 리프레쉬할수있다. 이때 액추에이터 엔드 포인트를

### 통해 수동으로 리프레쉬하거나, 스프링 클라우드 버스와 Git웹훅을 사용해서 자동으로 리프레쉬할수있다.





