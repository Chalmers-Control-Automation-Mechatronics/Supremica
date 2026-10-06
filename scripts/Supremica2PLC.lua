-- Supremica2CIF.lua, Lua script to convert Supremica models to CIF
-- Meant to be run as a script inside Supremica (with LuaJ embedded)
local luaj = luajava -- just shorthand 
--local script, ide, log = ... -- grab the arguments passed from Java via LuaJ
local script, ide, log = "Supremica2CIF.lua", IDE_GLOBAL, LOG_GLOBAL -- grab the arguments passed from Java via LuaJ

-- Get convenience function to generate file name
local Config = luaj.bindClass("org.supremica.properties.Config")
--local getFileName = dofile(Config.FILE_SCRIPT_PATH:getValue():getPath().."/getFileName.lua")
local getFileName = dofile("scripts/getFileName.lua")

-- Some useful Java classes
local JOptionPane = luaj.bindClass("javax.swing.JOptionPane")

local Variables -- Holds variable name, range, init, mark (filled by preProcess)
local CurrentEFA -- EFA currently processed (set by processEFA, contains .name and .variables)
local CurrentEdge -- Collects data for currently processed edge (set by processEdge)
local Storage = {} -- holds the generated EFA for delayed output (see processModule)
local FileName -- The filename to save under (set by processModule)
local ConstantRanges = {}
local GlobalActionMap = {}
local SharedVariables = {}
local Manager_variables = {}

local function loginfo(str) -- helper to write to log
  if str then log:info(str, 0) else log:info("nil string", 0) end
end

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
  if not fileExists(filename) then
    return filename, false
  end
  
  -- Separate the base name and the extension so we can insert _j in between
  local base = filename:match("^(.*)%.cif$") or filename
  local j = 1
  local newFilename
  
  repeat
    newFilename = base .. "_" .. j .. ".cif"
    j = j + 1
  until not fileExists(newFilename)
  
  return newFilename, true
end

local function saveFile(filename, contents)
  local finalFilename, wasAltered = checkFileExists(filename)
  print("Saving to: "..finalFilename)
  local file = io.open(finalFilename, "w")
  file:write(contents)
  file:close()
  return finalFilename, wasAltered
end

local function saveModel(fname, fpath, contents)
	local filename = showFileChooser(fname:gsub("%.cif$", ".plcopen.xml"), fpath)
    if not filename then
		print("User cancelled")
		return nil, nil
	end
	
	if fileExists(filename) then
		local JOptionPane = luaj.bindClass("javax.swing.JOptionPane")
		local reply = JOptionPane:showConfirmDialog(ide, filename.."\nOverwrite?", "File exists", JOptionPane.YES_NO_OPTION)
        if reply ~= JOptionPane.YES_OPTION then
            print("User cancelled overwrite.")
            return nil, nil
        end
    end
	
	local baseCifPath = filename:gsub("%.plcopen%.xml$", ".cif")
	
	local tempCifPath, wasAltered = saveFile(baseCifPath, contents)
	
	return tempCifPath, filename
end

local function savePLC(path, filepath)
    if not path then
        return
    end

    -- 1. Setup Java classes and check OS
    local System = luaj.bindClass("java.lang.System")
    local File = luaj.bindClass("java.io.File")
    local ProcessBuilder = luaj.bindClass("java.lang.ProcessBuilder")
    local ArrayList = luaj.bindClass("java.util.ArrayList")
    local osName = System:getProperty("os.name"):lower()
    local isWindows = osName:find("win") ~= nil
    
    -- 2. Dynamically resolve the absolute bin directory
    local appRoot = System:getProperty("user.dir")
    local devDir = luaj.newInstance("java.io.File", appRoot .. "/scripts/resources/escet/bin")
    local prodDir = luaj.newInstance("java.io.File", appRoot .. "/resources/escet/bin")
    
    local binDir = devDir
    if not binDir:exists() then binDir = prodDir end
    
    if not binDir:exists() or not binDir:isDirectory() then
        print("Error: ESCET bin directory is missing at " .. binDir:getAbsolutePath())
        return
    end

    local absoluteBin = binDir:getAbsolutePath()
    
    -- 3. Extract the exact file and its parent folder from the confirmed save path
    local absoluteCifFile = luaj.newInstance("java.io.File", path)
    local cifFileName = absoluteCifFile:getName()
    local parentDir = absoluteCifFile:getParentFile() 
    
    -- 4. Build the robust command array
    local cmdList = luaj.newInstance("java.util.ArrayList")
    if isWindows then
        cmdList:add("cmd.exe")
        cmdList:add("/c")
        cmdList:add(absoluteBin .. "\\cifplcgen.cmd")
        cmdList:add(cifFileName)
    else
        cmdList:add("sh")
        cmdList:add(absoluteBin .. "/cifplcgen.sh")
        cmdList:add(cifFileName)
    end
    
    -- 5. Configure and run ProcessBuilder
    local pb = luaj.newInstance("java.lang.ProcessBuilder", cmdList)
    pb:directory(parentDir)
    
    local logFileObj = luaj.newInstance("java.io.File", parentDir, "escet_log.txt")
    pb:redirectErrorStream(true)
    pb:redirectOutput(logFileObj)
    
    print("Executing ESCET PLC generator natively...")
    local process = pb:start()
    local exitCode = process:waitFor()
    local success = (exitCode == 0)
    
    local absoluteLog = logFileObj:getAbsolutePath()
    
    -- 6. Read log file and clean up
    local logFile = io.open(absoluteLog, "r")
    if logFile then
        print("--- ESCET TERMINAL OUTPUT ---")
        print(logFile:read("*a"))
        print("-----------------------------")
        logFile:close()
        os.remove(absoluteLog)
    else
        print("Warning: Could not read the ESCET log file.")
    end
    
    if success == true or success == 0 then
        print("Success: PLC implementation code generated automatically!")
		local generatedPlcPath = path:gsub("%.cif$", ".plcopen.xml")
		--local plcFilePath
		--if wasAltered then
		--	plcFilePath = path:gsub("(_%d+)%.cif$", ".plcopen.xml")
		--else
		--	plcFilePath = path:gsub("%.cif$", ".plcopen.xml")
		--end
		local plcFile = io.open(generatedPlcPath, "r")
        
        if plcFile then
            local content = plcFile:read("*a")
            plcFile:close()
            
            print("Post-processing: Moving inputs to <inputVars>...")
            local extractedInputs = ""
            
            -- Loop through our Variables table to find flagged inputs
            for varName, varInfo in pairs(Variables) do
                if varInfo.is_input then
                    -- Lua pattern to match the <variable> block and preceding whitespace
                    local pattern = "(%s*<variable name=\"" .. varName .. "\">.-</variable>)"
                    local match = content:match(pattern)
                    
                    if match then
                        extractedInputs = extractedInputs .. match
                        -- Remove the block from its original location
                        content = content:gsub(pattern, "", 1)
                        print("  -> Moved " .. varName .. " to inputs.")
                    end
                end
            end
            
            -- If we extracted any inputs, inject them into the XML
            if extractedInputs ~= "" then
                if content:match("<inputVars>") then
                    -- Append to existing <inputVars>
                    content = content:gsub("(<inputVars>)", "%1" .. extractedInputs)
                else
                    -- Create <inputVars> and place it right above <localVars>
                    local inputBlock = "\n          <inputVars>" .. extractedInputs .. "\n          </inputVars>"
                    content = content:gsub("(<localVars>)", inputBlock .. "\n          %1")
                end
            end
            
            -- Overwrite the file with the modified content
            local outFile = io.open(filepath, "w")
            outFile:write(content)
            outFile:close()
            print("Post-processing complete!")
			
			if generatedPlcPath ~= filepath then
				os.remove(generatedPlcPath)
			end
        else
            print("Warning: Could not find generated PLC file at " .. generatedPlcPath)
        end
    else
        print("Warning: ESCET code generator failed.")
    end
    
    os.remove(path)
end

-- Lua 5.2 and earlier do not have math.tointeger
local function tointeger(val)
  local num = tonumber(val)
  if not num then return val end
  
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
local DocumentManager = luaj.bindClass("net.sourceforge.waters.model.marshaller.DocumentManager")
local ProductDESElementFactory = luaj.bindClass("net.sourceforge.waters.plain.des.ProductDESElementFactory")
local ModuleCompiler = luaj.bindClass("net.sourceforge.waters.model.compiler.ModuleCompiler")
local SupremicaBuilder = luaj.bindClass("org.supremica.automata.waters.SupremicaSynchronousProductBuilder")

local efaKind = {} -- lookup table for automata type conversion
efaKind[ComponentKind.PLANT] = "plant"
efaKind[ComponentKind.PROPERTY] = "requirement"
efaKind[ComponentKind.SPEC] = "requirement"
efaKind[ComponentKind.SUPERVISOR] = "supervisor"

--local TextFrame = luaj.bindClass("org.supremica.gui.texteditor.TextFrame")
local textframe = luaj.newInstance("org.supremica.gui.texteditor.TextFrame", "CIF Export")
local pw = textframe:getPrintWriter()
textframe:setVisible(false)

local function print(str) -- redefine print to write to the textframe
  pw:println(str)
end
local function loginfo(str) -- helper to write to log
  if str then log:info(str, 0) else log:info("nil string", 0) end
