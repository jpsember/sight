package sight.gen;

import java.io.File;
import java.util.List;
import js.data.AbstractData;
import js.data.DataUtil;
import js.file.Files;
import js.json.JSList;
import js.json.JSMap;

public class RenderedNotes implements AbstractData {

  public File imageFile() {
    return mImageFile;
  }

  public List<RenderedChord> renderedChords() {
    return mRenderedChords;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "image_file";
  protected static final String _1 = "rendered_chords";

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
      m__hashcode = r;
    }
    return r;
  }

  protected File mImageFile;
  protected List<RenderedChord> mRenderedChords;
  protected int m__hashcode;

  public static final class Builder extends RenderedNotes {

    private Builder(RenderedNotes m) {
      mImageFile = m.mImageFile;
      mRenderedChords = DataUtil.mutableCopyOf(m.mRenderedChords);
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

  }

  public static final RenderedNotes DEFAULT_INSTANCE = new RenderedNotes();

  private RenderedNotes() {
    mImageFile = Files.DEFAULT;
    mRenderedChords = DataUtil.emptyList();
  }

}
