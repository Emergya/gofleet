package org.gofleet.module;

import java.util.LinkedList;
import java.util.List;

public abstract class UI implements IModule {

	public UI() {
		super();
		loadUI();
	}

	protected abstract void loadUI();

	@Override
	public String getTitle() {
		return "UI Module";
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public List<String> getDependencies() {
		return new LinkedList<String>();
	}

}
