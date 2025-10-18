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

-- Show simple error dialog
local function openIssueDialog(name, str)
  local JOptionPane = luaj.bindClass("javax.swing.JOptionPane")
  JOptionPane:showMessageDialog(ide, str, name, JOptionPane.ERROR_MESSAGE)
end

openIssueDialog("Oops!", "Eggs are not supposed to be green.")
openIssueDialog("Again!", "And neither are you!")

-- bindClass is like Java's import
local Helpers = luaj.bindClass("org.supremica.Lupremica.Helpers") 
if not Helpers then print("Lupremica.Helpers not found") return end

local EventKind = luaj.bindClass("net.sourceforge.waters.model.base.EventKind")
local ComponentKind = luaj.bindClass("net.sourceforge.waters.model.base.ComponentKind")
local VariableHelper = luaj.bindClass("org.supremica.automata.VariableHelper")
local VariableComponentProxy = luaj.bindClass("net.sourceforge.waters.model.module.VariableComponentProxy")
if not VariableComponentProxy then print("VariableComponentProxy not fond") return end

local efaKind = {} -- lookup table for automata type conversion
efaKind[ComponentKind.PLANT] = "plant"
efaKind[ComponentKind.PROPERTY] = "property"
efaKind[ComponentKind.SPEC] = "requirement"
efaKind[ComponentKind.SUPERVISOR] = "supervisor"

--local TextFrame = luaj.bindClass("org.supremica.gui.texteditor.TextFrame")
local textframe = luaj.newInstance("org.supremica.gui.texteditor.TextFrame", "CIF Export")
local pw = textframe:getPrintWriter();

local function print(str) -- redefine print to write to the textframe
  pw:println(str)
end
local function loginfo(str) -- helper to write to log
  if str then log:info(str, 0) else log:info("nil string", 0) end
end

