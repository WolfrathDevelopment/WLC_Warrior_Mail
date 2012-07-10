/* Joel Wolfrath, 2012
 * WarriorMail Android Application
 * This Activity is where the user enters their 
 * credentials to connect to the server.  The
 * username and password is passed to the application
 * class which manages connections to the server.
 */
	
package warrior.mail.namespace;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

public class Login extends Activity implements View.OnClickListener{
	Account account;
	private Button login;
	private EditText email;
	private EditText pass;
	private String user; 
	private String password;
	boolean clicked;
	ProgressDialog pd;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.login);
		account = (Account)getApplicationContext();
		clicked = false;
		login = (Button) findViewById(R.id.button1);
		login.setOnClickListener(this);
		email = (EditText) findViewById(R.id.editText1);
		pass = (EditText) findViewById(R.id.editText2);
		AlertDialog alert = null;
		pd = null;
		if(!isNetworkAvailable()){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("-Network Unavailable-\nPlease connect and try again.");
			alert = builder.create();
		}
		while(!isNetworkAvailable()){
			
		}
		if(alert != null){
			alert.dismiss();
		}
	} 
	
	@Override
	public void onPause()
	{
		super.onPause();
		if(pd != null)
			pd.dismiss();
		//start pd again on resume
	}
	
	private boolean isNetworkAvailable(){     
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			
		return (activeNetworkInfo != null) || wm.getWifiState() == WifiManager.WIFI_STATE_ENABLED;
	}
	
	public void onClick(View v){
		if(clicked)
			return;
		clicked = true;
		account = (Account)getApplicationContext();
		//user = email.getText().toString();
		//password = pass.getText().toString();
		user = "joel.wolfrath@mail.wlc.edu";
		password = "N1ghtmAre";
		account.setCredentials(user,password);
		new SendTask().execute("");
	}
	/*
	No networking allowed on the main thread.  
	Connects to the server with the entered credentials
	and retrieves the first 10 messages
	*/
	private class SendTask extends AsyncTask<String,String,String>{
		int result;
		
		@Override
		protected void onPreExecute(){
			pd = ProgressDialog.show(Login.this,"","Retrieving Inbox...", true,false);
		}
		
		@Override
		protected String doInBackground(String...strings){
			result = account.prepare();
			return null;
		}
	     
		@Override
	    protected void onPostExecute(String unused){
			//check result
			pd.dismiss();
			Intent intent = new Intent(Login.this,Warrior_MailActivity.class);
			finish();
			startActivity(intent);
	    }
	 }
}
