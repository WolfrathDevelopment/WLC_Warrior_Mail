/* Joel Wolfrath, 2012
 * WarriorMail Android Application
 * This class functions as the main user interface hosts the 4
 * main activities that the application supports. 
 * TabActiviy is deprecated, working on a better implementation
 */

package warrior.mail.namespace;

import javax.mail.MessagingException;

import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

@SuppressWarnings("deprecation")
public class Warrior_MailActivity extends TabActivity{
	TabHost tabs;
	TabHost.TabSpec tab;
	Resources res;
	Intent intent;
	Account account;
	ProgressDialog pd;
	int connectionSuccess;
	boolean sender;
	
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        account = (Account)getApplicationContext();
        //look to see if connected yet
        sender = false;
        connectionSuccess = -1;
        tabs = getTabHost();
        res = getResources();
        try {
			init();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onPause(){
		account.saveCredentials();
	}

	public void init() throws MessagingException{	
		intent = new Intent().setClass(this,Inbox.class);
		//Folders Tab not fully implemented
		tab = tabs.newTabSpec("Folders").setIndicator("Folders",res.getDrawable(R.drawable.folder3)).setContent(new Intent(Warrior_MailActivity.this,Folder.class));
		tabs.addTab(tab);
		
		tab = tabs.newTabSpec("Inbox").setIndicator("Inbox",res.getDrawable(R.drawable.email)).setContent(intent);
		tabs.addTab(tab);
		
		tab = tabs.newTabSpec("New Message").setIndicator("New",res.getDrawable(R.drawable.unused)).setContent(new Intent(Warrior_MailActivity.this,NewMessage.class).putExtra("index",-1));
		tabs.addTab(tab);
		//Search Tab not fully implemented, displays inbox for now.
		tab = tabs.newTabSpec("Search").setIndicator("Search",res.getDrawable(R.drawable.search)).setContent(new Intent(Warrior_MailActivity.this,SearchActivity.class));
		tabs.addTab(tab);
		
		tabs.setCurrentTab(1);
	}
}