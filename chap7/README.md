# 7장 REST서비스 사용하기

### 6장과 달리 이 장부터는.. 후에 있을 마이크로서비스 아키텍쳐

### 공부를 위해 책대로 서브모듈을 나누되 최신버젼의 코드로 마이그레이션했습니다.

### 이 장에서 배우는 내용
- `RestTemplate`을 사용해서 Rest API 사용하기
- `Traverson`을 사용해서 하이퍼미디어 API 이동하기

이전 장인 6장에서는 애플리케이션의 외부 클라이언트가 사용할 수 있는 엔드포인트를 정의하는데 초점을 두었다.

이때 타코클라우드 웹사이트로 서비스되는 단일-페이지 앵귤러 어플리케이션을 사용했지만, 실제로는 클라이언트가 

자바를 비롯해 어떤 언어로 작성된 애플리케이션도 될 수 있다.

스프링 애플리케이션에서 API를 제공하면서 다른 애플리케이션의 API를 요청하는 것은 흔한일이다. 이번장에서는 

REST API 클라이언트를 작성하고 사용하는 방법을 알아본다. 실제로 마이크로서비스에서는 REST API를 많이 사용한다.

그러므로 다른 REST API와 상호작용하는 방법을 알아 둘 필요가있다. 스프링은 다음과 같은 방법을 사용해 REST API를 사용할수있다.

#### [1] RestTemplate : 스프링 프레임워크에서 제공하는 간단하고 동기화된 REST 클라이언트
#### [2] Traverson: 스프링 HATEOAS에서 제공하는 하이퍼링크를 인식하는 동기화 REST 클라이언트로

#### 같은 이름의 자바스크립트 라이브러리로부터 비롯된 것이다.

#### [3] WebClient: 스프링 5에서 소개된 반응형 비동기 REST클라이언트

하지만 WebClient는 11장 스프링 반응형 웹 프레임워크에서 알아볼것이다.!

### RestTemplate으로 REST엔드포인트 사용하기

클라이언트 입장에서 REST리소스와 상호작용하려면 해야 할 일이 많아서 코드가 장황해진다.

즉, 저수준의 HTTP 라이브러리로 작업하면서 클라이언트는 클라이언트 인스턴스오 요청 객체를 생성하고, 해당 요청을 실행하고, 

응답을 분석하여 관련 도메인 객체와 연관시켜 처리해야한다. 또한, 그 와중에 발생될 수 있는 예외도 처리해야한다.

그리고 어떤 HTTP요청이 전송되더라도 이런 모든 진부한 작업이 반복된다.

이처럼 장황한 코드를 피하기 위해 스프링은 `RestTemplate`을 제공한다. JDBC를 사용할때 번거로운 작업을 

JDBCTemplate이 처리하듯이, RestTemplate은 REST리소스를 사용하는데 번잡한 일을 처리해준다.

`RestTemplate`은 REST리소스와 상호작용하기 위한 41개의 메소드를 제공한다. 그렇지만 고유한 작업을 수행하는 메소드는 12개이며

나머지는 이 메소드들의 오버로딩된 버젼이다.

## RestTemplate이 정의하는 고유한 작업을 수행하는 12개의 메소드

|메소드|기능 설명|
|:-|:-|
|delete(...)| 지정된 URL의 리소스에 HTTP DELETE요청을 수행한다 |
|exchange(...) | 지정된 HTTP 메소드를 URL에 대해 실행하며, 응답 몸체와 연결되는 객체를 포함하는 ResponseEntity를 반환한다|
|execute(...) | 지정된 HTTP메소드를 URL에 대해 실행하며, 응답 몸체와 연결되는 객체를 반환한다|
|getForEntity(...) | HTTP GET요청을 전송하며, 응답 몸체와 연결되는 객체를 포함하는 ResponseEntity를 반환한다|
|getForObject(...) | HTTP GET 요청을 전송하며 응답 몸체와 연결되는 객체를 반환한다|
|headForHeaders(...) | HTTP HEAD요청을 전송하며, 지정된 URL의 HTTP헤더를 반환한다|
|optionsForAllow(...)|HTTP OPTIONS요청을 전송하며, 지정된 URL의 Allow헤더를 반환한다|
|patchForEntity(...)|HTTP PATCH요청을 전송하며, 응답 몸체와 연결되는 결과 객체를 반환한다.|
|postForEntity(...)|URL에 데이터를 POST하며, 응답 몸체와 연결되는 객체를 포함하는 ResponseEntity를 반환한다|
|postForLocation(...)|URL에 데이터를 POST하며, 새로 생성된 리소스의 URL을 반환한다|
|postForObject(...)|URL에 데이터를 POST하며, 응답 몸체와 연결되는 객체를 반환한다|
|put(...)|리소스 데이터를 지정된 URL에 PUT  한다|

