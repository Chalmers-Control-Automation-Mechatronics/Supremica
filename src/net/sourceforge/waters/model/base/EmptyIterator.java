//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.compiler
//# CLASS:   EmptyIterator
//###########################################################################
//# $Id: EmptyIterator.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.base;

import java.util.Iterator;
import java.util.NoSuchElementException;


public class EmptyIterator implements Iterator
{

  //#########################################################################
  //# Constructor
  public static Iterator getInstance()
  {
    return sInstance;
  }

  private EmptyIterator()
  {
  }


  //#########################################################################
  //# Interface java.util.Iterator
  public boolean hasNext()
  {
    return false;
  }

  public Object next()
  {
    throw new NoSuchElementException
      ("EmptyIterator has no elements to return!");
  }

  public void remove()
  {
    throw new UnsupportedOperationException
      ("EmptyIterator does not support remove() operation!");
  } 


  //#########################################################################
  //# Data Members
  private static final EmptyIterator sInstance = new EmptyIterator();

}
