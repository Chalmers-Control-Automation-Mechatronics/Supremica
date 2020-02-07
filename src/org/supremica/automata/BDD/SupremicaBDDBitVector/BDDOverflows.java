package org.supremica.automata.BDD.SupremicaBDDBitVector;

import net.sf.javabdd.BDD;

public class BDDOverflows
{

  private final BDD result;
  private final BDD overflows;

  public BDDOverflows(final BDD result, final BDD overflows)
  {
    this.result = result;
    this.overflows = overflows;
  }

  public BDD getResult()
  {
    return result;
  }

  public BDD getOverflows()
  {
    return overflows;
  }

  @Override
  public String toString()
  {
    return String.format("result: %s, bdd: %s",
                         this.result.toString(), this.overflows.toString());
  }

}
