package sight;

import static js.base.Tools.*;
import static sight.Util.*;

import java.io.File;
import java.util.List;

import js.file.Files;
import js.geometry.MyMath;
import sight.gen.LessonCollection;
import sight.gen.RenderedChord;
import sight.gen.RenderedNotes;

public class LessonManager {

  public RenderedNotes choose() {
    var s = getSets();
    checkState(!s.isEmpty(), "no RenderedSets in collection");
    var rnd = MyMath.random();
    int j = 0;
    while (true) {
      j = rnd.nextInt(s.size());
      if (s.size() == 1 || j != mLastLessonIndex)
        break;
    }
    mLastLessonIndex = j;
    pr("choosing lesson #", j, "from size", s.size());
    return s.get(j);
  }

  public void prepare() {
    getSets();
  }

  private static int[][] subsets = { //
      { 0, 1, 2, 3 }, //
      { 3, 2, 1, 0 }, //
      { 4, 5, 6, 7 }, // 
      { 7, 6, 5, 4 } };

  private List<RenderedNotes> getSets() {

    if (mSets == null) {

      List<RenderedNotes> result = arrayList();

      var f = new File("lessons.json");

      mLessonCollection = Files.parseAbstractDataOpt(LessonCollection.DEFAULT_INSTANCE, f);
      checkArgument(mLessonCollection.renderedSets().size() != 0, "no chord sets found in lesson collection:",
          f, INDENT, mLessonCollection);

      todo(
          "a better approach is to parse the 'big' rendered set into our chord list, then subdivide it from there");

      for (var x : mLessonCollection.renderedSets()) {

        pr("extracting subsets from:", INDENT, x);

        var rn = chordLibrary().get(x);
        pr("...rendered chords:", INDENT, rn);

        if (rn.renderedChords().size() != 8) {
          alert("expected 8 chords, got:", INDENT, rn.renderedChords().size(), CR, x);
          continue;
        }

        for (var subset : subsets) {
          var y = rn.toBuilder();

          List<RenderedChord> sublist = arrayList();

          for (int j : subset) {
            sublist.add(rn.renderedChords().get(j));
          }
          y.renderedChords(sublist);
          pr("...adding:", INDENT, y);
          result.add(y.build());
        }
      }
      mSets = result;
    }
    return mSets;
  }

  private List<RenderedNotes> mSets;
  private LessonCollection mLessonCollection;
  private int mLastLessonIndex = -1;
}
