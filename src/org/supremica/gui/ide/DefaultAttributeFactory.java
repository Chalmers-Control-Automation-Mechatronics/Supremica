//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.hisc
//# CLASS:   DefaultAttributeFactory
//###########################################################################
//# $Id$
//###########################################################################


package org.supremica.gui.ide;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.AttributeFactory;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;


/**
 * <P>The IDE's default attribute factory.
 * The attribute factory is used to populate the drop-down menu in the
 * {@link net.sourceforge.waters.gui.dialog.AttributesPanel AttributesPanel}
 * that appears in some dialogs. The default factory specifies only one
 * attribute to suppress the generation of EPS files through the command
 * line interface {@link org.supremica.util.ProcessCommandLineArguments
 * ProcessCommandLineArguments}.</P>
 *
 * @author Robi Malik
 */

public class DefaultAttributeFactory implements AttributeFactory
{

  //#########################################################################
  //# Singleton Pattern
  public static DefaultAttributeFactory getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private DefaultAttributeFactory()
  {
  }

  private static class SingletonHolder {
    private static final DefaultAttributeFactory INSTANCE =
      new DefaultAttributeFactory();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.AttributeFactory
  @Override
  public Collection<String> getApplicableKeys
    (final Class<? extends Proxy> clazz)
  {
    if (clazz.isAssignableFrom(SimpleComponentProxy.class)) {
      return ATTRIBUTES_FOR_AUTOMATON;
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public List<String> getApplicableValues(final String attrib)
  {
    return Collections.emptyList();
  }


  //#########################################################################
  //# String Constants
  /**
   * The attribute key used to suppress EPS generation.
   */
  public static final String EPS_SUPPRESS_KEY = "EPS:suppress";


   //#########################################################################
  //# Attribute List Constants
  private static final Collection<String> ATTRIBUTES_FOR_AUTOMATON =
    Collections.singletonList(EPS_SUPPRESS_KEY);

}
