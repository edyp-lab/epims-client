/*
 * Copyright (C) 2021
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */


package fr.epims.util;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 *
 * Useful method to zip or unzip a file or a directory
 * <p>
 * Code taken from this tutorial: https://www.baeldung.com/java-compress-and-uncompress
 *
 * @author JM235353
 *
 */
public class UtilZip {

    public static void zip(String sourcePath, String outputFilePath) throws IOException {

        FileOutputStream fos = new FileOutputStream(outputFilePath);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        File fileToZip = new File(sourcePath);

        zipFile(fileToZip, fileToZip.getName(), zipOut);


        zipOut.close();
        fos.close();
    }

    public static void zipFileWithoutParentDirectory(String sourcePath, String outputFilePath) throws IOException {

        FileOutputStream fos = new FileOutputStream(outputFilePath);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        File fileToZip = new File(sourcePath);

        zipFileWithoutParentDirectory(fileToZip, fileToZip.getName(), zipOut);


        zipOut.close();
        fos.close();
    }

    private static void zipFileWithoutParentDirectory(File directoryToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (directoryToZip.isHidden()) {
            return;
        }
        if (!directoryToZip.isDirectory()) {
            throw new IOException(directoryToZip.getAbsolutePath()+" is not a directory");
        }

        File[] children = directoryToZip.listFiles();
        for (File childFile : children) {
            zipFile(childFile, childFile.getName(), zipOut);
        }

    }

    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }



    public static void unzip(String fileZipPath, String destinationDirectoryPath) throws IOException {

        File destinationDirectory = new File(destinationDirectoryPath);


        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZipPath))) {
            unzip(zis, destinationDirectory);
        }
    }

    private static void unzip(ZipInputStream zis, File destinationDirectory) throws IOException {

        byte[] buffer = new byte[1024];

        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            File newFile = newFile(destinationDirectory, zipEntry);
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                // fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                // write file content
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
    }

    /**
     * This method guards against writing files to the file system outside of the target folder.
     * This vulnerability is called Zip Slip, and we can read more about it here.
     *
     * @throws IOException
     */
    private static File newFile(File destinationDirectory, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDirectory, zipEntry.getName());

        String destDirPath = destinationDirectory.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    public static void exportResource(String resourceName, String exportPath) throws Exception {

        try (InputStream stream = ClassLoader.getSystemResource(resourceName).openStream();) {

           if (stream == null) {
                throw new Exception("Cannot get resource \"" + resourceName + "\" from Jar file.");
            }

            int readBytes;
            byte[] buffer = new byte[4096];
            try ( OutputStream  resStreamOut = new FileOutputStream(exportPath) ) {
                while ((readBytes = stream.read(buffer)) > 0) {
                    resStreamOut.write(buffer, 0, readBytes);
                }
            }
        } catch (Exception ex) {
            throw ex;
        }
    }

    public static String readRessouce(String resourceName) throws Exception {

        try (InputStream stream = ClassLoader.getSystemResource(resourceName).openStream(); InputStreamReader isr = new InputStreamReader(stream, "UTF-8"); ) {

            BufferedReader reader = new BufferedReader(isr);
            String         line;
            StringBuilder  stringBuilder = new StringBuilder();
            String         ls = System.getProperty("line.separator");

            try {
                while((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append(ls);
                }

                return stringBuilder.toString();
            } finally {
                reader.close();
            }

        } catch (Exception ex) {
            throw ex;
        }
    }

    public static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }
}
