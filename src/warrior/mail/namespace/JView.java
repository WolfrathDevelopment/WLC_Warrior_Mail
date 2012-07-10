/* Joel Wolfrath, 2012
 * WarriorMain Android Application
 * This is my custom class used to store Messages
 * and their data. Instance variables are public 
 * for easy access by other classes.
 */

package warrior.mail.namespace;

import android.os.Parcel;
import android.os.Parcelable;

public class JView implements Parcelable {
	public String subject;
	public String from;
	public boolean unread;
	public String body;
	public int inboxIndex;
	private long id;
	public static final Parcelable.Creator<JView> CREATOR = new Parcelable.Creator<JView>() {
    
		public JView createFromParcel(Parcel in) {
			return new JView(in);
		}

		public JView[] newArray(int size) {
			return new JView[size];
		}

	};
	
	public JView(){
		body = "";
	}
	
	public JView(String subject,String from,boolean unread){
		body = "";
		this.subject = subject;
		this.from = from;
		this.unread = unread;
	}
	
	public JView(Parcel parcel){
		subject = parcel.readString();
		from = parcel.readString();
		body = parcel.readString();
		unread = parcel.createBooleanArray()[0];
		inboxIndex = parcel.readInt();
	}

	@Override
	public int describeContents() {
		return inboxIndex;
	}

	@Override
	public void writeToParcel(Parcel out, int arg1) {
		out.writeString(subject);
		out.writeString(from);
		out.writeString(body);
		boolean[] array = new boolean[] {unread};
		out.writeBooleanArray(array);
		out.writeInt(inboxIndex);
	}
	
	public void setIndex(int index){
		inboxIndex = index;
	}
	
	public void setUnread(boolean arg){
		unread = arg;
	}
	
	public void setContent(String content){
		body = content;
	}
	
	public void setSubject(String subject){
		this.subject = subject;
	}
	
	public void setFrom(String f){
		from = f;
	}
	
	public void setId(long arg){
		id = arg;
	}
	
	public long getId(){
		return id;
	}
	
	public void updateIndex(){
		
	}
}
