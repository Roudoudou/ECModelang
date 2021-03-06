# ---------------------------------
# Finds CEGUI toolkit
#
# Sets CEGUI_FOUND
# Sets CEGUI_LIBRARIES
# Sets CEGUI_LIBRARY_DIRS
# Sets CEGUI_LDFLAGS
# Sets CEGUI_LDFLAGS_OTHERS
# Sets CEGUI_INCLUDE_DIRS
# Sets CEGUI_CFLAGS
# Sets CEGUI_CFLAGS_OTHERS
# ---------------------------------

IF(WIN32)
	FIND_PATH(PATH_CEGUI cegui/include/CEGUI.h PATHS ${OV_CUSTOM_DEPENDENCIES_PATH}/cegui)
	IF(PATH_CEGUI)
		SET(CEGUI_FOUND TRUE)
		SET(OgreCEGUIRenderer_FOUND TRUE)
		SET(CEGUI_INCLUDE_DIRS ${PATH_CEGUI}/cegui/include)
		SET(CEGUI_LIBRARIES_DEBUG CEGUIBase_d CEGUIOgreRenderer_d)
		SET(CEGUI_LIBRARIES_RELEASE CEGUIBase CEGUIOgreRenderer)
		SET(CEGUI_LIBRARY_DIRS ${PATH_CEGUI}/lib)
	ENDIF(PATH_CEGUI)
ENDIF(WIN32)

IF(UNIX)
	INCLUDE("FindThirdPartyPkgConfig")
	pkg_check_modules(CEGUI CEGUI)
	pkg_check_modules(OgreCEGUIRenderer CEGUI-OGRE)
ENDIF(UNIX)

IF(CEGUI_FOUND AND OgreCEGUIRenderer_FOUND)
	MESSAGE(STATUS "  Found CEGUI/OgreCEGUIRenderer...")
	INCLUDE_DIRECTORIES(${CEGUI_INCLUDE_DIRS} ${OgreCEGUIRenderer_INCLUDE_DIRS})
	ADD_DEFINITIONS(${CEGUI_CFLAGS} ${OgreCEGUIRenderer_CFLAGS})
	ADD_DEFINITIONS(${CEGUI_CFLAGS_OTHERS} ${OgreCEGUIRenderer_CFLAGS_OTHERS})
	# LINK_DIRECTORIES(${CEGUI_LIBRARY_DIRS} ${OgreCEGUIRenderer_LIBRARY_DIRS})
IF(UNIX)
	FOREACH(CEGUI_LIB ${CEGUI_LIBRARIES} ${OgreCEGUIRenderer_LIBRARIES})
		SET(CEGUI_LIB1 "CEGUI_LIB1-NOTFOUND")
		FIND_LIBRARY(CEGUI_LIB1 NAMES ${CEGUI_LIB} PATHS ${CEGUI_LIBRARY_DIRS} ${CEGUI_LIBDIR} NO_DEFAULT_PATH)
		FIND_LIBRARY(CEGUI_LIB1 NAMES ${CEGUI_LIB})
		IF(CEGUI_LIB1)
			MESSAGE(STATUS "    [  OK  ] Third party lib ${CEGUI_LIB1}")
			TARGET_LINK_LIBRARIES(${PROJECT_NAME} ${CEGUI_LIB1})
		ELSE(CEGUI_LIB1)
			MESSAGE(STATUS "    [FAILED] Third party lib ${CEGUI_LIB}")
		ENDIF(CEGUI_LIB1)
	ENDFOREACH(CEGUI_LIB)
ENDIF(UNIX)
IF(WIN32)
	FOREACH(CEGUI_LIB ${CEGUI_LIBRARIES_DEBUG})
		SET(CEGUI_LIB1 "CEGUI_LIB1-NOTFOUND")
		FIND_LIBRARY(CEGUI_LIB1 NAMES ${CEGUI_LIB} PATHS ${CEGUI_LIBRARY_DIRS} ${CEGUI_LIBDIR} NO_DEFAULT_PATH)
		FIND_LIBRARY(CEGUI_LIB1 NAMES ${CEGUI_LIB})
		IF(CEGUI_LIB1)
			MESSAGE(STATUS "    [  OK  ] Third party lib ${CEGUI_LIB1}")
			TARGET_LINK_LIBRARIES(${PROJECT_NAME} debug ${CEGUI_LIB1})
		ELSE(CEGUI_LIB1)
			MESSAGE(STATUS "    [FAILED] Third party lib ${CEGUI_LIB}")
		ENDIF(CEGUI_LIB1)
	ENDFOREACH(CEGUI_LIB)
	FOREACH(CEGUI_LIB ${CEGUI_LIBRARIES_RELEASE})
		SET(CEGUI_LIB1 "CEGUI_LIB1-NOTFOUND")
		FIND_LIBRARY(CEGUI_LIB1 NAMES ${CEGUI_LIB} PATHS ${CEGUI_LIBRARY_DIRS} ${CEGUI_LIBDIR} NO_DEFAULT_PATH)
		FIND_LIBRARY(CEGUI_LIB1 NAMES ${CEGUI_LIB})
		IF(CEGUI_LIB1)
			MESSAGE(STATUS "    [  OK  ] Third party lib ${CEGUI_LIB1}")
			TARGET_LINK_LIBRARIES(${PROJECT_NAME} optimized ${CEGUI_LIB1})
		ELSE(CEGUI_LIB1)
			MESSAGE(STATUS "    [FAILED] Third party lib ${CEGUI_LIB}")
		ENDIF(CEGUI_LIB1)
	ENDFOREACH(CEGUI_LIB)	
ENDIF(WIN32)

ELSE(CEGUI_FOUND AND OgreCEGUIRenderer_FOUND)
	MESSAGE(STATUS "  FAILED to find CEGUI/OgreCEGUIRenderer...")
ENDIF(CEGUI_FOUND AND OgreCEGUIRenderer_FOUND)
