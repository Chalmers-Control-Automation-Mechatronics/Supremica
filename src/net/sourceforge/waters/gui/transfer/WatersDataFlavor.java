//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

import gnu.trove.set.hash.THashSet;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.module.ConstantAliasProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventAliasProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.base.SubjectTools;


/**
 * <P>The supertype of all data flavours supported by WATERS
 * transferables.</P>
 *
 * <P>This class provides the key functionality for copy/paste and drag/drop
 * operations of different types of WATERS objects. The data to be copied
 * or dragged is stored in {@link Transferable} objects created by static
 * methods {@link #createTransferable(Collection) createTransferable()} in
 * this class. The data flavours supported by these transferables are
 * instances of this class or its subclasses, and they control the behaviour
 * of the transferables.</P>
 *
 * <P>Given a list of {@link Proxy} objects, the {@link
 * #createTransferable(Collection) createTransferable()} method creates a
 * transferable in the following steps:</P>
 * <OL>
 * <LI>The input list is reduced to ensure that the transferable does
 *     not contain any two items where one is an ancestor of another.</LI>
 * <LI>A list of possible data flavours is determined (using a {@link
 *     DataFlavorVisitor}). The first data flavour obtained becomes the
 *     <I>primary data flavour</I>, which determines how the transferable is
 *     created.</LI>
 * <LI>The primary data flavour determines the actual list of data flavours by
 *     a call to its {@link #reduceDataFlavorList(List) reduceDataFlavorList()}
 *     method.</LI>
 * <LI>The final list of data flavours for the transferable is obtained by
 *     adding Java's string data flavour {@link DataFlavor#stringFlavor} to
 *     the end of the list returned by {@link #reduceDataFlavorList(List)
 *     reduceDataFlavorList()}.</LI>
 * <LI>The data stored in the transferable is copied from the input data by
 *     a call to the primary data flavour's {@link
 *     #createExportData(Collection,ModuleContext) createExportData()}
 *     method.</LI>
 * <LI>Using the data flavours and the copied data, an instance of
 *     {@link ProxyTransferable} is created and returned as the
 *     transferable.</LI>
 * </OL>
 *
 * <P>When data is retrieved from a transferable by the {@link
 * Transferable#getTransferData(DataFlavor) getTransferData()} method, the
 * transferable calls the {@link
 * #createImportData(Collection) createImportData()} method
 * of the requested data flavour, which copies the data back for use by the
 * GUI.</P>
 *
 * @author Robi Malik
 */

public abstract class WatersDataFlavor extends DataFlavor
{

  //#########################################################################
  //# Factory Methods
  /**
   * Determines whether the given collection of {@link Proxy} objects can
   * be stored in a {@link Transferable}.
   * @return <CODE>true</CODE> if a call to {@link
   *         #createTransferable(Collection) createTransferable()} with the
   *         given data will create a {@link Transferable},
   *         <CODE>false</CODE> otherwise.
   */
  public static boolean canCopy(Collection<? extends Proxy> data)
  {
    data = getReducedData(data);
    final DataFlavorVisitor visitor = DataFlavorVisitor.getInstance();
    return visitor.canCopy(data);
  }

  /**
   * Creates a {@link Transferable} containing the given collection of
   * {@link Proxy} objects. This method automatically determines appropriate
   * data flavours for the given list of objects and makes modifications to
   * the data as necessary.
   * @param  data   The objects to be stored in the transferable.
   *                The data will be duplicated and copies will be stored
   *                in the transferable.
   * @return An instance of {@link ProxyTransferable} containing the data.
   * @throws IllegalArgumentException to indicate that no suitable
   *         data flavour could be identified for the given data.
   * @see WatersDataFlavor
   */
  public static ProxyTransferable createTransferable
    (final Collection<? extends Proxy> data)
  {
    return createTransferable(data, null, true);
  }

