# 2장 웹 애플리케이션 개발하기

## 이장에서 배우는 내용
- 모델 데이터를 브라우저에서 보여주기
- 폼 입력 처리하고 검사하기
- 뷰 템플릿 라이브러리 선택하기

### 해당 챕터는 1장의 프로젝트에 살을 덧붙이는 방식.

#### 타코 식자재 정의하기
```
@RequiredArgsConstructor
@Data
public class Ingredient {
	private final String id;
	private final String name;
	private final Type type;
	
	public static enum Type{
		WRAP, PROTEIN, VEGGINES, CHEESE, SAUCE
	}
}
```

위와 같이 롬복을 사용하려면 우리가 직접 pom.xml에 추가하면 된다.
```
		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<optional>true</optional>
		</dependency>
```
물론 ide의 힘을 빌리는게 더 편하다.

### 타코 디자인을 정의하는 도메인 객체
```
import java.util.List;

import lombok.Data;

@Data
public class Taco {
	private String name;
	private List<String> ingredients;
}
```

## 컨트롤러 클래스 생성하기
컨트롤러는 스프링 MVC 프레임워크의 중심적인 역할을 수행한다. 컨트로러는 HTTP요청을 처리하고,

브라우저에 보여줄 HTML을 뷰에 요청하거나 ,또는 REST형태의 응답 몸체에 직접 데이터를 추가한다.

이 장에서는 웹 브라우저의 콘텐츠를 생성하기 위해 뷰를 사용하는 컨트롤러에 초점을 두고 

REST API를 처리하는 컨트롤러 작성방법은 6장에서 다룰것이다.

#### 타코 클라우드 애플리케이션의 경우 다음일을 수행하는 간단한 컨트롤러가 필요하다.
- 요청 경로가 /design인 HTTP GET 요청을 처리한다.
- 식자재의 내역을 생성한다.
- 식자재 데이터의 HTML 작성을 뷰 템플릿에 요청하고 작성된 HTML을 웹 브라우저에 전송한다.

```
@Slf4j @Controller
@RequestMapping("/design")
public class DesignTacoController {
	
	@GetMapping
	public String showDesignForm(Model model) {
		List<Ingredient> ingredients=Arrays.asList(
				new Ingredient("FLTO", "Flour Tortilla", Type.WRAP),
				new Ingredient("COTO", "Corn Tortilla", Type.WRAP),
				new Ingredient("GRBF", "Ground Beef", Type.PROTEIN),
				new Ingredient("CARN", "Carnitas", Type.PROTEIN),
				new Ingredient("TMTO", "Diced Tomatoes", Type.VEGGINES),
				new Ingredient("LETC", "Lettuce", Type.VEGGINES),
				new Ingredient("CHED", "Cheddar", Type.CHEESE),
				new Ingredient("JACK", "Monterrey Jack", Type.CHEESE),
				new Ingredient("SLSA", "Salsa", Type.SAUCE),
				new Ingredient("SRCR", "Sour Cream", Type.SAUCE)
				);
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
	
}
```

DesignTacoController 클래스에서 첫번째로 주목할 것은 클래스 수준에 적용된 애노테이션 들이다.

#### 우선 @Slf4j는 컴파일 시에 Lombok에 제공되며, 이클래스에 자동으로 SLF4J Logger를 생성한다.

이 애노테이션은 다음 코드를 추가한 것과 같은 효과를 낸다
```
private static final org.slf4j.Logger log=
org.slf4j.LoggerFactory.getLogger(DesignTacoController.class);
```

그 다음 애노테이션은 @Controller이다.
#### @Controller애노테이션은 이 클래스가 컨트롤러로 식별되게 하며, 컴포넌트 검색을 해야한다는 것을 나타낸다.

따라서 스프링이 DesignTacoController 클래스를 찾은 후 스프링 애플리캐이션 컨텍스트의 빈으로 이클래스의 인스턴스를 자동생성한다.

#### 클래스 수준의 @RequestMapping과 함께 사용된 @GetMapping애노테이션은 /design의 HTTP GET요청이 
#### 수신될 때 그요청을 처리하기 위해 showDesignForm()메소드가 호출됨을 나타낸다.

## 스프링 MVC에서 사용할 수 있는 요청-대응 애노테이션들은 다음과 같다.

