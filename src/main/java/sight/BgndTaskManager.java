package sight;

import static js.base.Tools.*;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import js.base.BaseObject;

/**
 * Supports periodically calling tasks on Swing event thread
 */
public class BgndTaskManager extends BaseObject {

  public BgndTaskManager addTask(Runnable task) {
    assertMutable();
    mTaskList.add(task);
    return this;
  }

  public void start() {
    assertMutable();
    mScheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
      public Thread newThread(Runnable r) {
        Thread t = Executors.defaultThreadFactory().newThread(r);
        t.setDaemon(true);
        return t;
      }
    });
    mScheduledThreadPoolExecutor.scheduleWithFixedDelay(
        () -> SwingUtilities.invokeLater(() -> backgroundTask()), 1000, 15, TimeUnit.MILLISECONDS);
  }

  public void stop() {
    if (!started())
      return;
    mScheduledThreadPoolExecutor.shutdown();
    mScheduledThreadPoolExecutor = null;
  }

  private void backgroundTask() {
    log("executing background task", System.currentTimeMillis());
    for (Runnable r : mTaskList) {
      try {
        r.run();
      } catch (Throwable t) {
        pr("*** Caught exception in SwingTaskManager periodic background task:", INDENT, t);
        if (alert("always exiting")) {
          pr("Exiting immediately");
          System.exit(1);
        }
      }
    }
  }

  private void assertMutable() {
    checkState(!started(), "already started");
  }

  private boolean started() {
    return mScheduledThreadPoolExecutor != null;
  }

  private ScheduledThreadPoolExecutor mScheduledThreadPoolExecutor;
  private List<Runnable> mTaskList = arrayList();
}
