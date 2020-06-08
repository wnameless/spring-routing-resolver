/*
 *
 * Copyright 2015 Wei-Ming Wu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package com.github.wnameless.spring.routing.resolver;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newLinkedHashSet;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

/**
 * 
 * {@link RoutingPathResolver} searches all Spring annotated routing paths under
 * given package bases which are provided by {@link RequestMapping} annotations
 * into a list of {@link RoutingPath} objects.
 *
 */
public final class RoutingPathResolver {

  private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{[^}]+\\}");
  private static final Pattern PATH_VAR = Pattern.compile("\\{[^}]+\\}");
  private static final Pattern ANT_AA = Pattern.compile("\\*\\*");
  private static final Pattern ANT_A = Pattern.compile("\\*");
  private static final Pattern ANT_Q = Pattern.compile("\\?");

  private final Environment env;
  private final Set<RoutingPath> routingPaths = newLinkedHashSet();

  /**
   * Creates a {@link RoutingPathResolver}.
   * 
   * @param appCtx
   *          the Spring {@link ApplicationContext}
   * @param basePackages
   *          packages to be searched
   */
  public RoutingPathResolver(ApplicationContext appCtx,
      String... basePackages) {
    env = appCtx.getEnvironment();

    Map<String, Object> beans = appCtx.getBeansWithAnnotation(Controller.class);
    beans.putAll(appCtx.getBeansWithAnnotation(RestController.class));
    retainBeansByPackageNames(beans, basePackages);

    for (Object bean : beans.values()) {
      RequestMapping classMapping =
          bean.getClass().getAnnotation(RequestMapping.class);

      List<Method> mappingMethods =
          getMethodsListWithAnnotation(bean.getClass(), RequestMapping.class);
      mappingMethods.addAll(
          getMethodsListWithAnnotation(bean.getClass(), GetMapping.class));
      mappingMethods.addAll(
          getMethodsListWithAnnotation(bean.getClass(), PostMapping.class));
      mappingMethods.addAll(
          getMethodsListWithAnnotation(bean.getClass(), DeleteMapping.class));
      mappingMethods.addAll(
          getMethodsListWithAnnotation(bean.getClass(), PutMapping.class));
      mappingMethods.addAll(
          getMethodsListWithAnnotation(bean.getClass(), PatchMapping.class));

      for (Method method : mappingMethods) {
        Annotation methodMapping = method.getAnnotation(RequestMapping.class);
        if (methodMapping == null)
          methodMapping = method.getAnnotation(GetMapping.class);
        if (methodMapping == null)
          methodMapping = method.getAnnotation(PostMapping.class);
        if (methodMapping == null)
          methodMapping = method.getAnnotation(DeleteMapping.class);
        if (methodMapping == null)
          methodMapping = method.getAnnotation(PutMapping.class);
        if (methodMapping == null)
          methodMapping = method.getAnnotation(PatchMapping.class);

        for (Entry<String, RequestMethod> rawPathAndMethod : computeRawPaths(
            classMapping, methodMapping)) {
          String rawPath = rawPathAndMethod.getKey();
          String path = computePath(rawPath);
          String regexPath = computeRegexPath(path);
          routingPaths
              .add(new RoutingPath(rawPathAndMethod.getValue(), rawPath, path,
                  Pattern.compile(regexPath), bean.getClass().getAnnotations(),
                  method.getAnnotations(), method.getParameterAnnotations()));
        }
      }
    }
  }

  /**
   * Returns a list of {@link RoutingPath} under given package bases.
   * 
   * @return a list of {@link RoutingPath}
   */
  public List<RoutingPath> getRoutingPaths() {
    return newArrayList(routingPaths);
  }

  /**
   * Finds {@link RoutingPath}s by given annotation which may show on class or
   * method level of a {@link RequestMapping}.
   * 
   * @param annoType
   *          the class of an annotation
   * @return founded {@link RoutingPath}
   */
  public List<RoutingPath> findByAnnotationType(
      final Class<? extends Annotation> annoType) {
    List<RoutingPath> paths = newArrayList(routingPaths);

    Iterables.removeIf(paths, new Predicate<RoutingPath>() {

      @Override
      public boolean apply(RoutingPath item) {
        return !Iterables.any(item.getClassAnnotations(),
            ca -> annoType.equals(ca.annotationType()))
            && !Iterables.any(item.getMethodAnnotations(),
                ma -> annoType.equals(ma.annotationType()));
      }

    });

    return paths;
  }

  /**
   * Finds {@link RoutingPath}s by given annotation which only show on class
   * level of a {@link RequestMapping}.
   * 
   * @param annoType
   *          the class of an annotation
   * @return founded {@link RoutingPath}
   */
  public List<RoutingPath> findByClassAnnotationType(
      final Class<? extends Annotation> annoType) {
    List<RoutingPath> paths = newArrayList(routingPaths);

    Iterables.removeIf(paths, rp -> !Iterables.any(rp.getClassAnnotations(),
        ca -> annoType.equals(ca.annotationType())));

    return paths;
  }

  /**
   * Finds {@link RoutingPath}s by given annotation which only show on method
   * level of a {@link RequestMapping}.
   * 
   * @param annoType
   *          the class of an annotation
   * @return founded {@link RoutingPath}
   */
  public List<RoutingPath> findByMethodAnnotationType(
      final Class<? extends Annotation> annoType) {
    List<RoutingPath> paths = newArrayList(routingPaths);

    Iterables.removeIf(paths, rp -> !Iterables.any(rp.getMethodAnnotations(),
        ma -> annoType.equals(ma.annotationType())));

    return paths;
  }

  /**
   * Finds {@link RoutingPath}s by given path and request method.
   * 
   * @param requestPath
   *          to be found
   * @param method
   *          to be matched
   * @return founded {@link RoutingPath}
   */
  public RoutingPath findByRequestPathAndMethod(String requestPath,
      RequestMethod method) {
    for (RoutingPath routingPath : routingPaths) {
      if (routingPath.getPath().equals(requestPath)
          && routingPath.getMethod().equals(method))
        return routingPath;
    }

    for (RoutingPath routingPath : routingPaths) {
      if (requestPath.matches(routingPath.getRegexPath().pattern())
          && routingPath.getMethod().equals(method))
        return routingPath;
    }

    return null;
  }

  /**
   * Finds {@link RoutingPath}s by given path.
   * 
   * @param requestPath
   *          to be found
   * @return founded {@link RoutingPath}
   */
  public List<RoutingPath> findByRequestPath(String requestPath) {
    List<RoutingPath> paths = newArrayList();

    for (RoutingPath routingPath : routingPaths) {
      if (routingPath.getPath().equals(requestPath)) {
        paths.add(routingPath);
      } else if (requestPath.matches(routingPath.getRegexPath().pattern())) {
        paths.add(routingPath);
      }
    }

    return paths;
  }

  private List<Entry<String, RequestMethod>> computeRawPaths(
      RequestMapping classMapping, Annotation methodMapping) {
    List<Entry<String, RequestMethod>> rawPathsAndMethods = newArrayList();

    List<String> prefixPaths = classMapping == null ? newArrayList("")
        : classMapping.value().length != 0
            ? newArrayList(ImmutableSet.copyOf(classMapping.value()))
            : newArrayList(ImmutableSet.copyOf(classMapping.path()));
    if (prefixPaths.isEmpty()) prefixPaths.add("");

    List<String> suffixPaths = newArrayList();
    List<RequestMethod> requestMethods = newArrayList();
    if (methodMapping.annotationType().equals(RequestMapping.class)) {
      suffixPaths = ((RequestMapping) methodMapping).value().length != 0
          ? newArrayList(
              ImmutableSet.copyOf(((RequestMapping) methodMapping).value()))
          : newArrayList(
              ImmutableSet.copyOf(((RequestMapping) methodMapping).path()));

      requestMethods
          .addAll(Arrays.asList(((RequestMapping) methodMapping).method()));
    } else if (methodMapping.annotationType().equals(GetMapping.class)) {
      suffixPaths = ((GetMapping) methodMapping).value().length != 0
          ? newArrayList(
              ImmutableSet.copyOf(((GetMapping) methodMapping).value()))
          : newArrayList(
              ImmutableSet.copyOf(((GetMapping) methodMapping).path()));

      requestMethods.add(RequestMethod.GET);
    } else if (methodMapping.annotationType().equals(PostMapping.class)) {
      suffixPaths = ((PostMapping) methodMapping).value().length != 0
          ? newArrayList(
              ImmutableSet.copyOf(((PostMapping) methodMapping).value()))
          : newArrayList(
              ImmutableSet.copyOf(((PostMapping) methodMapping).path()));

      requestMethods.add(RequestMethod.POST);
    } else if (methodMapping.annotationType().equals(DeleteMapping.class)) {
      suffixPaths = ((DeleteMapping) methodMapping).value().length != 0
          ? newArrayList(
              ImmutableSet.copyOf(((DeleteMapping) methodMapping).value()))
          : newArrayList(
              ImmutableSet.copyOf(((DeleteMapping) methodMapping).path()));

      requestMethods.add(RequestMethod.DELETE);
    } else if (methodMapping.annotationType().equals(PutMapping.class)) {
      suffixPaths = ((PutMapping) methodMapping).value().length != 0
          ? newArrayList(
              ImmutableSet.copyOf(((PutMapping) methodMapping).value()))
          : newArrayList(
              ImmutableSet.copyOf(((PutMapping) methodMapping).path()));

      requestMethods.add(RequestMethod.PUT);
    } else if (methodMapping.annotationType().equals(PatchMapping.class)) {
      suffixPaths = ((PatchMapping) methodMapping).value().length != 0
          ? newArrayList(
              ImmutableSet.copyOf(((PatchMapping) methodMapping).value()))
          : newArrayList(
              ImmutableSet.copyOf(((PatchMapping) methodMapping).path()));

      requestMethods.add(RequestMethod.PATCH);
    }
    if (suffixPaths.isEmpty()) suffixPaths.add("");

    while (!prefixPaths.isEmpty()) {
      String prefixPath = prefixPaths.remove(0);

      while (!suffixPaths.isEmpty()) {
        String suffixPath = suffixPaths.remove(0);

        if (requestMethods.isEmpty()) {
          for (RequestMethod m : RequestMethod.values()) {
            rawPathsAndMethods.add(Maps.immutableEntry(
                PathUtils.joinPaths(prefixPath, suffixPath), m));
          }
        } else {
          for (RequestMethod m : requestMethods) {
            rawPathsAndMethods.add(Maps.immutableEntry(
                PathUtils.joinPaths(prefixPath, suffixPath), m));
          }
        }
      }
    }

    return rawPathsAndMethods;
  }

  private String computeRegexPath(String path) {
    path = Regexs.escapeSpecialCharacters(path, PLACEHOLDER, PATH_VAR, ANT_AA,
        ANT_A, ANT_Q);
    Matcher m = PATH_VAR.matcher(path);
    while (m.find()) {
      String match = m.group();
      path = path.replaceFirst(Pattern.quote(match), "[^/]+");
    }
    m = ANT_AA.matcher(path);
    while (m.find()) {
      String match = m.group();
      path = path.replaceFirst(Pattern.quote(match), ".\"");
    }
    m = ANT_A.matcher(path);
    while (m.find()) {
      String match = m.group();
      path = path.replaceFirst(Pattern.quote(match), "[^/]*");
    }
    m = ANT_Q.matcher(path);
    while (m.find()) {
      String match = m.group();
      path = path.replaceFirst(Pattern.quote(match), ".");
    }
    path = path.replaceAll(Pattern.quote("\""), "*");

    // the first slash of an URL can be omitted
    path = path.startsWith("/") ? path = "/?" + path.substring(1) : "/?" + path;
    // the last slash of an URL is optional if user not mentions
    if (!path.endsWith("/")) path = path + "/?";

    return path;
  }

  private String computePath(String rawPath) {
    String path = rawPath;
    Matcher m = PLACEHOLDER.matcher(rawPath);
    while (m.find()) {
      String placeholder = m.group();
      String trimmedPlaceholder =
          placeholder.substring(2, placeholder.length() - 1);
      String[] keyAndDefault = trimmedPlaceholder.split(":");
      String key = keyAndDefault[0];
      String deFault = "";
      if (keyAndDefault.length > 1) deFault = keyAndDefault[1];
      path = path.replaceFirst(Pattern.quote(placeholder),
          env.getProperty(key, deFault));
    }
    return path;
  }

  private void retainBeansByPackageNames(Map<String, Object> beans,
      String... basePackages) {
    Iterator<Object> beansIter = beans.values().iterator();
    while (beansIter.hasNext()) {
      String beanPackage = beansIter.next().getClass().getPackage().getName();
      boolean isKeep = false;
      for (String packageName : basePackages) {
        if (beanPackage.equals(packageName)
            || beanPackage.startsWith(packageName + ".")) {
          isKeep = true;
        }
      }
      if (!isKeep) beansIter.remove();
    }
  }

  private List<Method> getMethodsListWithAnnotation(final Class<?> cls,
      final Class<? extends Annotation> annotationCls) {
    Method[] allMethods = cls.getDeclaredMethods();
    List<Method> annotatedMethods = new ArrayList<Method>();
    for (Method method : allMethods) {
      if (method.getAnnotation(annotationCls) != null) {
        annotatedMethods.add(method);
      }
    }
    return annotatedMethods;
  }

}
