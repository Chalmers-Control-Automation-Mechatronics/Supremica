package net.sourceforge.waters.subject.module.builder;

class FunctionBlockInstance extends Named {
	static final pattern = /(?i)functionblockinstance/
	static final defaultAttr = 'name'
	static final parentAttr = 'statements'
	FunctionBlock type
	Map inputOutputMapping = [:]
	void setProperty(String property, Object newValue) {
		if (type && type.inputs.name.any{it == new IdentifierExpression(property)}) {
			inputOutputMapping[new IdentifierExpression(property)] = new Expression(newValue)
		} else if (type && type.outputs.name.any{it == new IdentifierExpression(property)}) {
			inputOutputMapping[new IdentifierExpression(property)] = new IdentifierExpression(newValue)
		}
		else metaClass.setProperty(this, property, newValue)
	}
	def addToModule(ModuleBuilder mb, Scope parent, programState) {
		Scope scope = [self:this, parent:parent]
		List newProgramState = type.inputs.collect{[scope:scope, input:inputOutputMapping[it.name], output:it.name]}
		type.addToModule(mb, scope, [*programState, *newProgramState])
	}
	List execute(Scope parent) {
		Scope scope = [self:this, parent:parent]
		List newProgramState = type.outputs.findAll{inputOutputMapping[it.name]}.collect{[scope:parent, statement:[input:inputOutputMapping[it.name], Q:scope.fullNameOf(it.name)] as Assignment]}
		newProgramState += type.inputs.collect{[scope:parent, statement:[input:inputOutputMapping[it.name], Q:scope.fullNameOf(it.name)] as Assignment]}
		newProgramState += type.execute(scope)
		newProgramState += type.outputs.findAll{inputOutputMapping[it.name]}.collect{[scope:parent, statement:[input:scope.fullNameOf(it.name), Q:inputOutputMapping[it.name]] as Assignment]}
	}

	List getNamedElements() {
		[*type.inputs, *type.outputs, *type.variables, *type.mainProgram.statements]
	}
	List getSubScopeElements() {
		type.mainProgram.statements
	}
}
