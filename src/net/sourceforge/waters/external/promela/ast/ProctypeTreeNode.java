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


import net.sourceforge.waters.external.promela.PromelaVisitor;
import net.sourceforge.waters.external.promela.SymbolTable;


import org.antlr.runtime.*;

public class ProctypeTreeNode extends PromelaTree
{
	public ProctypeTreeNode(final Token token){
		super(token);
		mProc = token.getText();
	}
	public String toString(){
		return "Proctype";
	}
	private final String mProc;
	public String getValue()
	{
		return mProc;
	}
    public Object acceptVisitor(final PromelaVisitor visitor)
    {
        return  visitor.visitProcType(this);
    }

    private SymbolTable mSymbolTable;//A pointer to the symbol table

    /**
     * A method to get the symbol table used for this current ProcTypeTreeNode.
     * It contains the variables etc. that are local to this scope, and is link up to higher scopes for global and broader range local variables, channels, etc.
     * @author Ethan Duff
     * @return The Symbol table for this ProcTypeTreeNode
     */
    public SymbolTable getSymbolTable()
    {
      return mSymbolTable;
    }

    /**
     * A method to set the symbol table for this ProcTypeTreeNode
     * @author Ethan Duff
     * @param table The table that has been created to for storing information relative to the scope of this ProcTypeTreeNode
     */
    public void setSymbolTable(final SymbolTable table)
    {
      mSymbolTable = table;
    }
}








