/**
 * Copyright (C) 2013 AlertMe.com Ltd
 */


package ch.qos.logback.core.rolling.data;

import java.util.concurrent.atomic.AtomicReference;

public class CustomData {

    private CustomData() {
        throw new IllegalStateException("Not implemented");
    }

    public static final AtomicReference<String> EXTRA_S3_FOLDER = new AtomicReference<String>(null);
}
