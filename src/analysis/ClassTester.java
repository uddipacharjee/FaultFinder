package analysis;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.runtime.RuntimeData;
//import org.junit.runner.Result;
//import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

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
		InputStream inputSTream=getTargetClass(targetName);
		final IRuntime runtime = new LoggerRuntime();
		final Instrumenter instr = new Instrumenter(runtime);
		//final byte[] instrumented = instr.instrument(getTargetClass(targetName), "");
		final byte[] instrumented = instr.instrument(inputSTream, "");
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
	public void collect(final ExecutionDataStore executionData, boolean currentTestPassed,String targetName) throws IOException {
		final CoverageBuilder coverageBuilder = new CoverageBuilder();
		Analyzer analyzer = new Analyzer(executionData, coverageBuilder);

		try {
			//analyzer.analyzeAll(classesDir);
			analyzer.analyzeClass(getTargetClass(targetName), targetName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		BufferedWriter bw = new BufferedWriter(new FileWriter("matrix"));
		BufferedWriter bwl = new BufferedWriter(new FileWriter("matLine"));
		
		for (final IClassCoverage cc : coverageBuilder.getClasses())
		{
			for (int i = cc.getFirstLine(); i <= cc.getLastLine(); i++) {
				
				if(getColor(cc.getLine(i).getStatus())=="green") {
					//System.out.print("("+i+")"+" ");
				    bw.write("1");
				}
				else {
					//System.out.print("["+i+"]"+" ");
					bw.write("0");
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
				//System.out.print("+");
				bw.write("+");
			}
			else {
				//System.out.print("-");
				bw.write("-");
			}
			//System.out.println();
			bw.newLine();
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
		//System.out.println(nMethod);
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
//		ca.targetName = Max.class.getName();
//		ca.junitName=MaxTest.class.getName();
//		ca.test(MaxTest.class);
//		ca.targetName = Triangle.class.getName();
//		ca.junitName=TriangleTest.class.getName();
//		ca.test(TriangleTest.class);
		
		//File file = new File("resources\\TriangleProject\\bin\\"); 
		//File file = new File("C:\\Users\\Hp\\eclipse-workspace\\TriangleProject\\bin");
        //convert the file to URL format
		//URL url = file.toURI().toURL();
		BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Hp\\eclipse-workspace\\FaultFinder\\bin\\names.txt"));
		
		String className=br.readLine(); //"triangle.Triangle"
		String testClassName=br.readLine(); //"triangle.TriangleTest"
		//String className="triangle.Triangle";
		//String testClassName="triangle.TriangleTest";
		
		//System.out.println(className+" "+testClassName);
		URL url=null;
		URL[] urls = new URL[]{url}; 
		        //load this folder into Class loader

		ClassLoader cl = new URLClassLoader(urls);
		        //load the Address class in 'c:\\other_classes\\'
		
		final Class<?>  cls = cl.loadClass(className);
		ca.targetName=cls.getName();
		final Class<?>  clsTest = cl.loadClass(testClassName);
		ca.junitName=clsTest.getName();
		//System.out.println(ca.targetName+" "+ca.junitName+" "+clsTest);
		//System.out.println(Calculator.class.getName()+" "+CalculatorTest.class.getName()+" "+CalculatorTest.class);
		InputStream inputSTream=ca.getTargetClass(ca.targetName);
		//System.out.println(inputSTream);
		ca.test(clsTest);


		/*
		 * ca.targetName = Calculator.class.getName();
		 * ca.junitName=CalculatorTest.class.getName(); ca.test(CalculatorTest.class);
		 */	
		//System.out.println("Passed= "+ca.totalPassed+"\nFailed= "+ca.totalFailed);
		//System.out.println(Arrays.asList(ca.testRequirements));
		//ca.print();
		HeuristicCalculator hc=new HeuristicCalculator(new TarantulaHeuristic(), ca.testRequirements.values());
		ArrayList<TestRequirement> rank=hc.calculateRank();
		for (TestRequirement testRequirement : rank) {
			System.out.print(testRequirement.toString());
			
		}
		
	}
}