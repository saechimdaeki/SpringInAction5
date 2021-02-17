# 🥇3장 데이터로 작업하기

### ㅍ이 장에서 배우는 내용
- 스프링 JdbcTemplate 사용하기
- SimpleJdbcInsert를 사용해서 데이터 추가하기
- 스프링 데이터(Spring Data)를 사용해서 JPA 선언하고 사용하기

# 🥇3-1 JDBC 사용해서 데이터 읽고 쓰기.
[3-2 JPA로 데이터 읽고쓰기](https://github.com/saechimdaeki/Spring_InAction5/blob/main/chap3/3-2JPA%EC%82%AC%EC%9A%A9%ED%95%B4%20%EB%8D%B0%EC%9D%B4%ED%84%B0%20%EC%A0%80%EC%9E%A5%ED%95%98%EA%B3%A0%20%EC%82%AC%EC%9A%A9%ED%95%98%EA%B8%B0.md)

수십년간 관계형 데이터베이스와 SQL은 데이터 퍼시스턴스의 최우선 선택으로 자리를 지켜왔다.

관계형 데이터를 사용할 경우 자바 개발자들이 선택할 수 있는 몇가지 방법이 있다. 그 중 가장 많이 사용하는

두 가지 방법이 `JDBC`와 `JPA`다. 스프링은 이 두가지를 모두 지원하며 스프링을 사용하지 않을 때에 비해

더 쉽게 JDBC나 JPA를 사용할수 있게 해준다. 

스프링의 JDBC 지원은 `JdbcTemplate`클래스에 기반을 둔다 JdbcTemplate은 JDBC를 사용할 때

요구되는 모든 형식적이고 상투적인 코드없이 개발자가 관계형 데이터베이스에 대한 SQL연산을 수행할 수 있는 방법을제공한다.

### 🌟먼저 JdbcTemplate을 사용하지 않고 자바로 SQL쿼리를 수행하는 방법을 보자

```
@Override
public Ingredient findById(String id){
    Connection connection=null;
    PreparedStatement statement=null;
    ResultSet resultSet=null;
    try{
        connection=dataSource.getConnection();
        statement=connection.prepareStatement(
            "select id, name, type from Ingredient where id =?");
        statement.setString(1,id);
        resultSet=statement.executeQuery();
        Ingredient ingredient=null;
        if(resultSet.next()){
            ingredient=new Ingredient(
                resultSet.getString("id"),
                resultSet.getString("name"),
                Ingredient.Type.valueOf(resultSet.getString("type")));
        }
        return ingredient;
    }catch(SQLException e){
        //TODO
    }
    finally{
        if(resultSet!=null){
            try{
                resultSet.close();
            }catch(SQLException e){}
        }
        if(statement!=null){
            try{
                statement.close();
            }catch(SQLException e){}
        }
        if(connection !=null){
            try{
                connection.close();
            }catch(SQLException e){}
        }
    }
    return null;
}
```

이처럼 데이터베이스 연결생성, 명령문 생성, 그리고 연결과 명령문 및 결과세트를 닫고 클린업하는
코드들로 쿼리코드가 둘러싸여있다.

설상가상으로 연결이나 명령문 등의 객체를 생성할 때 또는 쿼리를 수행할 때 얼마든지 많은 일들이 잘못될 수 있다.

따라서 SQLException예외를 처리해야한다. 그러나 이것은 문제의 해결방법을 찾는데 도움이 될 수 있고 안될 수 있다.

SQLException은 catch 블록으로 반드시 처리해야한느 checked 예외이다. 그러나 데이터베이스 연결 생성 실패나

작성 오류가 있는 쿼리와 같은 흔한문제들은 catch블록에서 해결할 수 없으므로 현재 메소드를 상위코드로 예외처리를 넘겨야한다.

### 🌟이제 이것과 대조되는 `JdbcTemplate`사용 메소드를 보자
```
private JdbcTemplate jdbc;

@Override
public Ingredient findById(String id){
    return jdbc.queryForObject(
        "select id, name , type from Ingredient where id=?",
        this::mapRowToIngredient,id);
}

private Ingredient mapRowToIngredient(ResultSet rs, int rowNum) throws SQLException{
    return new Ingredeint(
        rs.getString("id"),
        rs.getString("name"),
        Ingredient.Type.valueOf(rs.getString("type")));
}
```

전의 코드보다 훨씬 간단해졌으며 명령문이나 데이터베이스 연결 객체를 생성하는 코드가 아예없다.

그리고 메소드의 실행이 끝난후 객체를 클린업 하는 코드 또한 없다.

또한 catch블록에서 올바르게 처리할 수 없는 예외를 처리하는 코드도 없다. 

오직 쿼리를 수행하고 그결과를 Ingredient객체로 생성하는 것에 초점을 두는 코드만 존재한다.

### 🌟JdbcTemplate 사용하기
`JdbcTemplate`사용을 위해 먼저 의존성을 추가해주자
```
    <dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-jdbc</artifactId>
	</dependency>
```

#### 🏭이제 JDBC레포지토리를 정의해보자
식자재 레포지터리는 다음 연산을 수행해야 한다.
- 데이터베이스의 모든 식자재 데이터를 쿼리하여 Ingredient객체 컬렉션에 넣어야한다
- id를 사용해서 하나의 Ingredient를 쿼리해야한다
- Ingredient 객체를 데이터베이스에 저장해야한다.

따라서 이 세가지 연산을 메소드로 정의해보자 
```
public interface IngredientRepository {
	Iterable<Ingredient> findAll();
	Ingredient findById(String id);
	Ingredient save(Ingredient ingredient);
}
```

#### 🏭Ingredient 레포지토리가 해야 할 일을 IngredientRepository인터페이스에 정의하였으므로
#### 🏭JdbcTemplate을 이용해서 z데이터베이스 쿼리에 사용할 수 있도록 IngredientRepository인터페이스를 구현해야한다.
```
@Repository
public class JdbcIngredientRepository {
	private JdbcTemplate jdbc;
	
	@Autowired
	public JdbcIngredientRepository(JdbcTemplate jdbc) {
		this.jdbc=jdbc;
	}
}
```

여기서 JdbcIngredientRepository 클래스에는 `@Repository` 애노테이션이 지정되었다.

이것은 @Controller와 @Component외에 스프링이 정의하는 몇안되는 스테레오타입 애노테이션중하나다.

즉 @Repositorty를 지정함으로써 스프링 컴포넌트 검색에서 이 클래스를 자동으로 찾아 애플리케이션컨텍스트의 빈으로 생성한다.

### 🌟이제 JdbcTemplate을 이용해 데이터베이스를 쿼리해보자.

```
@Repository
public class JdbcIngredientRepository implements IngredientRepository{
	
	private JdbcTemplate jdbc;
	
	@Autowired
	public JdbcIngredientRepository(JdbcTemplate jdbc) {
		this.jdbc=jdbc;
	}

	@Override
	public Iterable<Ingredient> findAll() {
		return jdbc.query("select id, name, type from Ingredient", this::mapRowToIngredient);
	}

	@Override
	public Ingredient findById(String id) {
		return jdbc.queryForObject("select id, name, type from Ingredient where id=?", this::mapRowToIngredient);
	}

	private Ingredient mapRowToIngredient(ResultSet rs,int rowNum) throws SQLException{
		return new Ingredient(
				rs.getString("id"),
				rs.getString("name"),
				Ingredient.Type.valueOf(rs.getString("type"))
				);
				
	}

	@Override
	public Ingredient save(Ingredient ingredient) {
		jdbc.update("insert into Ingredient (id, name, type) values (?, ?, ?)",
				ingredient.getId(),
				ingredient.getName(),
				ingredient.getType().toString());
		return ingredient;
	}
}

```

코드를 보면 객체가 저장된 컬렉션을 반환하는 findAll과 findById메소드는 JdbcTemplate의 query()메소드를 사용한다.

`query()`메소드는 두 개의 인자를 받는다. 첫 번째 인자는 쿼리를 수행하는 `SQL`이며, 두 번째 인자는 스프링의

`RowMapper`인터페이스를 우리가 구현한 mapRowToIngredient메소드다.

이 메소드는 쿼리로 생성된 결과 ResultSet의 행 개수만큼 호출되며, 결과 세트의 모든 행을 각각 객체로 생성하고

List에 저장한 후 반환한다.

#### 🏭데이터를 추가하는 save메소드를 보면 update()메소드를 사용하고 있다.

JdbcTemplate의 update()메소드는 데이터베이스에 데이터를 추가하거나 변경하는 어떤 쿼리에도 사용될 수 있다.

결과 resultSet의 데이터를 객체로 생성할 필요가 없으므로 query()나 queryForObject보다 간단하다.

이제 JdbcIngredientRepository가 완성되었으므로 지난 장까지 하드 코딩했던 코드를 다음처럼 수정하자
```
//DesignController.class
private final IngredientRepository ingredientRepo;
	
	@Autowired
	public DesignTacoController(IngredientRepository ingrdeintRepo) {
		this.ingredientRepo=ingrdeintRepo;
	}
	
	@GetMapping
	public String showDesignForm(Model model) {
		
		List<Ingredient> ingredients=new ArrayList<>();
		ingredientRepo.findAll().forEach(i -> ingredients.add(i));
		
		Type[] types=Ingredient.Type.values();
		for(Type type:types) {
			model.addAttribute(type.toString().toLowerCase(),filterByType(ingredients,type));
		}
		model.addAttribute("taco",new Taco());
		
		return "design";
				
	}
```
----

### 🌟스키마 정의하고 데이터 추가하기
Ingredient 테이블 외에도 주문정보와 타코 디자인정보를 저장할 테이블들이 필요하고 
다음처럼 테이블을 작성할예정이다.

![image](https://user-images.githubusercontent.com/40031858/107218594-29bcc900-6a53-11eb-86de-9a5fdd312a4a.png)

- Ingredient: 식자재 정보를 저장한다
- Taco: 사용자가 식자재를 선택하여 생성한 타코 디자인에 관한 정보를 저장한다
- Taco_Ingredients: Taco와 Ingredient테이블간의 관계를 나타내며 Taco테이블의 각 행에 하나 이상의 행을포함
- Taco_Order: 주문 정보를 저장
- Taco_Order_Tacos: Taco_Order와 Taco테이블간의 관계를 나타내며 Taco_Order테이블의 각행에 대해 하나이상 행포함.

### 🌟JdbcTemplate을 사용해서 데이터를 저장하는 방법은 다음 두 가지가 있다.
- 직접 update()메소드를 사용
- SimpleJdbcInsert 래퍼 클래스를 사용한다.

이제 Ingredient객체를 저장할 때보다 퍼시스턴스 처리가 더 복잡할 때는 어떻게 update()메소드를 사용하는지 보자.

```
public interface OrderRepository {
	Order save(Order order);
}

public interface TacoRepository {
	Taco save(Taco design);
}
```

이제 해당 Repository를 구현하려면 다음 일을 수행하는 save()를 구현해야한다.
이를 구현해보자.
```
@Repository
public class JdbcTacoRepository implements TacoRepository{
	
	private JdbcTemplate jdbc;
	
	public JdbcTacoRepository(JdbcTemplate jdbc) {
		this.jdbc=jdbc;
	}
	
	@Override
	public Taco save(Taco taco) {
		long tacoId=saveTacoInfo(taco);
		taco.setId(tacoId);
		for(Ingredient ingredient: taco.getIngredients()) {
			saveIngredientToTaco(ingredient,tacoId);
		}
		return taco;
	}
	
	private long saveTacoInfo(Taco taco) {
		taco.setCreatedAt(new Date());
		PreparedStatementCreator psc=new PreparedStatementCreatorFactory("insert into Taco (name ,createdAt) valjues (?, ?)",
				Types.VARCHAR,Types.TIMESTAMP).newPreparedStatementCreator(
						Arrays.asList(taco.getName(),
								new Timestamp(taco.getCreatedAt().getTime())));
		KeyHolder keyHolder=new GeneratedKeyHolder();
		jdbc.update(psc,keyHolder);
		return keyHolder.getKey().longValue();
	}
	
	private void saveIngredientToTaco(
			Ingredient ingredient, long tacoId) {
		jdbc.update("insert into Taco_Ingredients (taco, ingredient) values (?, ?)",
				tacoId,ingredient.getId());
	}
	
}
```

여기서 사용하는 update()메소드는 PreparedStatementCreator 객체와 KeyHolder 객체를 인자로 받는다.

생성된 타코 ID를 제공하는 것이 바로 이 KeyHolder다. 그러나 이것을 사용하기 위해서는 PreparedStatementCreator도 생성해야한다.

PreparedStatementCreator 객체의 생성은 간단하지 않다. 실행할 SQL과 각 쿼리 매개변수의 타입을 

인자로 전달해 PreparedStatementCreatorFactory객체를 생성하는 것으로 시작해 이객체의

newPreparedStatemnetCreator()를 호출하며 이때 PreparedStatementCreatorFactory를 생성하기 위해 쿼리매개변수의 값을 인자로 전달한다.

이렇게 하여 PreparedStatementCreatorFactory객체가 생성되면 이객체와 KeyHolder객체를 

인자로 전달하여update()를 호출할 수 있다. 그리고 update()가 끝나고 keyHolder.getKey().longValue()로 타코id를 반환할수있다.

---

### 🌟이제 이를 사용할 컨트롤러를 수정해보자
```
@Slf4j @Controller
@RequestMapping("/design")
@SessionAttributes("order")
public class DesignTacoController {
	
	private final IngredientRepository ingredientRepo;
	private TacoRepository tacoRepo;
	
	@Autowired
	public DesignTacoController(IngredientRepository ingrdeintRepo, TacoRepository tacoRepo) {
		this.tacoRepo=tacoRepo;
		this.ingredientRepo=ingrdeintRepo;
	}
	
	@ModelAttribute(name = "order")
	public Order order() {
		return new Order();
	}
	
	@ModelAttribute(name = "taco")
	public Taco taco() {
		return new Taco();
	}
	
	@GetMapping
	public String showDesignForm(Model model) {
		
		List<Ingredient> ingredients=new ArrayList<>();
		ingredientRepo.findAll().forEach(i -> ingredients.add(i));
		
		Type[] types=Ingredient.Type.values();
		for(Type type:types) {
			model.addAttribute(type.toString().toLowerCase(),filterByType(ingredients,type));
		}
		model.addAttribute("taco",new Taco());
		
		return "design";
				
	}
	private List<Ingredient> filterByType(List<Ingredient> ingredients,Type type){
		return ingredients.stream()
				.filter(x -> x.getType().equals(type))
				.collect(Collectors.toList());
	}
	
	@PostMapping
	public String processDesign(@Valid Taco design, Errors errors, @ModelAttribute Order order) {
		
		if(errors.hasErrors()) {
			return "design";
		}
		Taco saved=tacoRepo.save(design);
		order.addDesign(saved);
		return "redirect:/orders/current";
	}
	
}
```

수정한 컨트롤러 코드는 다음과 같다. @ModelAttribute애노테이션은 Order객체가 모델에 생성되도록 해준다.

### 🌟그러나 하나의 세션에서 생성되는 Taco객체와 다르게

주문은 다수의 HTTP요청에 걸쳐 존재해야 한다. 다수의 타코를 생성하고 그것들을 하나의 주문으로 추가할 수 있게 하기 위해서이다.

이때 클래스 수준의 `@SessionAttributes` 애노테이션을 주문과 같은 모델 객체에 지정하면 된다.

그러면 세션에서 계속 보존되면서 다수의 요청에 걸쳐 사용될 수 있다.

하나의 타코 디자인을 실제 저장하는일은 processDesign()메소드에서 수행되는데 Order매개변수에는 

`@ModelAttribute`애노테이션이 지정되었다. 이 매개변수의 값이 모델로부터 전달되어야 한다는 것과

스프링 MVC가 이 매개변수에 요청 매개변수를 바인딩하지 않아야 한다는 것을 나타내기 위해서이다.


---- 
## 💕SimpleJdbcInsert를 사용해 데이터 추가하기.

### 🌟앞서 복잡한 PreparedStatementCreator를 보았을것이다.

#### 🏭`SimpleJdbcInsert`는 데이터를 더 쉽게 테이블에 추가하기위해 JdbcTemplate을 래핑한 객체이다.
```
@Repository
public class JdbcOrderRepository implements OrderRepository{
	
	private SimpleJdbcInsert orderInserter;
	private SimpleJdbcInsert orderTacoInserter;
	private ObjectMapper objectMapper;
	
	@Autowired
	public JdbcOrderRepository(JdbcTemplate jdbc) {
		this.orderInserter=new SimpleJdbcInsert(jdbc)
				.withTableName("Taco_Order")
				.usingGeneratedKeyColumns("id");
		
		this.orderTacoInserter=new SimpleJdbcInsert(jdbc)
				.withTableName("Taco_Order_Tacos");
		
		this.objectMapper=new ObjectMapper();
	}
	
	@Override
	public Order save(Order order) {
		order.setPlacedAt(new Date());
		long orderId=saveOrderDetails(order);
		order.setId(orderId);
		List<Taco> tacos=order.getTacos();
		for(Taco taco : tacos) {
			saveTacoToOrder(taco,orderId);
		}
		return order;
	}
	
	private long saveOrderDetails(Order order) {
		@SuppressWarnings("unchecked")
		Map<String, Object> values= objectMapper.convertValue(order, Map.class);
		values.put("placedAt", order.getPlacedAt());
		
		long orderId=orderInserter.executeAndReturnKey(values)
				.longValue();
		return orderId;
	}
	
	private void saveTacoToOrder(Taco taco,long orderId) {
		Map<String, Object> values=new HashMap<>();
		values.put("tacoOrder", orderId);
		values.put("tack", taco.getId());
		orderTacoInserter.execute(values);
	}
}
```

보다시피 orderInserter 인스턴스 변수에 지정되는 첫 번째 SimpleJdbcInserter인스턴스는 Taco_Order

테이블에 주문 데이터를 추가하기 위해 구성되며, 이때 Order 객체의 id 속성 값은 데이터베이스가 생성해 주는 것을 사용한다.

`SimpleJdbcInsert`는 데이터를 추가하는 두개의 유용한 메소드인 execute()와 executeAndReturnKey()를 갖고있다.

두 메소드는 모두 Map< String, Object>를 인자로 받는다. 이 Map의 키는 데이터가 추가되는 테이블의

열(column)에 대응되며 값은 해당 열에 추가되는 값이다.

---

이제 마지막으로 데이터베이스 식자재 데이터를 Ingredient객체로 변환하자
```
@Component
public class IngredientByIdConverter implements Converter<String, Ingredient>{
	
	private IngredientRepository ingredientRepo;
	
	@Autowired
	public IngredientByIdConverter(IngredientRepository ingredientRepo) {
		this.ingredientRepo=ingredientRepo;
	}
	
	
	@Override
	public Ingredient convert(String id) {
		return ingredientRepo.findById(id);
	}

}
```

##### 🏭IngredientByIdConverter 클래스에는 @Component 애노테이션을 지정했으므로 스프링에 의해 자동 생성 및 주입되는 빈으로 생성된다

`Converter< String,Ingredient>`에서 String은 변환할 값의 타입이고 Ingredient는 변환된 값의 타입이다.

즉 convert메소드에서는 IngredientRepository 인터페이슬르 구현한 JdbcIngredientRepository

클래스 인스턴스의 findById()메소드를 호출하고 이메소드에서는 변환할 String값을 id로 갖는 데이터를 db에서찾는다.

그리고 IngredientRepository의 메소드인 mapRowToIngredient()메소드를 호출해 결과 세트의 행 데이터를

속성 값으로 갖는 Ingredient객체를 생성하고 반환한다.

---

### 🌟SimpleJdbcInsert와 더불어 스프링의 JdbcTemplate은 일반적인 JDBC보다

### 🌟훨 씬 더 쉽게 관계형 데이터 베이스를 사용하도록 해준다.

## 💕그러나 `스프링 데이터 JPA`는 더욱 쉽게 해준다는 것을 3-2.md에서보자!