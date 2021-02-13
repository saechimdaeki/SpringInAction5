# 6장 REST 서비스 생성하기
## 이 장에서 배우는 내용
- 스프링 MVC에서 REST 엔드포인트 정의하기
- 하이퍼링크 REST 리소스 활성화하기
- 레포지토리 기반의 REST엔드포인트 자동화


이번 장에서는 스프링을 사용해서 타코클라우드 애플리케이션에 REST API를 제공할것이다. 이때 스프링 MVC 컨트롤러를 사용해서

REST 엔드포인트를 생성하기 위해 2장에서 배웠던 MVC를 사용한다. 또한, 4장에서 정의했던 스프링 데이터 레포지토리의 REST

엔드포인트도 외부에서 사용할 수 있게 자동으로 노출시킨다. 마지막으로, 그런 엔드포인트를 테스트하고 안전하게 만드는 법을 알아본다

## REST컨트롤러 작성하기

이 책에서는 앵귤러 프레임워크를 사용해서 SPA로 프론트엔드를 구축한다. 이책의 목적은 앵귤러가아니므로 핵심은 백엔드

스프링 코드에 초점을 둘 것이다.

### 서버에서 데이터 가져오기

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

### 서버에 데이터 전송하기 

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

### 서버의 데이터 변경하기

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

### 서버에서 데이터 삭제하기 

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

##







































