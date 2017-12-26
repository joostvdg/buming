module joostvdg.buming.simpleweb {
    requires joostvdg.buming.api;
    requires jdk.httpserver;

    provides com.github.joostvdg.buming.api.Server with com.github.joostvdg.buming.simpleweb.impl.ServerImpl;
}
