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
package com.github.wnameless.spring.routing.resolver.test.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.github.wnameless.spring.routing.resolver.test.controller2.ctrl.TestTypeAnno;

@TestTypeAnno
@RequestMapping("/home")
@Controller
public class TestController {

  @RequestMapping(path = { "/index", "/index/{ph1}/" }, method = GET)
  String home() {
    return "home";
  }

  @GetMapping(value = "/index/${test.var.1}")
  String home3() {
    return "home3";
  }

  @PostMapping(path = "/index/${test.var.1}")
  String home31() {
    return "home31";
  }

  @DeleteMapping(value = "/index/${test.var.1}")
  String home32() {
    return "home32";
  }

  @PutMapping(path = "/index/${test.var.1}")
  String home33() {
    return "home33";
  }

  @PatchMapping(value = "/index/${test.var.1}")
  String home34() {
    return "home34";
  }

  @RequestMapping(value = "/index/${test.var.2:yaya}", method = GET)
  String home4() {
    return "home4";
  }

}
