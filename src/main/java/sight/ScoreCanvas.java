package sight;

import static js.base.Tools.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import js.base.BaseObject;
import js.geometry.IPoint;
import js.geometry.IRect;
import js.geometry.Matrix;
import js.geometry.MyMath;
import js.graphics.ImgUtil;
import sight.gen.RenderedNotes;

public class ScoreCanvas extends BaseObject {

  public void setSourceImage(File sourceImage) {
    loadTools();
    ms = ImgUtil.read(sourceImage);
  }

  public void setNotes(RenderedNotes rn) {
    mRenderedNotes = rn;
    calcTransform();
    graphics().setTransform(mAtlasToCanvas.toAffineTransform());
  }

  /**
   * Calculate the size of the canvas image, and the transform to convert from
   * the atlas image to the canvas image
   */
  private void calcTransform() {
    var rn = mRenderedNotes;

    // We'll set the height to the height of the staff image, multipled by a constant
    var staffHeight = rn.staffRect().height;
    var extraAbove = round(staffHeight * .8);
    var extraBelow = round(staffHeight * .8);

    var canvasHeight = extraAbove + staffHeight + extraBelow;

    // Determine x offsets of the clef, keysig, and the (first) chord

    var xpad = staffHeight * .1;

    mClefX = round(xpad);
    mKeySigX = round(mClefX + rn.clefRect().width + xpad);
    mChordsX = round(mKeySigX + rn.keysigRect().width + staffHeight * .5);

    // The width given to each chord is a proportion of the staff height
    mChordWidth = round(staffHeight * 1.4);

    mContentWidth = round(mChordsX + mChordWidth * mMaxNotes + xpad);

    int padding = round(staffHeight * .5);
    mAtlasToCanvas = Matrix.getTranslate(padding, padding + extraAbove - rn.staffRect().y);
    mCanvasSize = new IPoint(mContentWidth + padding * 2, padding * 2 + canvasHeight);
  }

  public void render() {
    var rn = mRenderedNotes;

    // Stretch the staff image (a vertical strip) to fill the horizontal extent of the staff
    {
      var sr = rn.staffRect();
      var staffImg = getImage(sr);
      graphics().drawImage(staffImg, 0, sr.y, mContentWidth, sr.height, null);
    }
    // Draw the clef
    drawAtlasImage(rn.clefRect(), mClefX);

    // Draw the key signature
    drawAtlasImage(rn.keysigRect(), mKeySigX);

    // Draw up to four notes
    var cx = mChordsX;
    var rnd = MyMath.random();

    for (int i = 0; i < mMaxNotes; i++) {
      int j = rnd.nextInt(rn.renderedChords().size());
      var ch = rn.renderedChords().get(j);

      var r = getImage(ch.rect());
      graphics().drawImage(r, cx, ch.rect().y, null);
      cx += mChordWidth;
    }
  }

  private void drawAtlasImage(IRect atlasRect, int targetX) {
    graphics().drawImage(getImage(atlasRect), targetX, atlasRect.y, null);
  }

  public BufferedImage image() {
    if (mCanvasImage == null) {
      mCanvasImage = ImgUtil.build(mCanvasSize, ImgUtil.PREFERRED_IMAGE_TYPE_COLOR);
      mCanvasGraphics = image().createGraphics();
      mCanvasGraphics.setBackground(Color.white);
      mCanvasGraphics.clearRect(0, 0, mCanvasSize.x, mCanvasSize.y);
    }
    return mCanvasImage;
  }

  private BufferedImage getImage(IRect rect) {
    return ImgUtil.subimage(ms, rect);
  }

  private BufferedImage ms;

  private static int round(double v) {
    return (int) Math.round(v);
  }

  //  private IPoint toCanvas(IPoint pt) {
  //    return pt.sumWith(mTranslateToCanvas);
  //  }

  private Graphics2D graphics() {
    if (mCanvasGraphics == null) {
      image();
    }
    return mCanvasGraphics;
  }

  private RenderedNotes mRenderedNotes;

  private int mMaxNotes = 4;

  private Matrix mAtlasToCanvas;
  private IPoint mCanvasSize;
  private int mChordWidth;
  //  private int staffXStart, staffXEnd;
  private BufferedImage mCanvasImage;
  private Graphics2D mCanvasGraphics;
  private int mClefX, mKeySigX, mChordsX, mContentWidth;

}
