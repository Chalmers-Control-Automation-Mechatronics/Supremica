package net.sourceforge.waters.subject.module.builder;

class FunctionBlockCall extends Named {
	static final pattern = /(?i)functionblockcall/
	static final defaultAttr = 'name'
	static final parentAttr = 'statements'
	Map inputOutputMapping = [:]
	IdentifierExpression type
	void setProperty(String property, Object newValue) {
		if (metaClass.properties.any{it.name == property}) metaClass.setProperty(this, property, newValue)
		else if (newValue instanceof Expression) inputOutputMapping[new IdentifierExpression(property)] = newValue
		else if (newValue instanceof IdentifierExpression) inputOutputMapping[new IdentifierExpression(property)] = new Expression(newValue.text)
		else inputOutputMapping[new IdentifierExpression(property)] = new Expression(newValue)
	}

	List execute(Scope callingScope) {
		def instance = callingScope.namedElement(name)
		assert instance || type, "Undeclared function block or FB instance $name, referenced from ${callingScope.fullName}"
		FunctionBlock block
		if (!instance) {
			block = callingScope.namedElement(type)
			assert "Undeclared function block $type, referenced from ${callingScope.fullName}"
		}
		Scope instanceScope
		if (instance instanceof FunctionBlock || block) {
			if (!block) block = instance
			def instanceName
			if (type) instanceName = name
			else {
			    int i = 1
			    callingScope.self.namedElements.name.each { if (new IdentifierExpression("${block.name}$i") == it) ++i } 
			    instanceName = new IdentifierExpression("${block.name}$i")
			}
			instance = [name:instanceName, type:block.name] as FunctionBlockInstance
			callingScope.self.variables << instance
			instanceScope = [self:instance, parent:callingScope]
		} else {
			Scope instanceDeclarationScope = callingScope.scopeOf(instance.name)
			instanceScope = [self:instance, parent:instanceDeclarationScope]
			block = instanceDeclarationScope.namedElement(instance.type)
			assert block, 'Undeclared function block $block, referenced from ${instanceDeclarationScope}'
		}
//		println "FunctionBlockCall.execute, callingScope: ${callingScope.fullName}, instance:${instance.name}, type:${type.name}"
		List statements = block.outputs.findAll{inputOutputMapping[it.name]}.collect{[scope:callingScope, statement:[input:inputOutputMapping[it.name], Q:instanceScope.relativeNameOf(it.name, callingScope)] as Assignment]}
		statements += block.inputs.collect{[scope:callingScope, statement:[input:inputOutputMapping[it.name], Q:instanceScope.relativeNameOf(it.name, callingScope)] as Assignment]}
		statements += block.execute(instanceScope)
		statements += block.outputs.findAll{inputOutputMapping[it.name]}.collect{[scope:callingScope, statement:[input:instanceScope.relativeNameOf(it.name, callingScope), Q:new IdentifierExpression(inputOutputMapping[it.name].text)] as Assignment]}
	}
}
