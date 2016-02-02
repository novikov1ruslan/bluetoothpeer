package org.commons.logger;

import android.util.Log;
import android.util.SparseArray;

import org.apache.commons.lang3.time.FastDateFormat;

import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation logs to android.util.Log
 */
public class Print {
    /**
     * Caches file/line scopes for fast logging
     */
    private static final Map<String, SparseArray<String>> SCOPES = new ConcurrentHashMap<>();

    private static final ThreadLocal<String> threadName = new ThreadLocal<>();

    private final FastDateFormat dateFormat;

    private final Calendar calendar;

    private final WarningListener mWarningListener;

    public Print(WarningListener warningListener) {
        mWarningListener = warningListener;
        dateFormat = FastDateFormat.getInstance("HH:mm:ss.SSS");
        calendar = Calendar.getInstance();
    }

    public int println(int priority, String msg) {
        int ret = Log.println(priority, getScope(6), getThreadInfo() + msg);
        if (mWarningListener != null && priority >= Log.WARN) {
            mWarningListener.onWaring(msg, priority);
        }
        return ret;
    }

    public void push() {

    }

    protected String createFormattedMessage(int priority, String tag, String msg, String threadInfo) {
        calendar.setTimeInMillis(System.currentTimeMillis());
        String time = dateFormat.format(calendar);
        char level = LoggerImpl.logLevelToChar(priority);
        String pid = Integer.toString(android.os.Process.myPid());
        // 7 extra chars: level + ' ' + '\n' + '\t'. Note, that threadInfo includes trailing space
//			int capacity = time.length() + tag.length() + msg.length() + 7 + pid.length() + threadInfo.length();

        // format logs like logcat
        return time + ' ' + level + ' ' + pid + ' ' + threadInfo + tag + '\t' + msg + '\n';
    }

    protected String getThreadInfo() {
        // msg = String.format("[%s(%s)] %s", Thread.currentThread().getName(), Thread.currentThread().getId(),
        // msg);
        // msg = "[" + Thread.currentThread().getName() + "(" + Thread.currentThread().getId() + ")" +
        // Process.getThreadPriority(Process.myTid()) + "] " + msg;

        // StringBuilder stringBuilder = sb.get();
        // if (stringBuilder == null) {
        // stringBuilder = new StringBuilder(100);
        // sb.set(stringBuilder);
        // }

        String name = threadName.get();
        if (name == null) {
            StringBuilder stringBuilder = new StringBuilder(32);
            // stringBuilder.setLength(0);
            Thread curThread = Thread.currentThread();
            stringBuilder.append('[').append(curThread.getName()).append('(').append(curThread.getId()).append(")] ");
            name = stringBuilder.toString();
            threadName.set(name);
        }

        return name;
    }

    protected static String getScope(int skipDepth) {
        final StackTraceElement trace = Thread.currentThread().getStackTrace()[skipDepth];

        String fileName = trace.getFileName();
        SparseArray<String> fileScope = SCOPES.get(fileName);
        if (fileScope == null) {
            fileScope = new SparseArray<String>();
            SCOPES.put(fileName, fileScope);
        }

        int lineNumber = trace.getLineNumber();
        String scope = fileScope.get(lineNumber);
        if (scope == null) {
            scope = fileName.substring(0, fileName.indexOf('.')) + ":" + lineNumber + ":" + trace.getMethodName();
            fileScope.put(lineNumber, scope);
        }

        return scope;
    }
}
