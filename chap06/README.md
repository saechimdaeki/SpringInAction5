# 🥇6장 REST 서비스 생성하기
## 💕이 장에서 배우는 내용
- 스프링 MVC에서 REST 엔드포인트 정의하기
- 하이퍼링크 REST 리소스 활성화하기
- 레포지토리 기반의 REST엔드포인트 자동화

## 💕스프링최신버전 사용시 hateoas 책과 다른 변경사항
- `ResourceSupport` changed to `RepresentationModel`
- `Resource` changed to `EntityModel`
- `Resources` changed to `CollectionModel`
- `PagedResources` changed to `PagedModel`
- `ResourceAssembler` changed to `RepresentationModelAssembler`
- `ControllerLinkBuilder` changed to `WebMvcLinkBuilder`
- `ResourceProcessor` changed to `RepresentationModelProcessor`
## 💕이 레포지토리에서는 변경내용을 반영해 작성하겠습니다
### 🌟스프링부트 구버젼 사용할경우 변경하지 않아도 됨.
이번 장에서는 스프링을 사용해서 타코클라우드 애플리케이션에 REST API를 제공할것이다. 이때 스프링 MVC 컨트롤러를 사용해서

REST 엔드포인트를 생성하기 위해 2장에서 배웠던 MVC를 사용한다. 또한, 4장에서 정의했던 스프링 데이터 레포지토리의 REST

엔드포인트도 외부에서 사용할 수 있게 자동으로 노출시킨다. 마지막으로, 그런 엔드포인트를 테스트하고 안전하게 만드는 법을 알아본다

## 💕REST컨트롤러 작성하기

이 책에서는 앵귤러 프레임워크를 사용해서 SPA로 프론트엔드를 구축한다. 이책의 목적은 앵귤러가아니므로 핵심은 백엔드

스프링 코드에 초점을 둘 것이다.

### 🌟서버에서 데이터 가져오기

가장 최근에 생성된 타코를 보여주는 RecentTacosComponent를 앵귤러 코드에 정의하였다.
```
//최근 타코들의 내역을 보여주는 앵귤러 컴포넌트
import { Component, OnInit, Injectable} from '@angular/core';
import {Http} from '@angular/http';
import {HttpClient} from '@angular/common/http';

@Component({
    selector: 'recent-tacos',
    templateUrl: 'recents.component.html',
    styleUrls: ['./recents.component.css']
})

@Injectable()
export class RecentTacosComponent implements OnInit{
    recentTacos: any;

    constructor(private httpClient: HttpClient){}

    ngOnInit(){
        //최근 생성된 타코들을 서버에서 가져온다
        this.httpClient.get('http://localhost/design/recent')
        .subscribe(data => this.recentTacos = data);
    }
}

```

ngOnInit()메소드에 주목하자. 이 메소드에서 RecentTacosComponent는 주입된 Http모듈을 사용해 HTTP요청을 수행한다

이경우 recentTacos 모델 변수로 참조되는 타코들의 내역이 응답에 포함된다. 

타코 디자인 API요청을 처리하는 REST 사용 컨트롤러
```
@RestController
@RequestMapping(path="design", produces = "application/json")
@CrossOrigin(origins = "*")
public class DesignTacoController {
	
	private TacoRepository tacoRepo;
	
	@Autowired
	EntityLinks entityLinks;
	
	public DesignTacoController(TacoRepository tacoRepo) {
		this.tacoRepo=tacoRepo;
	}
	
	@GetMapping("/recent")
	public Iterable<Taco> recentTacos(){
		PageRequest page=PageRequest.of(0, 12,Sort.by("createdAt").descending());
		
		return tacoRepo.findAll(page).getContent();
	}
}
```

먼저 책에는 생략된게 너무 많은데 이를 추가해야한다
```
    <dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-hateoas</artifactId>
	</dependency>
```

`@RestContoller` 애노테이션은 다음 두가지를 지원한다. 우선, @Controller나 @Service와 같이 스테레오 타입

애노테이션이므로 이 애노테이션이 지정된 클래스를 컴포넌트 검색으로 찾을 수 있다.

즉 `@RestController` 애노테이션은 컨트롤러의 모든 HTTP요청 처리 메소드에서 HTTP응답 몸체에 직접 쓰는

값을 반환한다는 것을 스프링에게 알려준다. 따라서 반환값이 뷰를 통해 HTML로 변환되지않고 직접 HTTP응답으로 브라우저에 전달되어 나타난다.

또는 일반적인 스프링 MVC 컨트롤러처럼 클래스에 @Controller를 사용할 수도있다. 그러나 이때는

이 클래스의 모든 요청 처리 메소드에 @ResponseBody애노테이션을 지정해야만 @RestController와 같은 결과를 얻을수있다.

이외에도 ResponseEntity객체를 반환하는 또다른 방법이있지만 뒤에서 알아보자.

`@RequestMapping`애노테이션에는 produces속성도 설정되어 있다.  이것은 요청의 Accept헤더에

"application/json"이 포함된 요청만을 DesignController의 메소드에서 처리한다는 것을 나타낸다.

이경우 응답결과는 JSON형식이 되지만, produces 속성의 값은 String 배열로 저장되므로 다른 컨트롤러에서도 요청을 

처리할 수 있도록 JSON만이 아닌 다른 콘텐트 타입을 같이 지정할 수 있다.

예를들어 XML로출력하고자 하면 다음과같이 추가하면된다
```
@RequestMapping(path="/design" ,
                produces={"application/json", "text/xml"})
```

또한 `@CrossOrign`애노테이션이 지정되어 있다.앵귤러 코드는 api와 별도의 도메인에서 

실행 중이므로 앵귤러 클라이언트 에서 api를 사용못하게 웹 브라우저가 막는다.  이런 제약은 서버 응답에 CORS헤더를 포함시켜 극복할 수 있으며

