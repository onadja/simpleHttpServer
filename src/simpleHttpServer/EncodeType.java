package simpleHttpServer;

enum EncodeType {
	UTF8("utf-8"), ASCII("us-ascii"), UNSUPPORTED("unsupported");

	private String text;

	EncodeType(String text) {
		this.text = text.toLowerCase();
	}

	static boolean isSupported(String text) {
		return isAscii(text) || isUtf8(text);
	}

	static boolean isAscii(String text) {
		return text.toLowerCase().equals(EncodeType.ASCII.text);
	}

	static boolean isUtf8(String text) {
		return text.toLowerCase().equals(EncodeType.UTF8.text);
	}

	public String getText() {
		return text;
	}

};