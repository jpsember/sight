package sight;

import static js.base.Tools.*;
import static sight.Util.*;

import java.awt.BorderLayout;
import java.io.File;
import java.util.List;

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
import sight.gen.DrillStatus;
import sight.gen.GuiState;
import sight.gen.RenderedNotes;

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
    var ca = cmdLineArgs();
    if (ca.nextArgIf("create_chords")) {
      createChords();
      return;
    }

    SystemUtil.prepareForConsoleOrGUI(false);

    lessonManager();

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
      // SystemUtil.killAfterDelay(processExpr);
    }

    createFrame();
    var b = guiState().frameBounds();
    if (b.isValid()) {
      mFrame.frame().setBounds(b.toRectangle());
    }
    mFrame.frame().setVisible(true);

    mTaskManager = new BgndTaskManager();
    var m = MidiManager.SHARED_INSTANCE;
    m.start(config());
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
        if (!ch.equals(Chord.DEFAULT_INSTANCE)) {
          if (ch.equals(DEATH_CHORD)) {
            halt("DEATH CHORD pressed, quitting");
          }

          if (ch.equals(PREV_LESSON_CHORD)) {
            todo("this is very hacky.");
            if (mDrillState.cursor() == 0) {
              if (lessonHistory.size() > 1) {
                pop(lessonHistory);

                var b = DrillState.newBuilder();
                b.status(DrillStatus.ACTIVE);

                var r = last(lessonHistory);
                b.notes(r);

                var ic = new int[b.notes().renderedChords().size()];
                ic[0] = ICON_POINTER;
                b.icons(ic);

                mDrillState = b.build();
                canvas().setDrillState(mDrillState);
                canvas().repaint();
                return;
              }
            } else {
              var b = DrillState.newBuilder();
              b.status(DrillStatus.ACTIVE);
              b.notes(mDrillState.notes());
              var ic = new int[b.notes().renderedChords().size()];
              ic[0] = ICON_POINTER;
              b.icons(ic);
              mDrillState = b.build();
              canvas().setDrillState(mDrillState);
              canvas().repaint();
              return;
            }
          }

          processPlayerChord(ch);
        }
      }
    }

    // Switch state if appropriate
    //
    {
      if (mDrillState.status() == DrillStatus.DONE) {
        long elapsed = System.currentTimeMillis() - mDoneTime;
        if (elapsed >= config().donePauseTimeMs()) {
          prepareDrill();
          canvas().repaint();
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

  private long mDoneTime;

  //------------------------------------------------------------------
  // Frame
  // ------------------------------------------------------------------

  private void createFrame() {
    mFrame = new FrameWrapper();
    mFrame.frame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // We embed a JPanel that serves as a container for other components, 
    // the main one being the editor window, but others that may include
    // control panels or informational windows

    JPanel parentPanel = new JPanel(new BorderLayout());
    parentPanel.add(canvas());
    contentPane().add(parentPanel);
    // WTF, apparently this is necessary to get repainting to occur; see
    // https://groups.google.com/g/comp.lang.java.gui/c/vCbwLOX9Vow?pli=1
    contentPane().revalidate();
  }

  private Canvas canvas() {
    if (mCanvas == null)
      mCanvas = new Canvas();
    return mCanvas;
  }

  private List<RenderedNotes> lessonHistory = arrayList();

  private void prepareScore(DrillState.Builder b) {
    var r = lessonManager().choose();
    lessonHistory.add(r);
    b.notes(r);

    var ic = new int[b.notes().renderedChords().size()];
    ic[0] = ICON_POINTER;
    b.icons(ic);
  }

  /**
   * Get app frame's content pane
   */
  private JComponent contentPane() {
    return (JComponent) mFrame.frame().getContentPane();
  }

  private LessonManager lessonManager() {
    if (mLessonManager == null) {
      mLessonManager = new LessonManager();
      mLessonManager.prepare();
    }
    return mLessonManager;
  }
  // ------------------------------------------------------------------
  // Drill logic
  // ------------------------------------------------------------------

  private void prepareDrill() {
    var b = DrillState.newBuilder();
    b.status(DrillStatus.ACTIVE);
    prepareScore(b);
    mDrillState = b.build();
    canvas().setDrillState(mDrillState);
  }

  private void processPlayerChord(Chord ch) {
    var s = mDrillState;
    switch (s.status()) {
    case DONE:
      prepareDrill();
      canvas().repaint();
      break;
    case ACTIVE: {
      var exp = s.notes().renderedChords().get(s.cursor());
      var expChord = exp.chord();
      log("chord:", ch);
      log("expct:", expChord);
      int newIcon = (expChord.equals(ch)) ? ICON_RIGHT : ICON_WRONG;
      var b = s.toBuilder();
      b.icons()[s.cursor()] = newIcon;
      b.cursor(s.cursor() + 1);
      if (b.cursor() != s.notes().renderedChords().size()) {
        b.icons()[b.cursor()] = ICON_POINTER;
      } else {
        b.status(DrillStatus.DONE);
        mDoneTime = System.currentTimeMillis();
      }
      mDrillState = b.build();
      canvas().repaint();
    }
      break;
    default:
      badState("unexpected status:", mDrillState);
    }

  }

  private DrillState mDrillState = DrillState.DEFAULT_INSTANCE;
  private FrameWrapper mFrame;
  private Canvas mCanvas;
  private LessonManager mLessonManager;

  private void createChords() {
    pr("creating chords");
    SystemUtil.prepareForConsoleOrGUI(true);
    var m = MidiManager.SHARED_INSTANCE;
    m.start(config());

    List<Chord> score = arrayList();

    while (true) {
      sleepMs(50);
      // Look for changes in the current chord

      var ch = MidiManager.SHARED_INSTANCE.currentChord();
      if (ch != mPrevChord) {
        mPrevChord = ch;
        if (ch.equals(Chord.DEFAULT_INSTANCE))
          continue;
        if (ch.equals(DEATH_CHORD)) {
          halt("DEATH CHORD pressed, quitting");
        }

        if (ch.equals(CHORD_RESET_SCORE)) {
          score.clear();
        } else if (ch.equals(CHORD_REMOVE_LAST)) {
          if (!score.isEmpty())
            pop(score);
        } else
          score.add(ch);

        {
          var sb = new StringBuilder();
          for (var c : score) {
            if (sb.length() != 0)
              sb.append("    ");
            sb.append(encodeChord(c));
          }
          pr(sb.toString());
        }
      }
    }
  }

}
