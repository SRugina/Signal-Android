package org.thoughtcrime.securesms.stylify;

import android.app.Application;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.CharacterStyle;
import android.text.style.MetricAffectingSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.whispersystems.libsignal.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, application = Application.class)
public class StylifyTest {
  
  private List<Character> boldType;
  private List<Character> italicType;
  private List<Character> strikeType;
  private List<Character> boldItalicType;
  private List<Character> strikeBoldItalicType;
  
  private List<CharacterStyle> spanList;
  private List<Match> matchList;
  
  @Before
  public void SetUp() {
    
    boldType = new ArrayList<>(1);
    boldType.add('*');
    
    italicType = new ArrayList<>(1);
    italicType.add('_');
    
    strikeType = new ArrayList<>(1);
    strikeType.add('~');
    
    boldItalicType = new ArrayList<>(boldType);
    boldItalicType.addAll(italicType);
    
    strikeBoldItalicType = new ArrayList<>(strikeType);
    strikeBoldItalicType.addAll(boldType);
    strikeBoldItalicType.addAll(italicType);
    
    spanList = new ArrayList<>();
    matchList = new ArrayList<>();
  }
  
  @Test
  public void detectText_WithStyleOnly() {
    // test stylifyMatch
    
    Spannable text = new SpannableString("*bold*");
    List<Match> matchData = Stylify.stylifyMatch(text);
    
    List<Match> expectedData = new ArrayList<>(1);
    
    expectedData.add(new Match(0, 5, boldType));
    
    assertEquals(expectedData.toString(), matchData.toString());
    
    // test style
    
    Spannable styledText = Stylify.style(text);
    
    // check that the markup tags were removed
    assertEquals("bold", styledText.toString());
    
    // get all the spans attached to the Spannable
    Object[] spans = styledText.getSpans(0, styledText.length(), Object.class);
    
    // check that the correct number of spans were created
    assertEquals(1, spans.length);
    
    // check that the span is of the appropriate instance
    MetricAffectingSpan span1 = (MetricAffectingSpan) spans[0];
    StyleSpan styleSpan1 = (StyleSpan) span1.getUnderlying();
    assertEquals(Typeface.BOLD, styleSpan1.getStyle());

        /*
          Check that the start and end indices are the expected ones.
          The ends will be different to Match.lastIndex as markup tags were removed
         */
    assertEquals(0, styledText.getSpanStart(span1));
    assertEquals(4, styledText.getSpanEnd(span1));
    
    // test styleInPlace
    
    Editable inputBody = new SpannableStringBuilder(text);
    Pair<List<CharacterStyle>, List<Match>> newLists = Stylify.styleInPlace(inputBody, spanList,
        matchList);
    spanList = newLists.first();
    matchList = newLists.second();
    assertEquals(expectedData.toString(), matchList.toString());
    
    // check that no markup tags were removed
    assertEquals(text.toString(), inputBody.toString());
    
    // get all the spans attached to the Spannable
    Object[] inputSpans = inputBody.getSpans(0, inputBody.length(), Object.class);
    @SuppressWarnings("unchecked")
    List<CharacterStyle> inputSpanList =
        (List<CharacterStyle>) (List<?>) Arrays.asList(inputBody.getSpans(0, inputBody.length(),
            Object.class));
    
    assertEquals(spanList, inputSpanList);
    
    // check that the correct number of spans were created
    assertEquals(1, inputSpans.length);
    
    // check that the span is of the appropriate instance
    MetricAffectingSpan inputSpan1 = (MetricAffectingSpan) inputSpans[0];
    StyleSpan inputStyleSpan1 = (StyleSpan) inputSpan1.getUnderlying();
    assertEquals(Typeface.BOLD, inputStyleSpan1.getStyle());

        /*
          Check that the start and end indices are the expected ones.
          The ends should be Match.lastIndex + 1
         */
    assertEquals(0, inputBody.getSpanStart(inputSpan1));
    assertEquals(6, inputBody.getSpanEnd(inputSpan1));
  }
  
