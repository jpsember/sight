package sight;

import static js.base.Tools.*;
import static sight.Util.*;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import js.app.App;
import js.app.AppOper;
import js.file.Files;
import js.geometry.IRect;
import js.system.SystemUtil;
import sight.gen.Chord;
import sight.gen.DrillState;
import sight.gen.GuiState;
import sight.gen.Hand;
import sight.gen.KeySig;
import sight.gen.RenderedSet;

public class Sight extends App {

  public static void main(String[] args) {
    Sight app = new Sight();
    //app.setCustomArgs("-h");
    app.startApplication(args);
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

    prepareDrill();
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

    createFrame();
    var b = guiState().frameBounds();
    if (b.isValid()) {
      mFrame.frame().setBounds(b.toRectangle());
    }
    mFrame.frame().setVisible(true);

    mTaskManager = new BgndTaskManager();
    var m = MidiManager.SHARED_INSTANCE;
    m.start();
    mTaskManager.addTask(() -> swingBgndTask());
    mTaskManager.start();
  }

  private void swingBgndTask() {

    var currentTime = System.currentTimeMillis();

    // Watch for changes to frame location
    {
      var b = new IRect(mFrame.frame().getBounds());
      var s = guiState();
      if (!b.equals(s.frameBounds())) {
        mGuiState = s.toBuilder().frameBounds(b).build();
        mGuiStateModTime = currentTime;
      }
    }

    {
      var s = guiState();
      if (s.equals(mLastWrittenGuiState)) {
        mGuiStateModTime = currentTime;
      } else {
        if (currentTime - mGuiStateModTime > 1000) {
          Files.S.writePretty(guiStateFile(), s);
          mLastWrittenGuiState = s;
          mGuiStateModTime = currentTime;
        }
      }
    }

    // Look for changes in the current chord
    {
      var ch = MidiManager.SHARED_INSTANCE.currentChord();
      if (ch != mPrevChord) {
        mPrevChord = ch;
        pr("got a new chord:", INDENT, ch);
        if (ch.equals(DEATH_CHORD)) {
          halt("DEATH CHORD pressed, quitting");
        }
      }
    }
  }

  private BgndTaskManager mTaskManager;

  private GuiState mGuiState;
  private GuiState mLastWrittenGuiState = GuiState.DEFAULT_INSTANCE;
  private long mGuiStateModTime;

  private GuiState guiState() {
    if (mGuiState == null) {
      var f = guiStateFile();
      mGuiState = Files.parseAbstractDataOpt(GuiState.DEFAULT_INSTANCE, f);
    }
    return mGuiState;
  }

  private File guiStateFile() {
    return new File(".gui_state.json");
  }

  private Chord mPrevChord = Chord.DEFAULT_INSTANCE;

  private static final Chord DEATH_CHORD = Chord.newBuilder().keyNumbers(intArray(36)).build();

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
  private void populateFrame(JPanel parentPanel) {

    parentPanel.add(canvas());
  }

  private Canvas canvas() {
    if (mCanvas == null)
      mCanvas = new Canvas();
    return mCanvas;
  }

  private void prepareScore(DrillState.Builder b) {
    var rs = RenderedSet.newBuilder();
    rs.keySig(KeySig.E);
    rs.hand(Hand.RIGHT);
    rs.notes("<gis b dis>4 <gis' b dis gis> <fis, a cis e> <fis a c dis> <c' e g> <d f a> <e g b> <e ges b>");

    var r = rs.build();
    b.notes(chordLibrary().get(r));

    var ic = new int[b.notes().renderedChords().size()];
    ic[0] = ICON_POINTER;
    b.icons(ic);

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
  @Deprecated // not used?
  public final void performRepaint() {
    // repaintPanels(repaintFlags);
  }

  private Canvas mCanvas;

  private ChordLibrary chordLibrary() {
    if (mChordLibrary == null) {
      var c = new ChordLibrary();
      //c.ignoreCache();
      //c.alertVerbose();
      mChordLibrary = c;
    }
    return mChordLibrary;
  }

  private ChordLibrary mChordLibrary;

  // ------------------------------------------------------------------
  // Drill logic
  // ------------------------------------------------------------------

  private void prepareDrill() {
    var b = DrillState.newBuilder();
    prepareScore(b);
    mDrillState = b.build();
    canvas().setDrillState(mDrillState);
  }

  private DrillState mDrillState = DrillState.DEFAULT_INSTANCE;

}
