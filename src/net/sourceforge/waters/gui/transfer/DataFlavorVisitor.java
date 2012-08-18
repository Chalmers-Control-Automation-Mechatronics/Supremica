//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.gui.transfer
//# CLASS:   DataFlavorVisitor
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.transfer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.ConstantAliasProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventAliasProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.subject.module.LabelBlockSubject;


/**
 * <P>A utility class to determine the data flavours that can be supported
 * by a given collection of {@link Proxy} objects. This visitor is not
 * intended to be used directly, please call the static methods in class
 * {@link WaterDataFlavor} instead.</P>
 *
 * @see WatersDataFlavor
 * @author Robi Malik
 */

class DataFlavorVisitor extends DefaultModuleProxyVisitor
{

  //#########################################################################
  //# Singleton Pattern
  static DataFlavorVisitor getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private static class SingletonHolder {
    private static final DataFlavorVisitor INSTANCE =
      new DataFlavorVisitor();
  }


  //#########################################################################
  //# Constructors
  private DataFlavorVisitor()
  {
  }


  //#########################################################################
  //# Invocation
  /**
   * Determines whether the given data can be stored in a {@link
   * java.awt.datatransfer.Transferable Transferable}.
   * @param  data    Collection of objects to be stored in a transferable.
   *                 The data needs to be reduced, so it does not contain
   *                 any two objects such that one is an ancestor of the
   *                 other.
   * @return <CODE>true</CODE> if a nonempty list of data flavours can be
   *         found to represent the given data, <CODE>false</CODE> otherwise.
   * @see WatersDataFlavor#canCopy(Collection) WatersDataFlavor.canCopy()
   */
  boolean canCopy(final Collection<? extends Proxy> data)
  {
    try {
      if (data.isEmpty()) {
        return false;
      } else {
        List<WatersDataFlavor> flavors = visitCollection(data);
        if (!flavors.isEmpty()) {
          final WatersDataFlavor flavor0 = flavors.get(0);
          flavors = flavor0.reduceDataFlavorList(flavors);
        }
        return !flavors.isEmpty();
      }
    } catch (final VisitorException exception) {
      return false;
    }
  }

