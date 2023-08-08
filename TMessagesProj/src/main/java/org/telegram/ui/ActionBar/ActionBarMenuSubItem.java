package org.telegram.ui.ActionBar;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.Components.CheckBox2;
import org.telegram.ui.Components.Easings;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RLottieImageView;
import org.telegram.ui.Components.TextViewSwitcher;

public class ActionBarMenuSubItem extends FrameLayout {

    private TextView textView;
    private TextViewSwitcher subtextView;
    private RLottieImageView imageView;
    private CheckBox2 checkView;
    private ImageView rightIcon;

    private int textColor;
    private int iconColor;
    private int selectorColor;

    boolean top;
    boolean bottom;

    private int itemHeight = 48;
    private final Theme.ResourcesProvider resourcesProvider;
    Runnable openSwipeBackLayout;

    public ActionBarMenuSubItem(Context context, boolean top, boolean bottom) {
        this(context, false, top, bottom);
    }

    public ActionBarMenuSubItem(Context context, boolean needCheck, boolean top, boolean bottom) {
        this(context, needCheck ? 1 : 0, top, bottom, null);
    }

    public ActionBarMenuSubItem(Context context, boolean top, boolean bottom, Theme.ResourcesProvider resourcesProvider) {
        this(context, 0, top, bottom, resourcesProvider);
    }

    public ActionBarMenuSubItem(Context context, boolean needCheck, boolean top, boolean bottom, Theme.ResourcesProvider resourcesProvider) {
        this(context, needCheck ? 1 : 0, top, bottom, resourcesProvider);
    }

