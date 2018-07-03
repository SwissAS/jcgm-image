package net.sf.jcgm.imageio.plugins.cgm;

import java.awt.Dimension;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import net.sf.jcgm.core.CGM;
import net.sf.jcgm.core.CGMDisplay;
import net.sf.jcgm.core.Message;

class CGMZImageReader extends ImageReader {

	private CGM cgm;
	private Dimension size;
	private CGMMetadata metadata;

	public CGMZImageReader(ImageReaderSpi originatingProvider) {
		super(originatingProvider);
		this.cgm = null;
	}

	@Override
	public int getHeight(int imageIndex) throws IIOException {
		checkIndex(imageIndex);
		readHeader();
		return this.size.height;
	}

	@Override
	public IIOMetadata getImageMetadata(int imageIndex) throws IIOException {
		if (this.metadata != null) {
			return this.metadata;
		}
		checkIndex(imageIndex);
		readHeader();
		this.metadata = new CGMMetadata();
		return this.metadata;
	}

	@Override
	public ImageReadParam getDefaultReadParam() {
		return new CGMImageReadParam();
	}

	@Override
	public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) {
		checkIndex(imageIndex);
		int datatype = 0;
		ColorSpace rgb = ColorSpace.getInstance(1000);
		int bandOffsets[] = new int[3];
		bandOffsets[0] = 0;
		bandOffsets[1] = 1;
		bandOffsets[2] = 2;
		ImageTypeSpecifier imageType = ImageTypeSpecifier.createInterleaved(rgb, bandOffsets, datatype, false, false);
		List<ImageTypeSpecifier> list = new ArrayList<>(1);
		list.add(imageType);
		return list.iterator();
	}

	@Override
	public int getNumImages(boolean allowSearch) {
		return 1;
	}

	@Override
	public IIOMetadata getStreamMetadata() {
		return null;
	}

	@Override
	public int getWidth(int imageIndex) throws IIOException {
		checkIndex(imageIndex);
		readHeader();
		return this.size.width;
	}

	@Override
	public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException {
		checkIndex(imageIndex);
		if (this.cgm == null) {
			this.cgm = new CGM();
			this.cgm.read(new DataInputStream(
					new GZIPInputStream(new ImageInputToStreamAdapter((ImageInputStream) this.input))));
		}
		if (param instanceof CGMImageReadParam) {
			CGMImageReadParam cgmParam = (CGMImageReadParam) param;
			cgmParam.setMessages(this.cgm.getMessages());
		}
		Message m;
		for (Iterator<Message> iterator = this.cgm.getMessages().iterator(); iterator
				.hasNext(); processWarningOccurred(m.toString())) {
			m = iterator.next();
		}

		this.size = param.canSetSourceRenderSize() ? param.getSourceRenderSize() : null;
		if (this.size == null) {
			double dpi;
			if (param instanceof CGMImageReadParam) {
				dpi = ((CGMImageReadParam) param).getDPI();
			} else {
				dpi = (new CGMImageReadParam()).getDPI();
			}
			this.size = this.cgm.getSize(dpi);
			if (this.size == null) {
				this.size = new Dimension(600, 400);
			}
		}
		BufferedImage destination = getDestination(param, getImageTypes(0), this.size.width, this.size.height);
		CGMDisplay display = new CGMDisplay(this.cgm);
		java.awt.Graphics graphics = destination.getGraphics();
		display.scale(graphics, this.size.width, this.size.height);
		display.paint(graphics);
		return destination;
	}

	private void checkIndex(int imageIndex) {
		if (imageIndex != 0) {
			throw new IndexOutOfBoundsException("bad index");
		}
	}

	private void readHeader() throws IIOException {
		if (this.cgm != null) {
			return;
		}
		if (!(this.input instanceof ImageInputStream)) {
			throw new IllegalStateException("stream is not set or wrong type");
		}
		ImageInputStream stream = (ImageInputStream) this.input;
		try {
			this.cgm = new CGM();
			this.cgm.read(stream);
			this.size = this.cgm.getSize();
		} catch (IOException e) {
			throw new IIOException("IOException", e);
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		this.cgm = null;
	}

}
