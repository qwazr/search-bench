/*
 * Copyright 2017 Emmanuel Keller / QWAZR
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.search.bench.test;

import com.qwazr.search.bench.TtlLineReader;
import com.qwazr.search.bench.TtlLoader;
import com.qwazr.utils.FileUtils;
import com.qwazr.utils.LoggerUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Logger;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public abstract class BaseTest implements Function<TtlLineReader, Boolean> {

    @Parameterized.Parameter
    public Boolean warmup;

    public static final String SCHEMA_NAME = "schemaTest";

    public static final String INDEX_NAME = "indexTest";

    static Logger LOGGER = LoggerUtils.getLogger(BaseTest.class);

    protected static TestSettings currentSettings;
    protected static ExecutorService executor;
    protected static Path schemaDirectory;

    public static void before(final TestSettings settings) throws Exception {

        Thread.sleep(20000);

        // Download the DBPedia file
        if (!settings.ttlFile.exists()) {
            settings.ttlFile.getParentFile().mkdir();
            try (final InputStream input = new URL(settings.ttlUrl).openStream()) {
                try (ReadableByteChannel rbc = Channels.newChannel(input)) {
                    try (final FileOutputStream fos = new FileOutputStream(settings.ttlFile)) {
                        try (final FileChannel fileChannel = fos.getChannel()) {
                            fileChannel.transferFrom(rbc, 0, Long.MAX_VALUE);
                        }
                    }
                }
            }
        }

        currentSettings = settings;
        schemaDirectory = settings.schemaDirectory;
        executor = currentSettings.executor ? Executors.newCachedThreadPool() : null;

        if (!Files.exists(schemaDirectory))
            Files.createDirectory(schemaDirectory);
    }

    @AfterClass
    public static void after() throws InterruptedException {
        if (executor != null) {
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.MILLISECONDS);
            executor.shutdownNow();
        }
        System.gc();
        currentSettings = null;
    }

    protected final TtlLoader loader;
    private final int limit;

    BaseTest() {
        this.loader = new TtlLoader(currentSettings.ttlFile);
        this.limit = currentSettings.limit;
    }

    private static long count;

    protected void preTest() throws ExecutionException, InterruptedException {
    }

    @Test
    public void test100() throws IOException, ExecutionException, InterruptedException {
        preTest();
        LOGGER.info("START TEST " + currentSettings.toString());
        LOGGER.info("INDEX DIR: " + schemaDirectory);
        long time = System.currentTimeMillis();
        count = loader.load(limit, this);
        flush();
        time = System.currentTimeMillis() - time;
        final int rate = (int) ((count * 1000) / time);
        if (!warmup) {
            LOGGER.info("###");
            LOGGER.info("### " + getClass().getName());
            LOGGER.info("Rate: " + rate);
            LOGGER.info(count + " lines indexed");
            if (currentSettings.results != null)
                currentSettings.results.add(this, rate);
        }
        postTest();
        LOGGER.info("END TEST");
    }

    abstract void flush();

    abstract long getNumDocs() throws IOException;

    protected void postTest() throws IOException {
    }

    public void postCheck() throws IOException {
        Assert.assertEquals(count, getNumDocs());
    }

    @After
    public void endCheck() throws IOException {
        final Path rootPath = schemaDirectory.resolve(BaseTest.SCHEMA_NAME).resolve(currentSettings.indexes[0].index);
        long size = FileUtils.sizeOf(rootPath.resolve("data").toFile());
        if (currentSettings.indexes[0].taxonomy) {
            Path taxoPath = rootPath.resolve("taxonomy");
            size += FileUtils.sizeOf(taxoPath.toFile());
        }
        postCheck();
        if (!warmup)
            LOGGER.info("Index size: " + FileUtils.byteCountToDisplaySize(size));
    }

}
