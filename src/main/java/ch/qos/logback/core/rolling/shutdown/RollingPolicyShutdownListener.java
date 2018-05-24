/**
 * Copyright (C) 2013 AlertMe.com Ltd
 */


package ch.qos.logback.core.rolling.shutdown;

public interface RollingPolicyShutdownListener {

    /**
     * Shutdown hook that gets called when exiting the application.
     */
    void doShutdown();
}
