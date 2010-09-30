package com.kyosuke25.MemorialMemotter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

public class MemorialMemotterWidgetProvider extends AppWidgetProvider {

	private SharedPreferences pref;
	private List<AnniversaryItem> anniversaryList;

	private RemoteViews views;

	@Override
	public void onUpdate(
			Context context,
			AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		views = new RemoteViews(
				context.getPackageName(),
				R.layout.widget);

        // 表示データを設定ファイルから復元。
        // もし設定ファイルがなければ箱だけ生成。
		// 設定ファイルを呼び出し。
		pref = context.getSharedPreferences(
				MyAppConsts.PREFERENCE_NAME,
				0);
		if(pref != null){
			// Listを復元。
			this.anniversaryList = MyAppUtil.makeAnniversarryList(context, pref);
		}else{
			// 取得したデータを入れていく箱を生成。
			this.anniversaryList = new ArrayList<AnniversaryItem>();

			// この先prefが無いと困るので作る。
			pref.edit().commit();
		}

		this.displayAnniversaryList(context);

		appWidgetManager.updateAppWidget(appWidgetIds, views);
	}

	/**
	 * 記念日リストを表示する。
	 */
	private void displayAnniversaryList(Context context){

		// 本日日付のセット
		views.setTextViewText(
				R.id.widget_textview_today,
				this.makeDateText(context));

		int loopCount =
			anniversaryList.size() < MyAppConsts.WIDGET_LIST_SIZE?
					anniversaryList.size()
					:MyAppConsts.WIDGET_LIST_SIZE;

		int i = 0;
		for(; i<loopCount; i++){
			AnniversaryItem item = anniversaryList.get(i);
			views.setTextViewText(
					MyAppConsts.textViewIds[i],
					this.createWidgetText(
							item.getAnniversary(),
							item.getCount()));
		}
	}

	/**
	 * 日付の文字列を生成する。
	 *
	 * @param context
	 * @return
	 */
	private String makeDateText(Context context){

		SimpleDateFormat format =
			new SimpleDateFormat(
					context.getString(
							R.string.date_format));

		return format.format(new Date());
	}

	/**
	 * ウィジェット表示用テキストの生成
	 *
	 * @param anniversary
	 * @param date
	 * @return
	 */
	private String createWidgetText(String anniversary, String date){
		return date + ":" + anniversary;
	}
}
