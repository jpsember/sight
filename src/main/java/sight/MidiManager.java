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
import sight.gen.Chord;
import sight.gen.SightConfig;
import uk.co.xfactorylibrarians.coremidi4j.CoreMidiDeviceProvider;

public class MidiManager extends BaseObject {

  public static final MidiManager SHARED_INSTANCE = new MidiManager();

  public synchronized void start(SightConfig config) {
    if (mStarted)
      return;
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

      mOurReceiver = new MidiReceiver(config);

      findInputAndOutputDevices();
      mInputDevice.open();
      mOutputDevice.open();

      mTransmitter = mInputDevice.getTransmitter();

      // Bind the transmitter to the receiver so the receiver gets input from the transmitter
      mTransmitter.setReceiver(mOurReceiver);
      mInstrumentReceiver = mOutputDevice.getReceiver();

      mStarted = true;
    } catch (Throwable t) {
      throw asRuntimeException(t);
    }
  }

  public synchronized void stop() {
    if (!mStarted)
      return;
    // Close things down in the opposite order that they were opened
    close(mInstrumentReceiver, mTransmitter, mOutputDevice, mInputDevice, mOurReceiver);
  }

  public synchronized Chord currentChord() {
    if (!mStarted)
      return Chord.DEFAULT_INSTANCE;
    return mOurReceiver.currentChord();
  }

  private void findInputAndOutputDevices() throws MidiUnavailableException {
    List<MidiDevice> deviceCandidates = arrayList();

    var midiDevInfoList = MidiSystem.getMidiDeviceInfo();
    for (var devInfo : midiDevInfoList) {
      var name = devInfo.getName();
      var origName = name;
      pr("name:", name);
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

  public void play(Chord c) {

    final long MS_TO_MICROSEC = 1000;
    final int vel = 64;

    try {
      var r = mInstrumentReceiver;
      var ts = mOutputDevice.getMicrosecondPosition();
      checkState(ts > 0);

      for (int chordKey : c.keyNumbers()) {
        var k = chordKey - PITCH_TO_PIANO_KEY_NUMBER_OFFSET;
        var m1 = new ShortMessage(ShortMessage.NOTE_ON, 0, k, vel);
        var m2 = new ShortMessage(ShortMessage.NOTE_OFF, 0, k, 0);
        r.send(m1, ts + 3 * MS_TO_MICROSEC);
        r.send(m2, ts + 1000 * MS_TO_MICROSEC);
      }
    } catch (InvalidMidiDataException e) {
      throw asRuntimeException(e);
    }
  }

}
