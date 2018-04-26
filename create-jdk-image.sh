#!/usr/bin/env bash
JAVA_HOME=/home/joost/Tools/jdk-9.0.1
PATH=${JAVA_HOME}/bin:${PATH}
echo " > PWD=${PWD}"
echo " > JAVA_HOME=${JAVA_HOME}"

echo " > Recycling build folders"
rm -rf ${PWD}/mods/compiled
mkdir -p ${PWD}/mods/compiled
rm -rf ${PWD}/mods/jars
mkdir -p ${PWD}/mods/jars
rm -rf ${PWD}/buming-image
mkdir -p ${PWD}/buming-image

echo " > Compiling all classes"
javac -Xlint:unchecked -d ${PWD}/mods/compiled --module-source-path ${PWD}/src/ $(find src -name "*.java")

echo " > Create API module"
jar --create --file ${PWD}/mods/jars/joostvdg.buming.api.jar --module-version 1.0 -C ${PWD}/mods/compiled/joostvdg.buming.api .

echo " > Create Logging module"
jar --create --file ${PWD}/mods/jars/joostvdg.buming.logging.jar --module-version 1.0 -C ${PWD}/mods/compiled/joostvdg.buming.logging .

echo " > Create SimpleWeb module"
jar --create --file ${PWD}/mods/jars/joostvdg.buming.simpleweb.jar --module-version 1.0 -C ${PWD}/mods/compiled/joostvdg.buming.simpleweb .

echo " > Create Concurrency module"
jar --create --file ${PWD}/mods/jars/joostvdg.buming.concurrency.jar --module-version 1.0 -C ${PWD}/mods/compiled/joostvdg.buming.concurrency .

echo " > Create Sorting module"
jar --create --file ${PWD}/mods/jars/joostvdg.buming.sorting.jar --module-version 1.0 -C ${PWD}/mods/compiled/joostvdg.buming.sorting .

echo " > Create CLI module"
jar --create --file ${PWD}/mods/jars/joostvdg.buming.cli.jar --module-version 1.0  -e com.github.joostvdg.buming.cli.HelloWorld -C ${PWD}/mods/compiled/joostvdg.buming.cli .

echo " > Create buming-image"
rm -rf buming-image

echo "---------------------------------------------------------------------------------------------"
jlink --module-path ${PWD}/mods/jars/:/${JAVA_HOME}/jmods \
    --add-modules joostvdg.buming.cli \
    --add-modules joostvdg.buming.logging \
    --add-modules joostvdg.buming.api \
    --add-modules joostvdg.buming.simpleweb \
    --add-modules joostvdg.buming.concurrency \
    --add-modules joostvdg.buming.sorting \
    --launcher buming=joostvdg.buming.cli \
    --output buming-image
echo "---------------------------------------------------------------------------------------------"
echo "==================="
echo "== List Modules "
buming-image/bin/java --list-modules
echo "==================="
echo "==================="
echo "== Running app without plugins"
buming-image/bin/buming -server
echo "==================="
echo "==================="
#echo "== Running app with project generator plugin"
#buming-image/bin/cab ${PWD}/mods/compiled/joostvdg.buming.plugin.projectgenerator
#echo "==================="
