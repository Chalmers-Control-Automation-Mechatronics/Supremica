-- OpenModuleScript.lua, Lua script example of how to open a module in Supremica
-- Meant to be run as a script inside Supremica (with LuaJ embedded)

local luaj = luajava -- just shorthand 

local fname = "Z:/Supremica/examples/waters/tests/synthesis/transferline_1.wmod"

local function openModule(file, ide)
  local manager = ide:getDocumentContainerManager()
  manager:openContainer(file)
end

local script, ide = ... -- grab the arguments passed from Java via LuaJ

-- Or get the IDE reference this way (the above is preferred, this will be depreceated)
--local AnalyzerRunScript = luaj.newInstance("org.supremica.gui.ide.actions.AnalyzerRunScript")
--local ide = AnalyzerRunScript:getIDE()

local file = luaj.newInstance("java.io.File", fname)
openModule(file, ide)
