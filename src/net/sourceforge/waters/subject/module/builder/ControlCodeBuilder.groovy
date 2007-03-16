package net.sourceforge.waters.subject.module.builder;

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

class Converter {
	static final SCAN_CYCLE_EVENT_NAME = 'scanProgram'
	static final PROCESS_SCAN_CYCLE_EVENT_NAME = 'scanProcess'
	final static END_OF_SCANCYCLE_VARIABLE_NAME = 'endOfScanCycle'
	final static START_OF_SCANCYCLE_STATE_NAME = 'startOfScanCycle'
	final static END_OF_SCANCYCLE_STATE_NAME = 'endOfScanCycle'
	final static DO_CONTROL_SIGNAL_CHANGE_EVENT_NAME = 'doSignalChange'
	final static SKIP_CONTROL_SIGNAL_CHANGE_EVENT_NAME = 'skipSignalChange'
	final static TIMEOUT_EVENTS_ALIAS_NAME = 'timeoutEvent'
	final static PROCESS_EVENTS_ALIAS_NAME = 'processEvent'
	final static CONTROL_UNIT_PLANT_NAME = 'ControlUnit'
	static final DEFAULT_PROCESS_PLANT_NAME = 'Process'
	static final SEPARATOR_PATTERN = /\./
	static final SEPARATOR = '.'
}

class Assignment extends Named {
	Expression input
	IdentifierExpression Q
	static final pattern = /(?i)assign(?:ment)?/
	static final defaultAttr = 'name'
	static final parentAttr = 'statements'

	def addToModule(ModuleBuilder mb, List statements, int indexToThis, eventName) {
		Scope scope = statements[indexToThis].scope
		Variable assignedVariable = scope.namedElement(Q)
		if (assignedVariable.assignmentAutomatonNeeded(statements, indexToThis)) {
			mb.booleanVariable(Q.toSupremicaSyntax(scope), initial:assignedVariable.value, marked:assignedVariable.value ? true : false)
			mb.plant("ASSIGN_${Q.toSupremicaSyntax(scope)}", defaultEvent:eventName, deterministic:false) {
				state('q0', marked:true) {
					selfLoop(guard:input.toSupremicaSyntax(scope, statements[0..<indexToThis])) { set(Q.toSupremicaSyntax(scope)) }
					selfLoop(guard:new Expression("not (${input})").toSupremicaSyntax(scope, statements[0..<indexToThis])) { reset(Q.toSupremicaSyntax(scope)) }
				}
			}
		}
	}
	List execute(Scope parent) {
		[[statement:this, scope:parent]]
	}
	List getNamedElements() { return [] }
	List getSubScopes() { return [] } 
}

class Input extends ExternalVariable {
	static final pattern = /(?i)input/
	static final defaultAttr = 'name'
	static final parentAttr = 'inputs'
	def formatEventName(Scope scope) {
		name.toSupremicaSyntax(scope) + '_change'
	}
}
class Output extends ExternalVariable {
	static final pattern = /(?i)output/
	static final defaultAttr = 'name'
	static final parentAttr = 'outputs'
}
abstract class ExternalVariable extends Variable {
	def assignmentAutomatonNeeded(List statements, int indexToThis) {
		def statementScope = statements[indexToThis].scope
		def variableScope = statementScope.scopeOf(statements[indexToThis].statement.Q)
		def fullNameOfThis = statementScope.fullNameOf(statements[indexToThis].statement.Q)
		def needed = variableScope.global 
		int i = 0
		needed |= statements[0..indexToThis].any{s ->
			s.scope.identifiersInExpression(s.scope.expand(s.statement.input, statements[0..<i++])).any{it == fullNameOfThis}
		}
		needed &= indexToThis == statements.size() - 1 || statements[indexToThis+1..-1].every{it.scope.fullNameOf(it.statement.Q) != fullNameOfThis}
	}
}
class InternalVariable extends Variable {
	static final pattern = /(?i)variable/
	static final defaultAttr = 'name'
	static final parentAttr = 'variables'
		def assignmentAutomatonNeeded(List statements, int indexToThis) {
		def statementScope = statements[indexToThis].scope
		def variableScope = statementScope.scopeOf(statements[indexToThis].statement.Q)
		def fullNameOfThis = statementScope.fullNameOf(statements[indexToThis].statement.Q)
		def needed = false 
		int i = 0
		needed |= statements[0..indexToThis].any{s ->
			statementScope.identifiersInExpression(s.scope.expand(s.statement.input, statements[0..<i++])).any{it == fullNameOfThis}
		}
		needed &= indexToThis == statements.size() - 1 || statements[indexToThis+1..-1].every{it.scope.fullNameOf(it.statement.Q) != fullNameOfThis}
	}
}

