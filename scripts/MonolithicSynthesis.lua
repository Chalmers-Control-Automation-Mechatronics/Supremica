-- MonolithicSynthesis.lua, example of how to perform monolithic
-- synthesis for the in Supremica currently open module
local luaj = luajava -- just shorthand
local script, ide, logger = ... -- parameters from Supremica

local waters = "net.sourceforge.waters." -- more shorthand
-- bindings, similar to Java import
local ProductDESProxyFactory = luaj.bindClass(waters.."model.des.ProductDESProxyFactory")
local ProductDESElementFactory = luaj.bindClass(waters.."plain.des.ProductDESElementFactory")
local ModuleSubjectFactory = luaj.bindClass(waters.."subject.module.ModuleSubjectFactory")

-- Show that we're alive and kickin'
logger:info("Monolithic synthesis of current module...", 0)

-- Create a compiler
local manager = ide:getDocumentManager()
local desFactory = ProductDESElementFactory:getInstance()
local module = ide:getActiveDocumentContainer():getEditorPanel():getModuleSubject()
local compiler = luaj.newInstance(waters.."model.compiler.ModuleCompiler", manager, desFactory, module)
-- Configure the compiler
-- optimisation removes selfloops and redundant components
compiler:setOptimizationEnabled(true)
-- normalisation is needed for this module with advanced features
compiler:setNormalizationEnabled(true)
-- only report the first error even if there are several
compiler:setMultiExceptionsEnabled(false)
-- Compile the module
local des = compiler:compile()

-- Now for synthesis of the compiled module
local synthesizer = luaj.newInstance(waters.."analysis.monolithic.MonolithicSynthesizer", desFactory)
synthesizer:setModel(des)
synthesizer:run()
local result = synthesizer:getAnalysisResult()
if result:isSatisfied() then
	local supervisor = result:getComputedProductDES()
	local factory = ModuleSubjectFactory:getInstance()
	local importer = luaj.newInstance(waters.."model.marshaller.ProductDESImporter", factory)
	local supervisorModule = importer:importModule(supervisor)
	local supervisorComponents = supervisorModule:getComponentListModifiable()
	-- iterate over all components and add to the module one by one
	local iterator = supervisor:getAutomata():iterator()
	while iterator:hasNext() do
		local aut = iterator:next()
		local comp = importer:importComponent(aut)
		module:getComponentListModifiable():add(comp)
	end
	logger:info("Synthesized supervisor(s) added to module", 0)
else
	logger.info("Synthesis result is empty.", 0)
end