FROM openjdk:9-jdk AS build

RUN mkdir -p /usr/src/mods/jars
RUN mkdir -p /usr/src/mods/compiled

COPY . /usr/src
WORKDIR /usr/src

RUN javac -Xlint:unchecked -d /usr/src/mods/compiled --module-source-path /usr/src/src $(find src -name "*.java")
RUN jar --create --file /usr/src/mods/jars/joostvdg.dui.logging.jar --module-version 1.0 -C /usr/src/mods/compiled/joostvdg.dui.logging .
RUN jar --create --file /usr/src/mods/jars/joostvdg.dui.api.jar --module-version 1.0 -C /usr/src/mods/compiled/joostvdg.dui.api .
RUN jar --create --file /usr/src/mods/jars/joostvdg.dui.client.jar --module-version 1.0 -C /usr/src/mods/compiled/joostvdg.dui.client .
RUN jar --create --file /usr/src/mods/jars/joostvdg.dui.server.jar --module-version 1.0  -e com.github.joostvdg.dui.server.cli.DockerApp\
    -C /usr/src/mods/compiled/joostvdg.dui.server .

RUN rm -rf /usr/bin/dui-image
RUN jlink --module-path /usr/src/mods/jars/:/${JAVA_HOME}/jmods \
    --add-modules joostvdg.dui.api \
    --add-modules joostvdg.dui.logging \
    --add-modules joostvdg.dui.server \
    --add-modules joostvdg.dui.client \
    --launcher dui=joostvdg.dui.server \
    --output /usr/bin/dui-image

RUN ls -lath /usr/bin/dui-image
RUN ls -lath /usr/bin/dui-image
RUN /usr/bin/dui-image/bin/java --list-modules

FROM debian:stable-slim
ENV DATE_CHANGED="20180120-1525"
COPY --from=build /usr/bin/dui-image/ /usr/bin/dui
RUN /usr/bin/dui/bin/java --list-modules
#ENTRYPOINT ["/usr/bin/cli/bin/java", "--list-modules"]
ENTRYPOINT ["/usr/bin/dui/bin/dui"]


