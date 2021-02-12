# 4장 스프링 시큐리티

### 이 장에서 배우는 내용
- 스프링 시큐리티(Spring Security) 자동 구성하기
- 커스텀 사용자 스토리지 정의하기
- 커스텀 로그인 페이지 만들기
- CSRF공격으로부터 방어하기
- 사용자 파악하기

### 스프링 시큐리티 활성화하기
스프링 애플리케이션의 보안에서 맨 먼저 할 일은 스프링 부트 보안 스타터 의존성을 빌드 명세에 추가하는 것이다

다음과 같이 pom.xml에 < dependency>를 추가하자.
```
    <dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-security</artifactId>
	</dependency>
	
	<dependency>
		<groupId>org.springframework.security</groupId>
		<artifactId>spring-security-test</artifactId>
		<scope>test</scope>
	</dependency>
```

이제 웹 브라우저에서 (localhost:8080 이나 localhost:8080/design)에 접속하면

스프링 시큐리티에서 제공하는 HTTP기본 인증 대화상자가 나타난다

Username필드에 user Password는 다음과 같이 무작위로 생성된다
```
Using generated security password: 8b24f368-772e-413d-93e4-5f00e454ac53
```

이것으로 타코 클라우드 애플리케이션이 안전해졋다. 그러나 스프링 시큐리티는 지금부터 시작이다

우선 어떤 보안구성이 자동으로 제공되는지 알아보자

#### 이미 했듯 보안스터트를 프로젝트 빌드파일에 추가만 했을때는 다음의 보안구성이 제공된다.
 
- 모든 HTTP요청 경로는 `인증(authentication)`되어야 한다
- 어떤 특정 역할이나 권한이 없다
- 로그인 페이지가 따로 없다
- 스프링 시큐리티의 HTTP 기본인증을 사용해서 인증된다
- 사용자는 하나만 있으며, 이름은 user다. 비밀번호는 암호화해준다.

이제 최소한 다음 기능을 할 수 있도록 스프링 시큐리티를 구성하자
- 스프링 시큐리티의 HTTP인증 대화상자 대신 우리의 로그인 페이지로 인증하자
- 다수의 사용자를 제공하며, 새로운 타코클라우드 고객이 사용자로 등록할 수 있는 페이지가 있어야한다
- 서로 다른 HTTP요청 경로마다 서로 다른 보안 규칙을 적용한다(예를들어 홈페이지는 인증필요 x)

### 스프링 시큐리티 구성하기

장황한 xml기반의 구성을 포함해 그동안 스프링 시큐리티를 구성하는 방법은 여러가지가 있었다.

다행히도 최근에는 자바기반의 구성을 지원한다. 다음과같이 구성클래스를 작성하자
```
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
		.antMatchers("/design","/orders")
		.access("hasRole('ROLE_USER')")
		.antMatchers("/","/**").access("permitAll")
		.and()
		.httpBasic();
	}
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.inMemoryAuthentication()
		.withUser("user1")
		.password("${noop}password1")
		.authorities("ROLE_USER")
		.and()
		.withUser("user2")
		.password("{noop}password2")
		.authorities("ROLE_USER");
	}
}
```

`Securityconfig`클래스는 무엇을 하는 걸까? 간단히 말해 사용자의 HTTP요청 경로에

대해 접근 제한과 같은 보안 관련 처리를 우리가 원하는대로 할 수 있게 해준다.

이제 설정을 마쳤으니 http://localhost:8080/design에 접속해보자.

스프링 시큐리티의 HTTP기본인증 대화 상자 대신 다른 HTTP로그인 대화상자를 볼것이다.

사용자의 이름에는 user1, 비밀번호에는 password1을 입력하고 로그인하면 타코디자인폼이 나올것이다.

타코 클라우드 애플리케이션의 로그인 페이지를 생성하고 보안을 구성하기에 앞서 먼저 알아 둘 것이 있다.

즉 한 명 이상의 사용자를 처리할 수 있도록 사용자 정보를 유지관리하는 사용자 스터오를 구성하는 것이다.

스프링 시큐리티 에서는 여러가지의 사용자 스토어 구성 방법을 제공한다
- 인메모리 사용자 스토어
- JDBC기반 사용자 스토어 
- LDAP기반 사용자 스토어
- 커스텀 사용자 명세 서비스

