//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

package net.sourceforge.waters.analysis.distributed.application;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

/**
 * A mapping of serialisable attributes to specify information about
 * jobs.
 * @author Sam Douglas
 */
class BaseJobData implements Serializable, JobData
{
  /**
   * Uses an existing map of attributes. This map will not be copied.
   * @param attributes map to use.
   */
  protected BaseJobData(Map<String,Object> attributes)
  {
    mAttributes = attributes;
  }

  /**
   * Copy constructor. Copies the attribute map from another
   * job data object. The immutable flag is not copied, that is to
   * say the new object will be mutable.
   * @param other job data to copy attributes from.
   */
  protected BaseJobData(BaseJobData other)
  {
    this(new HashMap<String,Object>(other.mAttributes));
  }

  /**
   * Creates an empty attribute mapping.
   */
  public BaseJobData()
  {
    this(new HashMap<String,Object>());
  }


  public <T extends Serializable>
	    void set(String attr, T obj) 
    throws IllegalStateException,
    IllegalArgumentException
  {
    if (isImmutable())
      throw new IllegalStateException(IMMUTABLE_MESSAGE);

    if (obj != null && !(Serializable.class.isAssignableFrom(obj.getClass())))
      throw new IllegalArgumentException("Attribute value for '" + attr 
					 + "' was not serializable");

    mAttributes.put(attr, obj);
  }


  public void merge(JobData other)
  {
    if (isImmutable())
      throw new IllegalStateException(IMMUTABLE_MESSAGE);

    if (other == null)
      throw new NullPointerException("Other job data was null");

    if (this == other)
      throw new IllegalArgumentException("Cannot merge with self");

    for (Map.Entry<String,Object> entry : other.getAttributeMap().entrySet())
      {
	if (!mAttributes.containsKey(entry.getKey()))
	  mAttributes.put(entry.getKey(), entry.getValue());
      }
  }


  public Object get(String attr)
  {
    return mAttributes.get(attr);
  }


  public boolean contains(String attr)
  {
    return mAttributes.containsKey(attr);
  }


  public boolean isImmutable()
  {
    return mImmutable;
  }


  public void setImmutable()
  {
    mImmutable = true;
  }

  public Map<String,Object> getAttributeMap()
  {
    return Collections.unmodifiableMap(mAttributes);
  }

  public JobData getJobData()
  {
    return this;
  }

  private boolean mImmutable = false;
  private final Map<String,Object> mAttributes;

  private static final long serialVersionUID = 1L;
  private static final String IMMUTABLE_MESSAGE = "Cannot modify immutable job data";
}
