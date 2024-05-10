package sight.gen;

import js.data.AbstractData;
import js.geometry.IRect;
import js.json.JSMap;

public class GuiState implements AbstractData {

  public IRect frameBounds() {
    return mFrameBounds;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "frame_bounds";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mFrameBounds.toJson());
    return m;
  }

  @Override
  public GuiState build() {
    return this;
  }

  @Override
  public GuiState parse(Object obj) {
    return new GuiState((JSMap) obj);
  }

  private GuiState(JSMap m) {
    {
      mFrameBounds = IRect.DEFAULT_INSTANCE;
      Object x = m.optUnsafe(_0);
      if (x != null) {
        mFrameBounds = IRect.DEFAULT_INSTANCE.parse(x);
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
    if (object == null || !(object instanceof GuiState))
      return false;
    GuiState other = (GuiState) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mFrameBounds.equals(other.mFrameBounds)))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mFrameBounds.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected IRect mFrameBounds;
  protected int m__hashcode;

  public static final class Builder extends GuiState {

    private Builder(GuiState m) {
      mFrameBounds = m.mFrameBounds;
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
    public GuiState build() {
      GuiState r = new GuiState();
      r.mFrameBounds = mFrameBounds;
      return r;
    }

    public Builder frameBounds(IRect x) {
      mFrameBounds = (x == null) ? IRect.DEFAULT_INSTANCE : x.build();
      return this;
    }

  }

  public static final GuiState DEFAULT_INSTANCE = new GuiState();

  private GuiState() {
    mFrameBounds = IRect.DEFAULT_INSTANCE;
  }

}
