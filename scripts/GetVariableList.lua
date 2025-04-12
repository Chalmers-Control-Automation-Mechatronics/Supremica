-- GetVariableList.lua, retrieves and saves a list fo all variable with properties
local luaj = luajava -- just shorthand 
local script, ide = ... -- grab the arguments passed from Java via LuaJ

-- Get convenience function to generate file name
local Config = luaj.bindClass("org.supremica.properties.Config")
local getFileName = dofile(Config.FILE_SCRIPT_PATH:getValue():getPath().."/getFileName.lua")

local VariableComponentProxy = luaj.bindClass("net.sourceforge.waters.model.module.VariableComponentProxy")
if not VariableComponentProxy then print("VariableComponentProxy not fond") return end

local GetVariableList = luaj.bindClass("Lupremica.GetVariableList") 
if not GetVariableList then print("Lupremica.GetVariableList not found") return end

--[[
local module = ide:getActiveDocumentContainer():getEditorPanel():getModuleSubject()
local components = module:getComponentList() 
-- Gets the component list of this module. This List<Proxy> does not only contain the 
-- automata (SimpleComponentProxy) of the module, but also all EFA variables
-- (VariableComponentProxy) and module instances (InstanceProxy). 
-- All these items can be nested in foreach blocks (ForeachProxy).
-- From https://www.cs.waikato.ac.nz/~robi/waters-doc/index.html
local output = {} print("Size:", components:size())
for i = 1, components:size() do
    local proxy = components:get(i-1)
    -- local proxyClone = proxy:clone() -- seems to work
	-- if proxy:getProxyInterface():getSimpleName():equals("VariableComponentProxy") then
	if VariableComponentProxy.class:isInstance(proxy) then
		logger.info("var.getIdentifier(): " + proxy:getIdentifier())
		logger.info("var.getType().toString(): " + proxy:getType().toString())
		logger.info("var.getInitialStatePredicate().toString(): " + proxy:getInitialStatePredicate().toString())
	end
end
--]]

local fname = getFileName("components.csv") -- print(fname)
-- local file = io.open(fname, "w")
-- file:write(table.concat(output, ", "))
-- file:close()