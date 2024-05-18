package sight.gen;

import java.util.Arrays;
import js.data.AbstractData;
import js.data.DataUtil;
import js.json.JSMap;

public class LessonState implements AbstractData {

  public LessonStatus status() {
    return mStatus;
  }

  public String lessonId() {
    return mLessonId;
  }

  public int[] icons() {
    return mIcons;
  }

  public int cursor() {
    return mCursor;
  }

  public int questionCount() {
    return mQuestionCount;
  }

  public int correctCount() {
    return mCorrectCount;
  }

  public long timeMs() {
    return mTimeMs;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "status";
  protected static final String _1 = "lesson_id";
  protected static final String _2 = "icons";
  protected static final String _3 = "cursor";
  protected static final String _4 = "question_count";
  protected static final String _5 = "correct_count";
  protected static final String _6 = "time_ms";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mStatus.toString().toLowerCase());
    m.putUnsafe(_1, mLessonId);
    m.putUnsafe(_2, DataUtil.encodeBase64Maybe(mIcons));
    m.putUnsafe(_3, mCursor);
    m.putUnsafe(_4, mQuestionCount);
    m.putUnsafe(_5, mCorrectCount);
    m.putUnsafe(_6, mTimeMs);
    return m;
  }

  @Override
  public LessonState build() {
    return this;
  }

  @Override
  public LessonState parse(Object obj) {
    return new LessonState((JSMap) obj);
  }

  private LessonState(JSMap m) {
    {
      String x = m.opt(_0, "");
      mStatus = x.isEmpty() ? LessonStatus.DEFAULT_INSTANCE : LessonStatus.valueOf(x.toUpperCase());
    }
    mLessonId = m.opt(_1, "");
    {
      mIcons = DataUtil.EMPTY_INT_ARRAY;
      Object x = m.optUnsafe(_2);
      if (x != null) {
        mIcons = DataUtil.parseIntsFromArrayOrBase64(x);
      }
    }
    mCursor = m.opt(_3, 0);
    mQuestionCount = m.opt(_4, 0);
    mCorrectCount = m.opt(_5, 0);
    mTimeMs = m.opt(_6, 0L);
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof LessonState))
      return false;
    LessonState other = (LessonState) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mStatus.equals(other.mStatus)))
      return false;
    if (!(mLessonId.equals(other.mLessonId)))
      return false;
    if (!(Arrays.equals(mIcons, other.mIcons)))
      return false;
    if (!(mCursor == other.mCursor))
      return false;
    if (!(mQuestionCount == other.mQuestionCount))
      return false;
    if (!(mCorrectCount == other.mCorrectCount))
      return false;
    if (!(mTimeMs == other.mTimeMs))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mStatus.ordinal();
      r = r * 37 + mLessonId.hashCode();
      r = r * 37 + Arrays.hashCode(mIcons);
      r = r * 37 + mCursor;
      r = r * 37 + mQuestionCount;
      r = r * 37 + mCorrectCount;
      r = r * 37 + (int)mTimeMs;
      m__hashcode = r;
    }
    return r;
  }

  protected LessonStatus mStatus;
  protected String mLessonId;
  protected int[] mIcons;
  protected int mCursor;
  protected int mQuestionCount;
  protected int mCorrectCount;
  protected long mTimeMs;
  protected int m__hashcode;

  public static final class Builder extends LessonState {

    private Builder(LessonState m) {
      mStatus = m.mStatus;
      mLessonId = m.mLessonId;
      mIcons = m.mIcons;
      mCursor = m.mCursor;
      mQuestionCount = m.mQuestionCount;
      mCorrectCount = m.mCorrectCount;
      mTimeMs = m.mTimeMs;
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
    public LessonState build() {
      LessonState r = new LessonState();
      r.mStatus = mStatus;
      r.mLessonId = mLessonId;
      r.mIcons = mIcons;
      r.mCursor = mCursor;
      r.mQuestionCount = mQuestionCount;
      r.mCorrectCount = mCorrectCount;
      r.mTimeMs = mTimeMs;
      return r;
    }

    public Builder status(LessonStatus x) {
      mStatus = (x == null) ? LessonStatus.DEFAULT_INSTANCE : x;
      return this;
    }

    public Builder lessonId(String x) {
      mLessonId = (x == null) ? "" : x;
      return this;
    }

    public Builder icons(int[] x) {
      mIcons = (x == null) ? DataUtil.EMPTY_INT_ARRAY : x;
      return this;
    }

    public Builder cursor(int x) {
      mCursor = x;
      return this;
    }

    public Builder questionCount(int x) {
      mQuestionCount = x;
      return this;
    }

    public Builder correctCount(int x) {
      mCorrectCount = x;
      return this;
    }

    public Builder timeMs(long x) {
      mTimeMs = x;
      return this;
    }

  }

  public static final LessonState DEFAULT_INSTANCE = new LessonState();

  private LessonState() {
    mStatus = LessonStatus.DEFAULT_INSTANCE;
    mLessonId = "";
    mIcons = DataUtil.EMPTY_INT_ARRAY;
  }

}
