/* Joel Wolfrath, 2012
 * WarriorMail Android Application
 * This is the main Application class, the constructor
 * is the very first thing that is called in the application,
 * followed by the startup activity declared in the manifest.
 */

package warrior.mail.namespace;

import java.util.ArrayList;

import javax.mail.internet.InternetAddress;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;

public class Account extends Application{
	public JAdapter adapter;
	public static ArrayList<JView> localInbox;
	private MessagingService service;
	private ServiceConnection sConnection;
	private boolean isConnected;
	public String username,password;
	private SharedPreferences credentials;
	public int inboxCount;
	
	public Account(){
		sConnection = new ClientConnection();
		inboxCount = 0;
		isConnected = false;
    }
	
	public String getUser(){
		return username;
	}
	
	public void ensureConnection(){
		service.connect();
	}
	
	public void setCredentials(String user,String password){
		service.setCredentials(user, password);
		this.username = user;
		this.password = password;
		saveCredentials();
	}
	
	public void startService(){
		startService(new Intent(this,MessagingService.class));
		bindService(new Intent(this,MessagingService.class),sConnection,Context.BIND_ABOVE_CLIENT);
	}
	
	public int prepare(){
		int ret = service.connect();
		service.firstLoad();
		return ret;
	}
	
	public void refreshInbox(){
		localInbox = service.getLocalMessages();
	}
	
	public ArrayList<JView> getInbox(){
		localInbox = service.getLocalMessages();
		saveCredentials();
		return localInbox;
	}
	
	public void markMessageRead(JView which){
		service.markRead(which);
		localInbox = service.getLocalMessages();
	}
	
	public void deleteMessage(int which){
		service.markDeleted(localInbox.get(which));
		localInbox = service.getLocalMessages();
		adapter.notifyDataSetChanged();
	}

	public boolean isFirstRun(){
		loadCredentials();
		return !((username != null) && (password != null));
	}
	
	public boolean sendMessage(InternetAddress from,InternetAddress[] to,String subject,String body){
		return service.sendMessage(from, to, subject, body);
	}
	
	public void loadCredentials(){
		credentials = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		username = credentials.getString("user",null);
	    password = credentials.getString("pass",null);
	    inboxCount = credentials.getInt("count",-1);
	    if(service != null)
	    	service.setCredentials(username, password);
	}
	
	public int getRetainedCount(){
			loadCredentials();
		return inboxCount;
	}
	
	public void saveCredentials(){
		if(credentials == null){
			credentials = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		}
		      SharedPreferences.Editor editor = credentials.edit();
		      editor.putString("user",username);
		      editor.putString("pass",password);
		      if(localInbox.size() > 0)
		    	  editor.putInt("count",localInbox.get(0).inboxIndex);
		      // Commit the edits!
		      editor.commit();
	}
	
	public void startDownloader(){
		service.startDownloader();
	}
	
	class ClientConnection implements ServiceConnection{
		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			service = ((MessagingService.LocalBinder)binder).getService();
			isConnected = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			service = null;
			isConnected = false;
		}
	}
}
