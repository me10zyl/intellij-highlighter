package com.yilnz.intellij.highlighter;

import com.intellij.openapi.extensions.ExtensionFactory;

public class HighlighterFactory implements ExtensionFactory {

	private Highlighter highlighter;

	@Override
	public Object createInstance(String s, String s1) {
		if (highlighter == null) {
			highlighter = new Highlighter();
		}
		return highlighter;
	}
}