end

-- For processing primed guard expressions, we need dynamic nested loops
-- to evaluate all combinations of variable values 
-- This part contains a dynamic nested loop imiplementation


-- All build in functions in CIF. Automaton, locations, events, and variables are not allowed to be named like this
local CIFReserved = {
    ["abs"]=true, ["acos"]=true, ["acosh"]=true, ["alg"]=true, ["alphabet"]=true,
    ["and"]=true, ["any"]=true, ["asin"]=true, ["asinh"]=true, ["atan"]=true,
    ["atanh"]=true, ["attr"]=true, ["automaton"]=true, ["bernoulli"]=true,
    ["beta"]=true, ["binomial"]=true, ["break"]=true, ["case"]=true, ["cbrt"]=true,
    ["ceil"]=true, ["const"]=true, ["constant"]=true, ["cont"]=true, ["continue"]=true,
    ["controllable"]=true, ["cos"]=true, ["cosh"]=true, ["def"]=true, ["del"]=true,
    ["der"]=true, ["dict"]=true, ["disables"]=true, ["disc"]=true, ["dist"]=true,
    ["div"]=true, ["do"]=true, ["edge"]=true, ["elif"]=true, ["else"]=true,
    ["empty"]=true, ["end"]=true, ["enum"]=true, ["equation"]=true, ["erlang"]=true,
    ["event"]=true, ["exp"]=true, ["exponential"]=true, ["false"]=true, ["file"]=true,
    ["final"]=true, ["floor"]=true, ["fmt"]=true, ["for"]=true, ["func"]=true,
    ["gamma"]=true, ["geometric"]=true, ["goto"]=true, ["group"]=true, ["id"]=true,
    ["if"]=true, ["import"]=true, ["in"]=true, ["initial"]=true, ["input"]=true,
    ["int"]=true, ["invariant"]=true, ["list"]=true, ["ln"]=true, ["location"]=true,
    ["log"]=true, ["lognormal"]=true, ["macro"]=true, ["marked"]=true, ["max"]=true,
    ["min"]=true, ["mod"]=true, ["monitor"]=true, ["namespace"]=true, ["needs"]=true,
    ["normal"]=true, ["not"]=true, ["now"]=true, ["or"]=true, ["plant"]=true,
    ["poisson"]=true, ["pop"]=true, ["post"]=true, ["pow"]=true, ["pre"]=true,
    ["print"]=true, ["printfile"]=true, ["random"]=true, ["real"]=true,
    ["requirement"]=true, ["return"]=true, ["round"]=true, ["sample"]=true,
    ["scale"]=true, ["self"]=true, ["set"]=true, ["sign"]=true, ["sin"]=true,
    ["sinh"]=true, ["size"]=true, ["sqrt"]=true, ["state"]=true, ["string"]=true,
    ["sub"]=true, ["supervisor"]=true, ["svgcopy"]=true, ["svgfile"]=true,
    ["svgin"]=true, ["svgmove"]=true, ["svgout"]=true, ["switch"]=true, ["tan"]=true,
    ["tanh"]=true, ["tau"]=true, ["text"]=true, ["time"]=true, ["tobool"]=true,
    ["triangle"]=true, ["true"]=true, ["tuple"]=true, ["type"]=true,
    ["uncontrollable"]=true, ["uniform"]=true, ["urgent"]=true, ["value"]=true,
    ["void"]=true, ["weibull"]=true, ["when"]=true, ["while"]=true
}

-- In Supremica, identifiers can include colon (:) Events, EFA names, variable names, 
-- enum labels, all of those can include one or more colons. Forinstnace "var:X:Y:Z"
-- is a valid identifier in Supremica, as is "e::::1", and "Cat:0", "Room:4", etc
-- This is an unfortunate historical accident, and of course, CIF does not allow this
-- So, all such identifiers must be sanitized at input (or at least before output)
-- And we must guarantee unique sanitized ouput for unique un-sanitized input!
-- So we keep a double-directed map with both the sanitized and un-sanitized as keys
local Sanity, Insanity = {}, {} -- maps for sanitation
local NameChanges = {}

-- To desanitize means to make sure that some sane input does not clash
-- with some sanitized input, like "var:X" sanitizwes to "varX", and then
-- later an actual input is "varX". This input must be made unique.
local function desanitize(input, category)
  category = category or "general"
  local cacheKey = category .. "_" .. input
  assert(not input:find(":"), "Unsanitized input!")
  local desanitized = nil
  local i = 1
  repeat
    desanitized = input.."_"..i
	i = i + 1
  until not Insanity[desanitized]
  Sanity[cacheKey] = desanitized
  Insanity[desanitized] = input
  table.insert(NameChanges, "//" .. category .. ":" .. input .. "-->" .. desanitized)
  return desanitized
end

local function sanitize(input, category)
  category = category or "general"
  if category == "event" and input:find("%.{") then
    input = input:gsub("%.{.*", "")
  end
  input = input:gsub("%[", "_"):gsub("%]", "")
  local cacheKey = category .. "_" .. input
  -- If this has already been sanitized, just return that result
  if Sanity[cacheKey] then return Sanity[cacheKey] end
  -- Convert brackets into underscores (e.g. a[0] -> a_0), this happens when a Foreach block is used
 -- local cleanInput = input:gsub("%[", "_"):gsub("%]", "")
  local cleanInput = input
  if CIFReserved[cleanInput] then
    if category == "location" then cleanInput = "Loc_" .. cleanInput
    elseif category == "event" then cleanInput = "Ev_" .. cleanInput
    elseif category == "automaton" then cleanInput = "Aut_" .. cleanInput
    else cleanInput = "Id_" .. cleanInput end
  end
  if cleanInput:match("^%d") then
    if category == "location" then
      cleanInput = "Loc_" .. cleanInput
    elseif category == "event" then
      cleanInput = "Ev_" .. cleanInput
    elseif category == "automaton" then
      cleanInput = "Aut_" .. cleanInput
    else
      cleanInput = "Id_" .. cleanInput
    end
  end
  
  if category == "variable" then
    if cleanInput:match("^c_") or cleanInput:match("^u_") or cleanInput:match("^e_") then
        cleanInput = "var_" .. cleanInput
    end
  end
  
  -- Do we have (sane) input that some insane input already maps to?
  -- If so, we need to do something to make it unique
  if Insanity[cleanInput] then 
    -- assert(false, Insanity[input].." is already mapped to "..input)
    return desanitize(cleanInput, category)
  end
  
  -- Now we replace all colons by longer and longer strings of underscores
  -- starting with length 0, until we find something that is not in our map
  local sanitized = nil
  local i = 1 -- was 0
  -- Starting from 0 was a nice idea to get shorter strings, but
  -- it maps a single colon to the empty string, which does not work
  -- At least sequences of : are mapped to possibly shorter sequences of _
  -- With i = 1, colon-infested input sanitizes to something with underscore
  repeat
    sanitized = cleanInput:gsub(":+", string.rep("_", i))
    i = i + 1
  until not Insanity[sanitized]
  Insanity[sanitized] = input
  --Sanity[input] = sanitized
  Sanity[cacheKey] = sanitized
  if input ~= sanitized then
	table.insert(NameChanges, "//" .. category .. ":" .. input .. "-->" .. sanitized)
  end
  return sanitized
end

local function sanitizeMathExpression(expr)
    if not expr then return "" end
	expr = expr:gsub("\\max", "max"):gsub("\\min", "min"):gsub("%%", " mod ")
	local mathFuncs = {["min"]=true, ["max"]=true, ["abs"]=true, ["pow"]=true, ["sqrt"]=true, ["ceil"]=true, ["floor"]=true, ["round"]=true, ["sign"]=true, ["div"]=true, ["mod"]=true, ["true"]=true, ["false"]=true}
    -- Find every individual word in the equation
    for w in expr:gmatch("([_:%a][_:%w%[%]]*)") do
        -- If the word is a raw number (like "1" or "24"), skip it completely!
        if not tonumber(w) and not mathFuncs[w] then
            local replacement
            if Sanity["variable_" .. w] then 
                replacement = Sanity["variable_" .. w]
            else 
                replacement = sanitize(w, "variable") 
            end
            
            -- Escape brackets so Lua can replace them safely inside the math string
            local escaped_w = w:gsub("%[", "%%["):gsub("%]", "%%]")
            expr = expr:gsub(escaped_w, replacement)
        end
    end
    
    
    return expr
