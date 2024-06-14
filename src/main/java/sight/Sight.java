package sight;

import static js.base.Tools.*;
import static sight.Util.*;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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

public class Sight extends App implements KeyListener {

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

  private boolean editMode() {
    return config().createChords();
  }

  private void auxPerform() {

    //    if (false && editMode()) {
    //      createChords();
    //      return;
    //    }

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
    m.start();

    if (!editMode())
      lessonManager().init();

    mTaskManager.addTask(() -> swingBgndTask());
    mTaskManager.start();
  }

  /**
   * Application main loop, executes within Swing thread every ~15 ms (~ 60 fps)
   */
  private void swingBgndTask() {
    mCurrentFrameTime = System.currentTimeMillis();

    updateGUIState();

    var lessonState = lessonState();
    if (lessonState.status() == LessonStatus.NONE) {
      if (editMode()) {
        var b = createWork();
        b.status(LessonStatus.EDIT);
        writeWork();
      } else
        prepareLesson(null);
    }

    // Look for changes in the current chord
    {
      var ch = MidiManager.SHARED_INSTANCE.currentChord();
      if (ch != mPrevChord) {
        mPrevChord = ch;
        if (!ch.equals(Chord.DEFAULT_INSTANCE)) {
          quitIfDeathChord(ch);
          switch (lessonState.status()) {
          default:
            break;
          case ACTIVE:
            processPlayerChord(ch);
            break;
          case EDIT:
            processEditChord(ch);
            break;
          }
        }
      }
    }

    processLessonState();

    updateInfoMessage();

    refreshIfMessagesChanged();
    mCurrentFrameTime = 0;
  }

  private void processLessonState() {
    var lessonState = lessonState();
    long elapsed = currentTime() - lessonState.timeMs();

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
      if (elapsed >= config().doneSessionDurationMs()) {
        prepareLesson(null);
        refreshView("DONE_SESSION expired");
      }
    }
      break;

    case DONE: {
      if (elapsed >= config().doneLessonDurationMs()) {
        lessonManager().recordResult(lessonState);
        var doneSession = lessonManager().advance();
        if (doneSession) {
          var acc = lessonManager().accuracyAtLessonStartAndEnd();
          var b = createWork();
          b.status(LessonStatus.DONE_SESSION);
          writeWork();
          var endAcc = Math.round(acc[1] * 100);
          var diff = Math.round((acc[1] - acc[0]) * 100);
          Msg.set(MSG_MAIN, "$008000", "Done session! Accuracy:", diff >= 0 ? "+" + diff : "-" + diff, "=",
              endAcc + "%");
        } else {
          prepareLesson(null);
        }
        refreshView("DONE expired");
      }
    }
      break;

