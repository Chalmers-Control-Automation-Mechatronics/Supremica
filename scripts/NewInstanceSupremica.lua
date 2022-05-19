-- NewInstanceSupremica.lua -- testing to see if I can open a new instanec of Supremica
-- by a script running inside Supremica, and if so, what does the new instance look like
local luaj = luajava -- just shorthand

local newide = luaj.bindClass("org.supremica.gui.ide.IDE") -- ref to IDE class
newide:main({}) -- create a new instance of Supremica and start its GUI