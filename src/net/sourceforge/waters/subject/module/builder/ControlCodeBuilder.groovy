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
	final static SENSOR_EVENTS_ALIAS_NAME = 'sensorEvent'
	final static CONTROL_UNIT_PLANT_NAME = 'ControlUnit'
	static final DEFAULT_PROCESS_PLANT_NAME = 'Process'
	static final SEPARATOR_PATTERN = /\./
	static final SEPARATOR = '.'
		
	static String toSupremicaSyntax(String expr) {
		String newExpr = expr
		def replaceWordOp = {oldOp, newOp ->
			newExpr.replaceAll(/(?i)\s*\b${oldOp}\b\s*/){"$newOp"}
		}
		newExpr = replaceWordOp(/and/, ' & ')
		newExpr = replaceWordOp(/or/, ' | ')
		newExpr = newExpr.replace(':=', '£££')
		newExpr = newExpr.replace('=', '==')
		newExpr = newExpr.replace('£££', '=')
		newExpr = replaceWordOp(/not/, ' !')
		newExpr = newExpr.replace('.', '_')
		newExpr.trim()
	}
}

class Application extends Scope {
	boolean deferred = true
	static final pattern = /(?i)application/
	static final defaultAttr = 'name'
	List inputs = []
	List outputs = []
	List variables = []
	MainProgram mainProgram
	Process process
	def addToModule(ModuleSubject module, boolean generateDefaultProcessModel = true) {
		ModuleBuilder mb = new ModuleBuilder()
		mb.module(module) {
			[*inputs, *outputs, *variables].grep{it}.each { variable ->
				mb.booleanVariable(variable.name.toSupremicaSyntax(), initial:variable.value, marked:variable.value ? true : false)
			}
			event(Converter.SCAN_CYCLE_EVENT_NAME, controllable:false)
			event(Converter.PROCESS_SCAN_CYCLE_EVENT_NAME, controllable:false)
			event(inputs.collect{it.formatEventName()}, controllable:false)
			eventAlias(Converter.SENSOR_EVENTS_ALIAS_NAME, events:inputs.collect{it.formatEventName()})
			eventAlias(Converter.TIMEOUT_EVENTS_ALIAS_NAME, events:[])
			booleanVariable(Converter.END_OF_SCANCYCLE_VARIABLE_NAME, marked:true)
			plant(Converter.DEFAULT_PROCESS_PLANT_NAME) {
				state('q0', marked:true) {
					inputs.each{input -> mb.selfLoop(event:input.formatEventName()){mb.action(Converter.toSupremicaSyntax("${input.name} := not ${input.name}"))}}
				}
			}
			plant(Converter.CONTROL_UNIT_PLANT_NAME) {
				state(Converter.START_OF_SCANCYCLE_STATE_NAME, marked:true) 
				transition(event:Converter.SCAN_CYCLE_EVENT_NAME) { mb.set(Converter.END_OF_SCANCYCLE_VARIABLE_NAME) }
				state(Converter.END_OF_SCANCYCLE_STATE_NAME, marked:true) {
					outgoing(to:Converter.START_OF_SCANCYCLE_STATE_NAME, events:[Converter.SENSOR_EVENTS_ALIAS_NAME, Converter.TIMEOUT_EVENTS_ALIAS_NAME]) {
						reset(Converter.END_OF_SCANCYCLE_VARIABLE_NAME)
					}
				}
			}
			mainProgram?.addToModule(mb)
			process?.addToModule(mb)
		}
	}
	def toAutomata(boolean generateDefaultProcessModel = true) {
		addToModule(new ModuleBuilder().module(name.toSupremicaSyntax()), generateDefaultProcessModel)
	}
}

class MainProgram {
	boolean deferred = true
	static final pattern = /(?i)mainProgram?/
	static final defaultAttr = null
	static final parentAttr = 'mainProgram'
	List sequences = []
	List functionBlockInstances  = []
	def addToModule(ModuleBuilder mb) {
		functionBlockInstances.each { it.addToModule(mb) }
		sequences.each { it.addToModule(mb, Converter.SCAN_CYCLE_EVENT_NAME)	}
	}
}

