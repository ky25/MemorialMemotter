package com.kyosuke25.MemorialMemotter;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;


public class MyAppUtil {

	/**
	 * int型の配列から年月日文字列を作成する。
	 * @param dateInts
	 * @return
	 */
	public static String createDateForm(int[] dateInts){

        Calendar cal = Calendar.getInstance();
        cal.set(dateInts[0], dateInts[1], dateInts[2]);

        return DateFormat.getDateInstance(DateFormat.SHORT).format(cal.getTime());
	}

	/**
	 * 年・月・日それぞれの文字列から年月日文字列を作成する。
	 * それぞれをint型にした配列にしてデリゲート。
	 *
	 * @param year
	 * @param month
	 * @param day
	 * @return
	 */
	public static String createDateForm(String year, String month, String day){

		int[] dateInts = {Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day)};
		return createDateForm(dateInts);
	}

	/**
	 * あと○日とか○日後の文字列を作成する。（int配列が引数）
	 *
	 * @param context
	 * @param style
	 * @param dataInts
	 * @return
	 */
	public static String createCountForm(Context context, String style, int[] dateInts){

		// カウントダウン（あと○日）スタイル
		if(MyAppConsts.STYLE_COUNTDOWN.equals(style)){
			// 両方の日数を計算
			long gap = calcDateGapForCountdown(dateInts);

			if(gap == 1){
				return context.getString(R.string.tomorrow_style);
			}else if(gap == 0){
				return context.getString(R.string.today_style);
			}else{
				return context.getString(R.string.countdown_style_prefix)
						+ context.getString(R.string.count_spacer)
						+ gap
						+ context.getString(R.string.count_spacer)
						+ context.getString(R.string.countdown_style_without_space);
			}
		}
		// カウントアップ（○日目）スタイル
		else if(MyAppConsts.STYLE_COUNTUP.equals(style)){
			// 両方の日数を計算
			return context.getString(R.string.countup_style_without_space)
					+ context.getString(R.string.count_spacer)
					+ calcDateGapForCountup(dateInts)
					+ context.getString(R.string.count_spacer)
					+ context.getString(R.string.countup_style_postfix);
		}
		// それ以外はエラーを表す文字を返す。（ありえない）
		else{
			return context.getString(R.string.error_literal);
		}
	}

	/**
	 * あと○日とか○日後の文字列を作成する。（Stringの年・月・日が引数）
	 *
	 * @param context
	 * @param style
	 * @param year
	 * @param month
	 * @param day
	 * @return
	 */
	public static String createCountForm(Context context, String style, String year, String month, String day){
		int[] dateInts = {Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day)};
		return createCountForm(context, style, dateInts);
	}


	/**
	 * カウントダウン（あと○日）スタイル用に日付の差を計算する。
	 *
	 * @param dateInts
	 * @return
	 */
	private static long calcDateGapForCountdown(int[] dateInts){

		// あと○日の場合、記念日が「今日」を含む未来日付でなければ、
		// 年を1年先に進めて、その日との差を計算する。
		Calendar calendar = Calendar.getInstance();
		long gap = calcDateGap(dateInts);
		if(gap < 0){
			dateInts[0] = calendar.get(Calendar.YEAR) + 1;
			return calcDateGap(dateInts);
		}else{
			return gap;
		}
	}

	/**
	 * カウントアップ（○日目）スタイル用に日付の差を計算する。
	 *
	 * @param dateInts
	 * @return
	 */
	private static long calcDateGapForCountup(int[] dateInts){
		// 負なら正負反転する。
		// あと、+1から返す。開始日が1日目なので。
		return Math.abs(calcDateGap(dateInts)) + 1;
	}

	/**
	 * 日付の差を計算する。
	 *
	 * @param dateInts
	 * @return
	 */
	private static long calcDateGap(int[] dateInts){

		// 現在日時（long型）の取得
		Calendar calendar = Calendar.getInstance();
		clearTime(calendar);
		long today = calendar.getTimeInMillis();

		// 入力された日時（long型）の取得
		calendar.set(dateInts[0], dateInts[1], dateInts[2]);
		clearTime(calendar);
		long start = calendar.getTimeInMillis();

		// 両方の日数を計算
		return (start - today) / (24*60*60*1000);
	}

	/**
	 * 日付を00:00:00にクリアする。
	 *
	 * @param calendar
	 * @return
	 */
	private static Calendar clearTime(Calendar calendar){

		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		return calendar;
	}

	/**
	 * Twitter連携状態の確認
	 *
	 * @param twitterStatus
	 * @return
	 */
	public static boolean isConnected(String twitterStatus){
		if(twitterStatus != null
				&& twitterStatus.equals(MyAppConsts.STATUS_AVAILABLE)){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * 設定ファイルから記念日リストを読み込んで、Listに復元する。
	 *
	 * @return
	 */
	public static List<AnniversaryItem> makeAnniversarryList(
			Context context,
			SharedPreferences pref){
		List<AnniversaryItem> anniversaryList =
			new ArrayList<AnniversaryItem>();

		// 総記念日数を取得
		int counts = pref.getInt(
				MyAppConsts.PREF_KEY_OF_ANNIVERSARY_COUNT, 0);

		// 総記念日数だけfor文を回す。
		for(int i=0; i<counts; i++){
			AnniversaryItem anniversary = new AnniversaryItem();

			// 記念日内容の取得
			anniversary.setAnniversary(
					pref.getString(
							MyAppConsts.PREF_KEY_OF_ANNIVERSARY + i,
							MyAppConsts.PREF_DEFAULT_VALUE_STRING));
			// 記念日年の取得
			String year =
				pref.getString(
					MyAppConsts.PREF_KEY_OF_YEAR + i,
					MyAppConsts.PREF_DEFAULT_VALUE_STRING);
			anniversary.setAnniversaryYear(year);
			// 記念日月の取得
			String month =
				pref.getString(
						MyAppConsts.PREF_KEY_OF_MONTH + i,
						MyAppConsts.PREF_DEFAULT_VALUE_STRING);
			anniversary.setAnniversaryMonth(month);
			// 記念日日の取得
			String day =
				pref.getString(
						MyAppConsts.PREF_KEY_OF_DAY + i,
						MyAppConsts.PREF_DEFAULT_VALUE_STRING);
			anniversary.setAnniversaryDay(day);
			// 記念日日付の生成、セット。
			anniversary.setAnniversaryDate(
					MyAppUtil.createDateForm(year, month, day));
			// カウントスタイルの取得
			anniversary.setCountStyle(
					pref.getString(
							MyAppConsts.PREF_KEY_OF_COUNT_STYLE + i,
							MyAppConsts.PREF_DEFAULT_VALUE_STRING));
			// チェックボックスの状態の取得
			anniversary.setChecked(
					pref.getBoolean(
							MyAppConsts.PREF_KEY_OF_CHECKED + i,
							MyAppConsts.PREF_DEFAULT_VALUE_BOOLEAN));
			// あと○日とか○日目の文字列をセット
			anniversary.setCount(
					MyAppUtil.createCountForm(
							context,
							pref.getString(
									MyAppConsts.PREF_KEY_OF_COUNT_STYLE + i,
									MyAppConsts.PREF_DEFAULT_VALUE_STRING),
							year,
							month,
							day));
			// あと○日とか○日目のスタイルIDをセット
			anniversary.setCountStyle(
					pref.getString(
							MyAppConsts.PREF_KEY_OF_COUNT_STYLE + i,
							MyAppConsts.PREF_DEFAULT_VALUE_STRING));

			// リストにセット
			anniversaryList.add(anniversary);
		}

		return anniversaryList;
	}
}