스프링에서는 @CrossOrigin애노테이션을 지정하여 쉽게 CORS를 적용할 수 있다.

@CrossOrigin은 다른 도메인의 클라이언트에서 해당 REST API를 사용할 수 있게 해주는 스프링 애노테이션이다.


타코 ID로 특정 타코만 가져오는 엔드포인트를 제공하고 싶다면 어떻게할까? 메소드의 경로에 플레이스홀더

변수를 지정하고 해당 변수를 통해 ID를 인자로 받는 메소드를 추가하면된다.

```
    @GetMapping("/{id}")
	public Taco tacoById(@PathVariable("id") Long id) {
		Optional<Taco> optTaco= tacoRepo.findById(id);
		if(optTaco.isPresent()) {
			return optTaco.get();
		}
		return null;
	}
```
DesignTacoController의 기본경로가 /design이므로 이 메소드는 /design/{id} 경로의 GET요청을 처리한다. 

여기서 경로의 {id}부분이 플레이스 홀더이며 `@PathVariable`에 의해 {id}플레이스홀더에 대응되는

id매개변수에 해당 요청의 실제 값이 지정된다.

하지만 위와 같은 코드는 좋은방법이 아니다. null을 반환하면 컨텐츠가 없는데도 정상처리를 나타내는 

HTTP 200(OK) 상태코드를 클라이언트가 받기때문이다.  따라서 이때는 HTTP404(NotFound)상태 코드를

응답으로 반환하는 것이 더좋다.

```
    //이렇게 고치자!
	@GetMapping("/{id}")
	public ResponseEntity<Taco> tacoById(@PathVariable("id")Long id){
		Optional<Taco> optTaco=tacoRepo.findById(id);
		if(optTaco.isPresent()) {
			return new ResponseEntity<>(optTaco.get(),HttpStatus.OK);
		}
		return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
	}
```

이렇게하면 Taco 대신 ResponseEntity< Taco> 가 반환된다. 이 경우 찾은 타코가 있을때는 HTTP 200(OK) 상태

코드를 갖는 ResponseEntity에 Taco개게가 포함된다. 그러나 찾지 못했을때는 HTTP 404(NOT FOUND)

상태코드를 갖는 ResponseEntity에 null이 포함되어 클라이언트에서 가져오려는 타코가 없다는 것을 나타낸다.

이제 타코클라우드 API를 사용할 수 있다. 그리고 개발시에 API를 테스트할 때는 curl이나 HTTPPie를 사용해도 된다

명령행에서 curl을 사용해 최근 생성된 타코들을 가져오는 예는 다음과 같다
    
    $ curl localhost:8080/design/recent

HTTPPie를 사용할때는 다음과 같다

    $ http :8080/design/recent

### 🌟서버에 데이터 전송하기 

앵귤러 코드는 생략한다. 타코디자인 데이터를 요청하고 저장하는 메소드를 DesignTacoController에 추가하자.
```
    @PostMapping(consumes = "application/json")
	@ResponseStatus(HttpStatus.CREATED)
	public Taco postTaco(@RequestBody Taco taco) {
		return tacoRepo.save(taco);
	}
```

postTaco()는 HTTP POST요청을 처리하므로 @GetMapping대신 @PostMapping애노테이션을 지정하였다.

그리고 path속성을 지정하지 않았으므로 클래스에 지정된 @RequestMapping의 /design 경로에 대한 요청을 처리한다.

여기서는 consumes속성을 설정하였다. 따라서 Content-type이 application/json과 일치하는 요청만 처리한다.

postTaco()메소드의 taco매개변수에는 `@RequestBody`가 지정되었다. 이것은 요청 몸체의

JSON데이터가 Taco객체로 변환되어 taco 매개변수와 바인딩된다는 것을 나타낸다.

@RequestBody애노테이션은 중요하다. 이것이 지정되지 않으면 매개변수가 곧바로 Taco객체와 바인딩되는것으로 스프링 MVC가 간주하기에..

postTaco()메소드에는 @ResponseStatus(HttpStatus.CREATED) 애노테이션도 지정되어 있다.

따라서 해당 요청이 성공적이면서 요청의 결과로 리소스가 생성되면 HTTP 201(CREATED)상태코드가 클라이언트에게 전달된다. 

이경우 @ResponseStatus를 사용하지 않았을 때 요청 성공을 나타내는 HTTP 200(OK) 상태코드보다 

더 상세한 설명을 알려줄 수 있다. 그러므로 항상 @ResponseStatus를 사용하여 클라이언트에게 더 서술적이며 정확한

HTTP상태코드를 전달하는 것이 좋다.

### 🌟서버의 데이터 변경하기

데이터를 변경하기 위한 HTTP메소드로는 PUT과 PATCH가 있다. 왜 두개가 있는지 이유를 알고 컨트롤러를 작성하는게 중요하다

`PUT`은 데이터를 변경하는데 사용되기는 하지만, 실제로는 GET과 반대의 이미를 갖는다. 즉 GET요청은

서버로부터 클라이언트로 데이터를 전송하는 반면, `PUT`요청은 클라이언트로부터 서버로 데이터를 전송한다.

이런 관점에서 PUT은 데이터 전체를 교체하는 것이며 `PATCH`의 목적은 데이터의 일부분을 변경하는 것이다.

예를들어, 특정 주문 데이터의 주소를 변경하고 싶다고하자. REST API를 통해서 이렇게 할 수 있는 한가지 방법은 PUT요청을하는거다.

```
@PutMapping("/{orderId})
public Order putOrder(@RequestBody Order order){
    return repo.save(order);
}
```

그러나 이경우는 클라이언트에서 해당 주문 데이터 전체를 PUT요청으로 제출해야 한다. 

PUT은 해당 URL에 이 데이터를 쓰라는 의미이므로 이미 존재하는 해당 데이터 전체를 교체한다.