class Process extends Scope {
	static final pattern = /(?i)process?/
	static final defaultAttr = null
	static final parentAttr = 'process' 
	Process() {
		name = new IdentifierExpression('Process')
	}
	List sequences = []
	List functionBlockInstances  = []
	def addToModule(ModuleBuilder mb) {
		functionBlockInstances.each { it.addToModule(mb) }
		sequences.each { it.addToModule(mb, Converter.PROCESS_SCAN_CYCLE_EVENT_NAME) }
	}
}

class Sequence extends Scope {
	static final pattern = /(?i)sequence/
	static final defaultAttr = 'name'
	static final parentAttr = 'sequences'
	List steps = []
	List transitions = []
	def addToModule(ModuleBuilder mb, String scanEvent) {
		steps.each { step ->
			mb.booleanVariable(new IdentifierExpression(step, 'X').toSupremicaSyntax(), initial:step == steps[0], marked:step == steps[0])
		}
		println 'AAAAAAAAAAAAAA'
		println this.dump()
		println name.toSupremicaSyntax()
		println 'BBBBBBBBBBBBBB'
		mb.plant(supremicaName, defaultEvent:scanEvent, deterministic:false) {
			steps.each { step ->
				mb.state(step.name.text, marked:true) {
					transitions.findAll{it.from == step.name}.each { outgoing ->
						mb.selfLoop(guard:"!(${outgoing.guard.toSupremicaSyntax()})")
						mb.outgoing(to:outgoing.to.toSupremicaSyntax(), guard:outgoing.guard.toSupremicaSyntax()) {
							def targetStep = steps.find{it.name==outgoing.to}
							targetStep.resetQualifiers.each{mb.reset(new IdentifierExpression(it.name).toSupremicaSyntax(this))}
							[*targetStep.setQualifiers, *targetStep.nonstoredQualifiers].grep{it}.each{mb.set(new IdentifierExpression(it.name).toSupremicaSyntax(this))}
							step.nonstoredQualifiers.each{mb.reset(new IdentifierExpression(it.name).toSupremicaSyntax(this))}
							reset(new IdentifierExpression(step, 'X').toSupremicaSyntax())
							set(new IdentifierExpression(targetStep, 'X').toSupremicaSyntax())
						}
					}
				}
			}
		}
	}
}

class Step extends Named {
	static final pattern = /(?i)step/
	static final defaultAttr = 'name'
	static final parentAttr = 'steps'
	List setQualifiers = []
	List resetQualifiers = []
	List nonstoredQualifiers = []
}

class Transition extends Named {
	static final pattern = /(?i)tran(?:sition)?/
	static final defaultAttr = 'guard'
	static final parentAttr = 'transitions'
	IdentifierExpression from
	IdentifierExpression to
	Expression guard
}

class TimerOn extends Named {
	String input
	static final pattern = /(?i)ton|timerOn/
	static final defaultAttr = 'name'
	static final parentAttr = 'functionBlockInstances'
	static final isScope = false
	def formatTimeoutEventName() { Converter.toSupremicaSyntax("${name}_true") }
	def addToModule(ModuleBuilder mb) {
		mb.booleanVariable(Converter.toSupremicaSyntax("${name}.Q"), initial:false, marked: false)
		mb.event(Converter.toSupremicaSyntax("${name}_true"), controllable:false)
		mb.eventAlias(Converter.TIMEOUT_EVENTS_ALIAS_NAME, events:[formatTimeoutEventName()])
		mb.plant(Converter.toSupremicaSyntax("TON_$name"), defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME, deterministic:false) {
			state('ready', marked:true) {
				selfLoop(guard:Converter.toSupremicaSyntax("not (${input})"))
				outgoing(to:'running', guard:Converter.toSupremicaSyntax(input))
			}
			state('running', marked:true) {
				selfLoop(guard:Converter.toSupremicaSyntax(input))
				outgoing(guard:Converter.toSupremicaSyntax("not (${input})"), to:'ready')
				outgoing(to:'elapsed', event:formatTimeoutEventName()) { mb.set(Converter.toSupremicaSyntax("${name}.Q")) }
			}
			state('elapsed', marked:true) {
				selfLoop(guard:Converter.toSupremicaSyntax(input))
				outgoing(guard:Converter.toSupremicaSyntax("not (${input})"), to:'ready') { mb.reset(Converter.toSupremicaSyntax("${name}.Q")) }
			}
		}
	}
}