end
-- Note that CIF itself exploits this "feature" in Supremica when it generates
-- *.wmod from *.cif, see for instance, button_lamp.cif
local function getAutomataSafe(proj)
  -- Try the GUI Helpers first
  local success, list = pcall(function() return Helpers:getAutomatonList(proj) end)
  if success then return list end
  
  -- If it crashes (because it's a compiled Plain module), extract them manually!
  local plainList = luaj.newInstance("java.util.ArrayList")
  local comps = proj:getComponentList()
  for i = 1, comps:size() do
    local comp = comps:get(i-1)
    if tostring(comp:getClass()):find("SimpleComponent") then
      plainList:add(comp)
    end
  end
  return plainList
end

local function getVariablesSafe(proj)
  -- Try the GUI Helpers first
  local success, list = pcall(function() return Helpers:getVariableList(proj) end)
  if success then return list end
  
  -- If it crashes, extract manually!
  local plainList = luaj.newInstance("java.util.ArrayList")
  local comps = proj:getComponentList()
  for i = 1, comps:size() do
    local comp = comps:get(i-1)
    if tostring(comp:getClass()):find("VariableComponent") then
      plainList:add(comp)
    end
  end
  return plainList
end

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
--patterns.operator = "([%+%-%*/=]+)"
patterns.operator = "([%+%-%*/=%%]+)"
-- patterns.actiondetail = "([_:%a][_:%w]*)%s*([%+%-%*/=]+)%s*([_:%w]+)" -- "([_%a][_%w]*)%s*([%+%-%*/=]+)%s*([_%w]+)"
-- patterns.actionexpr = "([_:%a][_:%w]*%s*[%+%-%*/=]+%s*[_:%w]+)"-- "([_%a][_%w]*%s*[%+%-%*/=]+%s*[_%w]+)"
patterns.identifier = "([_%a][_%w]*)" -- this pattern does not catch colon, see sanitize()
--patterns.colonifier = "([_:%a][_:%w]*)" -- identifiers can include colon
patterns.colonifier = "([_:%a][_:%w%[%]]*)"
patterns.commasep = "%s*(.-)[,]"
patterns.operatorsep = "%s*"..patterns.colonifier.."%s*"..patterns.operator.."%s*(.*)"
patterns.primedexpr = "([_%a][_%w]*'%s*[%+%-%*=/]+%s*[_%w]+)"
patterns.guardexpr = "([_%a][_%w]*%s*[%+%-%*=/]+%s*[_%w]+)"
-- patterns.matchrange = "(%-?%d+)%.%.(%-?%d+)"
patterns.matchrange = "(%-?[%w_]+)%.%.(%-?[%w_]+)"
patterns.colonatend = ":$"
patterns.multiassign = "([_%a][_%w]*)%s*:="
-- https://www.lua.org/pil/20.2.html
-- https://iamreiyn.github.io/lua-pattern-tester/



local function preScanModel(project, efalist)
    local controllable, uncontrollable, arrayChildrenMap = {}, {}, {}
    local seenRawEvents = {}
    local varOwners = {} -- Tracks who owns variables for conflict detection

    -- 1. Grab Base Event Declarations
    local eventDeclList = project:getEventDeclList()
    for i = 1, eventDeclList:size() do
        local event = eventDeclList:get(i-1)
        local kind = event:getKind()
        if kind == EventKind.CONTROLLABLE then
            controllable[sanitize(event:getName(), "event")] = true
        elseif kind ~= EventKind.PROPOSITION then
            uncontrollable[sanitize(event:getName(), "event")] = true
        end
    end

    -- 2. The SINGLE PASS over all automata and edges
    for i = 1, efalist:size() do
        local efa = efalist:get(i-1)
        local efaName = sanitize(efa:getName(), "automaton")
        local edges = efa:getGraph():getEdges():iterator()

        while edges:hasNext() do
            local edge = edges:next()
            
            -- A) Process Events on this edge
            local evlist = edge:getLabelBlock():getEventIdentifierList():iterator()
            local currentEdgeEvents = {} -- Needed for GlobalActionMap
            
            while evlist:hasNext() do
                local rawName = evlist:next():toString()
                local evName = sanitize(rawName, "event")
                table.insert(currentEdgeEvents, evName)

                if not seenRawEvents[rawName] then
                    seenRawEvents[rawName] = true
                    local baseName_raw = rawName:match("^([^%[]+)%[")
                    if baseName_raw then
                        local baseName = sanitize(baseName_raw, "event")
                        if controllable[baseName] then
                            controllable[evName] = true
                            arrayChildrenMap[baseName] = arrayChildrenMap[baseName] or {}
                            table.insert(arrayChildrenMap[baseName], evName)
                        elseif uncontrollable[baseName] then
                            uncontrollable[evName] = true
                            arrayChildrenMap[baseName] = arrayChildrenMap[baseName] or {}
                            table.insert(arrayChildrenMap[baseName], evName)
                        end
                    elseif not controllable[evName] and not uncontrollable[evName] then
                        local baseName_raw = rawName:match("^([^%[]+)")
                        if baseName_raw then
                            local baseName = sanitize(baseName_raw, "event")
                            if controllable[baseName] then controllable[evName] = true
                            elseif uncontrollable[baseName] then uncontrollable[evName] = true end
                        end
                    end
                end
            end

            -- B) Process Guards/Actions on this edge (Shared Vars & Global Map)
            local gablock = edge:getGuardActionBlock()
            if gablock then
                local gatxt = gablock:toString():gsub("\n", ",")
                local _, aexpr = gatxt:match("%[,(.*)%],{,(.*)},")
                
                if aexpr then
                    aexpr = aexpr:match("[{%s,]*(.+),}")
                    if aexpr and aexpr ~= "" then
                        local str = aexpr .. ","
                        for cap in str:gmatch(patterns.commasep) do
                            local op, op_start, op_end
                            for _, vop in ipairs({"+=", "-=", "*=", "/=", "%=", "==", "="}) do
                                op_start, op_end = cap:find(vop, 1, true)
                                if op_start then op = vop; break end
                            end

                            if op then
                                local lhs_raw = cap:sub(1, op_start - 1):match("^%s*(.-)%s*$")
                                if lhs_raw then
                                    local lhs = sanitize(lhs_raw, "variable")

                                    -- CHECK 1: Shared Variables
                                    if not varOwners[lhs] then
                                        varOwners[lhs] = efaName
                                    elseif varOwners[lhs] ~= efaName then
                                        SharedVariables[lhs] = true
                                        if Variables and Variables[lhs] then
                                            Variables[lhs].owner = "Manager_" .. lhs
                                        end
                                    end

                                    -- CHECK 2: Global Action Map (Only if primes exist)
                                    
                                    local rhs = cap:sub(op_end + 1):match("^%s*(.-)%s*$")
                                    rhs = sanitizeMathExpression(rhs)

                                    if op == "+=" then rhs = lhs .. " + " .. rhs
                                    elseif op == "-=" then rhs = lhs .. " - " .. rhs
                                    elseif op == "*=" then rhs = lhs .. " * " .. rhs
                                    end

                                    -- Get source location
                                    local rawText = edge:getSource():toString():gsub("\n", "")
                                    local srcLoc
                                    for capture in rawText:gmatch("(%w+)") do
                                        if capture ~= "initial" and capture ~= "forbidden" and capture ~= "accepting" then
                                            srcLoc = sanitize(capture, "location")
                                            break
                                        end
                                    end

                                    -- Store in global map
                                    for _, evName in ipairs(currentEdgeEvents) do
                                        GlobalActionMap[evName] = GlobalActionMap[evName] or {}
                                        GlobalActionMap[evName][efaName] = GlobalActionMap[evName][efaName] or {}
                                        GlobalActionMap[evName][efaName][lhs] = GlobalActionMap[evName][efaName][lhs] or {}
                                        table.insert(GlobalActionMap[evName][efaName][lhs], {location = srcLoc, math = rhs})
                                    end
                                   
                                end
                            end
                        end
                    end
                end
            end
        end
    end

    -- Cleanup Array Parents
    for base, _ in pairs(arrayChildrenMap) do
        controllable[base] = nil
        uncontrollable[base] = nil
    end

    return controllable, uncontrollable, arrayChildrenMap
end



local function getBlockedEvents(efalist)
    local blockedEventsCIF = {}
--	efalist = Helpers:getAutomatonList(project)
--	efalist = getAutomataSafe(project)
    for i = 1, efalist:size() do
        local efa = efalist:get(i-1)
        
        --local success, blockedEvents = pcall(function() 
        --    return efa:getGraph():getBlockedEvents() 
        --end)
        local blockedEvents = efa:getGraph():getBlockedEvents()
        if blockedEvents ~= nil then
            local eventList = blockedEvents:getEventIdentifierList()
            
            if eventList ~= nil then
                for j = 0, eventList:size() - 1 do
                    local eventObj = eventList:get(j)           
					local rawName = eventObj:toString()        
                    --blockedEventsCIF[sanitize(eventObj:getName(), "event")] = true
					if rawName ~= ":accepting" and rawName ~= ":forbidden" and rawName ~= ":initial" then
					  blockedEventsCIF[sanitize(eventObj:toString(), "event")] = true
					end
                end
            end
        end
    end

    return blockedEventsCIF
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
local name = sanitize(container:getName(), "module")  -- should be sanitized? Windows does not allo : in filenames, *ix might od!
local raw_project = container:getEditorPanel():getModuleSubject()

local function containsForeach(componentList)
  if not componentList then return false end
  
  for i = 1, componentList:size() do
    local comp = componentList:get(i-1)
    
    -- 1. Explicitly target the ForeachSubject class
    local success, className = pcall(function() return tostring(comp:getClass()) end)
    if success and className:find("ForeachSubject") then
      return true
    end
    
    -- 2. If not a Foreach, check if it contains child components and recurse
    local hasChildren, childList = pcall(function() return comp:getComponentList() end)
    if hasChildren and childList and childList:size() > 0 then
      if containsForeach(childList) then 
        return true 
      end
    end
  end
  return false
end



