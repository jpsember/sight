package sight;

import static js.base.Tools.*;
import static sight.Util.*;

import java.util.List;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Transmitter;

import js.base.BaseObject;
import js.data.DataUtil;
import uk.co.xfactorylibrarians.coremidi4j.CoreMidiDeviceProvider;

public class MidiManager extends BaseObject {

  public static final MidiManager SHARED_INSTANCE = new MidiManager();

  public synchronized void start() {
    if (mStarted)
      return;
    try {
      pr("Working MIDI Devices:");
      for (var device : CoreMidiDeviceProvider.getMidiDeviceInfo()) {
        pr(device);
      }

      if (CoreMidiDeviceProvider.isLibraryLoaded()) {
        pr("CoreMIDI4J native library is running.");
      } else {
        pr("CoreMIDI4J native library is not available.");
      }

      mReceiver = new OurReceiver();

      mDevice = findInputDevice();
      mDevice.open();

      mTransmitter = mDevice.getTransmitter();

      // Bind the transmitter to the receiver so the receiver gets input from the transmitter
      mTransmitter.setReceiver(mReceiver);

      mStarted = true;
    } catch (Throwable t) {
      throw asRuntimeException(t);
    }
  }

  public synchronized void stop() {
    if (!mStarted)
      return;
    // Close things down in the opposite order that they were opened
    close(mTransmitter, mDevice, mReceiver);
  }

  public synchronized List<Integer> currentChord() {
    if (!mStarted)
      return DataUtil.emptyList();
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
  private OurReceiver mReceiver;
  private MidiDevice mDevice;
  private Transmitter mTransmitter;

}