abstract class Variable extends Named {
	boolean value
}

class Expression {
	Expression(String text) {
		this.text = text?.trim()
	}
	final String text
	
	String toSupremicaSyntax(Scope scope, List programState) {
		String newExpr = scope.expand(this, programState).text
		def replaceWordOp = {oldOp, newOp ->
			newExpr.replaceAll(/(?i)\s*\b${oldOp}\b\s*/){"$newOp"}
		}
		newExpr = replaceWordOp(/true/, ' 1 ')
		newExpr = replaceWordOp(/false/, ' 0 ')
		newExpr = replaceWordOp(/and/, ' & ')
		newExpr = replaceWordOp(/or/, ' | ')
		newExpr = newExpr.replace(':=', '£££')
		newExpr = newExpr.replace('=', '==')
		newExpr = newExpr.replace('£££', '=')
		newExpr = replaceWordOp(/not/, ' !')
		newExpr = newExpr.replace('.', '_')
		newExpr = newExpr.replaceAll(/\(\s+/){'('}
		newExpr = newExpr.replaceAll(/\s+\)/){')'}
		newExpr.trim()
	}
	String toString() {
		return text
	}
	boolean equals(object) {
		if (!object) return false
		if (object instanceof Expression) {
			if (text == object.text || (text && object.text && text.toLowerCase() == object.text.toLowerCase())) return true
			return 
		}
		false
	}
	int hashCode() {
		return text.toLowerCase().hashCode()
	}
}
class Named {
	IdentifierExpression name
}

class IdentifierExpression extends Expression {
	IdentifierExpression(String expr) {
		super(expr)
	}
	IdentifierExpression leftMostPart() {
		def parts = text.split(Converter.SEPARATOR_PATTERN)
		if (parts.size() > 1) return new IdentifierExpression(parts[0]) 
		null
	}
	IdentifierExpression exceptLeftMostPart() {
		def parts = text.split(Converter.SEPARATOR_PATTERN)
		if (parts.size() > 1) return new IdentifierExpression(parts[1..-1].join(Converter.SEPARATOR)) 
		else return new IdentifierExpression(text)
	}
	IdentifierExpression rightMostPart() {
		new IdentifierExpression(text.split(Converter.SEPARATOR_PATTERN)[-1])
	}
	String toSupremicaSyntax(Scope scope) {
		super.toSupremicaSyntax(scope, [])
	}
}

class ControlCodeBuilder extends BuilderSupport {

	ControlCodeBuilder() {
		functionblock('SR') {
			input 'S'
			input 'R'
			output 'Q'
			mainProgram {
				'Q := not R and Q or S'()
			}
		}
		functionblock('RS') {
			input 'S'
			input 'R'
			output 'Q'
			mainProgram {
				'Q := not R and (Q or S)'()
			}
		}
		functionblock('P') {
			input 'in'
			output 'Q'
			variable 'in_old'
			mainProgram {
				'Q := in and not in_old'()
				'in_old := in'()
			}
		}
		functionblock('N') {
			input 'in'
			output 'Q'
			variable 'in_old'
			mainProgram {
				'Q := not in and in_old'()
				'in_old := in'()
			}
		}
	}
	List functionBlocks = []
	static void main(args) {
		testBuilder3(true)
		//builder.saveModuleToFile()
	}
	static {
		//println "controlcode builder test" 
		//testBuilder2(false)
	}
	
	static final SET = 'setQualifiers'
	static final RESET = 'resetQualifiers'
	static final NONSTORED = 'nonstoredQualifiers'
			
