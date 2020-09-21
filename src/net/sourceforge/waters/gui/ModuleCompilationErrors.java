//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.gui;

import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.gui.language.ProxyNamer;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.NestedBlockProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.subject.base.ArrayListSubject;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.base.SubjectTools;
import net.sourceforge.waters.subject.module.EventDeclSubject;


public class ModuleCompilationErrors
{
  public ModuleCompilationErrors()
  {
    mErrors = new HashMap<>();
    mErrorCounts = new TObjectIntHashMap<>();
    mUnderlines = new THashSet<>();
  }

  /**
   * Returns <CODE>true</CODE> if the location should have an error icon. An
   * error icon should be displayed if there are errors at the location or any
   * of its descendants.
   *
   * @param location    The location to check.
   * @return <CODE>true</CODE> if an error icon should be displayed.
   */
  public boolean hasErrorIcon(final Proxy location)
  {
    return mErrorCounts.containsKey(location);
  }

  /**
   * Returns <CODE>true</CODE> if the location should be underlined.
   *
   * @param location    The location to check.
   * @return <CODE>true</CODE> if the location should be underlined.
   */
  public boolean isUnderlined(final Proxy location)
  {
    return mUnderlines.contains(location);
  }

  /**
   * Creates a string representation of all the errors at the location.
   *
   * @param location    The location for which to get the error message.
   * @return The string, or <CODE>null</CODE> if there are no errors.
   */
  public String getDetailedMessage(final Proxy location)
  {
    final List<EvalException> errors = mErrors.get(location);
    if (errors == null) {
      return null;
    }
    final StringBuilder sb = new StringBuilder();
    sb.append("<HTML>");
    for (final EvalException error : errors) {
      final String message = error.getMessage() + "\n";
      final String escaped = HTMLPrinter.encodeInHTML(message);
      sb.append(escaped);
    }
    sb.append("</HTML>");
    return sb.toString();
  }

  /**
   * Creates a summary of all the errors at the location and its descendants.
   *
   * @param location    The location for which to get the summary.
   * @return The summary, or <CODE>null</CODE> if there are no errors.
   */
  public String getSummaryMessage(final Proxy location)
  {
    final int count = mErrorCounts.get(location);
    if (count == 0) {
      return null;
    }
    final StringBuilder sb = new StringBuilder();
    sb.append(count);
    sb.append(" error");
    if (count != 1) {
      sb.append("s");
    }
    sb.append(" in ");
    sb.append(ProxyNamer.getItemClassName(location));
    return sb.toString();
  }

  /**
   * Records a compilation error.
   * @param error   The <CODE>EvalException</CODE> that represents the
   *                compilation error.
   */
  public void add(final EvalException error)
  {
    if (error.getLocation() instanceof ProxySubject) {
      ProxySubject location = (ProxySubject) error.getLocation();
      ProxySubject child = location;
      // Move out of simple expressions
      while (location instanceof SimpleExpressionProxy) {
        child = location;
        location = SubjectTools.getProxyParent(location);
      }
      // Children of lists get individual tooltips and underlines
      if (child.getParent() instanceof ArrayListSubject &&
          !(location instanceof EventDeclSubject)) {
        location = child;
      }
      List<EvalException> errors = mErrors.get(location);
      if (errors == null) {
        errors = new ArrayList<>();
        mErrors.put(location, errors);
      }
      errors.add(error);
      // Condition and foreach blocks get individual underlines
      if (location instanceof NestedBlockProxy) {
        location = child;
      }
      mUnderlines.add(location);
      // Increment error counts of ancestors
      while (location != null) {
        mErrorCounts.adjustOrPutValue(location, 1, 1);
        location = SubjectTools.getProxyParent(location);
      }
    }
  }

  /**
   * Returns all locations that have error icons.
   *
   * @return A <CODE>Set</CODE> containing all the locations.
   */
  public Set<ProxySubject> getAllLocations()
  {
    return mErrorCounts.keySet();
  }

  //#########################################################################
  //# Data Members
  private final Map<ProxySubject, List<EvalException>> mErrors;
  private final TObjectIntHashMap<ProxySubject> mErrorCounts;
  private final Set<ProxySubject> mUnderlines;

  //#########################################################################
  //# Class Constants
  public static final ModuleCompilationErrors NONE =
    new ModuleCompilationErrors();

}
