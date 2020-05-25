# spring security 整合OAUTH

## 引言
```txt
    <1>使用SpringSecurity整合OAuth2之前,应该对OAuth2有个基本了解,可以参考阮一峰的日志(http://www.ruanyifeng.com/blog/2019/04/oauth_design.html)
    <2>至少掌握以下名词的概念:
        资源拥有者:  一般用户
        服务提供商:  比如QQ,微信这种支持第三方程序获取资源的服务商
        第三方应用程序:    可以理解为需要借用服务提供商资源的外部程序
        认证服务器:  负责认证,颁发令牌(服务商提供)
        资源服务器:  确定拥有权限后即可访问资源(服务商提供)
    <3>将上面的名词概念对应到自己开发的系统中:
        需要开发:   
            1.认证服务器(认证/鉴权中心) 
            2.资源服务器(用户数据)
            3.第三方应用程序(我们的前端程序:H5,APP,PC端C#...)
        流程概要: 
            (密码模式)
            用户使用第三方客户端 -> 认证服务器 -> 颁发token
                             -> 携带token至资源服务器 -> 资源服务将token请求到认证中心鉴定权限 -> 权限通过则资源服务器放开资源给资源拥有者

```
## 项目版本
```xml
<!--SpringBoot2.1.14-->
<pom>
<artifactId>spring-boot-starter-security</artifactId>
<artifactId>spring-boot-starter-web</artifactId>
<artifactId>spring-security-oauth2:2.2.5</artifactId>
</pom>
```

## 大致流程(密码模式)
    -> ClientCredentialsTokenEndpointFilter | BasicAuthenticationFilter         //(client过滤器)
        -> /oauth/token                                                         //获取令牌的请求                                         
            -> TokenEndPoint                                                    //授权端点
                -> ClientDetailsService.loadClientByClientId()                  //查找客户端详情
                    -> ClientDetails                                            //客户端详情
                        -> TokenRequest                                         //原始令牌请求
                            -> TokenGranter(CompositeTokenGranter)              //令牌颁发者对象
                                -> ResourceOwnerPasswordTokenGranter            //具体的颁发者
                                    -> OAuth2Request + Authentication           //客户端请求+用户认证信息
                                        -> AuthorizationServerTokenServices     //授权服务器授权服务
                                            ->  OAuth2Authentication            //最终的OAuth2令牌                                 

## 源码阅读
@EnableResourceServer
@EnableAuthorizationServer

