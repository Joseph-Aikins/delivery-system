/*
 * Copyright (c) 2018. Property of Dennis Kwabena Bilson. No unauthorized duplication of this material should be made without prior permission from the developer
 */

package io.codelabs.recyclerview;

import android.transition.Transition;

/**
 * Dummy implementations of TransitionListener methods.
 */
public abstract class TransitionCallback implements Transition.TransitionListener {
	
	@Override
	public void onTransitionStart(Transition transition) {
		// no-op
	}
	
	@Override
	public void onTransitionEnd(Transition transition) {
		// no-op
	}
	
	@Override
	public void onTransitionCancel(Transition transition) {
		// no-op
	}
	
	@Override
	public void onTransitionPause(Transition transition) {
		// no-op
	}
	
	@Override
	public void onTransitionResume(Transition transition) {
		// no-op
	}
}
