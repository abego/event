package org.abego.event;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

//TODO: reduce Logging when problems with close/interrupt are resolved
class AsyncDispatcherGroupDefault implements AsyncDispatcherGroup, AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(AsyncDispatcherGroupDefault.class.getName());
    private final EventService eventService;
    private final BlockingQueue<Runnable> codeToRun = new LinkedBlockingQueue<>();
    private final Thread thread;
    private final AtomicBoolean closed = new AtomicBoolean();
    private final AtomicBoolean endRunLoop = new AtomicBoolean();

    private class AsyncEventDispatcherDefault implements EventDispatcher {
        private final ExplicitDispatcher explicitDispatcher =
                eventService.newExplicitDispatcher(this::onDispatchPending);

        @Override
        public void process(Object event, Iterable<Consumer<Object>> listeners) {
            if (!closed.get()) {
                explicitDispatcher.process(event, listeners);
            } else {
                LOGGER.log(Level.WARNING,
                        "Ignoring event {0}. AsyncDispatcherGroup already closed.", event);
            }
        }

        private void onDispatchPending(ExplicitDispatcher dispatcher) {
            LOGGER.log(Level.INFO, "dispatch is pending");
            LOGGER.log(Level.INFO,
                    "    will add dispatch to run by {0}. ", this);
            codeToRun.add(dispatcher::dispatch);
            LOGGER.log(Level.INFO,
                    "    did add dispatch to run by {0}. ", this);
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
                LOGGER.log(Level.INFO,
                        "Wait for code to run by dispatcher {0}.", this);
                Runnable runnable = codeToRun.take();
                LOGGER.log(Level.INFO,
                        "Got code to run by dispatcher {0}", this);
                LOGGER.log(Level.INFO, "    Will run.");
                runnable.run();
                LOGGER.log(Level.INFO, "    Did run.");

            } catch (InterruptedException e) {
                // Disabled SonarLint rule
                //     java:S2142 (“InterruptedException” should not be ignored)
                // because:
                //
                // Don't interrupt thread, to ensure the thread lives
                // long enough the pending code can run. The thread will die
                // once all pending items in the codeToRun queue and the
                // shutdown item (see #close) are executed.
                LOGGER.log(Level.INFO,
                        "Received InterruptedException. Will close dispatcher {0}.", this);
                LOGGER.log(Level.INFO, "   Will close dispatcher.");
                close();
                LOGGER.log(Level.INFO, "   InterruptedException handling done.");
                Thread.currentThread().interrupt();
                return;
            }
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
        LOGGER.log(Level.INFO, "Entering AsyncDispatcherGroup.close ({0})", this);
        closed.set(true);

        LOGGER.log(Level.INFO, "AsyncDispatcherGroup: queued Shutdown request ({0})", this);
        codeToRun.add(() -> {
            LOGGER.log(Level.INFO, "will set endRunLoop to true of AsyncDispatcherGroup ({0})", this);
            endRunLoop.set(true);
            LOGGER.log(Level.INFO, "did set endRunLoop to true of AsyncDispatcherGroup ({0})", this);
        });
        LOGGER.log(Level.INFO, "AsyncDispatcherGroup: queued Shutdown request ({0})", this);

    }

    public boolean isClosed() {
        return closed.get();
    }

    public Thread getThread() {
        return thread;
    }
}
