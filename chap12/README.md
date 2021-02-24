# 🥇 12장 리액티브 데이터 퍼시스턴스

## 🍰 이 장에서 배우는 내용
- 스프링 데이터의 리액티브 레포지토리
- 카산드라와 몽고DB의 리액티브 레포지토리 작성하기
- 리액티브가 아닌 레포지토리를 리액티브 사용에 맞추어 조정하기
- 카산드라를 사용한 데이터 모델링

이전 장에서는 스프링 WebFlux를 사용해 리액티브하고 블로킹이 없는 컨트롤러를 생성하는 법을 알아 보았다.

이것은 웹계층의 확장성을 향상시키는데 도움을 준다 그러나 이런 컨트롤러는 같이 작동되는 다른 컴포넌트도 블로킹이

없어야 진정한 블로킹이업는 컨트롤러가 될 수 있다. 만일 블로킹되는 레포지토리에 의존하는 스프링 WebFlux

리액티브 컨트롤러를 작성한다면, 이 컨트롤러는 해당 레포지토리에 데이터 생성을 기다리느라 블로킹될 것이다.

따라서 스프링 데이터를 사용해 리액티브 레포지토리를 작성하는 방법을 배워보자.

## 👟 스프링 데이터의 리액티브 개념 이해하기
스프링 데이터 Kay 릴리즈 트레인부터 스프링 데이터는 리액티브 레포지토리의 지원을 제공하기 시작하였다.

여기서는 카산드라, 몽고DB, 카우치베이스, 레디스로 데이터를 저장할 때 리액티브 프로그래밍 모델을 지원하는 것이 포함된다.

그러나 관계형 데이터베이스나 JPA는 리액티브 레포지토리가 지원되지 않는다. 관계형 데이터베이스는 업계에서

가장 많이 사용되지만, 스프링 데이터 JPA로 리액티브 프로그래밍 모델을 지원하려면 관계형 데이터베이스와

JDBC드라이버 역시 블로킹 되지 않는 리액티브 모델을 지원해야한다. 최소한 현재까지는 관계형 데이터베이스를 리액티브하게 사용하기 위한 지원이안된다.

### 🌟 스프링 데이터 리액티브 개요
스프링 데이터 리액티브의 핵심은 다음과 같이 요약할 수 있다. 즉, 리액티브 레포지토리는 도메인 타입이나 컬렉션 

대신 Mono나 Flux를 인자로 받거나 반환하는 메소드를 갖는다는 것이다. 예를 들어, 데이터베이스로부터 식자재 타입으로

Ingredient객체들을 가져오는 레포지토리 메소드는 다음과 같이 레포지토리 인터페이스에 선언될 수 있다.

    Flux<Ingredient> findByType(Ingredient.Type type);

이 코드를 보면 알 수 있듯, findByType()메소드는 Flux< Ingredient>를 반환한다. 

또한 , Taco객체를 저장하는 리액티브 레포지토리의 메소드 시그니처는 다음과 같다

    <Taco> Flux<Taco> saveAll(Publisher<Taco> tacoPublisher);

이 경우 saveAll()메소드는 Taco타입을 포함하는 Publisher인 Mono< Taco>나 Flux< Taco>를

인자로 받으며, Flux< Taco>를 반환한다. 이것은 직접 도메인 처리하는 즉, Taco객체를 인자로 받고

저장된 Taco 객체를 반환하는 save()메소드를 갖는 리액티브가 아닌 레포지토리와 다르다.

```
간단히 말해, 스프링 데이터의 리액티브 레포지토리는 스프링 데이터의 리액티브가 아닌 레포지토리와 거의 
동일한 프로그래밍 모델을 공유한다. 단, 리액티브 레포지토리는 도메인 타입이나 컬렉션 대신 Mono나 Flux를
인자로 받거나 반환하는 메소드를 갖는다는 것만 다르다.
```

### 🌟 리액티브와 리액티브가 아닌 타입간의 변환

기존에 관계형 데이터베이스가 있지만 스프링 데이터의 리액티브 프로그래밍 모델이 지원하는 4개의 데이터베이스 중

하나로 이전하는 것이 불가능하다. 이경우 리액티브 프로그래밍을 적용할 수 없는것일끼????

리액티브 프로그래밍의 장점은 `클라이언트부터 데이터베이스`까지 리액티브 모델을 가질 때 완전하게 발휘된다.

그러나 데이터베이스가 리액티브가 아닌 경우에도 여전히 일부 장점을 살릴 수 있다.

심지어는 선택한 데이터베이스가 블로킹 없는 리액티브 쿼리를 지원하지 않더라도 블로킹 되는 방식으로 데이터를 가져와서 가능한

빨리 리액티브 타입으로 변환하여 상위 컴포넌트들이 리액티브의 장점을 활용하게 할 수 있다.

예를들어, 관계형 데이터베이스와 스프링 데이터JPA를 사용한다고 할때 주문데이터 레포지토리는 다음과 같은 시그니처의 메소드를 가질 수 있다.

    List<Order> findByUser(User user);

이 메소드는 리액티브가 아닌 List< Order>를 반환하며 이 List는 지정된 사용자의 모든 주문을 포함한다.

