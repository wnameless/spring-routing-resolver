module com.github.wnameless.spring.routing.resolver {
  requires com.google.common;
  requires spring.core;
  requires spring.beans;
  requires transitive spring.web;
  requires transitive spring.context;

  exports com.github.wnameless.spring.routing.resolver;
}