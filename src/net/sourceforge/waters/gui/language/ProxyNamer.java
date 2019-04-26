//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

package net.sourceforge.waters.gui.language;

import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.module.AliasProxy;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.ConstantAliasProxy;
import net.sourceforge.waters.model.module.DefaultProductDESAndModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventAliasProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.PointGeometryProxy;
import net.sourceforge.waters.model.module.QualifiedIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.model.module.VariableMarkingProxy;


/**
 * <P>A utility class to give English names to items based on their type.</P>
 *
 * <P>These methods are used by actions and commands to produce nice labels
 * and tool tips. For example, a deletion operation that deletes a {@link
 * SimpleComponentProxy} object can be referred to as "Automaton
 * Deletion".</P>
 *
 * <P>Collections of items can get aggregate names, based on a <I>least
 * common supertype</I>. For example, a collection of {@link
 * SimpleNodeProxy} and {@link GroupNodeProxy} objects can be referred to
 * as "States". The type hierarchy used here is introduced specifically for
 * naming, and does not always correlate with the WATERS interface
 * hierarchy.</P>
 *
 * <P>This class only has static public methods. This may be changed
 * if support for different locales is needed.</P>
 *
 * @see ProxyNamerTest
 *
 * @author Robi Malik
 */

public class ProxyNamer {

  //#########################################################################
  //# Static Access
  /**
   * Gets a name for a single item.
   * @param  proxy      The item to be named.
   * @return The name, with capitalised initials.
   */
  public static String getItemClassName(final Proxy proxy)
  {
    return INSTANCE.getNameSingular(proxy);
  }

  /**
   * Gets a name for a single item with a prefixed article "a" or "an".
   * This method can generate descriptive text fragments such as "a
   * component" or "an event".
   * @param  proxy      The item to be named.
   * @return The name, all lower case.
   */
  public static String getUnqualifiedClassName(final Proxy proxy)
  {
    return INSTANCE.getNameUnqualified(proxy);
  }

  /**
   * Gets a name for one or more items.
   * @param  proxy      One of the items to be named.
   * @param  plural     <CODE>true</CODE> if the name should be returned in
   *                    plural form.
   * @return The name, with capitalised initials.
   */
  public static String getItemClassName(final Proxy proxy,
                                        final boolean plural)
  {
    return
      plural ? INSTANCE.getNamePlural(proxy) : INSTANCE.getNameSingular(proxy);
  }

  /**
   * Gets a name for a collection of items.
   * If the given collection contains only one item, the name for that
   * item is returned in singular form. Otherwise the <I>least common
   * supertype</I> for all items in the collection is determined, and
   * its name is returned in plural form.
   * @param  collection The collection to be named.
   * @return The name, with capitalised initials, or <CODE>null</CODE>
   *         if the collection is empty, or if no common supertype could
   *         be found.
   */
  public static String getCollectionClassName
    (final Collection<? extends Proxy> collection)
  {
    return INSTANCE.getName(collection);
  }


