-- PrintOutput.lua. Lua's standard 'print' is intercepted and its output
-- is shown in the logger pane as 'info' messages.
-- The full Supremica logger is available, note the extra argument.
local luaj = luajava -- just shorthand (not even necessary in this small example)
local script, ide, log = ... -- catch these handed over from Supremica

-- Calls to print are intercepted and shown in the logger pane
print("Let's print an important number: ", 42, "!")

-- Supremica's standard logger is also available
log:error("This is an error message!", 0)
log:info("This is just info (same as 'print')", 0) 
log:debug("Debug messages also work", 0) 
log:trace("This is a trace message", 0)

print(_VERSION)