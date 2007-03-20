package net.sourceforge.waters.subject.module.builder;

class Process extends Named {
	static final pattern = /(?i)process/
	static final defaultAttr = 'name'
	static final parentAttr = 'processes' 
	List variables = []
	List statements  = []

	List execute(Scope parent) {
		Scope scope = [parent:parent, self:this]
		statements.inject([]){executedStatements, statement -> executedStatements + statement.execute(scope)}
	}
	List getNamedElements() {
		return [*variables, *statements]
	}
	List getSubScopeElements() {
		return statements
	}
}