# 19장 스프링 배포하기

### 이 장에서 배우는 내용
- 스프링 애플리케이션을 WAR나 JAR 파일로 빌드하기
- 스프링 애플리케이션을 클라우드 파운드리에 푸쉬하기
- 스프링 애플리케이션을 도커 컨테이너에 패키징하기

## 🏓 배포 옵션
스프링 부트 애플리케이션은 다음 몇가지 방법으로 빌드하고 실행할 수 있다.
- STS나 IntelliJ등의 IDE에서 애플리케이션을 빌드하고 실행한다
- 메이븐 sprintboot:run이나 그래들 bootRun태스크를 사용하여 명령행에서 애플리케이션을 빌드하고 실행
- 메이븐이나 그래들을 사용해서 실행 가능한 JAR파일(명령행에서 실행되거나 클라우드에 배포될 수 있음)을 생성
- 메이븐이나 그래들을 사용해서 WAR파일(자바 애플리케이션 서버에 배포될수있음)을 생성한다.

개발시에는 이중 어떤 방법을 선택하더라도 애플리케이션을 실행할 수있다. 그러나 프로덕션이나 개발이 아닌 다른환경으로

애플리케이션을 배포할때는 어떨까?

IDE나 메이븐 및 그래들을 사용한 애플리케이션 실행은 프로덕션 환경에는 고려할 수 없다. 그러나 실행 가능한JAR파일이나

자바 WAR파일은 프로덕션 환경에 애플리케이션을 배포하는 확실한 방법이다. 그렇다면 JAR파일이나 자바 WAR파일 중

어떤것을 선택해야할까?

#### `자바 애플리케이션 서버에 배포하기`: 톰캣,웹스피어,웹로직 또는 다른 자바 애플리케익션 서버에 애플리케이션을 배포한다면 여지없이 WAR파일로 애플리케이션 빌드한다.

#### `클라우드에 배포하기`: 클라우드파운드리,AWS,마이코로소프트Azure,GCP또는 이외의 다른 클라우드 플랫폼으로 애플리케이션을 배포한다면 실행가능한 JAR파일이

#### 최상의 선택이다. 그리고 애플리케이션 서버에 적합한 WAR형식보다 JAR형식이 훨씬 간단하므로, 설사 클라우드 플랫폼에서

#### WAR파일 배포를 지원하더라도 JAR파일로 배포하는것이 좋다.

## 👟 WAR파일 빌드하고 배포하기

자바 애플리케이션 서버에 애플리케이션을 배포한다면 WAR파일을 빌드해야한다. 그리고 애플리케이션 서버가 애플리케이션을 실행하는 방법을

알도록 `DispatcherServlet`을 선언하는 서블릿 초기화 클래스도 WAR파일에 포함해야한다. 생성되는 프로젝트가 서블릿 초기화 클래스를 포함하고,

빌드 파일이 WAR파일을 생성하도록 Initializer가 보장해준다. 우선, 스프링의 DispatcherServlet을 구성해야한다. 이것은 종전에 web.xml 파일을 사용해서 처리할 수 있었다.

그러나 스프링 부트는 SpringBootServletInitializer를 사용해서 더 쉽게 해준다. `SrpingBootServletInitializer`는 스프링 `WebApplicationInitializer`인터페이스를

구현하는 스프링 부트의 특별한 구현체다. 스프링의 DispatcherServlet을 구성하는 것 외에도 SpringBootServletInitializer는 Filter,Servlet

,ServletContextInitializer 타입의 빈들을 스프링 애플리케이션 컨텍스트에서 찾아서 서블릿 컨테이너에 바인딩한다. SpringBootServletInitializer를

사용하려면 이것의 서브클래스를 생성하고 `configure()`메소드를 오버라이딩하여 스프링 구성클래스를 지정해야한다.

