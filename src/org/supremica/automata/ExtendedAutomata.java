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
 * @author Sajed Miremadi (miremads@chalmers.se)
 * @author Mohammad Reza Shoaei (shoaei@chalmers.se)
 * @version %I%, %G%
 * @since 1.0
 */

package org.supremica.automata;

import java.io.File;
import java.util.*;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.module.*;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.module.*;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.module.ScopeKind;
import org.supremica.automata.BDD.EFA.ForcibleEventAttributeFactory;


public class ExtendedAutomata implements Iterable<ExtendedAutomaton>
{
    private final ModuleSubjectFactory factory;
    private ModuleSubject module;

    private int nbrOfExAutomata = 0;
    private final ArrayList<ExtendedAutomaton> theExAutomata;
    private Map<String, EventDeclProxy> eventIdToProxyMap;
    private final Map<ExtendedAutomaton,Integer> exAutomatonToIndex;
    private final Map<String, ExtendedAutomaton> stringToExAutomaton;
    private boolean negativeValuesIncluded = false;
    public List<EventDeclProxy> unionAlphabet;
    List<VariableComponentProxy> variables;
    private final List<VariableComponentProxy> clocks;
    int domain = 0;
    private int largestClockDomain = 0;
    private List<VariableComponentProxy> parameters = null;
    private Map<String, MinMax> var2MinMaxValMap = null;
    Map<String, Integer> var2domainMap = null;
    public HashSet<EventDeclProxy> uncontrollableAlphabet = null;
    public HashSet<EventDeclProxy> controllableAlphabet = null;
    public HashSet<EventDeclProxy> forcibleAlphabet = null;
    HashSet<EventDeclProxy> plantAlphabet = null;

    final static String LOCAL_VAR_SUFFIX = ".curr";
    final static String CLOCK_PREFIX = "clock:";
    final static String GLOBAL_PREFIX = "global:";
    final static String PARAM_PREFIX = "param:";
   /**
   * The name to be used for the default forcible property used for timed EFAs.
   */

    //Variable that are used when the input model is a Resource Allocation System
    private final List<VariableComponentProxy> stageVars;
    Map<VariableComponentProxy, List<VariableComponentProxy>> var2relatedVarsMap = null;
    public double theoNbrOfReachableStates = 0;
    public int nbrOfEFAsVars = 0;
    private boolean modelHasNoPlants = true;
    private boolean modelHasNoSpecs = true;

    public ExtendedAutomata() {
        factory = ModuleSubjectFactory.getInstance();
        theExAutomata = new ArrayList<ExtendedAutomaton>();
        unionAlphabet = new ArrayList<EventDeclProxy>();
        variables = new ArrayList<VariableComponentProxy>();
        parameters = new ArrayList<VariableComponentProxy>();
        stageVars = new ArrayList<VariableComponentProxy>();
        clocks = new ArrayList<VariableComponentProxy>();
        var2MinMaxValMap = new HashMap<String, MinMax>();
        var2domainMap = new HashMap<String, Integer>();
        uncontrollableAlphabet = new HashSet<EventDeclProxy>();
        controllableAlphabet = new HashSet<EventDeclProxy>();
        forcibleAlphabet = new HashSet<EventDeclProxy>();
        plantAlphabet = new HashSet<EventDeclProxy>();
        exAutomatonToIndex = new HashMap<ExtendedAutomaton, Integer>();
        stringToExAutomaton = new HashMap<String, ExtendedAutomaton>();
    }

