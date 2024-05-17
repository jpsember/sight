package sight.gen;

import js.data.AbstractData;
import js.json.JSMap;

public class SightConfig implements AbstractData {

  public int donePauseTimeMs() {
    return mDonePauseTimeMs;
  }

  public int quiescentChordMs() {
    return mQuiescentChordMs;
  }

  public boolean viewRecentEdits() {
    return mViewRecentEdits;
  }

  public boolean createChords() {
    return mCreateChords;
  }

  public Hand hand() {
    return mHand;
  }

  public KeySig key() {
    return mKey;
  }

  public boolean inspectBoxes() {
    return mInspectBoxes;
  }

  public int seed() {
    return mSeed;
  }

  public String pattern() {
    return mPattern;
  }

  public int resolution() {
    return mResolution;
  }

  public boolean silentCorrection() {
    return mSilentCorrection;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "done_pause_time_ms";
  protected static final String _1 = "quiescent_chord_ms";
  protected static final String _2 = "view_recent_edits";
  protected static final String _3 = "create_chords";
  protected static final String _4 = "hand";
  protected static final String _5 = "key";
  protected static final String _6 = "inspect_boxes";
  protected static final String _7 = "seed";
  protected static final String _8 = "pattern";
  protected static final String _9 = "resolution";
  protected static final String _10 = "silent_correction";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mDonePauseTimeMs);
    m.putUnsafe(_1, mQuiescentChordMs);
    m.putUnsafe(_2, mViewRecentEdits);
    m.putUnsafe(_3, mCreateChords);
    m.putUnsafe(_4, mHand.toString().toLowerCase());
    m.putUnsafe(_5, mKey.toString().toLowerCase());
    m.putUnsafe(_6, mInspectBoxes);
    m.putUnsafe(_7, mSeed);
    m.putUnsafe(_8, mPattern);
    m.putUnsafe(_9, mResolution);
    m.putUnsafe(_10, mSilentCorrection);
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
    mDonePauseTimeMs = m.opt(_0, 1200);
    mQuiescentChordMs = m.opt(_1, 250);
    mViewRecentEdits = m.opt(_2, false);
    mCreateChords = m.opt(_3, false);
    {
      String x = m.opt(_4, "");
      mHand = x.isEmpty() ? Hand.DEFAULT_INSTANCE : Hand.valueOf(x.toUpperCase());
    }
    {
      String x = m.opt(_5, "");
      mKey = x.isEmpty() ? KeySig.DEFAULT_INSTANCE : KeySig.valueOf(x.toUpperCase());
    }
    mInspectBoxes = m.opt(_6, false);
    mSeed = m.opt(_7, 0);
    mPattern = m.opt(_8, "");
    mResolution = m.opt(_9, 160);
    mSilentCorrection = m.opt(_10, false);
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
    if (!(mDonePauseTimeMs == other.mDonePauseTimeMs))
      return false;
    if (!(mQuiescentChordMs == other.mQuiescentChordMs))
      return false;
    if (!(mViewRecentEdits == other.mViewRecentEdits))
      return false;
    if (!(mCreateChords == other.mCreateChords))
      return false;
    if (!(mHand.equals(other.mHand)))
      return false;
    if (!(mKey.equals(other.mKey)))
      return false;
    if (!(mInspectBoxes == other.mInspectBoxes))
      return false;
    if (!(mSeed == other.mSeed))
      return false;
    if (!(mPattern.equals(other.mPattern)))
      return false;
    if (!(mResolution == other.mResolution))
      return false;
    if (!(mSilentCorrection == other.mSilentCorrection))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mDonePauseTimeMs;
      r = r * 37 + mQuiescentChordMs;
      r = r * 37 + (mViewRecentEdits ? 1 : 0);
      r = r * 37 + (mCreateChords ? 1 : 0);
      r = r * 37 + mHand.ordinal();
      r = r * 37 + mKey.ordinal();
      r = r * 37 + (mInspectBoxes ? 1 : 0);
      r = r * 37 + mSeed;
      r = r * 37 + mPattern.hashCode();
      r = r * 37 + mResolution;
      r = r * 37 + (mSilentCorrection ? 1 : 0);
      m__hashcode = r;
    }
    return r;
  }

  protected int mDonePauseTimeMs;
  protected int mQuiescentChordMs;
  protected boolean mViewRecentEdits;
  protected boolean mCreateChords;
  protected Hand mHand;
  protected KeySig mKey;
  protected boolean mInspectBoxes;
  protected int mSeed;
  protected String mPattern;
  protected int mResolution;
  protected boolean mSilentCorrection;
  protected int m__hashcode;

  public static final class Builder extends SightConfig {

    private Builder(SightConfig m) {
      mDonePauseTimeMs = m.mDonePauseTimeMs;
      mQuiescentChordMs = m.mQuiescentChordMs;
      mViewRecentEdits = m.mViewRecentEdits;
      mCreateChords = m.mCreateChords;
      mHand = m.mHand;
      mKey = m.mKey;
      mInspectBoxes = m.mInspectBoxes;
      mSeed = m.mSeed;
      mPattern = m.mPattern;
      mResolution = m.mResolution;
      mSilentCorrection = m.mSilentCorrection;
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
      r.mDonePauseTimeMs = mDonePauseTimeMs;
      r.mQuiescentChordMs = mQuiescentChordMs;
      r.mViewRecentEdits = mViewRecentEdits;
      r.mCreateChords = mCreateChords;
      r.mHand = mHand;
      r.mKey = mKey;
      r.mInspectBoxes = mInspectBoxes;
      r.mSeed = mSeed;
      r.mPattern = mPattern;
      r.mResolution = mResolution;
      r.mSilentCorrection = mSilentCorrection;
      return r;
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

    public Builder createChords(boolean x) {
      mCreateChords = x;
      return this;
    }

    public Builder hand(Hand x) {
      mHand = (x == null) ? Hand.DEFAULT_INSTANCE : x;
      return this;
    }

    public Builder key(KeySig x) {
      mKey = (x == null) ? KeySig.DEFAULT_INSTANCE : x;
      return this;
    }

    public Builder inspectBoxes(boolean x) {
      mInspectBoxes = x;
      return this;
    }

    public Builder seed(int x) {
      mSeed = x;
      return this;
    }

    public Builder pattern(String x) {
      mPattern = (x == null) ? "" : x;
      return this;
    }

    public Builder resolution(int x) {
      mResolution = x;
      return this;
    }

    public Builder silentCorrection(boolean x) {
      mSilentCorrection = x;
      return this;
    }

  }

  public static final SightConfig DEFAULT_INSTANCE = new SightConfig();

  private SightConfig() {
    mDonePauseTimeMs = 1200;
    mQuiescentChordMs = 250;
    mHand = Hand.DEFAULT_INSTANCE;
    mKey = KeySig.DEFAULT_INSTANCE;
    mPattern = "";
    mResolution = 160;
  }

}