findByUser()가 호출되면 해당 쿼리가 실행되어 결과 데이터가 List에 저장된다. 그러나 이렇게 처리되는 동안 findByUser()는 블로킹된다.

왜냐하면 List가 리액티브 타입이 아니므로 리액티브 타입인 Flux가 제공하는 어떤 오퍼레이션도 수행할 수 없기 때문이다.

게다가 컨트롤러가 findByUser()를 호출했다면 결과를 리액티브하게 사용할 수 없어 확장성을 향상시킬수없다.

이처럼 블로킹 방식의 JPA레포지토리 메소드를 호출해서는 곤란하다. 그러나 이 경우 가능한 리액티브가 아닌 

List를 Flux로 변환하여 결과를 처리할 수 있다. 이때는 Flux.fromIterable()을 사용하면 된다.

```
List<Order> orders=repo.findByUSer(someUser);
Flux<Order> orderFlux=Flux.fromIterable(oreders);
```

마찬가지로 특정 ID의 주문 데이터를 가져올 때는 다음과 같이 Mono로 변환하면 된다
```
Order order = repo.findById(Long id);
Mono<Order> orderMono=Mono.just(order);
```

이처럼 Mono.just()메소드와 Flux의 fromIterable(),fromArray(),fromStream()메소드를 사용하면

레포지토리의 리액티브가 아닌 블로킹 코드를 격리시키고 애플리케이션의 어디서든 리액티브 타입으로 처리하게 할 수 있다.

또 Mono나 Flux를 사용하면서 리액티브가 아닌 JPA레포지토리에 save()를 호출해서 저장해야한다면?

다행히 Mono나 Flux 모두 자신들이 발행하는 데이터를 도메인 타입이나 Iterable타입으로 추출하는 오퍼레이션을 갖고있다.

예를들어, WebFlux컨트롤러가 Mono< Taco>를 받은 후 이것을 스프링 데이터 JPA레포지토리의 save()메소드를 사용해 저장한다고하면

Mono의 block()메소드를 호출해 Taco객체로 추출하면된다.
```
Taco taco=tacoMono.block();
tacoRepo.save(taco);
```
이름이 암시하든 `block()`메소드는 추출작업을 수행하기 위해 블로킹 오퍼레이션을 실행한다.

Flux이 데이터를 추출할 때는 `toIterable()`을 사용할 수 있다. WebFlux 컨트롤러가 Flux< Taco>를 받은 후

이것을 스프링 데이터JPA 레포지토리의 save()메소드를 사용해 저장한다면 다음과 같다
```
Iterable<Taco> tacos=tacoFlux.toIterable();
tacoRepo.saveAll(tacos);
```
Mono.block()과 마찬가지로 Flux.toIterable()은 Flux가 발행하는 모든 객체를 모아서 Iterable 타입으로 추출해준다.

그러나 Mono.block()이나 Flux.toIterable()은 추출 작업을 할 때 블로킹이되므로 리액티브 프로그래밍 모델을 벗어난다.

따라서 이런식의 Mono나 Flux사용은 가급적 적게하는것이좋다.

이처럼 블로킹되는 추출 오퍼레이션을 피하는 더 리액티브한 방법이 있다. 즉, Mono나 Flux를 구독하면서 발행되는

요소 각각에 원하는 오퍼레이션을 수행하는 것이다. 예를 들어 Flux< Taco>가 발행하는 Taco객체를

리액티브가 아닌 레포지토리에 저장할 때는 다음과 같이 할 수 있다.
```
tacoFlux.subscribe(taco -> {
    tacoRepo.save(taco);
});
```

여기서 레포지토리의 save()메소드는 여전히 리액티브가 아닌 블로킹 오퍼레이션이다. 그러나 Flux나 Mono가 발행하는 일괄처리보다는 더 바람직하다.

### 🌟 리액티브 레포지토리 개발하기

리액티브가 아닌 레포지토리 지원 위에 구축된 스프링 데이터 카산드라와 스프링데이터 몽고DB는 리액티브 모델도 지원한다.

따라서 데이터 퍼시스턴스를 제공하는 백엔드로 이 데이터베이스들을 사용하면, 스프링 애플리케이션이 웹 계층 부터

데이터베이스까지에 걸쳐 진정한 엔드-to-엔드 리액티브 플로우를 제공할 수 있다.

## 🏓 리액티브 카산드라 레포지토리 사용하기

카산드라는 분산처리, 고성능, 상시 가용, 궁극적인 일관성을 갖는 NoSQL 데이터베이스이다.

간단히 말해 카산드라는 데이터를 테이블에 저장된 행으로 처리하며, 각 행은 일대 다 관계의 많은 분산 노드에 걸쳐 분할된다.

즉, 한 노드가 모든 데이터를 갖지는 않지만 특정 행은
 다수의 노드에 걸쳐 복제될 수 있으므로 단일장애점을 없애준다.

`스프링 데이터 카산드라`는 카산드라 데이터베이스의 자동화된 레포지토리 지원을 제공하는데 이것은 관계형

데이터베이스의 스프링 JPA가 제공하는 것과 유사하면서도 다르다. 또한 `스프링 데이터 카산드라`는 애플리케이션의

도메인 타입을 데이터베이스 구조에 매핑하는 애노테이션을 제공한다.

