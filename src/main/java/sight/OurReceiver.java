package sight;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;

import js.base.BaseObject;

class OurReceiver extends BaseObject implements Receiver {

  public OurReceiver() {
    setName("OurReceiver");
    setVerbose(true);
    log("constructed");
  }

  @Override
  public void send(MidiMessage message, long timeStamp) {
    log("Receiver send, timestamp:", timeStamp, "message:", message);
  }

  @Override
  public void close() {
    log("closing");
  }
}