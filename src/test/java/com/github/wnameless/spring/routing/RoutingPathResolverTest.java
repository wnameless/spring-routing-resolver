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

import static com.google.common.collect.Lists.newArrayList;
import static net.sf.rubycollect4j.RubyCollections.ra;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.bind.annotation.RequestMethod;

import com.github.wnameless.spring.Application;
import com.github.wnameless.spring.controller2.ctrl.TestMethodAnno;
import com.github.wnameless.spring.controller2.ctrl.TestTypeAnno;

import net.sf.rubycollect4j.block.TransformBlock;
import nl.jqno.equalsverifier.EqualsVerifier;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest
public class RoutingPathResolverTest {

  @Autowired
  Environment env;

  @Autowired
  ApplicationContext appCtx;

  RoutingPathResolver pathRes;

  RoutingPathResolver pathRes2;

  RoutingPathResolver pathRes3;

  @Before
  public void setUp() {
    pathRes = new RoutingPathResolver(appCtx,
        "com.github.wnameless.spring.controller");
    pathRes2 = new RoutingPathResolver(appCtx,
        "com.github.wnameless.spring.controller2");
    pathRes3 = new RoutingPathResolver(appCtx,
        "com.github.wnameless.spring.controller3");
  }

  @Test
  public void testRoutingPathBean() {
    EqualsVerifier.forClass(RoutingPath.class).verify();
    assertTrue(pathRes.getRoutingPaths().get(0).toString()
        .startsWith("RoutingPath{method=GET, " + "rawPath=/home/index, "
            + "path=/home/index, " + "regexPath=/home/index, "
            + "classAnnotations=[@"));
  }

  @Test
  public void testRawPath() {
    assertEquals(ra(pathRes.getRoutingPaths())
        .map(new TransformBlock<RoutingPath, String>() {

          @Override
          public String yield(RoutingPath item) {
            return item.getRawPath();
          }

        }).sort(),
        ra("/home/index", "/home/index/{ph1}", "/home/index/${test.var.1}",
            "/home/index/${test.var.2:yaya}").sort());
  }

  @Test
  public void testRegexPath() {
    assertEquals(ra(pathRes.getRoutingPaths())
        .map(new TransformBlock<RoutingPath, String>() {

          @Override
          public String yield(RoutingPath item) {
            return item.getRegexPath().pattern();
          }

        }).sort(), ra("/home/index", "/home/index/[^/]+", "/home/index/haha",
            "/home/index/yaya").sort());
  }

  @Test
  public void testPath() {
    assertEquals(ra(pathRes.getRoutingPaths())
        .map(new TransformBlock<RoutingPath, String>() {

          @Override
          public String yield(RoutingPath item) {
            return item.getPath();
          }

        }).sort(), ra("/home/index", "/home/index/{ph1}", "/home/index/haha",
            "/home/index/yaya").sort());
  }

  @Test
  public void testfindByAnnotationType() {
    assertEquals(9, pathRes2.findByAnnotationType(TestTypeAnno.class).size());
    assertEquals(8, pathRes2.findByAnnotationType(TestMethodAnno.class).size());
  }

  @Test
  public void testfindByClassAnnotationType() {
    assertEquals(9,
        pathRes2.findByClassAnnotationType(TestTypeAnno.class).size());
    assertEquals(0,
        pathRes2.findByClassAnnotationType(TestMethodAnno.class).size());
  }

  @Test
  public void testfindByMethodAnnotationType() {
    assertEquals(8,
        pathRes2.findByMethodAnnotationType(TestMethodAnno.class).size());
    assertEquals(0,
        pathRes2.findByMethodAnnotationType(TestTypeAnno.class).size());
  }

  @Test
  public void testEmptyMethod() {
    assertTrue(ra(pathRes2.findByMethodAnnotationType(TestMethodAnno.class))
        .map(new TransformBlock<RoutingPath, RequestMethod>() {

          @Override
          public RequestMethod yield(RoutingPath item) {
            return item.getMethod();
          }

        }).containsAll(newArrayList(RequestMethod.values())));
  }

  @Test
  public void testFindByRequestPathAndMethod() {
    assertNull(pathRes.findByRequestPathAndMethod("/", RequestMethod.GET));
    assertEquals("/home/index/${test.var.2:yaya}",
        pathRes
            .findByRequestPathAndMethod("/home/index/yaya", RequestMethod.GET)
            .getRawPath());
    assertEquals("/home/index/{ph1}",
        pathRes
            .findByRequestPathAndMethod("/home/index/gogo", RequestMethod.GET)
            .getRawPath());
  }

  @Test
  public void testFindByRequestPath() {
    assertEquals(8, pathRes2.findByRequestPath("/b").size());
  }

  @Test
  public void testAntPattern() {
    RoutingPath rp = pathRes3.getRoutingPaths().get(0);
    assertEquals(1, pathRes3.getRoutingPaths().size());
    assertEquals("/ant/${test.var.1}/${test.var.3}/{aaa}/**/*/a+b-c?.json",
        rp.getRawPath());
    assertEquals("/ant/haha/yoyo/{aaa}/**/*/a+b-c?.json", rp.getPath());
    assertEquals("/ant/haha/yoyo/[^/]+/.*/[^/]*/a\\+b\\-c.\\.json",
        rp.getRegexPath().pattern());
  }

}
