module joostvdg.buming.logging {

    exports com.github.joostvdg.buming.logging;
    provides com.github.joostvdg.buming.logging.Logger with com.github.joostvdg.buming.logging.impl.LoggerImpl;
}
