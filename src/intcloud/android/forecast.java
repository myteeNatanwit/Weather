package intcloud.android;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class forecast extends Activity {
	TextView e_zone;
	TextView e_icon;
	TextView e_temp;
	TextView e_sum;
	TextView updatefield;
	double latitude;
	double longitude;
	int  bcolor;
	String mydata = "";  
	
	   public class List_item {
		    String theday;
		    String icon;
			String temperature;
			String summary;
		}
	   List<List_item> daily_forcast = new ArrayList<List_item>();
	   //List_item[] daily_forcast = new Array;
	   
	   ListAdapter MyListAdapter;	           
	  
	   
	   @Override
	   protected void onCreate(Bundle savedInstanceState) {
	      super.onCreate(savedInstanceState);
	      setContentView(R.layout.forecast);
	     // getActionBar().setDisplayHomeAsUpEnabled(true);
	      
	      MyListAdapter = new ListAdapter();
	        e_zone = (TextView) findViewById(R.id.e_zone);
			e_icon = (TextView) findViewById(R.id.e_icon);
			e_temp = (TextView) findViewById(R.id.e_temp);
			e_sum = (TextView) findViewById(R.id.e_sum);
			updatefield = (TextView) findViewById(R.id.updatefield);
			//e_day1 = (TextView) findViewById(R.id.e_day1);
			//e_day2 = (TextView) findViewById(R.id.e_day2);
			Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/weather.ttf");
			e_icon.setTypeface(tf);
			
			
			
	   
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
//        	 RelativeLayout bgElement = (RelativeLayout) findViewById(R.id.forecast);

//	    	 bcolor = Integer.parseInt(extras.getString("bcolor"));
//	    	 bgElement.setBackgroundColor(bcolor);    	 
            String data= extras.getString("data");
          //  Toast.makeText(getBaseContext(), "" + data.length(), Toast.LENGTH_LONG).show();
           parse_result(data);
        }
        
        ListView listView = (ListView) findViewById(R.id.mobile_list);
	    listView.setAdapter(MyListAdapter);
    }
    
	   
	   public class ListAdapter extends BaseAdapter {

	    	List<List_item> list_data = getDataForListView();
			@Override
			public int getCount() {
				return list_data.size();
			}

			@Override
			public List_item getItem(int arg0) {
				return list_data.get(arg0);
			}

			@Override
			public long getItemId(int arg0) {
				return arg0;
			}

			@Override
			public View getView(int arg0, View arg1, ViewGroup arg2) {
				
				if(arg1==null)
				{
					LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					arg1 = inflater.inflate(R.layout.listitem, arg2,false);
				}
				TextView icon = (TextView)arg1.findViewById(R.id.icon);
				TextView temp = (TextView)arg1.findViewById(R.id.textView1);
				TextView Desc = (TextView)arg1.findViewById(R.id.textView2);
				
				List_item item = list_data.get(arg0);
				Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/weather.ttf");
				icon.setTypeface(tf);
	    	    icon.setText(get_icon_code(item.icon));
				temp.setText(item.temperature);
				Desc.setText(item.summary);
				
				return arg1;
			}
			
			public List_item get_item(int position)
			{
				return list_data.get(position);
			}

	    }
	    
	    @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	        // Inflate the menu; this adds items to the action bar if it is present.
	  //      getMenuInflater().inflate(R.menu.list_view_with_simple_adapter, menu);
	        return true;
	    }
	    
	    public List<List_item> getDataForListView()
	    {
	    	/*
	    	List<List_item> itemslist = new ArrayList<List_item>();
	    	
	    	for(int i=0;i<8;i++)
	    	{
	    		List_item item = new List_item();
	    		item.temperature = "Chapter "+i;
	    		item.summary = "This is description for chapter "+i;
	    		itemslist.add(item);
	    	}
	    	
	    	return itemslist;
	    	*/
	    	return daily_forcast;
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
			DateFormat df = DateFormat.getDateTimeInstance();
	        Long dt;
	        float cloud;
	        float temp; 
			try {
	    		JSONObject json = new JSONObject(result);
	    		str =json.getString("timezone");
	    		e_zone.setText(str);
	    		JSONObject sys  = json.getJSONObject("currently");
	    	    dt = sys.getLong("time");
	        	String updatedOn = df.format(new Date(dt *1000));
	        	updatefield.setText("Last update: " + updatedOn);
	    		temp =sys.getInt("temperature");
	    		
	    		//convert from F to c
	    		temp = (temp - 32) * 5/9;
	    		str = String.format("%.00f", temp);
				e_temp.setText(str + "c");
				str = "Summary: " + sys.getString("summary");
	    	    e_sum.setText(str);

	    	    str =sys.getString("icon");
	    		
	    	    e_icon.setText(get_icon_code(str));
	    	   // RelativeLayout bgElement = (RelativeLayout) findViewById(R.id.container);
	    	   // bgElement.setBackgroundColor(bcolor);
	    	   
	    	    //get daily forecast
	    	    sys  = json.getJSONObject("daily");
	    	    JSONArray ja = new JSONArray();
	    	    ja = sys.getJSONArray("data");
	    	    int n = ja.length();
	    	    str = "" + n;
	    	    //showDialog(str);
	    	    
	    	    for (int i = 1, size = n; i < size; i++)
	    	    {
	    	      JSONObject objectInArray = ja.getJSONObject(i);
	    	      List_item item = new List_item();
	    	      item.icon =  objectInArray.getString("icon");
	    	      item.summary = objectInArray.getString("summary");
	    	      temp = objectInArray.getInt("temperatureMin");
	    	      temp = (temp - 32) * 5/9;
		    	  str = String.format("%.00f", temp);
		    	  item.temperature = str;
		    	  temp = objectInArray.getInt("temperatureMax");
	    	      temp = (temp - 32) * 5/9;
		    	  str = String.format("%.00f", temp);
	    	      item.temperature += "c-" + str+"c";
	    	      
	    	      dt = objectInArray.getLong("time");
		    	  dt = dt * 1000; 
		    	  SimpleDateFormat formatter = new SimpleDateFormat("EEE");
		    	  str = formatter.format(new java.util.Date(dt));
		    	  item.theday= str;
		    	  item.temperature = str + " " +  item.temperature;
		    	  //Toast.makeText(getBaseContext(), "" + str, Toast.LENGTH_LONG).show();
	    	      daily_forcast.add(item);
	    	      
	    	    }
	    	 
	    	} catch (JSONException e) {
				e.printStackTrace();
				Toast.makeText(getBaseContext(), "fail parse", Toast.LENGTH_LONG).show();
			}
		}
	
public void gohome(View v) {
	this.finish();
}
  }
  