그리고 만일 해당 주문의 속성이 생략되면 속성의 값은 null로 변경된다. 

그렇다면 데이터의 일부만 변경하고자하면 어떻게 요청을 처리해야할까?

특정 주문의 PATCH요청을 처리하는 컨트롤러 메소드는 다음과 같이 작성할 수 있다.

```
@PatchMapping(path="/{orderId}", consumes="application/json")
public Order patchOrder(@PathVariable("orderId") Long orderId, @RequestBody Order patch){
    Order order=repo.findById(orderId).get();
    if(patch.getDeliveryName()!=null){
        order.setDeliveryName(patch.getDeliveryName());
    }
    if(patch.getDeliveryStreet()!= null){
        order.setDeliveryStreet(patch.getDeliveryStreet());
    }
    if(patch.getDeliveryCity() != null){
        order.setDeliveryState(patch.getDeliveryState());
    }
    if(patch.getDeliveryZip() != null){
        order.setDeliveryZip(patch.getDeliveryZip()); //책에 오타있어서 수정함.
    }
    if(patch.getCcNumber() != null){
        order.setCcNumber(patch.getCcNumber());
    }
    if(patch.getCcExpiration() != null){
        order.setCcExpiration(patch.getCcExpiration());
    }
    if(patch.getCcCVV() != null){
        order.setCcCVV(patch.getCcCVV());
    }
    return repo.save(order);
}


```

putOrder()메소드의 경우 HTTP PUT의 의미대로 한 주문의 전체 데이터를 받고 저장한다. 그러나 HTTP PATCH의 의미를

따르는 patchMapping()에서는 데이터의 일부만 변경하기 위한 로직이 필요하다.  즉 해당 주문 데이터를 전송된 Order객체로

완전히 교체하는 대신, Order객체의 각 필드 값이 null이 아닌지 확인하고 기존 주문 데이터에 변경해야한다.

이방법을 사용하면 클라이언트에서 변경할 속성만 전송하면 된다. 그리고 서버에서는 클라이언트에서 지정하지 않은 속성의 기존데이터를 보관할수있다.

### 🌟서버에서 데이터 삭제하기 

데이터를 그냥 삭제할 때는 클라이언트에서 HTTP DELETE 요청으로 삭제를 요청하면 된다. 이때는 DELETE 요청을 처리하는 메소드에

스프링 MVC의 @DeleteMapping을 지정한다. 예를들어, 주문 데이터를 삭제하는 API의 컨트롤러 메소드는 다음과 같다

```
@DeleteMapping("/orderId}")
@ResponseStatus(code=HttpStatus.NO_CONTENT)
public void deleteOrder(@PathVariable("orderId") Long orderId){
    try{
        repo.deleteById(orderId);
    }catch(EmptyResultDataAccessException e){}
}
```

deleteOrder()메소드의 코드가 하는일은 특정 주문 데이터를 삭제하는 것이다. 이때 URL의 경로 변수로 제공된 주문 ID를 인자로

받아서 레포지토리의 deleteBhyId()메소드에 전달한다. 그리고 이 메소드가 실행될 떄 해당 주문이 존재하면 삭제되며 없으면

EmptyResultDataAccessException이 발생된다.

이외에 deleteOrder()메소드에는 @ResponseStatus가 지정되어 있다. 이것은 응답의 HTTP 상태코드가 204(NO CONNECT)가 되도록

하기위해서이다. 이 메소드는 주문 데이터를 삭제하는 것이므로 클라이언트에게 데이터를 반환할 필요가없다.

따라서 대개의 경우 DELETE요청의 응답은 몸체 데이터를 갖지 않으며, 반환 데이터가 없다는 것을 클라이언트가 알 수 있게 HTTP상태 코드를 사용한다.

## 💕하이퍼미디어 사용하기

API 클라이언트 코드에서는 흔히 하드코딩된 URL패턴을 사용하고 문자열로 처리한다. 그러나 API의 URL스킴이 변경되면 어떻게될까?

하드코딩된 클라이언트 코드는 API를 잘못인식하여 정상적으로 실행되지 않을 것이다. 따라서 API UTL을 하드코딩하고 

문자열로 처리하면 클라이언트 코드가 불안정해진다.

REST API를 구현하는 또 다른 방법으로 HATEOAS가 있다. 이것은 API로부터 반환되는 리소스에 해당 리소스와 관련된 하이퍼링크들이 포함된다.

따라서 클라이언트가 최소한의 API URL만 알면 반환되는 리소스와 관련하여 처리 가능한 다른 API URL들을 알아내어 사용할 수 있다.

예를들어, 하이퍼링크가 없는 형태의 최근 타코 리스트는 다음과 같이 JSON형식으로 클라이언트에서 수신될것이다
```
[
    {
        "id":4,
        "name: "Veg-Out",
        "createdAt": "2021-02-14T20:15:53.219+0000",
        "ingredients":[
            {"id":"FLTO", "name": "Flour Tortilla" , "type" : "WRAP"}
            ...
        ]
    },
    ...
]
```

이 경우 만일 클라이언트가 타코 자체에 대한 다른 HTTP작업을 수행하고 싶다면 /design경로의 URL에 id속성값을 

추가해야 한다는것을 알고 있어야 한다. 마찬가지로 식자재 중 하나에 HTTP작업을 수행하고 싶다면 /ingredients경로의 URL에

해당 식자재의 id속성 값을 추가해야 한다는 것을 알아야한다. 그리고 어떤 경우든 해당 경로 앞에 http://나 https:// 및 API호스트이름도 붙여야한다.

