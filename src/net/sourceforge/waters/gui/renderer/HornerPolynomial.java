//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   HornerPolynomial
//###########################################################################
//# $Id: HornerPolynomial.java,v 1.2 2007-05-24 18:58:54 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui.renderer;

import java.util.List;


public class HornerPolynomial
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a constant polynomial.
   * f(x) = a0.
   */
  public HornerPolynomial(final double a0)
  {
    this(0);
    mCoefficients[0] = a0;
  }

  /**
   * Creates a linear polynomial.
   * f(x) = a1*x + a0.
   */
  public HornerPolynomial(final double a1, final double a0)
  {
    this(1);
    mCoefficients[0] = a1;
    mCoefficients[1] = a0;
  }

  /**
   * Creates a quadratic polynomial.
   * f(x) = a2*x^2 + a1*x + a0.
   */
  public HornerPolynomial(final double a2, final double a1, final double a0)
  {
    this(2);
    mCoefficients[0] = a2;
    mCoefficients[1] = a1;
    mCoefficients[2] = a0;
  }

  /**
   * Creates a cubic polynomial.
   * f(x) = a3*x^3 + a2*x^2 + a1*x + a0.
   */
  public HornerPolynomial(final double a3, final double a2,
                          final double a1, final double a0)
  {
    this(3);
    mCoefficients[0] = a3;
    mCoefficients[1] = a2;
    mCoefficients[2] = a1;
    mCoefficients[3] = a0;
  }

  /**
   * Creates a biquadratic polynomial.
   * f(x) = a4*x^4 + a3*x^3 + a2*x^2 + a1*x + a0.
   */
  public HornerPolynomial(final double a4, final double a3, final double a2,
                          final double a1, final double a0)
  {
    this(4);
    mCoefficients[0] = a4;
    mCoefficients[1] = a3;
    mCoefficients[2] = a2;
    mCoefficients[3] = a1;
    mCoefficients[4] = a0;
  }

  /**
   * Creates a polynomial from an array of coefficients.
   * f(x) = a[n]*x^n + ... + a[1]*x + a[0].
   * @param a      The array of coefficients. The leading coefficient a[n]
   *               occurs last in the array. The array is copied to insure
   *               immutability of the polynomial.
   */
  public HornerPolynomial(final double[] a)
  {
    this(a.length - 1);
    final int top = a.length - 1;
    for (int i = 0; i < a.length; i++) {
      mCoefficients[top - i] = a[i];
    }
  }

  /**
   * Creates a polynomial from list of coefficients.
   * f(x) = a[n]*x^n + ... + a[1]*x + a[0].
   * @param list   The list of coefficients. The leading coefficient a[n]
   *               occurs last in the list. The list contents are copied
   *               to insure immutability of the polynomial.
   */
  public HornerPolynomial(final List<Double> list)
  {
    this(list.size() - 1);
    int i = list.size() - 1;
    for (final double coeff : list) {
      mCoefficients[i--] = coeff;
    }
  }

  private HornerPolynomial(final int degree)
  {
    mDegree = degree;
    mCoefficients = new double[degree + 1];
    mResults = new double[degree + 1];
    mEvaluatedBefore = 0;
    mUpperRow = new double[degree];
    mLowerRow = new double[degree];
  }


  //#########################################################################
  //# Simple Access
  public int getDegree()
  {
    return mDegree;
  }

  public double getCoefficient(final int exponent)
  {
    return mCoefficients[mDegree - exponent];
  }

  public double[] getCoefficients()
  {
    final double[] result = new double[mDegree + 1];
    for (int i = 0; i <= mDegree; i++) {
      result[i] = mCoefficients[mDegree - i];
    }
    return result;
  }

  public double getCurrentInput()
  {
    return mCurrentInput;
  }

  public void setCurrentInput(final double x)
  {
    if (mCurrentInput != x) {
      mCurrentInput = x;
      mEvaluatedBefore = 0;
    }
  }


  //#########################################################################
  //# Evaluating
  public double getValue(final double x)
  {
    setCurrentInput(x);
    return getValue();
  }

  public double getValue()
  {
    return getDerivativeValue(0);
  }

  public double getFirstDerivativeValue(final double x)
  {
    setCurrentInput(x);
    return getFirstDerivativeValue();
  }

  public double getFirstDerivativeValue()
  {
    return getDerivativeValue(1);
  }

  public double getFirstDerivativeValue(final int derivative, final double x)
  {
    setCurrentInput(x);
    return getDerivativeValue(derivative);
  }

  public double getDerivativeValue(final int derivative)
  {
    if (derivative > mDegree) {
      return 0.0;
    } else {
      evaluateUpTo(derivative);
      return mResults[derivative];
    }
  }


  //#########################################################################
  //# Derivatives
  public HornerPolynomial getFirstDerivative()
  {
    if (mDegree == 0) {
      return new HornerPolynomial(0.0);
    } else {
      final HornerPolynomial result = new HornerPolynomial(mDegree - 1);
      for (int i = 0; i < mDegree; i++) {
        final int exponent = mDegree - i;
        result.mCoefficients[i] = exponent * mCoefficients[i];
      }
      return result;
    }
  }


  //#########################################################################
  //# Newton Iteration
  public double newtonIteration(double x)
  {
    setCurrentInput(x);
    evaluateUpTo(1);
    double y = mResults[0];
    if (y == 0.0) {
      return x;
    }
    double yy = mResults[1];
    final boolean ygt0 = y > 0.0;
    if (yy == 0.0) {
      return x;
    }
    final boolean yygt0 = yy > 0.0;
    for (int i = 0; i < MAXITER; i++) {
      final double delta = y / yy;
      final double prevx = x;
      x -= delta;
      if (Math.abs(delta) < EPSILON) {
        return x;
      }
      setCurrentInput(x);
      evaluateUpTo(1);
      y = mResults[0];
      if (ygt0 ? y <= 0.0 : y >= 0.0) {
        return x;
      }
      yy = mResults[1];
      if (yygt0 ? yy <= 0.0 : yy >= 0.0) {
        return prevx;
      }
    }
    return x;
  }


  //#########################################################################
  //# Solving Quadratics
  public double findQuadraticApex()
  {
    checkQuadratic();
    return -0.5 * mCoefficients[1] / mCoefficients[0];
  }

  public double[] findQuadraticRoots()
  {
    final double apex = findQuadraticApex();
    final double det = apex * apex - mCoefficients[2] / mCoefficients[0];
    if (det < 0.0) {
      return null;
    } else if (det == 0.0) {
      final double[] roots = new double[1];
      roots[0] = apex;
      return roots;
    } else {
      final double sqrt = Math.sqrt(det);
      final double[] roots = new double[2];
      roots[0] = apex - sqrt;
      roots[1] = apex + sqrt;
      return roots;
    }      
  }


  //#########################################################################
  //# Solving Biquadratics
  public double findBiquadraticMinimum(final double x0, final double x1)
  {
    checkBiquadratic();
    if (mCoefficients[0] <= 0) {
      throw new IllegalStateException
        ("Leading coefficient of biquadratic must be positive!");
    }
    if (x1 <= x0) {
      throw new IllegalArgumentException("Illegal range: " + x0 + ".." + x1);
    }
    final double yy0 = getFirstDerivativeValue(x0);
    final double yy1 = getFirstDerivativeValue(x1);
    final HornerPolynomial derivative1 = getFirstDerivative();
    final HornerPolynomial derivative2 = derivative1.getFirstDerivative();
    final double apex = derivative2.findQuadraticApex();
    final double aslope = derivative2.getValue(apex);
    final boolean onlyone;
    final double extremal0;
    final double extremal1;
    if (aslope >= 0.0) {
      // Only one global minimum.
      onlyone = true;
      extremal0 = extremal1 = apex;
    } else {
      // Slope at apex negative, may be more than one global minimum.
      // Check extremals of 1st derivative ...
      final double[] extremals = derivative2.findQuadraticRoots();
      extremal0 = extremals[0];
      extremal1 = extremals[extremals.length - 1];
      onlyone =
        derivative1.getValue(extremal0) < 0.0 ||
        derivative1.getValue(extremal1) > 0.0;
    }
    if (onlyone) {
      if (yy1 <= 0.0) {
        return x1;
      } else if (yy0 >= 0.0) {
        return x0;
      } else if (derivative1.getValue(extremal0) < 0.0) {
        return derivative1.newtonIteration(x1);
      } else {
        return derivative1.newtonIteration(x0);
      }
    } else {
      // There really are two distinct global minima.
      if (x1 <= extremal0) {
        if (yy0 > 0.0 && yy1 < 0.0) {
          return derivative1.newtonIteration(x0);
        } else {
          return yy1 < 0.0 ? x0 : x1;
        }
      } else if (x0 >= extremal1) {
        if (yy0 < 0.0 && yy1 > 0.0) {
          return derivative1.newtonIteration(x1);
        } else {
          return yy0 < 0.0 ? x1 : x0;
        }
      } else {
        final double xcan0 =
          x0 < extremal0 && yy0 < 0.0 ? derivative1.newtonIteration(x0) : x0;
        final double xcan1 =
          x1 > extremal1 && yy1 > 0.0 ? derivative1.newtonIteration(x1) : x1;
        final double ycan0 = getValue(xcan0);
        final double ycan1 = getValue(xcan1);
        return ycan0 < ycan1 ? xcan0 : xcan1;
      }
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private void evaluateUpTo(final int derivative)
  {
    while (mEvaluatedBefore <= derivative) {
      final int current = mEvaluatedBefore++;
      final double[] upper = current == 0 ? mCoefficients : mUpperRow;
      final double[] lower = mLowerRow;
      final int top = mDegree - current;
      double prev = lower[0] = upper[0];
      for (int i = 1; i < top; i++) {
        lower[i] = prev = prev * mCurrentInput + upper[i];
      }
      mResults[current] = prev * mCurrentInput + upper[top];
      mLowerRow = mUpperRow;
      mUpperRow = lower;
    }
  }


  //#########################################################################
  //# Degree Checking
  private void checkQuadratic()
  {
    checkDegree(2, "Quadratic");
  }

  private void checkCubic()
  {
    checkDegree(3, "Cubic");
  }

  private void checkBiquadratic()
  {
    checkDegree(4, "Biquadratic");
  }

  private void checkDegree(final int degree, final String label)
  {
    if (mDegree != degree) {
      throw new IllegalStateException
        (label + " curve must have degree " + degree +
         ", but has degree " + mDegree + "!");
    }
    if (mCoefficients[0] == 0.0) {
      throw new IllegalStateException("Leading coefficient is zero!");
    }
  }


  //#########################################################################
  //# Data Members
  private final int mDegree;
  private final double mCoefficients[];
  private final double mResults[];
  private int mEvaluatedBefore;
  private double mUpperRow[];
  private double mLowerRow[];
  private double mCurrentInput = 0.0;


  //#########################################################################
  //# Class Constants
  /**
   * Accuracy constant. Used by Newton's method for convergence.
   */
  public static final double EPSILON = 1e-10;
  /**
   * Maximum number of Newton iterations.
   */
  public static final int MAXITER = 100;

}
