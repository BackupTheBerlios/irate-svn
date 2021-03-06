/*
 * 24 May 2004 - This code has been lifted from JavaLayer project and stripped
 * down by Stephen Blackheath for the iRate project.
 *
 * 12/12/99		Initial version.	mdm@techie.com
 *-----------------------------------------------------------------------
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
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */

package irate.plugin.autonormalize;

/**
 * This interface describes all error codes that can be thrown 
 * in <code>BistreamException</code>s.
 * 
 * @see BitstreamException
 * 
 * @author	MDM		12/12/99
 * @since	0.0.6
 */

public interface BitstreamErrors extends JavaLayerErrors
{
	
	/**
	 * An undeterminable error occurred. 
	 */
	static public final int UNKNOWN_ERROR = BITSTREAM_ERROR + 0;
	
	/**
	 * The header describes an unknown sample rate.
	 */
	static public final int UNKNOWN_SAMPLE_RATE = BITSTREAM_ERROR + 1;

	/**
	 * A problem occurred reading from the stream.
	 */
	static public final int STREAM_ERROR = BITSTREAM_ERROR + 2;
	
	/**
	 * The end of the stream was reached prematurely. 
	 */
	static public final int UNEXPECTED_EOF = BITSTREAM_ERROR + 3;
	
	/**
	 * The end of the stream was reached. 
	 */
	static public final int STREAM_EOF = BITSTREAM_ERROR + 4;
	
	/**
	 * 
	 */
	static public final int BITSTREAM_LAST = 0x1ff;
	
}