class Assignment extends Named {
	String input
	static final pattern = /(?i)assign(?:ment)?/
	static final defaultAttr = 'name'
	static final parentAttr = 'functionBlockInstances'
	static final isScope = false
	def addToModule(ModuleBuilder mb) {
		mb.plant(Converter.toSupremicaSyntax("ASSIGN_$name"), defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME, deterministic:false) {
			state('q0', marked:true) {
				selfLoop(guard:Converter.toSupremicaSyntax(input)) { set(name.toSupremicaSyntax()) }
				selfLoop(guard:Converter.toSupremicaSyntax("not (${input})")) { reset(name.toSupremicaSyntax()) }
			}
		}
	}
}

class SetReset extends Named {
	String set
	String reset
	static final pattern = /(?i)SR/
	static final defaultAttr = 'name'
	static final parentAttr = 'functionBlockInstances'
	def addToModule(ModuleBuilder mb) {
		mb.plant(Converter.toSupremicaSyntax("SR_$name"), defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME, deterministic:false) {
			state('false', marked:true) {
				outgoing(to:'true', guard:Converter.toSupremicaSyntax(set)){ set(name.toSupremicaSyntax()) }
				selfLoop(guard:new Expression("not (${set})").toSupremicaSyntax()) { reset(name.toSupremicaSyntax()) }
			}
			state('true', marked:true) {
				outgoing(guard:Converter.toSupremicaSyntax("$reset and not ($set)"), to:'false'){ reset(name.toSupremicaSyntax()) }
				selfLoop(guard:Converter.toSupremicaSyntax("not (${reset})")) { set(name.toSupremicaSyntax()) }
			}
		}
	}
}
class Input extends Named {
	static final pattern = /(?i)input/
	static final defaultAttr = 'name'
	static final parentAttr = 'inputs'
	def formatEventName() {
		name.toSupremicaSyntax() + '_change'
	}
	boolean value
}
class Output extends Named {
	static final pattern = /(?i)output/
	static final defaultAttr = 'name'
	static final parentAttr = 'outputs'
	boolean value
}
class Variable extends Named {
	static final pattern = /(?i)variable/
	static final defaultAttr = 'name'
	static final parentAttr = 'variables'
	boolean value
}
class Expression {
	Expression(text) {
		this.text = text
	}
	String text
	
	String toSupremicaSyntax() {
		Converter.toSupremicaSyntax(text)
	}
	String toString() {
		return text
	}
	def addToModule(ModuleBuilder mb) {
		
	}
	Expression asGlobal(Map scopeNameMap) {
		String newExpr = text
		scopeNameMap.each {scope, names ->
			names.each{name ->
				newExpr = newExpr.replaceAll(/\b$name\b/) {"$scope.$name"}
			}
		}
		new Expression(newExpr)
	}
	boolean equals(object) {
		if (!object) return false
		if (object instanceof Expression) return text.toLowerCase() == object.text.toLowerCase()
		false
	}
	
	int hashCode() {
		return text.toLowerCase().hashCode()
	}
	
	static {
		def local = new Expression('(apa and bepa) or cepa')
		def global = local.asGlobal([repa:['bepa', 'cepa']])
		assert global.text == '(apa and repa.bepa) or repa.cepa'
	}
}

