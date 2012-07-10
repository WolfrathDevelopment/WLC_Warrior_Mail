/* Joel Wolfrath, 2012
 * WarriorMail Android Application
 * This class serves as the Folder Tab of the
 * TabActivity.  Not fully implemented.
 */

package warrior.mail.namespace;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class Folder extends ListActivity implements OnItemClickListener
{
	ArrayAdapter<String> adapter;
	String[] vals = {"   Unread","   Inbox","   Sent","   Deleted","   Junk"};

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        ListView lv;
        lv = getListView();
        lv.setTextFilterEnabled(true);
        lv.setOnItemClickListener(this);
        adapter = new ArrayAdapter<String>(this,R.layout.folder,R.id.fname,vals);
        lv.setAdapter(adapter);
    }

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// Open a ListView Activity to handle the folders contents	
	}
}