카산드라는 오라클이나 SQL SERVER와 같은 관계형 데이터베이스와 유사한많은 개념을 공유하지만 관계형 데이터베이스가 아니고 여러면에서 매우다르다.

더 자세한 내용은 https://cassandra.apache.org/doc/latest/ 를참고하자.

### 🌟 스프링 데이터 카산드라 활성화하기

스프링데이터 카산드라 스타터 의존성은 두개가 있는데 리액티브 레포지토리를 작성할 것이므로 다음 의존성을 추가하자
```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-cassandra-reactive</artifactId>
</dependency>
```
이 의존성은 스프링 데이터 JPA 스타터 의존성대신 필요하다는 것을 알아두자. 즉, JPA를 사용한 관계형 데이터베이스에

타코클라우드 데이터를 저장하는 대신, 스프링 데이터를 사용해서 카산드라 데이터베이스에 저장한다.

따라서 스프링 데이터 JPA스타터 의존성과 모든 관계형데이터베이스 의존성(JDBC, H2)를 빌드에서 삭제해야한다.

이제 별도의 구성 없이 카산드라 레포지토리를 작성할 수 있다. 단, 일부 구성은 제공해야 하는데, 최소한 레포지토리가 운용되는

키 공간의 이름을 구성해야 하며, 이렇게 하기 위해 해당 키공간을 생성해야한다.

키 공간을 자동으로 생성하도록 스프링 데이터 카산드라를 구성할 수 있지만, 우리가 직접 하는것이 쉽다.

카산드라 CQL셀에서 다음과 같이 create keyspace명령을 사용하면 타코클라우드 애플리케이션의 키공간을 생성할 수 있다.
```
cqlsh> create keyspace tacocloud
    ... with replication={'class':'SimpleStrategy', 'replication_factor':1}
    ... and durable_wirtes=true;
```

여기서는 단순 복제 및 durable_writes가 true로 설정된 tacocloud라는 키공간을 생성한다.

replication_factor가 1일 떄는 각 행의 데이터를 여러 벌 복제하지 않고 한 벌만 유지함을 나타낸다.

복제를 처리하는 방법은 복제 전략이 결정하며, 여기서는 SimpleStrategy를 지정하였다. SimpleStrategy

복제 전략은 단일 데이터 센터 사용시에 좋다. 그러나 카산드라 클러스터가 다수의 데이터센터에 확산되어 있을때는 

NetworkTopologyStrategy를 고려할 수 있다. 키공간을 생성했으니 이제 spring.data.cassandra.keyspace-name속성을구성하자
```
spring:
  data:
    cassandra:
      keyspace-name: tacocloud
      schema-action: recreate-drop-unsued
```

여기서는 키공간 외에 spring.data.cassandra.schema-action을 recreate-drop-unsed로 설정하였다.

이설정은 개발에 매우 유용한데 매번 시작할때마다 모든 테이블과 사용자정의타입이 삭제되고 재생성되기때문이다.

기본적으로 스프링데이터 카산드라는 카산드라가 로컬로 실행되면서 9092포트를 리스닝하는것으로 간주한다. 

그러나 이것을 실무 설정에 하듯이 변경하고 싶을때는 다음과같이 spring.data.cassandra.contact-points와 spring.data.cassandra.port

를설정하면 된다.
```
spring:
  data:
    cassandra:
      keyspace-name: tacocloud
      contact-points:
      - casshost-1.tacocloud.com
      - casshost-2.tacocloud.com
      - casshost-3.tacocloud.com
      port: 9043
```

spring.data.cassandra.contact-points 속성은 카산드라 노드가 실행중인 호스트를 나타낸다. 

기본적으로는 localhost로 설정되지만, 호스트이름의 목록을 설정할 수도있다. 이경우 각 노드의 호스트

연결을 시도하여 카산드라 클러스터에 단일 장애점이 생기지 않게해주며, contact-points에 지정된

호스트 중 하나를 통해 애플리케이션이 클러스터에 연결될 수 있게해준다. 

### 🌟 카산드라 데이터 모델링 이해하기

카산드라는 관계형 데이터베이스와 많이다르다. 따라서 도메인 타입을 카산드라의 테이블로 매핑하기전에 알아 둘 중요한것이있다.

즉 카산드라 데이터 모델링은 관계형 데이터베이스에 저장하기 위해 데이터를 모델링하는 것과다르다.

카산드레 데이터 모델링에 관해 알아 둘 몇가지 중요한 사항은 다음과 같다.

---

카산드라 테이블은 얼마든지 많은 열을 가질 수 있다. 그러나 모든행이 같은 열을 갖지않고 행마다 서로다른열을 가질 수 있다.

카산드라 데이터베이스는 다수의 파티션에 걸쳐 분할된다. 테이블의 어떤 행도 하나이상의 파티션

에서 관리될 수 있다. 그러나 각 파티션은 모든행을 갖지않고 서로다른행을가질수있다.

카산드라 테이블은 두 종류의 키를 갖는다. 파티션키와 클러스터링 키다. 각 행이 유지관리되는 파티션을

결정하기 위해 해시오퍼레이션이 각 행의 파티션 키에 수행된다. 클러스터링 키는 각 행이 파티션 내부에서 유지

