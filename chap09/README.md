# 🥇 9장 스프링 통합하기
### [해당내용 공식문서](https://spring.io/projects/spring-integration)
## 💕 이 장에서 배우는 내용
- 실시간으로 데이터 처리하기
- 통합 플로우 정의하기
- 스프링 통합의 자바 DSL 정의 사용하기
- 이메일과 파일 시스템 및 다른 외부 시스템과 통합하기

이번 장에서는 `스프링 통합(Spring Integration)`으로 통합패턴을 사용한느 방법을 배울것이다.

스프링 통합은 << Enterprise Integration Patterns>>에서 보여준 대부분의 통합 패턴을 사용할 수 있게 구현한 것이다.

각 통합 패턴은 하나의 컴포넌트로 구혀노디며, 이것을 통해서 파이프라인으로 메시지가 데이터를 운반한다.

스프링 구성을 사용하면 데이터가 이동하는 파이프라인으로 이런 컴포넌트들을 조립할 수 있다. 

### 🌟 간단한 통합 플로우 선언하기

애플리케이션은 통합 플로우를 통해서 외부 리소스나 애플리케이션 자체에 데이터를 수신 또는 전송할 수 있으며, 스프링 통합은 이런

통합 플로우를 생성할 수 있게 해준다. 애플리케이션이 통합할 수 있는 그런 리소스 중 하나가 파일 시스템이다. 

이에 따라 스프링 통합의 많은 컴포넌트 중에 파일을 읽거나 쓰는 채널 어댑터(channel adapter)가있다

먼저 파일시스템에 데이터를 쓰는 통합 플로우를 생성하기에 앞서 의존성을 추가해보자.
```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-integration</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.integration</groupId>
    <artifactId>spring-integration-file</artifactId>
</dependency>
```

첫 번째 의존성은 스프링 통합의 스프링 부트 스타터다. 통합하려는 플로우와 무관하게 이 의존성은 스프링 통합 플로우의 개발 시에

반드시 추가해야한다. 

두 번째 의존성은 스프링 통합의 파일 엔드포인트 모듈이다. 이 엔드포인트모듈이 통합플로우로 파일을 읽거나, 통합 플로우로부터

파일 시스템으로 데이터를 쓸 수 있는 기능을 제공한다.

그 다음은 파일에 데이터를 쓸 수 있도록 애플리케이션에서 통합 플로우로 데이터를 전송하는 게이트웨이를 생성해야한다.

#### 🏭메소드 호출을 메시지로 변환하는 메시지 게이트웨이 인터페이스
```
@MessagingGateway(defaultRequestChannel="textInChannel")
public interface FileWriterGateway{
    void writeToFIle(
        @Header(FileHeaders.FILENAME) String filename,
        String data);
}
```

FileWriterGateWay에는 `@MessagingGateway`가 지정되었다. 이 애노테이션은 FileWriterGateway 인터페이스의

구현체(클래스)를 런타임 시에 생성하라고 스프링 통합에 알려준다. `@MessagingGateway`의 defaultRequestChannel 속성은

해당 인터페이스의 메소드 호출로 생성된 메시지가 이 속성에 지정된 메시지 채널로 전송된다는 것을 나타낸다.

여기서는 wirteToFIle()의 홏룰로 생긴 메시지가 textInChannel이라는 이름의 채널로 전송된다.

`writeToFile()`메소드는 두 개의 String타입 매개변수를 갖는다. 파일 이름과 파일에 쓰는 텍스트를 포함하는 데이터이다.

여기서 finename 매개변수엔느 @Header가 지정되었다. @Header애노테이션은 filename에 전달되는 값이 메시지

페이로드가 아닌 메시지 헤더에 있다는 것을 나타낸다. 반면에 data 매개변수 값은 메시지 페이로드로 전달된다.

이제 메시지 게이트웨이가 생성되었으므로 통합 플로우를 구성해야한다. 통합플로우는 다음 세가지 구성방법으로 정의할수있다.
- XML구성
- 자바구성
- DSL을 사용한 자바 구성

---

### 🌟 XML을 사용해서 통합 플로우 정의하기

XML구성의 사용은 이제는 거의쓰지않지만 스프링통합에서는 오랫동안 XML로 통합플로우를 정의했으므로 예 하나만 보자.!

#### 🏭 스프링 XML 구성을 사용해서 통합 플로우 정의하기

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:int="http://www.springframework.org/schema/integration"
    xmlns:int-file="http://www.springframework.org/schema/integration/file"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
    http://www.springframework.org/schema/beans/spring-beans.xsd
    http://www.springframework.org/schema/integration
    http://www.springframework.org/schema/integration/spring-integration.xsd
    http://www.springframework/org/schema/integration/file
    http://www.springframework.org/schema/integration/file/springintegration-file.xsd">

    <int:channel id="textIncChannel" />
    <int:transformer id="upperCase"
        input-channel="textInChannel"
        output-channel="fileWriterChannel"
        expression="payload.toUpperCase()"/>

    <int:channel id="fileWriterChannel"/>

    <int-file:outbound-channel-adapter id="writer"
        channel="fileWriterChannel"
        directory="/tmp/sia5/files"
        mode="APPEND"
        append-new-line="true"/>
    </beans>
