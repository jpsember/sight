package sight;

import static js.base.Tools.*;
import static sight.Util.*;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import js.base.BaseObject;
import js.data.DataUtil;
import js.file.Files;
import js.geometry.MyMath;
import js.parsing.RegExp;
import sight.gen.Hand;
import sight.gen.KeySig;
import sight.gen.Lesson;
import sight.gen.LessonCollection;
import sight.gen.LessonFolder;
import sight.gen.LessonStat;
import sight.gen.RenderedNotes;

public class LessonManager extends BaseObject {

  public void prepare() {
    //    alertVerbose();
    if (prepared())
      return;

    int seed = config().seed();
    if (seed == 0)
      seed = (int) System.currentTimeMillis();
    mLessonSelectionRand = new Random(seed);
    log("chose random number generator");

    var f = folderFile();
    mFolder = Files.parseAbstractDataOpt(LessonFolder.DEFAULT_INSTANCE, f).toBuilder();
    mFolderMod = false;
  }

  public boolean advance() {
    mPassCursor++;
    if (mPassCursor == mCurrentPassList.size()) {
      mPassCursor = 0;
      mPassNumber++;
      if (mPassNumber == REPS_PER_LESSON) {
        mLessonSet = null;
        mPassNumber = 0;
        return true;
      }
    }
    return false;
  }

  public String choose() {
    log("choose lesson");
    checkState(prepared());
    if (mLessonSet == null)
      prepareLessonSet();
    var key = mCurrentPassList.get(mPassCursor);
    if (SMALL)
      pr(VERT_SP, "choose lesson; pass:", mPassNumber, "cursor:", mPassCursor);
    log("pass:", mPassNumber, "cursor:", mPassCursor, "id:", key);
    mLastLessonId = key;
    return key;
  }

  public void recordResult(String lessonId, int pctRight) {
    var stat = lessonStat(lessonId).toBuilder();
    double currAcc = pctRight / 100.0;
    double EXP = 0.15;

    double updAcc = EXP * currAcc + (1 - EXP) * stat.accuracy();
    stat.accuracy((float) updAcc);
    log("recorded result for", lessonId, "% right:", pctRight, INDENT, stat);
    mFolder.stats().put(lessonId, stat);
    setModified("recorded result for lesson");
    flushFolder();
  }

  public RenderedNotes renderedNotes(String key) {
    var m = getSets();
    var r = m.get(key);
    checkState(r != null, "no RenderedNotes found for key:", key);
    return r;
  }

  private Map<String, Lesson> lessonMap() {
    getSets();
    return mLessonMap;
  }

  private Map<String, RenderedNotes> getSets() {
    if (mSets == null)
      mSets = constructSets();
    return mSets;
  }

  private Map<String, RenderedNotes> constructSets() {
    var f = new File("lessons.json");
    mLessonCollection = Files.parseAbstractDataOpt(LessonCollection.DEFAULT_INSTANCE, f);
    checkArgument(mLessonCollection.lessons().size() != 0, "no chord sets found in lesson collection:", f,
        INDENT, mLessonCollection);

    var hand = config().hand();
    if (hand == Hand.UNKNOWN)
      hand = Hand.BOTH;

    Map<String, RenderedNotes> result = hashMap();

    mLessonMap = hashMap();
    var renderMap = mLessonMap;

    for (var x : mLessonCollection.lessons()) {

      if (x.hand() == Hand.UNKNOWN) {
        var nparser = new ChordParser();
        nparser.parse(x.notes());
        var b = x.toBuilder();
        if (nparser.twoHands())
          b.hand(Hand.BOTH);
        else {
          b.hand(inferHandFromNotes(nparser.chords()));
        }
        x = b.build();
      }

      if (!(hand == Hand.BOTH || hand == x.hand())) {
        log("...lesson hand", x.hand(), "not desired", hand);
        continue;
      }

      if (config().key() != KeySig.UNDEFINED && config().key() != x.keySig()) {
        log("...lesson key", x.keySig(), "not desired", config().key());
        continue;
      }

      log(VERT_SP, "generating lessons from chord set:", x.id(), x.notes());

      List<Lesson> inspectionList = arrayList();
      var y = generateLessonsFromChordSet(x, inspectionList);

      if (inspectionList != null) {
        for (var il : inspectionList) {
          chordLibrary().generateInspection(il);
        }
      }

      for (var z : y) {
        var key = z.id();
        renderMap.put(key, z);
        result.put(key, chordLibrary().get(z));
      }
    }

    if (result.isEmpty())
      throw badState("no lessons match desired key signature + hand combination:", INDENT, config());
    return result;
  }

