package org.supremica.external.iec61131.builder

import groovy.util.BuilderSupport
import net.sourceforge.waters.subject.module.ModuleSubject
import net.sourceforge.waters.subject.module.builder.Util
import net.sourceforge.waters.subject.module.builder.ModuleBuilder

class Converter {
	static final SCAN_CYCLE_EVENT_NAME = 'scanProgram'
	static final NO_PROCESS_CHANGE_EVENT_NAME = 'noProcessChange'
//	final static END_OF_SCANCYCLE_VARIABLE_NAME = 'endOfScanCycle'
	final static START_OF_SCANCYCLE_STATE_NAME = 'startOfScanCycle'
	final static END_OF_SCANCYCLE_STATE_NAME = 'endOfScanCycle'
	//final static DO_CONTROL_SIGNAL_CHANGE_EVENT_NAME = 'doSignalChange'
	//final static SKIP_CONTROL_SIGNAL_CHANGE_EVENT_NAME = 'skipSignalChange'
	//final static TIMEOUT_EVENTS_ALIAS_NAME = 'timeoutEvent'
	final static PROCESS_EVENTS_ALIAS_NAME = 'processEvent'
	final static CONTROL_UNIT_PLANT_NAME = 'ControlUnit'
	//static final DEFAULT_PROCESS_PLANT_NAME = 'Process'
	static final SEPARATOR_PATTERN = /\./
	static final SEPARATOR = '.'
	static final NOT_INIT_VARIABLE_NAME = 'NOT_INIT'
}

class ControlCodeBuilder extends BuilderSupport {

	ControlCodeBuilder() {
		List standardFBs = []
		standardFBs << functionblock('SR') {
			input 'S'
			input 'R'
			output 'Q'
			logicProgram('program') {
				'Q := not R and Q or S'()
			}
		}
		standardFBs << functionblock('RS') {
			input 'S'
			input 'R'
			output 'Q'
			logicProgram('program') {
				'Q := not R and (Q or S)'()
			}
		}
		standardFBs << functionblock('P') {
			input 'in'
			output 'Q'
			variable 'old'
			logicProgram('program') {
				'Q := in and not old'()
				'old := in'()
			}
		}
		standardFBs << functionblock('N') {
			input 'in'
			output 'Q'
			variable 'old'
			logicProgram('program') {
				'Q := not in and old'()
				'old := in'()
			}
		}
		standardFBs << functionblock('S') {
			input 'in'
			output 'Q'
			logicProgram('program') {
				'Q := Q or in'()
			}
		}
		standardFBs << functionblock('R') {
			input 'in'
			output 'Q'
			logicProgram('program') {
				'Q := Q and not in'()
			}
		}
		functionBlocks = standardFBs
	}
	List functionBlocks = []
	static void main(args) {
//		testAutomataGenerator(true)
//		testAssignment(true)
//		testFunctionBlocks(true)
//		testSfc(true)
//		testSfcDeferred(true)
		//builder.saveModuleToFile()
	}

	static final SET = 'setQualifiers'
	static final RESET = 'resetQualifiers'
	static final NONSTORED = 'nonstoredQualifiers'
		
	static final NODE_TYPES = [LogicProgram,
	                           SequentialProgram,
	                           Sequence,
	                           Assignment,
	                           Process,
	                           Step,
	                           NAction,
	                           P1Action,
	                           P0Action,
	                           SetQualifier,
	                           ResetQualifier,
	                           NonstoredQualifier,
	                           Transition,
	                           FunctionBlock,
	                           FunctionBlockInstance,
	                           FunctionBlockCall,
	                           Input,
	                           Output,
	                           InternalVariable]

	def createNode(name){
		createNode(name, [:], null)
	}

	def createNode(name, value){
		createNode(name, [:], value)
	}
	def createNode(name, Map attributes){
		//println "Create node: name:$name, attributes:$attributes"
		def type = NODE_TYPES.find{name ==~ it.pattern && (!it.declaredFields.any{it.name == 'parentType'} || current?.class == it.parentType)}
		if (!type) {
			if (current instanceof Sequence) return createNode('transition', [guard:name, *:attributes])
			else if (!current || current.properties.keySet().any{it == FunctionBlockCall.parentAttr}) { //FB instance call
				if (attributes.name) { //call of FB instance with name attributes.name that should be created on the fly/implicitly
					attributes.type = name
					return createNode('functionBlockCall', attributes)
				} else { // call of either explicitly declared FB instance or implicitly declared without name 
					return createNode('functionBlockCall', [name:name, *:attributes])
				}
			} else { // FB instance declaration
				assert attributes.name
				return createNode('functionBlockInstance', [type:name, name:attributes.name])
			}
			assert false
		}
		if (type == Assignment && name =~ /\:=/) { //Assignment
			def parts = name.split(/\:=/)
			return createNode('assignment', [Q:parts[0], input:parts[1], *:attributes]) 
		}
		if (type instanceof Class) {
			def obj = type.newInstance()
			def setProperty  = {attribute ->
				if (attribute?.key) {
					def value
					Class propertyType = obj.metaClass.properties.find{it.name.toLowerCase() == attribute.key.toLowerCase()}?.type
					if (propertyType.isCase(Expression) && !(attribute.value instanceof Expression)) {
						value = new Expression(attribute.value)
					} else if (propertyType.isCase(IdentifierExpression) && !(attribute.value instanceof IdentifierExpression)) {
						value = new IdentifierExpression(attribute.value)
					} else {
						value = attribute.value
					}
					obj[attribute.key] = value
				}
			}
			if (attributes.name =~ /\:=/) {
				def parts = attributes.name.split(/\:=/)
				attributes.name = parts[0]
				attributes.value = parts[1]
			}
			if (type == Transition && !attributes.name) {
				int i = 1
				current.namedElements.name.text.each { if (it == "T$i") ++i }
				attributes.name = "T$i"
			}
			attributes.each{ setProperty(it) }
			return obj
		}
		[type:type, *:attributes] as Expando
	}

	def createNode(name, Map attributes, value){
	//	println "createNode, name: $name, attributes:$attributes, value:$value"
		def type = NODE_TYPES.find{name ==~ it.pattern && (!it.declaredFields.any{it.name == 'parentType'} || current.class == it.parentType) }
		if (value != null) {
			if (!type) return createNode(name, [name:value, *:attributes])
			else return createNode(name, [(type.defaultAttr):value, *:attributes])
		} else return createNode(name, attributes)
	}
	
	void setParent(parent, child) {
		//println "setParent: parent: ${parent.inspect()}, child: ${child.inspect()}"
		def parentAttr = child instanceof Expando ? child.type.parentAttr : child.parentAttr
		if (parentAttr) {
			if (parent instanceof Expando && !parent[parentAttr]) parent[parentAttr] = []	
			if (parent[parentAttr] instanceof List) parent[parentAttr] << child
			else parent[parentAttr] = child
		}
		def childAttr = child.metaClass.properties.find{it.type == parent.class}
		if (childAttr) {
			childAttr.setProperty(child, parent)
		}		
	}
	
	void nodeCompleted(parent, node) {
//		println "nodeCompleted: parent: $parent, node: $node"
		switch (node) {
		case Sequence: node.transitions.findAll{!it.to}.each{it.to = node.steps[0].name}; break
		case Step: parent.transitions.findAll{!it.to}.each{it.to = node.name}; break
		case Transition: if (!node.from) node.from = parent.steps[-1].name; break
		case FunctionBlock:
			if (!parent) { //Add predefined function blocks to root FB
				node[FunctionBlock.parentAttr] += functionBlocks
			}
			break
		}
	}
}
