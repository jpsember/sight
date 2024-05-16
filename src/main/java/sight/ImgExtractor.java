package sight;

import static js.base.Tools.*;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import js.base.BaseObject;
import js.base.BasePrinter;
import js.file.Files;
import js.geometry.IPoint;
import js.geometry.IRect;
import js.graphics.ImgUtil;
import js.graphics.Plotter;

public class ImgExtractor extends BaseObject {

  private static final float RES_FACTOR = 1 / 2.5f;
  private static final int LINE_THICKNESS = (int) (12 * RES_FACTOR);
  private static final int MERGE_DIST = (int) (18 * RES_FACTOR);
  private static final int PAD_DIST = MERGE_DIST / 2;

  public void setSource(BufferedImage img) {
    checkState(mSource == null);
    mSource = img;
  }

  public static final int RECT_STAFF_LINES = 0 //
      , RECT_CLEF = 1 //
      , RECT_KEYSIG = 2 //
      , RECT_HEADER_SIZE = 3 //
  ;

  public List<IRect> rects() {
    if (mSubImages == null) {
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
    return mSubImages;
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

    mStaffLineEndX = x;
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
    return true;
  }

  private void deleteRow(byte[] pix, int y) {
    int bytesPerRow = mWidth * 3;
    int r0 = y * bytesPerRow;
    Arrays.fill(pix, r0, r0 + bytesPerRow, (byte) 255);
  }

  private List<IRect> extractSubImages() {

    List<ActiveRect> active = arrayList();
    int staffCursor = 0;

    log("extractSubImages");

    if (alert("rendering image with staff lines omitted")) {
      byte[] mpix = Arrays.copyOf(mPixels, mPixels.length);

      for (int y = 0; y < mHeight; y++) {
        // If y is too close to a staff line, skip
        if (staffCursor < mStaffLines.size()) {
          int sl = mStaffLines.get(staffCursor);
          if (Math.abs(sl - y) * 2 <= LINE_THICKNESS) {
            deleteRow(mpix, y);
            continue;
          }
          if (sl < y) {
            staffCursor++;
          }
        }
      }
      var i2 = ImgUtil.bytesToBGRImage(mpix, new IPoint(mWidth, mHeight));
      ImgUtil.writeImage(Files.S, i2, new File("_SKIP_stripped.png"));
    }

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
        break;
      }
    }

    List<IRect> r = arrayList();
    var bounds = new IRect(0, 0, mWidth, mHeight);

    for (var ar : active) {
      var rect = IRect.rectContainingPoints(IPoint.with(ar.x0, ar.y0), IPoint.with(ar.x1, ar.y1));
      rect = rect.withInset(-PAD_DIST);
      rect = IRect.intersection(rect, bounds);
      r.add(rect);
    }
    r.sort((a, b) -> Integer.compare(a.x, b.x));

    // If the leftmost rectangle is very thin, it's an extraneous vertical line that we can delete
    if (r.size() != 0) {
      var x = r.get(0);
      pr("leftmost rect:", x);
      if (x.width <= 8) {
        pr("omitting, assuming vert bar");
        r.remove(0);
      }
    }

    // Insert at position 0 a rectangle representing a thin slice of the staff lines
    {
      var width = 8;
      int y0 = mStaffLines.get(0) - LINE_THICKNESS - 2;
      int yLast = last(mStaffLines) + LINE_THICKNESS + 2;

      var sr = new IRect(mStaffLineEndX - 1 - width, y0, width, yLast - y0);
      r.add(0, sr);
    }

    for (var rd : r)
      log("subImage at:", rd);

    if (mRenderedRectsImageFile != null)
      renderRects(mSource, r, mRenderedRectsImageFile);
    return r;
  }

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
      x0 = Math.min(x0, x);
      x1 = Math.max(x1, x);
      y1 = y;
      return true;
    }

    int x0, x1, y0, y1;
  }

  public ImgExtractor withRenderedRects(File imageFile) {
    mRenderedRectsImageFile = imageFile;
    return this;
  }

  public BufferedImage plotRects() {
    var b = ImgUtil.imageAsType(ImgUtil.deepCopy(mSource), Plotter.PREFERRED_IMAGE_TYPE);

    var pl = Plotter.build();
    pl.into(b);
    pl.withBlue();
    //    var g = b.createGraphics();
    //    g.setStroke(new BasicStroke(2));
    //    var clr = Color.blue;
    //    // clr = ImgUtil.withAlpha(clr,64);
    //    g.setColor(clr);

    var unscaledFont = new Font("Courier", Font.PLAIN, 16);
    var CONSOLE_FONT = unscaledFont.deriveFont(24);
    pl.graphics().setFont(CONSOLE_FONT);
    var fm = pl.graphics().getFontMetrics();
    int i = INIT_INDEX;
    for (var r : rects()) {
      i++;
      var r2 = r.withInset(-1);
      pl.drawRect(r2);
      var g = pl.graphics();
      g.drawString("" + i, r2.x, r2.y - fm.getAscent());
      //      g.drawRect(r2.x, r2.y, r2.width, r2.height);
    }

    return b;
  }

  private void renderRects(BufferedImage sourceImage, List<IRect> rects, File outputFile) {
  }

  private File mRenderedRectsImageFile;

  private BufferedImage mSource;
  private int mWidth, mHeight;
  private byte[] mPixels;
  private List<Integer> mStaffLines;
  private int mStaffLineEndX;
  private List<IRect> mSubImages;

}
