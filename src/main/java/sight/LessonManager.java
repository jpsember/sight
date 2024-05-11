package sight;

import static js.base.Tools.*;
import static sight.Util.*;

import java.io.File;
import java.util.List;

import js.file.Files;
import js.geometry.MyMath;
import sight.gen.LessonCollection;
import sight.gen.RenderedNotes;

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

      mLessonCollection = Files.parseAbstractDataOpt(LessonCollection.DEFAULT_INSTANCE, f);
      checkArgument(mLessonCollection.renderedSets().size() != 0, "no chord sets found in lesson collection:",
          f, INDENT, mLessonCollection);

      for (var x : mLessonCollection.renderedSets()) {
        var rn = chordLibrary().get(x);
        result.add(rn);
      }
      mSets = result;
    }
    return mSets;
  }

  private List<RenderedNotes> mSets;
  private LessonCollection mLessonCollection;
  private int mLastLessonIndex = -1;
  private int mRecentEditIndex;
}
