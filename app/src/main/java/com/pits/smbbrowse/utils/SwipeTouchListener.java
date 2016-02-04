package com.pits.smbbrowse.utils;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.pits.smbbrowse.R;
import com.pits.smbbrowse.adapters.ContentListAdapter;


public class SwipeTouchListener implements View.OnTouchListener {

    public static boolean sIsSwipe = false;
    private ListView mListView;
    private Helpers mHelpers;

    public SwipeTouchListener(ListView listView) {
        this.mListView = listView;
        mHelpers = new Helpers();
    }

    private final GestureDetector gestureDetector = new GestureDetector(new GestureListener());

    public boolean onTouch(final View view, final MotionEvent motionEvent) {
        return gestureDetector.onTouchEvent(motionEvent);
    }


    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 30;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            sIsSwipe = false;
            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        int pos = mListView.pointToPosition((int) e1.getX(), (int) e2.getY());
                        View wantedView = mListView.getChildAt(pos).findViewById(R.id.file_title);
                        View fadeInLayout = mListView.getChildAt(pos).findViewById(R.id.background);
                        if (diffX > 0) {
                            System.out.println(wantedView.getVisibility() == View.VISIBLE);
                            if(wantedView.getVisibility() == View.VISIBLE) {
                                mHelpers.fadeOutView(wantedView);
                                mHelpers.slideInView(fadeInLayout);
                                TextView textView = (TextView) fadeInLayout;
                                textView.setText("Delete");
                            } else {
                                mHelpers.fadeInView(fadeInLayout);
                                mHelpers.slideOutView(wantedView);
                            }
                            onSwipeRight(pos, wantedView);
                            sIsSwipe = true;
                        } else {
                            if(wantedView.getVisibility() == View.VISIBLE) {
                                mHelpers.fadeOutView(wantedView);
                                mHelpers.slideInView(ContentListAdapter.ViewHolder.background);
                                ContentListAdapter.ViewHolder.background.setText("Move");
                            } else {
                                mHelpers.fadeInView(ContentListAdapter.ViewHolder.background);
                                mHelpers.slideOutView(wantedView);
                                ContentListAdapter.ViewHolder.background.setText("Move");
                            }
                            onSwipeLeft(pos, wantedView);
                            sIsSwipe = true;
                        }
                    }
                } else {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeBottom();
                        } else {
                            onSwipeTop();
                        }
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }
    public void onSwipeRight(int position, View view) {}
    public void onSwipeLeft(int position, View view) {}
    public void onSwipeTop() {}
    public void onSwipeBottom() {}
}