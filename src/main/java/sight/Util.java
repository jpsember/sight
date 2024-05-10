package sight;

import static js.base.Tools.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Map;

import js.base.DateTimeTools;
import js.file.Files;

public final class Util {

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

}
