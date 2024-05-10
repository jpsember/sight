package sight;

import static js.base.Tools.*;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;

import js.base.BaseObject;
import js.data.DataUtil;

/**
 * This must be thread safe!
 */
class OurReceiver extends BaseObject implements Receiver {

  public OurReceiver() {
    setName("OurReceiver");
    setVerbose(true);
    log("constructed");
  }

  @Override
  public synchronized void send(MidiMessage message, long timeStamp) {
//    var t = Thread.currentThread();
//    pr("OurReceiver.send:", t, "id:", t.getId(), "name:", t.getName());
    var by = message.getMessage();
    //log("MidiMessage:", DataUtil.hexDump(by));

    if (by.length < 2) {
      pr("*** unexpected MidiMessage length:", DataUtil.hexDump(by));
      return;
    }
    var status = by[0];
    var data1 = by[1];
    var data2 = 0;
    if (by.length >= 3)
      data2 = by[2];

    if ((status & 0x80) != 0x80 || ((data1 | data2) & 0x80) != 0) {
      pr("*** ill-formed MidiMessage:", DataUtil.hexDump(by));
      return;
    }

    //    log("Receiver send, timestamp:", timeStamp, DataUtil.hex8(status), "status:", midiMessage(status));

    var highNyb = status & 0xf0;
    var channel = status & 0x0f;
    if (highNyb == 0x90) {
      if (channel != 0) {
        pr("*** received note on, channel:", channel);
        return;
      }
      int pitch = by[1];
      // log("...note on, pitch:", pitch);
      mKeysPressedSet.add(pitch);
      mLastPressTimestamp = System.currentTimeMillis();
      if (false && verbose())
        log("chord:", currentChord());
    } else if (highNyb == 0x80) {
      if (channel != 0)
        return;
      int pitch = by[1];
      mKeysPressedSet.remove(pitch);
      mLastPressTimestamp = System.currentTimeMillis();
      if (false && verbose())
        log("chord:", currentChord()); //chordStr());
    }
  }

  @Override
  public void close() {
    log("closing");
  }

  //  private String chordStr() {
  //    var sb = new StringBuilder();
  //    for (var x : mKeysPressedSet) {
  //      sb.append(' ');
  //      sb.append(x);
  //    }
  //    return "[" + sb + " ]";
  //  }

  private static final int QUIESCENT_CHORD_MS = 100;

  public synchronized List<Integer> currentChord() {
    // Update the chord if there hasn't been recent action
    if (mLastPressTimestamp != mCurrentChordTimestamp) {
      var tm = System.currentTimeMillis();
      if (tm - mLastPressTimestamp >= QUIESCENT_CHORD_MS) {
        List<Integer> x = arrayList();
        x.addAll(mKeysPressedSet);
        mCurrentChord = x;
        mCurrentChordTimestamp = mLastPressTimestamp;
      }
    }
    return mCurrentChord;
  }

  private SortedSet<Integer> mKeysPressedSet = new TreeSet<>();
  private long mLastPressTimestamp;
  private List<Integer> mCurrentChord = arrayList();
  private long mCurrentChordTimestamp;

}