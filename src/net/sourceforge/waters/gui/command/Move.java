package net.sourceforge.waters.gui.command;

import javax.swing.undo.AbstractUndoableEdit;
import java.awt.geom.Point2D;

public interface Move
    extends Command
{
    public void setDisplacement(Point2D neo);
    
    public Point2D getDisplacement();
}
