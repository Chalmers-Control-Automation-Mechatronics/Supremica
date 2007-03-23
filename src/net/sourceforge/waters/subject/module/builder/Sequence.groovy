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

class Step extends Named {
	static final pattern = /(?i)step/
	static final defaultAttr = 'name'
	static final parentAttr = 'steps'
	Sequence sequence
	List variables = [new InternalVariable(name:new IdentifierExpression('X'))]
	List statements = []		
	
	boolean isInitial() {
		sequence.steps[0] == this
	}
	                   
	List execute(Scope parent) {
		Scope scope = [self:this, parent:parent]
		ControlCodeBuilder ccb = new ControlCodeBuilder()
		Closure buildExpr = {stepEntrance ->
			sequence.transitions.findAll{(stepEntrance ? it.to : it.from) == name}.collect{t -> sequence.name + t.name + Transition.ENABLED.name}.text.join(' or ')
		}
		List s = ccb.RS(Q:'X', S:(initial ? "not ${Converter.NOT_INIT_VARIABLE_NAME} or ":'') + buildExpr(true), R:buildExpr(false)).execute(scope)
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

//	List setQualifiers = []
//	List resetQualifiers = []
//	List nonstoredQualifiers = []
}

class Transition extends Named {
	static final pattern = /(?i)tran(?:sition)?/
	static final defaultAttr = 'guard'
	static final parentAttr = 'transitions'
	IdentifierExpression from
	IdentifierExpression to
	Expression guard
	Sequence sequence
	static final ENABLED_NAME = 'enabled'
	public static final InternalVariable ENABLED = [name:new IdentifierExpression(ENABLED_NAME)]
	List getNamedElements() {
		[ENABLED]
	}
	List getSubScopeElements() {
		[]
	}
	def addProcessEvents(ModuleBuilder mb, Scope parent) {}
	List execute(Scope parent) {
		def ccb = new ControlCodeBuilder()
		Assignment enable = ccb."${ENABLED.name} := ${parent.fullNameOf(from+'X')} and (${guard})"()
		[[scope:[self:this, parent:parent] as Scope, statement:enable]]
	}
}