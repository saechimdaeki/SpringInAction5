# 18장 JMX로 스프링 모니터링하기

### 🌟 이 장에서 배우는 내용
- 액추에이터 엔드포인트 MBeans사용하기
- 스프링 빈을 MBeans로 노출하기
- 알림 발행(전송)하기

15년동안 `JMX`는 자바 애플리케이션을 모니터링하고 관리하는 표준 방법으로 사용되고 있다. MBeans로 알려진

컴포넌트를 노출함으로써 외부의 JMX클라이언트는 오퍼레이션 호출, 속성, 검삼, MBeans의 이벤트 모니터링을 통해 애플리케이션을 관리할수있다.

JMX는 스프링 부트 애플리케이션에 기본적으로 자동 활성화된다. 이에 따라 모든 액추에이터 엔드포인트는 MBeans로 노출된다.

또한 스프링 애플리케이션 컨텍스트의 어떤 다른 빈도 MBeans로 노출할 수 있게했다.

## 👟 액추에이터 MBeans 사용하기

16장에서 본것을 기억해보면 /heapdump를 제외한 모든 액추에이터 엔드포인트가 MBeans로 노출되어 있다. 따라서 어떤 JMX클라이언트를

사용해도 현재 실행 중인 스프링 부트 애플리케이션의 액추에이터 엔드포인트 MBeans와 연결할 수 있다. 

JConsole을 사용하면 org.springframework.boot 도메인 아래에 나타난 액추에이터 엔드포인트 Mbean들을 볼 수 있다.

액추에이터 엔드포인트 Mbeans는 HTTP의 경우처럼 명시적으로 포함시킬 필요 없이 기본으로 노출된다는 장점이 있다.

그러나 `management.endpoints.jmx.exposure.include`와 `management.endpoints.jmx.exposure.exclude`를

설정하여 MBeans로 노출되는 액추에이터 엔드포인트를 선택할 수 있다. 예를 들어, /health, /info, /bean

,/condition 엔드포인트만 액추에이터 엔드포인트 MBeans로 노출할 때는 다음과 같이 management.endpoints.jmx.exposure.include를 설정하면된다
```
management:
  endpoints:
    jmx:
      exposure:
        include: health,info,bean,conditions
```
또는 노출에서 제외할 때는 다음과 같이 `management.endpoints.jmx.exposure.exclude`를 설정한다.

```
management:
  endpoints:
    jmx:
      exposure:
        exclude:  env, metrics
```

여기서는 `management.endpoints.jmx.exposure.exclude`를 사용해서 /env와 /metrics엔드포인트를 노출에서 제외한다

그리고 다른 모든 엔드포인트는 여전히 MBeans로 노출된다. JConsole에서 액추에이터 MBeans 중 하나의 관리용 오퍼레이션

을 호출할 때는 왼쪽 패널 트리의 해당 엔드포인트 MBeans를 확장한 후 Operations 아래의 원하는 오퍼레이션을 선택하면된다.

## 👟 우리의 MBeans 생성하기
스프링은 우리가 원하는 어떤 빈도 `JMX Beans`로 쉽게 노출한다. 따라서 빈 클래스에 `@ManagedResource`애노테이션을 지정하고

메소드에는 `@ManagedOperation`을, 속성에는 `@ManagedAttribute`만 지정하면 된다. 나머지는 스프링이 알아서 해준다


예를 들어,타코 클라우드 시스템을 통해 주문된 타코의 수량을 추적하는 MBeans를 제공하고 싶다면 이경우 생성된

타코의 수량을 유지하는 서비스 빈을 아래와 같이 정의할 수 있다.

```
@Service
@ManagedResource
public class TacoCounter extends AbstractRepositoryEventListener<Taco>{
    private AtomicLong counter;

    public TacoCounter(TacoRepository tacoRepo){
        long initialCount=tacoRepo.caount();
        this.counter=new AtomicLong(initialCount);
    }

    @Override
    protected void onAfterCreate(Taco entity){
        counter.incrementAndGet();
    }

    @ManagedAttribute
    public long getTacoCount(){
        return counter.get();
    }

    @ManagedOperation
    public long increment(long delta){
        return counter.addAndGet(delta);
    }
}
```
여기서 TacoCounter 클래스에는 @Service 애노테이션이 지정되었으므로 스프링이 컴포넌트를 찾아주며, 이 클래스 인스턴스는

