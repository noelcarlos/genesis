package org.esmartpoint.genesis.helpers;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.codehaus.jettison.json.JSONObject;
import org.esmartpoint.dbutil.Cronometro;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@Component
public class HttpHelper {
	private final String USER_AGENT = "Mozilla/5.0";

	HttpClient client;
	
	static DateTime start;
	static DateTime last;
	static long operationsCounter;
	static long lastOperationCounter;
	static long operationsBufferSize;

	DecimalFormat longIntegerFormat = (DecimalFormat)NumberFormat.getNumberInstance(new Locale("es", "es"));
	PeriodFormatter formatter = new PeriodFormatterBuilder()
	    .appendDays().appendSuffix(" days ")
	    .appendHours().appendSuffix(" hours ")
	    .appendMinutes().appendSuffix(" minutes ")
	    .appendSeconds().appendSuffix(" seconds ")
	    .appendWeeks().appendSuffix(" weeks ")
	    .printZeroNever()
	    .toFormatter();
	
	public void openSession() {
		client = HttpClientBuilder.create().build();
		
		start = DateTime.now();
    	last = start;
    	operationsCounter = 0;
    	lastOperationCounter = 0;
    	operationsBufferSize = 1000;
	}
	
	public void closeSession() {
		client.getConnectionManager().shutdown();
	}

	public Object post(String url, String body, String resultType) throws Exception {
		Cronometro.start("ELASTICSEARCH");
		HttpPost request = new HttpPost(url);
	 
		// add request header
		request.addHeader("User-Agent", USER_AGENT);
		try {
			request.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
			HttpResponse response = client.execute(request);
			ByteArrayOutputStream sw = new ByteArrayOutputStream(32*1024);
			IOUtils.copy(response.getEntity().getContent(), sw, 32*1024);
			
			operationsCounter++;
			lastOperationCounter++;
			if (operationsCounter % operationsBufferSize == 0) {
				DateTime span = DateTime.now().minus(start.getMillis());
				DateTime lastSpan = DateTime.now().minus(last.getMillis());
				long diff = span.getMillis();
				long diffSpan = lastSpan.getMillis(); 
				Period period = new Period(start, DateTime.now());

				System.out.println("Inserted:" + longIntegerFormat.format(operationsCounter) + " items in: " + formatter.print(period));
				System.out.println("Overall speed: " + (long)(operationsCounter / (diff / 1000.0)) 
					+ " opers/sec current speed: " + (long)(lastOperationCounter / (diffSpan / 1000.0)) + " opers/sec");
				last = DateTime.now();
				lastOperationCounter = 0;
			}
			
			if (resultType.equals("JSON")) {
				return new JSONObject(sw.toString());
			} else {
				return sw.toString();
			}
		} finally {
			Cronometro.stop("ELASTICSEARCH");
		}
	}

	public Object put(String url, String body, String resultType) throws Exception {
		Cronometro.start("ELASTICSEARCH");
		HttpPut request = new HttpPut(url);
	 
		// add request header
		request.addHeader("User-Agent", USER_AGENT);
		try {
			request.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
			HttpResponse response = client.execute(request);
			ByteArrayOutputStream sw = new ByteArrayOutputStream(32*1024);
			IOUtils.copy(response.getEntity().getContent(), sw, 32*1024);
			
			operationsCounter++;
			lastOperationCounter++;
			if (operationsCounter % operationsBufferSize == 0) {
				DateTime span = DateTime.now().minus(start.getMillis());
				DateTime lastSpan = DateTime.now().minus(last.getMillis());
				long diff = span.getMillis();
				long diffSpan = lastSpan.getMillis(); 
				Period period = new Period(start, DateTime.now());

				System.out.println("Inserted:" + longIntegerFormat.format(operationsCounter) + " items in: " + formatter.print(period));
				System.out.println("Overall speed: " + (long)(operationsCounter / (diff / 1000.0)) 
					+ " opers/sec current speed: " + (long)(lastOperationCounter / (diffSpan / 1000.0)) + " opers/sec");
				last = DateTime.now();
				lastOperationCounter = 0;
			}
			
			if (resultType.equals("JSON")) {
				return new JSONObject(sw.toString());
			} else {
				return sw.toString();
			}
		} finally {
			Cronometro.stop("ELASTICSEARCH");
		}
	}
	
	public Object delete(String url, String resultType) throws Exception {
		Cronometro.start("ELASTICSEARCH");
		HttpDelete request = new HttpDelete(url);
		
		// add request header
		request.addHeader("User-Agent", USER_AGENT);
		try {
			HttpResponse response = client.execute(request);
			ByteArrayOutputStream sw = new ByteArrayOutputStream();
			IOUtils.copy(response.getEntity().getContent(), sw);
			if (resultType.equals("JSON")) {
				return new JSONObject(sw.toString());
			} else {
				return sw.toString();
			}
		} finally {
			Cronometro.stop("ELASTICSEARCH");
		}
	}
}
