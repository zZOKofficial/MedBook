package com.oxyorb.medbook.demo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.oxyorb.medbook.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One day's slots as a grid.
 *
 * A RecyclerView where the chamber cards on the profile screen are not, and for the
 * reason that screen gives for its own choice: a doctor has at most seven chambers, and
 * a sitting has up to twenty-four slots three abreast. That is past the point where
 * inflating the lot into a LinearLayout stops being the simpler answer.
 *
 * Emphasis runs the way the eye expects it to: the tile a person can actually do
 * something with is the raised one, and a time already gone recedes into the page. Free
 * slots are the whole point of the screen, so they are not the quietest thing on it.
 *
 * Every tile says its state in words as well as colour. A grid told apart only by
 * colour is a grid a colour-blind chamber manager cannot read, and it also keeps every
 * tile the same height, which a missing second line would not.
 */
public final class SlotAdapter extends RecyclerView.Adapter<SlotAdapter.SlotHolder> {

	public interface Listener {
		void onSlotSelected(DemoSchedule.Slot _slot);
	}

	private final List<DemoSchedule.Slot> slots = new ArrayList<>();
	private final Listener listener;
	private int epochDay;

	public SlotAdapter(Listener _listener) {
		listener = _listener;
		setHasStableIds(false);
	}

	public void show(List<DemoSchedule.Slot> _slots, int _epochDay) {
		slots.clear();
		slots.addAll(_slots);
		epochDay = _epochDay;
		notifyDataSetChanged();
	}

	public List<DemoSchedule.Slot> slots() {
		return Collections.unmodifiableList(slots);
	}

	@Override
	public int getItemCount() {
		return slots.size();
	}

	@Override
	public SlotHolder onCreateViewHolder(ViewGroup _parent, int _viewType) {
		return new SlotHolder(LayoutInflater.from(_parent.getContext())
			.inflate(R.layout.item_slot, _parent, false));
	}

	@Override
	public void onBindViewHolder(SlotHolder _holder, int _position) {
		_holder.bind(slots.get(_position), epochDay, listener);
	}

	static final class SlotHolder extends RecyclerView.ViewHolder {

		private final TextView time;
		private final TextView state;

		SlotHolder(View _view) {
			super(_view);
			time = _view.findViewById(R.id.slot_time);
			state = _view.findViewById(R.id.slot_state);
		}

		void bind(final DemoSchedule.Slot _slot, int _epochDay, final Listener _listener) {
			Context _context = itemView.getContext();
			time.setText(DemoFormat.time(_context, _epochDay, _slot.minuteOfDay));

			int _background;
			int _foreground;
			int _label;
			switch (_slot.state) {
				case DemoSchedule.STATE_YOURS:
					_background = com.google.android.material.R.attr.colorPrimaryContainer;
					_foreground = com.google.android.material.R.attr.colorOnPrimaryContainer;
					_label = R.string.slot_yours;
					break;
				case DemoSchedule.STATE_TAKEN:
					_background = com.google.android.material.R.attr.colorSurfaceContainerLow;
					_foreground = com.google.android.material.R.attr.colorOnSurfaceVariant;
					_label = R.string.slot_taken;
					break;
				default:
					_background = com.google.android.material.R.attr.colorSurfaceContainerHighest;
					_foreground = com.google.android.material.R.attr.colorOnSurface;
					_label = R.string.slot_free;
					break;
			}

			itemView.setBackgroundColor(ThemeColor.of(_context, _background));
			int _text = ThemeColor.of(_context, _foreground);
			time.setTextColor(_text);
			state.setTextColor(_text);
			state.setText(_label);

			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					_listener.onSlotSelected(_slot);
				}
			});
		}
	}
}
