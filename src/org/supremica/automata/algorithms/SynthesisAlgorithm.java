
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

public enum SynthesisAlgorithm
{
    MONOLITHIC("Monolithic", false),
    MODULAR("Modular"),
    COMPOSITIONAL("Compositional"),
    //IDD("IDD"),
    //MonolithicSingleFixpoint("MONOLITHIC (single fixpoint)", false),    // works, but is very slow [due to lame implementation :s ]
    BDD("BDD");    // works, but we cant handle the results yet
    
    /** Textual description. */
    private final String description;
    /** True if this algo prefers working on modular systems. */
    private final boolean preferModular;
    
    private SynthesisAlgorithm(String description)
    {
        this(description, true);
    }
    
    private SynthesisAlgorithm(String description, boolean preferModular)
    {
        this.description = description;
        this.preferModular = preferModular;
    }
    
    public String toString()
    {
        return description;
    }
    
    public boolean prefersModular()
    {
        return preferModular;
    }
    
    public static SynthesisAlgorithm fromDescription(String description)
    {
        for (SynthesisAlgorithm value: values())
        {
            if (value.description.equals(description))
            {
                return value;
            }
        }
        return null;
    }
}