  //#########################################################################
  //# Constructor
  @SuppressWarnings("unchecked")
  private ProxyNamer()
  {
    mNameMap = new HashMap<Class<? extends Proxy>,NameEntry>(32);
    createEntry(AutomatonProxy.class, "Automaton", "Automata", Proxy.class);
    createEntry(AliasProxy.class, "Alias", "Aliases", Proxy.class);
    createEntry(ConstantAliasProxy.class, "Named Constant", AliasProxy.class);
    createEntry(ComponentProxy.class, "Component", Proxy.class);
    createEntry(EdgeProxy.class, "Edge", Proxy.class, NodeProxy.class);
    createEntry(EventAliasProxy.class, "Event Alias", "Event Aliases",
                AliasProxy.class);
    createEntry(EventDeclProxy.class, "Event", Proxy.class,
                ComponentProxy.class);
    createEntry(ForeachProxy.class, "Foreach Block", Proxy.class);
    createEntry(GraphProxy.class, "Graph", "Graph", Proxy.class,
                SimpleComponentProxy.class);
    createEntry(GuardActionBlockProxy.class, "Guard/Action Block",
                IdentifierProxy.class, EdgeProxy.class);
    createEntry(GroupNodeProxy.class, "Group State", NodeProxy.class);
    createEntry(IdentifierProxy.class, "Label", Proxy.class,
                EventDeclProxy.class, LabelBlockProxy.class);
    createEntry(IndexedIdentifierProxy.class, "Label", IdentifierProxy.class);
    createEntry(InstanceProxy.class, "Instance", ComponentProxy.class);
    createEntry(LabelGeometryProxy.class, "Label", IdentifierProxy.class,
                SimpleNodeProxy.class);
    createEntry(LabelBlockProxy.class, "Labels", "Labels",
                EdgeProxy.class);
    createEntry(ModuleProxy.class, "Module", Proxy.class);
    createEntry(NodeProxy.class, "State", Proxy.class);
    createEntry(ParameterBindingProxy.class, "Binding", Proxy.class,
                InstanceProxy.class);
    createEntry(PointGeometryProxy.class, "Node Label", Proxy.class,
                SimpleNodeProxy.class);
    createEntry(Proxy.class, "Item");
    createEntry(QualifiedIdentifierProxy.class, "Label", IdentifierProxy.class);
    createEntry(SimpleComponentProxy.class, "Automaton", "Automata",
                ComponentProxy.class);
    createEntry(SimpleIdentifierProxy.class, "Label", IdentifierProxy.class);
    createEntry(SimpleNodeProxy.class, "State", NodeProxy.class);
    createEntry(VariableComponentProxy.class, "Variable",
                ComponentProxy.class);
    createEntry(VariableMarkingProxy.class, "Marking", Proxy.class,
                VariableComponentProxy.class);
    mCountVisitor = new CountVisitor();
  }


  //#########################################################################
  //# Initialisation
  private void createEntry(final Class<? extends Proxy> iface,
                           final String singular)
  {
    createEntry(iface, singular, null);
  }

  @SuppressWarnings("unchecked")
  private void createEntry(final Class<? extends Proxy> iface,
                           final String singular,
                           final String plural)
  {
    createEntry(iface, singular, plural, null);
  }

  @SuppressWarnings("unchecked")
  private void createEntry(final Class<? extends Proxy> iface,
                           final String singular,
                           final Class<? extends Proxy> superType,
                           final Class<? extends Proxy>... containers)
  {
    createEntry(iface, singular, singular + "s", superType, containers);
  }

  @SuppressWarnings("unchecked")
  private void createEntry(final Class<? extends Proxy> iface,
                           final String singular,
                           final String plural,
                           final Class<? extends Proxy> superType,
                           final Class<? extends Proxy>... containers)
  {
    final boolean vowel = hasInitialVowel(singular);
    createEntry(iface, singular, plural, vowel, superType, containers);
  }

  @SuppressWarnings("unchecked")
  private void createEntry(final Class<? extends Proxy> iface,
                           final String singular,
                           final String plural,
                           final boolean vowel,
                           final Class<? extends Proxy> superType,
                           final Class<? extends Proxy>... containers)
  {
    final NameEntry entry =
      new NameEntry(singular, plural, vowel, superType, containers);
    mNameMap.put(iface, entry);
  }


  //#########################################################################
  //# Simple Access
  @SuppressWarnings("unused")
  private String getName(final Proxy proxy, final boolean plural)
  {
    final Class<? extends Proxy> iface = proxy.getProxyInterface();
    return getName(iface, plural);
  }

  private String getName(final Class<? extends Proxy> iface,
                         final boolean plural)
  {
    return plural ? getNamePlural(iface) : getNameSingular(iface);
  }

