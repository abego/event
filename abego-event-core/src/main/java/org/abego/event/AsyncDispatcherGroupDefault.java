package org.abego.event;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

class AsyncDispatcherGroupDefault implements AsyncDispatcherGroup, AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(AsyncDispatcherGroupDefault.class.getName());
    private final EventService eventService;
    private final BlockingQueue<Runnable> codeToRun = new LinkedBlockingQueue<>();
    private final Thread thread;
    private final AtomicBoolean closed = new AtomicBoolean();
    private final AtomicBoolean endRunLoop = new AtomicBoolean();
    private final AtomicBoolean wasInterrupted = new AtomicBoolean();

    private class AsyncEventDispatcherDefault implements EventDispatcher {
        private final ExplicitDispatcher explicitDispatcher =
                eventService.newExplicitDispatcher(this::onDispatchPending);

        @Override
        public void process(Object event, Iterable<Consumer<Object>> listeners) {
            synchronized (closed) {
                if (!closed.get()) {
                    explicitDispatcher.process(event, listeners);
                } else {
                    LOGGER.log(Level.WARNING,
                            "Ignoring event {0}. AsyncDispatcherGroup already closed.", event);
                }
            }
        }

        private void onDispatchPending(ExplicitDispatcher dispatcher) {
            codeToRun.add(dispatcher::dispatch);
        }
    }

    private AsyncDispatcherGroupDefault(EventService eventService) {
        this.eventService = eventService;
        thread = new Thread(this::runLoop);
        thread.setDaemon(true);
        thread.start();
    }

    static AsyncDispatcherGroupDefault newAsyncEventDispatcherGroupDefault(EventService eventService) {
        return new AsyncDispatcherGroupDefault(eventService);
    }

    @Override
    public EventDispatcher newAsyncDispatcher() {
        return new AsyncEventDispatcherDefault();
    }

    @SuppressWarnings("squid:S2142") // see body
    private void runLoop() {
        while (!endRunLoop.get()) {
            try {
                Runnable runnable = codeToRun.take();
                runnable.run();

            } catch (InterruptedException e) {
                // Disabled SonarLint rule
                //     java:S2142 (“InterruptedException” should not be ignored)
                // because:
                //
                // Don't interrupt thread now, to ensure the thread lives
                // long enough the pending code can run. The thread will die
                // once all pending items in the codeToRun queue and the
                // shutdown item (see #close) are executed. Then there will be
                // a re-interrupt (see end of this method).
                wasInterrupted.set(true);
                close();
            }
        }
        if (wasInterrupted.get()) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Closes the {@link AsyncDispatcherGroup}.
     * <p>
     * When the AsyncDispatcherGroup is closed {@link EventDispatcher}s
     * it created will no longer dispatch "new" events. However, they will
     * still dispatch events posted before calling {@code close()}.
     */
    @Override
    public void close() {
        synchronized (closed) {
            closed.set(true);

            codeToRun.add(() -> endRunLoop.set(true));
        }
    }

    public boolean isClosed() {
        return closed.get();
    }

    public Thread getThread() {
        return thread;
    }
}
