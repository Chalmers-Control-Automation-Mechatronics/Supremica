//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica
//# PACKAGE: org.supremica.properties
//# CLASS:   ObjectProperty
//###########################################################################
//# $Id$
//###########################################################################

package org.supremica.properties;

public class ObjectProperty<T> extends Property
{

  //#########################################################################
  //# Constructors
  public ObjectProperty(final PropertyType type, final String key,
                        final T value, final String comment)
  {
    this(type, key, value, comment, null);
  }

  public ObjectProperty(final PropertyType type, final String key,
                        final T value, final String comment,
                        final T[] legalValues)
  {
    this(type, key, value, comment, legalValues, false);
  }

  public ObjectProperty(final PropertyType type, final String key,
                        final T value, final String comment,
                        final T[] legalValues, final boolean immutable)
  {
    super(type, key, comment, immutable);
    mDefaultValue = mValue = value;
    mLegalValues = legalValues;
  }


  //#########################################################################
  //# Overrides for org.supremica.properties.Property
  @Override
  public String getAsString()
  {
    return get().toString();
  }

  @Override
  public void set(final String value)
  {
    final Object oldvalue = mValue;
    setValue(parseObject(value));
    firePropertyChanged(oldvalue.toString());
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
    if (mValue != value) {
      checkValid(value);
      checkMutable();
      final String oldvalue = getAsString();
      mValue = value;
      firePropertyChanged(oldvalue);
    }
  }

  public boolean isValid(final T value)
  {
    if (value == null) {
      throw new IllegalArgumentException
        ("null property values are not allowed!");
    }
    if (mLegalValues == null) {
      return true;
    }
    for (int i = 0; i < mLegalValues.length; i++) {
      if (value.equals(mLegalValues[i])) {
        return true;
      }
    }
    return false;
  }

  public void checkValid(final T value)
  {
    if (!isValid(value)) {
      throw new IllegalArgumentException
        ("Assigning illegal value to property " + getFullKey() + ": " +
         value + "!");
    }
  }

  public T[] getLegalValues()
  {
    return mLegalValues;
  }

  public Class<?> getObjectClass()
  {
    return mDefaultValue.getClass();
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
    final StringBuffer outBuffer = new StringBuffer(len * 2);

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
  private T mValue;

}