  private String getName(final Collection<? extends Proxy> collection)
  {
    switch (collection.size()) {
    case 0:
      return null;
    case 1:
      final Proxy first = collection.iterator().next();
      if (!(first instanceof ForeachProxy)) {
        return getNameSingular(first);
      }
      // fall through ...
    default:
      // Collect how many items of each class are to be named ...
      final int size = collection.size();
      final TObjectIntHashMap<Class<? extends Proxy>> map =
        new TObjectIntHashMap<>(size, 0.5f, 0);
      mCountVisitor.collectCounts(collection, map);
      // Ignore foreach blocks if there are other types ...
      int count = 0;
      if (map.size() > 1) {
        count = map.remove(ForeachProxy.class);
      }
      // Ignore items that are constituents of others ...
      final TObjectIntIterator<Class<? extends Proxy>> outer = map.iterator();
      while (outer.hasNext()) {
        outer.advance();
        final TObjectIntIterator<Class<? extends Proxy>> inner = map.iterator();
        final Class<? extends Proxy> constituent = outer.key();
        while (inner.hasNext()) {
          inner.advance();
          final Class<? extends Proxy> container = inner.key();
          if (constituent != container &&
              isConstituentOfGeneralisation(constituent, container)) {
            outer.remove();
            break;
          }
        }
      }
      // Find the most general type and use its name ...
      Class<? extends Proxy> iface = null;
      final TObjectIntIterator<Class<? extends Proxy>> iter = map.iterator();
      while (iter.hasNext()) {
        iter.advance();
        final Class<? extends Proxy> key = iter.key();
        final Class<? extends Proxy> ancestor =
          getLeastCommonSuperType(iface, key);
        count += iter.value();
        iface = ancestor;
      }
      final NameEntry entry = getEntry(iface);
      if (count == 1) {
        return entry.getSingular();
      } else {
        return entry.getPlural();
      }
    }
  }

  private String getNameUnqualified(final Proxy proxy)
  {
    final Class<? extends Proxy> iface = proxy.getProxyInterface();
    final NameEntry entry = getEntry(iface);
    final String name = entry.getSingular().toLowerCase();
    final String article = entry.hasInitialVowel() ? "an " : "a ";
    return article + name;
  }

  private String getNameSingular(final Proxy proxy)
  {
    final Class<? extends Proxy> iface = proxy.getProxyInterface();
    return getNameSingular(iface);
  }

  private String getNameSingular(final Class<? extends Proxy> iface)
  {
    final NameEntry entry = getEntry(iface);
    return entry.getSingular();
  }

  private String getNamePlural(final Proxy proxy)
  {
    final Class<? extends Proxy> iface = proxy.getProxyInterface();
    return getNamePlural(iface);
  }

  private String getNamePlural(final Class<? extends Proxy> iface)
  {
    final NameEntry entry = getEntry(iface);
    return entry.getPlural();
  }


  //#########################################################################
  //# Auxiliary Methods
  private Class<? extends Proxy> getLeastCommonSuperType
    (final Class<? extends Proxy> iface1, final Class<? extends Proxy> iface2)
  {
    if (iface1 == null) {
      return iface2;
    } else if (iface2 == null) {
      return iface1;
    } else if (isSuperType(iface1, iface2)) {
      return iface1;
    } else if (isSuperType(iface2, iface1)) {
      return iface2;
    } else {
      Class<? extends Proxy> parent1 = getSuperType(iface1);
      while (parent1 != null && !isSuperType(parent1, iface2)) {
        parent1 = getSuperType(parent1);
      }
      return parent1;
    }
  }

  private boolean isSuperType(final Class<? extends Proxy> ancestor,
                              final Class<? extends Proxy> descendant)
  {
    if (ancestor == descendant) {
      return true;
    }
    final NameEntry entry = getEntry(descendant);
    final Class<? extends Proxy> parent = entry.getSuperType();
    if (parent == null) {
      return false;
    } else {
      return isSuperType(ancestor, parent);
    }
  }

