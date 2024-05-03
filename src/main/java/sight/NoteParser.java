package sight;

import static js.base.Tools.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import js.base.BaseObject;
import js.base.BasePrinter;
import js.data.IntArray;
import sight.gen.Chord;

public class NoteParser extends BaseObject {

  public void parse(String lilyPondExpr) {
    if (verbose())
      log("parse:", quote(lilyPondExpr));

    mText = lilyPondExpr;
    mCursor = 0;
    skipWs();

    mChords = arrayList();

    while (!done()) {
      log(VERT_SP, "reading another chord");

      var nb = Chord.newBuilder();
      var keynum = IntArray.newBuilder();

      mStartPitch = -1;
      
      if (readIf('<')) {
        log("entered chord");
        pushState();
        var nt = readNote();
        keynum.add(nt.keyNumbers()[0]);

        while (!readIf('>')) {
          nt = readNote();
          keynum.add(nt.keyNumbers()[0]);
        }

        log("exited chord");

        popState();
      } else {
        var nt = readNote();
        keynum.add(nt.keyNumbers()[0]);
      }

      // read duration (if present)
      if (peekIsDigit()) {
        var duration = readNumber(256);
        log("read duration:", duration);
      }

      nb.keyNumbers(keynum.array());
      mChords.add(nb.build());
    }
  }

  public List<Chord> chords() {
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

  private boolean readIf(String s) {
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
      ensure(value >= 0 && value <= maxValue, "number out of range");
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

  private static final Map<String, Integer> sPitchMap;

  // "<gis b dis>4 <gis' b dis gis> <fis, a cis e> <fis a c dis>"

  private Chord readNote() {
    todo("assume relative pitch, except if no start pitch defined");
    log("readNote");
    var sb = new StringBuilder();
    char x = peek();
    int pitch = x - 'a';
    log("pitch char:", x);
    ensure(pitch >= 0 && pitch <= 6, "expected pitch char");
    sb.append(x);
    read();

    if (readIf("is")) {
      sb.append("is");
    } else if (readIf("es")) {
      sb.append("es");
    }
    var word = sb.toString();
    log("looking at pitch map for:", quote(word));
    var note = sPitchMap.get(word);
    ensure(note != null, "pitch not in map:", word);

    int octaveAdj = 0;
    while (true) {
      if (readIf('\''))
        octaveAdj++;
      else if (readIf(','))
        octaveAdj--;
      else
        break;
    }
    var note2 = note + octaveAdj * 12;
    ensure(note2 >= 0 && note2 < 88, "note is out of range of 88-key piano", note2);
    var cb = Chord.newBuilder();
    int[] singleKeyArray = new int[1];
    singleKeyArray[0] = note2;
    cb.keyNumbers(singleKeyArray);
    var b = cb.build();
    log("note read:", word, b);
    return b;
  }

  // "<gis b dis>4 <gis' b dis gis> <fis, a cis e> <fis a c dis>"

  private void pushState() {
  }

  private void popState() {
  }

  static {
    var x = new HashMap<String, Integer>();
    x.put("c", 27);
    x.put("cis", 27 + 1);
    x.put("des", 29 - 1);
    x.put("d", 29);
    x.put("dis", 29 + 1);
    x.put("ees", 31 - 1);
    x.put("e", 31);
    x.put("f", 32);
    x.put("fis", 32 + 1);
    x.put("ges", 34 - 1);
    x.put("g", 34);
    x.put("gis", 34 + 1);
    x.put("aes", 36 - 1);
    x.put("a", 36);
    x.put("ais", 36 + 1);
    x.put("bes", 38 - 1);
    x.put("b", 38);
    sPitchMap = x;
  }

  private String mText;
  private int mCursor;
  private List<Chord> mChords;
  private int mStartPitch = -1;
}
