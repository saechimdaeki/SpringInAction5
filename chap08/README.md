# 🥇 8장 비동기 메시지 전송하기 

### 🌟이 장에서 배우는 내용
- 비동기 메시지 전송
- JMS,RabbitMQ,카프카(Kafka)를 사용해서 메시지 전송하기
- 브로커에서 메시지 가져오기
- 메시지 리스닝하기

---

#### 🏭앞서 7장에서는 REST를 사용한 `동기화` 통신을 알아보았다. 그러나 이것만이 개발자가 사용할 수 있는 

#### 🏭애플리케이션 간의 통신 형태는 아니다. `비동기` 메시징은 애플리케이션 간에 응답을 기다리지 않고 간접적으로 메시지를 전송하는 방법이다

### 🌟따라서 통신하는 애플리케이션 간의 결합도를 낮추고 확장성을 높여준다

이번 장에서는 비동기 메시징을 사용해 타코클라우드 웹사이트로부터 타코 클라우드주방의 별도 애플리케이션으로 주문 데이터를 전송할 것이다.

이 경우 스프링이 제공하는 다음의 비동기 메시징을 고려할 수 있다. 바로 `JMS`, `RabbitMQ`,`AMQP`, `아파치 카프카`

그리고 기본적인 메시지 전송과 수신에 추가하여, 스프링의 메시지 기반 POJO지원에 관해 알아볼 것이다.

이것은 EJB의 MDB(message-driven bean)와 유사하게 메시지를 수신하는 방법이다.

---

## 💕JMS로 메시지 전송하기

JMS는 두 개 이상의 클라이언트 간에 메시지 통신을 위한 공통API를 정의하는 자바표준이다.

JMS가 나오기전에는 메시지 통신을 중개하는 메시지 브로커들이 나름의 API를 갖고 있어서 애플리케이션의 메시징 코드가 브로커 간에 호환될 수 없었다. 

그러나 JMS를 사용하면 이것을 준수하는 모든 구현 코드가 공통인터페이스를 통해 함께 작동할 수 있다.

스프링은 `JmsTemplate`이라는 템플릿 기반의 클래스를 통해 JMS를 지원한다. `JmsTemplate`
을 사용하면 프로듀서가 큐와 토픽에 메시지를 전송하고

컨슈머는 그 메시지들을 받을 수 있다. 또한, 스프링은 메시지 기반의 POJO도 지원한다. POJO는 큐나 토픽에 도착하는 메시지에 반응하여 비동기 방식으로 메시지를 수신하는 자바객체다.

### 🌟 JMS 설정하기

JMS를사용하려면 JMS 클라이언트를 프로젝트의 빌드에 추가해야한다. 하지만 우선 아파치 ActiveMQ또는 더 최신의 ActiveMQ Artemis중 어느 브로커를 사용할지 결정해야한다.

Artemis는 ActiveMQ를 새롭게 다시 구현한 차세대 브로커이다. 따라서 이 프로젝트에서는 Artemis를사용한다 따라서 다음의 의존성을추가하자

```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-artemis</artifactId>
</dependency>
```
어떤 브로커를 선택하든 메시지를 송수신하는 코드 작성법에는 영향을 주지않으며, 브로커에 대한 연결을 생성하기 위해 스프링을 구성하는방법만 다르다.

기본적으로 스프링은 `Artemis`브로커가 localhost의 6166포트를 리스닝하는 것으로 간주한다. 애플리케이션 개발시는 문제없지만

실무환경에서는 해당 브로커를 어떻게 사용하는지 스프링에게 알려주는 몇가지 속성을 설정해야한다.

#### 🏭다음은 Artemis 브로커의 위치와 인증 정보를 구성하는 속성은 다음과같다
|속성|설명|
|:--:|:--:|
|spring.artemis.boot|브로커의 호스트|
|spring.artemis.port|브로커의 포트|
|spring.artemis.user|브로커를 사요하기 위한 사용자(선택속성)|
|spring.artemis.password|브로커를 사용하기 위한 사용자 암호(선택속성)|

예를들어 실무 환경의 설정에 사용할 수 있는 application.yml파일의 다음항목을보자
```
spring:
  artemis:
    host:  artemis.tacocloud.com
    port:  61617
    user:  tacoweb
    password: l3tm3in
```

이것은 artemis.tacocloud.com의 6167포트를 리스닝하는 브로커에 대한 연결을 생성하기 위해 스프링을 설정한다.

또한, 이 브로커와 상호작용할 애플리케이션의 인증 정보도 같이설정한다. 인증정보는 선택적이지만 실무에선 설정하는게좋다.

만일 Artemis대신 ActiveMQ를사용한다면 ActiveMQ에 특화된 속성을사용해야한다

#### 🏭ActiveMQ 브로커의 위치와 인증 정보를 구성하는 속성
|속성|설명|
|:--:|:--:|
|spring.activemq.broker-url|브로커의 URL|
|spring.activemq.user|브로커를 사용하기 위한 사용자(선택 속성)|
|spring.activemq.password|브로커를 사용하기 위한 사용자 암호(선택속성)|
|spring.activemq.in-memory|인메모리 브로커로 시작할 것인지의 여부(기본값은 true)|

브로커의 호스트이름과 포트를 별개의 속성으로 하는 대신, ActiveMQ의 브로커 주소는 spring.active.broker-url속성 하나로 지정한다.

그리고 다음의 YAML에 지정된 것처럼 URL은 tcp://URL형태로 지정해야한다.
```
spring:
  activemq:
    broker-url:  tcp://activemq.tacocloud.com
    user:  tacoweb
    password:  l3tm31n
```

Artemis나 ActiveMQ중 어느 것을 선택하든 브로커가 로컬에서 실행되는 개발 환경에서는 앞의 속성들을 구성할 필요없다.

그러나 ActiveMQ를 사용할 때는 스프링이 인메모리 브로커로 시작하지 않도록 spring.activemq.in-memory속성을 false로 해야한다.

왜냐하면 인메모리 브로커가 유용한 것처럼 보일 수 있지만, 같은 애플리케이션에서 메시지를 쓰고 읽을때만 유용하므로 사용에 제약이 따른다.

스프링에 내장된 브로커를 사용하는 대신 Artemis나 ActiveMQ브로커를 따로 설치하려면 다음문서를보자
- Artemis:https://activemq.apache.org/artemis/docs/latest/using-server.html
- ActiveMQ:http://activemq.apache.org/getting-started.html#GettingStarted-PreInstallationRequirements

### 🌟 JmsTemplate을 사용해서 메시지 전송하기

JMS스타터 의존성(Artemis또는 ActiveMQ)이 빌드에 지정되면, 메시지를 송수신하기 위해 주입 및 사용할 수 있는 `JmsTemplate`은 스프링 부트가 자동-구성한다.

`JmsTemplate`은 스프링 JMS 통합 지원의 핵심이다. 스프링의 다른 템플릿 기반 컴포넌트와 마찬가지로 `JmsTemplate`은 JMS로 작업하는데 필요한 코드를 줄여준다.

만일 JmsTemplate이 없다면 메시지 브로커와의 연결 및 세션을 생성하는 코드는 물론이고, 메시지를 전송하는 도중 발생할 수 있는 예외를

처리하는 수많은 코드도 직접작성해야한다. `JmsTemplate`은 실제로 우리가 원하는 `메시지 전송`에만 집중할수있게해준다.

