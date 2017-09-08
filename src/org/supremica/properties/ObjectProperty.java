//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica
//# PACKAGE: org.supremica.properties
//# CLASS:   ObjectProperty
//###########################################################################
//# $Id$
//###########################################################################

package org.supremica.properties;

import net.sourceforge.waters.model.base.ProxyTools;

/**
 * <P>A property of object type.</P>
 *
 * <P>This class is used to represent properties of string or enumeration type.
 * It supports type checking and comparison of values against a fixed list
 * of allowed possibilities.</P>
 *
 * @author Knut &Aring;kesson, Hugo Flordal, Robi Malik
 */

public class ObjectProperty<T> extends Property
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a mutable object property without legal values.
   * This constructor creates a property without legal values and using
   * the object type of the default value.
   * @param type    The property category.
   * @param key     The name of the property.
   * @param value   The default value of the property.
   * @param comment A textual description of the property.
   */
  public ObjectProperty(final PropertyType type, final String key,
                        final T value, final String comment)
  {
    this(type, key, value, null, value.getClass(), comment, false);
  }

  /**
   * Creates a mutable object property for an enumeration type.
   * @param type    The property category.
   * @param key     The name of the property.
   * @param value   The default value of the property.
   * @param clazz   The class of object values, which must be an enumeration
   *                type. The legal values are queried from this class.
   * @param comment A textual description of the property.
   */
  public ObjectProperty(final PropertyType type, final String key,
                        final T value, final Class<T> clazz,
                        final String comment)
  {
    this(type, key, value, clazz.getEnumConstants(), clazz, comment, false);
  }

  /**
   * Creates an object property by specifying all attributes.
   * @param type        The property category.
   * @param key         The name of the property.
   * @param value       The default value of the property.
   * @param legalValues An array containing the allowed values for the property.
   * @param clazz       The class of object values.
   * @param comment     A textual description of the property.
   * @param immutable   A flag, indicating whether the property value can be
   *                    changed by the user. Immutable (<CODE>true</CODE>)
   *                    means that it cannot be changed, and calling the
   *                    methods to change it will result in an exception.
   */
  public ObjectProperty(final PropertyType type, final String key,
                        final T value, final T[] legalValues,
                        final Class<?> clazz, final String comment,
                        final boolean immutable)
  {
    super(type, key, comment, immutable);
    mDefaultValue = mValue = value;
    mLegalValues = legalValues;
    mObjectClass = clazz;
  }


  //#########################################################################
  //# Overrides for org.supremica.properties.Property
  @Override
  public String getAsString()
  {
    return get().toString();
  }

  @Override
  public void set(final String text)
  {
    final T value = parseObject(text);
    setValue(value);
  }

  @Override
  public String valueToEscapedString()
  {
    return ObjectProperty.convert(getAsString(), false);
  }

  @Override
  public boolean currentValueDifferentFromDefaultValue()
  {
    return !mDefaultValue.equals(mValue);
  }


  //#########################################################################
  //# Simple Access
  public T get()
  {
    return mValue;
  }

  public void setValue(final T value)
  {
    if (!mValue.equals(value)) {
      checkValid(value);
      checkMutable();
      final String oldvalue = getAsString();
      mValue = value;
      firePropertyChanged(oldvalue);
    }
  }

  public boolean isValid(final Object value)
  {
    if (value == null) {
      return false;
    } else if (mLegalValues == null) {
      return mObjectClass.isAssignableFrom(value.getClass());
    } else {
      for (final T legal : mLegalValues) {
        if (value.equals(legal)) {
          return true;
        }
      }
      return false;
    }
  }

  public void checkValid(final Object value)
  {
    if (!isValid(value)) {
      throw new IllegalArgumentException
        ("Assigning illegal value to property " + getFullKey() + ": " +
         ProxyTools.toString(value) + "!");
    }
  }

  public T[] getLegalValues()
  {
    return mLegalValues;
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Parses a String.
   */
  private T parseObject(final String value)
  {
    if (mLegalValues != null) {
      for (final T object : mLegalValues) {
        if (object.toString().equals(value)) {
          return object;
        }
      }
      return null;
    } else {
      @SuppressWarnings("unchecked")
      final T object = (T) value;
      return object;
    }
  }


  // ALL OF THIS IS COMING FROM THE JAVA SDK CODE (Properties.java)
  private static String convert(final String theString,
                                final boolean escapeSpace)
  {
    final int len = theString.length();
    final StringBuilder outBuffer = new StringBuilder(len * 2);

    for (int x = 0; x < len; x++) {
      final char aChar = theString.charAt(x);

      switch (aChar) {

      case ' ':
        if ((x == 0) || escapeSpace) {
          outBuffer.append('\\');
        }

        outBuffer.append(' ');
        break;

      case '\\':
        outBuffer.append('\\');
        outBuffer.append('\\');
        break;

      case '\t':
        outBuffer.append('\\');
        outBuffer.append('t');
        break;

      case '\n':
        outBuffer.append('\\');
        outBuffer.append('n');
        break;

      case '\r':
        outBuffer.append('\\');
        outBuffer.append('r');
        break;

      case '\f':
        outBuffer.append('\\');
        outBuffer.append('f');
        break;

      default:
        if ((aChar < 0x0020) || (aChar > 0x007e)) {
          outBuffer.append('\\');
          outBuffer.append('u');
          outBuffer.append(toHex((aChar >> 12) & 0xF));
          outBuffer.append(toHex((aChar >> 8) & 0xF));
          outBuffer.append(toHex((aChar >> 4) & 0xF));
          outBuffer.append(toHex(aChar & 0xF));
        } else {
          if (specialSaveChars.indexOf(aChar) != -1) {
            outBuffer.append('\\');
          }

          outBuffer.append(aChar);
        }
      }
    }

    return outBuffer.toString();
  }

  private static char toHex(final int nibble)
  {
    return hexDigit[(nibble & 0xF)];
  }

  /** A table of hex digits */
  private static final char[] hexDigit = {'0', '1', '2', '3', '4', '5', '6',
                                          '7', '8', '9', 'A', 'B', 'C', 'D',
                                          'E', 'F'};
  private static final String specialSaveChars = "=: \t\r\n\f#!";


  //#########################################################################
  //# Data Members
  private final T mDefaultValue;
  private final T[] mLegalValues;
  private Class<?> mObjectClass;
  private T mValue;

}
