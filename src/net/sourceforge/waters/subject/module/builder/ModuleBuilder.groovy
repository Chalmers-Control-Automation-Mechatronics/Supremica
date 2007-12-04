package net.sourceforge.waters.subject.module.builder;

import groovy.util.BuilderSupport
import net.sourceforge.waters.subject.module.VariableComponentSubject
import net.sourceforge.waters.model.module.*
import net.sourceforge.waters.subject.module.*
import net.sourceforge.waters.gui.*
import net.sourceforge.waters.model.compiler.CompilerOperatorTable
import net.sourceforge.waters.model.expr.OperatorTable
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller
import net.sourceforge.waters.model.marshaller.ProxyMarshaller
import net.sourceforge.waters.model.marshaller.WatersMarshalException
import net.sourceforge.waters.model.module.ModuleProxy
import net.sourceforge.waters.model.module.ModuleProxyFactory
import net.sourceforge.waters.subject.module.ModuleSubjectFactory
import net.sourceforge.waters.model.expr.ExpressionParser
import net.sourceforge.waters.xsd.base.ComponentKind
import net.sourceforge.waters.xsd.base.EventKind
import net.sourceforge.waters.subject.module.EdgeSubject
import net.sourceforge.waters.model.expr.Operator
import org.supremica.gui.ide.IDE
import org.supremica.gui.InterfaceManager
import net.sourceforge.waters.xsd.module.ScopeKind

class ModuleBuilder extends BuilderSupport {

	static void main(args) {
		def module = testBuilder()
		Util.saveModuleToFile(module)
		Util.openInSupremica(module)
	}
	static {
		testBuilder()
	}
	
	private static final factory = ModuleSubjectFactory.instance
	private static final parser = new ExpressionParser(factory.instance, CompilerOperatorTable.instance)
	private Map transitionAttributes
	private String initialState
	private List transitionsThatNeedTargetState = []
	private List transitionsThatNeedSourceState = []
	private NodeSubject lastAddedState
	private String defaultEvent
	private SimpleComponentSubject currentComponent
	final static String VARIABLE_COMPONENT_NAME = 'variables'
	
	def createNode(name){
		return createNode(name, [:])
	}

	def createNode(name, value){
		switch(name) {
		case 'action': return parser.parse(value)
		case 'set': return parser.parse("$value = 1")
		case 'reset': return parser.parse("$value = 0")
		case 'module': if (value instanceof ModuleSubject) return createNode(name, [module:value]); break
		}
		createNode(name, [name:value])
	}

