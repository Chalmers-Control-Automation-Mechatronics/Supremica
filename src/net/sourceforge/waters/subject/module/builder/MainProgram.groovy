package net.sourceforge.waters.subject.module.builder;

class MainProgram {
	boolean deferred = true
	static final pattern = /(?i)mainProgram/
	static final defaultAttr = null
	static final parentAttr = 'mainProgram'
	List sequences = []
	List functionBlockInstances  = []
	def addToModule(ModuleBuilder mb, Scope scope) {
		println 'apa'
		println this.dump()
		functionBlockInstances.each { it.addToModule(mb, scope) }
		sequences.each { it.addToModule(mb, Converter.SCAN_CYCLE_EVENT_NAME)	}
	}
}