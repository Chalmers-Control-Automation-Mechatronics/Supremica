//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis
//# CLASS:   BlockedArrayList
//###########################################################################
//# $Id: BlockedArrayList.java,v 1.2 2006-07-20 02:28:36 robi Exp $
//###########################################################################

package net.sourceforge.waters.analysis;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.model.unchecked.Casting;


public class BlockedArrayList<T> extends AbstractList<T>{
  
  //#########################################################################
  //# Constructors
  public BlockedArrayList(final Class<T> clazz)
  {
    clazz_ = clazz;
    size_ = 0;
    blocks_ = new ArrayList<T[]>(128);
  }


  //#########################################################################
  //# Interface java.util.List
  public boolean add(T item)
  {
    int blockno = size_ / BLOCK_SIZE;
    if (blockno < blocks_.size()) {
      block = blocks_.get(blockno);
    } else {
      block = Casting.newArray(clazz_, BLOCK_SIZE);
      blocks_.add(block);
    }
    block[size_ % BLOCK_SIZE] = item;
    size_++;
    return true;        
  }
  
  public T get(int index)
  {
    int blockno = index / BLOCK_SIZE;
    if (blockno < blocks_.size()) {
      block = blocks_.get(blockno);
      return block[index % BLOCK_SIZE];
    } else {
      throw new IndexOutOfBoundsException("The index "+index+" is out of bounds of list size "+blocks_.size()+" !");
    }
  }
 
  public int size(){
    return size_;
  }


  //#########################################################################
  //# Data Members
  private final Class<T> clazz_;
  private int size_;                    //The number of nodes
  private List<T[]> blocks_;            //Fixed length blocks for nodes
  
  private T[] block;
  
  private final int BLOCK_SIZE = 1024;
   
}
