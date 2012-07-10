package warrior.mail.namespace;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.*;

public class ContentParser {
	public static String fetchText(Part message, textInfo ti, boolean escape, boolean showImages)
	{
	StringWriter sw = new StringWriter(1024);
	boolean html = false;
	
	try{
	    if(message != null && message.getContent() != null){
		if(message.getContent() instanceof Multipart){
		    Multipart parts = (Multipart)message.getContent();
		    BodyPart p;
		    boolean attachment = false;
		    boolean alternative = parts.getContentType().trim().toLowerCase().startsWith("multipart/alternative") ? true : false;
		    
		    InputStreamReader isr;
		    int available, retrieved;
		    char[] buffer = new char[512];
		    for(int i=0;i<parts.getCount();i++){
			p = parts.getBodyPart(i);
			
			if(p.getContentType().toLowerCase().startsWith("multipart")){
			    sw.write(fetchText(p, ti, escape, showImages));
			    break;
			}else if((Part.INLINE.equalsIgnoreCase(p.getDisposition()) 
				  || p.getDisposition() == null) 
				 && p.getContentType().toLowerCase().startsWith("text") &&
				 p.getFileName() == null){
			    
			    if(InputStream.class.isInstance(p.getContent())){
				InputStream ip = p.getInputStream();
				
				StringWriter subwriter = new StringWriter(ip.available());
				isr = new InputStreamReader(ip);
				while(isr.ready()){
				    retrieved = isr.read(buffer, 0, 512);
				    subwriter.write(buffer, 0, retrieved);
				}
				
				if(escape){
				    sw.write(escapeLineBreaksAndSpacesForHTML(subwriter.toString()));
				}else{
				    sw.write(subwriter.toString());
				}
			    }else{
				Object content = p.getContent();
				if(escape){
				    if(java.io.ByteArrayInputStream.class.isInstance(content)){
					int bcount = ((java.io.ByteArrayInputStream)content).available();
					byte[] c = new byte[bcount];
					((java.io.ByteArrayInputStream)content).read(c, 0, bcount);
					sw.write(escapeLineBreaksAndSpacesForHTML(new String(c)));
				    }else{
					sw.write(escapeLineBreaksAndSpacesForHTML(content.toString()));
				    }
				}else{
				    if(java.io.ByteArrayInputStream.class.isInstance(content)){
					int bcount = ((java.io.ByteArrayInputStream)content).available();
					byte[] c = new byte[bcount];
					((java.io.ByteArrayInputStream)content).read(c, 0, bcount);
					sw.write(new String(c));
				    }else{
					sw.write(content.toString());
				    }
				}
			    }
			    
			    if(p.getContentType().toLowerCase().indexOf("html") > 0) html = true;
			    if(alternative && !"".equals(sw.toString().trim())){
				break;
			    }
			    if(escape){
				sw.write("<br/>");
			    }else{
				sw.write("\r\n");
			    }
			}else if(p.getContentType().toLowerCase().startsWith("image") && showImages && !html){
			    // inline image
			    if(escape && message instanceof MimeMessage){
				sw.write("<br/>");
				sw.write("<img src=\"");
				sw.write(String.valueOf(i));
				sw.write("\"><br/>\r\n");
			    }
			}
		    }
		}else if(message.getContentType().toLowerCase().startsWith("text")){
		    if(escape){
			Object content = message.getContent();
			if(java.io.ByteArrayInputStream.class.isInstance(content)){
			    int bcount = ((java.io.ByteArrayInputStream)content).available();
			    byte[] c = new byte[bcount];
			    ((java.io.ByteArrayInputStream)content).read(c, 0, bcount);
			    sw.write(escapeLineBreaksAndSpacesForHTML(new String(c)));
			}else{
			    sw.write(escapeLineBreaksAndSpacesForHTML(content.toString()));
			}
		    }else{
			sw.write(message.getContent().toString());
		    }
		    
		}
	    }else{
		System.err.println("Message or message content is null");
	    }
	}catch(Exception ioe){
	    System.err.println("Exception reading mail: " + ioe.getMessage());
	    ioe.printStackTrace();
	}
	if(ti != null) ti.html = html;
	return sw.toString();
    }

    public static String escapeOutputForHTML(String value)
    {
	if(value != null && (value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0)){
	    return join(StringEscapeUtils.escapeHtml3(value).split("\r\n|\r|\n"), "<br/>\r\n", true);
	}
	return StringEscapeUtils.escapeHtml3(value);
    }

    public static String escapeLineBreaksForHTML(String value)
    {
	if(value != null && (value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0)){
	    return join(value.split("\r\n|\r|\n"), "<br/>\r\n", true);
	}

	return value;
    }

    public static String join(String[] source, String filler, boolean includeEmptyStrings)
    {
	if(source == null) return "";
	if(filler == null) filler = "";
	
	int i, size = 0;
	for(i=0;i<source.length;i++){
	    if(source[i] != null){
		if(i > 0) size += filler.length();
		size += source[i].length();
	    }
	}
	
	//StringBuffer sb = new StringBuffer(size);
	StringBuilder sb = new StringBuilder(size);
	for(i=0;i<source.length;i++){
	    if(source[i] != null && !"".equals(source)){
		sb.append(source[i]);
		sb.append(filler);
	    }else if(includeEmptyStrings){
		sb.append(filler);
	    }
	}
	return sb.toString();
	
    }

public static String escapeLineBreaksAndSpacesForHTML(String value)
    {
	if(value == null) return "";
	String[] lines = value.split("\r\n|\r|\n");
	StringBuilder tb = new StringBuilder(value.length()+5*lines.length);
	int i, j;
	String tline;
	for(i=0;i<lines.length;i++){
	    if(i > 0) tb.append("<br/>\r\n");
	    tline = StringUtils.stripStart(lines[i], " ");
	    j = lines[i].length() - tline.length();
	    if(j > 0) tb.append(StringUtils.repeat("&nbsp;", j));
	    j = tline.length();
	    tline = StringUtils.stripEnd(tline, " ");
	    j -= tline.length();
	    tb.append(tline);
	    if(j > 0) tb.append(StringUtils.repeat("&nbsp;", j));
	}
	return tb.toString();
    }

    public static String escapeLineBreaksSpacesAndEntitiesForHTML(String value)
    {
	if(value == null) return "";
	String[] lines = value.split("\r\n|\r|\n");
	StringBuilder tb = new StringBuilder(value.length()+5*lines.length);
	int i, j;
	String tline;
	for(i=0;i<lines.length;i++){
	    if(i > 0) tb.append("<br/>\r\n");
	    tline = StringUtils.stripStart(lines[i], " ");
	    j = lines[i].length() - tline.length();
	    if(j > 0) tb.append(StringUtils.repeat("&nbsp;", j));
	    j = tline.length();
	    tline = StringUtils.stripEnd(tline, " ");
	    j -= tline.length();
	    //tb.append(StringEscapeUtils.escapeXml(tline));
	    tb.append(StringEscapeUtils.escapeXml(tline));
	    if(j > 0) tb.append(StringUtils.repeat("&nbsp;", j));
	}
	return tb.toString();
    }
    
    public textInfo getInstance(){
    	return new textInfo();
    }
    
    class textInfo{
	boolean html = false;
    }
}
