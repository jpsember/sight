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

import js.file.Files;
import js.geometry.IRect;
import js.geometry.Matrix;
import js.graphics.ImgEffects;
import js.graphics.ImgUtil;
import sight.gen.RenderedNotes;

public class Canvas extends JPanel {

  private static final boolean DRAW_BOXES = false && alert("drawing boxes");

  public void paintComponent(Graphics graphics) {
    i24("canvas.paintComponent");

    var g = (Graphics2D) graphics;

    {
      g.setBackground(Color.white);
      var b = new IRect(g.getClipBounds());
      g.clearRect(0, 0, b.width, b.height);
    }

    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

    var s = lessonState();
    var id = s.lessonId();
    if (id.isEmpty())
      return;
    var notes = lessonManager().renderedNotes(id);
    // Update the atlas image if the lesson id has changed
    if (!id.equals(mAtlasLessonId)) {
      mAtlasLessonId = id;
      i24("reading atlas image:", notes.imageFile());
      mAtlasImage = imageCache().get(notes.imageFile());
    }

    calcTransform(notes);
    font(g);

    g.transform(mContentTransform.toAffineTransform());

    {
      var m = Msg.get(MSG_MAIN);
      if (m != null) {
        g.setColor(m.color());
        g.setFont(font(g));
        g.drawString(m.toString(), 0, mMessageY + mFontMetrics.getAscent());
      }
    }

    {
      var m = Msg.get(MSG_INFO);
      if (m != null) {
        g.setColor(m.color());
        g.setFont(font(g));
        g.drawString(m.toString(), 0, mInfoY + mFontMetrics.getAscent());
      }

    }

    if (DRAW_BOXES) {
      drawBox(g, Color.red, mClefX, mMessageY, mContentWidth, mMessageHeight);
      drawBox(g, Color.green, mClefX, mPad1Y, mContentWidth, mPadHeight);
      drawBox(g, Color.green, mClefX, mPad2Y, mContentWidth, mPadHeight);
    }

    {
      // Apply the additional transform for rendering images from the atlas

      g.transform(mAtlasToContent.toAffineTransform());
      var rn = notes;

      // Stretch the staff image (a vertical strip) to fill the horizontal extent of the staff
      {
        var sr = rn.staffRect();
        var staffImg = getImage(sr);
        // Start a bit past the left edge of the clef? no longer necessary
        final int xStart = 0; //mTwoStaves ? config().resolution() *  : 0;
        g.drawImage(staffImg, xStart, sr.y, mContentWidth - xStart, sr.height, null);

        if (DRAW_BOXES)
          drawBox(g, Color.magenta, mClefX, sr.y, mContentWidth, sr.height);
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

        var icNum = s.icons()[i] - 1;
        if (icNum >= 0) {
          var ic = icon(icNum);
          g.drawImage(ic, cx - ic.getWidth() / 2, mPromptY, null);
        }

        cx += mChordWidth;
      }
    }
    i24("canvas.paintComponent done");
  }

//  @Deprecated
//  public void clearMessage() {
//    //  mMessage = null;
//  }

  @Deprecated
  public void setInfoMessage(Color color, Object... message) {
    Msg.set(MSG_INFO, color, message);
  }

  @Deprecated
  public void setMessage(Color color, Object... message) {
    Msg.set(MSG_MAIN,color,message);
//    mMessageColor = color;
//    mMessage = BasePrinter.toString(message);
  }

