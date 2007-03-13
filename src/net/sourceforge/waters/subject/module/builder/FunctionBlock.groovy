package net.sourceforge.waters.subject.module.builder;

class FunctionBlock extends Scope {
	static final pattern = /(?i)fb|functionblock/
	static final defaultAttr = 'name'
	static final parentAttr = 'functionBlocks'
	String namePattern
	String getNamePattern() {
		namePattern ? /(?i)$namePattern/ : /(?i)${name.text}/
	}
	List inputs = []
	List outputs = []
	List variables = []
	MainProgram mainProgram
	Process process
	def addToModule(ModuleBuilder mb, FunctionBlockInstance instance) {
		println this.dump()
		mainProgram.addToModule(mb, instance)
	}
}

class FunctionBlockInstance extends Scope {
	static final pattern = /(?i)functionblockinstance/
	static final defaultAttr = null
	static final parentAttr = 'functionBlockInstances'
	String namePattern
	FunctionBlock type
	Object getProperty(String property) {
		if (getType() && (getType().inputs + getType().outputs).name.any{it == new IdentifierExpression(property)}) {
			return aliases[property]
		} else return super.getProperty(property)
	}
	void setProperty(String property, Object newValue) {
		if (type && (type.inputs + type.outputs).name.any{it == new IdentifierExpression(property)}) {
			aliases[property] = newValue
		} else metaClass.setProperty(this, property, newValue)
	}
	def addToModule(ModuleBuilder mb, Scope scope) {
		println this.dump()
		type.addToModule(mb, this)
	}
}
