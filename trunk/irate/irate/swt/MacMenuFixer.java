// Copyright 2004 by Brion Vibber <brion@pobox.com>
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or 
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
// http://www.gnu.org/copyleft/gpl.html

// SWT on Mac OS X does this really cute thing where whenever you switch
// windows it changes the main menu bar to the menu bar of whatever the
// active window is. If said window is a dialog box with no menu bar, this
// is pretty ugly -- it disappears all your menus.
//
// Conventionally on the Macintosh your whole application shares a single
// menu bar (that's why it's at the top of the screen, not on a window!)
// When in a window where some operations ar enot supported, you gray out
// those items/menus but they're still _there_. Also, the Edit menu is
// normally present and functional when a dialog box is active.
//
// This horrible hack will make an all-disabled copy of your main window's
// menu bar for each dialog you make. If not running on Mac OS X it'll return
// NULLs so it should be safe to use on all OSs.

package irate.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

class MacMenuFixer {
	static Menu mainMenuBar;
	
	// If we're not on Mac OS X, don't bother locking the menu bar item
	static public void setMenuBar(Menu menubar) {
	    if(System.getProperty("os.name").toLowerCase().startsWith("mac os x"))
			mainMenuBar = menubar;
	}
	
	static public Menu getMenuBar() {
		return mainMenuBar;
	}
	
	// If on a Mac, make a copy of the menu bar for the given shell
	// and gray out all the items. Otherwise return null.
	static public Menu grayMenuBar(Shell shell) {
	    if(mainMenuBar == null)
	    	return null;
	    if(!System.getProperty("os.name").toLowerCase().startsWith("mac os x"))
	    	return null;
		Menu gray = new Menu(shell, mainMenuBar.getStyle());
	    return copyGrayMenu(gray, mainMenuBar);
	}
	
	static Menu copyGrayMenu(Menu gray, Menu menu) {
		for(int i = 0; i < menu.getItemCount(); i++) {
			MenuItem item = menu.getItem(i);
			MenuItem grayItem = new MenuItem(gray, item.getStyle());
			grayItem.setText(item.getText());
			
			if((item.getStyle() & SWT.CASCADE) != 0) {
				// Submenu -- copy it.
				Menu submenu = new Menu(grayItem);
				copyGrayMenu(submenu, item.getMenu());
				grayItem.setMenu(submenu);
			} else {
				// Single item -- gray it out.
				grayItem.setEnabled(false);
			}
		}
		return gray;
	}

}