local function instantiateProjectNatively(proj_module)
  -- 1. Create a blank DocumentManager
  local doc_manager = luaj.newInstance("net.sourceforge.waters.model.marshaller.DocumentManager")
  
  -- 2. Try to instantiate the ModuleInstanceCompiler
  local compiler
  local success, res = pcall(luaj.newInstance, "net.sourceforge.waters.model.compiler.instance.ModuleInstanceCompiler", doc_manager, proj_module)
  
  if success and res then
     compiler = res
  else
     -- Fallback: If it requires the ModuleElementFactory explicitly
     local factory = luaj.newInstance("net.sourceforge.waters.plain.module.ModuleElementFactory")
     compiler = luaj.newInstance("net.sourceforge.waters.model.compiler.instance.ModuleInstanceCompiler", doc_manager, factory, proj_module)
  end
  
  -- 3. Run the compiler to unroll the Foreach blocks and Instances
  local compile_success, unrolled_module = pcall(function() return compiler:compile() end)
  
  if compile_success and unrolled_module then
      print("// Foreach loops and Instances successfully unrolled by Java API.")
      return unrolled_module
  else
      print("// WARNING: Native unrolling failed. Proceeding with raw project.")
      return proj_module
  end
end

-- Detect if flattening is needed
local needsInstantiation = containsForeach(raw_project:getComponentList())
local comps = raw_project:getComponentList() 
for i = 1, comps:size() do
  if tostring(comps:get(i-1):getClass()):find("ForeachComponent") then
    needsInstantiation = true
    break
  end
end


-- Ensure 'project' is the final, flat model before proceeding
local project = raw_project
if needsInstantiation then
  project = instantiateProjectNatively(raw_project)
end 



efalist = getAutomataSafe(project)
varlist = getVariablesSafe(project)


--local components = project:getComponentList() 

-- Prioritize variable names to stay the same, then location names, and lastly event names
local vIter = varlist:iterator()
while vIter:hasNext() do sanitize(vIter:next():getName(), "variable") end

-- 2. Reserve Location names second
for i = 1, efalist:size() do
  local efa = efalist:get(i-1)
  local rawAutName = efa:getName()
  if type(rawAutName) == "userdata" then rawAutName = rawAutName:getName() end
  sanitize(rawAutName, "automaton")
  --sanitize(efa:getName(), "automaton")
--[[  if needsFlattening then
	local states = efa:getStates():iterator()
	while states:hasNext() do
      local state = states:next()
      local rawStateName = state:getName()
      if type(rawStateName) == "userdata" then rawStateName = rawStateName:getName() end
      sanitize(rawStateName, "location")
    end --]]
  --else
    local nodes = efalist:get(i-1):getGraph():getNodes():iterator()
    while nodes:hasNext() do
      local rawText = nodes:next():toString():gsub("\n", "")
      for capture in rawText:gmatch("(%w+)") do
        if capture ~= "initial" and capture ~= "forbidden" and capture ~= "accepting" then
          sanitize(capture, "location")
          break
        end
      end
    end
  --end
end
local cevents, uevents, arrayEventMap = preScanModel(project, efalist)
local blockedEvents = getBlockedEvents(efalist)




--[[
  Supremica allows to compare two enums of different types, basically checking if the
  identifiers are equal when compared as strings. This is not allowed by CIF, only enums 
  of the same type can be compared. To get around this, we put all Supremica enums in
  one big Enums type, being careful to remove duplicates, while still keeping the enum
  ranges (expanded) for each Supremica enum variable. This set is built by processVariables()
--]]


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

-- When converting "!" to "not", need to be careful not to convert "!=" to "not="
-- First convert "!=" to a safe string, then convert "!", then convert the safe string back
local function handleNot(str)
  local safestr = "#="
  local convstr = str:gsub("!=", safestr)
  convstr = convstr:gsub("!", " not ")
  convstr= convstr:gsub(safestr, "!=")
  convstr = convstr:gsub("%%", " mod ")
  return convstr
end


-- SMART CONVERSION: Global Event-Action Map



-- Probably have to sanitize identifiers here, and replace inline
local function convertGuard(gstr)
  gstr = gstr:gsub("\\max", "max"):gsub("\\min", "min")
  local mathFuncs = {["min"]=true, ["max"]=true, ["abs"]=true, ["pow"]=true, ["sqrt"]=true, ["ceil"]=true, ["floor"]=true, ["round"]=true, ["sign"]=true, ["div"]=true, ["mod"]=true, ["true"]=true, ["false"]=true}
  for w in gstr:gmatch(patterns.colonifier) do
	local replacement = w
	    
    -- Smart lookup: Check what this word actually is in our memory cache!
	if mathFuncs[w] then
		replacement = w
    elseif Sanity["variable_" .. w] then
        replacement = Sanity["variable_" .. w]
    elseif Sanity["location_" .. w] then
        replacement = Sanity["location_" .. w]
    elseif Sanity["automaton_" .. w] then
        replacement = Sanity["automaton_" .. w]
	else
        -- If it's completely unknown, default to variable logic
        replacement = sanitize(w, "variable") 
    end
    
    -- Swap the word in the string
	local escaped_w = w:gsub("%[", "%%["):gsub("%]", "%%]")
	gstr = gstr:gsub(escaped_w, replacement)
    --gstr = gstr:gsub(w, replacement)
    --gstr = gstr:gsub(w, sanitize(w, "variable"))
--    loginfo(w.." -> "..gstr)
  end
  
  local convstr = gstr:gsub("==", "="):gsub("&", " and "):gsub("|", " or ") -- gsub("![^=]", "not ")
  convstr = handleNot(convstr)
  return convstr
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

local function buildLocalActionDictionary(aexpr)
  local actDict = {}
  if not aexpr or aexpr == "" then return actDict end
  
  local str = aexpr .. ","
  for cap in str:gmatch(patterns.commasep) do
	local op, op_start, op_end
	for _, vop in ipairs ({"+=", "-=", "*=", "/=", "%=", "==", "="}) do
		op_start, op_end = cap:find(vop, 1, true)
		if op_start then op = vop; break end
	end
	if op then
	  local lhs = cap:sub(1, op_start - 1):match("^%s*(.-)%s*$")
	  local rhs = cap:sub(op_end + 1):match("^%s*(.-)%s*$")
      lhs = sanitize(lhs, "variable")
	  rhs = sanitizeMathExpression(rhs)
      if op == "+=" then rhs = lhs .. " + " .. rhs
      elseif op == "-=" then rhs = lhs .. " - " .. rhs
      elseif op == "*=" then rhs = lhs .. " * " .. rhs
      end
      actDict[lhs] = rhs
    end
  end
  return actDict
end
-----------------------------------------------------------------
-- Preprocessing - Collect stuff necessary to be able to process
-----------------------------------------------------------------
local function preProcessMarkedValues(marklist)
  local markings = {}
  local mit = marklist:iterator()
  while mit:hasNext() do
    local mstr = mit:next():getPredicate():toString()  -- is sanitized by convertGuard
    local str = convertGuard(mstr)
    table.insert(markings, str)
  end
  return markings
end

local function preProcessInitPredicate(str)  -- is sanitized by convertGuard
  return convertGuard(str)
end

-- Global constants, do NOT assign!
local IS_INTEGER = " is integer " -- types of variables
local IS_BINARY = " is binary "
local IS_ENUM = " is enum "
local IS_BOOL = " is bool "

