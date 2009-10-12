package net.sourceforge.waters.analysis.distributed.safetyverifier;

import net.sourceforge.waters.analysis.distributed.schemata.AutomatonSchema;

/**
 * Select a number of automata
 */
interface AutomataSelector
{
  public AutomatonSchema[] select(AutomatonSchema[] automata);
}