#### `자바로 스프링 웹 애플리케이션 활성화하기`
```
public class IngredientServiceServletInitializer extends SpringBootServletInitializer{
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder){
        return builder.sources(IngredientServiceApplication.class);
    }
}
```

configure()메소드는 SpringApplicationBuilder를 인자로 받아 반환한다. 그리고 이때 스프링 구성 클래스를 등록하는

sources()메소드를 호출한다. IngredeintServiceApplication클래스만 등록하였는데 이클래스는 부트스트랩 클래스와 스프링

구성 클래스의 이중 용도로 사용한다.`SpringBootServletInitializer`의 서브클래스는 상용구 코드로 되어 있다.

따라서 참조하는 애플리케이션의 메인 구성 클래스만 다르고 이외에는 WAR파일로 빌드되는 모든 애플리케이션이 동일하다.  또한 거의 변경할 일이 없을 것이다.

서블릿 초기화 클래스가 작성되었으므로 이제는 프로젝트 빌드를 변경해야한다. 만일 메이븐으로 빌드한다면 pom.xml파일의 < packaging>요소를 war로설정한다

    <packaging>war</packaging>

그래들의 경우는 아래와같다

    apply plguin: 'war'

## 👟 클라우드 파운드리에 JAR파일 푸쉬하기
서버,하드웨어는 구입과 유지보수 비용이 비싸다 또한, 과중한 부담을 처리하는데 적합한 서버의 확장은 까다롭고 어마어마한 비용이든다.

따라서 오늘날은 클라우드에 애플리케이션을 배포하는것이 자체적인 데이터센터를 운영하는것에 비해 설득력있고 비용효율적이다.

클라우드는 여러 가지 선택할수 있지만 `PaaS`를 제공하는 클라우드가 대세다. `PaaS`는 몇가지 부가 서비스와 함께 미리

구축된 애플리케이션 배포 플랫폼을 제공하여 애플리케이션과 결합한다 또한, 우리 애플리케이션의 실행 인스턴스를

추가 또는 제거하여 규모확장을 쉽고 빠르게 해준다. 클라우드 파운드리는 애플리케이션의 개발, 배포, 확장을 위한 오픈소스/멀티 클라우드 Paas플랫폼이며, 클라우드

파운드리 재단에의해 확장되었다.

## 👟 도커 컨테이너에서 스프링 부트 실행하기
`도커`는 클라우드에서 모든 종류의 애플리케이션을 배포하는 사실상의 표준이 되었다. AWS, 마이크로소프트Azure, 구글클라우드플랫폼,

피보탈 웹서비스 등을 포함하는 서로 다른 많은 클라우드 환경에서 애플리케이션 배포를 위한 도커 컨테이너를 수용한다.

도커로 생성되는 것과 같은 컨테이너 애플리케이션의 아이디어는 실세계의 컨테이너에서 비롯되었다. 이처럼 컨테이너 애플리케이션은 공통된

컨테이너 형식을 공유하므로 컨테이너에 포함된 애플리케이션과 무관하게 어디서든 배포 및 실행할 수있다.

도커 이미지 생성도 어려운것이 아니며 Spotify를 사용하면 스프링 부트 빌드의 결과를 더 쉽게 도커컨테이너로 생성할 수 있다.

```
<build>
    <plugins>
    ...
        <plugin>
            <groupId><com.spotify/groupId>
            <artifactId>dockerfile-maven-plugin</artifactId>
            <version>1.4.3</version>
            <configuration>
                <repository>
                    ${docker.image.prefix}/${project.artifactId}
                </repository>
                <buildArgs>
                    <JAR_FILE>target/${project.build.fileName}.jar</JAR_FILE>
                </guildArgs>
            </configuration>
        </plugin>
    </plugins>
</build>
```
`<configuration>`블록 아래에는 도커 이미지 생성에 필요한 속성들을 설정한다. `<repository>` 요소에는 도커 레포지토리에 나타나는

