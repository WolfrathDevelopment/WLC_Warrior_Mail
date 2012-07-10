/* Joel Wolfrath, 2012
 * WarriorMail Android Application
 * This class is called when the New tab is selected
 * from the TabActivity.
 */

package warrior.mail.namespace;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class NewMessage extends Activity implements View.OnClickListener{
	EditText to,subject,body;
	Account account;
	Button send;
	InternetAddress sendto[];
	InternetAddress from;
	String sub;
	ProgressDialog pd;
	int which;
	
	@Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.write);
        account = (Account)getApplicationContext();
        sendto = new InternetAddress[1];
        try {
			from = new InternetAddress(account.getUser());
		} catch (AddressException e) {
			e.printStackTrace();
		}
        Bundle b = this.getIntent().getExtras();
        boolean forward = b.getBoolean("forward",false);
        boolean reply = b.getBoolean("reply",false);
        which = -1;
        which = b.getInt("index");
        to = (EditText) findViewById(R.id.to);
        subject = (EditText) findViewById(R.id.subject);
        body = (EditText) findViewById(R.id.body);
        if(forward || reply){
        	to.setText((CharSequence) b.get("from"));
        	subject.setText((CharSequence) b.get("sub"));
        }
        send = (Button)findViewById(R.id.sendButton);
        send.setOnClickListener(this);
	}

	@Override
	public void onClick(View view){
		if(which != -1){
			try{
				sendto[0] = new InternetAddress(account.adapter.getItem(which).from);
			} catch (MessagingException e) {}
		}
		else{
			try {
				sendto[0] = new InternetAddress(to.getText().toString());
			} catch (AddressException e1) {
			e1.printStackTrace();
			}
		}
		sub=subject.getText().toString();
		
		new SendTask().execute("");
	}
	
	@Override
	public void onPause(){
		super.onPause();
		if(pd != null)
			pd.dismiss();
	}
	
	//Background task which must do networking to send a message.
	private class SendTask extends AsyncTask<String,String,String>{
		
		boolean success;
		
		@Override
		protected void onPreExecute(){
			pd = ProgressDialog.show(NewMessage.this,"","Sending Message...", true,false);
		}
		//Should probably return boolean
		@Override
		protected String doInBackground(String...strings){
			success = account.sendMessage(from, sendto, sub,body.getText().toString());
			return null;
		}
	     
		@Override
	    protected void onPostExecute(String unused){
			if(success)
				pd.setMessage("Message Sent");
			else
				pd.setMessage("Send Failed");
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	        pd.dismiss();
	        body.setText("");
			to.setText("");
			subject.setText("");
			if(which != -1){
				finish();
			}
	    }
	
	 }
}
