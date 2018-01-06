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
rm -rf ${PWD}/dui-image
mkdir -p ${PWD}/dui-image

echo " > Compiling all classes"
javac -Xlint:unchecked -d ${PWD}/mods/compiled --module-source-path ${PWD}/src/ $(find src -name "*.java")

echo " > Create API module"
jar --create --file ${PWD}/mods/jars/joostvdg.dui.api.jar --module-version 1.0 -C ${PWD}/mods/compiled/joostvdg.dui.api .

echo " > Create Client module"
jar --create --file ${PWD}/mods/jars/joostvdg.dui.client.jar --module-version 1.0 -C ${PWD}/mods/compiled/joostvdg.dui.client .

echo " > Create Server module"
jar --create --file ${PWD}/mods/jars/joostvdg.dui.server.jar --module-version 1.0 -C ${PWD}/mods/compiled/joostvdg.dui.server .

echo " > Create Test module"
jar --create --file ${PWD}/mods/jars/joostvdg.dui.test.jar --module-version 1.0  -e com.github.joostvdg.dui.test.TestApp -C ${PWD}/mods/compiled/joostvdg.dui.test .

echo " > Create dui-image"
rm -rf dui-image

echo "---------------------------------------------------------------------------------------------"
jlink --module-path ${PWD}/mods/jars/:/${JAVA_HOME}/jmods \
    --add-modules joostvdg.dui.test \
    --add-modules joostvdg.dui.server \
    --add-modules joostvdg.dui.api \
    --add-modules joostvdg.dui.client \
    --launcher buming=joostvdg.dui.test \
    --output dui-image
echo "---------------------------------------------------------------------------------------------"
echo "==================="
echo "== List Modules "
dui-image/bin/java --list-modules
echo "==================="
echo "==================="
echo "== Running dui app"
dui-image/bin/buming -server
echo "==================="
echo "==================="
#echo "== Running app with project generator plugin"
#buming-image/bin/cab ${PWD}/mods/compiled/joostvdg.buming.plugin.projectgenerator
#echo "==================="
