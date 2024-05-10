package sight;

import static js.base.Tools.*;
import static sight.Util.*;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.ShortMessage;

import js.base.BaseObject;
import sight.gen.Chord;
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
    var m = MidiManager.SHARED_INSTANCE;
    m.start( );

    var prevChord = Chord.DEFAULT_INSTANCE;
    while (true) {
      sleepMs(50);
      var ch = m.currentChord();
      if (ch != prevChord) {
        prevChord = ch;
        if (ch.keyNumbers().length != 0)
          pr("chord:", ch);
        if (ch.equals(DEATH_CHORD)) {
          pr("DEATH CHORD PRESSED!");
          break;
        }
      }
    }

  }

  private static final Chord DEATH_CHORD = Chord.newBuilder().keyNumbers(intArray(36)).build();

  /* private */ void playExp() throws InvalidMidiDataException, MidiUnavailableException {
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

}
