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
import static java.util.Collections.unmodifiableList;

import java.lang.annotation.Annotation;
import java.util.List;

import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public final class RoutingPath {

  private final RequestMethod method;
  private final String rawPath;
  private final String regexPath;
  private final String path;
  private final List<Annotation> classAnnotations;
  private final List<Annotation> methodAnnotations;

  public RoutingPath(RequestMethod method, String rawPath, String regexPath,
      String path, Annotation[] classAnnotations,
      Annotation[] methodAnnotations) {
    this.method = checkNotNull(method);
    this.rawPath = checkNotNull(rawPath);
    this.regexPath = checkNotNull(regexPath);
    this.path = checkNotNull(path);
    this.classAnnotations = newArrayList(classAnnotations);
    this.methodAnnotations = newArrayList(methodAnnotations);
  }

  public RequestMethod getMethod() {
    return method;
  }

  public String getRawPath() {
    return rawPath;
  }

  public String getRegexPath() {
    return regexPath;
  }

  public String getPath() {
    return path;
  }

  public List<Annotation> getClassAnnotations() {
    return unmodifiableList(classAnnotations);
  }

  public List<Annotation> getMethodAnnotations() {
    return unmodifiableList(methodAnnotations);
  }

  @Override
  public boolean equals(final Object other) {
    if (this == other) return true;
    if (!(other instanceof RoutingPath)) return false;
    RoutingPath castOther = (RoutingPath) other;
    return Objects.equal(method, castOther.method)
        && Objects.equal(rawPath, castOther.rawPath)
        && Objects.equal(regexPath, castOther.regexPath)
        && Objects.equal(path, castOther.path)
        && Objects.equal(classAnnotations, castOther.classAnnotations)
        && Objects.equal(methodAnnotations, castOther.methodAnnotations);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(method, rawPath, regexPath, path, classAnnotations,
        methodAnnotations);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("method", method)
        .add("rawPath", rawPath).add("regexPath", regexPath).add("path", path)
        .add("classAnnotations", classAnnotations)
        .add("methodAnnotations", methodAnnotations).toString();
  }

}
