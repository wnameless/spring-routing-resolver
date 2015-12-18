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
package com.github.wnameless.spring.routing;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static net.sf.rubycollect4j.RubyCollections.hp;
import static net.sf.rubycollect4j.RubyCollections.newRubyArray;
import static net.sf.rubycollect4j.RubyCollections.ra;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import net.sf.rubycollect4j.RubyArray;
import net.sf.rubycollect4j.block.BooleanBlock;

public final class RoutingPathResolver {

  private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{[^}]+\\}");
  private static final Pattern PATH_VAR = Pattern.compile("\\{[^}]+\\}");

  private Environment env;
  private Set<RoutingPath> routingPaths = newLinkedHashSet();

  public RoutingPathResolver(ApplicationContext appCtx, Environment env,
      String... basePackages) {
    this.env = checkNotNull(env);
    Map<String, Object> beans = appCtx.getBeansWithAnnotation(Controller.class);
    beans.putAll(appCtx.getBeansWithAnnotation(RestController.class));
    retainBeansByPackageNames(beans, basePackages);

    for (Object bean : beans.values()) {
      List<Method> mappingMethods =
          getMethodsListWithAnnotation(bean.getClass(), RequestMapping.class);
      RequestMapping classRM =
          bean.getClass().getAnnotation(RequestMapping.class);
      for (Method method : mappingMethods) {
        RequestMapping methodRM = method.getAnnotation(RequestMapping.class);
        for (Entry<String, RequestMethod> rawPathAndMethod : computeRawPaths(
            classRM, methodRM)) {
          String rawPath = rawPathAndMethod.getKey();
          String path = computePath(rawPath);
          String regexPath = computeRegexPath(path);
          routingPaths.add(new RoutingPath(rawPathAndMethod.getValue(), rawPath,
              regexPath, path, bean.getClass().getAnnotations(),
              method.getAnnotations()));
        }
      }
    }
  }

  public List<RoutingPath> getRoutingPaths() {
    return newArrayList(routingPaths);
  }

  public List<RoutingPath> findByAnnotationType(
      final Class<? extends Annotation> annoType) {
    return ra(routingPaths).keepIf(new BooleanBlock<RoutingPath>() {

      @Override
      public boolean yield(RoutingPath item) {
        return ra(item.getClassAnnotations())
            .anyʔ(new BooleanBlock<Annotation>() {

          @Override
          public boolean yield(Annotation item) {
            return annoType.equals(item.annotationType());
          }

        }) || ra(item.getMethodAnnotations())
            .anyʔ(new BooleanBlock<Annotation>() {

          @Override
          public boolean yield(Annotation item) {
            return annoType.equals(item.annotationType());
          }

        });
      }

    }).toA();
  }

  public List<RoutingPath> findByClassAnnotationType(
      final Class<? extends Annotation> annoType) {
    return ra(routingPaths).keepIf(new BooleanBlock<RoutingPath>() {

      @Override
      public boolean yield(RoutingPath item) {
        return ra(item.getClassAnnotations())
            .anyʔ(new BooleanBlock<Annotation>() {

          @Override
          public boolean yield(Annotation item) {
            return annoType.equals(item.annotationType());
          }

        });
      }

    }).toA();
  }

  public List<RoutingPath> findByMethodAnnotationType(
      final Class<? extends Annotation> annoType) {
    return ra(routingPaths).keepIf(new BooleanBlock<RoutingPath>() {

      @Override
      public boolean yield(RoutingPath item) {
        return ra(item.getMethodAnnotations())
            .anyʔ(new BooleanBlock<Annotation>() {

          @Override
          public boolean yield(Annotation item) {
            return annoType.equals(item.annotationType());
          }

        });
      }

    }).toA();
  }

  public RoutingPath findByRequestPathAndMethod(String requestPath,
      RequestMethod method) {
    for (RoutingPath routingPath : routingPaths) {
      if (routingPath.getPath().equals(requestPath)
          && routingPath.getMethod().equals(method))
        return routingPath;
    }

    for (RoutingPath routingPath : routingPaths) {
      if (requestPath.matches(routingPath.getRegexPath())
          && routingPath.getMethod().equals(method))
        return routingPath;
    }

    return null;
  }

  public List<RoutingPath> findByRequestPath(String requestPath) {
    List<RoutingPath> paths = newArrayList();

    for (RoutingPath routingPath : routingPaths) {
      if (routingPath.getPath().equals(requestPath)) {
        paths.add(routingPath);
      } else if (requestPath.matches(routingPath.getRegexPath())) {
        paths.add(routingPath);
      }
    }

    return paths;
  }

  private List<Entry<String, RequestMethod>> computeRawPaths(
      RequestMapping classRM, RequestMapping methodRM) {
    List<Entry<String, RequestMethod>> rawPathsAndMethods = newArrayList();

    RubyArray<String> topPaths =
        classRM == null ? ra("") : ra(classRM.value()).uniq();
    RubyArray<String> lowPaths = ra(methodRM.value()).uniq();
    if (topPaths.isEmpty()) topPaths.unshift("");
    if (lowPaths.isEmpty()) lowPaths.unshift("");

    while (topPaths.anyʔ()) {
      String topPath = topPaths.shift();
      while (lowPaths.anyʔ()) {
        String lowPath = lowPaths.shift();
        if (methodRM.method().length == 0) {
          for (RequestMethod m : RequestMethod.values()) {
            rawPathsAndMethods.add(hp(joinPaths(topPath, lowPath), m));
          }
        } else {
          for (RequestMethod m : methodRM.method()) {
            rawPathsAndMethods.add(hp(joinPaths(topPath, lowPath), m));
          }
        }
      }
    }

    return rawPathsAndMethods;
  }

  private String computeRegexPath(String path) {
    Matcher m = PATH_VAR.matcher(path);
    while (m.find()) {
      String pathVar = m.group();
      path = path.replace(pathVar, "[^/]+");
    }
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
      path = path.replace(placeholder, env.getProperty(key, deFault));
    }
    return path;
  }

  private String joinPaths(String... files) {
    String pathSeprator = "/";

    RubyArray<String> ra = newRubyArray(files);
    for (int i = 1; i < ra.size(); i++) {
      int predecessor = i - 1;
      while (ra.get(predecessor).endsWith(pathSeprator)) {
        ra.set(predecessor,
            ra.get(predecessor).substring(0, ra.get(predecessor).length() - 1));
      }
      while (ra.get(i).startsWith(pathSeprator)) {
        ra.set(i, ra.get(i).substring(1));
      }
      ra.set(i, pathSeprator + ra.get(i));
    }
    return ra.join();
  }

  private void retainBeansByPackageNames(Map<String, Object> beans,
      String... basePackages) {
    Iterator<Object> beansIter = beans.values().iterator();
    while (beansIter.hasNext()) {
      String beanPackage = beansIter.next().getClass().getPackage().getName();
      boolean isKeep = false;
      for (String packageName : basePackages) {
        if (packageName.equals(beanPackage)
            || packageName.startsWith(beanPackage + ".")) {
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
