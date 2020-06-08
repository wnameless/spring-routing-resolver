[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.wnameless.spring/spring-routing-resolver/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.wnameless.spring/spring-routing-resolver)

spring-routing-resolver
=============
An easy way to find out all routing paths in Spring annotated controllers.

## Maven Repo
```xml
<dependency>
	<groupId>com.github.wnameless.spring</groupId>
	<artifactId>spring-routing-resolver</artifactId>
	<version>0.6.0</version>
</dependency>
```
## Important
Since v0.4.0, @GetMapping, @PostMapping, @DeleteMapping, @PutMapping, @PatchMapping are supported.

Before v0.4.0, this library only reads the url inside @RequestMapping(value="/path"), but leaves the url inside @RequestMapping(path="/path") unread, it has been fixed after v0.4.0.

Since v0.6.0, the minimal Java requirement is 8 and the Java Module is also supported.
# Quick Start
```java
@Autowired
ApplicationContext appCtx;

RoutingPathResolver pathRes = new RoutingPathResolver(appCtx, "com.example.controller");
```
### Spring annotated controller
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

### RoutingPath
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
// /?b/a/foo/.*/[^/]*\.json/?
System.out.println(rp.getClassAnnotations());
// [@com.example.annotation.TestTypeAnno(),...]
System.out.println(rp.getMethodAnnotations());
// [@com.example.annotation.TestMethodAnno(),...]
```
