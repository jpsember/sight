package sight;

import static js.base.Tools.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Map;

import js.base.DateTimeTools;
import js.file.Files;
import sight.gen.Chord;
import sight.gen.SightConfig;

public final class Util {

  public static final int MAX_KEY_NUMBER = 88;
  public static final int MIDDLE_C = 39;
  public static final Chord DEATH_CHORD = chordWith(36);
  public static final Chord PREV_LESSON_CHORD = chordWith(74);
  public static final Chord NEXT_LESSON_CHORD = chordWith(75);
  public static final Chord CHORD_RESET_SCORE = chordWith(73);
  public static final Chord CHORD_REMOVE_LAST = chordWith(71);

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

  public static SightConfig config() {
    if (sConfig == null) {
      var f = new File("sight_config.json");
      sConfig = Files.parseAbstractDataOpt(SightConfig.DEFAULT_INSTANCE, f);
      if (!f.exists())
        Files.S.writePretty(f, sConfig);
    }
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

  private static ChordLibrary sChordLibrary;

}