---

`RestTemplate`은 TRACE를 제외한 표준 HTTP메소드 각각에 대해 최소한 하나의 메소드를 갖고있다. 또한, execute()와

exchange()는 모든 HTTP메소드의 요청을 전송하기 위한 저수준의 범용 메소드를 제공한다

위의 표의 메소드는 다음의 세가지 형태로 오버로딩되어 있다
- 가변 인자 릿트에 지정된 URL 매개변수에 URL문자열을 인자로 받는다
- Map< String,String>에 지정된 URL매개변수에 URL문자열을 인자로 받는다
- java.net.URI를 URL에 대한 인자로 받으며, 매개변수화된 URL은 지원하지 않는다.

`RestTemplate`에서 제공하는 12개의 메소드와 이 메소드들의 오버로딩된 버전이 어떻게 작동되는지 이해하면 REST리소스를

사용하는 클라이언트를 잘 생성할 수 있을것이다. RestTemplate을 사용하려면 우리가 필요한 지점에

RestTemplate인스턴스를 생성해야 한다.

    RestTemplate rest=new RestTemplate();

또는 빈으로 선언하고 필요할 때 주입할 수도있다
```
@Bean
public RestTemplate restTemplate(){
    return new RestTemplate();
}
```

### 리소스 가져오기 (GET)

타코 클라우드 API로부터 식자재를 가져온다고 해보자. 만일 해당 API에 HATEOAS가 활성화 되지 않았다면 getForObjetc()를

사용해서 식자재를 가져올 수 있다. 예를 들어 RestTemplate을 사용해 특정 ID를갖는 Ingredient객체를 가져오는 코드는 다음과같다

```
public Ingredient getIngredientById(String ingredientId){
    return rest.getForObject("http://localhost:8080/ingredients/{id}",
    Ingredient.class,ingredientId);
}
```

여기서는 URL 변수의 가변 리스트와 URL문자열을 인자로 받게 오버로딩된 getForObject()를 사용한다.

getForObject()에 전달된 ingredientId 매개변수는 지정된 URL의 {id} 플레이스 홀더에 넣기 위해 사용된다.

getForObject()의 두 번째 매개변수는 응답이 바인딩되는 타입이다. 여기서는 JSON형식인 응답 데이터가 객체로 역직렬화되어 반환된다.

다른방법으로는 Map을사용해 URL변수들을 지정할수있다
```
public Ingredient getIngredientById(String ingredientId){
    Map<String,String> urlVariables=new HashMap<>();
    urlVariables.put("id",ingredientId);
    return rest.getForObject("http://localhost:8080/ingredients/{id}",
    Ingredient.class,urlVariables);
}
```

여기서 ingredientId값의 키는 "id"이며 요청이 수행될 때 {id} 플레이스홀더는 키가 id인 Map항목값으로 교체된다.

이와는 달리 URI 매개변수를 사용할 때는 URI객체를 구성하여 getForObject()를 호출해야한다
```
public Ingredient getIngredientById(String ingredientId){
    Map<String,String> urlVariables=new HashMap<>();
    urlVariables.put("id",ingredientId);
    URI url=UriComponentsBuilder
            .fromHttpUrl("http://localhost:8080/ingredients/{id}")
            .build(urlVariables);
    return rest.getForObject(url,Ingredient.class);
}
```

여기서 URI객체는 URL문자열 명세로 생성되며, 이 문자열의 {id}플레이스홀더는 바로 앞의 getForObject()오버로딩

버전과 동일하게 Map항목값으로 교체된다. `getForObject()`메소드는 리소스로 도메인 객체만 가져와서 응답결고로 반환한다.

그러나 클라이언트가 이외에 추가로 필요한 것이 있다면 `getForEntity()`를 사용할 수 있다.

`getForEntity()`는 getForObject()와 같은 방법으로 작동하지만, 응답 결과를 나타내는 도메인 

객체를 반환하는 대신 도메인 객체를 포함하는 `ResponseEntity`객체를 반환한다. ResponseEntity에는 응답헤더와

같은 더 상세한 응답 컨텐츠가 포함될 수 있다.

