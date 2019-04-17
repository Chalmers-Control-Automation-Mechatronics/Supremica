package net.sourceforge.waters.gui.options;

import java.awt.Component;


/**
 * Abstract class for parameters, stores the default IDs.
 *
 * @author Brandon Bassett
 */

public abstract class Parameter
{

  @SuppressWarnings("unused")
  private final int mID;
  private final String mName;
  private final String mDescription;

  public Parameter(final int id, final String name)
  {
    this(id, name, null);
  }

  public Parameter(final int id, final String name, final String description)
  {
    mID = id;
    mName = name;
    mDescription = description;
  }

  public String getName()
  {
    return mName;
  }

  public String getDescription()
  {
    return mDescription;
  }

  public abstract Component getComponent();

}