class IdentifierExpression extends Expression {
	IdentifierExpression(String expr) {
		super(expr)
	}
	IdentifierExpression(Named prefix, String suffix) {
		super("${prefix.fullName}${Converter.SEPARATOR}$suffix")
	}
	IdentifierExpression leftMostPart() {
		def parts = text.split(Converter.SEPARATOR_PATTERN)
		if (parts.size() > 1) return new IdentifierExpression(parts[0]) 
		null
	}
	IdentifierExpression exceptLeftMostPart() {
		def parts = text.split(Converter.SEPARATOR_PATTERN)
		println parts
		if (parts.size() > 1) return new IdentifierExpression(parts[1..-1].join(Converter.SEPARATOR)) 
		this
	}
	Named evaluate(Scope scope) {
		scope.namedObject(this)
	}
	IdentifierExpression fullName(Scope scope) {
		new IdentifierExpression(evaluate(scope).fullName)
	}
	String toSupremicaSyntax(Scope scope) {
		fullName(scope).toSupremicaSyntax()
	}
}

class Named {
	IdentifierExpression name
	def scope = this
	String getFullName() {
		println '<<<< ' << this.dump()
		print scope
		println ' >>>>'
		scope.parentScope ? "${scope.fullName}${Converter.SEPARATOR}$name" : name
	}
	String getSupremicaName() {
		name.toSupremicaSyntax(scope)
	}
}

class Scope extends Named {
	List namedObjects = []
	Scope parentScope
	void setScope(Scope scope){}
	def namedObject(IdentifierExpression expr) {
		if (!expr) return null
		def obj
		println '\\\\\\\\\\\\'
		println expr
		println expr.leftMostPart()
		println expr.exceptLeftMostPart()
		println namedObjects
		println this.dump()
		println '////////'
		def subScope = namedObject(expr.leftMostPart())
		if (subScope) obj = subScope.namedObject(expr.exceptLeftMostPart())
		else {
			obj = namedObjects.find{it.name == expr}
			if (!obj) obj = parentScope?.namedObject(expr)
		}
		obj
	}
}

class ControlCodeBuilder extends BuilderSupport {

	static void main(args) {
		def program = testBuilder()
		//builder.saveModuleToFile()
		Util.openModuleInSupremica(program.toAutomata())
	}
	static {
		println "controlcode builder test" 
		testBuilder()
	}
	
	def currentScope = null
	
	static final SET = 'setQualifiers'
	static final RESET = 'resetQualifiers'
	static final NONSTORED = 'nonstoredQualifiers'
			
	static final NODE_TYPES = [Application,
	                           MainProgram,
	                           Sequence,
	                           Assignment,
	                           SetReset,
	                           TimerOn,
	                           Process,
	                           Step,
	                           Transition,
	                           Input,
	                           Output,
	                           Variable,
	                           [name:SET, pattern:/(?i)S|set/, defaultAttr:'name', parentAttr:SET],
	                           [name:RESET, pattern:/(?i)R|reset/, defaultAttr:'name', parentAttr:RESET],
	                           [name:NONSTORED, pattern:/(?i)N|nonstored/, defaultAttr:'name', parentAttr:NONSTORED]]

	def createNode(name){
		def type = NODE_TYPES.find{name ==~ it.pattern}
		if (!type) return createNode('transition', name)
		createNode(name, [:], null)
	}

	def createNode(name, value){
		createNode(name, [:], value)
	}

	def createNode(name, Map attributes){
		println name << attributes
		def type = NODE_TYPES.find{name ==~ it.pattern}
		if (!type) return createNode('transition', attributes, name)
		if (type instanceof Class) {
			println type
			def obj = type.newInstance()
			attributes.each{ attribute ->
				if (attribute.key) {
					def value
					Class propertyType = obj.metaClass.properties.find{it.name == attribute.key}.type
					switch (propertyType) {
					case IdentifierExpression: value = new IdentifierExpression(attribute.value); break;
					case Expression: value = new Expression(attribute.value); break;
					default:
						value = attribute.value
					}
					obj.setProperty(attribute.key, value)
				}
			}
			if (obj instanceof Named && currentScope) {
				obj.scope = currentScope
				currentScope.namedObjects << obj
			}
			if (obj instanceof Scope) {
				println '---->' << obj << '<----' 
				println '---->' << (currentScope ? currentScope : 'null') << '<----' 
				obj.parentScope = currentScope
				currentScope = obj
			}
			return obj
		}
		[type:type, *:attributes] as Expando
	}

