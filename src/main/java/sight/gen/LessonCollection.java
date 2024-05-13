package sight.gen;

import java.util.List;
import js.data.AbstractData;
import js.data.DataUtil;
import js.json.JSList;
import js.json.JSMap;

public class LessonCollection implements AbstractData {

  public List<Lesson> lessons() {
    return mLessons;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "lessons";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    {
      JSList j = new JSList();
      for (Lesson x : mLessons)
        j.add(x.toJson());
      m.put(_0, j);
    }
    return m;
  }

  @Override
  public LessonCollection build() {
    return this;
  }

  @Override
  public LessonCollection parse(Object obj) {
    return new LessonCollection((JSMap) obj);
  }

  private LessonCollection(JSMap m) {
    mLessons = DataUtil.parseListOfObjects(Lesson.DEFAULT_INSTANCE, m.optJSList(_0), false);
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof LessonCollection))
      return false;
    LessonCollection other = (LessonCollection) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mLessons.equals(other.mLessons)))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      for (Lesson x : mLessons)
        if (x != null)
          r = r * 37 + x.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected List<Lesson> mLessons;
  protected int m__hashcode;

  public static final class Builder extends LessonCollection {

    private Builder(LessonCollection m) {
      mLessons = DataUtil.mutableCopyOf(m.mLessons);
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
    public LessonCollection build() {
      LessonCollection r = new LessonCollection();
      r.mLessons = DataUtil.immutableCopyOf(mLessons);
      return r;
    }

    public Builder lessons(List<Lesson> x) {
      mLessons = DataUtil.mutableCopyOf((x == null) ? DataUtil.emptyList() : x);
      return this;
    }

  }

  public static final LessonCollection DEFAULT_INSTANCE = new LessonCollection();

  private LessonCollection() {
    mLessons = DataUtil.emptyList();
  }

}