`JmsTemplate`은 다음을 비롯해서 메시지 전송에 유용한 여러 메소드를 갖고있다
```
//원시 메시지를 전송한다
void send(MessageCreator messageCreator) throws JmsException;
void send(Destination destination, MessageCreator messageCreator) throws JmsException;
void send(String destinationName,MessageCreator messageCreator) throws JmsException;

//객체로부터 변환된 메시지를 전송한다
void convertAndSend(Object message) throws JmsException;
void convertAndSend(Destionation destination, Object message) throws JmsException;
void ConvertAndSend(String destinationName, Object message) throws JmsException;

//객체로부터 반환되고 전송에 앞서 후처리되는 메시지를 전송한다
void convertAndSend(Object message, MessagePostProcessor postProcessor) throws JmsException;

void convertAndSend(Destination destination, Object message, MessagePostProecessor postProcessor) throws JmsException;

void convertAndSend(String destinationName, Object message, MessagePostProcessor postProcessor) throws JmsException;
```

이것을 보면 알 수 있듯 실제로는 `send()`와 `convertAndSend()`의 두 개 메소드만 있으며, 각 메소드는 서로 다른

매개변수를 지원하기 위해 오버로딩되어 있다.

- 제일 앞의 `send()`메소드 3개는 Message 객체를 생성하기 위해 MessageCreator를 필요로 한다.
- 중간의 `convertAndSend()` 메소드 3개는 Object 타입 객체를 인자로 받아 내부적으로 Message 타입으로 변환된다.
- 제일 끝의 `convertAndSend()`메소드 3개는 Object타입 객체를 Message타입으로 변환한다. 그러나 메시지가 전송되기전에 Message의 커스터마이징을위해 MessagePostProcessor도받는다.

---

게다가 이들 3개의 메소드 부류 각각은 3개의 오버로딩된 메소드로 구성되며, 이 메소드들은 JMS메시지의 도착지, 즉 메시지를 쓰는곳을 지정하는법이다르다.
- 첫 번째 메소드는 도착지 매개변수가 없으며, 해당 메시지를 기본 도착지로 전송한다.
- 두 번째 메소드는 해당 메시지의 도착지를 나타내는 Destination 객체를 인자로 받는다
- 세 번째 메소드는 해당 메시지의 도착지를 나타내는 문자열을 인자로 받는다

#### 🏭send()를 사용해 주문 데이터 전송하는 클래스는 다음과같다
```
@Service
public class JmsOrderMessagingService implements OrderMessagingService{
    private JmsTemplate jms;

    @Autowired
    public JmsOrderMessagingService(JmsTemplate jms){
        this.jms=jms;
    }

    @Override
    public void sendOrder(Order order){
        jms.send(new MessageCreator(){
            @Override
            public Message createMessage(Session session) throws JMSException{
                return session.createObjectMessage(order);
            }
        })
    }
}
```

sendOrder()메소드는 `MessageCreator`인터페이스를 구현한 익명의 내부 클래스를 인자로 전달하여 jms.send()를 호출한다.

그리고 익명의 내부 클래스는 createMessage()를 오버라이딩하여 전달된 Order객체로부터 새로운 메시지를 생성한다.

만약람다식을 사용한다면 다음과 같이 작성할 수 있다.
```
@Override
public void sendOrder(Order order){
    jms.send(session -> session.createObjectMessage(order));
}
```
그러나 jms.send()는 메시지의 도착지를 지정하지 않으므로 이 코드가 제대로 실행되게 하려면 기본 도착지 이름을 

spring.jms.template.default-destination속성에 지정해야한다. 예를들어, 다음과 같이 application.yml에 속성을지정할수있다.
```
spring:
  jms:
    template:
      default-destination: tacocloud.order.queue
```
이처럼 기본 도착지를 사용하는것이 가장 쉬운방법이다. 도착지 이름을 한 번만 지정하면 코드에서는 메시지가 전송되는

곳을 매번 신경쓰지않고 전송하는 것에만 집중할 수 있기 때문이다. 그러나 기본 도착지가 아닌 다른곳에 메시지를 전송해야 한다면

`send()`메소드의 매개변수로 도착지를 지정해야한다.

이렇게 하는 한 가지 방법은 `send()`의 첫 번째 매개변수로 `Destination`객체를 전달하는 것이다. 이경우 Destination 빈을 선언하고 

메시지 전송을 수행하는 빈에 주입하면 된다. 다음 빈에서는 타코 클라우드 주문 큐를 Destination빈으로 선언한다
```
@Bean
public Destination orderQueue(){
    return new ActiveMQQueue("tacocloud.order.queue");
}
```

여기서 사용된 `ActiveMQQueue`는 Artemis의 클래스이다. 이 Destination빈이 JmsOrderMessagingService에 주입되면 send()를 호출할 때 

이 빈을 사용해 메시지 도착지를 지정할 수있다.
```
private Destination orderQueue;

@Autowired
public JmsOrderMessagingService(JmsTemplate jms, Destination orderQueue){
    this.jms=jms;
    this.orederQueue=orderQueue;
}
...

@Override
public void sendOrder(Order order){
    jms.send(
        orderQueue,
        session -> session.createObjectMessage(order)
    );
}
```

이와 같이 Destination 객체를 사용해 메시지 도착지를 지정하면 도착지 이름만 지정하는 것보다 더 다양하게 도착지를 구성할 수 있다.

그러나 실제로는 도착지 이름 외에 다른것을 지정하는 일은 거의없으므로 send()의 첫 번째 인자로 Destination객체 대신 도착지 이름만 지정하는게쉽다.
```
@Override
public void sendOrder(Order order){
    jms.send(
        "tacocloud.order.queue",
        session -> session.createObjectMessage(order)
    );
}
```
send()메소드의 사용은 어렵지않지만 Message객체를 생성하는 MessageCreator를 두 번째 인자로 전달해야 하므로 코드가 조금 복잡해진다.

따라서 전송할 메시지 객체만 지정할 수 있다면 더간단해질것인데 convertAndSend()를 사용할 수 있다.

---

### 🌟 메시지 변환하고 전송하기

`JmsTemplates`의 `convertAndSend()`메소드는 `MessageCreator`를 제공하지 않아도 되므로 메시지 전송이 간단하다.

즉, 전송될 객체를 convertAndSend()의 인자로 직접 전달하면 해당 객체가 Message객체로 변환되어 전송된다.

예를들어 다음 코드는 주문객체(Order)를 지정된 도착지로 전송한다
```
@Override
public void sendOrder(Order order){
    jms.convertAndSend("tacocloud.order.queue",order);
}
```

send()메소드처럼 convertAndSend()는 Destination객체나 문자열 값으로 지정한 도착지를 인자로 받는다. 또는 도착지를

생략하여 기본 도착지로 메시지를 전송할 수 있다. 어떤 형태의 convertAndSend()를 사용하든 인자로 전달되는 Order객체는

Message객체로 변환된 후 전송된다. 이렇게 Message객체로 변환하는 번거로운 일은 MessageConverter를 구현하여 처리할 수 있다.

---

### 🌟 메시지 변환기 구현하기

