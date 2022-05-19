/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fest;

import static org.fest.swing.timing.Pause.*;
import org.fest.swing.timing.Timeout;

import fr.jmmc.oiexplorer.core.model.event.EventNotifier;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.timing.Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains utility methods
 * @author bourgesl
 */
public final class OIExplorerTestUtils {

    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(OIExplorerTestUtils.class.getName());
    /** 20s timeout */
    private static final Timeout LONG_TIMEOUT = Timeout.timeout(20000l);

    /**
     * Forbidden constructor
     */
    private OIExplorerTestUtils() {
        super();
    }

    /**
     * Waits until the asynchronous events are all processed (+ timeout)
     */
    public static void checkPendingEvents() {
        logger.debug("checkPendingEvents : enter");

        pause(new Condition("TaskRunning") {

            /**
             * Checks if the condition has been satisfied.
             * @return <code>true</code> if the condition has been satisfied, otherwise <code>false</code>.
             */
            @Override
            public boolean test() {

                return GuiActionRunner.execute(new GuiQuery<Boolean>() {

                    @Override
                    protected Boolean executeInEDT() {
                        final boolean done = !EventNotifier.isBusy();

                        if (logger.isDebugEnabled()) {
                            logger.debug("checkPendingEvents : done = {}", done);
                        }
                        return done;
                    }
                });

            }
        }, LONG_TIMEOUT);

        logger.debug("checkPendingEvents : exit");
    }

}
