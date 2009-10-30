
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
package org.supremica.automata;

import java.util.*;
import net.sourceforge.waters.xsd.base.ComponentKind;

public class AutomatonType
{
    private static List<AutomatonType> collection = new LinkedList<AutomatonType>();
    public static final AutomatonType UNDEFINED = new AutomatonType("Undefined", false);
    public static final AutomatonType PLANT = new AutomatonType("Plant", true);
    public static final AutomatonType SPECIFICATION = new AutomatonType("Specification", true);
    public static final AutomatonType SUPERVISOR = new AutomatonType("Supervisor", true);
    public static final AutomatonType PROPERTY = new AutomatonType("Property", true);
    
    private String identifier;
    
    private AutomatonType(String identifier, boolean add)
    {
        if (add)
        {
            collection.add(this);
        }
        
        this.identifier = identifier;
    }
    
    public static Iterator<AutomatonType> iterator()
    {
        return collection.iterator();
    }
    
    public String toString()
    {
        return identifier;
    }
    
    public static AutomatonType toType(String type)
    {
        for (Iterator<AutomatonType> it = collection.iterator(); it.hasNext(); )
        {
            AutomatonType thisOne = it.next();
            if (equals(thisOne, type))
            {
                return thisOne;
            }
        }
        
        return UNDEFINED;
    }
    
    public static ComponentKind toKind(AutomatonType type)
    {
        if (type == SPECIFICATION)
            return ComponentKind.SPEC;
        if (type == PROPERTY)
            return ComponentKind.PROPERTY;
        if (type == SUPERVISOR)
            return ComponentKind.SUPERVISOR;
        return ComponentKind.PLANT;
    }
    
    public static AutomatonType toType(ComponentKind type)
    {
        if (type == ComponentKind.PLANT)
        {
            return PLANT;
        }
        if (type == ComponentKind.SPEC)
        {
            return SPECIFICATION;
        }
        if (type == ComponentKind.SUPERVISOR)
        {
            return SUPERVISOR;
        }
        if (type == ComponentKind.PROPERTY)
        {
            return PROPERTY;
        }
        
        return UNDEFINED;
    }
    
    public static Object[] toArray()
    {
        return collection.toArray();
    }
    
    private static boolean equals(AutomatonType type, String ident)
    {
        if ((type == null) || (ident == null))
        {
            return false;
        }
        
        return ident.toLowerCase().equals(type.toString().toLowerCase());
    }
}