스프링 애플리케이션 컨텍스트의 빈으로 등록된다. 또한 이빈이 MBean도 된다는 것을 나타내는 `@ManagedResource`도 지정되었다.

그리고 getTacoCOunt()메소드는  `ManagedAttribute`가 지정되었으므로 MBeans 속성을 노출되며, increment() 메소드는 

`@ManagedOperation`이 지정되었으므로 MBeans오퍼레이션으로 노출된다.

또한 JMX와는 관련없지만 주목할 만한 기능이있는데 Abstract Repository EventListener의 서브클래스이므로

Taco객체가 TacoRepository를 통해 저장될 때 퍼시스턴스 관련 이벤트를 받을 수 있다. 즉 새로운 Taco객체가 생성되어

레포지토리에 저장될때마다 onAfterCreate()메소드가 호출되어 카운터를 1씩 증가시킨다. 그러나 AbstractRepositoryEventListener는

객체가 생성, 저장, 삭제되기 전과 후에 발생하는 이벤트 처리 메소드들도 제공한다. 기본적으로 MBeans 오퍼레이션과

속성은 풀 방식을 사용한다. 즉, MBeans 속성의 값이 변경되더라도 자동으로 알려주지 않으므로 JMX 클라이언트를

통해 봐야만 알 수 있다. 그러나 MBeans는 JMX클라이언트에 알림을 푸시할 수 있는 방법이 있다.

## 👟 알림 전송하기

스프링의 `NotifactionPublisher`를 사용하면 MBeans가 JMX클라이언트에 알림을 푸시 할 수 있다.

NotificationPublisher는 하나의 sendNotification()메소드를 갖는다. 이 메소드는 Notification 객체를

인자로 받아서 MBean을 구독하는 JMX클라이언트에게 발행한다. MBeans가 알림을 발행하려면 NotificationPublisherAware

인터페이스의 setNotificationPublisher()메소드를 구현해야 한다. 예를들어, 100개의 타코가 생성될 때마다

알림을 전송하고 싶다고 하자. 이때는 TacouCOunter클래스를 아래와 같이 변경하면된다.

```
@Service
@ManagedResource
public class TacoCounter extends AbstractRepositoryEventListener<Taco> implements NotificationPublisherAware{
    private AtomicLong counter;
    private NotificationPublisher np;
    ...

    @Override
    public void setNotificationPublisher(NotificationPublisher np){
        this.np=np;
    }
    ...
    @ManagedOperation
    public long increment(long delta){
        long before=counter.get();
        long after=counter.addAndGet(delta);
        if((after/100)>(before/100)){
            Notification notification=new Notification(
                "taco.count", this,
                before, after+ "th taco created!"
            );
            np.sendNotification(notification);
        }
        return after;
    }
}
```

이 경우 JMX클라이언트에서 알림을 받으려면 TacoCounter MBeans를 구독해야 한다. 그러면 100개의 타코가

생성될 때마다 해당 클라이언트는 알림을 받을 수 있다. 알림은 애플리케이션이 자신을 모니터링하는 클라이언트에게 능동적으로

데이터를 전송하여 알려주는 좋은 방법이다. 따라서 클라이언트가 지속적으로 반복해서 관리 속성을 조회하거나 

관리 오퍼레이션을 호출할 필요가없다.

# 18장 요약

### [1] 대부분의 액추에이터 엔드포인트는 JMX 클라이언트로 모니터링할 수 있는 MBeans로 사용할 수 있다.

### [2] 스프링은 스프링 애플리케이션 컨텍스트의 빈을 모니터링하기 위해 자동으로 JMX를 활성화한다.

### [3] 스프링 빈에 @ManagedResource 애노테이션을 지정하면 MBeans로 노출될 수 있다. 그리고 해당 빈의 메소드에

### @ManagedOperation을 지정하면 관리 오퍼레이션으로 노출 될 수 있으며, 속성에 @ManagedAttribute

### 를 지정하면 관리 속성으로 노출될 수 있다.

### [4] 스프링 빈은 NotificationPublisher를 사용하여 JMX클라이언트에게 알림을 전송할 수 있다.