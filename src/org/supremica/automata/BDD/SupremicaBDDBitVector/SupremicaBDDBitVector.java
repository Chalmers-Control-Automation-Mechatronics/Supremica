package org.supremica.automata.BDD.SupremicaBDDBitVector;

import java.math.BigInteger;
import java.util.Arrays;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import net.sf.javabdd.BDDException;
import net.sf.javabdd.BDDFactory;

/**
 *
 * @author Sajed
 */
public abstract class SupremicaBDDBitVector
{

    protected BDD[] bitvec;
    protected final BDDFactory mFactory;
    protected int bitNum;

    protected SupremicaBDDBitVector(final BDDFactory factory, final int bitNum)
    {
        mFactory = factory;
        bitvec = new BDD[bitNum];
        this.bitNum = bitNum;
    }

    protected BDDFactory getFactory()
    {
        return mFactory;
    }

    protected abstract SupremicaBDDBitVector buildSupBDDBitVector(int bitNum);

    protected abstract SupremicaBDDBitVector buildSupBDDBitVector(int bitNum, long c);

    protected abstract SupremicaBDDBitVector buildSupBDDBitVector(int bitNum, boolean c);

    protected void initialize(final boolean isTrue)
    {
        for (int n = 0; n < bitNum; n++)
            if (isTrue)
                bitvec[n] = mFactory.one();
            else
                bitvec[n] = mFactory.zero();
    }

    protected void initialize(final BDDDomain d)
    {
        initialize(d.vars());
    }

    protected void initialize(final int[] var)
    {
        for (int n=0 ; n<bitNum ; n++)
        {
            bitvec[bitNum-n-1] = mFactory.ithVar(var[n]);
        }
    }

    protected abstract void initialize(long val);

    protected abstract void initialize(BigInteger val);

    public SupremicaBDDBitVector copy()
    {
        final SupremicaBDDBitVector dst = buildSupBDDBitVector(bitNum);
        for (int n = 0; n < bitNum; n++)
            dst.bitvec[n] = bitvec[n].id();

        return dst;
    }

    public SupremicaBDDBitVector coerce(final int bitNum)
    {
        final SupremicaBDDBitVector dst = buildSupBDDBitVector(bitNum);
        final int minnum = Math.min(bitNum, this.bitNum);
        int n;
        for (n = 0; n < minnum; n++)
            dst.bitvec[n] = bitvec[n].id();

        for (; n < minnum; n++)
            dst.bitvec[n] = mFactory.zero();

        return dst;
    }

    public boolean isConst()
    {
        for (int n = 0; n < bitNum; n++)
        {
            final BDD b = bitvec[n];
            if (!b.isOne() && !b.isZero()) return false;
        }
        return true;
    }

    public abstract int val();

    public void free()
    {
        for (int n = 0; n < bitNum; n++) {
            bitvec[n].free();
        }
        bitvec = null;
    }

    public SupremicaBDDBitVector map2(final SupremicaBDDBitVector that, final BDDFactory.BDDOp op)
    {
//        if (bitNum != that.bitNum)
//          throw new BDDException();

        final SupremicaBDDBitVector res = buildSupBDDBitVector(getLargerLength(that));
        for (int n=0 ; n < res.length() ; n++)
        {
            BDD leftBDD = mFactory.zero();
            BDD rightBDD = mFactory.zero();

            if(n < this.length())
                leftBDD = bitvec[n];

            if(n < that.length())
                rightBDD = that.bitvec[n];

            res.bitvec[n] = leftBDD.apply(rightBDD, op);
        }
        return res;
    }

    public abstract BDD getBDDThatResultsMaxValue();

    public abstract ResultOverflows addConsideringOverflows(final SupremicaBDDBitVector that);

    public abstract ResultOverflows subConsideringOverflows(final SupremicaBDDBitVector that);

    public abstract SupremicaBDDBitVector add(final SupremicaBDDBitVector that);

    public abstract SupremicaBDDBitVector sub(final SupremicaBDDBitVector that);

    protected abstract BDD lthe(final SupremicaBDDBitVector r, BDD thanORequal);

    public abstract BDD equ(final SupremicaBDDBitVector r);

    public BDD lth(final SupremicaBDDBitVector r)
    {
      return lthe(r, mFactory.zero());
    }

    public BDD lte(final SupremicaBDDBitVector r)
    {
        return lthe(r, mFactory.one());
    }

    public BDD gth(final SupremicaBDDBitVector r)
    {
//        if (this.bitNum != r.bitNum)
//            throw new BDDException("gth operator: The length of the left-side vector is not equal to the right-side!");

        final BDD tmp = lte(r);
        final BDD p = tmp.not();
        return p;
    }

    public BDD gte(final SupremicaBDDBitVector r)
    {
//        if (this.bitNum != r.bitNum)
//            throw new BDDException("gte operator: The length of the left-side vector is not equal to the right-side!");

        final BDD tmp = lth(r);
        final BDD p = tmp.not();
        return p;
    }

