//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   CreateEdgeCommand
//###########################################################################
//# $Id: CreateEdgeCommand.java,v 1.11 2006-10-31 16:50:44 martin Exp $
//###########################################################################


package net.sourceforge.waters.gui.command;

import java.awt.geom.Point2D;
import java.awt.Point;

import java.util.Collection;
import java.util.Collections;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import net.sourceforge.waters.gui.renderer.LabelBlockProxyShape;

import net.sourceforge.waters.gui.ControlledSurface;
import net.sourceforge.waters.gui.renderer.GeometryTools;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.PointGeometrySubject;
import net.sourceforge.waters.subject.module.SplineGeometrySubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import net.sourceforge.waters.subject.module.LabelBlockSubject;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.GraphSubject;

import net.sourceforge.waters.xsd.module.SplineKind;


/**
 * the Command for Creation of nodes
 *
 * @author Simon Ware
 */

public class CreateEdgeCommand
    implements Command
{

	//#######################################################################
	//# Constructor
    /**
     * Constructs a new CreateNodeCommand with the specified surface and
     * creates the node in the x,y position specified.
     * @param surface the surface edited by this command
     * @param x the position upon which the node is created
     * @param y the position upon which the node is created
     */
    public CreateEdgeCommand(final GraphSubject graph,
                             final NodeSubject source,
                             final NodeSubject target,
                             final Point2D startPoint,
                             final Point2D endPoint)
    {
		mGraph = graph; 
		final Collection<Proxy> empty = Collections.emptyList();
    final Collection<Point2D> points;
    if (!startPoint.equals(endPoint)) {
       points = Collections.singleton(
         GeometryTools.getMidPoint(startPoint, endPoint));
    } else {
      points = Collections.singleton((Point2D) new Point((int)endPoint.getX() + 20,
                                                         (int)endPoint.getY() + 20));
    }
		final LabelGeometrySubject offset = new LabelGeometrySubject(
			new Point(LabelBlockProxyShape.DEFAULTOFFSETX,
						LabelBlockProxyShape.DEFAULTOFFSETY));
		final LabelBlockSubject labelBlock = 
			new LabelBlockSubject(empty, offset);
		final GuardActionBlockSubject guardActionBlock = 
			new GuardActionBlockSubject(null, null, offset);
		final SplineGeometrySubject spline = 
			new SplineGeometrySubject(points, SplineKind.INTERPOLATING);
		final PointGeometrySubject start = new PointGeometrySubject(startPoint);
		final PointGeometrySubject end = new PointGeometrySubject(endPoint);
		
		mCreated =	new EdgeSubject(source, target, labelBlock, guardActionBlock,
                                spline, start, end);
    }

    /**
     * Executes the Creation of the Node
     */
    public void execute()
    {
		mGraph.getEdgesModifiable().add(mCreated);
    }

    /** 
     * Undoes the Command
     */
    public void undo()
    {
		mGraph.getEdgesModifiable().remove(mCreated);
    }

    public String getName()
    {
		return mDescription;
    }
	
	public boolean isSignificant()
	{
		return true;
	}


	//#######################################################################
	//# Data Members
    /** The ControlledSurface Edited with this Command */
    private final GraphSubject mGraph;
    /** The Node Created by this Command */
    private final EdgeSubject mCreated;
    private final String mDescription = "Edge Creation";

}