  @Test
  public void detectText_FeaturingEveryStyle() {
    // test stylifyMatch
    
    Spannable text = new SpannableString("*bold* _italic_ ~strikethrough~");
    List<Match> matchData = Stylify.stylifyMatch(text);
    
    List<Match> expectedData = new ArrayList<>(3);
    
    expectedData.add(new Match(0, 5, boldType));
    expectedData.add(new Match(7, 14, italicType));
    expectedData.add(new Match(16, 30, strikeType));
    
    assertEquals(expectedData.toString(), matchData.toString());
    
    // test style
    
    Spannable styledText = Stylify.style(text);
    
    // check that the markup tags were removed
    assertEquals("bold italic strikethrough", styledText.toString());
    
    // get all the spans attached to the Spannable
    Object[] spans = styledText.getSpans(0, styledText.length(), Object.class);
    
    // check that the correct number of spans were created
    assertEquals(3, spans.length);
    
    // check that the spans are of the appropriate instances
    MetricAffectingSpan span1 = (MetricAffectingSpan) spans[0];
    StyleSpan styleSpan1 = (StyleSpan) span1.getUnderlying();
    assertEquals(Typeface.BOLD, styleSpan1.getStyle());
    
    MetricAffectingSpan span2 = (MetricAffectingSpan) spans[1];
    StyleSpan styleSpan2 = (StyleSpan) span2.getUnderlying();
    assertEquals(Typeface.ITALIC, styleSpan2.getStyle());
    
    CharacterStyle span3 = (CharacterStyle) spans[2];
    @SuppressWarnings("unused")
    StrikethroughSpan styleSpan3 = (StrikethroughSpan) span3.getUnderlying();

        /*
          Check that the start and end indices are the expected ones.
          The ends will be different to Match.lastIndex as markup tags were removed
         */
    assertEquals(0, styledText.getSpanStart(span1));
    assertEquals(4, styledText.getSpanEnd(span1));
    assertEquals(5, styledText.getSpanStart(span2));
    assertEquals(11, styledText.getSpanEnd(span2));
    assertEquals(12, styledText.getSpanStart(span3));
    assertEquals(25, styledText.getSpanEnd(span3));
    
    // test styleInPlace
    
    Editable inputBody = new SpannableStringBuilder(text);
    Pair<List<CharacterStyle>, List<Match>> newLists = Stylify.styleInPlace(inputBody, spanList,
        matchList);
    spanList = newLists.first();
    matchList = newLists.second();
    assertEquals(expectedData.toString(), matchList.toString());
    
    // check that no markup tags were removed
    assertEquals(text.toString(), inputBody.toString());
    
    // get all the spans attached to the Spannable
    Object[] inputSpans = inputBody.getSpans(0, inputBody.length(), Object.class);
    @SuppressWarnings("unchecked")
    List<CharacterStyle> inputSpanList =
        (List<CharacterStyle>) (List<?>) Arrays.asList(inputBody.getSpans(0, inputBody.length(),
            Object.class));
    
    assertEquals(spanList, inputSpanList);
    
    // check that the correct number of spans were created
    assertEquals(3, inputSpans.length);
    
    // check that the span is of the appropriate instance
    MetricAffectingSpan inputSpan1 = (MetricAffectingSpan) inputSpans[0];
    StyleSpan inputStyleSpan1 = (StyleSpan) inputSpan1.getUnderlying();
    assertEquals(Typeface.BOLD, inputStyleSpan1.getStyle());
    
    MetricAffectingSpan inputSpan2 = (MetricAffectingSpan) inputSpans[1];
    StyleSpan inputStyleSpan2 = (StyleSpan) inputSpan2.getUnderlying();
    assertEquals(Typeface.ITALIC, inputStyleSpan2.getStyle());
    
    CharacterStyle inputSpan3 = (CharacterStyle) inputSpans[2];
    @SuppressWarnings("unused")
    StrikethroughSpan inputStyleSpan3 = (StrikethroughSpan) inputSpan3.getUnderlying();

        /*
          Check that the start and end indices are the expected ones.
          The ends should be Match.lastIndex + 1
         */
    assertEquals(0, inputBody.getSpanStart(inputSpan1));
    assertEquals(6, inputBody.getSpanEnd(inputSpan1));
    assertEquals(7, inputBody.getSpanStart(inputSpan2));
    assertEquals(15, inputBody.getSpanEnd(inputSpan2));
    assertEquals(16, inputBody.getSpanStart(inputSpan3));
    assertEquals(31, inputBody.getSpanEnd(inputSpan3));
  }
  
