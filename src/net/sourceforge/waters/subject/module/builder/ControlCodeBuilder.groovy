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
		
	private static String toSupremicaSyntax(String expr) {
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
	private addDefaultProcessModel(ModuleBuilder mb) {
		mb.event(inputs.collect{it.formatEventName()}, controllable:false)
		mb.eventAlias(Converter.PROCESS_EVENTS_ALIAS_NAME, events:inputs.collect{it.formatEventName()})
		mb.plant(Converter.DEFAULT_PROCESS_PLANT_NAME) {
			state('q0', marked:true) {
				inputs.each{input -> mb.selfLoop(event:input.formatEventName()){mb.action(new Expression(this, "${input.name} := not ${input.name}").toSupremicaSyntax())}}
			}
		}
	}

	def addToModule(ModuleSubject module) {
		ModuleBuilder mb = new ModuleBuilder()
		mb.module(module) {
			[*inputs, *outputs, *variables].grep{it}.each { variable ->
				mb.booleanVariable(variable.name.toSupremicaSyntax(), initial:variable.value, marked:variable.value ? true : false)
			}
			event(Converter.SCAN_CYCLE_EVENT_NAME, controllable:false)
			booleanVariable(Converter.END_OF_SCANCYCLE_VARIABLE_NAME, marked:true)
			if (process) {
				event(Converter.PROCESS_SCAN_CYCLE_EVENT_NAME, controllable:false)
				eventAlias(Converter.PROCESS_EVENTS_ALIAS_NAME, events:[Converter.PROCESS_SCAN_CYCLE_EVENT_NAME])
			} else {
				addDefaultProcessModel(mb)
			}
			plant(Converter.CONTROL_UNIT_PLANT_NAME) {
				state(Converter.START_OF_SCANCYCLE_STATE_NAME, marked:true) 
				transition(event:Converter.SCAN_CYCLE_EVENT_NAME) { mb.set(Converter.END_OF_SCANCYCLE_VARIABLE_NAME) }
				state(Converter.END_OF_SCANCYCLE_STATE_NAME, marked:true) {
					outgoing(to:Converter.START_OF_SCANCYCLE_STATE_NAME, events:[Converter.PROCESS_EVENTS_ALIAS_NAME]) {
						reset(Converter.END_OF_SCANCYCLE_VARIABLE_NAME)
					}
				}
			}
			mainProgram?.addToModule(mb)
			process?.addToModule(mb)
		}
	}
	def toAutomata() {
		addToModule(new ModuleBuilder().module(name.toSupremicaSyntax()))
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
		name = new IdentifierExpression(this, 'Process')
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
			mb.booleanVariable("${step.name.toSupremicaSyntax()}_X", initial:step == steps[0], marked:step == steps[0])
		}
		mb.plant(supremicaName, defaultEvent:scanEvent, deterministic:false) {
			steps.each { step ->
				mb.state(step.name.text, marked:true) {
					selfLoop(guard:new Expression(this, "!(${transitions.findAll{it.from == step.name}.guard.text.join(') and !(')})").toSupremicaSyntax())
					transitions.findAll{it.from == step.name}.each { outgoing ->
						mb.outgoing(to:outgoing.to.text, guard:outgoing.guard.toSupremicaSyntax()) {
							def targetStep = steps.find{it.name==outgoing.to}
							targetStep.resetQualifiers.each{mb.reset(new IdentifierExpression(this, it.name).toSupremicaSyntax())}
							[*targetStep.setQualifiers, *targetStep.nonstoredQualifiers].grep{it}.each{mb.set(new IdentifierExpression(this, it.name).toSupremicaSyntax())}
							step.nonstoredQualifiers.each{mb.reset(new IdentifierExpression(this, it.name).toSupremicaSyntax())}
							assert step.scope == step
							reset(new IdentifierExpression(step, 'X').toSupremicaSyntax())
							set(new IdentifierExpression(targetStep, 'X').toSupremicaSyntax())
						}
					}
				}
			}
		}
	}
}

