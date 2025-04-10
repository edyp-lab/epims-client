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

import java.util.Stack;

/**
 *
 * Pool of threads used to execute actions
 *
 * @author JM235353
 *
 */
public class AccessDatabaseWorkerPool {



    private static AccessDatabaseWorkerPool m_workerPool = null;

    private Stack<AccessDatabaseWorkerThread> m_availableThreads = new Stack<>();

    private static int MAX_THREADS = 3;
    int m_nbThreads;

    public synchronized static AccessDatabaseWorkerPool getWorkerPool() {
        if (m_workerPool == null) {
            m_workerPool = new AccessDatabaseWorkerPool();
        }
        return m_workerPool;
    }

    private AccessDatabaseWorkerPool() {

    }

    public Object getMutex() {
        return this;
    }

    public synchronized void threadFinished(AccessDatabaseWorkerThread threadFinished) {
        m_availableThreads.add(threadFinished);

        notifyAll();
    }

    public synchronized AccessDatabaseWorkerThread getWorkerThread() {

        if (!m_availableThreads.isEmpty()) {
            return m_availableThreads.pop();
        }
        if (m_nbThreads < MAX_THREADS) {
            m_nbThreads++;
            AccessDatabaseWorkerThread thread = new AccessDatabaseWorkerThread(this);
            thread.start();
            return thread;
        }

        return null;

    }


}
