package edu.ncsu.soc.motivator;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.AfterClass;
import org.junit.runners.Parameterized.Parameters;

import edu.ncsu.soc.common.ClassPathSearcher;

import org.javasimon.SimonManager;
import org.javasimon.Stopwatch;

public abstract class JsonUtilsTest {

    private static final long NANOSECONDS_PER_SECOND = 1000000000L;
    
    @SuppressWarnings("resource")
    public static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        printStatistics();
    }
    
    public static Collection<Object[]> testFiles(String fileRegex) {
        Map<String, InputStream> jsonFilesMap = ClassPathSearcher.findFilesInClassPath(fileRegex);
        List<Object[]> parameters = new ArrayList<Object[]>();
        for(Entry<String, InputStream> entry : jsonFilesMap.entrySet()) {
            Object[] parameter = new Object[2];
            parameter[0] = entry.getKey();
            parameter[1] = convertStreamToString(entry.getValue());
            parameters.add(parameter);
        }
        return parameters;
    }
    
    private static void printStatistics() {        
        Collection<String> simonNames = SimonManager.getSimonNames();
        for (String string : simonNames) {
            if (string.length() > 0) {
                Stopwatch stopwatch = SimonManager.getStopwatch(string);
                if (stopwatch.getCounter() != 0L) {
//                    logger.info("JavaSimon Result: {}", stopwatch);       // uncomment for a simple output
                    System.out.println(stopwatch.getName());
                    System.out.println("\tcount: " + stopwatch.getCounter());
                    System.out.println("\tmax  : " + Double.valueOf(stopwatch.getMax())/NANOSECONDS_PER_SECOND);
                    System.out.println("\tmin  : " + Double.valueOf(stopwatch.getMin())/NANOSECONDS_PER_SECOND);
                    System.out.println("\tmu   : " + stopwatch.getMean()/NANOSECONDS_PER_SECOND);
                    System.out.println("\tsigma: " + stopwatch.getStandardDeviation()/NANOSECONDS_PER_SECOND);
                }
            }
        }
    }


}
