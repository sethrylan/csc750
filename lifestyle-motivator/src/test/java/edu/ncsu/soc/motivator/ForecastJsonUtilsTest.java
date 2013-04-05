package edu.ncsu.soc.motivator;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import edu.ncsu.soc.motivator.domain.Forecast;

@RunWith(Parameterized.class)
public class ForecastJsonUtilsTest extends JsonUtilsTest {

    private static final String FILE_REGEX = ".*forecast.*.json";
    
    private String filename;
    private String json;

    public ForecastJsonUtilsTest(String filename, String json) {
        this.filename = filename;
        this.json = json;
    }

    @Parameters
    public static Collection<Object[]> parameters() {
        return testFiles(FILE_REGEX);
    }
    
    @Test
    public void testJson() {
        Forecast forecast = JsonUtils.createFromJson(Forecast.class, this.json);
        assertNotNull(forecast);
        assertTrue(forecast.latitude != 0);
        assertTrue(forecast.longitude != 0);
        assertNotNull(forecast.currently);
    }
    
}
