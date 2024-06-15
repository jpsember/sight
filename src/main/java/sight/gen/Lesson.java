package sight.gen;

import js.data.AbstractData;
import js.json.JSMap;

public class Lesson implements AbstractData {

  public String id() {
    return mId;
  }

  public String description() {
    return mDescription;
  }

  public KeySig keySig() {
    return mKeySig;
  }

  public String notes() {
    return mNotes;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "id";
  protected static final String _1 = "description";
  protected static final String _2 = "key_sig";
  protected static final String _3 = "notes";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mId);
    m.putUnsafe(_1, mDescription);
    m.putUnsafe(_2, mKeySig.toString().toLowerCase());
    m.putUnsafe(_3, mNotes);
    return m;
  }

  @Override
  public Lesson build() {
    return this;
  }

  @Override
  public Lesson parse(Object obj) {
    return new Lesson((JSMap) obj);
  }

  private Lesson(JSMap m) {
    mId = m.opt(_0, "");
    mDescription = m.opt(_1, "");
    {
      String x = m.opt(_2, "");
      mKeySig = x.isEmpty() ? KeySig.DEFAULT_INSTANCE : KeySig.valueOf(x.toUpperCase());
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
    if (object == null || !(object instanceof Lesson))
      return false;
    Lesson other = (Lesson) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mId.equals(other.mId)))
      return false;
    if (!(mDescription.equals(other.mDescription)))
      return false;
    if (!(mKeySig.equals(other.mKeySig)))
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
      r = r * 37 + mId.hashCode();
      r = r * 37 + mDescription.hashCode();
      r = r * 37 + mKeySig.ordinal();
      r = r * 37 + mNotes.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected String mId;
  protected String mDescription;
  protected KeySig mKeySig;
  protected String mNotes;
  protected int m__hashcode;

  public static final class Builder extends Lesson {

    private Builder(Lesson m) {
      mId = m.mId;
      mDescription = m.mDescription;
      mKeySig = m.mKeySig;
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
    public Lesson build() {
      Lesson r = new Lesson();
      r.mId = mId;
      r.mDescription = mDescription;
      r.mKeySig = mKeySig;
      r.mNotes = mNotes;
      return r;
    }

    public Builder id(String x) {
      mId = (x == null) ? "" : x;
      return this;
    }

    public Builder description(String x) {
      mDescription = (x == null) ? "" : x;
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

  }

  public static final Lesson DEFAULT_INSTANCE = new Lesson();

  private Lesson() {
    mId = "";
    mDescription = "";
    mKeySig = KeySig.DEFAULT_INSTANCE;
    mNotes = "";
  }

}
