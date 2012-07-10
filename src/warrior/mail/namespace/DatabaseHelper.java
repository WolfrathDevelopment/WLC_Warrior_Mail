package warrior.mail.namespace;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper{
	private static final String DATABASE_NAME = "views.db";
	private static final int DATABASE_VERSION = 1;
	
	public static final String TABLE_VIEWS = "views";
	
	private static final String KEY_ID = "id";
	private static final String KEY_INDEX = "inbox_index";
    private static final String KEY_FROM = "msg_from";
    private static final String KEY_UNREAD = "unread";
    private static final String KEY_SUBJECT = "subject";
    private static final String KEY_BODY = "body";


	// Database creation sql statement
	private static final String CREATE_VIEWS_TABLE = "CREATE TABLE " + TABLE_VIEWS + " ("
            + KEY_ID + " INTEGER PRIMARY KEY," 
			+ KEY_INDEX + " INTEGER NOT NULL,"
            + KEY_UNREAD + " TEXT NOT NULL," 
			+ KEY_SUBJECT + " TEXT NOT NULL,"
            + KEY_FROM + " TEXT NOT NULL," 
			+ KEY_BODY + " TEXT NOT NULL);";

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_VIEWS_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//Drop older table if exists
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_VIEWS);
		
		//recreate table
		onCreate(db);
	}
	
	// Adding new view
	public void addView(JView view) {
		SQLiteDatabase db = this.getWritableDatabase();
		
	    ContentValues values = new ContentValues();
	    values.put(DatabaseHelper.KEY_INDEX,view.inboxIndex);
	    if(view.unread)
	    	values.put(KEY_UNREAD,"true");
	    else
	    	values.put(KEY_UNREAD, "false");
	    values.put(KEY_SUBJECT,view.subject);
	    values.put(KEY_FROM,view.from);
	    values.put(KEY_BODY,view.body);	   
	 
	    // Inserting Row
	    long id = db.insert(TABLE_VIEWS, null, values);
	    view.setId(id);
	    db.close(); // Closing database connection
	}
			 
	// Getting single contact
	public JView getJView(int id){
		SQLiteDatabase db = this.getReadableDatabase();
		 
	    Cursor cursor = db.query(TABLE_VIEWS, new String[] { KEY_ID,
	            KEY_INDEX, KEY_UNREAD,KEY_SUBJECT,KEY_FROM,KEY_BODY}, KEY_ID + "=?",
	            new String[] { String.valueOf(id) }, null, null, null, null);
	    if (cursor != null)
	        cursor.moveToFirst();
	 
	    JView view = new JView();
	    view.setId(Integer.parseInt(cursor.getString(0)));
	    view.inboxIndex = Integer.parseInt(cursor.getString(1));
	    if(cursor.getString(2).equals("true"))
	    	view.unread = true;
	    else
	    	view.unread =false;
	    view.subject = cursor.getString(3);
	    view.from = cursor.getString(4);
	    view.body = cursor.getString(5);
	    
	    db.close();
	    
	    return view;
	}
			 
	// Getting All Contacts
	public ArrayList<JView> getAllViews() {
		 ArrayList<JView> viewList = new ArrayList<JView>();
		    // Select All Query
		    String selectQuery = "SELECT  * FROM " + TABLE_VIEWS;
		 
		    SQLiteDatabase db = this.getWritableDatabase();
		    Cursor cursor = db.rawQuery(selectQuery, null);
		 
		    // looping through all rows and adding to list
		    if (cursor.moveToFirst()) {
		        do {
		        	JView view = new JView();
		    	    view.setId(Integer.parseInt(cursor.getString(0)));
		    	    view.inboxIndex = Integer.parseInt(cursor.getString(1));
		    	    if(cursor.getString(2).equals("true"))
		    	    	view.unread = true;
		    	    else
		    	    	view.unread =false;
		    	    view.subject = cursor.getString(3);
		    	    view.from = cursor.getString(4);
		    	    view.body = cursor.getString(5);
		    	    
		            // Adding contact to list
		            viewList.add(view);
		        } while (cursor.moveToNext());
		    }
		    for(int i=0;i<viewList.size();i++){
		    	int highestIndex = i;
		    	for(int j=i;j<viewList.size();j++){
		    		if(viewList.get(j).inboxIndex > viewList.get(highestIndex).inboxIndex)
		    			highestIndex = j;
		    	}
		    	JView mover = viewList.get(highestIndex);
		    	viewList.remove(highestIndex);
		    	viewList.add(i,mover);
		    }
		    db.close();
		    // return contact list
		    return viewList;
	}
			 
	// Getting views Count
	public int getViewCount() {
		String countQuery = "SELECT  * FROM " + TABLE_VIEWS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();
        db.close();
        return cursor.getCount();
	}
			
	// Updating single contact
	public int updateView(JView view) {
		SQLiteDatabase db = this.getWritableDatabase();
		 
		ContentValues values = new ContentValues();
	    values.put(DatabaseHelper.KEY_INDEX,view.inboxIndex);
	    if(view.unread)
	    	values.put(DatabaseHelper.KEY_UNREAD,"true");
	    else
	    	values.put(DatabaseHelper.KEY_UNREAD, "false");
	    values.put(DatabaseHelper.KEY_SUBJECT,view.subject);
	    values.put(DatabaseHelper.KEY_FROM,view.from);
	    values.put(DatabaseHelper.KEY_BODY,view.body);	   
	    
	    //updating row
	    int ret =  db.update(TABLE_VIEWS, values, KEY_ID + " = ?", new String[] { String.valueOf(view.getId()) });      
	    db.close();
	    
	    return ret;
	}
			 
	// Deleting single contact
	public void deleteView(JView view) {
		SQLiteDatabase db = this.getWritableDatabase();
	    db.delete(TABLE_VIEWS, KEY_ID + " = ?",
	            new String[] { String.valueOf(view.getId()) });
	    db.close();
	}
}