    public BDD neq(final SupremicaBDDBitVector r)
    {
        final BDD tmp = equ(r);
        final BDD p = tmp.not();
        return p;
    }

    public abstract void div_rec(final SupremicaBDDBitVector divisor,
                               final SupremicaBDDBitVector remainder,
                               final SupremicaBDDBitVector result,
                               final int step);

    public void replaceWith(final SupremicaBDDBitVector that)
    {
        if (bitNum != that.bitNum)
            throw new BDDException("replaceWith operator: The length of the left-side vector is not equal to the right-side!");

        free();
        this.bitvec = that.bitvec;
        that.bitvec = null;
    }

    public SupremicaBDDBitVector shl(final int pos, final BDD c)
    {
        final int minnum = Math.min(bitNum, pos);
        if (minnum < 0)
            throw new BDDException();
        final SupremicaBDDBitVector res = buildSupBDDBitVector(bitNum);
        int n;
        for (n = 0; n < minnum; n++)
            res.bitvec[n] = c.id();

        for (n = minnum; n < bitNum; n++)
            res.bitvec[n] = bitvec[n - pos].id();

        return res;
    }

    public SupremicaBDDBitVector shr(final int pos, final BDD c)
    {
        final int maxnum = Math.max(0, bitNum - pos);
        if (maxnum < 0)
            throw new BDDException();
        final SupremicaBDDBitVector res = buildSupBDDBitVector(bitNum);
        int n;
        for (n=maxnum ; n<bitNum ; n++)
            res.bitvec[n] = c.id();

        for (n=0 ; n<maxnum ; n++)
            res.bitvec[n] = bitvec[n+pos].id();

        return res;
    }

    public SupremicaBDDBitVector divmod(final long c, final boolean which)
    {
        if (c <= 0L)
            throw new BDDException();
        final SupremicaBDDBitVector divisor = buildSupBDDBitVector(bitNum, c);
        final SupremicaBDDBitVector tmp = buildSupBDDBitVector(bitNum, false);
        final SupremicaBDDBitVector tmpremainder = tmp.shl(1, bitvec[bitNum-1]);
        final SupremicaBDDBitVector result = this.shl(1, mFactory.zero());
        SupremicaBDDBitVector remainder;
        div_rec(divisor, tmpremainder, result, divisor.bitNum);
        remainder = tmpremainder.shr(1, mFactory.zero());
        tmp.free();
        tmpremainder.free();
        divisor.free();
        if (which) {
            remainder.free();
            return result;
        } else {
            result.free();
            return remainder;
        }
    }

    //Builds a boolean vector for multiplication with a constant
    public SupremicaBDDBitVector mulfixed(final int c)
    {
        SupremicaBDDBitVector res, next, rest;
        int n;
        if (bitNum == 0)
            throw new BDDException();
        if (c == 0)
            return buildSupBDDBitVector(bitNum, false);

        next = buildSupBDDBitVector(bitNum, false);

        for (n=1 ; n<bitNum ; n++)
            next.bitvec[n] = bitvec[n-1];

        rest = next.mulfixed(c>>1);
        if ((c & 0x1) != 0)
        {
            res = add(rest);
            rest.free();
        }
        else
            res = rest;
        //        next.free();
        return res;
    }

    //Builds a boolean vector for multiplication with a vector
    public SupremicaBDDBitVector mul(final SupremicaBDDBitVector right)
    {
        int n;
        final int localBitNum = this.bitNum + right.length();
        SupremicaBDDBitVector res,leftshifttmp, leftshift;

        if (localBitNum == 0  ||  right.length() == 0)
            throw new BDDException();

        res = buildSupBDDBitVector(localBitNum, false);
        leftshifttmp = copy();
        leftshift = leftshifttmp.coerce(localBitNum);
        //bvec_delref(leftshifttmp);
        leftshifttmp.free();
        for (n=0 ; n<right.length() ; n++)
        {
            final SupremicaBDDBitVector added = res.add(leftshift);
            int m;
            for (m=0 ; m<localBitNum ; m++)
                res.bitvec[m] = right.bitvec[n].ite(added.bitvec[m], res.bitvec[m]);

            // Shift 'leftshift' one bit left
            for (m=localBitNum-1 ; m>=1 ; m--)
            leftshift.bitvec[m] = leftshift.bitvec[m-1];
            leftshift.bitvec[0] = mFactory.zero();
            //bvec_delref(added);
            added.free();
        }
        //bvec_delref(leftshift);
        leftshift.free();
        return res;
    }

    public int length()
    {
        return bitNum;
    }

    public void setBit(final int i, final BDD bdd)
    {
	bitvec[i]=bdd.id();
    }

    public BDD getBit(final int n)
    {
        return bitvec[n];
    }

    protected int getLargerLength(final SupremicaBDDBitVector that)
    {
        if(that.length() > this.length())
        {
            return that.length();
        }

        return this.length();
    }

    @Override
    public String toString()
    {
        if (isConst()) {
            return String.format("%s", val());
        } else {
            return Arrays.toString(bitvec);
        }
    }

}