	def createNode(name, Map attributes, value){
		def type = NODE_TYPES.find{name ==~ it.pattern}
		if (!type) return null//createNode('transition', name)
		createNode(name, [(type.defaultAttr):value, *:attributes])
	}
	
	void setParent(parent, child) {
		println 'parent:' << parent.inspect()
		println 'child:' << child.inspect()
		def parentAttr = child instanceof Expando ? child.type.parentAttr : child.parentAttr
		if (parent instanceof Expando && !parent[parentAttr]) parent[parentAttr] = []	
		if (parent[parentAttr] instanceof List) parent[parentAttr] << child
		else parent[parentAttr] = child
	}
	
	void nodeCompleted(parent, node) {
		switch (node) {
		case Sequence: node.transitions.findAll{!it.to}.each{it.to = node.steps[0].name}; break
		case Step: parent.transitions.findAll{!it.to}.each{it.to = node.name}; break
		case Transition: if (!node.from) node.from = parent.steps[-1].name; break
		}
		if (node == currentScope) currentScope = currentScope.parentScope
	}
	
	public static testBuilder() {
		def controlCodeBuilder = new ControlCodeBuilder();
		def program = controlCodeBuilder.application(name:'testprogram', deferred:true) {
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
			mainProgram {
				TON(name:'Measuring_T_ge_1000', input:'BallMeasure.Measuring.X')
				SR('qInGate', set:'iStart and not iBallInGate and not qOutGate', reset:'iBallInGate')
				ASSIGN('qOutGate', input:'iBallInGate and iLiftDown and S0 and not mBallBetweenGateAndLift')
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
					'Measuring_T_ge_1000.Q AND iBigBall'()
					TRAN('Measuring_T_ge_1000.Q AND iSmallBall', to:'SmallBallFound')
					STEP('BigBallFound') {S('mBallIsBig')}
					transition('true', to:'Done')
					STEP('SmallBallFound') {R('mBallIsBig')}
					'true'()
					STEP('Done') {Reset('qMeasure')}
					'!iBallUp'()
				}
			}
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
		def moduleBuilder = new ModuleBuilder()
		def manualModule = moduleBuilder.module('testprogram') {
			booleanVariable(['iStart', 'iBallInGate', 'iBallDown', 'iBallUp'], marked:false)
			booleanVariable('iLiftDown', initial:true, marked:true)
			booleanVariable('iLiftUp', marked:false)
			booleanVariable('iSmallBall', marked:false)
			booleanVariable('iBigBall', marked:false)
			booleanVariable(['qInGate', 'qOutGate', 'qUp', 'qOut', 'qMeasure'], marked:false)
			booleanVariable(['mBallIsBig', 'mBallBetweenGateAndLift'], marked:false)
			booleanVariable(Converter.END_OF_SCANCYCLE_VARIABLE_NAME, marked:true)
			booleanVariable('Measuring_T_ge_1000_Q', marked:false)
			booleanVariable('Lift_S0_X', initial:true, marked:true)
			booleanVariable(['Lift_S1_X', 'Lift_S2_X'], marked:false)
			booleanVariable('BallMeasure_Init_X', initial:true, marked:true)
			booleanVariable(['BallMeasure_Measuring_X', 'BallMeasure_BigBallFound_X', 'BallMeasure_SmallBallFound_X', 'BallMeasure_Done_X'], marked:false)
			booleanVariable('Process_Lift_Down_X', initial:true, marked:true)
			booleanVariable(['Process_Lift_Middle_X', 'Process_Lift_Up_X'], initial:false, marked:false)
			event(Converter.SCAN_CYCLE_EVENT_NAME, controllable:false)
			event(Converter.PROCESS_SCAN_CYCLE_EVENT_NAME, controllable:false)
			event(['iStart_change', 'iBallInGate_change', 'iBallDown_change', 'iBallUp_change', 'iLiftDown_change', 'iLiftUp_change', 'iSmallBall_change', 'iBigBall_change'], controllable:false)
			event('Measuring_T_ge_1000_true', controllable:false)
			eventAlias(name:'sensorEvent', events:['iStart_change', 'iBallInGate_change', 'iBallDown_change', 'iBallUp_change', 'iLiftDown_change', 'iLiftUp_change', 'iSmallBall_change', 'iBigBall_change'])
			eventAlias(name:'timeoutEvent', events:['Measuring_T_ge_1000_true'])
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
			plant(Converter.CONTROL_UNIT_PLANT_NAME) {
				state(Converter.START_OF_SCANCYCLE_STATE_NAME, marked:true) {
					outgoing(to:Converter.END_OF_SCANCYCLE_STATE_NAME, event:Converter.SCAN_CYCLE_EVENT_NAME) {
						set(Converter.END_OF_SCANCYCLE_VARIABLE_NAME)
					}
				}
				state(Converter.END_OF_SCANCYCLE_STATE_NAME, marked:true) {
					outgoing(to:Converter.START_OF_SCANCYCLE_STATE_NAME, events:[Converter.SENSOR_EVENTS_ALIAS_NAME, Converter.TIMEOUT_EVENTS_ALIAS_NAME]) {
						reset(Converter.END_OF_SCANCYCLE_VARIABLE_NAME)
					}
				}
			}
			plant('TON_Measuring_T_ge_1000', defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME, deterministic:false) {
				state('ready', marked:true) {
					selfLoop(guard:'!(BallMeasure_Measuring_X)')
					outgoing(to:'running', guard:'BallMeasure_Measuring_X')
				}
				state('running', marked:true) {
					selfLoop(guard:'BallMeasure_Measuring_X')
					outgoing(to:'ready', guard:'!(BallMeasure_Measuring_X)')
					outgoing(to:'elapsed', event:'Measuring_T_ge_1000_true') { set('Measuring_T_ge_1000_Q') }
				}
				state('elapsed', marked:true) {
					selfLoop(guard:'BallMeasure_Measuring_X')
					outgoing(to:'ready', guard:'!(BallMeasure_Measuring_X)') { reset('Measuring_T_ge_1000_Q') }
				}
			}
			plant('SR_qInGate', defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME, deterministic:false) {
				state('false', marked:true) {
					outgoing(to:'true', guard:'iStart & !iBallInGate & !qOutGate') { set('qInGate') }
					selfLoop(guard:'!(iStart & !iBallInGate & !qOutGate)') { reset('qInGate') }
				}
				state('true', marked:true) {
					outgoing(to:'false', guard:'iBallInGate & !(iStart & !iBallInGate & !qOutGate)') { reset('qInGate') }
					selfLoop(guard:'!(iBallInGate)') { set('qInGate') }
				}
			}
			plant('ASSIGN_qOutGate', defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME, deterministic:false) {
				state('q0', marked:true) {
					selfLoop(guard:'iBallInGate & iLiftDown & S0 & !mBallBetweenGateAndLift') { set('qOutGate') }
					selfLoop(guard:'!(iBallInGate & iLiftDown & S0 & !mBallBetweenGateAndLift)') { reset('qOutGate') }
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
					selfLoop(guard:'!(Measuring_T_ge_1000_Q & iBigBall)')
					outgoing(to:'BigBallFound', guard:'Measuring_T_ge_1000_Q & iBigBall') {
						set('mBallIsBig')
						reset('BallMeasure_Measuring_X')
						set('BallMeasure_BigBallFound_X')
					}
					selfLoop(guard:'!(Measuring_T_ge_1000_Q & iSmallBall)')
					outgoing(to:'SmallBallFound', guard:'Measuring_T_ge_1000_Q & iSmallBall') {
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
					selfLoop(guard:'!(!qUp)')
					outgoing(guard:'!qUp', to:'Down') {
						set('iLiftDown')
						reset('Process_Lift_Middle_X')
						set('Process_Lift_Down_X')
					}
					selfLoop(guard:'!(qUp)')
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
		Util.assertGeneratedModuleEqualsManual(program.toAutomata(), manualModule)
		program
	}
}
