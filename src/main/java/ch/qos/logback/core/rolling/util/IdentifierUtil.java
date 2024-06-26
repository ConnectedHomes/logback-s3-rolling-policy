/**
 * Copyright (C) 2013 AlertMe.com Ltd
 */

package ch.qos.logback.core.rolling.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

public class IdentifierUtil {

    private IdentifierUtil() {
        throw new IllegalStateException("Not implemented");
    }

    public static String getIdentifier() {

        String identifier;

        //
        // 1. Try AWS EC2 Instance ID
        //

        identifier = getContentOfWebpage("http://instance-data/latest/meta-data/instance-id");

        if (identifier != null) {
            return identifier;
        }

        //
        // 2. Try hostname
        //

        identifier = getHostname();

        if (identifier != null) {
            return identifier;
        }

        //
        // 3. When the above 2 methods failed, generate a unique ID
        //

        return UUID.randomUUID().toString();
    }

    public static String getContentOfWebpage(final String location) {

        try {
            final URL url = new URL(location);

            final URLConnection con = url.openConnection();
            final InputStream in = con.getInputStream();
            String encoding = con.getContentEncoding();
            encoding = encoding == null ? "UTF-8" : encoding;

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final byte[] buf = new byte[8192];
            int len;

            while ((len = in.read(buf)) != -1) {
                baos.write(buf, 0, len);
            }

            final String body = new String(baos.toByteArray(), encoding);

            if (!body.isBlank()) {
                return body.trim();
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }

    public static String getHostname() {
        try {
            String hostname = InetAddress.getLocalHost().getHostAddress();

            if (hostname != null) {
                hostname = hostname.replaceAll("[^a-zA-Z0-9.]+", "").trim();
            }

            if (hostname != null && !hostname.isEmpty()) {
                return hostname;
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }
}
