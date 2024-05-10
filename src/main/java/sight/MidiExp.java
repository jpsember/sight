package sight;

import static js.base.Tools.*;
import static sight.Util.*;

import java.io.IOException;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import js.base.BaseObject;
import uk.co.xfactorylibrarians.coremidi4j.CoreMidiDeviceProvider;
import uk.co.xfactorylibrarians.coremidi4j.CoreMidiException;
import uk.co.xfactorylibrarians.coremidi4j.CoreMidiNotification;

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
      pr(device);
    }

    if (isCoreMidiLoaded()) {
      pr("CoreMIDI4J native library is running.");
    } else {
      pr("CoreMIDI4J native library is not available.");
    }

    var device = findInputDevice();

    Receiver receiver = new OurReceiver();
    mCloseList.add(receiver);

    device.open();

    mCloseList.add(device);

    var transmitter = device.getTransmitter();
    mCloseList.add(transmitter);

    // Bind the transmitter to the receiver so the receiver gets input from the transmitter
    transmitter.setReceiver(receiver);

    pr("now recording for 5s");

    pr("current thread:", Thread.currentThread());
    //
    for (int i = 0; i < 25; i++) {
      sleepMs(1000);
      var t = Thread.currentThread();

      pr("current thread:", t, "id:", t.getId(), "name:", t.getName());
    }

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

}
