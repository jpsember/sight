package sight;

import static js.base.Tools.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javax.swing.JPanel;

import js.file.Files;
import js.geometry.IRect;
import js.geometry.Matrix;
import js.geometry.MyMath;
import js.graphics.ImgUtil;
import sight.gen.RenderedNotes;

public class Canvas extends JPanel {

  public void paintComponent(Graphics graphics) {
    if (mRenderedNotes == null)
      return;

    var g = (Graphics2D) graphics;
    //var g = mCanvasGraphics;
    {
      g.setBackground(Color.white);
      var b = new IRect(g.getClipBounds());
      g.clearRect(0, 0, b.width, b.height);
    }

    calcTransform();
    g.setTransform(mAtlasToCanvas.toAffineTransform());

    var rn = mRenderedNotes;

    // Stretch the staff image (a vertical strip) to fill the horizontal extent of the staff
    {
      var sr = rn.staffRect();
      var staffImg = getImage(sr);
      g.drawImage(staffImg, 0, sr.y, mContentWidth, sr.height, null);
    }
    // Draw the clef
    drawAtlasImage(g, rn.clefRect(), mClefX);

    // Draw the key signature
    drawAtlasImage(g, rn.keysigRect(), mKeySigX);

    // Draw up to four notes
    var cx = mChordsX + mChordWidth / 2;
    var rnd = MyMath.random();

    for (int i = 0; i < mMaxNotes; i++) {
      int j = rnd.nextInt(rn.renderedChords().size());
      var ch = rn.renderedChords().get(j);

      var r = getImage(ch.rect());
      g.drawImage(r, cx - ch.rect().width / 2, ch.rect().y, null);

      // Draw something in the prompt region

      var ic = icon(rnd.nextInt(3));
      g.drawImage(ic, cx - ic.getWidth() / 2, mPromptY, null);

      cx += mChordWidth;
    }

  }

  public void setSourceImage(File sourceImage) {
    mAtlasImage = ImgUtil.read(sourceImage);
  }

  public void setNotes(RenderedNotes rn) {
    mRenderedNotes = rn;
  }

  /**
   * Calculate the size of the canvas image, and the transform to convert from
   * the atlas image to the canvas image
   */
  private void calcTransform() {
    var rn = mRenderedNotes;

    // We'll set the height to the height of the staff image, multipled by a constant
    var staffHeight = rn.staffRect().height;
    var extraAbove = round(staffHeight * 1.2);
    var extraBelow = round(staffHeight * 1.2);

    mPromptHeight = round(staffHeight * .4);

    var canvasHeight = extraAbove + staffHeight + extraBelow + mPromptHeight;

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
    mPromptY = canvasHeight - mPromptHeight;
    //    mCanvasSize = new IPoint(mContentWidth + padding * 2, padding * 2 + canvasHeight);
  }

  private BufferedImage icon(int index) {
    if (mIcons == null) {
      mIcons = arrayList();
      for (var name : split("cursor right wrong", ' ')) {
        var nm = name + ".png";
        var k = getClass();
        byte[] h = null;
        var res = Util.openResource(k, nm);
        h = Files.toByteArray(res, "icon");
        mIcons.add(ImgUtil.read(h));
      }
    }
    return mIcons.get(index);
  }

  private List<BufferedImage> mIcons;

  private void drawAtlasImage(Graphics2D g, IRect atlasRect, int targetX) {
    g.drawImage(getImage(atlasRect), targetX, atlasRect.y, null);
  }

  private BufferedImage getImage(IRect rect) {
    return ImgUtil.subimage(mAtlasImage, rect);
  }

  private static int round(double v) {
    return (int) Math.round(v);
  }

  private RenderedNotes mRenderedNotes;
  private int mMaxNotes = 4;
  private Matrix mAtlasToCanvas;
  private int mChordWidth;
  private int mClefX, mKeySigX, mChordsX, mContentWidth;
  private int mPromptHeight;
  private BufferedImage mAtlasImage;
  private int mPromptY;

}