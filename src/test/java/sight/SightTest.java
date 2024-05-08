package sight;

import static js.base.Tools.*;

import org.junit.Test;

import js.testutil.MyTestCase;

public class SightTest extends MyTestCase {

  @Test
  public void parser() {
    p("<gis b dis>4 <gis' b dis gis> <fis, a cis e> <fis a c dis>");
  }

  @Test
  public void rel1() {
    p("c d e f g a b c d e f g");
  }

  @Test
  public void rel2() {
    p("c'' g c f, c' a, e'' c");
  }

  @Test
  public void rel3() {
    // See https://lilypond.org/doc/v2.25/Documentation/notation/relative-octave-entry
    p("c' <c e g> <c' e g'>  <c, e, g''>");
  }

  private void p(String lilyPondExpr) {
    var parser = new NoteParser();
    parser.setVerbose(verbose());
    parser.parse(lilyPondExpr);
    var jsonList = list();
    for (var cd : parser.chords()) {
      var kn = list();
      for (var knum : cd.keyNumbers())
        kn.add(knum);
      jsonList.add(kn);
    }
    assertMessage(jsonList);
  }

}
