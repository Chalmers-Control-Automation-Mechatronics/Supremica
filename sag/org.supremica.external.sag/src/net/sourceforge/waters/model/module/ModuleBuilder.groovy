package net.sourceforge.waters.model.module;


import groovy.util.BuilderSupport
import net.sourceforge.waters.subject.module.VariableSubject
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

class ModuleBuilder extends BuilderSupport {

	static void main(args) {
		def moduleBuilder = new ModuleBuilder();
		moduleBuilder.module(name:'testmodule') {
			booleanVariable(name: 'y0', initial:false, marked:true)
			integerVariable(name: 'x0', range:1..4, initial:2, marked:2)
			event(name:'e0')
			event(name:'e1', controllable:false)
			event(name:'e2', controllable:false)
			proposition(name:'e3')
			event(name:'e4', ranges:[0..2])
			event(name:'e5', ranges:[0..2, 0..3], controllable:false)
			eventAlias(name:'someEvents', events:['e1', 'e2'])
			plant(name:'testcomponent', initialState:'q0') {
				state(name:'q0')
				state(name:'q1', marked:true)
				transition(from:'q0',
						   to:'q1',
						   events:['e0'],
						   guard:'y0 & x0 < 4') {
					action('x0 += 1')
					action('y0 = 1')
				}
				transition(from:'q1',
						   to:'q0',
						   events:['e1', 'e0'])
			}
			specification(name:'testcomponent2', initialState:'s0') {
				state(name:'s0')
				state(name:'s1', propositions:['e3'])
				booleanVariable(name:'y1', initial:false)
				transition(from:'s0',
						   to:'s1',
						   events:['e2'],
						   guard:'!y0')
				transition(from:'s0',
						   to:'s1',
						   events:['someEvents']) {
					action('y1 = 0')
				}
			}
			foreach(name:'i', range:0..2) {
				plant(name:'testcomponent3[i]', initialState:'q0') {
					state(name:'q0', initial:true)
					state(name:'q1', forbidden:true)
					transition(from:'q0',
					           to:'q1',
					           events:['e4[i]'],
					           guard:'y0 & x0 < 4') {
						action('x0 += 1')
					}
					transition(from:'q1', to:'q0', events:['e1', 'e0'])
				}
				foreach(name:'j', range:0..3) {
					specification(name:'testcomponent4[i][j]') {
						state(name:'q0', initial:true)
						transition(from:'q0', to:'q0', events:['e5[i][j]'])
					}
				}
			}
		}
		def module = moduleBuilder.module
		assert module.name == 'testmodule'
		assert module.componentList.name.contains('testcomponent')
		assert module.eventDeclList.findAll{it.kind == EventKind.CONTROLLABLE}.name == ['e0', 'e4']
		assert module.eventDeclList.findAll{it.kind == EventKind.UNCONTROLLABLE}.name == ['e1', 'e2', 'e5']
		assert module.eventDeclList.findAll{it.kind == EventKind.PROPOSITION}.name == [EventDeclProxy.DEFAULT_MARKING_NAME, EventDeclProxy.DEFAULT_FORBIDDEN_NAME, 'e3']
		assert module.eventAliasList.name == ['someEvents']
		assert module.eventAliasList.expression.eventList.name == ['e1','e2'] 
		assert module.componentList.find{it.name == VARIABLE_COMPONENT_NAME}.variables.type*.toString() == ['0..1', '1..4']
        assert module.componentList.find{it.name == VARIABLE_COMPONENT_NAME}.variables.initialValue.value == [0, 2]
		assert module.componentList.name == [VARIABLE_COMPONENT_NAME, 'testcomponent', 'testcomponent2', 'i']
		assert module.componentList.find{it.name == 'testcomponent'}.graph.nodes.name == ['q0', 'q1']
		assert module.componentList.find{it.name == 'testcomponent'}.graph.nodes.find{it.name == 'q0'}.initial
		assert module.componentList.find{it.name == 'testcomponent'}.graph.edges.collect{[it.source.name,
		                                                                                  it.target.name,
		                                                                                  it.labelBlock.eventList.name,
		                                                                                  it.guardActionBlock?.guards?.size() == 1 ? it.guardActionBlock.guards[0].toString() : null,
		                                                                                  it.guardActionBlock?.actions?.plainText]} == [['q0', 'q1', ['e0'], 'y0 & x0 < 4', ['x0 += 1', 'y0 = 1']],
		                                                                                                                                ['q1', 'q0', ['e1','e0'], null, null]]
		assert module.componentList.find{it.name == 'testcomponent2'}.variables.collect{[it.name, it.initialValue.value, it.type.toString()]} == [['y1', 0, '0..1']]
		assert module.componentList.find{it.name == 'testcomponent2'}.graph.nodes.name == ['s0', 's1']
		assert module.componentList.find{it.name == 'testcomponent2'}.graph.nodes.find{it.name == 's0'}.initial
		assert module.componentList.find{it.name == 'testcomponent2'}.graph.edges.collect{[it.source.name,
		                                                                                  it.target.name,
		                                                                                  it.labelBlock.eventList.name,
		                                                                                  it.guardActionBlock?.guards?.size() == 1 ? it.guardActionBlock.guards[0].toString() : null,
		                                                                                  it.guardActionBlock?.actions?.plainText]} == [['s0', 's1', ['e2'], '!y0', []],
			                                                                                                                            ['s0', 's1', ['someEvents'], null, ['y1 = 0']]]
		//assert false
		def saveToFile = { filename ->
		     def marshaller = new JAXBModuleMarshaller(factory, CompilerOperatorTable.instance)
		     marshaller.marshal(module, new File(filename))
		}
		def launchIDE = {
			InterfaceManager.instance.initLookAndFeel();
			IDE ide = new IDE()
			ide.visible = true
			ide.installContainer(module)
			//ide.openFiles([automataFile, extendedAutomataFile])
		}
		saveToFile("${module.name}.${WmodFileFilter.WMOD}")
		launchIDE()
	}

