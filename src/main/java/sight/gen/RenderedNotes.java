package sight.gen;

import java.io.File;
import java.util.List;
import js.data.AbstractData;
import js.data.DataUtil;
import js.file.Files;
import js.geometry.IRect;
import js.json.JSList;
import js.json.JSMap;

public class RenderedNotes implements AbstractData {

  public File imageFile() {
    return mImageFile;
  }

  public List<RenderedChord> renderedChords() {
    return mRenderedChords;
  }

  public IRect staffRect() {
    return mStaffRect;
  }

  public IRect clefRect() {
    return mClefRect;
  }

  public IRect keysigRect() {
    return mKeysigRect;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "image_file";
  protected static final String _1 = "rendered_chords";
  protected static final String _2 = "staff_rect";
  protected static final String _3 = "clef_rect";
  protected static final String _4 = "keysig_rect";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mImageFile.toString());
    {
      JSList j = new JSList();
      for (RenderedChord x : mRenderedChords)
        j.add(x.toJson());
      m.put(_1, j);
    }
    m.putUnsafe(_2, mStaffRect.toJson());
    m.putUnsafe(_3, mClefRect.toJson());
    m.putUnsafe(_4, mKeysigRect.toJson());
    return m;
  }

  @Override
  public RenderedNotes build() {
    return this;
  }

  @Override
  public RenderedNotes parse(Object obj) {
    return new RenderedNotes((JSMap) obj);
  }

  private RenderedNotes(JSMap m) {
    {
      mImageFile = Files.DEFAULT;
      String x = m.opt(_0, (String) null);
      if (x != null) {
        mImageFile = new File(x);
      }
    }
    mRenderedChords = DataUtil.parseListOfObjects(RenderedChord.DEFAULT_INSTANCE, m.optJSList(_1), false);
    {
      mStaffRect = IRect.DEFAULT_INSTANCE;
      Object x = m.optUnsafe(_2);
      if (x != null) {
        mStaffRect = IRect.DEFAULT_INSTANCE.parse(x);
      }
    }
    {
      mClefRect = IRect.DEFAULT_INSTANCE;
      Object x = m.optUnsafe(_3);
      if (x != null) {
        mClefRect = IRect.DEFAULT_INSTANCE.parse(x);
      }
    }
    {
      mKeysigRect = IRect.DEFAULT_INSTANCE;
      Object x = m.optUnsafe(_4);
      if (x != null) {
        mKeysigRect = IRect.DEFAULT_INSTANCE.parse(x);
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
    if (object == null || !(object instanceof RenderedNotes))
      return false;
    RenderedNotes other = (RenderedNotes) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mImageFile.equals(other.mImageFile)))
      return false;
    if (!(mRenderedChords.equals(other.mRenderedChords)))
      return false;
    if (!(mStaffRect.equals(other.mStaffRect)))
      return false;
    if (!(mClefRect.equals(other.mClefRect)))
      return false;
    if (!(mKeysigRect.equals(other.mKeysigRect)))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mImageFile.hashCode();
      for (RenderedChord x : mRenderedChords)
        if (x != null)
          r = r * 37 + x.hashCode();
      r = r * 37 + mStaffRect.hashCode();
      r = r * 37 + mClefRect.hashCode();
      r = r * 37 + mKeysigRect.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected File mImageFile;
  protected List<RenderedChord> mRenderedChords;
  protected IRect mStaffRect;
  protected IRect mClefRect;
  protected IRect mKeysigRect;
  protected int m__hashcode;

  public static final class Builder extends RenderedNotes {

    private Builder(RenderedNotes m) {
      mImageFile = m.mImageFile;
      mRenderedChords = DataUtil.mutableCopyOf(m.mRenderedChords);
      mStaffRect = m.mStaffRect;
      mClefRect = m.mClefRect;
      mKeysigRect = m.mKeysigRect;
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
    public RenderedNotes build() {
      RenderedNotes r = new RenderedNotes();
      r.mImageFile = mImageFile;
      r.mRenderedChords = DataUtil.immutableCopyOf(mRenderedChords);
      r.mStaffRect = mStaffRect;
      r.mClefRect = mClefRect;
      r.mKeysigRect = mKeysigRect;
      return r;
    }

    public Builder imageFile(File x) {
      mImageFile = (x == null) ? Files.DEFAULT : x;
      return this;
    }

    public Builder renderedChords(List<RenderedChord> x) {
      mRenderedChords = DataUtil.mutableCopyOf((x == null) ? DataUtil.emptyList() : x);
      return this;
    }

    public Builder staffRect(IRect x) {
      mStaffRect = (x == null) ? IRect.DEFAULT_INSTANCE : x.build();
      return this;
    }

    public Builder clefRect(IRect x) {
      mClefRect = (x == null) ? IRect.DEFAULT_INSTANCE : x.build();
      return this;
    }

    public Builder keysigRect(IRect x) {
      mKeysigRect = (x == null) ? IRect.DEFAULT_INSTANCE : x.build();
      return this;
    }

  }

  public static final RenderedNotes DEFAULT_INSTANCE = new RenderedNotes();

  private RenderedNotes() {
    mImageFile = Files.DEFAULT;
    mRenderedChords = DataUtil.emptyList();
    mStaffRect = IRect.DEFAULT_INSTANCE;
    mClefRect = IRect.DEFAULT_INSTANCE;
    mKeysigRect = IRect.DEFAULT_INSTANCE;
  }

}
