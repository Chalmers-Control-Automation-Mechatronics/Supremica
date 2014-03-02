//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   ModuleCompilationErrors
//###########################################################################
//# $Id$
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
import net.sourceforge.waters.model.module.ForeachProxy;
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
      if (child.getParent() instanceof ArrayListSubject
          && !(location instanceof EventDeclSubject)) {
        location = child;
      }
      List<EvalException> errors = mErrors.get(location);
      if (errors == null) {
        errors = new ArrayList<EvalException>();
        mErrors.put(location, errors);
      }
      errors.add(error);
      // Range and guard of foreach get individual underlines
      if (location instanceof ForeachProxy) {
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
