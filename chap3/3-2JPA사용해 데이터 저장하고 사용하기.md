# 🥇3-2 JPA를 사용해 데이터 저장하고 사용하기

### 🌟스프링 데이터  프로젝트는 여러 개의 하위 프로젝트로 구성되는 다소 규모가 큰 프로젝트이다.

### 🌟가장 많이 알려진 스프링 데이터 프로젝트들은 다음과 같다

- 스프링 데이터 JPA: 관계형 데이터베이스의 JPA 퍼시스턴스
- 스프링 데이터 MongoDB: 몽고 문서형 데이터베이스의 퍼시스턴스
- 스프링 데이터 Neo4: Neo4그래프 데이터베이스의 퍼시스턴스
- 스프링 데이터 레디스: 레디스 키-값 스토어의 퍼시스턴스
- 스프링 데이터 카산드라: 카산드라 데이터베이스의 퍼시스턴스

스프링 데이터에서는 레퍼지토리 인터페이스를 기반으로 이 인터페이스를 구현하는 레포지토리를 자동생성해준다.

## 💕스프링 데이터 JPA를 프로젝트에 추가하려면 다음처럼 pom.xml에 추가하자.

```
    <dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-data-jpa</artifactId>
	</dependency>
```

그러면 이제 도메인 객체에 애노테이션을 추가해보자.

```

==== Ingredient.java=======
@RequiredArgsConstructor
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE,force = true)
@Entity
public class Ingredient {
	
	@Id
	private final String id;
	private final String name;
	private final Type type;
	
	public static enum Type{
		WRAP, PROTEIN, VEGGINES, CHEESE, SAUCE
	}
}
```

### 🌟Ingredient를 JPA개체로 선언하려면 반드시 `@Entity`애노테이션을 추가해야한다.

또 이것의 id속성에는 반드시 `@Id`를 지정하여 이속성이 데이터베이스 개체를 고유하게 식별한다는 것을 나타내야한다.

JPA애노테이션과 더불어 클래스 수준의 `@NoArgsConstructor`애노테이션이 추가되었는데
### 🌟JPA에서는 개체가 없는 생성자를 무조건 가져야 하므로 Lombok을이용해 지정한 것이다.
(하지만 인자 없는 생성자의 사용을 원치않으므로 access레벨을 private로 설정하였다.)


마찬가지로 Taco클래스도 이처럼 해보자
```
@Data
@Entity
public class Taco {
	
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private Date createdAt;
	@NotNull
	@Size(min=5, message="Name must be at least 5 characters long")
	private String name;
	
	@ManyToMany(targetEntity = Ingredient.class)
	@Size(min = 1, message = "You must choose at least 1 ingredient")
	private List<Ingredient> ingredients;
	
	@PrePersist
	void createdAt() {
		this.createdAt=new Date();
	}
}
```

Taco 및 이것과 연관된 Ingredient들 간의 관계를 선언하기 위해 `@ManyToMany` 애노테이션이 지정되었다. 

하나의 Taco 객체는 많은 Ingredient객체를 가질 수 있는데 하나의 Ingredient는 여러 Taco객체에 포함될 수 있기때문이다.

또한 `@PrePersist`애노테이션이 메소드에 지정되어 있는데 이 메소드는 Taco객체가 저장되기전에 실행된다.

### 🌟JPA 레포지토리 선언하기

JDBC 버전의 레포지토리는 레포지토리가 제공하는 메소드를 우리가 명시적으로 선언했었다.

하지만 스프링데이터에서는 그 대신 CrudRepository인터페이스를 확장할 수 있다. 코드를보자.
```
public interface IngredientRepository extends CrudRepository<Ingredient, String> {
	
}
```
기존에 작성했던 jdbc 인터페이스를 없애었다.

`CrudRepository`인터페이스에는 데이터베이스의 CRUD 연산을 위한 많은 메소드가 선언되어있다.

CrudRepository는 매개변수화 타입이며 첫번째 매개변수는 레포지토리에 저장되는 개체 타입, 두번째는 개체 ID속성 타입이다.


### 🌟이제 테스트를위해 부트스트랩 클래스를 다음과 같이 변경하자
```
@SpringBootApplication
public class TacoCloudApplication {

	public static void main(String[] args) {
		SpringApplication.run(TacoCloudApplication.class, args);
	}
	
	@Bean
	public CommandLineRunner dataLoader(IngredientRepository repo) {
		return new CommandLineRunner() {
			
			@Override
			public void run(String... args) throws Exception {
				
				repo.save(new Ingredient("FLTO", "Flour Tortilla", Type.WRAP));
				repo.save(new Ingredient("COTO", "Corn Tortilla", Type.WRAP));
				repo.save(new Ingredient("GRBF", "Ground Beef", Type.PROTEIN));
				repo.save(new Ingredient("CARN", "Carnitas", Type.PROTEIN));
				repo.save(new Ingredient("TMTO", "Diced Tomatoes", Type.VEGGINES));
				repo.save(new Ingredient("LETC", "Lettuce", Type.VEGGINES));
				repo.save(new Ingredient("CHED", "Cheddar", Type.CHEESE));
				repo.save(new Ingredient("JACK", "Monterrey Jack", Type.CHEESE));
				repo.save(	new Ingredient("SLSA", "Salsa", Type.SAUCE));
				repo.save(new Ingredient("SRCR", "Sour Cream", Type.SAUCE));
				
			}
		};
	}
}
```

