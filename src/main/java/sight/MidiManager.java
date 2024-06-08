package sight;

import static js.base.Tools.*;
import static sight.Util.*;

import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

import js.base.BaseObject;
import js.base.DateTimeTools;
import sight.gen.Chord;
import uk.co.xfactorylibrarians.coremidi4j.CoreMidiDeviceProvider;

public class MidiManager extends BaseObject {

  public static final MidiManager SHARED_INSTANCE = new MidiManager();

  public synchronized void start() {
    if (ISSUE_28)
      setVerbose();
    if (mStarted)
      return;
    mStarted = true;

    attemptConnectToMidiDevice();
  }

  private long mLastConnectAttemptTime;

  private void attemptConnectToMidiDevice() {
    if (mMidiConnected)
      return;

    var currentTime = System.currentTimeMillis();
    if (currentTime - mLastConnectAttemptTime < DateTimeTools.SECONDS(2))
      return;
    mLastConnectAttemptTime = currentTime;

    log("attemptConnectToMidiDevice");

    try {
      if (verbose()) {
        log("Working MIDI Devices:");
        for (var device : CoreMidiDeviceProvider.getMidiDeviceInfo()) {
          log(device);
        }
      }

      if (CoreMidiDeviceProvider.isLibraryLoaded()) {
        log("CoreMIDI4J native library is running.");
      } else {
        log("CoreMIDI4J native library is not available.");
      }

      mOurReceiver = new MidiReceiver(config());

      findInputAndOutputDevices();
      mInputDevice.open();
      mOutputDevice.open();

      mTransmitter = mInputDevice.getTransmitter();

      // Bind the transmitter to the receiver so the receiver gets input from the transmitter
      mTransmitter.setReceiver(mOurReceiver);
      mInstrumentReceiver = mOutputDevice.getReceiver();
      mMidiConnected = true;
      log("...connected");
    } catch (Throwable t) {
      todo("display message about no midi found");
      disconnectMidi();
      //throw asRuntimeException(t);
    }
  }

  private void disconnectMidi() {
    log("disconnectMidi");
    // Close things down in the opposite order that they were opened
    close(mInstrumentReceiver, mTransmitter, mOutputDevice, mInputDevice, mOurReceiver);
    mInstrumentReceiver = null;
    mTransmitter = null;
    mOutputDevice = null;
    mInputDevice = null;
    mOurReceiver = null;
    mMidiConnected = false;
  }

  public synchronized void stop() {
    if (!mStarted)
      return;
    disconnectMidi();
  }

  public synchronized Chord currentChord() {
    var out = Chord.DEFAULT_INSTANCE;
    if (mStarted) {
      if (mMidiConnected) {
        try {
          out = mOurReceiver.currentChord();
        } catch (Throwable t) {
          pr("*** failed to get current chord:", t);
          disconnectMidi();
        }
      } else {
        attemptConnectToMidiDevice();
      }
    }
    return out;
  }

  public boolean midiAvailable() {
    return mMidiConnected;
  }

  private void findInputAndOutputDevices() throws MidiUnavailableException {
    List<MidiDevice> deviceCandidates = arrayList();

    var midiDevInfoList = MidiSystem.getMidiDeviceInfo();
    for (var devInfo : midiDevInfoList) {
      var name = devInfo.getName();
      var origName = name;
      name = chompPrefix(name, "CoreMIDI4J - ");
      if (name == origName)
        continue;
      if (!name.equals("CASIO USB-MIDI"))
        continue;

      var d = MidiSystem.getMidiDevice(devInfo);
      deviceCandidates.add(d);
    }

    // Determine which of the candidates are actual input or output devices

    for (var device : deviceCandidates) {
      try {
        device.open();
      } catch (Throwable t) {
        pr(".......failed to open device:", device.getDeviceInfo().getName());
        continue;
      }

      // Determine if device can transmit.  If not, it is not a valid input device
      try {
        var transmitter = device.getTransmitter();
        transmitter.close();
        mInputDevice = device;
      } catch (MidiUnavailableException e) {
        log("couldn't get transmitter");
      }

      // Determine if device can receive.  If not, it's not an output device
      try {
        var r = device.getReceiver();
        r.close();
        mOutputDevice = device;
      } catch (MidiUnavailableException e) {
        log("couldn't get receiver");
      }

      device.close();
    }

    checkNotNull(mInputDevice, "Can't find MidiDevice for input");
    checkNotNull(mOutputDevice, "Can't find MidiDevice for output");
  }

  private MidiManager() {
  }

  private boolean mStarted;
  private MidiReceiver mOurReceiver;
  private MidiDevice mInputDevice;
  private MidiDevice mOutputDevice;
  private Transmitter mTransmitter;
  private Receiver mInstrumentReceiver;

  public long playCorrection(Chord c, long delayMs) {
    long togetherDur = 1000;
    long brokenDur = 220;
    delayMs = playTogether(c, delayMs, togetherDur);
    delayMs = playBroken(c, delayMs, brokenDur);
    delayMs += togetherDur / 3;
    delayMs = playTogether(c, delayMs, togetherDur);
    return delayMs;
  }

  public long playTogether(Chord c, long delayMs, long chordDuration) {
    try {
      var ts = mOutputDevice.getMicrosecondPosition();
      checkState(ts > 0);

      final long postChordPause = chordDuration / 8;

      for (int chordKey : c.keyNumbers()) {
        sendKeyToDevice(chordKey, ts, delayMs, chordDuration - postChordPause);
      }
      return delayMs + chordDuration;
    } catch (InvalidMidiDataException e) {
      throw asRuntimeException(e);
    }
  }

  public long playTogether(Chord c, long delayMs) {
    return playTogether(c, delayMs, 1500);
  }

  public long playBroken(Chord c, long delayMs, long durationMs) {

    try {
      var ts = mOutputDevice.getMicrosecondPosition();
      checkState(ts > 0);

      final long postBrokenPause = (long) ((durationMs * 3.5) / 2);

      int index = INIT_INDEX;
      for (int chordKey : c.keyNumbers()) {
        index++;
        long delay = delayMs + index * durationMs;
        sendKeyToDevice(chordKey, ts, delay, durationMs - postBrokenPause);
      }

      return delayMs + durationMs * c.keyNumbers().length;
    } catch (InvalidMidiDataException e) {
      throw asRuntimeException(e);
    }
  }

  public long playBroken(Chord c, long delayMs) {
    return playBroken(c, delayMs, 350);
  }

  private void sendKeyToDevice(int chordKey, long deviceTimestamp, long delayMs, long durationMs)
      throws InvalidMidiDataException {
    final long MS_TO_MICROSEC = 1000;
    var r = mInstrumentReceiver;
    var midiKey = chordKey - PITCH_TO_PIANO_KEY_NUMBER_OFFSET;
    final int vel = 64;
    var m1 = new ShortMessage(ShortMessage.NOTE_ON, 0, midiKey, vel);
    var m2 = new ShortMessage(ShortMessage.NOTE_OFF, 0, midiKey, 0);
    r.send(m1, deviceTimestamp + delayMs * MS_TO_MICROSEC);
    r.send(m2, deviceTimestamp + (delayMs + durationMs) * MS_TO_MICROSEC);
  }

  private boolean mMidiConnected;
}