	static final NODE_TYPES = [MainProgram,
	                           FunctionBlock,
	                           FunctionBlockInstance,
	                           Sequence,
	                           Assignment,
	                           Process,
	                           Step,
	                           Transition,
	                           Input,
	                           Output,
	                           InternalVariable,
	                           [name:SET, pattern:/(?i)S|set/, defaultAttr:'name', parentAttr:SET],
	                           [name:RESET, pattern:/(?i)R|reset/, defaultAttr:'name', parentAttr:RESET],
	                           [name:NONSTORED, pattern:/(?i)N|nonstored/, defaultAttr:'name', parentAttr:NONSTORED]]

	def createNode(name){
		def type = NODE_TYPES.find{name ==~ it.pattern}
		if (!type)
			if (name =~ /\:=/) {
				def parts = name.split(/\:=/)
				return createNode('assignment', [name:"ASSIGN_${parts[0]}", Q:parts[0], input:parts[1]]) 
			} else return createNode('transition', name)
		createNode(name, [:], null)
	}

	def createNode(name, value){
		createNode(name, [:], value)
	}

	def createNode(name, Map attributes){
		def type = NODE_TYPES.find{name ==~ it.pattern}
//		println 'createNode, name:' << name << ' attributes:' << attributes << ' type:' << type.inspect()
		if (!type) type = functionBlocks.find{name ==~ it.namePattern}
		if (!type) return createNode('transition', attributes, name)
		//println type
		if (type instanceof Class) {
			def obj = type.newInstance()
			def setProperty  = {attribute ->
				if (attribute?.key) {
					def value
					Class propertyType = obj.metaClass.properties.find{it.name.toLowerCase() == attribute.key.toLowerCase()}?.type
					switch (propertyType) {
					case IdentifierExpression: value = new IdentifierExpression(attribute.value); break
					case Expression: value = new Expression(attribute.value); break
					default:
						value = attribute.value
					}
					obj[attribute.key] = value
				}
			}
			setProperty(attributes.find{it.key == 'type'})
			attributes.each{ setProperty(it) }
			if (obj instanceof FunctionBlock) functionBlocks << obj
			return obj
		}
		if (type instanceof FunctionBlock) {
			if (!attributes.name) attributes.name = "${type.name}_${attributes.Q}"
			return createNode('functionBlockInstance', [type:type, *:attributes])
		}
		[type:type, *:attributes] as Expando
	}

	def createNode(name, Map attributes, value){
		def type = NODE_TYPES.find{name ==~ it.pattern}
		if (!type) return createNode(name, [(FunctionBlockInstance.defaultAttr):value, *:attributes])
		createNode(name, [(type.defaultAttr):value, *:attributes])
	}
	
	void setParent(parent, child) {
		//println "setParent: parent: ${parent.inspect()}, child: ${child.inspect()}"
		def parentAttr = child instanceof Expando ? child.type.parentAttr : child.parentAttr
		if (parentAttr) {
			if (parent instanceof Expando && !parent[parentAttr]) parent[parentAttr] = []	
			if (parent[parentAttr] instanceof List) parent[parentAttr] << child
			else parent[parentAttr] = child
		}
	}
	
	void nodeCompleted(parent, node) {
//		println "nodeCompleted: parent: $parent, node: $node"
		switch (node) {
		case Sequence: node.transitions.findAll{!it.to}.each{it.to = node.steps[0].name}; break
		case Step: parent.transitions.findAll{!it.to}.each{it.to = node.name}; break
		case Transition: if (!node.from) node.from = parent.steps[-1].name; break
		}
	}
	
