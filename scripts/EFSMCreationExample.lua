-- EFSMCreationExample.lua, Example showing how to create a Supremica module in Lua
-- This is meant to be run as a script within Supremica
local luaj = luajava -- just shorthand
local script, ide, log = ... -- parameters from Supremica

local waters = "net.sourceforge.waters." -- more shorthand

-- These are like Java imports
local ModuleSubjectFactory = luaj.bindClass(waters.."subject.module.ModuleSubjectFactory")
local CompilerOperatorTable = luaj.bindClass(waters.."model.compiler.CompilerOperatorTable")
local EventDeclProxy = luaj.bindClass(waters.."model.module.EventDeclProxy")
local EventKind = luaj.bindClass(waters.."model.base.EventKind")
local ComponentKind = luaj.bindClass(waters.."model.base.ComponentKind")

local Collections = luaj.bindClass("java.util.Collections")

local factory = ModuleSubjectFactory:getInstance()
local optable = CompilerOperatorTable:getInstance()

local function createEvent(name, kind)
  local eventName = factory:createSimpleIdentifierProxy(name)
  local event = factory:createEventDeclProxy(eventName, kind)
  return event
end

local function createLocation(label, initial, marked)

  if not marked then -- Create nonmarked location
    return factory:createSimpleNodeProxy(label, nil, nil, initial, nil, nil, nil)
  end
  -- Create a marked location
  local nodeLabelAccepting = factory:createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_MARKING_NAME)
  local nodeLabelList = Collections:singletonList(nodeLabelAccepting)
  local marking = factory:createPlainEventListProxy(nodeLabelList)
  return factory:createSimpleNodeProxy(label, marking, nil, initial, nil, nil, nil)
  
end

local function createIntegerVariable(name, min, max, init)
  
  local varName = factory:createSimpleIdentifierProxy(name)
  
  local varMin = factory:createIntConstantProxy(min)
  local varMax = factory:createIntConstantProxy(max)
  local varRange = factory:createBinaryExpressionProxy(optable:getRangeOperator(), varMin, varMax)
  
  local varRef = factory:createSimpleIdentifierProxy(name)
  local varInitVal = factory:createIntConstantProxy(init)
  local varInitPred = factory:createBinaryExpressionProxy(optable:getEqualsOperator(), varRef, varInitVal)
  
  local var = factory:createVariableComponentProxy(varName, varRange, varInitPred)
  return var
end

