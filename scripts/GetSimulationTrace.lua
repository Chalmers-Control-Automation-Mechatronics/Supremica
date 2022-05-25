-- GetSimulationTrace.lua, retrieves and prints the currently active simulation trace
-- Works only if run when the Simulator is open and active
local luaj = luajava

local script, ide = ...

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

-- Get the configured TEMP directory, and write the file "trace.csv" to there
local Config = luaj.bindClass("org.supremica.properties.Config")
local tempdir = Config.FILE_TEMP_PATH:getValue():getPath()
local lastch = tempdir:sub(-1) -- get last character
local sep = "/"
if lastch == '/' or lastch == '\\' then
	sep = ""
end
local fname = tempdir..sep.."trace.csv"
local file = io.open(fname, "w")
file:write(table.concat(tab, ", "))
file:close()