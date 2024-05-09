package sight;

import static js.base.Tools.*;

import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;

import js.base.BaseObject;

public class MidiExp extends BaseObject {

  public void run() {
    try {

      openDevicePreventsAppFromExiting();
    } catch (Throwable t) {
      throw asRuntimeException(t);
    }
  }

  private void openDevicePreventsAppFromExiting() throws Throwable {

    var infoArray = MidiSystem.getMidiDeviceInfo();
    var deviceInfo = infoArray[3];

    var device = MidiSystem.getMidiDevice(deviceInfo);

    pr("inputDevice:", device.getDeviceInfo());
    pr("isOpen:", device.isOpen());

    // If I try opening the device and closing it,
    // the app never exits.

    device.open();
    device.close();

  }

}