local IS_MARKED = "\tmarked;" -- for locations
local IS_INITIAL = "\tinitial;"
local IN_ANY = " in any"

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
  if ConstantRanges[range] then
	range = ConstantRanges[range]
  end
  local initpred = preProcessInitPredicate(var:getInitialStatePredicate():toString())-- sanitized by convertGiuard
  local markings = preProcessMarkedValues(var:getVariableMarkings())-- sanitized by convertGiuard
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
  
  local typeStr = var:getType():toString()
  if ConstantRanges[typeStr] then
	typeStr = ConstantRanges[typeStr]
  end
  typeStr = typeStr:gsub("%[", ""):gsub("%]", ""):gsub("{", ""):gsub("}", "")
  for ident in typeStr:gmatch("([_:%a][_:%w]*)") do  -- should be sanitized, and patterns.colonifier used
	local saneident
	if ident == "true" or ident == "false" then
	  saneident = ident -- Keep them exactly as they are for CIF logic
	else
      saneident = sanitize(ident, "variable")
	  Enums[saneident] = true
	end
    range[#range + 1] = saneident
    
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
  
  local initpred = preProcessInitPredicate(var:getInitialStatePredicate():toString()) -- sanitized by convertGuard
  local markings = preProcessMarkedValues(var:getVariableMarkings())  -- sanitized by convertGuard
  local markstr = ""
  if #markings > 0 then
    markstr = table.concat(markings, ", ")
  end  
  return kind, range, initpred, markstr  
  
end

local function preProcessConstants()
  local success, constList = pcall(function() return raw_project:getConstantAliasList() end)
  if success and constList then
    for i = 1, constList:size() do
      local comp = constList:get(i-1)
      
      local rawName = comp:getName()
      if type(rawName) == "userdata" then pcall(function() rawName = rawName:getName() end) end
      local nameStr = tostring(rawName)
      
      local valSuccess, cVal = pcall(function() return comp:getConstantAliasExpression():toString() end)
      if not valSuccess then
         valSuccess, cVal = pcall(function() return comp:getExpression():toString() end)
      end
      
      -- If it has a range, store it in the memory map
      if valSuccess and cVal  then
		local cStr = cVal .. ""
		if cStr:find("%.%.") or cStr:find("%[") then
         ConstantRanges[nameStr] = cStr
	    elseif cStr:find("%[") then
			for word in cStr:gmatch("([a-zA-Z_][%w_]*)") do
				if not tonumber(word) then
					Enums[sanitize(word, "variable")] = true
				end
			end
		end
      end
    end
  end
end
 
local function getVariableInfo(var)
  local rawTypeStr = var:getType():toString()
  
  local typeStr = rawTypeStr
  if ConstantRanges[rawTypeStr] then
	typeStr = ConstantRanges[rawTypeStr]
  end
--[[  if typeStr:lower() == "boolean" or typeStr:lower() == "bool" then
    local initpred = preProcessInitPredicate(var:getInitialStatePredicate():toString())
    local markings = preProcessMarkedValues(var:getVariableMarkings())
    local markstr = ""
    if #markings > 0 then
        markstr = table.concat(markings, ", ")
    end
    return IS_BINARY, "0..1", initpred, markstr
  end --]]
  if typeStr:find("%.%.") then
	local bottom, topper = typeStr:match(patterns.matchrange)
	if bottom == "0" and topper == "1" then
		return getBinaryInfo(var)
	else
		return getIntegerInfo(var)
	end
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
    local name = sanitize(var:getName(), "variable")  -- should be sanitized
    local kind, range, init, mark = getVariableInfo(var)
	local is_input = false
	if var:isInput() and kind == IS_BOOL then
		is_input = true
	end
    variables[name] = {kind = kind, range = range, init = init, mark = mark, owner = nil, is_input = is_input}
  end
  
  return variables
end



local function preProcessing()
  preProcessConstants()
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
  local initial = false
  if srctxt:match("^%s*initial%s+") then
    initial = true
    srctxt = srctxt:gsub("^%s*initial%s+", "")
  end

    -- 2. Check for other standard properties
  local acc = srctxt:find(":accepting") ~= nil or srctxt:find("{.-accepting.-}") ~= nil
  local xxx = srctxt:find(":forbidden") ~= nil or srctxt:find("{.-forbidden.-}") ~= nil
    
    -- 3. Extract the clean location name (everything before the '{' bracket)
  local label = srctxt:match("^(.-)%s*{")
    
    -- If there is no bracket, the whole remaining string is the label
  if not label then
    label = srctxt:match("^%s*(.-)%s*$")
  end
  return label, initial, acc, xxx
end

-- Pure syntactic replacement of operators is not enough, as guards and actions include
-- variables, and in CIF these need to be prefixed by their owner names
-- Calling this function during processing will not fix all prefixing
local function prefixOwner(str)
  local orphans = {}
  local processed = {}
  
  local padded = " " .. str .. " "
  -- For identifiers that are variable names, if possible prefix with owner
  for ident in str:gmatch(patterns.identifier) do -- using patterns.identifier, since all should be sanitized
    local var = Variables[ident]
    if var and not processed[ident] then -- this is a variable
	  processed[ident] = true
	  if var.is_input then
		
      elseif var.owner then -- someone already owns this variable, is it us?
        if CurrentEFA.name ~= var.owner then -- owned by someone but not us
          -- Prefix with the owner
		  padded = padded:gsub("([^%w_%.])(" .. ident .. ")([^%w_])", "%1" .. var.owner .. ".%2%3")
        end
      else
        -- This variable is not yet owned by anyone
        -- Need to remember this to do the prefixing later
        orphans[ident] = true -- need a map for quick lookup and avoiding dupicates
      end
    end
  end  
  str = padded:sub(2,-2)
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
  if type(value) ~= "number" or type(bottom) ~= "number" or type(topper) ~= "number" then return false end
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
  
  -- Inputs need special treatment, as they cannot have initial values
  -- CIF itself does not forbid this, but the PLC code generator chokes
  local function makeVarDef(var)
    local out = {}
    
    if Inputs and Inputs[var] then
      table.insert(out, "\tinput "..Inputs[var].." "..var)
      return out
    end
	
	local initValue = nil
	if Variables[var].init and Variables[var].init ~= "" then
	  initValue = Variables[var].init:match("=%s*(.+)")
	end
	
	local declaration
	if Variables[var].kind == IS_ENUM then
	  declaration = "\tdisc Enums "..var
	elseif Variables[var].kind == IS_BOOL then
	  declaration = "\tdisc bool "..var
	elseif Variables[var].kind == IS_INTEGER or Variables[var].kind == IS_BINARY then
	  declaration = "\tdisc int["..Variables[var].range.."] "..var
	else
	  assert(false, "Unknown variable type: "..Variables[var].kind.." (variable: "..var..")")
	end
	  
	-- If an initial value exists, assign it strictly for PLC compatibility.
	-- If it does not exist, append 'in any' for pure CIF theoretical modeling.
	if initValue then
	  declaration = declaration .. " = " .. initValue
	else
	  declaration = declaration .. " in any"
	end
    
    --table.insert(out, "\tinitial "..Variables[var].init)
    --if Variables[var].mark and Variables[var].mark ~= "" then
    --  table.insert(out, "\tmarked "..Variables[var].mark)
    --end
	table.insert(out, declaration)
	if Variables[var].mark and Variables[var].mark ~= "" then
	  table.insert(out, "\tmarked "..Variables[var].mark)
	end
    return out
  end
  
  -- The given variable is assigned by this EFA, so it owns it
  -- CIF does not allow multiple EFA owning the same variable
  local function ownThisVariable(var)
	if not Variables[var] then return end
	if Variables[var].is_input then return end
    if SharedVariables[var] then
        Variables[var].owner = "Manager_" .. var
        if not Variables[var].decl then
            local out = makeVarDef(var)
            Variables[var].decl = table.concat(out, ";\n")..";\n"
        end
        return
    end

    if not Variables[var].owner then
      Variables[var].owner = CurrentEFA.name 
      local out = makeVarDef(var)
      Variables[var].decl = table.concat(out, ";\n")..";\n"
      table.insert(CurrentEFA.variables, Variables[var].decl)
      return
    end
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
  str = str:gsub("%(([^,]+),([^%)]+)%)", "(%1_COMMA_%2)")
  str = str..","
  for cap in str:gmatch(patterns.commasep) do -- (patterns.actionexpr) do
	cap = cap:gsub("_COMMA_", ",")
    
    --local lhs, op, rhs = cap:match(patterns.operatorsep) -- (patterns.actiondetail)
	local op, op_start, op_end
	for _, vop in ipairs ({"+=", "-=", "*=", "/=", "%=", "==", "="}) do
		op_start, op_end = cap:find(vop, 1, true)
		if op_start then op = vop; break end
	end
	if op then
		local lhs = cap:sub(1, op_start - 1):match("^%s*(.-)%s*$")
		local rhs = cap:sub(op_end + 1):match("^%s*(.-)%s*$")
		
	    lhs = sanitize(lhs, "variable")
		rhs = sanitizeMathExpression(rhs)

		
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
  end
  assert(orphans, "3. Oprhans nil!")
  return orphans
end

local function manageGuard(gstr)
  
  local newstr = convertGuard(gstr) -- sanitizes its input
  local prefxd, orphans = prefixOwner(newstr)
  --[[
    debugOrphans(CurrentEFA.name, orphans)
  --]]
  assert(orphans, "4. Oprhans nil!")
  return prefxd, orphans

end

-- Primed guards are unknown to CIF, and so must be turned into actions
-- This is not trivial...

patterns.primedident = "([_:%a][_:%w]*)'" -- matches supremica identifier, can include colon
-- Supremica guarantees that there is no space between end of variable
-- name and the prime (if manual input had it)

local function processGuard(str, gastore)
  -- str is a logical operator separated sequence of predicates
  -- Replacements can be done inline, see convertGuard()
  -- A complication is primed guards, that CIF do not recognize
  
  if not str or str == "" then return {} end
  
  local prefixed, orphans = manageGuard(str)
--  if prefixed:find("'") then -- handle primed variables in the guard
--    local gaset = generateGuardActionSet(prefixed)
--    table.insert(gastore.guards, gaset.guards)
 --   table.insert(gastore.actions, gaset.actions)
--  else -- simply convert syntactically and return    
  table.insert(gastore.guards, "("..prefixed..")")
--  end
  assert(orphans, "5. Orphans nil!")
  return orphans

  --[[ Text below is from before not even trying to handle primed guards 
  -- We have a guard with primed variable, for now assume it is varX' < varY
  -- foreach value v in DomX
  -- add transition with guard: v < varY, and action varX := v
  
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
  ]]--
end

-- SMART CONVERSION: Substitute based on Global Event Map
local function smartProcessPrimedGuard(gexpr, cevents, uevents, gastore, localDict)
  local newGuard = gexpr
  
  local allEvents = {}
  for _, ev in ipairs(cevents) do table.insert(allEvents, ev) end
  for _, ev in ipairs(uevents) do table.insert(allEvents, ev) end
  
  for baseVar in gexpr:gmatch("([_%a][_%w]*)'") do
    local safeBase = sanitize(baseVar, "variable")
    local primedVar = baseVar .. "'" 
	
	local op, rhs = newGuard:match(primedVar .. "%s*([=!<>]+)%s*([%-%w_]+)")
	local escapedRhs = rhs and rhs:gsub("%-", "%%-") or nil
	
	local cifOp = op
	if op == "==" then cifOp = "=" end
    
    local substitutionMath = nil
    local substitutionParts = {}
    
    -- PRIORITY 1: Check the local edge's action first
    if localDict and localDict[safeBase] then
	  if op and rhs then
		table.insert(substitutionParts, "(" .. localDict[safeBase] .. " " .. cifOp .. " " .. rhs .. ")")
	  else
        table.insert(substitutionParts, "(" .. localDict[safeBase] .. ")")
	  end
    else
      -- PRIORITY 2: Check global map for external automata
      for _, evName in ipairs(allEvents) do
        if GlobalActionMap[evName] then
          for efaName, varMap in pairs(GlobalActionMap[evName]) do
            if efaName ~= CurrentEFA.name and varMap[safeBase] then
              local actionList = varMap[safeBase]
              
              -- YOUR BRILLIANT LOGIC: Only one action = no location check needed!
              if #actionList == 1 then
				if op and rhs then
				  table.insert(substitutionParts, "(" .. actionList[1].math .. " " .. cifOp .. " " .. rhs .. ")")
				else
                  table.insert(substitutionParts, "(" .. actionList[1].math .. ")")
				end
              else
                -- Multiple actions = check the location
                for _, act in ipairs(actionList) do
				  if op and rhs then
					table.insert(substitutionParts, "(" .. efaName .. "." .. act.location .. " and (" .. act.math .. " " .. cifOp .. " " .. rhs .. "))")
				  else
                    table.insert(substitutionParts, "(" .. efaName .. "." .. act.location .. " and (" .. act.math .. "))")
				  end
                end
              end
            end
          end
        end
		if #substitutionParts > 0 then break end
      end
    end
    
    if #substitutionParts > 0 then
      -- Join multiple possibilities with OR
      substitutionMath = table.concat(substitutionParts, " or ")
	  if op and rhs then
		local escapePattern = primedVar .. "%s*" .. op .. "%s*" .. escapedRhs
		newGuard = newGuard:gsub(escapePattern, "(" .. substitutionMath .. ")")
	  else
      	newGuard = newGuard:gsub(primedVar, "(" .. substitutionMath .. ")")
	  end
    else
      -- PRIORITY 3: Orphan handling
      local eqPattern = primedVar .. "%s*==%s*([%-%w_]+)"
      local eqMatch = newGuard:match(eqPattern)
      
      if eqMatch then
        table.insert(gastore.actions, safeBase .. " := " .. eqMatch)
		ownThisVariable(safeBase)
        newGuard = newGuard:gsub(primedVar .. "%s*==%s*[%-%w_]+'?", "true")
      else
        newGuard = newGuard:gsub(primedVar, baseVar)
      end
    end
  end
  newGuard = newGuard:gsub("'", "")
  return newGuard
end

local function processGuardAction(gablock, cevs, uevs)
  if not gablock then return nil, nil, {} end 
  
  local function resolveNaked(matchVar, isNot)
      if matchVar == "true" or matchVar == "false" then
          return isNot and ("not " .. matchVar) or matchVar
      end
      
  	local baseVar = matchVar:gsub("%[.*%]", "")
  	baseVar = baseVar:match("([^%.]+)$") or baseVar
      local isRealBool = false
      
      if Variables and Variables[baseVar] and Variables[baseVar].decl then
          if Variables[baseVar].decl:find("bool") then
              isRealBool = true
          end
      end
      
      if isRealBool then
          return isNot and ("not " .. matchVar) or matchVar
      else
          return isNot and (matchVar .. " = 0") or (matchVar .. " = 1")
      end
  end
  
  local gastore = {}
  gastore.guards, gastore.actions = {}, {}
  
  local function stripCurly(str)
    return str:match("[{%s,]*(.+),}")
  end

  local gatxt = gablock:toString():gsub("\n", ",")
  local gexpr, aexpr = gatxt:match("%[,(.*)%],{,(.*)},")
  aexpr = stripCurly(aexpr)
  gexpr = stripCurly(gexpr)
  
  if aexpr then
    local cleanActions = {}
    for action in aexpr:gmatch("([^,]+)") do
        -- STRICT REGEX: Only match standard assignments like 'x = y'. 
        -- This safely ignores compound operators like '-=', '+='!
        local lhs, rhs = action:match("^%s*([a-zA-Z_][%w_%.]*)%s*=%s*(.+)%s*$")
        
        if lhs and rhs then
            -- 1. Handle logical NOT (!var or not var) by converting to integer subtraction
            local function resolveNot(var)
                local baseVar = var:match("([^%.]+)$") or var
                local isRealBool = false
                
                if Variables and Variables[baseVar] and Variables[baseVar].decl then
                    if Variables[baseVar].decl:find("bool") then
                        isRealBool = true
                    end
                end
                
                if isRealBool then
                    return "not " .. var
                else
                    return "1 - " .. var
                end
            end
            
            rhs = rhs:gsub("!%s*([a-zA-Z_][%w_%.]*)", resolveNot)
            rhs = rhs:gsub("not%s+([a-zA-Z_][%w_%.]*)", resolveNot)
            
            -- 2. Handle bitwise OR/AND
            if rhs:find("|") or rhs:find("&") then
                rhs = rhs:gsub("|", "+")
                rhs = rhs:gsub("&", "*")
                rhs = "min(1, " .. rhs .. ")"
            end
            
            table.insert(cleanActions, lhs .. " = " .. rhs)
        else
            -- If it's something like 'sticks-=2', it lands here completely untouched!
            table.insert(cleanActions, action)
        end
    end
    aexpr = table.concat(cleanActions, ", ")
  end
  
  if gexpr then
    local cleanGuards = {}
    for clause in gexpr:gmatch("([^,]+)") do
        if clause:match("%S") then -- Ignore empty chunks
			local trimmed = clause:match("^%s*(.-)%s*$")
            
            -- FIX: Translate Supremica's literal 0 and 1 into CIF booleans
            if trimmed == "0" then
                trimmed = "false"
            elseif trimmed == "1" then
                trimmed = "true"
            end
            table.insert(cleanGuards, trimmed)
        end
    end
	-- 2. NEW MODULAR STEP: Format the booleans inside the clean table
	if #cleanGuards > 0 then
	    -- Dynamic dictionary lookup for true CIF type

	
	    -- Format each clause individually using Context-Aware scanning
	    for i, clause in ipairs(cleanGuards) do
	        
	        -- A) Handle explicit negations first: 'not var' or '!var'
	        clause = clause:gsub("not%s+([a-zA-Z_][%w_%.%[%]]*)", function(v)
	            return resolveNaked(v, true)
	        end)
	        clause = clause:gsub("!%s*([a-zA-Z_][%w_%.%[%]]*)", function(v)
	            return resolveNaked(v, true)
	        end)
	        
	        -- B) Handle positive naked variables embedded inside larger sentences
	        local offset = 1
	        while true do
	            -- Find the next word that looks like a variable
	            local s, e, var = clause:find("([a-zA-Z_][%w_%.%[%]]*)", offset)
	            if not s then break end
	            
	            local keywords = {
	                ["and"]=true, ["or"]=true, ["not"]=true, 
	                ["true"]=true, ["false"]=true, ["min"]=true, 
	                ["max"]=true, ["div"]=true, ["mod"]=true
	            }
	            
	            if not keywords[var] then
	                -- Look at the first non-space character before and after the variable
	                local pre = clause:sub(1, s - 1)
	                local post = clause:sub(e + 1)
	                local pre_char = pre:match("([^%s])%s*$")
	                local post_char = post:match("^%s*([^%s])")
	                
					local comp_chars = {
	                  ["="]=true, ["<"]=true, [">"]=true, ["!"]=true,
	                  ["+"]=true, ["-"]=true, ["*"]=true, ["/"]=true, ["%"]=true, ["'"]=true
	                }
	                
	                -- If it does NOT touch a comparison character, it is naked!
	                if not ((pre_char and comp_chars[pre_char]) or (post_char and comp_chars[post_char])) then
	                    local replacement = resolveNaked(var, false)
	                    -- Inject the formatted variable back into the sentence
	                    clause = clause:sub(1, s - 1) .. replacement .. clause:sub(e + 1)
	                    -- Shift the offset so we don't scan the inserted '= 1' again
	                    e = s - 1 + #replacement 
	                end
	            end
	            offset = e + 1
	        end
	        
	        cleanGuards[i] = clause
	    end
	end
    gexpr = table.concat(cleanGuards, " and ")
  end
  
  if gexpr then

    -- gsub calls the resolveNaked function for every match it finds!
    gexpr = gexpr:gsub("not%s+([a-zA-Z_][%w_%.%[%]]*)", function(v) return resolveNaked(v, true) end)
    gexpr = gexpr:gsub("!%s*([a-zA-Z_][%w_%.%[%]]*)", function(v) return resolveNaked(v, true) end)
    gexpr = gexpr:gsub("%(%s*([a-zA-Z_][%w_%.%[%]]+)%s*%)", function(v)
        return "(" .. resolveNaked(v, false) .. ")"
    end)
  end

  -- --- NEW SMART LOGIC INTERCEPTION ---
  if gexpr and gexpr:find("'") then
	local localDict = buildLocalActionDictionary(aexpr)
	gexpr = smartProcessPrimedGuard(gexpr, cevs, uevs, gastore, localDict)
  end
  -- ------------------------------------

  local orph1 = processGuard(gexpr, gastore) 
  assert(orph1, "657: orph1 is nil")
  local orph2 = processAction(aexpr, gastore) 
  assert(orph2, "658: orph2 is nil")
  local orphans = mergeOrphans(orph1, orph2)
  assert(orphans, "6. Oprhans nil!")
  
  return gastore.guards, gastore.actions, orphans