도커 이미지의 이름을 지정한다. 여기에 지정했듯이 이름의 제일앞에는 docker.image.prefix라는 이름의

메이븐 속성 값이 지정되고 그 다음에는 메이븐 프로젝트의 artifactID가 붙는다. 프로젝트의 artifactID는 바로 위의 artifactId요소에

지정되어있으며 docker.image.prefix속성은 다음과같이 properties요소에 지정해야한다.

```
<properties>
    ...
    <docker.image.prefix><tacocloud/docker.image.prefix>
</properties>
```
이것이 타코클라우드의 식자재 서비스의 pom.xml파일이었다면 결과로 생성되는 도커이미지는 도커 레포지토리의 tacocloud/ingredient-service에 저장되었다.

`<buildArgs>`요소 아레에는 메이븐 빌드가 생성하는 JAR파일을 지정한다. 이때 target디렉토리에 있는 JAR파일의 이름을

결정하기 위해 메이븐 속성인 project.build.fileName을 사용한다. 이처럼 메이븐 빌드 명세에 제공한

정보 외의 다른 도커 이미지 정보는 Dockerfile이라는 이름의 파일에 정의된다. 때부분의 스프링 부트 애플리케이션은 다음과 같이 Dockerfile을 정의할수있다.

```
FROM openjdk:8-jdk-alpine
ENV SPRING_PROFILES_ACTIVE docker
VOLUME /tmp
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", \
            "-Djava.security.egd=file:/dev./urandom".\
            "-jar", \
            "/app.jar"]
```
이 내용을 보면 다음과 같다.

`FRO`M에는 새 이미지 기반이 되는 이미지를 지정한다. 새이미지는 기본이미지를 확장하며 여기서는 OpenJDK 버전 8을 기반으로하는 컨테이너 이미지다.

`ENV`에는 환경변수를 설정한다. 여기서는 활성 프로파일을 기반으로 스프링 부트 애플리케이션의 구성 속성을 변경할것이므로 SPRING_PROFILES_ACTIVE를 docker로 설정하였다.

`VOLUME`은 컨테이너의 마운트 지점을 생성한다. 여기서는 필요시에 컨테이너가 /tmp디렉터리에 데이터를 쓸수있도록 /tmp에 마운트 지점을 생성한다

`ARG`에는 빌드 시에 전달할 수 있는 인자를 선언한다. 여기서는 메이븐 플러그인의 < buildArgs>블록에

지정된 인자와 동일한 JAR_FILE이라는 이름의 인자를 선언한다

`COPY`는 지정된 경로의 파일을 다른경로로 복사한다. 여기서는 메이븐 플러그인에 지정된 JAR파일을 app.jar라는 이름의 파일로 도커이미지에 복사한다

`ENYTYPOINT`에는 컨테이너가 시작될 때 실행하기 위한 명령행 코드를 배열로 지정한다.

# 19장 요약
### [1]스프링 애플리케이션은 종전의 애플리케이션 서버, 클라우드 파운드리와 같은 Paas,도커 컨테이너 등을

### 포함해서 서로 다른 환경에 배포할 수 있다.

### [2] WAR파일을 빌드할 때는 스프링의 DispatcherServlet이 적합하게 구성되도록 SpringBootServletInitializer의

### 서브클래스를 포함해야한다

### [3] 실행가능 JAR파일을 빌드하면 WAR파일의 부담없이 스프링 부트 애플리케이션을 다수의 클라우드

### 플랫폼에 배포할 수 있다.

### [4] Spotify의 메이븐 플러그인을 사용하면 스프링 애플리케이션을 컨테이너에 패키징하기 쉽다. 이플러그인은

### 실행 가능 JAR파일을 도커 컨테이너에 래핑하므로 AWS, 마이크로소프트 Azure, 구글클라우드 플랫폼,

### PWS, PKS등을 포함하여 도커 컨테이너가 배포될 수 있는 곳이면 어디든지 애플리케이션을 배포할 수 있다.