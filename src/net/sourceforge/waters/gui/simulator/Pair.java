package net.sourceforge.waters.gui.simulator;

public class Pair<A,B>
{

  A a;
  B b;

  public Pair(A a, B b)
  {
    this.a = a;
    this.b = b;
  }

  public A getFirst()
  {
    return a;
  }
  public B getSecond()
  {
    return b;
  }
}