  @Test
  public void detectText_WithDoubleOverlappedStyle() {
    // test stylifyMatch
    
    Spannable text = new SpannableString("*_bold and italic_*");
    List<Match> matchData = Stylify.stylifyMatch(text);
    
    List<Match> expectedData = new ArrayList<>(1);
    
    expectedData.add(new Match(0, 18, boldItalicType));
    
    assertEquals(expectedData.toString(), matchData.toString());
    
    // test style
    
    Spannable styledText = Stylify.style(text);
    
    // check that the markup tags were removed
    assertEquals("bold and italic", styledText.toString());
    
    // get all the spans attached to the Spannable
    Object[] spans = styledText.getSpans(0, styledText.length(), Object.class);
    
    // check that the correct number of spans were created
    assertEquals(2, spans.length);
    
    // check that the spans are of the appropriate instances
    MetricAffectingSpan span1 = (MetricAffectingSpan) spans[0];
    StyleSpan styleSpan1 = (StyleSpan) span1.getUnderlying();
    assertEquals(Typeface.BOLD, styleSpan1.getStyle());
    
    MetricAffectingSpan span2 = (MetricAffectingSpan) spans[1];
    StyleSpan styleSpan2 = (StyleSpan) span2.getUnderlying();
    assertEquals(Typeface.ITALIC, styleSpan2.getStyle());

        /*
          Check that the start and end indices are the expected ones.
          The ends will be different to Match.lastIndex as markup tags were removed
         */
    assertEquals(0, styledText.getSpanStart(span1));
    assertEquals(15, styledText.getSpanEnd(span1));
    assertEquals(0, styledText.getSpanStart(span2));
    assertEquals(15, styledText.getSpanEnd(span2));
    
    // test styleInPlace
    
    Editable inputBody = new SpannableStringBuilder(text);
    Pair<List<CharacterStyle>, List<Match>> newLists = Stylify.styleInPlace(inputBody, spanList,
        matchList);
    spanList = newLists.first();
    matchList = newLists.second();
    assertEquals(expectedData.toString(), matchList.toString());
    
    // check that no markup tags were removed
    assertEquals(text.toString(), inputBody.toString());
    
    // get all the spans attached to the Spannable
    Object[] inputSpans = inputBody.getSpans(0, inputBody.length(), Object.class);
    @SuppressWarnings("unchecked")
    List<CharacterStyle> inputSpanList =
        (List<CharacterStyle>) (List<?>) Arrays.asList(inputBody.getSpans(0, inputBody.length(),
            Object.class));
    
    assertEquals(spanList, inputSpanList);
    
    // check that the correct number of spans were created
    assertEquals(2, inputSpans.length);
    
    // check that the span is of the appropriate instance
    MetricAffectingSpan inputSpan1 = (MetricAffectingSpan) inputSpans[0];
    StyleSpan inputStyleSpan1 = (StyleSpan) inputSpan1.getUnderlying();
    assertEquals(Typeface.BOLD, inputStyleSpan1.getStyle());
    
    MetricAffectingSpan inputSpan2 = (MetricAffectingSpan) inputSpans[1];
    StyleSpan inputStyleSpan2 = (StyleSpan) inputSpan2.getUnderlying();
    assertEquals(Typeface.ITALIC, inputStyleSpan2.getStyle());

        /*
          Check that the start and end indices are the expected ones.
          The ends should be Match.lastIndex + 1
         */
    assertEquals(0, inputBody.getSpanStart(inputSpan1));
    assertEquals(19, inputBody.getSpanEnd(inputSpan1));
    assertEquals(0, inputBody.getSpanStart(inputSpan2));
    assertEquals(19, inputBody.getSpanEnd(inputSpan2));
  }
  
