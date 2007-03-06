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
import net.sourceforge.waters.model.base.EqualCollection

class Converter {
	static final SCAN_CYCLE_EVENT_NAME = 'scan'
	final static END_OF_SCANCYCLE_VARIABLE_NAME = 'endOfScanCycle'
	final static START_OF_SCANCYCLE_STATE_NAME = 'startOfScanCycle'
	final static END_OF_SCANCYCLE_STATE_NAME = 'endOfScanCycle'
	final static DO_CONTROL_SIGNAL_CHANGE_EVENT_NAME = 'doSignalChange'
	final static SKIP_CONTROL_SIGNAL_CHANGE_EVENT_NAME = 'skipSignalChange'
	final static TIMEOUT_EVENTS_ALIAS_NAME = 'timeoutEvent'
	final static SENSOR_EVENTS_ALIAS_NAME = 'sensorEvent'
	final static CONTROL_UNIT_PLANT_NAME = 'ControlUnit'
	static final DEFAULT_PROCESS_PLANT_NAME = 'Process'

	static String toSupremicaSyntax(String expr) {
		String newExpr = expr
		def replaceWordOp = {oldOp, newOp ->
			newExpr.replaceAll(/(?i)\s*\b${oldOp}\b\s*/){"$newOp"}
		}
	//def replaceUnaryOp = {oldOp, newOp ->
		//	expr.replaceAll(/(?i)\s*\b${oldOp}\b\s*/){' $newOp'}
		//}
		newExpr = replaceWordOp(/and/, ' & ')
		newExpr = replaceWordOp(/or/, ' | ')
		newExpr = newExpr.replace(':=', '£££')
		newExpr = newExpr.replace('=', '==')
		newExpr = newExpr.replace('£££', '=')
		newExpr = replaceWordOp(/not/, ' !')
		//expr = expr.replaceAll(/(?i)\s*\band\b\s*/){' & '}
		//expr = expr.replaceAll(/(?i)(?:\s|\))+(and)(?:\s|\()+/){' & '}
		//{[and:' & ', or:' | ', ':=':' = ', '=':'=='][it.trim().toLowerCase()]}
		newExpr = newExpr.replace('.', '_')
		//expr = expr.replaceAll(/(?i)(?:^|\s|\())+not(?:\s|\())/){it[0] == '(' ? '!' : ' !' }
		newExpr.trim()
	}
}

