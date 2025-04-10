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


import fr.epims.MainFrame;
import fr.edyp.epims.json.FtpConfigurationJson;
import fr.epims.preferences.EpimsPreferences;
import fr.epims.preferences.PreferencesKeys;
import fr.epims.ui.common.FlatButton;
import fr.epims.ui.common.HourGlassPanel;
import fr.epims.ui.common.IconManager;
import fr.epims.ui.common.InfoDialog;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * File Chooser panel modified for downloading/uploading files from/to a server side.
 *
 * @author Matthew Robinson, Pavel Vorobiev, (modifications) JM235353
 *
 */
public class TreeFileChooserPanel extends HourGlassPanel {

    protected TreeFileChooser m_tree;
    private JScrollPane m_treeScrollPane = new JScrollPane();

    private FlatButton m_downloadButton;
    private FlatButton m_uploadButton;

    protected DefaultTreeModel m_model;
    private DefaultMutableTreeNode m_top;
    private FileSystemView m_fileSystemView;

    private TransferInterface m_transferInterface;

    private String[] m_autoExpandPathNames = null;

    private String m_autoSelectData = null;


    public TreeFileChooserPanel(FileSystemView fileSystemView, TransferInterface transferInterface, String[] autoExpandPathNames) {
        m_fileSystemView = fileSystemView;
        m_transferInterface = transferInterface;
        m_autoExpandPathNames = autoExpandPathNames;

        initTree();
    }



