package com.imaginary.swing;

import com.imaginary.util.FifoStack;
import javax.swing.SwingUtilities;

public abstract class WorkerThread {
    static private FifoStack queue  = new FifoStack();
    static private Thread    worker = null;

    /**
     * Places a worker thread object onto the worker queue for
     * execution in the worker thread. When the time is right, the
     * <CODE>run()</CODE> method in the specified <CODE>WorkerThread</CODE>
     * object will be run inside the worker thread. Upon completion,
     * the <CODE>complete()</CODE> method will then be executed inside
     * the event queue.
     * @param wt the worker to be executed inside the worker thread
     */
    static public void invokeWorker(WorkerThread wt) {
        synchronized( queue ) {
            queue.push(wt);
            if( worker == null ) {
                worker = new Thread() {
                        public void run() {
                            runThread();
                        }
                    };
                worker.setDaemon(true);
                worker.setPriority(Thread.NORM_PRIORITY);
                worker.setName("Worker Queue");
                worker.start();
            }
        }
    }

    static private void runThread() {
        while( true ) {
            final WorkerThread wt;
            
            synchronized( queue ) {
                if( queue.isEmpty() ) {
                    worker = null;
                    return;
                }
                wt = (WorkerThread)queue.pop();
            }
            try {
                Runnable r;
                
                wt.run();
                r = new Runnable() {
                        public void run() {
                            wt.complete();
                        }
                    };
                // place a call to the complete() method in the event queue
                SwingUtilities.invokeLater(r);
            }
            catch( Exception e ) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method is called inside the Swing event queue. An implementation
     * of this class does not need to implement this method unless it
     * wants processing to occur specifically in the event queue.
     */
    public void complete() {
    }

    /**
     * Implementors must implement this method to specify the processing
     * that should occur in the worker thread.
     */
    public abstract void run();
}
