package com.yilnz.intellij.highlighter;

import com.intellij.openapi.extensions.ExtensionFactory;

public class HighlighterFactory implements ExtensionFactory {

	@Override
	public Object createInstance(String s, String s1) {
		return new Highlighter();
	}
}

