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
import java.util.Map;

/**
 * An interface to objects providing job (or result) data.
 */
public interface JobData
{
  /**
   * Sets an attribute in the mapping. If the attribute already exists
   * then it will be remapped to this new value. All objects stored in
   * the job must be Serializable. This is defined as a generic method
   * to try and ensure this, but in some situations it may be
   * necessary to cast to Serializable, or a serializable type. If the
   * object does not appear to be serializable, then an illegal
   * argument exception will be thrown. If this method is called after
   * this job data object is set as immutable, then an illegal state
   * exception will be thrown.
   * @param attr the attribute name to use.
   * @param obj a Serializable object.
   * @throws IllegalArgumentException if the object is not
   *                                  serializable.
   * @throws IllegalStateException if this method is called on
   *                               immutable job data.
   */
  public <T extends Serializable> void set(String attr, T obj);

  /**
   * Merges attributes from another job data object into this job
   * data. Attributes that already exist in this job will be
   * unchanged.  Specifying a null value, or this object for the
   * 'other' parameter is illegal and will cause exceptions. Calling
   * this method when the immutable flag is set (on this object) will
   * result in an illegal state exception being thrown.
   * @param other the other job data to copy attributes from
   * @throws NullPointerException if other is null
   * @throws IllegalArgumentException if this object is passed as the
   *                                  other parameter.
   * @throws IllegalStateException if this object is flagged as
   *                               immutable.
   */
  public void merge(JobData other);

  /**
   * Gets an attribute from this job data. If the attribute does not
   * exist then null will be returned.
   * @param attr the attribute to get.
   * @return the attribute value, or null if doesn't exist.
   */
  public Object get(String attr);

  /**
   * Checks if an attribute exists in this job data.
   * @param attr attribute name to check.
   * @return true if the attribute exists.
   */
  public boolean contains(String attr);

  /**
   * Checks if this object allows the attribute mapping to change.
   * @return true if the object is immutable.
   */
  public boolean isImmutable();

  /**
   * Sets this object as being immutable. Once the object is
   * immutable, the only way to modify this mapping is to copy it into
   * a new object, using a copy constructor or attribute merge.
   */
  public void setImmutable();

  /**
   * Gets an unmodifiable view of the job data's attribute mapping.
   * @return an unmodifiable map of the jobs attributes.
   */
  public Map<String,Object> getAttributeMap();

  /**
   * Gets the job data object. Actual job data objects should return
   * themselves, decorators should return the underlying job
   * data. This provides closure over job data objects, and saves
   * having a sub-interface for decorators. A bit of a hack perhaps.
   * @return the job data object.
   */
  public JobData getJobData();
}
