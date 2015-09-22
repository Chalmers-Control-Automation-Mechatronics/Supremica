//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.external.promela.ast;

import net.sourceforge.waters.external.promela.PromelaType;
import net.sourceforge.waters.external.promela.PromelaVisitor;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;

public class VardefTreeNode extends PromelaTree
{
  public VardefTreeNode(final int token, final boolean visible, final PromelaType type)
  {
    this((Token)new CommonToken(token,"Vardefinition"));
    mVisible = visible;
    mVariableType = type;
  }

  public VardefTreeNode(final Token token)
  {
    super(token);
    mChanState = token.getText();
  }

  public String toString(){
    return "Vardefinition";
  }

  public String getValue()
  {
    return mChanState;
  }

  public Object acceptVisitor(final PromelaVisitor visitor)
  {
    return  visitor.visitVar(this);
  }

  /**
   * A method to get the visibility of the variable(s) that are being created
   * @author Ethan Duff
   * @return TRUE if the variable should be included in the system space or FALSE otherwise
   */
  public boolean isVisible()
  {
    return mVisible;
  }

  /**
   * A method to get the type of the variable(s) that are being created
   * @author Ethan Duff
   * @return A promela type matching this variable
   */
  public PromelaType getVariableType()
  {
    return mVariableType;
  }

  /**
   * A method to get the locality of the variable(s) being created
   * @author Ethan Duff
   * @return TRUE if the variable(s) are global, or FALSE if they are local to a method
   */
  public boolean isGlobal()
  {
    return mGlobal;
  }

  private final String mChanState;
  private boolean mVisible;
  private PromelaType mVariableType;
  private boolean mGlobal;
}