  /**
   * Creates a {@link Transferable} containing the given collection of
   * {@link Proxy} objects. This method automatically determines appropriate
   * data flavours for the given list of objects and makes modifications to
   * the data as necessary.
   * @param  data    The objects to be stored in the transferable.
   *                 The data will be duplicated and copies will be stored
   *                 in the transferable.
   * @param  context The module context of the module containing the data,
   *                 or <CODE>null</CODE> if the data does not belong to any
   *                 module being edited.
   * @param  supportsIdentifier Whether or not the created transferable
   *                 should support the identifier data flavour
   *                 {@link WatersDataFlavor#IDENTIFIER}. Most transferables
   *                 support this flavour, but it can be suppressed by setting
   *                 this parameter to <CODE>false</CODE>.
   * @return An instance of {@link ProxyTransferable} containing the data.
   * @throws IllegalArgumentException to indicate that no suitable
   *         data flavour could be identified for the given data.
   * @see WatersDataFlavor
   */
  public static ProxyTransferable createTransferable
    (Collection<? extends Proxy> data,
     final ModuleContext context,
     boolean supportsIdentifier)
  {
    data = getReducedData(data);
    final DataFlavorVisitor visitor = DataFlavorVisitor.getInstance();
    final List<WatersDataFlavor> flavors =
      visitor.getTransferDataFlavors(data);
    int count = flavors.size() + 1;
    if (flavors.contains(WatersDataFlavor.IDENTIFIER)) {
      if (!supportsIdentifier) {
        count--;
      } else if (!WatersDataFlavor.IDENTIFIER.supports(data)) {
        supportsIdentifier = false;
        count--;
      }
    }
    final DataFlavor[] flavorsArray = new DataFlavor[count];
    int index = 0;
    for (final DataFlavor flavor : flavors) {
      if (supportsIdentifier || flavor != WatersDataFlavor.IDENTIFIER) {
        flavorsArray[index++] = flavor;
      }
    }
    flavorsArray[index] = DataFlavor.stringFlavor;
    final WatersDataFlavor flavor0 = flavors.get(0);
    final List<Proxy> exportData = flavor0.createExportData(data, context);
    return new ProxyTransferable(flavorsArray, exportData);
  }

  /**
   * Creates a {@link Transferable} containing a single {@link Proxy} object.
   * This method behaves like the {@link #createTransferable(Collection)
   * createTransferable()} method.
   * @see WatersDataFlavor
   */
  public static ProxyTransferable createTransferable(final Proxy proxy)
  {
    return createTransferable(proxy, true);
  }

  /**
   * Creates a {@link Transferable} containing a single {@link Proxy} object.
   * This method behaves like the {@link
   * #createTransferable(Collection,ModuleContext,boolean)
   * createTransferable()} method.
   * @see WatersDataFlavor
   */
  public static ProxyTransferable createTransferable
    (final Proxy proxy, final boolean supportsIdentifier)
  {
    final List<Proxy> data = Collections.singletonList(proxy);
    return createTransferable(data, null, supportsIdentifier);
  }


  //#########################################################################
  //# Auxiliary Methods
  private static Collection<? extends Proxy> getReducedData
    (final Collection<? extends Proxy> data)
  {
    if (data.size() <= 1 ||
        !(data.iterator().next() instanceof ProxySubject)) {
      return data;
    } else {
      final Set<Proxy> set = new THashSet<Proxy>(data);
      final int size = data.size();
      final Collection<Proxy> reduced = new ArrayList<Proxy>(size);
      for (final Proxy proxy : data) {
        ProxySubject parent = (ProxySubject) proxy;
        while (true) {
          parent = SubjectTools.getProxyParent(parent);
          if (parent == null) {
            reduced.add(proxy);
            break;
          } else if (set.contains(parent)) {
            break;
          }
        }
      }
      return reduced;
    }
  }


  //#########################################################################
  //# Constructor
  /**
   * Creates the data flavour for the given {@link Proxy} type.
   * @param  clazz    The representation class of the data flavour.
   *                  Data flavours are compared by checking identity of
   *                  their representation classes, so their cannot be two
   *                  data flavours using the same representation class.
   */
  WatersDataFlavor(final Class<? extends Proxy> clazz)
  {
    super(clazz, ProxyTools.getShortClassName(clazz));
  }


  //#########################################################################
  //# Importing and Exporting Data
  /**
   * Determines the actual list of data flavours used by a transferable
   * with this data flavour as the primary data flavour. This method is
   * overridden by subclasses to suppress certain data flavours when a more
   * suitable primary data flavour has been identified.
   * @param  flavors  The list of possible data flavours determined for the
   *                  input data (by the {@link DataFlavorVisitor}). The
   *                  input list must not be modified by this method call.
   * @return The default implementation returns the input list unmodified.
   * @see WatersDataFlavor
   */
  List<WatersDataFlavor> reduceDataFlavorList
    (final List<WatersDataFlavor> flavors)
  {
    return flavors;
  }

  /**
   * Copies input data for use by a {@link Transferable} using this data
   * flavour as the primary data flavour. This method is used by a <I>Copy</I>
   * operation to store WATERS objects in the clipboard. Every subclass must
   * override this method to provide a way to duplicate the input data and
   * convert it to a form that can be stored in a transferable. Typically,
   * this is done using a
   * {@link net.sourceforge.waters.model.base.ProxyCloner ProxyCloner}.
   * @param  data    Collection of objects to be stored in a
   *                 {@link Transferable}.
   * @param  context The module context of the module containing the data,
   *                 or <CODE>null</CODE> if the data does not belong to any
   *                 module being edited.
   * @return List of data used to create a {@link ProxyTransferable}.
   *         The returned data should not share any references with the
   *         input.
   * @see WatersDataFlavor
   */
  abstract List<Proxy> createExportData(Collection<? extends Proxy> data,
                                        ModuleContext context);

