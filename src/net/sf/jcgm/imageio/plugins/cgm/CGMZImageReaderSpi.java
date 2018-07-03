package net.sf.jcgm.imageio.plugins.cgm;

import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

public class CGMZImageReaderSpi extends ImageReaderSpi {
	static final String vendorName = "Swiss AviationSoftware Ltd.";
	static final String version = "1";
	static final String readerClassName = "net.sf.jcgm.imageio.plugins.cgm.CGMZImageReader";
	static final String[] names = { "CGMZ" };
	static final String[] suffixes = { "cgmz", "cgm.gz" };
	static final String[] writerSpiNames = null;
	static final boolean supportsStandardStreamMetadataFormat = false;
	static final String nativeStreamMetadataFormatName = null;
	static final String nativeStreamMetadataFormatClassName = null;
	static final String[] extraStreamMetadataFormatNames = null;
	static final String[] extraStreamMetadataFormatClassNames = null;
	static final boolean supportsStandardImageMetadataFormat = false;
	static final String nativeImageMetadataFormatName = "net.sf.jcgm.imageio.plugins.cgm.CGMMetadata_1.0";
	static final String nativeImageMetadataFormatClassName = "net.sf.jcgm.imageio.plugins.cgm.CGMMetadata";
	static final String[] extraImageMetadataFormatNames = null;
	static final String[] extraImageMetadataFormatClassNames = null;

	public CGMZImageReaderSpi() {
		super(vendorName, version, names, suffixes, net.sf.jcgm.core.MIMETypes.CGM_MIME_Types, readerClassName,
				STANDARD_INPUT_TYPE, writerSpiNames, false, nativeStreamMetadataFormatName,
				nativeStreamMetadataFormatClassName, extraStreamMetadataFormatNames,
				extraStreamMetadataFormatClassNames, false, nativeImageMetadataFormatName,
				nativeImageMetadataFormatClassName, extraImageMetadataFormatNames, extraImageMetadataFormatClassNames);
	}

	@Override
	public boolean canDecodeInput(Object input) {
		if (!(input instanceof ImageInputStream)) {
			return false;
		}
		ImageInputStream imageInput = (ImageInputStream) input;
		byte[] magicCode = new byte[2];
		try {
			imageInput.mark();

			PushbackInputStream stream = new PushbackInputStream(new ImageInputToStreamAdapter(imageInput), 2);
			stream.read(magicCode);
			stream.unread(magicCode);
			if ((magicCode[0] == 31) && (magicCode[1] + 256 == 139)) {
				stream = new PushbackInputStream(new GZIPInputStream(stream), 2);
				stream.read(magicCode);
				stream.unread(magicCode);

				return (magicCode[0] == 0) && ((magicCode[1] & 0xE0) == 32);
			}
		} catch (Exception localException) {
		} finally {
			try {
				imageInput.reset();
			} catch (IOException localIOException2) {
			}
		}
		try {
			imageInput.reset();
		} catch (IOException localIOException3) {
		}
		return false;
	}

	@Override
	public ImageReader createReaderInstance(Object extension) {
		return new CGMZImageReader(this);
	}

	@Override
	public String getDescription(Locale locale) {
		return "CGM (Computer Graphics Metafile) Image Reader gzipped";
	}
}