class Program {
	String name
	boolean deferred = true
	static final pattern = /(?i)program/
	static final defaultAttr = 'name'
	List sequences = []
	List inputs = []
	List outputs = []
	List variables = []
	List functionBlockInstances  = []
	def addToModule(ModuleSubject module, boolean generateDefaultProcessModel = true) {
		ModuleBuilder mb = new ModuleBuilder()
		mb.module(module) {
			[*inputs, *outputs, *variables].grep{it}.each { variable ->
				mb.booleanVariable(Converter.toSupremicaSyntax(variable.name), initial:variable.value, marked:variable.value ? true : false)
			}
			event(Converter.SCAN_CYCLE_EVENT_NAME)
			event(inputs.collect{it.formatEventName()}, controllable:false)
			eventAlias(Converter.SENSOR_EVENTS_ALIAS_NAME, events:inputs.collect{it.formatEventName()})
			eventAlias(Converter.TIMEOUT_EVENTS_ALIAS_NAME, events:[])
			booleanVariable(Converter.END_OF_SCANCYCLE_VARIABLE_NAME, marked:true)
			plant(Converter.DEFAULT_PROCESS_PLANT_NAME) {
				state('q0', marked:true)
				inputs.each{input -> mb.selfLoop(event:input.formatEventName()){mb.action(Converter.toSupremicaSyntax("${input.name} := not ${input.name}"))}}
			}
			plant(Converter.CONTROL_UNIT_PLANT_NAME) {
				state(Converter.START_OF_SCANCYCLE_STATE_NAME, marked:true)
				transition(event:Converter.SCAN_CYCLE_EVENT_NAME) { mb.set(Converter.END_OF_SCANCYCLE_VARIABLE_NAME) }
				state(Converter.END_OF_SCANCYCLE_STATE_NAME, marked:true)
				transition(to:Converter.START_OF_SCANCYCLE_STATE_NAME, events:[Converter.SENSOR_EVENTS_ALIAS_NAME, Converter.TIMEOUT_EVENTS_ALIAS_NAME]) {
					reset(Converter.END_OF_SCANCYCLE_VARIABLE_NAME)
				}
			}
			functionBlockInstances.each { it.addToModule(mb) }
			sequences.each { it.addToModule(mb)	}
		}
	}
	def toAutomata(boolean generateDefaultProcessModel = true) {
		addToModule(new ModuleBuilder().module(name), generateDefaultProcessModel)
	}
}
class Sequence {
	String name
	static final pattern = /(?i)sequence/
	static final defaultAttr = 'name'
	static final parentAttr = 'sequences'
	List steps = []
	List transitions = []
	def addToModule(ModuleBuilder mb) {
		steps.each { step ->
			mb.booleanVariable(Converter.toSupremicaSyntax("${step.name}.X"), initial:step == steps[0], marked:step == steps[0])
		}
		mb.plant(name, defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME, deterministic:false) {
			steps.each { step ->
				mb.state(step.name, marked:true)
			}
			transitions.each { transition ->
				mb.selfLoop(from:transition.from, guard:"!(${Converter.toSupremicaSyntax(transition.guard)})")
				mb.transition(from:transition.from, to:transition.to, guard:Converter.toSupremicaSyntax(transition.guard)) {
					def targetStep = steps.find{it.name==transition.to}
					def sourceStep = steps.find{it.name==transition.from}
					targetStep.reset.each{mb.reset(it.name)}
					[*targetStep.set, *targetStep.nonstored].grep{it}.each{mb.set(it.name)}
					sourceStep.nonstored.each{mb.reset(it.name)}
					reset(Converter.toSupremicaSyntax("${sourceStep.name}.X"))
					set(Converter.toSupremicaSyntax("${targetStep.name}.X"))
				}
			}
		}
	}
}

class TimerOn {
	String name
	String input
	static final pattern = /(?i)ton|timerOn/
	static final defaultAttr = 'name'
	static final parentAttr = 'functionBlockInstances'
	def formatTimeoutEventName() { Converter.toSupremicaSyntax("${name}_true") }
	def addToModule(ModuleBuilder mb) {
		mb.booleanVariable(Converter.toSupremicaSyntax("${name}.Q"), initial:false, marked: false)
		mb.event(Converter.toSupremicaSyntax("${name}_true"), controllable:false)
		mb.eventAlias(Converter.TIMEOUT_EVENTS_ALIAS_NAME, events:[formatTimeoutEventName()])
		mb.plant(Converter.toSupremicaSyntax("TON_$name"), defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME, deterministic:false) {
			state('ready', marked:true)
			selfLoop(guard:Converter.toSupremicaSyntax("not (${input})"))
			transition(guard:Converter.toSupremicaSyntax(input))
			state('running', marked:true)
			selfLoop(guard:Converter.toSupremicaSyntax(input))
			transition(guard:Converter.toSupremicaSyntax("not (${input})"), to:'ready')
			transition(event:formatTimeoutEventName()) { mb.set(Converter.toSupremicaSyntax("${name}.Q")) }
			state('elapsed', marked:true)
			selfLoop(guard:Converter.toSupremicaSyntax(input))
			transition(guard:Converter.toSupremicaSyntax("not (${input})"), to:'ready') { mb.reset(Converter.toSupremicaSyntax("${name}.Q")) }
		}
	}
}

class Assignment {
	String name
	String input
	static final pattern = /(?i)assign(?:ment)?/
	static final defaultAttr = 'name'
	static final parentAttr = 'functionBlockInstances'
	def addToModule(ModuleBuilder mb) {
		mb.plant(Converter.toSupremicaSyntax("ASSIGN_$name"), defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME, deterministic:false) {
			state('q0', marked:true)
			selfLoop(guard:Converter.toSupremicaSyntax(input)) { set(Converter.toSupremicaSyntax(name)) }
			selfLoop(guard:Converter.toSupremicaSyntax("not (${input})")) { reset(Converter.toSupremicaSyntax(name)) }
		}
	}
}

