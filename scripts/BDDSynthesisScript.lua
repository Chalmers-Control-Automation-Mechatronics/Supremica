-- BDDSynthesizerScript.lua, Lua script example of how to use the BDD syntehsizer in Lua
-- Meant to be run as a script inside Supremica (with LuaJ embedded)
local luaj = luajava -- just shorthand 
local script, ide = ... -- grab the arguments passed from Java via LuaJ

-- Get convenience function to generate file name
local Config = luaj.bindClass("org.supremica.properties.Config")
local getFileName = dofile(Config.FILE_SCRIPT_PATH:getValue():getPath().."/getFileName.lua")

-- Set up some helpers
local actions = ide:getActions()
local editorSynthesizerAction  = actions.editorSynthesizerAction
local manager = ide:getDocumentContainerManager() 

-- bindClass is like Java's import
local EventKind = luaj.bindClass("net.sourceforge.waters.model.base.EventKind")
local SynthesisType = luaj.bindClass("org.supremica.automata.algorithms.SynthesisType")
local SynthesisAlgorithm = luaj.bindClass("org.supremica.automata.algorithms.SynthesisAlgorithm")

-- Set the options
local options = luaj.newInstance("org.supremica.automata.algorithms.EditorSynthesizerOptions")
options:setSaveInFile(false)
options:setPrintGuard(false)
options:setSynthesisType(SynthesisType.NONBLOCKING_CONTROLLABLE)
options:setSynthesisAlgorithm(SynthesisAlgorithm.MONOLITHICBDD) 
-- options:setSynthesisAlgorithm(SynthesisAlgorithm.PARTITIONBDD)
options:setExpressionType(options.ExpressionType.ADAPTIVE)
options:setAddGuards(false)

-- For all open modules, perform synthesis
local recent = manager:getRecent() -- java.util.List<DocumentContainer> of all open modules
for i = 1, recent:size() do
	local container = recent:get(i-1)
	local name = container:getName()
	if name ~= "New Module" then 
		print("**** Synthesizing for: "..name)
		local module = container:getEditorPanel():getModuleSubject()

		-- collect the controllable events, only them can we assign guards to
		local controllableEvents = luaj.newInstance("java.util.Vector")
		local eventDeclList = module:getEventDeclListModifiable()
		for i = 1, eventDeclList:size() do
		  local event = eventDeclList:get(i-1)
		  if event:getKind() == EventKind.CONTROLLABLE then
			controllableEvents:add(event:getName())
		  end
		end

		local exAutomata = luaj.newInstance("org.supremica.automata.ExtendedAutomata", module)
		local bddSynthesizer = luaj.newInstance("org.supremica.automata.BDD.EFA.BDDExtendedSynthesizer", exAutomata, options)
		bddSynthesizer:synthesize(options)
		bddSynthesizer:generateGuard(controllableEvents, options)

		local saveFile = luaj.newInstance("java.io.File", getFileName("BDDoutput.txt"))
		editorSynthesizerAction:saveOrPrintGuards(bddSynthesizer, controllableEvents,
											options:getSaveInFile(), options:getPrintGuard(),
											saveFile)

		print("Synthesis took "..bddSynthesizer:getSynthesisTimer():toString())
		print("Guards generated in "..bddSynthesizer:getGuardTimer():toString())
		
		if options:getAddGuards() then
		  bddSynthesizer:addGuardsToAutomata()
		end

		-- Cleanup...
		bddSynthesizer:done()
	end
end