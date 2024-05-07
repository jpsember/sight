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

    var r = rn.staffRect();
    var staffHeight = r.height;

    var extraAbove = round(staffHeight * .6);
    var extraBelow = round(staffHeight * .6);
    
    pr("extra above & below:",extraAbove,extraBelow);
    
    var canvasHeight = extraAbove + staffHeight + extraBelow;

    // Determine x offsets of the clef, keysig, and the (first) chord

    var xpad = staffHeight * .1;

    var clefWidth = rn.clefRect().width;

    xClef = round(xpad);
    xKeySig = round(xClef + clefWidth + xpad);
    xChords = round(xKeySig + xpad);

    // The width given to each chord is a proportion of the staff height
    mChordWidth = round(staffHeight * 1.2);

    contentWidth = round(xChords + mChordWidth * mMaxNotes + xpad);

    int padding = 20;
    mAtlasToCanvas = Matrix.getTranslate(padding, padding + extraAbove - rn.staffRect().y);
    mCanvasSize = new IPoint(contentWidth + padding * 2, padding * 2 + canvasHeight);
    
    pr("xClef:",xClef);
    pr("xKeySig:",xKeySig);
    pr("xChords:",xChords);
    pr("contentWidth:",contentWidth);
    pr("canvasSize:",mCanvasSize);
  }

  public void render() {
    var rn = mRenderedNotes;
    //var r = rn.staffRect();
    //    var origPt = new IPoint(staffXStart, r.y);
    //    var endPt = new IPoint(staffXEnd, r.endY());

    //    var plotStart = toCanvas(origPt);
    //    var plotEnd = toCanvas(endPt);

    var g = graphics();

    var sr = rn.staffRect();

    var staffImg = getImage(sr);

    //    pr("staffImg:", ImgUtil.toJson(staffImg));
    //    pr("plotStart:", plotStart);
    //    pr("plotEnd:", plotEnd);
    //    pr("sr:", sr);
    //    pr("canvas size:", mCanvasSize);

    //    ImgUtil.writeImage(Files.S, staffImg, new File("wtf.png"));
    //halt();

    g.drawImage(staffImg, 0, sr.y, contentWidth, sr.height, null);

    //    g.setColor(Color.red);
    //    g.drawRect(plotStart.x, plotStart.y, plotEnd.x - plotStart.x, plotEnd.y - plotStart.y);

  }

  public BufferedImage image() {
    if (mCanvasImage == null) {

      //      var rn = mRn;
      //      var r = rn.staffRect();
      //      var origPt = new IPoint(staffXStart, r.y);
      //      var endPt = new IPoint(staffXEnd, r.endY());

      //      var plotStart = toCanvas(origPt);
      //      var plotEnd = toCanvas(endPt);

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
  private int xClef, xKeySig, xChords, contentWidth;

}
