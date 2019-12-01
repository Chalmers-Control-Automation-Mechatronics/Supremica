package org.supremica.automata.algorithms.FT;

import java.util.List;

/**
 * Zenuity 2019 Hackfest
 *
 * Abstract class for representing type of gate.
 *
 * @author zhefei
 */

abstract public class GateNode extends Node
{

  protected List<EventNode> children;

  public GateNode(final String gateName)
  {
    super(gateName);
  }

  public void addChild(final EventNode child) {
    this.children.add(child);
  }

  public List<EventNode> getChildren() {
    return this.children;
  }
}
