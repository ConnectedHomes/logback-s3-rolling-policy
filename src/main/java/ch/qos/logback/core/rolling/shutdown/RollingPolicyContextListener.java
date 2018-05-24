/**
 * Copyright (C) 2013 AlertMe.com Ltd
 */


package ch.qos.logback.core.rolling.shutdown;

import com.google.common.collect.Lists;
import java.util.List;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class RollingPolicyContextListener implements ServletContextListener {

    private static final List<RollingPolicyShutdownListener> LISTENERS = Lists.newArrayList();

    /**
     * Registers a new shutdown hook.
     *
     * @param listener The shutdown hook to register.
     */
    public static void registerShutdownListener(final RollingPolicyShutdownListener listener) {
        if (!LISTENERS.contains(listener)) {
            LISTENERS.add(listener);
        }
    }

    /**
     * Deregisters a previously registered shutdown hook.
     *
     * @param listener The shutdown hook to deregister.
     */
    public static void deregisterShutdownListener(final RollingPolicyShutdownListener listener) {
        LISTENERS.remove(listener);
    }

    @Override
    public void contextInitialized(final ServletContextEvent servletContextEvent) {
        //Empty
    }

    @Override
    public void contextDestroyed(final ServletContextEvent servletContextEvent) {

        //Upload
        for (final RollingPolicyShutdownListener listener : LISTENERS) {
            listener.doShutdown();
        }
    }
}
