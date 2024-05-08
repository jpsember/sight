package sight;

import static js.base.Tools.*;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import js.app.App;
import js.app.AppOper;
import js.system.SystemUtil;

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

    //    UserEventManager.construct(getDefaultUserOperation());
    //    UserEventManager.sharedInstance().setListener((x) -> userEventManagerListener(x));
    //    KeyboardShortcutManager.construct(guiAppConfig().keyboardShortcutRegistry());

    createFrame();

    mFrame.frame().setVisible(true);
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
    var b = new JButton("hello");
    parentPanel.add(b);
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

}
