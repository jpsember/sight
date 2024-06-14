package sight.gen;

import java.util.List;
import js.data.AbstractData;
import js.data.DataUtil;
import js.json.JSList;
import js.json.JSMap;

@Deprecated
public class Session implements AbstractData {

  public List<String> lessonIds() {
    return mLessonIds;
  }

  public String lastLessonId() {
    return mLastLessonId;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "lesson_ids";
  protected static final String _1 = "last_lesson_id";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    {
      JSList j = new JSList();
      for (String x : mLessonIds)
        j.add(x);
      m.put(_0, j);
    }
    m.putUnsafe(_1, mLastLessonId);
    return m;
  }

  @Override
  public Session build() {
    return this;
  }

  @Override
  public Session parse(Object obj) {
    return new Session((JSMap) obj);
  }

  private Session(JSMap m) {
    mLessonIds = DataUtil.parseListOfObjects(m.optJSList(_0), false);
    mLastLessonId = m.opt(_1, "");
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof Session))
      return false;
    Session other = (Session) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mLessonIds.equals(other.mLessonIds)))
      return false;
    if (!(mLastLessonId.equals(other.mLastLessonId)))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      for (String x : mLessonIds)
        if (x != null)
          r = r * 37 + x.hashCode();
      r = r * 37 + mLastLessonId.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected List<String> mLessonIds;
  protected String mLastLessonId;
  protected int m__hashcode;

  public static final class Builder extends Session {

    private Builder(Session m) {
      mLessonIds = DataUtil.mutableCopyOf(m.mLessonIds);
      mLastLessonId = m.mLastLessonId;
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
    public Session build() {
      Session r = new Session();
      r.mLessonIds = DataUtil.immutableCopyOf(mLessonIds);
      r.mLastLessonId = mLastLessonId;
      return r;
    }

    public Builder lessonIds(List<String> x) {
      mLessonIds = DataUtil.mutableCopyOf((x == null) ? DataUtil.emptyList() : x);
      return this;
    }

    public Builder lastLessonId(String x) {
      mLastLessonId = (x == null) ? "" : x;
      return this;
    }

  }

  public static final Session DEFAULT_INSTANCE = new Session();

  private Session() {
    mLessonIds = DataUtil.emptyList();
    mLastLessonId = "";
  }

}
