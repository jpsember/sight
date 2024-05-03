package sight.gen;

import java.util.Arrays;
import js.data.AbstractData;
import js.data.DataUtil;
import js.json.JSMap;

public class Chord implements AbstractData {

  public int[] keyNumbers() {
    return mKeyNumbers;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "key_numbers";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, DataUtil.encodeBase64Maybe(mKeyNumbers));
    return m;
  }

  @Override
  public Chord build() {
    return this;
  }

  @Override
  public Chord parse(Object obj) {
    return new Chord((JSMap) obj);
  }

  private Chord(JSMap m) {
    {
      mKeyNumbers = DataUtil.EMPTY_INT_ARRAY;
      Object x = m.optUnsafe(_0);
      if (x != null) {
        mKeyNumbers = DataUtil.parseIntsFromArrayOrBase64(x);
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
    if (object == null || !(object instanceof Chord))
      return false;
    Chord other = (Chord) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(Arrays.equals(mKeyNumbers, other.mKeyNumbers)))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + Arrays.hashCode(mKeyNumbers);
      m__hashcode = r;
    }
    return r;
  }

  protected int[] mKeyNumbers;
  protected int m__hashcode;

  public static final class Builder extends Chord {

    private Builder(Chord m) {
      mKeyNumbers = m.mKeyNumbers;
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
    public Chord build() {
      Chord r = new Chord();
      r.mKeyNumbers = mKeyNumbers;
      return r;
    }

    public Builder keyNumbers(int[] x) {
      mKeyNumbers = (x == null) ? DataUtil.EMPTY_INT_ARRAY : x;
      return this;
    }

  }

  public static final Chord DEFAULT_INSTANCE = new Chord();

  private Chord() {
    mKeyNumbers = DataUtil.EMPTY_INT_ARRAY;
  }

}
