# SpringSecurity基本原理
## 引言
```text
    应用Spring Security到项目有一段时间了,特此讲探索过程记录如下
```

## 项目版本
```xml
<!--SpringBoot2.1.14-->
<pom>
<artifactId>spring-boot-starter-security</artifactId>
<artifactId>spring-boot-starter-web</artifactId>
</pom>
```

## 1过滤器链FilterChainProxy
### 1.1分析日志
```text
    logging:
      level:
        org.springframework.security: DEBUG
```
日志大致如下:
```text
2020-05-13 13:59:34.955 DEBUG 31516 --- [nio-8080-exec-1] o.s.security.web.FilterChainProxy        : /hello at position 1 of 15 in additional filter chain; firing Filter: 'WebAsyncManagerIntegrationFilter'
2020-05-13 13:59:34.955 DEBUG 31516 --- [nio-8080-exec-1] o.s.security.web.FilterChainProxy        : /hello at position 2 of 15 in additional filter chain; firing Filter: 'SecurityContextPersistenceFilter'
2020-05-13 13:59:34.957 DEBUG 31516 --- [nio-8080-exec-1] o.s.security.web.FilterChainProxy        : /hello at position 3 of 15 in additional filter chain; firing Filter: 'HeaderWriterFilter'
2020-05-13 13:59:34.957 DEBUG 31516 --- [nio-8080-exec-1] o.s.security.web.FilterChainProxy        : /hello at position 4 of 15 in additional filter chain; firing Filter: 'CsrfFilter'
2020-05-13 13:59:34.958 DEBUG 31516 --- [nio-8080-exec-1] o.s.security.web.FilterChainProxy        : /hello at position 5 of 15 in additional filter chain; firing Filter: 'LogoutFilter'
2020-05-13 13:59:34.958 DEBUG 31516 --- [nio-8080-exec-1] o.s.security.web.FilterChainProxy        : /hello at position 6 of 15 in additional filter chain; firing Filter: 'UsernamePasswordAuthenticationFilter'
2020-05-13 13:59:42.659 DEBUG 31516 --- [nio-8080-exec-1] o.s.security.web.FilterChainProxy        : /hello at position 7 of 15 in additional filter chain; firing Filter: 'DefaultLoginPageGeneratingFilter'
2020-05-13 13:59:42.659 DEBUG 31516 --- [nio-8080-exec-1] o.s.security.web.FilterChainProxy        : /hello at position 8 of 15 in additional filter chain; firing Filter: 'DefaultLogoutPageGeneratingFilter'
2020-05-13 13:59:42.659 DEBUG 31516 --- [nio-8080-exec-1] o.s.security.web.FilterChainProxy        : /hello at position 9 of 15 in additional filter chain; firing Filter: 'BasicAuthenticationFilter'
2020-05-13 13:59:42.659 DEBUG 31516 --- [nio-8080-exec-1] o.s.security.web.FilterChainProxy        : /hello at position 10 of 15 in additional filter chain; firing Filter: 'RequestCacheAwareFilter'
2020-05-13 13:59:42.660 DEBUG 31516 --- [nio-8080-exec-1] o.s.security.web.FilterChainProxy        : /hello at position 11 of 15 in additional filter chain; firing Filter: 'SecurityContextHolderAwareRequestFilter'
2020-05-13 13:59:42.660 DEBUG 31516 --- [nio-8080-exec-1] o.s.security.web.FilterChainProxy        : /hello at position 12 of 15 in additional filter chain; firing Filter: 'AnonymousAuthenticationFilter'
2020-05-13 13:59:42.661 DEBUG 31516 --- [nio-8080-exec-1] o.s.s.w.a.AnonymousAuthenticationFilter  : Populated SecurityContextHolder with anonymous token: 'org.springframework.security.authentication.AnonymousAuthenticationToken@3db8c1b1: Principal: anonymousUser; Credentials: [PROTECTED]; Authenticated: true; Details: org.springframework.security.web.authentication.WebAuthenticationDetails@b364: RemoteIpAddress: 0:0:0:0:0:0:0:1; SessionId: null; Granted Authorities: ROLE_ANONYMOUS'
2020-05-13 13:59:42.661 DEBUG 31516 --- [nio-8080-exec-1] o.s.security.web.FilterChainProxy        : /hello at position 13 of 15 in additional filter chain; firing Filter: 'SessionManagementFilter'
2020-05-13 13:59:42.662 DEBUG 31516 --- [nio-8080-exec-1] o.s.security.web.FilterChainProxy        : /hello at position 14 of 15 in additional filter chain; firing Filter: 'ExceptionTranslationFilter'
2020-05-13 13:59:42.662 DEBUG 31516 --- [nio-8080-exec-1] o.s.security.web.FilterChainProxy        : /hello at position 15 of 15 in additional filter chain; firing Filter: 'FilterSecurityInterceptor'
2020-05-13 13:59:42.727 DEBUG 31516 --- [nio-8080-exec-1] o.s.s.w.a.ExceptionTranslationFilter     : Access is denied (user is anonymous); redirecting to authentication entry point
```
### 1.2过滤器分析
从日志信息中可以看多很多过滤器,选择重要的过滤器分析
```
请求 --->                                                                                                                                                     |   |
            UsernamePasswordAuthenticationFilter    -       BasicAuthenticationFilter   -   ExceptionTranslationFilter   - FilterSecurityInterceptor       |API-SERVER|
响应 <---                                                                                                                                                     |   |   
```

