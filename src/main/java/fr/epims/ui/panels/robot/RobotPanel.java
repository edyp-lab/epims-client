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

package fr.epims.ui.panels.robot;

import fr.epims.MainFrame;
import fr.epims.dataaccess.AbstractDatabaseCallback;
import fr.epims.dataaccess.AccessDatabaseThread;
import fr.epims.dataaccess.DataManager;
import fr.edyp.epims.json.*;
import fr.epims.preferences.EpimsPreferences;
import fr.epims.preferences.PreferencesKeys;
import fr.epims.tasks.AddPlateTask;
import fr.epims.tasks.RobotPlateTask;
import fr.epims.tasks.SavePlateTask;
import fr.epims.ui.common.*;
import fr.epims.ui.dialogs.AddPlateDialog;
import fr.epims.ui.common.QuestionDialog;
import fr.epims.ui.dialogs.UpdateDataDialog;
import fr.epims.util.UtilDate;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.Channel;
import java.util.*;
import java.util.prefs.Preferences;

/**
 *
 * Panel with plate being modified and the list of free and used samples
 *
 * @author JM235353
 *
 */
public class RobotPanel extends HourGlassPanel {

    private static RobotPanel m_singleton = null;

    private DefaultListModel<EditablePlate> m_plateListModel;

    private RobotPlatePanel m_robotPlatePanel;

    private RobotSamplesPanel m_robotSamplesPanel;

    private RobotListPlatePanel m_robotListPlatePanel;

    private JList m_plateList;
    private JButton m_savePlateButton;
    private JButton m_undoButton;
    private JButton m_exportButton;
    private JComboBox<String> m_colorationModeCombobox;
    private final static String SAMPLE = "Sample Colour";
    private final static String TRYPSIN = "Trypsin Colour";
    private final static String USER = "User Colour";
    private final static String STUDY = "Study Colour";

    private JTabbedPane m_tabbedPane;

    public static RobotPanel getPanel() {
        if (m_singleton == null) {
            m_singleton = new RobotPanel();
        }
        return m_singleton;
    }

    private RobotPanel() {
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);



