/*
 * 24 May 2004 - This code has been lifted from JavaLayer project and stripped
 * down by Stephen Blackheath for the iRate project.
 *
 * 01/12/99		Initial version.	mdm@techie.com
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
 * This interface provides constants describing the error
 * codes used by the Decoder to indicate errors. 
 * 
 * @author	MDM
 */
public interface DecoderErrors extends JavaLayerErrors
{
	
	static public final int UNKNOWN_ERROR = DECODER_ERROR + 0;
	
	/**
	 * Layer not supported by the decoder. 
	 */
	static public final int UNSUPPORTED_LAYER = DECODER_ERROR + 1;
}
