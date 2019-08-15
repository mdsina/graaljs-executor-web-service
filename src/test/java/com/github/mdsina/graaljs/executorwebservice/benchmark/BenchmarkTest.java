package com.github.mdsina.graaljs.executorwebservice.benchmark;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mdsina.graaljs.executorwebservice.bindings.BindingsProviderFactory;
import com.github.mdsina.graaljs.executorwebservice.domain.Variable;
import com.github.mdsina.graaljs.executorwebservice.dto.ScriptDto;
import com.github.mdsina.graaljs.executorwebservice.execution.ContextWrapper;
import com.github.mdsina.graaljs.executorwebservice.execution.JavaScriptSourceExecutor;
import com.github.mdsina.graaljs.executorwebservice.exports.QueryString;
import com.github.mdsina.graaljs.executorwebservice.modules.QueryStringModule;
import com.github.mdsina.graaljs.executorwebservice.service.ScriptStorageService;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class BenchmarkTest {

    private static ScriptStorageService scriptStorageService;
    private static JavaScriptSourceExecutor javaScriptSourceExecutor;

    static {
        Engine engine = Engine.create();
        HostAccess build = HostAccess.newBuilder(HostAccess.ALL)
            // https://github.com/graalvm/graaljs/issues/165#issuecomment-493926312
            .targetTypeMapping(Long.class, Object.class, null, v -> v)
            // converted to BigDecimal because do not need to handle representation conversion from scientific
            .targetTypeMapping(Double.class, Object.class, null, BigDecimal::valueOf)
            .targetTypeMapping(List.class, Object.class, null, v -> v)
            .build();

        BasePooledObjectFactory<ContextWrapper> factory = new BasePooledObjectFactory<>() {
            @Override
            public ContextWrapper create() {
                Context context = Context.newBuilder("js")
                    .allowAllAccess(true)
                    .allowHostAccess(
                        build
                    )
                    .engine(engine)
                    .build();

                return new ContextWrapper(context);
            }

            @Override
            public PooledObject<ContextWrapper> wrap(ContextWrapper obj) {
                return new DefaultPooledObject<>(obj);
            }
        };


        GenericObjectPoolConfig<ContextWrapper> config = new GenericObjectPoolConfig<>();

        config.setMaxTotal(10);
        config.setMaxIdle(10);
        config.setMinIdle(10);
        config.setJmxEnabled(false); // TODO: configure jmx into pool

        GenericObjectPool<ContextWrapper> pool = new GenericObjectPool<>(factory, config);
        try {
            pool.preparePool();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }


        scriptStorageService = new ScriptStorageService();
        javaScriptSourceExecutor = new JavaScriptSourceExecutor(
            pool,
            new BindingsProviderFactory(new QueryStringModule(new QueryString())),
            new ObjectMapper()
        );
    }

    @Test
    public void executeJmhRunner() throws RunnerException, IOException {
        String relPath = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        File compilerDir = new File(relPath + "../compiler");

        String path = compilerDir.getCanonicalPath();

        Options jmhRunnerOptions = new OptionsBuilder()
            // set the class name regex for benchmarks to search for to the current class
            .include("\\." + this.getClass().getSimpleName() + "\\.")
            .warmupIterations(10)
            .measurementIterations(10)
            .forks(1)
            .addProfiler(GCProfiler.class)
            .shouldDoGC(true)
            .warmupTime(TimeValue.seconds(1))
            .measurementTime(TimeValue.seconds(1))
            .shouldFailOnError(true)
            .resultFormat(ResultFormatType.JSON)
//            .result("/dev/null") // set this to a valid filename if you want reports
            .jvmArgs(
                "-XX:+UnlockExperimentalVMOptions",
                "-XX:+EnableJVMCI",
                "-XX:+UseJVMCICompiler",
                "--module-path=" + path + File.separator + "graal-sdk.jar" + File.pathSeparator
                    + path + File.separator + "truffle-api.jar",
                "--upgrade-module-path=" + path + File.separator + "compiler.jar ",
                "-server"
            )
            .build();

        new Runner(jmhRunnerOptions).run();
    }

    @Benchmark
    public void someBenchmarkMethod() throws Exception {
        ScriptDto script = scriptStorageService.getScript("PERF_1");
        javaScriptSourceExecutor.execute(
            script,
            List.of(new Variable("I", "PERF_TEST"))
        );
    }
}
