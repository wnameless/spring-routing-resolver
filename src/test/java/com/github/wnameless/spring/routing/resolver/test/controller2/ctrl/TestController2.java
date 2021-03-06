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
package com.github.wnameless.spring.routing.resolver.test.controller2.ctrl;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@TestTypeAnno
@RestController
@RequestMapping(path = "/b")
public class TestController2 {

  @RequestMapping(value = "/a", method = POST)
  String home() {
    return "home";
  }

  @TestMethodAnno
  @RequestMapping
  String home2() {
    return "home2";
  }

  @PutMapping("/c/{cc}")
  String home3(@PathVariable(required = false) String cc) {
    return "home3";
  }

}
