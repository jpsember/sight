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
import sight.gen.KeySig;
import sight.gen.Lesson;
import sight.gen.LessonCollection;
import sight.gen.LessonFolder;
import sight.gen.LessonStat;
import sight.gen.LessonState;
import sight.gen.RenderedNotes;
import sight.gen.Session;

public class LessonManager extends BaseObject {

  public void init() {
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

    getSets();
  }

  public boolean advance() {
    mPassCursor++;
    if (mPassCursor == lessonIds().size()) {
      mPassCursor = 0;
      mPassNumber++;
      if (mPassNumber == REPS_PER_LESSON) {
        mAccuracyAtLessonEnd = calcLessonAccuracy();
        mSession = null;
        mPassNumber = 0;
        return true;
      }
    }
    return false;
  }

  public float[] accuracyAtLessonStartAndEnd() {
    float[] r = new float[2];
    r[0] = mAccuracyAtLessonStart;
    r[1] = mAccuracyAtLessonEnd;
    return r;
  }

  public String choose() {
    log("choose lesson");
    checkState(prepared());
    if (mSession == null)
      prepareLesson();

    if (mPassCursor == 0) {
      log("pass cursor is zero, choosing new permutation");
      preparePass();
    }

    var key = lessonIds().get(mPassCursor);
    if (SMALL)
      pr(VERT_SP, "choose lesson; pass:", mPassNumber, "cursor:", mPassCursor);
    log("pass:", mPassNumber, "cursor:", mPassCursor, "id:", key);
    session().lastLessonId(key);

    // Write the session to the filesystem 
    {
      var s = session();
      Files.S.writePretty(sessionPath(), s);
    }

    return key;
  }

  private File sessionPath() {
    return new File(".session.json");
  }

  public void recordResult(LessonState lessonState) {
    log("recordResult for LessonState:", INDENT, lessonState);

    var id = lessonState.lessonId();
    var stat = lessonStat(id).toBuilder();

    var pctRight = calcPercentRight(lessonState);

    double currAcc = pctRight / 100.0;
    double EXP = 0.15;

    double updAcc = EXP * currAcc + (1 - EXP) * stat.accuracy();
    stat.accuracy((float) updAcc);
    log("recorded result for", id, "% right:", pctRight, INDENT, stat);
    mFolder.stats().put(id, stat);
    i39(VERT_SP, "updating stats for lesson id:", id, INDENT, stat);
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
    var collection = Files.parseAbstractDataOpt(LessonCollection.DEFAULT_INSTANCE, f);

    if (ISSUE_43) {
      // Remove any lessons that aren't in a particular key
      var b = LessonCollection.newBuilder();
      for (var x : collection.lessons()) {
        if (x.keySig().equals(KeySig.D_FLAT)) {
          b.lessons().add(x);
        }
      }
      collection = b.build();
    }
    mLessonCollection = collection;

    checkArgument(mLessonCollection.lessons().size() != 0, "no chord sets found in lesson collection:", f,
        INDENT, mLessonCollection);

    Map<String, RenderedNotes> result = hashMap();

    mLessonMap = hashMap();
    var renderMap = mLessonMap;

    for (var x : mLessonCollection.lessons()) {
      if (config().key() != KeySig.UNDEFINED && config().key() != x.keySig()) {
        log("...lesson key", x.keySig(), "not desired", config().key());
        continue;
      }

      log(VERT_SP, "generating lessons from chord set:", x.id(), "key:", x.keySig(), "notes:", x.notes());

      var y = generateLessonsFromChordSet(x);

      for (var z : y) {
        var key = z.id();
        renderMap.put(key, z);
        try {
          result.put(key, chordLibrary().get(z));
        } catch (Throwable t) {
          badState("Failed getting chord from library:", INDENT, z, CR, t);
        }
      }
    }

    if (result.isEmpty())
      throw badState("no lessons match desired key signature + hand combination:", INDENT, config());
    return result;
  }

  private List<Lesson> generateLessonsFromChordSet(Lesson source) {

    // We need a distinct random number generator for each set we're generating
    var rand = new Random(calcHashFor(source));

    List<Lesson> out = arrayList();

    var notesString = source.notes().trim();
    var noteStrs = arrayList(notesString.split(" +"));

    {
      for (var x : noteStrs) {
        if (x.trim().isEmpty())
          badArg("empty string in notes:", INDENT, source, CR, noteStrs);
      }
    }

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
    log("...flushed lesson folder" /* , INDENT, mFolder */);
    mFolderMod = false;
  }

  private void prepareLesson() {
    if (SMALL)
      pr(VERT_SP, "===================== preparing lesson set");

    mSession = Session.newBuilder();

    if (config().repeat()) {
      var p = sessionPath();
      if (!p.exists())
        badState("No session to repeat:", INDENT, Files.infoMap(p));
      var s = Files.parseAbstractDataOpt(Session.DEFAULT_INSTANCE, p);
      log("repeating last session");
      mDesiredFirstLessonId = s.lastLessonId();
      mSession = s.toBuilder();
    } else {

      var rand = mLessonSelectionRand;

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

      t.sort(LESSON_COMPARATOR);
      List<String> orderedLessonIds = t;
      int numLess = orderedLessonIds.size();
      if (numLess == 0) {
        halt("no lessons found matching criteria");
      }
      List<String> ls = mSession.lessonIds();

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
    }

    mPassNumber = 0;
    mPassCursor = 0;
    mAccuracyAtLessonStart = calcLessonAccuracy();

    // Preload lessons into image cache
    imageCache().clear();
    for (var id : lessonIds()) {
      var lesson = lessonMap().get(id);
      var rn = chordLibrary().get(lesson);
      imageCache().get(rn.imageFile());
    }

  }

  private void preparePass() {
    checkState(mPassCursor == 0);

    var rand = mLessonSelectionRand;

    var lessonIds = lessonIds();
    if (lessonIds().size() > 1) {
      while (true) {
        MyMath.permute(lessonIds, rand);
        log("permutation:", lessonIds);
        if (!first(lessonIds).equals(session().lastLessonId()))
          break;
        log("...first element is same as last lesson id:", session().lastLessonId());
      }
    }

    var id = mDesiredFirstLessonId;
    if (nonEmpty(id)) {
      var slot = lessonIds().indexOf(id);
      checkState(slot >= 0, "can't find lesson:", id);
      lessonIds().remove(slot);
      lessonIds().add(0, id);
      mDesiredFirstLessonId = null;
    }
  }

  private float calcLessonAccuracy() {
    i39("calcLessonAccuracy; ids:", lessonIds());
    float accSum = 0;
    var lessonIds = lessonIds();
    for (var id : lessonIds) {
      var stat = lessonStat(id);
      accSum += stat.accuracy();
      i39("...stats for:", id, INDENT, stat);
    }
    float acc = accSum / lessonIds.size();
    i39("...calc'd accuracy:", acc);
    return acc;
  }

  private Session.Builder session() {
    return mSession;
  }

  private List<String> lessonIds() {
    return session().lessonIds();
  }

  private boolean mFolderMod;
  private Random mLessonSelectionRand;
  private Map<String, RenderedNotes> mSets;
  private LessonCollection mLessonCollection;
  private LessonFolder.Builder mFolder;
  private Map<String, Lesson> mLessonMap;
  private int mPassNumber;
  private int mPassCursor;
  private float mAccuracyAtLessonStart;
  private float mAccuracyAtLessonEnd;
  private Session.Builder mSession;
  private String mDesiredFirstLessonId;
}
