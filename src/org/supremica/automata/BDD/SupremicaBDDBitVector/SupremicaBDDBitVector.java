package org.supremica.automata.BDD.SupremicaBDDBitVector;

import java.math.BigInteger;
import java.util.LinkedList;

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

    public static final BitVectorOp equ = new EquOp();
    public static final BitVectorOp neq = new NeqOp();
    public static final BitVectorOp lth = new LthOp();
    public static final BitVectorOp lte = new LteOp();
    public static final BitVectorOp gth = new GthOp();
    public static final BitVectorOp gte = new GteOp();

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

    /**
     * Build a BDD bit vector. Calls the constructor of the relevant subclass.
     *
     * @param bitNum Number of bits in the vector
     * @return A non-initialized bit vector with specified number of bits.
     */
    protected abstract SupremicaBDDBitVector buildSupBDDBitVector(int bitNum);

    /**
     * Build a BDD bit vector. Calls the constructor of the relevant subclass.
     *
     * @param bitNum Number of bits in the vector
     * @param c The value to be represented by the bit vector
     * @return A bit vector initialized to represent the value of integer 'c'
     */
    protected abstract SupremicaBDDBitVector buildSupBDDBitVector(int bitNum, long c);

    /**
     * Build a BDD bit vector. Calls the constructor of the relevant subclass.
     *
     * @param bitNum Number of bits in the vector
     * @param c The boolean value to assign to all bits
     * @return A bit vector initialized with all bits set to the value of 'c'
     */
    protected abstract SupremicaBDDBitVector buildSupBDDBitVector(int bitNum, boolean c);

    /**
     * Initialize all bits to either one or zero.
     * @param isTrue true for ones, false for zeros.
     */
    protected void initialize(final boolean isTrue)
    {
        for (int n = 0; n < bitNum; n++)
            if (isTrue)
                bitvec[n] = mFactory.one();
            else
                bitvec[n] = mFactory.zero();
    }

    /**
     * Initialize the bits to the corresponding variable in the domain.
     * See {@link SupremicaBDDBitVector#initialize(int[])}.
     * @param d a BDD domain.
     */
    protected void initialize(final BDDDomain d)
    {
        initialize(d.vars());
    }

    /**
     * Initializes each bit to the ith variable in the current BDD factory.
     * @param var an array with BDD variable indices
     */
    protected void initialize(final int[] var)
    {
        if (var.length > bitNum) {
            throw new IllegalArgumentException(String.format("Trying to set %d variables, but this bit vector only has %d.", var.length, bitNum));
        }

        for (int n=0 ; n<bitNum ; n++)
        {
            if (n < var.length) {
                bitvec[n] = mFactory.ithVar(var[var.length-n-1]);
            } else {
                bitvec[n] = mFactory.zero();
            }
        }
    }

    /**
     * Sets the bit vector to represent the specified value.
     * @param val a constant that the bit vector shall represent.
     */
    protected abstract void initialize(long val);

    /**
     * Same as {@link SupremicaBDDBitVector#initialize(long)}
     */
    protected abstract void initialize(BigInteger val);

    /**
     * Create a deep copy of the bit vector.
     * @return a copy.
     */
    public SupremicaBDDBitVector copy()
    {
        final SupremicaBDDBitVector dst = buildSupBDDBitVector(bitNum);
        for (int n = 0; n < bitNum; n++)
            dst.bitvec[n] = bitvec[n].id();

        return dst;
    }

    /**
     * Copy this bit vector into a new one with specified size. The result is
     * truncated.
     * @param bitNum bit size of new bit vector.
     * @return a bit vector with given size.
     */
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

    /**
     * Resize the bit vector and take implementation details into account.
     * This method should try to return a meaningful bit vector.
     * @param bitNum bit size of new bit vector.
     * @return a bit vector with given size.
     */
    abstract public SupremicaBDDBitVector resize(int bitNum);

    /**
     * Checks whether the bit vector represents a fixed integer. A bit vector
     * can represent operations on variables and relations between variables, in
     * which case this method returns false.
     *
     * @return True if the bit vector represents a fixed integer.
     */
    public boolean isConst()
    {
        for (int n = 0; n < bitNum; n++)
        {
            final BDD b = bitvec[n];
            /*
             * A bit vector representing a fixed integer has each of its bits
             * set to True or False. The equivalent for BDDs are One and Zero.
             */
            if (!b.isOne() && !b.isZero()) return false;
        }
        return true;
    }

    /**
     * @return The integer representation of the bit vector. If the bit vector
     * does not represent exactly one integer, 0 is returned.
     */
    public abstract int val();

    /**
     * Free all the BDDs that are used in this bit vector.
     */
    public void free()
    {
        for (int n = 0; n < bitNum; n++) {
            bitvec[n].free();
        }
        bitvec = null;
    }

    /**
     * Apply a binary operator on two bit vectors.
     * @param that right hand side of operation.
     * @param op binary operator.
     * @return it vector representing the result of the opertion.
     */
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

    public BDD apply(final SupremicaBDDBitVector right, final BitVectorOp op) {
      return op.apply(this, right);
    }

    public abstract BDD getBDDThatResultsMaxValue();

    public abstract ResultOverflows addConsideringOverflows(final SupremicaBDDBitVector that);

    public abstract ResultOverflows subConsideringOverflows(final SupremicaBDDBitVector that);

    public abstract SupremicaBDDBitVector add(final SupremicaBDDBitVector that);

    public abstract SupremicaBDDBitVector sub(final SupremicaBDDBitVector that);

    /**
     * Less than (or equal).
     *
     * @param r Right hand side of comparison
     * @param thanORequal With equality? BDD.zero() for no, BDD.one() for yes.
     * @return A BDD representing the result of the comparison.
     */
    protected abstract BDD lthe(final SupremicaBDDBitVector r, BDD thanORequal);

    public abstract BDD equ(final SupremicaBDDBitVector r);

    /**
     * Less than (strict) comparison of two bit vectors.
     *
     * @param r Right hand side of comparison
     * @return A BDD representing the result of the comparison.
     */
    public BDD lth(final SupremicaBDDBitVector r)
    {
      return lthe(r, mFactory.zero());
    }

    /**
     * Less than or equal comparison of two bit vectors.
     *
     * @param r Right hand side of comparison
     * @return A BDD representing the result of the comparison.
     */
    public BDD lte(final SupremicaBDDBitVector r)
    {
        return lthe(r, mFactory.one());
    }

    /**
     * Greater than (strict) comparison of two bit vectors.
     *
     * @param r Right hand side of comparison
     * @return A BDD representing the result of the comparison.
     */
    public BDD gth(final SupremicaBDDBitVector r)
    {
//        if (this.bitNum != r.bitNum)
//            throw new BDDException("gth operator: The length of the left-side vector is not equal to the right-side!");

        final BDD tmp = lte(r);
        final BDD p = tmp.not();
        return p;
    }

    /**
     * Greater than or equal comparison of two bit vectors.
     *
     * @param r Right hand side of comparison
     * @return A BDD representing the result of the comparison.
     */
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

    /**
     * Long integer division of two bit vectors. Both quotient and remainder are
     * calculated. The calculation is performed recursively with one bit handled
     * in each recursion step.
     * <p>
     * https://en.wikipedia.org/wiki/Division_algorithm#Integer_division_(unsigned)_with_remainder
     *
     * @param divisor Bit vector representing the divisor in integer division
     * @param remainder The current remainder. Should be all zero bits for the
     * initial call of this algorithm. The resulting remainder after the
     * algorithm finishes is stored in this variable.
     * @param result The current quotient. Should be all zero bits for the
     * initial call of this algorithm. The resulting quotient after the
     * algorithm finishes is stored in this variable.
     * @param step The current bit. Should start with the msb.
     */
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

    /**
     * Shift left. Shifts the bits in this bit vector to the left (toward MSB).
     *
     * @param pos The number of positions to shift
     * @param c The BDD shifted in from the right
     * @return A copy of the bit vector with all bits shifted to the left.
     */
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

    /**
     * Shift right. Shifts the bits in this bit vector to the right (toward LSB).
     *
     * @param pos The number of positions to shift
     * @param c The BDD shifted in from the left
     * @return A copy of the bit vector with all bits shifted to the right.
     */
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

    /**
     * Integer division or modulo of the bit vector and a constant.
     *
     * @param c Divisor
     * @param which True for integer division, False for integer modulo.
     * @return Quotient or remainder from integer division, depending on value
     * of 'which' parameter.
     */
    public SupremicaBDDBitVector divmod(final long c, final boolean which)
    {
        if (c <= 0L)
            throw new BDDException();
        // Preallocate bit vectors used for the algorithm
        final SupremicaBDDBitVector divisor = buildSupBDDBitVector(bitNum, c);
        final SupremicaBDDBitVector remainder = buildSupBDDBitVector(bitNum, false);
        final SupremicaBDDBitVector result = buildSupBDDBitVector(bitNum, false);
        // Perform long division recursively
        div_rec(divisor, remainder, result, divisor.bitNum-1);
        // Clean up, and return quotient or remainder depending on choice.
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

        for (int i = 0; i<bitNum; i++) {
            if (bitvec[i] == null) {
                return "null";
            }
        }

        if (isConst()) {
            return String.format("%s", val());
        } else {

            // The bit vector is not constant so we want to figure out the set
            // of all values it can take on.
            final LinkedList<Integer> satSet = new LinkedList<Integer>();
            // The mask start at zero and is incremented in every loop. When the
            // increment overflows we have looped through all possible bit
            // assignments. In every iteration we compare the mask with this bit
            // vector. If the result can be satisfied, then we know that the
            // current value of the mask can be represented by this bit vector,
            // and we add that integer value to the set.
            final int min = this.min();
            final int max = this.max();
            if (max-min > 20) {
                return String.format("[min=%d, max=%d]", min, max);
            } else {
                final SupremicaBDDBitVector mask = buildSupBDDBitVector(bitNum, min);
                BDD overflow = mFactory.zero();
                while (mask.val() <= max && overflow.isZero()) {
                    final BDD equality = this.equ(mask);
                    if (equality.satCount() > 0) {
                        satSet.add(mask.val());
                    }
                    overflow = mask.increment();
                }

                return satSet.toString();
            }
        }
    }


    /**
     * Increments this bit vector with 1.
     * @return The carry bit of the whole operation. This is set to one when the
     * operation overflows.
     */
    abstract public BDD increment();

    /**
     * Sets a max and min value of the bit vector.
     * @return a new bit vector with the given bounds
     */
    public SupremicaBDDBitVector saturate(final int min, final int max) {
        final SupremicaBDDBitVector res = this.copy();
        final SupremicaBDDBitVector maxVec = res.max(min);
        final SupremicaBDDBitVector minVec = maxVec.min(max);
        return minVec;
    }

    /**
     * Sets a minimum value for the bit vector.
     * @return a new bit vector with the given lower bound
     */
    private SupremicaBDDBitVector min(final int val)
    {
        final SupremicaBDDBitVector constant = buildSupBDDBitVector(bitNum, val);
        final BDD less = this.lth(constant);
        final SupremicaBDDBitVector res = this.copy();
        for (int i=0; i<res.bitNum; i++) {
            res.bitvec[i] = less.ite(bitvec[i], constant.bitvec[i]);
        }
        return res;
    }

    /**
     * Sets a maximum value for the bit vector.
     * @return a new bit vector with the given upper bound
     */
    private SupremicaBDDBitVector max(final int val)
    {
      final SupremicaBDDBitVector constant = buildSupBDDBitVector(bitNum, val);
      final BDD greater = this.gth(constant);
      final SupremicaBDDBitVector res = this.copy();
      for (int i=0; i<res.bitNum; i++) {
          res.bitvec[i] = greater.ite(bitvec[i], constant.bitvec[i]);
      }
      return res;
    }

    /**
     * Finds the maximum value that is represented in the bit vector.
     * @return maximum value
     */
    abstract public int max();

    /**
     * Finds the minimum value that is represented in the bit vector.
     * @return minimum value
     */
    abstract public int min();

    /**
     * Finds out how many bits that are required to represent the set of values
     * that this bit vector represents.
     * @return required bits
     */
    abstract public int requiredBits();

    /**
     * Reduces the size of the bit vector to match what is needed to store the
     * set of values.
     * @return bit vector with less bits
     */
    public SupremicaBDDBitVector optimizeSize() {

      final int required = requiredBits();
      return resize(required);

    }

    public static abstract class BitVectorOp {

      abstract BDD apply(SupremicaBDDBitVector left,
                                                   SupremicaBDDBitVector right);

    }

    private static class EquOp extends BitVectorOp {

      @Override
      BDD apply(final SupremicaBDDBitVector left,
                                  final SupremicaBDDBitVector right)
      {
        return left.equ(right);
      }

    }

    private static class NeqOp extends BitVectorOp {

      @Override
      BDD apply(final SupremicaBDDBitVector left, final SupremicaBDDBitVector right)
      {
        return left.neq(right);
      }

    }

    private static class LthOp extends BitVectorOp {

      @Override
      BDD apply(final SupremicaBDDBitVector left, final SupremicaBDDBitVector right)
      {
        return left.lth(right);
      }

    }

    private static class LteOp extends BitVectorOp {

      @Override
      BDD apply(final SupremicaBDDBitVector left, final SupremicaBDDBitVector right)
      {
        return left.lte(right);
      }

    }

    private static class GthOp extends BitVectorOp {

      @Override
      BDD apply(final SupremicaBDDBitVector left, final SupremicaBDDBitVector right)
      {
        return left.gth(right);
      }

    }

    private static class GteOp extends BitVectorOp {

      @Override
      BDD apply(final SupremicaBDDBitVector left, final SupremicaBDDBitVector right)
      {
        return left.gte(right);
      }

    }

}
