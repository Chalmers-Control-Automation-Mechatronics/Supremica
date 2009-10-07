//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
/*
 *  Supremica Software License Agreement
 *
 *  The Supremica software is not in the public domain
 *  However, it is freely available without fee for education,
 *  research, and non-profit purposes.  By obtaining copies of
 *  this and other files that comprise the Supremica software,
 *  you, the Licensee, agree to abide by the following
 *  conditions and understandings with respect to the
 *  copyrighted software:
 *
 *  The software is copyrighted in the name of Supremica,
 *  and ownership of the software remains with Supremica.
 *
 *  Permission to use, copy, and modify this software and its
 *  documentation for education, research, and non-profit
 *  purposes is hereby granted to Licensee, provided that the
 *  copyright notice, the original author's names and unit
 *  identification, and this permission notice appear on all
 *  such copies, and that no charge be made for such copies.
 *  Any entity desiring permission to incorporate this software
 *  into commercial products or to use it for commercial
 *  purposes should contact:
 *
 *  Knut Akesson (KA), knut@supremica.org
 *  Supremica,
 *  Haradsgatan 26A
 *  431 42 Molndal
 *  SWEDEN
 *
 *  to discuss license terms. No cost evaluation licenses are
 *  available.
 *
 *  Licensee may not use the name, logo, or any other symbol
 *  of Supremica nor the names of any of its employees nor
 *  any adaptation thereof in advertising or publicity
 *  pertaining to the software without specific prior written
 *  approval of the Supremica.
 *
 *  SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 *  SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 *  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 *  Supremica or KA shall not be liable for any damages
 *  suffered by Licensee from the use of this software.
 *
 *  Supremica is owned and represented by KA.
 */

/*
 * @author Goran Cengic (cengic@chalmers.se)
 */

package org.supremica.automata;

import java.io.File;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.module.*;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.module.*;
import net.sourceforge.waters.xsd.base.EventKind;

public class ExtendedAutomata implements Iterable<ExtendedAutomaton>
{

	private ModuleSubjectFactory factory;
	private IdentifierSubject identifier;
	private ExpressionParser parser;
	private ModuleSubject module;
	private boolean expand;
    private int nbrOfExAutomata = 0;
    private ArrayList<ExtendedAutomaton> theExAutomata;
    private Map<String, EventDeclProxy> eventIdToProxyMap;
    List<EventDeclProxy> unionAlphabet;
    List<VariableComponentProxy> variables;
    int maxRangeOfVars = 0;
    HashMap<String,Integer> var2initValMap;
    boolean contradictingInitValues = false;

    public ExtendedAutomata()
    {
        factory = ModuleSubjectFactory.getInstance();
        theExAutomata = new ArrayList<ExtendedAutomaton>();
        unionAlphabet = new ArrayList<EventDeclProxy>();
        variables = new ArrayList<VariableComponentProxy>();
        var2initValMap = new HashMap<String, Integer>();
        contradictingInitValues = false;
    }

	public ExtendedAutomata(ModuleSubject module)
    {
        this();
        this.module = module;
        for(EventDeclProxy e: module.getEventDeclList())
        {
            if(e.getKind() != EventKind.PROPOSITION)
                unionAlphabet.add(e);
        }
        eventIdToProxyMap = new HashMap<String, EventDeclProxy>();
        for(EventDeclProxy e:module.getEventDeclList())
        {
            eventIdToProxyMap.put(e.getName(), e);
        }

        for(AbstractSubject sub:module.getComponentListModifiable())
        {
            if(sub instanceof VariableComponentProxy)
            {
                variables.add((VariableComponentProxy)sub);
            }

            if(sub instanceof SimpleComponentSubject)
            {
                nbrOfExAutomata++;
                addAutomatonToList(new ExtendedAutomaton(this, (SimpleComponentSubject)sub));
            }
        }

        for(VariableComponentProxy var:variables)
        {
            StringTokenizer token = new StringTokenizer(var.getInitialStatePredicate().toString(),"==");
            token.nextToken();
            int initialValue = Integer.parseInt(token.nextToken());
            if(!var2initValMap.containsKey(var.getName()))
            {
                var2initValMap.put(var.getName(), initialValue);
            }
            else if(var2initValMap.get(var.getName()) != initialValue)
            {
                contradictingInitValues = true;
            }

            token = new StringTokenizer(var.getType().toString(), "..");
            int lowerBound = Integer.parseInt(token.nextToken());
            int upperBound = -1;
            upperBound = Integer.parseInt(token.nextToken());
            
            if((upperBound-lowerBound+1)>maxRangeOfVars)
            {
                maxRangeOfVars = upperBound-lowerBound+1;
            }            
        }

  		parser = new ExpressionParser(factory, CompilerOperatorTable.getInstance());
    }

	public ExtendedAutomata(String name, boolean expand) 
	{
        this();
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

    public boolean isInitValuesContradicting()
    {
        return contradictingInitValues;
    }

    public int getMaxRangeOfVars()
    {
        return maxRangeOfVars;
    }

    public int getInitValueofVar(String var)
    {
        return var2initValMap.get(var);
    }

    public int size()
    {
        return nbrOfExAutomata;
    }

    public List<VariableComponentProxy> getVars()
    {
        return variables;
    }

    public List<EventDeclProxy> getInverseAlphabet(ExtendedAutomaton exAut)
    {
        List<EventDeclProxy> events = getUnionAlphabet();
        List<EventDeclProxy> invAlph = new ArrayList<EventDeclProxy>();
        for(EventDeclProxy e:events)
        {
            if(!exAut.getAlphabet().contains(e))
                invAlph.add(e);
        }

        return invAlph;
    }

	protected ModuleSubject getModule()
	{
		return module;
	}

    public EventDeclProxy eventIdToProxy(String id)
    {
        return eventIdToProxyMap.get(id);
    }

    public List<EventDeclProxy> getUnionAlphabet()
    {
        return unionAlphabet;
    }

	public void addEvent(String name)
	{
		addEvent(name,"controllable");
	}
    

    public Iterator<ExtendedAutomaton> iterator()
    {
        return theExAutomata.iterator();
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


	public void addAutomaton(ExtendedAutomaton exAutomaton)
	{
        addAutomatonToList(exAutomaton);
		module.getComponentListModifiable().add(exAutomaton.getComponent());
	}

    public void addAutomatonToList(ExtendedAutomaton exAutomaton)
    {
        theExAutomata.add(exAutomaton);
    }

    public ArrayList<AbstractSubject> getComponents()
    {
        ArrayList<AbstractSubject> components = new ArrayList<AbstractSubject>();

        for(ExtendedAutomaton exAut:theExAutomata)
            components.add(exAut.getComponent());
        
        return components;
    }

	public void writeToFile(File file)
	{

		if (expand)
		{
			ExtendedAutomataExpander.expandTransitions(module);
		}

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
