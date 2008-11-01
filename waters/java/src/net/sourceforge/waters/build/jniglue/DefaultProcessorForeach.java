//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   DefaultProcessorForeach
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.build.jniglue;

import java.util.Collection;
import java.util.Iterator;


class DefaultProcessorForeach implements ProcessorForeach {

  //#########################################################################
  //# Constructors
  DefaultProcessorForeach(final Collection glue)
  {
    mGlue = glue;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.build.jniglue.ProcessorForeach
  public Iterator getIterator()
  {
    return mGlue.iterator();
  }


  //#########################################################################
  //# Data Members
  private final Collection mGlue;

}