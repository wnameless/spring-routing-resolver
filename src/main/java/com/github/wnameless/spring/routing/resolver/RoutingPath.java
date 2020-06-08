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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.unmodifiableList;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ForwardingList;

/**
 * 
 * {@link RoutingPath} represents the detail information of a Spring annotated
 * routing path which is provided by the spring {@link RequestMapping}
 * annotation. It is an immutable class.
 *
 */
public final class RoutingPath {

  private final RequestMethod method;
  private final String rawPath;
  private final String path;
  private final Pattern regexPath;
  private final List<Annotation> classAnnotations;
  private final List<Annotation> methodAnnotations;
  private final List<List<Annotation>> parameterAnnotations = newArrayList();

  /**
   * Creates an {@link RoutingPath}.
   * 
   * @param method
   *          an allowable {@link RequestMethod}
   * @param rawPath
   *          the raw path value from the original {@link RequestMapping}
   * @param path
   *          the path which all place holders are replaced by Spring
   *          environment variables
   * @param regexPath
   *          the {@link Pattern} used to match valid HTTP requests
   * @param classAnnotations
   *          all class annotations of the original {@link RequestMapping}
   * @param methodAnnotations
   *          all method annotations of the original {@link RequestMapping}
   */
  public RoutingPath(RequestMethod method, String rawPath, String path,
      Pattern regexPath, Annotation[] classAnnotations,
      Annotation[] methodAnnotations, Annotation[][] parameterAnnotations) {
    this.method = checkNotNull(method);
    this.rawPath = checkNotNull(rawPath);
    this.regexPath = checkNotNull(regexPath);
    this.path = checkNotNull(path);
    this.classAnnotations = newArrayList(classAnnotations);
    this.methodAnnotations = newArrayList(methodAnnotations);
    for (Annotation[] annos : parameterAnnotations) {
      this.parameterAnnotations.add(newArrayList(annos));
    }
  }

  /**
   * Returns the {@link RequestMethod} of this mapping.
   * 
   * @return a {@link RequestMethod}
   */
  public RequestMethod getMethod() {
    return method;
  }

  /**
   * Returns the raw path of this mapping.
   * 
   * @return a raw path
   */
  public String getRawPath() {
    return rawPath;
  }

  /**
   * Returns the path of this mapping.
   * 
   * @return a path
   */
  public String getPath() {
    return path;
  }

  /**
   * Returns the regex path of this mapping.
   * 
   * @return a {@link Pattern} of the regex path
   */
  public Pattern getRegexPath() {
    return regexPath;
  }

  /**
   * Returns all class annotations of the original {@link RequestMapping}.
   * 
   * @return all class annotations
   */
  public List<Annotation> getClassAnnotations() {
    return unmodifiableList(classAnnotations);
  }

  /**
   * Returns all method annotations of the original {@link RequestMapping}.
   * 
   * @return all method annotations
   */
  public List<Annotation> getMethodAnnotations() {
    return unmodifiableList(methodAnnotations);
  }

  /**
   * Returns all parameter annotations of the original {@link RequestMapping}.
   * 
   * @return all parameter annotations
   */
  public List<List<Annotation>> getParameterAnnotations() {
    return unmodifiableList2(parameterAnnotations);
  }

  private <T> List<List<T>> unmodifiableList2(final List<List<T>> input) {
    return unmodifiableList(new ForwardingList<List<T>>() {
      @Override
      protected List<List<T>> delegate() {
        return unmodifiableList(input);
      }

      @Override
      public List<T> get(int index) {
        return unmodifiableList(delegate().get(index));
      }
    });
  }

  @Override
  public boolean equals(final Object other) {
    if (this == other) return true;
    if (!(other instanceof RoutingPath)) return false;
    RoutingPath castOther = (RoutingPath) other;
    return Objects.equal(method, castOther.method)
        && Objects.equal(rawPath, castOther.rawPath)
        && Objects.equal(path, castOther.path)
        && Objects.equal(regexPath, castOther.regexPath)
        && Objects.equal(classAnnotations, castOther.classAnnotations)
        && Objects.equal(methodAnnotations, castOther.methodAnnotations);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(method, rawPath, path, regexPath, classAnnotations,
        methodAnnotations);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("method", method)
        .add("rawPath", rawPath).add("path", path).add("regexPath", regexPath)
        .add("classAnnotations", classAnnotations)
        .add("methodAnnotations", methodAnnotations).toString();
  }

}
