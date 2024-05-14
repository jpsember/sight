package sight;

import static js.base.Tools.*;

import java.util.List;

import js.base.BaseObject;
import js.base.BasePrinter;
import js.data.IntArray;
import sight.gen.Chord;
import static sight.Util.*;

public class ChordParser extends BaseObject {

  public void parse(String chordsExpr) {
    if (verbose())
      log("parse:", quote(chordsExpr));

    mText = chordsExpr;
    mCursor = 0;
    skipWs();

    mChords = arrayList();

    while (!done()) {
      log(VERT_SP, "reading another chord");

      var nb = Chord.newBuilder();
      var keynum = IntArray.newBuilder();

      if (!peekIsDigit()) {
        badArg("trouble parsing:", INDENT, quote(chordsExpr));
      }

      while (peekIsDigit()) {
        var noteNum = readNumber(MAX_KEY_NUMBER);
        keynum.add(noteNum);
        if (!readIf('.'))
          break;
      }

      todo("?add support for duration later");

      nb.keyNumbers(keynum.array());
      mChords.add(nb.build());
    }
  }

  public List<Chord> chords() {
    todo("have parser determine if chords are for two hands");
    checkState(mChords != null);
    return mChords;
  }

  private boolean done() {
    return mCursor == mText.length();
  }

  private boolean readIf(char c) {
    if (peekOrZero() == c) {
      read();
      return true;
    }
    return false;
  }

  /* private */ boolean readIf(String s) {
    for (int i = 0; i < s.length(); i++) {
      if (peekOrZero(i) != s.charAt(i))
        return false;
    }
    advanceCursor(s.length());
    return true;
  }

  private char read() {
    if (verbose())
      log("reading char:", context());
    var c = peek();
    advanceCursor(1);
    return c;
  }

  private void skipWs() {
    while (!done() && peek() <= ' ')
      mCursor++;
  }

  private boolean peekIsDigit() {
    return !done() && isDigit(peek());
  }

  private static boolean isDigit(char c) {
    return (c >= '0' && c <= '9');
  }

  private char peek() {
    return mText.charAt(mCursor);
  }

  private char peekOrZero() {
    return peekOrZero(0);
  }

  private char peekOrZero(int offset) {
    int i = mCursor + offset;
    if (i >= mText.length())
      return 0;
    return mText.charAt(i);
  }

  private boolean ensure(boolean condition, Object... messages) {
    if (condition)
      return true;

    var msg = BasePrinter.toString(messages);

    if (mText != null)
      msg = msg + "\n at: " + context();
    var ex = new ParserException(msg, null);
    throw ex;
  }

  private String context() {
    if (mText == null)
      return "<no text yet>";
    return "[..." + mText.substring(mCursor) + "]";
  }

  public static class ParserException extends RuntimeException {

    public static ParserException withCause(Throwable cause, Object... messageObjects) {
      String message;
      if (messageObjects.length == 0)
        message = cause.getMessage();
      else
        message = BasePrinter.toString(messageObjects);
      return new ParserException(message, cause);
    }

    public static ParserException withMessage(Object... messageObjects) {
      return withCause(null, messageObjects);
    }

    public ParserException(String message, Throwable cause) {
      super(message, cause);
    }

  }

  private int readNumber(int maxValue) {
    ensure(peekIsDigit(), "expected number");
    int ndig = 0;
    while (true) {
      char c = peekOrZero(ndig);
      if (!isDigit(c))
        break;
      ndig++;
    }
    int value = 0;
    try {
      var digitSeq = mText.substring(mCursor, mCursor + ndig);
      log("digit seq:", quote(digitSeq));
      value = Integer.parseInt(digitSeq);
      ensure(value >= 0 && value < maxValue, "number out of range");
    } catch (Throwable t) {
      ensure(false, "trouble parsing number");
    }
    advanceCursor(ndig);
    return value;
  }

  private void advanceCursor(int chars) {
    ensure(chars >= 0, "can't advance negative amt");
    ensure(mCursor + chars <= mText.length(), "attempt to advance cursor past end of string");
    mCursor += chars;
    skipWs();
  }

  private String mText;
  private int mCursor;
  private List<Chord> mChords;
}
