/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.supremica.automata.IO;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.base.GeometryProxy;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.SAXModuleMarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.AliasProxy;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.BoxGeometryProxy;
import net.sourceforge.waters.model.module.ColorGeometryProxy;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.ConstantAliasProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EnumSetExpressionProxy;
import net.sourceforge.waters.model.module.EventAliasProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.FunctionCallExpressionProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifiedProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.ModuleSequenceProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.PointGeometryProxy;
import net.sourceforge.waters.model.module.QualifiedIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.SplineGeometryProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.model.module.VariableMarkingProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;

import org.xml.sax.SAXException;

/**
 * Converts Waters modules to NuSMV files
 *
 * Precondition: all automata should be flattened out, no complex nodes, foreach
 * events etc. Otherwise result will symply be nonsense (e.g. there is filter for
 * SimpleIdentifierProxy, that silently discard all the rest)
 *
 * This code is no more than a hack to convert Waters modules to NuSMV files.
 * The code is probably inefficient and unclean. It uses a lot of
 * functional programming constructs in many places, and sometimes they are not
 * consistent with the rest of the code. I will probably clean it up after reading some
 * "functional programming in java" resources. The reason for all this mess is that
 * I wrote conversion specification in the functional style
 *
 *
 *
 * <pre>
 * MODULE main
 * VAR
 *   for each automaton:
 *     variable name (q_automatonName)
 *     domain of the variable: list of all states of the automaton ({q_1_1, q_1_2})
 *   event: list of all events
 *   list of all variables and their domains
 * INIT
 *   conjunction(for each automaton: its state variable "=" its initial state)
 *   and for all variables their initial states (their initial predicates in case of
 *     waters' non-deterministic variables)
 * TRANS
 *   conjunction [ disjunction [ conjunction [stVar a `eq` sou, event `eq` ev, (next stVar a) `eq` des, guard, next varInAction `eq` action] | (sou, dest, ev, guard, action) <- arcs a] | a <- ats]
 *
 * CTLSPEC AG EF ( conjunction [ disjunction [ stVar a `eq` s | s <- markedStates a] | a <- ats] )
 *
 *
 * </pre>
 *
 * Since there are no marked states, but there are only propositions like
 * :accepting and :forbidden, it is very difficult to find single right way to
 * encode all the properties we would like to express. For now we will limit it to
 * :accepting proposition only for verification of non-blocking, but it have to be reconsidered.
 *
 *
 *
 * @author voronov
 */
public class EFAToNuSMV {


    private final static String EVENT_VAR_NAME = "event";
    private final static String INDENT = "  ";
    //private final static CompilerOperatorTable compilerOpTable = CompilerOperatorTable.getInstance();

    public static void main(final String[] args)
      throws SAXException, ParserConfigurationException,
             WatersUnmarshalException, IOException, URISyntaxException
    {
        //String name = "";
        //ModuleSubjectFactory factory = ModuleSubjectFactory.getInstance();
	//ModuleSubject module = new ModuleSubject(name, null);
        final ModuleProxyFactory moduleFactory      = ModuleElementFactory.getInstance();
        final OperatorTable optable                 = CompilerOperatorTable.getInstance();
        final SAXModuleMarshaller moduleMarshaller = new SAXModuleMarshaller(moduleFactory, optable);
        final ModuleProxy module                    = moduleMarshaller.unmarshal(new URI(args[0]));

        final PrintWriter pw = new PrintWriter(System.out);
        (new EFAToNuSMV()).print(module, pw, Arrays.asList(new SpecPrinterNonBlocking()));
        pw.flush();
    }

