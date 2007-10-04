//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica
//# PACKAGE: org.supremica.properties
//# CLASS:   IntegerProperty
//###########################################################################
//# $Id: IntegerProperty.java,v 1.6 2007-10-04 15:14:56 flordal Exp $
//###########################################################################

/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */
package org.supremica.properties;

public class IntegerProperty
    extends Property
{
    
    //#######################################################################
    //# Constructors
    public IntegerProperty(final PropertyType type,
        final String key,
        final int value,
        final String comment)
    {
        this(type, key, value, comment, false);
    }
    
    public IntegerProperty(final PropertyType type,
        final String key,
        final int value,
        final String comment,
        final boolean immutable)
    {
        this(type, key, value, comment, immutable,
            Integer.MIN_VALUE, Integer.MAX_VALUE);
    }
    
    public IntegerProperty(final PropertyType type,
        final String key,
        final int value,
        final String comment,
        final boolean immutable,
        final int min)
    {
        this(type, key, value, comment, immutable, min, Integer.MAX_VALUE);
    }
    
    public IntegerProperty(final PropertyType type,
        final String key,
        final int value,
        final String comment,
        final boolean immutable,
        final int min,
        final int max)
    {
        this(type, key, value, comment, immutable,
            Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
    }
    
    public IntegerProperty(final PropertyType type,
        final String key,
        final int value,
        final String comment,
        final boolean immutable,
        final int min,
        final int max,
        final int tick)
    {
        super(type, key, comment, immutable);
        mDefaultValue = value;
        mValue = value;
        mMin = min;
        mMax = max;
        mTick = tick;
        checkValid(value);
    }
    
    
    //#######################################################################
    //# Overrides for Abstract Base Class Property
    public void set(final String value)
    {
        set(Integer.parseInt(value));
    }
    
    public String getAsString()
    {
        return Integer.toString(mValue);
    }
    
    public boolean currentValueDifferentFromDefaultValue()
    {
        return mDefaultValue != mValue;
    }
    
    
    //#######################################################################
    //# Specific Access
    public int get()
    {
        return mValue;
    }
    
    public int getMinValue()
    {
        return mMin;
    }
    
    public int getMaxValue()
    {
        return mMax;
    }
    
    public int getTick()
    {
        return mTick;
    }
    
    public void set(final int value)
    {
        checkMutable();
        checkValid(value);
        final String oldvalue = getAsString();
        mValue = value;
        firePropertyChanged(oldvalue);
    }
    
    public boolean isValid(final int value)
    {
        return value >= mMin && value <= mMax;
    }
    
    public void checkValid(final int value)
    {
        if (!isValid(value))
        {
            throw new IllegalArgumentException
                ("Assigning illegal value to property " + getFullKey() +
                ": " + value + "!");
        }
    }
    
    
    //#######################################################################
    //# Data Members
    private final int mDefaultValue;
    private final int mMin;
    private final int mMax;
    private final int mTick;
    
    private int mValue;
    
}
