-- Supremica2CIF.lua, Lua script to convert Supremica models to CIF
-- Meant to be run as a script inside Supremica (with LuaJ embedded)
local luaj = luajava -- just shorthand 
local script, ide = ... -- grab the arguments passed from Java via LuaJ

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

local function processEFA(efa)

end

-- bindClass is like Java's import
local EventKind = luaj.bindClass("net.sourceforge.waters.model.base.EventKind")
local VariableComponentProxy = luaj.bindClass("net.sourceforge.waters.model.module.VariableComponentProxy")
if not VariableComponentProxy then print("VariableComponentProxy not fond") return end

-- Process the currently open module into <name>.cif
local manager = ide:getDocumentContainerManager() 
local container = manager:getActiveContainer()
local name = container:getName()
local module = container:getEditorPanel():getModuleSubject()
local components = module:getComponentList() 

print(getFileName(name..".cif"))

-- It seems we cannot use reflection, so the reflection-based approach to get elements
-- of different types fail. This would need to be handled on the Java side, methinks...

-- Create a VariableComponentProxy (see EFSMsimpleCreate.lua)
local ModuleSubjectFactory = luaj.bindClass("net.sourceforge.waters.subject.module.ModuleSubjectFactory")
local CompilerOperatorTable = luaj.bindClass("net.sourceforge.waters.model.compiler.CompilerOperatorTable")
local factory = ModuleSubjectFactory:getInstance()
local optable = CompilerOperatorTable:getInstance()

local function createIntegerVariable(name, min, max, init)
  
  local varName = factory:createSimpleIdentifierProxy(name)
  local varMin = factory:createIntConstantProxy(min)
  local varMax = factory:createIntConstantProxy(max)
  local varRange = factory:createBinaryExpressionProxy(optable:getRangeOperator(), varMin, varMax)
  local varRef = factory:createSimpleIdentifierProxy(name)
  local varInitVal = factory:createIntConstantProxy(init)
  local varInitPred = factory:createBinaryExpressionProxy(optable:getEqualsOperator(), varRef, varInitVal)
  local var = factory:createVariableComponentProxy(varName, varRange, varInitPred)
  
  local pinterface = var:getProxyInterface()
  local pclass = pinterface:getClass() -- "attempt to call nil"
  local pname = pclass:getName()
  print(pname)

  return var
  
end

local VariableComponentProxy vcp = createIntegerVariable("newVar", 0, 1, 0)

for i = 1, components:size() do
    local proxy = components:get(i-1)
    if proxy == nil then print("Nil proxy!") return end
    print("Non-nil proxy")
    local proxyclass = proxy:getProxyInterface() -- return VariableComponentProxy.class
--    print(proxyclass:getName())
--    if proxyclass:getSimpleName():equals("VariableComponentProxy") then
	if VariableComponentProxy.class:isInstance(proxyclass) then
		print("var.getIdentifier(): " + proxy:getIdentifier())
		print("var.getType().toString(): " + proxy:getType().toString())
		print("var.getInitialStatePredicate().toString(): " + proxy:getInitialStatePredicate().toString())
	end
end