package com.amaykov.finalproject;

import android.text.Layout;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.widget.TextView;

public final class ScrollFriendlyLinkMovementMethod extends LinkMovementMethod {

    private static ScrollFriendlyLinkMovementMethod instance;

    public static ScrollFriendlyLinkMovementMethod getInstance() {
        if (instance == null) {
            instance = new ScrollFriendlyLinkMovementMethod();
        }
        return instance;
    }

    private ScrollFriendlyLinkMovementMethod() {
    }

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();
            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            if (layout != null) {
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);
                ClickableSpan[] links = buffer.getSpans(off, off, ClickableSpan.class);
                if (links.length > 0) {
                    if (action == MotionEvent.ACTION_UP) {
                        links[0].onClick(widget);
                    }
                    return true;
                }
            }
        }
        return false;
    }
}
