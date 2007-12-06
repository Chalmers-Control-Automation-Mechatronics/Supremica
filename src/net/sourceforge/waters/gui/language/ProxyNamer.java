//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.language
//# CLASS:   ProxyNamer
//###########################################################################
//# $Id: ProxyNamer.java,v 1.4 2007-12-06 08:41:20 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui.language;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.module.AliasProxy;
import net.sourceforge.waters.model.module.ConstantAliasProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventAliasProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ForeachComponentProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.model.module.VariableMarkingProxy;


/**
 * <P>A utility class to give English names to items based on their type.</P>
 *
 * <P>These methods are used by actions and commands to produce nice labels
 * and tooltips. For example, a deletion operation that deletes a {@link
 * SimpleComponentProxy} object can be referred to as "Automaton
 * Deletion".</P>
 *
 * <P>Collections of items can get aggregate names, based on a <I>least
 * common supertype</I>. For example, a collection of {@link
 * SimpleNodeProxy} and {@link GroupNodeProxy} objects can be referred to
 * as "Nodes". The type hierarchy used here is introduced specifically for
 * naming, and does not always correlate with the WATERS interface
 * hierarchy.</P>
 *
 * <P>This class only has static public methods. This may be changed later
 * when support for different locales is needed.</P>
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
  private ProxyNamer()
  {
    mMap = new HashMap<Class<? extends Proxy>,NameEntry>(32);
    createEntry(AutomatonProxy.class, "Component");
    createEntry(AliasProxy.class, "Alias", "Aliases");
    createEntry(ConstantAliasProxy.class, "Named Constant",
                AliasProxy.class, false);
    createEntry(EdgeProxy.class, "Edge",
                NodeProxy.class, true);
    createEntry(EventAliasProxy.class, "Event Alias", "Event Aliases",
                AliasProxy.class, false);
    createEntry(EventDeclProxy.class, "Event");
    createEntry(ForeachComponentProxy.class, "Foreach Block",
                AutomatonProxy.class, false);
    createEntry(GraphProxy.class, "Graph", "Graph");
    createEntry(GuardActionBlockProxy.class, "Guard/Action Block",
                EdgeProxy.class, true);
    createEntry(GroupNodeProxy.class, "Group Node",
                NodeProxy.class, false);
    createEntry(IdentifierProxy.class, "Label",
                LabelBlockProxy.class, true);
    createEntry(IndexedIdentifierProxy.class, "Label",
                IdentifierProxy.class, false);
    createEntry(InstanceProxy.class, "Instance",
                AutomatonProxy.class, false);
    createEntry(LabelGeometryProxy.class, "Label",
                SimpleNodeProxy.class, true);
    createEntry(LabelBlockProxy.class, "Labels", "Labels",
                EdgeProxy.class, true);
    createEntry(NodeProxy.class, "Node");
    createEntry(ParameterBindingProxy.class, "Binding");
    createEntry(SimpleComponentProxy.class, "Automaton", "Automata",
                AutomatonProxy.class, false);
    createEntry(SimpleIdentifierProxy.class, "Label",
                IdentifierProxy.class, false);
    createEntry(SimpleNodeProxy.class, "Node",
                NodeProxy.class, false);
    createEntry(VariableComponentProxy.class, "Variable",
                AutomatonProxy.class, false);
    createEntry(VariableMarkingProxy.class, "Marking",
                VariableComponentProxy.class, true);
  }


  //#########################################################################
  //# Initialisation
  private void createEntry(final Class<? extends Proxy> iface,
                           final String singular)
  {
    createEntry(iface, singular, (Class<? extends Proxy>) null, false);
  }

  private void createEntry(final Class<? extends Proxy> iface,
                           final String singular,
                           final String plural)
  {
    createEntry(iface, singular, plural, null, false);
  }

  private void createEntry(final Class<? extends Proxy> iface,
                           final String singular,
                           final Class<? extends Proxy> parent,
                           final boolean constituent)
  {
    createEntry(iface, singular, singular + "s", parent, constituent);
  }

  private void createEntry(final Class<? extends Proxy> iface,
                           final String singular,
                           final String plural,
                           final Class<? extends Proxy> parent,
                           final boolean constituent)
  {
    final boolean vowel = hasInitialVowel(singular);
    createEntry(iface, singular, plural, vowel, parent, constituent);
  }

  private void createEntry(final Class<? extends Proxy> iface,
                           final String singular,
                           final String plural,
                           final boolean vowel,
                           final Class<? extends Proxy> parent,
                           final boolean constituent)
  {
    final NameEntry entry =
      new NameEntry(singular, plural, vowel, parent, constituent);
    mMap.put(iface, entry);
  }


  //#########################################################################
  //# Simple Access
  private String getName(final Proxy proxy, final boolean plural)
  {
    return plural ? getNamePlural(proxy) : getNameSingular(proxy);
  }

  private String getName(final Collection<? extends Proxy> collection)
  {
    switch (collection.size()) {
    case 0:
      return null;
    case 1:
      final Proxy first = collection.iterator().next();
      return getNameSingular(first);
    default:
      final int size = collection.size();
      final Map<Class<? extends Proxy>,Integer> map =
        new HashMap<Class<? extends Proxy>,Integer>(size);
      for (final Proxy proxy : collection) {
        final Class<? extends Proxy> iface = proxy.getProxyInterface();
        final Integer count = map.get(iface);
        if (count == null) {
          map.put(iface, 1);
        } else {
          map.put(iface, count + 1);
        }
      }
      int count = 0;
      Class<? extends Proxy> iface = null;
      for (final Map.Entry<Class<? extends Proxy>,Integer> entry :
             map.entrySet()) {
        final Class<? extends Proxy> key = entry.getKey();
        iface = getLeastCommonAncestor(iface, key);
        if (isConstituentOf(key, iface)) {
          // leave count unchanged
        } else if (isConstituentOf(iface, key)) {
          count = entry.getValue();
        } else {
          count += entry.getValue();
        }
      }
      final NameEntry entry = getEntry(iface);
      return count == 1 ? entry.getSingular() : entry.getPlural();
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
    final NameEntry entry = getEntry(iface);
    return entry.getSingular();
  }

  private String getNamePlural(final Proxy proxy)
  {
    final Class<? extends Proxy> iface = proxy.getProxyInterface();
    final NameEntry entry = getEntry(iface);
    return entry.getPlural();
  }


  //#########################################################################
  //# Auxiliary Methods
  private Class<? extends Proxy> getLeastCommonAncestor
    (final Class<? extends Proxy> iface1, final Class<? extends Proxy> iface2)
  {
    if (iface1 == null) {
      return iface2;
    } else if (iface2 == null) {
      return iface1;
    } else if (isAncestor(iface1, iface2)) {
      return iface1;
    } else if (isAncestor(iface2, iface1)) {
      return iface2;
    } else {
      Class<? extends Proxy> parent1 = getParent(iface1);
      while (parent1 != null && !isAncestor(parent1, iface2)) {
        parent1 = getParent(parent1);
      }
      return parent1;
    }
  }

  private boolean isAncestor(final Class<? extends Proxy> iface1,
                             final Class<? extends Proxy> iface2)
  {
    Class<? extends Proxy> parent2 = iface2;
    while (parent2 != null) {
      if (parent2 == iface1) {
        return true;
      }
      parent2 = getParent(parent2);
    }
    return false;
  }

  private boolean isConstituentOf(Class<? extends Proxy> item,
                                  final Class<? extends Proxy> ancestor)
  {
    while (item != ancestor) {
      final NameEntry entry = getEntry(item);
      if (!entry.isConstituent()) {
        return false;
      }
      item = entry.getParent();
    }
    return true;
  }

  private Class<? extends Proxy> getParent(final Class<? extends Proxy> iface)
  {
    final NameEntry entry = getEntry(iface);
    return entry.getParent();
  }

  private NameEntry getEntry(final Class<? extends Proxy> iface)
  {
    final NameEntry entry = mMap.get(iface);
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
    private NameEntry(final String singular,
                      final String plural,
                      final boolean vowel,
                      final Class<? extends Proxy> parent,
                      final boolean constituent)
    {
      mSingular = singular;
      mPlural = plural;
      mHasInitialVowel = vowel;
      mParent = parent;
      mIsConstituent = constituent;
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

    private Class<? extends Proxy> getParent()
    {
      return mParent;
    }

    private boolean isConstituent()
    {
      return mIsConstituent;
    }

    //#######################################################################
    //# Data Members
    private final String mSingular;
    private final String mPlural;
    private final boolean mHasInitialVowel;
    private final Class<? extends Proxy> mParent;
    private final boolean mIsConstituent;
  }


  //#########################################################################
  //# Data Members
  private final Map<Class<? extends Proxy>,NameEntry> mMap;


  //#########################################################################
  //# Static Class Constants
  private static final ProxyNamer INSTANCE = new ProxyNamer();

}
