package org.supremica.automata.BDD.SupremicaBDDBitVector;

import java.math.BigInteger;
import net.sf.javabdd.*;

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

    public PSupremicaBDDBitVector(final BDDFactory factory, final int bitNum, boolean b)
    {
        this(factory, bitNum);
        initialize(b);
    }

    public PSupremicaBDDBitVector(final BDDFactory factory, final int bitNum, final long val)
    {
        this(factory, bitNum);
        initialize(val);
    }

    public PSupremicaBDDBitVector(final BDDFactory factory,final BDDDomain d)
    {
      this(factory, d.varNum());
      initialize(d);
    }

    protected PSupremicaBDDBitVector buildSupBDDBitVector(int bitNum)
    {
        return new PSupremicaBDDBitVector(mFactory, bitNum);
    }

    protected PSupremicaBDDBitVector buildSupBDDBitVector(int bitNum, boolean val)
    {
        return new PSupremicaBDDBitVector(mFactory, bitNum, val);
    }

    protected PSupremicaBDDBitVector buildSupBDDBitVector(int bitNum, long val)
    {
        return new PSupremicaBDDBitVector(mFactory, bitNum, val);
    }

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

    public int val()
    {
        int n, val = 0;

        for (n = bitvec.length - 1; n >= 0; n--)
            if (bitvec[n].isOne())
                val = (val << 1) | 1;
            else if (bitvec[n].isZero())
                val = val << 1;
            else
                return 0;

        return val;
    }


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

            BDD newBDD = bit.and(bitvec[i]);
            if(!newBDD.isZero())
                bit = newBDD.id();
        }


        return bit;

    }

    public BDD equ(final SupremicaBDDBitVector r)
    {
//        if (this.bitNum != r.bitNum)
//            throw new BDDException("equ operator: The length of the left-side vector is not equal to the right-side!");

        BDD p = mFactory.one();
        for (int n=0 ; n< getLargerLength(r); n++)
        {

            BDD leftBDD = mFactory.zero();
            BDD rightBDD = mFactory.zero();

            if(n < this.bitNum)
                leftBDD = bitvec[n];

            if(n < r.bitNum)
                rightBDD = r.bitvec[n];

            final BDD tmp1 = leftBDD.apply(rightBDD, BDDFactory.biimp);
            final BDD tmp2 = tmp1.and(p);
            p = tmp2;
        }
        return p;
    }

    public ResultOverflows addConsideringOverflows(final SupremicaBDDBitVector that)
    {
//        if (bitvec.length != that.bitvec.length)
//            throw new BDDException("add operator: The length of the left-side vector is not equal to the right-side!");

        BDD c = mFactory.zero();
        PSupremicaBDDBitVector res = buildSupBDDBitVector(getLargerLength(that));

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

        BDD overflow = c.id();

        c.free();

        return new ResultOverflows(res,overflow);
    }

    public PSupremicaBDDBitVector addRemoveOverflows(final SupremicaBDDBitVector that)
    {
        ResultOverflows resOvfls = addConsideringOverflows(that);
        PSupremicaBDDBitVector result = (PSupremicaBDDBitVector)resOvfls.getResult();
        for(int i = 0; i < result.bitNum; i++)
        {
            result.bitvec[i] = result.bitvec[i].and(resOvfls.getOverflows().not());
        }

        return result;
    }

    public ResultOverflows subConsideringOverflows(final SupremicaBDDBitVector that)
    {
//       if (bitvec.length != that.bitvec.length)
//            throw new BDDException("sub operator: The length of the left-side vector is not equal to the right-side!");

        BDDFactory bdd = getFactory();

        BDD c = bdd.zero();
        PSupremicaBDDBitVector res = buildSupBDDBitVector(getLargerLength(that));

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
            BDD tmp2 = leftBDD.apply(tmp1, BDDFactory.less);
            tmp1.free();
            tmp1 = leftBDD.and(rightBDD);
            tmp1.andWith(c);
            tmp1.orWith(tmp2);
            c = tmp1;
        }

        BDD overflow = c.id();

        c.free();

        return new ResultOverflows(res,overflow);
        //return new ResultOverflows(res,highOrderCarryIn.xor(highOrderCarryOut));
    }

    public PSupremicaBDDBitVector add(final SupremicaBDDBitVector that)
    {
      return (PSupremicaBDDBitVector)addConsideringOverflows(that).getResult();
    }

    public PSupremicaBDDBitVector sub(final SupremicaBDDBitVector that)
    {
      return (PSupremicaBDDBitVector)subConsideringOverflows(that).getResult();
    }

    protected BDD lthe(final SupremicaBDDBitVector r, BDD thanORequal)
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

            BDD tmp1 = leftBDD.apply(rightBDD, BDDFactory.less);
            BDD tmp2 = leftBDD.apply(rightBDD, BDDFactory.biimp);
            tmp2.andWith(p);
            tmp1.orWith(tmp2);
            p = tmp1;
        }

        return p;
    }

    //This function needs to be modified
    public void div_rec(final SupremicaBDDBitVector divisor,
                               final SupremicaBDDBitVector remainder,
                               final SupremicaBDDBitVector result,
                               final int step)
    {
        BDD isSmaller = divisor.lte(remainder);
        PSupremicaBDDBitVector newResult = (PSupremicaBDDBitVector)result.shl(1, isSmaller);
        PSupremicaBDDBitVector zero = buildSupBDDBitVector(divisor.bitvec.length, false);
        PSupremicaBDDBitVector sub = buildSupBDDBitVector(divisor.bitvec.length, false);

        for (int n = 0; n < divisor.bitvec.length; n++)
            sub.bitvec[n] = isSmaller.ite(divisor.bitvec[n], zero.bitvec[n]);

        PSupremicaBDDBitVector tmp = (PSupremicaBDDBitVector)remainder.sub(sub);
        PSupremicaBDDBitVector newRemainder = (PSupremicaBDDBitVector)tmp.shl(1, result.bitvec[divisor.bitvec.length - 1]);

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
