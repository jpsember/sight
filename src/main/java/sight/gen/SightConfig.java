package sight.gen;

import java.io.File;
import js.data.AbstractData;
import js.file.Files;
import js.json.JSMap;

public class SightConfig implements AbstractData {

  public File chords() {
    return mChords;
  }

  public int donePauseTimeMs() {
    return mDonePauseTimeMs;
  }

  public int quiescentChordMs() {
    return mQuiescentChordMs;
  }

  public boolean viewRecentEdits() {
    return mViewRecentEdits;
  }

  public double noteScale() {
    return mNoteScale;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "chords";
  protected static final String _1 = "done_pause_time_ms";
  protected static final String _2 = "quiescent_chord_ms";
  protected static final String _3 = "view_recent_edits";
  protected static final String _4 = "note_scale";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mChords.toString());
    m.putUnsafe(_1, mDonePauseTimeMs);
    m.putUnsafe(_2, mQuiescentChordMs);
    m.putUnsafe(_3, mViewRecentEdits);
    m.putUnsafe(_4, mNoteScale);
    return m;
  }

  @Override
  public SightConfig build() {
    return this;
  }

  @Override
  public SightConfig parse(Object obj) {
    return new SightConfig((JSMap) obj);
  }

  private SightConfig(JSMap m) {
    {
      mChords = Files.DEFAULT;
      String x = m.opt(_0, (String) null);
      if (x != null) {
        mChords = new File(x);
      }
    }
    mDonePauseTimeMs = m.opt(_1, 1200);
    mQuiescentChordMs = m.opt(_2, 250);
    mViewRecentEdits = m.opt(_3, false);
    mNoteScale = m.opt(_4, 1.0);
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof SightConfig))
      return false;
    SightConfig other = (SightConfig) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mChords.equals(other.mChords)))
      return false;
    if (!(mDonePauseTimeMs == other.mDonePauseTimeMs))
      return false;
    if (!(mQuiescentChordMs == other.mQuiescentChordMs))
      return false;
    if (!(mViewRecentEdits == other.mViewRecentEdits))
      return false;
    if (!(mNoteScale == other.mNoteScale))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mChords.hashCode();
      r = r * 37 + mDonePauseTimeMs;
      r = r * 37 + mQuiescentChordMs;
      r = r * 37 + (mViewRecentEdits ? 1 : 0);
      r = r * 37 + (int) mNoteScale;
      m__hashcode = r;
    }
    return r;
  }

  protected File mChords;
  protected int mDonePauseTimeMs;
  protected int mQuiescentChordMs;
  protected boolean mViewRecentEdits;
  protected double mNoteScale;
  protected int m__hashcode;

  public static final class Builder extends SightConfig {

    private Builder(SightConfig m) {
      mChords = m.mChords;
      mDonePauseTimeMs = m.mDonePauseTimeMs;
      mQuiescentChordMs = m.mQuiescentChordMs;
      mViewRecentEdits = m.mViewRecentEdits;
      mNoteScale = m.mNoteScale;
    }

    @Override
    public Builder toBuilder() {
      return this;
    }

    @Override
    public int hashCode() {
      m__hashcode = 0;
      return super.hashCode();
    }

    @Override
    public SightConfig build() {
      SightConfig r = new SightConfig();
      r.mChords = mChords;
      r.mDonePauseTimeMs = mDonePauseTimeMs;
      r.mQuiescentChordMs = mQuiescentChordMs;
      r.mViewRecentEdits = mViewRecentEdits;
      r.mNoteScale = mNoteScale;
      return r;
    }

    public Builder chords(File x) {
      mChords = (x == null) ? Files.DEFAULT : x;
      return this;
    }

    public Builder donePauseTimeMs(int x) {
      mDonePauseTimeMs = x;
      return this;
    }

    public Builder quiescentChordMs(int x) {
      mQuiescentChordMs = x;
      return this;
    }

    public Builder viewRecentEdits(boolean x) {
      mViewRecentEdits = x;
      return this;
    }

    public Builder noteScale(double x) {
      mNoteScale = x;
      return this;
    }

  }

  public static final SightConfig DEFAULT_INSTANCE = new SightConfig();

  private SightConfig() {
    mChords = Files.DEFAULT;
    mDonePauseTimeMs = 1200;
    mQuiescentChordMs = 250;
    mNoteScale = 1.0;
  }

}