  @Test
  public void detectText_WithTripleOverlappedStyle() {
    // test stylifyMatch
    
    Spannable text = new SpannableString("~*_strikethrough bold and italic_*~");
    List<Match> matchData = Stylify.stylifyMatch(text);
    
    List<Match> expectedData = new ArrayList<>(1);
    
    expectedData.add(new Match(0, 34, strikeBoldItalicType));
    
    assertEquals(expectedData.toString(), matchData.toString());
    
    // test style
    
    Spannable styledText = Stylify.style(text);
    
    // check that the markup tags were removed
    assertEquals("strikethrough bold and italic", styledText.toString());
    
    // get all the spans attached to the Spannable
    Object[] spans = styledText.getSpans(0, styledText.length(), Object.class);
    
    // check that the correct number of spans were created
    assertEquals(3, spans.length);
    
    // check that the spans are of the appropriate instances
    CharacterStyle span1 = (CharacterStyle) spans[0];
    @SuppressWarnings("unused")
    StrikethroughSpan styleSpan1 = (StrikethroughSpan) span1.getUnderlying();
    
    MetricAffectingSpan span2 = (MetricAffectingSpan) spans[1];
    StyleSpan styleSpan2 = (StyleSpan) span2.getUnderlying();
    assertEquals(Typeface.BOLD, styleSpan2.getStyle());
    
    MetricAffectingSpan span3 = (MetricAffectingSpan) spans[2];
    StyleSpan styleSpan3 = (StyleSpan) span3.getUnderlying();
    assertEquals(Typeface.ITALIC, styleSpan3.getStyle());

        /*
          Check that the start and end indices are the expected ones.
          The ends will be different to Match.lastIndex as markup tags were removed
         */
    assertEquals(0, styledText.getSpanStart(span1));
    assertEquals(29, styledText.getSpanEnd(span1));
    assertEquals(0, styledText.getSpanStart(span2));
    assertEquals(29, styledText.getSpanEnd(span2));
    assertEquals(0, styledText.getSpanStart(span3));
    assertEquals(29, styledText.getSpanEnd(span3));
    
    // test styleInPlace
    
    Editable inputBody = new SpannableStringBuilder(text);
    Pair<List<CharacterStyle>, List<Match>> newLists = Stylify.styleInPlace(inputBody, spanList,
        matchList);
    spanList = newLists.first();
    matchList = newLists.second();
    assertEquals(expectedData.toString(), matchList.toString());
    
    // check that no markup tags were removed
    assertEquals(text.toString(), inputBody.toString());
    
    // get all the spans attached to the Spannable
    Object[] inputSpans = inputBody.getSpans(0, inputBody.length(), Object.class);
    @SuppressWarnings("unchecked")
    List<CharacterStyle> inputSpanList =
        (List<CharacterStyle>) (List<?>) Arrays.asList(inputBody.getSpans(0, inputBody.length(),
            Object.class));
    
    assertEquals(spanList, inputSpanList);
    
    // check that the correct number of spans were created
    assertEquals(3, inputSpans.length);
    
    // check that the span is of the appropriate instance
    CharacterStyle inputSpan1 = (CharacterStyle) inputSpans[0];
    @SuppressWarnings("unused")
    StrikethroughSpan inputStyleSpan1 = (StrikethroughSpan) inputSpan1.getUnderlying();
    
    MetricAffectingSpan inputSpan2 = (MetricAffectingSpan) inputSpans[1];
    StyleSpan inputStyleSpan2 = (StyleSpan) inputSpan2.getUnderlying();
    assertEquals(Typeface.BOLD, inputStyleSpan2.getStyle());
    
    MetricAffectingSpan inputSpan3 = (MetricAffectingSpan) inputSpans[2];
    StyleSpan inputStyleSpan3 = (StyleSpan) inputSpan3.getUnderlying();
    assertEquals(Typeface.ITALIC, inputStyleSpan3.getStyle());

        /*
          Check that the start and end indices are the expected ones.
          The ends should be Match.lastIndex + 1
         */
    assertEquals(0, inputBody.getSpanStart(inputSpan1));
    assertEquals(35, inputBody.getSpanEnd(inputSpan1));
    assertEquals(0, inputBody.getSpanStart(inputSpan2));
    assertEquals(35, inputBody.getSpanEnd(inputSpan2));
    assertEquals(0, inputBody.getSpanStart(inputSpan3));
    assertEquals(35, inputBody.getSpanEnd(inputSpan3));
  }
  