|애노테이션|설명|
|:-|:-:|
|@RequestMapping| 다목적 요청을 처리한다. |
|@GetMapping | HTTP GET 요청을 처리한다. |
|@PostMapping | HTTP POST 요청을 처리한다.|
|@PutMapping | HTTP PUT 요청을 처리한다.|
|@DeleteMapping | HTTP DELETE 요청을 처리한다.|
|@PatchMapping | HTTP PATCH 요청을 처리한다. |

이제 코드를 보면 식자재의 유형(고기,치즈,소스 등)을 List에서 필터링한 후 showDesignForm()의

인자로 전달되는 Model 객체의 속성으로 추가한다. 
### `Model`은 컨트롤러와 데이터를 보여주는 뷰 사이에서 데이터를 운반하는 객체이다.
궁극적으로 Model 객체의 속성에 있는 데이터는 뷰가 알수 있는 서블릿 요청 속성들로 복사된다.

showDesignForm()메소드는 제일 마지막에 "design"을 반환하는데 이것은 보델 데이터를 

브라우저에 나타내는 데 사용될 뷰의 논리적 이름이다.

---


## 뷰 디자인 하기
컨트롤러가 완성되었으므로 이제는 뷰를 만들 것이다. 뷰를 정의하는 여러가지 방법이 있지만 
Thymeleaf를 사용할 것이다.

Thymeleaf를 사용하려면 빌드 구성파일인 pom.xml에 의존성을 추가해야한다.
```
    <dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-thymeleaf</artifactId>
	</dependency>
```

이렇게 하면 스프링부트의 자동-구성에서 런타임시 classpath의 Thymeleaf를 찾아 빈을 자동으로 생성한다.

Thymeleaf와 같은 뷰 라이브러리들은 어떤 웹 프레임워크와도 사용 가능하도록 설계되었다.

따라서 스프링의 추상화 모델을 알지 못하며, 컨트롤러가 데이터를 넣는 `Model`대신 서블릿 요청 속성들을 사용한다.

그러므로 그런 뷰에게 요청을 전달하기 앞서 스프링은 Thymeleaf와 이외의 다른 뷰 템플릿이 사용하는 요청 속성에 모델 데이터를 복사한다.

Thymeleaf템플릿은 요청 데이터를 나타내는 요소 속성을 추가로 갖는 HTML이다. 예를들어 key가 "message"인

요청 속성이 있고, 이것을 Thymeleaf를 사용해 HTML< p> 태그로 나타내고자 했다면 이렇게 작성했을것이다.
```
<p th:text="${message}> placeholer message</p>
```
이 경우 템플릿이 HTML로 표현될 때 < p>요소의 몸체는 키가"message"인 서블릿 요청 속성의 값으로 교체된다. 

th:text는 교체를 수행하는 Thymeleaf네임스페이스 속성이다. ${}연산자는 요청속성의 값을 사용하라는 것을 알려준다.


Thymeleaf는 또한 다른속성으로 `th:each`를 제공한다. 이속성은 컬렉션을 반복처리하며, 해당 컬렉션의 각요소를 하나씩HTML로 나타낸다.

이제 타코 디자인 페이지를 보자.
```
design.html의 내용(후에 추가할 error폼까지 현재 담겨있음)


<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:th="http://www.thymeleaf.org">
  <head>
<meta charset="EUC-KR">
    <title>Taco Cloud</title>
    <link rel="stylesheet" th:href="@{/styles.css}" />
  </head>

  <body>
    <h1>Design your taco!</h1>
    <img th:src="@{/images/TacoCloud.png}"/>

    <form method="POST" th:object="${taco}">
    
    <span class="validationError"
          th:if="${#fields.hasErrors('ingredients')}"
          th:errors="*{ingredients}">Ingredient Error</span>
          
    <div class="grid">
      <div class="ingredient-group" id="wraps">
      <h3>Designate your wrap:</h3>
      <div th:each="ingredient : ${wrap}">
        <input name="ingredients" type="checkbox" th:value="${ingredient.id}"/>
        <span th:text="${ingredient.name}">INGREDIENT</span><br/>
      </div>
      </div>

      <div class="ingredient-group" id="proteins">
      <h3>Pick your protein:</h3>
      <div th:each="ingredient : ${protein}">
        <input name="ingredients" type="checkbox" th:value="${ingredient.id}"/>
        <span th:text="${ingredient.name}">INGREDIENT</span><br/>
      </div>
      </div>

      <div class="ingredient-group" id="cheeses">
      <h3>Choose your cheese:</h3>
      <div th:each="ingredient : ${cheese}">
        <input name="ingredients" type="checkbox" th:value="${ingredient.id}"/>
        <span th:text="${ingredient.name}">INGREDIENT</span><br/>
      </div>
      </div>

      <div class="ingredient-group" id="veggies">
      <h3>Determine your veggies:</h3>
      <div th:each="ingredient : ${veggies}">
        <input name="ingredients" type="checkbox" th:value="${ingredient.id}"/>
        <span th:text="${ingredient.name}">INGREDIENT</span><br/>
      </div>
      </div>

      <div class="ingredient-group" id="sauces">
      <h3>Select your sauce:</h3>
      <div th:each="ingredient : ${sauce}">
        <input name="ingredients" type="checkbox" th:value="${ingredient.id}"/>
        <span th:text="${ingredient.name}">INGREDIENT</span><br/>
      </div>
      </div>
      </div>

      <div>

      <h3>Name your taco creation:</h3>
      <input type="text" th:field="*{name}"/>
      <span th:text="${#fields.hasErrors('name')}">XXX</span>
      <span class="validationError"
            th:if="${#fields.hasErrors('name')}"
            th:errors="*{name}">Name Error</span>
      <br/>

      <button>Submit your taco</button>
      </div>
    </form>
  </body>
</html>
```


