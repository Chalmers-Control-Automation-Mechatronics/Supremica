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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.VariableComponentSubject;
import net.sourceforge.waters.xsd.base.EventKind;


public class ExtendedAutomata implements Iterable<ExtendedAutomaton>
{
    private final ModuleSubjectFactory factory;
    private ModuleSubject module;
    private boolean expand;

    private int nbrOfExAutomata = 0;
    private final ArrayList<ExtendedAutomaton> theExAutomata;
    private Map<String, EventDeclProxy> eventIdToProxyMap;
    public List<EventDeclProxy> unionAlphabet;
    List<VariableComponentProxy> variables;
    int domain = 0;
    Map<String, MinMax> var2MinMaxValMap = null;

    HashSet<EventDeclProxy> uncontrollableAlphabet = null;
    HashSet<EventDeclProxy> plantAlphabet = null;
    String locaVarSuffix = ".curr";

    Map<VariableComponentProxy,List<VariableComponentProxy>> var2relatedVarsMap = null;

    public ExtendedAutomata()
    {
        factory = ModuleSubjectFactory.getInstance();
        theExAutomata = new ArrayList<ExtendedAutomaton>();
        unionAlphabet = new ArrayList<EventDeclProxy>();
        variables = new ArrayList<VariableComponentProxy>();
        var2MinMaxValMap = new HashMap<String, MinMax>();
        uncontrollableAlphabet = new HashSet<EventDeclProxy>();
        plantAlphabet = new HashSet<EventDeclProxy>();
    }

    public ExtendedAutomata(final ModuleSubject module)
    {
        this();

        this.module = module;

        for(final EventDeclProxy e: module.getEventDeclList())
        {
            if(e.getKind() != EventKind.PROPOSITION)
            {
                unionAlphabet.add(e);
                if(e.getKind() == EventKind.UNCONTROLLABLE)
                {
                    uncontrollableAlphabet.add(e);
                }
            }
        }
        eventIdToProxyMap = new HashMap<String, EventDeclProxy>();
        for(final EventDeclProxy e:module.getEventDeclList())
        {
            eventIdToProxyMap.put(e.getName(), e);
        }

        var2relatedVarsMap = new HashMap<VariableComponentProxy, List<VariableComponentProxy>>();

        for(final AbstractSubject sub:module.getComponentListModifiable())
        {
            if(sub instanceof VariableComponentProxy)
            {
                final VariableComponentProxy var = (VariableComponentProxy)sub;
                var2relatedVarsMap.put(var, new ArrayList<VariableComponentProxy>());
                if(!sub.toString().contains(locaVarSuffix))
                    variables.add(((VariableComponentProxy)sub));

                final String varName = var.getName();
                final String range = var.getType().toString();
                int lowerBound = -1;
                int upperBound = -1;

                if(range.contains(CompilerOperatorTable.getInstance().getRangeOperator().getName()))
                {
                    lowerBound = Integer.parseInt(((BinaryExpressionProxy)var.getType()).getLeft().toString());
                    upperBound = Integer.parseInt(((BinaryExpressionProxy)var.getType()).getRight().toString());
                }
                else if (range.contains(","))
                {
                    final StringTokenizer token = new StringTokenizer(range, ", { }");
                    lowerBound = 0;
                    upperBound = token.countTokens();
                }

                final MinMax minMax = new MinMax(lowerBound,upperBound);

                if(!var2MinMaxValMap.containsKey(varName))
                {
                    var2MinMaxValMap.put(varName, minMax);
                }

    //            int currDomain = upperBound+1-lowerBound;
                final int currDomain = ((Math.abs(upperBound) >= Math.abs(lowerBound))?Math.abs(upperBound):Math.abs(lowerBound))+1;
                if(currDomain>domain)
                    domain = currDomain;


            }
        }

        //we multiply the domain with 2 to add 1 extra bit for the sign
        domain *= 2;

        for(final AbstractSubject sub:module.getComponentListModifiable())
        {
            if(sub instanceof SimpleComponentSubject)
            {
                nbrOfExAutomata++;
                final ExtendedAutomaton exAutomaton = new ExtendedAutomaton(this, (SimpleComponentSubject)sub);

                if(!exAutomaton.isSpecification())
                {
                    plantAlphabet.addAll(exAutomaton.getAlphabet());
                }
                theExAutomata.add(exAutomaton);
            }
        }

    }

    public List<VariableComponentProxy> getRelatedVars(final VariableComponentProxy var)
    {
        return var2relatedVarsMap.get(var);
    }

    public void addToRelatedVars(final VariableComponentProxy var, final List<VariableComponentProxy> vars)
    {
        var2relatedVarsMap.get(var).addAll(vars);
    }

