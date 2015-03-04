TEMPLATE = app
CONFIG += console
CONFIG -= app_bundle
CONFIG -= qt

SOURCES += main.cpp \
    select.cpp \
    presage.cpp \
    priorprobability.cpp

HEADERS += \
    select.h \
    presage.h \
    presageException.h \
    presageCallback.h \
    priorprobability.h

