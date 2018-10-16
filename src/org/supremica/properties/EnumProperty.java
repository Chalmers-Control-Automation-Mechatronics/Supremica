//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica
//# PACKAGE: org.supremica.properties
//# CLASS:   EnumProperty<T>
//###########################################################################
//# $Id$
//###########################################################################

package org.supremica.properties;

/**
 * <P>A property of enumeration type.</P>
 *
 * <P>Enumeration-type properties are parametrised with an enumeration
 * type and only accept values of that type. Conversion from string to
 * enumeration objects is done through the methods of Java's {@link Enum}
 * class.</P>
 *
 * <P>This class also provides legacy support to facilitate the conversion
 * of a {@link BooleanProperty} to use an enumeration type. This is done
 * by setting enumeration values to be used for "true" and "false", making
 * it possible to read in property files containing Boolean values.</P>
 *
 * @author Robi Malik
 */

public class EnumProperty<E extends Enum<E>> extends Property
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an editable enumeration property.
   * @param type       The property category.
   * @param key        The name of the property.
   * @param value      The default value of the property.
   * @param clazz      The class of object values, which must be an enumeration
   *                   type. The legal values are queried from this class.
   * @param comment    A textual description of the property.
   */
  public EnumProperty(final PropertyType type,
                      final String key,
                      final E value,
                      final Class<E> clazz,
                      final String comment)
  {
    this(type, key, value, clazz, comment, true);
  }

  /**
   * Creates an editable enumeration property with legacy support.
   * @param type       The property category.
   * @param key        The name of the property.
   * @param value      The default value of the property.
   * @param trueValue  A legacy value for the case when a property string
   *                   has the value "true".
   * @param falseValue A legacy value for the case when a property string
   *                   has the value "false".
   * @param clazz      The class of object values, which must be an enumeration
   *                   type. The legal values are queried from this class.
   * @param comment    A textual description of the property.
   */
  public EnumProperty(final PropertyType type,
                      final String key,
                      final E value,
                      final E trueValue,
                      final E falseValue,
                      final Class<E> clazz,
                      final String comment)
  {
    this(type, key, value, trueValue, falseValue, clazz, comment, true);
  }

  /**
   * Creates an enumeration property.
   * @param type       The property category.
   * @param key        The name of the property.
   * @param value      The default value of the property.
   * @param clazz      The class of object values, which must be an enumeration
   *                   type. The legal values are queried from this class.
   * @param comment    A textual description of the property.
   * @param editable   A flag, indicating whether the user can edit the
   */
  public EnumProperty(final PropertyType type,
                      final String key,
                      final E value,
                      final Class<E> clazz,
                      final String comment,
                      final boolean editable)
  {
    this(type, key, value, null, null, clazz, comment, editable);
  }

  /**
   * Creates an enumeration property with legacy support.
   * @param type       The property category.
   * @param key        The name of the property.
   * @param value      The default value of the property.
   * @param trueValue  A legacy value for the case when a property string
   *                   has the value "true".
   * @param falseValue A legacy value for the case when a property string
   *                   has the value "false".
   * @param clazz      The class of object values, which must be an enumeration
   *                   type. The legal values are queried from this class.
   * @param comment    A textual description of the property.
   * @param editable   A flag, indicating whether the user can edit the
   *                   property value through the properties dialog.
   */
  public EnumProperty(final PropertyType type,
                      final String key,
                      final E value,
                      final E trueValue,
                      final E falseValue,
                      final Class<E> clazz,
                      final String comment,
                      final boolean editable)
  {
    super(type, key, comment, editable);
    mEnumClass = clazz;
    mDefaultValue = mValue = value;
    mTrueValue = trueValue;
    mFalseValue = falseValue;
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
    final E value = parseObject(text);
    setValue(value);
  }

  @Override
  public boolean currentValueDifferentFromDefaultValue()
  {
    return mValue != mDefaultValue;
  }


  //#########################################################################
  //# Simple Access
  public E get()
  {
    return mValue;
  }

  /**
   * Sets the property to a new value.
   * @param  value   The new value for the property.
   * @return <CODE>true</CODE> if the new value was different from the old
   *         value, <CODE>false</CODE> if the property was unchanged.
   */
  public boolean setValue(final E value)
  {
    if (mValue != value) {
      final String oldText = getAsString();
      mValue = value;
      firePropertyChanged(oldText);
      return true;
    } else {
      return false;
    }
  }

  public E[] getLegalValues()
  {
    return mEnumClass.getEnumConstants();
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Parses a string into the enumeration type associated with this property.
   */
  private E parseObject(final String value)
  {
    if (mTrueValue != null && value.equalsIgnoreCase("true")) {
      return mTrueValue;
    } else if (mFalseValue != null && value.equalsIgnoreCase("false")) {
      return mFalseValue;
    } else {
      return Enum.valueOf(mEnumClass, value);
    }
  }


  //#########################################################################
  //# Data Members
  private Class<E> mEnumClass;
  private final E mDefaultValue;
  private E mValue;
  private E mTrueValue;
  private E mFalseValue;

}
