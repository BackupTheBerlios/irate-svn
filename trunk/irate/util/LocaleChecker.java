//package util;
/** Copyright 2004 Taras Glek(is this right?) */
import java.util.*;
import java.io.*;

public class LocaleChecker {

	public static void main(String arg[]) {
		if(arg.length == 0) {
			System.out.println("This program scans for missing translations\n Usage is:\nlocalechecker dir1 dir2 ...");
		}
		
		for(int i=0;i<arg.length;i++) {
			String dir = arg[i];
			String sub[] = new File(dir).list();
			Hashtable/*<String, Properties>*/ translations=new Hashtable();
			Properties master = null;
			
			for(int j=0;j<sub.length;j++) {
				String f = sub[j];
				String prefix = "locale",
					suffix = ".properties";
				int pos;
				if(f.indexOf(prefix) != 0 || (pos=f.indexOf(suffix)) + suffix.length() != f.length())
					continue;
				
				pos = f.indexOf("_");
				//load the master file
				if(pos == -1) {
					master = new Properties();
					try{
						master.load(new FileInputStream(new File(dir + "/"+f)));
					}catch(Exception e) {
						System.err.println(e);
					}
					continue;
				}
				//good
				String locale = f.substring(pos+1, pos+3);
				Properties p = new Properties();
				try {
					p.load(new FileInputStream(new File(dir + "/"+f)));
				}catch(Exception e) {
					System.err.println(e);
				}
				translations.put(locale, p);
			}
			//now validate
			Enumeration keys=translations.keys();
			while(keys.hasMoreElements()) {
				String locale = (String)keys.nextElement();
				Properties lprop = (Properties)translations.get(locale);
				Enumeration names = master.keys();
				String output = "";
				while(names.hasMoreElements()) {
					String key = (String)names.nextElement();
					String value = lprop.getProperty(key);
					if(value == null) {
						if(output.length() == 0)
							output="in "+dir+" locale "+locale+" is missing:\n";
						output+=key+"="+master.getProperty(key)+"\n";
					}
				}
				if(output.length()>0)
					System.out.println(output);
			}
		
		}
	}
}