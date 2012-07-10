/* Joel Wolfrath, 2012
 * WarriorMail Android Application
 * This class is my custom Array Adapter class
 * used for the ListView in the Inbox Activity.
 */

package warrior.mail.namespace;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class JAdapter extends ArrayAdapter<JView>
{
	private int size;
	private ArrayList<JView> list;
	
	public JAdapter(Context context,int resource,ArrayList<JView> list){
		super(context,resource,list);
		this.list = list;
		size = list.size()-1;
	}
	
	public void addNew(JView view)
	{
		list.add(0,view);
		this.size = list.size()-1;
		notifyDataSetChanged();
	}
	
	@Override
	public void notifyDataSetChanged() {
		this.list = Account.localInbox;
		this.size = list.size();
		super.notifyDataSetChanged();
	}
	
	public void addItem(JView view){
		list.add(view);
		this.size = list.size()-1;
		notifyDataSetChanged();
	}
	
	public void deleteItem(int index)
	{
		list.remove(index);
		this.size = list.size()-1;
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return size;
	}


	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View v = convertView;
		if(v == null){
			LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.newview,null);
		}
		
		if(position >= size){
			return v;
		}
			
		
		JView view = list.get(position);
		if(view != null){
			TextView f = (TextView) v.findViewById(R.id.from);
			TextView s = (TextView) v.findViewById(R.id.subject);
			ImageView iv = (ImageView) v.findViewById(R.id.icon);
			if(view.unread)
				iv.setImageResource(R.drawable.newmail);
			else
				iv.setImageResource(R.drawable.read);
			
			if(f != null)
				f.setText(view.from);
			if(s != null)
				s.setText(view.subject);
		}
		return v;
	}
}
