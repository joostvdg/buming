module joostvdg.buming.cli {

    requires joostvdg.buming.api;
    uses com.github.joostvdg.buming.api.Server;
    uses com.github.joostvdg.buming.api.ConcurrencyExample;

    requires joostvdg.buming.logging;
    uses com.github.joostvdg.buming.logging.Logger;
}