  @Test
  public void detectText_WithNestedStyles() {
    // test stylifyMatch
    
    Spannable text = new SpannableString("*bold text _with italic_ inside*");
    List<Match> matchData = Stylify.stylifyMatch(text);
    
    List<Match> expectedData = new ArrayList<>(3);
    
    expectedData.add(new Match(0, 10, boldType));
    expectedData.add(new Match(11, 23, boldItalicType));
    expectedData.add(new Match(24, 31, boldType));
    
    assertEquals(expectedData.toString(), matchData.toString());
    
    // test style
    
    Spannable styledText = Stylify.style(text);
    
    // check that the markup tags were removed
    assertEquals("bold text with italic inside", styledText.toString());
    
    // get all the spans attached to the Spannable
    Object[] spans = styledText.getSpans(0, styledText.length(), Object.class);
    
    // check that the correct number of spans were created
    assertEquals(4, spans.length);
    
    // check that the spans are of the appropriate instances
    MetricAffectingSpan span1 = (MetricAffectingSpan) spans[0];
    StyleSpan styleSpan1 = (StyleSpan) span1.getUnderlying();
    assertEquals(Typeface.BOLD, styleSpan1.getStyle());
    
    MetricAffectingSpan span2 = (MetricAffectingSpan) spans[1];
    StyleSpan styleSpan2 = (StyleSpan) span2.getUnderlying();
    assertEquals(Typeface.BOLD, styleSpan2.getStyle());
    
    MetricAffectingSpan span3 = (MetricAffectingSpan) spans[2];
    StyleSpan styleSpan3 = (StyleSpan) span3.getUnderlying();
    assertEquals(Typeface.ITALIC, styleSpan3.getStyle());
    
    MetricAffectingSpan span4 = (MetricAffectingSpan) spans[3];
    StyleSpan styleSpan4 = (StyleSpan) span4.getUnderlying();
    assertEquals(Typeface.BOLD, styleSpan4.getStyle());

        /*
          Check that the start and end indices are the expected ones.
          The ends will be different to Match.lastIndex as markup tags were removed
         */
    assertEquals(0, styledText.getSpanStart(span1));
    assertEquals(10, styledText.getSpanEnd(span1));
    assertEquals(10, styledText.getSpanStart(span2));
    assertEquals(21, styledText.getSpanEnd(span2));
    assertEquals(10, styledText.getSpanStart(span3));
    assertEquals(21, styledText.getSpanEnd(span3));
    assertEquals(21, styledText.getSpanStart(span4));
    assertEquals(28, styledText.getSpanEnd(span4));
    
    // test styleInPlace
    
    Editable inputBody = new SpannableStringBuilder(text);
    Pair<List<CharacterStyle>, List<Match>> newLists = Stylify.styleInPlace(inputBody, spanList,
        matchList);
    spanList = newLists.first();
    matchList = newLists.second();
    assertEquals(expectedData.toString(), matchList.toString());
    
    // check that no markup tags were removed
    assertEquals(text.toString(), inputBody.toString());
    
    // get all the spans attached to the Spannable
    Object[] inputSpans = inputBody.getSpans(0, inputBody.length(), Object.class);
    @SuppressWarnings("unchecked")
    List<CharacterStyle> inputSpanList =
        (List<CharacterStyle>) (List<?>) Arrays.asList(inputBody.getSpans(0, inputBody.length(),
            Object.class));
    
    assertEquals(spanList, inputSpanList);
    
    // check that the correct number of spans were created
    assertEquals(4, inputSpans.length);
    
    // check that the span is of the appropriate instance
    MetricAffectingSpan inputSpan1 = (MetricAffectingSpan) inputSpans[0];
    StyleSpan inputStyleSpan1 = (StyleSpan) inputSpan1.getUnderlying();
    assertEquals(Typeface.BOLD, inputStyleSpan1.getStyle());
    
    MetricAffectingSpan inputSpan2 = (MetricAffectingSpan) inputSpans[1];
    StyleSpan inputStyleSpan2 = (StyleSpan) inputSpan2.getUnderlying();
    assertEquals(Typeface.BOLD, inputStyleSpan2.getStyle());
    
    MetricAffectingSpan inputSpan3 = (MetricAffectingSpan) inputSpans[2];
    StyleSpan inputStyleSpan3 = (StyleSpan) inputSpan3.getUnderlying();
    assertEquals(Typeface.ITALIC, inputStyleSpan3.getStyle());
    
    MetricAffectingSpan inputSpan4 = (MetricAffectingSpan) inputSpans[3];
    StyleSpan inputStyleSpan4 = (StyleSpan) inputSpan4.getUnderlying();
    assertEquals(Typeface.BOLD, inputStyleSpan4.getStyle());

        /*
          Check that the start and end indices are the expected ones.
          The ends should be Match.lastIndex + 1
         */
    assertEquals(0, inputBody.getSpanStart(inputSpan1));
    assertEquals(11, inputBody.getSpanEnd(inputSpan1));
    assertEquals(11, inputBody.getSpanStart(inputSpan2));
    assertEquals(24, inputBody.getSpanEnd(inputSpan2));
    assertEquals(11, inputBody.getSpanStart(inputSpan3));
    assertEquals(24, inputBody.getSpanEnd(inputSpan3));
    assertEquals(24, inputBody.getSpanStart(inputSpan4));
    assertEquals(32, inputBody.getSpanEnd(inputSpan4));
  }
  
