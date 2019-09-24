package analysis;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.junit.runner.JUnitCore;
//import org.junit.runner.Result;
//import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import p1.Calculator;
import p1.CalculatorTest;

import org.jacoco.core.runtime.RuntimeData;
import org.jacoco.core.data.SessionInfoStore;
/*
 * import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.stream.FileImageInputStream;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.runtime.RuntimeData;
import org.junit.runner.JUnitCore;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
 * 
 * 
 */

public class ClassAnalyzer {
	/**
	 * A class loader that loads classes from in-memory data.
	 */
	public static class MemoryClassLoader extends ClassLoader
	{
		private final Map<String, byte[]> definitions = new HashMap<String, byte[]>();
		/**
		 * Add a in-memory representation of a class.
		 * 
		 * @param name name of the class
		 * @param bytes class definition
		 */
		public void addDefinition(final String name, final byte[] bytes) {
			definitions.put(name, bytes);
		}
		@Override
		protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException
		{
			final byte[] bytes = definitions.get(name);
			if (bytes != null)
				return defineClass(name, bytes, 0, bytes.length);
			return super.loadClass(name, resolve);
		}
	}
	private InputStream getTargetClass(final String name)
	{
		//System.out.println(name);
		final String resource = '/' + name.replace('.', '/') + ".class";
		//System.out.println(resource);
		//System.out.println(getClass().getResourceAsStream(resource));
		return getClass().getResourceAsStream(resource);
	}
	private void printCounter(final String unit, final ICounter counter)
	{
		final Integer missed    = Integer.valueOf(counter.getMissedCount());
		final Integer total     = Integer.valueOf(counter.getTotalCount());

		System.out.printf("%s of %s %s missed%n", missed, total, unit);
	}
	private String getColor(final int status)
	{
		switch (status) {
		case ICounter.NOT_COVERED:
			return "red";
		case ICounter.PARTLY_COVERED:
			return "yellow";
		case ICounter.FULLY_COVERED:
			return "green";
		}
		return "";
	}
	private Result runTest(final Class<?> testClazz, final String methodName)
            throws InitializationError {
        BlockJUnit4ClassRunner runner = new BlockJUnit4ClassRunner(testClazz) {
            @Override
            protected List<FrameworkMethod> computeTestMethods() {
                try {
                    Method method = testClazz.getMethod(methodName);
                    return Arrays.asList(new FrameworkMethod(method));

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        Result res = new Result();
        runner.run(res);
        return res;
    }
///*	private void analyze() throws Exception
//	{
//		final String targetName = Calculator.class.getName();
//		//System.out.println(targetName);
//		// For instrumentation and runtime we need a IRuntime instance to collect execution data:
//		final IRuntime runtime = new LoggerRuntime();
//		// The Instrumenter creates a modified version of our test target class that contains additional probes for execution data recording:
//		final Instrumenter instr = new Instrumenter(runtime);
//		final byte[] instrumented = instr.instrument(getTargetClass(targetName), "");
//		// Now we're ready to run our instrumented class and need to startup the runtime first:
//		final RuntimeData data = new RuntimeData();
//		runtime.startup(data);
//		// In this tutorial we use a special class loader to directly load the instrumented class definition from a byte[] instances.
//		final MemoryClassLoader memoryClassLoader = new MemoryClassLoader();
//		memoryClassLoader.addDefinition(targetName, instrumented);
//		final Class<?> targetClass = memoryClassLoader.loadClass(targetName);
//		// Here we execute our test target class through its Runnable interface:
//		/*final Runnable targetInstance = (Runnable) targetClass.newInstance();
//        targetInstance.run();*/
//
//		String junitName = CalculatorTest.class.getName();
//		memoryClassLoader.addDefinition(junitName, instr.instrument(getTargetClass(junitName), ""));
//		final Class<?> junitClass = memoryClassLoader.loadClass(junitName);
//		//JUnitCore junit = new JUnitCore();
//		//Result result = junit.run(junitClass);
//		Result result=runTest(junitClass, "testAdd");
////		Result result=runTest(junitClass,"testAdd");
////        System.err.println(result.getFailureCount());
//		System.out.println(result.isOK());
//		
//		//System.out.println("Failure count: " + result.getFailureCount());
//
//		// At the end of test execution we collect execution data and shutdown the runtime:
//		final ExecutionDataStore executionData = new ExecutionDataStore();
//		data.collect(executionData, new SessionInfoStore(), false);
//		runtime.shutdown();
//		// Together with the original class definition we can calculate coverage information:
//		collect(executionData, result.isOK(), targetName);
//		/*	final CoverageBuilder coverageBuilder = new CoverageBuilder();
//		final Analyzer analyzer = new Analyzer(executionData, coverageBuilder);
//		analyzer.analyzeClass(getTargetClass(targetName), targetName);
//		// Let's dump some metrics and line coverage information:
//		for (final IClassCoverage cc : coverageBuilder.getClasses())
//		{
//			System.out.printf("Coverage of class %s%n", cc.getName());
//			printCounter("instructions", cc.getInstructionCounter());
//			printCounter("branches", cc.getBranchCounter());
//			printCounter("lines", cc.getLineCounter());
//			printCounter("methods", cc.getMethodCounter());
//			printCounter("complexity", cc.getComplexityCounter());
//			for (int i = cc.getFirstLine(); i <= cc.getLastLine(); i++) {
//				System.out.printf("Line %s: %s%n", Integer.valueOf(i), getColor(cc.getLine(i).getStatus()));
//			}
//		}
//		*/
//	}
//	*/
	public void test(String methodName) throws Exception {
		final String targetName = Calculator.class.getName();
		
		final IRuntime runtime = new LoggerRuntime();
		final Instrumenter instr = new Instrumenter(runtime);
		final byte[] instrumented = instr.instrument(getTargetClass(targetName), "");
		final RuntimeData data = new RuntimeData();
		runtime.startup(data);
		
		final MemoryClassLoader memoryClassLoader = new MemoryClassLoader();
		memoryClassLoader.addDefinition(targetName, instrumented);
		final Class<?> targetClass = memoryClassLoader.loadClass(targetName);
		
		String junitName = CalculatorTest.class.getName();
		memoryClassLoader.addDefinition(junitName, instr.instrument(getTargetClass(junitName), ""));
		final Class<?> junitClass = memoryClassLoader.loadClass(junitName);
		Result result=runTest(junitClass, methodName);
		boolean currentTestPassed=result.isOK();
		System.out.println(methodName+" "+currentTestPassed);
		final ExecutionDataStore executionData = new ExecutionDataStore();
		data.collect(executionData, new SessionInfoStore(), false);
		runtime.shutdown();
		
		collect(executionData, currentTestPassed, targetName);
	}
	public void collect(final ExecutionDataStore executionData, boolean currentTestFailed,String targetName) {
		final CoverageBuilder coverageBuilder = new CoverageBuilder();
		Analyzer analyzer = new Analyzer(executionData, coverageBuilder);

		try {
			//analyzer.analyzeAll(classesDir);
			analyzer.analyzeClass(getTargetClass(targetName), targetName);
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (final IClassCoverage cc : coverageBuilder.getClasses())
		{
		/*	System.out.printf("Coverage of class %s%n", cc.getName());
			printCounter("instructions", cc.getInstructionCounter());
			printCounter("branches", cc.getBranchCounter());
			printCounter("lines", cc.getLineCounter());
			printCounter("methods", cc.getMethodCounter());
			printCounter("complexity", cc.getComplexityCounter());
		*/	
			for (int i = cc.getFirstLine(); i <= cc.getLastLine(); i++) {
				System.out.printf("Line %s: %s%n", Integer.valueOf(i), getColor(cc.getLine(i).getStatus()));
			}
		}
		
	}
	public void analyze() throws Exception {
		Class classobj = CalculatorTest.class;
		Method[] methods = classobj.getDeclaredMethods(); 
		int nMethod=0;
		for (Method method : methods) { 
            String MethodName = method.getName(); 
            //System.out.println("Name of the method: "+ MethodName); 
            nMethod++;
            test(MethodName);
        } 
		System.out.println(nMethod);
	}
	public static void main(final String[] args) throws Exception{
		ClassAnalyzer ca=new ClassAnalyzer();
		
		ca.analyze();
	}
}