앞서 보듯, SecurityConfig 클래스는 보안 구성클래스인 `WebSecurityConfigurerAdpater`의 서브클래스이다.

그리고 두개의 configure()메소드를 오버라이딩 하고있다.

`configure(HttpSecurity)`는 HTTP보안을 구성하는 메소드이며

`configure(AuthenticationManagerBuilder)`는 사용자 인증정보를 구성하는 메소드이다.

### 인메모리 사용자 스토어
사용자 정보를 유지,관리할 수 있는 곳 중하나가 인메모리이다. 만일 변경이 필요 없는 사용자만 정해놓고 

애플리케이션을 사용한다면 아예 보안구성코드 내부에 정의할 수 있을것이다.
```
@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.inMemoryAuthentication()
		.withUser("user1")
		.password("${noop}password1")
		.authorities("ROLE_USER")
		.and()
		.withUser("user2")
		.password("{noop}password2")
		.authorities("ROLE_USER");
	}
```

`withUser()`를 호출하면 해당 사용자의 구성이 시작되며. 사용자의 이름을 인자로 전달한다.

반면에 비밀번호와 부여권한은 각각 `password()`와 `authorities()`메ㅔ소드의 인자로 전달하요 호출한다.

그리고 `and()`메소드로 연속해서 `withUser()`를 호출하여 여러 사용자를 지정할수있다.


### 스프링5부터는 반드시 비밀번호를 암호화하므로 만일 password()메소드를 호출

### 하여 암호화 하지않으면 접근거부 또는 Internal Server Error가발생된다.

인메모리 사용자 스토어는 테스트 목적이나 간단한 어플리케이션에는 편리하다.

그러나 사용자 정보의 추가나 변경이 쉽지않다. 즉, 사용자 추가,삭제,변경해야한다면

보안 구성 코드를 변경한 후 애플리케이션을 다시 빌드하고 배포,설치해야한다.

### JDBC 기반의 사용자 스토어

사용자 정보는 관계형 데이터베이스로 유지,관리 되는경우가 많으므로 JDBC사용자스토어가 적합해보인다.

다음 코드를 추가해보자.
```
    @Autowired
	DataSource dataSource;

    @Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.jdbcAuthentication()
		.dataSource(dataSource);
    }
```

configure()에서는 AuthenticationManagerBuilder의 jdbcAuthentication()을 호출한다.

이때 데이터베이스를 액세스하는 방법을 알 수 있도록 dataSource()메소드를 호출해 DataSource도설정해야한다.

sql을 작성하고 사용자이름에 user1,비밀번호에 password1을입력하면 에러가 발생할것이다.

앞에서 데이터베이스는 정상생성되었지만, 비밀번호를 암호화하지않았기에 발생하는 오류이다.

스프링 시큐리티 5버전부터는 의무적으로 PasswordEncoder를 사용해서 비밀번호를 암호화해야되기 때문이다.

먼저 사용자정보 쿼리를 커스터마이징해보자
```
auth.jdbcAuthentication()
		.dataSource(dataSource)
		.usersByUsernameQuery("select username, password, enable from users "
				+ "where username=?")
		.authoritiesByUsernameQuery("select username, authority from authorities "
				+ "where username=?");
```

이 쿼리에서 사용하는 테이블의 이름은 스프링 시큐리티의 기본 데이터베이스 테이블과 달라도된다.

그러나 테이블이 갖는 열의 데이터타입과 길이는 일치해야한다.

### 스프링 시큐리티의 기본 SQL쿼리를 대체할때는 다음의 사항을 지켜야한다
- 매개변수는 하나이며, username이어야한다
- 사용자 정보 인증 쿼리에서는 username,password,enabled열의 값을 반환해야한다.
- 사용자권한 쿼리에서는 해당 사용자 이름과 부여된권한을 포함하는 0또는 다수의행을 반환할수있다.
- 그룹권한쿼리에서는 각각 그룹id,그룹이름,권한 열을 갖는 0또는 다수의 행을반환할수있다.

## 암호화된 비밀번호 사용하기
비밀번호를 데이터베이스에 저장할 때와 사용자가 입력한 비밀번호는 같은 암호화알고리즘을 사용해야한다.

비밀번호를 암호화 할때는 다음과같이 passwordEncoder()메ㅔ소드를 호출해 비밀번호인코더를지정한다.
```
.authoritiesByUsernameQuery("select username, authority from authorities "
				+ "where username=?").passwordEncoder(new BCryptPasswordEncoder());
```

