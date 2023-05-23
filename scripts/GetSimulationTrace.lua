-- GetSimulationTrace.lua, retrieves and saves the currently active simulation trace
-- Works only if run when the Simulator is open and active
local luaj = luajava
local script, ide = ...

-- Get convenience function to generate file name
local Config = luaj.bindClass("org.supremica.properties.Config")
local getFileName = dofile(Config.FILE_SCRIPT_PATH:getValue():getPath().."/getFileName.lua")

local container = ide:getActiveDocumentContainer()
local panel = container:getActivePanel()
local simulation = panel:getSimulation()
local trace = simulation:getTrace()

-- Collect the events from the trace
local events = trace:getEvents()
local tab = {"Events"}
for i = 1, events:size() do
	local event = events:get(i-1)
	table.insert(tab, event:getName())
end

local fname = getFileName("trace.csv")
local file = io.open(fname, "w")
file:write(table.concat(tab, ", "))
file:close()

print(fname.." saved")