#### 1.ClientCredentialsTokenEndpointFilter(废弃了)
ClientCredentialsTokenEndpointFilter实现了AbstractAuthenticationProcessingFilter,类似UsernamePasswordAuthenticationFilter
拦截路径
```java
public ClientCredentialsTokenEndpointFilter() {
    this("/oauth/token");
}
```
关键方法
```java
public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException, IOException, ServletException {

		if (allowOnlyPost && !"POST".equalsIgnoreCase(request.getMethod())) {
			throw new HttpRequestMethodNotSupportedException(request.getMethod(), new String[] { "POST" });
		}
        //获取clientId
		String clientId = request.getParameter("client_id");
		String clientSecret = request.getParameter("client_secret");

		// If the request is already authenticated we can assume that this
		// filter is not needed
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			return authentication;
		}
        
        // 适配成了UsernamePasswordAuthenticationToken
		UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(clientId,
				clientSecret);

		return this.getAuthenticationManager().authenticate(authRequest);

	}
```
好像是使用ClientCredentialsTokenEndpointFilter获取了客户端凭证
但是查看父类的
```java
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
        //ClientCredentialsTokenEndpointFilter的match
		if (!requiresAuthentication(request, response)) {
			chain.doFilter(request, response);

			return;
		}
		Authentication authResult;

		try {
			authResult = attemptAuthentication(request, response);

			}
		}
}
```
ClientCredentialsRequestMatcher是其路由匹配
```java
	protected static class ClientCredentialsRequestMatcher implements RequestMatcher {

        // ...

		public ClientCredentialsRequestMatcher(String path) {
			this.path = path;

		}

		@Override
		public boolean matches(HttpServletRequest request) {
			String uri = request.getRequestURI();
			int pathParamIndex = uri.indexOf(';');

			if (pathParamIndex > 0) 
				// strip everything after the first semi-colon
				uri = uri.substring(0, pathParamIndex);
			}

			String clientId = request.getParameter("client_id");

			if (clientId == null) {
                    // !!!
                    // 这里给了 Basic Auth 一个机会去认代替其工作 
				    // Give basic auth a chance to work instead (it's preferred anyway)
				return false;
			}

		}

	}
```
#### 2.BasicAuthenticationFilter
BasicAuthenticationFilter是OncePerRequestFilter子类
```java
	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response, FilterChain chain) {
	    
        //获取Header中的Authorization信息
		String header = request.getHeader("Authorization");
        
        //只过滤Basic认证
		if (header == null || !header.toLowerCase().startsWith("basic ")) {
			chain.doFilter(request, response);
			return;
		}
        
		try {
            //解码base64编码的header
			String[] tokens = extractAndDecodeHeader(header, request);
			assert tokens.length == 2;

			String username = tokens[0];


			if (authenticationIsRequired(username)) {
				UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
						username, tokens[1]);
				authRequest.setDetails(
						this.authenticationDetailsSource.buildDetails(request));
                // 认证Auth
				Authentication authResult = this.authenticationManager
						.authenticate(authRequest);

				if (debug) {
					this.logger.debug("Authentication success: " + authResult);
				}
                
                // 到了这一步也就是吧client的信息封装成了UsernamePasswordAuthenticationToken翻到了上下文中
				SecurityContextHolder.getContext().setAuthentication(authResult);

			}

			return;
		}
        //继续调用过滤器,直到目标接口
		chain.doFilter(request, response);
	}
```




#### 2.TokenEndpoint
终于进入这个接口了
```java
	@RequestMapping(value = "/oauth/token", method=RequestMethod.POST)
    //这里传入的principal就是上面过滤器设置的那个UsernamePasswordAuthenticationToken
	public ResponseEntity<OAuth2AccessToken> postAccessToken(Principal principal, @RequestParam
	Map<String, String> parameters) throws HttpRequestMethodNotSupportedException {
        
        //获取ClientID
		String clientId = getClientId(principal);
        //获取ClientDetails
		ClientDetails authenticatedClient = getClientDetailsService().loadClientByClientId(clientId);
        
        ///生成TokenRequest
		TokenRequest tokenRequest = getOAuth2RequestFactory().createTokenRequest(parameters, authenticatedClient);

		// ...省略代码

        // 根据tokenRequest和oauth2模式类型 -> 颁发token(重要)
		OAuth2AccessToken token = getTokenGranter().grant(tokenRequest.getGrantType(), tokenRequest);
		if (token == null) {
			throw new UnsupportedGrantTypeException("Unsupported grant type: " + tokenRequest.getGrantType());
		}
        // 返回 AT的JSON
		return getResponse(token);

	}
```

#### 3.CompositeTokenGranter
```java
	public OAuth2AccessToken grant(String grantType, TokenRequest tokenRequest) {
		for (TokenGranter granter : tokenGranters) {
            // 查找适合的令牌颁发者
			OAuth2AccessToken grant = granter.grant(grantType, tokenRequest);
			if (grant!=null) {
				return grant;
			}
		}
		return null;
	}
```