passwordEncoder()메소드는 스프링 시큐리티의 PasswordEncoder인터페이스를 구현한 어떤 객체도 인자로받을수있다.

- BCryptPasswordEncoder: bcrypt를 해싱 암호화한다
- NoOpPasswordEncoder: 암호화하지않는다
- Pbkdf2PasswordEncoder: PBKDF2를 암호화한다.
- SCryptPasswordEncoder: scrpyt를 해싱암호화한다
- StandardPasswordEncoder: SHA-256을 해싱암호화한다

이제 현상태에서 localhost:8080/design에 접속해 user1, password1로 로그인을해보자.

대화상자만 다시 나타날것이다. 

이유는 데이터베이스에 저장된 비밀번호는 암호화되지 않았지만 로그인 대화상자에 

입력한 비밀번호는 암호화되어서 두 값이 다른것으로 간주되어 로그인이 실패하기때문이다.

따라서 현재까지 작성한 configure()메소드가 데이터베이스의 사용자 정보를 읽어서

제대로 인증을 하는지 확인해보려면 역설적이지만 PasswordEncoder 인터페이스를

구현하되 비밀번호를 암호화하지않는 클래스를 임시로 작성하고 사용해야한다
```
public class NoEncodingPasswordEncoder implements PasswordEncoder {
	
	@Override
	public String encode(CharSequence rawPassword) {
		return rawPassword.toString();
	}
	
	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		return rawPassword.toString().equals(encodedPassword);
	}
}
```

따라서 앞서 BCrypt부분을 작성한 해당클래스로 변경하자. 이방법은 코드를 테스트할때만 임시로사용한다..!!!!

궁극적으로 사용자정보는 데이터베이스에서 유지, 관리할것이다. 그러나 JDBC기반으로

인증하는 jdbcAuthentication()대신 다른인증방법을 사용할것이지만 그전에 LDAP부터알아보자.

### LDAP기반 사용자 스토어

LDAP기반 인증으로 스프링 시큐리티를 구성하기 위헤서 ldapAuthentication()메소드를 사용할수있다.

이메소드는 LDAP를 jdbcAuthentication()처럼 사용할수있게해준다.
```
auth.ldapAuthentication()
		.userSearchBase("ou=people")
		.userSearchFilter("(uid={0}")
		.groupSearchBase("ou=groups")
		.groupSearchFilter("member={0}")
		.passwordCompare();
```

`userSearchFilter()`와 `groupSearchFilter()`메소드는 LDAP기본 쿼리의

필터를 제공하기 위해 사용되며, 여기서는 사용자와 그룹을 검색하기 위해 사용했다.

`userSearchBase()`메소드는 사용자를 찾기위한 기준점 쿼리를 제공하며 비밀번호를 비교하는 방법으로 LDAP

인증을 하고자할때는 passwordCompare()메소드를 호출하면된다.


일단은 먼저 사용자 정보를 저장하는 도메인 객체와 레포지토리 인터페이스부터 생성하자


### 사용자 개체 정의하기

```
@Entity
@Data @NoArgsConstructor(access = AccessLevel.PRIVATE,force = true)
@RequiredArgsConstructor
public class User implements UserDetails{
	private static final long serialVersionUID=1L;
	
	@Id
	@GeneratedValue(strategy =GenerationType.AUTO)
	private Long id;
	
	private final String username;
	
	private final String password;
	
	private final String fullname;
	
	private final String street;
	
	private final String city;
	
	private final String state;
	
	private final String zip;
	
	private final String phoneNumber;
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
	}
	
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}
	
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}
	
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}
	
	@Override
	public boolean isEnabled() {
		return true;
	}
	
}

```

몇가지 속성정의와 더불어 User클래스는 스프링 시큐리티의 UserDetails인터페이스를 구현한다.

### 사용자 명세 서비스 생성하기

스프링시큐리티의 UserDetailsService는 다음과 같이 간단한 인터페이스이다

```
public interface UserDetailsService{
    UserDetails loadUserByUsername(String username) thorws UsernameNotFoundException;
}
```
이 코드를 보면 알 수 있듯이, 이 인터페이스를 구현하는 클래스의 메소드에 사용자 이름이 인자로 

전달되며, 메소드 실행 후 UserDetails객체가 반환되거나 이름이없다면 exception발생시킨다.


