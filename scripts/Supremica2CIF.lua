-- Supremica2CIF.lua, Lua script to convert Supremica models to CIF
-- Meant to be run as a script inside Supremica (with LuaJ embedded)
local luaj = luajava -- just shorthand 
local script, ide, log = ... -- grab the arguments passed from Java via LuaJ

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
local ComponentKind = luaj.bindClass("net.sourceforge.waters.model.base.ComponentKind")
local VariableComponentProxy = luaj.bindClass("net.sourceforge.waters.model.module.VariableComponentProxy")
if not VariableComponentProxy then print("VariableComponentProxy not fond") return end

local efaKind = {}
efaKind[ComponentKind.PLANT] = "plant"
efaKind[ComponentKind.PROPERTY] = "property"
efaKind[ComponentKind.SPEC] = "requirement"
efaKind[ComponentKind.SUPERVISOR] = "supervisor"

--local TextFrame = luaj.bindClass("org.supremica.gui.texteditor.TextFrame")
local textframe = luaj.newInstance("org.supremica.gui.texteditor.TextFrame", "CIF Export")
local pw = textframe:getPrintWriter();

local function print(str) -- redefien print to write to the textframe
  pw:println(str)
end

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

local function processSourceTarget(srctxt)
  -- initial S0 { :accepting}
  -- S1 { :forbidden :accepting}
  local initial, label, acc, xxx
  
  -- This is fugly! Find a better way
  -- Note that this does not catch user-defined propositions
  for capture in srctxt:gmatch("(%w+)") do
    if capture == "initial" then
      initial = true
    elseif capture == "forbidden" then
      xxx = true
    elseif capture == "accepting" then
      acc = true
    else
      label = capture
    end
  end
  return label, initial, acc, xxx
end

local function processGuardAction(gatxt)
  
  local function stripCurly(str)
    return str:match("{%s*(.+)%s*}")
  end
  
  -- [{  v_req==1 & v_in==0 & v_s2==1}]{{  v_out=1}}
  -- []{{  v_req = 1}}
  
  local gcap, acap = gatxt:match("%[(.*)%]{(.*)}") 
  return stripCurly(gcap), stripCurly(acap) -- either (or both) of these can be nil
end

local function processEdge(edge)
	local src, init, acc, xxx = processSourceTarget(edge:getSource():toString():gsub("\n", ""))
	local trgt = processSourceTarget(edge:getTarget():toString():gsub("\n", "")) -- only care about label
	local guard, action = processGuardAction(edge:getGuardActionBlock():toString():gsub("\n", ""))
	local evlist = edge:getLabelBlock():getEventIdentifierList()
  
  print("location "..src..":")
  if init then print("\tinitial;") end
  if acc then print("\tmarked;") end
  local out = {}
	for i = 1, evlist:size() do
		local ev = evlist:get(i-1)
		table.insert(out, "\tedge ")
    table.insert(out, ev:toString())
    if guard then
      table.insert(out, "when")
      table.insert(out, guard)
    end
    if action then
      table.insert(out, "do")      
      table.insert(out, action)
    end
    table.insert(out, "goto")
    table.insert(out, trgt)
    table.insert(out, ";")
    print(table.concat(out, " "))
	end
end

local function processEFA(efa)
  local kind = efaKind[efa:getKind()]
	print(kind.." "..efa:getName())
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
