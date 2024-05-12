package sight;

import static js.base.Tools.*;
import static sight.Util.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import js.base.DateTimeTools;
import js.file.Files;
import sight.gen.Chord;
import sight.gen.Hand;
import sight.gen.SightConfig;

public final class Util {

  public static final int MAX_KEY_NUMBER = 88;
  public static final int CASIO_KEY_FIRST = 15;
  public static final int CASIO_KEY_LAST = 75;

  public static final int MIDDLE_C = 39;

  public static final Chord DEATH_CHORD = chordWith(CASIO_KEY_FIRST);
  public static final Chord CHORD_BACKUP = chordWith(CASIO_KEY_LAST - 3);
  public static final Chord PREV_LESSON_CHORD = chordWith(CASIO_KEY_LAST - 1);
  public static final Chord NEXT_LESSON_CHORD = chordWith(CASIO_KEY_LAST - 0);
  public static final Chord CHORD_RESET_SCORE = chordWith(CASIO_KEY_LAST - 2);
  public static final Chord CHORD_REMOVE_LAST = chordWith(CASIO_KEY_LAST - 4);

  public static void z(Object... msg) {
    if (false && alert("z messages in effect"))
      pr(msg);
  }

  /**
   * Get an input stream to a resource
   */
  public static BufferedInputStream openResource(Class theClass, String resourceName) {
    alert("!use new Files method for this, once updated dependency");
    try {
      InputStream is = theClass.getResourceAsStream(resourceName);
      if (is == null) {
        var packageName = theClass.getPackageName();
        // Look for the resource in a src directory
        var alt = "src/main/resources/" + packageName.replace('.', '/') + "/" + resourceName;
        is = Files.openInputStream(new File(alt));
      }
      if (is == null)
        die("no InputStream");

      return new BufferedInputStream(is);
    } catch (Throwable e) {
      pr("Failed to open resource for class:", theClass, "name:", resourceName);
      throw Files.asFileException(e);
    }
  }

  public static void sleepMs(long ms) {
    DateTimeTools.sleepForRealMs(ms);
  }

  public static void close(AutoCloseable... closeables) {
    todo("?replace the Files method with this (autocloseable instead of closeable)");
    try {
      for (var c : closeables) {
        if (c != null)
          c.close();
      }
    } catch (Exception e) {
      throw Files.asFileException(e);
    }
  }

  public static String midiMessage(int statusByteValue) {
    if (sStatusByteNames == null) {
      sStatusByteNames = hashMap();
      var x = sStatusByteNames;
      for (int i = 0; i < 16; i++) {
        x.put(144 + i, "Chan " + (i + 1) + " Note on");
        x.put(128 + i, "Chan " + (i + 1) + " Note off");
      }
    }
    var msg = sStatusByteNames.get(statusByteValue);
    if (msg == null)
      msg = "#UNKNOWN:" + statusByteValue;
    return msg;
  }

  public static int[] intArray(int... values) {
    return values;
  }

  private static Map<Integer, String> sStatusByteNames;

  public static final int ICON_NONE = 0, ICON_POINTER = 1, ICON_RIGHT = 2, ICON_WRONG = 3;

  public static void setConfig(SightConfig c) {
    sConfig = c.build();
  }

  public static SightConfig config() {
    //    if (sConfig == null) {
    //      var f = new File("sight_config.json");
    //      sConfig = Files.parseAbstractDataOpt(SightConfig.DEFAULT_INSTANCE, f);
    //      if (!f.exists())
    //        Files.S.writePretty(f, sConfig);
    //    }
    return sConfig;
  }

  public static Chord chordWith(int... keyNumbers) {
    var c = Chord.newBuilder();
    c.keyNumbers(keyNumbers);
    return c.build();
  }

  private static SightConfig sConfig;

  public static ChordLibrary chordLibrary() {
    if (sChordLibrary == null) {
      var c = new ChordLibrary();
      //c.ignoreCache();
      //c.alertVerbose();
      sChordLibrary = c;
    }
    return sChordLibrary;
  }

  public static String encodeChord(Chord c) {
    var sb = new StringBuilder();
    for (var kn : c.keyNumbers()) {
      if (sb.length() != 0)
        sb.append('.');
      sb.append(kn);
    }
    return sb.toString();
  }

  public static Hand inferHandFromNotes(List<Chord> chords) {
    final var db = false;
    if (db)
      pr("infer hand from notes:", chords);
    checkArgument(!chords.isEmpty());
    int noteMin = chords.get(0).keyNumbers()[0];
    int noteMax = noteMin;
    int noteCount = 0;
    int noteSum = 0;
    for (var c : chords) {
      for (var k : c.keyNumbers()) {
        noteCount++;
        noteSum += k;
        noteMin = Math.min(noteMin, k);
        noteMax = Math.max(noteMax, k);
      }
    }
    int avgNote = noteSum / noteCount;
    if (db)
      pr("avgNote:", avgNote, "min:", noteMin, "max:", noteMax);
    if (avgNote >= MIDDLE_C)
      return Hand.RIGHT;
    return Hand.LEFT;

  }

  private static ChordLibrary sChordLibrary;

}
