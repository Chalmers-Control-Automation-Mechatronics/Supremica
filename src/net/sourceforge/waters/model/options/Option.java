//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.model.options;


import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.expr.ParseException;

import org.apache.logging.log4j.LogManager;


/**
 * <P>A configurable parameter of a model analyser or other
 * {@link Configurable}.</P>
 *
 * <P>An option holds a value of specific type as determined by
 * the type parameter <CODE>T</CODE>, and there are subclasses for each
 * specific parameter type.</P>
 *
 * <P>Every option has a default value and a current value of its type.
 * Options are identified by an ID string to facilitate storing in
 * configuration files. They are accompanied by descriptive information
 * that can be presented to users to explain the option.</P>
 *
 * <P>The method {@link #createEditor(OptionContext) createEditor()} can be
 * used to obtain a user interface to edit the option. This is further
 * parametrised using an {@link OptionContext} so that different editors
 * can be requested depending on context (e.g. GUI or command line).</P>
 *
 * @author Brandon Bassett, Robi Malik
 */
public abstract class Option<T> implements Cloneable
{

  //#########################################################################
  //# Constructors
  protected Option(final String id,
                   final String shortName,
                   final String description,
                   final String commandLineOption,
                   final T defaultValue)
  {
    this(id, shortName, description, commandLineOption,
         defaultValue, defaultValue);
  }

  protected Option(final String id,
                   final String shortName,
                   final String description,
                   final String commandLineOption,
                   final T defaultValue,
                   final T value)
  {
    mID = id;
    mShortName = shortName;
    mDescription = description;
    mCommandLineOption = commandLineOption;
    mDefaultValue = defaultValue;
    mValue = value;
  }

  protected Option(final Option<T> template, final T defaultValue)
  {
    mID = template.mID;
    mShortName = template.mShortName;
    mDescription = template.mDescription;
    mCommandLineOption = template.mCommandLineOption;
    mDefaultValue = mValue = defaultValue;
  }


  //#########################################################################
  //# Interface java.lang.Cloneable
  @Override
  public Option<T> clone()
  {
    try {
      @SuppressWarnings("unchecked")
      final Option<T> cloned = (Option<T>) super.clone();
      cloned.mListeners = null;
      return cloned;
    } catch (final CloneNotSupportedException exception) {
      throw new WatersRuntimeException(exception);
    }
  }

  public Option<T> clone(final ChainedAnalyzerOptionPage newParent)
  {
    return clone();
  }


  //#########################################################################
  //# Simple Access
  /**
   * Gets the ID of this option.
   * The naming convention for options is
   * &quot;&lt;<I>prefix</I>&gt;.&lt;<I>name</I>&gt;&quot;,
   * where the <I>prefix</I> corresponds to the name of the class where
   * the option is defined, and the <I>name</I> corresponds to the methods
   * used to get and set the option value in that class.
   * @return A string that uniquely identifies this option.
   */
  public String getID()
  {
    return mID;
  }

  /**
   * Checks whether the ID of this option matches the given string.
   * @param  id  The ID to compare against.
   * @return <CODE>true</CODE> if the option's ID is equal to the
   *         argument, <CODE>false</CODE> otherwise.
   */
  public boolean hasID(final String id)
  {
    return mID.equals(id);
  }

  /**
   * Gets the short name of this option.
   * @return A short string identifying the option, typically used as
   *         a title or in GUI labels.
   */
  public String getShortName()
  {
    return mShortName;
  }

  /**
   * Gets the description of this option.
   * @return A longer string that describes the option, which may consist
   *         of one more sentences. It is typically used to explain the
   *         option more detail, e.g., as a tooltip.
   */
  public String getDescription()
  {
    return mDescription;
  }

  /**
   * Gets an abbreviated option name.
   * @return A single word string, starting with a dash
   *         (&quot;<CODE>-</CODE>&quot;), which can be used to set
   *         the option through a command line.
   */
  public String getCommandLineCode()
  {
    return mCommandLineOption;
  }

  public boolean isPersistent()
  {
    return true;
  }

  public boolean isEditable()
  {
    return mEditable;
  }

  public void setEditable(final boolean editable)
  {
    mEditable = editable;
  }


  //#########################################################################
  //# Value Access
  /**
   * Gets the default value of this option.
   * Every option has a hard-coded default value, which cannot be changed.
   * @see #getValue()
   */
  public T getDefaultValue()
  {
    return mDefaultValue;
  }

  /**
   * Gets the current value of this option.
   * Initially, every option's value is equal to its default value,
   * but it can be changed by calling {@link #setValue(Object) setValue()}.
   * @see #getDefaultValue()
   */
  public T getValue()
  {
    return mValue;
  }

