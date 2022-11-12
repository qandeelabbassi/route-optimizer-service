package tech.codeclever.util;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.codeclever.structure.Data;
import tech.codeclever.structure.Route;
import tech.codeclever.structure.Solution;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class WriteData {
    private static final Logger logger = LoggerFactory.getLogger(WriteData.class);
    public static class Path {
        String name;
        int from;
        int to;
    }
    public static void write(Solution s, Data data) throws IOException {
        s.getDist();
        if (!s.check_feasible()) {
            System.out.println(data.name + " wrong! infeasible solution occured");
            return;
        }

        ArrayList<Path> paths = new ArrayList<>();
        for (Route route : s.routes) {
            route.tasks.forEach(t -> {
                Path p = new Path();
                p.from = t.from;
                p.to = t.to;
                p.name = t.type.name();
                paths.add(p);
            });
        }
        String output = new Gson().toJson(paths);

        AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
        String dstBucket =  "mcgrp-graphs-out";
        String dstKey = "optimized-" + data.name + (int) MyParameter.running_time + ".txt";

        // Uploading to S3 destination bucket
        logger.info("Writing to: " + dstBucket + "/" + dstKey);
        try {
            InputStream is = new ByteArrayInputStream(output.getBytes());
            // Set Content-Length and Content-Type
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(output.getBytes().length);
            meta.setContentType("text/plain");
            s3Client.putObject(dstBucket, dstKey, is, meta);
        } catch (AmazonServiceException e) {
            logger.error(e.getErrorMessage());
        }
    }
}