#### UsernamePasswordAuthenticationFilter

UsernamePasswordAuthenticationFilter 继承 AbstractAuthenticationProcessingFilter

1.阅读AbstractAuthenticationProcessingFilter
```java
public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
        

        // <1>request是否需要认证(new AntPathRequestMatcher("/login", "POST")这个才匹配)
		if (!requiresAuthentication(request, response)) {
			chain.doFilter(request, response);

			return;
		}
		Authentication authResult;

		try {
            // <2>调用attemptAuthentication方法(调用子类的)
			authResult = attemptAuthentication(request, response);
			if (authResult == null) {
				// return immediately as subclass has indicated that it hasn't completed
				// authentication
				return;
			}
			sessionStrategy.onAuthentication(authResult, request, response);
		}
		catch (InternalAuthenticationServiceException | AuthenticationException  failed) {
		    
            // <3>调用失败处理器
			unsuccessfulAuthentication(request, response, failed);

			return;
		}
		if (continueChainBeforeSuccessfulAuthentication) {
			chain.doFilter(request, response);
		}
		
        // <4>调用成功处理器
		successfulAuthentication(request, response, chain, authResult);
	}
```
2.阅读UsernamePasswordAuthenticationFilter的attemptAuthentication
```java
	public Authentication attemptAuthentication(HttpServletRequest request,HttpServletResponse response) {
        
        // 读取username
		String username = obtainUsername(request);
        // 读取password
		String password = obtainPassword(request);
        
        // 初步构建一个未认证成功的UsernamePasswordAuthenticationToken
		UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
				username, password);
		
		// 允许子类去设置更信息的信息(remoteAddress.sessionId..)
		setDetails(request, authRequest);
        // 认证的重点在这里()
		return this.getAuthenticationManager().authenticate(authRequest);
	}
```
3.阅读AuthenticationManager
```java
    // 这个是认证管理器的顶级接口了
    public interface AuthenticationManager {
    
    // 唯一的方法就是认证
	Authentication authenticate(Authentication authentication) throws AuthenticationException;
}
```
4.阅读ProviderManager(AuthenticationManager的子类)
```java

    public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
        
        //获取authentication的类型(目前是UsernamePasswordAuthenticationToken)
		Class<? extends Authentication> toTest = authentication.getClass();

		Authentication result = null;
		Authentication parentResult = null;
		boolean debug = logger.isDebugEnabled();
 
        //遍历所有的AuthenticationProvider
		for (AuthenticationProvider provider : getProviders()) {
            //查找能支持这种authentication的AuthenticationProvider
			if (!provider.supports(toTest)) {
				continue;

			try {
                // 使用特定的AuthenticationProvider认证这个token
                // !重点在这里[查看这个方法]
				result = provider.authenticate(authentication);
				if (result != null) {
				    // 复制detail
					copyDetails(authentication, result);
					break;
				}
			}
            //捕获认证的异常
			catch (AccountStatusException e) {
				prepareException(e, authentication);
				throw e;
			}
			catch (InternalAuthenticationServiceException e) {
				prepareException(e, authentication);
				throw e;
			}
			catch (AuthenticationException e) {
				lastException = e;
			}
		}


		if (result != null) {
			if (eraseCredentialsAfterAuthentication
					&& (result instanceof CredentialsContainer)) {
			    // 擦除重要密码信息
				((CredentialsContainer) result).eraseCredentials();
			}

		    // 发布事件
			if (parentResult == null) {
				eventPublisher.publishAuthenticationSuccess(result);
			}
			return result;
		}

		// Parent was null, or didn't authenticate (or throw an exception).

		if (lastException == null) {
			lastException = new ProviderNotFoundException(messages.getMessage(
					"ProviderManager.providerNotFound",
					new Object[] { toTest.getName() },
					"No AuthenticationProvider found for {0}"));
		}

		// If the parent AuthenticationManager was attempted and failed than it will publish an AbstractAuthenticationFailureEvent
		// This check prevents a duplicate AbstractAuthenticationFailureEvent if the parent AuthenticationManager already published it
		if (parentException == null) {
			prepareException(lastException, authentication);
		}

		throw lastException;
	}
```

