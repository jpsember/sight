package sight;

import static js.base.Tools.*;

import js.app.AppOper;
import sight.gen.SightConfig;

public class SightOper extends AppOper {

  @Override
  public String userCommand() {
    return null;
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
