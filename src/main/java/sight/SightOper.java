package sight;

import static js.base.Tools.*;

import js.app.AppOper;
import js.base.BasePrinter;
import sight.gen.SightConfig;

public class SightOper extends AppOper {

  @Override
  public String userCommand() {
    return "sight";
  }

  @Override
  public String shortHelp() {
    return "practice sight reading";
  }

  @Override
  protected void longHelp(BasePrinter b) {
    b.pr("more to come");
    //    b.pr("where <options> include:");
    //    var hf = new HelpFormatter();
    //    hf.addItem("[ parent_dir <path> ]", "directory to contain project (default: current dir)");
    //    hf.addItem("[ name <string> ]", "name of project (default: name of parent_dir)");
    //    hf.addItem("[ zap_existing <directory> ]", "deleting existing directory before starting");
    //    b.pr(hf);
  }

  @Override
  public SightConfig defaultArgs() {
    return SightConfig.DEFAULT_INSTANCE;
  }

  @Override
  public SightConfig config() {
    if (mConfig == null)
      mConfig = (SightConfig) super.config();
    return mConfig;
  }

  private SightConfig mConfig;

  @Override
  public void perform() {
    loadTools();
    todo("No implementation yet");
  }

}
