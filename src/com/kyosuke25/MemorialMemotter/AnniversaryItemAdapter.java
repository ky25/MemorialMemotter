package com.kyosuke25.MemorialMemotter;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class AnniversaryItemAdapter extends ArrayAdapter<AnniversaryItem> {

	private  LayoutInflater inFlater;

	public AnniversaryItemAdapter(Context context, int rid, List<AnniversaryItem> list) {

		super(context, rid, list);

		inFlater = (LayoutInflater)context.getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
	}

	public View getView(int position, View contextView, ViewGroup parent){

		// データの取り出し
		final int p = position;
		final AnniversaryItem item = (AnniversaryItem)getItem(p);

		// レイアウトファイルからViewを作成
		final View view = inFlater.inflate(R.layout.anniversary, null);

		// 記念日の内容をセット
		TextView anniversary = (TextView)view.findViewById(R.id.anniversary_display);
		anniversary.setText(item.getAnniversary());

		// 記念日の日付年をセット
		TextView year = (TextView)view.findViewById(R.id.anniversary_year_display);
		year.setText(item.getAnniversaryYear());

		// 記念日の日付月をセット
		TextView month = (TextView)view.findViewById(R.id.anniversary_month_display);
		month.setText(item.getAnniversaryMonth());

		// 記念日の日付日をセット
		TextView day = (TextView)view.findViewById(R.id.anniversary_day_display);
		day.setText(item.getAnniversaryDay());

		// 記念日の日付文字列をセット
		TextView date = (TextView)view.findViewById(R.id.anniversary_date_display);
		date.setText(item.getAnniversaryDate());

		// あと○日とか○日目、っていう文字列をセット
		TextView count = (TextView)view.findViewById(R.id.anniversary_count_display);
		count.setText(item.getCount());

		// あと○日とか○日目、っていうスタイルのIDをセット
		TextView countStyle = (TextView)view.findViewById(R.id.anniversary_count_style_display);
		countStyle.setText(item.getCountStyle());

		// CheckBoxの状態をセット
		CheckBox checkbox = (CheckBox)view.findViewById(R.id.checkbox_for_anniversary);
		checkbox.setChecked(item.isChecked());

		// CheckBoxのチェックが押されたときの挙動。;
		checkbox.setOnCheckedChangeListener(
				new OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

						Context context = view.getContext();
						SharedPreferences pref =
							context.getSharedPreferences(
								MyAppConsts.PREFERENCE_NAME,
								context.MODE_PRIVATE);
						Editor editor = pref.edit();
						editor.putBoolean(MyAppConsts.PREF_KEY_OF_CHECKED + p, isChecked);
						editor.commit();

						item.setChecked(isChecked);
					}
				});

		return view;
	}
}
