package sight;

import static js.base.Tools.*;

import java.io.File;

import js.app.AppOper;
import js.base.BasePrinter;
import js.base.SystemCall;
import js.file.FileException;
import js.file.Files;
import js.graphics.ImgUtil;
import js.parsing.MacroParser;
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

  private String compileKey() {
    var k = config().keySig();
    var s = k.toString().toLowerCase();
    var s2 = chomp(s, "_flat");
    if (s2 != s) {
      s = s2 + "es";
    }
    s = s + " \\major";
    return s;
  }

  @Override
  public void perform() {

    String handFragName;
    switch (config().hand()) {
    default:
      throw notFinished("not yet supported:", config().hand());
    case RIGHT:
      handFragName = "right_hand.txt";
      break;
    case LEFT:
      handFragName = "left_hand.txt";
      break;
    }
    var template = frag(handFragName);

    var m = map();
    m.put("key", compileKey());
    m.put("notes", compileNotes(config().notes()));

    MacroParser parser = new MacroParser();
    parser.withTemplate(template).withMapper(m);
    String script = parser.content();

    File targetFile;

    if (alert("skipping the system call")) {
      targetFile = new File("/Users/home/github_projects/sight/_SKIP_work/hello.png");

    } else {
      // Create work directory

      var workDir = new File(Files.currentDirectory(), "_SKIP_work");
      files().deleteDirectory(workDir, "_SKIP_");
      files().mkdirs(workDir);

      String name = "hello";
      var sourceFile = new File(workDir, name + ".ly");
      targetFile = Files.setExtension(sourceFile, "png");

      files().writeString(sourceFile, script);

      {

        var s = new SystemCall();
        s.setVerbose(verbose());

        s.directory(workDir);
        s.arg("/opt/local/bin/lilypond", "--format=png", "-dresolution=" + config().resolution());
        s.arg(name + ".ly");
        s.call();

        if (!targetFile.exists()) {
          pr("target file doesn't exist?", targetFile);
          alert("problem compiling:", sourceFile, INDENT, s.systemErr());
          pr("targetFile:", Files.infoMap(targetFile));
          badState("trouble compiling");
        }
      }

    }
    var bi = ImgUtil.read(targetFile);

    var ext = new ImgExtractor();
    //    ext.alertVerbose();
    ext.setSource(bi);
    ext.extract();

    mark("do something with rectangles and the note expressions that generated them");
  }

  public String frag(String resourceName) {
    try {
      return Files.readString(getClass(), resourceName);
    } catch (FileException e) {
      // In case we're running in an IDE, look for it in other ways
      var pkg = "sight";
      var dir = new File(Files.homeDirectory(), "github_projects/sight/src/main/resources/" + pkg);
      var f = new File(dir, resourceName);
      return Files.readString(f);
    }
  }

  private String compileNotes(String notesExpr) {
    checkNonEmpty(notesExpr, "no notes given!");
    
    // Ensure that 
    return notesExpr.trim();
  }

}