`MessageConvertor`는 스프링에 정의된 인터페이스이며 두 개의 메소드만 정의되어 있다
```
public interface MessageConverter{
    Message toMessage(Object object, Session session) throws JMSException, MessageConversionException;

    Object fromMessage(Message message)
}
```

이렇게 있지만 구현하지않아도 된다. 스프링이 편리하게 구현해 주었기때문이다

### 🌟 공통적인 변환 작업을 해주는 스프링 메시지 변환기
|메시지 변환기|하는 일|
|:--:|:--:|
|MappingJackson2MessageConverter|Jackson 2 JSON 라이브러리를 사용해서 메시지를 JSON으로 상호변환한다|
|MarshallingMessageConverter|JAXB를 사용해서 메시지를 XML로 상호변환한다|
|MessagingMessageConverter|수신된 메시지의 MessageConverter를 사용해서 해당 메시지를 Message객체로 상호 변환한다. 또는 JMS헤더와 연관된 JmsHeaderMapper를 표준 메시지 헤더로 상호변환한다|
|SimpleMessageConverter|문자열을 TextMessage로 byte배열을 ByteMessage로 Map을 MapMessage로, Serializable객체를 ObjectMessage로 상호변환한다|

기본적으로는 `SimpleMessageConverter`가 사용되며, 이경우 전송될 객체가 Serializable 인터페이스를 구현하는 것이어야 한다.

이 메시지 변환기를 사용하는것이 좋지만 Serializable인터페이스를 구현해야한다는 제약을 피하기 위헤 MappingJackson2MessageConverter

와 같은 다른 메시지 변환기를 사용할 수도 있다. 다른 메시지 변환기를 적용할 때는 해당 변환기의 인스턴스를 빈으로 선언만 하면된다.

```
@Bean
public MappingJackson2MessageConverter messageConverter(){
    MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();

    messageConverter.setTypeIdPropertyName("_typeId");
    return messageConverter;
}
```

이 경우 MappingJackson2MessageConverter의 setTypeIdPropertyName()메소드를 호출한 후 이 메시지 변환기 인스턴스를 반환한다는 것에 유의하자.

수신된 메시지의 변환 타입을 메시지 수신자가 알아야 하기 때문에 이 부분이 매우 중요하다. 여기에는 변환되는 타입의 클래스이름이 포함된다.

그러나 이것은 유연성이 떨어지기에 유연성을 높이기위해 setTypeIdMappings()를 호출하여 실제 타입에 임의의 이름을 매핑시킬 수 있다.

```
@Bean
public MappingJackson2MessageConverter messageConverter(){
     MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();

    messageConverter.setTypeIdPropertyName("_typeId");
    
    Map<String,Class<?>> typeIdMappings=new HashMap<String,Class<?>>();
    typeIdMappings.put("order",Order.class);
    messageConverter.setTypeIdMappings(typeIdMappings);

    return messageConverter;
}
```

이 경우 해당 메시지의 _typeId속성에 전송되는 클래스 이름 대신 order값이 전송된다.

### 🌟 후처리 메시지 

수익성이 좋은 웹 비즈니스에 추가해 타코 채인점을 개설했다고 가정하자. 각 음식점은 주문 이행센터 즉, 웹에서 온라인 주문을 받아 타코를 조리하고 포장, 배달하는

음식점이 될수도있으므로 주문소스 정보를 주방으로 전송하는 방법이 필요하며 주방직원은 오프라인 가게주문과 다른 주문절차를 사용할것이다.

이 경우 온라인 주문을 나타내는 WEB또는 오프라인 가게주문을 나타내는 STORE를 값으로 갖는 새로운 source속성을 order객체에 추가하는것이좋을것이다

그러나 웹 사이트의 Order클래스와 주방애플리케이션 Order 클래스를 모두 변경해야하고 이정보는 오직 타코 준비에만 필요하다.

따라서 이때는 주문 소스 정보를 전달하기 위해 커스텀 헤더를 메시지에 추가하는것이 가장 쉬운 방법이다 만일 `send()`를 사용해 주문한다면 `setStringProperty()`를 사용

```
jms.send("tacocloud.order.queue",
    session -> {
        Message message=session.createObjectMessage(order);
        message.setStringProperty("X_ORDER_SOURCE","WEB");
    }
);

```
그러나 send()가 아닌 convertAndSend()를 사용하면 Message객체가 내부적으로 생성되므로 접근할수없다.

하지만 내부적으로 생성된 Message객체를 전송전에 변경할 수 있는 방법이 있는데 convertAndSend()의 마지막 인자로 MessagePostProcessor를

전달하면 Message객체가 생성된 후 이 객체에 우리가 필요한 처리를 할 수 있다.

```
convertAndSend()를 사용하지만 MessagePostProcessor를사용해 X_ORDER_SOURCE헤더를 추가하는 방법

jms.convertAndSend("tacocloud.order.queue", order, new MessagePostProcessor(){
    @Override
    public Message postProcessMessage(Message message) throws JMSException{
        message.setStringProperty("X_ORDER_SOURCE","WEB");
        return message;
    }
});
```

여기서 MessagePostProcessor는 함수형인터페이스이며 람다로 다음과같이 변경할 수 있다.
```
jms.convertAndSend("tacocloud.order.queue", order,
    message->{
        message.setStringProperty("X_ORDER_SOURCE","WEB");
        return message;
    });
```

여기서 구현된 MessagePostProcessor는 이코드의 convertAndSend()에서만 사용될 수 있는데 이처럼 리팩토링또한 가능하다
```
@GetMapping("/convertAndSend/order")
public String convertAndSendOrder(){
    Order order=buildOrder();
    jms.convertAndSend("tacocloud.order.queue",order,
    this::addOrderSource);

    return "Convert and sent order";
}

private Message addOrderSource(Message message) throws JMSException{
    message.setStringProperty("X_ORDER_SOURCE","WEB");
    return message;
}

```

### 🌟 JMS 메시지 수신하기 

메시지를 수신하는 방씩에는 두 가지가 있다. 코드에서 메시지를 요청하고 도착할 때까지 기다리는 `풀 모델`과 메시지가 수신가능하게 되면

우리코드로 자동전달하는 `푸시 모델`이다. `JmsTemplate`은 메시지를 수신하는 여러개의 메소드를 제공하지만, 모든 메소드가 풀모델을 사용한다.

따라서 이 메소드 중 하나를 호출하여 메시지를 요청하면 스레드에서 메시지를 수신할 수 있을때까지 기다린다.

이와는 달리 푸시모델을 사용할 수도 있으며, 이때는 언제든 메시지가 수신 가능할 때 자동 호출되는 메시지 리스너를 정의한다.

두 가지 방식 모두 용도에 맞게 사용할 수 있다 그러나 스레드의 실행을 막지 않으므로 일반적으로 푸시모델이 좋은선택이다.

단, 많은메시지가 너무 빨리도착한다면 리스너에 과부하가 걸리는 경우가생길수있다.

#### 🏭`JmsTemplate`을 사용해서 메시지 수신하기

다음의 메소드를 포함해서 JmsTemplate은 브로커로부터 메시지를 가져오는 여러개의 메소드를 제공한다.

```
Message receive() throws JmsException;
Message receive(Destination destination) throws JmsException;
Message receive(String destinationName) throws JmsException;

Object receiveAndConvert() throws JmsException;
Object receiveAndConvert(Destination destination) throws JmsException;
Object receiveAndConvert(String destinationName) throws JmsException;
```
이것을 보면 알 수 있듯 이 메소드들은 메시지를 전송하는 JmsTemplate의 send()와 convertAndSend()메소드에 대응한다.

