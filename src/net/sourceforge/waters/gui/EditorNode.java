//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EditorNode
//###########################################################################
//# $Id: EditorNode.java,v 1.39 2006-07-10 17:02:15 knut Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.*;
import javax.swing.undo.UndoableEdit;

import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.ChangeNameCommand;
import net.sourceforge.waters.gui.command.UndoableCommand;
import net.sourceforge.waters.gui.command.UndoInterface;

import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.NodeMovedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.Subject;

import net.sourceforge.waters.model.base.DuplicateNameException;
import net.sourceforge.waters.model.base.IndexedList;
import net.sourceforge.waters.model.base.GeometryProxy;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.module.ColorGeometrySubject;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.EventListExpressionSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.PointGeometrySubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;
import net.sourceforge.waters.xsd.module.AnchorPosition;

import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.subject.module.IndexedIdentifierSubject;

/**
 * <p>The internal editor representation of node objects.</p>
 *
 * <p>Nodes store geometry and {@link EditorShade} information.</p>
 *
 * @author Gian Perrone
 */
public class EditorNode
    extends EditorObject
    implements ModelObserver,
				Subject
{

	//########################################################################
	//# Constructor
	public EditorNode(final int x, final int y,
					  final SimpleNodeSubject subject,
					  final EditorSurface surface)
	{
		this(subject, surface, new Point2D.Double(x, y));
	}

	public EditorNode(final SimpleNodeSubject subject,
					  final EditorSurface surface)
	{
		this(subject, surface, null);
	}

	private EditorNode(final SimpleNodeSubject subject,
					   final EditorSurface surface,
					   final Point2D altpos)
	{
		mUndo = surface.getEditorInterface().getUndoInterface();

		// This is a node
		type = NODE;

		// Variables
		mSubject = subject;

		if (subject.getPointGeometry() == null) {
			subject.setPointGeometry(new PointGeometrySubject(new Point2D.Double(1000,1000)));
		}
		if (altpos != null) {
			subject.getPointGeometry().setPoint(altpos);
		}

		// Init propositions
		propGroup = new EditorPropGroup(this, surface);
		mSubject.addModelObserver(this);
		mModule = (ModuleSubject) mSubject.getDocument();
		if (mModule != null) {
			mModule.getEventDeclListModifiable().addModelObserver(this);
		}
		updateColors();
	}

	public EditorPropGroup getPropGroup()
	{
		return propGroup;
	}

    public void addProposition(IdentifierSubject ident)
    {
		final EventListExpressionSubject props = mSubject.getPropositions();
		final List<AbstractSubject> list = props.getEventListModifiable();
		list.add(ident);
    }

	public void removeProposition(IdentifierSubject ident)
	{
		final EventListExpressionSubject props = mSubject.getPropositions();
		final List<AbstractSubject> list = props.getEventListModifiable();
		list.remove(ident);
    }

    public boolean hasPropositions()
    {
		final EventListExpressionSubject props = mSubject.getPropositions();
		final List<AbstractSubject> list = props.getEventListModifiable();
		return (list.size() != 0);
    }

	/**
	 * If one color is enough for the marking, this is a way to do it.
	 */
    public void addDefaultProposition()
    {
		final String DEFAULTNAME = "omega";
		// Add to propositionlist in node
		final EventListExpressionSubject props = mSubject.getPropositions();
		final List<AbstractSubject> list = props.getEventListModifiable();
		list.add(new IndexedIdentifierSubject(DEFAULTNAME));
		// Add to eventlist in module
		final IndexedList<EventDeclSubject> decls = mModule.getEventDeclListModifiable();
		decls.add(new EventDeclSubject(DEFAULTNAME, EventKind.PROPOSITION));
    }

    public void clearPropositions()
    {
		final EventListExpressionSubject props = mSubject.getPropositions();
		final List<AbstractSubject> list = props.getEventListModifiable();
		list.clear();
    }

    public void attach(Observer o)
    {
		mObservers.add(o);
    }

    public void detach(Observer o)
    {
		mObservers.remove(o);
    }

	public void fireEditorChangedEvent(EditorChangedEvent e)
	{
		for (Observer o : mObservers)
		{
			o.update(e);
		}
	}

	public void setInitial(boolean newinitial)
	{
		mSubject.setInitial(newinitial);
	}

	public int hashCode()
	{
		return mSubject.hashCode();
	}

	/**
	 * Returns true if this node is initial.
	 */
	public boolean isInitial()
	{
		return mSubject.isInitial();
	}

	/**
	 * Returns true if this node is forbidden.
	 */
	public boolean isForbidden()
	{
		return false;
	}

	public boolean setName(String n, JComponent c)
	{
		if (n.length() == 0)
		{
			return false;
		}

		try
		{
			mSubject.setName(n);
		}
		catch (final DuplicateNameException e)
		{
			JOptionPane.showMessageDialog(c, e.getMessage());

			return false;
		}
		return true;
	}

	public String getName()
	{
		return mSubject.getName();
	}

	public void setX(int newxposition)
	{
	    setPosition(newxposition, getY());
	}

	public void setY(int newyposition)
	{
	    setPosition(getX(), newyposition);
	}

    public void setPosition(double x, double y)
    {
		GeometryProxy old = new PointGeometrySubject((Point2D)mSubject.getPointGeometry().getPoint().clone());
		mSubject.getPointGeometry().setPoint(new Point2D.Double(x, y));
		fireEditorChangedEvent(new NodeMovedEvent(old,
												  mSubject.getPointGeometry(),
												  mSubject));
    }

    public Point2D getPosition()
    {
		return mSubject.getPointGeometry().getPoint();
    }

	public int getX()
	{
		return (int) getPosition().getX();
	}

	public int getY()
	{
		return (int) getPosition().getY();
	}

	/**
	 * Returns true if the position (x, y) is above the drawn node
	 * (approximately).
	 */
	public boolean wasClicked(int x, int y)
	{
		// Within the square? Why not circle?
		return (((getX() - RADIUS) <= x) &&
				(x <= (getX() + RADIUS)) &&
				((getY() - RADIUS) <= y) &&
				(y <= (getY() + RADIUS)));
	}

	public void updateColors()
	{
		mColors.clear();
		if (mModule != null) {
			final IndexedList<EventDeclSubject> decls =
				mModule.getEventDeclListModifiable();
			final EventListExpressionSubject props =
				mSubject.getPropositions();
			final List<AbstractSubject> list = props.getEventListModifiable();
			for (final AbstractSubject prop : list) {
				// BUG: ForeachEventSubject not supported!
				final IdentifierSubject ident = (IdentifierSubject) prop;
				final String name = ident.getName();
				final EventDeclSubject decl = decls.get(name);
				if (decl == null) {
					mColors.add(EditorColor.DEFAULTMARKINGCOLOR);
					continue;
				}
				final ColorGeometrySubject geo = decl.getColorGeometry();
				if (geo == null) {
					mColors.add(EditorColor.DEFAULTMARKINGCOLOR);
					continue;
				}
				mColors.addAll(geo.getColorSet());
			}
		}
	}

	public SimpleNodeSubject getSubject()
	{
		return mSubject;
	}

	/**
	 * Returns an ellipse that outlines the node.
	 */
	public Ellipse2D.Double getEllipsicalOutline()
	{
		return new Ellipse2D.Double(getX()-RADIUS, getY()-RADIUS, RADIUS*2, RADIUS*2);
	}

	/**
	 * Returns a rectangle that outlines the node.
	 */
	public Rectangle2D.Double getRectangularOutline()
	{
		return new Rectangle2D.Double(getX()-RADIUS, getY()-RADIUS, RADIUS*2, RADIUS*2);
	}

	public void drawObject(Graphics g, boolean selected)
	{
		Graphics2D g2d = (Graphics2D) g;
		g2d.setStroke(BASICSTROKE);

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(getColor(selected));

		propGroup.setPanelLocation();
		if (selected)
		{
			propGroup.setVisible(false);
		}

		// Draw shadow?
		if (shadow && isHighlighted())
		{
			g2d.setStroke(SHADOWSTROKE);
			g2d.setColor(getShadowColor(selected));
			g2d.drawOval(getX() - RADIUS, getY() - RADIUS, RADIUS*2, RADIUS*2);
			g2d.setColor(getColor(selected));
			g2d.setStroke(BASICSTROKE);
		}

		// Draw the inside of the node
		if (mColors.size() == 0)
		{
			// There is no marking!
			// Draw the background white!
			g2d.setColor(Color.WHITE);
			g2d.fillOval(getX() - RADIUS, getY() - RADIUS, RADIUS*2, RADIUS*2);
		}
		else if (mColors.size() <= MAXDRAWNMARKINGS)
		{
			Arc2D.Double a = new Arc2D.Double();
			double startAngle = 0;
			double deltaAngle = (double) (360/mColors.size());
			for (final Color color : mColors) {
				// There are markings but they are fewer than
				// MAXDRAWNMARKINGS+1!
				// Draw nice colored pies!!
				a.setArcByCenter(getX(), getY(), RADIUS,
								 startAngle, deltaAngle, Arc2D.PIE);
				startAngle += deltaAngle;
				g2d.setColor(color);
				g2d.fill(a);
			}
		}
		else
		{
			// More than MAXDRAWNMARKINGS markings! Use the default marking color and draw a cross on top!
			g2d.setColor(EditorColor.DEFAULTMARKINGCOLOR);
			g2d.fillOval(getX() - RADIUS, getY() - RADIUS, RADIUS*2, RADIUS*2);

			//g2d.setColor(EditorColor.DEFAULTCOLOR);
			g2d.setColor(Color.WHITE);
			g2d.drawLine(getX(), getY() - RADIUS, getX(), getY() + RADIUS);
			g2d.drawLine(getX() - RADIUS, getY(), getX() + RADIUS, getY());
		}

		// Draw the border of the node
		g2d.setColor(getColor(selected));
		if (isInitial())
		{
			// Arrow or thick border on initial states?
			if (true)
				// Draw initial state arrow
				drawInitialStateArrow(g2d);
			else
				// Draw border thicker!
				g2d.setStroke(DOUBLESTROKE);
		}
		g2d.drawOval(getX()-RADIUS, getY()-RADIUS, RADIUS*2, RADIUS*2);
		g2d.setStroke(BASICSTROKE);
		if (isForbidden())
		{
			g2d.setColor(Color.RED);
			g2d.drawLine(getX() - (RADIUS+1), getY() - (RADIUS+1), getX() + (RADIUS+1), getY() + (RADIUS+1));
			g2d.drawLine(getX() + (RADIUS+1), getY() - (RADIUS+1), getX() - (RADIUS+1), getY() + (RADIUS+1));
		}
		/*
		// Draw a double circle on marked nodes... (one color marking only?)
		if (mColors.size() > 0)
			g2d.drawOval(getX()-RADIUS+2, getY()-RADIUS+2, RADIUS*2-4, RADIUS*2-4);
		*/
	}

	private void drawInitialStateArrow(Graphics2D g2d)
	{
		// Draw line
		int borderX = getX() + (int) ((RADIUS+4) * Math.sin(INITARROWANGLE)); // Why +4?
		int borderY = getY() + (int) ((RADIUS+4) * Math.cos(INITARROWANGLE)); // Why +4?
		int outerX = borderX + (int) (INITARROWLENGTH * Math.sin(INITARROWANGLE));
		int outerY = borderY + (int) (INITARROWLENGTH * Math.cos(INITARROWANGLE));
		g2d.setStroke(BASICSTROKE);
		g2d.drawLine(borderX, borderY, outerX, outerY);

		// Draw arrow
		double theta = INITARROWANGLE;
		int x = (int) Math.ceil(getX() + Math.sin(theta)*EditorNode.RADIUS);
		int y = (int) Math.ceil(getY() + Math.cos(theta)*EditorNode.RADIUS);
		EditorEdge.drawArrow(x, y, theta, g2d);
	}

    public void modelChanged(final ModelChangeEvent event)
    {
		//System.err.println("EditorNode modelChanged");
		switch (event.getKind()) {
		case ModelChangeEvent.ITEM_ADDED:
			if (event.getValue() == mSubject) {
				mModule = (ModuleSubject) mSubject.getDocument();
				/*
				if (mModule == null)
				{
					System.err.println("EditorNode: mModule == null");
				}
				if (mModule.getEventDeclListModifiable() == null)
				{
					System.err.println("EditorNode: mModule.getEventDeclListModifiable()  == null");
				}
				*/
				mModule.getEventDeclListModifiable().addModelObserver(this);
				updateColors();
			} else if (event.getValue() instanceof IdentifierSubject) {
				updateColors();
			}
			break;
		case ModelChangeEvent.ITEM_REMOVED:
			if (event.getValue() == mSubject) {
				mModule.getEventDeclListModifiable().removeModelObserver(this);
				mModule = null;
				updateColors();
			} else if (event.getValue() instanceof IdentifierSubject) {
				updateColors();
			}
			break;
		case ModelChangeEvent.GEOMETRY_CHANGED:
			if (event.getSource() instanceof ColorGeometrySubject) {
				updateColors();
			}
			break;
		}
    }

	//########################################################################
	//# Data Members
	protected int hash = 0;
	private final SimpleNodeSubject mSubject;
	private final Collection<Observer> mObservers = new HashSet<Observer>();
    private final Set<Color> mColors = new HashSet<Color>();
	private final UndoInterface mUndo;
	private ModuleSubject mModule;
	private EditorPropGroup propGroup;

	// Constants

	/** Radius of node. */
	public static final int RADIUS = 6;
	/** Angle of initial state arrow -- 135 degrees (plus correction) */
	public static final double INITARROWANGLE = 3*Math.PI/4 + Math.PI/2;
	/** Length of initial state arrow. */
	public static final int INITARROWLENGTH = 15;
	/** Maximum number of colors shown in a node. */
	private static final int MAXDRAWNMARKINGS = 4;

}
