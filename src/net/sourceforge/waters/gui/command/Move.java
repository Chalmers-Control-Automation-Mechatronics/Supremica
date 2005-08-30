package net.sourceforge.waters.gui.command;

import javax.swing.undo.AbstractUndoableEdit;
import java.awt.geom.Point2D;

public abstract class Move
    extends AbstractUndoableEdit
{
    abstract public void setDisplacement(Point2D neo);
    
    abstract public Point2D getDisplacement();
}
