package com.example.ds.daily_memo_reader.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by DS on 2/7/2016.
 */
public class EntryDetailImageView extends ImageView {
    public EntryDetailImageView(Context context) {
        super(context);
    }

    public EntryDetailImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EntryDetailImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    // Taken from Material Design Course Video
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int sevenEightHeight = MeasureSpec.getSize(widthMeasureSpec) * 7/8;
        int sevenEightHeightSpec =
                MeasureSpec.makeMeasureSpec(sevenEightHeight, MeasureSpec.EXACTLY);

        super.onMeasure(widthMeasureSpec, sevenEightHeightSpec);
    }
}