        JTabbedPane tabbedPane = createTabbedPane();

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        add(tabbedPane, c);

    }

    private JTabbedPane createTabbedPane() {
        m_tabbedPane = new JTabbedPane();

        m_robotListPlatePanel = new RobotListPlatePanel();
        JScrollPane scrollPane = new JScrollPane(m_robotListPlatePanel);
        m_tabbedPane.addTab("Plates", scrollPane);
        m_tabbedPane.setIconAt(0, IconManager.getIcon(IconManager.IconType.PLATE));

        Component createPlateComponent = createPlateComponent();
        m_tabbedPane.addTab("Design Plates", createPlateComponent);
        m_tabbedPane.setIconAt(1, IconManager.getIcon(IconManager.IconType.EDIT_PLATE));


        return m_tabbedPane;
    }

    public void update(RobotPlanningJson robotPlanning) {
        m_robotPlatePanel.update(robotPlanning);
    }

    public void reinit() {
        if (!m_loadDataDone) {
            return; // nothing to do
        }
        reinit_running = true;
        try {
            m_plateListModel.clear();
            m_loadDataDone = false;
            m_robotPlatePanel.reinit();
            m_robotSamplesPanel.reinit();
            m_robotListPlatePanel.reinit();

            m_savePlateButton.setEnabled(false);
            m_undoButton.setEnabled(false);
            m_exportButton.setEnabled(false);

        } catch (Exception e) {
            reinit_running = false;
            throw e;
        }
        reinit_running = false;
    }
    private boolean reinit_running = false;

    public void plateModified(boolean modified) {
        EditablePlate plate = (EditablePlate) m_plateList.getSelectedValue();
        plate.setModified(modified);
        m_plateList.updateUI();

        updateTabbedPane();

        updateButtons();

    }

    private void updateTabbedPane() {

        boolean modified = hasAPlateModified();

        m_tabbedPane.setEnabledAt(0, !modified);

        // allow only robot tabbed pane when there is a plate modified
        MainFrame.getMainWindow().enableTabbedPane(!modified, !modified, !modified,true, !modified, false);

    }

    private boolean hasAPlateModified() {
        int size = m_plateList.getModel().getSize();
        for (int i=0;i<size;i++) {
            EditablePlate plate = (EditablePlate) m_plateList.getModel().getElementAt(i);
            if (plate.isModified()) {
                return true;
            }
        }
        return false;
    }

    private void updateButtons() {
        EditablePlate plate = (EditablePlate) m_plateList.getSelectedValue();
        m_savePlateButton.setEnabled((plate!=null) && plate.isModified());
        m_undoButton.setEnabled((plate!=null) && plate.isModified());
        m_exportButton.setEnabled(plate!=null);
    }



    private Component createPlateComponent() {

        JPanel selectPlatePanel = createSelectPlatePanel();
        Component rightComponent = createRightComponent();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setTopComponent(selectPlatePanel);
        splitPane.setBottomComponent(rightComponent);
        splitPane.setDividerLocation(230);

        return splitPane;
    }

    private Component createRightComponent() {

        WellSelectionManager wellSelectionManager = new WellSelectionManager();

        m_robotPlatePanel = new RobotPlatePanel(this, 12,8, wellSelectionManager);

        m_robotSamplesPanel = new RobotSamplesPanel(wellSelectionManager, m_robotPlatePanel);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(m_robotPlatePanel);
        splitPane.setBottomComponent(m_robotSamplesPanel);
        splitPane.setDividerLocation(0.5);

        return splitPane;
    }



    private JPanel createSelectPlatePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(new Dimension(60,100));

        Border titledBorder = BorderFactory.createTitledBorder(" Plates ");
        panel.setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(0, 5, 5, 5);

        JToolBar toolbar = createToolbar();

        JScrollPane scrollPane = new JScrollPane();

        m_plateListModel = new DefaultListModel();
        m_plateList = new JList(m_plateListModel);
        m_plateList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        m_plateList.setCellRenderer(new PlateListRenderer());
        scrollPane.setViewportView(m_plateList);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        panel.add(toolbar, c);

        c.gridy++;
        c.weighty = 1;
        panel.add(scrollPane, c);

        m_plateList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (reinit_running) {
                    return;
                }

                EditablePlate currentPlate = m_robotPlatePanel.getCurrentPlate();
                EditablePlate plate = (EditablePlate) m_plateList.getSelectedValue();
                if ((currentPlate!=null) && currentPlate.equals(plate)) {
                    // nothing to do
                    return;
                }

                if ((currentPlate!=null) && currentPlate.isModified()) {
                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.INFO, "Plate being Modified", "You must Save or Undo your modifications on the current Plate before switching to another Plate.");
                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                    infoDialog.setVisible(true);

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            m_plateList.setSelectedValue(currentPlate, true);
                        }
                    });
                    return;
                }

                m_robotPlatePanel.setPlate(plate, false);
                m_robotSamplesPanel.setPlate(plate, null);

                updateButtons();

                repaint();
            }
        });


        return panel;
    }


    private JToolBar createToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
        toolbar.setFloatable(false);

        JButton addPlateButton = new FlatButton(IconManager.getIcon(IconManager.IconType.ADD_PLATE), false);
        toolbar.add(addPlateButton);
        addPlateButton.setToolTipText("Create a new Plate");

        toolbar.addSeparator(); // -------

        m_savePlateButton = new FlatButton(IconManager.getIcon(IconManager.IconType.SAVE), false);
        toolbar.add(m_savePlateButton);
        m_savePlateButton.setEnabled(false);
        m_savePlateButton.setToolTipText("Save Plate");

        m_undoButton = new FlatButton(IconManager.getIcon(IconManager.IconType.UNDO), false);
        toolbar.add(m_undoButton);
        m_undoButton.setEnabled(false);
        m_undoButton.setToolTipText("Undo All Modifications");

        toolbar.addSeparator(); // -------

        m_exportButton = new FlatButton(IconManager.getIcon(IconManager.IconType.EXPORT), false);
        toolbar.add(m_exportButton);
        m_exportButton.setEnabled(false);
        m_exportButton.setToolTipText("Export Plate to Excel File");

        toolbar.addSeparator(); // -------


        m_colorationModeCombobox = new JComboBox();
        m_colorationModeCombobox.addItem(SAMPLE);
        m_colorationModeCombobox.addItem(STUDY);
        m_colorationModeCombobox.addItem(TRYPSIN);
        m_colorationModeCombobox.addItem(USER);


        toolbar.add(m_colorationModeCombobox);



        m_colorationModeCombobox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setColourMode();
            }
        });

        addPlateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                UpdateDataDialog updateDataDialog = new UpdateDataDialog(MainFrame.getMainWindow(), RobotDataJson.class.getSimpleName());
                updateDataDialog.centerToWindow(MainFrame.getMainWindow());
                updateDataDialog.setVisible(true);

                if (updateDataDialog.isServerDown()) {
                    return;
                }

                if (updateDataDialog.isDataUpdated()) {

                    DatabaseVersionJson serverVersion = updateDataDialog.getServerDatabaseVersion();
                    String login = serverVersion.getLogin(RobotDataJson.class);
                    String user;
                    if ((login == null) || (login.length() == 0)) {
                        user = "Someone";
                    } else {
                        user = DataManager.getLastThenFirstNameFromActorKey(login);
                    }


                    QuestionDialog questionDialog = new QuestionDialog(MainFrame.getMainWindow(), "Robot Plates Update", user+" has modified Robot Plates. You can not proceed, do you want to reload ?");
                    questionDialog.centerToWindow(MainFrame.getMainWindow());
                    questionDialog.setVisible(true);
                    if (questionDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                        // reload plates
                        reinit();
                        loadData();
                    }
                    return;
                }

                HashSet<String> namesTaken = new HashSet<>();
                int size = m_plateListModel.getSize();
                for (int i=0;i<size;i++) {
                    EditablePlate editablePlate = m_plateListModel.getElementAt(i);
                    String name = editablePlate.getPlate().getName();
                    namesTaken.add(name);
                }

                Date d = new Date();
                String plateNameBase = UtilDate.dateToPlateName(d);
                String plateName = plateNameBase;
                int index = 1;
                while (namesTaken.contains(plateName)) {
                    plateName = plateNameBase + "-" + index;
                    index++;
                }

                Preferences preferences = EpimsPreferences.root();
                int defaultPlateSizeX = preferences.getInt (PreferencesKeys.PLATE_DIM_X, 12);
                int defaultPlateSizeY = preferences.getInt (PreferencesKeys.PLATE_DIM_Y, 8);


                AddPlateDialog dialog = new AddPlateDialog(MainFrame.getMainWindow(), namesTaken, plateName, defaultPlateSizeX, defaultPlateSizeY);
                dialog.centerToWindow(MainFrame.getMainWindow());
                dialog.setVisible(true);
                if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                    VirtualPlateJson virtualPlateJson = dialog.getVirtualPlateToCreate(DataManager.getLoggedUser());


                    AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
                        @Override
                        public boolean mustBeCalledInAWT() {
                            return true;
                        }

                        @Override
                        public void run(boolean success, long taskId, boolean finished) {
                            if (success) {

                                // save last plate size as preferences
                                preferences.putInt(PreferencesKeys.PLATE_DIM_X, virtualPlateJson.getXSize());
                                preferences.putInt(PreferencesKeys.PLATE_DIM_Y, virtualPlateJson.getYSize());


                                EditablePlate plate = new EditablePlate(virtualPlateJson);
                                m_plateListModel.addElement(plate);

                                m_plateList.setSelectedValue(plate, true);

                                m_robotListPlatePanel.reinit();

                                ArrayList<VirtualPlateJson> plateList = new ArrayList();
                                int size = m_plateListModel.getSize();
                                for (int i=0;i<size;i++) {
                                    EditablePlate editablePlate = m_plateListModel.getElementAt(i);
                                    plateList.add(editablePlate.getPlate());
                                }

                                m_robotListPlatePanel.setPlates(m_robotSamplesPanel.getUnassignedSamples(),  plateList);
                            } else {
                                InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Server Error\n\nAction has been aborted.");
                                infoDialog.centerToWindow(MainFrame.getMainWindow());
                                infoDialog.setVisible(true);
                            }
                        }
                    };

                    AddPlateTask task = new AddPlateTask(callback, virtualPlateJson);
                    AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

                }
            }
        });

        m_savePlateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {


                UpdateDataDialog updateDataDialog = new UpdateDataDialog(MainFrame.getMainWindow(), RobotDataJson.class.getSimpleName());
                updateDataDialog.centerToWindow(MainFrame.getMainWindow());
                updateDataDialog.setVisible(true);

                if (updateDataDialog.isServerDown()) {
                    return;
                }

                if (updateDataDialog.isDataUpdated()) {
                    DatabaseVersionJson serverVersion = updateDataDialog.getServerDatabaseVersion();
                    String login = serverVersion.getLogin(RobotDataJson.class);
                    String user;
                    if ((login == null) || (login.length() == 0)) {
                        user = "Someone";
                    } else {
                        user = DataManager.getLastThenFirstNameFromActorKey(login);
                    }


                    QuestionDialog questionDialog = new QuestionDialog(MainFrame.getMainWindow(), "Robot Plates Update", user+" has modified Robot Plates. You can not proceed, do you want to reload ?");
                    questionDialog.centerToWindow(MainFrame.getMainWindow());
                    questionDialog.setVisible(true);
                    if (questionDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                        // reload plates
                        reinit();
                        loadData();
                    }
                    return;
                }


                final EditablePlate plate = (EditablePlate) m_plateList.getSelectedValue();

                VirtualPlateJson virtualPlateJson = plate.getPlate();

                VirtualPlateJson virtualPlateJsonToSave = m_robotPlatePanel.getPlateToSave(virtualPlateJson);


                setLoading(getNewLoadingIndex(), false, true);
                AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
                    @Override
                    public boolean mustBeCalledInAWT() {
                        return true;
                    }

                    @Override
                    public void run(boolean success, long taskId, boolean finished) {
                        if (success) {
                            plate.setPlate(virtualPlateJsonToSave);
                            plateModified(false);
                            m_robotPlatePanel.setPlate(plate, true);

                            m_robotListPlatePanel.reinit();

                            ArrayList<VirtualPlateJson> plateList = new ArrayList();
                            int size = m_plateListModel.getSize();
                            for (int i=0;i<size;i++) {
                                EditablePlate editablePlate = m_plateListModel.getElementAt(i);
                                plateList.add(editablePlate.getPlate());
                            }

                            m_robotListPlatePanel.setPlates(m_robotSamplesPanel.getUnassignedSamples(), plateList);

                        } else {
                            InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Server Error\n\nAction has been aborted.");
                            infoDialog.centerToWindow(MainFrame.getMainWindow());
                            infoDialog.setVisible(true);
                        }
                        setLoaded(m_id);
                    }
                };

                SavePlateTask task = new SavePlateTask(callback, virtualPlateJsonToSave);
                AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
            }
        });

        m_undoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                QuestionDialog questionDialog = new QuestionDialog(MainFrame.getMainWindow(), "Discard All Modifications", "Do you want to discard all modifications of "+m_plateList.getSelectedValue().toString()+" plate ?");
                questionDialog.centerToWindow(MainFrame.getMainWindow());
                questionDialog.setVisible(true);
                if (questionDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                    EditablePlate plate = (EditablePlate) m_plateList.getSelectedValue();
                    HashSet<ColoredRobotPlanning> freedPlanningJsonSet = m_robotPlatePanel.setPlate(plate, true);

                    HashSet<ColoredRobotPlanning> cleanedHashSet = new HashSet<>();
                    HashSet<Integer> tmpMapToRelink = new HashSet<>();
                    if (freedPlanningJsonSet != null) {
                        for (ColoredRobotPlanning coloredRobotPlanning : freedPlanningJsonSet) {
                            RobotPlanningJson robotPlanningJson = coloredRobotPlanning.m_robotPlanning;

                            boolean alreadyDone = tmpMapToRelink.contains(robotPlanningJson.getId());
                            if (!alreadyDone) {
                                tmpMapToRelink.add(robotPlanningJson.getId());
                                cleanedHashSet.add(coloredRobotPlanning);
                                coloredRobotPlanning.m_wells.clear();
                            }
                        }
                    }

                    m_robotSamplesPanel.setPlate(plate, cleanedHashSet);

                    plateModified(false);
                }
            }
        });

        m_exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {

                // set colour mode used for excel
                Object previousColorMode =  m_colorationModeCombobox.getSelectedItem();
                m_colorationModeCombobox.setSelectedItem(STUDY);

                Preferences preferences = EpimsPreferences.root();
                String defaultDirString = preferences.get(PreferencesKeys.EXCEL_EXPORT_DIR, System.getProperty("user.home"));
                File defaultDir = new File(defaultDirString);
                if (!defaultDir.exists()) {
                    defaultDir = new File(System.getProperty("user.home"));
                    if (!defaultDir.exists()) {
                        defaultDir = new File(".");
                    }
                }


                JFileChooser fchooser = new JFileChooser(defaultDir);
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Excel", "xlsx");
                fchooser.setFileFilter(filter);
                fchooser.setSelectedFile(new File(((EditablePlate) m_plateList.getSelectedValue()).getPlate().getName()+".xlsx"));


                int result = fchooser.showSaveDialog(m_exportButton);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = fchooser.getSelectedFile();

                    String absolutePath = file.getAbsolutePath();
                    String fileName = file.getName();
                    if (fileName.indexOf('.') == -1) {
                        absolutePath += ".xlsx";
                    }

                    if (file.exists() && !file.canWrite()) {
                        InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Writable Error", "This File is not Writable");
                        infoDialog.centerToWindow(MainFrame.getMainWindow());
                        infoDialog.setVisible(true);

                        m_colorationModeCombobox.setSelectedItem(previousColorMode);
                        return;
                    }

                    if (! isFileClosed(file)) {
                        InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Writable Error", "This File is opened in another application like Excel.");
                        infoDialog.centerToWindow(MainFrame.getMainWindow());
                        infoDialog.setVisible(true);

                        m_colorationModeCombobox.setSelectedItem(previousColorMode);
                        return;
                    }

                    try {
                        ExcelXMLExporter.export(m_robotPlatePanel.getWells(), m_robotPlatePanel.getDimX(), m_robotPlatePanel.getDimY(), ((EditablePlate) m_plateList.getSelectedValue()).getPlate().getName(), absolutePath);

                        preferences.put(PreferencesKeys.EXCEL_EXPORT_DIR, file.getParentFile().getAbsolutePath());
                        try {
                            preferences.flush();
                        } catch (Exception prefException) {

                        }


                        InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.INFO, "Export", "Export to Excel has succeeded.");
                        infoDialog.centerToWindow(MainFrame.getMainWindow());
                        infoDialog.setVisible(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                        InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Export", "Export to Excel has failed.");
                        infoDialog.centerToWindow(MainFrame.getMainWindow());
                        infoDialog.setVisible(true);
                    }
                }

                m_colorationModeCombobox.setSelectedItem(previousColorMode);


            }
        });

        return toolbar;

    }

    /**
     * Check if the file is opened by another application
     * @param file
     * @return
     */
    private boolean isFileClosed(File file) {
        boolean closed;
        Channel channel = null;
        try {
            channel = new RandomAccessFile(file, "rw").getChannel();
            closed = true;
        } catch(Exception ex) {
            closed = false;
        } finally {
            if(channel!=null) {
                try {
                    channel.close();
                } catch (IOException ex) {
                    // exception handling
                }
            }
        }
        return closed;
    }

    private void setColourMode() {
        String colour = (String) m_colorationModeCombobox.getSelectedItem();
        if (colour.equals(SAMPLE)) {
            m_robotPlatePanel.setColourMode(RobotPlatePanel.COLOR_MODE.SAMPLE_COLOUR);
        } else if (colour.equals(TRYPSIN)) {
            m_robotPlatePanel.setColourMode(RobotPlatePanel.COLOR_MODE.TRYPSIN_COLOUR);
        } else if (colour.equals(USER)) {
            m_robotPlatePanel.setColourMode(RobotPlatePanel.COLOR_MODE.USER_COLOUR);
        } else if (colour.equals(STUDY)) {
            m_robotPlatePanel.setColourMode(RobotPlatePanel.COLOR_MODE.STUDY_COLOUR);
        }
    }


    public void loadData() {

        if (m_loadDataDone) {
            return;
        }


        UpdateDataDialog updateDataDialog = new UpdateDataDialog(MainFrame.getMainWindow(), null);
        updateDataDialog.centerToWindow(MainFrame.getMainWindow());
        updateDataDialog.setVisible(true);

        if (updateDataDialog.isServerDown()) {
            return;
        }

        setLoading(getNewLoadingIndex());

        final RobotDataJson[] robotDataJsonArray = new RobotDataJson[1];
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, boolean finished) {

                RobotDataJson robotDataJson = robotDataJsonArray[0];
                if (success) {
                    if (m_loadDataDone) {
                        return;
                    }

                    reinit_running = true;
                    try {

                        m_loadDataDone = true;

                        m_plateListModel.clear();

                        HashMap<String, VirtualPlateJson> platesMap = robotDataJson.getPlatesMap();

                        ArrayList<VirtualPlateJson> plateSortedList = new ArrayList(platesMap.values());
                        Collections.sort(plateSortedList);

                        for (VirtualPlateJson plate : plateSortedList) {
                            m_plateListModel.addElement(new EditablePlate(plate));
                        }

                        m_robotSamplesPanel.setAvailableSamples(robotDataJson.getFreeRobotPlanningList());

                        m_robotListPlatePanel.setPlates(m_robotSamplesPanel.getUnassignedSamples(), plateSortedList);

                    } catch (Exception e) {
                        reinit_running = false;
                        throw e;
                    }
                    reinit_running = false;

                } else {
                    reinit_running = true;
                    try {
                        m_plateListModel.clear();
                        m_robotSamplesPanel.setAvailableSamples(null);
                    } catch (Exception e) {
                        reinit_running = false;
                        throw e;
                    }
                    reinit_running = false;

                }

                setLoaded(m_id);
            }
        };

        RobotPlateTask task = new RobotPlateTask(callback, robotDataJsonArray);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
    }
    private boolean m_loadDataDone = false;



    public class PlateListRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (((EditablePlate) value).isModified()) {
                l.setIcon(IconManager.getIcon(IconManager.IconType.STAR_COMPULSORY));
                l.setHorizontalTextPosition(SwingConstants.LEADING);
            } else {
                l.setIcon(null);
                l.setHorizontalTextPosition(SwingConstants.LEFT);
            }

            return l;
        }
    }

    public void enableTabbedPane(boolean canEditPlates) {
        m_tabbedPane.setEnabledAt(1, canEditPlates);
        if (! canEditPlates) {
            m_tabbedPane.setSelectedIndex(0);
        }
    }

    public static boolean hasModification() {
        if (m_singleton == null) {
            return false;
        }
        return m_singleton.hasAPlateModified();
    }

}
