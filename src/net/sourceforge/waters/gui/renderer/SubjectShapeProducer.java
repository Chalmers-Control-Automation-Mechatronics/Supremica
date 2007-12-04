//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   SubjectShapeProducer
//###########################################################################
//# $Id: SubjectShapeProducer.java,v 1.30 2007-12-04 03:22:55 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui.renderer;

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
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;
import net.sourceforge.waters.subject.module.SplineGeometrySubject;
import net.sourceforge.waters.xsd.module.SplineKind;


public class SubjectShapeProducer
  extends ProxyShapeProducer
  implements ModelObserver
{

  //##########################################################################
  //# Constructors
  public SubjectShapeProducer(final GraphSubject graph,
                              final ModuleProxy module)
  {
    this(graph, graph, module);
  }

  public SubjectShapeProducer(final GraphProxy graph,
                              final Subject subject,
                              final ModuleProxy module)
  {
    super(graph, module);
    mSubject = subject;
    subject.addModelObserver(this);
  }


  //##########################################################################
  //# Clean up
  public void close()
  {
    mSubject.removeModelObserver(this);
    super.close();
  }


  //##########################################################################
  //# Cleaning the Cache
  private void removeMapping(final SimpleNodeProxy node)
  {
    unmap(node);
    removeMapping(node.getLabelGeometry());
  }

  private void removeMapping(final GroupNodeProxy node)
  {
    unmap(node);
  }

  private void removeMapping(final LabelGeometryProxy label)
  {
    unmap(label);
  }

  private void removeMapping(final EdgeProxy edge)
  {
    unmap(edge);
    removeMapping(edge.getLabelBlock());
    final GuardActionBlockProxy block = edge.getGuardActionBlock();
    if (block != null) {
      removeMapping(block);
    }
  }

  private void removeMapping(final LabelBlockProxy label)
  {
    unmap(label);
    for (final Proxy proxy : label.getEventList()) {
      unmap(proxy);
    }
  }

  private void removeMapping(final GuardActionBlockProxy block)
  {
    unmap(block);
    for (final SimpleExpressionProxy guard : block.getGuards()) {
      unmap(guard);
    }
    for (final BinaryExpressionProxy action : block.getActions()) {
      unmap(action);
    }
  }

  private void removeMapping(final Subject subject)
  {
    if (subject instanceof SimpleNodeProxy) {
      removeMapping((SimpleNodeProxy) subject);
    } else if (subject instanceof GroupNodeProxy) {
      removeMapping((GroupNodeProxy) subject);
    } else if (subject instanceof EdgeProxy) {
      removeMapping((EdgeProxy) subject);
    } else if (subject instanceof LabelBlockProxy) {
      removeMapping((LabelBlockProxy) subject);
    } else if (subject instanceof GuardActionBlockProxy) {
      removeMapping((GuardActionBlockProxy)subject);
    } else if (subject instanceof LabelGeometryProxy) {
      removeMapping((LabelGeometryProxy)subject);
    }
  }


  //##########################################################################
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
        for (final EdgeProxy edge : getGraph().getEdges()) {
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


  //##########################################################################
  //# Smarter Lookup Using Parents
  public EdgeProxyShape visitEdgeProxy(final EdgeProxy edge)
  {
    return createEdgeProxyShape(edge);
  }

  public GuardActionBlockProxyShape visitGuardActionBlockProxy
    (final GuardActionBlockProxy block)
  {
    final GuardActionBlockProxyShape shape =
      (GuardActionBlockProxyShape) lookup(block);
    if (shape != null) {
      return shape;
    } else {
      final GuardActionBlockSubject subject = (GuardActionBlockSubject) block;
      final EdgeSubject edge = (EdgeSubject) subject.getParent();
      final EdgeProxyShape eshape = visitEdgeProxy(edge);
      return createGuardActionBlockShape(block, eshape);
    }
  }

  public LabelBlockProxyShape visitLabelBlockProxy(final LabelBlockProxy block)
  {
    final LabelBlockProxyShape shape = (LabelBlockProxyShape) lookup(block);
    if (shape != null) {
      return shape;
    } else {
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


  //##########################################################################
  //# Data Members
  private final Subject mSubject;

}
