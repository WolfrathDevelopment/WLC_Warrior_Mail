/* Joel Wolfrath, 2012
 * WarriorMail Android Application
 * This Activity is a List of the downloaded messages.
 * The user can select a message to open, which provides
 * other options for interacting with the message.
 */

package warrior.mail.namespace;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ListView;

public class Inbox extends ListActivity implements ListView.OnItemClickListener{
	ListView lv;
	Account account;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        account = (Account)getApplicationContext();
        account.loadCredentials();
        account.startDownloader();
        //make sure to write/retrieve current inbox to hard drive
        lv = getListView();
        lv.setTextFilterEnabled(true);
        lv.setOnItemClickListener(this);
        //if statement to see where to get inbox
        //if(account.firstRun)
        	account.adapter = new JAdapter(this,R.layout.newview,account.getInbox());
        lv.setAdapter(account.adapter);
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	if(account.adapter != null)
    		account.adapter.notifyDataSetChanged();
    	account.ensureConnection();
    }
    
    @Override
    public void onPause(){
    	super.onPause();
    }
    
    @Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id){
		Log.v("Position:",position+"");
		Intent intent = new Intent(Inbox.this,ReadMessage.class);
		intent.putExtra("int",position);
		startActivity(intent);
	}
}
