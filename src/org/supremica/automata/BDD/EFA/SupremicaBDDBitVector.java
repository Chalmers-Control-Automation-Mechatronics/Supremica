// SupremicaBDDBitVector.java, created Jul 14, 2003 9:50:57 PM by jwhaley
// Copyright (C) 2003 John Whaley
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package org.supremica.automata.BDD.EFA;

import java.math.BigInteger;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import net.sf.javabdd.BDDException;
import net.sf.javabdd.BDDFactory;

/**
 * <p>Bit vector implementation for BDDs.</p>
 *
 * @author John Whaley
 * @version $Id$
 */

public class SupremicaBDDBitVector {

    private BDD[] bitvec;
    private final int signBitIndex;
    private final BDDFactory mFactory;


    public SupremicaBDDBitVector(final BDDFactory factory,
                                 final int bitnum)
    {
      mFactory = factory;
      signBitIndex = bitnum-1;
      bitvec = new BDD[bitnum];
    }

    public SupremicaBDDBitVector(final BDDFactory factory,
                                 final int bitnum,
                                 final boolean b)
    {
      mFactory = factory;
      signBitIndex = bitnum-1;
      bitvec = new BDD[bitnum];
      initialize(b);
    }

    public SupremicaBDDBitVector(final BDDFactory factory,
                                 final int bitnum,
                                 final long val)
    {
      mFactory = factory;
      signBitIndex = bitnum-1;
      bitvec = new BDD[bitnum];
      initialize(val);
    }

    public SupremicaBDDBitVector(final BDDFactory factory,
                                 final int bitnum,
                                 final BigInteger val)
    {
      mFactory = factory;
      signBitIndex = bitnum-1;
      bitvec = new BDD[bitnum];
      initialize(val);
    }

    public SupremicaBDDBitVector(final BDDFactory factory,
                                 final BDDDomain d)
    {
      final int bitnum = d.varNum();
      mFactory = factory;
      signBitIndex = bitnum-1;
      bitvec = new BDD[bitnum];
      initialize(d);
    }


    private void initialize(final boolean isTrue)
    {
      for (int n = 0; n < bitvec.length; n++)
        if (isTrue)
          bitvec[n] = mFactory.one();
        else
          bitvec[n] = mFactory.zero();
    }

    @SuppressWarnings("unused")
    private void initialize(final int val)
    {
      int absVal = Math.abs(val);
      for (int n = 0; n < bitvec.length; n++) {
        if ((absVal & 0x1) != 0)
          bitvec[n] = mFactory.one();
        else
          bitvec[n] = mFactory.zero();
        absVal >>= 1;
      }
      if(val<0)
      {
        final SupremicaBDDBitVector res = toTwosComplement();
        for (int n = 0; n < bitvec.length; n++)
          bitvec[n] = res.bitvec[n];
      }
    }

    private void initialize(final long val)
    {
      long absVal = Math.abs(val);
      for (int n = 0; n < bitvec.length; n++) {
        if ((absVal & 0x1) != 0)
          bitvec[n] = mFactory.one();
        else
          bitvec[n] = mFactory.zero();
        absVal >>= 1;
      }
      if (val<0) {
        final SupremicaBDDBitVector res = toTwosComplement();
        for (int n = 0; n < bitvec.length; n++)
          bitvec[n] = res.bitvec[n];
      }
    }

    private void initialize(final BigInteger val)
    {
      BigInteger absVal = val.abs();
      for (int n = 0; n < bitvec.length; n++) {
        if (absVal.testBit(0))
          bitvec[n] = mFactory.one();
        else
          bitvec[n] = mFactory.zero();
        absVal = absVal.shiftRight(1);
      }
      if (val.intValue()<0) {
        final SupremicaBDDBitVector res = toTwosComplement();
        for (int n = 0; n < bitvec.length; n++)
          bitvec[n] = res.bitvec[n];
      }
    }

    @SuppressWarnings("unused")
    private void initialize(final int offset, final int step)
    {
      for (int n=0 ; n<bitvec.length ; n++)
        bitvec[n] = mFactory.ithVar(offset+n*step);
    }

    private void initialize(final BDDDomain d)
    {
      initialize(d.vars());
    }

    private void initialize(final int[] var)
    {
      for (int n=0 ; n<bitvec.length ; n++)
        bitvec[n] = mFactory.ithVar(var[n]);
    }

