package org.supremica.automata.BDD.SupremicaBDDBitVector;

import java.math.BigInteger;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import net.sf.javabdd.BDDFactory;

/**
 *
 * @author Sajed
 */
public final class TCSupremicaBDDBitVector extends SupremicaBDDBitVector
{
    int signBitIndex;


    public TCSupremicaBDDBitVector(final BDDFactory factory, final int bitNum)
    {
        super(factory,bitNum);
        signBitIndex = bitNum-1;
    }

    public TCSupremicaBDDBitVector(final BDDFactory factory, final int bitNum, final boolean b)
    {
        this(factory, bitNum);
        initialize(b);
    }

    public TCSupremicaBDDBitVector(final BDDFactory factory, final int bitNum, final long val)
    {
        this(factory, bitNum);
        initialize(val);
    }

    public TCSupremicaBDDBitVector(final BDDFactory factory, final BDDDomain d)
    {
      this(factory, d.varNum());
      initialize(d);
    }

    public int getSignBitIndex()
    {
        return signBitIndex;
    }

    @Override
    protected TCSupremicaBDDBitVector buildSupBDDBitVector(final int bitNum)
    {
        return new TCSupremicaBDDBitVector(mFactory, bitNum);
    }

    @Override
    protected TCSupremicaBDDBitVector buildSupBDDBitVector(final int bitNum, final boolean val)
    {
        return new TCSupremicaBDDBitVector(mFactory, bitNum, val);
    }

    @Override
    protected TCSupremicaBDDBitVector buildSupBDDBitVector(final int bitNum, final long val)
    {
        return new TCSupremicaBDDBitVector(mFactory, bitNum, val);
    }

    //Not implemented yet
    @Override
    public BDD getBDDThatResultsMaxValue()
    {
        return bitvec[bitNum-2];
    }

    @Override
    protected void initialize(final long val)
    {
        long absVal = Math.abs(val);
        for (int n = 0; n < bitNum; n++)
        {
            if ((absVal & 0x1) != 0)
                bitvec[n] = mFactory.one();
            else
                bitvec[n] = mFactory.zero();

            absVal >>= 1;
        }
        if (val < 0)
        {
            final TCSupremicaBDDBitVector res = toTwosComplement();
            System.arraycopy(res.bitvec, 0, bitvec, 0, bitNum);
        }
    }

    @Override
    protected void initialize(final BigInteger val)
    {
        BigInteger absVal = val.abs();
        for (int n = 0; n < bitNum; n++)
        {
            if (absVal.testBit(0))
                bitvec[n] = mFactory.one();
            else
                bitvec[n] = mFactory.zero();

            absVal = absVal.shiftRight(1);
        }
        if (val.intValue()<0)
        {
            final TCSupremicaBDDBitVector res = toTwosComplement();
            System.arraycopy(res.bitvec, 0, bitvec, 0, bitNum);
        }
    }

    @Override
    public int val()
    {
        // We will shift bits into the variable val.
        int n, val = 0;
        boolean negative = false;
        TCSupremicaBDDBitVector finalBitVec = buildSupBDDBitVector(bitNum);
        if(bitvec[signBitIndex].isOne())
        {
            negative = true;
            // We have a negative number, so convert to positive since that is
            // easier to work with.
            finalBitVec = toTwosComplement();
        }
        else
            finalBitVec = (TCSupremicaBDDBitVector)copy();

        for (n = finalBitVec.bitNum - 1; n >= 0; n--)
            if (finalBitVec.bitvec[n].isOne())
              // This bit is always one, shift a 1 into the result.
              val = (val << 1) | 1;
            else if (finalBitVec.bitvec[n].isZero())
              // This bit is always zero, shift 0 into the result.
              val = val << 1;
            else
              // This bit vector does not represent a constant value.
              return 0;

        // Revert the conversion to positive number.
        if(negative)
            return (0-val);
        else
            return val;
    }

    /**
     * Create the twos complement of this bit vector. Same as multiply with -1.
     * Note that twos complement might overflow if the bit vector represents
     * the minimum value.
     * For instance, for three bits the minimum is -4. The twos complement of
     * -4 represented by three bits is -4.
     * @return the negation of the bit vector.
     */
    public TCSupremicaBDDBitVector toTwosComplement()
    {
        final TCSupremicaBDDBitVector res = buildSupBDDBitVector(bitNum);
        for (int n = 0; n < res.bitNum; n++)
            res.bitvec[n] = bitvec[n].not();

        return res.add(buildSupBDDBitVector(bitNum, 1));
    }


    @Override
    public BDD equ(final SupremicaBDDBitVector that)
    {
//        if (this.bitNum != r.bitNum)
//            throw new BDDException("equ operator: The length of the left-side vector is not equal to the right-side!");

        // We will go through all bits and see if they are equal. Chain all
        // operations with p.
        BDD p = mFactory.one();
        for (int n=0 ; n< getLargerLength(that); n++)
        {

            BDD leftBDD = bitvec[signBitIndex];
            BDD rightBDD = that.bitvec[((TCSupremicaBDDBitVector)that).getSignBitIndex()];

            if(n < this.bitNum)
                leftBDD = bitvec[n];

            if(n < that.bitNum)
                rightBDD = that.bitvec[n];

            // Create BDD expressing equality of the ith bit in this and that.
            final BDD tmp1 = leftBDD.apply(rightBDD, BDDFactory.biimp);
            // And with p to ensure that all bits are equal.
            final BDD tmp2 = tmp1.and(p);
            p = tmp2;
        }

        return p;
    }

