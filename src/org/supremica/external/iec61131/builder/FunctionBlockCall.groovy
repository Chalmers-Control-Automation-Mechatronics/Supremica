package org.supremica.external.iec61131.builder

class FunctionBlockCall {
	static final pattern = /(?i)functionblockcall/
	static final defaultAttr = 'name'
	static final parentAttr = 'statements'
	Map inputOutputMapping = [:]
	Identifier type
	Identifier name
	
	void setProperty(String property, Object newValue) {
		if (metaClass.properties.any{it.name == property}) metaClass.setProperty(this, property, newValue)
		else if (newValue instanceof Expression) inputOutputMapping[new Identifier(property)] = newValue
		else if (newValue instanceof Identifier) inputOutputMapping[new Identifier(property)] = new Expression(newValue.text)
		else inputOutputMapping[new Identifier(property)] = new Expression(newValue)
	}

	List getRuntimeAssignments(Scope callingScope) {
		def instance = callingScope.namedElement(name)
		assert instance || type, "Undeclared function block instance $name, referenced from ${callingScope.fullName}"
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
			    callingScope.self.namedElements.name.each { if (new Identifier("${block.name}$i") == it) ++i } 
			    instanceName = new Identifier("${block.name}$i")
			}
			instance = [name:instanceName, type:block.name] as FunctionBlockInstance
			callingScope.self.variables << instance
			instanceScope = [self:instance, parent:callingScope]
		} else {
			Scope instanceDeclarationScope = callingScope.scopeOf(instance.name)
			instanceScope = [self:instance, parent:instanceDeclarationScope]
			block = instanceDeclarationScope.namedElement(instance.type)
			assert block, "Undeclared function block ${instance.type}, referenced from ${instanceDeclarationScope}"
		}
//		println "FunctionBlockCall.getRuntimeAssignments, callingScope: ${callingScope.fullName}, instance:${instance.name}, type:${type.name}"
		List statements = block.outputs.findAll{inputOutputMapping[it.name]}.collect{[scope:callingScope, input:inputOutputMapping[it.name], Q:instanceScope.relativeNameOf(it.name, callingScope)] as RuntimeAssignment}
		statements += block.inputs.collect{[scope:callingScope, input:inputOutputMapping[it.name], Q:instanceScope.relativeNameOf(it.name, callingScope)] as RuntimeAssignment}
		statements += block.getRuntimeAssignments(instanceScope)
		statements += block.outputs.findAll{inputOutputMapping[it.name]}.collect{[scope:callingScope, input:instanceScope.relativeNameOf(it.name, callingScope), Q:new Identifier(inputOutputMapping[it.name].text)] as RuntimeAssignment}
	}
}
