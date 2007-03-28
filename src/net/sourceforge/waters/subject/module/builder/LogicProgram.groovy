package net.sourceforge.waters.subject.module.builder;

class LogicProgram {
	static final pattern = /(?i)logicProgram/
	static final defaultAttr = 'name'
	static final parentAttr = 'programs'
	IdentifierExpression name
	List statements = []
	List variables = []
	List execute(Scope parent) {
		Scope scope = [self:this, parent:parent]
		statements.inject([]){executedStatements, statement -> executedStatements + statement.execute(scope)}
	}
	List getNamedElements() {
		subScopeElements
	}
	List getSubScopeElements() {
		variables
	}
	def addProcessEvents(ModuleBuilder mb, Scope parent) {
		subScopeElements*.addProcessEvents(mb, [self:this, parent:parent] as Scope)
	}
}