관리되는 순서를 결정한다.

카산드라는 읽기 오퍼레이션에 최적화되어 있다. 따라서 테이블이 비정규화되고 데이터가 다수의 테이블에 걸쳐

중복되는 경우가 흔하다. 예를들어, 고객 정보는 고객테이블에 저장되지만 각 고객의 주문정보를

포함하는 테이블에도 중복저장될 수 있다. 이렇듯, JPA애노테이션을 단순히 카산드라 애노테이션으로

변경한다고 해서 타코도메인타입을 카산드라에 적용할 수 있는건 아니다.

---

### 🌟 카산드라 퍼시스턴스의 도메인 타입 매핑

도메인 타입들을 이전에는 JPA명세가 제공하는 애노테이션들로 나타냈었다. 그리고 이 애노테이션들은

도메인 타입을 관계형 데이터베이스에 저장하는 엔티티로 매핑하였다. 그러나 카산드라 퍼시스턴스에는

이 애노테이션들을 사용할 수 없다. 대신에 스프링데이터카산드라는 유사한 목적의 매핑애노테이션들을 제공한다.

카산드라에서 사용할 수 있는 새로운 Ingredient클래스는 다음과같다
```
@Data
@RequiredArgsConstructor
@NoArgsConstructor(access=AccessLevel.PRIVATE,force=true)
@Table("ingredients")
public class Ingredient{
    @PrimaryKey
    private final String id;
    private final String name;
    private final Type type;

    public static enum Type{
        WRAP,PROTEIN,VEGGIES,CHEESE,SAUCE
    }
}
```

여기서는 JPA퍼시스턴스에서 클래스에 지정했던 `@Entity`대신 `@Table`을 지정하였다. 

`@Table`은 식재료 데이터가 ingredients테이블에 저장 및 유지되어야 한다는것을 나타낸다. 

그리고 id 속성에 지정했던 `@Id`대신 `@PrimaryKey`애노테이션을 지정하였다.

#### Taco 클래스를 카산드라 tacos 테이블로 매핑하기
```
@Data
@RestResource(rel="tacos", path="tacos")
@Table("tacos")
public class Taco{
    @PrimaryKeyColumn(type=PrimaryKeyType.PARTITIONED)
    private UUID id= UUIDs.timeBased();

    @NotNull
    @Size(min=5, message="Name must be at least 5 characters long")
    private String name;

    @PrimaryKeyColumn(type=PrimaryKeyType.CLUSTERED,
        ordering=Ordering.DESCENDING)
    private Date createdAt=new Date();

    @Size(min=1, message="You must choose at least 1 ingredient")
    @Column("ingredients")
    private List<IngredientUDT> ingredients;
}
```

이코드를 보면 알 수 있듯이, Taco 클래스의 테이블 매핑에는 더 많은 것이 수반된다. 우선, 타코 데이터를

저장하는 테이블의 이름을 tacos로 지정하기 위해 Ingredient와 마찬가지로 @Table애노테이션이 사용되었다.


id속성은 여전히 기본키다. 그러나 여기서는 이것이 두개의 기본키 열중 하나다.

더 자세히 말해 id속성은 PrimaryKeyType.PARTITIONED타입으로 @PrimaryKeyColumn에 지정되어있다.

이것은 타코 데이터의 각 행이 저장되는 카산드라 파티션을 결정하기 위해 사용되는 파티션 키가 id속성이라는것을 나타낸다.

또한 id속성의 타입은 Long대신 UUID이며, 이것은 자동생성되는 ID값을 저장하는 속성에 흔히 사용하는 타입이다.

그리고 UUID는 새로운 Taco객체가 생성될 때 시간 기반의 UUID값으로 초기화된다.

조금 더 보면 또 다른 기본키열로 지정된 createdAt속성이 있다. 그러나 여기서는 `@PrimaryKeyColumn`의 type속성이

클러스터링 키라는 것을 나타낸다. 클러스터링 키는 파티션 내부에서 행의 순서를 결정하기 위해 사용되며, 여기서는

내림차순으로 설정되었다. 따라서 지정된 파티션 내부의 더 새로운 행이 tacos테이블에 먼저 나타난다.

제일 끝에 정의된 ingredients 속성을 저장하는 List대신 IngredientUDT객체를 저장하는 List로 정의되었다.

카산드라 테이블은 비정규화되어서 다른테이블과 중복되는 데이터를 포함시킬 수 있다. 따라서 모든 사용 가능한 식재료 데이터를 갖는

테이블은 ingredients지만 각 타코에 선택된 식재료는 여기 있는 tacos테이블의 ingredients열에

중복 저장될 수 있다. 그리고 ingredients테이블의 하나 이상의 행을 참조하는 대신, ingredients 속성은 선택된 각 식재료

전체 데이터를 포함한다.

여기서 새로운 IngredientUDT 클래스를 사용하는이유는 뭘까?? Ingredient클래스를 재사용은 못할까? 간단히 말해,

ingredients열처럼 데이터의 컬렉션을 포함하는 열은 네이티브타입의 컬렉션이거나 새로운 정의타입의 컬렉션이어야 하기 때문이다.

카산드라에서 사용자 정의 타입은 네이티브타입보다 더 다채로운 테이블열을 선언할 수 있게 해준다.

