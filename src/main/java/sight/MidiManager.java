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

      mReceiver = new MidiReceiver(config);

      mInputDevice = findInputDevice();
      mInputDevice.open();

      mTransmitter = mInputDevice.getTransmitter();

      // Bind the transmitter to the receiver so the receiver gets input from the transmitter
      mTransmitter.setReceiver(mReceiver);
      mInstrumentReceiver = mInputDevice.getReceiver();

      mStarted = true;
    } catch (Throwable t) {
      throw asRuntimeException(t);
    }
  }

  public synchronized void stop() {
    if (!mStarted)
      return;
    // Close things down in the opposite order that they were opened
    close(mInstrumentReceiver, mTransmitter, mInputDevice, mReceiver);
  }

  public synchronized Chord currentChord() {
    if (!mStarted)
      return Chord.DEFAULT_INSTANCE;
    return mReceiver.currentChord();
  }

  private MidiDevice findInputDevice() throws MidiUnavailableException {
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

    // Determine which of the candidates is an actual input device
    MidiDevice inputDevice = null;

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
        inputDevice = device;
      } catch (MidiUnavailableException e) {
        log("couldn't get transmitter");
      }
      device.close();
      if (inputDevice != null)
        return inputDevice;
    }

    throw badState("Can't find MidiDevice that can transmit");
  }

  private MidiManager() {
  }

  private boolean mStarted;
  private MidiReceiver mReceiver;
  private MidiDevice mInputDevice;
  private Transmitter mTransmitter;
  private Receiver mInstrumentReceiver;

  public void play(Chord c) {
    
    final long MS_TO_MICROSEC = 1000;
    
    try {
      var r = mInstrumentReceiver;
      for (int k : c.keyNumbers()) {
        var m1 = new ShortMessage(ShortMessage.NOTE_ON, 0, k, 127);
        var m2 = new ShortMessage(ShortMessage.NOTE_OFF, 0,k,127);
        r.send(m1, 0);
        r.send(m2, 500 * MS_TO_MICROSEC);
      }
    } catch (InvalidMidiDataException e) {
      throw asRuntimeException(e);
    }
  }

  private void midiExpPlay() {

    // adapted from https://stackoverflow.com/questions/69909883

    try {
      var receiver = MidiSystem.getReceiver();

      int[] notes = { 60, 64, 67, 60, 65, 67, 55, 59, 62, 55, 60, 62, 53, 57, 60, 53, 58, 60 };
      int[] times = { 0, 0, 0, 1000, 1000, 1000, 2000, 2000, 2000, 3000, 3000, 3000, 4000, 4000, 4000, 5000,
          5000, 5000 };

      for (int i = 0; i < notes.length; i++) {

        int note = notes[i];
        int time = times[i];
        pr(note, ":", time);
        receiver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, note, 127), time * 1000);
        receiver.send(new ShortMessage(ShortMessage.NOTE_OFF, 0, note, 127), (time + 1000) * 1000);
        Thread.sleep(1000);
      }

      Thread.sleep(7000);
    } catch (Throwable t) {
      halt("caught:", INDENT, t);
    }
  }

}
