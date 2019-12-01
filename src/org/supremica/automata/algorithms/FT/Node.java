package org.supremica.automata.algorithms.FT;

/**
 * Zenuity 2019 Hackfest
 *
 * General node in FT.
 *
 * @author zhefei
 */

public abstract class Node
{
  protected String name;

  public Node(final String name) {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }

  public void setName(final String name)
  {
    this.name = name;
  }
}
