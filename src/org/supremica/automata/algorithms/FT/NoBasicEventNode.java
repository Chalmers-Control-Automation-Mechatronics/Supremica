package org.supremica.automata.algorithms.FT;

/**
 * Zenuity 2019 Hackfest
 *
 * Intermediate or root event, having outgoing gate.
 *
 * @author zhefei
 */

public class NoBasicEventNode extends EventNode
{
  private GateNode gate;

  public NoBasicEventNode(final String name)
  {
    super(name);
  }

  public void setGate(final GateNode gate) {
    this.gate = gate;
  }

  public GateNode getGate() {
    return gate;
  }
}