예를들어, 도메인 객체인 식자재 데이터에 응답의 Date 헤더를 확인하고 싶다고하면 다음과같이 getForEntity()를 사용하면 쉽다.

```
public Ingredient getIngredientById(String ingredientId){
    ResponseEntity<Ingredient> responseEntity=
        rest.getForEntity("http://localhost:8080/ingredients/{id}",
            Ingredient.class,ingredientId);
    
    return responseEntity.getBody();
}
```

getForEntity()메소드는 getForObject()와 동일한 매개변수를 갖도록 오버로딩되어 있다. 

따라서 URL 변수들을 가변 인자 리스트나 URI객체로 전달하여 getForEntity()를 호출할 수 있다.

### 리소스 쓰기

HTTP PUT 요청을 전송하기 위해 RestTemplate은 put()메소드를 제공한다. 이 메소드는 3개의 오버로딩된 버전이 있으며,

직렬화된 후 지정된 URL로 전송되는 Object타입을 인자로 받는다. 이때 URL자체는 URI객체나 문자열로 지정될 수 있다.

그리고 getForObject()와 getForEntity()처럼 URL 변수들은 가변 인자 리스트나 Map으로 제공될 수 있다.

특정 식자재 리소스를 새로운 Ingredient객체의 데이터로 교체한다면 다음과같다.
```
public void update(Ingredient ingredient){
    rest.put"http://localhost:8080/ingredients/{id}",
    ingredient.getId());
}
```

여기서 URL은 문자열로 지정되었고 인자로 전달된 Ingredient객체의 id속성 값으로 교체되는 플레이스홀더를 갖는다.

put()메소드는 Ingredient 객체 자체를 전송하며, 반환 타입은 void이므로 이 메소드의 반환값을 처리할 필요는 없다.

### 리소스 삭제하기

타코 클라우드에서 특정 식자재를 더이상 제공하지 않으므로 해당 식자재를 완전히 삭제하고 싶다면 RestTemplate의 delete()메소드를 호출하면된다

```
public void deleteIngredient(Ingredient ingredient){
    rest.delete("http://localhost:8080/ingredeints/{id}",
            ingredient.getId());
}
```

여기서는 문자열로 지정된 URL과 URL변수값만 delete()의 인자로 전달한다. 그러나 다른 RestTemplate 메소드와 마찬가지로,

URL은 Map으로 된 URL 매개변수나 URI객체로 지정될 수 있다.

### 리소스 데이터 추가하기

새로운 식자재를 타코 클라우드 메뉴에 추가한다고 해보자. 이때는 요청 몸체에 식자재 데이터를 갖는 HTTP POST요청을 .../ingredients

엔드포인트에 하면 된다. RestTemplate은 POST요청을 전송하는 오버로딩된 3개의 메소드를 갖고있으며,

URL을 지정하는 형식은 모두 같다. POST요청이 수행된 후 새로 생성된 Ingredient 리소스를 반환받고 싶다면 다음과 같이 postForObejct()를 사용한다

```
public Ingredient createIngredient(Ingredient ingredient){
    return rest.postForObject("http://localhost:8080/ingredients",
            ingredient,
            Ingredient.class);
}
```

postForObject()메소드는 문자열 URL과 서버에 전송될 객체 및 이 객체의 타입을 인자로 받는다.

만일 클라이언트에서 새로 생성된 리소스의 위치가 추가로 필요하다면 `postForObject()`대신 `postForLocation()`을 호출할수있다.
```
public URI createIngredient(Ingredient ingredient){
    return rest.postForLocation("http://localhost:8080/ingredients",
                ingredient);
}
```

postForLocation()은 postForObject()와 동일하게 작동하지만, 리소스 객체 대신 새로 생성된 리소스의 URI를 반환한다는 것이 다르다.

반환된 URI는 해당 응답의 Location헤더에서 얻는다. 만일 새로 생성된 리소스의 위치와 리소스 객체 모두가 필요하다면 `postForEntity()`를호출할수있다.

```
public Ingredient createIngredient(Ingredient ingredient){
    ResponseEntity<Ingredient> responseEntity=
        rest.postForEntity("http://localhost:8080/ingredients",
            ingredient,
                Ingredient.class);
    return responseEntity.getBody();
}
```

RestTemplate 메소드들의 용도는 다르지만 사용하는 방법은 매우 유사해 쉽게 배워서 클라이언트 코드에 사용할 수 있다.

