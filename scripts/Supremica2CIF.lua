-- Supremica2CIF.lua, Lua script to convert Supremica models to CIF
-- Meant to be run as a script inside Supremica (with LuaJ embedded)
local luaj = luajava -- just shorthand 
local script, ide, log = ... -- grab the arguments passed from Java via LuaJ

-- Get convenience function to generate file name
local Config = luaj.bindClass("org.supremica.properties.Config")
local getFileName = dofile(Config.FILE_SCRIPT_PATH:getValue():getPath().."/getFileName.lua")

-- Some useful Java classes
local JOptionPane = luaj.bindClass("javax.swing.JOptionPane")

local function showFileChooser(fname, fpath)
	local fc = luaj.newInstance("javax.swing.JFileChooser", fpath)
	fc:setDialogTitle("Give CIF File")
  -- Unclear why this does not work, maybe the varargs
  -- local ff = luaj.newInstance("javax.swing.filechooser.FileNameExtensionFilter", "CIF file", "cif")
  -- fc:setFileFilter(ff)
  local suggestion = luaj.newInstance("java.io.File", fname)
  fc:setSelectedFile(suggestion)
  fc:setApproveButtonText("Save")
  local retval = fc:showOpenDialog(ide) 
  if retval== fc.APPROVE_OPTION then
		local fname = fc:getSelectedFile():getPath() -- does not work on Java > 8
		-- local fname = fc:getName(fc:getSelectedFile()) -- this works for Java > 8
    return fname
  else
    return nil
  end
end

local function fileExists(filename)
  local file = io.open(filename, "r")
  if file then
    file:close()
    return true
  end
  return false
end

local function checkFileExists(filename)
  local reply = JOptionPane.YES_OPTION
  if fileExists(filename) then
    reply = JOptionPane:showConfirmDialog(ide, filename.."\nOverwrite?", "File exists", JOptionPane.YES_NO_OPTION)
  end
  return reply == JOptionPane.YES_OPTION
end

local function saveFile(filename, contents)
  local reply = checkFileExists(filename)
  if reply then
    print("Saving to: "..filename)
    local file = io.open(filename, "w")
    file:write(contents)
    file:close()
  else
    print("Not saving model")
  end
end

local function saveModel(fname, fpath, contents)
	local filename = showFileChooser(fname, fpath)
  if filename then
    saveFile(filename, contents)
	else
		print("User cancelled")
	end
end

-- Lua 5.2 and earlier do not have math.tointeger
local function tointeger(val)
  local num = tonumber(val)
  if not num then return nil end
  
  return math.floor(num)
end

-- Show simple error dialog
local function showIssueDialog(name, str)
  JOptionPane:showMessageDialog(ide, str, name, JOptionPane.ERROR_MESSAGE)
end

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
local pw = textframe:getPrintWriter()
textframe:setVisible(true)

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
      controllable[event:getName()] = true
	  elseif kind ~= EventKind.PROPOSITION then
      uncontrollable[event:getName()] = true
    -- else -- is proposition, unclear how to deal with that
	  end
	end
	return controllable, uncontrollable
end

