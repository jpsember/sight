package sight;

import js.base.BaseObject;
import js.base.DateTimeTools;

import static js.base.Tools.*;
import static sight.Util.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import uk.co.xfactorylibrarians.coremidi4j.CoreMidiDeviceProvider;
import uk.co.xfactorylibrarians.coremidi4j.CoreMidiNotification;
import uk.co.xfactorylibrarians.coremidi4j.CoreMidiException;

public class MidiExp extends BaseObject {

  public static boolean isCoreMidiLoaded() throws CoreMidiException {
    return CoreMidiDeviceProvider.isLibraryLoaded();
  }

  public static void watchForMidiChanges() throws CoreMidiException {
    CoreMidiDeviceProvider.addNotificationListener(new CoreMidiNotification() {
      public void midiSystemUpdated() {
        System.out.println("The MIDI environment has changed.");
      }
    });
  }

  public void auxMain() throws Exception {
    System.out.println("Working MIDI Devices:");
    for (javax.sound.midi.MidiDevice.Info device : CoreMidiDeviceProvider.getMidiDeviceInfo()) {
      System.out.println("  " + device);
    }

    if (isCoreMidiLoaded()) {
      pr("CoreMIDI4J native library is running.");
    } else {
      pr("CoreMIDI4J native library is not available.");
    }

    watchForMidiChanges();
    pr("Watching for MIDI environment changes for several seconds...");
    sleepMs(5000);
    pr("...exiting");
  }

  public void run() {
    try {
      if (true) {
        auxMain();
        return;
      }
      if (false)
        playExp();
      else
        runAux0();
    } catch (Throwable t) {
      throw asRuntimeException(t);
    }
  }

  private void runAux0() throws MidiUnavailableException, InvalidMidiDataException, IOException {

    var midiDevInfo = MidiSystem.getMidiDeviceInfo();
    var x = midiDevInfo[3];

    var d = MidiSystem.getMidiDevice(x);

    var inputDevice = d;

    pr("inputDevice:", d.getDeviceInfo());
    pr("isOpen:", d.isOpen());

    //    // How do I access the 'stream' of midi data without storing it in a buffer?
    //    // https://stackoverflow.com/questions/18851866
    //
    //    Receiver receiver = new OurReceiver();
    //
    inputDevice.open();

    inputDevice.close();
    if (alert("halting early"))
      return;

    //
    //    // Get the transmitter class from your input device
    //    var transmitter = inputDevice.getTransmitter();
    //
    //    var sequencer = MidiSystem.getSequencer();
    //    //        // Open a connection to the default sequencer (as specified by MidiSystem)
    //    sequencer.open();
    //    // Get the receiver class from your sequencer
    //    receiver = sequencer.getReceiver();
    //    // Bind the transmitter to the receiver so the receiver gets input from the transmitter
    //    transmitter.setReceiver(receiver);
    //
    //    // Create a new sequence
    //    Sequence seq = new Sequence(Sequence.PPQ, 24);
    //    // And of course a track to record the input on
    //    Track currentTrack = seq.createTrack();
    //    // Do some sequencer settings
    //    sequencer.setSequence(seq);
    //    sequencer.setTickPosition(0);
    //    sequencer.recordEnable(currentTrack, -1);
    //    // And start recording
    //    sequencer.startRecording();
    //
    //    pr("now recording for 5s");
    //
    //    DateTimeTools.sleepForRealMs(5000);
    //
    //    // Stop recording
    //    if (sequencer.isRecording()) {
    //      pr("stopping recording");
    //      // Tell sequencer to stop recording
    //      sequencer.stopRecording();
    //
    //      // Retrieve the sequence containing the stuff you played on the MIDI instrument
    //      Sequence tmp = sequencer.getSequence();
    //
    //      if (true) {
    //        // Save to file
    //        var f = new File("jeff_experiment.mid");
    //        pr("saving to:", f);
    //        MidiSystem.write(tmp, 0, f);
    //        var fmt = MidiSystem.getMidiFileFormat(f);
    //        pr("MidiFileFormat:", fmt, fmt.properties());
    //      }
    //    }
    //
    //    pr("closing transmitter");
    //    autoClose(transmitter);
    //
    //    pr("closing input device");
    //
    //    autoClose(inputDevice);
    //    pr("exiting");
    //
    //    //    autoClose(transmitter, inputDevice);

  }

