# parallel_junit

This code is implemented to enable running the legacy Junit Tests in parallel. The legacy tests may or may not have static / singleton variables in it. It may not be possible to refactor all of the legacy tests to remove the use of statics / singleton

## How this works.
* Every Junit tests are run in different threads.
* The number of tests to run in parallel is configured through command line parameter "maxThreads"
* The runner reads a file containing the list of the test cases to run with their full class names
* The JunitParallelRunner uses a custom class loader (TestClassLoader) to load the legacy Junit test classes.
* Every thread does the following
    - Creates a new class loader and loads the test class.
    - Calls the JunitCore to run the tests.
* The JunitParallelRunnerPrintStream captures the console output from individual threads into their respective files.
