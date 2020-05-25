package net.rinsuki.apps.android.remotetouchagent;

import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.MotionEvent;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class Agent {
    public static void main(String[] args) {
        // なんかこうしないと動かなかったんですよね、なんで?
        KtAgent.main(args);
    }
}
