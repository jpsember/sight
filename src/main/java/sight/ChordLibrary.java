package sight;

import static js.base.Tools.*;

import java.io.File;
import java.util.List;

import js.base.BaseObject;
import js.base.SystemCall;
import js.data.DataUtil;
import js.file.FileException;
import js.file.Files;
import js.graphics.ImgUtil;
import js.parsing.MacroParser;
import sight.gen.KeySig;
import sight.gen.RenderedChord;
import sight.gen.RenderedNotes;
import sight.gen.RenderedSet;

public class ChordLibrary extends BaseObject {

  public ChordLibrary() {
    mCacheDirectory = new File("chord_library_cache");
    files().mkdirs(mCacheDirectory);
    mWorkDirectory = new File(mCacheDirectory, "lilypond_work");
    files().mkdirs(mWorkDirectory);
  }

  public ChordLibrary ignoreCache() {
    mIgnoreCache = true;
    return this;
  }

  public RenderedNotes get(RenderedSet rs) {
    todo("the report stuff is still wonky; can't use ?xxx since it doesn't look for a file for some reason");
    //todo("use a hash code with more resolution");
    var key = rs.hashCode();
    var baseName = DataUtil.hex32(key);

    var metadata = new File(mCacheDirectory, baseName + ".json");
    var imgFile = new File(mCacheDirectory, baseName + ".png");
    if (mIgnoreCache || !metadata.exists() || !imgFile.exists())
      compile(rs, metadata, imgFile);
    var rn = Files.parseAbstractData(RenderedNotes.DEFAULT_INSTANCE, metadata);
    return rn;
  }

  private void compile(RenderedSet rs, File metadata, File imgFile) {

    files().deletePeacefully(metadata);
    files().deletePeacefully(imgFile);

    var nparser = new NoteParser();
    nparser.parse(rs.notes());
    var chords = nparser.chords();

    String handFragName;
    switch (rs.hand()) {
    default:
      throw notFinished("not yet supported:", rs.hand());
    case RIGHT:
      handFragName = "right_hand.txt";
      break;
    case LEFT:
      handFragName = "left_hand.txt";
      break;
    }
    var template = frag(handFragName);

    var m = map();
    m.put("key", toLilyPond(rs.keySig()));
    m.put("notes", rs.notes());

    MacroParser parser = new MacroParser();
    parser.withTemplate(template).withMapper(m);
    String script = parser.content();

    File targetFile = imgFile;

    // Create work directory

    var sourceFile = new File(mWorkDirectory, "input.ly");

    files().writeString(sourceFile, script);

    {

      var s = new SystemCall();
      s.setVerbose(verbose());

      var tempOutputFile = new File(mWorkDirectory, "input.png");
      files().deletePeacefully(tempOutputFile);

      s.directory(mWorkDirectory);
      s.arg("/opt/local/bin/lilypond", "--format=png", "-dresolution=" + rs.resolution());
      s.arg("input.ly");
      s.call();

      if (!tempOutputFile.exists()) {
        alert("problem compiling:", sourceFile, INDENT, s.systemErr());
        pr("output file:", Files.infoMap(tempOutputFile));
        badState("trouble compiling");
      }
      files().moveFile(tempOutputFile, targetFile);
    }

    var bi = ImgUtil.read(targetFile);

    var ext = new ImgExtractor();
    //    ext.alertVerbose();
    ext.setSource(bi);
    var boxes = ext.rects();

    var renderedNotesBuilder = RenderedNotes.newBuilder();
    renderedNotesBuilder.imageFile(new File(targetFile.getName()));

    {
      int numBoxes = boxes.size();
      var hdrRects = ImgExtractor.RECT_HEADER_SIZE;
      if (chords.size() != numBoxes - hdrRects) {
        badState("number of chords:", chords.size(), "number of boxes:", numBoxes, "expected:",
            chords.size() + hdrRects);
      }

      List<RenderedChord> renderedChordsList = arrayList();

      var j = INIT_INDEX;
      for (var ch : chords) {
        j++;
        var i = j + hdrRects;
        var renc = RenderedChord.newBuilder();
        renc.chord(ch);
        renc.rect(boxes.get(i));
        renderedChordsList.add(renc);
      }
      renderedNotesBuilder.renderedChords(renderedChordsList);
    }

    files().writePretty(metadata, renderedNotesBuilder);
  }

  private String toLilyPond(KeySig keySig) {
    switch (keySig) {
    default:
      throw notFinished("key sig not supported:", keySig);
    case E:
      return "e \\major";
    }
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

  private Files files() {
    return Files.S;
  }

  private File mCacheDirectory;
  private File mWorkDirectory;
  private boolean mIgnoreCache;

}