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
 * The same day from the other side of the desk: the chamber's own list of serials.
 *
 * This is the half of the demo a hospital is actually being shown. The patient screen
 * proves a person can take a time; this proves somebody at the chamber would know about
 * it, in the order they would call them, with the one who did not turn up marked.
 *
 * Free slots are listed rather than hidden. A chamber reads its book to see the gaps.
 */
public final class ConsoleAdapter extends RecyclerView.Adapter<ConsoleAdapter.RowHolder> {

	private final List<DemoSchedule.Slot> slots = new ArrayList<>();
	private List<String> roster = Collections.emptyList();
	private int epochDay;
	private int noShowSerial;

	public ConsoleAdapter() {
		setHasStableIds(false);
	}

	/**
	 * @param _noShowSerial the serial that did not attend, or 0 for none. Only ever set
	 *                      for a time that has already passed -- a no-show in tomorrow's
	 *                      book is the kind of thing a chamber manager spots at once.
	 */
	public void show(List<DemoSchedule.Slot> _slots, List<String> _roster, int _epochDay,
			int _noShowSerial) {
		slots.clear();
		slots.addAll(_slots);
		roster = _roster;
		epochDay = _epochDay;
		noShowSerial = _noShowSerial;
		notifyDataSetChanged();
	}

	@Override
	public int getItemCount() {
		return slots.size();
	}

	@Override
	public RowHolder onCreateViewHolder(ViewGroup _parent, int _viewType) {
		return new RowHolder(LayoutInflater.from(_parent.getContext())
			.inflate(R.layout.item_console_row, _parent, false));
	}

	@Override
	public void onBindViewHolder(RowHolder _holder, int _position) {
		_holder.bind(slots.get(_position), roster, epochDay, noShowSerial);
	}

	static final class RowHolder extends RecyclerView.ViewHolder {

		private final TextView serial;
		private final TextView time;
		private final TextView patient;
		private final TextView flag;

		RowHolder(View _view) {
			super(_view);
			serial = _view.findViewById(R.id.row_serial);
			time = _view.findViewById(R.id.row_time);
			patient = _view.findViewById(R.id.row_patient);
			flag = _view.findViewById(R.id.row_flag);
		}

		void bind(DemoSchedule.Slot _slot, List<String> _roster, int _epochDay, int _noShowSerial) {
			Context _context = itemView.getContext();

			// Through a format argument, which is the only reason these come out in
			// Bengali numerals when the app is in Bengali. Built by hand they would not.
			serial.setText(_context.getString(R.string.console_serial_number, _slot.serial));
			time.setText(DemoFormat.time(_context, _epochDay, _slot.minuteOfDay));

			boolean _yours = _slot.state == DemoSchedule.STATE_YOURS;
			if (_yours) {
				patient.setText(R.string.console_you);
			} else if (_slot.state == DemoSchedule.STATE_TAKEN) {
				patient.setText(DemoPatients.name(_roster, _slot.serial));
			} else {
				patient.setText(R.string.console_free);
			}

			int _text = ThemeColor.of(_context, _slot.free()
				? com.google.android.material.R.attr.colorOutline
				: com.google.android.material.R.attr.colorOnSurface);
			time.setTextColor(_text);
			patient.setTextColor(_text);

			serial.setTextColor(ThemeColor.of(_context,
				com.google.android.material.R.attr.colorOutline));
			itemView.setBackgroundColor(_yours
				? ThemeColor.of(_context, com.google.android.material.R.attr.colorPrimaryContainer)
				: 0);
			if (_yours) {
				// The serial reads as a faint aside everywhere else, and would vanish
				// against the filled row. On this one it belongs to the row's own pair.
				int _onContainer = ThemeColor.of(_context,
					com.google.android.material.R.attr.colorOnPrimaryContainer);
				serial.setTextColor(_onContainer);
				time.setTextColor(_onContainer);
				patient.setTextColor(_onContainer);
			}

			boolean _noShow = !_slot.free() && !_yours && _slot.serial == _noShowSerial;
			flag.setVisibility(_noShow ? View.VISIBLE : View.GONE);
		}
	}
}