    public void print(final ModuleProxy m, final PrintWriter pw, final Collection<? extends SpecPrinter> specs) {
        pw.println("MODULE main");
        printVars(m, pw);
        printInits(m, pw);
        printTrans(m, pw);
        for(final SpecPrinter s: specs)
            s.print(m, pw);
        pw.println();
        pw.flush();
    }
    /**
     * Precondition: ComponentList of the ModuleSubject have to be flattened out,
     * i.e.  there should be only SimpleComponentProxy and VariableProxy, and no
     * InstanceProxy or ForeachComponentProxy
     * @param m
     * @param pw
     */
    private void printVars(final ModuleProxy m, final PrintWriter pw) {
        pw.println("VAR");
        /* Automata variables */
        for(final SimpleComponentProxy sc: getAutomata(m)){
            pw.println(INDENT + declVarAsSet(varName(sc), valueNames(getStates(sc))));
        }
        /* Current event variable (maybe it should be input variable IVAR?) */
        pw.println(INDENT + declVarAsSet(EVENT_VAR_NAME, valueNames(m.getEventDeclList())));
        /* EFA variables */
        for(final VariableComponentProxy v: getEFAVariables(m)){
            pw.println(INDENT + declVar(v.getName(), efaVarDomain(v)));
        }
    }
    private void printInits(final ModuleProxy m, final PrintWriter pw) {
        pw.println("INIT");
        final Collection<String> elems = new ArrayList<String>();
        for(final SimpleComponentProxy sc: getAutomata(m)){
            elems.add(exprEqualsSet(varName(sc), valueNames(getInitialStates(sc))));
        }
        for(final VariableComponentProxy v: getEFAVariables(m)){
            elems.add(watersExprToSmvExpr(v.getInitialStatePredicate()));
        }
        pw.println(INDENT + conjunction(elems));
    }

    /**
     * this will only work if all arcs has their transitions as SimpleIndentifierProxy
     * @param m
     * @param pw
     */
    private void printTrans(final ModuleProxy m, final PrintWriter pw) {
        pw.println("TRANS");
        final Collection<String> allAutomata = new ArrayList<String>();
        for(final SimpleComponentProxy sc: getAutomata(m)){
            final Collection<String> allEdges = new ArrayList<String>();
            allEdges.addAll(transitionConditions(sc));
            allEdges.addAll(stayConditions(sc, m));
            allAutomata.add(disjunction(allEdges));
        }
        final String allAutomataExpression = conjunction(allAutomata);
        pw.println(INDENT + allAutomataExpression);

    }

    private Collection<String> transitionConditions(final SimpleComponentProxy sc){
        return map(new Function<EdgeProxy, String>() {
            @Override
            public String f(final EdgeProxy edge) {
                return transitionCondition(sc, edge.getSource(), edge.getTarget(), getEvents(edge), edge.getGuardActionBlock());
            }
        }, sc.getGraph().getEdges());
    }
    private Collection<String> stayConditions(final SimpleComponentProxy sc, final ModuleProxy m){
        final Collection<String> pred = map(new Function<SimpleNodeProxy, String>() {
            @Override
            public String f(final SimpleNodeProxy n) {
                return transitionCondition(sc, n, n,
                        filterType(SimpleIdentifierProxy.class, n.getPropositions().getEventIdentifierList()),
                        null);
            }
        }, filterStatesWithPredicates(getStates(sc)));
        pred.add(stayAllCondition(sc, eventsNotInAlphabet(m, sc)));
        return pred;
    }

    private Collection<SimpleNodeProxy> filterStatesWithPredicates(final Collection<SimpleNodeProxy> nodes){
        return filter(new Filter<SimpleNodeProxy>() {

            @Override
            public Boolean f(final SimpleNodeProxy n) {
                if(n.getPropositions()==null)
                    return false;
                else if(n.getPropositions().getEventIdentifierList() == null)
                    return false;
                else
                    return n.getPropositions().getEventIdentifierList().size() > 0;
            }
        }, nodes);
    }

    private Collection<SimpleIdentifierProxy> eventsNotInAlphabet(final ModuleProxy m, final SimpleComponentProxy sc){

        final Collection<SimpleIdentifierProxy> events = getEvents(sc);
        events.addAll(getPropositions(sc));
        return filter(new Filter<SimpleIdentifierProxy>() {
            @Override
            public Boolean f(final SimpleIdentifierProxy value) {
                return !contains(events, value);
            }
        }, getEvents(m));
    }
    private boolean contains(final Collection<SimpleIdentifierProxy> ids, final SimpleIdentifierProxy v){
        for(final SimpleIdentifierProxy i: ids)
            if(i.getName().equals(v.getName()))
                return true;
        return false;
    }

    private Collection<SimpleIdentifierProxy> getEvents(final SimpleComponentProxy sc){
        return unlines(map(new Function<EdgeProxy, Collection<SimpleIdentifierProxy>>() {
            @Override
            public Collection<SimpleIdentifierProxy> f(final EdgeProxy value) {
                return filterType(SimpleIdentifierProxy.class, value.getLabelBlock().getEventIdentifierList());
            }
        }, sc.getGraph().getEdges()));
    }