이와는 다르게 API에 하이퍼미디어가 활성화 되면 API에는 자신과 관련된 URL이 나타나므로 그것을 클라이언트가 하드코딩하지 않아도 된다.
```
{
    "_embedded":{
        "tacoResourceList":[
            {
                "name":"Veg-Our",
                "createdAt":"2021-02-14T20:15:53.219+0000",
                "ingredients":[
                    {
                    "name":"Flour Tortilla", "type":"WRAP",
                    "_links":{
                        "self": { "href" : "http://localhost:8080/ingredients/FLTO"}
                        }
                    },
                    ...
                ]
            }
        ]
    },
    "_links":{
        "recents":{
            "href":"http://localhost:8080/design/recent"
        }
    }
}
```
이런 형태의 HATEOAS를 HAL이라고한다. 이것은 JSON응답에 하이퍼링크를 포함시킬때 주로 사용되는 형식이다.

이 타코 리스트의 각요소는 _links라는 속성을 포함하는데 이속성은 클라이언트가 관련 API를 수행할수있는 하이퍼링크를 포함한다.

타코와 해당타코의 식자재 모두 그들 리소스를 참조하는 self링크를 가지며, 리스트 전체는 자신을 참조하는 recents링크를갖는다

따라서 클라이언트 애플리케이션이 타코리스트의 특정 타코에 대해 HTTP요청을 수행해야할때 해당 타코 리소스의 URL을 지정하지않아도된다.

대신에 참조하는 selef링크를 요청하고 해당타코의 특정 식자재를 처리하고자 할 때는 해당 식자재의 selef링크만 접속하면된다.

스프링 HATEOAS프로젝트는 하이퍼링크를 스프링에 지원한다. 구체적으로 말해서 스프링 MVC컨트롤러에서 리소스를 반환하기 전에

해당 리소스에 링크를 추가하는데 사용할 수 있는 클래스와 리소스 어셈블러를 제공한다.

### 🌟하이퍼링크 추가하기

`스프링 HATEOAS`는 하이퍼링크 리소스를 나타내는 두 개의 기본타입인 `Resource`와 `Resources`를 제공한다.

Resource 타입은 단일 리소스를, 그리고 Resources는 리소스 컬렉션을 나타내며, 두 타입 모두 다른 리소스를 링크할 수 잇다.

두 타입이 전달하는 링크는 스프링 MVC컨트롤러 메소드에서 반환될 때 클라이언트가 받는 JSON에포함된다.

최근 생성된 타코 리스트에 하이퍼링크를 추가하려면 recentTacos()메소드에서 List< Taco>를 반환하는 대신 CollectionModel객체를 반환하도록 수정해야한다.

```
    @GetMapping("/recent")
	public CollectionModel<EntityModel<Taco>> recentTacos(){
		PageRequest page=PageRequest.of(0, 12,Sort.by("createdAt").descending());
		
		List<Taco> tacos=tacoRepo.findAll(page).getContent();
		CollectionModel<EntityModel<Taco>> recentResources=CollectionModel.wrap(tacos);
		
		recentResources.add(new Link("http://localhost:8080/design/recent","recents"));
		return recentResources;
	}
```

이렇게 수정된 recentTacos()에서는 직접 타코리스트를 반환하지 않고 대신 CollectionModel.wrap()을 사용해서

recnetTacos()의 반환타입인 CollectionModel< EntityModel < Taco>>의 인스턴스로 타코 리스트를 매핑한다.

그러나 CollectionModel 객체를 반환하기 전에 이름이 recents이고 URL이 http://localhost:8080/design/recent인 링크를 추가한다.

따라서 API요청에서 반환되는 리소스에 다음의 JSON코드가 포함된다

```
"_links":{
    "recents":{
        "href":"http://localhost:8080/design/recent"
    }
}
```

하지만 이것또한 URL을 하드코딩하는것으로 좋은방법은 아니다. 타코클라우드 애플리케이션을 개발용 컴퓨터에서만 실행된다면

모를까 로컬호스트와 포트를 나타내는 localhost:8080으로 URL을 하드코딩하면 안되기 때문이다. 다행하게도스프링 HATEOAS는

링크 빌더를 제공하여 URL을 하드코딩하지 않는 방법을 제공한다

스프링 HATEOAS링크 빌더 중 가장 유용한 것이 `ControllerLinkBuilder`다. 이 링크빌더를 사용하면 URL을

하드코딩하지 않고 호스트 이름을 알 수 있다. 그리고 컨트롤러의 기본 URL에 관련된 링크의 빌드를 도와주는 편리한 API를 제공한다

`WebMvcLinkBuilder`를 사용하면 recentTacos()의 하드 코딩된 Link를 다음과 같이 생성할 수 있다.
```
recentResources.add(WebMvcLinkBuilder.linkTo(DesignTacoController.class)
			.slash("recent")
			.withRel("recents")
			);
```

이제는 호스트 이름을 하드코딩할 필요가 없으며, /design 경로 역시 지정하지 않아도 된다.

대신에 기본경로가 /design인 링크를 DesignTacoController에 요청한다. WebMvcLinkBuilder는 이 컨트롤러의

기본 경로를 사용해서 Link객체를 생성한다. 그 다음에는 스프링 프로젝트에서 많이 사용하는 slash()메소드를 호출한다.

이 메소드는 이름 그대로 슬래시(/)와 인자로 전달된 값을 URL에 추가한다. 따라서 URL의 경로는 /design/recent가된다.

제일 끝에는 해당 Link의 관계 이름을 지정하며, 이 예에서는 recents다.

또한 WebMvcLinkBuilder에는 링크 URL을 하드코딩하지 않게 해주는 또 다른 메소드인 `linkTo()`가 있어서 slash()대신 호출할수도있다.

```
	recentResources.add(
			WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(DesignTacoController.class).recentTacos())
			.withRel("recents")
			);
```

`methodOn()`컨트롤러 클래스인 DesignTacoController를 인자로 받아 recentTacos()메소드를 호출할 수 있게해준다.

