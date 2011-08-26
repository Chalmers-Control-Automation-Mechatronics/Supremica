//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   TransitionProperty
//###########################################################################
//# $Id: TransitionProperty.java 4526 2008-11-17 01:35:52Z robi $
//###########################################################################

package net.sourceforge.waters.analysis.monolithic;


/**
 * Representation of a node in the reachability tree for SD Property iii.2
 *
 * It contains a state along with its occurrence image
 *
 * @author Mahvash Baloch
 */

class StateTuplePair
{
    /**  First Statetuple */
    private final StateTuple Tuple1;

    /** Second Statetuple*/
    private final StateTuple Tuple2;


    //#########################################################################
    //# Constructor
    /**
     * Creates a new StateTuple pair.
     * @param  Tuple1 First  Statetuple
     * @param  Tuple2 Second Statetuple
     */
    public StateTuplePair(final StateTuple Tuple1, final StateTuple Tuple2)
    {
	this.Tuple1 = Tuple1;
	this.Tuple2 = Tuple2;
	}

  //#########################################################################
    //# Invocation
    /**
     * returns first state tuple
     * @return state tuple
     */
    public StateTuple getTuple1()
    {
    return Tuple1;
    }

    /**
     * returns Second Statetuple
     * @return state tuple
     */
    public StateTuple getTuple2()
    {
    return Tuple2;
    }

    public int hashCode()
    {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((Tuple1 == null) ? 0 : Tuple1.hashCode());
      result = prime * result + ((Tuple2 == null) ? 0 : Tuple2.hashCode());
      return result;
    }

    public boolean equals(final Object obj)
    {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      final StateTuplePair other = (StateTuplePair) obj;
      if (Tuple1 == null) {
        if (other.Tuple1 != null)
          return false;
      } else if (!Tuple1.equals(other.Tuple1))
        return false;
      if (Tuple2 == null) {
        if (other.Tuple2 != null)
          return false;
      } else if (!Tuple2.equals(other.Tuple2))
        return false;
      return true;
    }

  }