    public SupremicaBDDBitVector copy()
    {
      final SupremicaBDDBitVector dst =
        new SupremicaBDDBitVector(mFactory, bitvec.length);
      for (int n = 0; n < bitvec.length; n++)
        dst.bitvec[n] = bitvec[n].id();
      return dst;
    }

    public SupremicaBDDBitVector coerce(final int bitnum)
    {
      final SupremicaBDDBitVector dst =
        new SupremicaBDDBitVector(mFactory, bitnum);
      final int minnum = Math.min(bitnum, bitvec.length);
      int n;
      for (n = 0; n < minnum; n++)
        dst.bitvec[n] = bitvec[n].id();
      for (; n < minnum; n++)
        dst.bitvec[n] = mFactory.zero();
      return dst;
    }

    public boolean isConst()
    {
      for (int n = 0; n < bitvec.length; n++) {
        final BDD b = bitvec[n];
        if (!b.isOne() && !b.isZero()) return false;
      }
      return true;
    }

    public int val()
    {
      int n, val = 0;
      boolean negative = false;
      SupremicaBDDBitVector finalBitVec =
        new SupremicaBDDBitVector(mFactory, bitvec.length);
      if(bitvec[signBitIndex].isOne())
      {
        negative = true;
        finalBitVec = toTwosComplement();
      }
      else
        finalBitVec = copy();
      for (n = finalBitVec.bitvec.length - 1; n >= 0; n--)
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

    public void free() {
        for (int n = 0; n < bitvec.length; n++) {
            bitvec[n].free();
        }
        bitvec = null;
    }

    public SupremicaBDDBitVector map2(final SupremicaBDDBitVector that,
                                      final BDDFactory.BDDOp op) {
        if (bitvec.length != that.bitvec.length)
          throw new BDDException();
        final SupremicaBDDBitVector res =
          new SupremicaBDDBitVector(mFactory, bitvec.length);
        for (int n=0 ; n < bitvec.length ; n++)
          res.bitvec[n] = bitvec[n].apply(that.bitvec[n], op);
        return res;
    }

    public SupremicaBDDBitVector toTwosComplement()
    {
      final SupremicaBDDBitVector res =
        new SupremicaBDDBitVector(mFactory, bitvec.length);
      for (int n = 0; n < res.bitvec.length; n++) {
        res.bitvec[n] = bitvec[n].not();
      }
      return res.add(new SupremicaBDDBitVector(mFactory, bitvec.length, 1));
    }

    public SupremicaBDDBitVector add(final SupremicaBDDBitVector that)
    {
      return addConsideringOverflows(that).getResult();
    }

    public ResultOverflows addConsideringOverflows
      (final SupremicaBDDBitVector that)
    {
      if (bitvec.length != that.bitvec.length)
        throw new BDDException();
      BDD c = mFactory.zero();
      BDD highOrderCarryIn = mFactory.zero();
      BDD highOrderCarryOut = mFactory.zero();
      final SupremicaBDDBitVector res =
        new SupremicaBDDBitVector(mFactory, bitvec.length);

      for (int n = 0; n < res.bitvec.length; n++) {
        /* bitvec[n] = l[n] ^ r[n] ^ c; */
        res.bitvec[n] = bitvec[n].xor(that.bitvec[n]);
        res.bitvec[n].xorWith(c.id());

        /* c = (l[n] & r[n]) | (c & (l[n] | r[n])); */
        final BDD tmp1 = bitvec[n].or(that.bitvec[n]);
        tmp1.andWith(c);
        final BDD tmp2 = bitvec[n].and(that.bitvec[n]);
        tmp2.orWith(tmp1);
        c = tmp2;
        if(n == signBitIndex-1)
        {
          highOrderCarryIn = c.id();
        }
        if(n == signBitIndex)
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
      return addConsideringOverflows(that.toTwosComplement());
    }

    public SupremicaBDDBitVector sub(final SupremicaBDDBitVector that)
    {
      return subConsideringOverflows(that).getResult();
    }

/*
    public SupremicaBDDBitVector sub(SupremicaBDDBitVector that) {

        if (bitvec.length != that.bitvec.length)
            throw new BDDException();

        BDDFactory bdd = getFactory();

        BDD c = bdd.zero();
        SupremicaBDDBitVector res = bdd.createBitVector(bitvec.length);

        for (int n = 0; n < res.bitvec.length; n++) {
            // bitvec[n] = l[n] ^ r[n] ^ c;
            res.bitvec[n] = bitvec[n].xor(that.bitvec[n]);
            res.bitvec[n].xorWith(c.id());

            // c = (l[n] & r[n] & c) | (!l[n] & (r[n] | c));
            BDD tmp1 = that.bitvec[n].or(c);
            BDD tmp2 = this.bitvec[n].apply(tmp1, BDDFactory.less);
            tmp1.free();
            tmp1 = this.bitvec[n].and(that.bitvec[n]);
            tmp1.andWith(c);
            tmp1.orWith(tmp2);

            c = tmp1;
        }
        c.free();

        return res;
    }
*/

    public BDD lte(final SupremicaBDDBitVector r)
    {
      if (this.bitvec.length != r.bitvec.length)
        throw new BDDException();
      BDD p = mFactory.one();
      for (int n=0 ; n<bitvec.length ; n++) {
        final BDD tmp1 = bitvec[n].apply(r.bitvec[n], BDDFactory.less);
        final BDD tmp2 = bitvec[n].apply(r.bitvec[n], BDDFactory.biimp);
        tmp2.andWith(p);
        tmp1.orWith(tmp2);
        p = tmp1;
      }
      final BDD SBILBDD = bitvec[signBitIndex];
      final BDD SBIRBDD = r.bitvec[signBitIndex];
      return (SBILBDD.and(SBIRBDD.not())).or((SBILBDD.or(SBIRBDD.not())).and(p));
    }

    public BDD lth(final SupremicaBDDBitVector r)
    {
      if (this.bitvec.length != r.bitvec.length)
        throw new BDDException();
      BDD p = mFactory.zero();
      for (int n=0 ; n<bitvec.length; n++) {
        final BDD tmp1 = bitvec[n].apply(r.bitvec[n], BDDFactory.less);
        final BDD tmp2 = bitvec[n].apply(r.bitvec[n], BDDFactory.biimp);
        tmp2.andWith(p);
        tmp1.orWith(tmp2);
        p = tmp1;
      }
      final BDD SBILBDD = bitvec[signBitIndex];
      final BDD SBIRBDD = r.bitvec[signBitIndex];
      return (SBILBDD.and(SBIRBDD.not())).or((SBILBDD.or(SBIRBDD.not())).and(p));
    }

    public BDD gth(final SupremicaBDDBitVector r)
    {
      final BDD tmp = lte(r);
      final BDD p = tmp.not();
      return p;
    }

    public BDD gte(final SupremicaBDDBitVector r)
    {
      final BDD tmp = lth(r);
      final BDD p = tmp.not();
      return p;
    }

    public BDD equ(final SupremicaBDDBitVector r)
    {
      if (this.bitvec.length != r.bitvec.length)
        throw new BDDException();
      BDD p = mFactory.one();
      for (int n=0 ; n<bitvec.length ; n++) {
        final BDD tmp1 = bitvec[n].apply(r.bitvec[n], BDDFactory.biimp);
        final BDD tmp2 = tmp1.and(p);
        p = tmp2;
      }
      return p;
    }

    public BDD neq(final SupremicaBDDBitVector r)
    {
      final BDD tmp = equ(r);
      final BDD p = tmp.not();
      return p;
    }

    public static void div_rec(final SupremicaBDDBitVector divisor,
                               final SupremicaBDDBitVector remainder,
                               final SupremicaBDDBitVector result,
                               final int step)
    {
      final BDD isSmaller = divisor.lte(remainder);
      final SupremicaBDDBitVector newResult = result.shl(1, isSmaller);
      final BDDFactory factory = divisor.mFactory;
      final SupremicaBDDBitVector zero =
        new SupremicaBDDBitVector(factory, divisor.bitvec.length, false);
      final SupremicaBDDBitVector sub =
        new SupremicaBDDBitVector(factory, divisor.bitvec.length, false);
      for (int n = 0; n < divisor.bitvec.length; n++)
        sub.bitvec[n] = isSmaller.ite(divisor.bitvec[n], zero.bitvec[n]);
      final SupremicaBDDBitVector tmp = remainder.add(sub.toTwosComplement());
      final SupremicaBDDBitVector newRemainder =
        tmp.shl(1, result.bitvec[divisor.bitvec.length - 1]);
      if (step > 1)
        div_rec(divisor, newRemainder, newResult, step - 1);
      tmp.free();
      sub.free();
      zero.free();
      isSmaller.free();
      result.replaceWith(newResult);
      remainder.replaceWith(newRemainder);
    }

    public void replaceWith(final SupremicaBDDBitVector that)
    {
      if (bitvec.length != that.bitvec.length)
        throw new BDDException();
      free();
      this.bitvec = that.bitvec;
      that.bitvec = null;
    }

    public SupremicaBDDBitVector shl(final int pos, final BDD c)
    {
      final int minnum = Math.min(bitvec.length, pos);
      if (minnum < 0)
        throw new BDDException();
      final SupremicaBDDBitVector res =
        new SupremicaBDDBitVector(mFactory, bitvec.length);
      int n;
      for (n = 0; n < minnum; n++)
        res.bitvec[n] = c.id();
      for (n = minnum; n < bitvec.length; n++)
        res.bitvec[n] = bitvec[n - pos].id();
      return res;
    }

    public SupremicaBDDBitVector shr(final int pos, final BDD c)
    {
      final int maxnum = Math.max(0, bitvec.length - pos);
      if (maxnum < 0)
        throw new BDDException();
      final SupremicaBDDBitVector res =
        new SupremicaBDDBitVector(mFactory, bitvec.length);
      int n;
      for (n=maxnum ; n<bitvec.length ; n++)
        res.bitvec[n] = c.id();
      for (n=0 ; n<maxnum ; n++)
        res.bitvec[n] = bitvec[n+pos].id();
      return res;
    }

    public SupremicaBDDBitVector divmod(final long c, final boolean which)
    {
      if (c <= 0L)
        throw new BDDException();
      final SupremicaBDDBitVector divisor =
        new SupremicaBDDBitVector(mFactory, bitvec.length, c);
      final SupremicaBDDBitVector tmp =
        new SupremicaBDDBitVector(mFactory, bitvec.length, false);
      final SupremicaBDDBitVector tmpremainder = tmp.shl(1, bitvec[bitvec.length-1]);
      final SupremicaBDDBitVector result = this.shl(1, mFactory.zero());
      SupremicaBDDBitVector remainder;
      div_rec(divisor, tmpremainder, result, divisor.bitvec.length);
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

    //////////////////////////////////

    //Builds a boolean vector for multiplication with a constant
    public SupremicaBDDBitVector mulfixed(final int c)
    {
      SupremicaBDDBitVector res, next, rest;
      int n;
      if (bitvec.length == 0)
        throw new BDDException();
      if (c == 0)
        return new SupremicaBDDBitVector(mFactory, bitvec.length, false);
      next = new SupremicaBDDBitVector(mFactory, bitvec.length, false);
      for (n=1 ; n<bitvec.length ; n++)
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
      final int bitnum = bitvec.length + right.size();
      SupremicaBDDBitVector res;
      SupremicaBDDBitVector leftshifttmp;
      SupremicaBDDBitVector leftshift;
      if (bitvec.length == 0  ||  right.size() == 0)
      {
        throw new BDDException();
      }
      res = new SupremicaBDDBitVector(mFactory, bitnum, false);
      leftshifttmp = copy();
      leftshift = leftshifttmp.coerce(bitnum);
      /*bvec_delref(leftshifttmp);*/
      leftshifttmp.free();
      for (n=0 ; n<right.size() ; n++)
      {
        final SupremicaBDDBitVector added = res.add(leftshift);
        int m;
        for (m=0 ; m<bitnum ; m++)
        {
          res.bitvec[m] = right.bitvec[n].ite(added.bitvec[m], res.bitvec[m]);
        }
        /* Shift 'leftshift' one bit left */
        for (m=bitnum-1 ; m>=1 ; m--)
          leftshift.bitvec[m] = leftshift.bitvec[m-1];
        leftshift.bitvec[0] = mFactory.zero();
        /*bvec_delref(added);*/
        added.free();
      }
      /*bvec_delref(leftshift);*/
      leftshift.free();
      return res;
    }


    ////////////////


    public int size() {
        return bitvec.length;
    }

    public void setBit(final int i, final BDD bdd)
    {
	bitvec[i]=bdd.id();
    }

    public BDD getBit(final int n) {
        return bitvec[n];
    }


    static class ResultOverflows
    {
      SupremicaBDDBitVector result;
      BDD overflows;

      public ResultOverflows(final SupremicaBDDBitVector result, final BDD overflows)
      {
        this.result = result;
        this.overflows = overflows;
      }

      public SupremicaBDDBitVector getResult()
      {
        return result;
      }

      public BDD getOverflows()
      {
        return overflows;
      }
    }
}