### 커스텀 사용자 명세 서비스 정의하기
```
@Service
public class UserRepositoryUserDetailsService implements UserDetailsService{
	private UserRepository userRepo;
	
	@Autowired
	public UserRepositoryUserDetailsService(UserRepository userRepo) {
		this.userRepo=userRepo;
	}
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user=userRepo.findByUsername(username);
		if(user!=null)
			return user;
		throw new UsernameNotFoundException("User " +username +"not found");
	}
}
```

`UserRepositoryUserDetailsService`에서는 생성자를 통해 UserRepository가 주입된다.

또한 `@Service`애노테이션이 지정되어 컴포넌트 검색을해 자동으로 빈으로 생성된다.

이제 이를 SecurityConfig클래스의 configure()메소드에서 사용해보자
```
    @Autowired
	private UserDetailsService userDetailsService;

    @Bean
	public PasswordEncoder encoder() {
		return new BCryptPasswordEncoder();
	}

    @Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(encoder());
    }

```

여기서 configure()제일 마지막 코드에 주목하면 단순히 encoder()메소드를 호출한후 반환값을 

passwordEncoder()의 인자로 전달하는 것처럼 보이지만 실제로는 다음절차로 실행된다.

즉, encoder()에 @Bean 애노테이션이 지정되었으므로, encoder()메소드가 생성한 BCryptPasswordEncoder 인스턴스가

스프링 애플리케이션 컨텍스트에 등록, 관리되며, 이 인스턴스가 애플리케이션 컨텍스트로부터 주입되어 반환된다.

이렇게 함으로써 우리가 원하는 종류의 PasswordEncoder 빈 객체를 스프링의 관리하에 사용할 수 있다.


이제 이를 사용하기 위해 컨트롤러를 작성하자
```
@Controller
@RequestMapping("/register")
public class RegistrationController {
	private UserRepository userRepo;
	private PasswordEncoder passwordEncoder;
	
	public RegistrationController(UserRepository userRepo, PasswordEncoder passwordEncoder) {
		this.userRepo=userRepo;
		this.passwordEncoder=passwordEncoder;
	}
	
	@GetMapping
	public String registerForm() {
		return "registration";
	}
	
	@PostMapping
	public String processRegistration(RegistrationForm form) {
		userRepo.save(form.toUser(passwordEncoder));
		return "redirect:/login";
	}	
}

@Data
public class RegistrationForm {
	private String username;
	private String password;
	private String fullname;
	private String street;
	private String city;
	private String state;
	private String zip;
	private String phone;
	
	public User toUser(PasswordEncoder passwordEncoder) {
		return new User(
			username,passwordEncoder.encode(password),
			fullname,street,city,state,zip,phone
				);				
	}
}

```

이렇게 사용자 등록과 인증 지원이 완성되었다. 그러나 지금은 어플리케이션을 시작해도 등록페이지를 볼 수 없다.

기본적으로 모든 웹 요청은 인증이 필요하기 때문이다. 이문제를 해결하기위해 웹 요청의 보안을처리해보자

## 웹 요청 보안 처리하기

타코를 디자인하거나 주문하기전에 사용자를 인증해야 한다는것이 타코클라우드 애플리케이션의 요구사항이다.

그러나 홈페이지, 로그인페이지, 인증되지않은 모든 사용자가 사용할수있어야한다.

이런 보안규칙을 작성하려면 다음처럼 configure(HttpSecurity)매소드를 오버라이딩 해야한다.

```
@Override
protected void configure(HttpSecurity http) throws Exception{
    ...
}
```

이 configure 메소드는 `HttpSecurity`객체를 인자로 받는다. 이 객체는 웹 수준에서 보안을 처리하는 방법을 구성하는데 사용한다.
- HTTP요청 처리를 허용하기 전에 충족되어야 할 특정 보안 조건을 구성한다.
- 커스텀 로그인 페이지를 구성한다
- 사용자가 애플리케이션의 로그아웃을 할 수 있도록 한다.
- CSRF공격으로부터 보호하도록 구성한다.

### /design과 /orders 요청은 인증된 사용자에게만 허용되어야한다.

### 그리고 이외 모든 다른 요청은 모든 사용자에게 허용되어야한다. 따라서 다음과같이 변경하자

