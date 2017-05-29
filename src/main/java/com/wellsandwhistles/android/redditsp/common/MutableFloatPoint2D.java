package com.wellsandwhistles.android.redditsp.common;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.view.MotionEvent;

public class MutableFloatPoint2D {

	public float x, y;

	public void reset() {
		x = 0;
		y = 0;
	}

	public void set(final MotionEvent event, final int pointerIndex) {
		x = event.getX(pointerIndex);
		y = event.getY(pointerIndex);
	}

	public void set(MutableFloatPoint2D other) {
		x = other.x;
		y = other.y;
	}

	public void set(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public void add(final MutableFloatPoint2D rhs, final MutableFloatPoint2D result) {
		result.x = x + rhs.x;
		result.y = y + rhs.y;
	}

	public void sub(final MutableFloatPoint2D rhs, final MutableFloatPoint2D result) {
		result.x = x - rhs.x;
		result.y = y - rhs.y;
	}

	public void add(final MutableFloatPoint2D rhs) {
		add(rhs, this);
	}

	public void sub(final MutableFloatPoint2D rhs) {
		sub(rhs, this);
	}

	public void scale(double factor) {
		x *= factor;
		y *= factor;
	}

	public double euclideanDistanceTo(final MutableFloatPoint2D other) {
		final float xDistance = x - other.x;
		final float yDistance = y - other.y;
		return Math.sqrt(xDistance * xDistance + yDistance * yDistance);
	}

	public float distanceSquared() {
		return x*x + y*y;
	}
}
