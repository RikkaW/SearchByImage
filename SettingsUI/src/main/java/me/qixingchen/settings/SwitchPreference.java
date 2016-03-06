package me.qixingchen.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.preference.TwoStatePreference;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.CompoundButton;

/**
 * Created by Rikka on 2016/2/25.
 */
public class SwitchPreference extends TwoStatePreference {
    private final Listener mListener = new Listener();
    // Switch text for on and off states
    private CharSequence mSwitchOn;
    private CharSequence mSwitchOff;
    private class Listener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!callChangeListener(isChecked)) {
                // Listener didn't like it, change it back.
                // CompoundButton will make sure we don't recurse.
                buttonView.setChecked(!isChecked);
                return;
            }
            SwitchPreference.this.setChecked(isChecked);
        }
    }
    /**
     * Construct a new SwitchPreference with the given style options.
     *
     * @param context The Context that will style this preference
     * @param attrs Style attributes that differ from the default
     * @param defStyleAttr An attribute in the current theme that contains a
     *        reference to a style resource that supplies default values for
     *        the view. Can be 0 to not look for defaults.
     * @param defStyleRes A resource identifier of a style resource that
     *        supplies default values for the view, used only if
     *        defStyleAttr is 0 or can not be found in the theme. Can be 0
     *        to not look for defaults.
     */
    public SwitchPreference(Context context, AttributeSet attrs, int defStyleAttr,
                            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.SwitchPreference, defStyleAttr, defStyleRes);
        setSummaryOn(TypedArrayUtils.getString(a, R.styleable.SwitchPreference_summaryOn,
                R.styleable.SwitchPreference_android_summaryOn));
        setSummaryOff(TypedArrayUtils.getString(a, R.styleable.SwitchPreference_summaryOff,
                R.styleable.SwitchPreference_android_summaryOff));
        setSwitchTextOn(TypedArrayUtils.getString(a,
                R.styleable.SwitchPreference_switchTextOn,
                R.styleable.SwitchPreference_android_switchTextOn));
        setSwitchTextOff(TypedArrayUtils.getString(a,
                R.styleable.SwitchPreference_switchTextOff,
                R.styleable.SwitchPreference_android_switchTextOff));
        setDisableDependentsState(TypedArrayUtils.getBoolean(a,
                R.styleable.SwitchPreference_disableDependentsState,
                R.styleable.SwitchPreference_android_disableDependentsState, false));
        a.recycle();

        setWidgetLayoutResource(R.layout.preference_switch_material);
    }
    /**
     * Construct a new SwitchPreference with the given style options.
     *
     * @param context The Context that will style this preference
     * @param attrs Style attributes that differ from the default
     * @param defStyleAttr An attribute in the current theme that contains a
     *        reference to a style resource that supplies default values for
     *        the view. Can be 0 to not look for defaults.
     */
    public SwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }
    /**
     * Construct a new SwitchPreference with the given style options.
     *
     * @param context The Context that will style this preference
     * @param attrs Style attributes that differ from the default
     */
    public SwitchPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.switchPreferenceStyle);
    }
    /**
     * Construct a new SwitchPreference with default style options.
     *
     * @param context The Context that will style this preference
     */
    public SwitchPreference(Context context) {
        this(context, null);
    }
    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        final SwitchCompat switchView = (SwitchCompat) holder.findViewById(R.id.switchWidget);
        syncSwitchView(switchView);
        syncSummaryView(holder);
    }

    @Override
    protected void onClick() {
        super.onClick();
        mSetInPost = true;
    }

    /**
     * Set the text displayed on the switch widget in the on state.
     * This should be a very short string; one word if possible.
     *
     * @param onText Text to display in the on state
     */
    public void setSwitchTextOn(CharSequence onText) {
        mSwitchOn = onText;
        notifyChanged();
    }
    /**
     * Set the text displayed on the switch widget in the off state.
     * This should be a very short string; one word if possible.
     *
     * @param offText Text to display in the off state
     */
    public void setSwitchTextOff(CharSequence offText) {
        mSwitchOff = offText;
        notifyChanged();
    }
    /**
     * Set the text displayed on the switch widget in the on state.
     * This should be a very short string; one word if possible.
     *
     * @param resId The text as a string resource ID
     */
    public void setSwitchTextOn(int resId) {
        setSwitchTextOn(getContext().getString(resId));
    }
    /**
     * Set the text displayed on the switch widget in the off state.
     * This should be a very short string; one word if possible.
     *
     * @param resId The text as a string resource ID
     */
    public void setSwitchTextOff(int resId) {
        setSwitchTextOff(getContext().getString(resId));
    }
    /**
     * @return The text that will be displayed on the switch widget in the on state
     */
    public CharSequence getSwitchTextOn() {
        return mSwitchOn;
    }
    /**
     * @return The text that will be displayed on the switch widget in the off state
     */
    public CharSequence getSwitchTextOff() {
        return mSwitchOff;
    }
    /**
     * @hide
     */
    private boolean mSetInPost;

    @Override
    protected void performClick(View view) {
        super.performClick(view);
        syncViewIfAccessibilityEnabled(view);
    }
    private void syncViewIfAccessibilityEnabled(View view) {
        AccessibilityManager accessibilityManager = (AccessibilityManager)
                getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (!accessibilityManager.isEnabled()) {
            return;
        }
        View switchView = view.findViewById(R.id.switchWidget);
        syncSwitchView(switchView);
        View summaryView = view.findViewById(android.R.id.summary);
        syncSummaryView(summaryView);
    }
    private void syncSwitchView(final View view) {
        if (view instanceof SwitchCompat) {
            final SwitchCompat switchView = (SwitchCompat) view;
            switchView.setOnCheckedChangeListener(null);
        }
        if (view instanceof SwitchCompat) {
            if (!mSetInPost) {
                ((SwitchCompat) view).setChecked(mChecked);
                mSetInPost = true;
            } else {
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        ((SwitchCompat) view).setChecked(mChecked);
                        mSetInPost = false;
                    }
                });
            }
        }
        if (view instanceof SwitchCompat) {
            final SwitchCompat switchView = (SwitchCompat) view;
            switchView.setTextOn(mSwitchOn);
            switchView.setTextOff(mSwitchOff);
            switchView.setOnCheckedChangeListener(mListener);
        }
    }


}
