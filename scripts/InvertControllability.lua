-- InvertControllability.lua, Lua script example of how to invert the controllability of events
-- Meant to be run as a script inside Supremica (with LuaJ embedded)
local luaj = luajava -- just shorthand 
local script, ide = ... -- grab the arguments passed from Java via LuaJ

-- bindClass is like Java's import
local EventKind = luaj.bindClass("net.sourceforge.waters.model.base.EventKind")

-- Get the currently open module
local module = ide:getActiveDocumentContainer():getEditorPanel():getModuleSubject()

-- Get teh alphabet as a modifiable list
local eventDeclList = module:getEventDeclListModifiable()
-- Invert the controllability of the events
for i = 1, eventDeclList:size() do
  local event = eventDeclList:get(i-1)
  local kind = event:getKind()
  if kind == EventKind.CONTROLLABLE then
    -- print("Event "..event:getName().." is controllable")
    event:setKind(EventKind.UNCONTROLLABLE)
  elseif kind == EventKind.UNCONTROLLABLE then
  	-- print("Event "..event:getName().." is uncontrollable")
  	event:setKind(EventKind.CONTROLLABLE)
  end
end