package com.emistoolbox.server.util;

import java.io.File;
import java.io.IOException;

public class DirectoryTraversal {
	public static interface Handler {
		void handle (File absolute,File relative) throws IOException;
	}

	private DirectoryTraversal () {}

	public static void traverse (File file,Handler handler) throws IOException {
		traverse (file,handler,null);
	}

	private static void traverse (File file,Handler handler,File relative) throws IOException {
		if (file.isDirectory ())
			for (String filename : file.list ())
				traverse (new File (file,filename),handler,new File (relative,filename));
		handler.handle (file,relative);
	}
}
