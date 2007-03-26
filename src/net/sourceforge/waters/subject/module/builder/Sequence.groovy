package net.sourceforge.waters.subject.module.builder;

class Sequence extends Named {
	static final pattern = /(?i)sequence/
	static final defaultAttr = 'name'
	static final parentAttr = 'sequences'
	List steps = []
	List transitions = []
   	List execute(Scope parent) {
		Scope scope = [self:this, parent:parent]
		List statements = []
		transitions.each { t ->
			statements += t.execute(scope)
		}
		steps.each { s ->
			statements += s.execute(parent)
		}
		statements
	}

	List getNamedElements() {
		subScopeElements
	}
	List getSubScopeElements() {
		transitions
	}
	def addProcessEvents(ModuleBuilder mb, Scope parent) {}	
}
class Transition extends Named {
	static final pattern = /(?i)tran(?:sition)?/
	static final defaultAttr = 'guard'
	static final parentAttr = 'transitions'
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
	List execute(Scope parent) {
		def ccb = new ControlCodeBuilder()
		int indexOfThis = sequence.transitions.indexOf(this)
		List higherPrioTrans = sequence.transitions[0..<indexOfThis].findAll{it.from == from}
		String mainExpr = "${parent.fullNameOf(from+'X')} and (${guard})"
		List prioExpr = higherPrioTrans.collect{parent.fullNameOf(it.name + ENABLED_NAME).text}
		Assignment enable = ccb."${ENABLED.name} := ${[mainExpr, *prioExpr].join(' and not ')}"()
		[[scope:[self:this, parent:parent] as Scope, statement:enable]]
	}
}
class Step extends Named {
	static final pattern = /(?i)step/
	static final defaultAttr = 'name'
	static final parentAttr = 'steps'
	Sequence sequence
	List variables = [new InternalVariable(name:new IdentifierExpression('X')),
	                  new InternalVariable(name:new IdentifierExpression('activation')),
	                  new InternalVariable(name:new IdentifierExpression('deactivation'))]
	List statements = []		
	
	boolean isInitial() {
		sequence.steps[0] == this
	}
	List execute(Scope parent) {
		variables.find{it.name == new IdentifierExpression('X')}.markedValue = (sequence.steps[0] == this)
		Scope scope = [self:this, parent:parent]
		ControlCodeBuilder ccb = new ControlCodeBuilder()
		Closure buildExpr = {stepEntrance ->
			sequence.transitions.findAll{(stepEntrance ? it.to : it.from) == name}.collect{t -> sequence.name + t.name + Transition.ENABLED.name}.text.join(' or ')
		}
		List s = ccb.RS(Q:'X', S:(initial ? "not ${Converter.NOT_INIT_VARIABLE_NAME} or ":'') + buildExpr(true), R:buildExpr(false)).execute(scope)
		s += ccb.P(Q:'activation', in:'X').execute(scope)
		s += ccb.N(Q:'deactivation', in:'X').execute(scope)
		statements.each{s += it.execute(scope)}
		s
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

class NAction extends Named {
	static final pattern = /(?i)N_Action/
	static final defaultAttr = null
	static final parentAttr = 'statements'
	Step step
	List statements = []
	List getNamedElements() {[]}
	List getSubScopeElements() {[]}
	def addProcessEvents(ModuleBuilder mb, Scope parent) {}
	List execute(Scope parent) {
		List execStatements = statements.inject([]) {list, elem -> list += elem.execute(parent)}
		execStatements.each{it.statement.condition = new Expression('X')}
		execStatements.statement.inject([]){list, elem -> list += elem.execute(parent)}
	}
}
class P1Action extends Named {
	static final pattern = /(?i)P1_Action/
	static final defaultAttr = null
	static final parentAttr = 'statements'
	Step step
	List statements = []
	List getNamedElements() {[]}
	List getSubScopeElements() {[]}
	def addProcessEvents(ModuleBuilder mb, Scope parent) {}
	List execute(Scope parent) {
		List execStatements = statements.inject([]) {list, elem -> list += elem.execute(parent)}
		execStatements.each{it.statement.condition = new Expression('activation')}
		execStatements.statement.inject([]){list, elem -> list += elem.execute(parent)}
	}
}
class P0Action extends Named {
	static final pattern = /(?i)P0_Action/
	static final defaultAttr = null
	static final parentAttr = 'statements'
	Step step
	List statements = []
	List getNamedElements() {println 'P0Actiongetnamed';[]}
	List getSubScopeElements() {println 'P0Actionsubscope';[]}
	def addProcessEvents(ModuleBuilder mb, Scope parent) {println 'P0Actionaddprocevent'}
	List execute(Scope parent) {
		List execStatements = statements.inject([]) {list, elem -> list += elem.execute(parent)}
		execStatements.each{it.statement.condition = new Expression('deactivation')}
		execStatements.statement.inject([]){list, elem -> list += elem.execute(parent)}
	}
}
class SetQualifier extends Named {
	static final pattern = /(?i)S/
	static final parentType = Step
	static final defaultAttr = 'name'
	static final parentAttr = 'statements'
	Step step
	List execute(Scope parent) {
		[[scope:parent, statement:[Q:name, input:new Expression("activation or $name")] as Assignment]]
	}
}
class ResetQualifier extends Named {
	static final pattern = /(?i)R/
	static final parentType = Step
	static final defaultAttr = 'name'
	static final parentAttr = 'statements'
	Step step
	List execute(Scope parent) {
		[[scope:parent, statement:[Q:name, input:new Expression("not activation and $name")] as Assignment]]
	}
}
class NonstoredQualifier extends Named {
	static final pattern = /(?i)N/
	static final parentType = Step
	static final defaultAttr = 'name'
	static final parentAttr = 'statements'
	Step step
	List execute(Scope parent) {
		new ControlCodeBuilder().SR(Q:name, S:'X', R:'deactivation').execute(parent)
	}
}