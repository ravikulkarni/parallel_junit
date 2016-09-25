package com.company;

import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.junit.runner.JUnitCore;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.io.*;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.*;

/**
 *
 *
 */

public class JunitParallelRunner {

  private static String testPlanName = "";
  private static final int MAX_WAIT_TIMES = 30;  //in minutes

  public static class TestClassLoader extends URLClassLoader {
    public TestClassLoader() {
      super(((URLClassLoader) getSystemClassLoader()).getURLs());
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
      if(name.startsWith("com.classes.to.load.in.separate.spaces")) {
        System.out.println("JunitParallelRunner: Loading class " + name + " from TestClassLoader " + this.toString());
        return super.findClass(name);
      }
      return super.loadClass(name);
    }
  }

  private static class TestRunCallable implements Callable <Result>{
    private String testPlanName = "";
    private PrintStream console = null;
    public TestRunCallable(String testPlanName, PrintStream console) {
      this.testPlanName = testPlanName;
      this.console = console;
    }
    public Result call() throws Exception {
      final String oldThreadName = Thread.currentThread().getName();
      Result result = new Result();
      try {
        Thread.currentThread().setName(testPlanName);
        console.println("JunitParallelRunner: Running " + testPlanName);
        TestClassLoader testClassLoader = new TestClassLoader();
        Class testPlanClass = testClassLoader.loadClass(testPlanName);
        result = JUnitCore.runClasses(testPlanClass);
        console.println("JunitParallelRunner: Completed running " + testPlanName);
        return result;
      } catch (Exception e) {
        console.println("JunitParallelRunner: Exception in Call method " + this.testPlanName + " Message:" + e.getMessage());
        StackTraceElement trace[] = e.getStackTrace();
        for(int i=0 ; i<trace.length; i++) {
          console.println("JunitParallelRunner: " + trace[i].toString());
        }
      } finally {
        Thread.currentThread().setName(oldThreadName);
      }
      return result;
    }

  }

  private static Class<?> getFromTestClassloader(Class<?> clazz) throws InitializationError {
    try {
      ClassLoader testClassLoader = new TestClassLoader();
      return Class.forName(clazz.getName(), true, testClassLoader);
    } catch (ClassNotFoundException e) {
      throw new InitializationError(e);
    }
  }

  public static String getTestPlanName() {
    return testPlanName;
  }

  private static ArrayList<String> getTestPlans() {
    String fileName = "listOfTests.txt";
    final ArrayList<String> testPlans = new ArrayList<String>();
    try {
      BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
      String line = "";
      while ((line = bufferedReader.readLine()) != null) {
        testPlans.add(line);
      }
    } catch(FileNotFoundException e) {
      System.out.println("File containing list of test cases not found");
    } catch(IOException e) {
      System.out.println("Exception when reading file contents.");
    }
    return testPlans;
  }

  public static void main(String args[]) {
    System.out.println("############# MAIN ###############");
    testPlanName = args[0];
    boolean errorFlag = false;

    PrintStream console = System.out;  //Use this later for printing on console which is the default PrintStream.

    int remainingResults = 0;
    int runCount = 0;
    int failureCount = 0;

    ArrayList<String> testPlans = getTestPlans();

    JunitParallelRunnerPrintStream JunitParallelRunnerPrintStream = new JunitParallelRunnerPrintStream();
    System.setOut(JunitParallelRunnerPrintStream);

    remainingResults = testPlans.size();
    Future<Result> futures[] = new Future[testPlans.size()];

    int maxThreads = 10;
    try {
      maxThreads = Integer.parseInt(System.getProperty("-maxThreads"));
    } catch (Exception e) {

    }
    console.println("Starting " + maxThreads + " tests in parallel");

    ExecutorService executorService = Executors.newFixedThreadPool(maxThreads);
    //ExecutorService executorService = new ThreadPoolExecutor(50,50,60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
      CompletionService completionService = new ExecutorCompletionService(executorService);
    console.println("######## JunitParallelRunner: Completion Service created.");
    for(int i=0;i<testPlans.size(); i++) {
      //futures[i] = executorService.submit(new TestRunCallable(testPlans.get(i), console)); //Print the start and end of the test run on console.
      futures[i] = completionService.submit(new TestRunCallable(testPlans.get(i), console)); //Print the start and end of the test run on console.
    }


    List<String> failedTests = new ArrayList<String>();

    Future<Result> completedFuture;
    console.println("######## JunitParallelRunner: Number of testplans  : " + remainingResults);
    while (remainingResults > 0) {
      try {
        //Wait for max 1 hour
        completedFuture = completionService.poll(MAX_WAIT_TIMES, TimeUnit.MINUTES);
        if(completedFuture == null) {
          console.println("######## JunitParallelRunner: All tests did not complete running in " + MAX_WAIT_TIMES + " minutes. Cancelling remaining runs");
          //Cancel all the futures
          for(Future f: futures) {
            if(f != null && !f.isDone()) {
              f.cancel(true);
            }

          }
          remainingResults = -1;
        } else {
          Result result = completedFuture.get();
          runCount = runCount + result.getRunCount();
          remainingResults--;
          console.println("######## JunitParallelRunner: Test Plans remaining : " + remainingResults);

          if (result.getFailureCount() > 0) {
            failureCount = failureCount + result.getFailureCount();
            List<Failure> failures = result.getFailures();
            for (Failure f : failures) {
              failedTests.add(f.getDescription().toString());
            }
          }
        }

      } catch (InterruptedException e) {
        console.println("Interrupted Exception when processing results");
        errorFlag = true;
      } catch (ExecutionException e) {
        console.println("Interrupted Exception when processing results");
        errorFlag = true;
      }

    }

    //All futures processed.
    console.println("######## JunitParallelRunner: Processing the Results");
    console.println("######## JunitParallelRunner: Tests run count    : " + runCount);
    console.println("######## JunitParallelRunner: Failed tests count : " + failureCount);
    console.println("######## JunitParallelRunner: Failed tests       : " + failedTests.toString());

    //Signal a shutdown
    if(remainingResults == 0) {
      console.println("Test Results from all test plans processed.");
      executorService.shutdownNow();
    }

    if(remainingResults == -1) {
      console.println("Test did not complete in " + MAX_WAIT_TIMES + " minutes.");
      executorService.shutdownNow();
      errorFlag = true;
    }

    if(errorFlag || failureCount > 0) {
      System.exit(-1);
    }


  }
}
