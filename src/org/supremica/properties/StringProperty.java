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

public class StringProperty
    extends Property
{
    private String defaultValue;
    private String value;
    private Object[] legalValues;
    boolean ignoreCase = false;
    
    public StringProperty(PropertyType type, String key, Object value, String comment)
    {
        this(type, key, value, comment, null);
    }
    
    public StringProperty(PropertyType type, String key, Object value, String comment, Object[] legalValues)
    {
        this(type, key, value, comment, legalValues, false);
    }
    
    public StringProperty(PropertyType type, String key, Object value, String comment, Object[] legalValues, boolean immutable)
    {
        this(type, key, value, comment, legalValues, immutable, false);
    }
    
    public StringProperty(PropertyType type, String key, Object value, String comment, Object[] legalValues, boolean immutable, boolean ignoreCase)
    {
        super(type, key, comment, immutable);
        this.defaultValue = value.toString();
        this.value = value.toString();
        this.legalValues = legalValues;
        this.ignoreCase = ignoreCase;
        if (!isValid(this.value))
        {
            throw new IllegalArgumentException("Illegal value");
        }
    }
    
    public String get()
    {
        return value;
    }
    
    public void set(String value)
    {
        if (isImmutable())
        {
            throw new IllegalStateException("This object is immutable, calling the set method is illegal.");
        }
        
        this.value = value;
    }
    
    public boolean isValid(String value)
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
            if (!ignoreCase)
            {
                if (value.equals(legalValues[i].toString()))
                {
                    return true;
                }
            }
            else
            {
                if (value.equalsIgnoreCase(legalValues[i].toString()))
                {
                    return true;
                }
            }
        }
        return false;
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
    
    public String valueToString()
    {
        return get();
    }
    
    public String valueToEscapedString()
    {
        return StringProperty.convert(get(), false);
    }
    
    public boolean currentValueDifferentFromDefaultValue()
    {
        if (!ignoreCase)
        {
            return !defaultValue.equals(value);
        }
        else
        {
            return !defaultValue.equalsIgnoreCase(value);
        }
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