따라서 해당 컨트롤러의 기본 경로와 recentTacos()의 매핑 경로 모두를 결정하는데 사용한다. 이제 URL의모든값을얻어 하드코딩하지않아도된다.

### 🌟리소스 어셈블러 사용하기

이제 리스트에 포함된 각 타코 리소스에 대한 링크를 추가해야한다. 이때 한가지 방법은 반복루프에서 Resources객체가 가지는 각

EntityModel< Taco> 요소에 link를 추가하는 것이다. 그러나 이경우는 타코리소스의 리스트를 반환하는 API코드마다 루프를 실행하는 코드가 

있어야하므로 번거로워 다른 전략이필요하다.

CollectionModel.wrap()에서 리스트의 각 타코를 EntityModel객체로 생성하는대신 Taco 객체를 새로운 TacoResource객체로 변환하는 유틸리티 클래스를 정의하자
```
public class TacoResource extends RepresentationModel<TacoResource>{
	
	@Getter
	private final String name;
	
	@Getter
	private final Date createdAt;
	
	@Getter
	private final List<Ingredient> ingredients;
	
	public TacoResource(Taco taco) {
		this.name=taco.getName();
		this.createdAt=taco.getCreatedAt();
		this.ingredients=taco.getIngredients();
	}
}
```

TacoResource는 Taco객체를 인자로 받는 하나의 생성자를 가지며, Taco객체의 속성 값을 자신의 속성에 복사한다.

따라서 Taco객체를 TacoResource객체로 쉽게 변환한다. 그러나 여기까지만 한다면 Taco객체들을 CollectionModel< TacoResource>

로 변환하기 위해 여전하 반복루프가 필요할 것이다. 따라서 리스트의 Taco객체들을 TacoResource객체들로 변환하는데 도움을 주기위해 다음을진행한다
```
public class TacoResourceAssembler extends RepresentationModelAssemblerSupport<Taco, TacoResource>{

	public TacoResourceAssembler() {
		super(DesignTacoController.class, TacoResource.class);
	}
	
	@Override
	protected TacoResource instantiateModel(Taco entity) {
		return new TacoResource(entity);
	}
	
	@Override
	public TacoResource toModel(Taco entity) {
		return createModelWithId(entity.getId(), entity);
	}

}
```

TacoResourceAssembler의 기본생성자에서는 슈퍼클래스인 `RepresentationModelAssemblerSupport`의 기본 생성자를 호출하며,

이때 TacoResource를 생성하면서 만들어지는 링크에 포함되는URL의 기본경로를 결정하기위해 DesignTacoController를 사용한다

`instantiateModel()`메소드는 인자로 전달된 Taco객체로 TacoResource인스턴스를 생성하도록 오버라이드 되었다. 

TacoResource가 기본 생성자를 갖고있다면 이 메소드는 생략할수있다. 그러나 여기서는 Taco객체로 TacoResource인스턴스를 

생성해야하므로 오버라이드 해야한다.

마지막으로 `toModel()`메소드는 RepresentationModelAssemblerSupport로부터 상속받을때 반드시 오버라이드 해야한다.

여기서는 Taco객체로 TacoResource인스턴스를 생성하면서 Taco객체의 id값으로 생성되는 self링크가 URL로 자동지정된다.

외견상으로는 toModel()이 instantiateModel()와 같은 목적을 갖는것처럼 보이지만 약간다르다. 

instantiantModel()은 EntityModel인스턴스만 생성하지만 toModel()은 EntityModel인스턴스를 생성하면서 링크도 추가한다.

```
public class TacoResources extends CollectionModel<TacoResource>{
	public TacoResources(List<TacoResource> tacoResources) {
		super(tacoResources);
	}
}

```
이제 recentTacos()메소드를 변경할수있다.
```
@GetMapping("/recent")
	public CollectionModel<EntityModel<Taco>> recentTacos(){
		PageRequest page=PageRequest.of(0, 12,Sort.by("createdAt").descending());
		
		List<Taco> tacos=tacoRepo.findAll(page).getContent();
		
		List<TacoResource> tacoResources= new TacoResourceAssembler().toResources(tacos);
		CollectionModel<TacoResource> recentResources= new CollectionModel<TacoResource>(tacoResources);
	recentResources.add(
			WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(DesignTacoController.class).recentTacos())
			.withRel("recents")
			);
	}
```

recentTacos()에서 새로운 TacoResource타입을 사용해 CollectionModel< EntityModel< Taco>> 대신

CollectionModel< TacoResource>를 반환한다. 즉 레포지토리로부터 타코들을 가져와서 Taco객체 리스트에 저장한 후 리스트를 TacoResourceAssembler와 toResources()메소드에 전달한다.

이제 식자재의 리소스 어셈블러 클래스도 만들어야한다
```
public class IngredientResource extends RepresentationModel {
    
    @Getter
    private String name;
    
    @Getter
    private Type type;
    
    public IngredientResource(Ingredient ingredient){
        this.name=ingredient.getName();
        this.type=ingredient.getType();
    }
}
```


```
public class IngredientResourceAssembler extends RepresentationModelAssemblerSupport<Ingredient, IngredientResource>{

    public IngredientResourceAssembler() {
        super(null/*IngredientController.class* 깃허브보니 13장에서 작성하는듯*/, IngredientResource.class);
    }   
    @Override
    public IngredientResource toModel(Ingredient entity) {
        return createModelWithId(entity.getId(),entity);
    }

    @Override
    protected IngredientResource instantiateModel(Ingredient entity) {
        return new IngredientResource(entity);
    }
    ...
    public CollectionModel<IngredientResource> toCollectionModel(Iterable<? extends Ingredient> entities) {
        return super.toCollectionModel(entities);
    }
}
```

## 💕p.s. 
tocollectionModel  즉 책에서의 toResources에 관해서는 책에는 물론 이책의 필자의 깃허브에도 어떠한 자료도 없다.
어떻게 사용하는지 의문인 상황..  

