/* Joel Wolfrath, 2012
 * WarriorMail Android Application
 * This class is called from the inbox activity
 * when the user selects a message.  Displays the
 * contents of the message to the user.
 */

package warrior.mail.namespace;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.view.View;

public class ReadMessage extends Activity implements AdapterView.OnItemSelectedListener{
	Account account;
	int which;
	EditText body;
	TextView from,subject;
	String msg;
	Object obj;
	Spinner spinner;
	JView message;

	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.read);
		account = (Account)getApplicationContext();
		Bundle b = this.getIntent().getExtras();
		spinner = (Spinner) findViewById(R.id.spinner1);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.choices, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinner.setAdapter(adapter);
	    spinner.setOnItemSelectedListener(this);
	    spinner.setPrompt("Options");
		which = b.getInt("int");
		message = Account.localInbox.get(which);
		from = (TextView)findViewById(R.id.readfrom);
		subject = (TextView)findViewById(R.id.readsubject);
		body = (EditText)findViewById(R.id.readbody);
		msg = message.body;
		if(msg==null || msg == "")
			body.setText("\n*This message has no content.*");
		else
			body.setText(msg);
		from.setText(message.from);
		subject.setText(message.subject);
		if(message.unread){
			message.unread = false;
			account.markMessageRead(message);
			account.adapter.notifyDataSetChanged();
		}
	}
		@Override
		public void onItemSelected(AdapterView<?> adapter, View v, int position,long id){
			Intent intent = new Intent(ReadMessage.this,NewMessage.class);
			intent.putExtra("from",from.getText());
			intent.putExtra("sub","RE: " + subject.getText());
			intent.putExtra("body",body.getText());
			intent.putExtra("index",position);
			
			switch(position){
				case 0: break;
				case 1: intent.putExtra("reply",true); startActivity(intent); break;
				case 2: account.deleteMessage(which); finish(); break;
				case 3: intent.putExtra("forward",true); startActivity(intent); break;
				default: break;
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0){}
}