class Step extends Scope {
	Step() {
		super()
		def x = new Variable()
		x.name = new IdentifierExpression(this, 'X')
		x.scope = this
		namedObjects << x
	}
	static final pattern = /(?i)step/
	static final defaultAttr = 'name'
	static final parentAttr = 'steps'
	List setQualifiers = []
	List resetQualifiers = []
	List nonstoredQualifiers = []
}

class Transition extends NamedImpl {
	static final pattern = /(?i)tran(?:sition)?/
	static final defaultAttr = 'guard'
	static final parentAttr = 'transitions'
	IdentifierExpression from
	IdentifierExpression to
	Expression guard
}

class TimerOn extends Scope {
	TimerOn() {
		def q = new Output()
		q.name = new IdentifierExpression(this, 'Q')
		q.scope = this
		namedObjects << q
	}
	String input
	static final pattern = /(?i)ton|timerOn/
	static final defaultAttr = 'name'
	static final parentAttr = 'functionBlockInstances'
	static final isScope = false
	def addToModule(ModuleBuilder mb) {
		mb.booleanVariable(new Expression(this, "${name}.Q").toSupremicaSyntax(), initial:false, marked: false)
		mb.plant("TON_${name.toSupremicaSyntax()}", defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME, deterministic:false) {
			state('ready', marked:true) {
				selfLoop(guard:new Expression(this, "not (${input})").toSupremicaSyntax())
				outgoing(to:'running', guard:new Expression(this, input).toSupremicaSyntax())
			}
			state('running', marked:true) {
				selfLoop(guard:new Expression(this, input).toSupremicaSyntax())
				outgoing(guard:new Expression(this, "not (${input})").toSupremicaSyntax(), to:'ready')
				outgoing(to:'elapsed') { mb.set(new Expression(this, "${name}.Q").toSupremicaSyntax()) }
			}
			state('elapsed', marked:true) {
				selfLoop(guard:new Expression(this, input).toSupremicaSyntax())
				outgoing(guard:new Expression(this, "not (${input})").toSupremicaSyntax(), to:'ready') { mb.reset(Converter.toSupremicaSyntax("${name}.Q")) }
			}
		}
	}
}

class Assignment extends NamedImpl {
	String input
	static final pattern = /(?i)assign(?:ment)?/
	static final defaultAttr = 'name'
	static final parentAttr = 'functionBlockInstances'
	static final isScope = false
	def addToModule(ModuleBuilder mb) {
		mb.plant("ASSIGN_${name.toSupremicaSyntax()}", defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME, deterministic:false) {
			state('q0', marked:true) {
				selfLoop(guard:new Expression(this, input).toSupremicaSyntax()) { set(name.toSupremicaSyntax()) }
				selfLoop(guard:new Expression(this, "not (${input})").toSupremicaSyntax()) { reset(name.toSupremicaSyntax()) }
			}
		}
	}
}

