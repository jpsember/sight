package sight.gen;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import js.data.AbstractData;
import js.data.DataUtil;
import js.json.JSList;
import js.json.JSMap;

public class LessonFolder implements AbstractData {

  public Map<String, LessonStat> stats() {
    return mStats;
  }

  @Deprecated
  public List<String> activeLessons() {
    return mActiveLessons;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "stats";
  protected static final String _1 = "active_lessons";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    {
      JSMap j = new JSMap();
      for (Map.Entry<String, LessonStat> e : mStats.entrySet())
        j.put(e.getKey(), e.getValue().toJson());
      m.put(_0, j);
    }
    {
      JSList j = new JSList();
      for (String x : mActiveLessons)
        j.add(x);
      m.put(_1, j);
    }
    return m;
  }

  @Override
  public LessonFolder build() {
    return this;
  }

  @Override
  public LessonFolder parse(Object obj) {
    return new LessonFolder((JSMap) obj);
  }

  private LessonFolder(JSMap m) {
    {
      mStats = DataUtil.emptyMap();
      {
        JSMap m2 = m.optJSMap("stats");
        if (m2 != null && !m2.isEmpty()) {
          Map<String, LessonStat> mp = new ConcurrentHashMap<>();
          for (Map.Entry<String, Object> e : m2.wrappedMap().entrySet())
            mp.put(e.getKey(), LessonStat.DEFAULT_INSTANCE.parse((JSMap) e.getValue()));
          mStats = mp;
        }
      }
    }
    mActiveLessons = DataUtil.parseListOfObjects(m.optJSList(_1), false);
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof LessonFolder))
      return false;
    LessonFolder other = (LessonFolder) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mStats.equals(other.mStats)))
      return false;
    if (!(mActiveLessons.equals(other.mActiveLessons)))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mStats.hashCode();
      for (String x : mActiveLessons)
        if (x != null)
          r = r * 37 + x.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected Map<String, LessonStat> mStats;
  protected List<String> mActiveLessons;
  protected int m__hashcode;

  public static final class Builder extends LessonFolder {

    private Builder(LessonFolder m) {
      mStats = DataUtil.mutableCopyOf(m.mStats);
      mActiveLessons = DataUtil.mutableCopyOf(m.mActiveLessons);
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
    public LessonFolder build() {
      LessonFolder r = new LessonFolder();
      r.mStats = DataUtil.immutableCopyOf(mStats);
      r.mActiveLessons = DataUtil.immutableCopyOf(mActiveLessons);
      return r;
    }

    public Builder stats(Map<String, LessonStat> x) {
      mStats = DataUtil.mutableCopyOf((x == null) ? DataUtil.emptyMap() : x);
      return this;
    }

    @Deprecated
    public Builder activeLessons(List<String> x) {
      mActiveLessons = DataUtil.mutableCopyOf((x == null) ? DataUtil.emptyList() : x);
      return this;
    }

  }

  public static final LessonFolder DEFAULT_INSTANCE = new LessonFolder();

  private LessonFolder() {
    mStats = DataUtil.emptyMap();
    mActiveLessons = DataUtil.emptyList();
  }

}