```

xml 구성에서 주목할 내용은 다음과 같다.

#### 1. textInChannel이라는 이름의 채널을 구성하였다. 이것은 FileWriterGateway의 요청채널과 설정된 것과 같은 채널이다.

#### FileWriterGateway의 writeToFIle()메소드가 호출되면 결과 메시지가 textInChannel로 전달된다.

#### 2. textInChannel로부터 메시지를 받는 변환기를 구성하였다. 이 변환기는 SpEL표현식을 사용해서 메시지 

#### 페이로드에 대해 toUpperCase()를 호출하여 대문자로 변환한다. 그리고 변환된 결과는 fileWriter Channel로전달된다.

#### 3. fileWriterChannel이라는 이름의 채널을 구성하였다. 이 채널은 변환기와 아웃바운드 채널 어댑터를 

#### 연결하는 전달자의 역할을 수행한다.

#### 4. 끝으로 int-file 네임스페이스를 사용하여 아웃바운드 채널 어댑터를 구성하였다. 이 XML 네임스페이스는

#### 파일에 데이터를 쓰기 위해 스프링 통합의 파일모듈에서 제공한다. 아웃바운드 채널 어댑터는 fileWriterChannel로 부터

#### 메시지를 받은 후 해당 메시지 페이로드를 directory속성에 지정된 디렉터리의 파일에 쓴다. 이 때

#### 파일 이름은 해당 메시지의 file_name헤더에 지정된 것을 사용한다. 만일 해당 파일이 이미있으면 기존 데이터에

#### 덮어쓰지않고 줄을 바꾸어 제일 끝에 추가한다.

![image](https://user-images.githubusercontent.com/40031858/108586173-09b0d200-7390-11eb-8f93-adfdb8d782e7.png)


스프링 부트 애플리케이션에서 XML구성을 사용하고자 한다면 XML을 리소스로 import해야한다.

이때 우리 애플리케이션의 자바 구성 클래스 중 하나에 스프링의 `@ImportResource`애노테이션을 지정하는것이 가장쉬운 방법이다.

예를들어,
```
@Configuration
@ImportResource("classpath:/filewriter-config.xml")
public class FileWriterIntegrationConfig{...}
```
이렇게 스프링 통합에서 XML기반의 구성을 사용해도 좋지만 많은 개발자들이 사용을 꺼리고 여러 이유로 여기까지만 알아보자.

## 💕 Java로 통합 플로우 구성하기

현재는 대부분의 스프링 애플리케이션이 XML 구성을 피하고 자바 구성을 사용한다. 실제 스프링 부트 애플리케이션에서 

자바 구성은 스프링의 자동-구성을 자연스럽게 보완해주는 방법이다. 따라서 스프링 부트 애플리케이션에 통합 플로우를

추가할 때는 XML보다는 자바로 플로우를 정의하는 것이 좋다.

#### 🏭 자바 구성을 사용해서 통합 플로우 정의하기

```
@Configuration
public class FileWriterIntegrationConfig{
    @Bean
    @Transformer(inputChannel="textInChannel",
                outputChannel="fileWriterChannel")
    public GenericTransformer<String,String> upperCaseTransformer(){
        return text->text.toUpperCase();
    }

