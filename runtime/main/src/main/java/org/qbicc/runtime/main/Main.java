package org.qbicc.runtime.main;

import static org.qbicc.runtime.CNative.*;
import static org.qbicc.runtime.posix.PThread.pthread_exit;

import org.qbicc.runtime.Build;
import org.qbicc.runtime.NotReachableException;
import org.qbicc.runtime.deserialization.RuntimeObjectDeserializer;

/**
 * Holds the native image main entry point.
 */
public final class Main {

    private Main() {
    }

    /**
     * This is a stub that gets redirected to the real main method by the main method plugin.
     *
     * @param args the arguments to pass to the main program
     */
    static native void userMain(String[] args);

    static native ThreadGroup createSystemThreadGroup();

    // TEMP: A Watermark field to verify RuntimeObjectDeserializer.initializeHeap() runs before user main is entered
    static int[] watermark;

    @export
    public static c_int main(c_int argc, char_ptr[] argv) {

        // first set up VM
        // ...
        // next set up the initial thread
        attachNewThread("main", createSystemThreadGroup());

        // Deserialize the initial heap
        RuntimeObjectDeserializer.initializeHeap();

        // TEMP: Remove watermark check once we are using heap serialization to initialize statics
        if (watermark.length != 2 || (watermark[0] + watermark[1] != 42)) {
            if (Build.Target.isPosix()) {
                pthread_exit(word(1L).cast(void_ptr.class));
            }
        }

        // now cause the initial thread to invoke main
        final String[] args = new String[argc.intValue()];
        for (int i = 1; i < argc.intValue(); i++) {
            args[i] = utf8zToJavaString(argv[i].cast());
        }
        //todo: string construction
        //String execName = utf8zToJavaString(argv[0].cast());
        try {
            userMain(args);
        } catch (Throwable t) {
            Thread.UncaughtExceptionHandler handler = Thread.currentThread().getUncaughtExceptionHandler();
            if (handler != null) {
                handler.uncaughtException(Thread.currentThread(), t);
            }
        }
        if (Build.Target.isPosix()) {
            pthread_exit(zero());
        }
        // todo: windows
        throw new NotReachableException();
    }
}
