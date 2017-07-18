# -*- Makefile -*-
#############################################################################
# Makefile for libwaters.so / waters.dll
#############################################################################
# Use through ant only, do not invoke 'make' directly
#############################################################################

#############################################################################
# OS dependent

TARGET_ARCH            ?= $(HOST_ARCH)

ifeq "$(findstring linux,$(HOST_ARCH))" "linux"
  export HOST_OS       = linux
  RMDIR                = rm -rf $(strip $(1))
  RMFILE               = rm -f $(strip $(1))
else
  export HOST_OS       = win32
  RMDIR                = $(if $(wildcard $(1)),rd /s /q $(subst /,\,$(1)))
  RMFILE               = $(if $(wildcard $(1)),del $(subst /,\,$(1)))
endif

ifeq "$(findstring linux,$(TARGET_ARCH))" "linux"
  export TARGET_OS     = linux
  export ARCH          = $(patsubst linux.%,%,$(TARGET_ARCH))
  export EXTRA_CFLAGS  = -fPIC
  export LIB_PREFIX    = lib
  export LIB_SUFFIX    = so
else
  export TARGET_OS     = win32
  export ARCH          = $(patsubst win32.%,%,$(TARGET_ARCH))
  export EXTRA_CFLAGS  = -D_JNI_IMPLEMENTATION_
  export EXTRA_LDFLAGS = -Wl,--kill-at -static-libgcc -static-libstdc++
  export LIB_SUFFIX    = dll
endif

ifeq "$(ARCH)" "$(TARGET_ARCH)"
  export OS_ARCH         = $(TARGET_ARCH)
else
  export OS_ARCH         = $(TARGET_OS).$(ARCH)
endif

ifeq "$(ARCH)" "x86"
  export ARCH_FLAGS      = -m32
endif

ifeq "$(ARCH)" "amd64"
  export ARCH_FLAGS      = -m64
endif

ifeq "$(DEBUG)" "1"
  export OS_ARCH_D       = $(OS_ARCH).debug
  export CFLAGS          = -g -DDEBUG -O2
else
  export OS_ARCH_D       = $(OS_ARCH)
  export CFLAGS          = -O6
endif


#############################################################################
# Cross-compiling Linux -> Windows

ifeq "$(CCPP)" ""

ifeq "$(HOST_OS)" "$(TARGET_OS)"
  export CXX             = g++
else
  ifeq "$(TARGET_OS)" "win32"
    ifeq "$(ARCH)" "x86"
      export CXX         = i686-w64-mingw32-g++
    endif
    ifeq "$(ARCH)" "amd64"
      export CXX         = x86_64-w64-mingw32-g++
    endif
  endif
endif

else
export CXX		 = $(CCPP)
endif

export LD                = $(CXX)


#############################################################################
# List of sources

export PROJECT  = waters
export INCDIR   = include
export SRCDIR   = src
export JNIDIR   = $(SRCDIR)/jni
export CPPDIR   = $(SRCDIR)/waters
export OBJDIR   = objects/$(OS_ARCH_D)
export LIBDIR   = lib/$(OS_ARCH_D)

export CPP	= $(wildcard $(CPPDIR)/analysis/*.cpp) \
              $(wildcard $(CPPDIR)/base/*.cpp) \
              $(wildcard $(CPPDIR)/des/*.cpp) \
              $(wildcard $(JNIDIR)/cache/*.cpp) \
              $(wildcard $(JNIDIR)/glue/*.cpp)
export HPP	= $(wildcard $(CPPDIR)/base/*.h) \
              $(wildcard $(CPPDIR)/des/*.h) \
              $(wildcard $(CPPDIR)/javah/*.h) \
              $(wildcard $(JNIDIR)/cache/*.h) \
              $(wildcard $(JNIDIR)/glue/*.h)

export OBJ      = $(CPP:$(SRCDIR)%.cpp=$(OBJDIR)%.o)
export LIB      = $(LIBDIR)/$(LIB_PREFIX)$(PROJECT).$(LIB_SUFFIX)

# flags

JNI_INC       = -I"$(INCDIR)/java" -I"$(INCDIR)/java/$(TARGET_OS)"

export CFLAGS   += -Wall -D__STDC_LIMIT_MACROS $(ARCH_FLAGS) $(EXTRA_CFLAGS)
export INCDIRS  = -I$(SRCDIR) $(JNI_INC)
export LDFLAGS  = $(ARCH_FLAGS) $(EXTRA_LDFLAGS)
export DEP      = dependencies.$(OS_ARCH)


#############################################################################
# Main Rules

all::
			@$(MAKE) -f Makefile.dep

clean::
			-$(call RMFILE,dependencies.*)
			-$(call RMDIR,objects)
			-$(call RMDIR,lib)

