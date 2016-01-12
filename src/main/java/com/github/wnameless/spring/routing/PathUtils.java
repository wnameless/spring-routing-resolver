/*
 *
 * Copyright 2016 Wei-Ming Wu
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

import static net.sf.rubycollect4j.RubyCollections.newRubyArray;

import net.sf.rubycollect4j.RubyArray;

/**
 * 
 * {@link PathUtils} provides some useful methods for path manipulation.
 *
 */
public final class PathUtils {

  private PathUtils() {}

  /**
   * Creates a joined path by given paths; a slash('/') is used as the
   * separator.
   * 
   * @param paths
   *          any paths
   * @return a joined path
   */
  public static String joinPaths(String... paths) {
    return joinPaths('/', paths);
  }

  /**
   * Creates a joined path by given paths.
   * 
   * @param separator
   *          used to join the paths
   * @param paths
   *          any paths
   * @return a joined path
   */
  public static String joinPaths(Character separator, String... paths) {
    String pathSeprator = separator.toString();

    RubyArray<String> ra = newRubyArray(paths);
    ra.delete("");
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

}
