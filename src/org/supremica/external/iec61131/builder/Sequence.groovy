package org.supremica.external.iec61131.builder

import net.sourceforge.waters.subject.module.builder.ModuleBuilder

class Sequence {
	static final pattern = /(?i)sequence/
	static final defaultAttr = 'name'
	static final parentAttr = 'sequences'
	IdentifierExpression name
	List steps = []
	List transitions = []
   	List getRuntimeAssignments(Scope parent) {
		Scope scope = [self:this, parent:parent]
		subScopeElements*.getRuntimeAssignments(scope).flatten()
	}
	List getControllableVariables(Scope parent) {
		Scope scope = [self:this, parent:parent]
		subScopeElements*.getControllableVariables(scope).flatten()
	}
	List getNamedElements() {
		subScopeElements
	}
	List getSubScopeElements() {
		transitions
	}
	def addProcessEvents(ModuleBuilder mb, Scope parent) {}	
}
class Transition {
	static final pattern = /(?i)tran(?:sition)?/
	static final defaultAttr = 'guard'
	static final parentAttr = 'transitions'
	IdentifierExpression name
	IdentifierExpression from
	IdentifierExpression to
	Expression guard
	Sequence sequence
	static final ENABLED_NAME = new IdentifierExpression('enabled')
	public static final InternalVariable ENABLED = [name:ENABLED_NAME]
	List getNamedElements() {
		[ENABLED]
	}
	List getSubScopeElements() {
		[]
	}
	def addProcessEvents(ModuleBuilder mb, Scope parent) {}
	List getRuntimeAssignments(Scope parent) {
		Scope scope = [self:this, parent:parent]
		def ccb = new ControlCodeBuilder()
		int indexOfThis = sequence.transitions.indexOf(this)
		List higherPrioTrans = sequence.transitions[0..<indexOfThis].findAll{it.from == from}
		String mainExpr = "${from + 'X'} and ($guard)"
		List prioExpr = higherPrioTrans.collect{(it.name + ENABLED_NAME).text}
		Assignment enableAssignment = ccb."${ENABLED.name} := ${[mainExpr, *prioExpr].join(' and not ')}"()
		enableAssignment.getRuntimeAssignments(scope)
	}
	List getControllableVariables(Scope parent) {[]}
}
class Step {
	static final pattern = /(?i)step/
	static final defaultAttr = 'name'
	static final parentAttr = 'steps'
	IdentifierExpression name
	Sequence sequence
	boolean marked
	List variables = [new InternalVariable(name:new IdentifierExpression('X')),
	                  new InternalVariable(name:new IdentifierExpression('activation')),
	                  new InternalVariable(name:new IdentifierExpression('deactivation'))]
	List statements = []		
	
	boolean isInitial() {
		sequence.steps[0] == this
	}
	List getRuntimeAssignments(Scope parent) {
		//Only one step can be marked due to efa restrictions
		//Mark the first marked step, or if no marked step, mark the initial step
		variables.find{it.name == new IdentifierExpression('X')}.markedValue = ([*sequence.steps.findAll{it.marked}, sequence.steps[0]][0] == this)
		Scope scope = [self:this, parent:parent]
		ControlCodeBuilder ccb = new ControlCodeBuilder()
		Closure buildExpr = {stepEntrance ->
			sequence.transitions.findAll{(stepEntrance ? it.to : it.from) == name}.collect{t -> sequence.name + t.name + Transition.ENABLED.name}.text.join(' or ')
		}
		List execStatements = []
		String activationExpr = (initial ? "not ${Converter.NOT_INIT_VARIABLE_NAME} or ":'') + buildExpr(true)
		String deactivationExpr = buildExpr(false)
		execStatements += ccb."activation := $activationExpr"().getRuntimeAssignments(scope)
		execStatements += ccb."deactivation := $deactivationExpr"().getRuntimeAssignments(scope)
		execStatements += ccb.SR(Q:'X', S:'activation', R:'deactivation').getRuntimeAssignments(scope)
		statements.each{execStatements += it.getRuntimeAssignments(scope)}
		execStatements
	}
	List getControllableVariables(Scope parent) {
		Scope scope = [self:this, parent:parent]
		subScopeElements*.getControllableVariables(scope).flatten()
	}
	List getNamedElements() {
		subScopeElements
	}
	List getSubScopeElements() {
		variables
	}
	def addProcessEvents(ModuleBuilder mb, Scope parent) {}	

	List booleanQualifiers = []
}

class NAction {
	static final pattern = /(?i)N_Action/
	static final defaultAttr = null
	static final parentAttr = 'statements'
	Step step
	List statements = []
	List getNamedElements() {[]}
	List getSubScopeElements() {[]}
	def addProcessEvents(ModuleBuilder mb, Scope parent) {}
	List getRuntimeAssignments(Scope parent) {
		statements*.getRuntimeAssignments(parent).flatten().collect{
			new Assignment(Q:it.Q, input:it.input, condition:new Expression('X')).getRuntimeAssignments(parent)
		}.flatten()
	}
}
class P1Action {
	static final pattern = /(?i)P1_Action/
	static final defaultAttr = null
	static final parentAttr = 'statements'
	Step step
	List statements = []
	List getNamedElements() {[]}
	List getSubScopeElements() {[]}
	def addProcessEvents(ModuleBuilder mb, Scope parent) {}
	List getRuntimeAssignments(Scope parent) {
		statements*.getRuntimeAssignments(parent).flatten().collect{
			new Assignment(Q:it.Q, input:it.input, condition:new Expression('activation')).getRuntimeAssignments(parent)
		}.flatten()
	}
}
class P0Action {
	static final pattern = /(?i)P0_Action/
	static final defaultAttr = null
	static final parentAttr = 'statements'
	Step step
	List statements = []
	List getNamedElements() {println 'P0Actiongetnamed';[]}
	List getSubScopeElements() {println 'P0Actionsubscope';[]}
	def addProcessEvents(ModuleBuilder mb, Scope parent) {println 'P0Actionaddprocevent'}
	List getRuntimeAssignments(Scope parent) {
		statements*.getRuntimeAssignments(parent).flatten().collect{
			new Assignment(Q:it.Q, input:it.input, condition:new Expression('deactivation')).getRuntimeAssignments(parent)
		}.flatten()
	}
}
class SetQualifier {
	static final pattern = /(?i)S/
	static final parentType = Step
	static final defaultAttr = 'variable'
	static final parentAttr = 'statements'
	IdentifierExpression variable
	Step step
	List getRuntimeAssignments(Scope parent) {
		[[scope:parent, Q:variable, input:new Expression("activation or $variable")] as RuntimeAssignment]
	}
}
class ResetQualifier {
	static final pattern = /(?i)R/
	static final parentType = Step
	static final defaultAttr = 'variable'
	static final parentAttr = 'statements'
	IdentifierExpression variable
	Step step
	List getRuntimeAssignments(Scope parent) {
		[[scope:parent, Q:variable, input:new Expression("not activation and $variable")] as RuntimeAssignment]
	}
}
class NonstoredQualifier {
	static final pattern = /(?i)N/
	static final parentType = Step
	static final defaultAttr = 'variable'
	static final parentAttr = 'statements'
	IdentifierExpression variable
	Step step
	List getRuntimeAssignments(Scope parent) {
		new ControlCodeBuilder().SR(Q:variable, S:'X', R:'deactivation').getRuntimeAssignments(parent)
	}
}