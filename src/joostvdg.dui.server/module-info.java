module joostvdg.dui.server {
    requires joostvdg.dui.api;
    requires joostvdg.dui.client;
    requires joostvdg.dui.logging;

    exports com.github.joostvdg.dui.server.api;
    uses com.github.joostvdg.dui.logging.Logger;
}