    public ExtendedAutomata(final String name, final boolean expand)
    {
    this();
    factory.createSimpleIdentifierProxy(name);
            module = new ModuleSubject(name, null);

            // make marking proposition
    final SimpleIdentifierProxy ident = factory.createSimpleIdentifierProxy
        (EventDeclProxy.DEFAULT_MARKING_NAME);
            module.getEventDeclListModifiable().add
        (factory.createEventDeclProxy(ident, EventKind.PROPOSITION));

            this.expand = expand;

            new ExpressionParser(factory, CompilerOperatorTable.getInstance());
    }

    public List<ExtendedAutomaton> getExtendedAutomataList()
    {
        return theExAutomata;
    }

    public void extDomain(final int d)
    {
        domain = d;
    }

    public String getlocVarSuffix()
    {
        return locaVarSuffix;
    }

    public HashSet<EventDeclProxy> getUncontrollableAlphabet()
    {
        return uncontrollableAlphabet;
    }

    public int getDomain()
    {
        return domain;
    }

    public int getMaxValueofVar(final String var)
    {
        return var2MinMaxValMap.get(var).getMax();
    }

    public int getMinValueofVar(final String var)
    {
        return var2MinMaxValMap.get(var).getMin();
    }

    public int size()
    {
        return nbrOfExAutomata;
    }

    public VariableComponentProxy getVariableByName(final String varName)
    {
        for(final VariableComponentProxy var:variables)
            if(var.getName().equals(varName))
                return var;

        throw new IllegalArgumentException ("There does not exists a variable in the model with name "+varName+"!");
    }

    public List<VariableComponentProxy> getVars()
    {
        return variables;
    }

    public List<EventDeclProxy> getInverseAlphabet(final ExtendedAutomaton exAut)
    {
        final List<EventDeclProxy> events = getUnionAlphabet();
        final List<EventDeclProxy> invAlph = new ArrayList<EventDeclProxy>();
        for(final EventDeclProxy e:events)
        {
            if(!exAut.getAlphabet().contains(e))
                invAlph.add(e);
        }

        return invAlph;
    }

    public ModuleSubject getModule()
    {
       return module;
    }

    public void addIntegerVariable(final String name, final int lowerBound, final int upperBound, final int initialValue, final Integer markedValue)
    {
        module.getComponentListModifiable().add(VariableHelper.createIntegerVariable(name, lowerBound, upperBound, initialValue, null));
    }

    public void addEnumerationVariable(final String name,final Collection<String> elements,final String initialValue,final Collection<String> markedValues)
    {
        final VariableComponentSubject var = VariableHelper.createEnumerationVariable(name, elements, initialValue, markedValues);
        if(!module.getComponentListModifiable().contains(var))
            module.getComponentListModifiable().add(var);
    }

    public EventDeclProxy eventIdToProxy(final String id)
    {
        return eventIdToProxyMap.get(id);
    }

    public List<EventDeclProxy> getUnionAlphabet()
    {
        return unionAlphabet;
    }

	public void addEvent(final String name)
	{
		addEvent(name,"controllable");
	}


    public Iterator<ExtendedAutomaton> iterator()
    {
        return theExAutomata.iterator();
    }

    public void addEvent(final String name, final String kind)
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


    public void addAutomaton(final ExtendedAutomaton exAutomaton)
    {
        theExAutomata.add(exAutomaton);
        module.getComponentListModifiable().add(exAutomaton.getComponent());
    }

    public HashSet<EventDeclProxy> getPlantAlphabet()
    {
        return plantAlphabet;
    }

    public ArrayList<AbstractSubject> getComponents()
    {
        final ArrayList<AbstractSubject> components = new ArrayList<AbstractSubject>();

        for(final ExtendedAutomaton exAut:theExAutomata)
            components.add(exAut.getComponent());

        return components;
    }

	public void writeToFile(final File file)
	{

		if (expand)
		{
			ExtendedAutomataExpander.expandTransitions(module);
		}

		try
		{
			final JAXBModuleMarshaller marshaller = new JAXBModuleMarshaller(factory, CompilerOperatorTable.getInstance());
			marshaller.marshal(module, file);
		}
		catch (final Exception e)
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

    class MinMax
    {
        private int min;
        private int max;

        public MinMax()
        {

        }
        public MinMax(final int min, final int max)
        {
            this.min = min;
            this.max = max;
        }

        public void setMin(final int min)
        {
            this.min = min;
        }

        public void setMax(final int max)
        {
            this.max = max;
        }

        public int getMin()
        {
            return min;
        }

        public int getMax()
        {
            return max;
        }

    }

}