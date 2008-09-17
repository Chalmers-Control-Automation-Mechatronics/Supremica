//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   CommandLineArgumentString
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

public abstract class CommandLineArgumentString
  extends CommandLineArgument
{

  //#########################################################################
  //# Constructors
  protected CommandLineArgumentString(final String name)
  {
    super(name);
  }


  //#######################################################################
  //# Simple Access
  protected String getValue()
  {
    return mValue;
  }


  //#######################################################################
  //# Parsing
  protected void parse(final Iterator<String> iter)
  {
    if (iter.hasNext()) {
      mValue = iter.next();
    } else {
      throw getMissingValueException();
    }
  }


  //#########################################################################
  //# Data Members
  private String mValue;

}