    private Collection<SimpleIdentifierProxy> getEvents(final ModuleProxy m){
        return filterType(SimpleIdentifierProxy.class, map(new Function<EventDeclProxy, IdentifierProxy>() {
            @Override
            public IdentifierProxy f(final EventDeclProxy value) {
                return value.getIdentifier();
            }
        }, m.getEventDeclList()));
    }

    private <T> Collection<T> unlines(final Collection<Collection<T>> cs){
        final Collection<T> res = new ArrayList<T>();
        for(final Collection<T> c: cs)
            for(final T e: c)
                res.add(e);
        return res;
    }

    private String transitionCondition
            ( final SimpleComponentProxy sc
            , final NodeProxy source
            , final NodeProxy target
            , final Collection<SimpleIdentifierProxy> events
            , final GuardActionBlockProxy gab
            ){
        final Collection<String> elems = new ArrayList<String>();
        elems.add(exprEquals(varName(sc), valueName(source)));
        elems.add(disjunctionOfEvents(events));
        elems.add(nextEquals(varName(sc), valueName(target)));
        if(gab != null){
            final Collection<SimpleExpressionProxy> gs = gab.getGuards();
            if(gs!=null && !gs.isEmpty())
                elems.add(allGuards(gs));
            final Collection<BinaryExpressionProxy> as = gab.getActions();
            if(as!=null && !as.isEmpty())
                elems.add(allActions(as));
        }
        return conjunction(elems);
    }

    private String stayAllCondition(final SimpleComponentProxy sc, final Collection<SimpleIdentifierProxy> events){
        return conjunction(Arrays.asList(nextEquals(varName(sc), varName(sc)), disjunctionOfEvents(events)));
    }

    private Collection<SimpleIdentifierProxy> getEvents(final EdgeProxy edge){
        return filterType(SimpleIdentifierProxy.class, edge.getLabelBlock().getEventIdentifierList());
    }

    private String disjunctionOfEvents(final Collection<SimpleIdentifierProxy> events){
        return disjunction(map(new Function<SimpleIdentifierProxy, String>() {
                    @Override
                    public String f(final SimpleIdentifierProxy value) {
                        return exprEquals(EVENT_VAR_NAME, eventName(value));
                    }
                }, events));
    }
    private String disjunctionOfEnabledEvents(final EdgeProxy edge){
        return disjunction(map(new Function<SimpleIdentifierProxy, String>() {
                    @Override
                    public String f(final SimpleIdentifierProxy value) {
                        return exprEquals(EVENT_VAR_NAME, eventName(value));
                    }
                }, getEvents(edge)));
    }

    private static String exprEquals(final String e1, final String e2){
        return "("+e1+"="+e2+")";
    }
    private static String declVar(final String v, final String e){
        return v + " : " + e + ";";
    }
    private static String declVarAsSet(final String v, final Collection<String> es){
        switch(es.size()){
            case 0: return "";
            case 1: return declVar(v, es.iterator().next());
            default: return declVar(v, "{" + printDelimited(", ", es) + "}");
        }
    }
    private static String exprEqualsSet(final String v, final Collection<String> es){
        switch(es.size()){
            case 0: return "";
            case 1: return exprEquals(v, es.iterator().next());
            default: return exprEquals(v, "{" + printDelimited(", ", es) + "}");
        }
    }
    private static String disjunction(final Collection<String> elems){
        return junction("|", elems);
    }
    private static String conjunction(final Collection<String> elems){
        return junction("&", elems);
    }
    private static String junction(final String jun, final Collection<String> elems){
        return "("+printDelimited(" " + jun + " ", elems) + ")";
    }
    private static String nextEquals(final String variable, final String expression){
        return exprEquals("next(" + variable +")", expression);
    }
    @SuppressWarnings("unused")
	private static String stmtNextAssign(final String v, final String e){
        return declVar("next(" + v + ")", e);
    }


    private static interface Function <S,D>{ public D f(S value); }

    private static interface Filter<T> extends Function<T, Boolean> {
        /**
         * returns true if element is OK
         * @param value value to check
         * @return      true if element is OK, false otherwise
         */
        @Override
        public Boolean f(T value);
    }

