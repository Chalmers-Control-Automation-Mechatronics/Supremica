//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
/*
 *   Copyright (C) 2006 Goran Cengic
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


public class ExtendedAutomata
{

	private ModuleSubjectFactory factory;
	private IdentifierSubject identifier;
	private ExpressionParser parser;
	private ModuleSubject module;
	private boolean expand;

	public ExtendedAutomata(String name, boolean expand) 
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

	public void addEvent(String name)
	{
		addEvent(name,"controllable");
	}
	
	public void addEvent(String name, String kind)
	{
        final SimpleIdentifierProxy ident =
            factory.createSimpleIdentifierProxy(name);
		if (kind.equals("controllable")) {
			module.getEventDeclListModifiable().add
                (factory.createEventDeclProxy(ident, EventKind.CONTROLLABLE));
		} else if (kind.equals("uncontrollable")) {
			module.getEventDeclListModifiable().add
                (factory.createEventDeclProxy(ident,
                                              EventKind.UNCONTROLLABLE));
		}
	}

	public void addAutomaton(ExtendedAutomaton automaton)
	{
		module.getComponentListModifiable().add(automaton.getComponent());
	}


	public void writeToFile(File file)
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


    //######################################################################
    //# Event Declarations
    /**
     * Checks whether the underlying module contains an event with the
     * given name.
     */
    boolean containsEvent(final String name)
    {
        for (final EventDeclProxy decl : module.getEventDeclList()) {
            if (decl.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a controllable event with the given name to the underlying
     * module if not yet present.
     */
    void includeControllableEvent(final String name)
    {
        if (!containsEvent(name)) {
            final SimpleIdentifierProxy ident =
                factory.createSimpleIdentifierProxy(name);
            final EventDeclSubject decl =
                factory.createEventDeclProxy(ident, EventKind.CONTROLLABLE);
            module.getEventDeclListModifiable().add(decl);
        }
    }

}
