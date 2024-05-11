package sight;

import static js.base.Tools.*;
import static sight.Util.*;

import java.io.File;
import java.util.List;

import js.file.Files;
import js.geometry.MyMath;
import js.json.JSMap;
import sight.gen.LessonCollection;
import sight.gen.RenderedSet;

public class LessonManager {

  public RenderedSet choose() {
    intArray();

    var s = getSets();
    checkState(!s.isEmpty(), "no RenderedSets in collection");
    var rnd = MyMath.random();
    return s.get(rnd.nextInt(s.size()));
  }

  public void prepare() {
    getSets();
  }

  private static int[][] subsets = { //
      { 0, 1, 2, 3 }, //
      { 3, 2, 1, 0 }, //
      { 4, 5, 6, 7 }, // 
      { 7, 6, 5, 4 } };

  private List<RenderedSet> getSets() {

    if (mSets == null) {

      List<RenderedSet> result = arrayList();

      var f = new File("lessons.json");

      mLessonCollection = Files.parseAbstractDataOpt(LessonCollection.DEFAULT_INSTANCE, f);
      checkArgument(mLessonCollection.renderedSets().size() != 0, "no chord sets found in lesson collection:",
          f, INDENT, mLessonCollection);
      for (var x : mLessonCollection.renderedSets()) {

        var s = x.notes() + " <";

        // split this into individual chords by splitting at '<' chars
        List<String> chordStrs = arrayList();
        int cursor = 0;
        while (cursor < s.length()) {
          pr("scanning str, cursor:", cursor);
          int i = s.indexOf('<', cursor);
          checkState(i >= 0);
          int j = s.indexOf('<', i + 1);
          if (j < 0)
            break;

          var cc = s.substring(i, j).trim();
          pr("cursor:", cursor, "i:", i);
          if (cc.length() != 0)
            chordStrs.add(cc);
          cursor = j;
        }

        if (chordStrs.size() != 8) {
          alert("expected 8 chords, got:", INDENT, chordStrs);
          continue;
        }

        for (var subset : subsets) {
          var y = x.toBuilder();

          var sb = new StringBuilder();
          for (int j : subset) {
            sb.append(' ');
            sb.append(chordStrs.get(j));
          }
          y.notes(sb.toString());
          pr("extracted:", INDENT, y);
          result.add(y.build());
        }
      }
      mSets = result;
    }
    return mSets;
  }

  private List<RenderedSet> mSets;
  private LessonCollection mLessonCollection;
}
