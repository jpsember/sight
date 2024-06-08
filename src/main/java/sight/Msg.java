package sight;

import static js.base.Tools.*;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import js.base.BasePrinter;

public class Msg {

  public static Map<Integer, String> map = concurrentHashMap();

  public static void remove(int index) {
    set(index);
  }

  public static void set(int index, Object... msg) {
    boolean change = false;
    if (msg.length == 0) {
      if (map.remove(index) != null) {
        pr("Msg.clear", index);
        change = true;
      }
    } else {
      var s = BasePrinter.toString(msg);
      var old = map.put(index, s);
      if (!s.equals(old)) {
        pr("Msg.set", index, s);
        change = true;
      }
    }
    if (change) {
      changeCounter.incrementAndGet();
      pr("incremented change");
    }
  }

  public static AtomicInteger changeCounter = new AtomicInteger();

  public static String get(int index) {
    return map.get(index);
  }

  private Msg() {
  }
}