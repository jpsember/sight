package sight.gen;

import js.data.AbstractData;
import js.json.JSMap;

public class SightConfig implements AbstractData {

  public KeySig keySig() {
    return mKeySig;
  }

  public Hand hand() {
    return mHand;
  }

  public String notes() {
    return mNotes;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "key_sig";
  protected static final String _1 = "hand";
  protected static final String _2 = "notes";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mKeySig.toString().toLowerCase());
    m.putUnsafe(_1, mHand.toString().toLowerCase());
    m.putUnsafe(_2, mNotes);
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
      String x = m.opt(_0, "");
      mKeySig = x.isEmpty() ? KeySig.DEFAULT_INSTANCE : KeySig.valueOf(x.toUpperCase());
    }
    {
      String x = m.opt(_1, "");
      mHand = x.isEmpty() ? Hand.DEFAULT_INSTANCE : Hand.valueOf(x.toUpperCase());
    }
    mNotes = m.opt(_2, "");
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
    if (!(mKeySig.equals(other.mKeySig)))
      return false;
    if (!(mHand.equals(other.mHand)))
      return false;
    if (!(mNotes.equals(other.mNotes)))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mKeySig.ordinal();
      r = r * 37 + mHand.ordinal();
      r = r * 37 + mNotes.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected KeySig mKeySig;
  protected Hand mHand;
  protected String mNotes;
  protected int m__hashcode;

  public static final class Builder extends SightConfig {

    private Builder(SightConfig m) {
      mKeySig = m.mKeySig;
      mHand = m.mHand;
      mNotes = m.mNotes;
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
      r.mKeySig = mKeySig;
      r.mHand = mHand;
      r.mNotes = mNotes;
      return r;
    }

    public Builder keySig(KeySig x) {
      mKeySig = (x == null) ? KeySig.DEFAULT_INSTANCE : x;
      return this;
    }

    public Builder hand(Hand x) {
      mHand = (x == null) ? Hand.DEFAULT_INSTANCE : x;
      return this;
    }

    public Builder notes(String x) {
      mNotes = (x == null) ? "" : x;
      return this;
    }

  }

  public static final SightConfig DEFAULT_INSTANCE = new SightConfig();

  private SightConfig() {
    mKeySig = KeySig.DEFAULT_INSTANCE;
    mHand = Hand.DEFAULT_INSTANCE;
    mNotes = "";
  }

}
