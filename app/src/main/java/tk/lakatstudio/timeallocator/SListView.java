package tk.lakatstudio.timeallocator;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

public class SListView extends ListView {

    private boolean isIntercepting = true;
    private float currentX = 0.0f;
    private float currentY = 0.0f;

    public SListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        super.onInterceptTouchEvent(event);
        getParent().requestDisallowInterceptTouchEvent(isIntercepting);
        final int action = event.getAction();
        Log.d("SListView", "_1_ Event: " + event.getAction());
        if (action == MotionEvent.ACTION_DOWN) {
            currentX = event.getX();
            currentY = event.getY();
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        Log.d("SListView", "_2_ Event: " + event.getAction());
        if (action == MotionEvent.ACTION_MOVE) {
            int index = getFirstVisiblePosition();
            View v = getChildAt(0);
            int top = (v == null) ? 0 : v.getTop();
            /*if ((event.getY() - currentY) > Math.abs(event.getX() - currentX) && (index == 0) && (top
                    >= 0) && isIntercepting) {*/
            if(shouldDisableScrollableParent(event)){
                // User scrolled vertically
                isIntercepting = false;
                getParent().requestDisallowInterceptTouchEvent(false);
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    public void setIsIntercepting(boolean isIntercepting) {
        this.isIntercepting = isIntercepting;
        
        getParent().requestDisallowInterceptTouchEvent(isIntercepting);
    }

    /*@Override
    public boolean onTouchEvent(MotionEvent ev) {
        return shouldDisableScrollableParent(ev);
        //return super.onTouchEvent(ev);
    }*/



    float previousX;
    float previousY;

    private boolean shouldDisableScrollableParent(final MotionEvent event) {
        boolean intercept = false;
        float x = event.getX();
        float y = event.getY();

        Log.d("SListView", "_1_ Y: " + y + "\tX: " + x + "\tEvent: " + event.getAction());

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = x - previousX;
                float dy = y - previousY;
                Log.d("SListView", "_2_ Y: " + dy + " X: " + dx);

                // Here you can try to detect the swipe. It will be necessary to
                // store more than the previous value to check that the user move constantly in the same direction
                intercept = detectSwipe(dx, dy);
                break;

            case MotionEvent.ACTION_BUTTON_PRESS:
                performClick();
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            default:
                break;
        }

        previousX = x;
        previousY = y;

        return intercept;
    }

    private boolean detectSwipe(final float dx, final float dy) {
        // True if we're scrolling in Y direction
        Log.d("SListView", "_3_ dY: " + dy + " dX: " + dx);
        if (Math.abs(dx) < Math.abs(dy)) {
            Log.d("asd", "Direction Y is yielding: " + dy);

            if (dy < 0 || dy > 0) {
                // Touch from lower to higher or vice versa
                return true;
            }

            // Top view isn't shown, can always scroll up
            final View view = getChildAt(0);
            if (view == null) {
                return true;
            }

            // Top view baseline doesn't equal to top of the RecyclerView
            if (view.getTop() < 0) {
                return true;
            }
        }

        return false;
    }
}
