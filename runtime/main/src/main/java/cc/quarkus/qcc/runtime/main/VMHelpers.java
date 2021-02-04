package cc.quarkus.qcc.runtime.main;

import static cc.quarkus.qcc.runtime.CNative.*;
import static cc.quarkus.qcc.runtime.posix.PThread.*;
import static cc.quarkus.qcc.runtime.stdc.Stdlib.*;

/**
 * Runtime Helpers to support the operation of the compiled code.
 */
public final class VMHelpers {

	// TODO: mark this with a "AlwaysInline" annotation
	public static final int fast_instanceof(Object instance, Class<?> castClass) {
		if (instance == null) return 0;	// null isn't an instance of anything
		Class<?> instanceClass = instance.getClass();
		if (instanceClass == castClass) {
			return 1;
		}
		return full_instanceof(instance, castClass);
	}

	// TODO: mark this with a "NoInline" annotation
	public static final int full_instanceof(Object instance, Class<?> castClass) {
		return 0; // TODO: full instance of support
	}

	private static final void omError(c_int nativeErrorCode) throws IllegalMonitorStateException {
	    int errorCode = nativeErrorCode.intValue();
	    if (0 != errorCode) {
	        throw new IllegalMonitorStateException("error code is: " + errorCode);
        }
    }

    // TODO: mark this with a "NoInline" annotation
    static final void monitorEnter(Object object) throws IllegalMonitorStateException {
        /* get native mutex associated with object, else atomically create a new one. */
        NativeObjectMonitor monitor = Main.objectMonitorNatives.computeIfAbsent(object, k -> {
                ptr<pthread_mutexattr_t> attr = malloc(sizeof(pthread_mutexattr_t.class));
                ptr<pthread_mutex_t> m = malloc(sizeof(pthread_mutex_t.class));
                omError(pthread_mutexattr_init(attr));
                /* recursive lock allows for locking multiple times on the same thread. */
                omError(pthread_mutexattr_settype(attr, PTHREAD_MUTEX_RECURSIVE));
                omError(pthread_mutex_init(m, attr));
                omError(pthread_mutexattr_destroy(attr));
                free(attr);
                return new NativeObjectMonitor(m);
            }
        );
        omError(pthread_mutex_lock(monitor.getPthreadMutex()));
    }

    // TODO: mark this with a "NoInline" annotation
    static final void monitorExit(Object object) throws IllegalMonitorStateException {
        NativeObjectMonitor monitor = Main.objectMonitorNatives.get(object);
        if (null == monitor) {
            throw new IllegalMonitorStateException("monitor could not be found for monitorexit");
        }
        omError(pthread_mutex_unlock(monitor.getPthreadMutex()));
    }
}