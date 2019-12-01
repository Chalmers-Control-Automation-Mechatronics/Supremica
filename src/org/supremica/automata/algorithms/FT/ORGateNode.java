package org.supremica.automata.algorithms.FT;

import java.util.ArrayList;

/**
 * Zenuity 2019 Hackfest
 *
 * OR gate.
 *
 * @author zhefei
 */

public class ORGateNode extends GateNode {

  public ORGateNode(final String name) {
    super(name);
    children = new ArrayList<EventNode>();
  }
}
