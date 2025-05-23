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

package fr.epims.ftp;


import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 *
 * File on the server side
 *
 * @author JM235353
 *
 */
public class ServerFile extends File {

    private char SEPARATOR = '/'; // default separator

    private final boolean m_isDirectory;
    private final long m_lastModified;
    private final long m_length;

    private final String m_serverFilePath;
    private final String m_serverFileName;

    private FTPServerFileSystemView m_systemView;

    public ServerFile(FTPServerFileSystemView systemView, String path, String name, boolean isDirectory, long lastModified, long length) {
        super(path);

        m_systemView = systemView;

        m_serverFilePath = path;
        m_serverFileName = name;

        // check the separator
        if (m_serverFilePath.length()>m_serverFileName.length()) {
            char serverSeparator = m_serverFilePath.charAt(m_serverFilePath.length()-m_serverFileName.length()-1);
            if ((serverSeparator== '/') || (serverSeparator== '\\')) {
                SEPARATOR = serverSeparator;
            }
        }

        m_isDirectory = isDirectory;
        m_lastModified = lastModified;
        m_length = length;
    }

    public void download(File destination) throws IOException {
        m_systemView.download(this, destination);
    }

    public void uploadto(File source) throws IOException {
        m_systemView.upload(source, this);
    }

    public File createDirectory(String name) {
        return m_systemView.createNewFolder(this, name);
    }

    public void abort() {
        m_systemView.abortFTPClient();
    }



    @Override
    public String getPath() {
        return m_serverFilePath;
    }

    @Override
    public String getName() {
        return m_serverFileName;
    }

    @Override
    public String getParent() {

        if (m_serverFilePath.length() <= m_serverFileName.length()) {
            return null;
        }

        String parentPath = m_serverFilePath.substring(0, m_serverFilePath.length()-m_serverFileName.length()-1);
        return parentPath;
    }

    @Override
    public File getParentFile() {
        String p = this.getParent();
        if (p == null) {
            return null;
        }

        int lastIndex = p.lastIndexOf(SEPARATOR);
        if (lastIndex == -1) {
            return new ServerFile(m_systemView, p, p, true, 0, 0);
        } else {
            return new ServerFile(m_systemView, p, p.substring(lastIndex+1, p.length()), true, 0, 0);
        }

    }

    @Override
    public boolean isAbsolute() {
        return true;
    }

    @Override
    public String getAbsolutePath() {
        return m_serverFilePath;
    }

    @Override
    public File getAbsoluteFile() {
        return this;
    }

    @Override
    public String getCanonicalPath() throws IOException {
        return m_serverFilePath;
    }

    @Override
    public File getCanonicalFile() throws IOException {
        return this;
    }

    @Override
    public boolean canRead() {
        return true;
    }

    @Override
    public boolean canWrite() {
        return false;
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public boolean isDirectory() {
        return m_isDirectory;
    }

    @Override
    public boolean isFile() {
        return !m_isDirectory;
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public long lastModified() {
        return m_lastModified;
    }

    @Override
    public long length() {
        return m_length;
    }

    @Override
    public boolean createNewFile() throws IOException {
        throw new SecurityException("createNewFile not allowed");
    }

    @Override
    public boolean delete() {
        throw new SecurityException("delete not allowed");
    }

    @Override
    public void deleteOnExit() {
        throw new SecurityException("deleteOnExit not allowed");
    }

    @Override
    public String[] list() {
        if (m_isDirectory) {
            File[] files = m_systemView.getFiles(this, false);
            String[] filesArray = new String[files.length];
            for (int i=0;i<filesArray.length;i++) {
                filesArray[i] = files[i].getPath();
            }
            return filesArray;
        }

        return null;
    }

    @Override
    public File[] listFiles() {

        if (m_isDirectory) {
            return m_systemView.getFiles(this, false);

        }

        return null;
    }

    @Override
    public File[] listFiles(FilenameFilter filter) {
        throw new UnsupportedOperationException("listFiles Not supported.");
    }

    @Override
    public File[] listFiles(FileFilter filter) {
        throw new UnsupportedOperationException("listFiles Not supported.");
    }

    @Override
    public boolean mkdir() {
        throw new SecurityException("mkdir not allowed");
    }
    @Override
    public boolean mkdirs() {
        throw new SecurityException("mkdir not allowed");
    }
    @Override
    public boolean renameTo(File dest) {
        throw new SecurityException("renameTo not allowed");
    }
    @Override
    public boolean setLastModified(long time) {
        throw new SecurityException("setLastModified not allowed");
    }

    @Override
    public boolean setReadOnly() {
        throw new SecurityException("setReadOnly not allowed");
    }

    @Override
    public boolean setWritable(boolean writable, boolean ownerOnly) {
        throw new SecurityException("setWritable not allowed");
    }

    @Override
    public boolean setReadable(boolean readable, boolean ownerOnly) {
        throw new SecurityException("setReadable not allowed");
    }

    @Override
    public boolean setExecutable(boolean executable, boolean ownerOnly) {
        throw new SecurityException("setReadable not allowed");
    }

    @Override
    public boolean canExecute() {
        return false;
    }

    @Override
    public long getTotalSpace() {
        throw new UnsupportedOperationException("getTotalSpace Not supported.");
    }

    @Override
    public long getFreeSpace() {
        throw new UnsupportedOperationException("getTotalSpace Not supported.");
    }

    @Override
    public long getUsableSpace() {
        throw new SecurityException("getUsableSpace not allowed");
    }



}
