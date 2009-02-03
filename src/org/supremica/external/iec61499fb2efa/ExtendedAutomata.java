/*
 *   Copyright (C) 2008 Goran Cengic
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 3 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

/*
 * @author Goran Cengic (cengic@chalmers.se)
 */

package org.supremica.external.iec61499fb2efa;

import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.JAXBException;

import java.util.Iterator;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

import java.io.Reader;
import java.io.StringReader;
import java_cup.runtime.Scanner;

import net.sourceforge.fuber.model.Variables;
import net.sourceforge.fuber.model.IntegerVariable;

import net.sourceforge.fuber.model.interpreters.Finder;
import net.sourceforge.fuber.model.interpreters.Evaluator;
import net.sourceforge.fuber.model.interpreters.Printer;
import net.sourceforge.fuber.model.interpreters.efa.Lexer;
import net.sourceforge.fuber.model.interpreters.efa.Parser;
import net.sourceforge.fuber.model.interpreters.abstractsyntax.Goal;
import net.sourceforge.fuber.model.interpreters.abstractsyntax.StatementList;
import net.sourceforge.fuber.model.interpreters.abstractsyntax.Expression;
import net.sourceforge.fuber.model.interpreters.abstractsyntax.Identifier;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.expr.TypeMismatchException;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.module.*;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.module.*;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.module.ScopeKind;


class ExtendedAutomata
{

	private ModuleSubjectFactory factory;
	private IdentifierSubject identifier;
	private ExpressionParser parser;
	private ModuleSubject module;
	private boolean expand;

	ExtendedAutomata(String name, boolean expand) 
	{
		factory = ModuleSubjectFactory.getInstance();

		identifier = factory.createSimpleIdentifierProxy(name);
		module = new ModuleSubject(name, null);

		// make marking proposition
        final SimpleIdentifierProxy ident = factory.createSimpleIdentifierProxy
            (EventDeclProxy.DEFAULT_MARKING_NAME);
		module.getEventDeclListModifiable().add
            (factory.createEventDeclProxy(ident, EventKind.PROPOSITION));

		this.expand = expand;

		parser = new ExpressionParser(factory, CompilerOperatorTable.getInstance());
	}

	protected ModuleSubject getModule()
	{
		return module;
	}

    void addEvent(final String name)
	{
		addEvent(name,"uo");
	}
	
    void addEvent(final String name, final String kind)
	{
        if (!containsEvent(name)) {
			final SimpleIdentifierProxy ident =
				factory.createSimpleIdentifierProxy(name);
			if (kind.equals("co")) {
				module.getEventDeclListModifiable().add
					(factory.createEventDeclProxy(ident,EventKind.CONTROLLABLE,true,ScopeKind.LOCAL,null,null));
			} 
			else if (kind.equals("uo")) 
			{
				module.getEventDeclListModifiable().add
					(factory.createEventDeclProxy(ident,EventKind.UNCONTROLLABLE,true,ScopeKind.LOCAL,null,null));
			}
			else if (kind.equals("cu")) 
			{
				module.getEventDeclListModifiable().add
					(factory.createEventDeclProxy(ident,EventKind.CONTROLLABLE,false,ScopeKind.LOCAL,null,null));
			}
			else if (kind.equals("uu")) 
			{
				module.getEventDeclListModifiable().add
					(factory.createEventDeclProxy(ident,EventKind.UNCONTROLLABLE,false,ScopeKind.LOCAL,null,null));
			}
		}
	}

    boolean containsEvent(final String name)
    {
        for (final EventDeclProxy decl : module.getEventDeclList()) {
            if (decl.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    void addAutomaton(ExtendedAutomaton automaton)
	{
		module.getComponentListModifiable().add(automaton.getComponent());
	}

    void writeToFile(File file)
	{
		
		if (expand)
		{
			ExtendedAutomataExpander.expandTransitions(module);
		}
		
		Logger.output("ExtendedAutomata.writeToFile(): Writing model file: " + file.getName());
		
		try
		{
			JAXBModuleMarshaller marshaller = new JAXBModuleMarshaller(factory, CompilerOperatorTable.getInstance());	
			marshaller.marshal(module, file);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
