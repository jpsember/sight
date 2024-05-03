package sight;

import static js.base.Tools.*;

import org.junit.Test;

import js.testutil.MyTestCase;

public class SightTest extends MyTestCase {

  @Test
  public void parser() {
    p("<gis b dis>4 <gis' b dis gis> <fis, a cis e> <fis a c dis>");
  }

  private void p(String lilyPondExpr) {
    var parser = new NoteParser();
    parser.setVerbose(verbose());
    parser.parse(lilyPondExpr);
    var jsonList = list();
    for (var cd : parser.chords())
      jsonList.add(cd);
    assertMessage(jsonList);
  }

}
