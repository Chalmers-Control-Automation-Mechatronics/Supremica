//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Supremica
//# PACKAGE: org.supremica.properties
//# CLASS:   ColorProperty
//###########################################################################
//# $Id: 0afaa5405a35fce59d92513e5ee9a115fbdafe6c $
//###########################################################################

package org.supremica.properties;

import java.awt.Color;
import java.awt.Component;


/**
 * A property that holds a colour value.
 * Colours are converted to text using standard hex notation, e.g. "#ff0096".
 *
 * @author Robi Malik
 */

public class ColorProperty
  extends Property
{

  //#########################################################################
  //# Constructors
  public ColorProperty(final PropertyType type,
                       final String key,
                       final Color value,
                       final String comment)
  {
    this(type, key, value, comment, true);
  }

  public ColorProperty(final PropertyType type,
                       final String key,
                       final Color value,
                       final String comment,
                       final boolean editable)
  {
    super(type, key, comment, editable);
    mDefaultValue = value;
    mValue = value;
  }


  //#########################################################################
  //# Overrides for Abstract Base Class Property
  @Override
  public void set(final String value)
  {
    final Color color = Color.decode(value);
    set(color);
  }

  @Override
  public String getAsString()
  {
    final int rgb = mValue.getRGB() & 0xffffff;
    final String hex = Integer.toHexString(rgb | 0x1000000);
    return "#" + hex.substring(1);
  }

  @Override
  public boolean currentValueDifferentFromDefaultValue()
  {
    return !mDefaultValue.equals(mValue);
  }


  //#########################################################################
  //# Specific Access
  public Color get()
  {
    return mValue;
  }

  public void set(final Color color)
  {
    if (!mValue.equals(color)) {
      final Color oldColor = mValue;
      mValue = color;
      firePropertyChanged(oldColor);
    }
  }

  /**
   * Adds a property change listener to this property that updates the
   * background colour of the given component to match the value of
   * this property.
   * @param  comp        The component tracking the colour.
   * @return The property change listener created. This return value should
   *         be used to unregister the listener using {@link
   *         #removePropertyChangeListener(SupremicaPropertyChangeListener)
   *         removePropertyChangeListener()} when it is no longer needed.
   */
  public SupremicaPropertyChangeListener addBackgroundListener
    (final Component comp)
  {
    comp.setBackground(mValue);
    final SupremicaPropertyChangeListener Listener =
      new BackgroundListener(comp);
    addPropertyChangeListener(Listener);
    return Listener;
  }


  //#########################################################################
  //# Inner Class BackgroundListener
  /**
   * A listener to update the background colour of a component to match
   * the value of a colour property.
   */
  private class BackgroundListener
    implements SupremicaPropertyChangeListener
  {
    //#######################################################################
    //# Constructor
    private BackgroundListener(final Component comp)
    {
      mComponent = comp;
    }

    //#######################################################################
    //# Interface org.supremica.properties.PropertyChangeListener
    @Override
    public void propertyChanged(final SupremicaPropertyChangeEvent event)
    {
      mComponent.setBackground(mValue);
    }

    //#######################################################################
    //# Data Members
    private final Component mComponent;
  }


  //#########################################################################
  //# Data Members
  private final Color mDefaultValue;

  private Color mValue;

}
