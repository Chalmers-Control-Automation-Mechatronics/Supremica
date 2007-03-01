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

class ControlCodeBuilder extends BuilderSupport {

	static void main(args) {
		def builder = createTestBuilder()
		//builder.saveModuleToFile()
		//builder.openModuleInSupremica()
	}
	static {
		println "controlcode builder test" 
		createTestBuilder()
	}
	
	ModuleSubject module
	ModuleBuilder moduleBuilder
	
	ModuleSubject getModule() {
		moduleBuilder = new ModuleBuilder()
		moduleBuilder.module(program.name) {
			[*program.input, *program.output, *program.variable].grep{it}.each { variable ->
				moduleBuilder.booleanVariable(variable.name, initial:variable.value)
			}
			moduleBuilder.event('scan')
			program.sequence.each { sequence ->
				moduleBuilder.specification(sequence.name, defaultEvent:'scan', deterministic:false) {
					sequence.step.each { step ->
						moduleBuilder.state(step.name, marked:true)
					}
					sequence.transition.each { transition ->
						moduleBuilder.selfLoop(from:transition.from, guard:"!(${transition.guardInSupremicaSyntax()})")
						moduleBuilder.transition(from:transition.from, to:transition.to, guard:transition.guardInSupremicaSyntax()) {
							def targetStep = sequence.step.find{it.name==transition.to}
							targetStep.reset.each{moduleBuilder.reset(it.name)}
							[*targetStep.set, *targetStep.nonstored].grep{it}.each{moduleBuilder.set(it.name)}
							sequence.step.find{it.name==transition.from}.nonstored.each{moduleBuilder.reset(it.name)}
						}
					}
				}
			}
		}
		moduleBuilder.module
	}
	
	static final PROGRAM = 'program'
	static final TRANSITION = 'transition'
	static final STEP = 'step'
	static final SEQUENCE = 'sequence'
	static final SET = 'set'
	static final RESET = 'reset'
	static final NONSTORED = 'nonstored'
	static final OUTPUT = 'output'			
	static final INPUT = 'input'		
	static final VARIABLE = 'variable'						
	static final NODE_TYPES = [[name:PROGRAM, pattern:/(?i)program/, defaultAttr:'name'],
	                           [name:SEQUENCE, parent:PROGRAM, pattern:/(?i)sequence/, defaultAttr:'name'],
	                           [name:STEP, parent:SEQUENCE, pattern:/(?i)step/, defaultAttr:'name'],
	                           [name:TRANSITION, parent:SEQUENCE, pattern:/(?i)tran(?:sition)?/, defaultAttr:'guard'],
	                           [name:SET, parent:STEP, pattern:/(?i)S|set/, defaultAttr:'name'],
	                           [name:RESET, parent:STEP, pattern:/(?i)R|reset/, defaultAttr:'name'],
	                           [name:NONSTORED, parent:STEP, pattern:/(?i)N|nonstored/, defaultAttr:'name'],
	                           [name:INPUT, parent:PROGRAM, pattern:/(?i)input/, defaultAttr:'name'],
	                           [name:OUTPUT, parent:PROGRAM, pattern:/(?i)output/, defaultAttr:'name'],
	                           [name:VARIABLE, parent:PROGRAM, pattern:/(?i)variable/, defaultAttr:'name']]
	                           
	
	
	Expando program
	
	private lastStep
	
	Object invokeMethod(String name, Object args){
//		println name
//		println args
		super.invokeMethod(name, args)
	}
	protected void setClosureDelegate(Closure closure, Object node) {
		closure.setDelegate(this)
		println node.inspect()
		println closure.delegate.dump()
	}
	
	def createNode(name){
		createNode(TRANSITION, name)
	}

	def createNode(name, value){
		createNode(name, [:], value)
	}

	def createNode(name, Map attributes){
		def type = NODE_TYPES.find{name ==~ it.pattern}
		if (!type) return null
		[type:type, *:attributes] as Expando
	}

	def createNode(name, Map attributes, value){
		def type = NODE_TYPES.find{name ==~ it.pattern}
		if (!type) return null
		createNode(name, [(type.defaultAttr):value, *:attributes])
	}
	
	void setParent(parent, child){
		assert parent?.type.name == child.type.parent
		if (!parent[child.type.name]) parent[child.type.name] = []
		parent[child.type.name] << child
	}
	
	void nodeCompleted(parent, node) {
		switch(node.type.name) {
		case TRANSITION:
			node.guardInSupremicaSyntax = {node.guard.replaceAll(/(?i)\s*(?:and|or|not)\s*/){[and:' & ', or:' | ', not:' !'][it.trim().toLowerCase()]}.trim()}
			if (!node.from) node.from = parent.step[-1].name
			break
		case STEP: parent.transition.findAll{!it.to}.each{it.to = node.name}; break
		case SEQUENCE: node.transition.findAll{!it.to}.each{it.to = node.step[0].name}; break
		case PROGRAM: program = node; break
		}
	}
	
