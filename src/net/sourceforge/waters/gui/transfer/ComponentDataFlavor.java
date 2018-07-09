//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.gui.transfer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.analyzer.AutomataCloner;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyCloner;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.DescendingModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.IdentifiedProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.QualifiedIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.model.module.VariableMarkingProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;


/**
 * A data flavour representing a collection of simple components
 * ({@link SimpleComponentProxy}) or other elements of a module's component
 * list. It differs from the {@link ModuleDataFlavor} in that it collects and
 * adds all events found in the copied data to the transferable so that they
 * can be copied from one module to another when copying and pasting automata.
 *
 * @author Robi Malik
 */

class ComponentDataFlavor extends ModuleDataFlavor
{

  //#########################################################################
  //# Constructor
  ComponentDataFlavor()
  {
    super(ComponentProxy.class);
  }

  //#########################################################################
  //# Importing and Exporting Data
  @Override
  List<Proxy> createExportData(final Collection<? extends Proxy> data,
                               final ModuleContext context)
  {
    if (context == null) {
      return super.createExportData(data, context);
    } else {
      final EventCollector collector = new EventCollector(context);
      final Set<EventDeclProxy> events = new TreeSet<>();
      collector.collect(data, events);
      final List<Proxy> export = new ArrayList<>(data.size() + events.size());
      final ProxyCloner cloner = ModuleElementFactory.getCloningInstance();
      for (final Proxy item : data) {
        final Proxy clone = cloner.getClone(item);
        export.add(clone);
      }
      for (final EventDeclProxy decl : events) {
        final Proxy clone = cloner.getClone(decl);
        export.add(clone);
      }
      return export;
    }
  }

  @Override
  List<Proxy> createImportData(final Collection<? extends Proxy> data)
  {
    final List<Proxy> proxyList = new ArrayList<Proxy>();
    final ModuleProxyFactory factory = ModuleSubjectFactory.getInstance();
    final ProductDESProxyFactory autFactory =
      ProductDESElementFactory.getInstance();
    final AutomataCloner autCloner = new AutomataCloner(autFactory);
    final ProxyCloner cloner = factory.getCloner();
    for (final Proxy p : data) {
      if (p instanceof AutomatonProxy)
        proxyList.add((Proxy)autCloner.clone((AutomatonProxy) p));
      else
        proxyList.add(cloner.getClone(p));
    }
    return proxyList;
  }


  //#########################################################################
  //# Inner Class EventCollector
  private static class EventCollector extends DescendingModuleProxyVisitor
  {
    //#######################################################################
    //# Constructor
    private EventCollector(final ModuleContext context)
    {
      mContext = context;
    }

    //#######################################################################
    //# Invocation
    private void collect(final Collection<? extends Proxy> data,
                         final Set<EventDeclProxy> events)
    {
      try {
        mEvents = events;
        visitCollection(data);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Object visitEdgeProxy(final EdgeProxy edge) throws VisitorException
    {
      final LabelBlockProxy labelBlock = edge.getLabelBlock();
      return visitLabelBlockProxy(labelBlock);
    }

    @Override
    public Object visitForeachProxy(final ForeachProxy foreach)
      throws VisitorException
    {
      final List<Proxy> body = foreach.getBody();
      return visitCollection(body);
    }

    @Override
    public Object visitGroupNodeProxy(final GroupNodeProxy proxy)
    {
      return null;
    }

    @Override
    public Object visitIdentifiedProxy(final IdentifiedProxy proxy)
    {
      return null;
    }

    @Override
    public Object visitIndexedIdentifierProxy(final IndexedIdentifierProxy ident)
      throws VisitorException
    {
      final String name = ident.getName();
      addEvent(name);
      return null;
    }

    @Override
    public Object visitQualifiedIdentifierProxy(final QualifiedIdentifierProxy ident)
    {
      return null;
    }

    @Override
    public Object visitSimpleIdentifierProxy(final SimpleIdentifierProxy ident)
      throws VisitorException
    {
      final String name = ident.getName();
      addEvent(name);
      return null;
    }

    @Override
    public Object visitVariableComponentProxy(final VariableComponentProxy var)
      throws VisitorException
    {
      final List<VariableMarkingProxy> variableMarkings =
        var.getVariableMarkings();
      return visitCollection(variableMarkings);
    }

    @Override
    public Object visitVariableMarkingProxy(final VariableMarkingProxy marking)
      throws VisitorException
    {
      final IdentifierProxy proposition = marking.getProposition();
      return proposition.acceptVisitor(this);
    }

    //#######################################################################
    //# Auxiliary Methods
    private void addEvent(final String name)
    {
      final EventDeclProxy decl = mContext.getEventDecl(name);
      if (decl != null) {
        mEvents.add(decl);
      }
    }

    //#######################################################################
    //# Data Members
    private final ModuleContext mContext;
    private Set<EventDeclProxy> mEvents;
  }

}