class SetReset {
	String name
	String set
	String reset
	static final pattern = /(?i)SR/
	static final defaultAttr = 'name'
	static final parentAttr = 'functionBlockInstances'
	def addToModule(ModuleBuilder mb) {
		mb.plant(Converter.toSupremicaSyntax("SR_$name"), defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME, deterministic:false) {
			state('false', marked:true)
			transition(guard:Converter.toSupremicaSyntax(set)){ set(Converter.toSupremicaSyntax(name)) }
			selfLoop(guard:new Expression("not (${set})").toSupremicaSyntax()) { reset(Converter.toSupremicaSyntax(name)) }
			state('true', marked:true)
			transition(guard:Converter.toSupremicaSyntax("$reset and not ($set)"), to:'false'){ reset(Converter.toSupremicaSyntax(name)) }
			selfLoop(guard:Converter.toSupremicaSyntax("not (${reset})")) { set(Converter.toSupremicaSyntax(name)) }
		}
	}
}
class Expression {
	public Expression(String text = '') {
		this.text = text
	}
	String text
	
	String toSupremicaSyntax() {
		Converter.toSupremicaSyntax(text)
	}
	String toString() {return text}
	def addToModule(ModuleBuilder mb) {
		
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
	
	static final TRANSITION = 'transitions'
	static final STEP = 'steps'
	static final SET = 'set'
	static final RESET = 'reset'
	static final NONSTORED = 'nonstored'
	static final OUTPUT = 'outputs'
	static final INPUT = 'inputs'
	static final VARIABLE = 'variables'
			
	static final NODE_TYPES = [Program,
	                           Sequence,
	                           Assignment,
	                           SetReset,
	                           TimerOn,
	                           [name:STEP, pattern:/(?i)step/, defaultAttr:'name', parentAttr:STEP],
	                           [name:TRANSITION, pattern:/(?i)tran(?:sition)?/, defaultAttr:'guard', parentAttr:TRANSITION],
	                           [name:SET, pattern:/(?i)S|set/, defaultAttr:'name', parentAttr:SET],
	                           [name:RESET, pattern:/(?i)R|reset/, defaultAttr:'name', parentAttr:RESET],
	                           [name:NONSTORED, pattern:/(?i)N|nonstored/, defaultAttr:'name', parentAttr:NONSTORED],
	                           [name:INPUT, pattern:/(?i)input/, defaultAttr:'name', parentAttr:INPUT],
	                           [name:OUTPUT, pattern:/(?i)output/, defaultAttr:'name', parentAttr:OUTPUT],
	                           [name:VARIABLE, pattern:/(?i)variable/, defaultAttr:'name', parentAttr:VARIABLE]]
	                           
	def createNode(name){
		createNode('transition', name)
	}

	def createNode(name, value){
		createNode(name, [:], value)
	}

	def createNode(name, Map attributes){
		def type = NODE_TYPES.find{name ==~ it.pattern}
		if (!type) return null
		if (type instanceof Class) {
			def obj = type.newInstance()
			attributes.each{obj.setProperty(it.key, it.value)}
			return obj
		}
		[type:type, *:attributes] as Expando
	}

	def createNode(name, Map attributes, value){
		def type = NODE_TYPES.find{name ==~ it.pattern}
		if (!type) return null
		createNode(name, [(type.defaultAttr):value, *:attributes])
	}
	
	void setParent(parent, child) {
		println parent.inspect()
		println child.inspect()
		def parentAttr = child instanceof Expando ? child.type.parentAttr : child.parentAttr
		if (!parent[parentAttr]) parent[parentAttr] = []	
		parent[parentAttr] << child
	}
	
	void nodeCompleted(parent, node) {
		switch (node) {
		case Sequence: node.transitions.findAll{!it.to}.each{it.to = node.steps[0].name}; break
		case Expando:
			switch(node.type.name) {
			case TRANSITION:
				if (!node.from) node.from = parent.steps[-1].name
				break
			case STEP: parent.transitions.findAll{!it.to}.each{it.to = node.name}; break
			case INPUT: node.formatEventName = {Converter.toSupremicaSyntax("${node.name}_change")}; break
			}
		}
	}
	
	public static testBuilder() {
		def controlCodeBuilder = new ControlCodeBuilder();
		def program = controlCodeBuilder.program(name:'testprogram', deferred:true) {
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
			TON(name:'Measuring_T_ge_1000', input:'Measuring.X')
			SR('qInGate', set:'iStart and not iBallInGate and not qOutGate', reset:'iBallInGate')
			ASSIGN('qOutGate', input:'iBallInGate and iLiftDown and S0 and not mBallBetweenGateAndLift')
			sequence(name:'Lift') {
				STEP('S0'){R('qUp')}
				'iBallDown'()
				step('S1'){S('qUp')}
				'MeasureDone.X'()
				Step('S2'){N('qOut')}
				'not iBallUp'()
			}
			SEQuence('BallMeasure') {
				STEP('MeasureInit')
				TRAN 'iBallUp'
				STEP('Measuring') {S('qMeasure')}
				'Measuring_T_ge_1000.Q AND iBigBall'()
				TRAN('Measuring_T_ge_1000.Q AND iSmallBall', to:'SmallBallFound')
				STEP('BigBallFound') {S('mBallIsBig')}
				transition('true', to:'MeasureDone')
				STEP('SmallBallFound') {R('mBallIsBig')}
				'true'()
				STEP('MeasureDone') {Reset('qMeasure')}
				'!iBallUp'()
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
			booleanVariable('S0_X', initial:true, marked:true)
			booleanVariable(['S1_X', 'S2_X'], marked:false)
			booleanVariable('MeasureInit_X', initial:true, marked:true)
			booleanVariable(['Measuring_X', 'BigBallFound_X', 'SmallBallFound_X', 'MeasureDone_X'], marked:false)
			event(Converter.SCAN_CYCLE_EVENT_NAME)
			event(['iStart_change', 'iBallInGate_change', 'iBallDown_change', 'iBallUp_change', 'iLiftDown_change', 'iLiftUp_change', 'iSmallBall_change', 'iBigBall_change'], controllable:false)
			event('Measuring_T_ge_1000_true', controllable:false)
			eventAlias(name:'sensorEvent', events:['iStart_change', 'iBallInGate_change', 'iBallDown_change', 'iBallUp_change', 'iLiftDown_change', 'iLiftUp_change', 'iSmallBall_change', 'iBigBall_change'])
			eventAlias(name:'timeoutEvent', events:['Measuring_T_ge_1000_true'])
			plant(Converter.DEFAULT_PROCESS_PLANT_NAME) {
				state('q0', marked:true)
				selfLoop(event:'iStart_change') {action('iStart = !iStart')}
				selfLoop(event:'iBallInGate_change') {action('iBallInGate = !iBallInGate')}
				selfLoop(event:'iBallDown_change') {action('iBallDown = !iBallDown')}
				selfLoop(event:'iBallUp_change') {action('iBallUp = !iBallUp')}
				selfLoop(event:'iLiftDown_change') {action('iLiftDown = !iLiftDown')}
				selfLoop(event:'iLiftUp_change') {action('iLiftUp = !iLiftUp')}
				selfLoop(event:'iSmallBall_change') {action('iSmallBall = !iSmallBall')}
				selfLoop(event:'iBigBall_change') {action('iBigBall = !iBigBall')}
			}
			plant(Converter.CONTROL_UNIT_PLANT_NAME) {
				state(Converter.START_OF_SCANCYCLE_STATE_NAME, marked:true)
				transition(event:Converter.SCAN_CYCLE_EVENT_NAME) {
					set(Converter.END_OF_SCANCYCLE_VARIABLE_NAME)
				}
				state(Converter.END_OF_SCANCYCLE_STATE_NAME, marked:true)
				transition(events:[Converter.SENSOR_EVENTS_ALIAS_NAME, Converter.TIMEOUT_EVENTS_ALIAS_NAME], to:Converter.START_OF_SCANCYCLE_STATE_NAME) {
					reset(Converter.END_OF_SCANCYCLE_VARIABLE_NAME)
				}
			}
			plant('TON_Measuring_T_ge_1000', defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME, deterministic:false) {
				state('ready', marked:true)
				selfLoop(guard:'!(Measuring_X)')
				transition(guard:'Measuring_X')
				state('running', marked:true)
				selfLoop(guard:'Measuring_X')
				transition(guard:'!Measuring_X', to:'ready')
				transition(event:'Measuring_T_ge_1000_true') { set('Measuring_T_ge_1000_Q') }
				state('elapsed', marked:true)
				selfLoop(guard:'Measuring_X')
				transition(guard:'!Measuring_X', to:'ready') { reset('Measuring_T_ge_1000_Q') }
			}
			plant('SR_qInGate', defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME, deterministic:false) {
				state('false', marked:true)
				transition(guard:'iStart & !iBallInGate & !qOutGate') { set('qInGate') }
				selfLoop(guard:'!(iStart & !iBallInGate & !qOutGate)') { reset('qInGate') }
				state('true', marked:true)
				transition(guard:'iBallInGate & !(iStart & !iBallInGate & !qOutGate)', to:'false') { reset('qInGate') }
				selfLoop(guard:'!iBallInGate') { set('qInGate') }
			}
			plant('ASSIGN_qOutGate', defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME, deterministic:false) {
				state('q0', marked:true)
				selfLoop(guard:'iBallInGate & iLiftDown & S0 & !mBallBetweenGateAndLift') { set('qOutGate') }
				selfLoop(guard:'!(iBallInGate & iLiftDown & S0 & !mBallBetweenGateAndLift)') { reset('qOutGate') }
			}
			plant('Lift', defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME, deterministic:false) {
				state('S0', marked:true)
				selfLoop(guard:'!(iBallDown)')
				transition(guard:'iBallDown') {
					set('qUp')
					reset('S0_X')
					set('S1_X')
				}
				state('S1', marked:true)
				selfLoop(guard:'!(MeasureDone_X)')
				transition(guard:'MeasureDone_X') {
					set('qOut')
					reset('S1_X')
					set('S2_X')
				}
				state('S2', marked:true)
				selfLoop(guard:'!(!iBallUp)')
				transition(to:'S0', guard:'!iBallUp') {
					reset('qUp')
					reset('qOut')
					reset('S2_X')
					set('S0_X')
				}
			}
			plant(name:'BallMeasure', defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME, deterministic:false) {
				state('MeasureInit', marked:true)
				selfLoop(guard:'!(iBallUp)')
				transition(guard:'iBallUp') {
					set('qMeasure')
					reset('MeasureInit_X')
					set('Measuring_X')
				}
				state('Measuring', marked:true)
				selfLoop(guard:'!(Measuring_T_ge_1000_Q & iBigBall)')
				transition(guard:'Measuring_T_ge_1000_Q & iBigBall') {
					set('mBallIsBig')
					reset('Measuring_X')
					set('BigBallFound_X')
				}
				selfLoop(guard:'!(Measuring_T_ge_1000_Q & iSmallBall)')
				transition(guard:'Measuring_T_ge_1000_Q & iSmallBall', to:'SmallBallFound') {
					reset('mBallIsBig')
					reset('Measuring_X')
					set('SmallBallFound_X')
				}
				state('BigBallFound', marked:true)
				selfLoop(guard:'!(true)')
				transition(guard:'true', to:'MeasureDone'){
					reset('qMeasure')
					reset('BigBallFound_X')
					set('MeasureDone_X')
				}
				state('SmallBallFound', marked:true)
				selfLoop(guard:'!(true)')
				transition(guard:'true'){
					reset('qMeasure')
					reset('SmallBallFound_X')
					set('MeasureDone_X')
				}
				state('MeasureDone', marked:true)
				selfLoop(guard:'!(!iBallUp)')
				transition(guard:'!iBallUp', to:'MeasureInit') {
					reset('MeasureDone_X')
					set('MeasureInit_X')
				}
			}
		}
		Util.assertGeneratedModuleEqualsManual(program.toAutomata(), manualModule)
		program
	}
}