  @Test
  public void detectText_StartingAndEndingWithStyle() {
    // test stylifyMatch
    
    Spannable text = new SpannableString("*bold* Yes? No? *another bold*");
    List<Match> matchData = Stylify.stylifyMatch(text);
    
    List<Match> expectedData = new ArrayList<>(2);
    
    expectedData.add(new Match(0, 5, boldType));
    expectedData.add(new Match(16, 29, boldType));
    
    assertEquals(expectedData.toString(), matchData.toString());
    
    // test style
    
    Spannable styledText = Stylify.style(text);
    
    // check that the markup tags were removed
    assertEquals("bold Yes? No? another bold", styledText.toString());
    
    // get all the spans attached to the Spannable
    Object[] spans = styledText.getSpans(0, styledText.length(), Object.class);
    
    // check that the correct number of spans were created
    assertEquals(2, spans.length);
    
    // check that the spans are of the appropriate instances
    MetricAffectingSpan span1 = (MetricAffectingSpan) spans[0];
    StyleSpan styleSpan1 = (StyleSpan) span1.getUnderlying();
    assertEquals(Typeface.BOLD, styleSpan1.getStyle());
    
    MetricAffectingSpan span2 = (MetricAffectingSpan) spans[1];
    StyleSpan styleSpan2 = (StyleSpan) span2.getUnderlying();
    assertEquals(Typeface.BOLD, styleSpan2.getStyle());

        /*
          Check that the start and end indices are the expected ones.
          The ends will be different to Match.lastIndex as markup tags were removed
         */
    assertEquals(0, styledText.getSpanStart(span1));
    assertEquals(4, styledText.getSpanEnd(span1));
    assertEquals(14, styledText.getSpanStart(span2));
    assertEquals(26, styledText.getSpanEnd(span2));
    
    // test styleInPlace
    
    Editable inputBody = new SpannableStringBuilder(text);
    Pair<List<CharacterStyle>, List<Match>> newLists = Stylify.styleInPlace(inputBody, spanList,
        matchList);
    spanList = newLists.first();
    matchList = newLists.second();
    assertEquals(expectedData.toString(), matchList.toString());
    
    // check that no markup tags were removed
    assertEquals(text.toString(), inputBody.toString());
    
    // get all the spans attached to the Spannable
    Object[] inputSpans = inputBody.getSpans(0, inputBody.length(), Object.class);
    @SuppressWarnings("unchecked")
    List<CharacterStyle> inputSpanList =
        (List<CharacterStyle>) (List<?>) Arrays.asList(inputBody.getSpans(0, inputBody.length(),
            Object.class));
    
    assertEquals(spanList, inputSpanList);
    
    // check that the correct number of spans were created
    assertEquals(2, inputSpans.length);
    
    // check that the span is of the appropriate instance
    MetricAffectingSpan inputSpan1 = (MetricAffectingSpan) inputSpans[0];
    StyleSpan inputStyleSpan1 = (StyleSpan) inputSpan1.getUnderlying();
    assertEquals(Typeface.BOLD, inputStyleSpan1.getStyle());
    
    MetricAffectingSpan inputSpan2 = (MetricAffectingSpan) inputSpans[1];
    StyleSpan inputStyleSpan2 = (StyleSpan) inputSpan2.getUnderlying();
    assertEquals(Typeface.BOLD, inputStyleSpan2.getStyle());

        /*
          Check that the start and end indices are the expected ones.
          The ends should be Match.lastIndex + 1
         */
    assertEquals(0, inputBody.getSpanStart(inputSpan1));
    assertEquals(6, inputBody.getSpanEnd(inputSpan1));
    assertEquals(16, inputBody.getSpanStart(inputSpan2));
    assertEquals(30, inputBody.getSpanEnd(inputSpan2));
  }
  
  @Test
  public void detectText_WithStyleInMiddle() {
    // test stylifyMatch
    
    Spannable text = new SpannableString("Before. *bold* After.");
    List<Match> matchData = Stylify.stylifyMatch(text);
    
    List<Match> expectedData = new ArrayList<>(1);
    
    expectedData.add(new Match(8, 13, boldType));
    
    assertEquals(expectedData.toString(), matchData.toString());
    
    // test style
    
    Spannable styledText = Stylify.style(text);
    
    // check that the markup tags were removed
    assertEquals("Before. bold After.", styledText.toString());
    
    // get all the spans attached to the Spannable
    Object[] spans = styledText.getSpans(0, styledText.length(), Object.class);
    
    // check that the correct number of spans were created
    assertEquals(1, spans.length);
    
    // check that the span is of the appropriate instance
    MetricAffectingSpan span1 = (MetricAffectingSpan) spans[0];
    StyleSpan styleSpan1 = (StyleSpan) span1.getUnderlying();
    assertEquals(Typeface.BOLD, styleSpan1.getStyle());

        /*
          Check that the start and end indices are the expected ones.
          The ends will be different to Match.lastIndex as markup tags were removed
         */
    assertEquals(8, styledText.getSpanStart(span1));
    assertEquals(12, styledText.getSpanEnd(span1));
    
    // test styleInPlace
    
    Editable inputBody = new SpannableStringBuilder(text);
    Pair<List<CharacterStyle>, List<Match>> newLists = Stylify.styleInPlace(inputBody, spanList,
        matchList);
    spanList = newLists.first();
    matchList = newLists.second();
    assertEquals(expectedData.toString(), matchList.toString());
    
    // check that no markup tags were removed
    assertEquals(text.toString(), inputBody.toString());
    
    // get all the spans attached to the Spannable
    Object[] inputSpans = inputBody.getSpans(0, inputBody.length(), Object.class);
    @SuppressWarnings("unchecked")
    List<CharacterStyle> inputSpanList =
        (List<CharacterStyle>) (List<?>) Arrays.asList(inputBody.getSpans(0, inputBody.length(),
            Object.class));
    
    assertEquals(spanList, inputSpanList);
    
    // check that the correct number of spans were created
    assertEquals(1, inputSpans.length);
    
    // check that the span is of the appropriate instance
    MetricAffectingSpan inputSpan1 = (MetricAffectingSpan) inputSpans[0];
    StyleSpan inputStyleSpan1 = (StyleSpan) inputSpan1.getUnderlying();
    assertEquals(Typeface.BOLD, inputStyleSpan1.getStyle());

        /*
          Check that the start and end indices are the expected ones.
          The ends should be Match.lastIndex + 1
         */
    assertEquals(8, inputBody.getSpanStart(inputSpan1));
    assertEquals(14, inputBody.getSpanEnd(inputSpan1));
  }
  
