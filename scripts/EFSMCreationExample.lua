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

local function createBinaryExpression(var, op, val)
  local expVar = factory:createSimpleIdentifierProxy(var)
  
  if type(val) == "number" then
    local expVal = factory:createIntConstantProxy(val)
    return factory:createBinaryExpressionProxy(op, expVar, expVal)
  elseif type(val) == "string" then
    local expVal = factory:createSimpleIdentifierProxy(val)
    return factory:createBinaryExpressionProxy(op, expVar, expVal)
  end
  
  return nil
end

local function createLabelBlock(events)
  local labels = luaj.newInstance("java.util.ArrayList", #events)
  for i = 1, #events do
    local event = factory:createSimpleIdentifierProxy(events[i])
    labels:add(event)
  end
  return factory:createLabelBlockProxy(labels, nil)
end

local function createEFSMmodule(name)

  -- Create three events
  local events = luaj.newInstance("java.util.ArrayList", 3)
  -- Controllable event c
  local eventC = createEvent("c", EventKind.CONTROLLABLE)
  events:add(eventC)
  -- uncontrollable event u
  local eventU = createEvent("u", EventKind.UNCONTROLLABLE)
  events:add(eventU)
  -- Proposition :accepting (used for marking)
  local propAcc = createEvent(EventDeclProxy.DEFAULT_MARKING_NAME, EventKind.PROPOSITION)
  events:add(propAcc)
  
  -- Create two variables and an EFSM
  local components = luaj.newInstance("java.util.ArrayList", 3)
  --Integer variable x with range 0..10, init 0
  local varX = createIntegerVariable("x", 0, 10, 0)
  components:add(varX)
  -- Enumeration variable y with range a, b, c, init a
  local varY = createEnumeration("y", {"a", "b", "c" }, "a")
  components:add(varY)
  -- Create two locations for the EFSM
  local states = luaj.newInstance("java.util.ArrayList", 2)
  -- Location q0, initial and unmarked
  local state0 = factory:createSimpleNodeProxy("q0", nil, nil, true, nil, nil, nil)
  states:add(state0)
  -- Location q1, not initial but marked
  local nodeLabelAccepting = factory:createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_MARKING_NAME)
  local nodeLabelList = Collections:singletonList(nodeLabelAccepting)
  local nodeLabelExpression = factory:createPlainEventListProxy(nodeLabelList)
  local state1 = factory:createSimpleNodeProxy("q1", nodeLabelExpression, nil, false, nil, nil, nil)
  states:add(state1)
  -- Create two edges for the EFSM
  local edges = luaj.newInstance("java.util.ArrayList", 2)
  -- edge 1 ...
  -- ... with guard x > 2 && y != a
  local exp1 = createBinaryExpression("x", optable:getGreaterThanOperator(), 2)
  local exp2 = createBinaryExpression("y", optable:getNotEqualsOperator(), "a")
  local guard1 = factory:createBinaryExpressionProxy(optable:getAndOperator(), exp1, exp2);
	local guards1 = Collections:singletonList(guard1)
  -- ... and actions x += 1 and y = c
  local act1 = createBinaryExpression("x", optable:getIncrementOperator(), 1)
  local act2 = createBinaryExpression("y", optable:getAssignmentOperator(), "c")
  local actions1 = luaj.newInstance("java.util.ArrayList", 2)
  actions1:add(act1)
  actions1:add(act2)
  local gaBlock = factory:createGuardActionBlockProxy(guards1, actions1, nil)
  -- ... with two events c and u
  local labels1 = createLabelBlock({"c", "u"})
  -- ... from s0 to s1
  local edge1 = factory:createEdgeProxy(state0, state1, labels1, gaBlock, nil, nil, nil)
  edges:add(edge1)
  -- edge 2... (alternative, use conditional instead of guard/action block)
  -- ... with guard x > 2
  local cond1 = createBinaryExpression("x", optable:getGreaterThanOperator(), 2)
  -- ... with action y = c
  local cond2 = createBinaryExpression("y", optable:getAssignmentOperator(), "c")
  local guard2 = factory:createBinaryExpressionProxy(optable:getAndOperator(), cond1, cond2)
  -- ... with event c
  local edge2LabelC = factory:createSimpleIdentifierProxy("c")
  local condLabels2 = Collections:singletonList(edge2LabelC)
  local cond = factory:createConditionalProxy(condLabels2, guard2)
  local labels2 = Collections:singletonList(cond)
  local labelBlock2 = factory:createLabelBlockProxy(labels2, nil)
  -- ... from s1 to s0
  local edge2 = factory:createEdgeProxy(state1, state0, labelBlock2, nil, nil, nil, nil)
  edges:add(edge2)
  
  -- Create the EFSM 
  local deterministic, blockedEvents = true, nil
  local graph = factory:createGraphProxy(deterministic, blockedEvents, states, edges)
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

-- Save the wmod to teh default file save path
saveModuleAsWMOD(mod)