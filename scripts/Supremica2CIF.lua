-- Supremica2CIF.lua, Lua script to convert Supremica models to CIF
-- Meant to be run as a script inside Supremica (with LuaJ embedded)
local luaj = luajava -- just shorthand 
local script, ide = ... -- grab the arguments passed from Java via LuaJ

-- Get convenience function to generate file name
local Config = luaj.bindClass("org.supremica.properties.Config")
local getFileName = dofile(Config.FILE_SCRIPT_PATH:getValue():getPath().."/getFileName.lua")

-- Helper functions
local function saveFile(fname)
	local fc = luaj.newInstance("javax.swing.JFileChooser")
	fc:setDialogTitle("Give CIF File")
	local retval = fc:showOpenDialog()
	if retval == fc.APPROVE_OPTION then
		local fname = fc:getSelectedFile():getPath() -- does not work on Java > 8
		-- local fname = fc:getName(fc:getSelectedFile()) -- this works for Java > 8
		print("File: "..fname)
	else
		print("User cancelled")
	end
end

local Helpers = luaj.bindClass("org.supremica.Lupremica.Helpers") 
if not Helpers then print("Lupremica.Helpers not found") return end

-- bindClass is like Java's import
local EventKind = luaj.bindClass("net.sourceforge.waters.model.base.EventKind")
local VariableComponentProxy = luaj.bindClass("net.sourceforge.waters.model.module.VariableComponentProxy")
if not VariableComponentProxy then print("VariableComponentProxy not fond") return end

local function getEvents(module)
	local controllable, uncontrollable = {}, {}
	
	local eventDeclList = module:getEventDeclList()
	for i = 1, eventDeclList:size() do
	  local event = eventDeclList:get(i-1)
	  local kind = event:getKind()
	  if kind == EventKind.CONTROLLABLE then
	  	controllable[#controllable+1] = event:getName()
	  elseif kind ~= EventKind.PROPOSITION then
	  	uncontrollable[#uncontrollable+1] = event:getName()
	  end
	end
	return controllable, uncontrollable
end

-- Process the currently open module into <name>.cif
local manager = ide:getDocumentContainerManager() 
local container = manager:getActiveContainer()
local name = container:getName()
local module = container:getEditorPanel():getModuleSubject()
local components = module:getComponentList() 

print(getFileName(name..".cif"))

local efalist = Helpers:getAutomatonList(module)
local varlist = Helpers:getVariableList(module)
local cevents, uevents = getEvents(module)
print("controllable "..table.concat(cevents, ", ")..";")
print("uncontrollable "..table.concat(uevents, ", ")..";")

local function processEdge(edge)
	local src = edge:getSource():toString():gsub("\n", "")
	local trgt = edge:getTarget():toString():gsub("\n", "")
	local gablock = edge:getGuardActionBlock():toString():gsub("\n", "")
	local evlist = edge:getLabelBlock():getEventIdentifierList()
	for i = 1, evlist:size() do
		local ev = evlist:get(i-1)
		print("Edge: <"..src..", "..ev:toString()..", "..trgt..">")
		print(gablock)
	end
end

local function processEFA(efa)
	print("automaton "..efa:getName())
	local graph = efa:getGraph()
	local edges = graph:getEdges()
	local iterator = edges:iterator()
	while iterator:hasNext() do
		processEdge(iterator:next())
	end
end

for i = 1, efalist:size() do
	local efa = efalist:get(i-1)
	processEFA(efa)
end
