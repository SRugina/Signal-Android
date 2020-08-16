package org.thoughtcrime.securesms.stylify;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.CharacterStyle;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Sorts a Collection of Matches by their (starting) index in ascending order.
 *
 * @see Match
 */
class SortByIndex implements Comparator<Match> {
  public int compare(Match a, Match b) {
    return a.index - b.index;
  }
}

/**
 * Applies rich text styling to messages.
 *
 * @author Sebastian Rugina (dev@srugina.com)
 * @see StyleType
 * @see Match
 */
public class Stylify {
  
  /**
   * To add a new style type, add it here (and to StyleType's REGEX_PUNCT if it is not already
   * contained within it).
   * <p>
   * NOTE: only one character should be used for styling since multi-character styles have not
   * been thoroughly tested.
   *
   * @see StyleType
   */
  public static final Map<String, StyleType> STYLES = new HashMap<>();
  
  static {
    STYLES.put("bold", new StyleType('*', new StyleSpan(Typeface.BOLD)));
    STYLES.put("italic", new StyleType('_', new StyleSpan(Typeface.ITALIC)));
    STYLES.put("strike", new StyleType('~', new StrikethroughSpan()));
  }
  
  public static List<Match> stylifyMatch(Spannable text) {
    List<Match> styleArray = new ArrayList<>();
    
    for (StyleType style : STYLES.values()) {
      Matcher matcher = style.regex.matcher(text);
      while (matcher.find()) {
        List<Character> type = new ArrayList<>();
        type.add(style.character);
        Match stylePair = new Match(matcher.start(), matcher.end() - 1, type);
        styleArray.add(stylePair);
      }
    }
    
    Collections.sort(styleArray, new SortByIndex());
    
    return mergeIntoArray(styleArray);
  }
  
  private static List<Match> mergeIntoArray(List<Match> arr) {
    List<Match> res = new ArrayList<>();
    
    for (Match match : arr) {
      foldInNewMatch(res, match);
    }
    return res;
  }
  
  private static void foldInNewMatch(List<Match> arr, Match match) {
    int i = arr.size() - 1;
    Match prev;
    if (i != -1) {
      prev = arr.get(i);
    } else {
      prev = null;
    }
    
    if (prev != null) {
      int maxStyleDistance = prev.type.size() + match.type.size();
      
      if (match.index < prev.lastIndex) {
        if (match.lastIndex < prev.lastIndex) {
          if (match.index - prev.index > maxStyleDistance) {
            arr.add(i, new Match(prev.index, match.index - 1, new ArrayList<>(prev.type)));
            i++;
            prev = arr.get(i);
            prev.index = match.index;
          }
          if (prev.lastIndex - match.lastIndex > maxStyleDistance) {
            List<Match> newMatches = new ArrayList<>();
            newMatches.add(new Match(prev.index, match.lastIndex,
                new ArrayList<>(prev.type)));
            newMatches.add(new Match(match.lastIndex + 1, prev.lastIndex,
                new ArrayList<>(prev.type)));
            
            arr.addAll(newMatches);
            arr.remove(i);
            prev = arr.get(i);
          }
          prev.type.addAll(match.type);
          return;
        }
        if (match.lastIndex > prev.lastIndex) {
          return;
        }
      }
    }
    arr.add(match);
  }
  
  public static Spannable style(Spannable messageBody) {
    
    int missingStylesCount = 0;
    for (StyleType style : STYLES.values()) {
      if (messageBody.toString().indexOf(style.character) == -1) {
        missingStylesCount++;
      }
    }
    if (missingStylesCount == STYLES.size()) {
      return messageBody;
    }
    
    List<Match> matchData = stylifyMatch(messageBody);
    
    List<Spannable> results = new ArrayList<>();
    int last = 0;
    
    for (Match match : matchData) {
      List<Character> type = new ArrayList<>(match.type);
      int start = match.index;
      int end = match.lastIndex + 1;
      
      if (last < start) {
        SpannableString textWithNoStyle = new SpannableString(messageBody.subSequence(last, start));
        results.add(textWithNoStyle);
      }
      
      SpannableStringBuilder content = new SpannableStringBuilder(messageBody.subSequence(start,
          end));
      
      // remove all styling characters from rendered text's start and end
      for (char ignored : type) {
        if (type.contains(content.charAt(0))) {
          content.delete(0, 1);
        }
        if (type.contains(content.charAt(content.length() - 1))) {
          content.delete(content.length() - 1, content.length());
        }
      }
      
      // apply styling to text
      for (StyleType style : STYLES.values()) {
        if (type.contains(style.character)) {
          content.setSpan(CharacterStyle.wrap(style.style), 0, content.length(),
              Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
      }
      results.add(content);
      last = end;
    }
    
    if (last < messageBody.length()) {
      results.add(new SpannableString(messageBody.subSequence(last, messageBody.length())));
    }
    
    SpannableStringBuilder message = new SpannableStringBuilder();
    for (Spannable text : results) {
      message.append(text);
    }
    
    return message;
  }
}