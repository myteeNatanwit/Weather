package intcloud.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
	TextView updatefield;
	ImageButton forecast_btn;
	TextView e_day1;
	TextView e_day2;
	double latitude;
	double longitude;
	int  bcolor;
	String mydata = ""; 
	ProgressDialog progress; 
	
	LocationManager locationManager;
	LocationListener locationListner;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		progress = ProgressDialog.show(this, "", "Loading...");
		// get reference to the views

		e_zone = (TextView) findViewById(R.id.e_zone);
		e_icon = (TextView) findViewById(R.id.e_icon);
		e_temp = (TextView) findViewById(R.id.e_temp);
		e_sum = (TextView) findViewById(R.id.e_sum);
		updatefield = (TextView) findViewById(R.id.updatefield);

		Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/weather.ttf");
		e_icon.setTypeface(tf);
	
		// check if connect to get GPS listener to access weather API
		if(isConnected()){
			if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
					!= PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
            }
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			locationListner = new MyLocationListner();
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListner);
			}
			Toast.makeText(getBaseContext(), "Loading data ...", Toast.LENGTH_LONG).show();
        }

		else{
			e_zone.setText("You are not connected");

		}
		
		forecast_btn=(ImageButton)findViewById(R.id.ib);

	}
	
	// button action
	public void call_screen(View v) 
    {
		if (mydata.length() > 0) {
        Intent myintent = new Intent(getApplicationContext(), forecast.class);
        myintent.putExtra("data", mydata);
        startActivity(myintent);  
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
	case "clear-night": code = getString(R.string.wi_night_clear); bcolor = android.graphics.Color.CYAN; break;
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
		DateFormat df = DateFormat.getDateTimeInstance();
        float humid;
        float cloud;
        float temp; 
		try {
    		JSONObject json = new JSONObject(result);
    		str =json.getString("timezone");
    		e_zone.setText(str);
    		JSONObject sys  = json.getJSONObject("currently");
        	String updatedOn = df.format(new Date(sys.getLong("time")*1000));
        	updatefield.setText("Last update: " + updatedOn);
    		temp =sys.getInt("temperature");
    		
    		//convert from F to c
    		temp = (temp - 32) * 5/9;
    		str = String.format("%.00f", temp);
    		
			e_temp.setText(str + "c");
			str = sys.getString("humidity");
			humid = Float.parseFloat(str) * 100;
			
			str = sys.getString("cloudCover");
			cloud = Float.parseFloat(str) * 100;
			str = "Summary: " + sys.getString("summary")
				+ "\n" + "Humidity: " +  String.format("%.00f",humid) +"%"
    			+ "\n" + "Cloud Cover: " +  String.format("%.00f",cloud) + "%"
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
            mydata = result;
        	parse_result(result);
        	progress.dismiss();  
        	//call_screen(null);
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
         alertDialog.show();
    }
   
    //Location listener
	class MyLocationListner implements LocationListener {
		@Override
		public void onLocationChanged(Location location) {
			if (location != null) {
				latitude = location.getLatitude();
				longitude = location.getLongitude();
				locationManager.removeUpdates(locationListner);
				System.out.println(latitude + " " + longitude);
				//locationManager = null;
				// call AsynTask to perform network operation on separate thread
				String myurl = "https://api.forecast.io/forecast/d4234c817b34a6b145ee78345d14e2e9/" +  latitude + "," + longitude;
	//			Toast.makeText(getBaseContext(), myurl, Toast.LENGTH_LONG).show();
				new HttpAsyncTask().execute(myurl);
			}
			else {
                System.out.println("fail " + latitude + " " + longitude);
            }
		}

		@Override
		public void onProviderDisabled(String arg0) {
	
		}

		@Override
		public void onProviderEnabled(String arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// TODO Auto-generated method stub

		}

	}	
	//manual refresh
	public void refresh(View v) {
		Toast.makeText(getBaseContext(), "refreshing ...", Toast.LENGTH_LONG).show();
		if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
				== PackageManager.PERMISSION_GRANTED) {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
					0, locationListner);
		}
	}
}
