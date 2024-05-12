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

/**
 * This must be thread safe!
 */
class MidiReceiver extends BaseObject implements Receiver {

  // We need to convert from MIDI pitches to the index of the key on an 88-key piano.
  private static final int PITCH_TO_PIANO_KEY_NUMBER_OFFSET = 39 - 60;

  public MidiReceiver(SightConfig config) {
    mConfig = config;
    log("constructed");
  }

  @Override
  public synchronized void send(MidiMessage message, long timeStamp) {
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

    var highNyb = status & 0xf0;
    var channel = status & 0x0f;
    if (highNyb == 0x90) {
      if (channel != 0) {
        pr("*** received note on, channel:", channel);
        return;
      }
      int pitch = pitchToKeyNumber(by[1]);
      mKeysPressedSet.add(pitch);
      mChordSet.add(pitch);
      mLastPressTimestamp = System.currentTimeMillis();
      log("note on, chord set:", mChordSet);
    } else if (highNyb == 0x80) {
      if (channel != 0)
        return;
      int pitch = pitchToKeyNumber(by[1]);
      mKeysPressedSet.remove(pitch);
      if (mKeysPressedSet.isEmpty())
        mChordSet.clear();
      // Update the current chord (which will still include the released key)
      var cc = constructChord();
      if (!cc.equals(mCurrentChord)) {
        mCurrentChord = cc;
        mCurrentChordTimestamp = mLastPressTimestamp;
      }
      
    }
  }

  private int pitchToKeyNumber(int pitch) {
    var mod = pitch + PITCH_TO_PIANO_KEY_NUMBER_OFFSET;
    checkArgument(mod >= 0 && mod < 88, "pitch is outside range of 88-key piano:", pitch);
    return mod;
  }

  @Override
  public void close() {
    log("closing");
  }

  public synchronized Chord currentChord() {
    boolean db = true;
    // Update the chord if there hasn't been recent key down action
    if (mLastPressTimestamp != mCurrentChordTimestamp) {
      var tm = System.currentTimeMillis();
      if (db)
        pr("...currentChord; ms since press:", tm - mLastPressTimestamp, "key num:", mChordSet);
      if (tm - mLastPressTimestamp >= mConfig.quiescentChordMs()) {
        updateCurrentChord();
        if (db)
          pr("...... set chord to:", mCurrentChord.keyNumbers(),"timestamp:",mCurrentChordTimestamp);
      }
    }
    return mCurrentChord;
  }

  private Chord constructChord() {
    List<Integer> x = arrayList();
    x.addAll(mChordSet);
    return Chord.newBuilder().keyNumbers(DataUtil.intArray(x)).build();
  }

  private void updateCurrentChord() {
    mCurrentChord = constructChord();
    mCurrentChordTimestamp = mLastPressTimestamp;
  }

  private SightConfig mConfig;
  private SortedSet<Integer> mKeysPressedSet = new TreeSet<>();
  private SortedSet<Integer> mChordSet = new TreeSet<>();
  private long mLastPressTimestamp;
  private Chord mCurrentChord = Chord.DEFAULT_INSTANCE;
  private long mCurrentChordTimestamp;
}