  private List<Lesson> generateLessonsFromChordSet(Lesson source, List<Lesson> inspectionList) {

    // We need a distinct random number generator for each set we're generating
    var rand = new Random(calcHashFor(source));

    List<Lesson> out = arrayList();

    var noteStrs = arrayList(source.notes().split(" +"));

    int numNotes = noteStrs.size();

    int numPerms = 1;
    if (numNotes > 3) {
      numPerms = 5;
    }

    if (inspectionList != null) {
      for (int i = 0; i < numNotes; i += NOTES_PER_LESSON) {

        var b = source.toBuilder();
        b.description(source.description() + "_" + i);
        var sb = new StringBuilder();
        for (var j = i; j < i + NOTES_PER_LESSON; j++) {
          if (j >= numNotes)
            continue;
          var chordStr = noteStrs.get(j);
          sb.append(chordStr);
          sb.append(' ');
        }
        b.notes(sb.toString().trim());
        b.id(DataUtil.hex32(calcHashFor(b)));
        inspectionList.add(b.build());
      }
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

    String result;
    int j = chordStr.indexOf(':');
    if (j >= 0) {
      var ca = chordStr.substring(0, j);
      var cb = chordStr.substring(j + 1);
      ca = auxRandomlyOmitNotes(ca, rand);
      cb = auxRandomlyOmitNotes(cb, rand);
      result = ca + ":" + cb;
    } else
      result = auxRandomlyOmitNotes(chordStr, rand);

    // pr("randomly omit notes:", INDENT, chordStr, CR, result);

    if (false)
      log("randomly omit notes:", INDENT, chordStr, CR, result);
    return result;
  }

  private String auxRandomlyOmitNotes(String chordStr, Random rand) {

    checkArgument(chordStr.indexOf(':') < 0);

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
    return result;
  }

  private File folderFile() {
    return new File("lesson_folder.json");
  }

  private LessonStat lessonStat(String lessonId) {
    return mFolder.stats().getOrDefault(lessonId, LessonStat.DEFAULT_INSTANCE);
  }

  private Comparator<String> LESSON_COMPARATOR = new Comparator<String>() {
    @Override
    public int compare(String h1, String h2) {
      var s1 = lessonStat(h1);
      var s2 = lessonStat(h2);
      float acc1 = s1.accuracy();
      float acc2 = s2.accuracy();
      int diff = Float.compare(acc1, acc2);
      if (diff == 0)
        diff = String.CASE_INSENSITIVE_ORDER.compare(h1, h2);
      return diff;
    }
  };

  private void setModified(String cause) {
    if (!mFolderMod) {
      log("setting folder modified:", cause);
      mFolderMod = true;
    }
  }

  private boolean prepared() {
    return mFolder != null;
  }

  private void flushFolder() {
    if (!mFolderMod)
      return;

    var f = folderFile();
    Files.S.writePretty(f, mFolder);
    log("...flushed lesson folder", INDENT, mFolder);
    mFolderMod = false;
  }

  private void prepareLessonSet() {
    if (SMALL)
      pr(VERT_SP, "===================== preparing lesson set");
    var rand = mLessonSelectionRand;

    if (mLessonSet == null) {
      log("preparing new lesson set");
      List<String> t = arrayList();

      Pattern p = null;
      {
        var s = config().pattern();
        if (nonEmpty(s)) {
          p = RegExp.pattern(s);
        }
      }

      for (var ent : lessonMap().entrySet()) {
        var id = ent.getKey();
        var lesson = ent.getValue();
        if (config().hand() != Hand.UNKNOWN) {
          if (lesson.hand() != config().hand()) {
            log("hand", lesson.hand(), "!=", config().hand());
            continue;
          }
        }
        if (p != null) {
          var m = p.matcher(lesson.description());
          if (!m.find()) {
            log("pattern", quote(config().pattern()), "doesn't match description",
                quote(lesson.description()));
            continue;
          }
        }
        t.add(id);
      }

      //  t.addAll(lessonMap().keySet());
      t.sort(LESSON_COMPARATOR);
      List<String> orderedLessonIds = t;
      int numLess = orderedLessonIds.size();
      if (numLess == 0) {
        halt("no lessons found matching criteria");
      }
      List<String> ls = arrayList();
      mLessonSet = ls;

      var maxLessonsPerSession = Math.min(numLess, MAX_LESSONS_PER_SESSION);

      for (int i = 0; i < maxLessonsPerSession; i++) {
        if (ls.size() == maxLessonsPerSession)
          break;
        // Choose a lesson that has a particular position in the accuracy distribution

        String id = null;
        {
          double pos = ((i + .5) / maxLessonsPerSession) * numLess;
          int slot = (int) Math.round(pos);
          slot = MyMath.clamp(slot, 0, numLess - 1);
          id = orderedLessonIds.get(slot);
          log("i:", i, "slot:", slot, "id:", id);
          if (ls.contains(id)) {
            log("...already in set");
            int k = rand.nextInt(numLess);
            while (true) {
              k = (k + 1) % numLess;
              id = orderedLessonIds.get(k);
              log("....sequential scan, k:", k);
              if (!ls.contains(id))
                break;
            }
          }
          log("...adding:", id);
          ls.add(id);
        }
      }
      mPassNumber = 0;
      mLastLessonId = "";
      mPassCursor = 0;
    }

    if (mPassCursor == 0) {
      log("pass cursor is zero, choosing new permutation");
      List<String> q = arrayList();
      mCurrentPassList = q;
      q.addAll(mLessonSet);
      while (true) {
        MyMath.permute(q, rand);
        log("permutation:", q);
        if (!first(q).equals(mLastLessonId))
          break;
        log("...first element is same as last lesson id:", mLastLessonId);
      }
    }
  }

  private boolean mFolderMod;
  private Random mLessonSelectionRand;
  private Map<String, RenderedNotes> mSets;
  private LessonCollection mLessonCollection;
  private LessonFolder.Builder mFolder;
  private Map<String, Lesson> mLessonMap;
  private List<String> mLessonSet;
  private List<String> mCurrentPassList;
  private int mPassNumber;
  private int mPassCursor;
  private String mLastLessonId;
}