class SetReset extends NamedImpl {
	String set
	String reset
	static final pattern = /(?i)SR/
	static final defaultAttr = 'name'
	static final parentAttr = 'functionBlockInstances'
	def addToModule(ModuleBuilder mb) {
		mb.plant("SR_${name.toSupremicaSyntax()}", defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME, deterministic:false) {
			state('false', marked:true) {
				outgoing(to:'true', guard:new Expression(this, set).toSupremicaSyntax()){ set(name.toSupremicaSyntax()) }
				selfLoop(guard:new Expression(this, "not (${set})").toSupremicaSyntax()) { reset(name.toSupremicaSyntax()) }
			}
			state('true', marked:true) {
				outgoing(guard:new Expression(this, "$reset and not ($set)").toSupremicaSyntax(), to:'false'){ reset(name.toSupremicaSyntax()) }
				selfLoop(guard:new Expression(this, "not (${reset})").toSupremicaSyntax()) { set(name.toSupremicaSyntax()) }
			}
		}
	}
}
class Input extends NamedImpl {
	static final pattern = /(?i)input/
	static final defaultAttr = 'name'
	static final parentAttr = 'inputs'
	def formatEventName() {
		name.toSupremicaSyntax() + '_change'
	}
	boolean value
}
class Output extends NamedImpl {
	static final pattern = /(?i)output/
	static final defaultAttr = 'name'
	static final parentAttr = 'outputs'
	boolean value
}
class Variable extends NamedImpl {
	static final pattern = /(?i)variable/
	static final defaultAttr = 'name'
	static final parentAttr = 'variables'
	boolean value
}
class Expression {
	protected static final KEYWORDS = [/(?i)and/, /(?i)or/, /(?i)not/, /(?i)true/, /(?i)false/]
	Expression(Named context, String text) {
		this.context = context
		this.text = text
	}
	String text
	def context
	String fullyQualified() {
		text.replaceAll(/\b\w+\b(?:${Converter.SEPARATOR_PATTERN}\b\w+\b)*/) { word ->
			if (!KEYWORDS.any{keyword -> word ==~ keyword}) {
//				println '#######'
//				println 'Word:' << word << ' context:' << context << ' scope:' << context.scope 
//				println 'E#######'
				def obj = context.scope.namedObject(new IdentifierExpression(context, word))
				assert obj, "Undeclared identifier $word in expr '$text', context ${context.name} and scope ${context.scope.name}" 
				return context.scope.namedObject(new IdentifierExpression(context, word)).fullName
			}
			else return word
		} 
	}
	String toSupremicaSyntax() {
		String newExpr = fullyQualified()
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
		newExpr = newExpr.replaceAll(/\(\s+/){'('}
		newExpr = newExpr.replaceAll(/\\s+\)/){')'}
		newExpr.trim()
	}
	String toString() {
		return text
	}
	def addToModule(ModuleBuilder mb) {
		
	}
	
	boolean equals(object) {
		if (!object) return false
		if (object instanceof Expression) return text.toLowerCase() == object.text.toLowerCase()
		false
	}
	
	int hashCode() {
		return text.toLowerCase().hashCode()
	}
}

class IdentifierExpression extends Expression {
	IdentifierExpression(Named context, String expr) {
		super(context, expr)
//		println 'context:' << context << ' expr:' << expr
	}
	IdentifierExpression(Named context, Named prefix, String suffix) {
		super(context, "${prefix.fullName}${Converter.SEPARATOR}$suffix")
	}
	String leftMostPart() {
		def parts = text.split(Converter.SEPARATOR_PATTERN)
		if (parts.size() > 1) return parts[0] 
		null
	}
	String exceptLeftMostPart() {
		def parts = text.split(Converter.SEPARATOR_PATTERN)
		if (parts.size() > 1) return parts[1..-1].join(Converter.SEPARATOR) 
		text
	}
}

abstract class Named {
	IdentifierExpression name
	abstract Scope getScope()
	abstract String getFullName()
	String getSupremicaName() {
		name.toSupremicaSyntax()
	}
}
class NamedImpl extends Named {
	Scope scope
	String getFullName() {
		scope.parentScope ? "${scope.fullName}${Converter.SEPARATOR}$name" : name
	}
	
}
class Scope extends Named {
	List namedObjects = []
	Scope parentScope
	Scope getScope() {
		return this 
	}
	void setScope(Scope scope){}
	String getFullName() {
		parentScope?.parentScope ? "${parentScope.fullName}${Converter.SEPARATOR}$name" : name
	}
	def namedObject(IdentifierExpression expr) {
//		println '---------'
//		println this
//		println this.namedObjects.name
//		println expr
//		println '========='
		if (!expr) return null
		if (expr == this.name) return this
		def obj
		def subScope = expr.leftMostPart() ? namedObject(new IdentifierExpression(this, expr.leftMostPart())) : null
		if (subScope) obj = subScope.namedObject(new IdentifierExpression(subScope, expr.exceptLeftMostPart()))
		else {
			obj = namedObjects.find{it.name == expr}
			if (!obj) obj = parentScope?.namedObject(expr)
		}
		obj
	}
}

