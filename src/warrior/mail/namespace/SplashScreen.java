//Joel Wolfrath, 2012
//WarriorMail Android Application

package warrior.mail.namespace;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.content.Intent;

public class SplashScreen extends Activity{
	Account account;
	protected boolean active=true;
	protected int splashTime=5000;
	protected int increment=250;
	protected int sleepTime=100;
	
	@Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        account = (Account)getApplicationContext();
        account.startService();
		if(!account.isFirstRun()) {
			finish();
			startActivity(new Intent(SplashScreen.this,Warrior_MailActivity.class));
		}
		else {
			setContentView(R.layout.splash);
        	count();
		}
	}
	
	public void count(){
		//Thread for displaying SplashScreen
        Thread splashThread=new Thread(){
        	@Override
        	public void run(){
        		try{
        			int elapsedTime=0;
        			while(active && (elapsedTime < splashTime)){
        				sleep(sleepTime);
        				if(active)
        					elapsedTime+=increment;
        			}	
        		}
        		catch(InterruptedException e){Log.e("INTERRUPTED EXCEPTION!","Splash Screen.java");}
        		finally{
        			finish();
        			startActivity(new Intent(SplashScreen.this,Login.class));
        		}
        	}
        };
        splashThread.start();
	}
	//Continue to Login if user touches the screen
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if(event.getAction()==MotionEvent.ACTION_DOWN)
			active=false;
		return true;
	}
}