    public ExtendedAutomata(final String name) {
        this();
        factory.createSimpleIdentifierProxy(name);
        module = new ModuleSubject(name, null);
        // make marking proposition
        final SimpleIdentifierProxy ident = factory.createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_MARKING_NAME);
        module.getEventDeclListModifiable().add(factory.createEventDeclProxy(ident, EventKind.PROPOSITION));
    }

    public ExtendedAutomata(final ModuleSubject module) {
        this(module, 0);
    }

    public ExtendedAutomata(final ModuleSubject module, final int globalClockDomain) {
        this();

        this.module = module;

        for (final EventDeclProxy e : module.getEventDeclList()) {
            if (e.getKind() != EventKind.PROPOSITION) {
                unionAlphabet.add(e);
                if (e.getKind() == EventKind.UNCONTROLLABLE) {
                    uncontrollableAlphabet.add(e);
                } else {
                    controllableAlphabet.add(e);
                }
                if (ForcibleEventAttributeFactory.isForcible(e.getAttributes())) {
                    forcibleAlphabet.add(e);
                }
            }
        }
        eventIdToProxyMap = new HashMap<String, EventDeclProxy>();
        for (final EventDeclProxy e : module.getEventDeclList()) {
            eventIdToProxyMap.put(e.getName(), e);
        }

        var2relatedVarsMap = new HashMap<VariableComponentProxy, List<VariableComponentProxy>>();

        final ArrayList<Proxy> components = new ArrayList<Proxy>(module.getComponentList());
        if (globalClockDomain > 0) {
            components.add(VariableHelper.createIntegerVariable(CLOCK_PREFIX + GLOBAL_PREFIX, 0, globalClockDomain, 0, null));
        }


        for (final Proxy sub : components) {
            if (sub instanceof VariableComponentProxy) {
                final VariableComponentProxy var = (VariableComponentProxy) sub;
                final String range = var.getType().toString();

                int lowerBound = 0;

                if (range.contains(CompilerOperatorTable.getInstance().getRangeOperator().getName())) {
                    lowerBound = Integer.parseInt(((BinaryExpressionProxy) var.getType()).getLeft().toString());
                }

                if (lowerBound < 0) {
                    negativeValuesIncluded = true;
                    break;
                }
            }
        }

        for (final Proxy sub : components) {
            if (sub instanceof VariableComponentProxy) {
                final VariableComponentProxy var = (VariableComponentProxy) sub;
                var2relatedVarsMap.put(var, new ArrayList<VariableComponentProxy>());
                if (!sub.toString().contains(LOCAL_VAR_SUFFIX)) {
                    variables.add(((VariableComponentProxy) sub));
                }

                if (var.getName().startsWith(FlowerEFABuilder.STAGE_PREFIX)) {
                    stageVars.add(var);
                }

                if (var.getName().startsWith(CLOCK_PREFIX)) {
                    clocks.add(var);
                }

                if (var.getName().startsWith(PARAM_PREFIX)) {
                    parameters.add(var);
                }

                final String varName = var.getName();
                final String range = var.getType().toString();
                int lowerBound = 0;
                int upperBound = 0;

                if (range.contains(CompilerOperatorTable.getInstance().getRangeOperator().getName())) {
                    lowerBound = Integer.parseInt(((BinaryExpressionProxy) var.getType()).getLeft().toString());
                    upperBound = Integer.parseInt(((BinaryExpressionProxy) var.getType()).getRight().toString());
                } else if (range.contains(",")) {
                    final StringTokenizer token = new StringTokenizer(range, ", { }");
                    upperBound = token.countTokens();
                } else {
                    throw new IllegalArgumentException("The variable domain is not defined!");
                }

                final MinMax minMax = new MinMax(lowerBound, upperBound);
                if (theoNbrOfReachableStates == 0) {
                    theoNbrOfReachableStates = (double) (Math.abs(upperBound - lowerBound + 1));
                } else {
                    theoNbrOfReachableStates *= ((double) Math.abs(upperBound - lowerBound + 1));
                }

                if (!var2MinMaxValMap.containsKey(varName)) {
                    var2MinMaxValMap.put(varName, minMax);
                }

                int currDomain = -1;
                if (negativeValuesIncluded) {
                    final double lb = Math.abs(lowerBound);
                    final double ub = Math.abs(upperBound);

                    if (ub >= lb) {
                        currDomain = ((int) Math.pow(2, (int) Math.ceil(Math.log(ub + 1) / Math.log(2)) + 1));
                    } else {
                        currDomain = ((int) Math.pow(2, (int) Math.ceil(Math.log(lb) / Math.log(2)) + 1));
                    }

                } else {
                    currDomain = upperBound + 1;
                }

                var2domainMap.put(var.getName(), currDomain);

                if (currDomain > domain) {
                    domain = currDomain;
                }
                if (clocks.contains(var) && currDomain > largestClockDomain) {
                        largestClockDomain = currDomain;
                }
            }
        }

        for (final AbstractSubject sub : module.getComponentListModifiable()) {
            if (sub instanceof SimpleComponentSubject) {
                final ExtendedAutomaton exAutomaton = new ExtendedAutomaton(this, (SimpleComponentSubject) sub);

                if (theoNbrOfReachableStates == 0) {
                    theoNbrOfReachableStates = ((double) exAutomaton.nbrOfNodes());
                } else {
                    theoNbrOfReachableStates *= ((double) exAutomaton.nbrOfNodes());
                }

                if (exAutomaton.isSpecification()) {

                    modelHasNoSpecs = false;
                } else {
                    plantAlphabet.addAll(exAutomaton.getAlphabet());
                    modelHasNoPlants = false;
                }
                theExAutomata.add(exAutomaton);
                exAutomatonToIndex.put(exAutomaton, nbrOfExAutomata);
                stringToExAutomaton.put(exAutomaton.getName(), exAutomaton);
                nbrOfExAutomata++;
            }
        }

        nbrOfEFAsVars = variables.size() + theExAutomata.size();
    }

    public boolean isEventForcible(final EventDeclProxy e){
        return forcibleAlphabet.contains(e);
    }

    public String getGlobalClockName() {
        return CLOCK_PREFIX + GLOBAL_PREFIX;
    }

    public List<VariableComponentProxy> getClocks() {
        return clocks;
    }

    public List<VariableComponentProxy> getParameters() {
        return parameters;
    }

    public List<VariableComponentProxy> getStageVars() {
        return stageVars;
    }

    public boolean modelHasNoPlants() {
        return modelHasNoPlants;
    }

    public boolean modelHasNoSpecs() {
        return modelHasNoSpecs;
    }

    public int getVarDomain(final String varName)
    {
        return var2domainMap.get(varName);
    }

    public boolean isNegativeValuesIncluded() {
        return negativeValuesIncluded;
    }

    public List<VariableComponentProxy> getRelatedVars(final VariableComponentProxy var) {
        return var2relatedVarsMap.get(var);
    }

    public void addToRelatedVars(final VariableComponentProxy var, final List<VariableComponentProxy> vars) {
        var2relatedVarsMap.get(var).addAll(vars);
    }

    public int getExAutomatonIndex(final ExtendedAutomaton efa)
    {
        return exAutomatonToIndex.get(efa);
    }

    public List<ExtendedAutomaton> getExtendedAutomataList() {
        return theExAutomata;
    }

    public void extDomain(final int d) {
        domain = d;
    }

    public String getlocVarSuffix() {
        return LOCAL_VAR_SUFFIX;
    }

    public HashSet<EventDeclProxy> getUncontrollableAlphabet() {
        return uncontrollableAlphabet;
    }

    public int getDomain() {
        return domain;
    }

    public int getLargestClockDomain() {
        return largestClockDomain;
    }

    public int getMaxValueofVar(final String var) {
        return var2MinMaxValMap.get(var).getMax();
    }

    public int getMinValueofVar(final String var) {
        return var2MinMaxValMap.get(var).getMin();
    }

    public int size() {
        return nbrOfExAutomata;
    }

    public VariableComponentProxy getVariableByName(final String varName) {
        for (final VariableComponentProxy var : variables) {
            if (var.getName().equals(varName)) {
                return var;
            }
        }

        throw new IllegalArgumentException("There does not exists a variable in the model with name " + varName + "!");
    }

    public List<VariableComponentProxy> getVars() {
        return variables;
    }

    public List<EventDeclProxy> getInverseAlphabet(final ExtendedAutomaton exAut) {
        final List<EventDeclProxy> events = getUnionAlphabet();
        final List<EventDeclProxy> invAlph = new ArrayList<EventDeclProxy>();
        for (final EventDeclProxy e : events) {
            if (!exAut.getAlphabet().contains(e)) {
                invAlph.add(e);
            }
        }

        return invAlph;
    }

    public ModuleSubject getModule() {
        return module;
    }

    public void addIntegerVariable(final String name, final int lowerBound, final int upperBound, final int initialValue, final Integer markedValue) {

        module.getComponentListModifiable().add(VariableHelper.createIntegerVariable(name, lowerBound, upperBound, initialValue, markedValue));
    }

    public void addEnumerationVariable(final String name, final Collection<String> elements, final String initialValue, final Collection<String> markedValues) {
        final VariableComponentSubject var = VariableHelper.createEnumerationVariable(name, elements, initialValue, markedValues);
        if (!module.getComponentListModifiable().contains(var)) {
            module.getComponentListModifiable().add(var);
        }
    }

    public EventDeclProxy eventIdToProxy(final String id) {
        return eventIdToProxyMap.get(id);
    }

    public List<EventDeclProxy> getUnionAlphabet() {
        return unionAlphabet;
    }

    public void addEvent(final String name)
    {
        addEvent(name,EventKind.CONTROLLABLE.value());
    }


    public Iterator<ExtendedAutomaton> iterator() {
        return theExAutomata.iterator();
    }

    public boolean addEvent(final EventDeclProxy event){
        if(eventIdToProxyMap.get(event.getName()) == null){
            module.getEventDeclListModifiable().add((EventDeclSubject)event);
            eventIdToProxyMap.put(event.getName(), event);
            if(event.getKind() == EventKind.CONTROLLABLE) {
                controllableAlphabet.add(event);
            }
            else if(event.getKind() == EventKind.UNCONTROLLABLE) {
                uncontrollableAlphabet.add(event);
            }
            return true;
        }
        return false;
    }

    public void addEvent(final String name, final String kind){
        addEvent(name, kind, true);
    }

    public EventDeclProxy addEvent(final String name, final String kind, final boolean observable) throws NullPointerException
    {
        try {
            if(name == null){
                throw new NullPointerException("ExtendedFiniteAutomata.AddEvent(): Null input name.");
            }
            EventDeclProxy event = eventIdToProxyMap.get(name);
              if(event == null){
                  final SimpleIdentifierProxy ident = factory.createSimpleIdentifierProxy(name);
                  if(kind == null){
                      throw new NullPointerException("ExtendedFiniteAutomata.AddEvent(): Null kind name.");
                  }
                  if (kind.equalsIgnoreCase(EventKind.CONTROLLABLE.value())) {
                      event = factory.createEventDeclProxy(ident, EventKind.CONTROLLABLE, observable, ScopeKind.LOCAL, null, null, null);
                      module.getEventDeclListModifiable().add((EventDeclSubject)event);
                      if(!eventIdToProxyMap.containsKey(name)){
                          controllableAlphabet.add(event);
                          eventIdToProxyMap.put(name, event);
                      }
                  } else if (kind.equalsIgnoreCase(EventKind.UNCONTROLLABLE.value())) {
                      event = factory.createEventDeclProxy(ident,EventKind.UNCONTROLLABLE, observable, ScopeKind.LOCAL, null, null, null);
                      module.getEventDeclListModifiable().add((EventDeclSubject)event);
                      if(!eventIdToProxyMap.containsKey(name)){
                          uncontrollableAlphabet.add(event);
                          eventIdToProxyMap.put(name, event);
                      }
                  } else if (kind.equalsIgnoreCase(EventKind.PROPOSITION.value())){
                      event = factory.createEventDeclProxy(ident, EventKind.PROPOSITION, observable, ScopeKind.LOCAL, null, null, null);
                      module.getEventDeclListModifiable().add((EventDeclSubject)event);
                      eventIdToProxyMap.put(name, event);
                  }
              }
            return event;            
        } catch (final NullPointerException e) {
            throw e;
        }
    }

    public Set<VariableComponentProxy> extractVariablesFromExpr(final SimpleExpressionProxy expr)
    {
        final Set<VariableComponentProxy> vars = new HashSet<VariableComponentProxy>();
        for(final Proxy proxy:module.getComponentList())
        {
            if(proxy instanceof VariableComponentProxy)
            {
                final VariableComponentProxy var = (VariableComponentProxy)proxy;
                if(expr.toString().contains(var.getName())) {
                    vars.add(var);
                }
            }
        }
        return vars;
    }
    /**
     * Add the automaton to the list of automata. All events will be added to the union alphabet set.
     * @param exAutomaton The EFA
     */
    public void addAutomaton(final ExtendedAutomaton exAutomaton)
    {
        theExAutomata.add(exAutomaton);
        stringToExAutomaton.put(exAutomaton.getName(), exAutomaton);
        module.getComponentListModifiable().add(exAutomaton.getComponent());
        for(final EventDeclProxy event : exAutomaton.getAlphabet()){
            if(eventIdToProxyMap.get(event.getName()) == null){
                eventIdToProxyMap.put(event.getName(), event);
                module.getEventDeclListModifiable().add((EventDeclSubject)event);
                unionAlphabet.add(event);
                if (event.getKind().value().equals("controllable") || event.getKind().equals(EventKind.CONTROLLABLE)) {
                    controllableAlphabet.add(event);
                } else if (event.getKind().value().equals("uncontrollable") || event.getKind().equals(EventKind.UNCONTROLLABLE)) {
                    uncontrollableAlphabet.add(event);
                }
                if(exAutomaton.getKind() == ComponentKind.PLANT){
                    plantAlphabet.add(event);
                }
            }
        }
        if(!exAutomaton.getMarkedLocations().isEmpty()) {
            addEvent(EventDeclProxy.DEFAULT_MARKING_NAME, EventKind.PROPOSITION.value());
        }
    }

    public ExtendedAutomaton getExtendedAutomaton(final String name){
        return stringToExAutomaton.get(name);
    }

    public HashSet<EventDeclProxy> getPlantAlphabet() {
        return plantAlphabet;
    }

    public ArrayList<AbstractSubject> getComponents() {
        final ArrayList<AbstractSubject> components = new ArrayList<AbstractSubject>();

        for (final ExtendedAutomaton exAut : theExAutomata) {
            components.add(exAut.getComponent());
        }

        return components;
    }

    public void writeToFile(final File file) {
        try {
            final JAXBModuleMarshaller marshaller = new JAXBModuleMarshaller(factory, CompilerOperatorTable.getInstance());
            marshaller.marshal(module, file);
        } catch (final Exception e) {
            System.err.println(e);
        }
    }

    //######################################################################
    //# Event Declarations
    /**
     * Checks whether the underlying module contains an event with the
     * given name.
     */
    boolean containsEvent(final String name) {
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
    void includeControllableEvent(final String name) {
        if (!containsEvent(name)) {
            final SimpleIdentifierProxy ident =
                    factory.createSimpleIdentifierProxy(name);
            final EventDeclSubject decl =
                    factory.createEventDeclProxy(ident, EventKind.CONTROLLABLE);
            module.getEventDeclListModifiable().add(decl);
        }
    }

    class MinMax {

        private int min;
        private int max;

        public MinMax() {
        }

        public MinMax(final int min, final int max) {
            this.min = min;
            this.max = max;
        }

        public void setMin(final int min) {
            this.min = min;
        }

        public void setMax(final int max) {
            this.max = max;
        }

        public int getMin() {
            return min;
        }

        public int getMax() {
            return max;
        }
    }
}
