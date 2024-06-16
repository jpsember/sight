package sight;

import static js.base.Tools.*;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;

import js.base.BaseObject;
import js.data.DataUtil;
import sight.gen.Chord;
import sight.gen.SightConfig;
import static sight.Util.*;

/**
 * This must be thread safe!
 */
class MidiReceiver extends BaseObject implements Receiver {

  public MidiReceiver(SightConfig config) {
    mConfig = config;
    log("constructed");
  }

  @Override
  public synchronized void send(MidiMessage message, long timeStamp) {
    var by = message.getMessage();
    if (verbose() && false)
      log("MidiMessage:", DataUtil.hexDump(by));

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

    long ourTimestamp = System.currentTimeMillis();

    var highNyb = status & 0xf0;
    var channel = status & 0x0f;
    if (highNyb == 0x90) {
      if (channel != 0) {
        pr("*** received note on, channel:", channel);
        return;
      }
      int pitch = pitchToKeyNumber(by[1]);
      i12("key DOWN:", -pitch);

      mDownSet.add(pitch);
      switch (mState) {
      case STATE_ALLUP:
        mChordSet.add(pitch);
        setState(STATE_SOMEDOWN, "key down");
        mState = STATE_SOMEDOWN;
        mActionTime = ourTimestamp;
        break;
      case STATE_SOMEDOWN:
        mChordSet.add(pitch);
        mActionTime = ourTimestamp;
        break;
      }
    } else if (highNyb == 0x80) {
      if (channel != 0)
        return;
      int pitch = pitchToKeyNumber(by[1]);
      i12("key UP  :", pitch);

      mDownSet.remove(pitch);
      switch (mState) {
      case STATE_SOMEDOWN:
        constructChordFromChordKeys();
        setState(mDownSet.isEmpty() ? STATE_ALLUP : STATE_WAITUP, "key up");
        break;
      case STATE_WAITUP:
        if (mDownSet.isEmpty())
          setState(STATE_ALLUP, "all keys up");
        break;
      }
    }
  }

  private int pitchToKeyNumber(int pitch) {
    var mod = pitch + PITCH_TO_PIANO_KEY_NUMBER_OFFSET;
    checkArgument(mod >= 0 && mod < MAX_KEY_NUMBER, "pitch is outside range of 88-key piano:", pitch);
    return mod;
  }

  @Override
  public void close() {
    log("closing");
  }

  public synchronized Chord currentChord() {
    if (mState == STATE_SOMEDOWN) {
      checkState(mActionTime != 0);
      long currTime = System.currentTimeMillis();
      long timeSinceAction = currTime - mActionTime;
      i12("SOMEDOWN, time since action:", timeSinceAction);
      if (timeSinceAction >= mConfig.quiescentChordMs()) {
        constructChordFromChordKeys();
        i12("constructed current chord:", mCurrentChord);
        setState(STATE_WAITUP, "delay elapsed since action time");
      }
    }
    return mCurrentChord;
  }

  private void constructChordFromChordKeys() {
    List<Integer> x = arrayList();
    x.addAll(mChordSet);
    mCurrentChord = Chord.newBuilder().keyNumbers(DataUtil.intArray(x)).build();
    mChordSet.clear();
  }

  private static final int STATE_ALLUP = 0, STATE_SOMEDOWN = 1, STATE_WAITUP = 2;

  private void setState(int newState, String cause) {
    i12("state changing from", stateName(mState), "==>", stateName(newState), "; cause:", cause);
    log("state changing from", stateName(mState), "==>", stateName(newState), "; cause:", cause);
    mState = newState;
  }

  private static List<String> sStateNames = split("ALLUP SOMEDOWN WAITUP", ' ');

  private static String stateName(int s) {
    return sStateNames.get(s);
  }

  private SightConfig mConfig;
  private SortedSet<Integer> mDownSet = new TreeSet<>();
  private SortedSet<Integer> mChordSet = new TreeSet<>();
  private Chord mCurrentChord = Chord.DEFAULT_INSTANCE;
  private int mState;
  private long mActionTime;

}