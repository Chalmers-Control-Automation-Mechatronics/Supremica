//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   CommandLineArgumentInteger
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.util.Iterator;


/**
 * An integer command line argument passed to a {@link ModelVerifierFactory}.
 *
 * @author Robi Malik
 */

public abstract class CommandLineArgumentInteger
  extends CommandLineArgument
{

  //#########################################################################
  //# Constructors
  protected CommandLineArgumentInteger(final String name,
                                       final String description)
  {
    super(name, description);
  }


  //#######################################################################
  //# Simple Access
  protected String getArgumentTemplate()
  {
    return "<n>";
  }

  protected int getValue()
  {
    return mValue;
  }


  //#######################################################################
  //# Parsing
  protected void parse(final Iterator<String> iter)
  {
    if (iter.hasNext()) {
      final String value = iter.next();
      mValue = Integer.parseInt(value);
    } else {
      throw getMissingValueException();
    }
  }


  //#########################################################################
  //# Data Members
  private int mValue;

}
