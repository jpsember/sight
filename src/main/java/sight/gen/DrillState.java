package sight.gen;

import java.util.Arrays;
import js.data.AbstractData;
import js.data.DataUtil;
import js.json.JSMap;

public class DrillState implements AbstractData {

  public DrillStatus status() {
    return mStatus;
  }

  public int[] icons() {
    return mIcons;
  }

  public int cursor() {
    return mCursor;
  }

  public RenderedNotes notes() {
    return mNotes;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "status";
  protected static final String _1 = "icons";
  protected static final String _2 = "cursor";
  protected static final String _3 = "notes";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mStatus.toString().toLowerCase());
    m.putUnsafe(_1, DataUtil.encodeBase64Maybe(mIcons));
    m.putUnsafe(_2, mCursor);
    m.putUnsafe(_3, mNotes.toJson());
    return m;
  }

  @Override
  public DrillState build() {
    return this;
  }

  @Override
  public DrillState parse(Object obj) {
    return new DrillState((JSMap) obj);
  }

  private DrillState(JSMap m) {
    {
      String x = m.opt(_0, "");
      mStatus = x.isEmpty() ? DrillStatus.DEFAULT_INSTANCE : DrillStatus.valueOf(x.toUpperCase());
    }
    {
      mIcons = DataUtil.EMPTY_INT_ARRAY;
      Object x = m.optUnsafe(_1);
      if (x != null) {
        mIcons = DataUtil.parseIntsFromArrayOrBase64(x);
      }
    }
    mCursor = m.opt(_2, 0);
    {
      mNotes = RenderedNotes.DEFAULT_INSTANCE;
      Object x = m.optUnsafe(_3);
      if (x != null) {
        mNotes = RenderedNotes.DEFAULT_INSTANCE.parse(x);
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
    if (object == null || !(object instanceof DrillState))
      return false;
    DrillState other = (DrillState) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mStatus.equals(other.mStatus)))
      return false;
    if (!(Arrays.equals(mIcons, other.mIcons)))
      return false;
    if (!(mCursor == other.mCursor))
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
      r = r * 37 + mStatus.ordinal();
      r = r * 37 + Arrays.hashCode(mIcons);
      r = r * 37 + mCursor;
      r = r * 37 + mNotes.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected DrillStatus mStatus;
  protected int[] mIcons;
  protected int mCursor;
  protected RenderedNotes mNotes;
  protected int m__hashcode;

  public static final class Builder extends DrillState {

    private Builder(DrillState m) {
      mStatus = m.mStatus;
      mIcons = m.mIcons;
      mCursor = m.mCursor;
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
    public DrillState build() {
      DrillState r = new DrillState();
      r.mStatus = mStatus;
      r.mIcons = mIcons;
      r.mCursor = mCursor;
      r.mNotes = mNotes;
      return r;
    }

    public Builder status(DrillStatus x) {
      mStatus = (x == null) ? DrillStatus.DEFAULT_INSTANCE : x;
      return this;
    }

    public Builder icons(int[] x) {
      mIcons = (x == null) ? DataUtil.EMPTY_INT_ARRAY : x;
      return this;
    }

    public Builder cursor(int x) {
      mCursor = x;
      return this;
    }

    public Builder notes(RenderedNotes x) {
      mNotes = (x == null) ? RenderedNotes.DEFAULT_INSTANCE : x.build();
      return this;
    }

  }

  public static final DrillState DEFAULT_INSTANCE = new DrillState();

  private DrillState() {
    mStatus = DrillStatus.DEFAULT_INSTANCE;
    mIcons = DataUtil.EMPTY_INT_ARRAY;
    mNotes = RenderedNotes.DEFAULT_INSTANCE;
  }

}
