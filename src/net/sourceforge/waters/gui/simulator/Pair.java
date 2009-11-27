package net.sourceforge.waters.gui.simulator;

public class Pair<A, B>
{

  // #################################################################################
  // # Constructors
  public Pair(final A a, final B b)
  {
    this.a = a;
    this.b = b;
  }

  // #################################################################################
  // # Accessor Methods
  public A getFirst()
  {
    return a;
  }

  public B getSecond()
  {
    return b;
  }

  // #################################################################################
  // # Data Members
  A a;
  B b;
}