반면에 우리가 사용하는 API에서 하이퍼링크를 포함해야 한다면 RestTemplate은 도움이 안된다.

이때는 `Traverson`과 같은 클라이언트 라이브러리를 사용하는것이 좋다!

## Traverson으로 REST API 사용하기
`Traverson`은 스프링 데이터 HATEOAS에 같이 제공되며, 스프링 애플리케이션에서 하이퍼 미디어 API를  사용할 수 있는 솔루션이다.

`Traverson`을 사용할 때는 우선 해당 API의 기본 URI를 갖는 객체를 생성해야 한다.
```
Traverson traverson=new Traverson(
    URI.create("http://localhost:8080/api"),MediaTypes.HAL_JSON
);
```
Traverson을 타코 클라우드 기본 URL로 지정하였다. Traverson에서는 이 URL만 지정하면 되며, 이후부터는

각 링크의 관계이름으로 API를 사용한다. 또한 Traverson생성자에는 해당 API가 HAL스타일의 하이퍼링크를 갖는

JSON응답을 생성한다는 것을 인자로 지정할 수 있다. 이 인자를 지정하는 이유는 수신되는 리소스 데이터를 분석하는 방법을

traverson이 알수 있게 하기 위해서다. 어디서든 Traverson이 필요할땐 객체를 생성하거나 주입되는 빈으로 선언할수있다.

예를들어 모든 식자재 리스트를 가져온다면 다음과 같이 작성할수있다.
```
/* 책과달리 스프링 최신버전 코드로 변경하였음*/
ParameterizedTypeReference<CollectionModel<Ingredient>> ingredientType=
    new ParameterizedTypeReference<CollectionType<Ingredient>>(){};

    CollectionModel<Ingredient> ingredeintRes=
        traverson
            .follow("ingredeints")
            .toObject(ingredeintType);
    Collection<Ingredient> ingredeint=ingredientRes.getContent();
```

이처럼 Traverson객체의 follw()메소드를 호출하면 리소스 링크의 관계이름이 ingredientrs인 리소스로 이동할 수 있다. 

이 시점에서 클라이언트는 ingredients로 이동했으므로 toObject()를 호출하여 해당 리소스의 콘텐츠를 가져와야한다.

`toObject()`메소드의 인자에는 데이터를 읽어 들이는 객체의 타입을 지정해야한다. 이때 고려할것이있는데 `CollectionModel<Ingredient>` 타입의 객체로 읽어 들여야하는데, 자바에서는 

런타임시에 제네릭 타입의 타입정보가 소거되어 리소스 타입을 지정하기 어렵다 .. 그러나 `ParameterizedTypeReference`를 생성하면 리소스타입을 지정할수있다.

지금까지 보았듯이 Traverson을 사용하면 HATEOAS가 활성화 된 API를 이동하면서 해당 API만의 리소스를 쉽게 가져올수있다.

그러나 Traverson은 API에 리소스를 쓰거나 삭제하는 메소드를 제공하지 않는다. 이와는 반대로 RestTemplate은 리소스를 쓰거나

삭제할수있지만 API를 이동하는것은 쉽지 않다.

### 따라서 API의 이동과 리소스의 변경이나 삭제 모두를 해야한다면 RestTemplate과 Traverson을 함께써야한다.

Traverson은 새로운 리소스가 생성될 링크로 이동할 때도 사용할 수 있으며, 이동한다음에는 해당 링크를 RestTemplate에 지정하여 

우리가 필요한 POST, Put, DELETE또는 어떤 다른 HTTP요청도 할 수 있다. 예를들면 다음과같다

```
private Ingredient addIngredeient(Ingredient ingredient){
    String ingredeintsUrl= traverson
        .follow("ingredients")
        .asLink()
        .getHref();
    
    return rest.postForObject(ingredientsUrl, ingredient, Ingredient.class);
}
```

ingredients링크를 따라간 후에는 asLink()를 호출하여 ingredeints링크 자체를 요청한다. 

그리고 getHref()를 호출하여 이 링크의 URL을 가져온다. 이렇게 URL을 얻은다음에는 RestTemplate 인스턴스의

postForObject()를 호출하여 새로운 식자재를 추가할 수 있다.

# 7장 요약 
### [1] 클라이언트는 RestTemplate을 사용해서 REST API에 대한 HTTP요청을 할 수 있다.

### [2] Traverson을 사용하면 클라이언트가 응답에 포함된 하이퍼링크를 사용해서 원하는 API로이동할수있다.













