//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: wnet.sourceforge.aters.gui
//# CLASS:   EditorObject
//###########################################################################
//# $Id$
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.*;
import java.awt.geom.Point2D;

import net.sourceforge.waters.subject.base.Subject;

/**
 * <p>The super-class for all objects internally stored within an
 * EditorSurface.</p>
 *
 * @author Gian Perrone
 */

public abstract class EditorObject
{
    protected boolean visible;
    private int hash = 0;
    
    public EditorObject()
    {
    }
    
    public void drawObject(Graphics g, boolean selected)
    {
    }
    
    public abstract int getX();
    
    public abstract int getY();
    
    public abstract void setPosition(double x, double y);
    
    public abstract Point2D getPosition();
    
    public abstract Subject getSubject();
}
