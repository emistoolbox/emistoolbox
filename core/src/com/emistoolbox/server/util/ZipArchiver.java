package com.emistoolbox.server.util;

import info.joriki.io.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipArchiver {
	private ZipArchiver () {}

	public static void archive (File file,File zipFile,final boolean delete) throws IOException {
		final ZipOutputStream out = new ZipOutputStream (new FileOutputStream (zipFile));
		DirectoryTraversal.traverse (file,new DirectoryTraversal.Handler () {
			public void handle (File absolute,File relative) throws IOException {
				if (!absolute.isDirectory ()) {
					out.putNextEntry (new ZipEntry (relative.getPath ().replace (File.separatorChar,'/')));
					Util.copy (absolute,out);
				}
				if (delete)
					absolute.delete ();
			}
		}); 
		out.close ();
	}
}
