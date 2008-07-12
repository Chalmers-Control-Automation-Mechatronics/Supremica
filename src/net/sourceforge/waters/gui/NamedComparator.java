package net.sourceforge.waters.gui;

import java.util.Comparator;


public class NamedComparator
  implements Comparator<Object>
{
  private static NamedComparator INSTANCE = new NamedComparator();
  
  private NamedComparator()
  {
  }
  
  public int compare(Object n1, Object n2)
  {
    return n1.toString().compareTo(n2.toString());
  }
  
  public static NamedComparator getInstance()
  {
    return INSTANCE;
  }
}
