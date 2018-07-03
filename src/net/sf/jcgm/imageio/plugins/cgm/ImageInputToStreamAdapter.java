package net.sf.jcgm.imageio.plugins.cgm;

import java.io.IOException;
import java.io.InputStream;

import javax.imageio.stream.ImageInputStream;

public class ImageInputToStreamAdapter extends InputStream {

	private ImageInputStream imageInput;

	public ImageInputToStreamAdapter(ImageInputStream in) {
		this.imageInput = in;
	}

	@Override
	public int read() throws IOException {
		return this.imageInput.read();
	}

	@Override
	public int read(byte[] b) throws IOException {
		return this.imageInput.read(b);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return this.imageInput.read(b, off, len);
	}

	@Override
	public int available() throws IOException {
		long len = this.imageInput.length();
		return len >= 0L ? (int) len : 0;
	}

	@Override
	public boolean markSupported() {
		return true;
	}

	@Override
	public void mark(int readlimit) {
		this.imageInput.mark();
	}

	@Override
	public void reset() throws IOException {
		this.imageInput.reset();
	}

	@Override
	public long skip(long n) throws IOException {
		return this.imageInput.skipBytes(n);
	}

	@Override
	public void close() throws IOException {
		this.imageInput.close();
	}
}