    public ActionBarMenuSubItem(Context context, int needCheck, boolean top, boolean bottom, Theme.ResourcesProvider resourcesProvider) {
        super(context);
        this.resourcesProvider = resourcesProvider;

        this.top = top;
        this.bottom = bottom;

        textColor = getThemedColor(Theme.key_actionBarDefaultSubmenuItem);
        iconColor = getThemedColor(Theme.key_actionBarDefaultSubmenuItemIcon);
        selectorColor = getThemedColor(Theme.key_dialogButtonSelector);

        updateBackground();
        setPadding(AndroidUtilities.dp(18), 0, AndroidUtilities.dp(18), 0);

        imageView = new RLottieImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        imageView.setColorFilter(new PorterDuffColorFilter(iconColor, PorterDuff.Mode.MULTIPLY));
        addView(imageView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, 40, Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT)));

        textView = new TextView(context);
        textView.setLines(1);
        textView.setSingleLine(true);
        textView.setGravity(Gravity.LEFT);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setTextColor(textColor);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        addView(textView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL));

        if (needCheck > 0) {
            checkView = new CheckBox2(context, 26, resourcesProvider);
            checkView.setDrawUnchecked(false);
            checkView.setColor(-1, -1, Theme.key_radioBackgroundChecked);
            checkView.setDrawBackgroundAsArc(-1);
            if (needCheck == 1) {
                addView(checkView, LayoutHelper.createFrame(26, LayoutHelper.MATCH_PARENT, Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT)));
            } else {
                addView(checkView, LayoutHelper.createFrame(26, LayoutHelper.MATCH_PARENT, Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT)));
                textView.setPadding(LocaleController.isRTL ? AndroidUtilities.dp(34) : 0, 0, LocaleController.isRTL ? 0 : AndroidUtilities.dp(34), 0);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(itemHeight), View.MeasureSpec.EXACTLY));
        if (expandIfMultiline && textView.getLayout().getLineCount() > 1) {
            super.onMeasure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(itemHeight + 8), View.MeasureSpec.EXACTLY));
        }
    }

    public void setItemHeight(int itemHeight) {
        this.itemHeight = itemHeight;
    }

    public void setChecked(boolean checked) {
        if (checkView == null) {
            return;
        }
        checkView.setChecked(checked, true);
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setEnabled(isEnabled());
        if (checkView != null && checkView.isChecked()) {
            info.setCheckable(true);
            info.setChecked(checkView.isChecked());
            info.setClassName("android.widget.CheckBox");
        }
    }

    public void setCheckColor(int colorKey) {
        checkView.setColor(-1, -1, colorKey);
    }

    public void setRightIcon(int icon) {
        setRightIcon(icon, null);
    }

    public void setRightIcon(int icon, OnClickListener listener) {
        if (rightIcon == null) {
            rightIcon = new ImageView(getContext());
            rightIcon.setScaleType(ImageView.ScaleType.CENTER);
            rightIcon.setColorFilter(iconColor, PorterDuff.Mode.MULTIPLY);
            if (LocaleController.isRTL) {
                rightIcon.setScaleX(-1);
            }
            addView(rightIcon, LayoutHelper.createFrame(24, LayoutHelper.MATCH_PARENT, Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT)));
        }
        setPadding(AndroidUtilities.dp(LocaleController.isRTL ? listener != null ? 0 : 8 : 18), 0, AndroidUtilities.dp(LocaleController.isRTL ? 18 : listener != null ? 0 : 8), 0);
        rightIcon.setImageResource(icon);
        if (listener != null) {
            rightIcon.getLayoutParams().width = AndroidUtilities.dp(40);
            rightIcon.setOnClickListener(listener);
            rightIcon.setBackground(Theme.createRadSelectorDrawable(selectorColor, 6, 0, 0, 6));
            int iconWidth = AndroidUtilities.dp(40);
            ((FrameLayout.LayoutParams) textView.getLayoutParams()).setMargins(LocaleController.isRTL ? iconWidth : 0, 0, LocaleController.isRTL ? 0 : iconWidth, 0);
        }
    }

    public void setTextAndIcon(CharSequence text, int icon) {
        setTextAndIcon(text, icon, null);
    }

    boolean expandIfMultiline;

    public void setMultiline() {
        setMultiline(true);
    }

    public void setMultiline(boolean changeSize) {
        textView.setLines(2);
        if (changeSize) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        } else {
            expandIfMultiline = true;
        }
        textView.setSingleLine(false);
        textView.setGravity(Gravity.CENTER_VERTICAL);
    }

    public void setTextAndIcon(CharSequence text, int icon, Drawable iconDrawable) {
        textView.setText(text);
        if (icon != 0 || iconDrawable != null || checkView != null) {
            if (iconDrawable != null) {
                imageView.setImageDrawable(iconDrawable);
            } else {
                imageView.setImageResource(icon);
            }
            imageView.setVisibility(VISIBLE);
            textView.setPadding(LocaleController.isRTL ? 0 : AndroidUtilities.dp(43), 0, LocaleController.isRTL ? AndroidUtilities.dp(43) : 0, 0);
        } else {
            imageView.setVisibility(INVISIBLE);
            textView.setPadding(0, 0, 0, 0);
        }
    }

    public ActionBarMenuSubItem setColors(int textColor, int iconColor) {
        setTextColor(textColor);
        setIconColor(iconColor);
        return this;
    }

    public void setTextColor(int textColor) {
        if (this.textColor != textColor) {
            textView.setTextColor(this.textColor = textColor);
        }
    }

    public void setIconColor(int iconColor) {
        if (this.iconColor != iconColor) {
            imageView.setColorFilter(new PorterDuffColorFilter(this.iconColor = iconColor, PorterDuff.Mode.MULTIPLY));
        }
    }

    public void setIcon(int resId) {
        imageView.setImageResource(resId);
    }

    public void setIcon(Drawable drawable) {
        imageView.setImageDrawable(drawable);
    }

    public void setAnimatedIcon(int resId) {
        imageView.setAnimation(resId, 24, 24);
    }

    public void onItemShown() {
        if (imageView.getAnimatedDrawable() != null) {
            imageView.getAnimatedDrawable().start();
        }
    }

    public void setText(CharSequence text) {
        textView.setText(text);
    }

    public void setSubtextColor(int color) {
        subtextView.setTextColor(color);
    }

    public void setSubtext(String text) {
        setSubtext(text, false);
    }

    public void setSubtext(String text, boolean animated) {
        if (subtextView == null) {
            subtextView = new TextViewSwitcher(getContext()) {
                @Override
                protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                    super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(100), MeasureSpec.AT_MOST));
                }
            };
            subtextView.setFactory(() -> {
                TextView view = new TextView(getContext());
                view.setLines(1);
                view.setSingleLine(true);
                view.setGravity(Gravity.LEFT);
                view.setEllipsize(TextUtils.TruncateAt.END);
                view.setTextColor(getThemedColor(Theme.key_groupcreate_sectionText));
                view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
                return view;
            });
            Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.alpha_in);
            anim.setInterpolator(Easings.easeInOutQuad);
            subtextView.setInAnimation(anim);
            anim = AnimationUtils.loadAnimation(getContext(), R.anim.alpha_out);
            anim.setInterpolator(Easings.easeInOutQuad);
            subtextView.setOutAnimation(anim);
            subtextView.setVisibility(GONE);
            if (imageView.getVisibility() == VISIBLE) subtextView.setPadding(LocaleController.isRTL ? 0 : AndroidUtilities.dp(43), 0, LocaleController.isRTL ? AndroidUtilities.dp(43) : 0, 0);
            addView(subtextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL, 0, 10, 0, 0));
        }
        boolean visible = !TextUtils.isEmpty(text);
        boolean oldVisible = subtextView.getVisibility() == VISIBLE;
        if (visible != oldVisible) {
            subtextView.setVisibility(visible ? VISIBLE : GONE);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) textView.getLayoutParams();
            layoutParams.bottomMargin = visible ? AndroidUtilities.dp(10) : 0;
            textView.setLayoutParams(layoutParams);
        }
        subtextView.setText(text, animated);
    }

    public TextView getTextView() {
        return textView;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setSelectorColor(int selectorColor) {
        if (this.selectorColor != selectorColor) {
            this.selectorColor = selectorColor;
            updateBackground();
        }
    }

    public void updateSelectorBackground(boolean top, boolean bottom) {
        if (this.top == top && this.bottom == bottom) {
            return;
        }
        this.top = top;
        this.bottom = bottom;
        updateBackground();
    }

    void updateBackground() {
        setBackground(Theme.createRadSelectorDrawable(selectorColor, top ? 6 : 0, bottom ? 6 : 0));
    }

    private int getThemedColor(int key) {
        return Theme.getColor(key, resourcesProvider);
    }

    public CheckBox2 getCheckView() {
        return checkView;
    }

    public void openSwipeBack() {
        if (openSwipeBackLayout != null) {
            openSwipeBackLayout.run();
        }
    }

    public ImageView getRightIcon() {
        return rightIcon;
    }
}
