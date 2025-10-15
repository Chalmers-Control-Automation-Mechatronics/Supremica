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
local VariableHelper = luaj.bindClass("org.supremica.automata.VariableHelper")
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
local function loginfo(str)
  if str then log:info(str, 0) else log:info("nil string", 0) end
end

local function getEvents(project)
	local controllable, uncontrollable = {}, {}
	
	local eventDeclList = project:getEventDeclList()
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

--[[ Things to consider:
  * In CIF, variables are local, so for each EFA we need to know which variables it affects
  * For non-int variable types we need to define specific types
  * In CIF, two EFA cannot affect the same variable, to get around this we could synch all 
    EFA that affect the same variable
  * CIF has no +=, -= etc, these need to be converted to ordinary var = var + x expressions
  * Supremica has implicit guards to protect for out-of-bounds assignment, CIF has not, so
    such guards should be added when necessary
  * CIF has no next-state value, so primed guards need to be converted to assignment actions
--]]

-- Process the currently open module into <name>.cif
local manager = ide:getDocumentContainerManager() 
local container = manager:getActiveContainer()
local name = container:getName()
local project = container:getEditorPanel():getModuleSubject()
local components = project:getComponentList() 

local efalist = Helpers:getAutomatonList(project)
local varlist = Helpers:getVariableList(project)
local cevents, uevents = getEvents(project)


-- Preprocessing - Collect stuff necessary to be able to process
local function preProcessMarkedValues(marklist)
  local markings = {}
  local mit = marklist:iterator()
  while mit:hasNext() do
    table.insert(markings, mit:next():getPredicate():toString())
  end
  return markings
end

local function getVariableInfo(var)
  local kind = " is symbolic "
  if VariableHelper:isBinary(var) then
    kind = " is binary "
  elseif VariableHelper:isInteger(var) then
    kind = " is integer "
  end
  local range = var:getType():toString()
  local initpred = var:getInitialStatePredicate():toString()
  local markings = preProcessMarkedValues(var:getVariableMarkings())
  local markstr = ""
  if #markings > 0 then
    markstr = "{"..table.concat(markings, ", ").."}"
  end  
  return kind, range, initpred, markstr
end

local function preProcessVariables()
  local variables = {}
  
  local iterator = varlist:iterator()
  while iterator:hasNext() do
    local var = iterator:next()
    local name = var:getName()
    local kind, range, init, mark = getVariableInfo(var)
    -- loginfo(name..kind..range..", <"..init..">, "..mark)
    variables[name] = {kind = kind, range = range, init = init, mark = mark}

  end
  
  return variables
end

local function preProcessing()
  print("Preprocessing...")
  local stuff = {}
  stuff.Vars = preProcessVariables()
  
  -- Just checkin'...
  for name, info in pairs(stuff.Vars) do
    loginfo(name..info.kind..info.range..", <"..info.init..">, "..info.mark)
  end
  
  return stuff
end

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

local function processGuardAction(gablock)
  
  if not gablock then return nil, nil end -- not all edges have GA-blocks
  
  local function stripCurly(str)
    return str:match("[{%s,]*(.+),}")
  end
  
  local function convertGuard(gstr)
    if not gstr then return end
    return gstr:gsub("==", "="):gsub("&", " and "):gsub("|", " or "):gsub("!", "not ")
  end
  
  local function convertAction(astr)
    if not astr then return end
    return astr:gsub("=", ":=")
  end

-- Multiple actions on the same edge should be comma-separated in CIF
  local gatxt = gablock:toString():gsub("\n", ",")
  -- Replacing \n by , results in this type of expr:
  -- [,{, v_req==1 & v_in==0 & v_s2==1,}],{,{, v_out=1,}},
  -- [,{, v_out==1 & v_s2==0,}],{,},
  -- [,],{,{, v_in = 0, v_out = 0,}},
  
  local gcap, acap = gatxt:match("%[,(.*)%],{,(.*)},")
  return convertGuard(stripCurly(gcap)), convertAction(stripCurly(acap))
end

local function manageSourceTarget(srctrgt, efaDB)
  local label, init, acc, xxx = processSourceTarget(srctrgt:toString():gsub("\n", ""))
  
  if not efaDB.Locations[label] then
    efaDB.Locations[label] = {"location "..label..";"}
    if init then table.insert(efaDB.Locations[label], "\tinitial;") end
    if acc then table.insert(efaDB.Locations[label], "\tmarked;") end
  end
  return label
end

local function processEdge(edge, efaDB)
  
	local src = manageSourceTarget(edge:getSource(), efaDB)
  local trgt = manageSourceTarget(edge:getTarget(), efaDB)
	local guard, action = processGuardAction(edge:getGuardActionBlock())
	local evlist = edge:getLabelBlock():getEventIdentifierList()
  
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
    
    table.insert(efaDB.Locations[src], table.concat(out, " "))
	end
end

local function processEFA(efa)
  local efaDB = {} -- holds stuff releated to this particular efa
  efaDB.Locations = {} -- Holds the locations with events, guard, actions
  
  local kind = efaKind[efa:getKind()]
	local graph = efa:getGraph()
	local edges = graph:getEdges()
	local iterator = edges:iterator()
	while iterator:hasNext() do
		processEdge(iterator:next(), efaDB)
	end
  print(kind.." "..efa:getName());
  for src, body in pairs(efaDB.Locations) do
    print(table.concat(body, "\n"))
  end
  print("end\n")
end

local function processModule()
  
  print(getFileName(name..".cif"))

  if #cevents > 0 then print("controllable "..table.concat(cevents, ", ")..";") end
  if #uevents > 0 then print("uncontrollable "..table.concat(uevents, ", ")..";") end

  for i = 1, efalist:size() do
    local efa = efalist:get(i-1)
    processEFA(efa)
  end
end

preProcessing()
--processModule(efalist)
