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

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "chords";
  protected static final String _1 = "done_pause_time_ms";
  protected static final String _2 = "quiescent_chord_ms";

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
    mQuiescentChordMs = m.opt(_2, 125);
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
      m__hashcode = r;
    }
    return r;
  }

  protected File mChords;
  protected int mDonePauseTimeMs;
  protected int mQuiescentChordMs;
  protected int m__hashcode;

  public static final class Builder extends SightConfig {

    private Builder(SightConfig m) {
      mChords = m.mChords;
      mDonePauseTimeMs = m.mDonePauseTimeMs;
      mQuiescentChordMs = m.mQuiescentChordMs;
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

  }

  public static final SightConfig DEFAULT_INSTANCE = new SightConfig();

  private SightConfig() {
    mChords = Files.DEFAULT;
    mDonePauseTimeMs = 1200;
    mQuiescentChordMs = 125;
  }

}