    private static <T> Collection<T> filter(final Filter<T> filter, final Collection<T> collection) {
        final Collection<T> res = new ArrayList<T>();
        for(final T elem: collection)
            if(filter.f(elem))
                res.add(elem);
        return res;
    }

    private static <S,D> Collection<D> filterType(final Class<D> type, final Collection<S> collection) {
        final Collection<D> res = new ArrayList<D>();
        for(final S elem: collection)
            if(type.isInstance(elem))
                res.add(type.cast(elem));
        return res;
    }
    private static <S,D> Collection<D> map(final Function<S,D> fun, final Collection<S> sou){
        final Collection<D> res = new ArrayList<D>();
        for(final S elem: sou)
            res.add(fun.f(elem));
        return res;
    }

    private static Collection<SimpleComponentProxy> getAutomata(final ModuleProxy m){
        return filterType( SimpleComponentProxy.class,m.getComponentList());
    }
    private static Collection<VariableComponentProxy> getEFAVariables(final ModuleProxy m){
        return filterType( VariableComponentProxy.class,m.getComponentList());
    }
    private static Collection<SimpleNodeProxy> getStates(final SimpleComponentProxy sc){
        return filterType( SimpleNodeProxy.class,sc.getGraph().getNodes());
    }
    private static Collection<SimpleNodeProxy> getInitialStates(final SimpleComponentProxy sc){
        return filter(
              new Filter<SimpleNodeProxy>() {
                  @Override
                  public Boolean f(final SimpleNodeProxy t) { return t.isInitial(); }
              },
              getStates(sc)
        );
    }

    @SuppressWarnings("unused")
	private Collection<String> edgeElements(final EdgeProxy edge, final SimpleComponentProxy sc){
        final Collection<String> elems = new ArrayList<String>();
        elems.add(exprEquals(varName(sc), valueName(edge.getSource())));
        elems.add(disjunctionOfEnabledEvents(edge));
        elems.add(nextEquals(varName(sc), valueName(edge.getTarget())));
        final GuardActionBlockProxy gab = edge.getGuardActionBlock();
        if(gab != null){
            final Collection<SimpleExpressionProxy> gs = gab.getGuards();
            if(!gs.isEmpty())
                elems.add(allGuards(gs));
            final Collection<BinaryExpressionProxy> as = edge.getGuardActionBlock().getActions();
            if(!as.isEmpty())
                elems.add(allActions(as));
        }
        return elems;
    }

    @SuppressWarnings("unused")
	private String getPropositionAsSelfLoop(final SimpleComponentProxy sc, final NodeProxy n, final SimpleIdentifierProxy idf){
        return conjunction(Arrays.asList(
                exprEquals(varName(sc), valueName(n))
                , nextEquals(varName(sc), valueName(n))
                , exprEquals(EVENT_VAR_NAME, eventName(idf))
                ));
    }

    @SuppressWarnings("unused")
	private Collection<EventDeclProxy> filterPropositions(final Collection<EventDeclProxy> evs){
        return filter(new Filter<EventDeclProxy>() {
            @Override
            public Boolean f(final EventDeclProxy value) {
                return value.getKind()==EventKind.PROPOSITION;
            }
        }, evs);
    }
    private Collection<SimpleIdentifierProxy> getPropositions(final SimpleComponentProxy sc){
        final Collection<SimpleIdentifierProxy> res  = new ArrayList<SimpleIdentifierProxy>();
        for(final NodeProxy n: sc.getGraph().getNodes()){
            res.addAll(filterType(SimpleIdentifierProxy.class, n.getPropositions().getEventIdentifierList()));
        }
        return res;
    }

    private String varName(final SimpleComponentProxy sc){
        return "a_" + escape(sc.getName());
    }
    private String valueName(final NamedProxy p){
        if(p instanceof SimpleNodeProxy){
            return escape(((SimpleNodeProxy)p).getName());  // state constants will have no prefix
        } else if(p instanceof EventDeclProxy){
            return eventName((EventDeclProxy)p);
        } else {
            throw new IllegalArgumentException("Unexpected element type: " + p.getClass().toString());
        }
    }
    private static String eventName(final EventDeclProxy p){
        return eventName(p.getName());
    }
    private static String eventName(final SimpleIdentifierProxy p){
        return eventName(p.getName());
    }
    private static String eventName(final String name){
        return "e_" + escape(name);
    }

