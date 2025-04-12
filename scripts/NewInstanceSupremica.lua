-- NewInstanceSupremica.lua --  open a new instance of Supremica
-- by a script running inside Supremica, and open a module in it
-- This can also be done from "outside" by interfacing with Supremica
local luaj = luajava -- just shorthand

local newide = luaj.bindClass("org.supremica.gui.ide.IDE") -- ref to IDE class
newide:main({}) -- create a new instance of Supremica and start its GUI

local ide = newide:getTheIDE() -- get a reference to the new instance

local file = luaj.newInstance("java.io.File", "Z:/Supremica/examples/waters/tests/synthesis/transferline_2.wmod")
local manager = ide:getDocumentContainerManager()
manager:openContainer(file)