  private boolean isConstituentOfGeneralisation(Class<? extends Proxy> constituent,
                                                Class<? extends Proxy> container)
  {
    while (constituent != null) {
      final NameEntry entry = getEntry(constituent);
      final Class<? extends Proxy>[] containers = entry.getContainers();
      if (containers.length > 0) {
        break;
      }
      constituent = getSuperType(constituent);
    }
    if (constituent == null) {
      return false;
    }
    while (container != null && container != constituent) {
      if (isContainer(container, constituent)) {
        return true;
      }
      container = getSuperType(container);
    }
    return false;
  }

  private boolean isContainer(final Class<? extends Proxy> ancestor,
                              final Class<? extends Proxy> descendant)
  {
    if (ancestor == descendant) {
      return true;
    }
    final NameEntry entry = getEntry(descendant);
    for (final Class<? extends Proxy> parent : entry.getContainers()) {
      if (isContainer(ancestor, parent)) {
        return true;
      }
    }
    return false;
  }

  private Class<? extends Proxy> getSuperType(final Class<? extends Proxy> iface)
  {
    final NameEntry entry = getEntry(iface);
    return entry.getSuperType();
  }

  private NameEntry getEntry(final Class<? extends Proxy> iface)
  {
    final NameEntry entry = mNameMap.get(iface);
    if (entry != null) {
      return entry;
    } else {
      throw new IllegalArgumentException
        ("ProxyNamer does not support class " + iface.getName() + "!");
    }
  }


  //#########################################################################
  //# Auxiliary Static Methods
  private static boolean hasInitialVowel(final String word)
  {
    final char ch = word.charAt(0);
    return ch == 'A' || ch == 'E' || ch == 'I' || ch == 'O' || ch == 'U';
  }


  //#########################################################################
  //# Inner Class NameEntry
  private static final class NameEntry
  {
    //#######################################################################
    //# Constructor
    @SuppressWarnings("unchecked")
    private NameEntry(final String singular,
                      final String plural,
                      final boolean vowel,
                      final Class<? extends Proxy> superType,
                      final Class<? extends Proxy>... containers)
    {
      mSingular = singular;
      mPlural = plural;
      mHasInitialVowel = vowel;
      mSuperType = superType;
      mContainers = containers;
    }

    //#######################################################################
    //# Simple Access
    private String getSingular()
    {
      return mSingular;
    }

    private String getPlural()
    {
      return mPlural;
    }

    private boolean hasInitialVowel()
    {
      return mHasInitialVowel;
    }

    private Class<? extends Proxy>[] getContainers()
    {
      return mContainers;
    }

    private Class<? extends Proxy> getSuperType()
    {
      return mSuperType;
    }

    //#######################################################################
    //# Debugging
    @Override
    public String toString()
    {
      return mSingular;
    }

    //#######################################################################
    //# Data Members
    private final String mSingular;
    private final String mPlural;
    private final boolean mHasInitialVowel;
    private final Class<? extends Proxy> mSuperType;
    private final Class<? extends Proxy>[] mContainers;
  }


  //#########################################################################
  //# Inner Class CountVisitor
  private static class CountVisitor
    extends DefaultProductDESAndModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
    private void collectCounts(final Collection<? extends Proxy> collection,
                               final TObjectIntHashMap<Class<? extends Proxy>> map)
    {
      try {
        mMap = map;
        visitCollection(collection);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      } finally {
        mMap = null;
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ProxyVisitor
    @Override
    public Object visitProxy(final Proxy proxy)
    {
      mMap.adjustOrPutValue(proxy.getProxyInterface(), 1, 1);
      return null;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Object visitForeachProxy(final ForeachProxy foreach)
      throws VisitorException
    {
      visitProxy(foreach);
      return visitCollection(foreach.getBody());
    }

    //#######################################################################
    //# Data Members
    private TObjectIntHashMap<Class<? extends Proxy>> mMap;
  }


  //#########################################################################
  //# Data Members
  private final Map<Class<? extends Proxy>,NameEntry> mNameMap;
  private final CountVisitor mCountVisitor;


  //#########################################################################
  //# Static Class Constants
  private static final ProxyNamer INSTANCE = new ProxyNamer();

}