	def createNode(name, Map attributes){
		def node = null
		switch (name) {
		case 'module':
			node = attributes.module ? attributes.module : factory.createModuleProxy(attributes.name, null)
			if (attributes.name) node.name = attributes.name
			if (!attributes.module) {
				node.eventDeclListModifiable << factory.createEventDeclProxy(EventDeclProxy.DEFAULT_MARKING_NAME, EventKind.PROPOSITION)
				node.eventDeclListModifiable << factory.createEventDeclProxy(EventDeclProxy.DEFAULT_FORBIDDEN_NAME, EventKind.PROPOSITION)
			}
			['module'].each{attributes.remove(it)}
			break
		case 'booleanVariable' :
			attributes.range = 0..1
			attributes.initialValue = attributes.initialValue ? 1 : 0
			attributes.markedValue = [(false):0,(true):1][attributes.markedValue]
		case 'integerVariable' :
			if (attributes.name instanceof List) node = attributes.name.collect{VariableHelper.createIntegerVariable(it, attributes.range.from, attributes.range.to, attributes.initialValue, attributes.markedValue)}
			else node = VariableHelper.createIntegerVariable(attributes.name, attributes.range.from, attributes.range.to, attributes.initialValue, attributes.markedValue)
			['initialValue', 'range', 'markedValue'].each{attributes.remove(it)}
			break
		case 'event' :
			if (attributes.range && !attributes.ranges) return createNode(name, [ranges:[attributes.range], *:attributes])
			Closure createEvent = {label ->
				factory.createEventDeclProxy(label,
                            attributes.controllable == null || attributes.controllable ? EventKind.CONTROLLABLE : EventKind.UNCONTROLLABLE,
                            true,
                            ScopeKind.LOCAL,
                            attributes.ranges?.collect{createRangeExpression(it)},
                            null)
            }
			node = (attributes.name instanceof List) ? attributes.name.collect{createEvent(it)} : createEvent(attributes.name)
			['controllable', 'ranges', 'range'].each{attributes.remove(it)}
			break
		case 'proposition':
			node = factory.createEventDeclProxy(attributes.name, EventKind.PROPOSITION)
			break
		case 'specification' :
			attributes.isSpecification = true
		case 'plant':
		case 'automaton' :
			def identifier = parser.parseIdentifier(attributes.name)
			node = current instanceof ModuleProxy ? current.componentList.find{it.identifier == identifier} : current.body.find{it.identifier == identifier} 
			if (!node) {
				node = factory.createSimpleComponentProxy(identifier,
	                                                  attributes.isSpecification ? ComponentKind.SPEC : ComponentKind.PLANT,
	                                                  factory.createGraphProxy())
			}
	        if (attributes.deterministic != null) node.graph.deterministic = attributes.deterministic 
	        initialState = attributes.initialState
			defaultEvent = attributes.defaultEvent
			currentComponent = node
			['deterministic', 'initialState', 'defaultEvent', 'isSpecification'].each{attributes.remove(it)}
			break
		case 'state' :
			node = current.graph.nodes.find{it.name == attributes.name}
			if (!node) node = factory.createSimpleNodeProxy(attributes.name)
			node.initial = (attributes.name == initialState)
			if (attributes.marked) node.propositions.eventListModifiable << parser.parse(EventDeclProxy.DEFAULT_MARKING_NAME)
			if (attributes.forbidden) node.propositions.eventListModifiable << parser.parse(EventDeclProxy.DEFAULT_FORBIDDEN_NAME)
			node.propositions.eventListModifiable.addAll(attributes.propositions.collect{parser.parse(it)})
			['forbidden', 'marked', 'propositions'].each{attributes.remove(it)}
			def transitionsWithThisTarget = transitionsThatNeedTargetState.findAll{!it.targetName || it.targetName == node.name}
			transitionsWithThisTarget.each{it.transition.target = node}
			transitionsThatNeedTargetState -= transitionsWithThisTarget
			def transitionsWithThisSource = transitionsThatNeedSourceState.findAll{it.sourceName == node.name}
			transitionsWithThisSource.each{it.transition.source = node}
			transitionsThatNeedSourceState -= transitionsWithThisSource
			lastAddedState = node
			break
		case 'selfLoop':
			return createNode('transition', [selfLoop:true, *:attributes])
		case 'incoming':
			return createNode('transition', [incoming:true, *:attributes])
		case 'outgoing':
			return createNode('transition', [outgoing:true, *:attributes])
		case 'transition':
			if (attributes.event && !attributes.events)	attributes.events = [attributes.event]
			node = factory.createEdgeProxy(null,
					                       null,
					                       factory.createLabelBlockProxy(attributes.events ? attributes.events.collect{parser.parse(it)} : [parser.parse(defaultEvent)], null),
					                       attributes.guard ? factory.createGuardActionBlockProxy([parser.parse(attributes.guard, Operator.TYPE_BOOLEAN)], null, null) : null,
					                       null,
					                       null,
					                       null)
			transitionAttributes = attributes.clone()
			['selfLoop', 'events', 'event', 'from', 'to', 'guard', 'incoming', 'outgoing'].each{attributes.remove(it)}
			break
		case 'foreach':
			node = factory.createForeachComponentProxy(attributes.name, createRangeExpression(attributes.range))
			['range'].each{attributes.remove(it)}
			break
		case 'eventAlias':
			node = factory.createEventAliasProxy(parser.parseIdentifier(attributes.name), factory.createPlainEventListProxy(attributes.events.collect{parser.parse(it)}))
			['events'].each{attributes.remove(it)}
			break
		default:
			assert false : "No match for node name \"$name\""
		}
		assert !attributes || (attributes.size() == 1 && attributes.name), "The attributes ${attributes} does not match node '$name' with name '${attributes.name}'"
		return node
	}