local function createEnumeration(name, values, init)
  
  local varName = factory:createSimpleIdentifierProxy(name)
  
  local enumMembers = luaj.newInstance("java.util.ArrayList", #values)
  for i = 1, #values do
    local member = factory:createSimpleIdentifierProxy(values[i])
    enumMembers:add(member)
  end
  local varRange = factory:createEnumSetExpressionProxy(enumMembers)
  
  local varRef = factory:createSimpleIdentifierProxy(name)
  local varInitVal = factory:createSimpleIdentifierProxy(init)
  local varInitPred = factory:createBinaryExpressionProxy(optable:getEqualsOperator(), varRef, varInitVal)
  
  local var = factory:createVariableComponentProxy(varName, varRange, varInitPred)
  return var
end

local function createBinaryExpression(op1, op, op2)
  
  local function getOperandType(operand)
    if type(operand) == "number" then
      return factory:createIntConstantProxy(tonumber(operand))
    elseif type(operand) == "string" then
      return factory:createSimpleIdentifierProxy(operand)
    end
    -- Type is neither number nor string, just return as is
    return operand
  end

  local operand1 = getOperandType(op1)
  local operand2 = getOperandType(op2)
  
  return factory:createBinaryExpressionProxy(op, operand1, operand2)
  
end

local function createLabelBlock(events)
  local labels = luaj.newInstance("java.util.ArrayList", #events)
  for i = 1, #events do
    local event = factory:createSimpleIdentifierProxy(events[i])
    labels:add(event)
  end
  return factory:createLabelBlockProxy(labels, nil)
end

local function makeList(...)
  local args = {...}
  if #args == 1 then
    return Collections:singletonList(args[1])
  end
  local list = luaj.newInstance("java.util.ArrayList", #args)
  for i = 1, #args do
    list:add(args[i])
  end
  return list
end

local function createEFSMmodule(name)

  -- Create three events
  local eventC = createEvent("c", EventKind.CONTROLLABLE)
  local eventU = createEvent("u", EventKind.UNCONTROLLABLE)
  -- Proposition :accepting (used for marking)
  local propAcc = createEvent(EventDeclProxy.DEFAULT_MARKING_NAME, EventKind.PROPOSITION)
  local events = makeList(eventC, eventU, propAcc)
  
  -- Create two variables and an EFSM, these are the components
  local varX = createIntegerVariable("x", 0, 10, 0) -- Integer variable x, range 0..10, init 0
  local varY = createEnumeration("y", {"a", "b", "c" }, "a") -- Enumeration variable y, range a, b, c, init a
  local components = makeList(varX, varY) -- the EFSM is created and added below
  
  -- Create two locations for the EFSM
  local loc0 = createLocation("q0", true, false) -- Location q0; initial, unmarked
  local loc1 = createLocation("q1", false, true) -- Location q1; not initial, marked
  local locations = makeList(loc0, loc1)
  
  -- Create two edges for the EFSM
  -- edge 1 ...
  -- ... with guard x > 2 && y != a
  local exp1 = createBinaryExpression("x", optable:getGreaterThanOperator(), 2)
  local exp2 = createBinaryExpression("y", optable:getNotEqualsOperator(), "a")
  local guard1 = createBinaryExpression(exp1, optable:getAndOperator(), exp2)
	local guards1 = makeList(guard1) 
  -- ... and actions x += 1 and y = c
  local act1 = createBinaryExpression("x", optable:getIncrementOperator(), 1)
  local act2 = createBinaryExpression("y", optable:getAssignmentOperator(), "c")
  local actions1 = makeList(act1, act2)
  
  local gaBlock = factory:createGuardActionBlockProxy(guards1, actions1, nil)
  -- ... with two events c and u
  local labels1 = createLabelBlock({"c", "u"})
  -- ... from q0 to q1
  local edge1 = factory:createEdgeProxy(loc0, loc1, labels1, gaBlock, nil, nil, nil)
  
  -- edge 2... (alternative, use conditional instead of guard/action block)
  -- ... with guard x > 2
  local cond1 = createBinaryExpression("x", optable:getGreaterThanOperator(), 2)
  -- ... and action y = c
  local cond2 = createBinaryExpression("y", optable:getAssignmentOperator(), "c")
  local guard2 = createBinaryExpression(cond1, optable:getAndOperator(), cond2)
  -- ... with event c
  local edge2LabelC = factory:createSimpleIdentifierProxy("c")
  local condLabels2 = makeList(edge2LabelC) 
  local cond = factory:createConditionalProxy(condLabels2, guard2)
  local labels2 = makeList(cond) 
  local labelBlock2 = factory:createLabelBlockProxy(labels2, nil)
  -- ... from q1 to q0
  local edge2 = factory:createEdgeProxy(loc1, loc0, labelBlock2, nil, nil, nil, nil)
  
  local edges = makeList(edge1, edge2)
  
  -- Create the EFSM 
  local deterministic, blockedEvents = true, nil
  local graph = factory:createGraphProxy(deterministic, blockedEvents, locations, edges)
  local efsmName = factory:createSimpleIdentifierProxy(name)
  local efsm = factory:createSimpleComponentProxy(efsmName, ComponentKind.PLANT, graph);
  components:add(efsm)
  
  -- Combine events, variables, and EFSM to make module
  local moduleName = name.."_module"
	local mod = factory:createModuleProxy(moduleName, "Automatically created demo module.", nil, nil, events, nil, components);
  print("Successfully created module: "..mod:getName())  
  return mod
end

local function saveModuleAsWMOD(mod)
  
  local Config = luaj.bindClass("org.supremica.properties.Config")
  local MarshallingTools = luaj.bindClass(waters.."model.marshaller.MarshallingTools")
  
  local savePath = Config.FILE_SAVE_PATH:getValue():toString()
  local wmod = savePath.."/"..mod:getName()..".wmod"
  MarshallingTools:saveModule(mod, wmod)
end

-- It all starts here, really... create the EFSM and open it in the Supremica IDE
local mod = createEFSMmodule("FirstOne")
local manager = ide:getDocumentContainerManager()
local container = luaj.newInstance("org.supremica.gui.ide.ModuleContainer", ide, mod)
manager:addContainer(container)

-- Save the wmod to the default file save path
saveModuleAsWMOD(mod)