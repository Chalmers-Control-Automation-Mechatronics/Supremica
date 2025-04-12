-- FileChooser.lua, Example showing how to use a file chooser in Lua
-- This is meant to be run as a script within Supremica
-- This is the example given in the Lupremica paper (IFAC WC 2023)
local luaj = luajava -- just shorthand
local script, ide, log = ... -- parameters from Supremica

local fc = luaj.newInstance("javax.swing.JFileChooser")
fc:setDialogTitle("Lupremica file chooser")
local retval = fc:showOpenDialog()
if retval == fc.APPROVE_OPTION then
	local fname = fc:getSelectedFile():getPath() -- does not work on Java > 8
	-- local fname = fc:getName(fc:getSelectedFile()) -- this works for Java > 8
	print("File: "..fname)
else
	print("User cancelled")
end