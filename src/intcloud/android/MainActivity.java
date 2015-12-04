package intcloud.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {
	TextView e_zone;
	TextView e_icon;
	TextView e_temp;
	TextView e_sum;
	TextView e_day1;
	TextView e_day2;
	double latitude;
	double longitude;
	int  bcolor;
	LocationManager locationManager;
	LocationListener locationListner;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	
		// get reference to the views

		e_zone = (TextView) findViewById(R.id.e_zone);
		e_icon = (TextView) findViewById(R.id.e_icon);
		e_temp = (TextView) findViewById(R.id.e_temp);
		e_sum = (TextView) findViewById(R.id.e_sum);
		//e_day1 = (TextView) findViewById(R.id.e_day1);
		//e_day2 = (TextView) findViewById(R.id.e_day2);
		Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/weather.ttf");
		e_icon.setTypeface(tf);
	
		// check if connect to get GPS listener to access weather API
		if(isConnected()){
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			locationListner = new MyLocationListner();
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
					0, locationListner);	
			Toast.makeText(getBaseContext(), "Loading data ...", Toast.LENGTH_LONG).show();
        }
		else{
			e_zone.setText("You are not connected");
		}
	}

	// standard Get
	public static String GET(String url){
		InputStream inputStream = null;
		String result = "";
		try {
			// create HttpClient
			HttpClient httpclient = new DefaultHttpClient();
			// make GET request to the given URL
			HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
			// receive response as inputStream
			inputStream = httpResponse.getEntity().getContent();
			// convert inputstream to string
			if(inputStream != null)
				result = convertInputStreamToString(inputStream);
			else
				result = "Cannot connect";
		
		} catch (Exception e) {
			Log.d("InputStream", e.getLocalizedMessage());
		}
		return result;
	}
