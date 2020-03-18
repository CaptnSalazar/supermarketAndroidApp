package com.example.grocerylist3;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.AllOf.allOf;

/* https://developer.android.com/training/testing/espresso/basics
The Espresso API encourages test authors to think in terms of what a user might do while
interacting with the application - locating UI elements and interacting with them. At the same time,
the framework prevents direct access to activities and views of the application because holding on
to these objects and operating on them off the UI thread is a major source of test flakiness. Thus,
you will not see methods like getView() and getCurrentActivity() in the Espresso API. You can still
safely operate on views by implementing your own subclasses of ViewAction and ViewAssertion. */

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    /* So when to use getContext() vs getTargetContext()?
The documentation doesn't do a great job of explaining the differences so here it is from my POV:
You know that when you do instrumentation tests on Android then you have two apps:
    The test app, that executes your test logic and tests your "real" app
    The "real" app (that your users will see)
So when you are writing your tests and you want to load a resource of your real app, use getTargetContext().
If you want to use a resource of your test app (e.g. a test input for one of your tests) then call getContext().
    Context context = InstrumentationRegistry.getInstrumentation().getTargetContext(); */

    SQLiteDatabase database;

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setUp() throws Exception {
        /*Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Context context = ApplicationProvider.getApplicationContext();  */
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        //clear grocerylist3.db
        GroceryDBHelper dbHelper = new GroceryDBHelper(appContext);
        database = dbHelper.getWritableDatabase(); //Create and/or open a testDatabase that will be used for reading and writing.
        database.execSQL("DELETE FROM " + GroceryContract.GroceryEntry.TABLE_NAME);
        database.execSQL("DELETE FROM " + GroceryContract.SupermarketsVisited.TABLE_NAME_MARKET);
    }


    @Test
    public void addingItemTest() {
        onView(withId(R.id.editTextNewItem));   //Look for a property that helps to find the editTextNewItem. The editText in MainActivity has a unique R.id, as expected.
        onView(withId(R.id.editTextNewItem)).perform(typeText("Orange"));
        onView(withId(R.id.buttonAddItem));     //Look for a property that helps to find the buttonAddItem. The button in the MainActivity has a unique R.id, as expected.
        onView(withId(R.id.buttonAddItem)).perform(click());   //Perform the click
        onView(withId(R.id.editTextNewItem)).check(matches(withText(""))); //editTextNewItem should be cleared when we press "Add"
    }


    @Test
    public void addingMarketTest() {
        /* I think this might not be right but I kept it just in case:
        https://stackoverflow.com/questions/28431647/matchesnotisdisplayed-fails-with-nomatchingviewexception
        If the view is there in the view hierarchy but in an invisible state (visibility is set
        to 'INVISIBLE'), use not(isDisplayed). However, if the view is not there at all in the view
        hierarchy (e.g. visibility set to 'GONE'), doesNotExist() is used.*/

        checkViewsDisplayedWhenNotEditingMarket();

        onView(withId(R.id.buttonEditSpinner));
        onView(withId(R.id.buttonEditSpinner)).perform(click());
        onView(withId(R.id.editTextMarketName));
        onView(withId(R.id.editTextMarketName)).perform(typeText("Aaa Loja"));
        onView(withId(R.id.editTextMarketLocation));
        onView(withId(R.id.editTextMarketLocation)).perform(typeText("Brazil"));
        onView(withId(R.id.buttonConfirm));
        onView(withId(R.id.buttonConfirm)).perform(click());
        // PROBLEM I HAD: WHEN YOU PRESS CONFIRM AND THE SUPERMARKET IS ALREADY THERE, THE VIEW DOESN'T GO BACK.

        //onData(instanceOf(Market.class)).atPosition(0).perform(click());  //this works
        onData(instanceOf(Market.class)).atPosition(0).check(matches(withText("Aaa Loja (Brazil)"))); //this works
        checkViewsDisplayedWhenNotEditingMarket();

        onView(withId(R.id.buttonEditSpinner));
        onView(withId(R.id.buttonEditSpinner)).perform(click());
        onView(withId(R.id.editTextMarketName));
        onView(withId(R.id.editTextMarketName)).perform(typeText("Aaa Loja"));
        onView(withId(R.id.editTextMarketLocation));
        onView(withId(R.id.editTextMarketLocation)).perform(typeText("Brazil"));
        onView(withId(R.id.buttonConfirm));
        onView(withId(R.id.buttonConfirm)).perform(click());

        // https://stackoverflow.com/questions/37184933/espresso-ondata-error-performing-load-adapter-data-on-view
        onData(instanceOf(Market.class)).atPosition(0).check(matches(withText("Aaa Loja (Brazil)"))); //this works
        //because the market already existed in spinner, pressing confirm will make app stay in "market editing mode".
        checkViewsDisplayedWhileEditingMarket();
    }


    //This test sometimes doesn't work for some reason, especially if you run all the tests at once.
    @Test
    public void tickingItemInList() {
        DatabaseTestHelper.populateTableGroceryList(database);
//        ViewInteraction appCompatEditText = onView(
//                Matchers.allOf(withId(R.id.editTextNewItem),
//                        childAtPosition(
//                                Matchers.allOf(withId(R.id.rootLayout),
//                                        childAtPosition(
//                                                withId(android.R.id.content),
//                                                0)),
//                                0),
//                        isDisplayed()));
//        appCompatEditText.perform(click());
//
//        ViewInteraction appCompatEditText2 = onView(
//                Matchers.allOf(withId(R.id.editTextNewItem),
//                        childAtPosition(
//                                Matchers.allOf(withId(R.id.rootLayout),
//                                        childAtPosition(
//                                                withId(android.R.id.content),
//                                                0)),
//                                0),
//                        isDisplayed()));
//        appCompatEditText2.perform(replaceText("jui"), closeSoftKeyboard());
//
//        ViewInteraction appCompatButton = onView(
//                Matchers.allOf(withId(R.id.buttonAddItem), withText("Add"),
//                        childAtPosition(
//                                Matchers.allOf(withId(R.id.rootLayout),
//                                        childAtPosition(
//                                                withId(android.R.id.content),
//                                                0)),
//                                1),
//                        isDisplayed()));
//        appCompatButton.perform(click());
//
//        ViewInteraction editText = onView(
//                Matchers.allOf(withId(R.id.edittext_product_name), withText("Jui"),
//                        childAtPosition(
//                                childAtPosition(
//                                        withId(R.id.recyclerview),
//                                        3), //juice is the fourth item out of five (indexed 0-4)
//                                1),
//                        isDisplayed()));
//        editText.check(matches(withText("Jui")));
        ViewInteraction editTextFirstItem = onView(
                Matchers.allOf(withId(R.id.textViewProductName), withText(DatabaseTestHelper.firstItem),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.recyclerview),
                                        0),
                                1),
                        isDisplayed()));
        editTextFirstItem.check(matches(withText(DatabaseTestHelper.firstItem)));

        ViewInteraction editTextSecondItem = onView(
                Matchers.allOf(withId(R.id.textViewProductName), withText(DatabaseTestHelper.secondItem),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.recyclerview),
                                        1),
                                1),
                        isDisplayed()));
        editTextSecondItem.check(matches(withText(DatabaseTestHelper.secondItem)));

        //I think trying to get the third item doesn't work because the app has the softkeyboard open.
        closeKeyboardByAddingPreExistingItemThatYouWannaCheck(DatabaseTestHelper.thirdItem);
        ViewInteraction editTextThirdItem = onView(
                Matchers.allOf(withId(R.id.textViewProductName), withText(DatabaseTestHelper.thirdItem),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.recyclerview),
                                        2),
                                1),
                        isDisplayed()));
        editTextThirdItem.check(matches(withText(DatabaseTestHelper.thirdItem)));

        ViewInteraction checkBoxThirdItem = onView(
                Matchers.allOf(withId(R.id.checkBox),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.recyclerview),
                                        2),
                                2),
                        isDisplayed()));
        checkBoxThirdItem.check(matches(isNotChecked()));
        checkBoxThirdItem.perform(click());
        checkBoxThirdItem.check(matches(isChecked()));

        // Maybe check what happens when you select a different market?
    }


    // THIS TEST SOMETIMES PASSES AND SOMETIMES FAILS
    @Test
    public void spinnerTest() {
        DatabaseTestHelper.populateTableGroceryList(database);
        DatabaseTestHelper.populateTableSupermarkets(database);

        String firstSelectedMarket = DatabaseTestHelper.newMarketName3 + " (" + DatabaseTestHelper.newMarketLocation3 + ")";
        onView(withId(R.id.spinner)).check(matches(withSpinnerText(containsString(firstSelectedMarket))));

        String newSelectedMarket = DatabaseTestHelper.newMarketName1 + " (" + DatabaseTestHelper.newMarketLocation1 + ")";
        onView(withId(R.id.spinner)).perform(click());
        onData(instanceOf(Market.class)).atPosition(0).perform(click());

        onView(withId(R.id.spinner)).check(matches(withSpinnerText(containsString(newSelectedMarket))));
    }


    private void closeKeyboardByAddingPreExistingItemThatYouWannaCheck(String itemName) {
        ViewInteraction appCompatEditText = onView(
                Matchers.allOf(withId(R.id.editTextNewItem),
                        childAtPosition(Matchers.allOf(withId(R.id.rootLayout), childAtPosition(withId(android.R.id.content),0)),0),
                        isDisplayed()));
        appCompatEditText.perform(click());

        ViewInteraction appCompatEditText2 = onView(
                Matchers.allOf(withId(R.id.editTextNewItem),
                        childAtPosition(
                                Matchers.allOf(withId(R.id.rootLayout),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                0),
                        isDisplayed()));
        appCompatEditText2.perform(replaceText(itemName), closeSoftKeyboard());

        ViewInteraction appCompatButton = onView(
                Matchers.allOf(withId(R.id.buttonAddItem), withText("Add"),
                        childAtPosition(
                                Matchers.allOf(withId(R.id.rootLayout),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatButton.perform(click());
    }


    private void checkViewsDisplayedWhenNotEditingMarket() {
        onView(withId(R.id.editTextMarketName)).check(matches(not(isDisplayed())));
        onView(withId(R.id.editTextMarketLocation)).check(matches(not(isDisplayed())));
        onView(withId(R.id.buttonConfirm)).check(matches(not(isDisplayed())));
        onView(withId(R.id.buttonCancel)).check(matches(not(isDisplayed())));

        onView(withId(R.id.buttonEditSpinner)).check(matches(isDisplayed()));

        onView(withId(R.id.editTextNewItem)).check(matches(isDisplayed()));
        onView(withId(R.id.buttonAddItem)).check(matches(isDisplayed()));
        onView(withId(R.id.toggleButtonDeleteItem)).check(matches(isDisplayed()));
        onView(withId(R.id.toggleButtonEditSave)).check(matches(isDisplayed()));
        onView(withId(R.id.recyclerview)).check(matches(isDisplayed()));
    }


    private void checkViewsDisplayedWhileEditingMarket() {
        onView(withId(R.id.editTextMarketName)).check(matches(isDisplayed()));
        onView(withId(R.id.editTextMarketLocation)).check(matches(isDisplayed()));
        onView(withId(R.id.buttonConfirm)).check(matches(isDisplayed()));
        onView(withId(R.id.buttonCancel)).check(matches(isDisplayed()));

        onView(withId(R.id.buttonEditSpinner)).check(matches(isDisplayed()));

        onView(withId(R.id.editTextNewItem)).check(matches(not(isDisplayed())));
        onView(withId(R.id.buttonAddItem)).check(matches(not(isDisplayed())));
        onView(withId(R.id.toggleButtonDeleteItem)).check(matches(not(isDisplayed())));
        onView(withId(R.id.toggleButtonEditSave)).check(matches(not(isDisplayed())));
        onView(withId(R.id.recyclerview)).check(matches(not(isDisplayed())));
    }


    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
