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
 * The <code>Decoder</code> class encapsulates the details of
 * decoding an MPEG audio frame. 
 * 
 * @author	MDM	
 * @version 0.0.7 12/12/99
 * @since	0.0.5
 */
public class Decoder implements DecoderErrors
{
    private Averager averager;

	static private final Params DEFAULT_PARAMS = new Params();
	
	/**
	 * The Bistream from which the MPEG audio frames are read.
	 */
	//private Bitstream				stream;
	
	/**
	 * The decoder used to decode layer III frames.
	 */
	private LayerIIIDecoder			l3decoder;
	/*
        private LayerIIDecoder			l2decoder;
	private LayerIDecoder			l1decoder;
        */

	private int						outputFrequency;
	private int						outputChannels;
	
	private Params					params;
	
	private boolean					initialized;
		
	
	/**
	 * Creates a new <code>Decoder</code> instance with default 
	 * parameters.
	 */
	
	public Decoder(Averager averager)
	{
		this(null, averager);
	}

	/**
	 * Creates a new <code>Decoder</code> instance with default 
	 * parameters.
	 * 
	 * @param params	The <code>Params</code> instance that describes
	 *					the customizable aspects of the decoder.  
	 */
	public Decoder(Params params0, Averager averager)
	{
            this.averager = averager;
		if (params0==null)
			params0 = DEFAULT_PARAMS;
	
		params = params0;
	}
	
	static public Params getDefaultParams()
	{
		return (Params)DEFAULT_PARAMS.clone();
	}
	
	/**
	 * Decodes one frame from an MPEG audio bitstream.
	 * 
	 * @param header		The header describing the frame to decode.
	 * @param bitstream		The bistream that provides the bits for te body of the frame. 
	 * 
	 * @return A SampleBuffer containing the decoded samples.
	 */
	public void decodeFrame(Header header, Bitstream stream)
		throws DecoderException
	{
		if (!initialized)
		{
			initialize(header);
		}
		
		int layer = header.layer();
		
		FrameDecoder decoder = retrieveDecoder(header, stream, layer);
		
		decoder.decodeFrame();
	}
	
	/**
	 * Retrieves the sample frequency of the PCM samples output
	 * by this decoder. This typically corresponds to the sample
	 * rate encoded in the MPEG audio stream.
	 * 
	 * @param the sample rate (in Hz) of the samples written to the
	 *		output buffer when decoding. 
	 */
	public int getOutputFrequency()
	{
		return outputFrequency;
	}
	
	/**
	 * Retrieves the number of channels of PCM samples output by
	 * this decoder. This usually corresponds to the number of
	 * channels in the MPEG audio stream, although it may differ.
	 * 
	 * @return The number of output channels in the decoded samples: 1 
	 *		for mono, or 2 for stereo.
	 *		
	 */
	public int getOutputChannels()
	{
		return outputChannels;	
	}
	
	protected DecoderException newDecoderException(int errorcode)
	{
		return new DecoderException(errorcode, null);
	}
	
	protected DecoderException newDecoderException(int errorcode, Throwable throwable)
	{
		return new DecoderException(errorcode, throwable);
	}
	
	protected FrameDecoder retrieveDecoder(Header header, Bitstream stream, int layer)
		throws DecoderException
	{
		FrameDecoder decoder = null;
		
		// REVIEW: allow channel output selection type
		// (LEFT, RIGHT, BOTH, DOWNMIX)
		switch (layer)
		{
		case 3:
			if (l3decoder==null)
			{
				l3decoder = new LayerIIIDecoder(stream, 
					header, OutputChannels.BOTH_CHANNELS, averager);
			}						
			
			decoder = l3decoder;
			break;
                        /* To do!
		case 2:
			if (l2decoder==null)
			{
				l2decoder = new LayerIIDecoder();
				l2decoder.create(stream, 
					header, filter1, filter2, 
					output, OutputChannels.BOTH_CHANNELS);				
			}
			decoder = l2decoder;
			break;
		case 1:
			if (l1decoder==null)
			{
				l1decoder = new LayerIDecoder();
				l1decoder.create(stream, 
					header, filter1, filter2, 
					output, OutputChannels.BOTH_CHANNELS);				
			}
			decoder = l1decoder;
			break;
                */
		}
						
		if (decoder==null)
		{
			throw newDecoderException(UNSUPPORTED_LAYER, null);
		}
		
		return decoder;
	}
	
	private void initialize(Header header)
		throws DecoderException
	{
		
		// REVIEW: allow customizable scale factor
		float scalefactor = 32700.0f;
		
		int mode = header.mode();
		int layer = header.layer();
		int channels = mode==Header.SINGLE_CHANNEL ? 1 : 2;

					
		outputChannels = channels;
		outputFrequency = header.frequency();
		
		initialized = true;
	}
	
	/**
	 * The <code>Params</code> class presents the customizable
	 * aspects of the decoder. 
	 * <p>
	 * Instances of this class are not thread safe. 
	 */
	public static class Params implements Cloneable
	{
		private OutputChannels	outputChannels = OutputChannels.BOTH;
		
		public Params()
		{			
		}
		
		public Object clone()
		{
			try
			{
				return super.clone();
			}
			catch (CloneNotSupportedException ex)
			{				
				throw new InternalError(this+": "+ex);
			}
		}
				
		public void setOutputChannels(OutputChannels out)
		{
			if (out==null)
				throw new NullPointerException("out");
			
			outputChannels = out;
		}
		
		public OutputChannels getOutputChannels()
		{
			return outputChannels;
		}
	};
}

