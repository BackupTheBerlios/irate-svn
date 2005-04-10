package javazoom.jlGui;

/**
 * BasicPlayerListener.
 *
 *-----------------------------------------------------------------------
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published
 *   by the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */

/**
 * BasicPlayerListener.
 * This interface defines method that a player should implement to be notify
 * from Audio events.
 *
 * @author	E.B from JavaZOOM
 *
 * Homepage : http://www.javazoom.net
 */
public interface BasicPlayerListener
{
  public void updateCursor(int cursor, int total);

  public void updateMediaData(byte[] data);

  public void updateMediaState(String state);
}
