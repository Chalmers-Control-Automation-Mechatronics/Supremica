package org.supremica.automata.BDD.SupremicaBDDBitVector;

import java.math.BigInteger;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import net.sf.javabdd.BDDFactory;

/**
 *
 * @author Sajed
 */
public final class PSupremicaBDDBitVector extends SupremicaBDDBitVector
{

    public PSupremicaBDDBitVector(final BDDFactory factory, final int bitNum)
    {
        super(factory,bitNum);
    }

    public PSupremicaBDDBitVector(final BDDFactory factory, final int bitNum, final boolean b)
    {
        this(factory, bitNum);
        initialize(b);
    }

    public PSupremicaBDDBitVector(final BDDFactory factory, final int bitNum, final long val)
    {
        this(factory, bitNum);
        initialize(val);
    }

    public PSupremicaBDDBitVector(final BDDFactory factory,final int bitNum, final BDDDomain d)
    {
      this(factory, bitNum);
      initialize(d);
    }

    @Override
    protected PSupremicaBDDBitVector buildSupBDDBitVector(final int bitNum)
    {
        return new PSupremicaBDDBitVector(mFactory, bitNum);
    }

    @Override
    protected PSupremicaBDDBitVector buildSupBDDBitVector(final int bitNum, final boolean val)
    {
        return new PSupremicaBDDBitVector(mFactory, bitNum, val);
    }

    @Override
    protected PSupremicaBDDBitVector buildSupBDDBitVector(final int bitNum, final long val)
    {
        return new PSupremicaBDDBitVector(mFactory, bitNum, val);
    }

    @Override
    protected void initialize(long val)
    {
        for (int n = 0; n < bitvec.length; n++)
        {
            if ((val & 0x1) != 0)
                bitvec[n] = mFactory.one();
            else
                bitvec[n] = mFactory.zero();

            val >>= 1;
        }
    }

    @Override
    protected void initialize(BigInteger val)
    {
        for (int n = 0; n < bitvec.length; n++) {
            if (val.testBit(0))
                bitvec[n] = mFactory.one();
            else
                bitvec[n] = mFactory.zero();

            val = val.shiftRight(1);
        }
    }

    @Override
    public int val()
    {
        // We will shift bits into the variable val.
        int n, val = 0;

        for (n = bitvec.length - 1; n >= 0; n--)
            if (bitvec[n].isOne())
                // This bit is always one, shift a 1 into the result.
                val = (val << 1) | 1;
            else if (bitvec[n].isZero())
                // This bit is always zero, shift 0 into the result.
                val = val << 1;
            else
                // This bit vector does not represent a constant value.
                return 0;

        return val;
    }


    @Override
    public BDD getBDDThatResultsMaxValue()
    {
        BDD bit = bitvec[0];

        for(int i = 1 ; i < bitvec.length ; i++)
        {
            if(bit.isZero())
            {
                bit = bitvec[i];
                continue;
            }

            if(bit.satCount() == 1)
                return bit;

            final BDD newBDD = bit.and(bitvec[i]);
            if(!newBDD.isZero())
                bit = newBDD.id();
        }


        return bit;

    }

