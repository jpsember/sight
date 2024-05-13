package sight.gen;

import js.data.AbstractData;
import js.json.JSMap;

public class LessonStat implements AbstractData {

  public int frequency() {
    return mFrequency;
  }

  public int correct() {
    return mCorrect;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "frequency";
  protected static final String _1 = "correct";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mFrequency);
    m.putUnsafe(_1, mCorrect);
    return m;
  }

  @Override
  public LessonStat build() {
    return this;
  }

  @Override
  public LessonStat parse(Object obj) {
    return new LessonStat((JSMap) obj);
  }

  private LessonStat(JSMap m) {
    mFrequency = m.opt(_0, 0);
    mCorrect = m.opt(_1, 0);
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof LessonStat))
      return false;
    LessonStat other = (LessonStat) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mFrequency == other.mFrequency))
      return false;
    if (!(mCorrect == other.mCorrect))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mFrequency;
      r = r * 37 + mCorrect;
      m__hashcode = r;
    }
    return r;
  }

  protected int mFrequency;
  protected int mCorrect;
  protected int m__hashcode;

  public static final class Builder extends LessonStat {

    private Builder(LessonStat m) {
      mFrequency = m.mFrequency;
      mCorrect = m.mCorrect;
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
    public LessonStat build() {
      LessonStat r = new LessonStat();
      r.mFrequency = mFrequency;
      r.mCorrect = mCorrect;
      return r;
    }

    public Builder frequency(int x) {
      mFrequency = x;
      return this;
    }

    public Builder correct(int x) {
      mCorrect = x;
      return this;
    }

  }

  public static final LessonStat DEFAULT_INSTANCE = new LessonStat();

  private LessonStat() {
  }

}
