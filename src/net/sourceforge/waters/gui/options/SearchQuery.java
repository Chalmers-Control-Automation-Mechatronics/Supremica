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

package net.sourceforge.waters.gui.options;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 *
 * @author Benjamin Wheeler
 */
public class SearchQuery
{

  public void setPattern(final String regex) throws PatternSyntaxException {
    mRegex = regex;
    mPattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    startNewSearch();
  }

  public boolean matches(final String value) {
    return mPattern.matcher(value).find();
  }

  public OptionPanel<?> getLastMatched() {
    return mLastMatched;
  }

  public void addResult(final OptionPanel<?> result) {
    mResults.add(result);
  }

  public OptionPanel<?> getResult() {
    mNewSearch = false;
    if (mResults == null) return null;
    if (mResultIndex < 0) {
      mResults = mResults.stream().distinct().collect(Collectors.toList());
      mResultIndex = 0;
    }
    if (mResults.size() == 0) return null;
    if (mResultIndex == mResults.size()) mResultIndex = 0;
    mLastMatched = mResults.get(mResultIndex);
    mResultIndex++;
    return mLastMatched;
  }

  public void startNewSearch() {
    mNewSearch = true;
    mResultIndex = -1;
    mResults = new LinkedList<>();
  }

  public boolean isNewSearch() {
    return mNewSearch;
  }

  public String getRegex() {
    return mRegex;
  }

  private String mRegex;
  private Pattern mPattern;
  private OptionPanel<?> mLastMatched;
  private List<OptionPanel<?>> mResults;
  private int mResultIndex = -1;
  private boolean mNewSearch = true;

}
