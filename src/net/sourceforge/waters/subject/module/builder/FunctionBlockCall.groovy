package net.sourceforge.waters.subject.module.builder;

class FunctionBlockCall extends Named {
	static final pattern = /(?i)functionblockcall/
	static final defaultAttr = 'name'
	static final parentAttr = 'statements'
	Map inputOutputMapping = [:]
	void setProperty(String property, Object newValue) {
		if (metaClass.properties.any{it.name == property}) metaClass.setProperty(this, property, newValue)
		else inputOutputMapping[new IdentifierExpression(property)] = new Expression(newValue)
	}

	List execute(Scope callingScope) {
		def instance = callingScope.namedElement(name)
		assert instance, 'Undeclared function block or FB instance $name, referenced from ${callingScope}'
		FunctionBlock type
		Scope instanceScope
		if (instance instanceof FunctionBlock) {
			type = instance
			int i = 1
			callingScope.self.namedElements.name.each { if (new IdentifierExpression("${type.name}$i") == name) ++i } 
			instance = [name:new IdentifierExpression("${type.name}$i"), type:type.name] as FunctionBlockInstance
			callingScope.self.variables << instance
			instanceScope = [self:instance, parent:callingScope]
		} else {
			Scope instanceDeclarationScope = callingScope.scopeOf(instance.name)
			instanceScope = [self:instance, parent:instanceDeclarationScope]
			type = instanceDeclarationScope.namedElement(instance.type)
			assert type, 'Undeclared function block $type, referenced from ${instanceDeclarationScope}'
		}
//		println "FunctionBlockCall.execute, callingScope: ${callingScope.fullName}, instance:${instance.name}, type:${type.name}"
		List statements = type.outputs.findAll{inputOutputMapping[it.name]}.collect{[scope:callingScope, statement:[input:inputOutputMapping[it.name], Q:instanceScope.relativeNameOf(it.name, callingScope)] as Assignment]}
		statements += type.inputs.collect{[scope:callingScope, statement:[input:inputOutputMapping[it.name], Q:instanceScope.relativeNameOf(it.name, callingScope)] as Assignment]}
		statements += type.execute(instanceScope)
		statements += type.outputs.findAll{inputOutputMapping[it.name]}.collect{[scope:callingScope, statement:[input:instanceScope.relativeNameOf(it.name, callingScope), Q:new IdentifierExpression(inputOutputMapping[it.name].text)] as Assignment]}
	}
}
