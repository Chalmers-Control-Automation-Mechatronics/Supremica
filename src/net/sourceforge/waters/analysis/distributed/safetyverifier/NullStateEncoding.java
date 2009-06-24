package net.sourceforge.waters.analysis.distributed.safetyverifier;

import net.sourceforge.waters.analysis.distributed.schemata.*;

public class NullStateEncoding extends StateEncoding
{
  public NullStateEncoding (ProductDESSchema des)
  {
    mModel = des;
  }

  public StateTuple encodeState(int[] unpacked)
  {
    return new StateTuple(unpacked.clone());
  }

  public int[] decodeState(StateTuple packed)
  {
    return packed.getStateArray();
  }
  
  public String interpret(int[] unpacked)
  {
    StringBuilder sb = new StringBuilder();
    sb.append("[");

    for (int i = 0; i < unpacked.length; i++)
      {
	if (i != 0)
	  sb.append(",");

	sb.append(mModel.getAutomaton(i).getState(unpacked[i]).getName());
      }

    sb.append("]");
    return sb.toString();
  }

  public String interpret(StateTuple state)
  {
    return interpret(state.getStateArray());
  }

  private final ProductDESSchema mModel;
}