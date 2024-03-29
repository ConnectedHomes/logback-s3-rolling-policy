/**
 * Copyright (C) 2013 AlertMe.com Ltd
 */


package ch.qos.logback.core.rolling.aws;

import ch.qos.logback.core.rolling.data.CustomData;
import ch.qos.logback.core.rolling.shutdown.RollingPolicyShutdownListener;
import ch.qos.logback.core.rolling.util.IdentifierUtil;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AmazonS3ClientImpl implements RollingPolicyShutdownListener {
    private static final Logger log = LoggerFactory.getLogger(AmazonS3ClientImpl.class);

    private final String awsAccessKey;
    private final String awsSecretKey;
    private final String s3BucketName;
    private final String s3FolderName;

    private final boolean prefixTimestamp;
    private final boolean prefixIdentifier;

    private final String identifier;

    private AmazonS3 amazonS3Client;
    private final ExecutorService executor;

    public AmazonS3ClientImpl(final String awsAccessKey, final String awsSecretKey, final String s3BucketName,
                              final String s3FolderName, final boolean prefixTimestamp,
                              final boolean prefixIdentifier) {

        this.awsAccessKey = awsAccessKey;
        this.awsSecretKey = awsSecretKey;
        this.s3BucketName = s3BucketName;
        this.s3FolderName = s3FolderName;

        this.prefixTimestamp = prefixTimestamp;
        this.prefixIdentifier = prefixIdentifier;

        executor = Executors.newFixedThreadPool(1);
        amazonS3Client = null;

        identifier = prefixIdentifier ? IdentifierUtil.getIdentifier() : null;
    }

    public void uploadFileToS3Async(final String filename, final Date date) {
        uploadFileToS3Async(filename, date, false);
    }

    public void uploadFileToS3Async(final String filename, final Date date, final boolean overrideTimestampSetting) {
        initAmazonS3Client();

        final File file = new File(filename);

        //If file does not exist or if empty, do nothing
        if (!file.exists() || file.length() == 0) {
            return;
        }

        //Build S3 path
        final StringBuffer s3ObjectName = new StringBuffer();
        if (getS3FolderName() != null) {
            s3ObjectName.append(format(getS3FolderName(), date)).append('/');
        }

        //Extra custom S3 (runtime) folder?
        if (CustomData.EXTRA_S3_FOLDER.get() != null) {
            s3ObjectName.append(CustomData.EXTRA_S3_FOLDER.get()).append('/');
        }

        //Add timestamp prefix if desired
        if (prefixTimestamp || overrideTimestampSetting) {
            s3ObjectName.append(new SimpleDateFormat("yyyyMMdd_HHmmss").format(date)).append('_');
        }

        //Add identifier prefix if desired
        if (prefixIdentifier) {
            s3ObjectName.append(identifier).append('_');
        }

        s3ObjectName.append(file.getName());

        // Queue thread to upload
        final Runnable uploader = () -> {
            try {
                final PutObjectRequest putObjectRequest =
                        new PutObjectRequest(getS3BucketName(), s3ObjectName.toString(), file)
                                .withCannedAcl(CannedAccessControlList.BucketOwnerFullControl);

                amazonS3Client.putObject(putObjectRequest);
            } catch (final Exception ex) {
                log.warn("Cannot put request to queue", ex);
            }
        };

        executor.execute(uploader);
    }

    private void initAmazonS3Client() {
        if (amazonS3Client != null) {
            return;
        }

        // If the access and secret key is not specified then try to use other providers
        if (getAwsAccessKey() == null || getAwsAccessKey().isBlank()) {
            amazonS3Client = AmazonS3ClientBuilder.defaultClient();
        } else {
            final AWSCredentials creds = new BasicAWSCredentials(getAwsAccessKey(), getAwsSecretKey());
            amazonS3Client = AmazonS3ClientBuilder
                    .standard()
                    .withCredentials(new AWSStaticCredentialsProvider(creds))
                    .build();
        }
    }

    /**
     * Shutdown hook that gets called when exiting the application.
     */
    @Override
    public void doShutdown() {
        try {

            //Wait until finishing the upload
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }

    private String format(final String s, final Date date) {
        final Pattern pattern = Pattern.compile("%d\\{(.*?)}");
        final Matcher matcher = pattern.matcher(s);

        String result = s;

        while (matcher.find()) {
            final String match = matcher.group(1);

            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(match);
            final String replace = simpleDateFormat.format(date);

            result = s.replace(String.format("%%d{%s}", match), replace);
        }

        return result;
    }

    public String getAwsAccessKey() {
        return awsAccessKey;
    }

    public String getAwsSecretKey() {
        return awsSecretKey;
    }

    public String getS3BucketName() {
        return s3BucketName;
    }

    public String getS3FolderName() {
        return s3FolderName;
    }

    public boolean isPrefixTimestamp() {
        return prefixTimestamp;
    }

    public boolean isPrefixIdentifier() {
        return prefixIdentifier;
    }

    public String getIdentifier() {
        return identifier;
    }

}