    public void setConfiguration(FtpConfigurationJson configuration, final String autoselectData, String[] autoExpandPathNames) {
        m_autoExpandPathNames = autoExpandPathNames;
        ((FTPServerFileSystemView) m_fileSystemView).setConfiguration(configuration);
        m_treeScrollPane.getViewport().remove(m_tree);

        m_autoSelectData = autoselectData;

        setLoading(getNewLoadingIndex());

        final TreeFileChooserPanel panel = this;

        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                final File[] roots = m_fileSystemView.getRoots(); // this line takes time


                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        fillTree(roots, false);

                        panel.updateUI();
                        panel.revalidate();

                        setLoaded(m_id);


                    }
                });
            }
        });
        t.start();

        m_downloadButton.setEnabled(false);

        m_uploadButton.setEnabled(false);
    }


    public Dimension getPreferredSize() {
        return new Dimension(600,400);
    }

    public void initTree() {
        setLayout(new GridBagLayout());

        setLoading(getNewLoadingIndex());

        final TreeFileChooserPanel panel = this;

        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                final File[] roots = m_fileSystemView.getRoots(); // this line takes time

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        fillTree(roots, true);

                        panel.updateUI();
                        panel.revalidate();

                        setLoaded(m_id);
                    }
                });
            }
        });
        t.start();



    }

    private void fillTree(File[] roots, boolean mustConstructPanel) {

        String rootDirectoryName = null;
        if ((roots != null) && (roots.length == 1)) {
            // should always be the case
            rootDirectoryName =  roots[0].getName();
        }
        m_transferInterface.setRootDirectoryName(rootDirectoryName);


        m_top = new DefaultMutableTreeNode(new IconData(IconManager.getIcon(IconManager.IconType.COMPUTER_NETWORK), null, "Server"));

        ArrayList<DefaultMutableTreeNode> nodesToExpand = new ArrayList<>();

        for (int k = 0; k < roots.length; k++) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(new IconData(IconManager.getIcon(IconManager.IconType.FOLDER), IconManager.getIcon(IconManager.IconType.FOLDER_EXPANDED), new FileNode(roots[k])));
            m_top.add(node);
            node.add(new DefaultMutableTreeNode(Boolean.TRUE));
            nodesToExpand.add(node);

        }

        m_model = new DefaultTreeModel(m_top);
        m_tree = new TreeFileChooser(m_model);

        m_tree.addTreeExpansionListener(new TreeExpansionListener() {

            @Override
            public void treeExpanded(TreeExpansionEvent tee) {

                TreeUtils.saveExpansionState(m_tree, null);
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent tee) {
                TreeUtils.saveExpansionState(m_tree, null);
            }

        });

        m_tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {

                boolean downloadAllowed = hasOnlySelectedFilesAnddirectories();
                m_downloadButton.setEnabled(downloadAllowed);

                boolean uploadAllowed = hasOnlySelectedADirectory();
                m_uploadButton.setEnabled(uploadAllowed);
            }
        });

        m_tree.setDragEnabled(true);

        m_tree.putClientProperty("JTree.lineStyle", "Angled");

        TreeCellRenderer renderer = new IconCellRenderer();
        m_tree.setCellRenderer(renderer);

        m_tree.addTreeExpansionListener(new DirExpansionListener());


        m_tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        m_tree.setShowsRootHandles(true);
        m_tree.setEditable(false);

        m_treeScrollPane.getViewport().add(m_tree);

        if (mustConstructPanel) {
            constructPanel();
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (DefaultMutableTreeNode node : nodesToExpand) {
                    TreePath path = new TreePath(node.getPath());
                    expandTreePath(path);
                }

            }
        });


    }

    private void select(DefaultMutableTreeNode parent, String nameSearched) {
        int nb = parent.getChildCount();
        for (int i = 0; i < nb; i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(i);
            if (child.isLeaf()) {
                if (child.toString().startsWith(nameSearched)) {
                    TreePath path = new TreePath(child.getPath());
                    // JPM.WART : do not know when the nodes are really displayed
                    // so we wait for 0.5 seconde before selecting the found node
                    Timer timer = new Timer(500, new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            m_tree.scrollPathToVisible(path);
                            m_tree.setSelectionPath(path);

                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                }
            } else {
                select(child, nameSearched);
            }
        }
    }

    private void constructPanel() {


        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setOrientation(SwingConstants.VERTICAL);

        FlatButton refreshButton = new FlatButton(IconManager.getIcon(IconManager.IconType.REFRESH),false);
        refreshButton.setToolTipText("Refresh");
        toolbar.add(refreshButton);

        m_downloadButton = new FlatButton(IconManager.getIcon(IconManager.IconType.DOWNLOAD), false);
        m_downloadButton.setEnabled(false);
        m_downloadButton.setToolTipText("Download");
        toolbar.add(m_downloadButton);

        m_uploadButton = new FlatButton(IconManager.getIcon(IconManager.IconType.UPLOAD), false);
        m_uploadButton.setEnabled(false);
        m_uploadButton.setToolTipText("Upload");
        // toolbar.add(m_uploadButton); //JPM : upload is now possible but the functionnality is hidden to the users for the moment //JPM.TODO UPLOAD


        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        add(m_treeScrollPane, c);

        c.gridx++;
        c.weightx = 0;
        add(toolbar, c);



        refreshButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                updateTree();
            }
        });

        m_downloadButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                Preferences preferences = EpimsPreferences.root();
                String defaultDirString = preferences.get(PreferencesKeys.FTP_DOWNLOAD_DIR, System.getProperty("user.home"));
                File defaultDir = new File(defaultDirString);
                if (!defaultDir.exists()) {
                    defaultDir = new File(System.getProperty("user.home"));
                    if (!defaultDir.exists()) {
                        defaultDir = new File(".");
                    }
                }


                JFileChooser fchooser = new JFileChooser(defaultDir);
                fchooser.setDialogTitle("Select Download Destination Directory");
                fchooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fchooser.setAcceptAllFileFilterUsed(false);

                int result = fchooser.showSaveDialog(m_downloadButton);

                if (result == JFileChooser.APPROVE_OPTION) {

                    File directory = fchooser.getSelectedFile();
                    if (!directory.exists()) {
                        InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Writable Error", "Directory does not exist");
                        infoDialog.centerToWindow(MainFrame.getMainWindow());
                        infoDialog.setVisible(true);
                        return;
                    }
                    if (!directory.canWrite()) {
                        InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Writable Error", "Directory is not Writable");
                        infoDialog.centerToWindow(MainFrame.getMainWindow());
                        infoDialog.setVisible(true);
                        return;
                    }

                    preferences.put(PreferencesKeys.FTP_DOWNLOAD_DIR, directory.getAbsolutePath());
                    try {
                        preferences.flush();
                    } catch (Exception prefException) {

                    }

                    ArrayList<TransferInfo> downloadList = new ArrayList<>();

                    ArrayList<File> selectedFiles = getSelectedFilesAndDirectories();
                    for (File f : selectedFiles) {
                        ServerFile serverFile = (ServerFile) f;
                        File destination = new File(directory.getAbsoluteFile(), f.getName());

                        downloadList.add(new TransferInfo(destination, serverFile, true));

                    }

                    m_transferInterface.addFilesToTransfer(downloadList);


                }

            }
        });


        m_uploadButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                Preferences preferences = EpimsPreferences.root();
                String defaultDirString = preferences.get(PreferencesKeys.FTP_DOWNLOAD_DIR, System.getProperty("user.home"));
                File defaultDir = new File(defaultDirString);
                if (!defaultDir.exists()) {
                    defaultDir = new File(System.getProperty("user.home"));
                    if (!defaultDir.exists()) {
                        defaultDir = new File(".");
                    }
                }


                JFileChooser fchooser = new JFileChooser(defaultDir);
                fchooser.setDialogTitle("Select Files to Upload");
                fchooser.setMultiSelectionEnabled(true);
                fchooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                fchooser.setAcceptAllFileFilterUsed(false);

                int result = fchooser.showOpenDialog(m_uploadButton);

                if (result == JFileChooser.APPROVE_OPTION) {

                    //JPM.TODO
                    /*File directory = fchooser.getSelectedFile();
                    if (!directory.exists()) {
                        InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Writable Error", "Directory does not exist");
                        infoDialog.centerToWindow(MainFrame.getMainWindow());
                        infoDialog.setVisible(true);
                        return;
                    }
                    if (!directory.canWrite()) {
                        InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Writable Error", "Directory is not Writable");
                        infoDialog.centerToWindow(MainFrame.getMainWindow());
                        infoDialog.setVisible(true);
                        return;
                    }

                    preferences.put(PreferencesKeys.FTP_DOWNLOAD_DIR, directory.getAbsolutePath());
                    try {
                        preferences.flush();
                    } catch (Exception prefException) {

                    }

                    ArrayList<DownloadInfo> downloadList = new ArrayList<>();

                    ArrayList<File> selectedFiles = getSelectedFilesAndDirectories();
                    for (File f : selectedFiles) {
                        ServerFile serverFile = (ServerFile) f;
                        File destination = new File(directory.getAbsoluteFile(), f.getName());

                        downloadList.add(new DownloadInfo(destination, serverFile));

                    }*/


                    ArrayList<File> selectedFiles = getSelectedFilesAndDirectories();
                    ServerFile serverFile = (ServerFile) selectedFiles.get(0);

                    ArrayList<TransferInfo> downloadList = new ArrayList<>();

                    File[] localFiles = fchooser.getSelectedFiles();
                    for (File localFile : localFiles) {

                        downloadList.add(new TransferInfo(localFile, serverFile, false));
                    }
                    m_transferInterface.addFilesToTransfer(downloadList);


                }

            }
        });


    }


    public void updateTree() {

        HashSet<String> expandedPaths = TreeUtils.getExpansionState(m_tree);

        m_top.removeAllChildren();
        m_model.reload(m_top);

        DefaultMutableTreeNode node;

        File[] roots = m_fileSystemView.getRoots();

        for (int k = 0; k < roots.length; k++) {
            node = new DefaultMutableTreeNode(new IconData(IconManager.getIcon(IconManager.IconType.FOLDER), IconManager.getIcon(IconManager.IconType.FOLDER_EXPANDED), new FileNode(roots[k])));
            m_top.add(node);
            node.add(new DefaultMutableTreeNode(Boolean.TRUE));
        }

        m_model.reload();

        TreeUtils.setExpansionState(expandedPaths, m_tree, (DefaultMutableTreeNode) m_tree.getModel().getRoot(), null);
    }

    public static TreePath getPath(TreeNode treeNode) {
        ArrayList<Object> nodes = new ArrayList<Object>();
        if (treeNode != null) {
            nodes.add(treeNode);
            treeNode = treeNode.getParent();
            while (treeNode != null) {
                nodes.add(0, treeNode);
                treeNode = treeNode.getParent();
            }
        }

        return nodes.isEmpty() ? null : new TreePath(nodes.toArray());
    }

    public void expandTreePath(TreePath path) {
        m_tree.expandPath(path);
    }

    public void expandMultipleTreePath(HashSet<String> directories, String pathLabel) {

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) m_model.getRoot();
        Enumeration totalNodes = root.depthFirstEnumeration();

        while (totalNodes.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) totalNodes.nextElement();

            TreePath nodePath = new TreePath(node.getPath());

            if (node.toString().toLowerCase().equalsIgnoreCase(pathLabel)) {

                m_tree.getModel().addTreeModelListener(new TreeModelListener() {

                    @Override
                    public void treeNodesChanged(TreeModelEvent tme) {
                        ;
                    }

                    @Override
                    public void treeNodesInserted(TreeModelEvent tme) {
                        ;
                    }

                    @Override
                    public void treeNodesRemoved(TreeModelEvent tme) {
                        ;
                    }

                    @Override
                    public void treeStructureChanged(TreeModelEvent tme) {
                        Enumeration mountingPointChildren = node.children();

                        while (mountingPointChildren.hasMoreElements()) {

                            DefaultMutableTreeNode child = (DefaultMutableTreeNode) mountingPointChildren.nextElement();

                            if (directories.contains(child.toString())) {
                                m_tree.expandPath(new TreePath(child.getPath()));
                            }
                        }

                    }

                });

                m_tree.expandPath(nodePath);

                break;
            }
        }
    }

    public JTree getTree() {
        return m_tree;
    }

    DefaultMutableTreeNode getTreeNode(TreePath path) {
        return (DefaultMutableTreeNode) (path.getLastPathComponent());
    }

    public static FileNode getFileNode(DefaultMutableTreeNode node) {
        if (node == null) {
            return null;
        }
        Object obj = node.getUserObject();
        if (obj instanceof IconData) {
            obj = ((IconData) obj).getObject();
        }
        if (obj instanceof FileNode) {
            return (FileNode) obj;
        } else {
            return null;
        }
    }



    public void actionDone(boolean serverSideModified, boolean actionListModified) {
        if (serverSideModified) {
            updateTree();
        }
    }

    // Make sure expansion is threaded and updating the tree model
    // only occurs within the event dispatching thread.
    class DirExpansionListener implements TreeExpansionListener {

        @Override
        public void treeExpanded(TreeExpansionEvent event) {
            final DefaultMutableTreeNode node = getTreeNode(
                    event.getPath());
            final FileNode fnode = getFileNode(node);

            Thread runner = new Thread() {
                @Override
                public void run() {
                    if (fnode != null && fnode.expand(node)) {
                        Runnable runnable = new Runnable() {
                            public void run() {
                                m_model.reload(node);

                                if (m_autoExpandPathNames != null) {
                                    int count = m_model.getChildCount(node);
                                    for (int i = 0; i < count; i++) {
                                        DefaultMutableTreeNode child = (DefaultMutableTreeNode) m_model.getChild(node, i);
                                        FileNode fileNode = getFileNode(child);
                                        String name = fileNode.getFile().getName();

                                        for (String autoExpandName : m_autoExpandPathNames) {
                                            if (name.equals(autoExpandName)) {
                                                SwingUtilities.invokeLater(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        TreePath path = new TreePath(child.getPath());
                                                        expandTreePath(path);
                                                    }
                                                });
                                                break;
                                            }
                                        }


                                    }
                                }

                            }
                        };
                        SwingUtilities.invokeLater(runnable);
                    }
                }
            };
            runner.start();
        }

        @Override
        public void treeCollapsed(TreeExpansionEvent event) {
        }
    }

    private ArrayList<File> getSelectedFilesAndDirectories() {

        ArrayList<File> selectedFiles = new ArrayList<File>();

        TreePath[] paths = m_tree.getSelectionPaths();
        HashSet<TreePath> pathSelectedSet = new HashSet<>();
        for (TreePath path : paths) {
            pathSelectedSet.add(path);
        }
        if (paths != null) {
            for (TreePath path : paths) {

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

                // check that a parent is not selected
                boolean parentAlreadySelected = false;
                TreePath parentPath = path.getParentPath();
                while (parentPath != null) {
                    if (pathSelectedSet.contains(parentPath)) {
                        parentAlreadySelected = true;
                        break;
                    }
                    parentPath = parentPath.getParentPath();
                }
                if (parentAlreadySelected) {
                    continue;
                }


                Object data = node.getUserObject();
                if (data instanceof IconData) {
                    Object extraData = ((IconData) data).getObject();
                    if (extraData instanceof FileNode) {
                        File f = ((FileNode) extraData).getFile();

                        if (f.isFile() || f.isDirectory()) {
                            selectedFiles.add(f);
                        }
                    }
                }
            }
        }
        return selectedFiles;
    }

    private boolean hasOnlySelectedFilesAnddirectories() {
        boolean oneFileAtLeast = false;

        TreePath[] paths = m_tree.getSelectionPaths();
        if (paths != null) {
            for (TreePath path : paths) {

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                Object data = node.getUserObject();
                if (data instanceof IconData) {
                    Object extraData = ((IconData) data).getObject();
                    if (extraData instanceof FileNode) {
                        File f = ((FileNode) extraData).getFile();

                        if (f.isFile() || f.isDirectory()) {
                            oneFileAtLeast = true;
                        } else {
                            return false;
                        }
                    }
                }
            }
        }

        return oneFileAtLeast;
    }

    public boolean hasOnlySelectedADirectory() {

        TreePath[] paths = m_tree.getSelectionPaths();
        if (paths == null) {
            return false;
        }
        if (paths.length!=1) {
            return false;
        }

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) paths[0].getLastPathComponent();
        Object data = node.getUserObject();
        if (data instanceof IconData) {
            Object extraData = ((IconData) data).getObject();
            if (extraData instanceof FileNode) {
                File f = ((FileNode) extraData).getFile();
                return (f.isDirectory());
            }
        }

        return false;
    }


    public class FileNode {

        protected File m_file;

        public FileNode(File file) {
            m_file = file;
        }

        public File getFile() {
            return m_file;
        }

        @Override
        public String toString() {
            return m_file.getName().length() > 0 ? m_file.getName()
                    : m_file.getPath();
        }

        public boolean expand(DefaultMutableTreeNode parent) {
            DefaultMutableTreeNode flag = (DefaultMutableTreeNode) parent.getFirstChild();
            if (flag == null) {
                // No flag
                return false;
            }

            Object obj = flag.getUserObject();
            if (!(obj instanceof Boolean)) {
                return false;      // Already expanded
            } else {

            }

            parent.removeAllChildren();  // Remove Flag

            File[] files = m_file.listFiles();
            if (files == null) {
                return true;
            }

            ArrayList v = new ArrayList();

            for (int k = 0; k < files.length; k++) {
                File f = files[k];

                FileNode newNode = new FileNode(f);

                boolean isAdded = false;
                for (int i = 0; i < v.size(); i++) {
                    FileNode nd = (FileNode) v.get(i);
                    if (newNode.compareTo(nd) < 0) {
                        v.add(i, newNode);
                        isAdded = true;
                        break;
                    }
                }
                if (!isAdded) {
                    v.add(newNode);
                }
            }

            for (int i = 0; i < v.size(); i++) {
                FileNode nd = (FileNode) v.get(i);
                boolean isDirectory = nd.getFile().isDirectory();
                IconData idata = new IconData(IconManager.getIcon(isDirectory ? IconManager.IconType.FOLDER : IconManager.IconType.FILE), isDirectory ? IconManager.getIcon(IconManager.IconType.FOLDER_EXPANDED) : null, nd);
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(idata);
                parent.add(node);

                if (nd.hasSubDirs()) {
                    node.add(new DefaultMutableTreeNode(Boolean.TRUE));
                }
            }

            if (m_autoSelectData != null) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        String nameSearched = m_autoSelectData+".";
                        select(m_top, nameSearched);
                    }
                });
            }

            return true;
        }

        public boolean hasSubDirs() {
            return (m_file.isDirectory());
        }

        public int compareTo(FileNode toCompare) {
            return m_file.getName().compareToIgnoreCase(
                    toCompare.m_file.getName());
        }

    }

    private class IconCellRenderer extends DefaultTreeCellRenderer {


        public IconCellRenderer() {
            super();
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean isLeaf, int row, boolean hasFocus) {
            Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, isLeaf, row, hasFocus);

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object obj = node.getUserObject();
            setText(obj.toString());

            if (obj instanceof Boolean) {
                setText("Loading data...");
                setIcon(IconManager.getIcon(IconManager.IconType.HOUR_GLASS_MINI16));
            } else if (obj instanceof IconData) {
                IconData idata = (IconData) obj;
                if (expanded) {
                    setIcon(idata.getExpandedIcon());
                } else {
                    setIcon(idata.getIcon());
                }
            } else {
                setIcon(null);
            }
            return c;
        }

    }

    private class IconData {

        protected Icon m_icon;
        protected Icon m_expandedIcon;
        protected Object m_data;

        public IconData(Icon icon, Object data) {
            m_icon = icon;
            m_expandedIcon = null;
            m_data = data;
        }

        public IconData(Icon icon, Icon expandedIcon, Object data) {
            m_icon = icon;
            m_expandedIcon = expandedIcon;
            m_data = data;
        }

        public Icon getIcon() {
            return m_icon;
        }

        public Icon getExpandedIcon() {
            return m_expandedIcon != null ? m_expandedIcon : m_icon;
        }

        public Object getObject() {
            return m_data;
        }

        @Override
        public String toString() {
            return m_data.toString();
        }



    }

}



