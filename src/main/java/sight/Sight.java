package sight;

import static js.base.Tools.*;

import java.awt.BorderLayout;
import java.io.File;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.sound.midi.Transmitter;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import js.app.App;
import js.app.AppOper;
import js.base.DateTimeTools;
import js.system.SystemUtil;
import sight.gen.Hand;
import sight.gen.KeySig;
import sight.gen.RenderedSet;

public class Sight extends App {

  public static void main(String[] args) {
    loadTools();
    Sight app = new Sight();
    //app.setCustomArgs("-h");
    app.startApplication(args);
    //app.exitWithReturnCode();
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  /**
   * Register the single operation for this application
   */
  @Override
  protected final void registerOperations() {
    registerOper(new AppOper() {
      @Override
      public String userCommand() {
        return "_sight_oper_";
      }

      @Override
      public void perform() {
        auxPerform();
      }

      @Override
      protected String shortHelp() {
        return "";
      }
    });
  }

  private void auxPerform() {
    SystemUtil.prepareForConsoleOrGUI(false);

    // Continue starting app within the Swing thread
    //
    SwingUtilities.invokeLater(() -> {
      continueStartupWithinSwingThread();
    });
  }

  /**
   * Continue startup of app from within Swing thread
   */
  private void continueStartupWithinSwingThread() {
    if (alert("!killing any other instances")) {
      String processExpr = getClass().getName();
      SystemUtil.killProcesses(processExpr);
      SystemUtil.killAfterDelay(processExpr);
    }
    if (true) {
      midiExpPlay();
      return;
    }

    createFrame();

    mFrame.frame().setVisible(true);

  }

  private void midiExpPlay() {

    // adapted from https://stackoverflow.com/questions/69909883

    try {

      {
        MidiDevice inputDevice = null;

        int index = INIT_INDEX;
        var infos = MidiSystem.getMidiDeviceInfo();
        for (var x : infos) {
          index++;
          pr("#", index);
          pr("...name:", quote(x.getName()), "desc:", quote(x.getDescription()));
          if (x.getName().equals("CASIO USB-MIDI")) {
            inputDevice = MidiSystem.getMidiDevice(x);
            pr("......set as input device");
            break;
          }
        }

        checkState(inputDevice != null, "can't find Casio");

        var sequencer = MidiSystem.getSequencer();
        Transmitter transmitter;
        Receiver receiver;

        // Open a connection to your input device
        inputDevice.open();
        // Open a connection to the default sequencer (as specified by MidiSystem)
        sequencer.open();
        // Get the transmitter class from your input device
        transmitter = inputDevice.getTransmitter();
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

          if (false) {
            // Save to file
            var f = new File("jeff_experiment.mid");
            pr("saving to:", f);
            MidiSystem.write(tmp, 0, f);
            var fmt = MidiSystem.getMidiFileFormat(f);
            pr("MidiFileFormat:", fmt, fmt.properties());
          }
        }
      }

      if (true)
        return;

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

  //------------------------------------------------------------------
  // Frame
  // ------------------------------------------------------------------

  public final FrameWrapper appFrame() {
    return mFrame;
  }

  private void createFrame() {
    mFrame = new FrameWrapper();
    mFrame.frame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    rebuildFrameContent();
  }

  public final void rebuildFrameContent() {

    // We embed a JPanel that serves as a container for other components, 
    // the main one being the editor window, but others that may include
    // control panels or informational windows

    JPanel parentPanel = new JPanel(new BorderLayout());
    populateFrame(parentPanel);
    contentPane().add(parentPanel);
    // WTF, apparently this is necessary to get repainting to occur; see
    // https://groups.google.com/g/comp.lang.java.gui/c/vCbwLOX9Vow?pli=1
    contentPane().revalidate();
  }

  /**
   * Add appropriate components to the app frame's parent panel. Default does
   * nothing
   */
  public void populateFrame(JPanel parentPanel) {
    mCanvas = new Canvas();
    //    var b = new JButton("hello");
    parentPanel.add(mCanvas);

    var rs = RenderedSet.newBuilder();
    rs.keySig(KeySig.E);
    rs.hand(Hand.RIGHT);
    rs.notes("<gis b dis>4 <gis' b dis gis> <fis, a cis e> <fis a c dis> <c' e g> <d f a> <e g b> <e ges b>");

    var r = rs.build();
    var cl = new ChordLibrary();
    //cl.ignoreCache();

    //cl.alertVerbose();
    var rn = cl.get(r);
    var sc = mCanvas;
    sc.setNotes(rn);
    sc.setSourceImage(rn.imageFile());
  }

  /**
   * Get app frame's content pane
   */
  public final JComponent contentPane() {
    return (JComponent) mFrame.frame().getContentPane();
  }

  private FrameWrapper mFrame;

  /**
   * Trigger a repaint of various app components
   */
  public final void performRepaint() {
    // repaintPanels(repaintFlags);
  }

  private Canvas mCanvas;
}
