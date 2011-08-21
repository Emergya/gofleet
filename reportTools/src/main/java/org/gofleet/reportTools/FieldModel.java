package org.gofleet.reportTools;

import java.util.LinkedList;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.apache.commons.logging.LogFactory;

public class FieldModel<T> implements ListModel, ComboBoxModel {

	static final org.apache.commons.logging.Log LOG = LogFactory
			.getLog(FieldModel.class);
	private LinkedList<T> data = new LinkedList<T>();
	private LinkedList<ListDataListener> dataListeners = new LinkedList<ListDataListener>();
	private T selected = null;

	public FieldModel(T[] data) {
		if (data != null)
			for (T t : data)
				if (t != null)
					this.data.add(t);
	}

	@Override
	public void addListDataListener(ListDataListener l) {
		this.dataListeners.add(l);
	}

	public void moveUp(T object) {
		LOG.trace("moveUp(" + object + ")");
		try {
			int index = this.data.indexOf(object);
			if (index > 0) {
				index--;
				this.removeElement(object);
				this.addElement(object, index);

			}
		} catch (Throwable t) {
			LOG.error("moveUp " + object, t);
		}
	}

	public void moveDown(T object) {
		LOG.trace("moveDown(" + object + ")");
		try {
			int index = this.data.indexOf(object);
			if (index >= 0) {
				index++;
				this.removeElement(object);
				this.addElement(object, index);

			}
		} catch (Throwable t) {
			LOG.error("moveDown " + object, t);
		}
	}

	@Override
	public T getElementAt(int index) {
		try {
			return data.get(index);
		} catch (Throwable t) {
			return null;
		}
	}

	public List<T> getAllElements() {
		LinkedList<T> res = new LinkedList<T>();
		res.addAll(this.data);
		return res;
	}

	public void clear() {
		synchronized (this.data) {
			int size = this.data.size();
			this.data.clear();
			for (ListDataListener listener : dataListeners) {
				ListDataEvent event = new ListDataEvent(this,
						ListDataEvent.INTERVAL_REMOVED, 0, size);
				ListDataEvent event2 = new ListDataEvent(this,
						ListDataEvent.CONTENTS_CHANGED, 0, size);
				listener.intervalAdded(event);
				listener.intervalAdded(event2);
			}
		}
	}

	public void addElement(T object) {
		this.addElement(object, this.data.size());
	}

	public void addElement(T object, int index) {
		if (object == null)
			return;
		synchronized (this.data) {
			LOG.debug("addElement(" + object + ", " + index + ")");
			if (index >= this.data.size())
				this.data.add(object);
			else
				this.data.add(index, object);
			for (ListDataListener listener : dataListeners) {
				ListDataEvent event = new ListDataEvent(this,
						ListDataEvent.INTERVAL_ADDED, this.data.size() - 1,
						this.data.size());
				ListDataEvent event2 = new ListDataEvent(this,
						ListDataEvent.CONTENTS_CHANGED, this.data.size() - 1,
						this.data.size());
				listener.intervalAdded(event);
				listener.intervalAdded(event2);
			}
		}
	}

	public void removeElement(T object) {
		if (object == null)
			return;
		synchronized (this.data) {
			int index = this.data.indexOf(object);
			if (this.data.remove(object)) {
				for (ListDataListener listener : dataListeners) {
					ListDataEvent event = new ListDataEvent(this,
							ListDataEvent.INTERVAL_REMOVED, index, index + 1);
					ListDataEvent event2 = new ListDataEvent(this,
							ListDataEvent.CONTENTS_CHANGED, index, index + 1);
					listener.intervalAdded(event);
					listener.intervalAdded(event2);
				}
			}
		}
	}

	@Override
	public int getSize() {
		return data.size();
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		this.dataListeners.remove(l);
	}

	@Override
	public Object getSelectedItem() {
		return this.selected;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setSelectedItem(Object anItem) {
		try {
			this.selected = (T) anItem;
		} catch (Throwable t) {
			LOG.error("Wrong type of object", t);
		}
	}

	public void addAll(T[] data2) {
		for (T t : data2)
			this.addElement(t);
	}
}
