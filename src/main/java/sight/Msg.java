package sight;

import static js.base.Tools.*;
import static sight.Util.*;

import java.awt.Color;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import js.base.BasePrinter;

public class Msg {

  private static Map<Integer, String> map = concurrentHashMap();

  public static void remove(int index) {
    set(index);
  }

  public static void set(int index, Object... msg) {
    if (msg.length == 0) {
      if (map.remove(index) != null) {
        changeCounter.incrementAndGet();
      }
    } else {
      var s = BasePrinter.toString(msg);
      var old = map.put(index, s);
      if (!s.equals(old)) {
        changeCounter.incrementAndGet();
      }
    }
  }

  public static int getChangeCounter() {
    return changeCounter.get();
  }

  private static AtomicInteger changeCounter = new AtomicInteger();

  public static Msg get(int index) {
    var s = map.get(index);
    if (s == null)
      return null;
    var c = Color.blue;
    if (s.startsWith("$")) {
      c = new Color(hexToInt(s.substring(1, 3)), hexToInt(s.substring(3, 5)), hexToInt(s.substring(5, 7)));
      s = s.substring(7).trim();
    }
    return new Msg(c, s);
  }

  final String message;
  final Color color;

  private Msg(Color c, String m) {
    message = m;
    color = c;
  }

}