5.阅读DaoAuthenticationProvider的父类AbstractUserDetailsAuthenticationProvider

```java
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

		// Determine username
		String username = (authentication.getPrincipal() == null) ? "NONE_PROVIDED"
				: authentication.getName();


			try {
                //根据username获取user
				user = retrieveUser(username,
						(UsernamePasswordAuthenticationToken) authentication);
			}
			catch (UsernameNotFoundException notFound) {
				logger.debug("User '" + username + "' not found");

				if (hideUserNotFoundExceptions) {
					throw new BadCredentialsException(messages.getMessage(
							"AbstractUserDetailsAuthenticationProvider.badCredentials",
							"Bad credentials"));
				}
				else {
					throw notFound;
				}
			}


		

		try {
			preAuthenticationChecks.check(user);
			additionalAuthenticationChecks(user,
					(UsernamePasswordAuthenticationToken) authentication);
		}
		catch (AuthenticationException exception) {
			if (cacheWasUsed) {
				// There was a problem, so try again after checking
				// we're using latest data (i.e. not from the cache)
				cacheWasUsed = false;
				user = retrieveUser(username,
						(UsernamePasswordAuthenticationToken) authentication);
				preAuthenticationChecks.check(user);
				additionalAuthenticationChecks(user,
						(UsernamePasswordAuthenticationToken) authentication);
			}
			else {
				throw exception;
			}
		}

		postAuthenticationChecks.check(user);

		if (!cacheWasUsed) {
			this.userCache.putUserInCache(user);
		}
		Object principalToReturn = user;
		if (forcePrincipalAsString) {
			principalToReturn = user.getUsername();
		}
		// 创建认证信息
		return createSuccessAuthentication(principalToReturn, authentication, user);
	}




```

6.阅读DaoAuthenticationProviderde的retrieveUser方法

```java
	protected final UserDetails retrieveUser(String username,
			UsernamePasswordAuthenticationToken authentication)
			throws AuthenticationException {
		prepareTimingAttackProtection();
		try {
            //获取UserDetailsService()调用其的loadUserByUsername
			UserDetails loadedUser = this.getUserDetailsService().loadUserByUsername(username);
			if (loadedUser == null) {
				throw new InternalAuthenticationServiceException(
						"UserDetailsService returned null, which is an interface contract violation");
			}
			return loadedUser;
		}
		catch (UsernameNotFoundException ex) {
			mitigateAgainstTimingAttack(authentication);
			throw ex;
		}
		catch (InternalAuthenticationServiceException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new InternalAuthenticationServiceException(ex.getMessage(), ex);
		}
	}
```