    private static String escape(final String s){
        return s.replace(":", "dblColon");
    }
    private <T extends NamedProxy> Collection<String> valueNames(final Collection<T> es){
        return map(
                new Function<T, String>() {
                    @Override
                    public String f(final T value){return valueName(value);}
                },
                es);
    }
    private static String printDelimited(final String delim, final Collection<String> c){
        final StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for(final String s: c){
            if(isFirst)
                isFirst = false;
            else
                sb.append(delim);
            sb.append(s);
        }
        return sb.toString();
    }

    private String efaVarDomain(final VariableComponentProxy v){
        if(v.getType() instanceof BinaryExpressionProxy){
            final BinaryExpressionProxy b = (BinaryExpressionProxy) v.getType();
            if(b.getOperator().getName().equals("..")){
                if(b.getLeft() instanceof IntConstantProxy){
                    final IntConstantProxy ic = (IntConstantProxy) b.getLeft();
                    final int i = ic.getValue();
                    if(b.getRight() instanceof IntConstantProxy){
                        final IntConstantProxy jc = (IntConstantProxy) b.getRight();
                        final int j = jc.getValue();
                        return "" + i + ".." + j;
                    } else {
                        throw new IllegalArgumentException("expected int constant on the right of ..");
                    }
                } else {
                    throw new IllegalArgumentException("expected int constant on the left of ..");
                }
            } else {
                throw new IllegalArgumentException("unrecognized operator for domain range (expected ..): " + b.getOperator().getName());
            }
        } else {
            throw new IllegalArgumentException("unrecognized type of binary expression for domain (expected binary ..): " + v.getType().getClass().toString());
        }
    }

