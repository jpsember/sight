package sight;

import static js.base.Tools.*;

import js.guiapp.GUIApp;
import js.guiapp.MenuBarWrapper;

public class Sight extends GUIApp {

  public static void main(String[] args) {
    loadTools();
    Sight app = new Sight();
    //app.setCustomArgs("-h");
    app.startApplication(args);
    //app.exitWithReturnCode();
  }

  @Override
  public void repaintPanels(int repaintFlags) {
    pr("repaintPanels, flags:", repaintFlags);
  }

  @Override
  public void populateMenuBar(MenuBarWrapper m) {
    pr("populateMenuBar");
  }

}
