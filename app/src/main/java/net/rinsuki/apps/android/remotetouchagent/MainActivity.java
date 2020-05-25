package net.rinsuki.apps.android.remotetouchagent;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.InputEvent;
import android.os.Bundle;
import android.view.MotionEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.attribute.PosixFileAttributes;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String cmd = "CLASSPATH=" + this.getPackageCodePath() + " /system/bin/app_process /system/bin net.rinsuki.apps.android.remotetouchagent.Agent $0 $1";
        System.out.println(cmd);
        try {
            File file = new File("/sdcard/remotetouchagent");
            FileWriter writer = new FileWriter(file);
            writer.write("#!/system/bin/sh\n");
            writer.write(cmd);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