#### 4.ResourceOwnerPasswordTokenGranter
①
```java
	public OAuth2AccessToken grant(String grantType, TokenRequest tokenRequest) {

		if (!this.grantType.equals(grantType)) {
			return null;
		}
		
       
		String clientId = tokenRequest.getClientId();
        //查找客户凭证
		ClientDetails client = clientDetailsService.loadClientByClientId(clientId);
        //获取AT
		return getAccessToken(client, tokenRequest);

	}
```
② 获取 OAuth2AccessToken
```java
	protected OAuth2AccessToken getAccessToken(ClientDetails client, TokenRequest tokenRequest) {
        // DefaultTokenServices
        // 重要
		return tokenServices.createAccessToken(getOAuth2Authentication(client, tokenRequest));
	}
```
③ 获取OAuth2Authentication
```java
    @Override
	protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {

		Map<String, String> parameters = new LinkedHashMap<String, String>(tokenRequest.getRequestParameters());
        // 获取用户名
		String username = parameters.get("username");
        // 获取密码
		String password = parameters.get("password");
        // 生成AT
		Authentication userAuth = new UsernamePasswordAuthenticationToken(username, password);
        
		((AbstractAuthenticationToken) userAuth).setDetails(parameters);
		try {  
            // 认证这个用户信息
			userAuth = authenticationManager.authenticate(userAuth);

		}
		// createOAuth2Request
		OAuth2Request storedOAuth2Request = getRequestFactory().createOAuth2Request(client, tokenRequest);
		// OAuth2Request + UsernamePasswordAuthenticationToken
		return new OAuth2Authentication(storedOAuth2Request, userAuth);
	}
```
④ 获取OAuth2Request
```java
	public OAuth2Request createOAuth2Request(ClientDetails client) {
		Map<String, String> requestParameters = getRequestParameters();
		HashMap<String, String> modifiable = new HashMap<String, String>(requestParameters);
		// Remove password if present to prevent leaks
		modifiable.remove("password");
		modifiable.remove("client_secret");
		// Add grant type so it can be retrieved from OAuth2Request
		modifiable.put("grant_type", grantType);
        // 返回一个OAuth2Request
		return new OAuth2Request(modifiable, client.getClientId(), client.getAuthorities(), true, this.getScope(),
				client.getResourceIds(), null, null, null);
	}
...
```
⑤ Create AccessToken
```java
	@Transactional
	public OAuth2AccessToken createAccessToken(OAuth2Authentication authentication) throws AuthenticationException {
        // 查看这个token是否已经存在
		OAuth2AccessToken existingAccessToken = tokenStore.getAccessToken(authentication);
		OAuth2RefreshToken refreshToken = null;
        // 如果存在则
		if (existingAccessToken != null) {
            //是否过期
			if (existingAccessToken.isExpired()) {
			    //如果过期了,查看原来的刷新token是不是空的
				if (existingAccessToken.getRefreshToken() != null) {
                    //如果原刷新token不是空的,则获取
					refreshToken = existingAccessToken.getRefreshToken();
					// The token store could remove the refresh token when the
					// access token is removed, but we want to
					// be sure...
                    // 移除旧refreshToken
					tokenStore.removeRefreshToken(refreshToken);
				}
				// 移除过期的token
				tokenStore.removeAccessToken(existingAccessToken);
			}
			else {
				// Re-store the access token in case the authentication has changed
                // 如果没过期重新存一次 免得有更改了
				tokenStore.storeAccessToken(existingAccessToken, authentication);
                // 返回就行了
				return existingAccessToken;
			}
		}
        
        // 如果不存在token 
		if (refreshToken == null) {
			refreshToken = createRefreshToken(authentication);
		}
		// But the refresh token itself might need to be re-issued if it has
		// expired.
		else if (refreshToken instanceof ExpiringOAuth2RefreshToken) {
			ExpiringOAuth2RefreshToken expiring = (ExpiringOAuth2RefreshToken) refreshToken;
			if (System.currentTimeMillis() > expiring.getExpiration().getTime()) {
				refreshToken = createRefreshToken(authentication);
			}
		}
        
        //! create AT
		OAuth2AccessToken accessToken = createAccessToken(authentication, refreshToken);
		tokenStore.storeAccessToken(accessToken, authentication);
		// In case it was modified
		refreshToken = accessToken.getRefreshToken();
		if (refreshToken != null) {
			tokenStore.storeRefreshToken(refreshToken, authentication);
		}
		return accessToken;

	}
```

