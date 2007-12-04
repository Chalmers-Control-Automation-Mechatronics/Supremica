//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.language
//# CLASS:   ProxyNamer
//###########################################################################
//# $Id: ProxyNamer.java,v 1.2 2007-12-04 03:22:55 robi Exp $
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
    createEntry(ConstantAliasProxy.class, "Named Constant", AliasProxy.class);
    createEntry(EdgeProxy.class, "Edge", GraphProxy.class);
    createEntry(EventAliasProxy.class, "Event Alias", "Event Aliases",
                AliasProxy.class);
    createEntry(EventDeclProxy.class, "Event");
    createEntry(ForeachComponentProxy.class, "Foreach Block",
                AutomatonProxy.class);
    createEntry(GraphProxy.class, "Graph", "Graph");
    createEntry(GuardActionBlockProxy.class, "Guard/Action Block",
                LabelBlockProxy.class);
    createEntry(GroupNodeProxy.class, "Group Node", NodeProxy.class);
    createEntry(IdentifierProxy.class, "Label");
    createEntry(IndexedIdentifierProxy.class, "Label", IdentifierProxy.class);
    createEntry(InstanceProxy.class, "Instance", AutomatonProxy.class);
    createEntry(LabelBlockProxy.class, "Labels", "Labels");
    createEntry(NodeProxy.class, "Node", GraphProxy.class);
    createEntry(ParameterBindingProxy.class, "Binding");
    createEntry(SimpleComponentProxy.class, "Automaton", "Automata",
                AutomatonProxy.class);
    createEntry(SimpleIdentifierProxy.class, "Label", IdentifierProxy.class);
    createEntry(SimpleNodeProxy.class, "Node", NodeProxy.class);
    createEntry(VariableComponentProxy.class, "Variable",
                AutomatonProxy.class);
    createEntry(VariableMarkingProxy.class, "Marking");
  }


  //#########################################################################
  //# Initialisation
  private void createEntry(final Class<? extends Proxy> iface,
                           final String singular)
  {
    createEntry(iface, singular, (Class<? extends Proxy>) null);
  }

  private void createEntry(final Class<? extends Proxy> iface,
                           final String singular,
                           final String plural)
  {
    createEntry(iface, singular, plural, null);
  }

  private void createEntry(final Class<? extends Proxy> iface,
                           final String singular,
                           final Class<? extends Proxy> parent)
  {
    createEntry(iface, singular, singular + "s", parent);
  }

  private void createEntry(final Class<? extends Proxy> iface,
                           final String singular,
                           final String plural,
                           final Class<? extends Proxy> parent)
  {
    final boolean vowel = hasInitialVowel(singular);
    createEntry(iface, singular, plural, parent, vowel);
  }

  private void createEntry(final Class<? extends Proxy> iface,
                           final String singular,
                           final String plural,
                           final Class<? extends Proxy> parent,
                           final boolean vowel)
  {
    final NameEntry entry = new NameEntry(singular, plural, parent, vowel);
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
      final Proxy proxy = collection.iterator().next();
      return getNameSingular(proxy);
    default:
      final Class<? extends Proxy> iface = getLeastCommonAncestor(collection);
      final NameEntry entry = mMap.get(iface);
      return entry == null ? null : entry.getPlural();
    }      
  }

  private String getNameUnqualified(final Proxy proxy)
  {
    final Class<? extends Proxy> iface = proxy.getProxyInterface();
    final NameEntry entry = mMap.get(iface);
    final String name = entry.getSingular().toLowerCase();
    final String article = entry.hasInitialVowel() ? "an " : "a ";
    return article + name;
  }

  private String getNameSingular(final Proxy proxy)
  {
    final Class<? extends Proxy> iface = proxy.getProxyInterface();
    final NameEntry entry = mMap.get(iface);
    return entry.getSingular();
  }

  private String getNamePlural(final Proxy proxy)
  {
    final Class<? extends Proxy> iface = proxy.getProxyInterface();
    final NameEntry entry = mMap.get(iface);
    return entry.getPlural();
  }


  //#########################################################################
  //# Auxiliary Methods
  private Class<? extends Proxy> getLeastCommonAncestor
    (final Collection<? extends Proxy> collection)
  {
    final Iterator<? extends Proxy> iter = collection.iterator();
    final Proxy first = iter.next();
    Class<? extends Proxy> result = first.getProxyInterface();
    while (iter.hasNext() && result != null) {
      final Proxy next = iter.next();
      final Class<? extends Proxy> iface = next.getProxyInterface();
      result = getLeastCommonAncestor(result, iface);
    }
    return result;
  }

  private Class<? extends Proxy> getLeastCommonAncestor
    (final Class<? extends Proxy> iface1, final Class<? extends Proxy> iface2)
  {
    if (isAncestor(iface1, iface2)) {
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

  private Class<? extends Proxy> getParent(final Class<? extends Proxy> iface)
  {
    final NameEntry entry = mMap.get(iface);
    if (entry != null) {
      return entry.getParent();
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
                      final Class<? extends Proxy> parent,
                      final boolean vowel)
    {
      mSingular = singular;
      mPlural = plural;
      mParent = parent;
      mHasInitialVowel = vowel;
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

    private Class<? extends Proxy> getParent()
    {
      return mParent;
    }

    private boolean hasInitialVowel()
    {
      return mHasInitialVowel;
    }

    //#######################################################################
    //# Data Members
    private final String mSingular;
    private final String mPlural;
    private final Class<? extends Proxy> mParent;
    private final boolean mHasInitialVowel;
  }


  //#########################################################################
  //# Data Members
  private final Map<Class<? extends Proxy>,NameEntry> mMap;


  //#########################################################################
  //# Static Class Constants
  private static final ProxyNamer INSTANCE = new ProxyNamer();

}
