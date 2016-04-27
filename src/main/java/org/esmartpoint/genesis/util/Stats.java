package org.esmartpoint.genesis.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public class Stats {
	
	static DateTime start;
	static DateTime last;
	static long operationsCounter;
	static long lastOperationCounter;
	static long operationsBufferSize;

	static DecimalFormat longIntegerFormat = (DecimalFormat)NumberFormat.getNumberInstance(new Locale("es", "es"));
	static PeriodFormatter formatter = new PeriodFormatterBuilder()
	    .appendDays().appendSuffix(" days ")
	    .appendHours().appendSuffix(" hours ")
	    .appendMinutes().appendSuffix(" minutes ")
	    .appendSeconds().appendSuffix(" seconds ")
	    .appendWeeks().appendSuffix(" weeks ")
	    .printZeroNever()
	    .toFormatter();

	static public void init() {
    	start = DateTime.now();
    	last = start;
    	operationsCounter = 0;
    	lastOperationCounter = 0;
    	operationsBufferSize = 1000;
	}

	static public boolean iterate() {
		operationsCounter++;
		lastOperationCounter++;
		if (operationsCounter % operationsBufferSize == 0) {
			return true;
		}
		return false;
	}
	
	static public void printSpeed() {
		if (operationsCounter % operationsBufferSize == 0) {
			DateTime span = DateTime.now().minus(start.getMillis());
			DateTime lastSpan = DateTime.now().minus(last.getMillis());
			long diff = span.getMillis();
			long diffSpan = lastSpan.getMillis(); 
			Period period = new Period(start, DateTime.now());

			System.out.println("Inserted:" + longIntegerFormat.format(operationsCounter) + " items in: " + formatter.print(period));
			System.out.println("Overall speed: " + (long)(operationsCounter / (diff / 1000.0)) 
				+ " opers/sec current speed: " + (long)(lastOperationCounter / (diffSpan / 1000.0)) + " opers/sec");

			/* Restart */
			last = DateTime.now();
			lastOperationCounter = 0;
		}
	}
	
}
