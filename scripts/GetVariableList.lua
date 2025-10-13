-- GetVariableList.lua, retrieves a list of the variables
local luaj = luajava -- just shorthand 
local script, ide = ... -- grab the arguments passed from Java via LuaJ

-- Get convenience function to generate file name
local Config = luaj.bindClass("org.supremica.properties.Config")
local getFileName = dofile(Config.FILE_SCRIPT_PATH:getValue():getPath().."/getFileName.lua")

-- local VariableComponentProxy = luaj.bindClass("net.sourceforge.waters.model.module.VariableComponentProxy")
-- if not VariableComponentProxy then print("VariableComponentProxy not fond") return end

local Helpers = luaj.bindClass("org.supremica.Lupremica.Helpers") 
if not Helpers then print("Lupremica.Helpers not found") return end

local module = ide:getActiveDocumentContainer():getEditorPanel():getModuleSubject()
if not module then print("No active module found") return end

local varlist = Helpers:getVariableList(module)
for i = 1, varlist:size() do
	local var = varlist:get(i-1)
	print(var:getIdentifier():toString().. " : "..
		var:getType():toString().." : "..
		var:getInitialStatePredicate():toString())
end

-- To retrieve also lists of the automata and events, uncomment this:
--[[
local automata = Helpers:getAutomatonList(module)
for i = 1, automata:size() do
	local aut = automata:get(i-1)
	print(aut:getName().." : "..aut:getKind():toString())
end

local eventDeclList = module:getEventDeclList()
for i = 1, eventDeclList:size() do
  local event = eventDeclList:get(i-1)
  print(event:getName())
end
--]]