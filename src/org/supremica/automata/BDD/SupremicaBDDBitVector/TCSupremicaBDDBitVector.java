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
        int n, val = 0;
        boolean negative = false;
        TCSupremicaBDDBitVector finalBitVec = buildSupBDDBitVector(bitNum);
        if(bitvec[signBitIndex].isOne())
        {
            negative = true;
            finalBitVec = toTwosComplement();
        }
        else
            finalBitVec = (TCSupremicaBDDBitVector)copy();

        for (n = finalBitVec.bitNum - 1; n >= 0; n--)
            if (finalBitVec.bitvec[n].isOne())
              val = (val << 1) | 1;
            else if (finalBitVec.bitvec[n].isZero())
              val = val << 1;
            else
                return 0;

        if(negative)
            return (0-val);
        else
            return val;
    }

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

        BDD p = mFactory.one();
        for (int n=0 ; n< getLargerLength(that); n++)
        {

            BDD leftBDD = bitvec[signBitIndex];
            BDD rightBDD = that.bitvec[((TCSupremicaBDDBitVector)that).getSignBitIndex()];

            if(n < this.bitNum)
                leftBDD = bitvec[n];

            if(n < that.bitNum)
                rightBDD = that.bitvec[n];

            final BDD tmp1 = leftBDD.apply(rightBDD, BDDFactory.biimp);
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
      final TCSupremicaBDDBitVector shiftedRemainder = (TCSupremicaBDDBitVector) remainder.shl(1, bitvec[step]);
      final BDD isSmaller = divisor.lte(shiftedRemainder);
      final SupremicaBDDBitVector newResult = result.shl(1, isSmaller);
      final SupremicaBDDBitVector zero = buildSupBDDBitVector(divisor.bitvec.length, false);
      final SupremicaBDDBitVector sub = buildSupBDDBitVector(divisor.bitvec.length, false);

      for (int n = 0; n < divisor.bitvec.length; n++)
          sub.bitvec[n] = isSmaller.ite(divisor.bitvec[n], zero.bitvec[n]);

      final TCSupremicaBDDBitVector newRemainder = shiftedRemainder.sub(sub);

      if (step > 0)
          div_rec(divisor, newRemainder, newResult, step - 1);

      shiftedRemainder.free();
      sub.free();
      zero.free();
      isSmaller.free();

      result.replaceWith(newResult);
      remainder.replaceWith(newRemainder);
    }

}