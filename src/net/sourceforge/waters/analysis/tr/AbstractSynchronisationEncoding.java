//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   AbstractSynchronisationEncoding
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.tr;

import net.sourceforge.waters.model.des.AutomatonTools;


/**
 * @author Robi Malik, Sahar Mohajerani
 */
public abstract class AbstractSynchronisationEncoding
{

  //#######################################################################
  //# Initialization
  public static AbstractSynchronisationEncoding createEncoding
    (final int[] sizes, final int numStates)
  {
     int size = 0;
    for(int i=0; i< sizes.length; i++) {
      size += AutomatonTools.log2(sizes[i]);
    }
    if (size <= IntSynchronisationEncoding.MAX_BITS) {
      return new IntSynchronisationEncoding(sizes, numStates);
    } else if (size <= LongSynchronisationEncoding.MAX_BITS){
      return new LongSynchronisationEncoding(sizes, numStates);
    } else {
      return new ArraySynchronisationEncoding(sizes, numStates);
    }
  }


  //#######################################################################
  //# Constructor
  AbstractSynchronisationEncoding(final int[] sizes, final int numStates)
  {
    mNumberOfAutomata = sizes.length;
  }


  //#######################################################################
  //# Access
  public abstract int getStateCode(int[] tuple);

  public abstract void addState(int[] tuple, int code);

  public abstract int getMapSize();

  public int getMemoryEstimate()
  {
    return mNumberOfAutomata*getMapSize();
  }

  public abstract boolean compose(TRPartition partition);


  //#######################################################################
  //# Data members
  private final int mNumberOfAutomata;

}