  /**
   * Changes the current value of this option to the given new value.
   */
  public boolean setValue(final T value)
  {
    if (ProxyTools.equals(value, mValue)) {
      return false;
    } else {
      final String oldValue = getGuiName(mValue);
      mValue = value;
      firePropertyChanged(oldValue);
      return true;
    }
  }

  /**
   * Restores the current value of this option to its default value.
   */
  public void restoreDefaultValue()
  {
    setValue(mDefaultValue);
  }

  /**
   * Changes the current value of this option by parsing the given
   * string.
   * @throws ParseException to indicate the the text cannot be
   *         converted to an option value.
   */
  public abstract void set(String text) throws ParseException;

  /**
   * Gets the GUI representable name of the given option value, which is
   * to render combo boxes or similar components. The default
   * implementation calls {@link #toString()} while also displaying
   * <CODE>null</CODE> values as an empty string.
   */
  public String getGuiName(final T value)
  {
    if (value == null) {
      return "";
    } else {
      return value.toString();
    }
  }

  /**
   * Gets the raw name of the given option value, which can be
   * displayed in the console or in property files. The default
   * implementation simply calls {@link #toString()}, but this may be
   * overridden in subclasses that distinguish pretty GUI names from
   * console names.
   */
  public String getConsoleName(final T value)
  {
    return getGuiName(value);
  }


  //#########################################################################
  //# Event Handling
  public void addOptionChangeListener(final OptionChangeListener listener)
  {
    if (mListeners == null) {
      mListeners = new LinkedList<>();
    }
    mListeners.add(listener);
  }

  public void removeOptionChangeListener(final OptionChangeListener listener)
  {
    if (mListeners != null) {
      mListeners.remove(listener);
      if (mListeners.isEmpty()) {
        mListeners = null;
      }
    }
  }

  public void firePropertyChanged(final String oldValue)
  {
    if (mListeners != null) {
      final String newValue = getGuiName(mValue);
      final OptionChangeEvent event =
        new OptionChangeEvent(this, oldValue, newValue);
      final List<OptionChangeListener> copy =
        new ArrayList<>(mListeners);
      for (final OptionChangeListener listener : copy) {
        listener.optionChanged(event);
      }
    }
    OptionFileManager.savePropertiesLater();
  }


  //#########################################################################
  //# Loading and Saving
  public void load(final Properties properties, final String prefix)
  {
    final String key = prefix + "." + mID;
    final String value = properties.getProperty(key);
    if (value != null) {
      properties.remove(key);
      try {
        set(value);
      } catch (final ParseException exception) {
        LogManager.getLogger().warn(exception.getMessage());
      }
    }
  }

  public void save(final Writer writer,
                   final LeafOptionPage page,
                   final boolean saveAll)
    throws IOException
  {
    final String value = getConsoleName(mValue);
    final String defaultValue = getConsoleName(mDefaultValue);
    if (saveAll || !value.equals(defaultValue)) {
      writer.append("# ");
      final String pageComment = page.getShortDescription();
      if (pageComment != null) {
        writer.append(pageComment);
        writer.append(": ");
      }
      writer.append(getShortName());
      writer.append('\n');
      writer.append(page.getPrefix());
      writer.append('.');
      final String escapedKey = OptionFileManager.getEscapedString(getID());
      writer.append(escapedKey);
      final String escapedValue = OptionFileManager.getEscapedString(value);
      if (!escapedValue.isEmpty()) {
        writer.append(" ");
        writer.append(escapedValue);
      }
      writer.append("\n\n");
    }
  }


  //#########################################################################
  //# Editor
  /**
   * Creates an editor object to facilitate control of the option through
   * a user interface.
   * @param  context  The option context that defines the environment
   *                  in which editing happens. There are different option
   *                  context for command line or GUI.
   * @see OptionContext
   */
  public abstract OptionEditor<T> createEditor(OptionContext context);


  //#########################################################################
  //# Debugging
  @Override
  public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append(mID);
    builder.append(" \"");
    builder.append(mShortName);
    builder.append("\" = ");
    if (mValue == null) {
      builder.append(" null");
    } else {
      builder.append(mValue);
    }
    return builder.toString();
  }


  //#########################################################################
  //# Data Members
  private final String mID;
  private final String mShortName;
  private final String mDescription;
  private final String mCommandLineOption;
  private final T mDefaultValue;
  private T mValue;
  private boolean mEditable = true;
  private List<OptionChangeListener> mListeners;

}
