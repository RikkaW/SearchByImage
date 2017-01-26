package rikka.searchbyimage.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.vending.billing.IabResult;
import com.android.vending.billing.Inventory;
import com.android.vending.billing.Purchase;

import rikka.searchbyimage.BuildConfig;
import rikka.searchbyimage.R;
import rikka.searchbyimage.support.Settings;
import rikka.searchbyimage.utils.IabHelperWrapper;
import rikka.searchbyimage.utils.PackageUtils;

public class DonateActivity extends BaseActivity {

    private View mButtonPlay;
    private View mButtonAlipay;

    private IabHelperWrapper mIabHelperWrapper;

    private final static String SKU_DONATE_1 = "donate_1";
    private final static String SKU_DONATE_2 = "donate_2";
    private final static String SKU_DONATE_5 = "donate_5";
    private final static String SKU_DONATE_10 = "donate_10";

    private final static String[] SKU_DONATE = {
            SKU_DONATE_1,
            SKU_DONATE_2,
            SKU_DONATE_5,
            SKU_DONATE_10,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mIabHelperWrapper = new IabHelperWrapper(this, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAo1u82whWExfU5LtocWOLQKxLW0BpQbgiQiySHrgQikLEOgpi/s4mbRuB6cB4jsLOCp/mzoVlb/NhTx3w3VaoCZr51EwpKq5+zqcf2s2ZdTtM7tbQxlfUKSRLxP+MwGq59nIMfDkXc7kDOwtBRQKLxCacT0IEX/tj1BkQcVjv40rKBPnHqdf4gL7wE7Ch+Z+AL9OiGTLVYDb1g8HUjdNyMShrcUgX5luP7HIeIKJ8nUMForqNTM7Hpr9JdXPmkb/InYCzoYXklA5CrfTUTMT0I+SqfxyPMLIFmURKysveKKBJk3mtIH4wYabmK+XuUVYJxVfZDhAqF4SLMqrBbOZ6OQIDAQAB", new IabHelperWrapper.OnQueryInventoryFinishedListener() {
            @Override
            public void onFinished(IabHelperWrapper iabHelperWrapper, IabResult result, Inventory inventory) {
                if (result.isFailure()) {
                    return;
                }

                iabHelperWrapper.consume(inventory.getPurchase(SKU_DONATE_1));
                iabHelperWrapper.consume(inventory.getPurchase(SKU_DONATE_2));
                iabHelperWrapper.consume(inventory.getPurchase(SKU_DONATE_5));
                iabHelperWrapper.consume(inventory.getPurchase(SKU_DONATE_10));
            }
        });

        mButtonPlay = findViewById(android.R.id.button1);
        mButtonAlipay = findViewById(android.R.id.button2);

        mButtonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BuildConfig.hideOtherEngine && !BuildConfig.DEBUG) {
                    Toast.makeText(v.getContext(), R.string.donate_play_required, Toast.LENGTH_SHORT).show();
                } else {
                    if (!mIabHelperWrapper.isSuccess()) {
                        Toast.makeText(v.getContext(), R.string.pay_gp_set_up_failed, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    showPlayDialog();
                }
            }
        });

        if (BuildConfig.hideOtherEngine/*!PackageUtils.isPackageInstalled(DonateActivity.this, "com.eg.android.AlipayGphone")*/) {
            mButtonAlipay.setVisibility(View.GONE);
        }

        mButtonAlipay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!PackageUtils.isPackageInstalled(v.getContext(), "com.eg.android.AlipayGphone")) {
                    Toast.makeText(v.getContext(), "您没有安装支付宝客户端。", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!PackageUtils.isPackageEnabled(v.getContext(), "com.eg.android.AlipayGphone")) {
                    Toast.makeText(v.getContext(), "您的支付宝客户端已被禁用。", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("alipayqr://platformapi/startapp?saId=10000007&qrcode=https%3A%2F%2Fqr.alipay.com%2Faex01083scje5axcttivf13")));

                    Settings.instance(v.getContext()).putBoolean(Settings.DONATED, true);
                    findViewById(android.R.id.text1).setVisibility(View.VISIBLE);
                } catch (Exception ignored) {
                }
            }
        });

        if (!Settings.instance(DonateActivity.this).getBoolean(Settings.DONATED, false) && !BuildConfig.DEBUG) {
            findViewById(android.R.id.text1).setVisibility(View.GONE);
        }
    }

    private void showPlayDialog() {
        new AlertDialog.Builder(this)
                .setItems(new CharSequence[]{"1 USD", "2 USD", "5 USD", "10 USD"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mIabHelperWrapper.purchase(DonateActivity.this, SKU_DONATE[which], new IabHelperWrapper.OnPurchaseSuccessListener() {
                            @Override
                            public void onSuccess(IabHelperWrapper iabHelperWrapper, Purchase purchase) {
                                iabHelperWrapper.consume(purchase);

                                Settings.instance(getApplicationContext()).putBoolean(Settings.DONATED, true);
                                Settings.instance(getApplicationContext()).putBoolean(Settings.HIDE_DONATE_REQUEST, true);

                                if (!isFinishing()) {
                                    findViewById(android.R.id.text1).setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mIabHelperWrapper.isSuccess()) {
            mIabHelperWrapper.onDestroy();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mIabHelperWrapper.isSuccess()
                && mIabHelperWrapper.onActivityResult(requestCode, resultCode, data)) {
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
