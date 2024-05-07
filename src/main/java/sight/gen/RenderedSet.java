package sight.gen;

import js.data.AbstractData;
import js.json.JSMap;

public class RenderedSet implements AbstractData {

  public Hand hand() {
    return mHand;
  }

  public KeySig keySig() {
    return mKeySig;
  }

  public String notes() {
    return mNotes;
  }

  public int resolution() {
    return mResolution;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "hand";
  protected static final String _1 = "key_sig";
  protected static final String _2 = "notes";
  protected static final String _3 = "resolution";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mHand.toString().toLowerCase());
    m.putUnsafe(_1, mKeySig.toString().toLowerCase());
    m.putUnsafe(_2, mNotes);
    m.putUnsafe(_3, mResolution);
    return m;
  }

  @Override
  public RenderedSet build() {
    return this;
  }

  @Override
  public RenderedSet parse(Object obj) {
    return new RenderedSet((JSMap) obj);
  }

  private RenderedSet(JSMap m) {
    {
      String x = m.opt(_0, "");
      mHand = x.isEmpty() ? Hand.DEFAULT_INSTANCE : Hand.valueOf(x.toUpperCase());
    }
    {
      String x = m.opt(_1, "");
      mKeySig = x.isEmpty() ? KeySig.DEFAULT_INSTANCE : KeySig.valueOf(x.toUpperCase());
    }
    mNotes = m.opt(_2, "");
    mResolution = m.opt(_3, 300);
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof RenderedSet))
      return false;
    RenderedSet other = (RenderedSet) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mHand.equals(other.mHand)))
      return false;
    if (!(mKeySig.equals(other.mKeySig)))
      return false;
    if (!(mNotes.equals(other.mNotes)))
      return false;
    if (!(mResolution == other.mResolution))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mHand.ordinal();
      r = r * 37 + mKeySig.ordinal();
      r = r * 37 + mNotes.hashCode();
      r = r * 37 + mResolution;
      m__hashcode = r;
    }
    return r;
  }

  protected Hand mHand;
  protected KeySig mKeySig;
  protected String mNotes;
  protected int mResolution;
  protected int m__hashcode;

  public static final class Builder extends RenderedSet {

    private Builder(RenderedSet m) {
      mHand = m.mHand;
      mKeySig = m.mKeySig;
      mNotes = m.mNotes;
      mResolution = m.mResolution;
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
    public RenderedSet build() {
      RenderedSet r = new RenderedSet();
      r.mHand = mHand;
      r.mKeySig = mKeySig;
      r.mNotes = mNotes;
      r.mResolution = mResolution;
      return r;
    }

    public Builder hand(Hand x) {
      mHand = (x == null) ? Hand.DEFAULT_INSTANCE : x;
      return this;
    }

    public Builder keySig(KeySig x) {
      mKeySig = (x == null) ? KeySig.DEFAULT_INSTANCE : x;
      return this;
    }

    public Builder notes(String x) {
      mNotes = (x == null) ? "" : x;
      return this;
    }

    public Builder resolution(int x) {
      mResolution = x;
      return this;
    }

  }

  public static final RenderedSet DEFAULT_INSTANCE = new RenderedSet();

  private RenderedSet() {
    mHand = Hand.DEFAULT_INSTANCE;
    mKeySig = KeySig.DEFAULT_INSTANCE;
    mNotes = "";
    mResolution = 300;
  }

}