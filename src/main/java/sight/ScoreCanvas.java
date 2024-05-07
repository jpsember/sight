package sight;

import static js.base.Tools.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import js.base.BaseObject;
import js.geometry.IPoint;
import js.geometry.IRect;
import js.graphics.ImgUtil;
import sight.gen.RenderedNotes;

public class ScoreCanvas extends BaseObject {

  public void setSourceImage(File sourceImage) {
    ms = ImgUtil.read(sourceImage);
  }

  public void setNotes(RenderedNotes rn) {
    loadTools();
    mRn = rn;

    calcSize();
  }

  public void render() {
    var rn = mRn;
    var r = rn.staffRect();
    var origPt = new IPoint(staffXStart, r.y);
    var endPt = new IPoint(staffXEnd, r.endY());

    var plotStart = toCanvas(origPt);
    var plotEnd = toCanvas(endPt);

    var g = graphics();

    var sr = rn.staffRect();

    var staffImg = getImage(sr);

    pr("staffImg:", ImgUtil.toJson(staffImg));
    pr("plotStart:", plotStart);
    pr("plotEnd:", plotEnd);
    pr("sr:", sr);
    pr("canvas size:", mCanvasSize);

    //    ImgUtil.writeImage(Files.S, staffImg, new File("wtf.png"));
    //halt();
    g.drawImage(staffImg, plotStart.x, plotStart.y, plotEnd.x, plotEnd.y, null);

    g.setColor(Color.red);
    g.drawRect(plotStart.x, plotStart.y, plotEnd.x - plotStart.x, plotEnd.y - plotStart.y);

  }

  public BufferedImage image() {
    if (mImage == null) {

      //      var rn = mRn;
      //      var r = rn.staffRect();
      //      var origPt = new IPoint(staffXStart, r.y);
      //      var endPt = new IPoint(staffXEnd, r.endY());

      //      var plotStart = toCanvas(origPt);
      //      var plotEnd = toCanvas(endPt);

      mImage = ImgUtil.build(mCanvasSize, ImgUtil.PREFERRED_IMAGE_TYPE_COLOR);
      mGraphics = image().createGraphics();
      mGraphics.setBackground(Color.white);
      mGraphics.clearRect(0, 0, mCanvasSize.x, mCanvasSize.y);
    }
    return mImage;
  }

  private BufferedImage getImage(IRect rect) {
    return ImgUtil.subimage(ms, rect);
  }

  private BufferedImage ms;

  private void calcSize() {
    var rn = mRn;
    var r = rn.staffRect();
    double staffHeight = r.height;
    int y0 = round(r.y - staffHeight * .6);
    int y1 = round(r.y + staffHeight * 1.6);

    //    int x0 = round(r2.x - staffHeight * .2);
    int noteWidth = round(staffHeight * 1.2);

    var r3 = rn.keysigRect();
    int notesXStart = r3.endX() + round(staffHeight * .3);
    int notesXEnd = notesXStart + noteWidth * mMaxNotes;
    staffXEnd = notesXEnd + round(staffHeight * .5);
    var r2 = rn.clefRect();
    staffXStart = round(r2.x - staffHeight * .2);

    int padding = 20;

    mTranslateToCanvas = new IPoint(-staffXStart + padding, -y0 + padding);
    mCanvasSize = new IPoint(staffXEnd - staffXStart + padding * 2, y1 - y0 + padding * 2);
  }

  private static int round(double v) {
    return (int) Math.round(v);
  }

  private IPoint toCanvas(IPoint pt) {
    return pt.sumWith(mTranslateToCanvas);
  }

  private Graphics2D graphics() {
    if (mGraphics == null) {
      image();
    }
    return mGraphics;
  }

  private RenderedNotes mRn;

  private int mMaxNotes = 4;

  private IPoint mTranslateToCanvas;
  private IPoint mCanvasSize;
  private int staffXStart, staffXEnd;
  private BufferedImage mImage;
  private Graphics2D mGraphics;
}
