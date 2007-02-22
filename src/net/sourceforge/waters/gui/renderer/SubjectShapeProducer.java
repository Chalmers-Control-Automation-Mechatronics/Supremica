//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   SubjectShapeProducer
//###########################################################################
//# $Id: SubjectShapeProducer.java,v 1.27 2007-02-22 03:08:31 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui.renderer;

import java.awt.Frame;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.swing.JDialog;

import net.sourceforge.waters.gui.EditorGraph;
import net.sourceforge.waters.gui.springembedder.SpringAbortDialog;
import net.sourceforge.waters.gui.springembedder.SpringEmbedder;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.EventListExpressionSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.GroupNodeSubject;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.LabelBlockSubject;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.PointGeometrySubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;
import net.sourceforge.waters.subject.module.SplineGeometrySubject;
import net.sourceforge.waters.xsd.module.SplineKind;

import org.supremica.properties.Config;


public class SubjectShapeProducer
  extends ProxyShapeProducer
  implements ModelObserver
{
	
  //########################################################################
  //# Constructors
  public SubjectShapeProducer(final GraphSubject graph,
                              final ModuleProxy module,
                              final Frame root)
  {
    super(module);
    mGraph = graph;
		graph.addModelObserver(this);
	}
	
  public SubjectShapeProducer(final Subject graph,
                              final ModuleProxy module)
  {
    super(module);
    mGraph = (GraphProxy) graph;
    graph.addModelObserver(this);
  }
	

  //########################################################################
  //# Overrides for Base Class ProxyShapeProducer
  public GuardActionBlockProxyShape visitGuardActionBlockProxy
    (final GuardActionBlockProxy ga)
  {
    final GuardActionBlockProxyShape shape =
      (GuardActionBlockProxyShape) getMap().get(ga);
    if (shape != null) {
      return shape;
    } else {
      // Tricky --- a guard/action block may have been uncached independently
      // from its edge. We must provide the parent shape!
      final GuardActionBlockSubject subject = (GuardActionBlockSubject) ga;
      final EdgeProxy edge = (EdgeProxy) subject.getParent();
      final EdgeProxyShape eshape = visitEdgeProxy(edge);
      return createGuardActionBlockShape(ga, eshape);
    }
  }

  public LabelBlockProxyShape visitLabelBlockProxy(final LabelBlockProxy block)
  {
    final LabelBlockProxyShape shape =
      (LabelBlockProxyShape) getMap().get(block);
    if (shape != null) {
      return shape;
    } else {
      // Tricky --- a label block may have been uncached independently
      // from its edge. We must provide the parent shape!
      final LabelBlockSubject subject = (LabelBlockSubject) block;
      final Subject parent = subject.getParent();
      if (parent instanceof EdgeProxy) {
        final EdgeProxy edge = (EdgeProxy) parent;
        final EdgeProxyShape eshape = visitEdgeProxy(edge);
        return createLabelBlockShape(block, eshape);
      } else {
        return createLabelBlockShape(block, null);
      }
    }
  }


  //########################################################################
  //# Cleaning the Cache	
	private void removeMapping(NodeProxy node)
	{
		getMap().remove(node);
    if (node instanceof SimpleNodeProxy) {
      removeMapping(((SimpleNodeProxy)node).getLabelGeometry());
    }
	}
	
	private void removeMapping(GroupNodeProxy node)
	{
		getMap().remove(node);
	}
	
	private void removeMapping(LabelGeometryProxy label)
	{
		getMap().remove(label);
	}
	
  private void removeMapping(final EdgeProxy edge)
  {
    getMap().remove(edge);
    removeMapping(edge.getLabelBlock());
    final GuardActionBlockProxy ga = edge.getGuardActionBlock();
    if (ga != null) {
      removeMapping(edge.getGuardActionBlock());
    }
  }
	
	private void removeMapping(LabelBlockProxy label)
	{
		getMap().remove(label);
		for (Proxy p : label.getEventList())
		{
			getMap().remove(p);
		}
	}
	
	private void removeMapping(GuardActionBlockProxy GA)
	{
		getMap().remove(GA);
		for(BinaryExpressionProxy action : GA.getActions())
		{
			getMap().remove(GA.getActions());
		}
		List<SimpleExpressionProxy> guards = GA.getGuards();
		if(!guards.isEmpty())
		{
			SimpleExpressionProxy guard = guards.get(0);
			getMap().remove(guard);
		}
	}
	
	private void removeMapping(Subject subject)
	{
		if (subject instanceof SimpleNodeProxy)
		{
			removeMapping((SimpleNodeProxy)subject);
		}
		if (subject instanceof GroupNodeProxy)
		{
			removeMapping((GroupNodeProxy)subject);
		}
		if (subject instanceof EdgeProxy)
		{
			removeMapping((EdgeProxy)subject);
		}
		if (subject instanceof LabelBlockProxy)
		{
			removeMapping((LabelBlockProxy)subject);
		}
		if (subject instanceof GuardActionBlockProxy)
		{
			removeMapping((GuardActionBlockProxy)subject);
		}
    if (subject instanceof LabelGeometryProxy)
		{
			removeMapping((LabelGeometryProxy)subject);
		}
	}

	
  //########################################################################
  //# Interface net.sourceforge.waters.subject.base.ModelObserver
  public void modelChanged(final ModelChangeEvent event)
  {
    final Subject esource = event.getSource();
    switch (event.getKind()) {
    case ModelChangeEvent.ITEM_REMOVED:
    case ModelChangeEvent.ITEM_ADDED:
      final Subject parent = esource.getParent();
      if (parent instanceof EventListExpressionSubject) {
        final Subject grandparent = parent.getParent();
        if (grandparent instanceof SimpleNodeProxy) {
          removeMapping((SimpleNodeProxy) grandparent);
        } else if (parent instanceof LabelBlockProxy) {
          removeMapping((LabelBlockProxy) parent);
        }
      } else if (parent instanceof GuardActionBlockProxy) {
        removeMapping((GuardActionBlockProxy) parent);
      } else {
        removeMapping((Subject) event.getValue());
      }
      break;
    case ModelChangeEvent.NAME_CHANGED:
      if (esource instanceof SimpleNodeProxy) {
        final SimpleNodeProxy node = (SimpleNodeProxy) esource;
        removeMapping(node.getLabelGeometry());
      } else {
        removeMapping(esource);
      }
      break;
    case ModelChangeEvent.GEOMETRY_CHANGED:
      if (esource instanceof NodeProxy) {
        for (final EdgeProxy edge : mGraph.getEdges()) {
          if (edge.getSource() == esource || edge.getTarget() == esource) {
            removeMapping(edge);
          }
        }
      }
      removeMapping(esource);
      break;
    default:
      removeMapping(esource);
    }
  }


  //########################################################################
  //# Data Members
  private final GraphProxy mGraph;

}
