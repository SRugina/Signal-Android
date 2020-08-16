package org.thoughtcrime.securesms.stylify;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores the starting index, type of styling(s) (using Character to identify each type), and last
 * index of each match that a rich text regex (see StyleType) finds.
 *
 * @author Sebastian Rugina (dev@srugina.com)
 * @see Stylify
 * @see StyleType
 */
public class Match {
  /**
   * Inclusive.
   */
  public int index;
  public List<Character> type;
  /**
   * Exclusive.
   */
  public int lastIndex;
  
  Match(int index, int lastIndex, List<Character> type) {
    this.index = index;
    this.lastIndex = lastIndex;
    this.type = type;
  }
  
  /**
   * For tests to be able to compare Matches.
   */
  @Override
  @NonNull
  public String toString() {
    List<Object> test = new ArrayList<>();
    test.add(index);
    test.add(lastIndex);
    test.add(type);
    return test.toString();
  }
}