receive()메소드는 원시 메시지를 수신하는 반면, receiveAndConvert()메소드는 메시지를 도메인 타입으로 변환하기 위해 구성된 메시지 변환기를 사용한다.

그리고 각 메소드에서 도착지 이름을 갖는 Destination객체나 문자열을 지정하거나 기본 도착지를 사용할 수 있다.

### 🌟큐에서 주문 데이터 가져오기
```
@Component
public class JmsOrderReceiver implements OrderReceiver{
    private JmsTemplate jms;
    private MessageConverter converter;

    @Autowired
    public JmsOrderService(JmsTemplate jms, MessageConverter converter){
        this.jms=jms;
        this.converter=converter;
    }

    public Order receiveOrder(){
        Message message=jns.receive("tacocloud.order.queue)";
        return (Order) converter.froMessage(message);
    }
}

```

여기서는 주문 데이터를 가져올 도착지를 문자열(String)로 지정했다 . receive()메소드는 변환되지 않은 메시지를 반환한다.

그러나 여기서 필요한것은 메시지 내부의 Order객체이기 떄문에 주입된 메시지 변환기를 사용해 receive()

메소드가 반환한 수신 메시지를 Order객체로 변환한다. 수신 메시지의 타입 Id속성은 해당 메시지를

Order객체로 변환하라고알려주지만 변환된 객체의 타입은 Object이므로 캐스팅후 반환해야한다.

메시지의 속성과 헤더를 살펴봐야 할 때는 원시 `Message`객체를 메시지로 수신하는 것이 유용할수있다. 그러나 메시지의 메타데이터는

필요없고 페이로드만 필요할 때가 있다. 이경우 두단계의 절차로 페이로드를 도메인 타입으로 변환하며, 메시지 변환기가 해당

컴포넌트에 주입되어야한다. 메시지의 페이로드만 필요할때는 receiveAndConvert()를 사용하는 것이 더 간단하다.

### 🌟변환된 Order객체 수신하기
```
@Component
public class JmsOrderReceiver implements OrderReceiver{
    private JmsTemplate jms;

    @Autowired
    public JmsOrderReceiver(JmsTemplate jms){
        this.jms=jms;
    }
    public Order receiveOrder(){
        return (Order) jms.receiveAndConvert("tacocloud.order.queue");
    }
}
```
변경된 JmsOrderReceiver에서는 receiveOrder()메소드의 코드가 단 한줄이며 더이상 MessageConverter를 주입할 필요강벗다.

모든 메시지 변환은 내부적으로 receiveAndConvert()에서 수행되기 떄문이다.

타코클라우드 주방 애플리케이션에서 receiveOrder()를 어떻게 사용할수있을까? 타코 클라우드 주방들중 하나에서 일하는

음식 조리사는 타코를 만들 준비가 되었다는 것을 나타내기 위해 버튼을 누르거나 다른액션을 취할수있다.

그러면 receiveOrder()가 호출되어 receive()나 receiveAndConvert()가 수행될 것이며, 주문 메시지가 수신될 때까지는 아무일도 생기지않는다.

그리고 주문 메시지가 수신되면 receiveOrder()로부터 반환되고 이 정보는 조리사가 일을 하도록 주문 명세를 보여주는데 사용된다.

### 🌟 메시지 리스너 선언하기

`receive()`나 `receiveAndConvert()`를 호출해야 하는 풀 모델과 달리, 메시지 리스너는 메시지가 도착할 때까지 대기하는 수동적 컴포넌트다.

JMS메시지에 반응하는 메시지 리스너를 생성하려면 컴포넌트의 메소드에 `@JmsListener`를 지정해야한다.

#### 🏭주문 데이터를 리스닝하는 OrderListener컴포넌트

```
@Component
public class OrderListener{
    private KitchenUI ui;

    @Autowired
    public OrderListener(KitchenUI ui){
        this.ui=ui;
    }

    @JmsListener(destination = "tacocloud.order.queue")
    public void receiveOrder(Order order){
        ui.displayOrder(order);
    }
}
```

receiveOrder()메소드에는 tacocloud.order.queue도착지의 메시지를 `리스닝`하기 위해 `@JmsListener`애노테이션이 지정되었다 이 메소드는 JmsTemplate을 사용히지 않으먀ㅕ, 우리

애플리케이션 코드에서도 호출되지 않는다. 대신에 스프링의 프레임워크 코드가 특정 도착지에 메시지가 도착하는 것을 기다리다가

도착하면 해당 메시지에 적재된 Order객체가 인자로 전달됨녀서 receiveOrder()메소드가 자동으로 호출된다.

여러면에서 `@JmsListener`애노테이션은 스프링 MVC의 요청매핑 애노테이션과 유사한데 `@JmsListener`가 지정된 메소드들은 지정된 도착지에 들어오는 메시지에 반응한다

메시지 리스너는 중단없이 다수의 메시지를 빠르게 처리할 수 있어서 좋은선택이 될 때가 있다. 그러나 타코 클라우드애플리케이션의 경우

주방의 음식조리사가 주문이 들어오는 만큼 빠르게 타코를 준비할 수 없어서 병목현생이 생길수있다. 그래서 직원에게 과부하가 걸리지 않도록

주방의 사용자 인터페이스는 도착하는 주문을 버퍼링해야한다. 메시지리스너는 나쁘다는게 아니라 메시지가 빠르게 처리될 수 있을때는 딱 맞는다.

그러나 메시지 처리기가 자신의 시간에 맞춰 더많은 메시지를 요청할 수 있어야 한다면 `JmsTemplate`이 제공하는 풀 모델이 더적합할것이다.

`JMS`는 표준 자바 명세에 정의되어 있고 여러 브로커에서 지원되므로 자바의 메시징에 많이 사용된다. 그러나 JMS는 몇가지 단점이 있으며

그 중 가장 중요한것은 JMS가 자바 애플리케이션에서만 사용할 수 있다는것이다. `RabbitMQ`와 카프카 같은 새로운 메시징 시스템은

이런 단점을 해결해 다른 언어와 JVM외의 다른 플랫폼에서 사용할 수 있다.


## 💕RabbitMQ와 AMQP사용하기

AMQP의 가장 중요한 구현이라 할 수 있는 `RqbbitMQ`는 JMS보다 더 진보된 메시지 라우팅 전략을 제공한다.

JMS메시지가 수신자가 가져갈 메시지 도착지의 이름을 주소로 사용하는 반면, AMQP메시지는 수신자가 리스닝하는 큐와 분리된

거래소 이름과 라우팅 키를 주소로한다. 거래소와 큐간의 관계는 다음과같다.

### 🌟RabbitMQ거래소로 전송되는 메시지는 라우팅키와 바인딩을 기반으로 하나이상의 큐로 전달된다.