--[[ Things to consider:
  * In CIF, variables are local, so for each EFA we need to know which variables it affects
  * For non-int variable types we need to define specific types
  * Supremica allows to compare two enums of different "types", CIF does not
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
  * Boolean variables cannot be considered as 0..1 variables when converting t0 PLC code! 
    Codesys complains that "cannot conver type DINT to type BOOL". Supremica has no BOOL type...
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
local CurrentEFA -- EFA currently processed (set by processEFA, contains .name and .variables)
local CurrentEdge -- Collects data for currently processed edge (set by processEdge)
local Storage = {} -- holds the generated EFA for delayed output (see outputEFA)
local FileName -- The filename to save under (set by processModule)

--[[
  Supremica allows to compare two enums of different types, basically checking if the
  identifiers are equal when compared as strings. This is not allowed by CIF, only enums 
  of the same type can be compared. To get around this, we put all Supremica enums in
  one big Enums type, being careful to remove duplicates, while still keeping the enum
  ranges (expanded) for each Supremica enum variable. This set is built by processVariables()
--]]
local Enums = {}

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
patterns.matchrange = "(%-?%d+)%.%.(%-?%d+)"
patterns.identifier = "([_%a][_%w]*)"
patterns.colonatend = ":$"
-- https://www.lua.org/pil/20.2.html
-- https://iamreiyn.github.io/lua-pattern-tester/

local rewrites = {}
rewrites.doit = function(lhs, op, rhs)
  local newrhs = lhs..op..rhs
  return lhs.." := "..newrhs, newrhs
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
-- Values outside the domain can be assigned a variable in Supremica
-- so even in that case should we generate explicit guards in CIF
rewrites["="] = function(lhs, rhs) 
  return lhs.." := "..rhs, rhs
end
rewrites["=="] = function(lhs, rhs)
  return rewrites.doit(lhs, " = ", rhs)
end

local function convertGuard(gstr)
  return gstr:gsub("==", "="):gsub("&", " and "):gsub("|", " or "):gsub("![^=]", "not ")
end

local function debugOrphans(name, orphans)
  for k, v in pairs(orphans) do
    loginfo(name..": "..k)
  end
end

-- The orphans are collected in a simple map
local function mergeOrphans(orph1, orph2)
  assert(orph1, "orph1 is nil")
  assert(orph2, "orph2 is nil")
  
  for k, v in pairs(orph2) do
    orph1[k] = v
  end
  return orph1
end

-- Return the end values of an integer range (inclusive)
local function getIntRangeLimits(range)
  local bottom, topper = range:match(patterns.matchrange)
  return tointeger(bottom), tointeger(topper)
end
-----------------------------------------------------------------
-- Preprocessing - Collect stuff necessary to be able to process
-----------------------------------------------------------------
local function preProcessMarkedValues(marklist)
  local markings = {}
  local mit = marklist:iterator()
  while mit:hasNext() do
    local mstr = mit:next():getPredicate():toString()
    local str = convertGuard(mstr)
    table.insert(markings, str)
  end
  return markings
end

local function preProcessInitPredicate(str)
  return convertGuard(str)
end

-- Global constants, do NOT assign!
local IS_INTEGER = " is integer "
local IS_BINARY = " is binary "
local IS_ENUM = " is enum "
local IS_BOOL = " is bool "

-- for looking up the Boolean values (see getEnumInfo())
local TFlookup = {} 
TFlookup["true"] = true
TFlookup["false"] = true
  
--[[ There are no Boolean variables in Supremica, only 0-1 integers
  Three options to deal with these:
  1. Convert them to "disc int[0..1]" in CIF. This allows to do arithmetic on them. However, 
    this creates a problem with PLC code generation! When a variable is defined to be 
    disc int[0..1], then it is not converted by CIF into a bool in the PLC code, but remains
    integer, which makes sense, but then its mapping to boolean I/O is refused by Codesys.
  2. Convert then to "disc bool" in CIF. This has the problem of not allowing arithemetic on
    them, which is typical with 0-1 variables
  3. If an enum variable has the range "true,false" or "false,true", then we treat this as a
    bool on the CIF side. In this case Booleans are treated as a special variant of enums,
    and Supremica's binary and integer types are both treated as integer. 
    
  Option 3 is implemented here.
--]]
local function getIntegerInfo(var)
  
  local range = var:getType():toString()
  local initpred = preProcessInitPredicate(var:getInitialStatePredicate():toString())
  local markings = preProcessMarkedValues(var:getVariableMarkings())
  local markstr = ""
  if #markings > 0 then
    markstr = table.concat(markings, ", ")
  end  
  return IS_INTEGER, range, initpred, markstr  
  
end
local function getBinaryInfo(var)
  local kind, range, initpred, markstr = getIntegerInfo(var)
  -- Should we always (or never?) treat 0-1 variables as bool?
  local bottom, topper = getIntRangeLimits(range)
  assert(bottom == 0 and topper == 1, "Unexpected range "..bottom..".."..topper.." for binary variable")
  
  return IS_BINARY, range, initpred, markstr
end

-- A special type of Supremica enums ar ethose with range [true,false] or [false,true]
-- Those are treated as booleans on the CIF side.
-- Note that we could in Supremica have an enum with range [true,middle,false]
-- or even a degenerate one with single lement range [false]
-- Such cannot be allowd to slip through to CIF
local function getEnumInfo(var)
  
  local kind = IS_ENUM -- this is the default assumption
  
  local range = {}
  for ident in var:getType():toString():gmatch(patterns.identifier) do
    range[#range + 1] = ident
    Enums[ident] = true -- addToEnums(ident)
  end
  
  if #range == 2 then -- this might be a bool
    if TFlookup[range[1]] and TFlookup[range[2]] then -- the two values were "true" and "false", this is a bool
      kind = IS_BOOL
      -- loginfo(var:getName()..IS_BOOL)
      Enums["true"] = nil   -- remove from the set of enums
      Enums["false"] = nil
    end
  else -- check that true or false are not used as enum values in any other way
    for i = 1, #range do
      if TFlookup[range[i]] then
        showIssueDialog("Boolean values used as enum values...", 
          "Non-boolean enums cannot use \"true\" or \"false\" as enum values\n"..
          var:getName().."\nhas one or both of these in its range\nPlease avoid this.\nQuitting...")
        textframe:setVisible(false)
        assert(false, "Non-Boolean use of \"true\" or \"false\" is not allowed by CIF")
      end
    end
  end
  
  local initpred = preProcessInitPredicate(var:getInitialStatePredicate():toString())
  local markings = preProcessMarkedValues(var:getVariableMarkings())
  local markstr = ""
  if #markings > 0 then
    markstr = table.concat(markings, ", ")
  end  
  return kind, range, initpred, markstr  
  
end
 
local function getVariableInfo(var)
  if VariableHelper:isBinary(var) then
    return getBinaryInfo(var)
  elseif VariableHelper:isInteger(var) then
    return getIntegerInfo(var)
  else -- it is an enum
    return getEnumInfo(var)
  end
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
    variables[name] = {kind = kind, range = range, init = init, mark = mark, owner = nil} -- efa = {}}
  end
  
  return variables
end

local function preProcessing()
  
  Variables = preProcessVariables()
  
  --[[ Just checkin'...
  for name, info in pairs(Variables) do
    if info.kind == IS_ENUM then
      loginfo(name..info.kind.."["..table.concat(info.range, ",").."], <"..info.init..">, "..info.mark)
    else
      loginfo(name..info.kind..info.range..", <"..info.init..">, "..info.mark)
    end
  end
  --]]
  
end
----------------------------------------------
-- Preprocessing above, main processing below
----------------------------------------------
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

-- Pure syntactic replacement of operators is not enough, as guards and actions include
-- variables, and in CIF these need to be prefixed by their owner names
-- Calling this function during processing will not fix all prefixing
local function prefixOwner(str)
  local orphans = {}
  -- For identifiers that are variable names, if possible prefix with owner
  for ident in str:gmatch(patterns.identifier) do
    local var = Variables[ident]
    if var then -- this is a variable
      if var.owner then -- someone already owns this variable, is it us?
        if CurrentEFA.name ~= var.owner then -- owned by someone but not us
          -- Prefix with the owner
          str = str:gsub(ident, var.owner.."."..ident)
        end
      else
        -- This variable is not yet owned by anyone
        -- Need to remember this to do the prefixing later
        orphans[ident] = true -- need a map for quick lookup and avoiding dupicates
      end
    end
  end  
  assert(orphans, "8. Oprhans nil!")
  return str, orphans
end
-- The above code relies on the fact that Supremica does not implement proper namespaces
-- Variable names, enum values, automata names, must be all distinct from each other
-- But note! Events can have the same label as enum value, variable name, automata name
-- Also, we cannot have things like "X.ident", for which the code above would wreak havoc 

-- Returns the full extension of an integer range given as bottom..topper
-- restricted to the given limits (inclusive)
local function unfoldRange(range, botlimit, toplimit)

  local out = {}
  local bottom, topper = getIntRangeLimits(range)
  if botlimit then
    bottom = math.max(bottom, botlimit)
  end
  if toplimit then
    topper = math.min(topper, toplimit)
  end
  for i = bottom, topper do
    table.insert(out, i)
  end
  
  return out -- table like {bottom, bottom + 1, ..., topper - 1, topper}
end

local function isWithinRange(value, range)
  -- value is int, range is string like "9..55"
  local bottom, topper = getIntRangeLimits(range)
  return bottom <= value and value <= topper
end 

local function protectIntBinary(range, newrhs)
  -- First check a special case, newrhs single number
  -- if that number is within the range, no need of protective guard
  local val = tointeger(newrhs)
  if val then -- newrhs is simply a number that can be checked
    if isWithinRange(val, range) then -- no need to add guard
      return nil, {} -- second element here is orphans, should it be empty?
    end
  end
  -- newrhs either not a number or not within range
  local bottom, topper = getIntRangeLimits(range) -- range:match(patterns.matchrange)
  -- newguard = (lhs.bottom <= newrhs and newrhs <= lhs.topper)
  local owned, orphans = prefixOwner(newrhs)
  assert(orphans, "9. Oprhans nil!")
  return "("..bottom.." <= "..owned.." and "..owned.." <= "..topper..")", orphans
end

local function protectEnums(range, newrhs)
  -- For enums, the range looks like {e1, e2, e3}
  -- the guard should check all values, and then disjunct them
  -- Is there a better way in CIF?
  -- Note that checking all values here will not work, since enum1 = enum2 is valid
  local out, orphans = {}, {}
  for i = 1, #range do
    local enumval = range[i]
    if newrhs == enumval then -- assignment is of an existing enum value, no need to guard
      return nil, {}
    end
    local owned, orph = prefixOwner(newrhs)
    assert(orph, "42: Orph is nil!")
    table.insert(out, owned.." = "..enumval)
    orphans = mergeOrphans(orphans, orph)
  end
  assert(orphans, "10. Oprhans nil!")
  return "("..table.concat(out, " or ")..")", orphans
end

-- Supremica has implict guards that protect againts out-of-domain assignments
-- These need to be explicitly added for CIF
local function addProtectiveGuards(lhs, newrhs, gastore)
    -- lhs is a variable name, newrhs is the rewritten rhs
    local var = Variables[lhs]
    if var.kind == IS_INTEGER or var.kind == IS_BINARY then
      local guard, orphans = protectIntBinary(var.range, newrhs)
      table.insert(gastore.guards, guard)
      assert(orphans, "1. Oprhans nil!")
      return orphans
    elseif var.kind == IS_ENUM or var.kind == IS_BOOL then 
      local guard, orphans = protectEnums(var.range, newrhs)
      table.insert(gastore.guards, guard)
      assert(orphans, "2. Oprhans nil!")
      return orphans
    end
    assert(false, "Unknown variable type: "..Variables[var].kind.." (variable: "..var..")")
  end
  
  local function makeVarDef(var)
    if Variables[var].kind == IS_ENUM then
      return "\tdisc Enums "..var
    elseif Variables[var].kind == IS_BOOL then
      return "\tdisc bool "..var
    elseif Variables[var].kind == IS_INTEGER or Variables[var].kind == IS_BINARY then
      return "\tdisc int["..Variables[var].range.."] "..var
    end
    assert(false, "Unknown variable type: "..Variables[var].kind.." (variable: "..var..")")
  end
  
  -- The given variable is assigned by this EFA, so it owns it
  -- CIF does not allow multiple EFA owning the same variable
  local function ownThisVariable(var)
    
    if not Variables[var].owner then -- this variable is still orphan
      Variables[var].owner = CurrentEFA.name -- remember the owner of this variable
      local out = {}
      table.insert(out, makeVarDef(var))
      table.insert(out, "\tinitial "..Variables[var].init)
      if Variables[var].mark and Variables[var].mark ~= "" then
        table.insert(out, "\tmarked "..Variables[var].mark)
      end
      table.insert(CurrentEFA.variables, table.concat(out, ";\n")..";\n")
      return
    end
    -- else some efa already owns this variable, it might be us
    if Variables[var].owner == CurrentEFA.name then -- we are the owner, all is fine
      return
    end
    -- Someone else already owns this variable, and it isn't us
    local owners = Variables[var].owner.." and "..CurrentEFA.name
    showIssueDialog("Multiple EFA assign same variable...", "In CIF, two automata cannot assign the same variable.\n"..
      var.."\nis assigned by "..owners..".\nMake it be assigned in a single EFA.\nQuitting...")
    
    textframe:setVisible(false)
    assert(false, "Different EFA assigning the same variable is not allowed by CIF")
    
  end
  
-- For each action, guards should be added that gurantee no over- or underflow
-- A primed guard must be turned into an action, which then requires to add guards!
-- So, processAction must be able to generate guards, and
-- processGuards must be able to generate actions!
-- Also, processAction must have access to the currentEFA so that it can record and check
-- if two different EFA assign the same variable, CIF does not allow this
local function processAction(str, gastore)
  -- str is a comma-separated sequence of actions, possibly empty
  if not str or str == "" then return {} end
  local orphans = {}
  for cap in str:gmatch(patterns.actionexpr) do
    local lhs, op, rhs = cap:match(patterns.actiondetail)
    local expr, newrhs = rewrites[op](lhs, rhs)
    local action, orph1 = prefixOwner(expr)
    assert(orph1, "22. orph1 nil")
    orphans = mergeOrphans(orphans, orph1)
    table.insert(gastore.actions, action)
    ownThisVariable(lhs)
    if newrhs then -- only when necessary
      local orph2 = addProtectiveGuards(lhs, newrhs, gastore)
      assert(orph2, "23. orph2 nil")
      orphans = mergeOrphans(orphans, orph2)
    end
  end
  assert(orphans, "3. Oprhans nil!")
  return orphans
end

local function manageGuard(gstr)
  
  local newstr = convertGuard(gstr)
  local prefxd, orphans = prefixOwner(newstr)
  --[[
    debugOrphans(CurrentEFA.name, orphans)
  --]]
  assert(orphans, "4. Oprhans nil!")
  return prefxd, orphans

end

-- Primed guards are unknown to CIF, and so must be turned into actions
local function processGuard(str, gastore)
  -- str is a logical operator separated sequence of predicates
  -- Replacements can be done inline, see convertGuard()
  -- The only(?) complication is primed guards, that CIF do not recognize
  
  if not str or str == "" then return {} end
  
  if not str:find("'") then -- simply convert syntactically and return
    -- loginfo(str)
    local prefixed, orphans = manageGuard(str)
    table.insert(gastore.guards, "("..prefixed..")")
    assert(orphans, "5. Orphans nil!")
    return orphans
  end
  -- Else we have a guard with at least one primed variable
  -- This has to be turned into an action.
  
  -- guard like (varX' == 1) is trivially rewritten as action (varX := 1)
  -- with protective guard (varX.bottom <= 1 and 1 <= varX.topper)
  
  -- guard like (varX' == varY), can in CIF be written as the action (varX := varY)
  -- with protecting guard (varX.bottom <= varY and varY <= varX.topper)
  
  -- guard like (varX' < 2), can in CIF be written as (varX := {0, 1})
  -- with the assignment set unfoldRange(varX.range, _, 2-1)
  -- To determine the assignment range requires to know the operator (<, <=, >, >=)
  -- No protective guard needed!
  
  -- guard like (1 < varX' & varX' < 4) can in CIF be written as (varX := {2, 3})
  -- The assignment set being unfoldRange(varX.range, 1+1, 4-1)
  -- No protective guard needed!
  
  -- guard like (varX' < varY), ...
  -- To properly convert this requires knowing the current value of varY!
  
  -- guard like (varY' == 2 | varZ' == -7) should be turned into what?
  -- It seems that Supremica itself has problems with this, see Issue #151
  
  showIssueDialog("Cannot handle primed guards...", 
    "Currently this script does not handle primed guards. Please rewrite\n"..str..
    " in "..CurrentEFA.name.."\nas action(s).\n")
  textframe:setVisible(false)
  assert(false, "Supremica2CIF cannot handle primed guards")
end

local function processGuardAction(gablock)
  
  if not gablock then return nil, nil, {} end -- not all edges have GA-blocks
  
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
  
  local orph1 = processGuard(gcap, gastore) assert(orph1, "657: orph1 is nil")
  local orph2 = processAction(acap, gastore) assert(orph2, "658: orph2 is nil")
  local orphans = mergeOrphans(orph1, orph2)
  assert(orphans, "6. Oprhans nil!")
  return table.concat(gastore.guards, " and "), table.concat(gastore.actions, ", "), orphans

end

local function manageSourceTarget(srctrgt)
  local label, init, acc, xxx = processSourceTarget(srctrgt:toString():gsub("\n", ""))
  
  if not CurrentEFA.locations[label] then
    local out = {"location "..label..":"}
    if init then table.insert(out, "\tinitial;") end
    if acc then table.insert(out, "\tmarked;") end
    CurrentEFA.locations[label] = { table.concat(out, "\n") }
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
  if #events == 0 then return nil end
  
  local out = {}
  table.insert(out, "\tedge ")
  table.insert(out, table.concat(events, ", "))
  if guard and guard ~= "" then
    table.insert(out, "when")
    table.insert(out, guard)
  end
  if action and action ~= "" then
    table.insert(out, "do")      
    table.insert(out, action)
  end
  table.insert(out, "goto")
  table.insert(out, target)
  table.insert(out, ";")
  
  return table.concat(out, " ")
end

local function processEdge(edge)
  
	local src = manageSourceTarget(edge:getSource())
  local trgt = manageSourceTarget(edge:getTarget())
	local guard, action, orphans = processGuardAction(edge:getGuardActionBlock())
  assert(orphans, "7. Oprhans nil!")
	local cevents, uevents = getEdgeEvents(edge)
  
  -- Make different edges for controllable and uncontrollable events
  if #cevents > 0 then
    table.insert(CurrentEFA.locations[src], {makeEdge(trgt, cevents, guard, action), orphans})
  end
  if #uevents > 0 then
    table.insert(CurrentEFA.locations[src], {makeEdge(trgt, uevents, guard, action), orphans})
  end  
end

-- For each edge, after processing, there will be a set of orphans that need to 
-- be owner-prefixed when the edge is output

local function processEFA(efa)
  
  -- Set up global current EFA holder
  CurrentEFA = {name = efa:getName()}
  CurrentEFA.kind = efaKind[efa:getKind()]
  CurrentEFA.variables = {} -- in CIF, variables are local to EFA, need to collect
  CurrentEFA.locations = {} -- Holds the locations with events, guard, actions
  
  -- Get edge iterator from Supremica
	local graph = efa:getGraph()
	local edges = graph:getEdges()
	local iterator = edges:iterator()
	while iterator:hasNext() do
		processEdge(iterator:next())
	end

end

local function getEventTable(ev)
  local out = {}
  for k, v in pairs(ev) do
    table.insert(out, k)
  end
  return out
end

local function outputEvents()
  local c = getEventTable(cevents)
  local u = getEventTable(uevents)
  if #c > 0 then print("controllable "..table.concat(c, ", ")..";") end
  if #u > 0 then print("uncontrollable "..table.concat(u, ", ")..";\n") end
end

local function outputEnums()
  local out = {}
  for e, _ in pairs(Enums) do
    out[#out + 1] = e
  end
  if #out > 0 then
    print("// All Supremica enums are put in a single CIF enum type, since")
    print("// in Supremica enums of different types can be compared.")
    print("enum Enums = "..table.concat(out, ", ")..";\n")
  end
end

-- In Supremica, variables are not owned by any specific EFA, they are free for all
-- In CIF, variables MUST be owned by some EFA
-- In the conversion, the first EFA to assign a variable is considered its owner
-- 1. Multiple EFA assigning the same variable cannot be allowed, see ownThisVariable()
-- 2. A variable not assigned by any EFA, but always keeping its initial value, could be replaced
-- by its initial value, and CIF warns about this. BUT! The initial value can be nondeterministic!

local function outputEdge(edge, name)
  
  local str = edge[1]
  local orphans = edge[2]

  for var, _ in pairs(orphans) do
    local owner = Variables[var].owner
    if owner ~= name then -- sometimes self-owned variables in actions are added to orphans. BUG!
      str = str:gsub(var, owner.."."..var)
    end
  end
  print(str)
  
end

-- Must delay the output of the EFA, until we know which EFA touches which variable
-- This so, since we need to prefix other EFA's variables with their toucher's name
-- This applies to both guards and actions, as we could have an action varX := varY,
-- which if varY is owned by efa2 needs to be converted to varX := efa2.varY
-- So, for each edge there will be a set of orphans that need to be owner-prefixed 
-- before the edge is output
local function outputEFA(efa)
  print(efa.kind.." "..efa.name..":");
  print(table.concat(efa.variables, "\n"))
  for src, body in pairs(efa.locations) do
    if #body == 1 then -- no edges, might also not have "initial" or "marked"
      print(body[1]:gsub(patterns.colonatend, ";")) -- change : to ; if : is the last char
    else
      print(body[1])
      for i = 2, #body do
        outputEdge(body[i], efa.name)
      end
    end
  end
  print("end // "..efa.name.."\n")
end

local function processModule()
  
  local filename, filepath = getFileName(name..".cif")
  print("/***")
  print(" * CIF model generated from Supremica by Supremica2CIF.lua script")
  print(" * Generated: "..os.date("%Y-%m-%d, %H:%M"))
  print(" * Supremica model: "..name)
  print(" * Saved to: "..filename)
  print("***/")
  outputEvents()
  outputEnums()
  
  for i = 1, efalist:size() do
    local efa = efalist:get(i-1)
    processEFA(efa)
    Storage[#Storage+1] = CurrentEFA
  end
  
  -- Here, some variables may not be owned by any EFA. This is not allowed in CIF
  -- So we go through all variables and assign orphans to an arbitrary EFA
  for var, body in pairs(Variables) do
    if not body.owner then
      -- loginfo(var.." has no owner, assign: "..CurrentEFA.name)
      ownThisVariable(var)
    end
  end
  
  -- Now all EFAs have been processed, so we know which variable is owned by which EFA
  -- Outputting EFA can now owner-prefix variables in guards and actions
  -- But we only need to handle the orphans, all other have already been prefixed
  for i = 1, #Storage do
    outputEFA(Storage[i])
  end
  
  local textpanel = textframe:getTextPanel()
  local textarea = textpanel:getTextArea()
  local text = textarea:getText()
  saveModel(name..".cif", filepath, text)
  
end

preProcessing()
processModule()