local function getEvents(project)
	local controllable, uncontrollable = {}, {}
	
	local eventDeclList = project:getEventDeclList()
	for i = 1, eventDeclList:size() do
	  local event = eventDeclList:get(i-1)
	  local kind = event:getKind()
	  if kind == EventKind.CONTROLLABLE then
	  	-- controllable[#controllable+1] = event:getName()
      controllable[event:getName()] = true
	  elseif kind ~= EventKind.PROPOSITION then
	  	-- uncontrollable[#uncontrollable+1] = event:getName()
      uncontrollable[event:getName()] = true
	  end
	end
	return controllable, uncontrollable
end

--[[ Things to consider:
  * In CIF, variables are local, so for each EFA we need to know which variables it affects
  * For non-int variable types we need to define specific types
  * In CIF, two EFA cannot affect the same variable, to get around this we could synch all 
    EFA that affect the same variable
  * CIF has no +=, -= etc, these need to be rewritten to ordinary var = var + x expressions
  * Supremica has implicit guards to protect for out-of-bounds assignment, CIF has not, so
    such guards should be added when necessary
    # Such guards can *always* be added, and handled syntactically:
      disc int[0..5] var;
      edge up when 0 <= var + x and var + x <= 5 do var := var + x
      edge dn when 0 <= var - x and var - x <= 5 do var := var - x
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

local Variables -- Holds variable name, range, init, mark (filled by preProcess)
local CurrentEFA -- Name of EFA currently processed (set by processEFA)
local CurrentEdge -- Collects data for currently processed edge, (set by processEdge)

-- Rewriting
local pluseq = "+=" -- a += b into a = a + b
local minuseq = "-=" -- a += b into a = a - b
local multeq = "*="
local diveq = "/="

local patterns = {}
-- capture lhs += rhs, lhs == rhs, lhs = rhs, etc
-- expr patterns capture whole expressions, (lhs op rhs)
-- detail patterns capture details (lhs)(op)(rhs)
patterns.actiondetail = "([_%a][_%w]*)%s*([%+%-%*/=]+)%s*([_%w]+)"
patterns.actionexpr = "([_%a][_%w]*%s*[%+%-%*/=]+%s*[_%w]+)"
patterns.primedexpr = "([_%a][_%w]*'%s*[%+%-%*=/]+%s*[_%w]+)"
patterns.guardexpr = "([_%a][_%w]*%s*[%+%-%*=/]+%s*[_%w]+)"
patterns.matchrange = "(%d+)%.%.(%d+)"
-- https://www.lua.org/pil/20.2.html
-- https://iamreiyn.github.io/lua-pattern-tester/

local rewrites = {}
rewrites.doit = function(lhs, op, rhs)
  local newlhs = lhs..op..rhs
  -- loginfo("newlhs: "..newlhs)
  return lhs.." := "..newlhs, newlhs
end
rewrites[pluseq] = function(lhs, rhs)
  return rewrites.doit(lhs, " + ", rhs)
end
rewrites[minuseq] = function(lhs, rhs)
  return rewrites.doit(lhs, " - ", rhs)
end
rewrites[multeq] = function(lhs, rhs)
  return rewrites.doit(lhs, " * ", rhs)
end
-- Are these better handled by gsub on the original string?
-- The expressions can look like "v_req==1 & v_in==0 & v_s2==1"
rewrites["="] = function(lhs, rhs) 
  return rewrites.doit(lhs, " := ", rhs)
end
rewrites["=="] = function(lhs, rhs)
  return rewrites.doit(lhs, " = ", rhs)
end
  
-- Preprocessing - Collect stuff necessary to be able to process
local function preProcessMarkedValues(marklist)
  local markings = {}
  local mit = marklist:iterator()
  while mit:hasNext() do
    table.insert(markings, mit:next():getPredicate():toString())
  end
  return markings
end

-- Global constants, do NOT assign!
local IS_INTEGER = " is integer "
local IS_BINARY = " is binary "
local IS_ENUM = " is enum "

local function getVariableInfo(var)
  local kind = IS_ENUM
  if VariableHelper:isBinary(var) then
    kind = IS_BINARY
  elseif VariableHelper:isInteger(var) then
    kind = IS_INTEGER
  end
  local range = var:getType():toString()
  local initpred = var:getInitialStatePredicate():toString()
  local markings = preProcessMarkedValues(var:getVariableMarkings())
  local markstr = ""
  if #markings > 0 then
    -- markstr = "{"..table.concat(markings, ", ").."}"
    markstr = table.concat(markings, ", ")
  end  
  return kind, range, initpred, markstr
end

-- In Supremica, initial value predicates cannot be primed
-- and initial value predicates cannot refer to other variables
-- But they can be written as 0 == var
-- In CIF, we write: disc int[0..1] var in any; initial var = 0 or var = 1; marked var = 1;
-- Does CIF also allow 0 = var? YES! So we do not have to handle this

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
  
  Variables = preProcessVariables()
  
  --[[ Just checkin'...
  for name, info in pairs(stuff.Vars) do
    loginfo(name..info.kind..info.range..", <"..info.init..">, "..info.mark)
  end
  --]]
  
end
-- Preprocessing above, main processing below
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

local function convertGuard(gstr)
  if not gstr then return end
  return gstr:gsub("==", "="):gsub("&", " and "):gsub("|", " or "):gsub("!", "not ")
end
  
-- For each action guards should be added that gurantee no over- or underflow
-- A primed guard must be turned into an action, which then requires to add guards!
-- So, processAction must be able to generate guards, and
-- processGuards must be able to generate actions!
-- Also, processAction must have access to the currentEFA so that it can record and check
-- that/if two different EFA assign the same variable, CIF does not allow this
local function processAction(str, gastore)
  -- str is a comma-separated sequence of actions, possibly empty
  if not str or str == "" then return nil end
  
  local cifexpr = {}
  
  for cap in str:gmatch(patterns.actionexpr) do
    local lhs, op, rhs = cap:match(patterns.actiondetail)
    local expr, newlhs = rewrites[op](lhs, rhs)
    cifexpr[#cifexpr+1] = expr
    -- lhs is a variable name, should now add guard
    -- (lhs.bottom <= newlhs and newlhs <= lhs.topper)
    local var = Variables[lhs]
    if var.kind == IS_INTEGER  then
      local bottom, topper = var.range:match(patterns.matchrange)
      local newguard = "("..tonumber(bottom).."<="..newlhs.." and "..newlhs.."<="..tonumber(topper)..")"
      table.insert(gastore.guards, newguard)
    end
  end
  return table.concat(cifexpr, ", ")
end

-- Primed guards are unknown to CIF, and so must be turned into actions
-- 
local function processGuard(str, gastore)
  if not str or str == "" then return nil end
  
end


local function processGuardAction(gablock)
  
  if not gablock then return nil, nil end -- not all edges have GA-blocks
  
  local gastore = {}
  gastore.guards, gastore.actions = {}, {}
  
  local function stripCurly(str)
    return str:match("[{%s,]*(.+),}")
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
  
  -- Get rid of commas and outer braces
  local gcap, acap = gatxt:match("%[,(.*)%],{,(.*)},")
  acap = stripCurly(acap)
  gcap = stripCurly(gcap)
  
  return convertGuard(gcap, gastore), processAction(acap, gastore)
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

local function getEdgeEvents(edge)
  local evlist = edge:getLabelBlock():getEventIdentifierList()
  local cevs, uevs = {}, {}
  local iter = evlist:iterator()
  
  while iter:hasNext() do
    local ev = iter:next():getName()
    if cevents[ev] then 
      table.insert(cevs, ev)
    elseif uevents[ev] then
      table.insert(uevs, ev)
    else
      assert(false, "Event "..ev.." not in project event list!")
    end
  end
  
  return cevs, uevs
end

local function makeEdge(target, events, guard, action)
  
  local out = {}
  table.insert(out, "\tedge ")
  table.insert(out, table.concat(events, ", "))
  if guard then
    table.insert(out, "when")
    table.insert(out, guard)
  end
  if action then
    table.insert(out, "do")      
    table.insert(out, action)
  end
  table.insert(out, "goto")
  table.insert(out, target)
  table.insert(out, ";")
  
  return table.concat(out, " ")
end

local function processEdge(edge, efaDB)
  
  -- Set up global edge data repo -- Maybe not needed? Only in processGuardAction?
  CurrentEdge = {}
  CurrentEdge.guards = {}
  CurrentEdge.actions = {}
  CurrentEdge.uevents = {}
  CurrentEdge.cevents = {}

	local src = manageSourceTarget(edge:getSource(), efaDB)
  local trgt = manageSourceTarget(edge:getTarget(), efaDB)
	local guard, action = processGuardAction(edge:getGuardActionBlock())
	local controllable, uncontrollable = getEdgeEvents(edge) -- edge:getLabelBlock():getEventIdentifierList()
  
  table.insert(efaDB.Locations[src], makeEdge(trgt, controllable, guard, action))
  table.insert(efaDB.Locations[src], makeEdge(trgt, uncontrollable, guard, action))
end

local function processEFA(efa)
  
  -- Set up global current EFA name holder
  CurrentEFA = efa:getName()
  
  local efaDB = {} -- holds stuff releated to this particular efa
  efaDB.Locations = {} -- Holds the locations with events, guard, actions
  
  local kind = efaKind[efa:getKind()]
	local graph = efa:getGraph()
	local edges = graph:getEdges()
	local iterator = edges:iterator()
	while iterator:hasNext() do
		processEdge(iterator:next(), efaDB)
	end
  print(kind.." "..CurrentEFA);
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
processModule(efalist)