  /**
   * Determines a list of data flavours for the given collection.
   * This method checks all items in the given collection and returns a
   * list of data flavours that can support all items.
   * @param  data    Collection of objects to be stored in a transferable.
   *                 The data needs to be reduced, so it does not contain
   *                 any two objects such that one is an ancestor of the
   *                 other.
   * @return List of data flavours in order of preference. The first element
   *         in the list is the <I>primary</I> data flavour.
   * @throws {@link IllegalArgumentException} to indicate that no data flavour
   *         could be identified that supports all the data.
   * @see WatersDataFlavor
   */
  List<WatersDataFlavor> getTransferDataFlavors
    (final Collection<? extends Proxy> data)
  {
    try {
      if (data.isEmpty()) {
        throw new IllegalArgumentException
          ("Can't determine data flavour for empty list of data!");
      } else {
        mFailedObject = null;
        List<WatersDataFlavor> flavors = visitCollection(data);
        if (!flavors.isEmpty()) {
          final WatersDataFlavor flavor0 = flavors.get(0);
          flavors = flavor0.reduceDataFlavorList(flavors);
        }
        if (!flavors.isEmpty()) {
          return flavors;
        } else {
          throw new IllegalArgumentException
            ("Can't determine data flavour for object of type " +
             ProxyTools.getShortClassName(mFailedObject) + "!");
        }
      }
    } catch (final VisitorException exception) {
      throw exception.getRuntimeException();
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.ProxyVisitor
  @Override
  public List<WatersDataFlavor> visitProxy(final Proxy proxy)
  {
    if (mFailedObject == null) {
      mFailedObject = proxy;
    }
    return Collections.emptyList();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  @Override
  public List<WatersDataFlavor> visitComponentProxy(final ComponentProxy comp)
  {
    return LIST_COMPONENT;
  }

  @Override
  public List<WatersDataFlavor> visitConstantAliasProxy
    (final ConstantAliasProxy alias)
  {
    return LIST_CONSTANT_ALIAS;
  }

  @Override
  public List<WatersDataFlavor> visitEdgeProxy(final EdgeProxy edge)
    throws VisitorException
  {
    final LabelBlockProxy block = edge.getLabelBlock();
    if (block == null) {
      return LIST_EDGE;
    }
    final List<WatersDataFlavor> flavors = visitLabelBlockProxy(block);
    if (flavors.isEmpty()) {
      return LIST_EDGE;
    }
    final int size = LIST_EDGE.size() + flavors.size();
    final List<WatersDataFlavor> combined =
      new ArrayList<WatersDataFlavor>(size);
    combined.addAll(LIST_EDGE);
    combined.addAll(flavors);
    return combined;
  }

  @Override
  public List<WatersDataFlavor> visitEventAliasProxy
    (final EventAliasProxy alias)
  {
    return LIST_EVENT_ALIAS;
  }

  @Override
  public List<WatersDataFlavor> visitEventDeclProxy(final EventDeclProxy decl)
  {
    return LIST_EVENT_DECL;
  }

  @Override
  public List<WatersDataFlavor> visitEventListExpressionProxy
    (final EventListExpressionProxy elist)
  throws VisitorException
  {
    final List<Proxy> list = elist.getEventIdentifierList();
    return visitCollection(list);
  }

  @Override
  public List<WatersDataFlavor> visitForeachProxy(final ForeachProxy foreach)
    throws VisitorException
  {
    final Collection<? extends Proxy> body = foreach.getBody();
    if (body.isEmpty()) {
      return LIST_FOREACH;
    } else {
      return visitCollection(body);
    }
  }

  @Override
  public List<WatersDataFlavor> visitGuardActionBlockProxy
    (final GuardActionBlockProxy block)
  {
    return LIST_GUARD_ACTION_BLOCK;
  }

  @Override
  public List<WatersDataFlavor> visitIdentifierProxy
    (final IdentifierProxy ident)
  {
    return LIST_IDENTIFIER;
  }

  @Override
  public List<WatersDataFlavor> visitLabelBlockProxy
    (final LabelBlockProxy block)
  throws VisitorException
  {
    final List<Proxy> list = block.getEventIdentifierList();
    if(list.isEmpty()){
      return LIST_GRAPH;
    }
    final List<WatersDataFlavor> flavors = visitEventListExpressionProxy(block);
    final LabelBlockSubject subject = (LabelBlockSubject) block;
    if (subject.getParent() instanceof GraphProxy) {
      if (flavors.isEmpty()) {
        return LIST_GRAPH;
      } else {
        final int size = LIST_GRAPH.size() + flavors.size();
        final List<WatersDataFlavor> combined =
          new ArrayList<WatersDataFlavor>(size);
        combined.addAll(LIST_GRAPH);
        combined.addAll(flavors);
        return combined;
      }
    } else {
      return flavors;
    }
  }

  @Override
  public List<WatersDataFlavor> visitLabelGeometryProxy
    (final LabelGeometryProxy geo)
  {
    return LIST_LABEL_GEOMETRY;
  }

  @Override
  public List<WatersDataFlavor> visitNodeProxy(final NodeProxy node)
  {
    return LIST_GRAPH;
  }

  @Override
  public List<WatersDataFlavor> visitParameterBindingProxy
    (final ParameterBindingProxy binding)
  {
    return LIST_PARAMETER_BINDING;
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.model.base.AbstractModuleProxyVisitor
  @Override
  public List<WatersDataFlavor> visitCollection
    (final Collection<? extends Proxy> collection)
  throws VisitorException
  {
    List<WatersDataFlavor> list = Collections.emptyList();
    for (final Proxy proxy : collection) {
      @SuppressWarnings("unchecked")
      final List<WatersDataFlavor> current =
        (List<WatersDataFlavor>) proxy.acceptVisitor(this);
      if (current.isEmpty()) {
        return current;
      } else if (list.isEmpty()) {
        list = new ArrayList<WatersDataFlavor>(current);
      } else {
        list.retainAll(current);
        if (list.isEmpty()) {
          break;
        }
      }
    }
    return list;
  }


  //#########################################################################
  //# Data Members
  private Proxy mFailedObject;


  //#########################################################################
  //# Class Constants
  private static final List<WatersDataFlavor> LIST_COMPONENT =
    Collections.singletonList(WatersDataFlavor.COMPONENT);
  private static final List<WatersDataFlavor> LIST_CONSTANT_ALIAS =
    Collections.singletonList(WatersDataFlavor.CONSTANT_ALIAS);
  private static final List<WatersDataFlavor> LIST_EDGE =
    Arrays.asList(new WatersDataFlavor[]{WatersDataFlavor.EDGE,
                                         WatersDataFlavor.GRAPH});
  private static final List<WatersDataFlavor> LIST_EVENT_ALIAS =
    Arrays.asList(new WatersDataFlavor[]{WatersDataFlavor.EVENT_ALIAS,
                                         WatersDataFlavor.IDENTIFIER});
  private static final List<WatersDataFlavor> LIST_EVENT_DECL =
    Arrays.asList(new WatersDataFlavor[]{WatersDataFlavor.EVENT_DECL,
                                         WatersDataFlavor.IDENTIFIER});
  private static final List<WatersDataFlavor> LIST_GRAPH =
    Collections.singletonList(WatersDataFlavor.GRAPH);
  private static final List<WatersDataFlavor> LIST_GUARD_ACTION_BLOCK =
    Collections.singletonList(WatersDataFlavor.GUARD_ACTION_BLOCK);
  private static final List<WatersDataFlavor> LIST_IDENTIFIER =
    Collections.singletonList(WatersDataFlavor.IDENTIFIER);
  private static final List<WatersDataFlavor> LIST_FOREACH =
    Arrays.asList(new WatersDataFlavor[]{WatersDataFlavor.COMPONENT,
                                         WatersDataFlavor.EVENT_ALIAS,
                                         WatersDataFlavor.IDENTIFIER});
  private static final List<WatersDataFlavor> LIST_LABEL_GEOMETRY =
    Arrays.asList(new WatersDataFlavor[]{WatersDataFlavor.LABEL_GEOMETRY,
                                         WatersDataFlavor.GRAPH});
  private static final List<WatersDataFlavor> LIST_PARAMETER_BINDING =
    Collections.singletonList(WatersDataFlavor.PARAMETER_BINDING);

}
