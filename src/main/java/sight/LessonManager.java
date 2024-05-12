package sight;

import static js.base.Tools.*;
import static sight.Util.*;

import java.io.File;
import java.util.List;
import java.util.Random;

import js.file.Files;
import js.geometry.MyMath;
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

      mRand = new Random(1965);
      mLessonCollection = Files.parseAbstractDataOpt(LessonCollection.DEFAULT_INSTANCE, f);
      checkArgument(mLessonCollection.renderedSets().size() != 0, "no chord sets found in lesson collection:",
          f, INDENT, mLessonCollection);

      for (var x : mLessonCollection.renderedSets()) {
        var y = generateLessonsFromChordSet(x);
        for (var z : y) {
          var rn = chordLibrary().get(z);
          result.add(rn);
        }
      }
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

    pr("split:",INDENT,source.notes(),CR,"to:",CR,noteStrs);
    
    final int NOTES_PER_LESSON = 4;
    for (int perm = 0; perm < 2; perm++) {

      int[] strs2 = new int[noteStrs.length];
      for (int i = 0; i < strs2.length; i++)
        strs2[i] = i;

      if (perm != 0) {
        strs2 = permutation(strs2.length);
      }

      for (int i = 0; i <= strs2.length - NOTES_PER_LESSON; i += NOTES_PER_LESSON) {
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