```
...
@Override
protected void configure(HttpSecuirty http) throws Exception {
    http.authorizeRequests()
        .antMatchers("/design", "/orders")
        .hasRole("ROLE_USER")
        .antMatchers("/", "/**").permitAll();
}
```

`authorizeRequests()`는 ExpressionInterceptUrlRegistry 객첼ㄹ 반환한다.

이 객체를 사용하면 URL 경로와 패턴 및 해당 경로의 보안 요구사항을 구성할수있으며 위에서는 두가지규칙을 지정했다.
- /design과 /orders의 요청은 ROLE_USER의 권한을 갖는 사용자에게만 허용된다.
- 이외의 모든 요청은 모든 사용자에게 허용된다.

hasRole()과 permittAll()은 요청 경로의 보안 요구를 선언하는 메소드이다. 아래는
사용가능한 모든 메소드를 보여준다

### 요청경로가 보안 처리되는 방법을 정의하는 구성 메소드
|메소드|하는일|
|:-:|:-:|
|access(String)|인자로 전달된 SpEL표현식이 true면 접근을허용한다|
|anonymous()|익명의 사용자에게 접근을 허용한다|
|authenticated()|익명이 아닌 사용자로 인증된 경우 접근을 허용한다|
|denyAll()|무조건 접근을 거부한다|
|fullyAuthenticated()|익명이 아니거나 또는 remember-me가 아닌 사용자로 인증되면 접근을 허용한다|
|hasAnyAuthority(String...)|지정된 권한 중 어떤것이라도 사용자가 갖고있으면 접근을 허용한다|
|hasAnyRole(String...)|지정된 역할 중 어느하나라도 사용자가 갖고있으면 접근을 허용한다|
|hasAuthority(String)|지정된 권한을 사용자가 갖고있으면 접근을 허용한다|
|hasIpAddress(String)|지정된 IP주소로부터 요청이 오면 접근을 허용한다|
|hasRole(String)|지정된 역할을 사용자가 갖고있으면 접근을 허용한다|
|not()|다른 접근 메소드들의 효력을 무효화한다|
|permitAll()|무조건 접근을 허용한다|
|rememberMe()|rememberme(이전 로그인 정보를 쿠키나 데이터베이스로 저장한 후 일정 기간내에 다시 접근시 저장된 정보로 자동로그인됨) 을 통해 인증된 사용자의 접근을 허용한다|

---

위의 표에있는 대부분의 메소드는 요청 처리의 기본적인 보안규칙을 제공한다. 그러나 각 메소드에

정의된 보안 규칙만 사용된다는 제약이 있다. 따라서 이의 대안으로 access()메소드를 사용하면 더 

풍부한 보안규칙을 선언하기 위해 SpEL을 사용할 수 있다. 스프링 시큐리티에서는 SpEL을확장하여 특정값과 함수를갖고있다

### 스프링 시큐리티에서 확장된 SpEL

|보안 표현식|산출 결과|
|:-:|:-:|
|authentication|해당 사용자의 인증 객체|
|denyAll|항상 false를 산출한다|
|hasAnyRole(역할 내역)|지정된 역할 중 어느 하나라도 해당사용자가 갖고있으면true|
|hasRole(역할)|지정된 역할을 해당 사용자가 갖고있으면 true|
|hasIpAddress(Ip주소)|지정된 IP주소로부터 해당 요청이 온것이면 true |
|isAnonymous()|해당 사용자가 익명 사용자면 true|
|isAuthenticated()|해당사용자가 익명이 아닌 사용자가 인증되었으면 true|
|isFullyAuthenticated()|해당 사용자가 익명이 아니거나 또는 remember-me가 아닌 사용자로 인증되었으면 true|
|isRememberMe()|해당 사용자가 remember-me 기능으로 인증되었으면 true|
|permitAll|항상 true를 산출한다|
|principal|해당사용자의 principal 객체|

예를들어 access()메소드를 hasRole() 및 permitAll 표현식과 함께 사용하면 아래와 같이 configure()메소드를 다시작성할수있다

#### 스프링 표현식을 사용해서 인증 규칙 정의하기

```
@Override
protected void configure(HttpSecurity http )throws Exception{
    http.authorizeRequests()
    .antMatchers("/design","/orders")
    .access("hasRole('ROLE_USER')")
    .antMatchers("/","/**").access("permitAll");
}
```

