package sight.gen;

import js.data.AbstractData;
import js.json.JSMap;

public class ChordSet implements AbstractData {

  public String name() {
    return mName;
  }

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

  protected static final String _0 = "name";
  protected static final String _1 = "key_sig";
  protected static final String _2 = "hand";
  protected static final String _3 = "notes";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mName);
    m.putUnsafe(_1, mKeySig.toString().toLowerCase());
    m.putUnsafe(_2, mHand.toString().toLowerCase());
    m.putUnsafe(_3, mNotes);
    return m;
  }

  @Override
  public ChordSet build() {
    return this;
  }

  @Override
  public ChordSet parse(Object obj) {
    return new ChordSet((JSMap) obj);
  }

  private ChordSet(JSMap m) {
    mName = m.opt(_0, "");
    {
      String x = m.opt(_1, "");
      mKeySig = x.isEmpty() ? KeySig.DEFAULT_INSTANCE : KeySig.valueOf(x.toUpperCase());
    }
    {
      String x = m.opt(_2, "");
      mHand = x.isEmpty() ? Hand.DEFAULT_INSTANCE : Hand.valueOf(x.toUpperCase());
    }
    mNotes = m.opt(_3, "");
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof ChordSet))
      return false;
    ChordSet other = (ChordSet) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mName.equals(other.mName)))
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
      r = r * 37 + mName.hashCode();
      r = r * 37 + mKeySig.ordinal();
      r = r * 37 + mHand.ordinal();
      r = r * 37 + mNotes.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected String mName;
  protected KeySig mKeySig;
  protected Hand mHand;
  protected String mNotes;
  protected int m__hashcode;

  public static final class Builder extends ChordSet {

    private Builder(ChordSet m) {
      mName = m.mName;
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
    public ChordSet build() {
      ChordSet r = new ChordSet();
      r.mName = mName;
      r.mKeySig = mKeySig;
      r.mHand = mHand;
      r.mNotes = mNotes;
      return r;
    }

    public Builder name(String x) {
      mName = (x == null) ? "" : x;
      return this;
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

  public static final ChordSet DEFAULT_INSTANCE = new ChordSet();

  private ChordSet() {
    mName = "";
    mKeySig = KeySig.DEFAULT_INSTANCE;
    mHand = Hand.DEFAULT_INSTANCE;
    mNotes = "";
  }

}
