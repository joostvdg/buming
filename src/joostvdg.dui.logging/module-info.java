import com.github.joostvdg.dui.logging.Logger;
import com.github.joostvdg.dui.logging.impl.LoggerImpl;

module joostvdg.dui.logging {

    exports com.github.joostvdg.dui.logging;
    provides Logger with LoggerImpl;
}
