package com.tomgibara.crinch.bits;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileBitReaderFactory {

	public static final int DEFAULT_BUFFER_SIZE = 8192;
	
	public enum Mode {
		MEMORY,
		CHANNEL,
		STREAM
	}

	private final Mode mode;
	private final File file;
	private final int bufferSize;
	private byte[] bytes = null;

	public FileBitReaderFactory(File file, Mode mode) {
		this(file, mode, DEFAULT_BUFFER_SIZE);
	}
	
	public FileBitReaderFactory(File file, Mode mode, int bufferSize) {
		if (file == null) throw new IllegalArgumentException("null file");
		if (mode == null) throw new IllegalArgumentException("null mode");
		if (bufferSize < 1) throw new IllegalArgumentException("non-positive bufferSize");
		this.file = file;
		this.mode = mode;
		this.bufferSize = bufferSize;
	}
	
	public File getFile() {
		return file;
	}
	
	public Mode getMode() {
		return mode;
	}
	
	public int getBufferSize() {
		return bufferSize;
	}
	
	//TODO should be generalized to just BitReader, but setPosition is necessary
	public ByteBasedBitReader newReader() {
		try {
			switch(mode) {
			case MEMORY : return new ByteArrayBitReader(getBytes());
			case STREAM : return new InputStreamBitReader(new FileInputStream(file));
			case CHANNEL: return new FileChannelBitReader(new RandomAccessFile(file, "r").getChannel(), bufferSize, true);
			default: throw new IllegalStateException("Unexpected mode: " + mode);
			}
		} catch (IOException e) {
			throw new BitStreamException(e);
		}
	}

	private byte[] getBytes() throws IOException {
		synchronized (this) {
			if (bytes == null) {
				int size = (int) file.length();
				bytes = new byte[size];
				FileInputStream in = null;
				try {
					in = new FileInputStream(file);
					new DataInputStream(in).readFully(bytes);
				} finally {
					try {
						in.close();
					} catch (IOException e) {
						System.err.println("Failed to close file! " + file);
					}
				}
			}
			return bytes;
		}
	}
}