비정규화된 관계형 데이터베이스 외부 키처럼 사용된다. 단 다른 테이블의 한 행에 대한 참조만 갖는 외부키와는 대조적으로

사용자 정의 타입은 열의 다른 테이블의 한행으로부터 복사될 수 있는 데이터를 실제로 갖는다.

즉, tacos테이블의 ingredients열은 식재료 자체를 정의하는 클래스 인스턴스의 컬렉션을 포함한다.

Ingredeint클래스는 사용자 정의 타입으로 사용할수없다. 왜냐하면 `@Table`애노테이션이 이미 Ingredient 

클래스를 카산드라에 저장하는 엔티티로 매핑했기 때문이다. 따라서 taco테이블의 ingredients열에

식재료 데이터가 어떻게 저장되는지 정의하기 위해 새로운 클래스를 생성해야한다. 
```
@Data
@RequiredArgsConstructor
@NoArgsConstructor(access=AccessLevel.PRIVATE, force=true)
@UserDefinedType("ingredient")
public class IngredientUDT{
    private final String name;
    private final Ingredient.Type type;
}
```

IngredientUDT는 Ingredient 클래스와 매우 유사하지만, 엔티티에 매핑하는데 필요한 요구사항은 훨씬 간단하다.

이 애노테이션이 지정되지 않으면 몇가지 속성을 갖는 평범한 클래스가된다. 또한 IngredientUDT

클래스는 id속성을 포함하지않는다. 소스클래스인 Ingredient의 id속성을 가질 필요가 없기때문이다.

(사용자 정의 타입은 우리가 원하는 어떤 속성도 가질 수 있지만, 테이블 정의와 똑같지않아도된다.)

##### 외부 키와 조인을 사용하는 대신 카산드라 테이블은 비정규화되며,관련된 테이블로부터 복사된 데이터를 포함하는 사용자 정의타입을 갖는다.