    private static String watersExprToSmvExpr(final SimpleExpressionProxy exp){
        try {
            return (String)exp.acceptVisitor(new ExpressionToSmvVisitorTransConstraint());
        } catch (final VisitorException ex) {
            Logger.getLogger(EFAToNuSMV.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalArgumentException("unprintable expression: ", ex);
        }
    }

    private String allGuards(final Collection<SimpleExpressionProxy> c){
        return conjunction(map(new Function<SimpleExpressionProxy, String>() {
            @Override
            public String f(final SimpleExpressionProxy value) {
                return watersExprToSmvExpr(value);
            }
        }, c));
    }
    private String allActions(final Collection<BinaryExpressionProxy> c){
        return conjunction(map(new Function<BinaryExpressionProxy, String>() {
            @Override
            public String f(final BinaryExpressionProxy value) {
                return watersExprToSmvExpr(value);
            }
        }, c));
    }


    private static class ExpressionToSmvVisitorTransConstraint implements ModuleProxyVisitor {

        @Override
        public Object visitAliasProxy(final AliasProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        private static Map<String, String> replaceable = new HashMap<String, String>();
        private static Collection<String> unsupported = new ArrayList<String>();
        static {
            replaceable.put("+", "+");
            replaceable.put("-", "-");
            replaceable.put("*", "*");
            replaceable.put("/", "/");
            replaceable.put("==", "=");
            //replaceable.put("=", "next()=");  // this replacement is only valid when converting Action Expression to NuSMV TRANS constraint
            replaceable.put("&", "&");
            replaceable.put("|", "|");
            replaceable.put(">", ">");
            replaceable.put("<", "<");
            replaceable.put(">=", ">=");
            replaceable.put("<=", "<=");
            replaceable.put("!=", "!=");
            //replaceable.put("", "");

            unsupported.add("..");
            unsupported.add("+=");
            unsupported.add("-=");
        }

        @Override
        public Object visitBinaryExpressionProxy(final BinaryExpressionProxy proxy) throws VisitorException {
            final String op = proxy.getOperator().getName();

            if(unsupported.contains(op)){
                throw new IllegalArgumentException("operator " + op + " not supported");
            } else if(replaceable.containsKey(op)){
                return proxy.getLeft().acceptVisitor(this)
                        + replaceable.get(op)
                        + proxy.getRight().acceptVisitor(this);
            } else if (op.equals("=")) {
                return nextEquals((String)proxy.getLeft().acceptVisitor(this), (String)proxy.getRight().acceptVisitor(this));
            } else {
                throw new IllegalArgumentException("unrecognized operator: " + op);
            }
        }

        @Override
        public Object visitBoxGeometryProxy(final BoxGeometryProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object visitColorGeometryProxy(final ColorGeometryProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object visitComponentProxy(final ComponentProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object visitConstantAliasProxy(final ConstantAliasProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object visitEdgeProxy(final EdgeProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object visitEnumSetExpressionProxy(final EnumSetExpressionProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object visitEventAliasProxy(final EventAliasProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object visitEventDeclProxy(final EventDeclProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object visitEventListExpressionProxy(final EventListExpressionProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object visitExpressionProxy(final ExpressionProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object visitForeachProxy(final ForeachProxy proxy) throws VisitorException {
          throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object visitFunctionCallExpressionProxy(final FunctionCallExpressionProxy proxy) throws VisitorException {
          throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object visitGraphProxy(final GraphProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object visitGroupNodeProxy(final GroupNodeProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object visitGuardActionBlockProxy(final GuardActionBlockProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object visitIdentifiedProxy(final IdentifiedProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object visitIdentifierProxy(final IdentifierProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object visitIndexedIdentifierProxy(final IndexedIdentifierProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object visitInstanceProxy(final InstanceProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object visitIntConstantProxy(final IntConstantProxy proxy) throws VisitorException {
            return Integer.toString(proxy.getValue());
        }

        @Override
        public Object visitLabelBlockProxy(final LabelBlockProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object visitLabelGeometryProxy(final LabelGeometryProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object visitModuleProxy(final ModuleProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object visitModuleSequenceProxy(final ModuleSequenceProxy proxy)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object visitNodeProxy(final NodeProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object visitParameterBindingProxy(final ParameterBindingProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object visitPlainEventListProxy(final PlainEventListProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object visitPointGeometryProxy(final PointGeometryProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object visitQualifiedIdentifierProxy(final QualifiedIdentifierProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object visitSimpleComponentProxy(final SimpleComponentProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object visitSimpleExpressionProxy(final SimpleExpressionProxy proxy) throws VisitorException {
            System.err.println("\n\nHello world!!\n\n");
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object visitSimpleIdentifierProxy(final SimpleIdentifierProxy proxy) throws VisitorException {
            return proxy.getName();
        }

        @Override
        public Object visitSimpleNodeProxy(final SimpleNodeProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object visitSplineGeometryProxy(final SplineGeometryProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object visitUnaryExpressionProxy(final UnaryExpressionProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object visitVariableComponentProxy(final VariableComponentProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object visitVariableMarkingProxy(final VariableMarkingProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object visitProxy(final Proxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object visitGeometryProxy(final GeometryProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object visitNamedProxy(final NamedProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object visitDocumentProxy(final DocumentProxy proxy) throws VisitorException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    public static interface SpecPrinter {
        public void print(ModuleProxy m, PrintWriter pw);
    }
    public static class SpecPrinterNonBlocking implements SpecPrinter {

        private final String markingEventName;
        public SpecPrinterNonBlocking(final String markingEventName){
            this.markingEventName = markingEventName;
        }
        public SpecPrinterNonBlocking(){
            this(EventDeclProxy.DEFAULT_MARKING_NAME);
        }
        @Override
        public void print(final ModuleProxy m, final PrintWriter pw) {
            pw.print("CTLSPEC AG(EF(");
            final Collection<String> parts = new ArrayList<String>();

            /* first part - marking event is fired */
            parts.add(exprEquals(EVENT_VAR_NAME, eventName(markingEventName)));

            /* second part - all EFA varables have "marked" values */
            for(final VariableComponentProxy v: getEFAVariables(m)){
                for(final VariableMarkingProxy vm: v.getVariableMarkings()){
                    // for now we do only :accepting proposition
                    if(vm.getProposition() instanceof SimpleIdentifierProxy){
                        if(((SimpleIdentifierProxy)vm.getProposition()).getName().equals(markingEventName))
                            parts.add(exprEquals(v.getName(), watersExprToSmvExpr(vm.getPredicate())));
                    } else {
                        throw new IllegalArgumentException("unsupported identifier class: " + vm.getProposition().getClass().toString());
                    }
                }
            }
            pw.print(conjunction(parts));
            pw.println("))");
        }
    }
}
