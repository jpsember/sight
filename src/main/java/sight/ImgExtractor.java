package sight;

import static js.base.Tools.*;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.List;
import js.base.BaseObject;
import js.data.DataUtil;
import js.geometry.IRect;
import js.graphics.ImgUtil;

public class ImgExtractor extends BaseObject {

  public void setSource(BufferedImage img) {
    loadTools();
    mSource = img;
  }

  public List<BufferedImage> extract() {
    if (mExtracted == null) {
      BufferedImage image = mSource;
      if (verbose())
        log("extracting pixels from:", INDENT, ImgUtil.toJson(image));
      mPixels = ImgUtil.bgrPixels(mSource);
      mWidth = image.getWidth();
      mHeight = image.getHeight();

      mStaffLines = findStaffLines();
      log("staff lines:", mStaffLines);

      mSubImages = extractSubImages();
    }
    return mExtracted;
  }

  private static final int LINE_THICKNESS = 3;

  private List<Integer> findStaffLines() {
    // Look for rightmost column that contains some (non-white) pixels
    int x = mWidth - 1;
    outer: while (x >= 0) {
      for (int y = 0; y < mHeight; y++) {
        if (readPixel(x, y)) {
          break outer;
        }
      }
      x--;
    }
    checkState(x >= 0, "can't find any non-white pixels");
    log("found non-white pixels at x:", x);

    List<Integer> lineRows = arrayList();
    int lastLineY = -1;
    for (int y = 0; y < mHeight; y++) {
      if (!readPixel(x, y))
        continue;
      if (lastLineY < 0 || lastLineY < y - LINE_THICKNESS) {
        lineRows.add(y);
        lastLineY = y;
      }
    }

    return lineRows;
  }

  int count;

  private boolean readPixel(int x, int y) {
    if (x < 0 || x >= mWidth)
      badArg("x out of range:", x, "width:", mWidth);
    if (y < 0 || y >= mHeight)
      badArg("y out of range:", y, "height:", mHeight);
    int offset = (y * mWidth + x) * 3;
    var p = mPixels;
    var b = p[offset + 0];
    var g = p[offset + 1];
    var r = p[offset + 2];
    if (b == -1 && g == -1 && r == -1)
      return false;
    if (count++ < 20) {
      pr("read x,y:", x, y, "got:", b, g, r);
    }
    return true;
  }

  private List<IRect> extractSubImages() {
    List<IRect> r = arrayList();

    List<ActiveRect> active = arrayList();
    int staffCursor = 0;
    for (int y = 0; y < mHeight; y++) {
      // If y is too close to a staff line, skip
      if (staffCursor < mStaffLines.size()) {
        int sl = mStaffLines.get(staffCursor);
        if ((sl - y) * 2 <= LINE_THICKNESS)
          continue;
      }

      xloop: for (int x = 0; x < mWidth; x++) {
        if (!readPixel(x, y))
          continue;
        // Add pixel to an active rect, if possible
        for (var ar : active) {
          if (ar.expandFor(x, y))
            continue xloop;
        }
        // Create a new active rect
        var ar = new ActiveRect(x, y);
        active.add(ar);
      }
      
      // If any rects have been inactive for enough rows, remove it and add to the 
    }
    return r;
  }

  private BufferedImage mSource;
  private List<BufferedImage> mExtracted;
  private int mWidth, mHeight;
  private byte[] mPixels;
  private List<Integer> mStaffLines;
  private List<IRect> mSubImages;

  private static final int MERGE_DIST = 8;

  private static class ActiveRect {
    ActiveRect(int x, int y) {
      x0 = x;
      x1 = x;
      y0 = y;
      y1 = y;
      lastYAdded = y;
    }

    boolean expandFor(int x, int y) {
      if (x < x0 - MERGE_DIST || x > x1 + MERGE_DIST)
        return false;
      x0 = Math.min(x0, x);
      x1 = Math.max(x1, x);
      lastYAdded = y;
      return true;
    }

    int x0, x1, y0, y1;
    int lastYAdded = -1;
  }

}
