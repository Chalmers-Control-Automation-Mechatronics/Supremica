package net.sourceforge.waters.gui.command;

import java.awt.Point;
import net.sourceforge.waters.gui.renderer.LabelProxyShape;
import java.awt.geom.Point2D;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;

public class RecallLabelCommand implements Command
{
  private final LabelGeometrySubject mLabel;
  private final Point2D mPrev;
  
  public RecallLabelCommand(LabelGeometrySubject label)
  {
    mLabel = label;
    mPrev = label.getOffset();
  }
  
  public void execute()
  {
    Point2D p = new Point(LabelProxyShape.DEFAULTOFFSETX,
                          LabelProxyShape.DEFAULTOFFSETY);
    mLabel.setOffset(p);
  }

  public String getName()
  {
    return "recall label";
  }

  public boolean isSignificant()
  {
    return true;
  }

  public void undo()
  {
    mLabel.setOffset(mPrev);
  }
}

