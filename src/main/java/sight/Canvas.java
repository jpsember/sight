package sight;

import static js.base.Tools.*;
import static sight.Util.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JPanel;

import js.file.Files;
import js.geometry.IRect;
import js.geometry.Matrix;
import js.graphics.ImgEffects;
import js.graphics.ImgUtil;
import sight.gen.DrillState;
import sight.gen.RenderedNotes;

public class Canvas extends JPanel {

  public void paintComponent(Graphics graphics) {
    z("painting canvas");
    if (mDrillState == null)
      return;
    var notes = mDrillState.notes();
    if (notes == null)
      return;

    var g = (Graphics2D) graphics;

    {
      g.setBackground(Color.white);
      var b = new IRect(g.getClipBounds());
      g.clearRect(0, 0, b.width, b.height);
    }

    calcTransform(notes);
    g.setTransform(mAtlasToCanvas.toAffineTransform());
    var rn = notes;
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    
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
    int numNotes = Math.min(mMaxNotes, rn.renderedChords().size());

    for (int i = 0; i < numNotes; i++) {
      var ch = rn.renderedChords().get(i);
      var r = getImage(ch.rect());
      g.drawImage(r, cx - ch.rect().width / 2, ch.rect().y, null);

      // Render icon in prompt region, if appropriate

      var icNum = mDrillState.icons()[i] - 1;
      if (icNum >= 0) {
        var ic = icon(icNum);
        g.drawImage(ic, cx - ic.getWidth() / 2, mPromptY, null);
      }

      cx += mChordWidth;
    }

  }

  public void setDrillState(DrillState s) {
    mDrillState = s;
    var sourceImage = s.notes().imageFile();
    mAtlasImage = ImgUtil.read(sourceImage);
  }

  /**
   * Calculate the size of the canvas image, and the transform to convert from
   * the atlas image to the canvas image
   * 
   * @param rn
   */
  private void calcTransform(RenderedNotes rn) {

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
    var scaleTfm = Matrix.getScale((float) config().noteScale());
    mAtlasToCanvas = Matrix.preMultiply(mAtlasToCanvas, scaleTfm);
    mPromptY = canvasHeight - mPromptHeight;
  }

  private BufferedImage icon(int index) {
    if (mIcons == null) {
      mIcons = arrayList();
      var names = "up-sign checked unchecked";
      for (var name : split(names, ' ')) {
        var nm = name + ".png";
        var k = getClass();
        byte[] h = null;
        var res = Util.openResource(k, nm);
        h = Files.toByteArray(res, "icon");
        var img = ImgUtil.read(h);
        double targetHeight = 64;
        double f = targetHeight / img.getHeight();
        var scaled = ImgEffects.scale(img, f);
        mIcons.add(scaled);
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

  private int mMaxNotes = 4;
  private Matrix mAtlasToCanvas;
  private int mChordWidth;
  private int mClefX, mKeySigX, mChordsX, mContentWidth;
  private int mPromptHeight;
  private BufferedImage mAtlasImage;
  private int mPromptY;
  private DrillState mDrillState;
}
