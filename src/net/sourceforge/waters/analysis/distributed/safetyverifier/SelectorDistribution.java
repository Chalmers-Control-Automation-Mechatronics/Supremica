package net.sourceforge.waters.analysis.distributed.safetyverifier;

import net.sourceforge.waters.analysis.distributed.schemata.AutomatonSchema;
import net.sourceforge.waters.analysis.distributed.schemata.ProductDESSchema;


public class SelectorDistribution extends HashStateDistribution
{
  private static final long serialVersionUID = -6465883083272581701L;

  public SelectorDistribution(String[] handlers, ProductDESSchema model,
		  					  StateEncoding encoding, AutomataSelector selector)
  {
    super(handlers);

    //We will just work with what we are given
    AutomatonSchema[] selectedAutomata = selector.select(model.getAutomata());

    for (AutomatonSchema aut : selectedAutomata) 
      {
	System.err.format("%s: %d\n", aut.getName(), aut.getStateCount());
      }
     
   
    mHashAutomata = new int[selectedAutomata.length];
    mAutomataShift = new int[selectedAutomata.length];
    int cshift = 0;
    for (int i = 0; i < selectedAutomata.length; i++)
      {
	mHashAutomata[i] = selectedAutomata[i].getAutomatonId();
	mAutomataShift[i] = cshift;
	cshift += Util.clog2(selectedAutomata[i].getStateCount());
      }    

    mEncoding = encoding;
  }

  public StateHandler lookupStateHandler(StateTuple state)
  {
    int hash = 0;
    for (int i = 0; i < mHashAutomata.length; i++)
      {
	int aut = mHashAutomata[i];
	int shift = mAutomataShift[i];
	hash |= mEncoding.decodeAutomatonState(state, aut) << shift;
      }
    return hashLookupStateHandler(hash);
  }

  private final int[] mHashAutomata;
  private final int[] mAutomataShift;
  private final StateEncoding mEncoding;

}