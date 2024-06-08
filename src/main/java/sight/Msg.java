package sight;

import static js.base.Tools.*;

import java.awt.Color;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import js.base.BasePrinter;

public class Msg {

  public static Msg DEFAULT_MESSAGE = new Msg(Color.black, "");

  public static Map<Integer, Msg> map = concurrentHashMap();

  public static void set(int index, Color color, Object... msg) {
    boolean change = false;
    if (msg.length == 0) {
      pr("Msg.clear", index);
      if (map.remove(index) != null)
        change = true;
    } else {
      Msg m = new Msg(color, msg);
      pr("Msg.set", index, msg);

      var old = map.put(index, m);
      if (old == null || !m.key().equals(old.key()))
        change = true;
    }
    if (change) {
      changeCounter.incrementAndGet();
      pr("incremented change");
    }
  }

  public static AtomicInteger changeCounter = new AtomicInteger();
  //  public static void set(int index, Msg msg) {
  //    if (msg == null || nullOrEmpty(msg.mMessage))
  //      map.remove(index);
  //    else
  //      map.put(index, msg);
  //  }

  public static Msg get(int index) {
    return map.get(index);
  }

  public Msg(Color color, Object... msg) {
    mColor = color;
    mMessage = BasePrinter.toString(msg);
  }

  @Override
  public String toString() {
    return mMessage;
  }

  public String key() {
    return mColor.toString() + ":" + mColor;
  }

  public Color color() {
    return mColor;
  }

  private String mMessage;
  private Color mColor;

}