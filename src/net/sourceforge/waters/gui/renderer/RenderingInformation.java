package net.sourceforge.waters.gui.renderer;

import java.awt.Color;


public class RenderingInformation
{
  private final boolean mIsSelected;
  private final boolean mShowHandles;
  private final Color mColor;
  private final Color mShadowColor;
  private final boolean mIsFocused;
  private final int mPriority;

  /*
   * public RenderingInformation(EditorObject o) { this(false, false, false,
   * EditorObject.NOTDRAG, o); }
   */

  public RenderingInformation(final boolean isSelected,
                              final boolean showHandles,
                              final boolean isFocused, final Color color,
                              final Color shadowColor, final int priority)
  {
    mIsSelected = isSelected;
    mShowHandles = showHandles;
    mIsFocused = isFocused;
    mColor = color;
    mShadowColor = shadowColor;
    mPriority = priority;
  }

  public boolean isSelected()
  {
    return mIsSelected;
  }

  public boolean showHandles()
  {
    return mIsSelected && mShowHandles;
  }

  public boolean isFocused()
  {
    return mIsFocused;
  }

  public Color getColor()
  {
    return mColor;
  }

  public Color getShadowColor()
  {
    return mShadowColor;
  }

  public int getPriority()
  {
    return mPriority;
  }
}
