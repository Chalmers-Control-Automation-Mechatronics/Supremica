package net.sourceforge.waters.gui.command;

import net.sourceforge.waters.gui.ControlledSurface;
import java.util.List;
import net.sourceforge.waters.subject.base.ProxySubject;
import java.util.Set;
import java.util.HashSet;
import net.sourceforge.waters.subject.module.NodeSubject;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

public class UpdateErrorCommand
  implements Command
{
  private final ControlledSurface mSurface;
  private final Set<ProxySubject> mOError;
  private final Set<ProxySubject> mNError;
  
  public UpdateErrorCommand(ControlledSurface surface)
  {
    mSurface = surface;
    mOError = surface.getErrorList();
    mNError = findError();
  }
  
  public void execute()
	{
		mSurface.setErrorList(mNError);
	}
	
	public void undo()
	{
		mSurface.setErrorList(mOError);
	}
	
	public boolean isSignificant()
	{
		return false;
	}
	
	public String getName()
	{
		return "Set Error List";
	}
  
  private Set<ProxySubject> findError()
  {
    Set<ProxySubject> error = new HashSet<ProxySubject>();
    try {
      for (NodeSubject n1 : mSurface.getGraph().getNodesModifiable()) {
        Shape s1 = mSurface.getShapeProducer().getShape(n1).getShape();
        for (NodeSubject n2 : mSurface.getGraph().getNodesModifiable()) {
          if (n1 != n2) {
            Shape s2 = mSurface.getShapeProducer().getShape(n2).getShape();
            if (overlap(s1, s2)) {
              error.add(n1);
              error.add(n2);
            }
          }
        }
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
    return error;
  }
  
  private boolean overlap(Shape s1, Shape s2)
  {
    if (s1.equals(s2)) {
      return true;
    }
    Rectangle2D r1 = s1.getBounds2D();
    Rectangle2D r2 = s2.getBounds2D();
    return r1.intersects(r2) && !(r1.contains(r2) || r2.contains(r1));
  }
}
