module joostvdg.buming.concurrency {
    requires joostvdg.buming.api;
    requires joostvdg.buming.logging;
    uses com.github.joostvdg.buming.logging.Logger;

    provides com.github.joostvdg.buming.api.ConcurrencyExample with
        com.github.joostvdg.buming.concurrency.NoVisibility,
        com.github.joostvdg.buming.concurrency.monitor.MonitorExample,
        com.github.joostvdg.buming.concurrency.latch.CountDownLatchExample,
        com.github.joostvdg.buming.concurrency.memoization.Memoization,
        com.github.joostvdg.buming.concurrency.simplelock.SimpleLock,
        com.github.joostvdg.buming.concurrency.dining.DiningPhilosophers,
        com.github.joostvdg.buming.concurrency.stop.GracefulStop;
}
