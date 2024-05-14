package sight.gen;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import js.data.AbstractData;
import js.data.DataUtil;
import js.json.JSMap;

public class LessonFolder implements AbstractData {

  public Map<String, LessonStat> stats() {
    return mStats;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "stats";

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
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mStats.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected Map<String, LessonStat> mStats;
  protected int m__hashcode;

  public static final class Builder extends LessonFolder {

    private Builder(LessonFolder m) {
      mStats = DataUtil.mutableCopyOf(m.mStats);
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
      return r;
    }

    public Builder stats(Map<String, LessonStat> x) {
      mStats = DataUtil.mutableCopyOf((x == null) ? DataUtil.emptyMap() : x);
      return this;
    }

  }

  public static final LessonFolder DEFAULT_INSTANCE = new LessonFolder();

  private LessonFolder() {
    mStats = DataUtil.emptyMap();
  }

}
