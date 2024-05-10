package sight;

import static js.base.Tools.*;

import java.util.List;

import js.base.BaseObject;
import js.base.BasePrinter;
import js.data.IntArray;
import js.geometry.MyMath;
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

      if (readIf('<')) {
        keynum.add(readNote());
        // Preserve the relative pitch vars that are in effect after the first note in the chord.
        var s1 = mStartPitchBare;
        var s2 = mStartPitchOctaveOffset;

        while (!readIf('>')) {
          keynum.add(readNote());
        }
        mStartPitchBare = s1;
        mStartPitchOctaveOffset = s2;
      } else {
        keynum.add(readNote());
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

  /**
   * Read a single note, e.g. "gis", "dis''"; return its number
   */
  private int readNote() {
    log("readNote");

    int cStart = mCursor;
    char x = read();
    ensure(x >= 'a' && x <= 'g', "expected pitch char");
    var bareNote = MyMath.myMod(x - 'c', BARE_NOTES_TOTAL);
    log("bare note:", bareNote);

    int accidentalAdj = 0;
    if (readIf("is")) {
      accidentalAdj = 1;
    } else if (readIf("es")) {
      accidentalAdj = -1;
    }
    int cEnd = mCursor;
    String debWord = mText.substring(cStart, cEnd);

    int octaveAdj = 0;
    while (true) {
      if (readIf('\''))
        octaveAdj++;
      else if (readIf(','))
        octaveAdj--;
      else
        break;
    }

    log(VERT_SP, "note exp:", debWord, "bare note:", bareNote, "start pitch:", mStartPitchBare,
        "startPitchOctaveOffset:", mStartPitchOctaveOffset);

    if (mStartPitchBare >= 0) {

      octaveAdj += mStartPitchOctaveOffset;

      var relDist = bareNote - mStartPitchBare;
      if (relDist > 3)
        relDist -= BARE_NOTES_TOTAL;
      else if (relDist < -3)
        relDist += BARE_NOTES_TOTAL;

      bareNote = mStartPitchBare + relDist;

      var modded = MyMath.myMod(bareNote, BARE_NOTES_TOTAL);
      var octAdj = ((bareNote - modded) / BARE_NOTES_TOTAL);

      bareNote = modded;
      octaveAdj += octAdj;
    }

    log("...updating start pitch bare to:", bareNote);
    log("...updating octave offset to:", octaveAdj);
    mStartPitchBare = bareNote;
    mStartPitchOctaveOffset = octaveAdj;

    var note = bareNoteToOctave(bareNote) + octaveAdj * 12 + accidentalAdj;
    log("...calc keybd note as:", note);

    ensure(note >= 0 && note < 88, "note is out of range of 88-key piano", note);
    log("note read:", debWord, note);
    return note;
  }

  // 27 represents the index of the C below middle C on an 88-key piano.
  private static final int[] sBareNoteToPianoKeyTable = { 27, 29, 31, 32, 34, 36, 38 };

  private int bareNoteToOctave(int bareNote) {
    return sBareNoteToPianoKeyTable[bareNote];
  }

  private static final int BARE_NOTES_TOTAL = 7; // c=0,d=1,...,a=5, b = 6

  private String mText;
  private int mCursor;
  private List<Chord> mChords;

  private int mStartPitchBare = -1;
  private int mStartPitchOctaveOffset;
}
