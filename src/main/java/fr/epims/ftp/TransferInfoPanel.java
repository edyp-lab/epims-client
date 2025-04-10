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

import fr.epims.ui.common.DecoratedTable;
import fr.epims.ui.common.FlatButton;
import fr.epims.ui.common.IconManager;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * Panel which displays all the files to download or upload with their status
 *
 * @author JM235353
 *
 */
public class TransferInfoPanel extends JPanel {

    private DecoratedTable m_transferTable;
    private TransferTableModel m_model;
    private FlatButton m_deleteButton;


    public TransferInfoPanel() {
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        m_transferTable = new DecoratedTable();
        m_model = new TransferTableModel();
        m_transferTable.setModel(m_model);
        FTPTransferThread.getTransferThread().setModel(m_model);

        JScrollPane tableScrollPane = new JScrollPane(m_transferTable);
        m_transferTable.setFillsViewportHeight(true);
        tableScrollPane.setViewport(tableScrollPane.getViewport());

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        add(tableScrollPane, c);

        c.gridx++;
        c.weightx = 0;
        add(createToolbar(), c);

        ListSelectionModel selectionModel = m_transferTable.getSelectionModel();

        selectionModel.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                int nbRowSelected = m_transferTable.getSelectedRows().length;
                m_deleteButton.setEnabled(nbRowSelected >0);
            }
        });


    }

    private JToolBar createToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setOrientation(SwingConstants.VERTICAL);

        m_deleteButton = new FlatButton(IconManager.getIcon(IconManager.IconType.ERASER),false);
        m_deleteButton.setEnabled(false);
        toolbar.add(m_deleteButton);

        m_deleteButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                HashSet<Integer> rowToRemoveSet = new HashSet();
                for (int row : m_transferTable.getSelectedRows()) {
                    row = m_transferTable.convertRowIndexToModel(row);
                    rowToRemoveSet.add(row);
                }
                m_model.removeFromDownloadList(rowToRemoveSet);

            }
        });

        return toolbar;

    }


    public Dimension getPreferredSize() {
        return new Dimension(600,200);
    }

    public void addFilesToTransfer(ArrayList<TransferInfo> transferInfoList) {
        m_model.addDownloadInfoList(transferInfoList);

        // awake the thread which transfers data
        FTPTransferThread.getTransferThread().awake();

    }

    public void setRootDirectoryName(String rootDirectoryName) {
        m_model.setRootDirectoryName(rootDirectoryName);
    }

    public void updateList() {
        m_model.updateData();
    }


    public static class TransferInfoRenderer extends DefaultTableCellRenderer {

        private boolean m_state;

        public TransferInfoRenderer(boolean state) {
            m_state = state;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            JLabel label = (JLabel) super.getTableCellRendererComponent(table, "", isSelected, false, row, column);

            TransferInfo transferInfo = (TransferInfo) value;

            label.setIcon(m_state ? transferInfo.getStateIcon() : transferInfo.getTransferIcon());

            return this;
        }
    }
}
