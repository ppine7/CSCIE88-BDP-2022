package cscie88.week2;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;


public class LogLineParser {

	public static DateTimeFormatter hourDateTimeFormatter = DateTimeFormatter
			.ofPattern("yyyyMMdd_HH")
			.withZone(ZoneOffset.UTC)
			.withLocale(Locale.US);

	/**
	 * expected format of a log line:
	 * <uuid> <timestamp> <url> <userId> <country> <ua_browser> <ua_os> <response_status> <TTFB>
	 * example:
	 * 20ac3311c7a64d7a8675e5c1c776857b,2018-09-12T00:03:53.280Z,http://example.com/?url=114,user-038,RS,Firefox,Android,413,0.2393
	 * with each field separated by ',' character
	 * 
	 * @param logLine
	 */
	public static LogLine parseLine(String logLine) {
		if (StringUtils.isEmpty(logLine))
			return null;
		String [] tokens = logLine.split(",");	
		if (tokens.length < 9) {
			// ignore incorrectly formatted lines
			return null;
		}
		Instant eventInstant = Instant.parse(tokens[1]);
        //get date time only
        ZonedDateTime eventDateTime = ZonedDateTime.ofInstant(eventInstant, ZoneId.of(ZoneOffset.UTC.getId()));
        // convert integer and float values
        int responseCode = Integer.parseInt(tokens[7]);
        float ttfb = Float.parseFloat(tokens[8])
;        return new LogLine(tokens[0], eventDateTime, tokens[2], tokens[3], tokens[4],
        		tokens[5], tokens[6], responseCode, ttfb);
	}
	
	public static void main(String[] args) {
		// test parsing
		String logLineStr = "20ac3311c7a64d7a8675e5c1c776857b,2018-09-12T00:03:53.280Z,http://example.com/?url=114,user-038,RS,Firefox,Android,413,0.2393";
		LogLine logLine = LogLineParser.parseLine(logLineStr);
		System.out.println("Test parsed log line:\n" + logLine);
	}

}
