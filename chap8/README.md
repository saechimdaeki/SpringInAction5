# 8장 비동기 메시지 전송하기

### 이 장에서 배우는 내용
- 비동기 메시지 전송
다.
- JMS,RabbitMQ,카프카(Kafka)를 사용해서 메시지 전송하기
- 브로커에서 메시지 가져오기
- 메시지 리스닝하기

---

#### 앞서 7장에서는 REST를 사용한 `동기화` 통신을 알아보았다. 그러나 이것만이 개발자가 사용할 수 있는 

#### 애플리케이션 간의 통신 형태는 아니다. `비동기` 메시징은 애플리케이션 간에 응답을 기다리지 않고 간접적으로 메시지를 전송하는 방법이다

### 따라서 통신하는 애플리케이션 간의 결합도를 낮추고 확장성을 높여준다

이번 장에서는 비동기 메시징을 사용해 타코클라우드 웹사이트로부터 타코 클라우드주방의 별도 애플리케이션으로 주문 데이터를 전송할 것이다.

이 경우 스프링이 제공하는 다음의 비동기 메시징을 고려할 수 있다. 바로 `JMS`, `RabbitMQ`,`AMQP`, `아파치 카프카`

그리고 기본적인 메시지 전송과 수신에 추가하여, 스프링의 메시지 기반 POJO지원에 관해 알아볼 것이다.

이것은 EJB의 MDB(message-driven bean)와 유사하게 메시지를 수신하는 방법이다.

---

## JMS로 메시지 전송하기

JMS는 두 개 이상의 클라이언트 간에 메시지 통신을 위한 공통API를 정의하는 자바표준이다.

JMS가 나오기전에는 메시지 통신을 중개하는 메시지 브로커들이 나름의 API를 갖고 있어서 애플리케이션의 메시징 코드가 브로커 간에 호환될 수 없었다. 

그러나 JMS를 사용하면 이것을 준수하는 모든 구현 코드가 공통인터페이스를 통해 함께 작동할 수 있다.

스프링은 `JmsTemplate`이라는 템플릿 기반의 클래스를 통해 JMS를 지원한다. `JmsTemplate`
을 사용하면 프로듀서가 큐와 토픽에 메시지를 전송하고

컨슈머는 그 메시지들을 받을 수 있다. 또한, 스프링은 메시지 기반의 POJO도 지원한다. POJO는 큐나 토픽에 도착하는 메시지에 반응하여 비동기 방식으로 메시지를 수신하는 자바객체다.

### JMS 설정하기

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

#### 다음은 Artemis 브로커의 위치와 인증 정보를 구성하는 속성은 다음과같다
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

#### ActiveMQ 브로커의 위치와 인증 정보를 구성하는 속성
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

### JmsTemplate을 사용해서 메시지 전송하기

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

#### send()를 사용해 주문 데이터 전송하는 클래스는 다음과같다
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

### 메시지 변환하고 전송하기

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

### 메시지 변환기 구현하기

`MessageConvertor`는 스프링에 정의된 인터페이스이며 두 개의 메소드만 정의되어 있다
```
public interface MessageConverter{
    Message toMessage(Object object, Session session) throws JMSException, MessageConversionException;

    Object fromMessage(Message message)
}
```

이렇게 있지만 구현하지않아도 된다. 스프링이 편리하게 구현해 주었기때문이다

### 공통적인 변환 작업을 해주는 스프링 메시지 변환기
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

### 후처리 메시지 

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