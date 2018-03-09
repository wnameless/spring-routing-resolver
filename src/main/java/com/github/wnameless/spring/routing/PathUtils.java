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

import java.util.ArrayList;
import java.util.Arrays;

import com.google.common.base.Joiner;

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

    ArrayList<String> pathList = new ArrayList<String>(Arrays.asList(paths));
    pathList.remove("");
    for (int i = 1; i < pathList.size(); i++) {
      int predecessor = i - 1;
      while (pathList.get(predecessor).endsWith(pathSeprator)) {
        pathList.set(predecessor, pathList.get(predecessor).substring(0,
            pathList.get(predecessor).length() - 1));
      }
      while (pathList.get(i).startsWith(pathSeprator)) {
        pathList.set(i, pathList.get(i).substring(1));
      }
      pathList.set(i, pathSeprator + pathList.get(i));
    }

    return Joiner.on("").join(pathList);
  }

}
