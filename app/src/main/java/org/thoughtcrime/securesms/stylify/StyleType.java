package org.thoughtcrime.securesms.stylify;

import android.text.style.CharacterStyle;

import java.util.regex.Pattern;

/**
 * Stores the precompiled regex, char, and CharacterStyle styling for a style type.
 * <p>
 * A regex for each style (as apposed to one universal rich text regex) is used because it is
 * faster, as a universal regex would lead to more backtracking and potentially to an
 * exponential effect called "catastrophic backtracking".
 *
 * @author Sebastian Rugina (dev@srugina.com)
 * @see Stylify
 */
public class StyleType {
  /**
   * POSIX [:punct:], excluding the $ (otherwise we'd have to add all currency symbols for
   * international feature parity), and excluding \ (so one could escape styling if needed), but
   * including spaces.
   * <p>
   * NOTE: If any new styles are added to Stylify's STYLES that are not included in here, they
   * need to be.
   *
   * @see Stylify
   */
  private static final String REGEX_PUNCT = "[^\\p{Punct}\\s~]|[$\\\\]";
  public Pattern regex;
  public char character;
  public CharacterStyle style;
  
  StyleType(char character, CharacterStyle style) {
    this.character = character;
    this.style = style;
    String test = "(?<!" + REGEX_PUNCT + ")" + Pattern.quote(String.valueOf(character)) +
        "[^\\s].*?[^\\s]" + Pattern.quote(String.valueOf(character)) + "(?!" + REGEX_PUNCT + ")";
    this.regex = Pattern.compile(test);
  }
}