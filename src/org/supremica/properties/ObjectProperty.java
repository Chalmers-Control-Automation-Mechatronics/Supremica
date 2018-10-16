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
   * Creates an editable object property without legal values.
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
    this(type, key, value, null, value.getClass(), comment, true);
  }

  /**
   * Creates an editable object property for an enumeration type.
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
    this(type, key, value, clazz.getEnumConstants(), clazz, comment, true);
  }

  /**
   * Creates an object property by specifying all attributes.
   * @param type        The property category.
   * @param key         The name of the property.
   * @param value       The default value of the property.
   * @param legalValues An array containing the allowed values for the property.
   * @param clazz       The class of object values.
   * @param comment     A textual description of the property.
   * @param editable    A flag, indicating whether the user can edit the
   *                    property value through the properties dialog.
   */
  public ObjectProperty(final PropertyType type, final String key,
                        final T value, final T[] legalValues,
                        final Class<?> clazz, final String comment,
                        final boolean editable)
  {
    super(type, key, comment, editable);
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

  /**
   * Sets the property to a new value.
   * @param  value   The new value for the property.
   * @return <CODE>true</CODE> if the new value was different from the old
   *         value, <CODE>false</CODE> if the property was unchanged.
   */
  public boolean setValue(final T value)
  {
    if (!mValue.equals(value)) {
      checkValid(value);
      final String oldvalue = getAsString();
      mValue = value;
      firePropertyChanged(oldvalue);
      return true;
    } else {
      return false;
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


  //#########################################################################
  //# Data Members
  private final T mDefaultValue;
  private final T[] mLegalValues;
  private Class<?> mObjectClass;
  private T mValue;

}
