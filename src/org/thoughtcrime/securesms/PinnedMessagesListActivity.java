package org.thoughtcrime.securesms;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.WindowCompat;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;

import org.thoughtcrime.securesms.color.MaterialColor;
import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.database.ThreadDatabase;
import org.thoughtcrime.securesms.mms.GlideApp;
import org.thoughtcrime.securesms.mms.GlideRequests;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.recipients.RecipientModifiedListener;
import org.thoughtcrime.securesms.util.DynamicLanguage;
import org.thoughtcrime.securesms.util.DynamicTheme;
import org.thoughtcrime.securesms.util.Util;
import org.thoughtcrime.securesms.util.ViewUtil;

public class PinnedMessagesListActivity extends PassphraseRequiredActionBarActivity
        implements ConversationFragment.ConversationFragmentListener, RecipientModifiedListener {

    private static final String TAG = ConversationActivity.class.getSimpleName();

    public static final String ADDRESS_EXTRA = "address";
    public static final String THREAD_ID_EXTRA = "thread_id";
    public static final String IS_ARCHIVED_EXTRA = "is_archived";
    public static final String DISTRIBUTION_TYPE_EXTRA = "distribution_type";

    protected ConversationTitleView titleView;
    private Recipient recipient;
    private long threadId;
    private int distributionType;
    private boolean archived;

    private final DynamicTheme dynamicTheme = new DynamicTheme();
    private final DynamicLanguage dynamicLanguage = new DynamicLanguage();
    private ConversationFragment fragment;

    @Override
    protected void onPreCreate() {
        dynamicTheme.onCreate(this);
        dynamicLanguage.onCreate(this);
    }

    @Override
    public void onCreate(Bundle state, @NonNull MasterSecret masterSecret) {
        Log.w(TAG, "onCreate()");

        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.pinned_messages_activity);

        TypedArray typedArray = obtainStyledAttributes(new int[]{R.attr.conversation_background});
        int color = typedArray.getColor(0, Color.WHITE);
        typedArray.recycle();

        getWindow().getDecorView().setBackgroundColor(color);

        ConversationFragment conversationFragment = new ConversationFragment();
        conversationFragment.onlyPinned = true;

        fragment = initFragment(R.id.fragment_content, conversationFragment,
                masterSecret, dynamicLanguage.getCurrentLocale());

        initializeActionBar();
        initializeViews();
        initializeResources();
    }

    protected void initializeActionBar() {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar == null) throw new AssertionError();

        supportActionBar.setDisplayHomeAsUpEnabled(false);
        supportActionBar.setCustomView(R.layout.conversation_title_view);
        supportActionBar.setDisplayShowCustomEnabled(true);
        supportActionBar.setDisplayShowTitleEnabled(false);
    }


    private void initializeViews() {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar == null) throw new AssertionError();

        titleView = (ConversationTitleView) supportActionBar.getCustomView();
        titleView.setOnBackClickedListener(view -> super.onBackPressed());
    }

    private void initializeResources() {
        if (recipient != null) recipient.removeListener(this);

        recipient = Recipient.from(this, getIntent().getParcelableExtra(ADDRESS_EXTRA), true);
        threadId = getIntent().getLongExtra(THREAD_ID_EXTRA, -1);
        archived = getIntent().getBooleanExtra(IS_ARCHIVED_EXTRA, false);
        distributionType = getIntent().getIntExtra(DISTRIBUTION_TYPE_EXTRA, ThreadDatabase.DistributionTypes.DEFAULT);
        GlideRequests glideRequests = GlideApp.with(this);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            LinearLayout conversationContainer = ViewUtil.findById(this, R.id.conversation_container);
            conversationContainer.setClipChildren(true);
            conversationContainer.setClipToPadding(true);
        }

        setActionBarColor(recipient.getColor());

        titleView.setCustomTitle(glideRequests, recipient,"Pinned Messages", "With " + recipient.getProfileName());

        recipient.addListener(this);
    }

    private void setActionBarColor(MaterialColor color) {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar == null) throw new AssertionError();
        supportActionBar.setBackgroundDrawable(new ColorDrawable(color.toActionBarColor(this)));
        setStatusBarColor(color.toStatusBarColor(this));
    }

    @Override
    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onModified(Recipient recipient) {
        Log.w(TAG, "onModified(" + recipient.getAddress().serialize() + ")");
        Util.runOnMain(() -> {
            Log.w(TAG, "onModifiedRun(): " + recipient.getRegistered());
            setActionBarColor(recipient.getColor());
            invalidateOptionsMenu();
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return true;
        }
    }
}
