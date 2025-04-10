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

package fr.epims.dataaccess;

import javax.swing.*;


/**
 *
 * Base class for authentified or non authentified tasks
 *
 * @author JM235353
 *
 */
public abstract class AbstractDatabaseTask implements Comparable<AbstractDatabaseTask> {


    protected TaskInfo m_taskInfo = null;

    // callback is called by the AccessDatabaseThread when the data is fetched
    protected AbstractDatabaseCallback m_callback;

    // id of the action
    protected Long m_id;
    private static long m_idIncrement = 0;

    protected TaskError m_taskError = null;
    protected int m_errorId = -1;

    protected boolean m_prioritary = false;


    public AbstractDatabaseTask(AbstractDatabaseCallback callback, TaskInfo taskInfo) {
        m_taskInfo = taskInfo;
        m_callback = callback;

        m_idIncrement++;
        if (m_idIncrement == Long.MAX_VALUE) {
            m_idIncrement = 0;
        }
        m_id = m_idIncrement;

    }

    public void setPrioritary() {
        m_prioritary = true;
    }

    public TaskInfo getTaskInfo() {
        return m_taskInfo;
    }

    public void updatePercentage() {
        // nothing to do
    }

    /**
     * called when a task is aborted.
     * It is important in the case of tasks with subtask
     * to clean the data loaded (and not let partially loaded data)
     */
    public void abortTask() {
        m_taskInfo.setAborted();
    }


    public void deleteThis() {
        m_callback = null;
    }



    /**
     * Return the id of the Task
     *
     * @return
     */
    public Long getId() {
        return m_id;
    }




    /**
     * Method called by the AccessDatabaseThread to fetch Data from database
     *
     * @return
     */
    public abstract boolean fetchData();

    /**
     * Method called by the AccessDatabaseThread to check if data is or not
     * already known
     *
     * @return
     */
    public abstract boolean needToFetch();

    /**
     * Return if there are sub tasks which remain to be done later
     *
     * @return
     */
    public boolean hasSubTasksToBeDone() {
        return false;
    }

    /**
     * Method called after the data has been fetched
     *
     * @param success boolean indicating if the fetch has succeeded
     */
    public void callback(final boolean success, final boolean finished) {
        if (m_callback == null) {

            m_taskInfo.setFinished(success, m_taskError, true);

            return;
        }

        m_callback.setErrorMessage(m_taskError, m_errorId);

        if (m_callback.mustBeCalledInAWT()) {
            // Callback must be executed in the Graphical thread (AWT)
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    m_callback.run(success, m_id, finished);
                    m_taskInfo.setFinished(success, getTaskError(), true);
                }
            });
        } else {
            // Method called in the current thread
            // In this case, we assume the execution is fast.
            m_callback.run(success, m_id, finished);
            m_taskInfo.setFinished(success, getTaskError(), true);
        }


    }

    /**
     * Used to prioritize actions
     *
     * @param task
     * @return
     */
    @Override
    public int compareTo(AbstractDatabaseTask task) {

        if ((m_prioritary) && (!task.m_prioritary)) {
            return -1;
        } else if ((!m_prioritary) && (task.m_prioritary)) {
            return 1;
        }

        // for equal priority, we compare on id : priority is given to older id == smaller
        long diff = m_id - task.m_id;
        if (diff == 0) {
            return 0;
        }
        return (diff) > 0 ? 1 : -1;
    }

    public TaskError getTaskError() {
        return m_taskError;
    }
}
