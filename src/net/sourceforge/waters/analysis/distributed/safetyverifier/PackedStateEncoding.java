package net.sourceforge.waters.analysis.distributed.safetyverifier;

import net.sourceforge.waters.analysis.distributed.schemata.*;

/**
 * A packed state encoding. This represents a synchronous product
 * state in an integer array, using the fewest bits possible.
 */
public class PackedStateEncoding extends StateEncoding
{
  public PackedStateEncoding(ProductDESSchema des)
  {
    mModel = des;
    mStartBit = new int[des.getAutomataCount()];
    mLength = new int[des.getAutomataCount()];

    int c_bit = 0;

    for (int i = 0; i < mStartBit.length; i++)
      {
	mStartBit[i] = c_bit;
	mLength[i] = calculateBitsNeeded(des.getAutomaton(i));

	//Increment the current bit by the length of the
	//previous automaton coding.
	c_bit += mLength[i];
      }

    //The length of the encoded state array, in 32 bit
    //integers.
    mStateArrayLength = (int)Math.ceil(c_bit / 32.0);
  }

  /**
   * Encode a product state from a vector of unpacked state
   * indices. Each element in the input array corresponds to
   * an automaton in the product.
   * @param unpacked Unpacked state vector.
   * @return Packed state tuple.
   */
  public StateTuple encodeState(int[] unpacked)
  {
    int[] pstate = new int[mStateArrayLength];

    for (int i = 0; i < unpacked.length; i++)
      {
	encodeAutomatonState(pstate, i, unpacked[i]);
      }

    return new StateTuple(pstate);
  }


  /**
   * Return an unpacked state vector from a packed tuple.  Indices in
   * the unpacked state vector correspond to automata in the product;
   * the value in each element a state index for that automaton.
   * @param packed The packed state tuple.
   * @return An unpacked state vector.
   */
  public int[] decodeState(StateTuple packed)
  {
    int[] pstate = packed.getStateArray();
    int[] unpacked = new int[mStartBit.length];
    
    for (int i = 0; i < unpacked.length; i++)
      {
	unpacked[i] = decodeAutomatonState(pstate, i);
      }

    return unpacked;
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

  private void encodeAutomatonState(int[] pstate, int aut, int value)
  {
    int index = mStartBit[aut] / 32;
    int sbit = mStartBit[aut] % 32;
    int length = mLength[aut];
    
    //Lengths greater than 32 bits are silly, no automaton could have
    //that many states, nor could it be stored in an integer.
    assert (length < 32);

    //Calculate the length that needs to be stored in the
    //current element.
    int ce_length = Math.min(length, 32 - sbit);

    //Build a value mask.
    //In the case where ce_len == 32 - sbit; by substitution
    //you get 32 - (32 - sbit), equivalent to shifting by
    //sbit. In the case where ce_len == length, it is
    //equivalent to shifting sbit + left over bits, i.e.
    // [00001111100000000]
    //  ----     --------  (shift right by 12 bits to get this)
    int vmask = ~(0) >>> (32 - ce_length);

    pstate[index] = (pstate[index] & ~(vmask << sbit)) | (value & vmask) << sbit;

    //If there is still more data to pack, do this now.
    if (ce_length < length)
      {
	length = length - ce_length;
	value = value >> ce_length;
	vmask = ~(0) >>> (32 - length);
	pstate[index+1] = (pstate[index+1] & ~(vmask)) | (value & vmask);
      }
  }

  private int decodeAutomatonState(int[] pstate, int aut)
  {
    //Same as for encodeAutomatonState
    int index = mStartBit[aut] / 32;
    int sbit = mStartBit[aut] % 32;
    int length = mLength[aut];
    assert (length < 32); 

    int ce_length = Math.min(length, 32 - sbit);
    int vmask = ~(0) >>> (32 - ce_length);

    int value = (pstate[index] & (vmask << sbit)) >>> sbit;

    if (ce_length < length)
      {
	length = length - ce_length;
	value = value << length;
	vmask = ~(0) >>> (32 - length);
	value = value | (pstate[index+1] & vmask);
      }

    return value;
  }

  private int calculateBitsNeeded(AutomatonSchema at)
  {
    return clog2(at.getStateCount());
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


  private final ProductDESSchema mModel;

  //Array of start bit and length for each automaton in the DES
  //product. The length is the number of bits needed to encode the
  //state. As it is very unlikely any automaton will have more than 32
  //bits of states, the encoding will not span more than one integer
  //in the state representation.
  private final int[] mStartBit;
  private final int[] mLength;
  private final int mStateArrayLength;
}