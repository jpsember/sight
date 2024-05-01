package sight;

import static js.base.Tools.*;

import js.app.App;

public class Sight extends App {

  public static void main(String[] args) {
    loadTools();
    Sight app = new Sight();
    app.setCustomArgs("-h");
    app.startApplication(args);
    app.exitWithReturnCode();
  }

  
  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  protected void registerOperations() {
    registerOper(new SightOper());
  }

}
