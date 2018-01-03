package com.github.joostvdg.buming.api;

import com.github.joostvdg.buming.logging.Logger;

public interface ConcurrencyExample {
    String name();
    void start(Logger logger);
}
