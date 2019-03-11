package rippledServerInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.json.JSONObject;

public class RippledServerInfo {

	//Global variables for ease of use and clarity
	public static final String MY_FILE = "C:/myoutput/data.txt"; // Static file in local system to write our output
	public static final int POLL_INTERVAL = 1000; 				 //Polling interval of Ripple Daemon in milliseconds - set to 1 second
	public static final int SLEEP_DURATION = 180000; 			  //Duration of the polling in milliseconds - set to 3 minutes
	public static final String RIPPLE_URL = "http://s1.ripple.com:51234";
	public static final String JSON_REQUEST = "{ \"method\" : \"server_info\" , \"params\" : [ {} ] }";

	public static void main(String[] args) throws IOException {
		
        File file = new File(MY_FILE);
        PrintWriter printWriter = null;  
        try { printWriter = new PrintWriter(file); }
        	catch (IOException ex) { ex.printStackTrace(); }
        
		Timer timer = new Timer();
		timer.schedule(new RippledServerInfo.GetTimeSyncData(), 0, POLL_INTERVAL);
		
		try { Thread.sleep(SLEEP_DURATION); }
			catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
		
		timer.cancel();
		timer.purge();
		
		if ( printWriter != null ) {printWriter.close();}
				
        Scanner sc = new Scanner(file);
        
        List<Integer> integers = new ArrayList<>();
        while(sc.hasNextLine()){
            String line = sc.nextLine();
            String[] details = line.split(",");
            int age = Integer.parseInt(details[1]);
            integers.add(age);
        }
        
        sc.close();

        Map<Integer, Integer> lookup = new HashMap<>();
        for (int key : integers) {
            if(lookup.containsKey(key)) {
                lookup.put(key, lookup.get(key) + 1);
            } else {
                lookup.put(key, 1);
            }
        }
        System.out.println("-----------------------------");
        int added = 0 ;
        for (Integer keys : lookup.keySet()) {
            System.out.println("Ledger " + keys + " took " + lookup.get(keys) + " seconds to be validated");
            added += lookup.get(keys);
        }
        
        Integer min = Collections.min(lookup.values());
        Integer max = Collections.max(lookup.values());
        System.out.println("-----------------------------");
        System.out.println("Sample size: " + lookup.size());
        System.out.println("Min(): " + min);
        System.out.println("Max(): " + max);
        System.out.println("Avg(): " + added / lookup.size());
        

	}
	
	public static class GetTimeSyncData extends TimerTask {
	    public void run() {
		    try {
	            URL url = new URL(RIPPLE_URL);
	            HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();
	            httpConn.setConnectTimeout(5000);
	            httpConn.setRequestProperty("Content-Type", "application/json");
	            httpConn.setDoOutput(true);
	            httpConn.setRequestMethod("POST");
	            
	            OutputStream os = httpConn.getOutputStream();
	            os.write(JSON_REQUEST.getBytes());
	            os.flush();

	            BufferedReader br = new BufferedReader(new InputStreamReader((httpConn.getInputStream())));

	            if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK){
	            StringBuilder stringBuilder = new StringBuilder();
	            String output;
	            while ((output = br.readLine()) != null) {
	            stringBuilder.append(output);
	            	}
	            String resp = stringBuilder.toString();
	           
	            JSONObject jsonObjresp = new JSONObject(resp);
	            JSONObject jsonObjInfo = jsonObjresp.getJSONObject("result").getJSONObject("info");
	            String strDateTime = jsonObjInfo.get("time").toString();
	            int intSeq  = (int) jsonObjInfo.getJSONObject("validated_ledger").get("seq");
	            var indx = strDateTime.indexOf('.');
	            strDateTime = strDateTime.substring(0, indx);

 	            Calendar cal = Calendar.getInstance();
	            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
	            try { cal.setTime(sdf.parse(strDateTime)); } catch (ParseException e) { e.printStackTrace(); }
	            
	            int hour = cal.get(Calendar.HOUR_OF_DAY);
	            int minute = cal.get(Calendar.MINUTE);
	            int second = cal.get(Calendar.SECOND);
	            String seqQueryTime = hour + ":" + minute + ":" + second;
	             	           
 	            Files.write(Paths.get(MY_FILE), (seqQueryTime + "," + intSeq+"\r\n").getBytes(), StandardOpenOption.APPEND);  //Files.write() method closes file after all bytes written
	            System.out.println(seqQueryTime + "," + intSeq);
         
	            br.close();
	            httpConn.disconnect();
	            
	            } else { 
	               System.out.println("HTTP Status Code: " + httpConn.getResponseCode());
	            	}
	            } catch (Exception e) {
	            	e.printStackTrace();
	            	}
			
	    }
	}

}
