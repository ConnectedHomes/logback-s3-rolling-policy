/**
 * Copyright (C) 2013 AlertMe.com Ltd
 */


package ch.qos.logback.core.rolling.shutdown;


public class ShutdownHookUtil {

    private ShutdownHookUtil() {
        throw new IllegalStateException("Not implemented");
    }

    public static void registerShutdownHook(final RollingPolicyShutdownListener listener, final ShutdownHookType shutdownHookType) {
        switch (shutdownHookType) {
            case SERVLET_CONTEXT:
                RollingPolicyContextListener.registerShutdownListener(listener);
                break;
            case JVM_SHUTDOWN_HOOK:
                Runtime.getRuntime().addShutdownHook(new Thread(new RollingPolicyJVMListener(listener)));
                break;
            case NONE:
            default:
                //Do nothing
                break;
        }
    }
}
