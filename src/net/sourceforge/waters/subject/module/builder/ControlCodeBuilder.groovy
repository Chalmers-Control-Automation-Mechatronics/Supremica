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
	static final NOT_INIT_VARIABLE_NAME = 'NOT_INIT'
}

class Input extends ExternalVariable {
	static final pattern = /(?i)input/
	static final defaultAttr = 'name'
	static final parentAttr = 'inputs'
/*	def formatEventName(Scope scope) {
		name.toSupremicaSyntax(scope) + '_change'
	}*/
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
			statementScope.globalScope.identifiersInExpression(s.scope.expand(s.statement.input, statements[0..<i++])).any{it == fullNameOfThis}
		}
		needed &= indexToThis == statements.size() - 1 || statements[indexToThis+1..-1].every{it.scope.fullNameOf(it.statement.Q) != fullNameOfThis}
		needed
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
		// Don't add if it is not used in any earlier assignment expression, inluding this
		needed |= statements[0..indexToThis].any{s ->
			statementScope.globalScope.identifiersInExpression(s.scope.expand(s.statement.input, statements[0..<i++])).any{it == fullNameOfThis}
		}
		// Don't add if it wil be added later
		needed &= indexToThis == statements.size() - 1 || statements[indexToThis+1..-1].every{it.scope.fullNameOf(it.statement.Q) != fullNameOfThis}
	}
	def addProcessEvents(ModuleBuilder mb, Scope parent) {}
}

abstract class Variable extends Named {
	boolean value
	boolean markedValue
}

class Expression {
	Expression(String text) {
		this.text = text?.trim()
	}
	final String text
	
	Expression expand(scope, programState) {
		scope.expand(this, programState)
	}
	
	String toSupremicaSyntax() {
		String newExpr = text
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
	Expression cleanup() {
		String oldExpr = ''
		String newExpr = text
		while (oldExpr != newExpr) {
			oldExpr = newExpr
			newExpr = newExpr.replaceAll(/\(\s*${FULL_ID_PATTERN}\s*\)/){it[1..-2]}
			newExpr = newExpr.replaceAll(/(?i)\(\s*not\s+${FULL_ID_PATTERN}\s*\)/){it[1..-2]}
			newExpr = newExpr.replaceAll(/(?i)not\s+not/){''}
			newExpr = newExpr.replaceAll(/\s\s/){' '}
			newExpr = newExpr.replaceAll(/\(\((?:\s|${FULL_ID_PATTERN})*\)\)/){it[1..-2]}
		}
		new Expression(newExpr)
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
	List findIdentifiers() {
		List ids = []
		(text =~ FULL_ID_PATTERN).each{match -> if (!KEYWORDS.any{keyword -> match ==~ keyword}) ids << new IdentifierExpression(match)}
		ids
	}
	protected static final KEYWORDS = [/(?i)and/, /(?i)or/, /(?i)not/, /(?i)true/, /(?i)false/]
	
	static final SIMPLE_ID_PATTERN = /\b_*[a-zA-Z]\w*\b*/
	static final FULL_ID_PATTERN = /${SIMPLE_ID_PATTERN}(?:${Converter.SEPARATOR_PATTERN}${SIMPLE_ID_PATTERN})*/
	static {
		assert 'apa' ==~ FULL_ID_PATTERN
		assert '__apa' ==~ FULL_ID_PATTERN
		assert '_apa1' ==~ FULL_ID_PATTERN
		assert !('_1apa' ==~ FULL_ID_PATTERN)
		assert !('1apa' ==~ FULL_ID_PATTERN)
		assert 'a__p1a' ==~ FULL_ID_PATTERN
		assert 'a__p1a' ==~ FULL_ID_PATTERN
		assert 'a__p1a.asd' ==~ FULL_ID_PATTERN
		assert 'a__p1a.__asd.sd1' ==~ FULL_ID_PATTERN
		assert !('a__p1a.__1.sd1' ==~ FULL_ID_PATTERN)
		assert new Expression('apa_bepa.cepa and (lepa_pep_as or epa.opa)').findIdentifiers().text == ['apa_bepa.cepa', 'lepa_pep_as', 'epa.opa']
		assert new Expression('Py2_in and not Py2_old').findIdentifiers().text == ['Py2_in', 'Py2_old']
		assert new Expression('apa and ((pepa)) and (not (cepa))').cleanup().text == 'apa and pepa and not cepa'
		assert new Expression('apa or ((sopa and ((pepa.rl or lepa))))').cleanup().text == 'apa or ((sopa and (pepa.rl or lepa)))'
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
		super.expand(scope, []).toSupremicaSyntax()
	}
	IdentifierExpression plus(other) {
		new IdentifierExpression("${this}${Converter.SEPARATOR}$other")
	}
	boolean startsWith(IdentifierExpression other) {
		text.toLowerCase().startsWith(other.text.toLowerCase())
	}
	IdentifierExpression relativeTo(IdentifierExpression scope) {
		if (startsWith(scope)) return new IdentifierExpression(text[scope.text.size()+1..-1])
		else if (scope.startsWith(this)) return rightMostPart()
		else return this
	}
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
		testSfc(true)
		testAutomataGenerator(true)
		testAssignment(true)
		testFunctionBlocks(true)
		//builder.saveModuleToFile()
	}
	static {
/*		testAutomataGenerator(false)
		testAssignment(false)
		testFunctionBlocks(false)
		testSfc(false)
	*/
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
			else if (!attributes.keySet().every{it =~ /(?i)name/}) { //FB instance call
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
				variable "${Converter.NOT_INIT_VARIABLE_NAME}"
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
				RS(Q:'S1_X', S:"not ${Converter.NOT_INIT_VARIABLE_NAME} or mySequence_T3_enabled or mySequence_T4_enabled", R:'mySequence_T1_enabled or mySequence_T2_enabled')
				P('S1_P1', Q:'S1_activation', in:'S1_X')
				N('S1_N1', Q:'S1_deactivation', in:'S1_X')
				SR(Q:'u5', S:'S1_X', R:'S1_deactivation') 
				'u2 := y1'('S1_deactivation')
				RS(Q:'S2_X', S:'mySequence_T1_enabled', R:'mySequence_T3_enabled')
				P('S2_P1', Q:'S2_activation', in:'S2_X')
				N('S2_N1', Q:'S2_deactivation', in:'S2_X')
				'u4 := S2_activation or u4'()
				'u1 := y1 or y2'('S2_X')
				RS(Q:'S3_X', S:'mySequence_T2_enabled', R:'mySequence_T4_enabled')
				P('S3_P1', Q:'S3_activation', in:'S3_X')
				N('S3_N1', Q:'S3_deactivation', in:'S3_X')
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
			event(['someProcess_change', 'Process_y2_change', 'Process_y3_change'], controllable:false)
			eventAlias(name:Converter.PROCESS_EVENTS_ALIAS_NAME, events:['someProcess_change', 'Process_y2_change', 'Process_y3_change'])
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