![image](https://user-images.githubusercontent.com/40031858/108445740-41405100-72a0-11eb-8099-1ec24341fb05.png)

RabbitMQ 거래소로 전송되는 메시지는 라우팅 키와 바인딩을 기반으로 하나 이상의 큐로 전달된다

메시지가 RabbitMQ 브로커에 도착하면 주소로 지정된 거래소에 들어간다. 거래소는 하나 이상의 큐에 메시지를 전달할 책임이 있다.

이때 거래소 타입, 거래소와 큐 간의 바인딩, 메시지의 라우팅 키값을 기반으로 처리한다.

### 🌟다음을 포함해 여러 종류의 거래소가 있다
#### 🏭 기본(default): 브로커가 자동으로 생성하는 특별한 거래소. 해당 메시지의 라우팅 키와 이름이 같은 큐로 메시지를 전달한다.

#### 모든 큐는 자동으로 기본 거래소와 연결된다.

#### 🏭 다이렉트(direct): 바인딩 키가 해당 메시지의 라우팅 키와 같은 큐에 메시지를 전달한다.

#### 🏭 토픽(Topic): 바인딩 키가 해당 메시지의 라우팅 키와 일치하는 하나 이상의 큐에 메시지를 전달한다.

#### 🏭 팬아웃(Fanout): 바인딩 키나 라우팅 키에 상관없이 모든 연결된 큐에 메시지를 전달한다.

#### 🏭 헤더(Header): 토픽 거래소와 유사하며, 라우팅 키 대신 메시지 헤더값을 기반으로 한다는것만 다르다.

#### 🏭 데드레터(Dead letter):전달 불가능한 즉, 정의된 어떤 거래소-큐 바인딩과도 일치하지 않는 모든메시지를 보관하는 잡동사니거래소

거래소의 가장 간단한 형태는 기본거래소와 팬아웃거래소이며 이것들은 JMS의 큐 및 토픽과 거의 일치한다.

그러나 다른 거래소들을 사용하면 더 유연한 라우팅 스킴을 정의할 수 있다. 메시지는 라우팅 키를 갖고 거래소로 전달되고

큐에서 읽혀져 소비된다는 것을 이해하는것이 가장 중요하다. 메시지는 바인딩 정의를 기반으로 거래소로부터 큐로 전달된다.

---

이제 스프링을 사용해 RabbitMQ 메시지를 전송 및 수신하려면 Artemis나 ActiveMQ대신에 스프링 부트의 AMQP스타터 의존성을 빌드에추가한다
```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```
이처럼 AMQP 스타터를 빌드에 추가하면 다른 지원 컴포넌트는 물론이고 AMQP 연결 팩토리와 RabbitTemplate 빈을 생성하는 자동구성이 수행된다.


### 🌟RabbitMQ 브로커의 위치와 인증 정보를 구성하는 속성

|속성|설명|
|:--|:--:|
|spring.rabbitmq.addresses|쉼표로 구분된 리스트 형태의 RabbitMQ 브로커 주소|
|spring.rabbitmq.host|브로커의 호스트(기본값은 localhost)|
|spring.rabbitmq.port|브로커의 포트(기본값은 5672)|
|spring.rabbitmq.username|브로커를 사용하기 위한 사용자 이름(선택속성)|
|spring.rabbitmq.password|브로커를 사용하기 위한 사용자 암호(선택속성)|

개발 목적이라면 RabbitMQ 브로커가 로컬컴퓨터에서 실행되고 5672포트를 리스닝할 것이며, 인증정보가 필요 없을것이다. 

따라서 이 속성들은 개발 시에는 많이 사용하지 않는다 그러나 애플리케이션을 실무환경으로 이양할때는 유용하다.

예를들어 실무환경으로 이양할때 RabbitMQ브로커가 rabbit.tacocloud.com이라는 서버에서 실행되고 5673 포트를 리스닝하며 인증정보가필요하다면
```
spring:
  profiles: prod
  rabbitmq:
    host: rabbit.tacocloud.com
    port: 5673
    useername: tacoweb
    password: l3tm31n
```

## 💕RabbitTemplate을 사용해서 메시지 전송하기 

`RabbitMQ`메시징을 위한 스프링 지원의 핵심은 `RabbitTemplate`이다. RabbitTemplate은 JmsTemplate과 유사한 메소드들을 제공한다. 그러나 RabbitMQ특유의 작동 방법에따른 미세한 차이가있다.

`RabbitTemplate`을 사용한 메시지 전송의 경우에 send()와 convertAndSend()메소드는 같은이름의 `JmsTemplate`메소드와 유사하다.

그러나 지정된 큐나 토픽에만 메시지를 전송했던 `JmsTemplate`메소드와 달리 `RabbitTemplate`메소드는 거래소와 라우팅 키의 형태로 메시지를 전송한다.

`RabbitTemplate`을 사용한 메시지 전송에 가장 유용한 메소드를 보면 다음과 같다.
```
//원시 메시지를 전송한다
void send(Message message) throws AmqpException;
void send(String roujtingKey, Message message) throws AmqpException;
void send(String exchange, String routingKey, Message message) throws AmqpException;

//객체로부터 변환된 메시지를 전송한다.
void convertAndSend(Object message) throws AmqpException;
void convertAndSend(String routingKey, Object message) throws AmqpException;

void convertAndSend(String exchange, String routingKey, Object message) throws AmqpException;

//객체로부터 변환되고 후처리(post-processing)되는 메시지를 전송한다
void convertAndSend(Object message, MessagePostProcessor mPP) throws AmqpExceptionl

void convertAndSend(String routingKey, Object message, MessagePostProcessor messagePostProcesser) throws AmqpException;

void convertAndSend(String exchange, String routingKey, Object message,
    MessagePostProcessor messagePostProcessor) throws AmqpException;
```
이 메소드들은 JmsTemplate의 대응되는 메소드와 유사한 패턴을 따른다. 제일앞의 `send()`메소드 3개는 모두 원시 `Message`객체를 전송한다.

그 다음 3개의 `convertAndSend()` 메소드는 전송에 앞서 내부적으로 메시지로 변환될 객체를 인자로 받는다. 

마지막 3개의 `convertAndSend()` 메소드는 바로 앞의 3개와 거의 같지만, 브로커에게 전송되기 전에 Message 객체를 조작하는데 사용

될 수 있는 `MessagePostProcessor`인자를 받는다.

이 메소드들은 도착지이름(또는 Destination객체) 대신, 거래소와 라우팅 키를 지정하는 문자열 값을 인자로 받는다는 점에서

JmsTemplate의 대응되는 메소드들과 다르다. 거래소를 인자로 받지 않는 메소드들은 기본 거래소로 메시지를 전송한다. 마찬가지로

라우팅 키를 인자로 받지 않는 메소드들은 기본 라우팅 키로 전송되는 메시지를 갖는다.

이제 `RabbitTemplate`을 사용해 타코주문데이터를 전송해보자 첫번째 방법은 send()메소드를 사용하는 것이다.

그러나 Order객체를 Message객체로 변환후 send()를 호출해야한다. 만일 메시지 변환기로 사용할 수 있는 getMessageConverter()메소드가 RabbitTemplate에 없었다면 힘들었을것이다.

### 🌟RabbitTemplate.send()로 메시지 전송하기

```
@Service
public class RabbitOrderMessagingService implements OrderMessagingService{
    private RabbitTemplate rabbit;

    @Autowired
    public RabbitOrderMessagingService(RabbitTemplate rabbit){
        this.rabbit=rabbit;
    }

    public void sendOrder(Order order){
        MessageConverter converter= rabbit.getMessageConverter();
        MessageProperties props=new MessageProperties();
        Message message= converter.toMessage(order,props);
        rabbit.send("tacocloud.order", message);
    }
}
```

이처럼 MessageConverter가 있으면 Order객체를 Message객체로 변환하기 쉽다. 메시지 속성은 MessageProperties를

사용해서 제공해야 한다. 그러나 메시지 속성을 설정할 필요가 없다면 MessageProperties의 기본 인스턴스면 족하다.

그리고 모든 준비가 완료되면 send()를 호출할 수 있다. 이때 메시지와 함께 거래소 및 라우팅 키를 인자로 전달한다.

위의 예에서는 tacocloud.order만 인자로 전달하므로 기본 거래소가 사용된다. 기본 거래소의 이름은 빈 문자열인 ""이며,

이것은 RabbitMQ 브로커가 자동으로 생성하는 기본 거래소와 일치한다. 이와 동일하게 기본 라우팅 키도 ""이다.

이런 기본값은 spring.rabbitmq.template.exchange와 spring.rabbitmq.template.routing-key 속성을 설정해 변경할수있다.
```
spring:
  rabbitmq:
    template:
      exchange: tacocloud.orders
      routing-key: kitchens.central
```

이 경우 거래소를 지정하지 않은 모든 메시지는 이름이 tacocloud.orders인 거래소로 자동 전송된다.

만일 send()나 convertAndSend()를 호출할때 라우팅 키도 지정되지 않으면 해당 메시지는 kitchens.central을 라우팅키로갖는다.

메시지 변환기로 Message 객체를 생성하는 것은 매우 쉽다. 그러나 모든 변환 작업을 RabbitTemplate이 처리하도록 convertAndSend()를 사용하면 훨씬쉽다,
```
public void sendOrder(Order order){
    rabbit.convertAndSend("tacocloud.order",order);
}
```

### 🌟메시지 변환기 구성하기

기본적으로 메시지 변환은 `SimpleMessageConverter`로 수행되며, 이것은 String과 같은 간단한 타입과 `Serializable`객체를 `Message`객체로

변환할 수 있다. 그러나 스프링은 다음을 포함해서 RabbitTemplate에 사용할 수 있는 여러개의 메시지 변환기를 제공한다.

- Jackson2JsonMessageConverter: Jackson2JSONProcessor를 사용해서 객체를 JSON으로 상호 변환한다
- MarshallingMessageConverter: 스프링 Marshaller와 Unmarshaller를 사용해서 변환한다
- SerializerMessageConverter: 스프링의 Serializer와 Deserializer를 사용해서 String과 객체를 변환한다
- SimpleMessageConverter: String, byte배열,Serializable타입을 변환한다
- ContentTypeDelegatingMessageConverter: contentType 헤더를 기반으로 다른 메시지 변환기에 변환을 위임한다.

메시지 변환기를 변경할 때는 `MessageConverter`타입의 빈을 구성하면 된다. 예를들어, JSON기반 메시지 변환의 경우는 다음과같다.

```
@Bean
public MessageConverter messageConverter(){
    return new Jackson2JsonMessageConverter();
}
```
이렇게 하면 스프링 부트 자동구성에서 이 빈을 찾아서 기본 메시지 변환기 대신 이 빈을 RabbitTemplate으로 주입한다

### 🌟메시지 속성 설정하기

JMS에서처럼 전송하는 메시지의 일부 헤더를 설정해야 할 경우가 있다. 이때는 Message객체를 생성할때 메시지 변환기에 제공하는 MessageProperties인스턴스를 통해 설정할수있다.

## 💕RabbitMQ로부터 메시지 수신하기 

JMS에서처럼 RabbitMQ의 경우도 다음 두가질르 선택할 수 있다.
- RabbitTemplate을 사용해서 큐로부터 메시지를 가져온다
- @RabbitListener가 지정된 메소드로 메시지가 푸시된다.

우선 큐로부터 메시지를 가져오는 풀 모델 기반의 RabbitTemplate.receive()메소드부터 알아보자

### 🌟 RabbitTemplate을 사용해서 메시지 수신하기

RabbitTemplate은 큐로부터 메시지를 가져오는 여러 메소드를 제공하며, 가장 유용한것을 보면 다음과 같다
```
//메시지를 수신한다
Message receive() throws AmqpException;
Message receive(String queueName) throws AmqpException;
Message receive(long timeoutMillis) throws AmqpException;
Message receive(String queueName, long timeoutMillis) throws AmqpException;

//메시지로부터 변환된 객체를 수신한다
Object receiveAndConvert() throws AmqpException;
Object receiveAndConvert(String queueName) throws AmqpException;
Object receiveAndConvert(long timeoutMillis) throws AmqpException;
Object receiveAndConvert(String queueName, long timeoutMillis) throws AmqpException;

//메시지로부터 변환된 타입-안전(type-safe)객체를 수신한다
<T> T receiveAndConvert(ParameterizedTypeReference<T> type) throws AmqpException;

<T> T receiveAndConvert(String queueName, ParameterizedTypeReference<T> type) throws AmqpException;

<T> T receiveAndConvert(long timoutMillis, ParameterizedTypeRefercence<T> type) throws AmqpException;

<T> T receiveAndConvert(String queueName, long timeoutMillis, ParameterizedTypeReference<T> type) throws AmqpException;
```

이 메소드들은 앞에서 설명했던 send() 및 convertAndSend() 메소드들과 대칭된다. 즉, send()가 원시 Message 객체를 전송하는데 사용된 반면,

receive()는 큐로부터 원시 Message객체를 수신한다. 마찬가지로 receiveAndConvert()는 메시지를 수신한 후 메시지 변환기를 사용해 수신메시지를 도메인객체로 변환하고 반환한다.

그러나 메소드 시그니처 특히 매개변수에서 분명한 차이가 있다. 우선, 수신 메소드의 어느 것도 거래소나 라우팅 키를 매개변수로 갖지않는다.

왜냐하면 거래소와 라우팅 키는 메시지를 큐로 전달하는데 사용되지만, 일단 메시지가 큐에 들어가면 다음 메시지 도착지는

큐로부터 메시지를 소비하는 컨슈머이기 때문이다. 따라서 메시지를 소비하는 애플리케이션은 거래소 및 라우팅 키를 신경 쓸 필요없고 큐만알면된다.

---

### 🌟 RabbitTemplate을 사용해서 RabbitMQ로부터 주문 데이터 가져오기
```
@Component
public class RabbitOrderReceiver{
    private RabbitTemplate rabbit;
    private MessageConverter converter;

    @Autowired
    public RabbitOrderReceiver(RabbitTemplate rabbit){
        this.rabbit=rabbit;
        this.converter=rabbit.getMessageConverter();
    }

    public Order receiveOrder(){
        Message message= rabbit.receive("tacocloud.order");
        return message != null ? (Order) converter.fromMessage(message) : null;
    }
}
```

여기서는 receiveOrder() 메소드에서 모든것을 처리한다. 즉, 주입된 RabbitTemplate의 receive()메소드를 호출하여 tacocloud.orders큐로부터

주문 데이터를 가져온다. 이때 타임아웃 값을 인자로 전달하지 않았으므로 바로 Message 객체 또는 null 값이 반환된다. 

그리고 Message객체가 반환되면 RabbitTemplate의 MessageConverter를 사용하여 Message 객체를 Order객체로 변환한다.

만일 30초기다리기로 결정했다면 다음과 같이 receive()메소드의 인자로 30,000밀리초를 전달하여 receiveOrder() 메소드를 변경하면된다.
```
public Order receiveOrder(){
    Message message=rabbit.receive("tacocloud.order.queue",30000);
    return message !=null ? (Order) converter.fromMessage(message) :null;
}
```

하지만 이처럼 하드코딩된숫자를 보는게 불편할 수 도있다. 이때 스프링 부트 구성속성으로 타임아웃을 구성할 수 있게 `@ConfigurationProperties` 애노테이션이

지정된 클래스를 생성하는 것이 좋겟다는 생각을 할 수 있을것이다. 그러나 스프링 부트는이미 이를 제공한다. 

따라서 다음과 같이 설정하면된다
```
spring:
  rabbitmq:
    template:
      receive-timeout: 30000
```

receiveOrder() 메소드를 다시 보면 RabbitTemplate의 메시지 변환기를 사용해 수신 Message 객체를 Order객체로 변환하는 것을 알 수 있다.

그러나 RabbitTemplate이 메시지 변환기를 가지고 있음에도 자동으로 변환해줄수없는 이유는 무엇일까?????

`receiveAndConvert()`메소드가 있는 이유가 바로 그때문이다. receiveAndConvert()를 사용하면 다음과 같이 다시작성할수있다.
```
public Order receiveOrder(){
    return (Order) rabbit.receiveAndConvert("tacocloud.order.queue");
}
```
이 코드가 훨씬 더 간단하다. 단지 Object타입을 Order타입으로 캐스팅하는 것만 고려하면된다.

그러나 캐스팅 말고도 다른방법이 있다. 즉, `ParameterizedTypeReference`를 receiveAndConvert()의 인자로 전달하여 직접 Order객체를 수신하게 하는것이다.

```
public Order receiveOrder(){
    return rabbit.receiveAndConvert("tacocloud.order.queue", 
        new ParameterizedTypeReference<Order>(){});
}
```
---

### 🌟 리스너를 사용해서 RabbitMQ 메시지 처리하기

메시지 기반의 RabbitMQ 빈을 위해서 스프링을 `RabbitListener`를 제공한다. 메시지가 큐에 도착할 때 메소드가 자동 호출되도록

지정하기 위해서는 `@RabbitListener`애노테이션을 RabbitMQ빈의 메소드에 지정해야한다.

#### 🏭 RabbitMQ 메시지 리스너로 메소드를 선언하기
```
@Component
public class OrderListener{
    private KitchenUI ui;

    @Autowired
    public OrderListener(KitchenUI ui){
        this.ui=ui;
    }
    @RabbitListener(queues="tacocloud.order.queue")
    public void receiveOrder(Order order){
        ui.displayOrder(order);
    }
}
```

기존의 리스너 애노테이션을 @JmsListener 에서 @RabbitListener로 변경했는데. 사용하는 메시지 브로커와 리스너가 다르더라도

리스너 애노테이션만 변경하면 이처럼 거의 동일한 코드를 사용할수있다. 실제로 @RabbitListener는 @JmsListener와 거의 동일하게 작동한다.

따라서 서로 다른 메시지 브로커인 RabbitMQ, Artemis, ActiveMQ를 사용하는 코드를 작성할 때 완전히 다른 프로그래밍을 배울 필요 없다. 

---


## 💕 카프카 사용하기 

아파치 카프카는 가장 새로운 메시징 시스템이며 ActiveMQ, Artemis,RabbitMQ와 유사한 메시지 브로커이다. 그러나 카프카는 특유의 아키텍처를 갖고있다.

카프카는 높은 확장성을 제공하는 클러스터로 실행되도록 설계되었다. 그리고 클러스터의 모든 카프카 인스턴스에 걸쳐 토픽을 파티션으로 

분할하여 메시지를 관리한다. RabbitMQ가 거래소와 큐를 사용해서 메시지를 처리하는 반면, 카프카는 토픽만 사용한다.

카프카의 토픽은 클러스터의 모든 브로커에 걸쳐 복제된다. 클러스터의 각 노드는 하나 이상의 토픽에 대한 리더로 동작하며, 토픽데이터를 관리하고

클러스터의 다른 노드로 데이터를 복제한다. 각 토픽은 여러개의 파티션으로 분할될 수 있다. 이 경우 클러스터의 각 노드는

한토픽의 하나 이상의 파티션의 리더가 된다.


#### 🏭 카프카 클러스터는 여러개의 브로커로 구성되며, 각 브로커는 토픽의 파티션의 리더로 동작한다.

![image](https://user-images.githubusercontent.com/40031858/108483862-8b482780-72de-11eb-8388-969a8280c2bf.png)


### 🌟 카프카 사용을 위해 스프링 설정하기

카프카를 사용해서 메시지를 처리하려면 이에 적합한 의존성을 빌드에 추가해야한다. 그러나 JMS나 RabbitMQ와 달리 카프카는 스프링 부트 스타터가없다.
```
그렇지만 의존성만 추가하면된다.
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>

```

이처럼 의존성을 추가하면 스프링부트가 카프카 사용을 위한 자동-구성을 해준다. 따라서 우리는 `KafkaTemplate`을 주입하고 메시지를 전송, 수신하면된다.

메시지를 전송 및 수신하기에 앞서, 카프카를 사용할 때 편리한 몇가지 속성을 알아야한다. 특히 `KafkaTemplate`은 기본적으로

localhost에서 실행되면서 9092포트를 리스닝하는 카프카 브로커를 사용한다. 애플리케이션을 개발할 때는 로컬의 카프카 브로커를 사용하면좋다.

spring.kafka.bootstrap-servers 속성에는 카프카 클러스터로의 초기 연결에 사용되는 하나 이상의 카프카 서버들의 위치를 설정한다.

예를들어, 클러스터의 카프카 서버 중 하나가 kafka.tacocloud.com에서 실행되고 9092포트를 리스닝한다면, 이서버의 위치를 다음과 같이구성할수 있다.

```
spring:
  kafka:
    bootstrap-servers:
    -  kafka.tacocloud.com:9092
    -  kafka.tacocloud.com:9093
    ...
```
spring.kafka.bootstrap-servers는 복수형이며 이렇게 서버리스트를 받으므로 클러스터의 여러 서버를 지정할수있다.

### 🌟 KafkaTemplate을 사용해서 메시지 전송하기

```
ListenableFuture<SendResult<K, V>> send(String topic, V data);
ListenableFuture<SendResult<K, V>> send(String topic, K key, V data);
ListenableFuture<SendResult<K, V>> send(String topic, Integer partition, K key, V data);

ListenableFuture<SendResult<K, V>> send(String topic,
                            Integer partition, Long timestamp, K key, V data);

ListenableFuture<SendResult<K, V>> send(ProducerRecord<K, V> record);
ListenableFuture<SendResult<K, V>> send(Message<?> message);
ListenableFuture<SendResult<K, V>> sendDefault(V data);
ListenableFuture<SendResult<K, V>> sendDefault(K key, V data);
ListenableFuture<SendResult<K, V>> sendDefault(Integer partition, K key, V data);

ListenableFuture<SendResult<K, V>> sendDefault(Integer partition, Long time stamp, K key, V data);
```

제일 먼저 알아 둘 것은 `convertAndSend()`메소드가 없다는 것이다. 왜냐하면 KafkaTemplate은 제네릭 타입을 사용하고, 메시지를 전송할 때 직접 도메인 타입을

처리할 수 있기 때문이다. 따라서 모든 send()메소드가 convertAndSend()의 기능을 갖고 있다.

또한, send()와 sendDefault()에는 JMS나 Rabbit에 사용했던 것과 많이 다른 매개변수들이 있다. 

카프카에서는 메시지를 전송할 때는 메시지가 전송되는 방법을 알려주는 다음 매개변수를 지정할 수 있다.
- 메시지가 전송될 토픽(send()에 필요함)
- 토픽 데이터를 쓰는 파티션(선택적임)
- 레코드 전송 키(선택적임)
- 타임스태프(선택적이며 기본값은 System.currentTimeMillis())
- 페이로드(메시지에 적재된 순수한 데이터(예를들어, Order객체)이며 필수임)

토픽과 페이로드는 가장 중요한 매개변수들이다. 파티션과 키는 send()와 sendDefault()에 매개변수로 제공되는 추가 정보일 뿐

`KafkaTemplate`을 사용하는 방법에는 거의 영향을 주지 않는다. 바로 위에 봤듯, send()메소드에는 ProducerRecord를 전송하는 것도 있다.

`ProducerRecord`는 모든 선행 매개변수들을 하나의 객체에 담은 타입이다. 또한 Message 객체를 전송하는 send()메소드도 있지만, 

이경우는 우리 도메인 객체를 Message객체로 변환해야한다. 대개의 경우에 ProducerRecord나 Message객체를 생성 및

전송하는 것보다는 다른 send()메소드 중 하나를 사용하는 것이 쉽다.

#### 🏭KafkaTemplate을 사용해서 주문 데이터 전송하기
```
@Service
public class KafkaOrderMessagingService implements OrderMessagingService{
    private KafkaTemplate<String, Order> kafkaTemplate;

    @Autowired
    public KafkaOrderMessagingService(KafkaTemplate<String, Order> kafkaTemplate){
        this.kafkaTemplate=kafkaTemplate;
    }

    @Override
    public void sendOrder(Order order){
        kafkaTemplate.send("tacocloud.orders.topic",order);
    }
}
```

여기서 sendOrder()메소드는 주입된 KafkaTemplate의 send()메소드를 사용해서 tacocloud.orders.topic이라는 이름의 

토픽으로 Order객체를 전송한다. `Kafka`라는 단어가 코드의 이곳저곳에 포함된 것 외에는 JMS와 Rabbit에 사용했던 코드와 크게다르지않다.

기본토픽을 설정한다면 sendOrder()메소드를 약간 더 간단하게 만들 수 있다. 우선 spring.kafka.template.default-topic에 설정하자
```
spring:
  kafka:
    template:
      default-topic: tacocloud.orders.topic
```
그 다음에 sendOrder()메소드에서 send()대신 sendDefault()를 호출하면 된다. 이때는 토픽 이름을 인자로 전달하지 않는다.
```
@Override
public void sendOrder(Order order){
    kafkaTemplate.sendDefault(order);
}
```

### 🌟 카프카 리스너 작성하기

send()와 sendDefault()특유의 메소드 시그니처 외에도 KafkaTemplate은 메시지를 수신하는 메소드를 일체 제공하지 않는다

는 점에서 JmsTemplate이나 RabbitTemplate과 다르다. 따라서 스프링을 사용해서 카프카 토픽의 메시지를

가져오는 유일한 방법은 메시지 리스너를 작성하는 것이다.

카프카의 경우 메시지 리스너는 `@KafkaListener`애노테이션이 지정된 메소드에 정의된다. 

@KafkaListener는 @JmsListener나 @RabbitListener와 거의 유사하며, 동일한 방법으로 사용된다.

#### 🏭 @KafkaListener를 사용해 주문 데이터 수신하기
```
@Component
public class OrderListener{
    private KitchenUI ui;

    @Autowired
    public OrderListener(KitchenUI ui){
        this.ui=ui;
    } 

    @KafkaListener(topics="tacocloud.orders.topic")
    public void handle(Order order){
        ui.displayOrder(order);
    }
}
```
tacocloud.orders.topic이라는 이름의 토픽에 메시지가 도착할 때 자동 호출되어야 한다는 것을 나타내기 위해 handle()메소드에는

`@KafkaListener`애노테이션이 지정되었다. 그리고 페이로드인 Order객체만 handle()의 이ㄴ자로 받는다. 

그러나 메시지의 추가적인 메타데이터가 필요하다면 ConsumerRecord나 Message객체도 인자로 받을 수 있다.

예를들어 다음의 handle()메소드에서는 수신된 메시지의 파티션과 타임스탬프를 로긍하기 위해 ConsumerRecord를 인자로 받는다
```
@KafkaListener(topics="tacocloud.orders.topic")
public void handle(Order order, ConsumerRecord<Order> record){
    log.info("Received from partition{} with timestamp{}", record.partition(), record.timestamp());

    ui.displayOrder(order);
}
```

이와 유사하게 ConsumerRecord 대신 Message 객체를 요청하여 같은일을 처리할 수 있다.
```
@KafkaListener(topics="tacocloud.orders.topic")
public void handle(Order order, Message<Order> message){
    MessageHeaders headers=message.getHeaders();
    log.info("Received from partition {} with timestamp {}" , 
        headers.get(KafkaHeaders.RECEIVED_PARTITION_ID)
        headers.get(KafkaHeaders.RECEIVED_TIMESTAMP)
    );
    ui.displayOrder(order);
}
```

메시지 페이로드는 ConsumerRecord.value()나 Message.getPayload()를 사용해도 받을 수 있다는 점을 알아두자.

이것은 handle()의 매개변수로 직접 Order객체를 요청하는 대신 ConsumerRecord나 Message 객체를 통해 Order객체를 요청할수있음을 의미한다..

## 💕실행 방법

```

1. ./mvnw clean package
2. java -jar tacos/target/taco-cloud-0.0.1-SNAPSHOT.jar
3. java -jar tacocloud-kitchen/target/tacocloud-kitchen-0.0.1-SNAPSHOT.jar

```


# 🥇 8장 요약 

### 🌟[1] 애플리케이션 간 비동기 메시지 큐를 이용한 통신 방식은 간접 계층을 제공하므로 

### 애플리케이션 간의 결합도는 낮추면서 확장성은 높인다.

### 🌟[2] 스프링은 JMS, RabbitMQ또는 아파치 카프카를 사용해 비동기 메시징을 지원한다

### 🌟[3] 스프링 애플리케이션은 템플릿 기반의 클라이언트인 JmsTemplate,RabbitTemplate

### 또는 KafkaTemplate을 사용해서 메시지 브로커를 통한 메시지 전송을 할 수 있다.

### 🌟[4] 메시지 수신 애플리케이션 같은 템플릿 기반의 클라이언트들을 사용해서 풀모델

### 형태의 메시지 소비(가져오기)를 할 수 있다.

### 🌟[5] 메시지 리스너 애노테이션인 @JmsListener, @RabbitListener 또는 @KafkaListener

### 를 빈 메서드에 지정하면 푸시모델의 형태로 컨슈머에게 메시지가 전송 될 수 있다.