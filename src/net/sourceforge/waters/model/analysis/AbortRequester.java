//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.model.analysis;

/**
 * <P>Interface to characterise threads or objects that request an
 * analysis operation to abort. This interface provides a method to create
 * an exception that can be thrown in response to the abort which may
 * include information about how the abort was triggered.</P>
 *
 * <P>Typical implementations of this interface will return a
 * {@link UserAbortException} to indicate an abort by user request
 * or an {@link OverflowException} to indicate a timeout, typically using a
 * constructor call <CODE>new {@link
 * OverflowException#OverflowException(OverflowKind, int)
 * OverflowException}({@link OverflowKind}.{@link OverflowKind#TIME TIME},
 * &lt;<I>seconds</I>&gt;)</CODE>.</P>
 *
 * <P>The static variable {@link #USER} is provided for convenience, to
 * specify a user-requested abort.</P>
 *
 * @author Robi Malik
 */

public interface AbortRequester
{
  /**
   * Creates an exception to be thrown in response to an abort request.
   */
  public AnalysisAbortException createAbortException();

  /**
   * An object that implements the {@link AbortRequester} interface by
   * return a {@link UserAbortException}.
   */
  public static AbortRequester USER = new AbortRequester()
  {
    @Override
    public UserAbortException createAbortException()
    {
      return new UserAbortException();
    }

  };
}