  private void runAux() throws MidiUnavailableException, InvalidMidiDataException, IOException {

    // adapted from https://stackoverflow.com/questions/69909883

    List<MidiDevice> deviceCandidates = arrayList();
    var midiDevInfo = MidiSystem.getMidiDeviceInfo();

    var inputDevice = findInputDevice();
    if (alert("halting early"))
      return;
    // How do I access the 'stream' of midi data without storing it in a buffer?
    // https://stackoverflow.com/questions/18851866

    Receiver receiver = new OurReceiver();

    inputDevice.open();

    // Get the transmitter class from your input device
    var transmitter = inputDevice.getTransmitter();

    var sequencer = MidiSystem.getSequencer();
    //        // Open a connection to the default sequencer (as specified by MidiSystem)
    sequencer.open();
    // Get the receiver class from your sequencer
    receiver = sequencer.getReceiver();
    // Bind the transmitter to the receiver so the receiver gets input from the transmitter
    transmitter.setReceiver(receiver);

    // Create a new sequence
    Sequence seq = new Sequence(Sequence.PPQ, 24);
    // And of course a track to record the input on
    Track currentTrack = seq.createTrack();
    // Do some sequencer settings
    sequencer.setSequence(seq);
    sequencer.setTickPosition(0);
    sequencer.recordEnable(currentTrack, -1);
    // And start recording
    sequencer.startRecording();

    pr("now recording for 5s");

    DateTimeTools.sleepForRealMs(5000);

    // Stop recording
    if (sequencer.isRecording()) {
      pr("stopping recording");
      // Tell sequencer to stop recording
      sequencer.stopRecording();

      // Retrieve the sequence containing the stuff you played on the MIDI instrument
      Sequence tmp = sequencer.getSequence();

      if (true) {
        // Save to file
        var f = new File("jeff_experiment.mid");
        pr("saving to:", f);
        MidiSystem.write(tmp, 0, f);
        var fmt = MidiSystem.getMidiFileFormat(f);
        pr("MidiFileFormat:", fmt, fmt.properties());
      }
    }

    pr("closing transmitter");
    autoClose(transmitter);

    pr("closing input device");

    autoClose(inputDevice);
    pr("exiting");

    //    autoClose(transmitter, inputDevice);

  }

  private void playExp() throws InvalidMidiDataException, MidiUnavailableException {
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
      sleepMs(1000);
    }

  }

  private MidiDevice findInputDevice() throws MidiUnavailableException {
    List<MidiDevice> deviceCandidates = arrayList();

    var midiDevInfo = MidiSystem.getMidiDeviceInfo();
    int i = INIT_INDEX;
    for (var x : midiDevInfo) {
      i++;
      pr("#", i, "name:", quote(x.getName()), "desc:", quote(x.getDescription()));
      if (x.getName().equals("CASIO USB-MIDI")) {
        var d = MidiSystem.getMidiDevice(x);
        deviceCandidates.add(d);
        pr("...found input device candidate");
      }
    }

    // Determine which of the candidates is an actual input device
    MidiDevice inputDevice = null;

    int v = INIT_INDEX;
    for (var c : deviceCandidates) {
      v++;
      if (v != 1)
        continue;

      pr("counter:", v, "...trying to get transmitter for:", c);
      try {
        c.open();
      } catch (Throwable t) {
        pr(".......failed to open device:", c.getDeviceInfo().getName());
        continue;
      }

      try {
        var tr = c.getTransmitter();
        tr.close();
        inputDevice = c;

      } catch (MidiUnavailableException e) {
        pr("couldn't get transmitter");
      }
      c.close();
      if (inputDevice != null)
        return inputDevice;
    }

    throw badState("Can't find MidiDevice that can transmit");
  }

}
