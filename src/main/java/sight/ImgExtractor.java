package sight;

import static js.base.Tools.*;

import java.awt.image.BufferedImage;
import java.util.List;

import js.base.BaseObject;
import js.base.BasePrinter;
import js.geometry.IPoint;
import js.geometry.IRect;
import js.geometry.MyMath;
import js.graphics.ImgUtil;

public class ImgExtractor extends BaseObject {

  private static final int LINE_THICKNESS = 6;
  private static final int MERGE_DIST = 18;
  private static final int PAD_DIST = MERGE_DIST / 2;

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
    //log("found non-white pixels at x:", x);

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
    if (false && count++ < 20) {
      pr("read x,y:", x, y, "got:", b, g, r);
    }
    return true;
  }

  private List<IRect> extractSubImages() {

    List<ActiveRect> active = arrayList();
    int staffCursor = 0;

    log("extractSubImages");

    for (int y = 0; y < mHeight; y++) {

      log("y:", y);
      // If y is too close to a staff line, skip
      if (staffCursor < mStaffLines.size()) {
        int sl = mStaffLines.get(staffCursor);
        log("sc:", staffCursor, "y:", sl);
        if (Math.abs(sl - y) * 2 <= LINE_THICKNESS) {
          log("...too close to staff");
          continue;
        }
        if (sl < y) {
          staffCursor++;
          log("past this staff line, incr cursor");
        }
      }

      xloop: for (int x = 0; x < mWidth; x++) {
        if (!readPixel(x, y))
          continue;
        // log("...pixel found at:", x);
        // Add pixel to an active rect, if possible
        for (var ar : active) {
          if (ar.expandFor(x, y))
            continue xloop;
        }

        // Create a new active rect
        var ar = new ActiveRect(x, y);
        active.add(ar);

        log("found pixel, added new rect for:", x, y);
      }

      // Merge any active rects that overlap (or nearly overlap) horizontally
      outer: while (true) {
        for (var r1 : active)
          for (var r2 : active) {
            if (r1 == r2)
              continue;

            if (r1.x0 - MERGE_DIST > r2.x1)
              continue;
            if (r2.x0 - MERGE_DIST > r1.x1)
              continue;

            var m = ActiveRect.merge(r1, r2);
            List<ActiveRect> merged = arrayList();
            merged.add(m);
            log("merged active rects:", INDENT, r1, CR, r2, CR, m);
            for (var r3 : active) {
              if (!(r3 == r1 || r3 == r2))
                merged.add(r3);
            }
            active = merged;
            continue outer;
          }
        //        if (origSize == active.size())
        break;
      }
    }

    List<IRect> r = arrayList();
    var bounds = new IRect(0, 0, mWidth, mHeight);

    for (var ar : active) {
      var rect = IRect.rectContainingPoints(IPoint.with(ar.x0, ar.y0), IPoint.with(ar.x1, ar.y1));
      rect = rect.withInset(-PAD_DIST);
      rect = IRect.intersection(rect, bounds);
//      log("subImage found at:", rect);
      r.add(rect);
    }
    r.sort((a, b) -> Integer.compare(a.x, b.x));
    for (var rd : r) log("subImage at:",rd);
    return r;
  }

  private BufferedImage mSource;
  private List<BufferedImage> mExtracted;
  private int mWidth, mHeight;
  private byte[] mPixels;
  private List<Integer> mStaffLines;
  private List<IRect> mSubImages;

  private static class ActiveRect {

    @Override
    public String toString() {
      return BasePrinter.toString(" x:", x0, "..", x1, "y:", y0, "..", y1);
    }

    public static ActiveRect merge(ActiveRect r1, ActiveRect r2) {
      var r = new ActiveRect();
      r.x0 = Math.min(r1.x0, r2.x0);
      r.x1 = Math.max(r1.x1, r2.x1);
      r.y0 = Math.min(r1.y0, r2.y0);
      r.y1 = Math.max(r1.y1, r2.y1);
      return r;
    }

    ActiveRect() {
    }

    ActiveRect(int x, int y) {
      x0 = x;
      x1 = x;
      y0 = y;
      y1 = y;
    }

    boolean expandFor(int x, int y) {
      if (x < x0 - MERGE_DIST || x > x1 + MERGE_DIST)
        return false;
      // pr("expanding:", this, "for:", x, y);
      checkState(x != 814, "attempt to expand for:", x, y);
      x0 = Math.min(x0, x);
      x1 = Math.max(x1, x);
      y1 = y;
      return true;
    }

    int x0, x1, y0, y1;
  }

}
