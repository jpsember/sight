package sight;

import static js.base.Tools.*;
import static sight.Util.*;

import java.io.File;
import java.util.List;
import java.util.Random;

import js.base.BaseObject;
import js.base.SystemCall;
import js.file.FileException;
import js.file.Files;
import js.geometry.IRect;
import js.geometry.MyMath;
import js.graphics.ImgUtil;
import js.parsing.MacroParser;
import sight.gen.Chord;
import sight.gen.Hand;
import sight.gen.KeySig;
import sight.gen.RenderedChord;
import sight.gen.RenderedNotes;
import sight.gen.Lesson;

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

  public RenderedNotes get(Lesson rs) {
    var baseName = rs.id();

    var metadata = new File(mCacheDirectory, baseName + ".json");
    var imgFile = new File(mCacheDirectory, baseName + ".png");
    if (mIgnoreCache || !metadata.exists() || !imgFile.exists()) {
      if (ISSUE_24)
        checkState(!wtf);
      
      // We need a distinct random number generator for each set we're generating
      int seed = idToInteger(rs.id()) | 1;
      mOurRand = new Random(seed);
      compile(rs, metadata, imgFile, false);
    }
    var rn = Files.parseAbstractData(RenderedNotes.DEFAULT_INSTANCE, metadata).toBuilder();
    rn.imageFile(new File(mCacheDirectory, rn.imageFile().toString()));
    return rn.build();
  }

  public void generateInspection(Lesson rs) {
    var baseName = filenameSafe(rs.description());
    var dir = new File(mCacheDirectory, "inspect");
    Files.S.mkdirs(dir);
    var imgFile = new File(dir, baseName + ".png");
    if (!imgFile.exists()) {
      compile(rs, null, imgFile, true);
    }
  }

  private static final String filenameSafe(String s) {
    var legal = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    var sb = new StringBuilder();
    var prevLegal = false;
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (legal.indexOf(c) < 0) {
        if (prevLegal) {
          sb.append('_');
          prevLegal = false;
        }
      } else {
        prevLegal = true;
        sb.append(c);
      }
    }
    var r = sb.toString();
    if (r.isEmpty())
      r = "_SAFE_";
    return r;
  }

  private void compile(Lesson rs, File metadata, File imgFile, boolean inspection) {
    pr("compiling lesson:", rs.description());

    if (!inspection)
      files().deletePeacefully(metadata);
    files().deletePeacefully(imgFile);

    var nparser = new ChordParser();
    nparser.parse(rs.notes());

    var hand = rs.hand();
    if (hand == Hand.UNKNOWN) {
      if (nparser.twoHands())
        hand = Hand.BOTH;
      else
        hand = inferHandFromNotes(nparser.chords());
    }

    if (rs.keySig() == KeySig.UNDEFINED)
      badArg("no key signature defined");

    String script;

    List<Chord> chordsRH;
    List<Chord> chordsLH;

    Random rnd2 = null;
    int newSeed = 0;
    if (!inspection) {
      newSeed = mOurRand.nextInt() | 1;

      // We need to use the same random number sequence for both left and right hands
      rnd2 = new Random(newSeed);
    }

    if (nparser.twoHands()) {

      chordsRH = nparser.chordsRH();
      chordsLH = nparser.chordsLH();

      checkState(chordsRH.size() == chordsLH.size(), "mismatched left/right chord counts");

      var template = frag("score_two_hands.txt");

      var m = map();
      m.put("key", toLilyPond(rs.keySig()));
      m.put("notes_rh", encodeLily(chordsRH, rnd2, inspection));
      m.put("notes_lh", encodeLily(chordsLH, new Random(newSeed), inspection));

      MacroParser parser = new MacroParser();
      parser.withTemplate(template).withMapper(m);
      script = parser.content();

    } else {

      var chords = nparser.chords();
      chordsLH = chords;
      chordsRH = chords;

      var template = frag("score_template.txt");

      var m = map();
      m.put("key", toLilyPond(rs.keySig()));
      m.put("notes", encodeLily(chords, rnd2, inspection));
      m.put("clef", hand == Hand.LEFT ? "bass" : "treble");

      MacroParser parser = new MacroParser();
      parser.withTemplate(template).withMapper(m);
      script = parser.content();

    }

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
    ext.setSource(bi);
    var boxes = ext.rects();

    if (config().inspectBoxes()) {
      var bx = ext.plotRects();
      var d = Files.parent(targetFile);
      var bn = Files.basename(targetFile);
      var f = new File(d, bn + "_rects.png");
      ImgUtil.writeImage(files(), bx, f);
      pr("...wrote:", f);
    }

    var nb = RenderedNotes.newBuilder();
    nb.imageFile(new File(targetFile.getName()));

    {
      int numChords = chordsRH.size();

      int numBoxes = boxes.size();
      var hdrRects = ImgExtractor.RECT_HEADER_SIZE;

      if (numChords != numBoxes - hdrRects) {
        if (rs.keySig() == KeySig.C && numChords == numBoxes - (hdrRects - 1)) {
          // Insert a small, empty rect for the key signature, which is not rendered for C major
          numBoxes++;
          var b = boxes.get(ImgExtractor.RECT_CLEF);
          boxes.add(ImgExtractor.RECT_KEYSIG, new IRect(b.x + 3, b.y, 1, 1));
        } else
          badState("number of chords:", numChords, "number of boxes:", numBoxes, "expected:",
              numChords + hdrRects);
      }

      nb.staffRect(boxes.get(ImgExtractor.RECT_STAFF_LINES));
      nb.clefRect(boxes.get(ImgExtractor.RECT_CLEF));
      nb.keysigRect(boxes.get(ImgExtractor.RECT_KEYSIG));
      nb.description(rs.description());
      List<RenderedChord> renderedChordsList = arrayList();

      for (int j = 0; j < numChords; j++) {
        Chord ca;
        Chord cb = null;
        ca = chordsLH.get(j);
        if (nparser.twoHands()) {
          cb = chordsRH.get(j);
        }

        var i = j + hdrRects;
        var renc = RenderedChord.newBuilder();
        renc.chordA(ca);
        renc.chordB(cb);
        renc.rect(boxes.get(i));
        renderedChordsList.add(renc);
      }
      nb.renderedChords(renderedChordsList);
    }

    if (!inspection)
      files().writePretty(metadata, nb);
  }

  private String toLilyPond(KeySig keySig) {
    switch (keySig) {
    default:
      throw notFinished("key sig not supported:", keySig);
    case E:
      return "e \\major";
    case C:
      return "c \\major";
    }
  }

  private String frag(String resourceName) {
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

  private static String[] sDurations = { //
      "4 4 4 4", //
      "4. 8 4 4", //
      "4. 4. 8 4", //
      "2 2 4 4", //
      "2. 2. 8 4.", //
  };

  private String encodeLily(List<Chord> chords, Random rand, boolean inspection) {

    int index = 0;
    if (!inspection) {
      checkArgument(chords.size() == NOTES_PER_LESSON, "expected", NOTES_PER_LESSON, "chords, got:",
          chords.size());
      index = rand.nextInt(sDurations.length);
    }
    var durationExpr = sDurations[index];
    var durationArray = split(durationExpr, ' ');
    if (!inspection) {
      durationArray = MyMath.permute(durationArray, rand);
    }

    var sb = new StringBuilder();
    var i = INIT_INDEX;
    for (var c : chords) {
      i++;
      sb.append(" <");
      for (var kn : c.keyNumbers()) {
        sb.append(keyNumberToLilyNote(kn));
        sb.append(' ');
      }

      sb.append(">");
      sb.append(durationArray.get(i));
      sb.append(' ');
    }
    return sb.toString();
  }

  private static String[] sKeyNumToLilyNote;
  private static String[] sLilyOctaveSuffix = { ",,,", ",,", ",", "", "'", "''", "'''", "''''", "'''''", };
  private static String[] sLilyNoteName = { "c", "cis", "d", "dis", "e", "f", "fis", "g", "gis", "a", "ais",
      "b" };

  private static String keyNumberToLilyNote(int keyNumber) {
    if (sKeyNumToLilyNote == null) {
      var x = new String[MAX_KEY_NUMBER];
      sKeyNumToLilyNote = x;
      int oct = 0;
      int noteOff = 12 - 3;

      for (int i = 0; i < MAX_KEY_NUMBER; i++) {
        var s = sLilyNoteName[noteOff] + sLilyOctaveSuffix[oct];
        x[i] = s;
        if (++noteOff == 12) {
          noteOff = 0;
          oct++;
        }

      }
    }
    return sKeyNumToLilyNote[keyNumber];
  }

  private Random mOurRand;

}
