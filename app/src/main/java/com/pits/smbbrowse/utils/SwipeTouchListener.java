package com.pits.smbbrowse.utils;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;


public class SwipeTouchListener implements View.OnTouchListener {

    public static boolean sIsSwipe = false;
    private ListView mListView;

    public SwipeTouchListener(ListView listView) {
        this.mListView = listView;
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
                        if (diffX > 0) {
                            onSwipeRight(pos);
                            sIsSwipe = true;
                        } else {
                            onSwipeLeft(pos);
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
    public void onSwipeRight(int position) {}
    public void onSwipeLeft(int position) {}
    public void onSwipeTop() {}
    public void onSwipeBottom() {}
}