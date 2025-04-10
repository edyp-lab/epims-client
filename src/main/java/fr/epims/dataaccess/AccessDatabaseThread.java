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

import java.util.*;

import org.slf4j.LoggerFactory;

/**
 *
 * Dispatch actions to be done (generally data to load from the server) to a pool of threads.
 *
 * @author JM235353
 *
 */
public class AccessDatabaseThread extends Thread {

    private static AccessDatabaseThread m_instance;
    private PriorityQueue<AbstractDatabaseTask> m_actions;
    private HashMap<Long, AbstractDatabaseTask> m_actionMap;
    private ArrayList<Long> m_abortedActionIdList;

    private AccessDatabaseWorkerPool m_workerPool = null;

    private AccessDatabaseThread() {
        super("AccessDatabaseThread"); // useful for debugging

        m_actions = new PriorityQueue<>();
        m_actionMap = new HashMap<>();
        m_abortedActionIdList = new ArrayList<>();

        m_workerPool = AccessDatabaseWorkerPool.getWorkerPool();

    }

    public static AccessDatabaseThread getAccessDatabaseThread() {
        if (m_instance == null) {
            m_instance = new AccessDatabaseThread();
            m_instance.start();
        }
        return m_instance;
    }

    /**
     * Main loop of the thread
     */
    @Override
    public void run() {
        try {
            LoggerFactory.getLogger("Epims.Client").debug("Start Access Database Thread");
            while (true) {
                AbstractDatabaseTask action = null;
                synchronized (this) {

                    while (true) {

                        // Management of aborted task
                        if (!m_abortedActionIdList.isEmpty()) {
                            int nbAbortedTask = m_abortedActionIdList.size();
                            for (int i=0;i<nbAbortedTask;i++) {
                                Long taskId = m_abortedActionIdList.get(i);
                                AbstractDatabaseTask taskToStop = m_actionMap.remove(taskId);
                                if (taskToStop == null) {
                                    continue;
                                }
                                if (m_actions.contains(taskToStop)) {
                                    m_actions.remove(taskToStop);

                                    //TaskInfoManager.getTaskInfoManager().cancel(abortedTask.getTaskInfo());

                                    TaskInfo info = taskToStop.getTaskInfo();
                                    if (info.isWaiting()) {
                                        // task has not already started, cancel it
                                        TaskInfoManager.getTaskInfoManager().cancel(info);
                                    } else {
                                        taskToStop.abortTask();
                                    }

                                    taskToStop.deleteThis();
                                }
                            }
                            m_abortedActionIdList.clear();
                        }



                        // look for a task to be done
                        if (!m_actions.isEmpty()) {
                            action = m_actions.poll();
                            break;
                        }
                        wait();
                    }
                    notifyAll();
                }




                Object workerPoolMutex = m_workerPool.getMutex();
                synchronized (workerPoolMutex) {

                    AccessDatabaseWorkerThread workerThread = null;
                    while (true) {
                        workerThread = m_workerPool.getWorkerThread();
                        if (workerThread != null) {
                            break;
                        }
                        workerPoolMutex.wait();
                    }
                    workerThread.setAction(action);

                    workerPoolMutex.notifyAll();
                }



            }


        } catch (Throwable t) {
             LoggerFactory.getLogger("Epims.Client").error("Unexpected exception in main loop of AccessDatabaseThread", t);
            m_instance = null; // reset thread
        }

    }

    public void actionDone(AbstractDatabaseTask task) {
        synchronized (this) {
            // check if subtasks need to be done
            if (task.hasSubTasksToBeDone()) {
                // put back action in the queue for subtasks
                task.updatePercentage();
                m_actions.add(task);

            } else {
                // action completely finished
                m_actionMap.remove(task.getId());

                TaskError taskError = task.getTaskError();
                task.getTaskInfo().setFinished((taskError==null), taskError, true);
            }


            notifyAll();
        }

    }

    /**
     * Add a task to be done later according to its priority
     *
     */
    public final void addTask(AbstractDatabaseTask task) {

        // check if we need to fetch data for this action
        if (!task.needToFetch()) {
            // fetch already done : return immediately
            task.callback(true, true);


            return;
        }

        TaskInfoManager.getTaskInfoManager().add(task.getTaskInfo());

        // action is queued
        synchronized (this) {
            m_actions.add(task);
            m_actionMap.put(task.getId(), task);
            notifyAll();
        }
    }

    public final void abortTask(Long taskId) {
        synchronized (this) {
            abortTaskImpl(taskId);
        }
    }

    public final void abortTasks(Set<Long> taskIds) {
        synchronized (this) {
            Iterator<Long> it = taskIds.iterator();
            while (it.hasNext()) {
                abortTask(it.next());
            }
        }
    }

    private void abortTaskImpl(Long taskId) {
        AbstractDatabaseTask task = m_actionMap.get(taskId);
        if (task == null) {
            // task is already finished
            return;
        }
        m_abortedActionIdList.add(taskId);

    }


}
