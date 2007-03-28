package org.supremica.external.iec61131.builder

import groovy.util.BuilderSupport
//import net.sourceforge.waters.subject.module.VariableSubject
//import net.sourceforge.waters.model.module.*
import net.sourceforge.waters.subject.module.ModuleSubject
import net.sourceforge.waters.subject.module.builder.Util
import net.sourceforge.waters.subject.module.builder.ModuleBuilder
//import net.sourceforge.waters.gui.*
//import net.sourceforge.waters.model.compiler.CompilerOperatorTable
//import net.sourceforge.waters.model.expr.OperatorTable
//import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller
//import net.sourceforge.waters.model.marshaller.ProxyMarshaller
//import net.sourceforge.waters.model.marshaller.WatersMarshalException
//import net.sourceforge.waters.model.module.ModuleProxy
//import net.sourceforge.waters.model.module.ModuleProxyFactory
//import net.sourceforge.waters.subject.module.ModuleSubjectFactory
//import net.sourceforge.waters.model.expr.ExpressionParser
//import net.sourceforge.waters.xsd.base.ComponentKind
//import net.sourceforge.waters.xsd.base.EventKind
//import net.sourceforge.waters.subject.module.EdgeSubject
//import net.sourceforge.waters.model.expr.Operator
//import org.supremica.gui.ide.IDE
//import org.supremica.gui.InterfaceManager

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
	static {
		testAutomataGenerator(false)
		testAssignment(false)
		testFunctionBlocks(false)
		testSfc(false)
		testSfcDeferred(false)
	
		//println "controlcode builder test" 
		//testBuilder2(false)
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
	
	static void testSfc(openInSupremica) {
		def ccb = new ControlCodeBuilder()
		def appSfc = ccb.application('sfcapp') {
			input 'y1 := true'
			input 'y2 := true'
			output 'u1'
			output 'u2'
			output 'u3'
			output 'u4'
			output 'u5'
			sequentialProgram('program') {
				sequence('mySequence') {
					Step ('S1'){
						N('u5')
						P0_Action {
							'u2 := y1'()
						}
					}
					'y1'();    'y2'(to:'S3')
					Step('S2'){
						S('u4')
						N_action {
							'u1 := y1 or y2'()
						}
					}
					'not y1'(to:'S1')
								Step('S3'){
									P1_action{'u3 := y2'()}
									R('u4')
								}
								'true'()
				}
			}
		}
		def appSfcLess = ccb.application('sfcapp') {
			input 'y1 := true'
			input 'y2 := true'
			output 'u1'
			output 'u2'
			output 'u3'
			output 'u4'
			output 'u5'
			logicProgram('program') {
				variable('S1_X', markedValue:true)
				variable 'S2_X'
				variable 'S3_X'
				variable("${Converter.NOT_INIT_VARIABLE_NAME}", markedValue:true)
				variable 'mySequence_T1_enabled'
				variable 'mySequence_T2_enabled'
				variable 'mySequence_T3_enabled'
				variable 'mySequence_T4_enabled'
				variable 'S1_activation'
				variable 'S2_activation'
				variable 'S3_activation'
				variable 'S1_deactivation'
				variable 'S2_deactivation'
				variable 'S3_deactivation'
				'mySequence_T1_enabled := S1_X and y1'()
				'mySequence_T2_enabled := S1_X and y2 and not mySequence_T1_enabled'()
				'mySequence_T3_enabled := S2_X and not y1'()
				'mySequence_T4_enabled := S3_X and true'()
				"S1_activation := not ${Converter.NOT_INIT_VARIABLE_NAME} or mySequence_T3_enabled or mySequence_T4_enabled"()
				'S1_deactivation := mySequence_T1_enabled or mySequence_T2_enabled'()
				SR(Q:'S1_X', S:"S1_activation", R:'S1_deactivation')
				SR(Q:'u5', S:'S1_X', R:'S1_deactivation') 
				'u2 := y1'('S1_deactivation')
				'S2_activation := mySequence_T1_enabled'()
				'S2_deactivation := mySequence_T3_enabled'()
				SR(Q:'S2_X', S:'S2_activation', R:'S2_deactivation')
				'u4 := S2_activation or u4'()
				'u1 := y1 or y2'('S2_X')
				'S3_activation := mySequence_T2_enabled'()
				'S3_deactivation := mySequence_T4_enabled'()
				SR(Q:'S3_X', S:'S3_activation', R:'S3_deactivation')
				'u3 := y2'('S3_activation')
				'u4 := not S3_activation and u4'()
				"${Converter.NOT_INIT_VARIABLE_NAME} := true"()
			}
		}
		ModuleSubject sfcModule = appSfc.toAutomata()
		ModuleSubject sfcLessModule = appSfcLess.toAutomata()
		Util.assertGeneratedModuleEqualsManual(sfcModule, sfcLessModule)
		if (openInSupremica) {
			Util.openModuleInSupremica(sfcModule)
		}

	}
	static void testSfcDeferred(openInSupremica) {
		def ccb = new ControlCodeBuilder()
		def appSfc = ccb.application('testSfcDeferred') {
			sequentialProgram('deferred', deferred:true) {
				sequence('sfcA') {
					Step ('A1')
					'B1.X'()
					Step('A2')
					'true'()
				}
				sequence('sfcB') {
					Step ('B1')
					'A1.X'()
					Step('B2')
					'true'()
				}
			}
		}
		def appSfcLess = ccb.application('testSfcDeferred') {
			logicProgram('deferred') {
				variable('A1_X', markedValue:true)
				variable 'A2_X'
				variable('B1_X', markedValue:true)
				variable 'B2_X'
				variable("${Converter.NOT_INIT_VARIABLE_NAME}", markedValue:true)
				variable 'sfcA_T1_enabled'
				variable 'sfcA_T2_enabled'
				variable 'sfcB_T1_enabled'
				variable 'sfcB_T2_enabled'
				variable 'A1_activation'
				variable 'A2_activation'
				variable 'B1_activation'
				variable 'B2_activation'
				variable 'A1_deactivation'
				variable 'A2_deactivation'
				variable 'B1_deactivation'
				variable 'B2_deactivation'
				'sfcA_T1_enabled := A1_X and B1_X'()
				'sfcA_T2_enabled := A2_X and true'()
				'sfcB_T1_enabled := B1_X and A1_X'()
				'sfcB_T2_enabled := B2_X and true'()
				"A1_activation := not ${Converter.NOT_INIT_VARIABLE_NAME} or sfcA_T2_enabled"()
				'A1_deactivation := sfcA_T1_enabled'()
				SR(Q:'A1_X', S:"A1_activation", R:'A1_deactivation')
				'A2_activation := sfcA_T1_enabled'()
				'A2_deactivation := sfcA_T2_enabled'()
				SR(Q:'A2_X', S:"A2_activation", R:'A2_deactivation')
				"B1_Activation := not ${Converter.NOT_INIT_VARIABLE_NAME} or sfcB_T2_enabled"()
				'B1_deactivation := sfcB_T1_enabled'()
				SR(Q:'B1_X', S:"B1_activation", R:'B1_deactivation')
				'B2_activation := sfcB_T1_enabled'()
				'B2_deactivation := sfcB_T2_enabled'()
				SR(Q:'B2_X', S:"B2_activation", R:'B2_deactivation')
				"${Converter.NOT_INIT_VARIABLE_NAME} := true"()
			}
		}
		ModuleSubject sfcModule = appSfc.toAutomata()
		ModuleSubject sfcLessModule = appSfcLess.toAutomata()
		Util.assertGeneratedModuleEqualsManual(sfcModule, sfcLessModule)
		if (openInSupremica) {
			Util.openModuleInSupremica(sfcModule)
		}

	}
	static void testAutomataGenerator(openInSupremica) {
		def ccb = new ControlCodeBuilder()
		def appStateless = ccb.application('testapp2') {
			input 'y1'
			input 'y2'
			input 'y3'
			output 'u3'
			output 'u7'
			variable 'FB1instance_x' 
			logicProgram('program') {
				'u7 := not y2 and u7 or (FB1instance_x and y3)'()
				'FB1instance_x := not y3 and FB1instance_x or y2'()
				'u3 := true'()
			}
			process('someProcess') {
				logicProgram('program') {
					'y1 := not (not u3) and y1 or (y2 and u3)'()
				}
			}
		}

		ModuleSubject correctModule = new ModuleBuilder().module('testapp2') {
			event(Converter.SCAN_CYCLE_EVENT_NAME, controllable:false)
			booleanVariable(['y1', 'y2', 'y3', 'u3', 'FB1instance_x', 'u7'], initial:false, marked:false)
			event([Converter.NO_PROCESS_CHANGE_EVENT_NAME, 'someProcess_change', 'Process_y2_change', 'Process_y3_change'], controllable:false)
			eventAlias(name:Converter.PROCESS_EVENTS_ALIAS_NAME, events:[Converter.NO_PROCESS_CHANGE_EVENT_NAME, 'someProcess_change', 'Process_y2_change', 'Process_y3_change'])
			plant(Converter.CONTROL_UNIT_PLANT_NAME) {
				state(Converter.START_OF_SCANCYCLE_STATE_NAME, marked:true) {
					outgoing(to:Converter.END_OF_SCANCYCLE_STATE_NAME, event:Converter.SCAN_CYCLE_EVENT_NAME) {
					}
				}
				state(Converter.END_OF_SCANCYCLE_STATE_NAME, marked:true) {
					outgoing(to:Converter.START_OF_SCANCYCLE_STATE_NAME, events:[Converter.PROCESS_EVENTS_ALIAS_NAME]) {
					}
				}
			}
			plant('ASSIGN_u3', defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME, deterministic:false) {
				state('q0', marked:true) {
					selfLoop(guard:'1') { set('u3') }
					selfLoop(guard:'!1') { reset('u3') }
				}
			}
			plant('ASSIGN_FB1instance_x', defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME, deterministic:false) {
				state('q0', marked:true) {
					selfLoop(guard:'!y3 & FB1instance_x | y2') { set('FB1instance_x') }
					selfLoop(guard:'!(!y3 & FB1instance_x | y2)') { reset('FB1instance_x') }
				}
			}
			plant('ASSIGN_u7', defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME, deterministic:false) {
				state('q0', marked:true) {
					selfLoop(guard:'!y2 & u7 | (FB1instance_x & y3)') { set('u7') }
					selfLoop(guard:'!(!y2 & u7 | (FB1instance_x & y3))') { reset('u7') }
				}
			}
			plant('ASSIGN_y1', defaultEvent:'someProcess_change', deterministic:false) {
				state('q0', marked:true) {
					selfLoop(guard:'u3 & y1 | (y2 & u3)') { set('y1') }
					selfLoop(guard:'!(u3 & y1 | (y2 & u3))') { reset('y1') }
				}
			}
			plant('ASSIGN_y2', defaultEvent:'Process_y2_change', deterministic:false) {
				state('q0', marked:true) {
					selfLoop(guard:'!y2') { set('y2') }
					selfLoop(guard:'y2') { reset('y2') }
				}
			}
			plant('ASSIGN_y3', defaultEvent:'Process_y3_change', deterministic:false) {
				state('q0', marked:true) {
					selfLoop(guard:'!y3') { set('y3') }
					selfLoop(guard:'y3') { reset('y3') }
				}
			}
		}
		
		ModuleSubject generatedModuleFromStateless = appStateless.toAutomata()
		Util.assertGeneratedModuleEqualsManual(generatedModuleFromStateless, correctModule)
		
		if (openInSupremica) {
			Util.openModuleInSupremica(generatedModuleFromStateless)
		}
	}
	static testFunctionBlocks(boolean openInSupremica) {
		def ccb = new ControlCodeBuilder()
		def app = ccb.application('functionBlockTest') {
			input 'y1'
			input 'y2'
			input 'y3'
			output 'u1'
			output 'u2'
			output 'u3'
			output 'u5'
			output 'u6'
			output 'u7'
			output 'u4'
			output 'u8'
			RS 'RS1'
			functionblock('FB1') {
				input 'y1'
				input 'y2'
				output 'u1'
				variable 'x'
				logicProgram('program') {
					SR(Q:'u1', S:'x AND y2', R:'y1')
					SR(Q:'x', S:'y1', R:'y2')
				}
			}
			logicProgram('program') {
				'u2 := not y1'()
				SR('SR1', S:'y1', R:'y2 and u2')
				'u5 := SR1.Q'()
				RS1(S:'y1', R:'y2 and u2', Q:'u6')
				P('Py2', in:'y2')
				'u4 := Py2.Q and y1'()
				FB1('FB1instance', y1:'y2', y2:'y3', u1:'u7')
				'u8 := y1 or y2'('y3')
			}
			process('someProcess') {
				variable 'x'
				logicProgram('program') {
					SR(Q:'y1', S:'y2 and u3 or x', R:'not u3')
					'x := not u2'()
				}
			}
			process('process3') {
				functionblock('ProcessFb') {
					input 'in'
					output 'q'
					logicProgram('program') {
						'q := not in'()
					}
				}
				logicProgram('program') {
					ProcessFb(q:'y3', in:'u2')
				}
			}
		}
		def appAssignmentOnly = ccb.application('functionBlockTest') {
			input 'y1'
			input 'y2'
			input 'y3'
			output 'u1'
			output 'u2'
			output 'u3'
			output 'u5'
			output 'u6'
			output 'u7'
			output 'u4'
			output 'u8'
			logicProgram('program') {
				variable 'FB1instance_u1'
				variable 'FB1instance_y1'
				variable 'FB1instance_y2'
				variable 'FB1instance_program_SR1_Q'
				variable 'FB1instance_program_SR1_S'
				variable 'FB1instance_program_SR1_R'
				variable 'FB1instance_program_SR2_Q'
				variable 'FB1instance_program_SR2_S'
				variable 'FB1instance_program_SR2_R'
				variable 'FB1instance_x'
				variable 'SR1_S'
				variable 'SR1_R'
				variable 'SR1_Q'
				variable 'RS1_Q'
				variable 'RS1_S'
				variable 'RS1_R'
				variable 'Py2_in'
				variable 'Py2_Q'
				variable 'Py2_old'
				'u2 := not y1'()
				'SR1_S := y1'()
				'SR1_R := y2 and u2'()
				'SR1_Q := not SR1_R and SR1_Q or SR1_S'()
				'u5 := SR1_Q'()
				'RS1_Q := u6'()
				'RS1_S := y1'()
				'RS1_R := y2 and u2'()
				'RS1_Q := not RS1_R and (RS1_Q or RS1_S)'()
				'u6 := RS1_Q'()
				'Py2_in := y2'()
				'Py2_Q := Py2_in and not Py2_old '()
				'Py2_old := Py2_in'()
				'u4 := Py2_Q and y1'()
				'FB1instance_u1 := u7'()
				'FB1instance_y1 := y2'()
				'FB1instance_y2 := y3'()
				'FB1instance_program_SR1_Q := FB1instance_u1'()
				'FB1instance_program_SR1_S := FB1instance_x and FB1instance_y2'()
				'FB1instance_program_SR1_R := FB1instance_y1'()
				'FB1instance_program_SR1_Q := not FB1instance_program_SR1_R and FB1instance_program_SR1_Q or FB1instance_program_SR1_S'()
				'FB1instance_u1 := FB1instance_program_SR1_Q'()
				'FB1instance_program_SR2_Q := FB1instance_x'()
				'FB1instance_program_SR2_S := FB1instance_y1'()
				'FB1instance_program_SR2_R := FB1instance_y2'()
				'FB1instance_program_SR2_Q := not FB1instance_program_SR2_R and FB1instance_program_SR2_Q or FB1instance_program_SR2_S'()
				'FB1instance_x := FB1instance_program_SR2_Q'()
				'u7 := FB1instance_u1'()
				'u8 := (y3) and (y1 or y2) or (not (y3) and u8)'()
			}
			process('someProcess') {
				variable 'x'
				logicProgram('program') {
					variable 'SR1_Q'
					variable 'SR1_S'
					variable 'SR1_R'
					'SR1_Q := y1'()
					'SR1_S := y2 and u3 or x'()
					'SR1_R := not u3'()
					'SR1_Q := not SR1_R and SR1_Q or SR1_S'()
					'y1 := SR1_Q'()
					'x := not u2'()
				}
			}
			process('process3') {
				logicProgram('program') {
					'y3 := not u2'()
				}
			}
		}
		ModuleSubject generatedModule = app.toAutomata()
		ModuleSubject generatedModuleFromAssignmentOnly = appAssignmentOnly.toAutomata()
		Util.assertGeneratedModuleEqualsManual(generatedModule, generatedModuleFromAssignmentOnly)
		if (openInSupremica) {
			Util.openModuleInSupremica(generatedModule)
		}
	}
	static testAssignment(boolean openInSupremica) {
		def ccb = new ControlCodeBuilder()
		def appAssignmentOnly = ccb.application('assignmentTest') {
			input 'y1'
			input 'y2'
			input 'y3'
			output 'u1'
			output 'u2'
			output 'u3'
			output 'u5'
			output 'u6'
			output 'u7'
			output 'u4'
			variable 'x2'
			variable 'SR1_S'
			variable 'SR1_R'
			variable 'SR1_Q'
			variable 'RS1_Q'
			variable 'RS1_S'
			variable 'RS1_R'
			variable 'Py2_in'
			variable 'Py2_Q'
			variable 'Py2_old'
			logicProgram('program') {
				'u2 := not y1'()
				'u1 := y1 and u2'()
				'SR1_S := y1'()
				'SR1_R := y2 and u2'()
				'SR1_Q := not SR1_R and SR1_Q or SR1_S'()
				'u5 := SR1_Q'()
				'RS1_Q := u6'()
				'RS1_S := y1'()
				'RS1_R := y2 and u2'()
				'RS1_Q := not RS1_R and (RS1_Q or RS1_S)'()
				'u6 := RS1_Q'()
				'Py2_in := y2'()
				'Py2_Q := Py2_in and not Py2_old '()
				'Py2_old := Py2_in'()
				'u4 := Py2_Q and y1'()
				'x2 := True'()
			}
			process('someProcess') {
				logicProgram('program') {
					'SR1_Q := y1'()
					'SR1_S := y2 and u3'()
					'SR1_R := not u3'()
					'SR1_Q := not SR1_R and SR1_Q or SR1_S'()
					'y1 := SR1_Q'()
				}
			}
		}
		def appStateless = ccb.application('assignmentTest') {
			input 'y1'
			input 'y2'
			input 'y3'
			output 'u1'
			output 'u2'
			output 'u3'
			output 'u5'
			output 'u6'
			output 'u7'
			output 'u4'
	//		variable 'x2'
	//		variable 'SR1_S'
	//		variable 'SR1_R'
			variable 'SR1_Q'
	//		variable 'RS1_Q'
	//		variable 'RS1_S'
	//		variable 'RS1_R'
	//		variable 'Py2_in'
	//		variable 'Py2_Q'
			variable 'Py2_old'
			logicProgram('program') {
				//'x2 := True'()
				'u4 := (y2 and not Py2_old) and y1'()
				'Py2_old := y2'()
				//'Py2_Q := y2 and not Py2_old'()
				//'Py2_in := y2'()
				'u6 := not (y2 and not y1) and (u6 or y1)'()
				//'RS1_Q := not (y2 and not y1) and (u6 or y1)'()
				//'RS1_R := y2 and not y1'()
				//'RS1_S := y1'()
				//'RS1_Q := u6'()
				'u5 := not (y2 and not y1) and SR1_Q or y1'()
				'SR1_Q := not (y2 and not y1) and SR1_Q or y1'()
				//'SR1_R := y2 and not y1'()
				//'SR1_S := y1'()
				'u1 := y1 and not y1'()
				'u2 := not y1'()
			}
			process('someProcess') {
				logicProgram('program') {
					//'SR1_Q := y1'()
					//'SR1_S := y2 and u3'()
					//'SR1_R := not u3'()
					//'SR1_Q := not (not u3) and y1 or (y2 and u3)'()
					'y1 := not (not u3) and y1 or (y2 and u3)'()
				}
			}
		}
		ModuleSubject generatedModuleFromAssignmentOnly = appAssignmentOnly.toAutomata()
		ModuleSubject generatedModuleFromStateless = appStateless.toAutomata()
		Util.assertGeneratedModuleEqualsManual(generatedModuleFromAssignmentOnly, generatedModuleFromStateless)
		if (openInSupremica) {
			Util.openModuleInSupremica(generatedModuleFromAssignmentOnly)
		}
	}
}
