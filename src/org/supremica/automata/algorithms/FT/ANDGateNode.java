package org.supremica.automata.algorithms.FT;

import java.util.ArrayList;

/**
 * Zenuity 2019 Hackfest
 *
 * AND gate.
 *
 * @author zhefei
 */

public class ANDGateNode extends GateNode {

  public ANDGateNode(final String name) {
    super(name);
    children = new ArrayList<EventNode>(2);
  }

  public EventNode getLeft()
  {
    return children.get(0);
  }
  public EventNode getRight()
  {
    return children.get(1);
  }
}