	def createNode(name, Map attributes, value){
		return createNode(name, [name:value, *:attributes] )
	}
	
	void setParent(parent, child){
		if (child instanceof Collection) {
			child.each{setParent(parent, it)}
			return
		}
		switch (parent) {
		case ModuleSubject:
			switch (child) {
			case ComponentSubject:
				if (!parent.componentList.contains(child)) parent.componentListModifiable << child
				break
			case ForeachComponentSubject:
				parent.componentListModifiable << child
				break
			case VariableSubject:
				def dummyComponent = parent.componentList.find{it.name == VARIABLE_COMPONENT_NAME}
				if (!dummyComponent) {
					dummyComponent = factory.createSimpleComponentProxy(parser.parseIdentifier(VARIABLE_COMPONENT_NAME), ComponentKind.PLANT, factory.createGraphProxy())
					dummyComponent.graph.nodesModifiable << factory.createSimpleNodeProxy('dummy', factory.createPlainEventListProxy([parser.parse(EventDeclProxy.DEFAULT_MARKING_NAME)]), true, null, null, null)
					parent.componentListModifiable << dummyComponent
				}
				dummyComponent.variablesModifiable << child
				break
			case EventDeclSubject:
				parent.eventDeclListModifiable << child
				break
			case AliasSubject:
				assert child.expression instanceof EventListExpressionProxy
				def existingEventAlias = parent.eventAliasList.find{it.name == child.name}
				if (existingEventAlias) existingEventAlias.expression.eventListModifiable.addAll(child.expression.eventList.collect{parser.parse(it.name)})
				else parent.eventAliasListModifiable << child
				break
			default:
				assert false, "Parent: $parent, Child: ${child.dump()}"
			}
			break
		case ForeachComponentSubject:
			switch (child) {
			case ComponentSubject:
				parent.bodyModifiable << child
				break
			case ForeachComponentSubject:
				parent.bodyModifiable << child
				break
			default:
				assert false, "Parent: $parent, Child: $child"
			}
			break
		case ComponentSubject:
			switch (child) {
			case NodeSubject:
				if (!parent.graph.nodes.contains(child)) parent.graph.nodesModifiable << child
				if (!initialState && parent.graph.nodes.size() == 1) child.initial = true //The first state becomes initial by default
				break
			case EdgeSubject:
				assert parent.graph.nodes
				assert !transitionAttributes.incoming
				assert !transitionAttributes.outgoing
				assert !transitionAttributes.selfLoop
				if (!transitionAttributes.from) child.source = lastAddedState
				else child.source = parent.graph.nodesModifiable.get(transitionAttributes.from) 
				if (!child.source) transitionsThatNeedSourceState << [transition:child, sourceName:transitionAttributes.from]
				//if (transitionAttributes.selfLoop) child.target = child.source
				if (transitionAttributes.to) child.target = parent.graph.nodes.find{it.name == transitionAttributes.to}
				if (!child.target) transitionsThatNeedTargetState << [transition:child, targetName:transitionAttributes.to]
				parent.graph.edgesModifiable << child
				transitionAttributes = null
				break
			case VariableSubject:
				parent.variablesModifiable << child
				break
			default:
				assert false, "Parent: $parent, Child: $child"
			}
			break
		case EdgeSubject:
			switch (child) {
			case SimpleExpressionProxy:
				if (!parent.guardActionBlock) {
					parent.guardActionBlock = factory.createGuardActionBlockProxy()
				}
				parent.guardActionBlock.actionsModifiable << child
				break
			default:
				assert false, "Parent: $parent, Child: $child"
			}
			break
		case SimpleNodeSubject:
			switch (child) {
			case EdgeSubject:
				if (transitionAttributes.selfLoop) {
					child.source = parent
					child.target = parent
				} else if (transitionAttributes.incoming) {
					child.target = parent
					assert transitionAttributes.from, transitionAttributes
					child.source = currentComponent.graph.nodes.find{it.name == transitionAttributes.from}
				} else if (transitionAttributes.outgoing) {
					child.source = parent
					assert transitionAttributes.to, transitionAttributes
					child.target = currentComponent.graph.nodes.find{it.name == transitionAttributes.to}
				} else assert false
				if (!child.target) transitionsThatNeedTargetState << [transition:child, targetName:transitionAttributes.to]
				if (!child.source) transitionsThatNeedSourceState << [transition:child, sourceName:transitionAttributes.from]
				currentComponent.graph.edgesModifiable << child
				transitionAttributes = null
				break
			default:
				assert false, "Parent: $parent, Child: $child"
			}
			break
		default:
			assert false, "Parent: $parent, Child: $child"
		}
	}
	