    @Override
    public ResultOverflows addConsideringOverflows(final SupremicaBDDBitVector that)
    {
//        if (bitNum != that.bitNum)
//            throw new BDDException();

        BDD c = mFactory.zero();
        BDD highOrderCarryIn = mFactory.zero();
        BDD highOrderCarryOut = mFactory.zero();
        final TCSupremicaBDDBitVector res = buildSupBDDBitVector(getLargerLength(that));

        for (int n = 0; n < res.bitNum; n++)
        {

            BDD leftBDD = bitvec[signBitIndex];
            BDD rightBDD = that.bitvec[((TCSupremicaBDDBitVector)that).getSignBitIndex()];

            if(n < this.length())
                leftBDD = bitvec[n];

            if(n < that.length())
                rightBDD = that.bitvec[n];

            /* bitvec[n] = l[n] ^ r[n] ^ c; */
            res.bitvec[n] = leftBDD.xor(rightBDD);
            res.bitvec[n].xorWith(c.id());

            /* c = (l[n] & r[n]) | (c & (l[n] | r[n])); */
            final BDD tmp1 = leftBDD.or(rightBDD);
            tmp1.andWith(c);
            final BDD tmp2 = leftBDD.and(rightBDD);
            tmp2.orWith(tmp1);
            c = tmp2;
            if(n == res.getSignBitIndex()-1)
            {
                highOrderCarryIn = c.id();
            }
            if(n == res.getSignBitIndex())
            {
                highOrderCarryOut = c.id();
            }
        }
        c.free();

        //if carryIn and carrtOut on the high order bit are different an overflow has occured
        return new ResultOverflows(res,highOrderCarryIn.xor(highOrderCarryOut));
    }

    @Override
    public ResultOverflows subConsideringOverflows(final SupremicaBDDBitVector that)
    {
      // TODO: toTwosComplement might result in overflows.
      return addConsideringOverflows(((TCSupremicaBDDBitVector)that).toTwosComplement());
    }

    @Override
    public TCSupremicaBDDBitVector add(final SupremicaBDDBitVector that)
    {
      return (TCSupremicaBDDBitVector)addConsideringOverflows(that).getResult();
    }

    @Override
    public TCSupremicaBDDBitVector sub(final SupremicaBDDBitVector that)
    {
      return (TCSupremicaBDDBitVector)subConsideringOverflows(that).getResult();
    }

    @Override
    protected BDD lthe(final SupremicaBDDBitVector that, final BDD thanORequal)
    {
//        if (this.bitNum != r.bitNum)
//            throw new BDDException();
        BDD p = thanORequal;
        for (int n = 0; n < getLargerLength(that); n++)
        {
            BDD leftBDD = bitvec[signBitIndex];
            BDD rightBDD = that.bitvec[((TCSupremicaBDDBitVector)that).getSignBitIndex()];

            if(n < this.length())
                leftBDD = bitvec[n];

            if(n < that.length())
                rightBDD = that.bitvec[n];

            final BDD tmp1 = leftBDD.apply(rightBDD, BDDFactory.less);
            final BDD tmp2 = leftBDD.apply(rightBDD, BDDFactory.biimp);
            tmp2.andWith(p);
            tmp1.orWith(tmp2);
            p = tmp1;
        }
        final BDD SBILBDD = bitvec[signBitIndex];
        final BDD SBIRBDD = that.bitvec[((TCSupremicaBDDBitVector)that).getSignBitIndex()];
        return (SBILBDD.and(SBIRBDD.not())).or((SBILBDD.or(SBIRBDD.not())).and(p));
    }

    //This function needs to be modified
    // Now it is modified, but unclear what the initial comment meant. This
    // method is identical to the one in PSupremicaBDDBitVector, except some
    // type casts, so there seems to be really no reason as to why these two
    // methods cannot be implemented in the superclass. The only obstacle is
    // the method sub(), but that should be easy to have an abstract declaration
    // in the superclass. -- Jonas Krook
    @Override
    public void div_rec(final SupremicaBDDBitVector divisor,
                               final SupremicaBDDBitVector remainder,
                               final SupremicaBDDBitVector result,
                               final int step)
    {
      final SupremicaBDDBitVector shiftedRemainder = remainder.shl(1, bitvec[step]);
      final BDD isSmaller = divisor.lte(shiftedRemainder);
      final SupremicaBDDBitVector newResult = result.shl(1, isSmaller);
      final SupremicaBDDBitVector zero = buildSupBDDBitVector(divisor.bitvec.length, false);
      final SupremicaBDDBitVector sub = buildSupBDDBitVector(divisor.bitvec.length, false);

      for (int n = 0; n < divisor.bitvec.length; n++)
          sub.bitvec[n] = isSmaller.ite(divisor.bitvec[n], zero.bitvec[n]);

      final SupremicaBDDBitVector newRemainder = shiftedRemainder.sub(sub);

      if (step > 0)
          div_rec(divisor, newRemainder, newResult, step - 1);

      shiftedRemainder.free();
      sub.free();
      zero.free();
      isSmaller.free();

      result.replaceWith(newResult);
      remainder.replaceWith(newRemainder);
    }