end
--[[
    In Supremica, if no locations are marked, then all locations are considered to be marked
    The reasoning behind this is that:
    1. In cases, like dealing only with controllability, where it does not matter if no locations
      are marked, it also does not matter if all locations are marked;
    2. In cases, like dealing with nonblocking, where it matters if locations are marked, modeling
      a system with no marked locations is meaningless
    So, if some location is marked, we set the allmarked flag false
--]]
local function manageSourceTarget(srctrgt)
  local label, init, acc, xxx = processSourceTarget(srctrgt:toString():gsub("\n", ""))
  
  label = sanitize(label, "location")
  
  if not CurrentEFA.locations[label] then
    local out = {"location "..label..":"}
    if init then table.insert(out, IS_INITIAL) end
    if acc then 
      table.insert(out, IS_MARKED)
      CurrentEFA.allmarked = false 
    end
    CurrentEFA.locations[label] = { table.concat(out, "\n") }
  end
  return label
end

local function getEdgeEvents(edge)
  local evlist = edge:getLabelBlock():getEventIdentifierList()
  local cevs, uevs = {}, {}
  local iter = evlist:iterator()
  
  while iter:hasNext() do
   -- local ev = sanitize(iter:next():getName(), "event")  -- should be sanitized
   local ev = sanitize(iter:next():toString(), "event")
   if arrayEventMap and arrayEventMap[ev] then
       for _, childEv in ipairs(arrayEventMap[ev]) do
           if cevents[childEv] then 
             table.insert(cevs, childEv)
           elseif uevents[childEv] then
             table.insert(uevs, childEv)
           end
       end
   else
    if cevents[ev] then 
      table.insert(cevs, ev)
    elseif uevents[ev] then
      table.insert(uevs, ev)
    else
      assert(false, "Event "..ev.." not in project event list!")
    end
   end
  end
  
  return cevs, uevs
