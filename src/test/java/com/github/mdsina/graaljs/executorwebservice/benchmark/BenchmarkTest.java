package com.github.mdsina.graaljs.executorwebservice.benchmark;

import com.github.mdsina.graaljs.executorwebservice.domain.Variable;
import com.github.mdsina.graaljs.executorwebservice.execution.JavaScriptSourceExecutor;
import com.github.mdsina.graaljs.executorwebservice.script.Script;
import com.github.mdsina.graaljs.executorwebservice.script.ScriptStorageService;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    void setup(ScriptStorageService sss, JavaScriptSourceExecutor jsse) {
        scriptStorageService = sss;
        javaScriptSourceExecutor = jsse;
    }

    @Test
    public void executeJmhRunner() throws RunnerException, IOException {
        String relPath = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        File compilerDir = new File(relPath + "../compiler");

        String path = compilerDir.getCanonicalPath();

        Options jmhRunnerOptions = new OptionsBuilder()
            // set the class name regex for benchmarks to search for to the current class
            .include("\\." + this.getClass().getSimpleName() + "\\.")
            .warmupIterations(15)
            .measurementIterations(10)
            // do not use forking or the benchmark methods will not see references stored within its class
            .forks(0)
            // do not use multiple threads
            .threads(1)
            .shouldDoGC(true)
            .addProfiler(GCProfiler.class)
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
        Script script = scriptStorageService.getScript("PERF_1");
        javaScriptSourceExecutor.execute(
            script.getId(),
            script.getBody(),
            List.of(Variable.builder().name("I").value("PERF_TEST").build())
        );
    }
}
