-- EFSMsimpleCreate.lua, Example showing how to create a Supremica module in Lua
-- This is meant to be run as a script within Supremica
-- This is the example given in the Lupremica paper (IFAC WC 2023)
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

  -- controllable event "c", stored in an alphabet
  local eventC = createEvent("c", EventKind.CONTROLLABLE)
  -- marking proposition
  local mark = createEvent(EventDeclProxy.DEFAULT_MARKING_NAME, EventKind.PROPOSITION)
  local alphabet = makeList(eventC, mark)
  -- integer variable x, range 0..10, init 5
  local varX = createIntegerVariable("x", 0, 10, 5)   
  -- locations: q0, initial, unmarked; q1, marked
  local loc0 = createLocation("q0", true, false) -- Location q0; initial, unmarked
  local loc1 = createLocation("q1", false, true) -- Location q1; not initial, marked
  local locations = makeList(loc0, loc1)
  -- create guard x > 2
  local guard = createBinaryExpression("x", optable:getGreaterThanOperator(), 2)
  local guards = makeList(guard) 
  -- create action x -= 1 
  local action = createBinaryExpression("x", optable:getDecrementOperator(), 1)
  local actions = makeList(action)
  local label = createLabelBlock({"c"})
  -- create guard/action block
  local gaBlock = factory:createGuardActionBlockProxy(guards, actions, nil)
  -- edge from q0 to q1
  local edge = factory:createEdgeProxy(loc0, loc1, label, gaBlock, nil, nil, nil)
  local edges = makeList(edge)
  -- deterministic, no blocked events, locations and edges 
  local graph = factory:createGraphProxy(true, nil, locations, edges)
  local efsmName = factory:createSimpleIdentifierProxy("MyEFSM")
  local efsm = factory:createSimpleComponentProxy(efsmName, ComponentKind.PLANT, graph);
  
  local components = makeList(varX, efsm) -- put the components together
  
  -- Combine events, variables, and EFSM to make module
  local mod = factory:createModuleProxy("MyModule", "Module comment", nil, nil, alphabet, nil, components);
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