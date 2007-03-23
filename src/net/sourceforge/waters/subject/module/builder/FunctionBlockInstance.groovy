package net.sourceforge.waters.subject.module.builder;

class FunctionBlockInstance extends Named {
	static final pattern = /(?i)functionblockinstance/
	static final defaultAttr = 'name'
	//static final parentAttr = 'statements'
	static final parentAttr = 'variables'
	//FunctionBlock type
	IdentifierExpression type
//	Map inputOutputMapping = [:]
/*	void setProperty(String property, Object newValue) {
		if (type && type.inputs.name.any{it == new IdentifierExpression(property)}) {
			inputOutputMapping[new IdentifierExpression(property)] = new Expression(newValue)
		} else if (type && type.outputs.name.any{it == new IdentifierExpression(property)}) {
			inputOutputMapping[new IdentifierExpression(property)] = new IdentifierExpression(newValue)
		}
		else metaClass.setProperty(this, property, newValue)
	}
*/
/*	List execute(Scope parent) {
		Scope scope = [self:this, parent:parent]
		List newProgramState = type.outputs.findAll{inputOutputMapping[it.name]}.collect{[scope:parent, statement:[input:inputOutputMapping[it.name], Q:scope.relativeNameOf(it.name, parent)] as Assignment]}
		newProgramState += type.inputs.collect{[scope:parent, statement:[input:inputOutputMapping[it.name], Q:scope.relativeNameOf(it.name, parent)] as Assignment]}
		newProgramState += type.execute(scope)
		newProgramState += type.outputs.findAll{inputOutputMapping[it.name]}.collect{[scope:parent, statement:[input:scope.relativeNameOf(it.name, parent), Q:inputOutputMapping[it.name]] as Assignment]}
	}
*/
	FunctionBlock getType(Scope parent) {
		parent.namedElement(type)
	}
/*	List getNamedElements(Scope parent) {
		FunctionBlock type = getType(parent)
		[*type.inputs, *type.outputs, *type.variables, *type.programs]
	}
	List getSubScopeElements(Scope parent) {
		FunctionBlock type = getType(parent)
		[*type.programs, *type.variables]
	}*/
	List getNamedElements() {[]}
	List getSubScopeElements() {[]}
	def addProcessEvents(ModuleBuilder mb, Scope parent) {
		FunctionBlock type = getType(parent)
		type.addProcessEvents(mb, [self:this, parent:parent] as Scope)
	}
}
