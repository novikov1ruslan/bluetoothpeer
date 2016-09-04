package com.ivygames.morskoiboi.screen.view;

import android.support.annotation.NonNull;
import android.view.MotionEvent;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TouchStateTest {

    @Test
    public void testStartedDragging() {
        TouchState touchState = new TouchState();

        touchState.setEvent(newMotionEvent(100f, 200f, MotionEvent.ACTION_UP));
        assertThat(touchState.isDragging(), is(false));

        touchState.setEvent(newMotionEvent(100f, 200f, MotionEvent.ACTION_DOWN));
        assertThat(touchState.isDragging(), is(true));

        touchState.setEvent(newMotionEvent(100f, 200f, MotionEvent.ACTION_UP));
        assertThat(touchState.isDragging(), is(false));

        touchState.setEvent(newMotionEvent(100f, 200f, MotionEvent.ACTION_MOVE));
        assertThat(touchState.isDragging(), is(false));

        touchState.setEvent(newMotionEvent(100f, 200f, MotionEvent.ACTION_DOWN));
        assertThat(touchState.isDragging(), is(true));

        touchState.setEvent(newMotionEvent(100f, 200f, MotionEvent.ACTION_MOVE));
        assertThat(touchState.isDragging(), is(true));
    }

    @NonNull
    public static TouchState newTouchState(float x, float y, int action) {
        TouchState touchState = new TouchState();
        touchState.setEvent(newMotionEvent(x, y, action));
        return touchState;
    }

    @NonNull
    private static MotionEvent newMotionEvent(float x, float y, int action) {
        MotionEvent event = mock(MotionEvent.class);
        when(event.getX()).thenReturn(x);
        when(event.getY()).thenReturn(y);
        when(event.getAction()).thenReturn(action);
        return event;
    }

}