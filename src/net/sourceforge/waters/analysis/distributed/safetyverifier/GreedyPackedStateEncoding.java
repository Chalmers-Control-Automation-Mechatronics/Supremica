package net.sourceforge.waters.analysis.distributed.safetyverifier;

import java.util.Arrays;
import java.util.Comparator;

import net.sourceforge.waters.analysis.distributed.schemata.*;

/**
 * A slightly simpler, but less naive state encoding.
 * This greedily allocates automata in decreasing order
 * order of size. This should give a reasonably efficient
 * encoding, but will avoid the fairly rare case where
 * the data spans a word boundary.
 *
 * This is based on the approach used in the Waters 
 * native checker.
 */
public class GreedyPackedStateEncoding extends StateEncoding
{
  public GreedyPackedStateEncoding(ProductDESSchema des)
  {
    mModel = des;

    //Get the automata and sort them by size. To facilitate this, the
    //automaton schema now stores the id, rather than having it
    //implicit in the ordering of automata.
    
    //The total number of words necessary can be calculated, and the
    //automata can be allocated to words in order of size. In most
    //cases, this should result in fairly good packing efficiency, as
    //large automata are uncommon: the small ones should fill in the
    //gaps.
    AutomatonSchema[] aut = des.getAutomata();
    Arrays.sort(aut, new AutomatonSizeComparator());
    
    mWordIndex = new int[aut.length];
    mNumBits = new int[aut.length];
    mBitMask = new int[aut.length];
    mShift = new int[aut.length];
    
    //The worst possible encoding will have one word
    //for every automaton. While this is very unlikely,
    //it gives an upper bound on tuple size.
    int[] wordUsage = new int[aut.length];

    //Assume initially that we will use 1 word.
    int wordCount = 1;

    for (int i = 0; i < aut.length; i++)
      {
	int id = aut[i].getAutomatonId();
	int length = clog2(aut[i].getStateCount());

	//An automaton with 1 state will have a length of zero. This
	//doesn't have to be allocated anywhere.
	if (length == 0)
	  continue;

	//Find the first word that the value will fit into.
	int destWord = -1;
	for (int w = 0; w < wordCount; w++)
	  {
	    if (wordUsage[w] + length <= 32)
	      {
		//Word will be full if we put it here
		destWord = w;
		break;
	      }
	  }

	//If the value didn't fit then add a new one.
	if (destWord < 0)
	  destWord = wordCount++;

	//The automaton state can be encoded
	//into this word.
	mWordIndex[id] = destWord;
	mShift[id] = wordUsage[destWord];
	mBitMask[id] = ((~0) >>> (32 - length)) << mShift[id];
	mNumBits[id] = length;
	wordUsage[destWord] += length;
      }

    mWordCount = wordCount;

    System.err.format("Automaton count: %d, Encoded word count: %d\n", aut.length, mWordCount);
  }

  public void outputDebugging()
  {
    for (int i = 0; i < mWordIndex.length; i++)
      {
	System.err.format("word: %d, bits: %d, bitmask: %d, shift: %d\n",
			  mWordIndex[i], 
			  mNumBits[i],
			  mBitMask[i],
			  mShift[i]);
      }
  }

  public StateTuple encodeState(int[] unpacked, int depth)
  {
    int[] pstate = new int[mWordCount];

    for (int i = 0; i < unpacked.length; i++)
      {
	pstate[mWordIndex[i]] |= (unpacked[i] << mShift[i]) & mBitMask[i];
      }

    return new StateTuple(pstate, depth);
  }

  public int[] decodeState(StateTuple packed)
  {
    return decodeState(packed, new int[mWordIndex.length]);
  }

  public int[] decodeState(StateTuple packed, int[] unpacked)
  {
    int[] pstate = packed.getStateArray();
    
    for (int i = 0; i < unpacked.length; i++)
      {
	unpacked[i] = (pstate[mWordIndex[i]] & mBitMask[i]) >>> mShift[i];
      }

    return unpacked;
  }

  public int decodeAutomatonState(StateTuple packed, int a)
  {
    return (packed.getStateArray()[mWordIndex[a]] & mBitMask[a]) >>> mShift[a];
  }

  public String interpret(StateTuple packed)
  {
    return interpret(decodeState(packed));
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

  private static class AutomatonSizeComparator implements Comparator<AutomatonSchema>
  {
    public int compare(AutomatonSchema a, AutomatonSchema b)
    {
      return b.getStateCount() - a.getStateCount();
    }
  }


  private static int clog2(int x)
  {
    x--;
    int y = 0;
    while (x > 0) 
      {
	x >>= 1;
	y++;
      }
    return y;
  }

  public int getEncodedLength()
  {
    return mWordCount;
  }

  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

  private final int[] mWordIndex;
  private final int[] mNumBits;
  private final int[] mBitMask;
  private final int[] mShift;
  private final int mWordCount;
  
  private final ProductDESSchema mModel;
}