class ControlCodeBuilder extends BuilderSupport {

	static void main(args) {
		testBuilder(true)
		//builder.saveModuleToFile()
	}
	static {
		println "controlcode builder test" 
		testBuilder(false)
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
		//println 'createNode, name:' << name << ' attributes:' << attributes
		def type = NODE_TYPES.find{name ==~ it.pattern}
		if (!type) return createNode('transition', attributes, name)
		if (type instanceof Class) {
			def obj = type.newInstance()
			attributes.each{ attribute ->
				if (attribute.key) {
					def value
					Class propertyType = obj.metaClass.properties.find{it.name == attribute.key}.type
					switch (propertyType) {
					case IdentifierExpression: value = new IdentifierExpression(obj, attribute.value); break;
					case Expression: value = new Expression(obj, attribute.value); break;
					default:
						value = attribute.value
					}
					obj[attribute.key] = value
				}
			}
			assert !(obj instanceof Step) || obj == obj.scope
			if (obj instanceof Named && currentScope) {
				obj.scope = currentScope
				currentScope.namedObjects << obj
			}
			if (obj instanceof Scope) {
				obj.parentScope = currentScope
				currentScope = obj
			}
			assert !(obj instanceof Step) || obj == obj.scope
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
		//println 'parent:' << parent.inspect()
		//println 'child:' << child.inspect()
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
				mainProgram {
					TON(name:'Measuring_T_ge_1000', input:'BallMeasure.Measuring.X')
					SR('qInGate', set:'iStart and not iBallInGate and not qOutGate', reset:'iBallInGate')
					ASSIGN('qOutGate', input:'iBallInGate and iLiftDown and Lift.S0 and not mBallBetweenGateAndLift')
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
				booleanVariable('Measuring_T_ge_1000_Q', marked:false)
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
				plant('TON_Measuring_T_ge_1000', defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME, deterministic:false) {
					state('ready', marked:true) {
						selfLoop(guard:'!(BallMeasure_Measuring_X)')
						outgoing(to:'running', guard:'BallMeasure_Measuring_X')
					}
					state('running', marked:true) {
						selfLoop(guard:'BallMeasure_Measuring_X')
						outgoing(to:'ready', guard:'!(BallMeasure_Measuring_X)')
						outgoing(to:'elapsed') { set('Measuring_T_ge_1000_Q') }
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
						selfLoop(guard:'iBallInGate & iLiftDown & Lift_S0 & !mBallBetweenGateAndLift') { set('qOutGate') }
						selfLoop(guard:'!(iBallInGate & iLiftDown & Lift_S0 & !mBallBetweenGateAndLift)') { reset('qOutGate') }
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
						selfLoop(guard:'!(Measuring_T_ge_1000_Q & iBigBall) & !(Measuring_T_ge_1000_Q & iSmallBall)')
						outgoing(to:'BigBallFound', guard:'Measuring_T_ge_1000_Q & iBigBall') {
							set('mBallIsBig')
							reset('BallMeasure_Measuring_X')
							set('BallMeasure_BigBallFound_X')
						}
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
		
		Util.assertGeneratedModuleEqualsManual(programWithManualProcess.toAutomata(), correctProgramWithManualProcess)
		Util.assertGeneratedModuleEqualsManual(programWithDefaultProcess.toAutomata(), correctProgramWithDefaultProcess)
		if (openInSupremica) {
			Util.openModuleInSupremica(programWithManualProcess.toAutomata())
			Util.openModuleInSupremica(programWithDefaultProcess.toAutomata())
		}
	}
}
