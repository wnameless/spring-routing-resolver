spring-routing-resolver
=============
An easy way to find out all routing paths in Spring annotated controllers.

##Maven Repo
```xml
<dependency>
	<groupId>com.github.wnameless.spring</groupId>
	<artifactId>spring-routing-resolver</artifactId>
	<version>0.2.0</version>
</dependency>
```

#Quick Start
```java
@Autowired
Environment env;

@Autowired
ApplicationContext appCtx;

RoutingPathResolver pathRes = new RoutingPathResolver(appCtx, env, "com.example.controller");
```
A Spring annotated controller
```java
package com.example.controller;

@TestTypeAnno
@RestController
@RequestMapping(value = "/b")
public class TestController2 {

  @TestMethodAnno
  @RequestMapping(value = "/a/${no.such.var:foo}/**/*.json", method = POST)
  String home() {
    return "home";
  }

}
```

RoutingPath
```java
List<RoutingPath> routingPaths = pathRes.getRoutingPaths();
RoutingPath rp : routingPaths.get(0);
System.out.println(rp.getMethod());
// POST
System.out.println(rp.getRawPath());
// /b/a/${no.such.var:foo}/**/*.json
System.out.println(rp.getPath());
// /b/a/foo/**/*.json
System.out.println(rp.getRegexPath());
// /b/a/foo/.*/[^/]*\.json
System.out.println(rp.getClassAnnotations());
// [@com.example.annotation.TestTypeAnno(),...]
System.out.println(rp.getMethodAnnotations());
// [@com.example.annotation.TestMethodAnno(),...]
```
