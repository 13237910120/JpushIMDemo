package boby.com.jpushimdemo.mine.utils;

import android.os.Looper;

/**
 * Created by boby on 2017/11/20 0020.
 */

public class ThreadUtil {

    private static HandlerPoster mainPoster = null;

    private static HandlerPoster getMainPoster() {
        if (mainPoster == null) {
            synchronized (ThreadUtil.class) {
                if (mainPoster == null) {
                    mainPoster = new HandlerPoster(Looper.getMainLooper(), 20);
                }
            }
        }
        return mainPoster;
    }

    /**
     * Asynchronously
     * The child thread asynchronous run relative to the main thread,
     * not blocking the child thread
     *
     * @param runnable Runnable Interface
     */
    public static void runOnMainThreadAsync(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
            return;
        }
        getMainPoster().async(runnable);
    }

    /**
     * Synchronously
     * The child thread relative thread synchronization operation,
     * blocking the child thread,
     * thread for the main thread to complete
     *
     * @param runnable Runnable Interface
     */
    public static void runOnMainThreadSync(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
            return;
        }
        SyncPost poster = new SyncPost(runnable);
        getMainPoster().sync(poster);
        poster.waitRun();
    }

    public static void dispose() {
        if (mainPoster != null) {
            mainPoster.dispose();
            mainPoster = null;
        }
    }
}
