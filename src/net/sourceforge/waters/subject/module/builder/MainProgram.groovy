package net.sourceforge.waters.subject.module.builder;

class LogicProgram extends Named {
	static final pattern = /(?i)logicProgram/
	static final defaultAttr = 'name'
	static final parentAttr = 'programs'
	List statements = []
	List variables = []
	List execute(Scope parent) {
		Scope scope = [self:this, parent:parent]
		statements.inject([]){executedStatements, statement -> executedStatements + statement.execute(scope)}
	}
	List getNamedElements() {
		//[*variables, *statements]
		subScopeElements
	}
	List getSubScopeElements() {
		variables
		//statements
	}
	def addProcessEvents(ModuleBuilder mb, Scope parent) {
		//statements*.addProcessEvents(mb, [self:this, parent:parent] as Scope)
		subScopeElements*.addProcessEvents(mb, [self:this, parent:parent] as Scope)
	}
}
class SequentialProgram extends Named {
	static final pattern = /(?i)sequentialProgram/
	static final defaultAttr = 'name'
	static final parentAttr = 'programs'
	static final NOT_INIT_VARIABLE = new InternalVariable(name:new IdentifierExpression(Converter.NOT_INIT_VARIABLE_NAME))
	List variables = [NOT_INIT_VARIABLE]
	List sequences = []
	
	List getNamedElements() {
		subScopeElements//[*variables, *sequences]
	}
	List getSubScopeElements() {
		[*variables, *sequences, *sequences.steps]
	}
	List execute(Scope parent) {
		Scope scope = [self:this, parent:parent]
		List statements = sequences.inject([]){executedStatements, sequence -> executedStatements + sequence.execute(scope)}
		statements << [scope:scope, statement:new Assignment(Q:NOT_INIT_VARIABLE.name, input:new Expression('true'))]
	}
	def addProcessEvents(ModuleBuilder mb, Scope parent) {
		subScopeElements*.addProcessEvents(mb, [self:this, parent:parent] as Scope)
	}

}