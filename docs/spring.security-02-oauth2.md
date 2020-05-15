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
<artifactId>spring-security-oauth2:2.2.3</artifactId>
</pom>
```

## 阅读源码
```text
@EnableResourceServer
@EnableAuthorizationServer
```
### 源码阅读

#### TokenEndpoint
这个接口提供(/oauth/token)的功能,先阅读这个入口
```java

```