package sight;

import static js.base.Tools.*;
import static sight.Util.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JPanel;

import js.base.BasePrinter;
import js.file.Files;
import js.geometry.IRect;
import js.geometry.Matrix;
import js.graphics.ImgEffects;
import js.graphics.ImgUtil;
import sight.gen.DrillState;
import sight.gen.RenderedNotes;

public class Canvas extends JPanel {

  public void paintComponent(Graphics graphics) {

    if (mDrillState == null) {
      pr("DrillState is null!", INDENT, ST);
      return;
    }

    var notes = lessonManager().renderedNotes(mDrillState.lessonId());

    var g = (Graphics2D) graphics;

    {
      g.setBackground(Color.white);
      var b = new IRect(g.getClipBounds());
      g.clearRect(0, 0, b.width, b.height);
    }

    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

    calcTransform(notes);

    var orig = g.getTransform();

    if (!nullOrEmpty(mMessage)) {
      g.transform(mMessageMatrix.toAffineTransform());
      g.setColor(mMessageColor);
      g.setFont(font(g));
      g.drawString(mMessage, 10, 10 + mFontMetrics.getAscent());
      g.setTransform(orig);
    }

    {
      g.transform(mAtlasToCanvas.toAffineTransform());
      var rn = notes;

      // Stretch the staff image (a vertical strip) to fill the horizontal extent of the staff
      {
        var sr = rn.staffRect();
        var staffImg = getImage(sr);
        // Start a bit past the left edge of the clef
        final int WILD_ASS_GUESS = 25;
        g.drawImage(staffImg, WILD_ASS_GUESS, sr.y, mContentWidth - WILD_ASS_GUESS, sr.height, null);
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
      g.setTransform(orig);
    }

  }

  public void setDrillState(DrillState s) {
    mDrillState = s;
    var notes = lessonManager().renderedNotes(s.lessonId());
    var sourceImage = notes.imageFile();
    mAtlasImage = ImgUtil.read(sourceImage);
  }

  public void clearMessage() {
    mMessage = null;
  }

  public void setMessage(Color color, Object... message) {
    mMessageColor = color;
    mMessage = BasePrinter.toString(message);
  }

  /**
   * Calculate the size of the canvas image, and the transform to convert from
   * the atlas image to the canvas image
   */
  private void calcTransform(RenderedNotes rn) {
    if (rn.equals(mCachedRenderedNotesForTfm))
      return;
    mCachedRenderedNotesForTfm = rn;

    // We'll set the height to the height of the staff image, multipled by a constant
    var trueStaffHeight = rn.staffRect().height;
    var staffHeight = 100;

    var extraAbove = round(staffHeight * 1.2);
    var extraBelow = round(staffHeight * 1.2);

    mPromptHeight = round(staffHeight * .4);

    var canvasHeight = extraAbove + trueStaffHeight + extraBelow + mPromptHeight;

    // Determine x offsets of the clef, keysig, and the (first) chord

    var xpad = staffHeight * .1;

    mClefX = round(xpad);
    mKeySigX = round(mClefX + rn.clefRect().width + xpad);
    mChordsX = round(mKeySigX + rn.keysigRect().width + staffHeight * .5);

    // The width given to each chord is a proportion of the staff height
    mChordWidth = round(staffHeight * 1.4);

    mContentWidth = round(mChordsX + mChordWidth * mMaxNotes + xpad);

    var messageHeight = round(staffHeight * .1);

    int padding = round(staffHeight * .5);

    mMessageMatrix = Matrix.getTranslate(20, messageHeight * .75f);

    mAtlasToCanvas = Matrix.getTranslate(padding, padding + extraAbove - rn.staffRect().y);
    var scaleTfm = Matrix.getScale((float) config().noteScale());
    mAtlasToCanvas = Matrix.preMultiply(mAtlasToCanvas, scaleTfm);
    mAtlasToCanvas = Matrix.preMultiply(mAtlasToCanvas, Matrix.getTranslate(0, messageHeight));

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

  private Font font(Graphics2D g) {
    if (mFont == null) {
      mFont = new Font(Font.SANS_SERIF, Font.BOLD, 35);
      mFontMetrics = g.getFontMetrics(mFont);
    }
    return mFont;
  }

  private FontMetrics mFontMetrics;
  private Font mFont;

  private int mMaxNotes = 4;
  private Matrix mAtlasToCanvas;
  private Matrix mMessageMatrix;

  private int mChordWidth;
  private int mClefX, mKeySigX, mChordsX, mContentWidth;
  private int mPromptHeight;
  private BufferedImage mAtlasImage;
  private int mPromptY;
  private DrillState mDrillState;
  private RenderedNotes mCachedRenderedNotesForTfm;
  private String mMessage;
  private Color mMessageColor;

}
