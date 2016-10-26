package com.shizhefei.view.indicator;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;

import com.shizhefei.view.viewpager.DurationScroller;
import com.shizhefei.view.viewpager.SViewPager;

import java.lang.reflect.Field;

/**todo
 *
 * IndicatorViewPager的扩展版
 * 组合viewpager和指示器,少不了handler.post 与 viewpager.setCurrentItem() 控制器就是handler的 handleMessage
 * 方法在自定义的handler里 AutoPlayHandler
 * Created by LuckyJayce on 2016/7/30.
 */
public class BannerComponent extends IndicatorViewPager {

    private final Handler handler;
    private long time = 3000;
    private DurationScroller scroller;

    public BannerComponent(Indicator indicator, ViewPager viewPager, boolean indicatorClickable) {
        super(indicator, viewPager, indicatorClickable);
        // TODO: 2016/10/26 设置handler  viewPager.setCurrentItem(current + 1) 发送消息
        handler = new AutoPlayHandler(Looper.getMainLooper());
        // TODO: 2016/10/26 设置触摸监听
        viewPager.setOnTouchListener(onTouchListener);
        //设置scroller
        initViewPagerScroll();
    }


    private void initViewPagerScroll() {
        // TODO: 2016/10/26 重要 反射获取类的参数并修改某一对象的参数值
        try {
            Field mScroller = ViewPager.class.getDeclaredField("mScroller"); //viewpager中的参数mmScroller
            mScroller.setAccessible(true);  //无障碍
            scroller = new DurationScroller(
                    viewPager.getContext()); //scroller的子类
            mScroller.set(viewPager, scroller); //把这个viewpager的scroller设置成我想要的
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void setScrollDuration(int scrollDuration) {
        if (scroller != null) {
            scroller.setScrollDuration(scrollDuration);
        }
    }

    @Override
    protected void iniOnItemSelectedListener() {
        indicatorView.setOnItemSelectListener(new Indicator.OnItemSelectedListener() {

            @Override
            public void onItemSelected(View selectItemView, int select, int preSelect) {
                if (viewPager instanceof SViewPager) {
                    setCurrentItem(select, ((SViewPager) viewPager).isCanScroll());
                } else {
                    setCurrentItem(select, true);
                }
            }
        });
    }

    @Override
    protected void iniOnPageChangeListener() {
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                indicatorView.setCurrentItem(mAdapter.getRealPosition(position), true);
                if (onIndicatorPageChangeListener != null) {
                    onIndicatorPageChangeListener.onIndicatorPageChange(indicatorView.getPreSelectItem(), mAdapter.getRealPosition(position));
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                indicatorView.onPageScrolled(mAdapter.getRealPosition(position), positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                indicatorView.onPageScrollStateChanged(state);
            }
        });
    }

    @Override
    public void setCurrentItem(int item, boolean anim) {
        // TODO: 2016/10/26 指示器的跳动
        int count = mAdapter.getCount();
        if (count > 0) {
            int current = viewPager.getCurrentItem();
            int pp = mAdapter.getRealPosition(current);
            int add;
            if (item > pp) {
                add = ((item - pp) % count);
            } else {
                add = -((pp - item) % count);
            }
            if (Math.abs(add) > viewPager.getOffscreenPageLimit() && viewPager.getOffscreenPageLimit() != count) {
                viewPager.setOffscreenPageLimit(count);
            }
            viewPager.setCurrentItem(current + add, anim);
            indicatorView.setCurrentItem(item, anim);
        }
    }

    private LoopAdapter mAdapter;

    @Override
    public void setAdapter(IndicatorPagerAdapter adapter) {
        if (!(adapter instanceof LoopAdapter)) {
            throw new RuntimeException("请设置继承于IndicatorViewPagerAdapter或者IndicatorViewPagerAdapter的adapter");
        }
        this.mAdapter = (LoopAdapter) adapter;
        mAdapter.setLoop(true);
        super.setAdapter(adapter);
        int count = mAdapter.getCount();
        int center = Integer.MAX_VALUE / 2;
        if (count > 0) {
            center = center - center % count; // TODO: 2016/10/26 这个值计算的不知道有什么用处,只能说是个初始值吧,没什么要紧的
        }
        viewPager.setCurrentItem(center, false);
    }


    public void setAutoPlayTime(long time) {
        this.time = time;
    }

    private boolean isRunning;

    public void startAutoPlay() {
        isRunning = true;
        handler.removeCallbacksAndMessages(null);
        handler.sendEmptyMessageDelayed(1, time);
    }

    public void stopAutoPlay() {
        isRunning = false;
        handler.removeCallbacksAndMessages(null);
    }

    private class AutoPlayHandler extends Handler {

        public AutoPlayHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // TODO: 2016/10/26 viewpager的滚动
            int current = viewPager.getCurrentItem();
            viewPager.setCurrentItem(current + 1, true);
            if (isRunning) {
                handler.sendEmptyMessageDelayed(1, time);
            }
        }
    }

    // TODO: 2016/10/26 控制滑动 down时取消handler,up时开始handler
    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    handler.removeCallbacksAndMessages(null);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (isRunning) {
                        handler.removeCallbacksAndMessages(null);
                        handler.sendEmptyMessageDelayed(1, time);
                    }
                    break;
            }
            return false;
        }
    };
}