일단 공부를위해 TacoResources의 List를 CollectionModel로 변경을 시켜놓았다. 최신버전에는 이렇게 해야한다!! 


### 🌟embedded관계 이름 짓기
```
{
    "_embedded":{
        "tacoResourceList":[
            ...
        ]
    }
}
```

여기서 embedded밑의 tacoResourceList라는 이름에 주목하자.  이 이름은 Resources객체가 List< TacoResource>로부터

생성되었다는 것을 나타낸다. 만일 TacoResource클래스의 이름을 다른것으로 변경한다면 결과 JSON필드 이름이 그에 맞춰서 바뀔것이다. 

따라서 변경 전의 이름을 사용하는 클라이언트 코드가 제대로 실행되지 않을것이다.

이럴때 `@Relation`애노테이션을 사용하면 자바로 정의된 리소스 타입 클래스 이름과 JSON필드 이름간의 결합도를 낮출 수 있다.

즉 , 다음과 같이 TacoResource에 @Relation을 추가하면 스프링 HATEOAS가 결과 JSON필드 이름을 짓는 방법을 지정할수있다.

```
@Relation(value="taco", collectionRelation="tacos")
public class TacoResource extends RepresentationModel<TacoResource>{
    ...
}
```

여기서는 TacoResource객체 리스트가 Resources객체에서 사용될 때 tacos라는 이름이 되도록 지정하였다. 

JSON에서는 TacoResource객체가 taco로 참조된다.

이에따라 /design/recent로부터 반환되는 JSON은 다음과같다
```
{
    "_embedded":{
        "tacos":[
            ...
        ]
    }
}
```

스프링 HATEOAS는 직관적이고 쉬운 방법으로 API에 링크를 추가하지만 우리가 필요로 하지않는 몇줄의 코드를 자동으로 추가한다.

API의 URL스킴이 변경되면 클라이언트 코드 실행이 중단됨에도 자동으로 추가되는 코드가 싫어서 API에 HATEOAS사용을 고려하지 않는 개발자들도있긴하다.

만일 스프링 데이터를 레포지토리로 사용한다면 또 다른방법도있다.

### 🌟데이터 기반 서비스 활성화하기

스프링 데이터는 우리가 코드에 정의한 인터페이스를 기반으로 레포지토리 구현체를 자동으로 생성하고 필요한 기능을 수행한다.

그러나 스프링 데이터에는 애플리케이션의 API를 정의하는데 도움을 줄 수 있는 기능도 있다.

스프링 데이터 REST는 스프링데이터의 또 다른 모듈이며, 스프링 데이터가 생성하는 레포지토리의 REST API를 자동생성한다.

따라서 스프링 데이터 REST를 빌드에 추가하면 정의한 각 레포지토리 인터페이스를 사용하는 API를 얻을수있다.
```
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-data-rest</artifactId>
</dependency>
```

이렇게하면 스프링데이터를 사용중인 프로젝트에서 RESTAPI를 노출시킬수있다. 스프링 데이터 REST 스타터가 빌드에 포함되었으므로

스프링 데이터가 생성한 모든 레포지토리 (데이터 JPA, 데이터 몽고) 의 REST API가 자동생성될 수 있도록 스프링 데이터REST가 자동-구성되기 때문이다.

스프링 데이터 REST가 생성하는 REST엔드포인트는 우리가 직접 생성한 것만큼 좋다 그리고 이 엔드포인트를 사용하렴녀 지금까지

생성했던 `@RestController`애노테이션을 지정된 모든 클래스에서 제거해야한다.
```
% curl localhost:8080/ingredients
{
  "_embedded" : {
    "ingredients" : [ {
      "name" : "Flour Tortilla",
      "type" : "WRAP",
      "_links" : {
        "self" : {
          "href" : "http://localhost:8080/ingredients/FLTO"
        },
        "ingredient" : {
          "href" : "http://localhost:8080/ingredients/FLTO"
        }
      }
    }, {
      "name" : "Corn Tortilla",
      "type" : "WRAP",
      "_links" : {
        "self" : {
          "href" : "http://localhost:8080/ingredients/COTO"
        },
        "ingredient" : {
          "href" : "http://localhost:8080/ingredients/COTO"
        }
      }
    }, {
      "name" : "Ground Beef",
      "type" : "PROTEIN",
      "_links" : {
        "self" : {
          "href" : "http://localhost:8080/ingredients/GRBF"
        },
        "ingredient" : {
          "href" : "http://localhost:8080/ingredients/GRBF"
        }
      }
    }, {
      "name" : "Carnitas",
      "type" : "PROTEIN",
      "_links" : {
        "self" : {
          "href" : "http://localhost:8080/ingredients/CARN"
        },
        "ingredient" : {
          "href" : "http://localhost:8080/ingredients/CARN"
        }
      }
    }, {
      "name" : "Diced Tomatoes",
      "type" : "VEGGINES",
      "_links" : {
        "self" : {
          "href" : "http://localhost:8080/ingredients/TMTO"
        },
        "ingredient" : {
          "href" : "http://localhost:8080/ingredients/TMTO"
        }
      }
    }, {
      "name" : "Lettuce",
      "type" : "VEGGINES",
      "_links" : {
        "self" : {
          "href" : "http://localhost:8080/ingredients/LETC"
        },
        "ingredient" : {
          "href" : "http://localhost:8080/ingredients/LETC"
        }
      }
    }, {
      "name" : "Cheddar",
      "type" : "CHEESE",
      "_links" : {
        "self" : {
          "href" : "http://localhost:8080/ingredients/CHED"
        },
        "ingredient" : {
          "href" : "http://localhost:8080/ingredients/CHED"
        }
      }
    }, {
      "name" : "Monterrey Jack",
      "type" : "CHEESE",
      "_links" : {
        "self" : {
          "href" : "http://localhost:8080/ingredients/JACK"
        },
        "ingredient" : {
          "href" : "http://localhost:8080/ingredients/JACK"
        }
      }
    }, {
      "name" : "Salsa",
      "type" : "SAUCE",
      "_links" : {
        "self" : {
          "href" : "http://localhost:8080/ingredients/SLSA"
        },
        "ingredient" : {
          "href" : "http://localhost:8080/ingredients/SLSA"
        }
      }
    }, {
      "name" : "Sour Cream",
      "type" : "SAUCE",
      "_links" : {
        "self" : {
          "href" : "http://localhost:8080/ingredients/SRCR"
        },
        "ingredient" : {
          "href" : "http://localhost:8080/ingredients/SRCR"
        }
      }
    } ]
  },
  "_links" : {
    "self" : {
      "href" : "http://localhost:8080/ingredients"
    },
    "profile" : {
      "href" : "http://localhost:8080/profile/ingredients"
    }
  }
}                                       
```
이렇게 빌드에 의존성만 지정했는데 엔드포인트는 물론이고 하이퍼링크까지 포함된 리소스도 얻게되었다. REST API가 자동생성되었기 때문이다.

