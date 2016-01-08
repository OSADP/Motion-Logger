package com.example.mitmotionapp.review;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.mitmotionapp.AppConstants;
import com.example.mitmotionapp.BaseFragment;
import com.example.mitmotionapp.R;
import com.example.mitmotionapp.databases.DBAdapter;
import com.example.mitmotionapp.readingsobject.SessionObject;

public class Review extends BaseFragment {

	// public static final int DIALOG_DELETE = 47;

	boolean DeleteMode = false;

	ImageView goDetails;
	LinearLayout linearLayout;
	RelativeLayout hidden;
	Button editBtn;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.review, container, false);
		linearLayout = (LinearLayout) view.findViewById(R.id.inner_main_layout);

		// TODO: Fix this button reference by ensuring the right views are
		// references (now, we get a null pointer exception)
		editBtn = (Button) view.findViewById(R.id.header_rightBtn);
		editBtn.setOnClickListener(editlistener);

		DBAdapter db = new DBAdapter(getActivity());
		Cursor c = db.getAllRecords();
		if (c.moveToFirst()) {
			do {
				Integer id = (int) (long) c.getLong(0);
				System.out.println(id);
				// Creating RelativeLayout....
				RelativeLayout rl = new RelativeLayout(getActivity());
				RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.MATCH_PARENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);
				rl.setLayoutParams(rlp);
				// rl.setPadding(20, 10, 5, 10);
				rl.setBackgroundResource(R.drawable.bottom_border);

				LinearLayout linear = new LinearLayout(getActivity());
				RelativeLayout.LayoutParams llp = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.WRAP_CONTENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);
				llp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				linear.setLayoutParams(llp);
				linear.setPadding(10, 10, 0, 10);
				linear.setOrientation(LinearLayout.VERTICAL);
				// Crating heading textView...
				TextView unNamedHeading = new TextView(getActivity());
				LinearLayout.LayoutParams tv = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT);
				unNamedHeading.setLayoutParams(tv);
				unNamedHeading.setTextColor(Color.BLACK);
				unNamedHeading.setText((c.getString(1)));
				unNamedHeading.setTextSize(17);
				unNamedHeading.setTypeface(unNamedHeading.getTypeface(),
						Typeface.BOLD);
				linear.addView(unNamedHeading);

				// Creating date TextView...
				TextView timestamp = new TextView(getActivity());
				timestamp.setLayoutParams(tv);
				timestamp.setTextColor(Color.BLACK);
				timestamp.setText(c.getString(2));
				timestamp.setTextSize(15);
				linear.addView(timestamp);
				rl.addView(linear);

				// Creating nextImage....
				RelativeLayout.LayoutParams ilp = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.WRAP_CONTENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);
				ilp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				ImageView nextImage = new ImageView(getActivity());

				nextImage.setPadding(10, 15, 0, 10);
				nextImage.setLayoutParams(ilp);
				nextImage.setImageResource(R.drawable.ic_action_next_item);
				rl.addView(nextImage);

				rl.setId(id);
				rl.setOnClickListener(detaillistener);

				// add relativeLayout into Linear layout...
				linearLayout.addView(rl);

			} while (c.moveToNext());
		}
		db.close();

		return view;
	}

	private OnClickListener detaillistener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			SessionObject.setIdforNext(v.getId());
			System.out.println("view id");
			System.out.println(v.getId());
			if (!DeleteMode) {
				/* Go to next fragment in navigation stack */
				mActivity.pushFragments(AppConstants.TAB_B, new Details(),
						true, true);
			} else {
				// Toast.makeText(getActivity(),"Delete not implemented",Toast.LENGTH_SHORT).show();
				mActivity.pushFragments(AppConstants.TAB_B, new Delete(), true,
						true);
			}
		}
	};

	private OnClickListener editlistener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			// SessionObject.setIdforNext(v.getId());
			// Toast.makeText(getActivity(),"Edit/delete not implemented",Toast.LENGTH_SHORT).show();
			View parent = (View) v.getParent();
			Button header_rightBtn = (Button) parent
					.findViewById(R.id.header_rightBtn);

			DeleteMode = !DeleteMode;
			if (DeleteMode) {
				// Toast.makeText(getActivity(),"In \"Delete Mode\"",Toast.LENGTH_SHORT).show();
				header_rightBtn.setText(R.string.delete);
			} else {
				// Toast.makeText(getActivity(),"In \"Review Mode\"",Toast.LENGTH_SHORT).show();
				header_rightBtn.setText(R.string.review);
			}
			// Toast.makeText(getActivity(),"Edit/delete not implemented",Toast.LENGTH_SHORT).show();
		}
	};

}
