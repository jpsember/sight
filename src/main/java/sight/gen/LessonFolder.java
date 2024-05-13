package sight.gen;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import js.data.AbstractData;
import js.data.DataUtil;
import js.json.JSMap;

public class LessonFolder implements AbstractData {

  public Map<Integer, LessonStat> stats() {
    return mStats;
  }

  public int[] activeLessons() {
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
      for (Map.Entry<Integer, LessonStat> e : mStats.entrySet())
        j.put(e.getKey().toString(), e.getValue().toJson());
      m.put(_0, j);
    }
    m.putUnsafe(_1, DataUtil.encodeBase64Maybe(mActiveLessons));
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
          Map<Integer, LessonStat> mp = new ConcurrentHashMap<>();
          for (Map.Entry<String, Object> e : m2.wrappedMap().entrySet())
            mp.put(Integer.parseInt(e.getKey()), LessonStat.DEFAULT_INSTANCE.parse((JSMap) e.getValue()));
          mStats = mp;
        }
      }
    }
    {
      mActiveLessons = DataUtil.EMPTY_INT_ARRAY;
      Object x = m.optUnsafe(_1);
      if (x != null) {
        mActiveLessons = DataUtil.parseIntsFromArrayOrBase64(x);
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
    if (!(Arrays.equals(mActiveLessons, other.mActiveLessons)))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mStats.hashCode();
      r = r * 37 + Arrays.hashCode(mActiveLessons);
      m__hashcode = r;
    }
    return r;
  }

  protected Map<Integer, LessonStat> mStats;
  protected int[] mActiveLessons;
  protected int m__hashcode;

  public static final class Builder extends LessonFolder {

    private Builder(LessonFolder m) {
      mStats = DataUtil.mutableCopyOf(m.mStats);
      mActiveLessons = m.mActiveLessons;
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
      r.mActiveLessons = mActiveLessons;
      return r;
    }

    public Builder stats(Map<Integer, LessonStat> x) {
      mStats = DataUtil.mutableCopyOf((x == null) ? DataUtil.emptyMap() : x);
      return this;
    }

    public Builder activeLessons(int[] x) {
      mActiveLessons = (x == null) ? DataUtil.EMPTY_INT_ARRAY : x;
      return this;
    }

  }

  public static final LessonFolder DEFAULT_INSTANCE = new LessonFolder();

  private LessonFolder() {
    mStats = DataUtil.emptyMap();
    mActiveLessons = DataUtil.EMPTY_INT_ARRAY;
  }

}
