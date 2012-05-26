package net.idlesoft.android.apps.github.ui.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import net.idlesoft.android.apps.github.R;

public
class FlowLayout extends ViewGroup
{
	private int mHorizontalSpacing;
	private int mVerticalSpacing;

	public
	FlowLayout(Context context)
	{
		super(context);
	}

	public
	FlowLayout(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout);
		try {
			mHorizontalSpacing = a.getDimensionPixelSize(R.styleable.FlowLayout_horizontalSpacing, 0);
			mVerticalSpacing = a.getDimensionPixelSize(R.styleable.FlowLayout_verticalSpacing, 0);
		} finally {
			a.recycle();
		}
	}

	@Override
	protected
	void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);

		int width = 0;
		int height = getPaddingTop();

		int currentWidth = getPaddingLeft();
		int currentHeight = 0;

		boolean breakLine = false;

		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			LayoutParams lp = (LayoutParams) child.getLayoutParams();
			measureChild(child, widthMeasureSpec, heightMeasureSpec);

			if (breakLine || currentWidth + child.getMeasuredWidth() > widthSize) {
				height += currentHeight + mVerticalSpacing;
				currentHeight = 0;
				if (currentWidth > width) width = currentWidth;
				currentWidth = getPaddingLeft();
			}

			int spacing = mHorizontalSpacing;
			if (lp.spacing > -1) {
				spacing = lp.spacing;
			}

			lp.x = currentWidth;
			lp.y = height;

			currentWidth += child.getMeasuredWidth() + spacing;
			int childHeight = child.getMeasuredHeight();
			if (childHeight > currentHeight) currentHeight = childHeight;

			breakLine = lp.breakLine;
		}

		height += currentHeight;
		if (currentWidth > width) width = currentWidth;

		width += getPaddingRight();
		height += getPaddingBottom();

		setMeasuredDimension(resolveSize(width, widthMeasureSpec),
							 resolveSize(height, heightMeasureSpec));
	}

	@Override
	protected
	void onLayout(boolean changed, int l, int t, int r, int b)
	{
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			LayoutParams lp = (LayoutParams) child.getLayoutParams();
			child.layout(lp.x, lp.y, lp.x + child.getMeasuredWidth(), lp.y + child.getMeasuredHeight());
		}
	}

	@Override
	protected
	boolean checkLayoutParams(ViewGroup.LayoutParams p)
	{
		return p instanceof LayoutParams;
	}

	@Override
	protected
	LayoutParams generateDefaultLayoutParams()
	{
		return new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	}

	@Override
	public
	LayoutParams generateLayoutParams(AttributeSet attrs)
	{
		return new LayoutParams(getContext(), attrs);
	}

	@Override
	protected
	ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p)
	{
		return new LayoutParams(p.width, p.height);
	}

	public static class LayoutParams extends ViewGroup.LayoutParams
	{
		public boolean breakLine = false;
		public int spacing = -1;

		private int x;
		private int y;

		public
		LayoutParams(Context context, AttributeSet attrs)
		{
			super(context, attrs);

			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout);
			try {
				spacing = a.getDimensionPixelSize(R.styleable.FlowLayout_LayoutParams_layout_spacing, -1);
				breakLine = a.getBoolean(R.styleable.FlowLayout_LayoutParams_breakLine, false);
			} finally {
				a.recycle();
			}
		}

		public
		LayoutParams(int width, int height)
		{
			super(width, height);
		}
	}
}