	ModuleProxy module 
	private static final factory = ModuleSubjectFactory.instance
	private static final parser = new ExpressionParser(factory.instance, CompilerOperatorTable.instance)
	private transitionAttributes
	private String initialState
	final static String VARIABLE_COMPONENT_NAME = 'variables'
		
	def createNode(name){
		null
	}

	def createNode(name, value){
		switch(name) {
		case 'action':
			return parser.parse(value)
		}
		assert false, "name:$name, value:$value"
	}

	def createNode(name, Map attributes){
		def node = null
		switch (name) {
		case 'module':
			node = factory.createModuleProxy(attributes.name, null)
			module = node
			module.eventDeclListModifiable << factory.createEventDeclProxy(EventDeclProxy.DEFAULT_MARKING_NAME, EventKind.PROPOSITION)
			module.eventDeclListModifiable << factory.createEventDeclProxy(EventDeclProxy.DEFAULT_FORBIDDEN_NAME, EventKind.PROPOSITION)
			break
		case 'booleanVariable' :
			node = VariableHelper.createIntegerVariable(attributes.name, 0, 1, attributes.initial ? 1 : 0, attributes.marked ? 1 : 0);
			break
		case 'integerVariable' :
			node = VariableHelper.createIntegerVariable(attributes.name, attributes.range.from, attributes.range.to, attributes.initial, attributes.marked)
			break
		case 'event' :
			node = factory.createEventDeclProxy(attributes.name,
					                            attributes.controllable == null || attributes.controllable ? EventKind.CONTROLLABLE : EventKind.UNCONTROLLABLE,
					                            true,
					                            attributes.ranges?.collect{createRangeExpression(it)},
					                            null)
			break
		case 'proposition':
			node = factory.createEventDeclProxy(attributes.name, EventKind.PROPOSITION)
			break
		case 'plant' :
			node = factory.createSimpleComponentProxy(parser.parseIdentifier(attributes.name),
	                                                  attributes.get('kind', ComponentKind.PLANT),
	                                                  factory.createGraphProxy())
			initialState = attributes.initialState
	        break
		case 'specification' :
			node = factory.createSimpleComponentProxy(parser.parseIdentifier(attributes.name),
	                                                  attributes.get('kind', ComponentKind.SPEC),
	                                                  factory.createGraphProxy())
			initialState = attributes.initialState
			break
		case 'state' :
			node = factory.createSimpleNodeProxy(attributes.name)
			node.initial = (attributes.name == initialState)
			if (attributes.marked) node.propositions.eventListModifiable << parser.parse(EventDeclProxy.DEFAULT_MARKING_NAME)
			if (attributes.forbidden) node.propositions.eventListModifiable << parser.parse(EventDeclProxy.DEFAULT_FORBIDDEN_NAME)
			node.propositions.eventListModifiable.addAll(attributes.propositions.collect{parser.parse(it)})
			break
		case 'transition':
			node = factory.createEdgeProxy(null,
					                       null,
					                       factory.createLabelBlockProxy(attributes.events.collect{parser.parse(it)}, null),
					                       attributes.guard ? factory.createGuardActionBlockProxy([parser.parse(attributes.guard, Operator.TYPE_BOOLEAN)], null, null) : null,
					                       null,
					                       null,
					                       null)
			transitionAttributes = attributes
			break
		case 'foreach':
			node = factory.createForeachComponentProxy(attributes.name, createRangeExpression(attributes.range))
			break
		case 'eventAlias':
			node = factory.createAliasProxy(parser.parseIdentifier(attributes.name), factory.createPlainEventListProxy(attributes.events.collect{parser.parse(it)}))
			break
		default:
			assert false : "No match for node name \"$name\""
		}
		return node
	}

	def createNode(name, Map attributes, value){
		null
	}
	
	void setParent(parent, child){
		switch (parent) {
		case ModuleSubject:
			switch (child) {
			case ComponentSubject:
				parent.componentListModifiable << child
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
				parent.eventAliasListModifiable << child
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
				parent.graph.nodesModifiable << child
				break
			case EdgeSubject:
				child.source = parent.graph.nodes.find{it.name == transitionAttributes.from}
				child.target = parent.graph.nodes.find{it.name == transitionAttributes.to}
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
	}
}