  /**
   * Copies the data from a {@link Transferable} using this data flavour as
   * the primary data flavour. This method is used by a <I>Paste</I> operation
   * retrieve data from the clipboard and insert in the GUI. Every subclass
   * must override this method to provide a way to duplicate the transfer data
   * and convert it to a form that can be used by an application.
   * @param  data     Collection of objects stored in a
   *                  {@link ProxyTransferable}.
   * @return List of data to be inserted in an application.
   *         The returned data should not share any references with the
   *         transfer data.
   * @see WatersDataFlavor
   */
  abstract List<Proxy> createImportData(Collection<? extends Proxy> data);


  //#########################################################################
  //# Available Data Flavours
  /**
   * The data flavour for a list of module components, as contained in the
   * components list tree-view. It is implemented as a
   * {@link ProxyTransferable} and contains a list of objects of type
   * {@link SimpleComponentProxy}, {@link VariableComponentProxy},
   * {@link InstanceProxy}, {@link ForeachProxy}, and possibly
   * {@link EventDeclProxy}. The event declarations are added to facilitate
   * copying and pasting of automata between modules together with their
   * events.
   * @see ComponentDataFlavor
   */
  public static final WatersDataFlavor COMPONENT = new ComponentDataFlavor();

  /**
   * The data flavour for a list of automaton proxy. It is implemented as a
   * {@link ProxyTransferable} and contains a list of objects of type
   * {@link AutomatonProxy} and possibly
   * {@link SimpleComponentProxy}. The event declarations are added to facilitate
   * copying and pasting of automata between modules together with their
   * events.
   * @see AutomatonDataFlavor
   */
  public static final WatersDataFlavor AUTOMATON = new AutomatonDataFlavor();

  /**
   * The data flavour for a list of constant aliases, as contained in the
   * named constants tree-view. It is implemented as a {@link
   * ProxyTransferable} and contains a list of objects of type {@link
   * ConstantAliasProxy}.
   */
  public static final WatersDataFlavor CONSTANT_ALIAS =
    new ModuleDataFlavor(ConstantAliasProxy.class);

  /**
   * The data flavour for a list of edges ({@link EdgeProxy}) of a graph.
   */
  public static final WatersDataFlavor EDGE = new EdgeDataFlavor();

  /**
   * The data flavour for a list of event aliases, as contained in the event
   * aliases tree-view. It is implemented as a {@link ProxyTransferable} and
   * contains a list of objects of type {@link EventAliasProxy} or
   * {@link net.sourceforge.waters.model.module.ForeachProxy}.
   */
  public static final WatersDataFlavor EVENT_ALIAS =
    new ModuleDataFlavor(EventAliasProxy.class);

  /**
   * The data flavour for a list of event declarations, as contained in the
   * event list-view. It is implemented as a {@link ProxyTransferable} and
   * contains a list of objects of type {@link EventDeclProxy}.
   */
  public static final WatersDataFlavor EVENT_DECL =
    new ModuleDataFlavor(EventDeclProxy.class);

  /**
   * The data flavour for a graph. This is implemented as a {@link
   * ProxyTransferable} that contains a single object of type {@link
   * GraphProxy}.
   */
  public static final WatersDataFlavor GRAPH = new GraphDataFlavor();

  /**
   * The data flavour for a guard/action block. It is implemented as a
   * {@link ProxyTransferable} that contains a single object of type
   * {@link GuardActionBlockProxy}.
   */
  public static final WatersDataFlavor GUARD_ACTION_BLOCK =
    new ModuleDataFlavor(GuardActionBlockProxy.class);

  /**
   * The data flavour for a list of event labels, as found on an edge of a
   * graph. It is implemented as a {@link ProxyTransferable} and contains a
   * list of objects of type {@link IdentifierProxy} or {@link
   * net.sourceforge.waters.model.module.ForeachProxy}
   */
  public static final IdentifierDataFlavor IDENTIFIER =
    new IdentifierDataFlavor();

  /**
   * The data flavour for a node labels. Node labels are represented in a
   * graph using {@link net.sourceforge.waters.model.module.LabelGeometryProxy}
   * objects, which are converted to {@link
   * net.sourceforge.waters.model.module.SimpleIdentifierProxy
   * SimpleIdentifierProxy} objects in the transferable.
   */
  public static final WatersDataFlavor LABEL_GEOMETRY =
    new NodeLabelDataFlavor();

  /**
   * The data flavour for a list of parameter bindings, as contained in the
   * components list tree-view. It is implemented as a {@link
   * ProxyTransferable} and contains a list of objects of type {@link
   * ParameterBindingProxy}.
   */
  public static final WatersDataFlavor PARAMETER_BINDING =
    new ModuleDataFlavor(ParameterBindingProxy.class);

}
