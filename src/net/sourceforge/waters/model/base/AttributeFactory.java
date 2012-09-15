//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Base
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   AttributeFactory
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.base;

import java.util.Collection;
import java.util.List;


/**
 * A configuration utility to define user defined attributes.
 *
 * An attribute factory defines names and possible values of attributes
 * for given proxy classes. This can be used by the GUI to provide suggestions
 * to the user when editing attribute maps.
 *
 * @author Robi Malik
 */

public interface AttributeFactory
{

  /**
   * Returns a list of attribute names that can be used for an item
   * of the given type.
   * @param  clazz  A proxy class or interface for which attributes are sought.
   * @return The attribute names for objects of this type, in any order.
   *         If no attributes are applicable, an empty collection is returned.
   */
  public Collection<String> getApplicableKeys(Class<? extends Proxy> clazz);

  /**
   * Returns a list of attribute values that can be used for an attribute
   * with the given name.
   * @param  attrib  The name of the attribute to be given a value.
   * @return List of attribute value strings in the order suggested to the
   *         user. If the attribute takes no value, an empty list is returned.
   */
  public List<String> getApplicableValues(String attrib);

}