    @Override
    public BDD equ(final SupremicaBDDBitVector r)
    {
//        if (this.bitNum != r.bitNum)
//            throw new BDDException("equ operator: The length of the left-side vector is not equal to the right-side!");

        // We will go through all bits and see if they are equal. Chain all
        // operations with p.
        BDD p = mFactory.one();
        for (int n=0 ; n< getLargerLength(r); n++)
        {

            BDD leftBDD = mFactory.zero();
            BDD rightBDD = mFactory.zero();

            if(n < this.bitNum)
                leftBDD = bitvec[n];

            if(n < r.bitNum)
                rightBDD = r.bitvec[n];

            // Create BDD expressing equality of the ith bit in this and r.
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
//        if (bitvec.length != that.bitvec.length)
//            throw new BDDException("add operator: The length of the left-side vector is not equal to the right-side!");

        BDD c = mFactory.zero();
        final PSupremicaBDDBitVector res = buildSupBDDBitVector(getLargerLength(that));

        for (int n = 0 ; n < res.bitvec.length ; n++)
        {

            BDD leftBDD = mFactory.zero();
            BDD rightBDD = mFactory.zero();

            if(n < this.length())
                leftBDD = bitvec[n];

            if(n < that.length())
                rightBDD = that.bitvec[n];

            /* bitvec[n] = l[n] ^ r[n] ^ c; */
            res.bitvec[n] = leftBDD.xor(rightBDD);
            res.bitvec[n].xorWith(c.id());

            /* c = (l[n] & r[n]) | (c & (l[n] | r[n])); */
            BDD tmp1 = leftBDD.or(rightBDD);
            tmp1 = tmp1.and(c);
            BDD tmp2 = leftBDD.and(rightBDD);
            tmp2 = tmp2.or(tmp1);
            c = tmp2;
        }

        final BDD overflow = c.id();

        c.free();

        return new ResultOverflows(res,overflow);
    }

    public PSupremicaBDDBitVector addRemoveOverflows(final SupremicaBDDBitVector that)
    {
        final ResultOverflows resOvfls = addConsideringOverflows(that);
        final PSupremicaBDDBitVector result = (PSupremicaBDDBitVector)resOvfls.getResult();
        for(int i = 0; i < result.bitNum; i++)
        {
            result.bitvec[i] = result.bitvec[i].and(resOvfls.getOverflows().not());
        }

        return result;
    }

    @Override
    public ResultOverflows subConsideringOverflows(final SupremicaBDDBitVector that)
    {
//       if (bitvec.length != that.bitvec.length)
//            throw new BDDException("sub operator: The length of the left-side vector is not equal to the right-side!");

        final BDDFactory bdd = getFactory();

        BDD c = bdd.zero();
        final PSupremicaBDDBitVector res = buildSupBDDBitVector(getLargerLength(that));

        for (int n = 0; n < res.bitvec.length; n++)
        {

            BDD leftBDD = mFactory.zero();
            BDD rightBDD = mFactory.zero();

            if(n < this.length())
                leftBDD = bitvec[n];

            if(n < that.length())
                rightBDD = that.bitvec[n];

            // bitvec[n] = l[n] ^ r[n] ^ c;
            res.bitvec[n] = leftBDD.xor(rightBDD);
            res.bitvec[n].xorWith(c.id());

            // c = (l[n] & r[n] & c) | (!l[n] & (r[n] | c));
            BDD tmp1 = rightBDD.or(c);
            final BDD tmp2 = leftBDD.apply(tmp1, BDDFactory.less);
            tmp1.free();
            tmp1 = leftBDD.and(rightBDD);
            tmp1.andWith(c);
            tmp1.orWith(tmp2);
            c = tmp1;
        }

        final BDD overflow = c.id();

        c.free();

        return new ResultOverflows(res,overflow);
        //return new ResultOverflows(res,highOrderCarryIn.xor(highOrderCarryOut));
    }

    @Override
    public PSupremicaBDDBitVector add(final SupremicaBDDBitVector that)
    {
      return (PSupremicaBDDBitVector)addConsideringOverflows(that).getResult();
    }

    @Override
    public PSupremicaBDDBitVector sub(final SupremicaBDDBitVector that)
    {
      return (PSupremicaBDDBitVector)subConsideringOverflows(that).getResult();
    }

    @Override
    protected BDD lthe(final SupremicaBDDBitVector r, final BDD thanORequal)
    {
//        if (this.bitvec.length != r.bitvec.length)
//            throw new BDDException("lte operator: The length of the left-side vector is not equal to the right-side!");

        BDD p = thanORequal.id();
        for (int n=0 ; n<getLargerLength(r) ; n++)
        {
            /* p = (!l[n] & r[n]) |
            *     bdd_apply(l[n], r[n], bddop_biimp) & p; */
            BDD leftBDD = mFactory.zero();
            BDD rightBDD = mFactory.zero();

            if(n < this.length())
                leftBDD = bitvec[n];

            if(n < r.length())
                rightBDD = r.bitvec[n];

            // Check if current left bit is less than current right bit
            final BDD tmp1 = leftBDD.apply(rightBDD, BDDFactory.less);
            // Check if current left bit is equivalent to current right bit
            final BDD tmp2 = leftBDD.apply(rightBDD, BDDFactory.biimp);
            // Carry the result from previous bit
            tmp2.andWith(p);
            // Check if current left bit is less than or equal to right bit
            tmp1.orWith(tmp2);
            // Remember the carry
            p = tmp1;
        }

        return p;
    }

    //This function needs to be modified
    // Now it is modified, but unclear what the initial comment meant -- Jonas Krook
    @Override
    public void div_rec(final SupremicaBDDBitVector divisor,
                               final SupremicaBDDBitVector remainder,
                               final SupremicaBDDBitVector result,
                               final int step)
    {
        // Shift the msb of the dividend left into the remainder to search for
        // a part of the dividend that are greater than the divisor.
        final SupremicaBDDBitVector shiftedRemainder = remainder.shl(1, bitvec[step]);
        final BDD isSmaller = divisor.lte(shiftedRemainder);
        // If the divisor is smaller than the remainder, we can conclude that,
        // at this bit, we have a quotient of 1. Shift previous quotients to the
        // left since they were calculated for more significant bits.
        final SupremicaBDDBitVector newResult = result.shl(1, isSmaller);

        // Build up a bit vector for subtraction. If the remainder were greater
        // than the divisor and we added 1 to the quotient, we need to subtract
        // the same amount from the remainder.
        final SupremicaBDDBitVector zero = buildSupBDDBitVector(divisor.bitvec.length, false);
        final SupremicaBDDBitVector sub = buildSupBDDBitVector(divisor.bitvec.length, false);

        for (int n = 0; n < divisor.bitvec.length; n++)
            sub.bitvec[n] = isSmaller.ite(divisor.bitvec[n], zero.bitvec[n]);

        final SupremicaBDDBitVector newRemainder = shiftedRemainder.sub(sub);

        // Do the same for the next less significant bit, if any.
        if (step > 0)
            div_rec(divisor, newRemainder, newResult, step - 1);

        shiftedRemainder.free();
        sub.free();
        zero.free();
        isSmaller.free();

        // The remainder and quotient in the last recursion step are the answers
        // we want. Bypass them to the top.
        result.replaceWith(newResult);
        remainder.replaceWith(newRemainder);
    }

    @Override
    public SupremicaBDDBitVector resize(final int bitNum) {

      final SupremicaBDDBitVector res = buildSupBDDBitVector(bitNum, false);

      // Go though all bits for the new bit vector.
      for (int i = 0; i < bitNum; i++) {
        // The default value if the new bit vector is larger.
        BDD z = mFactory.zero();
        if (i < this.bitNum) {
          // The current bit vector has information.
          z = bitvec[i];
        }
        res.setBit(i, z);
      }

      return res;
    }

    @Override
    public BDD increment() {
      BDD carry = mFactory.one();
      for (int i = 0; i<bitNum; i++) {
        // Half adder
        final BDD res = bitvec[i].xor(carry);
        carry = bitvec[i].and(carry);
        bitvec[i] = res;
      }
      return carry;
    }

    @Override
    public int max() {

      int value = 0;
      BDD bdd = mFactory.one();
      // Start with MSB and find the first bit that can be 1.
      for (int i=bitNum-1; i>=0; i--) {
        value = value << 1;
        if ((bitvec[i].and(bdd)).satCount() > 0) {
          // This bit can be one, given previous more significant bits.
          value = value | 1;
          // The max must have this bit satisfiable, so remember that.
          bdd = bdd.and(bitvec[i]);
        }
      }

      return value;
    }

    @Override
    public int min() {

      int value = 0;
      BDD bdd = mFactory.one();
      // Start with MSB and find the first bit that can be 0.
      for (int i=bitNum-1; i>=0; i--) {
        value = value << 1;
        if (bitvec[i].not().and(bdd).satCount() < 1) {
          // This bit cannot be zero, given previous more significant bits.
          value = value | 1;
        } else {
          // This bit can be zero.
          // The min shall satisfy a zero for this bit, so remember that.
          bdd = bdd.and(bitvec[i].not());
        }
      }

      return value;
    }

    @Override
    public int requiredBits() {

      int required = -1;
      // Find the MSB that can be one.
      for (int i = 0; i < bitNum; i++) {
        if (bitvec[i].satCount() > 0) {
          required = i;
        }
      }

      // Add one to convert to length from zero based index.
      return required+1;

    }

}
