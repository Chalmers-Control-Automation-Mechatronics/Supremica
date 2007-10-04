//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica
//# PACKAGE: org.supremica.properties
//# CLASS:   StringProperty
//###########################################################################
//# $Id: ObjectProperty.java,v 1.1 2007-10-04 15:14:56 flordal Exp $
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

public class ObjectProperty
    extends Property
{
    private String defaultValue;
    private Object mValue;
    private Object[] legalValues;
    
    public ObjectProperty(PropertyType type, String key, Object value, String comment)
    {
        this(type, key, value, comment, null);
    }
    
    public ObjectProperty(PropertyType type, String key, Object value, String comment, Object[] legalValues)
    {
        this(type, key, value, comment, legalValues, false);
    }
    
    public ObjectProperty(PropertyType type, String key, Object value, String comment, Object[] legalValues, boolean immutable)
    {
        super(type, key, comment, immutable);
        this.defaultValue = value.toString();
        mValue = value.toString();
        this.legalValues = legalValues;
    }
    
    public Object get()
    {
        return mValue;
    }
    
    public void set(final String value)
    {
        final Object oldvalue = mValue;
        set(parseObject(value));
        firePropertyChanged(oldvalue.toString());
    }
   
    public void set(final Object value)
    {
        checkValid(value);
        checkMutable();
        mValue = value;
    }
        
    /**
     * Parses a String.
     */
    private Object parseObject(String value)
    {
        if (legalValues != null)
        {
            for (Object object: legalValues)
            {
                if (object.toString().equals(value))
                {
                    return object;
                }
            }
            return null;
        }
        else
        {
            return value;
        }
    }
    
    public boolean isValid(Object value)
    {
        if (value == null)
        {
            throw new IllegalArgumentException("null strings are not allowed.");
        }
        if (legalValues == null)
        {
            return true;
        }
        for (int i = 0; i < legalValues.length; i++)
        {
            if (value.equals(legalValues[i]))
            {
                return true;
            }
        }
        return false;
    }

    public void checkValid(final Object value)
    {
        if (!isValid(value)) {
            throw new IllegalArgumentException
                ("Assigning illegal value to property " + getFullKey() +
                 ": " + value + "!");
        }
    }
    
    public Object[] legalValues()
    {
        return legalValues;        
    }
 
    public String[] legalValuesAsStrings()
    {
        String[] legalValuesAsStrings = new String[legalValues.length];
        for (int i = 0; i < legalValues.length; i++)
        {
            legalValuesAsStrings[i] = legalValues[i].toString();
        }
        return legalValuesAsStrings;        
    }
    
    public String getAsString()
    {
        return get().toString();
    }
    
    public String valueToEscapedString()
    {
        return ObjectProperty.convert(get().toString(), false);
    }
    
    public boolean currentValueDifferentFromDefaultValue()
    {
        return !defaultValue.equals(mValue);
    }
    
    // --------------------------------------------------------------
    // ALL OF THIS IS COMING FROM THE JAVA SDK CODE (Properties.java)
    private static char toHex(int nibble)
    {
        return hexDigit[(nibble & 0xF)];
    }
    
    /** A table of hex digits */
    private static final char[] hexDigit = { '0', '1', '2', '3', '4', '5',
    '6', '7', '8', '9', 'A', 'B',
    'C', 'D', 'E', 'F' };
    private static final String keyValueSeparators = "=: \t\r\n\f";
    private static final String strictKeyValueSeparators = "=:";
    private static final String specialSaveChars = "=: \t\r\n\f#!";
    private static final String whiteSpaceChars = " \t\r\n\f";
    
    private static String convert(String theString, boolean escapeSpace)
    {
        int len = theString.length();
        StringBuffer outBuffer = new StringBuffer(len * 2);
        
        for (int x = 0; x < len; x++)
        {
            char aChar = theString.charAt(x);
            
            switch (aChar)
            {
                
                case ' ' :
                    if ((x == 0) || escapeSpace)
                    {
                        outBuffer.append('\\');
                    }
                    
                    outBuffer.append(' ');
                    break;
                    
                case '\\' :
                    outBuffer.append('\\');
                    outBuffer.append('\\');
                    break;
                    
                case '\t' :
                    outBuffer.append('\\');
                    outBuffer.append('t');
                    break;
                    
                case '\n' :
                    outBuffer.append('\\');
                    outBuffer.append('n');
                    break;
                    
                case '\r' :
                    outBuffer.append('\\');
                    outBuffer.append('r');
                    break;
                    
                case '\f' :
                    outBuffer.append('\\');
                    outBuffer.append('f');
                    break;
                    
                default :
                    if ((aChar < 0x0020) || (aChar > 0x007e))
                    {
                        outBuffer.append('\\');
                        outBuffer.append('u');
                        outBuffer.append(toHex((aChar >> 12) & 0xF));
                        outBuffer.append(toHex((aChar >> 8) & 0xF));
                        outBuffer.append(toHex((aChar >> 4) & 0xF));
                        outBuffer.append(toHex(aChar & 0xF));
                    }
                    else
                    {
                        if (specialSaveChars.indexOf(aChar) != -1)
                        {
                            outBuffer.append('\\');
                        }
                        
                        outBuffer.append(aChar);
                    }
            }
        }
        
        return outBuffer.toString();
    }
}
