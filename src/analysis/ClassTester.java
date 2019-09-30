package analysis;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import p3.Max;
import p3.MaxTest;
import triangle.Triangle;
import triangle.TriangleTest;

import org.jacoco.core.runtime.RuntimeData;
import org.jacoco.core.data.SessionInfoStore;

public class ClassTester {
	/**
	 * A class loader that loads classes from in-memory data.
	 */
	String targetName;
	String junitName;
	int totalPassed;
	int totalFailed;
	private HashMap<Integer, TestRequirement> testRequirements = new HashMap<Integer, TestRequirement>();
	
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
/*	
	private void printCounter(final String unit, final ICounter counter)
	{
		final Integer missed    = Integer.valueOf(counter.getMissedCount());
		final Integer total     = Integer.valueOf(counter.getTotalCount());

		System.out.printf("%s of %s %s missed%n", missed, total, unit);
	}
*/
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
	public void analyze(String methodName) throws Exception {
		//targetName = Calculator.class.getName();
		//targetName = Max.class.getName();
		
		final IRuntime runtime = new LoggerRuntime();
		final Instrumenter instr = new Instrumenter(runtime);
		final byte[] instrumented = instr.instrument(getTargetClass(targetName), "");
		final RuntimeData data = new RuntimeData();
		runtime.startup(data);
		
		final MemoryClassLoader memoryClassLoader = new MemoryClassLoader();
		memoryClassLoader.addDefinition(targetName, instrumented);
		final Class<?> targetClass = memoryClassLoader.loadClass(targetName);
		
		//junitName = CalculatorTest.class.getName();
		//junitName = MaxTest.class.getName();
		memoryClassLoader.addDefinition(junitName, instr.instrument(getTargetClass(junitName), ""));
		final Class<?> junitClass = memoryClassLoader.loadClass(junitName);
		Result result=runTest(junitClass, methodName);
		boolean currentTestPassed=result.isOK();
		
		if(currentTestPassed) totalPassed++;
		else totalFailed++;
		//System.out.println(methodName+" "+currentTestPassed);
		final ExecutionDataStore executionData = new ExecutionDataStore();
		data.collect(executionData, new SessionInfoStore(), false);
		runtime.shutdown();
		
		collect(executionData, currentTestPassed, targetName);
	}
	public void collect(final ExecutionDataStore executionData, boolean currentTestPassed,String targetName) {
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
			for (int i = cc.getFirstLine(); i <= cc.getLastLine(); i++) {
				
				if(getColor(cc.getLine(i).getStatus())=="green") {
					System.out.print(1+" ");
				}
				else {
					System.out.print(0+" ");
				}
				//System.out.printf("Line %s: %s%n", Integer.valueOf(i), getColor(cc.getLine(i).getStatus()));
				
				//System.out.println();
				HitCounter hc=null;
				if(getColor(cc.getLine(i).getStatus())=="green" && currentTestPassed) {
					hc=HitCounter.ExecutedBYPassedTest;
				}
				else if(getColor(cc.getLine(i).getStatus())!="green" && currentTestPassed) {
					hc=HitCounter.NotExecutedByPassedTest;
				}
				else if(getColor(cc.getLine(i).getStatus())=="green" && !currentTestPassed) {
					hc=HitCounter.ExecutedByFailedTest;
				}
				else {
					hc=HitCounter.NotExecutedByFailedTes;
				}
				
				updateRequirement(targetName, i, hc);
			}
			if(currentTestPassed) {
				System.out.print("+");
			}
			else {
				System.out.print("-");
			}
			System.out.println();
		}
	}
	private void updateRequirement(String className, int lineNumber, HitCounter hitresult) {
		TestRequirement testRequirement = new TestRequirement(className, lineNumber);
		TestRequirement foundRequirement = testRequirements.get(testRequirement.hashCode());

		if (foundRequirement == null) {
			testRequirements.put(testRequirement.hashCode(), testRequirement);
		} else {
			testRequirement = foundRequirement;
		}

		if (hitresult==HitCounter.ExecutedBYPassedTest) {
			testRequirement.increase_cep();
		} else if(hitresult==HitCounter.ExecutedByFailedTest){
			testRequirement.increase_cef();
		}
		else if(hitresult==HitCounter.NotExecutedByPassedTest) {
			testRequirement.increase_cnp();
		}
		else testRequirement.increase_cnf();
		//System.out.println("class name: "+targetName+" Line number: "+testRequirement.getLineNumber()+" cef: "+testRequirement.getCef()+" cep: "+testRequirement.getCep()+" cnp "+testRequirement.getCnp()+" cnf "+testRequirement.getCnf());
	}
	public void test(Class classobj) throws Exception {
		//Class classobj = CalculatorTest.class;
		Method[] methods = classobj.getDeclaredMethods(); 
		int nMethod=0;
		for (Method method : methods) { 
            String MethodName = method.getName(); 
            //System.out.println("Name of the method: "+ MethodName); 
            nMethod++;
            analyze(MethodName);
        } 
		System.out.println(nMethod);
	}
	public void generateMatrix() {		
	}
	public void print() {
		for (Entry<Integer, TestRequirement> entry : testRequirements.entrySet()) {
		    System.out.println(entry.getValue());
		}
	}
	public static void main(final String[] args) throws Exception{
		ClassTester ca=new ClassTester();
		//ca.targetName = Max.class.getName();
		//ca.junitName=MaxTest.class.getName();
		//ca.analyze(MaxTest.class);
		ca.targetName = Triangle.class.getName();
		ca.junitName=TriangleTest.class.getName();
		ca.test(TriangleTest.class);
		System.out.println("Passed= "+ca.totalPassed+"\nFailed= "+ca.totalFailed);
		//System.out.println(Arrays.asList(ca.testRequirements));
		//ca.print();
		HeuristicCalculator hc=new HeuristicCalculator(new TarantulaHeuristic(), ca.testRequirements.values());
		ArrayList<TestRequirement> rank=hc.calculateRank();
		for (TestRequirement testRequirement : rank) {
			System.out.println(testRequirement.toString());
		}
	}
}