package com.example.grocerylist3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    /* So when to use getContext() vs getTargetContext()?
The documentation doesn't do a great job of explaining the differences so here it is from my POV:
You know that when you do instrumentation tests on Android then you have two apps:
    The test app, that executes your test logic and tests your "real" app
    The "real" app (that your users will see)
So when you are writing your tests and you want to load a resource of your real app, use getTargetContext().
If you want to use a resource of your test app (e.g. a test input for one of your tests) then call getContext(). */
    Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

    @Before
    public void setUp() throws Exception {
        //Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @Test
    public void onCreate() {
        /* So when to use getContext() vs getTargetContext()?
The documentation doesn't do a great job of explaining the differences so here it is from my POV:
You know that when you do instrumentation tests on Android then you have two apps:
    The test app, that executes your test logic and tests your "real" app
    The "real" app (that your users will see)
So when you are writing your tests and you want to load a resource of your real app, use getTargetContext().
If you want to use a resource of your test app (e.g. a test input for one of your tests) then call getContext(). */
        //Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        //Context context = ApplicationProvider.getApplicationContext();

    }

    @Test
    public void addMarketToTable () {

    }
}
