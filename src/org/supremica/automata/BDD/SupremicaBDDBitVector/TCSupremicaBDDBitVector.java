package org.supremica.automata.BDD.SupremicaBDDBitVector;

import java.math.BigInteger;
import net.sf.javabdd.*;

/**
 *
 * @author Sajed
 */
public class TCSupremicaBDDBitVector extends SupremicaBDDBitVector
{
    int signBitIndex;


    public TCSupremicaBDDBitVector(final BDDFactory factory, final int bitNum)
    {
        super(factory,bitNum);
        signBitIndex = bitNum-1;
    }

    public TCSupremicaBDDBitVector(final BDDFactory factory, final int bitNum, boolean b)
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

    protected TCSupremicaBDDBitVector buildSupBDDBitVector(int bitNum)
    {
        return new TCSupremicaBDDBitVector(mFactory, bitNum);
    }

    protected TCSupremicaBDDBitVector buildSupBDDBitVector(int bitNum, boolean val)
    {
        return new TCSupremicaBDDBitVector(mFactory, bitNum, val);
    }

    protected TCSupremicaBDDBitVector buildSupBDDBitVector(int bitNum, long val)
    {
        return new TCSupremicaBDDBitVector(mFactory, bitNum, val);
    }

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
        if (val<0)
        {
            final TCSupremicaBDDBitVector res = toTwosComplement();
            for (int n = 0; n < bitNum; n++)
                bitvec[n] = res.bitvec[n];
        }
    }

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
            for (int n = 0; n < bitNum; n++)
                bitvec[n] = res.bitvec[n];
        }
    }

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

        return (TCSupremicaBDDBitVector)res.add(buildSupBDDBitVector(bitNum, 1));
    }  

    public ResultOverflows addConsideringOverflows
      (final SupremicaBDDBitVector that)
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

    public ResultOverflows subConsideringOverflows(final SupremicaBDDBitVector that)
    {
      return addConsideringOverflows(((TCSupremicaBDDBitVector)that).toTwosComplement());
    }

    public TCSupremicaBDDBitVector add(final SupremicaBDDBitVector that)
    {
      return (TCSupremicaBDDBitVector)addConsideringOverflows(that).getResult();
    }

    public TCSupremicaBDDBitVector sub(final SupremicaBDDBitVector that)
    {
      return (TCSupremicaBDDBitVector)subConsideringOverflows(that).getResult();
    }

    protected BDD lthe(final SupremicaBDDBitVector that, BDD thanORequal)
    {
//        if (this.bitNum != r.bitNum)
//            throw new BDDException();
        
        BDD p = thanORequal;
        for (int n=0 ; n<getLargerLength(that) ; n++)
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
    public void div_rec(final SupremicaBDDBitVector divisor,
                               final SupremicaBDDBitVector remainder,
                               final SupremicaBDDBitVector result,
                               final int step)
    {
        final BDD isSmaller = divisor.lte(remainder);
        final TCSupremicaBDDBitVector newResult = (TCSupremicaBDDBitVector)result.shl(1, isSmaller);
        final TCSupremicaBDDBitVector zero = buildSupBDDBitVector(divisor.bitNum, false);
        final TCSupremicaBDDBitVector sub = buildSupBDDBitVector(divisor.bitNum, false);

        for (int n = 0; n < divisor.bitNum; n++)
            sub.bitvec[n] = isSmaller.ite(divisor.bitvec[n], zero.bitvec[n]);

        final TCSupremicaBDDBitVector tmp = (TCSupremicaBDDBitVector)remainder.add(sub.toTwosComplement());
        final TCSupremicaBDDBitVector newRemainder = (TCSupremicaBDDBitVector)tmp.shl(1, result.bitvec[divisor.bitNum - 1]);

        if (step > 1)
            div_rec(divisor, newRemainder, newResult, step - 1);
        
        tmp.free();
        sub.free();
        zero.free();
        isSmaller.free();

        result.replaceWith(newResult);
        remainder.replaceWith(newRemainder);
    }

}