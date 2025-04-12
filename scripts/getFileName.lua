-- Convenience function to put together the filename to save output into
local luaj = luajava

local function getFileName(fname)
    local Config = luaj.bindClass("org.supremica.properties.Config")
    local tempdir = Config.FILE_SAVE_PATH:getValue():getPath() -- use default save path
    local lastch = tempdir:sub(-1) -- get last character
    local sep = "/"
    if lastch == '/' or lastch == '\\' then
        sep = ""
    end
    return tempdir..sep..fname
end

return getFileName