    case RETRY:
      if (elapsed >= config().retryLessonDurationMs()) {
        lessonManager().recordResult(lessonState);
        prepareLesson(lessonState.lessonId());
        refreshView("RETRY expired");
      }
      break;
    }

    while (!mKeyEventQueue.isEmpty()) {
      var x = mKeyEventQueue.remove();
      pr("proc key:",x);
      if (x.getModifiersEx() == 0 && x.getKeyChar() == 'q') {
        pr("...'q' pressed, quitting");
        System.exit(0);
      }
      pr("...unhandled key press:", x);
    }
  }

  /**
   * If messages have changed since the last frame, refresh the canvas
   */
  private void refreshIfMessagesChanged() {
    var newSig = Msg.getChangeCounter();
    if (newSig != mMessagesSignature) {
      log("newSig:", newSig, "old:", mMessagesSignature);
      mMessagesSignature = newSig;
      refreshView("message(s) changed");
    }
  }

  private int mMessagesSignature;

  private void updateInfoMessage() {
    String msg = null;
    if (!MidiManager.SHARED_INSTANCE.midiAvailable())
      msg = "No MIDI device found";
    if (msg != null)
      Msg.set(MSG_INFO, "$ff0000", msg);
    else
      Msg.remove(MSG_INFO);
  }

  /**
   * Record changes to frame rectangle (and any other GUI elements to be
   * persisted), and periodically save changes to filesystem
   */
  private void updateGUIState() {
    {
      var b = new IRect(mFrame.frame().getBounds());
      var s = guiState();
      if (!b.equals(s.frameBounds())) {
        mGuiState = s.toBuilder().frameBounds(b).build();
        mGuiStateModTime = currentTime();
      }
    }

    {
      var s = guiState();
      if (s.equals(mLastWrittenGuiState)) {
        mGuiStateModTime = currentTime();
      } else {
        if (currentTime() - mGuiStateModTime > 1000) {
          Files.S.writePretty(guiStateFile(), s);
          mLastWrittenGuiState = s;
          mGuiStateModTime = currentTime();
        }
      }
    }
  }

  private long currentTime() {
    var x = mCurrentFrameTime;
    checkState(x != 0);
    return x;
  }

  private long mCurrentFrameTime;
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
    canvas().repaint();
  }

  //------------------------------------------------------------------
  // Frame
  // ------------------------------------------------------------------

  private void createFrame() {
    mFrame = new FrameWrapper();
    mFrame.frame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    mFrame.frame().addKeyListener(this);

    // We embed a JPanel that serves as a container for other components, 
    // the main one being the editor window, but others that may include
    // control panels or informational windows

    JPanel parentPanel = new JPanel(new BorderLayout());
    parentPanel.add(canvas());
    contentPane().add(parentPanel);
  }

  private Canvas canvas() {
    if (mCanvas == null)
      mCanvas = new Canvas();
    return mCanvas;
  }

  /**
   * Get app frame's content pane
   */
  private JComponent contentPane() {
    return (JComponent) mFrame.frame().getContentPane();
  }

  // ------------------------------------------------------------------
  // Lesson logic
  // ------------------------------------------------------------------

  private void prepareLesson(String keyIfRepeat) {
    var b = createWork();
    b.status(LessonStatus.ACTIVE);
    b.cursor(0);
    b.questionCount(0).correctCount(0);
    Msg.remove(MSG_MAIN);

    String key;
    if (nonEmpty(keyIfRepeat))
      key = keyIfRepeat;
    else
      key = lessonManager().choose();
    b.lessonId(key);

    var notes = lessonManager().renderedNotes(key);
    var ic = new int[notes.renderedChords().size()];
    ic[0] = ICON_POINTER;
    b.icons(ic);

    writeWork();
  }

  private void processPlayerChord(Chord ch) {
    //alertVerbose();
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
      if (!config().silentCorrection())
        MidiManager.SHARED_INSTANCE.playCorrection(expChord, 600);
    }

    int newIcon = correct ? ICON_RIGHT : ICON_WRONG;
    b.icons()[b.cursor()] = newIcon;

    b.questionCount(b.questionCount() + 1);

    // If there was a mistake, we're going to make user repeat this chord

    if (correct) {
      b.correctCount(b.correctCount() + 1);
      b.cursor(b.cursor() + 1);
      if (b.cursor() != notes.renderedChords().size()) {
        b.icons()[b.cursor()] = ICON_POINTER;
      } else {
        if (b.correctCount() == b.questionCount())
          b.status(LessonStatus.DONE);
        else {
          b.status(LessonStatus.RETRY);
          Msg.set(MSG_MAIN, "$ff0000", "Try Again");
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

  // ------------------------------------------------------------------
  // Convenience methods for manipulating DrillState
  // ------------------------------------------------------------------

  /**
   * Construct a builder from a copy of the lesson state
   */
  private LessonState.Builder createWork() {
    currentTime();
    var b = lessonState().build().toBuilder();
    mTempLessonState = b;
    var ic = b.icons();
    b.icons(Arrays.copyOf(ic, ic.length));
    return b;
  }

  /**
   * Replace lesson state with the work version, and trigger a repaint
   */
  private void writeWork() {
    var b = mTempLessonState;
    if (b.status() != lessonState().status())
      b.timeMs(currentTime());
    setLessonState(b);
    refreshView("replaced drill state");
    mTempLessonState = null;
  }

  private LessonState.Builder mTempLessonState;
  private Queue<KeyEvent> mKeyEventQueue = new LinkedList<>();

  // ------------------------------------------------------------------
  // JFrame KeyListener interface
  // ------------------------------------------------------------------

  @Override
  public void keyTyped(KeyEvent e) {
  }

  @Override
  public void keyPressed(KeyEvent e) {
    mKeyEventQueue.add(e);
  }

  @Override
  public void keyReleased(KeyEvent e) {
  }

  // ------------------------------------------------------------------
  // Edit mode
  // ------------------------------------------------------------------

  private void processEditChord(Chord ch) {
    pr("processEditChord:", ch);

    int expHand = (mEditList.size() % 2);
    if (expHand == 0) {
      mEditList.add(ch);
    } else {
      var prev = last(mEditList);
      if (lastNote(prev) >= firstNote(ch)) {
        pr("chord not above LH:", INDENT, prev, CR, ch);
        return;
      }
      mEditList.add(ch);
    }

    pr(compileChords(mEditList));
  }

  private static String compileChords(List<Chord> chords) {
    var sb = new StringBuilder();
    int slot = INIT_INDEX;
    for (var c : chords) {
      slot++;
      if (sb.length() != 0) {
        if (config().hand() == Hand.BOTH && slot % 2 == 1)
          sb.append(":");
        else
          sb.append("    ");
      }
      sb.append(encodeChord(c));
    }
    return sb.toString();
  }

  private void createChords() {
    pr("creating chords");
    SystemUtil.prepareForConsoleOrGUI(true);
    var m = MidiManager.SHARED_INSTANCE;
    m.start();

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

  private List<Chord> mEditList = arrayList();

}
