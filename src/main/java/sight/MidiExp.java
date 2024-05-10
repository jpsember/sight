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

  public void run() {
    try {

      if (false)
        playExp();
      else
        runAux0();
    } catch (Throwable t) {
      throw asRuntimeException(t);
    }
  }

  private void runAux0()
      throws MidiUnavailableException, InvalidMidiDataException, IOException, CoreMidiException {

    pr("Working MIDI Devices:");
    for (var device : CoreMidiDeviceProvider.getMidiDeviceInfo()) {
      pr(INDENT, device);
    }

    if (isCoreMidiLoaded()) {
      pr("CoreMIDI4J native library is running.");
    } else {
      pr("CoreMIDI4J native library is not available.");
    }

    //      watchForMidiChanges();
    //      pr("Watching for MIDI environment changes for several seconds...");
    //      sleepMs(5000);
    //      pr("...exiting");
    //

    var device = findInputDevice();
    pr("input device:", device);

    Receiver receiver = new OurReceiver();
    mCloseList.add(receiver);

    device.open();

    mCloseList.add(device);

    var transmitter = device.getTransmitter();
    mCloseList.add(transmitter);

//    //
//    var sequencer = MidiSystem.getSequencer();
//    //    //        // Open a connection to the default sequencer (as specified by MidiSystem)
//    sequencer.open();
//    mCloseList.add(sequencer);
//
//    //    // Get the receiver class from your sequencer
//    receiver = sequencer.getReceiver();
//    mCloseList.add(receiver);

    //    // Bind the transmitter to the receiver so the receiver gets input from the transmitter
    transmitter.setReceiver(receiver);
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
    pr("now recording for 5s");
    //
    sleepMs(5000);

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

    {
      var cl = mCloseList;
      while (!cl.isEmpty()) {
        var x = pop(cl);
        autoClose(x);
      }
    }

    pr("exiting");
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

  private List<AutoCloseable> mCloseList = arrayList();

  private MidiDevice findInputDevice() throws MidiUnavailableException {
    List<MidiDevice> deviceCandidates = arrayList();

    var midiDevInfo = MidiSystem.getMidiDeviceInfo();
    int i = INIT_INDEX;
    for (var x : midiDevInfo) {
      i++;

      var nm = x.getName();
      var nm2 = chompPrefix(nm, "CoreMIDI4J - ");
      if (nm == nm2)
        continue;
      if (!nm2.equals("CASIO USB-MIDI"))
        continue;

      var d = MidiSystem.getMidiDevice(x);
      deviceCandidates.add(d);
      pr("...found input device candidate");
    }

    // Determine which of the candidates is an actual input device
    MidiDevice inputDevice = null;

    int v = INIT_INDEX;
    for (var c : deviceCandidates) {
      v++;
      //      if (v != 1)
      //        continue;

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