end
-- Check an actio for multiple assignment of the same variable
-- This is not allowed in CIF, but fine in Supremica (see ConflictingAssignment.wmod)
local function checkMultiAssignment(action)
  
  local cache = {}
  for var in action:gmatch(patterns.multiassign) do
    if not cache[var] then
      cache[var] = true
    else
      return true, var -- there are multiple assignments to this variable
    end
  end
  return false -- there are no multiple assignments
end
  
local function makeEdge(source, target, events, guard, action)
  if #events == 0 then return nil end
  local local_actions = {}
    
  if action and action ~= "" then
      local act_str = action .. ","
      for act in act_str:gmatch("%s*(.-)%s*,") do
          if act ~= "" then
			-- Find the operator to isolate the Left-Hand Side
              local op_start
              for _, vop in ipairs({"+=", "-=", "*=", "/=", "%=", ":=", "="}) do
                  op_start = act:find(vop, 1, true)
                  if op_start then break end
              end
              
              if op_start then
                  -- Extract the full LHS (e.g., "Manager_v2_0.v2_0" or "v1_0")
                  local prefixedLHS = act:sub(1, op_start - 1):match("^%s*(.-)%s*$")
                  local coreVar = prefixedLHS
                  
                  -- EXACTLY YOUR LOGIC: Look for the dot, take what's after it
                  local dotPos = prefixedLHS:find("%.")
                  if dotPos then
                      coreVar = prefixedLHS:sub(dotPos + 1)
                  end
                  
                  -- Check if it is strictly in the SharedVariables registry
                  if SharedVariables[coreVar] then
                      
                      -- Route to manager registry
                      if not Manager_variables[coreVar] then Manager_variables[coreVar] = {} end
                      
                      local managerGuard = CurrentEFA.name .. "." .. source
                      if guard and guard ~= "" then
                          managerGuard = managerGuard .. " and (" .. guard .. ")"
                      end
                      
                      local eventStr = table.concat(events, ", ")
                      local managerEdge = "\tedge " .. eventStr .. " when " .. managerGuard .. " do " .. act .. " ;"
                      table.insert(Manager_variables[coreVar], managerEdge)
                      
                      -- NOTE: We DO NOT insert it into `local_actions`. 
                      -- This is how it gets successfully deleted from the original EFA!
                  else
                      table.insert(local_actions, act)
                  end
              else
                  table.insert(local_actions, act)
              end
          end
      end
  end
  local out = {}
  table.insert(out, "\tedge ")
  table.insert(out, table.concat(events, ", "))
  if guard and guard ~= "" then
    table.insert(out, "when")
    table.insert(out, guard)
  end 
 --[[ if action and action ~= "" then
    table.insert(out, "do")      
    table.insert(out, action)
  end --]]
  if #local_actions > 0 then
    table.insert(out, "do " .. table.concat(local_actions, ", "))
  end
  table.insert(out, "goto")
  table.insert(out, target)
  table.insert(out, ";")
  
  return table.concat(out, " ")
end

-- Search the given set for a table, and assert that there is max one
local function getTable(set)
  local tab = nil
  local ord = {}
  
  -- For transitiosn with no guard and no action, set is nil here
  if set == nil then return tab, ord end
  
  for i = 1, #set do
    if type(set[i]) == "table" then
      assert(tab == nil, "There should be max one table")
      tab = set[i]
    else
      table.insert(ord, set[i])
    end
  end
  return tab, ord
end
-- There are at most one table in each set, and 
-- if there is in one there should be in the other
local function findTables(gset, aset)
  local gtab, gord = getTable(gset)
  local atab, aord = getTable(aset)
  assert((gtab and atab) or (not gtab and not atab), "Should have either no tables or one of each")
  return gtab, atab, gord, aord
