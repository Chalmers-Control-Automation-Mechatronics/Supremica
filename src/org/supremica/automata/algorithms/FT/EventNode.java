package org.supremica.automata.algorithms.FT;

/**
 * Zenuity 2019 Hackfest
 *
 * Abstract class for representing type of fault event.
 *
 * @author zhefei
 */

abstract public class EventNode extends Node
{
  public EventNode(final String eventName)
  {
    super(eventName);
  }
}
