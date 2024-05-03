package sight.gen;

import js.data.AbstractData;
import js.geometry.IRect;
import js.json.JSMap;

public class RenderedChord implements AbstractData {

  public Chord chord() {
    return mChord;
  }

  public IRect rect() {
    return mRect;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "chord";
  protected static final String _1 = "rect";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mChord.toJson());
    m.putUnsafe(_1, mRect.toJson());
    return m;
  }

  @Override
  public RenderedChord build() {
    return this;
  }

  @Override
  public RenderedChord parse(Object obj) {
    return new RenderedChord((JSMap) obj);
  }

  private RenderedChord(JSMap m) {
    {
      mChord = Chord.DEFAULT_INSTANCE;
      Object x = m.optUnsafe(_0);
      if (x != null) {
        mChord = Chord.DEFAULT_INSTANCE.parse(x);
      }
    }
    {
      mRect = IRect.DEFAULT_INSTANCE;
      Object x = m.optUnsafe(_1);
      if (x != null) {
        mRect = IRect.DEFAULT_INSTANCE.parse(x);
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
    if (object == null || !(object instanceof RenderedChord))
      return false;
    RenderedChord other = (RenderedChord) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mChord.equals(other.mChord)))
      return false;
    if (!(mRect.equals(other.mRect)))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mChord.hashCode();
      r = r * 37 + mRect.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected Chord mChord;
  protected IRect mRect;
  protected int m__hashcode;

  public static final class Builder extends RenderedChord {

    private Builder(RenderedChord m) {
      mChord = m.mChord;
      mRect = m.mRect;
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
    public RenderedChord build() {
      RenderedChord r = new RenderedChord();
      r.mChord = mChord;
      r.mRect = mRect;
      return r;
    }

    public Builder chord(Chord x) {
      mChord = (x == null) ? Chord.DEFAULT_INSTANCE : x.build();
      return this;
    }

    public Builder rect(IRect x) {
      mRect = (x == null) ? IRect.DEFAULT_INSTANCE : x.build();
      return this;
    }

  }

  public static final RenderedChord DEFAULT_INSTANCE = new RenderedChord();

  private RenderedChord() {
    mChord = Chord.DEFAULT_INSTANCE;
    mRect = IRect.DEFAULT_INSTANCE;
  }

}
