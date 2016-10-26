package com.shizhefei.indicator;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.shizhefei.indicator.demo.R;
import com.shizhefei.view.indicator.BannerComponent;
import com.shizhefei.view.indicator.Indicator;
import com.shizhefei.view.indicator.IndicatorViewPager;
import com.shizhefei.view.indicator.slidebar.ColorBar;
import com.shizhefei.view.indicator.slidebar.ScrollBar;

public class BannerActivity extends Activity {

    private BannerComponent bannerComponent;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_banner);
        // TODO: 2016/10/26 先要明白,这里是两个控件,一个是viewpager ,底部白线是指示器 FixedIndicatorView
        //ViewPager,Indicator
        ViewPager viewPager = (ViewPager) findViewById(R.id.banner_viewPager);
        viewPager.setOffscreenPageLimit(2);

        Indicator indicator = (Indicator) findViewById(R.id.banner_indicator);  // TODO: FixedIndicatorView implements Indicator
        // TODO:  设置滑动块
        indicator.setScrollBar(new ColorBar(getApplicationContext(), Color.WHITE, 0, ScrollBar.Gravity.CENTENT_BACKGROUND));

// TODO: 2016/10/26 绑定两个控件,要有两个view对象,并通过handler设置滚动
        // TODO: 2016/10/26 需要 绑定数据
        //关键是两个view的互动,
        bannerComponent = new BannerComponent(indicator, viewPager, false);//绑定两个view对象,itemClickable
        bannerComponent.setAdapter(adapter);  //设置adapter

        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                images = new int[]{};
                adapter.notifyDataSetChanged();
            }
        });

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                images = new int[]{R.drawable.p2};
                adapter.notifyDataSetChanged();
            }
        });

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                images = new int[]{R.drawable.p1, R.drawable.p2, R.drawable.p3, R.drawable.p4};
                adapter.notifyDataSetChanged();
            }
        });
        //默认就是800毫秒，设置单页滑动效果的时间
//        bannerComponent.setScrollDuration(800);
        //设置播放间隔时间，默认情况是3000毫秒
        bannerComponent.setAutoPlayTime(2500);
    }

    @Override
    protected void onStart() {
        super.onStart();
        bannerComponent.startAutoPlay();
    }

    @Override
    protected void onStop() {
        super.onStop();
        bannerComponent.stopAutoPlay();
    }

    private int[] images = {R.drawable.p1, R.drawable.p2, R.drawable.p3, R.drawable.p4};

    private IndicatorViewPager.IndicatorViewPagerAdapter adapter = new IndicatorViewPager.IndicatorViewPagerAdapter() {
        // TODO: 2016/10/26 adapter的补充
        @Override
        public View getViewForTab(int position, View convertView, ViewGroup container) {
            //todo 不知道什么作用 去掉会报非法异常
            if (convertView == null) {
                convertView = new View(container.getContext());
            }
            return convertView;
        }

        @Override
        public View getViewForPage(int position, View convertView, ViewGroup container) {
            // TODO: 2016/10/26 添加图片
            if (convertView == null) {
                convertView = new ImageView(getApplicationContext());
                convertView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }
            ImageView imageView = (ImageView) convertView;
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageResource(images[position]);
            return convertView;
        }

        @Override
        public int getCount() {
            return images.length;
        }
    };
}