  /**
   * Calculate the size of the canvas image, and the transform to convert from
   * the atlas image to the canvas image
   */
  private void calcTransform(RenderedNotes rn) {
    var atlasStaffRect = rn.staffRect();

    mTwoStaves = rn.clefRect().height > rn.clefRect().width * 3;
    mStandardSize = mTwoStaves ? atlasStaffRect.height / 3 : atlasStaffRect.height;

    mMessageHeight = stdScale(0.4);
    mInfoHeight = stdScale(0.3);

    mPadHeight = stdScale(0.90);
    var staffHeight = atlasStaffRect.height;
    var promptHeight = stdScale(1.6);

    startAllocPix();
    mMessageY = allocPix(mMessageHeight);
    mPad1Y = allocPix(mPadHeight);
    mCanvasStaffY = allocPix(staffHeight);
    mPad2Y = allocPix(mPadHeight);
    mPromptY = allocPix(promptHeight);
    mInfoY = allocPix(mInfoHeight);
    mContentHeight = allocPix(0);

    startAllocPix();
    mClefX = allocPix(rn.clefRect().width + stdScale(0.1));
    mKeySigX = allocPix(rn.keysigRect().width + stdScale(0.5));
    mChordWidth = stdScale(1.4);
    mChordsX = allocPix(mChordWidth * mMaxNotes + stdScale(0.1));
    mContentWidth = allocPix(0);

    var canvasSize = new IRect(getBounds()).size();

    mContentTransform = Matrix.getTranslate((canvasSize.x - mContentWidth) / 2,
        (canvasSize.y - mContentHeight) / 2);

    // Determine transformation from a location in the atlas to the canvas content region.
    //
    // We don't care about the x coordinates within the atlas, since we will be explicitly
    // providing the x coordinate.

    // The y coordinate should be transformed from atlas space to canvas space, where
    // the canvas space includes the message and first padding region above the staff.

    int xOffset = 0;
    int yOffset = mCanvasStaffY - atlasStaffRect.y;

    mAtlasToContent = Matrix.getTranslate(xOffset, yOffset);
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
        double targetHeight = config().resolution() / 4;
        double f = targetHeight / img.getHeight();
        var scaled = ImgEffects.scale(img, f);
        mIcons.add(scaled);
      }
    }
    return mIcons.get(index);
  }

  private void drawAtlasImage(Graphics2D g, IRect atlasRect, int targetX) {
    var image = getImage(atlasRect);
    g.drawImage(image, targetX, atlasRect.y, null);
    if (DRAW_BOXES)
      drawBox(g, Color.blue, targetX, atlasRect.y, atlasRect.width, atlasRect.height);
  }

  private void drawBox(Graphics2D g, Color color, int x, int y, int w, int h) {
    if (alert("rendering box")) {
      g.setColor(color);
      g.drawRect(x, y, w, h);
    }
  }

  private BufferedImage getImage(IRect rect) {
    return ImgUtil.subimage(mAtlasImage, rect);
  }

  private Font font(Graphics2D g) {
    if (mFont == null) {
      i24("constructing font");
      checkState(mMessageHeight != 0);
      // I think constructing the font was taking a lot of time.
      mFont = new Font(Font.SANS_SERIF, Font.BOLD, mMessageHeight);
      mFontMetrics = g.getFontMetrics(mFont);
      i24("done constructing font");
    }
    return mFont;
  }

  private int stdScale(double amt) {
    return (int) Math.round(amt * mStandardSize);
  }

  private void startAllocPix() {
    mNextCoordinate = 0;
  }

  private int allocPix(int pixels) {
    var result = mNextCoordinate;
    mNextCoordinate += pixels;
    return result;
  }

  private List<BufferedImage> mIcons;

  // Standard number of pixels to represent other quantities as a proportion of
  private int mStandardSize;
  // The next y coordinate to return from call to allocHeight()
  private int mNextCoordinate;
  private int mMessageY, mCanvasStaffY, mPromptY, mContentHeight, mPad1Y, mPad2Y, mInfoY;

  private FontMetrics mFontMetrics;
  private Font mFont;

  private int mMaxNotes = 4;
  private Matrix mAtlasToContent;

  private int mChordWidth;
  private int mClefX, mKeySigX, mChordsX, mContentWidth;
  private int mMessageHeight;
  private int mInfoHeight;
  private int mPadHeight;
  private BufferedImage mAtlasImage;
  private String mAtlasLessonId;

//  private Msg mMainMessage;
//  private Msg mInfoMessage;
  //  
  //  private String mMessage;
  //  private Color mMessageColor;
  //  
  //  private String mInfoMessage;
  //  private Color mInfoMessageColor;

  private Matrix mContentTransform;
  private boolean mTwoStaves;
}