  @Test
  public void detectText_WithStyleSurroundedByPunctuation() {
    // test stylifyMatch
    
    Spannable text = new SpannableString("(*bold*) _italic_,");
    List<Match> matchData = Stylify.stylifyMatch(text);
    
    List<Match> expectedData = new ArrayList<>(2);
    
    expectedData.add(new Match(1, 6, boldType));
    expectedData.add(new Match(9, 16, italicType));
    
    assertEquals(expectedData.toString(), matchData.toString());
    
    // test style
    
    Spannable styledText = Stylify.style(text);
    
    // check that the markup tags were removed
    assertEquals("(bold) italic,", styledText.toString());
    
    // get all the spans attached to the Spannable
    Object[] spans = styledText.getSpans(0, styledText.length(), Object.class);
    
    // check that the correct number of spans were created
    assertEquals(2, spans.length);
    
    // check that the spans are of the appropriate instances
    MetricAffectingSpan span1 = (MetricAffectingSpan) spans[0];
    StyleSpan styleSpan1 = (StyleSpan) span1.getUnderlying();
    assertEquals(Typeface.BOLD, styleSpan1.getStyle());
    
    MetricAffectingSpan span2 = (MetricAffectingSpan) spans[1];
    StyleSpan styleSpan2 = (StyleSpan) span2.getUnderlying();
    assertEquals(Typeface.ITALIC, styleSpan2.getStyle());

        /*
          Check that the start and end indices are the expected ones.
          The ends will be different to Match.lastIndex as markup tags were removed
         */
    assertEquals(1, styledText.getSpanStart(span1));
    assertEquals(5, styledText.getSpanEnd(span1));
    assertEquals(7, styledText.getSpanStart(span2));
    assertEquals(13, styledText.getSpanEnd(span2));
    
    // test styleInPlace
    
    Editable inputBody = new SpannableStringBuilder(text);
    Pair<List<CharacterStyle>, List<Match>> newLists = Stylify.styleInPlace(inputBody, spanList,
        matchList);
    spanList = newLists.first();
    matchList = newLists.second();
    assertEquals(expectedData.toString(), matchList.toString());
    
    // check that no markup tags were removed
    assertEquals(text.toString(), inputBody.toString());
    
    // get all the spans attached to the Spannable
    Object[] inputSpans = inputBody.getSpans(0, inputBody.length(), Object.class);
    @SuppressWarnings("unchecked")
    List<CharacterStyle> inputSpanList =
        (List<CharacterStyle>) (List<?>) Arrays.asList(inputBody.getSpans(0, inputBody.length(),
            Object.class));
    
    assertEquals(spanList, inputSpanList);
    
    // check that the correct number of spans were created
    assertEquals(2, inputSpans.length);
    
    // check that the span is of the appropriate instance
    MetricAffectingSpan inputSpan1 = (MetricAffectingSpan) inputSpans[0];
    StyleSpan inputStyleSpan1 = (StyleSpan) inputSpan1.getUnderlying();
    assertEquals(Typeface.BOLD, inputStyleSpan1.getStyle());
    
    MetricAffectingSpan inputSpan2 = (MetricAffectingSpan) inputSpans[1];
    StyleSpan inputStyleSpan2 = (StyleSpan) inputSpan2.getUnderlying();
    assertEquals(Typeface.ITALIC, inputStyleSpan2.getStyle());

        /*
          Check that the start and end indices are the expected ones.
          The ends should be Match.lastIndex + 1
         */
    assertEquals(1, inputBody.getSpanStart(inputSpan1));
    assertEquals(7, inputBody.getSpanEnd(inputSpan1));
    assertEquals(9, inputBody.getSpanStart(inputSpan2));
    assertEquals(17, inputBody.getSpanEnd(inputSpan2));
  }
  