end
-- Create a new table and insert elem into it
local function doInsert(tab, elem)
  local newtab = {table.unpack(tab)} -- copy, we know the elements are not tables
  newtab[#newtab+1] = elem
  return newtab
end
  
local function addEdges(src, trgt, events, guards, actions, orphans)
  local gtable, atable, gordinary, aordinary = findTables(guards, actions)
  if not gtable then -- need to check only one here, due the assert above
    local edge = makeEdge(src, trgt, events, table.concat(gordinary, " and "), table.concat(aordinary, ", "))
    table.insert(CurrentEFA.locations[src], {edge, orphans})
  else -- we have to handle the tables
    assert(#gtable == #atable, "Sizes of the two tables should match")
    -- For each pair from gtable and atable, merge with the ordinary guards and actions
    -- make a new edge and add it to the source location
    for i = 1, #gtable do
      local newguard = doInsert(gordinary, gtable[i])
      local newact = doInsert(aordinary, atable[i])
      local edge = makeEdge(src, trgt, events, table.concat(newguard, " and "), table.concat(newact, ", "))
      table.insert(CurrentEFA.locations[src], {edge, orphans})      
    end
  end
end

local function processEdge(edge)
  
  local src = manageSourceTarget(edge:getSource())
  local trgt = manageSourceTarget(edge:getTarget())
  local cevents, uevents = getEdgeEvents(edge)
  local guard, action, orphans = processGuardAction(edge:getGuardActionBlock(), cevents, uevents)
  assert(orphans, "7. Oprhans nil!")

  
  -- Make different edges for controllable and uncontrollable events
  if #cevents > 0 then
    addEdges(src, trgt, cevents, guard, action, orphans)
  end
  if #uevents > 0 then
    addEdges(src, trgt, uevents, guard, action, orphans)
  end  
end

local function resolveNode(node)
    local className = tostring(node:getClass())
    
    while className:find("NodeRef") do
        local success = pcall(function() node = node:getNode() end)
        if not success then break end
        className = tostring(node:getClass())
    end
    
    return node, className
end
-- For each edge, after processing, there will be a set of orphans that need to 
-- be owner-prefixed when the edge is output
local function getLeafNodes(rawNode)
    local leaves = {}
    
    -- 1. Fully resolve any pointers first
    local node, className = resolveNode(rawNode)

    -- 2. Check the resolved concrete class
    if className:find("GroupNode") then
        local success, children = pcall(function() return node:getImmediateChildNodes() end)
        if not success or not children then 
            success, children = pcall(function() return node:getNodeList() end) 
        end
        if success and children then
            local iter = children:iterator()
            while iter:hasNext() do
                for _, leaf in ipairs(getLeafNodes(iter:next())) do
                    table.insert(leaves, leaf)
                end
            end
        end
    else
        -- It is now guaranteed to be a resolved SimpleNode
        table.insert(leaves, node)
    end
    return leaves
end



local function processEFA(efa)
  
  -- Set up global current EFA holder
  CurrentEFA = {name = sanitize(efa:getName(), "automaton")} -- should be sanitized
  CurrentEFA.kind = efaKind[efa:getKind()]
  CurrentEFA.allmarked = true -- In Supremica, if no location explcictly marked, all locations are marked
  CurrentEFA.variables = {} -- in CIF, variables are local to EFA, need to collect
  CurrentEFA.locations = {} -- Holds the locations with events, guard, actions
  
  -- Get edge iterator from Supremica
	local graph = efa:getGraph()
	local edges = graph:getEdges()
	local nodes = graph:getNodes()
	
	local nodeIterator = nodes:iterator()
	while nodeIterator:hasNext() do
		local rawNode = nodeIterator:next()
		local actualNode, nodeClass = resolveNode(rawNode)
		if not nodeClass:find("GroupNode") then
		  manageSourceTarget(actualNode)
		else
			local leaves = getLeafNodes(actualNode)
			for _, leaf in ipairs(leaves) do
				manageSourceTarget(leaf)
			end
		end
	end
	
	--[[local edgeIterator = edges:iterator()
	while edgeIterator:hasNext() do
		processEdge(edgeIterator:next())
	end--]]
	
	local groupEdgeIter = graph:getEdges():iterator()
    while groupEdgeIter:hasNext() do
        local edge = groupEdgeIter:next()
        
        -- RESOLVE the pointers before checking the class!
        local actualSrc, srcClass = resolveNode(edge:getSource())
        local actualTgt, tgtClass = resolveNode(edge:getTarget())
        
        local isSrcGroup = srcClass:find("GroupNode") ~= nil
        local isTgtGroup = tgtClass:find("GroupNode") ~= nil
        
        -- If the edge touches a group (or a pointer to a group), wire the transitions!
        if isSrcGroup or isTgtGroup then
            local srcLeaves = getLeafNodes(actualSrc)
            local tgtLeaves = getLeafNodes(actualTgt)
            
            local cevents, uevents = getEdgeEvents(edge)
            local guard, action, orphans = processGuardAction(edge:getGuardActionBlock(), cevents, uevents)
            
            for _, sLeaf in ipairs(srcLeaves) do
                local sName = manageSourceTarget(sLeaf)
                for _, tLeaf in ipairs(tgtLeaves) do
                    local tName = manageSourceTarget(tLeaf)
                    if #cevents > 0 then addEdges(sName, tName, cevents, guard, action, orphans) end
                    if #uevents > 0 then addEdges(sName, tName, uevents, guard, action, orphans) end
                end
            end
		else 
			processEdge(edge)
        end
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
  if #u > 0 then print("uncontrollable "..table.concat(u, ", ")..";") end
  print("")
end

local function outputBlockedEvents()
    local blockedArray = getEventTable(blockedEvents)
    table.sort(blockedArray)
    
    if #blockedArray > 0 then
        for _, eventName in ipairs(blockedArray) do
            print("requirement invariant " .. eventName .. " needs false;")
        end
    end
	print("")
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

local function outputConstants()
  local declared = {}
  local hasConstants = false
  
  -- 1. Ask the Java API directly for the exact Constant Alias List
  local success, constList = pcall(function() return raw_project:getConstantAliasList() end)
  
  if success and constList then
    for i = 1, constList:size() do
      local comp = constList:get(i-1)
      
      -- Extract the name (e.g., "size")
      local rawName = comp:getName()
      if type(rawName) == "userdata" then pcall(function() rawName = rawName:getName() end) end
      local nameStr = tostring(rawName)
      local cleanName = sanitize(nameStr, "variable")
      
      -- Extract the value from the ConstantAliasExpression
      local valSuccess, cVal = pcall(function() return comp:getConstantAliasExpression():toString() end)
      
      -- Fallback just in case it's stored under a generic getExpression()
      if not valSuccess then
         valSuccess, cVal = pcall(function() return comp:getExpression():toString() end)
      end
      
      if valSuccess and cVal then
		cVal = tostring(cVal)
		if not ConstantRanges[nameStr] then
			-- Only print single elements (Booleans, Single Enums, or Standard Integers)
          if cStr == "true" or cStr == "false" then
              print("const bool " .. cleanName .. " = " .. cStr .. ";")
          elseif Enums[cStr] then
              print("const Enums " .. cleanName .. " = " .. cStr .. ";")
          else
              print("const int " .. cleanName .. " = " .. cStr .. ";")
          end
          
          hasConstants = true
		end
      end
    end
  end
  
  if hasConstants then print("") end
end
--[[
    In Supremica, variables are not owned by any specific EFA, they are free for all
    In CIF, variables MUST be owned by some EFA
    In the conversion, the first EFA to assign a variable is considered its owner
    1. Multiple EFA assigning the same variable cannot be allowed, see ownThisVariable()
    2. A variable not assigned by any EFA, but always keeping its initial value, could be replaced
      by its initial value, and CIF warns about this. BUT! The initial value can be nondeterministic!
--]]
local function outputInputs()
  local hasInputs = false
  for name, info in pairs(Variables) do
    if info.is_input then
      if not hasInputs then
        print("// Global input variables")
        hasInputs = true
      end
      -- Since is_input is strictly checked, we can just print the bool declaration
      print("input bool " .. name .. ";")
    end
  end
  if hasInputs then print("") end
end

local function outputEdge(edge, name)
  
  local str = edge[1]
  local orphans = edge[2]
  
  local padded = " " .. str .. " "

  for var, _ in pairs(orphans) do
    local owner = Variables[var].owner
    -- self-owned variables may be incorrectly classified as orphans,
    -- if it is not known at the time of prefixing, see prefixOwner(), 
    -- that the variable is owned by the current efa
    if owner ~= name then 
		padded = padded:gsub("([^%w_%.])(" .. var .. ")([^%w_])", "%1" .. owner .. ".%2%3")
    end
  end
  str = padded:sub(2, -2)
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
      if efa.allmarked then
        print(body[1])
        print(IS_MARKED)
	  else
        print(body[1]:gsub(patterns.colonatend, ";")) -- change : to ; if : is the last char
	  end
    else
      print(body[1])
      if efa.allmarked then
        print(IS_MARKED)
      end
      for i = 2, #body do
        outputEdge(body[i], efa.name)
      end
    end
  end
  print("end // "..efa.name.."\n")
end

local function outputVariableManagers()
    for varName, edges in pairs(Manager_variables) do
        print("automaton Manager_" .. varName .. ":")
        
        local monitorEvents = {}
        local seenEvents = {}
        for _, edgeStr in ipairs(edges) do
            local evs = edgeStr:match("edge%s+(.-)%s+when") or edgeStr:match("edge%s+(.-)%s+do")
            if evs then
                for ev in evs:gmatch("([^,]+)") do
                    ev = ev:match("^%s*(.-)%s*$")
                    if not seenEvents[ev] then
                        table.insert(monitorEvents, ev)
                        seenEvents[ev] = true
                    end
                end
            end
        end
        
        if #monitorEvents > 0 then
            print("\tmonitor " .. table.concat(monitorEvents, ", ") .. ";")
        end
        
        if Variables[varName] and Variables[varName].decl then
            print("\t" .. Variables[varName].decl)
        end
        
        print("\n\tlocation:")
        print("\t\tinitial; marked;")
		for _, edgeStr in ipairs(edges) do
            -- 1. Strip the redundant self-prefix (e.g., "Manager_t_0.")
            local redundantPrefix = "Manager_" .. varName .. "%."
            local cleanEdge = edgeStr:gsub(redundantPrefix, "")
            
            -- 2. ORPHAN SWEEP: Resolve variables whose owners were unknown during extraction
            local padded = " " .. cleanEdge .. " "
			local seenVars = {}
            for ident in padded:gmatch("([_%a][_%w]*)") do
				if not seenVars[ident] then
					seenVars[ident] = true
	                local owner = nil
	                
	                -- YOUR LOGIC: If it's a shared variable, we absolutely know its future owner
	                if SharedVariables[ident] then
	                    owner = "Manager_" .. ident
	                -- Otherwise, rely on the global registry which should now be fully populated
	                elseif Variables and Variables[ident] and Variables[ident].owner then
	                    owner = Variables[ident].owner
	                end
	                
	                if owner then
	                    -- If the variable belongs to another plant/manager, safely prefix it!
	                    if owner ~= "Manager_" .. varName then
	                        padded = padded:gsub("([^%w_%.])(" .. ident .. ")([^%w_])", "%1" .. owner .. ".%2%3")
	                    end
	                end
				end
            end
            cleanEdge = padded:sub(2, -2)
            
            print("\t\t" .. cleanEdge)
        end
        print("end // Manager_" .. varName .. "\n")
    end
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
  outputBlockedEvents()
  outputEnums()
  outputConstants()
  outputInputs()
  
  for i = 1, efalist:size() do
    local efa = efalist:get(i-1)
--[[	if needsFlattening then
		processFlattenedEFA(efa)
	else --]]
    processEFA(efa)
	--end
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
  
  outputVariableManagers()
  
  if #NameChanges > 0 then
	print("// Automatic naming conlficts resolved")
	for i = 1, #NameChanges do
		print(NameChanges[i])
	end
  end
  
  local textpanel = textframe:getTextPanel()
  local textarea = textpanel:getTextArea()
  local text = textarea:getText()
  local tempCifPath, filename = saveModel(name..".cif", filepath, text)
  if tempCifPath and filename then
  savePLC(tempCifPath, filename)
  end
  
  textframe:dispose()
  
end

preProcessing()
processModule()