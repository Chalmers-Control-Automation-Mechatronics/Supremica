//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica
//# PACKAGE: org.supremica.properties
//# CLASS:   SupremicaPropertyChangeEvent
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.options;

import java.util.EventObject;

/**
 *
 * @author Benjamin Wheeler
 */
public class OptionChangeEvent extends EventObject
{

  //#######################################################################
  //# Constructor
  public OptionChangeEvent(final Option<?> option,
                                      final String oldvalue,
                                      final String newvalue)
  {
      super(option);
      mOldValue = oldvalue;
      mNewValue = newvalue;
  }


  //#######################################################################
  //# Simple Access
  @Override
  public Option<?> getSource()
  {
      return (Option<?>) super.getSource();
  }

  public String getOldValue()
  {
      return mOldValue;
  }

  public String getNewValue()
  {
      return mNewValue;
  }


  //#######################################################################
  //# Data Members
  private final String mOldValue;
  private final String mNewValue;


  //#######################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
