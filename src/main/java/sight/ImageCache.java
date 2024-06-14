package sight;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;

import js.base.BaseObject;
import js.graphics.ImgUtil;

import static js.base.Tools.*;
import static sight.Util.*;

public class ImageCache extends BaseObject {

  public void clear() {
    loadUtil();
    mCache.clear();
  }

  public BufferedImage get(File file) {
    var img = mCache.get(file);
    if (img == null) {
      img = ImgUtil.read(file);
      if (mCache.size() > 20)
        clear();
      mCache.put(file, img);
    }
    return img;
  }

  private Map<File, BufferedImage> mCache = concurrentHashMap();
}

