-- BDDSynthesizerScript.lua, Lua script example of how to use the BDD syntehsizer in Lua
-- Meant to be run as a script inside Supremica (with LuaJ embedded)

local luaj = luajava -- just shorthand 
local script, ide = ... -- grab the arguments passed from Java via LuaJ

local actions = ide:getActions()
local editorSynthesizerAction  = actions.editorSynthesizerAction

local EventKind = luaj.bindClass("net.sourceforge.waters.model.base.EventKind")

local module = ide:getActiveDocumentContainer():getEditorPanel():getModuleSubject()

local options = luaj.newInstance("org.supremica.automata.algorithms.EditorSynthesizerOptions");

-- collect the controllable events
local controllableEvents = luaj.newInstance("java.util.Vector")
local eventDeclList = module:getEventDeclListModifiable()
for i = 1, eventDeclList:size() do
  local event = eventDeclList:get(i-1)
  if event:getKind() == EventKind.CONTROLLABLE then
    controllableEvents:add(event:getName())
  end
end

local exAutomata = luaj.newInstance("org.supremica.automata.ExtendedAutomata", module)
local bddSynthesizer = luaj.newInstance("org.supremica.automata.BDD.EFA.BDDExtendedSynthesizer", exAutomata, options)
bddSynthesizer:synthesize(options)
bddSynthesizer:generateGuard(controllableEvents, options)
editorSynthesizerAction:saveOrPrintGuards(bddSynthesizer, controllableEvents,
                                    options:getSaveInFile(), options:getPrintGuard(),
                                    module:getName(), "R:/BDDsynthOutput.txt")
if options:getAddGuards() then
  bddSynthesizer:addGuardsToAutomata()
end

-- Cleanup...
bddSynthesizer:done()