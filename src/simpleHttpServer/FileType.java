package simpleHttpServer;

import java.util.Arrays;

public enum FileType {
	Text("text/html", new String[] { "html", "htm", "txt" }), Application("application/javascript",
			new String[] { "js" }), Image("image/jpeg", new String[] { "jpg", "jpeg" });

	private String name;
	private String[] extensions;

	private FileType(String name, String[] extensions) {
		this.name = name;
		this.extensions = extensions;
	}

	public String getName() {
		return name;
	}

	public String[] getExtensions() {
		return extensions;
	}

	public static FileType getContentTypeByExtension(String extension) {
		if (Arrays.asList(FileType.Text.getExtensions()).contains(extension)) {
			return FileType.Text;
		} else if (Arrays.asList(FileType.Application.getExtensions()).contains(extension)) {
			return FileType.Application;
		} else if (Arrays.asList(FileType.Image.getExtensions()).contains(extension)) {
			return FileType.Image;
		} else {
			return null;
		}
	}
}