또 추가적으로 예를들어, 화요일의 타코생성은 ROLE_USER권한을 갖는 사용자에게만 허용하고싶다면
```
@Override
protected void configure(HttpSecurity http )throws Exception{
    http.authorizeRequests()
    .antMatchers("/design","/orders")
    .access("hasRole('ROLE_USER') && "+
    "T(java.util.Calendar).getInstance().get("+
    "T(java.util.Calendar).DAY_OF_WEEK) == " +
    "T(java.util.Calendar).TUESDAY")
    .antMatchers("/","/**").access("permitAll");
}
```

이렇게 `SpEL`을 사용하면 가능성은 무궁무진하므로 어떤 보안 규칙도 작성할수있다.

### 커스텀 로그인 페이지 생성하기

기본 로그인 페이지를 교체하려면 우선 커스텀 로그인 페이지가 있는 경로를 스프링 시큐리티에 알려주어야한다.

이것은 `configure(HttpSecurity)` 메소드의 인자로 전달되는 HttpSecurity 객체의
formLogin()을 호출해서할수있다.

```
http
		.authorizeRequests()
		.antMatchers("/design","/orders")
		.access("hasRole('ROLE_USER')")
		.antMatchers("/","/**")
		.access("permitAll")
		.and()
		.formLogin()
		.loginPage("/login")
```

formLogin()호출 코드 앞에 `and()`호출을 추가해 인증 구성 코드와 연결시킨다는것에 유의하자

`and()`메소드는 인증구성이 끝나서 추가적인 HTTP구성을 적용할 준비가 되었다는 것을나타낸다.

and()는 새로운 구성을 시작할때마다 사용할수있다.

`formLogin()`은 우리의 커스텀로그인폼을 구성하기 위해 호출한다.

그다음에 호출하는 `loginPage()`에는 커스텀 로그인 페이지의 경로를 지정한다. 그러면 사용자가 인증되지않아

로그인이 필요하다고 스프링 시큐리티가 판단할때 해당경로로 연결해준다.

기본적으로 스프링 시큐리티는 /login 경로로 로그인 요청을 처리하며, 사용자 이름과 비밀번호 필드의

이름은 username과 password로 간주한다. 그러니 아것은 우리가 구성할수있다. 예를들어
```
.and()
.formLogin()
.loginPage("/login")
.loginProcessingUrl("/authenticate")
.usernameParameter("user")
.passwordParameter("pwd")
```
이 경우 스프링 시큐리티는 /authenticate 경로의 요청으로 로그인을 처리한다. 그리고

사용자 이름과 비밀번호 필드의 이름도 user와 pwd가된다.

로그인하면 해당 사용자의 로그인이 필요하다고 스프링 시큐리티가 판단했을 당시에 사용자가 머물던

페이지로 바로 이동한다. 그러나 사용자가 직접 로그인 페이지로 이동했을 경우는 로그인한 후 루트경로로 이동한다.

하지만 로그인후 이동할 페이지를 다음과같이 변경할 수 있다.
```
.and()
.formLogin()
.loginPage("/login")
.defaultSuccessUrl("/design")
```
이 경우는 사용자가 직접 로그인 페이지 이동후 로그인 성공적으로했다면 /design페이지로 이동할것이다.

또한 로그인전 어떤 페이지에 있었는지와 무관하게 로그인 후에는 무조건 /design 페이지로 이동하도록 할 수 있다. 

이때는 `defaultSuccessUrl`의 두번째 인자로 true를 전달하면 된다
```
.and()
.formLogin()
.loginPage("/login")
.defaultSuccessUrl("/design",true)
```

로그아웃도 로그인처럼 중요하다. 로그아웃을 하기 위해서는 HttpSecurity객체의 logout을 호출해야한다
```
and()
.logout()
.logoutSuccessUrl("/")
```
사용자가 로그아웃을하면 기본적으로는 로그인페이지로 다시 이동된다. 그러나 다른페이지로 이동시키고

싶다면 로그아웃 이후에 이동할 페이지를 지정하여 `logoutSuccessUrl()`을 호출하면된다.

### CSRF공격 방어하기
CSRF(크로스사이트요청위조)는 많이 알려진 보안공격이다.

즉 사용자가 웹사이트에 로그인한 상태에서 악의적인 코드가 삽입된 페이지를 열면 공격대상이 되는 웹사이트에