  @Test
  public void doNotDetectText_WithNoStyle() {
    // test stylifyMatch
    
    Spannable text = new SpannableString("Plain text");
    List<Match> matchData = Stylify.stylifyMatch(text);
    
    assertEquals(matchData.toString(), "[]");
    
    Spannable styledText = Stylify.style(text);
    
    // check that the plaintext remained intact
    assertEquals(text.toString(), styledText.toString());
    
    // get all the spans attached to the Spannable (should be 0)
    Object[] spans = styledText.getSpans(0, styledText.length(), Object.class);
    
    // check that no spans were created
    assertEquals(0, spans.length);
    
    // test styleInPlace
    
    Editable inputBody = new SpannableStringBuilder(text);
    Pair<List<CharacterStyle>, List<Match>> newLists = Stylify.styleInPlace(inputBody, spanList,
        matchList);
    spanList = newLists.first();
    matchList = newLists.second();
    assertEquals(matchList.toString(), "[]");
    
    // check that the plaintext remained intact
    assertEquals(text.toString(), inputBody.toString());
    
    // get all the spans attached to the Spannable (should be 0)
    Object[] inputSpans = inputBody.getSpans(0, inputBody.length(), Object.class);
    @SuppressWarnings("unchecked")
    List<CharacterStyle> inputSpanList =
        (List<CharacterStyle>) (List<?>) Arrays.asList(inputBody.getSpans(0, inputBody.length(),
            Object.class));
    
    assertEquals(spanList, inputSpanList);
    
    // check that no spans were created
    assertEquals(0, inputSpans.length);
  }
  
  @Test
  public void doNotDetectText_WithInvalidStyles() {
    // test stylifyMatch
    
    Spannable text = new SpannableString("d*bold* _ italic_ ~strikethrough~d \\*more bold*");
    List<Match> matchData = Stylify.stylifyMatch(text);
    
    assertEquals(matchData.toString(), "[]");
    
    Spannable styledText = Stylify.style(text);
    
    // check that the plaintext remained intact
    assertEquals(text.toString(), styledText.toString());
    
    // get all the spans attached to the Spannable (should be 0)
    Object[] spans = styledText.getSpans(0, styledText.length(), Object.class);
    
    // check that no spans were created
    assertEquals(0, spans.length);
    
    // test styleInPlace
    
    Editable inputBody = new SpannableStringBuilder(text);
    Pair<List<CharacterStyle>, List<Match>> newLists = Stylify.styleInPlace(inputBody, spanList,
        matchList);
    spanList = newLists.first();
    matchList = newLists.second();
    assertEquals(matchList.toString(), "[]");
    
    // check that the plaintext remained intact
    assertEquals(text.toString(), inputBody.toString());
    
    // get all the spans attached to the Spannable (should be 0)
    Object[] inputSpans = inputBody.getSpans(0, inputBody.length(), Object.class);
    @SuppressWarnings("unchecked")
    List<CharacterStyle> inputSpanList =
        (List<CharacterStyle>) (List<?>) Arrays.asList(inputBody.getSpans(0, inputBody.length(),
            Object.class));
    
    assertEquals(spanList, inputSpanList);
    
    // check that no spans were created
    assertEquals(0, inputSpans.length);
  }
  
  @Test
  public void doNotDetectText_WithStyleOverMultipleLines() {
    // test stylifyMatch
    
    Spannable text = new SpannableString("_italic\nover\nmultiple\nlines_");
    List<Match> matchData = Stylify.stylifyMatch(text);
    
    assertEquals(matchData.toString(), "[]");
    
    Spannable styledText = Stylify.style(text);
    
    // check that the plaintext remained intact
    assertEquals(text.toString(), styledText.toString());
    
    // get all the spans attached to the Spannable (should be 0)
    Object[] spans = styledText.getSpans(0, styledText.length(), Object.class);
    
    // check that no spans were created
    assertEquals(0, spans.length);
    
    // test styleInPlace
    
    Editable inputBody = new SpannableStringBuilder(text);
    Pair<List<CharacterStyle>, List<Match>> newLists = Stylify.styleInPlace(inputBody, spanList,
        matchList);
    spanList = newLists.first();
    matchList = newLists.second();
    assertEquals(matchList.toString(), "[]");
    
    // check that the plaintext remained intact
    assertEquals(text.toString(), inputBody.toString());
    
    // get all the spans attached to the Spannable (should be 0)
    Object[] inputSpans = inputBody.getSpans(0, inputBody.length(), Object.class);
    @SuppressWarnings("unchecked")
    List<CharacterStyle> inputSpanList =
        (List<CharacterStyle>) (List<?>) Arrays.asList(inputBody.getSpans(0, inputBody.length(),
            Object.class));
    
    assertEquals(spanList, inputSpanList);
    
    // check that no spans were created
    assertEquals(0, inputSpans.length);
  }
}