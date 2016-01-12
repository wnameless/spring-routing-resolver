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
package com.github.wnameless.regex;

import static net.sf.rubycollect4j.RubyCollections.ra;
import static net.sf.rubycollect4j.RubyCollections.range;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.rubycollect4j.block.TransformBlock;

/**
 * 
 * {@link Regexs} provides some useful methods for regular expression.
 *
 */
public final class Regexs {

  private Regexs() {}

  /**
   * The pattern of all special characters in regular expression.
   */
  public static final Pattern REGEX_SP_CH =
      Pattern.compile("[\\\\\\[\\.\\[\\]\\{\\}\\(\\)\\*\\+\\-\\?\\^\\$\\]\\|]");

  /**
   * Returns a string with all regular expression special characters escaped.
   * 
   * @param input
   *          any string
   * @param excludedPatterns
   *          special characters in those excluded patterns are ignored
   * @return a string with all regular expression special characters escaped
   */
  public static String escapeSpecialCharacters(String input,
      Pattern... excludedPatterns) {
    List<Matcher> matchers = patterns2Matchers(excludedPatterns, input);
    initMatchers(matchers);

    Matcher m = REGEX_SP_CH.matcher(input);
    StringBuffer sb = new StringBuffer();
    while (m.find()) {
      if (escapableCharacter(m.start(), matchers)) {
        m.appendReplacement(sb, "\\\\" + input.charAt(m.start()));
      }
    }
    m.appendTail(sb);

    return sb.toString();
  }

  private static boolean escapableCharacter(int chIdx, List<Matcher> matchers) {
    boolean advancing = false;
    do {
      for (Matcher m : matchers) {
        if (!m.hitEnd() && (m.end() - 1 < chIdx)) {
          if (m.find()) advancing = true;
        }
        try {
          if (range(m.start(), m.end() - 1).coverʔ(chIdx)) return false;
        } catch (IllegalStateException e) {}
      }
    } while (advancing);
    return true;
  }

  private static void initMatchers(List<Matcher> matchers) {
    for (Matcher m : matchers) {
      m.find();
    }
  }

  private static List<Matcher> patterns2Matchers(Pattern[] patterns,
      final String input) {
    return ra(patterns).map(new TransformBlock<Pattern, Matcher>() {

      @Override
      public Matcher yield(Pattern item) {
        return item.matcher(input);
      }

    });
  }

}
