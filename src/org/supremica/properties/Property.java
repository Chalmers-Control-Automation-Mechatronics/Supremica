//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica
//# PACKAGE: org.supremica.properties
//# CLASS:   Property
//###########################################################################
//# $Id: Property.java,v 1.4 2007-08-21 00:03:16 robi Exp $
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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public abstract class Property
{

    //#######################################################################
    //# Constructors
    public Property(final PropertyType type,
                    final String key,
                    final String comment,
                    final boolean immutable)
    {
        assert(type != null);
        this.type = type;
        this.key = key;
        this.comment = comment;
        this.immutable = immutable;
        COLLECTION.add(this);
        FULLKEY_TO_PROPERTY.put(getFullKey(), this);
    }


    //#######################################################################
    //# Simple Access
    public final PropertyType getPropertyType()
    {
        return type;
    }

    public final String getKey()
    {
        return key;
    }

    public final String getFullKey()
    {
        return type.toString() + "." + key;
    }

    public String getComment()
    {
        return comment;
    }

    public boolean isImmutable()
    {
        return immutable;
    }

    public String valueToEscapedString()
    {
        return valueToString();
    }


    //#######################################################################
    //# General Object Handling
    public final String toString()
    {
        return getFullKey() + " " + valueToString();
    }

    /*
     * Can there be two objects representing the same property?
     * I hope not! ~~~Robi
    public boolean equals(Object other)
    {
        if (other == null || (!(other instanceof Property)))
        {
            return false;
        }
        return getFullKey().equals(((Property)other).getFullKey());
    }
     */


    //#######################################################################
    //# Provided by Subclasses
    public abstract void set(String value);

    public abstract String valueToString();

    public abstract boolean currentValueDifferentFromDefaultValue();


    //#######################################################################
    //# Accessing the Properties List
    /**
     * Gets the list of all registered properties.
     * @return An unmodifiable list of properties.
     */
    public static List<Property> getAllProperties()
    {
        return UNMOD_COLLECTION;
    }

    public static Property getProperty(final String fullKey)
    {
        return FULLKEY_TO_PROPERTY.get(fullKey);
    }


    //#######################################################################
    //# Data Members
    private final PropertyType type;
    private final boolean immutable;
    private final String key;
    private final String comment;


    //#######################################################################
    //# Static Class Variables
    private static final List<Property> COLLECTION =
        new LinkedList<Property>();
    private static final List<Property> UNMOD_COLLECTION =
        Collections.unmodifiableList(COLLECTION);
    private static final HashMap<String,Property> FULLKEY_TO_PROPERTY =
        new HashMap<String, Property>();

}
