
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
package org.supremica.automata.algorithms;

public enum SynthesisType
{
    CONTROLLABLE("controllable"),
    NONBLOCKING("nonblocking"),
    NONBLOCKINGCONTROLLABLE("nonblocking and controllable"),
    UNSAFETY("unsafety (for RAS models)"),
    NONBLOCKINGCONTROLLABLEOBSERVABLE("nonblocking, controllable and observable");


    /** Textual description. */
    private final String description;

    private SynthesisType(final String description)
    {
        this.description = description;
    }

    public static SynthesisType[] editorValues()
    {
        final SynthesisType[] vals = new SynthesisType[4];
        int i = 0;
        for(final SynthesisType v:SynthesisType.values())
        {
            if(i < 4)
            {
                vals[i] = v;
            }
            i++;
        }

        return vals;
    }

    public static SynthesisType[] analyzerValues()
    {
        final SynthesisType[] vals = new SynthesisType[3];
        vals[0] = CONTROLLABLE;
        vals[1] = NONBLOCKING;
        vals[2] = NONBLOCKINGCONTROLLABLE;
        return vals;
    }

    @Override
    public String toString()
    {
        return description;
    }

    public static SynthesisType fromDescription(final String description)
    {
        for (final SynthesisType value: values())
        {
            if (value.description.equals(description))
            {
                return value;
            }
        }
        return null;
    }
}
