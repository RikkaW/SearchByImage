package rikka.searchbyimage.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.android.vending.billing.IabBroadcastReceiver;
import com.android.vending.billing.IabHelper;
import com.android.vending.billing.IabResult;
import com.android.vending.billing.Inventory;
import com.android.vending.billing.Purchase;

import rikka.searchbyimage.BuildConfig;

/**
 * Created by Rikka on 2017/1/23.
 */

public class IabHelperWrapper implements IabBroadcastReceiver.IabBroadcastListener {

    public interface OnQueryInventoryFinishedListener {
        void onFinished(IabHelperWrapper iabHelperWrapper, IabResult result, Inventory inventory);
    }

    private OnQueryInventoryFinishedListener mOnQueryInventoryFinishedListener;

    public interface OnPurchaseSuccessListener {
        void onSuccess(IabHelperWrapper iabHelperWrapper, Purchase purchase);
    }

    private static final String TAG = "IabHelperWrapper";

    private static final int RC_REQUEST = 10001;

    private Context mContext;

    // The helper object
    private IabHelper mHelper;

    // Provides purchase notification while this app is running
    private IabBroadcastReceiver mBroadcastReceiver;

    private boolean isSuccess;

    public IabHelperWrapper(Context context, String base64EncodedPublicKey, final OnQueryInventoryFinishedListener onQueryInventoryFinishedListener) {
        mContext = context;

        mOnQueryInventoryFinishedListener = onQueryInventoryFinishedListener;

        // Create the helper, passing it our context and the public key to verify signatures with
        Log.d(TAG, "Creating IAB helper.");
        mHelper = new IabHelper(context, base64EncodedPublicKey);

        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(BuildConfig.DEBUG);

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        Log.d(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    complain("Problem setting up in-app billing: " + result);
                    isSuccess = false;
                    return;
                }

                isSuccess = true;

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;

                // Important: Dynamically register for broadcast messages about updated purchases.
                // We register the receiver here instead of as a <receiver> in the Manifest
                // because we always call getPurchases() at startup, so therefore we can ignore
                // any broadcasts sent while the app isn't running.
                // Note: registering this listener in an Activity is a bad idea, but is done here
                // because this is a SAMPLE. Regardless, the receiver must be registered after
                // IabHelper is setup, but before first call to getPurchases().
                mBroadcastReceiver = new IabBroadcastReceiver(IabHelperWrapper.this);
                IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
                mContext.registerReceiver(mBroadcastReceiver, broadcastFilter);

                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                Log.d(TAG, "Setup successful. Querying inventory.");
                try {
                    mHelper.queryInventoryAsync(new QueryInventoryFinishedCallback(mOnQueryInventoryFinishedListener));
                } catch (IabHelper.IabAsyncInProgressException e) {
                    complain("Error querying inventory. Another async operation in progress.");
                }
            }
        });
    }

    // Listener that's called when we finish querying the items and subscriptions we own
    private class QueryInventoryFinishedCallback implements IabHelper.QueryInventoryFinishedListener {

        private OnQueryInventoryFinishedListener mOnQueryInventoryFinishedListener;

        public QueryInventoryFinishedCallback(OnQueryInventoryFinishedListener onQueryInventoryFinishedListener) {
            mOnQueryInventoryFinishedListener = onQueryInventoryFinishedListener;
        }

        @Override
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                complain("Failed to query inventory: " + result);
                return;
            }

            if (mOnQueryInventoryFinishedListener != null) {
                mOnQueryInventoryFinishedListener.onFinished(IabHelperWrapper.this, result, inventory);
            }

            Log.d(TAG, "Query inventory was successful.");
        }
    }

    private class PurchaseFinishedCallback implements IabHelper.OnIabPurchaseFinishedListener {

        private OnPurchaseSuccessListener mOnPurchaseSuccessListener;

        PurchaseFinishedCallback(OnPurchaseSuccessListener onPurchaseSuccessListener) {
            mOnPurchaseSuccessListener = onPurchaseSuccessListener;
        }

        @Override
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                complain("Error purchasing: " + result);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                complain("Error purchasing. Authenticity verification failed.");
                return;
            }

            Log.d(TAG, "Purchase successful.");

            if (mOnPurchaseSuccessListener != null) {
                mOnPurchaseSuccessListener.onSuccess(IabHelperWrapper.this, purchase);
            }
        }
    }

    public void consume(Purchase purchase) {
        if (purchase != null && verifyDeveloperPayload(purchase)) {
            Log.d(TAG, "Consuming " + purchase.getSignature());
            try {
                mHelper.consumeAsync(purchase, mConsumeFinishedListener);
            } catch (IabHelper.IabAsyncInProgressException e) {
                complain("Error consuming. Another async operation in progress.");
            }
        }
    }

    // Called when consumption is complete
    private IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            // We know this is the "gas" sku because it's the only one we consume,
            // so we don't check which sku was consumed. If you have more than one
            // sku, you probably should check...
            if (result.isSuccess()) {
                // successfully consumed, so we apply the effects of the item in our
                // game world's logic, which in our case means filling the gas tank a bit
                Log.d(TAG, "Consumption successful. Provisioning.");
            } else {
                complain("Error while consuming: " + result);
            }
            Log.d(TAG, "End consumption flow.");
        }
    };


    private boolean verifyDeveloperPayload(Purchase purchase) {
        return true;
    }

    @Override
    public void receivedBroadcast() {
        // Received a broadcast notification that the inventory of items has changed
        Log.d(TAG, "Received broadcast notification. Querying inventory.");
        try {
            mHelper.queryInventoryAsync(new QueryInventoryFinishedCallback(mOnQueryInventoryFinishedListener));
        } catch (IabHelper.IabAsyncInProgressException e) {
            complain("Error querying inventory. Another async operation in progress.");
        }
    }

    private void complain(String message) {
        Log.e(TAG, "**** Error: " + message);
        alert("Error: " + message);
    }

    private void alert(String message) {
        /*AlertDialog.Builder bld = new AlertDialog.Builder(mContext);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        Log.d(TAG, "Showing alert dialog: " + message);
        bld.create().show();*/
        if (BuildConfig.DEBUG) {
            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        }
    }

    public void purchase(Activity activity, String sku, @Nullable OnPurchaseSuccessListener listener) {
        Log.d(TAG, "Launching purchase " + sku);

        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
        String payload = "";

        try {
            mHelper.launchPurchaseFlow(activity, sku, RC_REQUEST,
                    new PurchaseFinishedCallback(listener), payload);
        } catch (IabHelper.IabAsyncInProgressException e) {
            complain("Error launching purchase flow. Another async operation in progress.");
        }
    }

    public void onDestroy() {
        // very important:
        if (mBroadcastReceiver != null) {
            mContext.unregisterReceiver(mBroadcastReceiver);
        }

        // very important:
        Log.d(TAG, "Destroying helper.");
        if (mHelper != null) {
            try {
                mHelper.disposeWhenFinished();
            } catch (IllegalArgumentException ignored) {

            }
            mHelper = null;
        }
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (mHelper == null) return true;

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            //super.onActivityResult(requestCode, resultCode, data);
            return false;
        } else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
        return true;
    }

    public boolean isSuccess() {
        return isSuccess;
    }
}
