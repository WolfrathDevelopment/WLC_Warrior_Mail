package warrior.mail.namespace;

import java.util.ArrayList;

import javax.mail.internet.InternetAddress;

import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

public class MessagingService extends Service {
	private boolean shouldNotify;
	private int isConnected;
	private ArrayList<Integer> deleteQueue = new ArrayList<Integer>();
	private ArrayList<Integer> markQueue = new ArrayList<Integer>();
	private OutlookClient client;
	private Account account;
	public String username,password;
	private IBinder sBinder = new LocalBinder();
	DatabaseHelper database;
	private ClientThread cThread;
	
	protected Handler newMessageHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			JView view = msg.getData().getParcelable("message");
			database.addView(view);
			account.refreshInbox();
			account.adapter.notifyDataSetChanged();
			account.inboxCount++;
			account.saveCredentials();
			notifyUser();
		}
	};
	
	public void setCredentials(String user,String password){
		this.username = user;
		this.password = password;
	}
	
	public void markRead(JView which){
		database.updateView(which);
		markQueue.add(which.inboxIndex);
	}
	
	public void markDeleted(JView view){
		database.deleteView(view);
		deleteQueue.add(view.inboxIndex);
		adjustIndicies(view.inboxIndex);
		
	}
	
	private void adjustIndicies(int deletedIndex){
		//go through database
		//refresh localInbox, can assume localInbox is in numerical order
	}
	
	public ArrayList<JView> getLocalMessages(){
		return database.getAllViews();
	}
	
	private void notifyUser(){
		if(shouldNotify){
			try {
		        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
		        r.play();
		    } catch (Exception e) {}
		}
	}
	
	public void setNotificationSettings(boolean play){
		shouldNotify = play;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		shouldNotify = true;
		if(account == null)
			account = (Account)getApplicationContext();
		
		return START_STICKY;
	}
	
	public void startDownloader(){
		if(cThread == null){
			cThread = new ClientThread();
			client.setInboxCount(account.getRetainedCount());
			cThread.execute("");
		}
		else if(!cThread.isRunning){
			client.setInboxCount(account.getRetainedCount());
			cThread.execute("");
		}
	}
	
	public int connect(){
		if(client.session == null)
			isConnected = client.connect();
		else
			return OutlookClient.CONNECTION_SUCCESSFUL;
		return isConnected;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		account = (Account)getApplicationContext();
		client = new OutlookClient(this);
		database = new DatabaseHelper(this);
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return sBinder;
	}
	
	@Override
	public void unbindService(ServiceConnection conn) {
		super.unbindService(conn);
	}
	
	public void grabCredentials(){
		account.loadCredentials();
	}
	
	public void firstLoad(){
		ArrayList<JView>localInbox = client.retrieveInbox();
		for(int i=0;i<localInbox.size();i++){
			JView view = localInbox.get(i);
			database.addView(view);
		}
	}
	
	public boolean sendMessage(InternetAddress from,InternetAddress[] to,String subject,String body){
		return client.sendMessage(from, to, subject, body);
	}
	
	class LocalBinder extends Binder{
		MessagingService getService(){
			return MessagingService.this;
		}
	}
	
	class ClientThread extends AsyncTask<String,String,String>{
		public boolean isRunning = false;
		
		@Override
		protected String doInBackground(String... params) {
			isRunning = true;
			while(true){
				if(client.session == null)
					client.connect();
					
				if(client.hasNewMessages()){
					JView view = new JView();
					view = client.getNewMessage();
					Message msg = new Message();
					Bundle b = new Bundle();
					b.putParcelable("message",view);
					msg.setData(b);
					newMessageHandler.sendMessage(msg);
				}
				
				if(deleteQueue.size() > 0){
					//mark deleted
				}
				
				if(markQueue.size() > 0){
					//mark read
				}
			}
			//isRunning = false;
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			isRunning = false;
		}
	}
}