package simpleHttpServer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.xml.bind.DatatypeConverter;

public class FileData {
	private File file;
	private byte[] fileContentAscii;
	private byte[] fileContentUtf8;
	private byte[] fileContentOther;
	private String etag;

	private FileType fileType;

	private EncodeType encodeType;

	public EncodeType getEncodeType() {
		return encodeType;
	}

	public FileData(String filePath) {
		initialiseData(filePath);
	}

	private void initialiseData(String filePath) {
		file = new File(filePath);
		try {
			BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));
			byte[] byteFileContent = new byte[input.available()];
			input.read(byteFileContent);
			input.close();
			CharsetDecoder utf8Decoder = StandardCharsets.UTF_8.newDecoder();
			CharsetDecoder asciiDecoder = StandardCharsets.US_ASCII.newDecoder();
			utf8Decoder.reset();
			asciiDecoder.reset();

			if (identify(byteFileContent, utf8Decoder)) {
				encodeType = EncodeType.UTF8;
			} else if (identify(byteFileContent, asciiDecoder)) {
				encodeType = EncodeType.ASCII;
			} else {
				encodeType = EncodeType.UNSUPPORTED;
			}
			fileType = FileType.getContentTypeByExtension(getFileExtension());

			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(byteFileContent);
			md.update(getFilePath().toString().getBytes(StandardCharsets.UTF_8));
			etag = DatatypeConverter.printHexBinary(md.digest());
			fileContentOther = byteFileContent;
		} catch (IOException e) {
			file = null;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

	}

	private boolean identify(byte[] bytes, CharsetDecoder decoder) {
		try {
			decoder.decode(ByteBuffer.wrap(bytes));
		} catch (CharacterCodingException e) {
			return false;
		}
		return true;
	}

	public Path getFilePath() {
		return file.toPath();
	}

	public String getFileName() {
		return file.getName();
	}

	public String getFileExtension() {
		int i = file.getName().lastIndexOf('.');
		int p = Math.max(file.getName().lastIndexOf('/'), file.getName().lastIndexOf('\\'));
		return i > p ? file.getName().substring(i + 1) : "";

	}

	public ByteBuffer getFileContentAscii() {
		if (!fileType.equals(FileType.Text)) {
			return null;
		}
		if (fileContentAscii != null || encodeType.equals(EncodeType.ASCII)) {
			fileContentAscii = fileContentOther;
			return ByteBuffer.wrap(fileContentAscii);
		}
		return convertToCharset(StandardCharsets.UTF_8, StandardCharsets.US_ASCII, ByteBuffer.wrap(fileContentOther));
	}

	public ByteBuffer getFileContentUtf8() {
		if (!fileType.equals(FileType.Text)) {
			return null;
		}
		if (fileContentUtf8 != null || encodeType.equals(EncodeType.UTF8)) {
			fileContentUtf8 = fileContentOther;
			return ByteBuffer.wrap(fileContentUtf8);
		}
		return convertToCharset(StandardCharsets.US_ASCII, StandardCharsets.UTF_8, ByteBuffer.wrap(fileContentOther));
	}

	private ByteBuffer convertToCharset(Charset fromCharset, Charset toCharset, ByteBuffer inputBuffer) {
		return toCharset.encode(fromCharset.decode(inputBuffer));
	}

	public ByteBuffer getfileContentOther() {
		return ByteBuffer.wrap(fileContentOther);
	}

	public String getEtag() {
		return etag;
	}

	public ByteBuffer getFileContentWithCharset(String charset) {
		if (EncodeType.isAscii(charset)) {
			return getFileContentAscii();
		}
		if (EncodeType.isUtf8(charset)) {
			return getFileContentUtf8();
		}
		return null;
	}

	public boolean fileIsFound() {
		return file != null;
	}

}
