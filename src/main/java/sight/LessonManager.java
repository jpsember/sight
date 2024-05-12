package sight;

import static js.base.Tools.*;
import static sight.Util.*;

import java.io.File;
import java.util.List;
import java.util.Random;

import js.file.Files;
import js.geometry.MyMath;
import sight.gen.Hand;
import sight.gen.KeySig;
import sight.gen.LessonCollection;
import sight.gen.RenderedNotes;
import sight.gen.RenderedSet;

public class LessonManager {

  public RenderedNotes choose() {
    var s = getSets();
    checkState(!s.isEmpty(), "no RenderedSets in collection");

    int j = 0;
    if (config().viewRecentEdits() && mRecentEditIndex < s.size()) {
      j = s.size() - mRecentEditIndex - 1;
      mRecentEditIndex++;
    } else {

      var rnd = MyMath.random();

      while (true) {
        j = rnd.nextInt(s.size());
        if (s.size() == 1 || j != mLastLessonIndex)
          break;
      }
    }
    mLastLessonIndex = j;
    return s.get(j);
  }

  public void prepare() {
    getSets();
  }

  private List<RenderedNotes> getSets() {

    if (mSets == null) {

      List<RenderedNotes> result = arrayList();

      var f = new File("lessons.json");

      mRand = new Random(config().seed());
      mLessonCollection = Files.parseAbstractDataOpt(LessonCollection.DEFAULT_INSTANCE, f);
      checkArgument(mLessonCollection.renderedSets().size() != 0, "no chord sets found in lesson collection:",
          f, INDENT, mLessonCollection);

      var hand = config().hand();
      if (hand == Hand.UNKNOWN)
        hand = Hand.BOTH;

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

        var y = generateLessonsFromChordSet(x);
        for (var z : y) {
          var rn = chordLibrary().get(z);
          result.add(rn);
        }
      }

      if (result.isEmpty())
        throw badState("no lessons match desired key signature + hand combination:", INDENT, config());
      mSets = result;
    }
    return mSets;
  }

  private int[] permutation(int size) {
    var random = mRand;
    int[] p = new int[size];
    for (int i = 0; i < size; i++)
      p[i] = i;
    for (int i = size - 1; i >= 1; i--) {
      int j = random.nextInt(i + 1);
      int tmp = p[i];
      p[i] = p[j];
      p[j] = tmp;
    }
    return p;
  }

  private List<RenderedSet> generateLessonsFromChordSet(RenderedSet source) {

    List<RenderedSet> out = arrayList();

    var noteStrs = source.notes().split(" +");
    int numNotes = noteStrs.length;

    final int NOTES_PER_LESSON = 4;
    int numPerms = 1;
    if (numNotes > 3)
      numPerms = 5;

    for (int perm = 0; perm < numPerms; perm++) {

      int[] strs2 = new int[numNotes];
      for (int i = 0; i < numNotes; i++)
        strs2[i] = i;

      if (perm != 0) {
        strs2 = permutation(numNotes);
      }

      for (int i = 0; i <= numNotes - NOTES_PER_LESSON; i += NOTES_PER_LESSON) {
        var b = source.toBuilder();
        b.description(source.description() + " " + perm + ":" + i);
        var sb = new StringBuilder();
        for (var j = i; j < i + NOTES_PER_LESSON; j++) {
          sb.append(noteStrs[strs2[j]]);
          sb.append(' ');
        }
        b.notes(sb.toString().trim());
        out.add(b.build());
      }
    }
    return out;
  }

  private Random mRand;
  private List<RenderedNotes> mSets;
  private LessonCollection mLessonCollection;
  private int mLastLessonIndex = -1;
  private int mRecentEditIndex;
}