위의 design.html의 < form> 태그를 보면 method속성이 post로 설정되어 있는데도
< form>에는 action속성이

선언되지 않은 것을 알 수 있다. 이경우 폼이 제출되면 브라우저가 폼의 모든 데이터를 모아서 폼에 나타난 GET 요청과

같은 경로(/design)로 서버에 HTTP POST요청을 전송한다. 따라서 이요청을 처리하는 컨트롤러의 메소드가 있어야한다.

```
DesignTacoController에 추가.

@PostMapping
	public String processDesign(Taco design) {
		//이 지점에서 타코 디자인 (선택된 식자재 내역)을 저장한다.
		// 이작업은 3장에서 할 것이다.
		log.info("Processing design:"+design);
		return "redirect:/orders/current";
	}
```

이렇게 함으로써 타코를 생성한 사용자는 자신들의 타코를 받기 위해 주문을 처리하는 폼으로 접속할 수 있다.

그러나 아직 /orders/current경로의 요청을 처리할 컨트롤러가 없으므로 이를 만들어준다.
```
@Slf4j @Controller
@RequestMapping("/orders")
public class OrderController {
	
	@GetMapping("/current")
	public String orderForm(Model model) {
		model.addAttribute("order",new Order());
		return "orderForm";
	}
	
	@PostMapping
	public String processOrder(Order order) {
		log.info("Order submitted: " + order);
		return "redirect:/";
	}
}

@Data
public class Order {
	private String deliveryName;
	private String deliveryStreet;
	private String deliveryCity;
	private String deliveryState;
	private String deliveryZip;
	private String ccNumber;
	private String ccExpiration;
	private String ccCVV;
}
```

### 폼 입력 유효성 검사하기

식자재 선택을 하지않거나 주소를 입력하지 않거나 현재로는 사용자의 타코 생성을 막을 방법이 없다.

그런 필드들의 유효성 검사를 하는 방법을 아직 지정하지 않았기 때문이다.

폼의 유효성 검사를 하는 한가지 방법은 if/then블록을 너저분하게 추가하는 것이 있지만 너무 번거롭고 디버깅이 어렵다.

### 다행스럽게도 스프링은 자바의 빈 유효성검사`(Bean Validation)` API를 지원한다.

이것을 사용하면 어플리케이션에 추카코드를 작성하지 않고 유효성 검사 규칙을 쉽게 선언할 수 있다.

스프링 MVC에 유효성 검사를 적용하려면 다음과 같이 해야한다.
- 유효성 검사할 클래스에 검사 규칙을 선언한다.
- 유효성 검사를 하는 컨트롤러 메소드에 검사를 수행한다는것을 지정한다.
- 검사 에러를 보여주도록 폼 뷰를 수정한다.

validation을 위해 다음을 pom.xml에 추가하자
```
<dependency>
	<groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

validation을 추가후 Order를 다음과 같이 변경할 수 있다.
```
@Data
public class Order {
	
	@NotBlank(message = "Name is required")
	private String deliveryName;
	
	@NotBlank(message = "Street is required")
	private String deliveryStreet;
	