    @Override
    public SupremicaBDDBitVector resize(final int bitNum) {

      final SupremicaBDDBitVector res = buildSupBDDBitVector(bitNum, false);

      // Go through all bits of the new bit vector.
      for (int i = 0; i < bitNum; i++) {
        // If the new bit vector is larger we need to pad with the sign bit.
        // And if the new vector is smaller we need to keep the sign.
        BDD z = bitvec[signBitIndex];
        if (i < this.signBitIndex) {
          // This bit vector has info, so just copy it.
          z = bitvec[i];
        }
        res.setBit(i, z);
      }

      return res;
    }

    @Override
    public BDD increment() {

      BDD carry = mFactory.one();
      for (int i = 0; i<signBitIndex; i++) {
        // Half adder
        final BDD res = bitvec[i].xor(carry);
        carry = bitvec[i].and(carry);
        bitvec[i] = res;
      }
      // Overflows occur when [0 1 .. 1] is incremented. Determine if that has
      // happened.
      final BDD res = bitvec[signBitIndex].xor(carry);
      carry = bitvec[signBitIndex].not().and(carry);
      bitvec[signBitIndex] = res;
      return carry;
    }

    private int maxTC() {

      int value = 0;
      BDD bdd = mFactory.one();
      // We know the maximum is positive, so require the sign bit to be zero and
      // find the max of the remaining bits.
      bdd = bdd.and(bitvec[signBitIndex].not());
      for (int i=signBitIndex-1; i>=0; i--) {
        value = value << 1;
        if ((bitvec[i].and(bdd)).satCount() > 0) {
          value = value | 1;
          bdd = bdd.and(bitvec[i]);
        }
      }

      return value;
    }

    private int minTC() {

      int value = 0;
      BDD bdd = mFactory.one();
      // We know that the minimum is positive, so require the sign bit to be
      // zero and find the minimum of the remaining bits.
      bdd = bdd.and(bitvec[signBitIndex].not());
      for (int i=signBitIndex-1; i>=0; i--) {
        value = value << 1;
        if (bitvec[i].not().and(bdd).satCount() < 1) {
          value = value | 1;
        } else {
          bdd = bdd.and(bitvec[i].not());
        }
      }

      return value;
    }

    @Override
    public int max() {

      TCSupremicaBDDBitVector t = (TCSupremicaBDDBitVector) resize(bitNum+1);
      int value = 0;

      if (bitvec[signBitIndex].isOne()) {
        // If all numbers that this vector can represent are negative, then we
        // can find the maximum by taking the twos complement, find its min, and
        // multiply with -1.
        t = t.toTwosComplement();
        value = -t.minTC();
      } else {
        // The max is positive.
        t = (TCSupremicaBDDBitVector) t.copy();
        value = t.maxTC();
      }

      return value;
    }

    @Override
    public int min() {

      TCSupremicaBDDBitVector t = (TCSupremicaBDDBitVector) resize(bitNum+1);
      int value = 0;

      if (bitvec[signBitIndex].isZero()) {
        // The minimum is positive.
        t = (TCSupremicaBDDBitVector) t.copy();
        value = t.minTC();
      } else {
        // The minimum is negative, so we do twos complement and find its max.
        // Then multiply the result with -1.
        t = t.toTwosComplement();
        value = -t.maxTC();
      }

      return value;
    }

    @Override
    public int requiredBits() {

      // First check how many bits are required for the positive numbers that
      // this vector can represent.
      // We add two later. If no 1s are found for the positive part, then
      // the bit vector might represent 0, and no bits are required.
      int requiredPos = -2;
      final BDD pos = bitvec[signBitIndex].not();
      for (int i = 0; i < signBitIndex; i++) {
        if (pos.and(bitvec[i]).satCount() > 0) {
          requiredPos = i;
        }
      }

      // Then check how many bits are required for the negative numbers that
      // this vector can represent.
      int requiredNeg = -1;
      final BDD neg = bitvec[signBitIndex];
      if (neg.satCount() > 0 ) {
        // We know that we have at least one negative number, so at least one
        // bit is needed. (The one will be added later.)
        requiredNeg = 0;
      }
      for (int i = 0; i < signBitIndex; i++) {
        // Search for the most significant 1 which is followed by 0.
        if (neg.and(bitvec[i].not()).satCount() > 0) {
          requiredNeg = i+1;
        }
      }

      // Convert to size from zero based index, and use the largest.
      // Positive indices need to be incremented by two since a sign bit is
      // needed.
      final int required = Math.max(requiredPos+2, requiredNeg+1);

      return required;

    }

}