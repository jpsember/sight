package sight;

import static js.base.Tools.*;
import static sight.Util.*;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import js.base.BaseObject;
import js.data.DataUtil;
import js.file.Files;
import js.geometry.MyMath;
import sight.gen.Hand;
import sight.gen.KeySig;
import sight.gen.LessonCollection;
import sight.gen.LessonFolder;
import sight.gen.LessonStat;
import sight.gen.RenderedNotes;
import sight.gen.RenderedSet;

public class LessonManager extends BaseObject {

  private boolean prepared() {
    return mFolder != null;
  }

  public String choose() {
    log("choose lesson");
    checkState(prepared());

    addActiveLessons();
    var list = mFolder.activeLessons();
    checkState(list.size() != 0, "no active lessons");

    String key;
    while (true) {
      key = list.get(mLessonSelectionRand.nextInt(list.size()));
      if (list.size() == 1 || !key.equals(mLastLessonKey))
        break;
    }
    log("chose lesson with key:", key);
    mLastLessonKey = key;
    return key;
  }

  public void recordResult(int pctRight) {
    todo("have it pass in the hash code for the lesson");

  }

  private Map<String, RenderedSet> renderedSetMap() {
    getSets();
    return mRenderedSetMap;
  }

  public RenderedNotes renderedNotes(String key) {
    var m = getSets();
    var r = m.get(key);
    checkState(r != null, "no RenderedNotes found for key:", key);
    return r;
  }

  private Map<String, RenderedNotes> getSets() {
    if (mSets == null) {

      Map<String, RenderedNotes> result = hashMap();

      var f = new File("lessons.json");

      mLessonCollection = Files.parseAbstractDataOpt(LessonCollection.DEFAULT_INSTANCE, f);
      checkArgument(mLessonCollection.renderedSets().size() != 0, "no chord sets found in lesson collection:",
          f, INDENT, mLessonCollection);

      var hand = config().hand();
      if (hand == Hand.UNKNOWN)
        hand = Hand.BOTH;

      mRenderedSetMap = hashMap();

      for (var x : mLessonCollection.renderedSets()) {

        {

          if (x.hand() == Hand.UNKNOWN) {
            var nparser = new ChordParser();
            nparser.parse(x.notes());
            var chords = nparser.chords();
            x = x.toBuilder().hand(inferHandFromNotes(chords)).build();
          }
        }

        if (!(hand == Hand.BOTH || hand == x.hand())) {
          pr("...lesson hand", x.hand(), "not desired", hand);
          continue;
        }

        if (config().key() != KeySig.UNDEFINED && config().key() != x.keySig()) {
          pr("...lesson key", x.keySig(), "not desired", config().key());
          continue;
        }

        log(VERT_SP, "generating lessons from chord set:", x.id(), x.notes());

        var y = generateLessonsFromChordSet(x);
        for (var z : y) {
          var key = z.id();
          mRenderedSetMap.put(key, z);
          log("...getting cached value");
          var rn = chordLibrary().get(z);
          result.put(key, rn);
        }
      }

      if (result.isEmpty())
        throw badState("no lessons match desired key signature + hand combination:", INDENT, config());
      mSets = result;
    }
    return mSets;
  }

  private List<RenderedSet> generateLessonsFromChordSet(RenderedSet source) {

    // We need a distinct random number generator for each set we're generating
    var rand = new Random(calcHashFor(source));

    List<RenderedSet> out = arrayList();

    var noteStrs = arrayList(source.notes().split(" +"));

    int numNotes = noteStrs.size();

    int numPerms = 1;
    if (numNotes > 3) {
      numPerms = 5;
    }

    for (int perm = 0; perm < numPerms; perm++) {
      if (SMALL && perm != 1)
        continue;

      List<String> permuted = noteStrs;
      checkState(rand != null);
      if (perm != 0) {
        permuted = MyMath.permute(noteStrs, rand);
      }

      for (int i = 0; i <= numNotes - NOTES_PER_LESSON; i += NOTES_PER_LESSON) {
        if (SMALL && i != 0)
          break;
        var b = source.toBuilder();
        b.description(source.description() + " " + perm + ":" + i);
        var sb = new StringBuilder();
        for (var j = i; j < i + NOTES_PER_LESSON; j++) {
          var chordStr = permuted.get(j);
          var chordStr2 = randomlyOmitNotes(chordStr, rand);
          sb.append(chordStr2);
          sb.append(' ');
        }
        b.notes(sb.toString().trim());
        b.id(DataUtil.hex32(calcHashFor(b)));
        out.add(b.build());
      }
    }
    return out;
  }

