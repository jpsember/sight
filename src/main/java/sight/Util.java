package sight;

import static js.base.Tools.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;

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

}