![image](https://user-images.githubusercontent.com/40031858/108956708-7dfeb480-76b3-11eb-9b1f-fe3172c48523.png)

---

### Order클래스를 카산드라 tacoOrders테이블로 매핑하기
```
@Data
@Table("tacoorders")
public class Order implements Serializable{
    private static final long serialVersionUID= 1L;

    @PrimaryKey
    private UUID id=UUIDs.timeBased();

    private Date placedAt=new Date();

    @Column("user")
    private UserUDT user;

    @Column("tacos")
    private List<TacoUDT> tacos=new ArrayList<>();

    public void addDesign(TacoUDT design){
        this.tacos.add(design);
    }
}

```
여기서는 우선 `@Table`을 사용해 Order를 tacoorders테이블로 매핑한다. 그리고 id속성에는 `@PrimaryKey`애노테이션만

지정되었다. 이경우 파티션키와 클러스터링 키 모두로 이 속성이 사용된다는 것을 나타내며, 행의 순서는 기본값으로 설정된다.

또한 tacos속성은 List< Taco>대신 List< TacoUDT>로 정의되었고 tacos열에 저장된다. Order와 

Taco/TacoUDT간의 관계는 관계형 데이터베이스처럼 다른 테이블을 행들의 외부 키를 통해 조인하는 것이 아닌

주문된 모든 타코의 데이터를 tacoorders테이블에 포함시킨다. 빠른 데이터 검색에 테이블을 최적화하기위함이다.

이와 유사하게 user속성은 UserUDT로 정의되었고 user열에 저장된다. 즉, 주문한 사용자 데이터를 tacoorders테이블이 포함한다.
```
@Data
@UserDefinedType("taco")
public class TacoUDT{
    private final String name;
    private final List<IngredientUDT> ingredients;
}

@UserDefinedType("user")
@Data
public class UserUDT{
    private final String username;
    private final String fullname;
    private final String phoneNumber;
}
```

### 🌟 리액티브 카산드라 레포지토리 작성하기

리액티브 카산드라 레포지토리를 작성할 때는 두개의 기본 인터페이스인 `ReactiveCassandraRepository`나 

`ReactiveCrudRepository`를 선택할 수 있다. 둘 중 어떤 것을 선택하는 가는 어떻게 레포지토리를 사용하느냐에 달려있다. 

`ReactiveCassandraRepository`는 ReactiveCrudRepository를 확장하여 새 객체가 저장될 때 사용되는 insert()메소드의 몇 가지

변형 버전을 제공하며, 이외에는 동일한 메소드를 제공한다. 만일 데이터를 추가한다면 `ReactiveCassandraRepository`를

선택할 수 있으며 그렇지 않을때는`ReactiveCrudRepository`를 선택하는 것이 좋다.

이제 기존에 작성했던 `IngredientRepository`를 생각해보자. 이 인터페이스는 초기엠나 식재료 데이터를 

데이터베이스에 추가하는데 사용되며 이외에는 새로운 종류의 식재료를 거의 추가하지않는다.

따라서 `IngredientRepository`는 `ReactiveCrudRepository`를 확장하면 된다

```
public interface IngredientRepository extends ReactiveCrudRepository<Ingredient, String>{

}
```
이제 ReactiveCrudRepository를 확장하므로 IngredientRepository의 메소드들은 Flux나 Mono 타입을 처리한다.

예를들어, 이제는 findAll()메소드에서 Iterable< Ingredient>대신 Flux< Ingredient>를 반환한다.

예를들어, allIngredients()메소드는 다음과 같이 변경한다
```
@GetMapping
public Flux<Ingredient> allIngredients(){
    return repo.findAll();
}
```

TacoRepository인터페이스 변경은 약간 더 복잡한데 `PagingAndSortingRepository` 대신 `ReactiveCassandraRepository`를

확장해야한다. 그리고 제네릭 타입 매개변수로 Long타입의 ID속성을 갖는 Taco객체 대신, ID를 UUID속성으로

갖는 Taco객체를 사용해야한다
```
public interface TacoRepository extends ReactiveCrudRepository<Taco, UUID>{

}
```

따라서 PagingAndSortingRepository인터페이스의 확장이나결과 페이지의 처리에 관해 더 이상 신경쓰지 않아도 된다.

대신에 DesignTacoController의 recentTacos()메소드에는 자신이 반환하는 Flux에 take()를 호출해 결과의 한 페이지에

채울 Taco객체의 수를 제한해야한다.

마지막으로 UserRepository를 살펴보자. UserRepository는 커스텀 쿼리 메소드인 findByUsername()을 갖고있다.

리액티브 카산드라 레포지토리로 변경된 UserRepository인터페이스는 다음과같다
```
public interface UserRepository extends ReactiveCassandraRepository<User,UUID>{
    @AllowFiltering
    Mono<User> findByUsername(String username);
}
```

카산드라의 특성상 관계형 데이터베이스에서 SQL로 하듯이 테이블을 단순하게 where절로 쿼리할 수없다.

카산드라는 데이터 읽기에 최적화된다. 그러나 where절을 사용한 필터링 결과는 빠른 쿼리와는 달리

너무 느리게 처리될 수 있다. 그렇지만 결과가 하나 이상의 열로 필터링되는 테이블 쿼리에는 매우 유용하므로 where

절을 사용할 필요가 있다. 이때 `@AllowFiltering`애노테이션을 사용하면 된다.

`@AllowFiltering`을 지정하지 않은 findByUsername()의 경우 내부적으로 다음과 같이 쿼리가 수행될 것이라고 예상할 수있다.

    select * from users where username='검색할 사용자 이름';

다시말하지만 이처럼 단순한 where절은 카산드라에서 허용되지 않는다. 따라서 `@AllowFiltering`애노테이션을

findByUsername()에 지정하여 다음과 같은 쿼리가 내부적으로 수행되게 할 수 있다.

    select * from users where username='검색할 사용자 이름' allow filtering;

쿼리 끝의 allow filtering 절은 '쿼리 성능에 잠재적인 영향을 준다는 것을 알고있지만, 어쨋든 수행해야한다'.

라는것을 카산드라에게 알려주고 이경우 카산드라는 where절을 허용하고 결과 데이터를 필터링한다.

이렇게 카산드라에는 많은 기능이있고 스프링 데이터와 리액터를 카산드라와 같이 사용하면 그런 기능들을 스프링에 한껏 적용할 수 있다.

## 🍰 리액티브 몽고DB 레포지토리 작성하기

몽고DB는 잘알려진 NoSQL데이터 베이스 중 하나다. 카산드라가 테이블의 행으로 데이터를 저장하는 데이터베이스인 반면,

몽고DB는 문서형 데이터베이스다. 몽고DB는 BSON(Binary JSON)형식의 문서로 데이터를 저장하며,

다른 데이터베이스에서 데이터를 쿼리하는 것과 거의 유사한 방법으로 문서를 쿼리하거나 검색할 수 있다.

### 🌟 스프링 데이터 몽고DB활성화하기

리액티브 프로젝트를 할것이기에 리액티브 스프링 데이터 몽고DB스타터 의존성을 추가해야한다.
```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb-reactive</artifactId>
</dependency>
```

기본적으로 스프링 데이터 몽고DB는 몽고DB가 로컬로 실행되면서 27017포트를 리스닝하는 것으로 간주한다.

그러나 테스트와 개발에 편리하도록 내장된 몽고 DB를 대신사용할 수 있다. 이때는 다음과 같이 FlapDoodle 내장

몽고 DB의존성을 빌드에 추가하면된다.
```
<dependency>
    <groupId>de.flapdoodle.embed</groupId>
    <artifactId>de.flapdoodle.embed.mongo</artifactId>
</dependency>
```

Flapdoodle내장 인터페이스는 인메모리 몽고DB데이터베이스를 사용하는 것과 동일한 편의성을제공한다.

그러나 애플리케이션을 다시 시작하면 모든 데이터가 없어지고 데이터베이스가 초기화된다. 몽고 db설정은 다음과같다.
```
data:
  mongodb:
    host:  mongodb.tacocloud.com
    port: 27018
    username: tacocloud
    password: 1234
```
이 속성 모두 반드시 설정되어야 하는건 아니지만 각 속성내역은 다음과같다
- spring.data.mongodb.host:몽고DB서버가 실행중인 호스트의 이름이며, 기본값은 localhost다
- spring.data.mongodb.port:몽고DB서버가 리스닝하는 포트이며 , 기본값은 27017이다.
- spring.data.mongodb.username:몽고DB접근에 사용되는 사용자이름
- spring.data.mongodb.password:몽고DB접근에 사용되는 비밀번호
- spring.data.mongodb.database:데이터베이스 이름이며 기본값은 test다.

### 🌟 도메인 타입을 문서로 매핑하기

스프링데이터 몽고DB는 몽고DB에 저장되는 문서 구조로 도메인 타입을 매핑하는데 유용한 애노테이션들을 제공한다.

이런 애노테이션들이 6개 있지만, 그 중 3개만이 대부분의 경우에 유용하다.
- `@Id`: 이것이 지정된 속성을 문서 ID로 지정한다
- `@Document`: 이것이 지정된 도메인 타입을 몽고 DB에 저장되는 문서로 선언한다.
- `@Field`: 몽고DB의 문서에 속성을 저장하기 위해 필드이름을 지정한다.

이러한 세 개의 애노테이션 중 `@Id`와 `@Document`는 반드시 필요하다. 그리고 `@Field`가

지정되지 않은 도메인 타입의 속성들은 필드 이름과 속성이름을 같은 것으로 간주한다.

이 애노테이션들을 Ingredient클래스에 적용한 코드는 다음과 같다.
```
@Data
@RequiredArgsConstructor
@NoArgsConstructor(access=AccessLevel.PRIVATE,force=true)
@Document
public class Ingredient{
    @Id
    private final String id;
    private final String name;
    private final Type type;

    public static enum Type{
        WRAP, PROTEIN, VEGGIES, CHEESE, SAUCE
    }
}
```

여기서는 Ingredient가 몽고DB에 저장되거나 읽을 수 있는 문서엔티티라는 것을 나타내기 위해 클래스수준의 `@Document`애노테이션을

지정하였다. 기본적으로 컬렉션이름은 클래스이름과 같고 첫자만 소문자이다. 여기서는 컬렉션이름을 지정하지 않았으므로 Ingredient객체는 

ingredient라는 이름의 컬렉션에 저장된다. 그러나 다음과같이 `@Document`의 collection속성을 설정해 변경할수있다.

```
@Data @RequiredArgsConstructor
@NoArgsConstructor(access=AccessLevel.PRIVATE,force=true)
@Document(collection="ingredients")
public class Ingredient{
    ...
}
```

그리고 id속성에는 @Id가 지정되었다. 이것은 저장된 문서의 ID로 id속성을 지정한다.

String과 Long타입을 포함해서 Serializable타입인 어떤속성에도 @Id를 사용할 수 있다.

여기서는 id속성이 String 타입으로 지정되었으므로 @Id를 사용하기 위해 다른타입으로 변경할 필요는없다.

```
@Data
@RestResource(rel="tacos",path="tacos")
@Document
public class Taco{
    @Id
    private String id;

    @NotNull
    @Size(min=5,message="Name must be at least 5 characters long")
    private String name;

    private Date createdAt=new Date();

    @Size(min=1,message="You must choose at least 1 ingredient")
    private List<Ingredient> ingredients;
}
```

카산드라와 달리 몽고DB의 경우는 위처럼 Taco클래스 매핑이 훨씬 간단하다. 하지만 알아 둘것이있다.

id속성이 String으로 변경되었다. @Id는 어떤 Serializable타입에도 적용될 수 있으므로 Serializable인터페이스를

구현하는 또 다른 타입을 사용할 수있을것이다. 그러나 ID로 String타입의 속성을 사용하면 이 속성값이 데이터베이스에

저장될 때 몽고DB가 자동으로 ID값을 지정해준다(null일경우) 따라서 속성값의 설정을 걱정할필요없다.

그리고 ingredients속성을 보면 Ingredient객체를 저장한 컬렉션인 List< Ingredient>는 
JPA버전과 다르게

별도의 몽고DB 컬렉션에 저장되지 않으며, 카산드라 버전과 매우 유사하게 비정규화된 상태로 타코문서에 직접 저장한다.

그러나 `카산드라와는 다르게 몽고DB에서는 사용자 정의 타입을 만들 필요없이 어떤 타입도 사용할수있다.`

`@Document`가 지정된 또 다른 타입이나 단순한 POJO모두 가능하다. 이제 Order클래스르보자.

```
@Data
@Document
public class Order implements Serializable{
    private static final long serialVersionUID=1L;

    @Id
    private String id;

    private Date placedAt=new Date();

    @Filed("customer")
    private User user;

    //.. 생략

    private List<Taco> taocos=new ArrayList<>();

    public void addDesign(Taco design){
        this.tacos.add(design);
    }
}

```
이 역시 간단하다 그렇지만 user 속성에는 `@Field`를 지정하였다 customer열을 문서에 저장한다는 것을 나타내기위해서다.

### 🌟 리액티브 몽고DB 레포지토리 인터페이스 작성하기

스프링 데이터 몽고DB는 스프링 데이터 JPA및 스프링 데이터 카산드라가 제공하는것과 유사한 자동 레포지토리 지원을 제공한다.

몽고DB의 리액티브 레포지토리를 작성할때는`ReactiveCrudRepository`나 `ReactiveMongoRepository`를 선택할 수 있다.

둘의 차이는 ReactiveCrudRepository가 새로운 문서나 기존문서의 save()메소드에 의존하는 반면, `ReactiveMongoRepository`는

새로운 문서의 저장에 최적화된 소수의 특별한 insert()를 제공한다.

우선 Ingredient객체를 문서로 저장하는 레포지토리를 정의하는 것부터 시작해보자. 식재료를 저장한 문서는 초기에만 식자재

데이터를 데이터베이스에 추가할때 생성되고 이외에는 거의추가되지않는다.

따라서 새로운 문서의 저장에 최적화된`ReactiveMongoRepository`는 유용하지 않으므로 `ReactiveCrudRepository`를

확장하는 IngredientRepository를 작성하자

```
@CrossOrigin(origins="*")
public interface IngredientRepository extends ReactiveCrudRepository<Ingredient, String>{

}
```

이것은 이전에 작성했던 카산드라의 IngredientRepository 인터페이스와 똑같다!! 이것이 ReactiveCrudRepository인터페이스

를확장할때의 장점 중 하나다. 즉 다양한 데이터베이스 타입에 걸쳐 동일하므로 몽고DB나 카산드라의 경우에도 똑같이 사용된다.

이제 이 리액티브 레포지토리는 그냥 도메인타입이나 컬렉션이아닌 Flux나 Mono 타입으로  도메인객체를 처리한다.

예를들어 findAll()메소드는 Iterable< Ingredient>대신 Flux< Ingredient>를반환한다. 따라서 이 리액티브 레포지토리는 

엔드-to-엔드 리액티브 플로우의 일부가 될 수있다.

몽고DB의 문서로 Taco객체를 저장하는 레포지토리를 정의해보자 식재료문서와는 다르게 타코문서는 자주생성한다.

따라서 `ReactiveMongoRepository`의 최적화된 `insert()`메소드가 유용할 수 있다.
```
public interface TacoRepository extends ReactiveMongoRepository<Taco, String>{
    Flux<Taco> findByOrderByCreatedAtDesc();
}
```

`ReactiveCrudRepository`에 비해 `ReactiveMongoRepository`를 사용할 때의 유일한 단점은 몽고DB에

특화되어서 다른 데이터베이스에는 사용할 수 없다는것이다.

TacoRepository에는 새로운 메소드가 있다. 이 메소드는 최근 생성된 타코들의 리스트를 보여주는 것을 지원하기 위한 것이다.

이 레포지토리의 JPA버전에서는 PagingAndSortingRepository를 확장하였지만 이것은 리액티브 레포지토리에 적합하지않다. 

카산드라버전에서는 테이블 정의읭 클러스터링 키에 의해 어떻게 데이터를 정렬할지 결정한다. 따라서 최근

생성된 타코들을 가져오기 위해 레포지토리에서 특별히 할 것이없다.

그러나 몽고 DB의 경우는 최근 생성된 타코들을 레포지토리에서 가져올 수 있다. 이름이 특이하지만

`findByOrderByCreatedAtDesc()`메소드는 커스텀 쿼리 메소드의 명명규칙을따른다.

즉 Taco객체를 찾은 후 createdAt속성의 값을 기준 내림차순으로 결과를 정렬하라는것을 의미한다.

예를들어 최근생성된 타코들을 보여주는 컨트롤러에서 당음과같이 findByOrderByCreatedAtDesc()를 호출할수있다.
```
    Flux<Taco> recents=repo.findByOrderByCreatedAtDesc()
                    .take(12);
```

이 경우 결과로 생성되는 Flux는 12개의 Taco항목만 갖는다. 다음은 OrderRepository인터페이스를 알아보자.
```
public interface OrderRepository extends ReactiveMongoRepository<Order,String>{

}
```

Order문서는 자주 생성될 것이다. 따라서 OrderRepository는 insert()메소드로 제공되는 최적화의 장점을

얻기위해 ReactiveMongoRepository를 확장한다. 이외에는 다른레포지토리와 비교해 특별한것이없다.

마지막으로 User객체를 문서로 저장하는 레포지토리 인터페이스를 살펴보자.
```
public interface UserRepository extends ReactiveMongoRepository<User,String>{
    Mono<User> findByUsername(String username);
}
```

여기서는 User객체대신 Mono< User>를 반환하도록 변경되었고 그 외 특별한점은없다.

# ⚡12장 요약
### [1] 스프링 데이터는 카산드라, 몽고DB,카우치베이스, 레디스 데이터베이스의 리액티브 레포지토리를 지원한다

### [2] 스프링 데이터의 리액티브 레포지토리는 리액티브가 아닌 레포지토리와 동일한 프로그래밍 모델을 따른다.

### 단 , Flux나 Mono와 같은 리액티브 타입을 사용한다.

### [3] JPA레포지토리와 같은 리액티브가 아닌 레포지토리는 Mono나 Flux를 사용하도록 조정할 수 있다.

### 그러나 데이터를 가져오거나 저장할 때 여전히 블로킹이 생긴다.

### [4] 관계형이 아닌 데이터베이스를 사용하려면 해당 데이터베이스에서 데이터를 저장하는 

### 방법에 맞게 데이터를 모델링하는 방법을 알아야한다





