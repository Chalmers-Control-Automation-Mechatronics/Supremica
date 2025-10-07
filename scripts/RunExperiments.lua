-- RunExperiments.lua, runs chosen synthesis/verification algorithms on all currently open modules
-- Note that Supremica allows to save logger output to file, see Configure > Options > Log > Log file
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

-- Set the options for BDD based synthesis
local options = luaj.newInstance("org.supremica.automata.algorithms.EditorSynthesizerOptions")
options:setSaveInFile(false) -- the final save overwrites all earlier
options:setPrintGuard(false)
options:setSynthesisType(SynthesisType.NONBLOCKING_CONTROLLABLE)
options:setSynthesisAlgorithm(SynthesisAlgorithm.MONOLITHICBDD) 
-- options:setSynthesisAlgorithm(SynthesisAlgorithm.PARTITIONBDD)
options:setExpressionType(options.ExpressionType.ADAPTIVE)
options:setAddGuards(false)
options:setPeakBDD(true)

-----------------------------------------------------------------
local function RunBDDSynthesis(name, container, options)

	print("**** Synthesizing for: "..name.." ****")
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
	print("Peak BDD nodes: "..bddSynthesizer:peakBDDNodes())

	if options:getAddGuards() then
	  bddSynthesizer:addGuardsToAutomata()
	end

	-- Cleanup...
	bddSynthesizer:done()
end
------------------------------------------
-- For all open modules, perform synthesis
local recent = manager:getRecent() -- java.util.List<DocumentContainer> of all open modules
for i = recent:size(), 1, -1 do -- traverse list backwards
	local container = recent:get(i-1)
	local name = container:getName()
	if name ~= "New Module" then -- Don't do this on empty or unnamed modules
		RunBDDSynthesis(name, container, options)
	end
end
