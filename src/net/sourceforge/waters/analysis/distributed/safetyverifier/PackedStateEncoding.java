//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

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

  public void outputDebugging()
  {
    int[] bits = new int[mStateArrayLength * 32];

    for (int i = 0; i < mStartBit.length; i++)
      {
	for (int k = 0; k < mLength[i]; k++)
	  {
	    bits[mStartBit[i] + k]++;
	  }
      }

    for (int i = 0; i < bits.length; i++)
      {
	System.out.print(bits[i]);
      }
    System.out.println();

    for (int i = 0; i < mStartBit.length; i++)
      {
	System.out.format("aut: %d, startbit: %d, length: %d\n",
			  i, mStartBit[i], mLength[i]);
      }
  }

  /**
   * Encode a product state from a vector of unpacked state
   * indices. Each element in the input array corresponds to
   * an automaton in the product.
   * @param unpacked Unpacked state vector.
   * @return Packed state tuple.
   */
  public StateTuple encodeState(int[] unpacked, int depth)
  {
    int[] pstate = new int[mStateArrayLength];

    for (int i = unpacked.length - 1; i >=0; i--)
      {
	encodeAutomatonState(pstate, i, unpacked[i]);
      }

    StateTuple t = new StateTuple(pstate, depth);


    int[] unpacked2 = decodeState(t);

      //Check if the state encodes/decodes correctly. 
      if (!java.util.Arrays.equals(unpacked, unpacked2))
	{
	  String errmsg = String.format("Encoding failure: %s %s", 
					java.util.Arrays.toString(unpacked),
					java.util.Arrays.toString(unpacked2));
	  System.err.println(errmsg);
	  throw new RuntimeException(errmsg);
      }

    return t;
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

    //If the length is 0 bits, we must handle this as a special case
    //(otherwise the vmask calculation becomes ~0 >>> 32, which is ~0
    //and undesirable.  If the length is 0, then 0 is the only posible
    //state. This encodes to nothing, so just return.
    if (length == 0)
      return;

    //Calculate the length that needs to be stored in the
    //current element.
    int ce_length = Math.min(length, 32 - sbit);
    assert (ce_length > 0);

    //Build a value mask.
    //In the case where ce_len == 32 - sbit; by substitution
    //you get 32 - (32 - sbit), equivalent to shifting by
    //sbit. In the case where ce_len == length, it is
    //equivalent to shifting sbit + left over bits, i.e.
    // [00001111100000000]
    //  ----     --------  (shift right by 12 bits to get this)
    int vmask = ~(0) >>> (32 - ce_length);

    pstate[index] = (pstate[index] & ~(vmask << sbit)) | ((value & vmask) << sbit);

    //If there is still more data to pack, do this now.
    if (ce_length < length)
      {
	length = length - ce_length;
	value = value >>> ce_length;
	vmask = ~(0) >>> (32 - length);
	pstate[index+1] = (pstate[index+1] & ~(vmask)) | (value & vmask);
      }
  }

  public int decodeAutomatonState(StateTuple state, int automaton)
  {
    return decodeAutomatonState(state.getStateArray(), automaton);
  }

  private int decodeAutomatonState(int[] pstate, int aut)
  {
    //Same as for encodeAutomatonState
    int index = mStartBit[aut] / 32;
    int sbit = mStartBit[aut] % 32;
    int length = mLength[aut];

    assert (length < 32); 

    //If this automaton encodes into 0 bits, the only possible state
    //is 0. This must be handled as a special case to prevent the
    //mask being incorrectly calculated.
    if (length == 0)
      return 0;

    int ce_length = Math.min(length, 32 - sbit);
    int vmask = ~(0) >>> (32 - ce_length);

    int value = (pstate[index] & (vmask << sbit)) >>> sbit;

    if (ce_length < length)
      {
	length = length - ce_length;
	vmask = ~(0) >>> (32 - length);
	value = value | ((pstate[index+1] & vmask) << length);
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

  public int getEncodedLength()
  {
    return mStateArrayLength;
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


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
