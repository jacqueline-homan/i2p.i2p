/*
 * Created on Sep 02, 2005
 * 
 *  This file is part of susidns project, see http://susi.i2p/
 *  
 *  Copyright (C) 2005 <susi23@mail.i2p>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *  
 * $Revision: 1.3 $
 */

package i2p.susi.dns;

import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.i2p.data.DataHelper;
import net.i2p.util.SecureFileOutputStream;

public class SubscriptionsBean extends BaseBean
{
	private String action, fileName, content, serial, lastSerial;
	
	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getFileName()
	{
		loadConfig();
		
		fileName = ConfigBean.addressbookPrefix + properties.getProperty( "subscriptions", "subscriptions.txt" );
		
		return fileName;
	}

	private void reload()
	{
		File file = new File( getFileName() );
		if( file != null && file.isFile() ) {
			StringBuilder buf = new StringBuilder();
			BufferedReader br = null;
			try {
				br = new BufferedReader( new FileReader( file ) );
				String line;
				while( ( line = br.readLine() ) != null ) {
					buf.append( line );
					buf.append( "\n" );
				}
				content = buf.toString();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (br != null)
					try { br.close(); } catch (IOException ioe) {}
			}
		}
	}
	
	private void save()
	{
		File file = new File( getFileName() );
		try {
			// trim and sort
			List<String> urls = new ArrayList<String>();
                        InputStream in = new ByteArrayInputStream(content.getBytes("UTF-8"));
                        String line;
                        while ((line = DataHelper.readLine(in)) != null) {
				line = line.trim();
                                if (line.length() > 0)
                                    urls.add(line);
			}
			Collections.sort(urls);
			PrintWriter out = new PrintWriter( new SecureFileOutputStream( file ) );
			for (String url : urls) {
				out.println(url);
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getMessages() {
		String message = "";
		if( action != null ) {
                        if (_context.getBooleanProperty(PROP_PW_ENABLE) ||
			    (serial != null && serial.equals(lastSerial))) {
				if (action.equals(_("Save"))) {
					save();
				/*******
					String nonce = System.getProperty("addressbook.nonce");
					if (nonce != null) {	
						// Yes this is a hack.
						// No it doesn't work on a text-mode browser.
						// Fetching from the addressbook servlet
						// with the correct parameters will kick off a
						// config reload and fetch.
				*******/
					if (content != null && content.length() > 2) {
						message = _("Subscriptions saved, updating addressbook from subscription sources now.");
						          // + "<img height=\"1\" width=\"1\" alt=\"\" " +
						          // "src=\"/addressbook/?wakeup=1&nonce=" + nonce + "\">";
						_context.namingService().requestUpdate(null);
					} else {
						message = _("Subscriptions saved.");
					}
				} else if (action.equals(_("Reload"))) {
					reload();
					message = _("Subscriptions reloaded.");
				}
			}			
			else {
				message = _("Invalid form submission, probably because you used the \"back\" or \"reload\" button on your browser. Please resubmit.")
                                          + ' ' +
                                          _("If the problem persists, verify that you have cookies enabled in your browser.");
			}
		}
		if( message.length() > 0 )
			message = "<p class=\"messages\">" + message + "</p>";
		return message;
	}

	public String getSerial()
	{
		lastSerial = "" + Math.random();
		action = null;
		return lastSerial;
	}

	public void setSerial(String serial ) {
		this.serial = serial;
	}

	public void setContent(String content) {
		// will come from form with \r\n line endings
		this.content = content;
	}

	public String getContent()
	{
		if( content != null )
			return content;
		
		reload();
		
		return content;
	}

	/** translate */
	private static String _(String s) {
		return Messages.getString(s);
	}
}
