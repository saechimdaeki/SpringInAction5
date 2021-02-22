# 🥇1장 스프링 시작하기

## 💕스프링이란?

### 🌟스프링은 스프링 애플리케이션 컨텍스트라는 컨테이너를 제공하는데 이것이 어플리케이션 컴포넌트들을 생성하고 관리한다.
### 🌟그리고 애플리케이션 컴포넌트 또는 `빈`들은 스프링 애플리케이션 컨텍스트 내부에서 서로 연결되어 완전한 애플리케이션을 만든다.

### 🌟빈의 상호 연결은 `의존성 주입(DI)`라고 알려진 패턴을 기반으로 수행된다.
즉, 애플리케이션 컴포넌트에서 의존하는 다른 빈의 생성과 관리를 자체적으로 하는 대신 별도의 컨테이너가 해주며,
이 개체에서는 모든 컴포넌트를 생성,관리하고 해당 컴포넌트를 필요로 하는 빈에 주입한다.
일반적으로 이것은 생성자 인자 또는 속성의 접근자 메소드를 통해 처리된다.

아래의 그림은 재고서비스와 제품서비스 컴포넌트 빈과 스프링 애플리케이션 컨텍스트간 관계를 보여준다.

![스프링 애플리케이션 컨텍스트](https://user-images.githubusercontent.com/40031858/107111499-dea88780-6893-11eb-9ecc-1f5e2496861a.png)

### 🌟이처럼 애플리케이션 컴포넌트는 스프링 애플리케이션 컨텍스트에 의해 관리되고 상호 주입된다.

최신 버전의 스프링은 자바 기반의 configuration을 사용하며 다음과 같이 사용된다.
```
@Configuration
public class ServiceConfiguration{
    @Bean
    public InventoryService inventoryService(){
        return new InventoryService();
    }
    @Bean
    public ProductService productService(){
        return new ProductService(inventoryService());
    }
}
```

### 🌟여기서 @Configuration 애노테이션은 이것이 각 빈을 스프링 애플리케이션 컨텍스트에 제공하는 구성 클래스라는 것을 스프링에게 알려준다

구성 클래스의 메소드에는 @Bean 애노테이션이 지정되어 있으며, 이것은 각 메소드에서 반환되는 객체가 애플리케이션 컨텍스트의 빈으로 추가되어야 한다는 것을 나타냄.


---
## 💕taco프로젝트 구조.

해당 폴더의 프로젝트에 있어서 Spring Web, Thymeleaf, Test 의존성 항목은 < artifactId> 에 starter 단어를 포함하고 있음을 알 수 있다.
이것은 스프링 부트 스타터 의존성을 나타낸다. 이 의존성 항목들은 자체적으로 라이브러리 코드를 갖지않고 다른 라이브러리의 것을 사용한다.
스타터 의존성은 다음 세가지 장점이 있다.
- 우리가 필요로 하는 모든 라이브러리의 의존성을 선언하지 않아도 되므로 빌드 파일이 작아지고 관리하기 쉬워짐
- 라이브러리 이름이 아닌 기능의 관점으로 의존성을 생각할 수 있다
- 라이브러리들의 버전을 걱정하지 않아도 된다. (스프링 부트의 버젼만 신경쓰면 된다.)


[애플리케이션의 부트스태랩(구동)]
TacoCloudApplication.java는 다음과 같다.
```
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication    <------ 스프링 부트 애플리케이션
public class TacoCloudApplication {
	public static void main(String[] args) {
		SpringApplication.run(TacoCloudApplication.class, args);
	}
}
```
### 🌟@SpringBootApplication은 다음 세개의 애노테이션이 결합한 것이다
- @SpringBootConfiguration: 현재 클래스를 구성 클래스로 지정한다. 필요하다면 자바 기반의 스프링 프레임워크
구성을 현재 클래스에 추가할 수 있다. 이 애노테이션이 @Configuratiion 애노테이션의 특화된 형태이다.

- @EnableAutoConfiguration: 스프링 부트 자동-구성을 활성화한다.
- @ComponentScan: 컴포넌트 검색을 활성화 한다. 이것은 @Component,@Conmtroller, @Service등의 애노테이션과 함께 클래스를 선언할 수 있게 해준다.
그러면 스프링은 자동으로 그런 클래스를 찾아 어플리케이션컨텍스트에 컴포넌트로 등록한다. 

### 🌟먼저 홈페이지컨트롤러 코드는 다음과 같다.
```
@Controller
public class HomeController {
	@GetMapping("/")
	public String home() {
		return "home";
	}
}  
```
### 🌟home.html은 다음과 같다
```
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
xmlns:th="http://www.thymeleaf.org">
<head>
<meta charset="UTF-8">
<title>Taco Cloud</title>
</head>
<body>
<h1>Welcome to...</h1>
<img th:src="@{/images/TacoCloud.png}"/>
</body>
</html>
```

### 🌟그러면 이제 컨트롤러를 테스트 해보자.

```
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
@WebMvcTest(HomeController.class)
class HomeControllerTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@Test
	public void testHomePage() throws Exception{
		mockMvc.perform(get("/"))
		.andExpect(status().isOk())
		.andExpect(view().name("home"))
		.andExpect(content().string(containsString("Welcome to...")));
	}
}
```

@WebMvcTest는 스프링 부트에서 제공하는 특별한 테스트 애노테이션이며 스프링 MVC 애플리케이션의 형태로 테스트가 실행되도록 한다.
즉, HomeController가 스프링 MVC에 등록되므로 우리가 스프링 MVC에 웹 요청을 보낼 수 있다.

testHomePage()메소드에는 홈페이지에 대해 수행하고자 하는 테스트를 정의한다. 우선 루트 경로인 /의 HTTP GET 요청을 MockMvc 객체로 수행한다.
그리고 우리가 기대하는 것 exception을 다음과 같이 설정한다.
- 응답은 반드시 HTTP 200(OK)상태가 되어야 한다.
- 뷰의 이름은 반드시 home이어야한다.
- 브라우저에 보이는 뷰에는 반드시 'Welcoe to...'텍스트가 포함되어야 한다.
세가지 중 하나라도 충족하지 않으면 테스트는 실패한다.!


---
## 💕스프링 부트 DevTools 알아보기
#### 🏭이름에서 암시하듯 DevTools는 스프링 개발자에게 다음과 같은 개발시점의 편리한 도구를 제공한다.
- 코드가 변경될 때 자동으로 애플리케이션 다시 시작
- 브라우저로 전송되는 리소스가 변경될 때 자동으로 브라우저를 새로고침한다
- 템플릿 캐시를 자동으로 비활성화한다
- 만일 H2 데이터베이스가 사용 중이라면 자동으로 H2콘솔을 활성화 한다.


# 🥇스프링 살펴보기 요약(각 장마다 진행하면서 상세설명)
- 웹 애플리케이션 생성, 데이터베이스 사용, 애플리케이션 보안, 마이크로 서비스 등에서 개발자의 노력을 덜어주는
것이 스프링의목표
- 스프링 부트는 손쉬운 의존성 관리, 자동구성,런타임 시의 애플리케이션 내부 작동파악을 스프링에서 할수있게함.
- 스프링 애플리케이션은 스프링Initializer를 사용해서 초기 설정할 수 있다. 
스프링 Initializer는 웹을 기반으로하며 대부분의 자바개발환경지원
- 빈이라고하는 컴포넌트는 스프링 애플리케이션 컨텍스트에서 자바나 xml로 선언할 수 있으며,
컴포넌트 스캔으로 찾거나 스프링 부트 자동구성에서 자동으로 구성할 수도 있다.