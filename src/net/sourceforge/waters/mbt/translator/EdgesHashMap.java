package net.sourceforge.waters.mbt.translator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class EdgesHashMap extends HashMap<String, List<EdgeNode>>
{

  // constructor

  public EdgesHashMap() {

  }

  public void add(final String event, final String begin, final String end,
      final String guard, final String action)
  {

    List<EdgeNode> EdgesList;
    EdgeNode EdgeN;

    if (this.containsKey(event)) {

      EdgesList = get(event);

    } else {

      EdgesList = new LinkedList<EdgeNode>();

    }

    EdgeN = new EdgeNode(begin, end, guard, action);
    EdgesList.add(EdgeN);
    this.put(event, EdgesList);
  }
  
  
  //#########################################################################
  //# Static Class Variables
  public static final long serialVersionUID = 1;

}