자동으로 폼이 제출되고 이사이트는 위조된 공격명령이 믿을 수 있는 사용자로부터 제출된 것으로 판단하게 되어 공격에노출된다.

CSRF공격을 막기 위해 애플리케이션에서는 폼의 숨김 필드에 넣을 CSRF토큰을생성할수있다.

그리고 해당 필드에 토큰을 넣은 후 나중에 서버에서 사용한다. 이후에 해당 폼이 제출될 때는

폼으 다른데이터와 함께 토큰도 서버로 전송된다. 그리고 서버에서는 이 토큰을 원래 생성되었던

토큰과 비교하며, 토큰이 일치하면 해당 요청의 처리가 허용된다.

다행히 스프링시큐리티에는 내장된 CSRF방어 기능이있다. 또한 이 기능이 기본으로 활성화되어 있어서 별도로 구성할필요없다.

단지 CSRF토큰을 넣을 _csrf라는 이름의 필드를 애플리케이션이 제출하는 폼에 포함시키면된다.

---

이제 Order 클래스에 새로운 속성을 추가해보자
```
@ManyToOne
private User user;
```
user속성의 @ManyToOne애노테이션은 한 건의 주문이 한 명의 사용자에 속한다는것을나타낸다.

그리고 반대로 한명의 사용자는 여러주문을 가질 수 있다.

주문을 처리하는 OrderController에서는 processOrder()메소드가 주문을 저장하는 일을 수행한다.

따라서 인증된 사용자가 누구인지 결정한후, Order 객체의 setUser()를 호출하여 해당 주문을 사용자와 

연결하도록 processOrder()메소드를 수정해야한다.

사용자가 누구인지 결정하는 방법은 여러가지 있으며 가장 많이사용되는 방법은 다음과같다.
- Principal 객체를컨트롤러 메소드에 주입한다
- Authentication 객체를 컨트롤러 메소드에 주입한다
- SecurityContextHolder를 사용해서 보안 컨텍스트를 얻는다.
- @AuthenticationPrincipal 애노테이션을 메소드에 지정한다

따라서 OrderController를 다음과 같이 변경하자
```
@PostMapping
	public String processOrder(@Valid Order order, Errors errors, SessionStatus sessionStatus
			,@AuthenticationPrincipal User user) {
		if(errors.hasErrors()) {
			return "orderForm";
		}
		//log.info("Order submitted: " + order);
		order.setUser(user);
		orderRepo.save(order);
		sessionStatus.setComplete();
		return "redirect:/";
	}
```

`@AutehnticationPrincipal`의 장점은 타입 변환이 필요없고 Authentication과 동일하게

보안 특정코드만을 갖는다. 일단, User객체가 processOrder()에 전달되면 해당 주문에서 사용할 준비가 된것이다.

OrderController의 orderForm()변경
```
	@GetMapping("/current")
	public String orderForm(@AuthenticationPrincipal User user, @ModelAttribute Order order) {
		//model.addAttribute("order",new Order());
		if(order.getDeliveryName()==null)
			order.setDeliveryName(user.getFullname());
		if(order.getDeliveryStreet()==null)
			order.setDeliveryStreet(user.getStreet());
		if(order.getDeliveryCity()==null)
			order.setDeliveryCity(user.getCity());
		if(order.getDeliveryState()==null)
			order.setDeliveryState(user.getState());
		if(order.getDeliveryZip()==null)
			order.setDeliveryZip(user.getZip());
		
		return "orderForm";
	}
```
이렇게하면 주문의 GET요청이 제출될 때 해당 사용자의 이름과 주소가 미리 채워진 상태로 주문폼이 전송될수있다.
## 이제 스프링 시큐리티를 적용한 taco-cloud가 되었다

# 4장 요약
#### [1] 스프링 시큐리티의 자동-구성은 보안을 시작하는데 좋은 방법이다. 그러나 대부분의 애플리케이션에서는

#### 나름의 보안 요구사항을 충족하기 위해 별도의 보안 구성이 필요하다

#### [2] 사용자 정보는 여러 종류의 사용자 스토어에 저장되고 관리될 수 있다.

#### [3] 스프링 시큐리티는 자동으로 CSRF공격을 방어한다

#### [4] 인증된 사용자에 관한 정보는 SecurityContext객체를 통해서 얻거나 `@AuthenticationPrincipal`을 사용해 컨트롤러에 주입하면된다.