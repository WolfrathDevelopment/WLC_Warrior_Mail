package warrior.mail.namespace;

import java.util.ArrayList;
import java.util.Properties;

import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class OutlookClient {
	public static final int CONNECTION_SUCCESSFUL = 0;
	public static final int CONNECTION_FAILED_NETWORK = 1;
	public static final int CONNECTION_FAILED_CREDENTIALS = 2;
	
	private final String HOST = "pod51000.outlook.com";
	Properties props;
	Session session;
	Store store;
	Folder inbox;
	int inboxCount,currentCount;
	boolean isConnected;
	private SMTPAuthenticator auth;
	MessagingService service;
	
	public OutlookClient(MessagingService service){
		this.service = service;
		inboxCount = 0; currentCount=0;
		isConnected = false;
	}
	
	public int connect(){
		if(service.username == null || service.password == null)
			service.grabCredentials();
		props = new Properties();
		auth = new SMTPAuthenticator(service.username,service.password);
		System.setProperty("javax.net.debug","sll,handshake");
		props.setProperty("mail.imaps.port", "993");
		props.put("mail.transport.protocol", "smtp");
        props.setProperty("mail.smtp.ssl.trust", "pod51000.outlook.com");
        props.put("mail.smtp.starttls.enable","true");
        props.put("mail.smtp.host","pod51000.outlook.com"); 
        props.put("mail.smtp.port","587");
        props.put("mail.smtp.auth", "true");
        props.setProperty("mail.smtp.auth.ntlm.domain", "wlc.edu");
        props.setProperty("mail.smtp.starttls.required", "true");
        props.setProperty("mail.smtp.auth.mechanisms", "LOGIN NTLM");
        props.setProperty("mail.user",service.username);
        props.setProperty("mail.password",service.password);
		session = Session.getInstance(props,auth);
		try{
			store = session.getStore("imaps");
		} 
		catch (NoSuchProviderException e) {
			e.printStackTrace();
		}
		try {
			store.connect(HOST,993,service.username,service.password);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		
		try{
			inbox = store.getFolder("INBOX");
		} 
		catch (MessagingException e) {}
		openFolder(inbox);
		isConnected = store.isConnected();
		
		if(isConnected)
			return OutlookClient.CONNECTION_SUCCESSFUL;
		else if(isNetworkAvailable())
			return OutlookClient.CONNECTION_FAILED_CREDENTIALS;
		
		return OutlookClient.CONNECTION_FAILED_NETWORK;
	}
	
	public void setInboxCount(int set){
		inboxCount = set;
	}
	public void openFolder(Folder folder)
	{
		try {
			folder.open(Folder.READ_WRITE);
			} catch (MessagingException e) {}
	}
	
	public void closeFolder(Folder folder, boolean expunge){
		/*
		try {
			folder.close(expunge);
			} catch (MessagingException e) {}
			*/
	}
	
	public String getContent(Message m){
		ContentParser cparse = new ContentParser();
		return ContentParser.fetchText(m, cparse.getInstance(),false,true);
		/*
		String ret = "";
		try{	
			Object content = m.getContent();
			if (content instanceof String)
				ret = (String) content;
			else if (content instanceof Multipart) {
				Multipart multipart = (Multipart) content;
				for(int j=0;j<multipart.getCount();j++){
					BodyPart part = multipart.getBodyPart(j);
					if(part.getContent() instanceof Multipart)
						continue;
					else if(part.getContent() instanceof BASE64DecoderStream)
						continue;
					else if(part.getContent() instanceof IMAPInputStream)
						continue;
					else if(part.getContent() instanceof IMAPNestedMessage)
						continue;
					ret += (String) part.getContent() + "\n";
				}
			}
		}
			catch(MessagingException e){} catch (IOException e) {
				e.printStackTrace();
			}
		ret = android.text.Html.fromHtml(ret).toString();
		return ret;
		*/
	}
	
	public ArrayList<JView> retrieveInbox(){
		if(!inbox.isOpen())
			openFolder(inbox);
		FetchProfile fetcher = new FetchProfile();
		fetcher.add(FetchProfile.Item.ENVELOPE);
		fetcher.add(FetchProfile.Item.CONTENT_INFO);
		fetcher.add(FetchProfile.Item.FLAGS);
		
		ArrayList<JView> list = new ArrayList<JView>();
		Message[] downloaded = null;
		int messageCount=0;
		
		try {
			inboxCount = messageCount = inbox.getMessageCount();
			if(messageCount > 10){
				downloaded = inbox.getMessages(messageCount-10,messageCount);
				messageCount = 10;
			}
			else
				downloaded = inbox.getMessages();
			inbox.fetch(downloaded,fetcher);
			
			Log.v("LOGGER COUNT",""+messageCount);
			
			
		} catch (MessagingException e) {}
		
		try{
			for(int i=0;i<messageCount;i++){
				JView note = new JView();
				Message temp = downloaded[messageCount-i-1];
				note.inboxIndex = messageCount-i-1;
				note.body = getContent(temp);
				if(temp.isSet(Flags.Flag.RECENT))
					note.unread = true;
				else
					note.unread = false;
				note.from= "" + temp.getFrom()[0];
				String f="";
	        	for(int j=0;j<note.from.length()-1;j++){
	        		if(note.from.charAt(j) == '<')
	        			break;
	        		f += note.from.charAt(j);
	        	}
				note.from = f;
				Log.v("DOWNLOADED",f);
				note.subject = temp.getSubject();
				list.add(note);
			}
			
			closeFolder(inbox,false);
		}catch (MessagingException e) {
			e.printStackTrace();
		}
		
		return list;
	}
	
	public JView getNewMessage()
	{
		Message message = null;
		JView update = new JView();
		if(!store.isConnected()){
			try {
				store.connect(service.username,service.password);
			} catch (MessagingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		if(!inbox.isOpen())
			openFolder(inbox);
		Log.v("LOGGER","REFRESH INBOX FUNCTION");
		try {
			currentCount = inbox.getMessageCount();
		} catch (MessagingException e1) {
			
			e1.printStackTrace();
		}
			try{
				inboxCount++;
				message = inbox.getMessage(inboxCount);
				update.inboxIndex = inboxCount;
				update.subject = message.getSubject();
				String from= "" + message.getFrom()[0];
				update.body = getContent(message);
				String f="";
				for(int j=0;j<from.length()-1;j++){
					if(from.charAt(j) == '<')
						break;
					f += from.charAt(j);
				}
				update.from = f;
				update.unread = true;
				//isUpdating = false;
			} catch (MessagingException e) {}
		
		//closeFolder(inbox,false);
		
		return update;
	}
		
	public boolean hasNewMessages()
	{
		if(store == null || !store.isConnected() ){
			/*
			try {
				store.connect(account.username,account.password);
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
			return false;
		}
		
		if(!inbox.isOpen())
			openFolder(inbox);
		boolean ret = false;
		try {
			currentCount = inbox.getMessageCount();
		} catch (MessagingException e1) {
			e1.printStackTrace();
		}
		Log.v("INBOXCOUNT",inboxCount+"");
		Log.v("NewCount",currentCount+"");
		if(inboxCount < currentCount){
			Log.v("LOGGER","DETECTED NEW MESSAGE");
			ret = true;
		}
		if(inboxCount>currentCount)
			inboxCount = currentCount;
		closeFolder(inbox,false);
		return ret;
	}
	
	public boolean delete(int which){
		if(!inbox.isOpen())
			openFolder(inbox);
		/*
		try {
			account.adapter.getItem(which).current.setFlag(Flags.Flag.DELETED,true);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		boolean success = false;
		try {
			success = account.adapter.getItem(which).current.isSet(Flags.Flag.DELETED);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		inboxCount--;
		closeFolder(inbox,true);
		//return success;
		return true;
	}
	
	public void markRead(int which){
		if(!inbox.isOpen())
			openFolder(inbox);
		/*
		try {
			account.adapter.getItem(which).current.setFlag(Flags.Flag.SEEN,true);
		} catch (MessagingException e) {
	
		}
		*/
		//.getItem(which).unread = false;
		closeFolder(inbox,true);
	}

	public void disconnect(){
		if(inbox.isOpen())
			closeFolder(inbox,true);
		try {
			store.close();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		isConnected = false;
	}
	
	public boolean sendMessage(InternetAddress from,InternetAddress[] to,String sub,String body){
		Message outgoing = new MimeMessage(session);
		try {
			outgoing.setFrom(from);
			outgoing.setRecipients(Message.RecipientType.TO,to);
			outgoing.setContent(body,"text/plain");
			outgoing.setSubject(sub);
		} catch (MessagingException e1) {
			Log.v("LOGGER","MESSAGINGEXCEPTION");
			return false;
		}
		
		try {
			Transport.send(outgoing);
		} catch (MessagingException e) {
			Log.v("LOGGER","MESSAGINGEXCEPTION");
			Log.e("STACK","TRACE",e);
			//tell user it failed
			return false;
		}
		return true;
	}
	
	public void synchronize(){
		
	}
	
	private boolean isNetworkAvailable(){     
		ConnectivityManager connectivityManager = (ConnectivityManager) service.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		WifiManager wm = (WifiManager) service.getSystemService(Context.WIFI_SERVICE);
			
		return (activeNetworkInfo != null) || wm.getWifiState() == WifiManager.WIFI_STATE_ENABLED;
	}
	
	private class SMTPAuthenticator extends javax.mail.Authenticator{
		String user;
		String pass;
		public SMTPAuthenticator(String u,String p){
			super();
			this.user = u;
			this.pass = p;
		}
        public PasswordAuthentication getPasswordAuthentication() {
           return new PasswordAuthentication(user,pass);
        }
    }

}
