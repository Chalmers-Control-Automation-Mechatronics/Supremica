//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   MoveEdgeCommand
//###########################################################################
//# $Id: MoveEdgeCommand.java,v 1.7 2006-11-03 15:01:56 torda Exp $
//###########################################################################


package net.sourceforge.waters.gui.command;

import java.awt.geom.Point2D;
import java.awt.Point;

import java.util.Collection;
import java.util.Collections;

import net.sourceforge.waters.gui.ControlledSurface;
import net.sourceforge.waters.gui.renderer.GeometryTools;

import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.PointGeometrySubject;
import net.sourceforge.waters.subject.module.SplineGeometrySubject;
import net.sourceforge.waters.xsd.module.SplineKind;


/**
 * the Command for Creation of nodes
 *
 * @author Simon Ware
 */

public class MoveEdgeCommand
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
    public MoveEdgeCommand(final ControlledSurface surface,
						   final EdgeSubject edge,
						   final NodeSubject neo,
						   boolean source,
						   int x,
						   int y)
    {
      mSurface = surface;
      mEdge = edge;
      mNew = neo;
      mSource = source;
      mNPos = new PointGeometrySubject(new Point(x, y));
      if (source)
      {
        mOPos = new PointGeometrySubject(edge.getStartPoint().getPoint());
        mOld = edge.getSource();
        mDescription = "Change Edge Source";
      }
      else
      {
        mOPos = new PointGeometrySubject(edge.getEndPoint().getPoint());
        mOld = edge.getTarget();
        mDescription = "Change Edge Target";
      }
		  mOTPoint = edge.getGeometry().clone();
    }

    /**
     * Executes the Creation of the Node
     */
    public void execute()
    {
		if (mSource)
		{
			mEdge.setSource(mNew);
			mEdge.setStartPoint(mNPos);
		}
		else
		{
			mEdge.setTarget(mNew);
			mEdge.setEndPoint(mNPos);
		}
		final Collection<Point2D> points;
    if (mEdge.getTarget() != mEdge.getSource()) {
      points = Collections.singleton(
                    GeometryTools.getMidPoint(mEdge.getStartPoint().getPoint(),
										mEdge.getEndPoint().getPoint()));
    } else {
      Point2D p = mEdge.getStartPoint().getPoint();
      p.setLocation(p.getX() + 20, p.getY() + 20);
      points = Collections.singleton(p);
    }
		mEdge.setGeometry(
			new SplineGeometrySubject(points, SplineKind.INTERPOLATING));
		mSurface.getEditorInterface().setDisplayed();
    }

    /** 
     * Undoes the Command
     */    
    public void undo()
    {
      if (mSource)
      {
        mEdge.setSource(mOld);
        mEdge.setStartPoint(mOPos);
      }
      else
      {
        mEdge.setTarget(mOld);
        mEdge.setEndPoint(mOPos);
      }
      mEdge.setGeometry(mOTPoint);
      mSurface.getEditorInterface().setDisplayed();
    }
	
	public boolean isSignificant()
	{
		return true;
	}

  public String getName()
  {
		return mDescription;
  }


	//#######################################################################
	//# Data Members
	private final ControlledSurface mSurface;
	private final EdgeSubject mEdge;
	private final NodeSubject mOld;
	private final NodeSubject mNew;
	private final boolean mSource;
	private final PointGeometrySubject mNPos;
	private final PointGeometrySubject mOPos;
	private final SplineGeometrySubject mOTPoint;
	private final String mDescription;	
}
