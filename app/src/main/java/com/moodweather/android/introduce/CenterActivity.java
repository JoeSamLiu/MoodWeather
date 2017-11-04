package com.moodweather.android.introduce;

import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.moodweather.android.R;

/**
 * 居中位置移动的启动动画
 * 三种动画方式: 移动\渐变\缩放.
 * <p>
 */
public class CenterActivity extends AppCompatActivity {
    public static final int STARTUP_DELAY = 300; // 启动延迟
    public static final int ANIM_ITEM_DURATION = 1000;
    public static final int ITEM_DELAY = 300;
    private LinearLayout llContainer;
    private ImageView ivLogo;
    private Button bChoice2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_center);
        bChoice2 = (Button)findViewById(R.id.b_choice_2);
        llContainer = (LinearLayout)findViewById(R.id.ll_container);
        ivLogo = (ImageView)findViewById(R.id.iv_logo);

        // 向上移动
        ViewCompat.animate(ivLogo)
                .translationY(-300)
                .setStartDelay(STARTUP_DELAY) //启动延迟
                .setDuration(ANIM_ITEM_DURATION)//持续时间
                .setInterpolator(new DecelerateInterpolator(1.2f))
                .start();

        for (int i = 0; i < llContainer.getChildCount(); i++) {
            View v = llContainer.getChildAt(i);
            ViewPropertyAnimatorCompat viewAnimator;
            // TextView控件, Button是TextView的子类
            if (!(v instanceof Button)) {
                // 渐变动画，从消失到显示
                viewAnimator = ViewCompat.animate(v)
                        .translationY(50).alpha(1)
                        .setStartDelay((ITEM_DELAY * i) + 500)
                        .setDuration(ANIM_ITEM_DURATION);
            } else { // Button控件, 从缩小到扩大
                viewAnimator = ViewCompat.animate(v)
                        .scaleY(1).scaleX(1)
                        .setStartDelay((ITEM_DELAY * i) + 500)
                        .setDuration(ANIM_ITEM_DURATION / 2);
            }
            viewAnimator.setInterpolator(new DecelerateInterpolator()).start();
        }
        bChoice2.setOnClickListener(v -> onBackPressed());
    }
}
