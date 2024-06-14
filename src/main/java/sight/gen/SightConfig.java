package sight.gen;

import js.data.AbstractData;
import js.json.JSMap;

public class SightConfig implements AbstractData {

  public int retryLessonDurationMs() {
    return mRetryLessonDurationMs;
  }

  public int doneLessonDurationMs() {
    return mDoneLessonDurationMs;
  }

  public int doneSessionDurationMs() {
    return mDoneSessionDurationMs;
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

  public boolean repeat() {
    return mRepeat;
  }

  public String lessonId() {
    return mLessonId;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "retry_lesson_duration_ms";
  protected static final String _1 = "done_lesson_duration_ms";
  protected static final String _2 = "done_session_duration_ms";
  protected static final String _3 = "quiescent_chord_ms";
  protected static final String _4 = "view_recent_edits";
  protected static final String _5 = "create_chords";
  protected static final String _6 = "hand";
  protected static final String _7 = "key";
  protected static final String _8 = "inspect_boxes";
  protected static final String _9 = "seed";
  protected static final String _10 = "pattern";
  protected static final String _11 = "resolution";
  protected static final String _12 = "silent_correction";
  protected static final String _13 = "repeat";
  protected static final String _14 = "lesson_id";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mRetryLessonDurationMs);
    m.putUnsafe(_1, mDoneLessonDurationMs);
    m.putUnsafe(_2, mDoneSessionDurationMs);
    m.putUnsafe(_3, mQuiescentChordMs);
    m.putUnsafe(_4, mViewRecentEdits);
    m.putUnsafe(_5, mCreateChords);
    m.putUnsafe(_6, mHand.toString().toLowerCase());
    m.putUnsafe(_7, mKey.toString().toLowerCase());
    m.putUnsafe(_8, mInspectBoxes);
    m.putUnsafe(_9, mSeed);
    m.putUnsafe(_10, mPattern);
    m.putUnsafe(_11, mResolution);
    m.putUnsafe(_12, mSilentCorrection);
    m.putUnsafe(_13, mRepeat);
    m.putUnsafe(_14, mLessonId);
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
    mRetryLessonDurationMs = m.opt(_0, 1200);
    mDoneLessonDurationMs = m.opt(_1, 400);
    mDoneSessionDurationMs = m.opt(_2, 3600);
    mQuiescentChordMs = m.opt(_3, 250);
    mViewRecentEdits = m.opt(_4, false);
    mCreateChords = m.opt(_5, false);
    {
      String x = m.opt(_6, "");
      mHand = x.isEmpty() ? Hand.DEFAULT_INSTANCE : Hand.valueOf(x.toUpperCase());
    }
    {
      String x = m.opt(_7, "");
      mKey = x.isEmpty() ? KeySig.DEFAULT_INSTANCE : KeySig.valueOf(x.toUpperCase());
    }
    mInspectBoxes = m.opt(_8, false);
    mSeed = m.opt(_9, 0);
    mPattern = m.opt(_10, "");
    mResolution = m.opt(_11, 160);
    mSilentCorrection = m.opt(_12, false);
    mRepeat = m.opt(_13, false);
    mLessonId = m.opt(_14, "");
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
    if (!(mRetryLessonDurationMs == other.mRetryLessonDurationMs))
      return false;
    if (!(mDoneLessonDurationMs == other.mDoneLessonDurationMs))
      return false;
    if (!(mDoneSessionDurationMs == other.mDoneSessionDurationMs))
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
    if (!(mRepeat == other.mRepeat))
      return false;
    if (!(mLessonId.equals(other.mLessonId)))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mRetryLessonDurationMs;
      r = r * 37 + mDoneLessonDurationMs;
      r = r * 37 + mDoneSessionDurationMs;
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
      r = r * 37 + (mRepeat ? 1 : 0);
      r = r * 37 + mLessonId.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected int mRetryLessonDurationMs;
  protected int mDoneLessonDurationMs;
  protected int mDoneSessionDurationMs;
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
  protected boolean mRepeat;
  protected String mLessonId;
  protected int m__hashcode;

  public static final class Builder extends SightConfig {

    private Builder(SightConfig m) {
      mRetryLessonDurationMs = m.mRetryLessonDurationMs;
      mDoneLessonDurationMs = m.mDoneLessonDurationMs;
      mDoneSessionDurationMs = m.mDoneSessionDurationMs;
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
      mRepeat = m.mRepeat;
      mLessonId = m.mLessonId;
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
      r.mRetryLessonDurationMs = mRetryLessonDurationMs;
      r.mDoneLessonDurationMs = mDoneLessonDurationMs;
      r.mDoneSessionDurationMs = mDoneSessionDurationMs;
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
      r.mRepeat = mRepeat;
      r.mLessonId = mLessonId;
      return r;
    }

    public Builder retryLessonDurationMs(int x) {
      mRetryLessonDurationMs = x;
      return this;
    }

    public Builder doneLessonDurationMs(int x) {
      mDoneLessonDurationMs = x;
      return this;
    }

    public Builder doneSessionDurationMs(int x) {
      mDoneSessionDurationMs = x;
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

    public Builder repeat(boolean x) {
      mRepeat = x;
      return this;
    }

    public Builder lessonId(String x) {
      mLessonId = (x == null) ? "" : x;
      return this;
    }

  }

  public static final SightConfig DEFAULT_INSTANCE = new SightConfig();

  private SightConfig() {
    mRetryLessonDurationMs = 1200;
    mDoneLessonDurationMs = 400;
    mDoneSessionDurationMs = 3600;
    mQuiescentChordMs = 250;
    mHand = Hand.DEFAULT_INSTANCE;
    mKey = KeySig.DEFAULT_INSTANCE;
    mPattern = "";
    mResolution = 160;
    mLessonId = "";
  }

}