  private String randomlyOmitNotes(String chordStr, Random rand) {
    var noteNums = split(chordStr, '.');
    List<String> out = arrayList();
    for (var x : noteNums) {
      if (rand.nextInt(30) >= 10)
        out.add(x);
    }
    String result;
    if (out.isEmpty())
      result = chordStr;
    else
      result = String.join(".", out);
    log("randomly omit notes:", INDENT, chordStr, CR, result);
    return result;
  }

  public void prepare() {
    if (prepared())
      return;

    mLessonSelectionRand = new Random(1965);
    log("chose random number generators");

    var f = folderFile();
    mFolder = Files.parseAbstractDataOpt(LessonFolder.DEFAULT_INSTANCE, f).toBuilder();
    mFolderMod = false;
  }

  private File folderFile() {
    return new File("lesson_folder.json");
  }

  /**
   * Add active lesson if the current ones all have high accuracy
   */
  private void addActiveLessons() {
    var b = mFolder;

    // build list of active lessons, sorted by accuracy
    List<String> ls = arrayList();
    ls.addAll(b.activeLessons());
    ls.sort(new Comparator<String>() {
      @Override
      public int compare(String h1, String h2) {

        var s1 = mFolder.stats().getOrDefault(h1, LessonStat.DEFAULT_INSTANCE);
        var s2 = mFolder.stats().getOrDefault(h2, LessonStat.DEFAULT_INSTANCE);
        int acc1 = calcAccuracy(s1);
        int acc2 = calcAccuracy(s2);

        int diff = Integer.compare(acc1, acc2);
        if (diff == 0)
          diff = Integer.compare(h1.hashCode(), h2.hashCode());
        return diff;
      }
    });
    log("active lessons currently:", ls);

    final int MAX_ACTIVE_LESSONS = 8;
    final int MIN_ACTIVE_LESSONS = 4;

    while (ls.size() > MAX_ACTIVE_LESSONS) {
      pop(ls);
      setModified("trimming active lessons down");
    }

    List<String> lessonKeys = arrayList();
    lessonKeys.addAll(renderedSetMap().keySet());

    while (ls.size() < MIN_ACTIVE_LESSONS && availLessonsCount() > ls.size()) {
      // Choose a lesson that is not in the list
      String key = null;
      while (true) {
        //  pr("choosing lesson not in list");
        int k = mLessonSelectionRand.nextInt(availLessonsCount());
        key = lessonKeys.get(k);
        if (!b.activeLessons().contains(key))
          break;
      }
      ls.add(key);
      setModified("added new lesson to active list");
    }
    b.activeLessons(ls);
    log("active lessons now:", INDENT, b.activeLessons());
  }

  private int calcAccuracy(LessonStat stat) {
    if (stat.frequency() == 0)
      return 0;
    return (stat.correct() * 100) / stat.frequency();
  }

  private int availLessonsCount() {
    return renderedSetMap().size();
  }

  private void setModified(String cause) {
    if (!mFolderMod) {
      log("setting folder modified:", cause);
      mFolderMod = true;
    }
  }

  private boolean mFolderMod;

  private Random mLessonSelectionRand;
  private Map<String, RenderedNotes> mSets;
  private LessonCollection mLessonCollection;
  private String mLastLessonKey;

  private LessonFolder.Builder mFolder;
  private Map<String, RenderedSet> mRenderedSetMap;

}