// get icon code
	public String get_icon_code(String icon_str){
	String code = getString(R.string.wi_day_sunny);
	bcolor = android.graphics.Color.CYAN;
	switch (icon_str){
	case "clear": code = getString(R.string.wi_day_sunny); bcolor = android.graphics.Color.CYAN; break;
	case "clear-day": code = getString(R.string.wi_day_sunny); bcolor = android.graphics.Color.YELLOW; break;
	case "clear-night": code = getString(R.string.wi_night_clear); bcolor = android.graphics.Color.DKGRAY; break;
	case "rain": code = getString(R.string.wi_day_showers); bcolor = android.graphics.Color.GREEN; break;
	case "wind": code = getString(R.string.wi_day_cloudy_windy); bcolor = android.graphics.Color.CYAN; break;
	case "fog": code = getString(R.string.wi_day_fog); bcolor = android.graphics.Color.GRAY; break;
	case "cloudy": code = getString(R.string.wi_day_cloudy); bcolor = android.graphics.Color.LTGRAY; break;
	case "partly-cloudy-day": code = getString(R.string.wi_day_cloudy_gusts); bcolor = android.graphics.Color.GRAY; break;
	case "partly-cloudy-night": code = getString(R.string.wi_night_alt_cloudy); bcolor = android.graphics.Color.GREEN; break;
	}
	return code;
	}
	
	// parse json from GET request	
	public void parse_result(String result){
		String str;
		//showDialog("" + result.length());
		try {
    		JSONObject json = new JSONObject(result);
    		str =json.getString("timezone");
    		e_zone.setText(str);
    		JSONObject sys  = json.getJSONObject("currently");
    		str =sys.getString("temperature");
    		//convert from f to c
    		Float f= Float.parseFloat(str);
    		f = (f - 32) * 5/9;
    		//str = Float.toString(f);
    		str = String.format("%.00f", f);
			e_temp.setText(str + "c");
			str = sys.getString("humidity");
			f= Float.parseFloat(str) * 100;
			
    		str = "Summary: " + sys.getString("summary") 
    			+ "\n" + "Humidity: " + f +"%"
    			+ "\n" + "Cloud Cover: " + sys.getString("cloudCover") + "%"
    			+ "\n" + "Wind Speed: " + sys.getString("windSpeed");
    		
    	    e_sum.setText(str);
    		str =sys.getString("icon");
    		
    	    e_icon.setText(get_icon_code(str));
    	    RelativeLayout bgElement = (RelativeLayout) findViewById(R.id.container);
    	    bgElement.setBackgroundColor(bcolor);
    	   
    	    //get daily forecast
    	    sys  = json.getJSONObject("daily");
    	    JSONArray ja = new JSONArray();
    	    ja = sys.getJSONArray("data");
    	    int n = ja.length();
    	    str = "" + n;
    	    //showDialog(str);
    	    /*
    	    for (int i = 0, size = n; i < size; i++)
    	    {
    	      JSONObject objectInArray = ja.getJSONObject(i);
    	      str= "" + objectInArray.getString("icon");
    	      str+= " " + objectInArray.getString("temperatureMin");
    	      str+= " " + objectInArray.getString("temperatureMax");
    	      e_day1.setText(str);
    	    }*/
    	  /*  
    	  JSONObject objectInArray = ja.getJSONObject(0);
    	  long lo = objectInArray.getLong("time");
    	  lo = lo * 1000;
    	  java.util.Date dateTime=new java.util.Date(lo);
    	  
  	      str= dateTime +" " + objectInArray.getString("icon");
  	       
  	      f= Float.parseFloat(objectInArray.getString("temperatureMin"));
		  f = (f - 32) * 5/9;
		  str+= " " +String.format("%.00f", f)+ "c";
 		    	      
  	      f= Float.parseFloat(objectInArray.getString("temperatureMax"));
		  f = (f - 32) * 5/9;
		  str+= " " +String.format("%.00f", f) + "c";
		  
  	      e_day1.setText(str);
  	      objectInArray = ja.getJSONObject(1);
  	      lo = objectInArray.getLong("time");
  	      //showDialog("" + lo);
  	      lo = lo * 1000;
  	      dateTime= new java.util.Date(lo);
	      str= dateTime +" " + objectInArray.getString("icon");
	      str= "" + objectInArray.getString("icon");
	      str+= " " + objectInArray.getString("temperatureMin");
	      str+= " " + objectInArray.getString("temperatureMax");
	      e_day2.setText(str);
	      */
    	} catch (JSONException e) {
			e.printStackTrace();
			Toast.makeText(getBaseContext(), "fail parse", Toast.LENGTH_LONG).show();
		}
	}

	//conversion routine	
    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
        
        inputStream.close();
        return result;
        
    }

    //check if there is connection
    public boolean isConnected(){
    	ConnectivityManager connMgr = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
    	    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
    	    if (networkInfo != null && networkInfo.isConnected()) 
    	    	return true;
    	    else
    	    	return false;	
    }

    //http call with closure
    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
              
            return GET(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
 //       	Toast.makeText(getBaseContext(), "Got data from api.forecast.io", Toast.LENGTH_LONG).show();
        	parse_result(result);
       }
    }
    
    //general purpose dialog
    public void showDialog(String dialogMsg){
    	AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
    	 
        // Setting Dialog Title
        alertDialog.setTitle("Title of the Android dialog");
 
        // Setting Dialog Message
        alertDialog.setMessage(dialogMsg);
        alertDialog.setCancelable(true);
        alertDialog.setPositiveButton("OK",
            new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int which) {
                    	 dialog.cancel();
                     }
                 });
 
        // Showing Alert Message
        alertDialog.show();
    }
    
    //Location listener
	class MyLocationListner implements LocationListener {
	//	@Override
		public void onLocationChanged(Location location) {
			if (location != null) {
				latitude = location.getLatitude();
				longitude = location.getLongitude();
				locationManager.removeUpdates(locationListner);
				//locationManager = null;
				//System.out.println(latitude + " " + longitude );
				//showDialog(latitude + " " + longitude);
				// call AsynTask to perform network operation on separate thread
				String myurl = "https://api.forecast.io/forecast/d4234c817b34a6b145ee78345d14e2e9/" +  latitude + "," + longitude;
	//			Toast.makeText(getBaseContext(), myurl, Toast.LENGTH_LONG).show();
				//new HttpAsyncTask().execute("https://api.forecast.io/forecast/d4234c817b34a6b145ee78345d14e2e9/37.8267,-122.423");
				new HttpAsyncTask().execute(myurl);
			}
		}

//		@Override
		public void onProviderDisabled(String arg0) {
	
		}

	//	@Override
		public void onProviderEnabled(String arg0) {
			// TODO Auto-generated method stub

		}

	//	@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// TODO Auto-generated method stub

		}

	}	
	public void refresh(View v) {
		Toast.makeText(getBaseContext(), "refreshing ...", Toast.LENGTH_LONG).show();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, locationListner);	
	}
}