또한 'Flour Tortilla'의 식자재 항목의 self 링크에 대해서도 클라이언트인 것처럼 curl을 사용해 GET요청할수있다.
```
curl http://localhost:8080/ingredients/FLTO
{
  "name" : "Flour Tortilla",
  "type" : "WRAP",
  "_links" : {
    "self" : {
      "href" : "http://localhost:8080/ingredients/FLTO"
    },
    "ingredient" : {
      "href" : "http://localhost:8080/ingredients/FLTO"
    }
  }
}
```

스프링 데이터 REST가 생성한 엔드포인트들은 GET은 물론 POST, PUT, DELETE메소드도 지원한다.

스프링 데이터 REST가 자동 생성한 API와 관련해서 한 가지 할일은 해당 API의 기본 경로를 설정하는 것이다.

해당 API의 엔드포인트가 작성한 모든 다른 컨트롤러와 충돌하지 않게 하기 위함이다. 스프링 데이터 REST가 자동생성한

API의 기본경로는 다음과 같이 spring.data.rest.base-path속성에 설정한다.
```
spring:
    data:
        rest:
            base-path: /api
```

여기서는 스프링 데이터 REST 엔드포인트의 기본 경로를 /apifh 설정하였으므로 이제는 식자재 엔드포인트가 /api/ingredients다.

```
% curl http://localhost:8080/api/tacos
{
    "timestamp":"2021-02-14T05:43:52.725+00:00",
    "status":404,"error":"Not Found",
    "message":"No message available",
    "path":"/api/tacos"
}%    
```
그러나 이 경우는 예상대로 수행되지 않았다. Ingredient와 IngredientRepository인터페이스의 경우는 스프링 데이터 REST가

/api/ingredients 엔드포인트를 노출시켰는데 taco와 TacoRepository의 경우 노출시키지 않는 이유는 무엇일까?

### 🌟리소스 경로와 관계이름 조정하기

실제로는 스프링 데이터 REST는 tacos라는 엔드포인트를 제공한다. 그러나 엔드포인트를 노출하는 방법이 문제다.

즉 스프링 데이터 레포지토리의 엔드포인트를 생성할 때 스프링 데이터 REST는 해당 엔드포인트와 관련된 엔티티 클래스 이름의 복수형을 사용한다.

따라서 Ingredient의 경우는 엔드포인트가 /ingredients가 되며 Order는 /orders가된다.

그러나 `taco`의 경우 복수형은 스프링데이터 REST가 `tacoes` 로 지정하므로 주의하자.

스프링 데이터 REST의 복수형 관련 문제점을 해결하려면 다음과 같이 애노테이션을 추가하면된다
```
@Data
@Entity
@RestResource(rel="tacos",path = "tacos")
public class Taco {
```

`RestResource`애노테이션을 지정하면 관계이름과 경로를 우리가 원하는 것으로 변경할 수 있다. 

### 🌟페이징과 정렬

모든 링크는 선택적 매개변수인 page, size, sort를 제공한다.

예를들어 페이지 크기가 5인 첫번째 페이지를 요청할 경우 다음 GET요청을 하면된다
    
    $ curl "localhost:8080/api/tacos?size=5"

5개이상의 타코가 있어서 다음과 같이 page매개변수를 추가하면 두번째 페이지의 타코를 요청할수있다

    $ curl "localhost:8080/api/tacos?size=5&page=1"

또한 sort를쓸경우 다음과 같이 사용할 수 있다.

    $ curl "localhost:8080/api/tacos?sort=createdAt,desc&page=0&size=12"

### 🌟커스텀 엔드포인트 추가하기

스프링 데이터 REST는 스프링 데이터 레포지토리의 CRUD 작업을 수행하는 엔드포인트 생성을 잘 하도록한다. 하지만 때로는 

기본적인 CRUD API로부터 탈피하여 우리 나름의 엔드포인트를 생성해야 할 때가있다

이 때 `@RestController` 애노테이션이 지정된 빈을 구현하여 스프링 데이터 REST가 자동생성하는 엔드포인트에 보충할수있다.

그러나 이때는 다음 두가지를 고려해서 API컨트롤러를 작성해야한다
[1] 엔트포인트 컨트롤러는 스프링 데이터 REST의 기본 경로로 매핑되지 않는다. 따라서 이 때는 스프링 데이터 REST의 기본 경로를

포함하여 우리가 원하는 기본 경로가 앞에 붙도록 매핑시켜야한다. 그러나 기본 경로가 변경될 때는 해당 컨트롤러의 매핑이 일치되도록 수정해야한다

