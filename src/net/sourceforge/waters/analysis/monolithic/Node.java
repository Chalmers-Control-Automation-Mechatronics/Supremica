//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   
//##################################################################

package net.sourceforge.waters.analysis.monolithic;

import java.util.BitSet;

/**
 * Representation of a node in the reachability tree for SD Property iii.2
 *
 * It contains a state along with its occurrence image
 *
 * @author Mahvash Baloch
 */

class Node
{
    /** State tuple */
    private final StateTuple sTuple;

    /** Occurrence Image of the string associated with the State*/
    private final BitSet Occu;


    //#########################################################################
    //# Constructor
    /**
     * Creates a new Node. It creates a node for the tree from given parameters
     * @param  sTuple State tuple
     * @param  events the occurrence Image of the string
     */
    public Node(final StateTuple sTuple, final BitSet events)
    {
	this.sTuple = sTuple;
	this.Occu = events;
	}

  //#########################################################################
    //# Invocation
    /**
     * returns state tuple
     * @return state tuple
     */
    public StateTuple getTuple()
    {
    return sTuple;
    }

    /**
     * returns Occurrence Image
     * @return Occurrence Image
     */
    public BitSet getOccu()
    {
    return Occu;
    }

    public int hashCode()
    {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((Occu == null) ? 0 : Occu.hashCode());
      result = prime * result + ((sTuple == null) ? 0 : sTuple.hashCode());
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
      final Node other = (Node) obj;
      if (Occu == null) {
        if (other.Occu != null)
          return false;
      } else if (!Occu.equals(other.Occu))
        return false;
      if (sTuple == null) {
        if (other.sTuple != null)
          return false;
      } else if (!sTuple.equals(other.sTuple))
        return false;
      return true;
    }


  }
