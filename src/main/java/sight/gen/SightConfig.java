package sight.gen;

import java.io.File;
import js.data.AbstractData;
import js.file.Files;
import js.json.JSMap;

public class SightConfig implements AbstractData {

  public File chords() {
    return mChords;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "chords";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mChords.toString());
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
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mChords.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected File mChords;
  protected int m__hashcode;

  public static final class Builder extends SightConfig {

    private Builder(SightConfig m) {
      mChords = m.mChords;
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
      return r;
    }

    public Builder chords(File x) {
      mChords = (x == null) ? Files.DEFAULT : x;
      return this;
    }

  }

  public static final SightConfig DEFAULT_INSTANCE = new SightConfig();

  private SightConfig() {
    mChords = Files.DEFAULT;
  }

}
