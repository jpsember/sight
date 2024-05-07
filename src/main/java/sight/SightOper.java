package sight;

import static js.base.Tools.*;

import java.io.File;

import js.app.AppOper;
import js.base.BasePrinter;
import js.graphics.ImgUtil;
import sight.gen.Hand;
import sight.gen.KeySig;
import sight.gen.RenderedSet;
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
    if (mConfig == null) {
      loadTools();
      mConfig = (SightConfig) super.config();
    }
    return mConfig;
  }

  private SightConfig mConfig;

  @Override
  public void perform() {

    var rs = RenderedSet.newBuilder();
    rs.keySig(KeySig.E);
    rs.hand(Hand.RIGHT);
    rs.notes("<gis b dis>4 <gis' b dis gis> <fis, a cis e> <fis a c dis> <c' e g> <d f a> <e g b> <e ges b>");

    var r = rs.build();
    var cl = new ChordLibrary();
    //cl.ignoreCache();

    //cl.alertVerbose();
    var rn = cl.get(r);
    var sc = new ScoreCanvas();
    sc.setNotes(rn);
    sc.setSourceImage(rn.imageFile());
    sc.render();
    var img = sc.image();
    ImgUtil.writeImage(files(), img, new File("canvas.png"));
  }

}
