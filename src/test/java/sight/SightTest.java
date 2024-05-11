package sight;

import static js.base.Tools.*;

import org.junit.Test;

import js.testutil.MyTestCase;

public class SightTest extends MyTestCase {

  @Test
  public void notes1() {
    q("68.71.75.80 47.50.54.59");
  }

  private void q(String ourNotesExpr) {
    var parser = new ChordParser();
    parser.setVerbose(verbose());
    parser.parse(ourNotesExpr);
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