부트스트랩 클래스를 변경한 이유는 애플리케이션이 시작되면서 호출되는 dataLoader에서 데이터베이스에 미리 

저장할 필요가 있기 때문이다. 또한 converter클래스의 convert메소드를 다음과 같이 변경하자.
```
@Override
	public Ingredient convert(String id) {
		Optional<Ingredient> optionalIngredient=ingredientRepo.findById(id);
		return optionalIngredient.isPresent() ? 
				optionalIngredient.get():null;
	}
```

이처럼 스프링 데이터 JPA의 CrudRepository에서 제공하는 메소드들은 범용적인 데이터저장에 훌륭하다.

하지만 기본적인 데이터 저장이상의 요구사항이 있다면?? 어떻게될까

### 🌟JPA레포티토리 커스터마이징 하기 

CrudRepository에서 제공하는 기본적인 CRUD연산에 추가하여, 특정 zip코드로 배달된 모든 주문

데이터도 데이터베이스에서 가져와야한다고 하자. 이것은 다음과 같이 OrderRepository에 메소드를 선언하면된다
```
List<Order> findByDeliveryZip(String deliveryZip);
```

레포지토리 구현체를 생성할 때 스프링 데이터는 해당 레포지토리 인터페이스에 정의된 메소드를 찾아 메소드

이름을 분석하며, 저장되는 객체의 컨텍스트에서 메소드의 용도가 무엇인지 파악한다.

본질적으로 스프링데이터는 일종의 `DSL(Domain Specific Language)`를 정의하고 있어서

퍼시스턴스에 관한내용이 레포지토리 메소드의 시그니처에 표현된다.

이렇게하면 스프링 데이터는 findByDeliveryZip()메소드가 주문객체(Order)들을 찾으려 한다는 것을 안다.

왜냐면 매개변수를 Order로 지정했기 때문이다. 그리고 메소드 이름인 findByDeliveryZip()은 이 메소드가 

Order의 DeliveryZip속성과 일치하는 모든 개체를 찾아야한다는 것을 확실하게 판단하도록 해준다.

이처럼 매우간단한것은 물론 더복잡한 이름도 처리할 수 있다.

더 복잡한 예를 생각해보자. `지정된 일자 범위 내에서 특정 Zip코드로 배달된 모든 주문`을 쿼리하자.

이경우는 다음 메소드를 OrderRepository에 추가하면 된다.

```
List<Order> readOrderByDeliveryZipAndPlacedAtBetween(
    String deliveryZip, Date startDate, Date endDate
);
```

![image](https://user-images.githubusercontent.com/40031858/107237557-36005080-6a6a-11eb-9860-ba7c720a301f.png)


묵시적으로 수행되는 Equals와 Between 연산에 추가하여 스프링 데이터 메소드 시그니처에는 다음연산자 중 어느것도 포함될수있다.
- IsAfter, After, IsGreaterThan, GreaterThan
- IsGreaterThanEqual, GreaterThanEqual
- IsBefore, Before , IsLessThan, LessThan
- IsLessThanEqual, LessThanEqual
- IsBetween, Between
- IsNull, Null
- IsNotNull, NotNull
- IsIn, In
- IsNotIn, NotIn
- IsStartingWith, StartingWith, StartsWith
- IsEndingWith, EndingWith, EndsWith
- IsContaining, Containg, Contains
- IsLike, Like
- IsNotLike, NotLike
- IsTrue, True
- IsFalse, False
- Is, Equals
- IsNot, Not
- IgnoringCase, IgnoresCase

모든 String 비교에서 대소문자를 무시하기 위해 IgnoringCase와 IgnoresCase 대신
AllIgnoringCase 

또는 AllIgnoresCase를 메소드 이름으로 사용할 수 있다. 예를들면 다음과 같다.

```
List<Order> findByDeliveryToAndDeliveryCityAllIgnoresCase(
    String deliveryTo,String deliveryCity
)
```

마지막으로 지정된 열의 값을 기준으로 정렬하기 위해 메소드 이름의 끝에 orderby를 추가할 수 도 있다.

```
deliveryTo속성값을 기준으로 정렬하는 예시
List<Order> findByDeliveryCityOrderByDeliveryTo(String city);
```

이름규칙은 비교적 간단한 쿼리에서는 유용할 수 있다. 그러나 더 복잡한 쿼리의 경우에는 메소드 이름만으로 감당하기어렵다.

이때는 어떤 이름이든 원하는것을 지정후 해당 메소드가 호출될때 `@Query`애노테이션을 지정하자.
```
@Query("Order o where o.deliveryCity='Seattle'")
List<Order> readOrdersDeliveredInSeattle();
```

이렇게 `@Query`를 사용하면 시애틀에 배달된 모든 주문을 요청할 수 있다. 우리가 생각하는 어떤 쿼리를 수행할 때에도

@Query를 사용할 수 있다 심지어는 이름규칙을 준수하여 쿼리 수행어렵거나 불가능 할때도 가능하다.

# 🥇3장 요약
- 스프링의 JdbcTemplate은 JDBC작업을 굉장히 쉽게 해준다
- 데이터베이스가 생성해주는 ID값을 알아야할때는 PreparedStatementCreater와 KeyHoler를 함께사용할수있다.
- 데이터 추가를 쉽게 실행할때는 SimpleJdbcInsert를 사용하자
- 스프링 데이터 JPA는 레포지토리 인터페이스를 작성하듯이 JPA퍼시스턴스를 쉽게해준다