[2] 컨트롤러에 정의한 엔드포인트는 스프링 데이터 REST 엔드포인트에서 반환되는 리소스의 하이퍼링크에 자동으로 포함되지 않는다. 이것은 클라이언트가

관계 이름을 사용해 커스텀 엔드포인트를 찾을 수 없다는 의미다.

---

먼저 기본 경로에 관한 문제를 해결해보자 스프링 데이터 REST는 `@RepositoryRestController`를 포함한다. 

이것은 스프링 데이터 REST엔드포인트에 구성되는 것과 동일한 기본 경로로 매핑되는 컨트롤러 클래스에 지정하는 새로운 애노테이션이다

간단히 말해 `@RepositoryRestController`가 지정된 컨트롤러의 모든 경로 매핑은 spring.data.rest.base-path 속성의 값이 앞에 붙은 경로를 갖는다

```
@RepositoryRestController
public class RecentTacosController {

    private TacoRepository tacoRepo;

    public RecentTacosController(TacoRepository tacoRepo){
        this.tacoRepo=tacoRepo;
    }

    @GetMapping(path = "/tacos/recent",produces = "application/hal+json")
    public ResponseEntity<CollectionModel<TacoResource>> recentTacos(){
        PageRequest page=PageRequest.of(0,12, Sort.by("createdAt").descending());

        List<Taco> tacos=tacoRepo.findAll(page).getContent();

        CollectionModel<TacoResource> tacoResources=  new TacoResourceAssembler().toCollectionModel(tacos);

        CollectionModel<TacoResource> recentResources=new CollectionModel<>(tacoResources); //책에 있는 방식대로 List로 하는것은 되지않는다.
        // 코드를 제공하지 않을뿐더라 강제형변환을 하더라도 에러가뜸 따라서 이렇게 타입을 변경함으로써 책이 원하는 답을 냄.

        recentResources.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(RecentTacosController.class).recentTacos())
        .withRel("recents"));
        return new ResponseEntity<>(recentResources, HttpStatus.OK);
    }
}
```

여기서 @GetMapping은 /tacos/recent경로로 매핑되지만, RecentTacosController클래스의 @RepositoryRestController애노테이션이

지정되어있으므로 맨앞에 스프링 데이터 REST의 기본경로가 추가된다 따라서 recentTacos()메소드는 /api/tacos/recent 의 GET요청을 처리하게된다.

여기서 한 가지 증요한것이있다. @RepositoryRestController는 @RestController와 이름이 유사하지만, @RestController와

동일한 기능을 수행하지않는다. 특히 @RepositoryRestController는 핸들러 메소드의 반환값을 요청 응답의 몸체에 자동으로 수록하지 않는다.

따라서 해당 메소드에 @ResponseBody애노테이션을 지정하거나 해당 메소드에서 응답데이터를 포함하는 ResponseEntity를 반환해야한다.

이제 실행되면 /api/tacos/recent의 Get요청을 할 때 가장 최근에 생성된 타코를 12개까지 반환한다. 그러나 /api/tacos를

요청할 때는 여전히 하이퍼링크 리스트에 나타나지않을것이다.. 이걸 해결해야한다!

### 🌟커스텀 하이퍼링크를 스프링 데이터 엔드포인트에 추가하기

최근에 생성된 타코의 엔드포인트가 /api/tacos에서 반환된 하이퍼링크 중에 없다면 클라이언트가 가장 최근  타코들을 가져오는

방법을 어떻게 알 수 있을까? 추론을하거나 하는 방식이나 하드코딩해야할것이다.

그러나 리소스 프로세서 빈을 선언하면 스프링 데이터 REST가 자동으로 포함시키는 링크리스트에 해당 링크를 추가할 수 있다.

스프링 데이터 HATEOAS는 RepresentationModelProcessor를 제공한다. 이것은 API를 통해 리소스가 반환되기 전에 리소스를 조작하는 인터페이스이다.
 
```
커스텀 링크를 스프링 데이터 REST 엔드포인트에 추가하기
@Configuration
public class SpringDataRestConfiguration {

    @Bean
    public RepresentationModelProcessor<PagedModel<EntityModel<Taco>>> tacoProcessor(EntityLinks links){
        return model -> {
            model.add(
                    links.linkFor(Taco.class)
                    .slash("recent")
                    .withRel("recents")
            );
            return model;
        };
    }
}

```

이경우 PagedNodel< EntityModel< Taco>>가 반환되면 가장 최근에 생성된 타코들의 링크를 받게되며 /api/tacos요청응답에도 해당 링크들이 포함된다.

# 🥇6장 요약
### [🌟1] REST엔드포인트는 스프링 MVC, 그리고 브라우저 지향의 컨트롤러와 동일한 프로그래밍

### 모델을 따르는 컨트롤러로 생성할 수 있다.

### 🌟[2] 모델과 뷰를 거치지않고 요청 응답 몸체에 직접 데이터를 쓰기 위해 컨트롤러의 핸들러 메소드에는 

### @ResponseBody애노테이션을 지정할 수 있으며, ResponseEntity객체를 반환할수있다

### 🌟[3] @RestController애노테이션을 컨트롤러에 지정하면 해당 컨트롤러의 각 핸들러

### 메소드에 @ResponseBody를 지정하지 않아도 되므로 컨트롤러를 단순화해준다

### 🌟[4] 스프링 HATEOAS는 스프링 MVC에서 반환되는 리소스의 하이퍼링크를 추가할 수 있게한다.

### 🌟[5]스프링 데이터 레포지토리는 스프링 데이터 REST를사용하는 REST API로 자동노출될 수 있다.


## 💕실행방법
	
	1. mvnw clean package
	2. cd chap6\taco-cloud\target
	3. java -jar taco-cloud-0.0.1-SNAPSHOT.jar

## 💕책과 달리 최신버젼의 스프링부트 코드로 하였으며 서브모듈을 오직 두개로하였습니다.

