    @Bean
    @ServiceActivator(inputChannel="fileWriterChannel")
    public FileWritingMessageHandler fileWriter(){
        FileWritingMessageHandler handler=
                new FileWritingMessageHandler(new File("/tmp/sia5/files));
        handler.setExpectReply(false);
        handler.setFileExistsMode(FileExistsMode.APPEND);
        handler.setAppendNewLine(true);
        return handler;
    }
}

```

이 자바 구성에서는 두 개의 빈을 정의한다. 변환기와 파일-쓰기 메시지 핸들러다. 변환기법인 `GenericTransformer`는 함수형

인터페이스이므로 메시지 텍스트에 toUpperCase()를 호출하는 람다로구현할 수 있다. GenericTransformer에는 

`@Transformer`가 지정되었다. 이 애노테이션은 GenericTransformer가 textInChannel의 메시지를 받아서

fileWriterChannel로 쓰는 통합 플로우 변환기라는 것을 지정한다.

파일- 쓰기 빈에는 `@ServiceActivator`가 지정되었다. 이 애노테이션은 fileWriterChannel로부터 메시지를 받아서 FileWriteringMessageHandler의 인스턴

스로 정의된 서비스에 넘겨줌을 나타낸다. FileWritingMessageHandler는 메시지 핸들러이며, 메시지 페이로드를

지정된 디렉터리의 파일에 쓴다. 이때 파일 이름은 해당 메시지의 file_name헤더에 지정된 것을 사용한다. 그리고 

XMl구성과 동일하게 해당 파일이 이미 있으면 기존 데이터에 덮어 쓰지않고 줄을 바꾸어 제일 끝에 추가한다.


`FileWritingMessageHandler` 빈의 구성에서 한 가지 특이한 것은 setExpectReply(false)를 호출한다는 것이다.

이 메소드는 서비스에서 응답 채널(플로우의 업스트림 컴포넌트로 값이 반환될 수 있는 채널)을 사용하지 않음을 나타낸다.

만일 setExpectReply(false)를 호출하지 않으면, 통합 플로우가 정상적으로 작동하더라도 응답 채널이 구성되지 않았다는 로그메시지가나타난다.

위의 자바구성에서 채널들을 별도 선언하지 않았는데 textInCHannel과 fileWriterChannel이라는 이름의

빈이 없으면 이 채널들은 자동으로 생성되기 때문이다 그러나 각 채널의 구성방법을 제어하고싶다면 별도의 빈으로 구성할 수 있다.

```
@Bean
public MessageChannel textInChannel(){
    return new DirectChannel();
}

...

@Bean
public MessageChannel fileWriterChannel(){
    return new DirectChannel();
}
```

이러한 자바중심의 구성방향이 구성방법또한 알기쉽다. 그러나 스프링 통합의 자바 DSL구성 방법을 사용하면 코드를 더 간소화할수있다.

## 💕 스프링 통합의 DSL구성 사용하기
스프링 통합의 자바 DSL을 사용하면 각 컴포넌트를 별도의 빈으로 선언하지 않고 전체 플로우를 하나의 빈으로 선언한다.

```
@Configuration
public class FileWriterIntegrationConfig{

    @Bean
    public IntegrationFlow fileWriterFlow(){
        return IntegrationFLows
            .from(MessageChannels.direct("textInChannel"))
            .<String, String>transform(t -> t.toUpperCase())
            .handle(Files
                .outboundAdapter(new File("/tmp/sia5/files"))
                .fileExistsMode(FileExistsMode.APPEND)
                .appendNewLine(true))
                .get();
    }
}
```

이 구성은 전체 플로우를 하나의 빈 메소드에 담고있어서 코드를 최대한 간결하게 작성할 수 있다. IntegrationFlows클래스는 플로우를 선언할수 있는 빌더 API를시작시킨다.

textInChannel이라는 이름의 채널로부터 메시지를 수신하면서 시작하고 그다음 메시지 페이로드를 대문자로 바꾸는 변환기가 실행된다.

그리고 변환된 메시지는 스프링 통합의 파일 모듈에 제공되는 Files타입으로부터 생성된 아웃바운드 채널 어댑터에서 처리된다.

끝으로 .get()을 호출하여 return문에서 반환되는 IntegrationFlow인스턴스를 가져온다. 이렇게 하나의 빈메서드가 통합플로우를 정의한다.

자바 구성 예와 마찬가지로 여기서도 채널 빈을 따로 선언할 필요없다. 별도로 선언되지 않은 textInChannel을 참조하더라도 같은 이름의 

채널 빈이 없어서 스프링 통합이 자동생성해 주기 때문이다. 그러나 원한다면 해당 채널빈을 별도로 선언할 수 있다.

변환기를 아웃바운드 채널 어댑터와 연결하는 채널의 경우에 이 채널을 별도로 구성할 필요가 있다면 다음과 같이 플로우 정의에서 channel()을호출해 참조할수있다.
```
@Bean
public IntegrationFlow fileWriterFlow(){
    return IntegrationFlows
        .from(MessageChannels.direct("textInChannel"))
        .<String,String>transform(t -> t.toUpperCase())
        .channel(MessageChannels.direct("fileWriterChannel"))
        .handle(Files
            .outboundAdapter(new File("/tmp/sia5/files"))
            .fileExistsMode(FileExistsMode.APPEND)
            .appendNewLine(true))
            .get();
}
```


---

# ⚡ 스프링 통합의 컴포넌트 살펴보기

통합 플로우는 하나 이상의 컴포넌트로 구성되며, 그 내역은 다음과 같다. 더 이상의 코드를 작성하기 앞서

각 컴포넌트가 통합 플로우에서 맡은 역할을 간단히 알아보자.

- 채널(Channel): 한 요소로부터 다른 요소로 메시지를 전달한다
- 필터(Filter):조건에 맞는 메시지가 플로우를 통과하게 해준다
- 변환기(Transformer): 메시지 값을 변경하거나 메시지 페이로드의 타입을 다른 타입으로 변환한다
- 라우터(Router):여러 채널 중 하나로 메시지를 전달하며, 대게 메시지 헤더를 기반으로 한다
- 분배기(Splitter):들어오는 메시지를 두 개 이상의 메시지로 분할하며, 분할된 각 메시지는 다른 채널로 전송된다.
- 집적기(Aggregator):분배기와 상반된 것으로 별개의 채널로부터 전달되는 다수의 메시지를 하나의 메시지로 결합한다
- 서비스 액티베이터(Service activator): 메시지를 처리하도록 자바 메소드에 메시지를 넘겨준후 메서드의 반환값을 출력채널로 전송
- 채널 어댑터(Channel adpater):외부 시스템에 채널을 연결한다. 외부 시스템으로부터 입력을 받거나 쓸 수 있다.
- 게이트웨이(Gateway):인터페이스를 통해 통합 플로우로 데이터를 전달한다

### ⭐ 메시지 채널
메시지 채널은 통합 파이프라인을 통해서 메시지가 이동하는 수단이다. 즉 채널은 스프링 통합의 다른 부분을 연결하는 통로이다.

#### 메시지 채널은 통합 플로우의 서로 다른 컴포넌트 간에 데이터를 전달하는 통로이다.

스프링 통합은 다음을 포함해 여러 채널 구현체를 제공한다.
#### 🧇 PublishSubscribeChannel: 이것으로 전송되는 메시지는 하나 이상의 컨슈머로 전달된다.

#### 컨슈머가 여럿일 때는 모든 컨슈머가 해당 메시지를 수신한다.

#### 🧇 QueueChannel: 이것으로 전송되는 메시지는 FIFO 방식으로 컨슈머가 가져갈 때까지 큐에 저장된다.

#### 컨슈머가 여럿일 때는 그 중 하나의 컨슈머만 해당 메시지를 수신한다

#### 🧇 PriorityChannel: QueueChannel과 유사하지만, FIFO방식 대신 메시지의 priority헤더를 기반으로 컨슈머가 메시지를 가져간다.

#### 🧇 RendezvousChannel:QueueChannel과 유사하지만, 컨슈머가 메시지를 수신할 때까지 메시지 전송자가 채널을 차단한다는것이 다르다.

#### 🧇 DirectChannel: PublishSubscribeChannel과 유사하지만 전송자와 동일한 스레드로 실행되는 컨슈머를

#### 호출하여 단일 컨슈머에게 메시지를 전송한다. 이 채널은 트랜잭션을 지원한다.

#### 🧇 ExecutorChannel: DirectChannel과 유사하지만 TaskExecutor를 통해서 메시지가 전송된다. 이 채널타입은 트랜잭션을 지원하지않는다.

#### 🧇 FluxMessageChannel: 프로젝트 리액터의 플럭스를 기반으로하는 리액티브 스트림즈 퍼블리셔 채널이다.

자바 구성과 자바 DSL 구성 모두에서 입력 채널은 자동으로 생성되며, 기본적으로 DirectChannel이 사용된다. 

그러나 다른 채널 구현체를 사용하고 싶다면 해당 채널을 별도의 빈으로 선언하고 통합 플로우에서 참조해야한다.

예를들어 PublishSubscribeChannel을 선언하려면 다음과같이 @Bean이 지정된 메소드를 선언한다
```
@Bean
public MessageChannel orderChannel(){
    return new PublishSubscribeChannel();
}
```

그다음 통합플로우 정의에서 이 채널을 이름으로 참조한다. 예를들어, 이 채널을 서비스 액티베이터에서 소비한다면 

`@ServiceActivator`애노테이션의 inputChannel 속성에서 이 채널이름으로 참조하면 된다
    
    @ServiceActivator(inputChannel="orderChannel")

또는 자바 DSL 구성을 사용할 때는 channel() 메소드의 호출에서 참조한다.
```
@Bean
public IntegrationFlow orderFlow(){
    return IntegrationFlows
        ...
        .channel("orderChannel")
        ...
        .get();
}
```

`QueueChannel`을 사용할 때는 컨슈머가 이 채널을 폴링하도록 구성하는 것이 중요하다. 예를들어 다음과 같이 빈을 선언햇다고 해보자.
```
@Bean
public MessageChannel orderChannel(){
    return new QuqueChannel();
}
```

이것을 입력 채널로 사용할 때 컨슈머는 도착한 메시지 여부를 폴링해야한다. 컨슈머가 서비스 액티베이터인 경우는 다음과 같이

`@ServiceActivator`애노테이션을 지정할 수 있다.

```
@ServiceActivator(inputChannel="orderChannel", poller=@Poller(fixedRate="1000"))
```
이 서비스 액티베이터는 orderChannel이라는 이름의 채널로부터 매 1초당 1번씩 읽을 메시지가 있는지 확인한다.

### 🌟 필터
필터는 통합 파이프라인의 중간에 위치할 수 있으며, 플로우의 전 단계로부터 다음단계로의 메시지 전달을 허용 또는 불허한다.

예를들어, 정수 값을 갖는 메시지가 numberChannel이라는 이름의 채널로 입력되고, 짝수인 경우만 evenNumberChannel이라는

이름의 채널로 전달된다면 이경우 다음과 같이 @Filter애노테이션이 지정된 필터를 선언할 수 있다.
```
@Filter(inputChannel= "numberChannel",
        outputChannel="evenNumberChannel")
    public boolean evenNumberFilter(Integer number){
        return number %2 ==0;
    }
```
또는 자바 DSL구성을 사용해서 통합 플로우를 정의한다면 다음과 같이 filter()메소드를호출할수있다.
```
@Bean
public IntegrationFlow evenNumberFlow(AtomicInteger integerSource){
    return IntegrationFlows
        ...
        .<Integer>filter((p) -> p%2 ==0)
        ...
        .get();
}
```

여기서는 람다를 사용해 필터를 구현했지만, 실제로는 filter()메소드가 GenericSelector를 인자로 받는다.

### 🌟 변환기.

변환기는 메시지 값의 변경이나 타입을 변환하는 일을 수행한다. 변환 작업은 숫자 값의 연산이나 문자열 값 조작과 같은 간단한 것이 될수도있다.

또는 ISBN을 나타내는 문자열을 사용해 검색후 해당 책의 자세한 내용을 반환하는 것같은 복잡한 작업도 가능하다.

예를들어, 정수 값을 포함하는 메시지가 numberChannel이라는 이름의 채널로 입력되고, 이 숫자를 로마 숫자를 포함하는

문자열로 변환한다고 해보자. 이 경우 다음과 같이 `@Transformer`애노테이션을 지정하여 `GenericTransformer`타입의 빈을 선언할수있다.

```
@Bean
@Transformer(inputChannel="numberChannel",
            outputChannel="romanNumberChannel")
    public GenericTransformer<Integer, String> romanNumTransformer(){
        return RomanNumbers::toRoman;
    }
```

`@Transformer`애노테이션은 이 빈을 변환기 빈으로 지정한다. 즉 numberChannel이라는 이름의 채널로부터

Integer값을 수신하고 static 메소드인 toRoman()을 사용해서 변환을 수행한다. 그리고 반환 결과는

romanNumberChannel이라는 이름의 채널로 전송된다. 자바 DSl구성에서는 toRoman()메소드의

메소드 참조를 인자로 전달하여 transform()을 호출하므로 더 쉽다.

```
@Bean
public IntegerationFlow transformerFlow(){
    return IntegrationFlows
        ...
        .transform(RomanNumbers::toRoman)
        ...
        .get();
}
```

### 🌟 라우터

라우터는 전달 조건을 기반으로 통합 플로우 내부를 분기(서로 다른 채널로 메시지를 전달) 한다.

예를 들어, 정수값을 전달하는 numberChannel이라는 이름의 채널이 있고 모든 작수 메시지는 evenChannel이라는 이름의 채널로 전달하고,

홀수 메시지는 oddChannel이라는 이름의 채널로 전달한다고 하자. 이 라우터를 통합플로우에 생성할 때는

`@Router`가 지정된 AbstractMessageRouter 타입의 빈을 선언하면 된다.

```
@Bean
@Router(inputChannel="numberChannel")
public AbstractMessageRounter evenOddRouter(){
    return new AbstractMessageRouter(){
        @Override
        protected Collection<MessageChannel> determineTargetChannels(Message<?> message){
            Integer number= (Integer) message.getPayload();
            if(number%2==0){
                return Collections.singleton(evenChannel());
            }
            return Collections.singleton(oddChannel());
        }
    };
}

@Bean
public MessageChannel evenChannel(){
    return new DirectChannel();
}

@Bean 
public MessageChannel oddChannel(){
    return new DirectChannel();
}
```

여기서 선언한 AbstractMessageRouter 빈은 numberChannel이라는 이름의 입력 채널로부터 메시지를 받는다.

그리고 이 빈을 구현한 익명의 내부 클래스에서는 메시지 페이로드를 검사하여 짝수일때는 evenChaannel이라는 이름의 채널을 반환한다.

그리고 짝수가 아닐때는 입력 채널 페이로드의 숫자가 홀수일 것이므로 이때는 oddChannel이라는 이름의 채널이 반환된다.

자바 DSL 구성에서는 다음과 같이 플로우 정의에서 route()메소드를 호출하여 라우터를 선언한다
```
@Bean
public IntegrationFlow numberRoutingFlow(AtomicInteger source){
    return IntegrationFlows
        ...
            .<Integer,String>route(n -> n%2==0 ? "EVEN": "ODD", mapping -> mapping
            .subFlowMapping("EVEN",
            sf -> sf.<Integer,Integer> transform(n->n*10)
                .handle((i,h)->{...})
                )
                .subFlowMapping("ODD",sf -> sf
                    .transform(RomanNumbers::toRoman)
                    .handle((i,h)-> {...}))
                )
                .get();
}
```

### 🌟 분배기 

때로는 통합 플로우에서 하나의 메시지를 여러 개로 분할하여 독립적으로 처리하는 것이 유용할 수 있다.

분배기가 그런 메시지를 분할하고 처리해준다.

분배기는 여러 상황에서 유용하다 특히, 분배기를 사용할 수 있는 중요한 두 가지 경우가 있다.

#### 🧇 [1] 메시지 페이로드가 같은 타입의 컬렉션 항목들을 포함하며, 각 메시지 페이로드 별로 처리하고자 할때다.

#### 🧇 [2] 연관된 정보를 함께 전달하는 하나의 메시지 페이로드는 두 개 이상의 서로 다른 타입 메시지로 분할될 수 있다.

하나의 메시지 페이로드를 두 개이상의 서로 다른 타입 메시지로 분할할 때는 수신페이로드의 각 부분을 추출하여 컬렉션의

요소들로 반환하는 POJO를 정의하면 된다. 예를들어, 주문 데이터를 전달하는 메시지는 대금 청구 정보와

주문 항목 리스트의 두가지 메시지로 분할할 수 있다.
```
public class OrderSplitter{
    public Collection<Obejct> splitOrderIntoParts(PurchaseOrder po){
        ArrayList<Object> parts=new ArrayList<>();
        parts.add(po.getBillingInfo());
        parts.add(po.getLineItems());
        return parts;
    }
}
```

그 다음에 `@Splitter`애노테이션을 지정하여 통합 플로우의 일부로 OrderSplitter빈을 선언할수 있다.
```
@Bean
@Splitter(inputChannel="poChannel",
            outputChannel="splitOrderChannel")
    public OrderSplitter orderSplitter(){
        return new OrderSplitter();
    }
```

여기서는 주문 메시지가 poChannel이라는 이름의 채널로 도착하며, OrderSplitter에 의해 분할된다. 

그 다음에 컬렉션으로 반환되는 각 항목은 splitOrderChannel이라는 이름의 채널에 별도의 메시지로 전달한다. 

플로우의 이 지점에서 PayloadTypeRouter를 선언하여 대금 청구 정보와 주문 항목 정보를 각 정보에 적합한 하위플로우로 전달할 수 있다.

```
@Bean
@Router(inputChannel="splitOrderChannel")
public MessageRouter splitOrderRouter(){
    PayloadTypeRouter router=new PayloadTypeRouter();
    router.setChannelMapping(
        BillingInfo.class.getName(), "billingInfoChannel");
        
        router.setChannelMapping(
            List.class.getName(), "lineItemsChannel");
        return router;
}
```

PayloadTypeRouter는 각 페이로드 타입을 기반으로 서로다른 채널에 메시지를 전달한다. 즉 BillingInfo타입의

페이로드는 billingInfoChannel로 전달되어 처리되며 컬렉션에 저장된 주문 항목 들은 List타입으로 lineItemsChannel에 전달된다.

여기서는 하나의 플로우가 두 개의 하위 플로우로 분할된다. BillingInfo객체가 전달되는 플로우와 List< LineItem>이 전달되는 플로우다.

그러나 List< LineItem>을 처리하는 대신 각 LineItem을 별도로 처리하고 싶다면 어떻게 해야할까? 

이때는 List< LineItem>을 다수의 메시지로 분할하기 위해 @Splitter애노테이션을 지정한 메소드를 작성하고 이 메소드에는 처리된

LineItem이 저장된 컬렉션을 반환하면된다.

```
@Splitter(inputChannel="lineItemChannel", outputChannel="lineItemChannel")
public List<LineItem> lineItemSplitter(List<LineItem> lineItems){
    return lineItems;
}
```

자바 DSl을 사용해서 이와 동일한 분배기/라우터 구성을 선언할때는 다음과같이 split()과 route()메소드를 호출하면된다.

```
return IntegrationFlows
    ...
        .split(orderSplitter())
        .<Object, String> route(
            p -> {
                if(p.getClass().isAssignableFrom(BillingInfo.class)){
                    return "BILLING_INFO";
                }else{
                    return "LINE_ITEMS";
                }
            }, mapping -> mapping
                .subFlowMapping("BILLING_INFO",
                    sf -> sf.<BillingInfo> handle((billingInfo,h)->{
                        ...
                    }))
                .subFlowMapping("LINE_ITEMS",
                    sf->sf.split()
                        .<LineItem> handle((lineItem,h)->{
                            ...
                        }))
        ).get();
```

### 🌟 서비스 액티베이터
서비스 액티베이터는 입력 채널로부터 메시지를 수신하고 이 메시지를 MessageHandler인터페이스를 구현한 클래스(빈)에 전달한다.

#### `서비스 액티베이터는 메시지를 받는 즉시 MessageHandler를 통해 서비스를 호출한다`

스프링통합은 MessageHandler를 구현한 여러 클래스를 제공한다. 그러나 서비스 액티베이터의 기능을

수행하기 위해 커스텀 클래스를 제공해야 할 때가 있다. 예를들어, 다음 코드에서는 서비스 액티베이터로

구성된 MessageHandler빈을 선언하는 방법을 보여준다
```
@Bean
@ServiceActivator(inputChannel="someChannel")
public MessageHandler sysoutHandler(){
    return message -> {
        System.out.println("Message payload: "+ message.getPayload());
    }
}
```
someChannel이라는 이름의 채널로부터 받은 메시지를 처리하는 서비스 액티베이터로 지정하기 위해 이 빈은 `ServiceActivator`애노테이션이 지정되었다.

MessageHandler자체는 람다로 구현했으며 메시지를 받으며 이것의 페이로드를 표준 출력스트림으로 보낸다.

또는 받은 메시지의 데이터를 처리한 후 새로운 페이로드를 반환하는 서비스 액티베이터를 선언할 수 도 있다.

이경우 이 빈은 MessageHandler가 아닌 GenericHandler를 구현한것이어야한다.
```
@Bean
@ServiceActivator(inputChannel="orderChannel",
                outputChannel="completeChannel")
    public GenericHandler<Order> orderHandler(OrderRepository orderRepo){
        return (payLoad, headers)->{
            return orderRepo.save(payload);
        }
    }
```

이 서비스 액티베이터는 Order타입의 메시지 페이로드를 처리하는 GenericHandler를 구현하며, 주문 메시지가 도착하면

레포지토리를 통해 저장한다. 그리고 저장된 Order 객체가 반환되면 completeChannel이라는 이름의 출력채널로 전달된다.

GenericHandler는 메시지 페이로드는 물론이고 메시지 헤더도 받는다는것을 알아두자. 

또한 이도 자바 DSL구성으로도 사용할 수 있다. 플로우 정의에서 handle()메소드의 인자로 MessageHandler나 GenericHandler를전달하면된다
```
public IntegrationFlow someFlow(){
    return IntegrationFlows
        ...
            .handle(msg -> {
                System.out.println("Message payload: " + msg.getPayload());
            }).get();
}
```

### 🌟 게이트웨이
게이트웨이는 애플리케이션이 통합 플로우로 데이터를 제출하고 선택적으로 플로우의 처리 결과인 응답을 받을 수 있는 수단이다.

![image](https://user-images.githubusercontent.com/40031858/108596171-b148f700-73c6-11eb-97c5-f01bfef17516.png)


#### 🏭 서비스 게이트웨이는 애플리케이션이 통합 플로우로 메시지를 전송할 수 있는 인터페이스이다.

앞서 FileWriterGateway를 사용한 메시지 게이트웨이의 예는 이미 봤는데 FileWriterGateway는 단방향 게이트웨이이며,

파일에 쓰기 위한 문자열을 인자로 받고 void를 반환하는 메소드를 갖고있다, 양방향 게이트웨이의 작성도 

어렵지 않으며, 이때는 게이트웨이 인터페이스를 작성할 때 통합 플로우로 전송할 값을 메소드에 반환해야한다.

예를 들어, 문자열을 받아서 모두 대문자로 변환하는 간단한 통합 플로우의 앞 쪽에 잇는 게이트웨이를 생각해보자. 이 게이트웨이 인터페이스는 다음과다.

```
@Component
@MessagingGateway(defaultRequestChannel="inChannel",
            defaultReplyChannel="outChannel")
    public interface UpperCaseGateway{
        String uppercase(String in);        
    }
```

놀라운 사실은 이 인터페이스를 구현할 필요가 없다는 것이다. 지정된 채널을 통해 데이터를 전송하고

수신하는 구현체를 스프링 통합이 런타임 시에 자동으로 제공하기 때문이다.

uppercase()가 호출되면 지정된 문자열이 통합 플로우의 inChannel로 전달된다. 그리고 플로우가 어떻게 정의되고

무슨일을 하는지와 상관없이 데이터가 outChannel로 도착하면 uppercase()메소드로부터 반환된다.

이것을 자바 DSL구성으로 나타내면다음과같다
```
@Bean
public IntegrationFlow uppercaseFlow(){
    return IntegrationFlows
        .from("inChannel")
        .<String,String> transform(s -> s.toUpperCase())
        .channel("outChannel")
        .get();
}
```

여기서 inChannel로 데이터가 입력되면서 플로우가 시작된다. 그다음 대문자로 변환하기 위해 람다로 정의된 변환기에

의해 메시지 페이로드가 변환되고 그리고 결과 메시지는 outChannel로 전달된다.

### 🌟 채널 어댑터

채널 어댑터는 통합 플로우의 입구와 출구를 나타낸다. 데이터는 인바운드 채널 어댑터를 통해 통합플로우로 들어오고,

아웃바운드 채널 어댑터를 통해 통합플로우에서 나간다. 인바운드 채널 어댑터는 플로우에 지정된

데이터 소스에 따라 여러가지 형태를 갖는다. 예를들어 증가되는 숫자를 AtomicInteger로부터 플로우로

넣는 인바운드 채널 어댑터를 선언할 수 있다. 자바 구성을 사용해서 작성하면 다음과 같다.
```
@Bean
@InboundChannelAdapter(
    poller=@Poller(fixedRate="1000"), channel="numberChannel")
    public MessageSource<Integer> numberSource(AtomicInteger source){
        return ()->{
            return new GenericMessage<>(source.getAndIncrement());
        };
    }
```

이 @Bean 메소드는 `@InboundChannelAdpater`애노테이션이 지정되었으므로 인바운드 채널 어댑터 빈으로 선언된다.

이 빈은 주입된 AtomicInteger로부터 numberChannel이라는 이름의 채널로 매초마다 한번씩 숫자를 전달한다

자바 구성에서는 `@InboundChannelAdpeter가 인바운드 채널 어댑터를 지정하지만, 자바DSL의 경우에는 from()메소드가

인바운드 채널어댑터의 일을 수행한다. 자바 구성의 것과 유사한 인바운드 채널 어댑터를 자바 DSL로 정의하면 다음과같다.
```
@Bean
public IntegrationFlow someFlow(AtomicInteger integerSource){
    return IntegrationFlows
        .from(integerSource, "getAndIncrement",
            c-> c.poller(Pollers.fixedRate(1000)))
            ...
            .get();
}
```

종종 채널 어댑터는 스프링 통합의 여러 엔드포인트 모듈 중 하나에서 제공된다. 예를들어, 지정된 디렉터리를 모니터링하여

해당 디렉터리에 저장하는 파일을 file-channel이라는 이름의 채널에 메시지로 전달하는 인바운드 채널 어댑터가 필요하다고 해보자

이 경우 스프링통합 파일 엔드포인트 모듈의 FileReadingMessageSource를 사용하는 다음의 자바구성으로 구현할 수 있다
```
@Bean
@InboundChannelAdapter(channel="file-channel",
                    poller=@Poller(fixedDelay="1000"))
    public MessageSource<File> fileReadingMessageSource(){
        FileReadingMessageSource sourceReader=new FileReadingMessageSource();
        sourceReader.setDirecotry(new File(INPUT_DIR));
        sourceReader.setFilter(new SimplePatternFileListFilter(FILE_PATTERN));
        return sourceReader;
    }
```
이것과 동일한 파일-읽기 인바운드 채널 어댑터를 자바 DSL로 작성할 때는 Files 클래스의 inboundAdapter()메소드를 사용할 수 있다. 
`
아웃바운드 채널어댑터는 통합 플로우의 끝단이며, 최종 메시지를 애플리케이션이나 다른시스템에 넘겨준다
```
@Bean
public IntegrationFlow fileReaderFlow(){
    return IntegrationFlows
        .from(Files.inboundAdapter(new File(INPUT_DIR))
            .patternFilter(FILE_PATTERN))
            .get();
}
```

메시지 핸들러로 구현되는 서비스 액티베이터는 아웃바운드 채널 어댑터로 자주 사용된다. 특히, 데이터가 애플리케이션 자체에 전달될 필요가 있을때다.

### 🌟 엔드포인트 모듈 

스프링 통합은 우리 나름의 채널 어댑터를 생성할 수 있게 해준다. 그러나 아래 표에있는것을 포함해 다양한 외부 시스템과의

통합을 위해 채널 어댑터가 포함된 24개 이상의 엔드포인트 모듈을 스프링 통합이 제공한다.

#### 🏭 스프링 통합은 외부 시스템과의 통합을 위한 24개이상의 엔트포인트 모듈 제공

|모듈|의존성 ID|
|:--|:--:|
|AMQP|spring-integration-amqp|
|스프링 애플리케이션 이벤트|spring-integration-event|
|RSS와 Atom|spring-integration-feed|
|파일시스템|spring-integration-file|
|FTP/FTPS|spring-integration-ftp|
|GemFire|spring-integration-getmfire|
|HTTP|spring-integration-http|
|JDBC|spring-integration-jdbc|
|JPA|spring-integration-jpa|
|JMS|spring-integration-jms|
|이메일|spring-integration-mail|
|MongoDB|spring-integration-mongodb|
|MQTT|spring-integration-mqtt|
|Redis|spring-integration-redis|
|RMI|spring-integration-rmi|
|SFTP|spring-integration-sftp|
|STOMP|spring-integration-stomp|
|스트림|spring-integration-stream|
|Syslog|spring-integration-syslog|
|TCP/UDP|spring-integration-ip|
|Twitter|spring-integration-twitter|
|웹서비스|spring-integration-ws|
|WebFlux|spring-integration-webflux|
|WebSocket|spring-integration-websocket|
|XMPP|spring-integration-xmpp|
|Zookeeper|spring-integration-zookeeper|

이 표에서 알 수 있듯이, 스프링 통합은 여러 가지 통합 요구를 충족시키기 위해 광범위한 컴포넌트들을 제공한다.

각 엔드포인트 모듈은 채널 어댑터를 제공하며, 채널 얻배터는 자바 구성을 사용해 빈으로 선언되거나 자바 DSL 구성을 사용해 static 메소드로 참조할 수 있다. 


## 🍰 이메일 통합 플로우 생성하기

타코 클라우드에서 고객들이 이메일로 타코 디자인을 제출하거나 주문할수 있다. 그리고 이메일로 타코 주문을 전송하기 위해

방문하는 모든 사람들에게 전단지를 전송하고 광고를 낼것이다. 이것은 엄청난 성공이지만 그러기에는 아직 이르다.

너무 많은 이메일이 쏟아져 들어와 이메일만 읽으면서 주문시스템에 주문 명세를 제출하는 임시직운을 고용해야한다.

이제 이렇게 타코클라우드 받은 편지함의 타코 주문이메일을 지속적으로 확인하여 이메일의 주문 명세를 파싱한 후 해당 주문 데이터의

처리를 위해 타코 클라우드애 제출하는 통합 플로우를 구현할 것이다. 

```
@Data
@ConfigurationProperties(prefix="tacocloud.email")
@Component
public class EmailProperties{
    private String username;
    private String password;
    private String host;
    private String mailbox;
    private long pollRate=3000;

    public String getImapUrl(){
        return String.format("imaps://%s:%s@%s/%s",
            this.username,this.password,this.host,this.mailbox);
    }
}
```

EmailProperties 클래스에는 tacocloud.email로 설정된 prefix속성을 갖는 `@ConfigurationProperties`

애노테이션이 지정되었다. 따라서 이메일을 읽는데 필요한 명세를 다음과 같이 application.yml파일에 구성할 수 있다.
```
tacocloud:
  email:
    host: imap.tacocloud.com
    mailbox: INBOX
    username: taco-in-flow
    password: 1L0v3T4c0s
    poll-rate: 10000
```

이제는 EmailProperties를 사용해서 통합플로우를 구성할것이다. 우리가 생성할 플로우는 다음과같다.

![image](https://user-images.githubusercontent.com/40031858/108598489-39cc9500-73d1-11eb-9936-e14e433876e1.png)

이 플로우를 정의할 때 다음 두가지 중 하나를 선택할 수 있다.

#### [1] 플로우를 타코 클라우드 애플리케이션 자체에 정의한다
: 이경우 타코 주문 데이터를 생성하기 위해 정의했던 레포지토리들을 플로우의 끝에서 서비스 액티베이터가 호출할 것이다.

#### [2] 플로우를 별도의 애플리케이션으로 정의한다
: 이경우 서비스 액티베이터가 타코클라우드 API에 Post요청을 전송하여 타코 주문 데이터를 제출할것이다.

---

서비스 액티베이터가 구현되는 방법 외에는 어느 것을 선택하든 플로우 자체는 무관하다. 

그러나 메인 타코 클라우드 애플리케이션에 이미 정의된 것과 약간 다른타코, 주문, 식자재를 나타내는 타입들이

필요하므로 기존 도메인 타입과의 혼선을 피하기 위해 별도 애플리케이션에 통합플로우를 정의하는 방향으로하자

### ⭐ 이메일을 받아 주문으로 제출하기 위해 통합 플로우 정의하기
```
@Configuration
public class TacoOrderEmailIntegrationConfig{
    @Bean
    public IntegrationFlow tacoOrderEmailFlow(
        EmailProperties emailProps,
        EmailToOrderTransformer emailToOrderTransformer,
        OrderSubmitMessageHandler orderSubmitHandler){
            return IntegrationFlows
                .from(Mail.imapInboundAdapter(emailProps.getImapUrl()),
                    e-> e.poller(
                        Pollers.fixedDelay(emailProps.getPollRate())))
                .transform(emailToOrderTransformer)
                .handle(orderSubmitHandler)
                .get();
        }
}
```

tacoOrderEmailFlow() 메소드에 정의된 타코 주문 이메일 플로우는 3개의 서로 다른 컴포넌트로 구성된다

### IMAP이메일 인바운드 채널 어댑터
: 이 채널 어댑터는 EmailProperties의 getImapUrl()메소드로부터 생성된 IMP URL로 생성되며, 

EmailProperties의 pollRate속성에 설정된 지연시간이 될 때마다 이메일을 확인한다. 받은 이메일은 변환기에 연결하는 채널로 전달된다.

### 이메일을 Order객체로 변환하는 변환기
: 이변환기는 tacoOrderEmailFlow() 메소드로 주입되는 EmailToOrderTransformer에 구현된다.

변환된 주문 데이터(Order객체)는 다른 채널을 통해 최종 컴포넌트로 전달된다

### 핸들러(아웃바운드 채널 어댑터로 작동)
: 핸들러는 Order객체를 받아서 타코 클라우드의 REST API로 제출한다

---

Mail.imapInboundAdapter() 호출을 가능하게 하려면 Email 엔드포인트 모듈의 의존성을 추가해야한다
```
<dependency>
    <groupId>org.springframework.integration</groupId>
    <artifactId>spring-integration-file</artifactId>
</dependency>
```

AbstractMailMessageTransformer의 서브클래스인 EmailToOrderTransformer 클래스는 스프링 통합의 Transformer 인터페이스를구현한것이다.

AbstractMailMessageTransformer가 이미 Transformer 인터페이스를 구현하고 있기 때문이다.

### 통합 변환기를 사용해서 입력 이메일을 타코 주문(Order객체)로 변환하기
```
@Component
public class EmailToOrderTransformer extends AbstractMailMessageTransformer<Order>{
    @Override
    protected AbstractIntegrationMessageBuilder<Order> doTransform(Message mailMessage) throws Exception{
        Order tacoOrder=processPayload(mailMessage);
        return MessageBuilder.withPayload(tacoOrder);
    }
    ...
}
```

AbstractMailMessageTransformer는 페이로드가 이메일인 메시지를 처리하는 데 편리한 베이스 클래스다.

입력 메시지로부터 이메일 정보를 Message객체(doTransform()메소드의 인자로 전달)로 추출하는 일을 지원한다.

doTransform()메소드에서는 Message객체를 private 메소드인 processPayload()의 인자로 전달하여 이메일을 Order객체로 파싱한다.
```
@Data
public class Order{
    private final String email;
    private List<Taco> tacos=new ArrayList<>();

    public void addTaco(Taco taco){
        this.tacos.add(taco);
    }
}
```

### 메시지 핸들러를 통해서 타코 클라우드 API에 주문을 POST하기
```
@Component
public class OrderSubmitMessageHandler implements GenericHandler<Order>{
    private RestTemplate rest;
    private ApiProperties apiProps;

    public OrderSubmitMessageHandler(
        ApiProperties apiProps, RestTemplate rest){
            this.apiProps=apiProps;
            this.rest=rest;
    }

    @Override
    public Object handle(Order order, Map<String, Object> headers){
        rest.postForObject(apiProps.getUrl(),order,String.class);
        return null;
    }
}
```

GenericHandler 인터페이스의 요구사항을 충족하기 위해 OrderSubmitMessageHandler는 handle()메소드를 오버라이딩한다.

이 메소드는 입력된 Order 객체를 받으며, 주입된 RestTemplate을 사용해서 주문을 제출한다.

끝으로, 이 핸들러가 플로우의 제일 끝이라는 것을 나타내기 위해 handle()메소드가 null을반환한다.

ApiProperties는 URL의 하드코딩을 피하기 위해 postForObject()호출에 사용되었으며, 이것은 다음과 같은 구성속성파일이다.

```
@Data
@ConfigurationProperties(prefix="tacocloud.api")
@Component
public class ApiProperties{
    private String url;
}
```
그리고 application.yml에는 타코클라우드 API의 URL을 다음과 같이 구성할 수 있다.
```
tacocloud:
  api:
    url: http://api.tacocloud.com
```

RestTemplate이 OrderSubmitMessageHandler에 주입되어 프로젝트에서 사용될 수 있게하려면 또 다음 의존성을 추가해야한다
```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

이렇게하면 RestTemplate을 classpath에서 사용할 수 있으며, 또한 스프링 MVC의 자동-구성도 수행된다.

독립 실행형의 스프링 통합 플로우의 경우는 애플리케이션에서 스프링 MVC또는 자동-구성이 제공하는 내장된 톰캣조차 필요없다

따라서 application.yml에서 스프링 MVC자동구성을 비활성화한다.
```
spring:
  main:
    web-application-type: none
```
spring.main.web-application-type 속성은 servlet, reactive, none중 하나로 설정할 수 있다.

스프링 MVC가 classpath에 있을때는 이 속성값을 자동구성이 servlet으로 설정한다 하지만 여기서는 mvc와 톰캣이 자동구성되지않게 설정했다.

# 스프링 부트 최신버전 사용시 참고
```
// 스프링부트 구 버전 코드
@Component
public class OrderSubmitMessageHandler
       implements GenericHandler<Order> {

  private RestTemplate rest;
  private ApiProperties apiProps;

  public OrderSubmitMessageHandler(ApiProperties apiProps, RestTemplate rest) {
    this.apiProps = apiProps;
    this.rest = rest;
  }

  @Override
  public Object handle(Order order, Map<String, Object> headers) {
    rest.postForObject(apiProps.getUrl(), order, String.class);
    return null;
  }
}

//스프링부트 최신 버전코드 (현재 2.4.2)
@Component
public class OrderSubmitMessageHandler
       implements GenericHandler<Order> {

  private RestTemplate rest;
  private ApiProperties apiProps;

  public OrderSubmitMessageHandler(ApiProperties apiProps, RestTemplate rest) {
    this.apiProps = apiProps;
    this.rest = rest;
  }
  @Override
  public Object handle(Order order, MessageHeaders messageHeaders) {

    rest.postForObject(apiProps.getUrl(),order,String.class);
    return null;
  }
}

```

# ⚡ 9장 요약
#### 🌟 [1] 스프링 통합은 플로우를 정의할 수 있게 해준다. 데이터는 애플리케이션으로 들어오거나 나갈때 플로우를 통해 처리할수있다.

#### 🌟 [2] 통합플로우는 XML,Java,Java DSL을 사용해서 정의할 수 있다

#### 🌟 [3] 메시지 게이트웨이와 채널 어댑터는 통합 플로우의 입구나 출구의 역할을 한다.

#### 🌟 [4] 메시지는 플로우 내부에서 변환, 분할, 집적, 전달될 수 있으며, 서비스 액티베이터에 의해 처리될 수 있다.
#### 🌟 [5] 메시지 채널은 통합 플로우의 컴포넌트들을 연결한다