	private SimpleExpressionProxy createRangeExpression(Range range) {
		assert range
		factory.createBinaryExpressionProxy(CompilerOperatorTable.instance.getBinaryOperator(".."),
		                                    factory.createIntConstantProxy(range.from),
		                                    factory.createIntConstantProxy(range.to))
	}
	
	void nodeCompleted(parent, node) {
		if (node instanceof Collection) {
			node.each{nodeCompleted(parent, it)}
			return
		}
		switch(node) {
		case(SimpleComponentSubject):
			assert transitionsThatNeedTargetState.empty, 'Automaton ' << node.name << ' is lacking states ' << transitionsThatNeedTargetState.targetName << '. Existing states:' << node.graph.nodes.name
			assert transitionsThatNeedSourceState.empty, 'Automaton ' << node.name << ' is lacking states ' << transitionsThatNeedSourceState.targetName << '. Existing states:' << node.graph.nodes.name
			lastAddedState = null
			initialState = null
			defaultEvent = null
			currentComponent = null
			break
		case (ModuleSubject):
			assert !transitionAttributes
			assert !initialState
			assert transitionsThatNeedTargetState == []
			assert transitionsThatNeedSourceState == []
			assert !defaultEvent
			assert !currentComponent
			break
		}
	}
	
	public static ModuleSubject testBuilder() {
		def moduleBuilder = new ModuleBuilder();
		ModuleSubject module = moduleBuilder.module('testmodule') {
			booleanVariable('y0', initialValue:false, markedValue:true)
			integerVariable(name:['x0', 'x1'], range:1..4, initialValue:2, markedValue:2)
			event(name:'e0')
			event(['e1', 'e2'], controllable:false)
			proposition(name:'e3')
			event('e4', ranges:[0..2])
			event(name:'e5', ranges:[0..2, 0..3], controllable:false)
			eventAlias(name:'someEvents', events:['e1', 'e2'])
			plant(name:'testcomponent', initialState:'q0', defaultEvent:'e0') {
				state(name:'q0')
				state('q1', marked:true)
				transition(from:'q0',
						   to:'q1',
						   guard:'y0 & x0 < 4') {
					action('x0 += 1')
					set('y0')
				}
				transition(from:'q1',
						   to:'q0',
						   events:['e1', 'e0'])
			}
			specification(name:'testcomponent2', deterministic:false) {
				state('s0') {
					selfLoop(events:['e1'])
				}
				state(name:'s1', propositions:['e3']) {
					selfLoop(events:['e1'])
					incoming(from:'s0', events:['someEvents']) {
						reset('y1')
					}
				}
				booleanVariable(name:'y1', initialValue:false)
				transition(from:'s0', to:'s1', event:'e2', guard:'!y0')
			}
			foreach('i', range:0..2) {
				automaton('testcomponent3[i]', initialState:'q0') { //becomes plant by default 
					state(name:'q0')
					transition(event:'e4[i]',
					           guard:'y0 & x0 < 4') {
						action('x0 += 1')
					}
					transition(events:['e4[i]'],
					           guard:'!y0 & x0 < 4') {
						action('x0 += 1')
					}
					state(name:'q1', forbidden:true) {
						outgoing(to:'q0', events:['e1', 'e0'])
					}
				}
				foreach(name:'j', range:0..3) {
					automaton(name:'testcomponent4[i][j]', initialState:'q0', isSpecification:true) {
						state(name:'q0')
						transition(from:'q0', to:'q0', events:['e5[i][j]'])
					}
				}
			}
		}
		assert module.name == 'testmodule'
		assert module.componentList.name.contains('testcomponent')
		assert module.eventDeclList.findAll{it.kind == EventKind.CONTROLLABLE}.name == ['e0', 'e4']
		assert module.eventDeclList.findAll{it.kind == EventKind.UNCONTROLLABLE}.name == ['e1', 'e2', 'e5']
		assert module.eventDeclList.findAll{it.kind == EventKind.PROPOSITION}.name == [EventDeclProxy.DEFAULT_MARKING_NAME, EventDeclProxy.DEFAULT_FORBIDDEN_NAME, 'e3']
		assert module.eventAliasList.name == ['someEvents']
		assert module.eventAliasList.expression.eventList.name == ['e1','e2'] 
		assert module.componentList.find{it.name == VARIABLE_COMPONENT_NAME}.variables.type*.toString() == ['0..1', '1..4', '1..4']
        assert module.componentList.find{it.name == VARIABLE_COMPONENT_NAME}.variables.initialValue.value == [0, 2, 2]
		assert module.componentList.name == [VARIABLE_COMPONENT_NAME, 'testcomponent', 'testcomponent2', 'i']
		assert module.componentList.find{it.name == 'testcomponent'}.graph.deterministic
		assert module.componentList.find{it.name == 'testcomponent'}.graph.nodes.name == ['q0', 'q1']
		assert module.componentList.find{it.name == 'testcomponent'}.graph.nodes.find{it.name == 'q0'}.initial
		assert module.componentList.find{it.name == 'testcomponent'}.graph.edges.collect {
			[it.source.name,
		     it.target.name,
			 it.labelBlock.eventList.name,
			 it.guardActionBlock?.guards?.size() == 1 ? it.guardActionBlock.guards[0].toString() : null,
			 it.guardActionBlock?.actions?.plainText]
		} == [['q0', 'q1', ['e0'], 'y0 & x0 < 4', ['x0 += 1', 'y0 = 1']],
		      ['q1', 'q0', ['e1','e0'], null, null]]
		assert module.componentList.find{it.name == 'testcomponent2'}.variables.collect {
			[it.name, it.initialValue.value, it.type.toString()]
		} == [['y1', 0, '0..1']]
		assert !module.componentList.find{it.name == 'testcomponent2'}.graph.deterministic
		assert module.componentList.find{it.name == 'testcomponent2'}.graph.nodes.name == ['s0', 's1']
		assert module.componentList.find{it.name == 'testcomponent2'}.graph.nodes.find{it.name == 's0'}.initial
		assert module.componentList.find{it.name == 'testcomponent2'}.graph.edges.collect {
			[it.source.name,
		     it.target.name,
		     it.labelBlock.eventList.name,
		     it.guardActionBlock?.guards?.size() == 1 ? it.guardActionBlock.guards[0].toString() : null,
		     it.guardActionBlock?.actions?.plainText]
		} == [['s0', 's0', ['e1'], null, null], 
		      ['s1', 's1', ['e1'], null, null],
		      ['s0', 's1', ['someEvents'], null, ['y1 = 0']],
		      ['s0', 's1', ['e2'], '!y0', []]]
		module
	}
}