	public static testBuilder(openInSupremica) {
		def generateTestProgram = {defaultProcess ->
			def controlCodeBuilder = new ControlCodeBuilder();
			controlCodeBuilder.application(name:defaultProcess ? 'testprogramWithDefaultProcess' : 'testprogramWitCustomProcess', deferred:true) {
				input 'iStart'
				input 'iBallInGate'
				input 'iBallDown'
				input 'iBallUp'
				input('iLiftDown', value:true)
				input 'iLiftUp'
				input 'iSmallBall'
				input 'iBigBall'
				output 'qInGate'
				output 'qOutGate'
				output 'qUp'
				output 'qOut'
				output 'qMeasure'
				variable 'mBallIsBig'
				variable 'mBallBetweenGateAndLift'
				functionblock('SR') {
					input 'S'
					input 'R'
					output 'Q'
					mainProgram {
						'Q := not R and Q or S'()
					}
				}
				mainProgram {
					'qOutGate := iBallInGate and iLiftDown and Lift.S0 and not mBallBetweenGateAndLift'()
					SR(Q:'qInGate', S:'iStart and not iBallInGate and not qOutGate', R:'iBallInGate')
					sequence(name:'Lift') {
						STEP('S0'){
							R('qUp')
						}
						'iBallDown'()
						Step('S1'){S('qUp')}
						'BallMeasure.Done.X'()
						Step('S2'){N('qOut')}
						'not iBallUp'()
					}
					SEQuence('BallMeasure') {
						STEP('Init')
						TRAN 'iBallUp'
						STEP('Measuring') {S('qMeasure')}
						'iBigBall'()
						TRAN('iSmallBall', to:'SmallBallFound')
						STEP('BigBallFound') {S('mBallIsBig')}
						transition('true', to:'Done')
						STEP('SmallBallFound') {R('mBallIsBig')}
						'true'()
						STEP('Done') {Reset('qMeasure')}
						'!iBallUp'()
					}
				}
				if (!defaultProcess) {
					process {
						sequence(name:'Lift') {
							STEP('Down') {N('iLiftDown')}
							'qUp'()
							STEP('Middle')
							'not qUp'(to:'Down')
							'qUp'()
							STEP('Up') {N('iLiftUp')}
							'not qUp'(to:'Middle')
						}
					}
				}
			}
		}
		def correctProgram = { defaultProcess ->
			def moduleBuilder = new ModuleBuilder()
			moduleBuilder.module(defaultProcess ? 'testprogramWithDefaultProcess' : 'testprogramWitCustomProcess') {
				booleanVariable(['iStart', 'iBallInGate', 'iBallDown', 'iBallUp'], marked:false)
				booleanVariable('iLiftDown', initial:true, marked:true)
				booleanVariable('iLiftUp', marked:false)
				booleanVariable('iSmallBall', marked:false)
				booleanVariable('iBigBall', marked:false)
				booleanVariable(['qInGate', 'qOutGate', 'qUp', 'qOut', 'qMeasure'], marked:false)
				booleanVariable(['mBallIsBig', 'mBallBetweenGateAndLift'], marked:false)
				booleanVariable(Converter.END_OF_SCANCYCLE_VARIABLE_NAME, marked:true)
				booleanVariable('Lift_S0_X', initial:true, marked:true)
				booleanVariable(['Lift_S1_X', 'Lift_S2_X'], marked:false)
				booleanVariable('BallMeasure_Init_X', initial:true, marked:true)
				booleanVariable(['BallMeasure_Measuring_X', 'BallMeasure_BigBallFound_X', 'BallMeasure_SmallBallFound_X', 'BallMeasure_Done_X'], marked:false)
				event(Converter.SCAN_CYCLE_EVENT_NAME, controllable:false)
				if (!defaultProcess) {
					booleanVariable('Process_Lift_Down_X', initial:true, marked:true)
					booleanVariable(['Process_Lift_Middle_X', 'Process_Lift_Up_X'], initial:false, marked:false)
					event(Converter.PROCESS_SCAN_CYCLE_EVENT_NAME, controllable:false)
					eventAlias(name:Converter.PROCESS_EVENTS_ALIAS_NAME, events:[Converter.PROCESS_SCAN_CYCLE_EVENT_NAME])
				} else {
					event(['iStart_change', 'iBallInGate_change', 'iBallDown_change', 'iBallUp_change', 'iLiftDown_change', 'iLiftUp_change', 'iSmallBall_change', 'iBigBall_change'], controllable:false)
					eventAlias(name:Converter.PROCESS_EVENTS_ALIAS_NAME, events:['iStart_change', 'iBallInGate_change', 'iBallDown_change', 'iBallUp_change', 'iLiftDown_change', 'iLiftUp_change', 'iSmallBall_change', 'iBigBall_change'])
				}
				if (defaultProcess) {
					plant(Converter.DEFAULT_PROCESS_PLANT_NAME) {
						state('q0', marked:true) {
							selfLoop(event:'iStart_change') {action('iStart = !iStart')}
							selfLoop(event:'iBallInGate_change') {action('iBallInGate = !iBallInGate')}
							selfLoop(event:'iBallDown_change') {action('iBallDown = !iBallDown')}
							selfLoop(event:'iBallUp_change') {action('iBallUp = !iBallUp')}
							selfLoop(event:'iLiftDown_change') {action('iLiftDown = !iLiftDown')}
							selfLoop(event:'iLiftUp_change') {action('iLiftUp = !iLiftUp')}
							selfLoop(event:'iSmallBall_change') {action('iSmallBall = !iSmallBall')}
							selfLoop(event:'iBigBall_change') {action('iBigBall = !iBigBall')}
						}
					}
				}
				plant(Converter.CONTROL_UNIT_PLANT_NAME) {
					state(Converter.START_OF_SCANCYCLE_STATE_NAME, marked:true) {
						outgoing(to:Converter.END_OF_SCANCYCLE_STATE_NAME, event:Converter.SCAN_CYCLE_EVENT_NAME) {
							set(Converter.END_OF_SCANCYCLE_VARIABLE_NAME)
						}
					}
					state(Converter.END_OF_SCANCYCLE_STATE_NAME, marked:true) {
						outgoing(to:Converter.START_OF_SCANCYCLE_STATE_NAME, events:[Converter.PROCESS_EVENTS_ALIAS_NAME]) {
							reset(Converter.END_OF_SCANCYCLE_VARIABLE_NAME)
						}
					}
				}
				plant('ASSIGN_qOutGate', defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME, deterministic:false) {
					state('q0', marked:true) {
						selfLoop(guard:'iBallInGate & iLiftDown & Lift_S0 & !mBallBetweenGateAndLift') { set('qOutGate') }
						selfLoop(guard:'!(iBallInGate & iLiftDown & Lift_S0 & !mBallBetweenGateAndLift)') { reset('qOutGate') }
					}
				}
				plant('ASSIGN_qInGate', defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME, deterministic:false) {
					state('q0', marked:true) {
						selfLoop(guard:'!(iBallInGate) & qInGate | (iStart & !iBallInGate & !qOutGate)') { set('qInGate') }
						selfLoop(guard:'!(!(iBallInGate) & qInGate | (iStart & !iBallInGate & !qOutGate))') { reset('qInGate') }
					}
				}
				
				plant('Lift', defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME, deterministic:false) {
					state('S0', marked:true) {
						selfLoop(guard:'!(iBallDown)')
					}
					transition(guard:'iBallDown') {
						set('qUp')
						reset('Lift_S0_X')
						set('Lift_S1_X')
					}
					state('S1', marked:true) {
						selfLoop(guard:'!(BallMeasure_Done_X)')
					}
					transition(guard:'BallMeasure_Done_X') {
						set('qOut')
						reset('Lift_S1_X')
						set('Lift_S2_X')
					}
					state('S2', marked:true) {
						selfLoop(guard:'!(!iBallUp)')
						outgoing(to:'S0', guard:'!iBallUp') {
							reset('qUp')
							reset('qOut')
							reset('Lift_S2_X')
							set('Lift_S0_X')
						}
					}
				}
				plant(name:'BallMeasure', defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME, deterministic:false) {
					state('Init', marked:true) {
						selfLoop(guard:'!(iBallUp)')
					}
					transition(guard:'iBallUp') {
						set('qMeasure')
						reset('BallMeasure_Init_X')
						set('BallMeasure_Measuring_X')
					}
					state('Measuring', marked:true) {
						selfLoop(guard:'!(iBigBall) & !(iSmallBall)')
						outgoing(to:'BigBallFound', guard:'iBigBall') {
							set('mBallIsBig')
							reset('BallMeasure_Measuring_X')
							set('BallMeasure_BigBallFound_X')
						}
						outgoing(to:'SmallBallFound', guard:'iSmallBall') {
							reset('mBallIsBig')
							reset('BallMeasure_Measuring_X')
							set('BallMeasure_SmallBallFound_X')
						}
					}
					state('BigBallFound', marked:true) {
						selfLoop(guard:'!(true)')
						outgoing(to:'Done', guard:'true'){
							reset('qMeasure')
							reset('BallMeasure_BigBallFound_X')
							set('BallMeasure_Done_X')
						}
					}
					state('SmallBallFound', marked:true) {
						selfLoop(guard:'!(true)')
						outgoing(to:'Done', guard:'true') {
							reset('qMeasure')
							reset('BallMeasure_SmallBallFound_X')
							set('BallMeasure_Done_X')
						}
					}
					state('Done', marked:true) {
						selfLoop(guard:'!(!iBallUp)')
						outgoing(guard:'!iBallUp', to:'Init') {
							reset('BallMeasure_Done_X')
							set('BallMeasure_Init_X')
						}
					}
				}
				if (!defaultProcess) {
					plant('Process_Lift', defaultEvent:Converter.PROCESS_SCAN_CYCLE_EVENT_NAME, deterministic:false) {
						state('Down', marked:true) {
							selfLoop(guard:'!(qUp)')
						}
						transition(guard:'qUp') {
							reset('iLiftDown')
							reset('Process_Lift_Down_X')
							set('Process_Lift_Middle_X')
						}
						state('Middle', marked:true) {
							selfLoop(guard:'!(!qUp) & !(qUp)')
							outgoing(guard:'!qUp', to:'Down') {
								set('iLiftDown')
								reset('Process_Lift_Middle_X')
								set('Process_Lift_Down_X')
							}
							outgoing(to:'Up', guard:'qUp') {
								set('iLiftUp')
								reset('Process_Lift_Middle_X')
								set('Process_Lift_Up_X')
							}
						}
						state('Up', marked:true) {
							selfLoop(guard:'!(!qUp)')
							outgoing(guard:'!qUp', to:'Middle') {
								reset('iLiftUp')
								reset('Process_Lift_Up_X')
								set('Process_Lift_Middle_X')
							}
						}
					}
				}
			}
		}
		def programWithManualProcess = generateTestProgram(false)
		def programWithDefaultProcess = generateTestProgram(true)
		def correctProgramWithManualProcess = correctProgram(false)
		def correctProgramWithDefaultProcess = correctProgram(true)
		
		printscope programWithDefaultProcess.runtimeScope, 0
/*		Util.assertGeneratedModuleEqualsManual(programWithManualProcess.toAutomata(), correctProgramWithManualProcess)
		Util.assertGeneratedModuleEqualsManual(programWithDefaultProcess.toAutomata(), correctProgramWithDefaultProcess)
		if (openInSupremica) {
			Util.openModuleInSupremica(programWithManualProcess.toAutomata())
			Util.openModuleInSupremica(programWithDefaultProcess.toAutomata())
		}*/
	}
	static void testBuilder3(openInSupremica) {
		def ccb = new ControlCodeBuilder()
		def app = ccb.application('testapp2') {
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
			functionblock('FB1') {
				input 'y1'
				input 'y2'
				output 'u1'
				variable 'x'
				mainProgram {
					SR('SR1', Q:'u1', S:'x AND y2', R:'y1')
					SR('SR2', Q:'x', S:'y1', R:'y2')
				}
			}
			mainProgram {
				'u2 := not y1'()
				'u1 := y1 and u2'()
				SR('SR1', S:'y1', R:'y2 and u2')
				'u5 := SR1.Q'()
				RS('RS1', S:'y1', R:'y2 and u2', Q:'u6')
				P('Py2', in:'y2')
				'u4 := Py2.Q and y1'()
				'x2 := True'()
				'u3 := u1 or u2'()
				FB1('FB1instance', y1:'y2', y2:'y3', u1:'u7')
			}
		}
		ModuleSubject correctModule = new ModuleBuilder().module('testapp2') {
			event(Converter.SCAN_CYCLE_EVENT_NAME, controllable:false)
			booleanVariable(['y1', 'y2', 'y3', 'u2', 'u1', 'SR1_Q', 'u5', 'u6', 'Py2_in_old', 'u4', 'u3', 'FB1instance_x', 'u7'], initial:false, marked:false)
			event(['y1_change', 'y2_change', 'y3_change'], controllable:false)
			eventAlias(name:Converter.PROCESS_EVENTS_ALIAS_NAME, events:['y1_change', 'y2_change', 'y3_change'])
			plant(Converter.DEFAULT_PROCESS_PLANT_NAME) {
				state('q0', marked:true) {
					selfLoop(event:'y1_change') {action('y1 = !y1')}
					selfLoop(event:'y2_change') {action('y2 = !y2')}
					selfLoop(event:'y3_change') {action('y3 = !y3')}
				}
			}
			plant(Converter.CONTROL_UNIT_PLANT_NAME) {
				state(Converter.START_OF_SCANCYCLE_STATE_NAME, marked:true) {
					outgoing(to:Converter.END_OF_SCANCYCLE_STATE_NAME, event:Converter.SCAN_CYCLE_EVENT_NAME) {
					//	set(Converter.END_OF_SCANCYCLE_VARIABLE_NAME)
					}
				}
				state(Converter.END_OF_SCANCYCLE_STATE_NAME, marked:true) {
					outgoing(to:Converter.START_OF_SCANCYCLE_STATE_NAME, events:[Converter.PROCESS_EVENTS_ALIAS_NAME]) {
					//	reset(Converter.END_OF_SCANCYCLE_VARIABLE_NAME)
					}
				}
			}
			plant('ASSIGN_u2', defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME, deterministic:false) {
				state('q0', marked:true) {
					selfLoop(guard:'!y1') { set('u2') }
					selfLoop(guard:'!(!y1)') { reset('u2') }
				}
			}
			plant('ASSIGN_u1', defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME, deterministic:false) {
				state('q0', marked:true) {
					selfLoop(guard:'y1 & !y1') { set('u1') }
					selfLoop(guard:'!(y1 & !y1)') { reset('u1') }
				}
			}
			plant('ASSIGN_SR1_Q', defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME, deterministic:false) {
				state('q0', marked:true) {
					selfLoop(guard:'!(y2 & !y1) & SR1_Q | y1') { set('SR1_Q') }
					selfLoop(guard:'!(!(y2 & !y1) & SR1_Q | y1)') { reset('SR1_Q') }
				}
			}
			plant('ASSIGN_u5', defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME, deterministic:false) {
				state('q0', marked:true) {
					selfLoop(guard:'!(y2 & !y1) & SR1_Q | y1') { set('u5') }
					selfLoop(guard:'!(!(y2 & !y1) & SR1_Q | y1)') { reset('u5') }
				}
			}
			plant('ASSIGN_u6', defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME, deterministic:false) {
				state('q0', marked:true) {
					selfLoop(guard:'!(y2 & !y1) & (u6 | y1)') { set('u6') }
					selfLoop(guard:'!(!(y2 & !y1) & (u6 | y1))') { reset('u6') }
				}
			}
			plant('ASSIGN_Py2_in_old', defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME, deterministic:false) {
				state('q0', marked:true) {
					selfLoop(guard:'y2') { set('Py2_in_old') }
					selfLoop(guard:'!(y2)') { reset('Py2_in_old') }
				}
			}
			plant('ASSIGN_u4', defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME, deterministic:false) {
				state('q0', marked:true) {
					selfLoop(guard:'(y2 & !Py2_in_old) & y1') { set('u4') }
					selfLoop(guard:'!((y2 & !Py2_in_old) & y1)') { reset('u4') }
				}
			}
			plant('ASSIGN_u3', defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME, deterministic:false) {
				state('q0', marked:true) {
					selfLoop(guard:'(y1 & !y1) | !y1') { set('u3') }
					selfLoop(guard:'!((y1 & !y1) | !y1)') { reset('u3') }
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
		}
		
		ModuleSubject generatedModule = app.toAutomata()
		Util.assertGeneratedModuleEqualsManual(generatedModule, correctModule)
		
		if (openInSupremica) {
			Util.openModuleInSupremica(generatedModule)
		}
	}
}