	@NotBlank(message = "City is required")
	private String deliveryCity;
	
	@NotBlank(message = "State is required")
	private String deliveryState;
	
	@NotBlank(message = "Not a valid credit card number")
	private String deliveryZip;
	
	@CreditCardNumber(message = "Not a valid credit card number")
	private String ccNumber;
	
	@Pattern(regexp="^(0[1-9]|1[0-2])([\\/])([1-9][0-9])$",
	           message="Must be formatted MM/YY")
	private String ccExpiration;
	
	@Digits(integer=3, fraction=0, message="Invalid CVV")
	private String ccCVV;
}
```

보다시피 ccNumberthrtjddls @CreditCardNumber애노테이션은 Luhn알고리즘 검사에 합격한 

유효한 신용 카드 번호이어야 한다는 것을 선언함으로써 악성 데이터를 방지한다.

이제 제출된 Order의 유효성 검사는 아래와 같이 할 수 있다.
```
@PostMapping
	public String processOrder(@Valid Order order, Errors errors) {
		if(errors.hasErrors()) {
			return "orderForm";
		}
		log.info("Order submitted: " + order);
		return "redirect:/";
	}
```

### 뷰 컨트롤러로 작업하기

지금까지 타코 클라우드 어플리케이션 세가지 컨트롤러를 작성하였다. 각 컨트롤러는 다른기능을 제공하지만
프로그래밍 패턴은 다음과 동일하다.

[1] 스프링 컴포넌트 검색에서 자동으로 찾은 후 스프링 애플리케이션 컨텍스트의 빈으로 생성되는 컨트롤러 클래스임을

나타내기 위해 그것들 모두 @Controller애노테이션을 사용한다.

[2] HomeController외의 다른 컨트롤러에서는 자신이 처리하는 요청 패턴을 정의하기위해 클래스 수준의 

@RequestMapping애노테이션을 사용한다.

[3] 메소드에서 어떤 종류의 요청을 처리해야 하는지 나타내기 위해 @GetMapping또는 @PostMapping

애노테이션이 지정된 하나 이상의 메소드를 가짐

---

우리가 작성하는 대부분 컨트롤러는 지금 애기한 패턴을 따르지만 모델 데이터나 사용자 입력을 처리하지 않는

간단한 컨트롤러의 경우는 다른 방법으로 컨트롤러를 정의할 수 있다. 이를 알아보자
```
public class WebConfig implements WebMvcConfigurer{
	
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/").setViewName("home");
	}

}
```

WebConfig는 뷰 컨트롤러의 역할을 수행하는 구성 클래스이며, 가장 중요한 것은 `WebMvcConfigurer` 인터페이스를 구현한다는 것이다.

WebMvcConfigurer인터페이스는 스프링 MVC를 구성하는 메소드를 정의하고 있다. 그리고 인터페이스임에도 정의된 모든 메소드의 기본구현을 제공한다.

여기서 addViewControllers()메소드는 하나 이상의 뷰컨트롤러를 등록하기 위해 사용할 수 있는 ViewControllerRegistry를 인자로받는다.

이렇게 함으로써 구성클래스의 몇 줄안되는 코드로 HomeController를 대체할 수 있다. 


# 2장 요약!
#### [1] 스프링은 스프링 MVC라는 강력한 웹 프레임워크를 제공하는데, 스프링 MVC는 
#### 스프링 애플리케이션의 웹 프론트엔드 개발에 사용한다.

#### [2] 스프링 MVC는 애노테이션 기반으로 하며, @RequestMapping, @GetMapping, @PostMapping
#### 과 같은 애노테이션을 사용해서 요청 처리 메소드를 선언할 수 있다.

#### [3] 대부분의 요청 처리 메소드들은 마지막에 Thymeleaf템플릿과 같은 논리 뷰 이름을 반환한다.
#### 모델 데이터와 함께 해당 요청을 전달하기 위해서이다.

#### [4] 스프링 MVC는 자바 빈 유효성 검사 API와 Hibernate Validator등의 유효성 검사 API 구현
#### 컴포넌트를 통해 유효성 검사를 지원한다.

#### [5] 모델 데이터가 없거나 처리할 필요가 없는 HTTP GET요청을 처리할 때는 뷰 컨트롤러를 사용할 수 있다.

#### [6] Thymleaf에 추가하여 스프링은 다양한 뷰 템플릿을 지원한다.