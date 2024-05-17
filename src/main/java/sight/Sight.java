package sight;

import static js.base.Tools.*;
import static sight.Util.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.util.Arrays;
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
import sight.gen.LessonState;
import sight.gen.LessonStatus;
import sight.gen.GuiState;
import sight.gen.Hand;
import sight.gen.SightConfig;

public class Sight extends App {

  public static void main(String[] args) {
    Sight app = new Sight();
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
        return "sight";
      }

      @Override
      public SightConfig defaultArgs() {
        return SightConfig.DEFAULT_INSTANCE;
      }

      @Override
      public void perform() {
        setConfig(config());
        auxPerform();
      }

      @Override
      protected String shortHelp() {
        return "";
      }
    });
  }

  private void auxPerform() {

    if (false && alert("play exp")) {
      playExp();
      return;
    }
    if (config().createChords()) {
      createChords();
      return;
    }

    SystemUtil.prepareForConsoleOrGUI(false);

    lessonManager().prepare();

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

    mCurrentTime = System.currentTimeMillis();

    var lessonState = lessonState();

    if (lessonState.status() == LessonStatus.NONE)
      prepareLesson();

    // Watch for changes to frame location
    {
      var b = new IRect(mFrame.frame().getBounds());
      var s = guiState();
      if (!b.equals(s.frameBounds())) {
        mGuiState = s.toBuilder().frameBounds(b).build();
        mGuiStateModTime = mCurrentTime;
      }
    }

    {
      var s = guiState();
      if (s.equals(mLastWrittenGuiState)) {
        mGuiStateModTime = mCurrentTime;
      } else {
        if (mCurrentTime - mGuiStateModTime > 1000) {
          Files.S.writePretty(guiStateFile(), s);
          mLastWrittenGuiState = s;
          mGuiStateModTime = mCurrentTime;
        }
      }
    }

    // Look for changes in the current chord
    {
      var ch = MidiManager.SHARED_INSTANCE.currentChord();
      if (ch != mPrevChord) {
        mPrevChord = ch;
        if (!ch.equals(Chord.DEFAULT_INSTANCE)) {
          quitIfDeathChord(ch);

          if (lessonState.status() == LessonStatus.ACTIVE)
            processPlayerChord(ch);
        }
      }
    }

    long elapsed = mCurrentTime - lessonState.timeMs();

    // Switch state if appropriate
    //
    switch (lessonState.status()) {
    default:
      break;

    case SHOWING_ERROR:
      if (elapsed > 2000) {
        var b = createWork();
        b.icons()[b.cursor()] = ICON_POINTER;
        b.status(LessonStatus.ACTIVE);
        writeWork();
        refreshView("changed icon back to pointer");
      }
      break;

    case DONE_SESSION: {
      if (elapsed >= config().donePauseTimeMs() * 3) {
        prepareLesson();
        refreshView("DONE_SESSION expired");
      }
    }
      break;

    case DONE: {
      if (elapsed >= config().donePauseTimeMs() / 3) {
        lessonManager().recordResult(lessonState.lessonId(), calcPercentRight(lessonState));
        var doneSession = lessonManager().advance();
        if (doneSession) {
          var acc = lessonManager().accuracyAtLessonStartAndEnd();
          var b = createWork();
          b.status(LessonStatus.DONE_SESSION);
          writeWork();
          var endAcc = Math.round(acc[1] * 100);
          var diff = Math.round((acc[1] - acc[0]) * 100);

          canvas().setMessage(new Color(0, 128, 0), "Done session! Accuracy:",
              diff >= 0 ? "+" + diff : "-" + diff, "=", endAcc + "%");
        } else {
          prepareLesson();
        }
        refreshView("DONE expired");
      }
    }
      break;
    case RETRY:
      if (elapsed >= config().donePauseTimeMs()) {
        lessonManager().recordResult(lessonState.lessonId(), calcPercentRight(lessonState));
        prepareLesson();
        refreshView("RETRY expired");
      }
      break;
    }

    mCurrentTime = 0;
  }

  private static int calcPercentRight(LessonState s) {
    checkArgument(s.cursor() != 0);
    int c = 0;
    for (int i = 0; i < s.cursor(); i++)
      if (s.icons()[i] == ICON_RIGHT)
        c++;
    return (c * 100) / s.cursor();
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

  private void refreshView(String reason) {
    i24("refreshView:", reason);
    canvas().repaint();
  }

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

    if (false && !ISSUE_24) {
      // WTF, apparently this is necessary to get repainting to occur; see
      // https://groups.google.com/g/comp.lang.java.gui/c/vCbwLOX9Vow?pli=1
      contentPane().revalidate();
    }
  }

  private Canvas canvas() {
    if (mCanvas == null)
      mCanvas = new Canvas();
    return mCanvas;
  }

  private List<String> lessonHistory = arrayList();

  /**
   * Get app frame's content pane
   */
  private JComponent contentPane() {
    return (JComponent) mFrame.frame().getContentPane();
  }

  // ------------------------------------------------------------------
  // Drill logic
  // ------------------------------------------------------------------

  private void prepareLesson() {
    var b = createWork();
    b.status(LessonStatus.ACTIVE);
    b.cursor(0);
    b.hadError(false);
    canvas().clearMessage();

    var key = lessonManager().choose();
    lessonHistory.add(key);
    b.lessonId(key);

    var notes = lessonManager().renderedNotes(key);
    var ic = new int[notes.renderedChords().size()];
    ic[0] = ICON_POINTER;
    b.icons(ic);

    writeWork();
  }

  private void processPlayerChord(Chord ch) {
    //alertVerbose();
    i24("processPlayerChord:", ch);
    var b = createWork();
    var notes = lessonManager().renderedNotes(b.lessonId());
    var exp = notes.renderedChords().get(b.cursor());
    var expChord = mergeChords(exp.chordA(), exp.chordB());
    log("chord:", ch);
    log("expct:", expChord);

    if (ch.equals(CHORD_CHEAT)) {
      ch = expChord;
    }
    boolean correct = expChord.equals(ch);
    if (!correct) {
      i24("Expected:", expChord, "Played:", ch);
      b.hadError(true);
      if (!config().silentCorrection())
        MidiManager.SHARED_INSTANCE.playCorrection(expChord, 600);
    }

    int newIcon = correct ? ICON_RIGHT : ICON_WRONG;
    b.icons()[b.cursor()] = newIcon;

    // If there was a mistake, we're going to make user repeat this chord

    if (correct) {
      b.cursor(b.cursor() + 1);
      if (b.cursor() != notes.renderedChords().size()) {
        b.icons()[b.cursor()] = ICON_POINTER;
      } else {
        if (!b.hadError())
          b.status(LessonStatus.DONE);
        else {
          b.status(LessonStatus.RETRY);
          canvas().setMessage(Color.RED, "Try Again");
        }
      }
    } else {
      b.status(LessonStatus.SHOWING_ERROR);
      b.timeMs(System.currentTimeMillis());
    }
    writeWork();
  }

  private FrameWrapper mFrame;
  private Canvas mCanvas;

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
        quitIfDeathChord(ch);

        if (ch.equals(CHORD_RESET_SCORE)) {
          score.clear();
        } else if (ch.equals(CHORD_REMOVE_LAST)) {
          if (!score.isEmpty())
            pop(score);
        } else
          score.add(ch);

        {
          var sb = new StringBuilder();
          int slot = INIT_INDEX;
          for (var c : score) {
            slot++;
            if (sb.length() != 0) {
              if (config().hand() == Hand.BOTH && slot % 2 == 1)
                sb.append(":");
              else
                sb.append("    ");
            }
            sb.append(encodeChord(c));
          }
          pr(sb.toString());
        }
      }
    }
  }

  private void playExp() {
    pr("experiment for sending midi to device");
    SystemUtil.prepareForConsoleOrGUI(true);
    var m = MidiManager.SHARED_INSTANCE;
    m.start(config());

    while (true) {
      sleepMs(50);

      // Look for changes in the current chord

      var ch = m.currentChord();
      if (ch != mPrevChord) {
        mPrevChord = ch;
        if (ch.equals(Chord.DEFAULT_INSTANCE))
          continue;
        quitIfDeathChord(ch);
        m.playCorrection(ch, 700);
      }
    }
  }

  // ------------------------------------------------------------------
  // Convenience methods for manipulating DrillState
  // ------------------------------------------------------------------

  /**
   * Construct a builder from a copy of the drill state
   */
  private LessonState.Builder createWork() {
    checkState(mCurrentTime != 0);
    var b = lessonState().build().toBuilder();
    mTempLessonState = b;
    var ic = b.icons();
    b.icons(Arrays.copyOf(ic, ic.length));
    return b;
  }

  /**
   * Replace drill state with the work version, and trigger a repaint
   */
  private void writeWork() {
    var b = mTempLessonState;
    if (b.status() != lessonState().status())
      b.timeMs(mCurrentTime);
    setLessonState(b);
    refreshView("replaced drill state");
    mTempLessonState = null;
  }

  private LessonState.Builder mTempLessonState;
  private long mCurrentTime;

}