	public static ControlCodeBuilder createTestBuilder() {
		def controlCodeBuilder = new ControlCodeBuilder();
		controlCodeBuilder.program(name:'testprogram', deferred:true) {
			input 'iBallDown'
			input 'iBallUp'
			input('iLiftDown', value:false)
			input 'iLiftUp'
			input 'iSmallBall'
			input 'iBigBall'
			output 'qUp'
			output 'qOut'
			output 'qMeasure'
			variable 'mBallIsBig'
			sequence(name:'Lift') {
				STEP('S0'){R('qUp')}
				'iBallDown'()
				step('S1'){S('qUp')}
				'iBallUp and iLiftUp'()
				Step('S2'){N('qOut')}
				'not iBallUp'()
			}
			SEQuence('BallMeasure') {
				STEP('MeasureInit')
				TRAN 'iBallUp'
				STEP('Measuring') {S('qMeasure')}
				'MeasuringTimeout AND iBigBall'()
				TRAN('MeasuringTimeout AND iSmallBall', to:'SmallBallFound')
				STEP('BigBallFound') {S('mBallIsBig')}
				transition('true', to:'MeasureDone')
				STEP('SmallBallFound') {R('mBallIsBig')}
				'true'()
				STEP('MeasureDone') {Reset('qMeasure')}
				'!iBallUp'()
			}
		}
		def moduleBuilder = new ModuleBuilder()
		moduleBuilder.module('testprogram') {
			booleanVariable 'iBallDown'
			booleanVariable 'iBallUp'
			booleanVariable('iLiftDown', initial:false)
			booleanVariable 'iLiftUp'
			booleanVariable 'iSmallBall'
			booleanVariable 'iBigBall'
			booleanVariable 'qUp'
			booleanVariable 'qOut'
			booleanVariable 'qMeasure'
			booleanVariable 'mBallIsBig'
			event('scan')
			specification('Lift', defaultEvent:'scan', deterministic:false) {
				state('S0', marked:true)
				selfLoop(guard:'!(iBallDown)')
				transition(guard:'iBallDown') {
					set('qUp')
				}
				state('S1', marked:true)
				selfLoop(guard:'!(iBallUp & iLiftUp)')
				transition(guard:'iBallUp & iLiftUp') {
					set('qOut')
				}
				state('S2', marked:true)
				selfLoop(guard:'!(!iBallUp)')
				transition(to:'S0', guard:'!iBallUp') {
					reset('qUp')
					reset('qOut')
				}
			}
			specification(name:'BallMeasure', defaultEvent:'scan', deterministic:false) {
				state('MeasureInit', marked:true)
				selfLoop(guard:'!(iBallUp)')
				transition(guard:'iBallUp') {set('qMeasure')}
				state('Measuring', marked:true)
				selfLoop(guard:'!(MeasuringTimeout & iBigBall)')
				transition(guard:'MeasuringTimeout & iBigBall') {set('mBallIsBig')}
				selfLoop(guard:'!(MeasuringTimeout & iSmallBall)')
				transition(guard:'MeasuringTimeout & iSmallBall', to:'SmallBallFound') {reset('mBallIsBig')}
				state('BigBallFound', marked:true)
				selfLoop(guard:'!(true)')
				transition(guard:'true', to:'MeasureDone'){reset('qMeasure')}
				state('SmallBallFound', marked:true)
				selfLoop(guard:'!(true)')
				transition(guard:'true'){reset('qMeasure')}
				state('MeasureDone', marked:true)
				selfLoop(guard:'!(!iBallUp)')
				transition(guard:'!iBallUp', to:'MeasureInit')
			}
		}
//		println controlCodeBuilder.module.componentList.graph.edges.guardActionBlock.findAll{it}.actions
//		println moduleBuilder.module.componentList.graph.edges.guardActionBlock.findAll{it}.actions
//		println controlCodeBuilder.module.componentList.graph.edges.guardActionBlock.findAll{it}.guards
//		println moduleBuilder.module.componentList.graph.edges.guardActionBlock.findAll{it}.guards
//		println controlCodeBuilder.module.componentList.graph.nodes
//		println moduleBuilder.module.componentList.graph.nodes
//		println controlCodeBuilder.module.componentList.graph.edges.labelBlock
//		println moduleBuilder.module.componentList.graph.edges.labelBlock
//		println controlCodeBuilder.module.componentList.graph.edges
//		println moduleBuilder.module.componentList.graph.edges
		assert controlCodeBuilder.module.equalsByContents(moduleBuilder.module)
//		moduleBuilder.openModuleInSupremica()
		controlCodeBuilder
	}
}