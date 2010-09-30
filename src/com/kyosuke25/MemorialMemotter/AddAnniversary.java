package com.kyosuke25.MemorialMemotter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.DatePicker.OnDateChangedListener;

/**
 * 記念日を入力する。
 *
 * @author kyosuke
 *
 */
public class AddAnniversary extends Activity implements OnDateChangedListener{

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.anniversary_add);

		final Intent intent = getIntent();

		// ステータスが「update」なら各フィールドに初期値を入れる。
		if(intent.getExtras() != null &&
				MyAppConsts.INTENT_VALUE_OF_STATUS_UPDATE.equals(
						intent.getExtras().getString(MyAppConsts.INTENT_KEY_OF_STATUS))){
			// 記念日内容
			((EditText)findViewById(R.id.anniversary_input)).setText(
					intent.getExtras().getString(MyAppConsts.INTENT_KEY_OF_ANNIVERSARY));
			// 日付
    		DatePicker datePicker = (DatePicker)findViewById(R.id.anniversary_date);
    		int[] date = intent.getExtras().getIntArray(MyAppConsts.INTENT_KEY_OF_DATE);
    		datePicker.init(date[0], date[1], date[2], this);

    		// カウントスタイル
    		RadioGroup radio = (RadioGroup)findViewById(R.id.count_style);
    		String countStyle = intent.getExtras().getString(MyAppConsts.INTENT_KEY_OF_COUNTSTYLE);
    		if(MyAppConsts.STYLE_COUNTDOWN.equals(countStyle)){
    			radio.check(R.id.countdown_radio);
    		}else if(MyAppConsts.STYLE_COUNTUP.equals(countStyle)){
    			radio.check(R.id.countup_radio);
    		}else{
    			// 空になるときはバグ。
    		}
		}

		// 追加ボタン
        Button addButton = (Button)findViewById(R.id.anniversary_add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {

        		// 記念日内容をセット
        		intent.putExtra(
        				MyAppConsts.INTENT_KEY_OF_ANNIVERSARY,
        				((EditText)findViewById(R.id.anniversary_input)).getText().toString());

        		// 記念日をセット
        		DatePicker datePicker = (DatePicker)findViewById(R.id.anniversary_date);
        		int[] date = {
        				datePicker.getYear(),
						datePicker.getMonth(),
						datePicker.getDayOfMonth()
				};
        		intent.putExtra(MyAppConsts.INTENT_KEY_OF_DATE, date);

        		// あと○日か○日後のどちらかをセット
        		RadioGroup radio = (RadioGroup)findViewById(R.id.count_style);
        		int style = radio.getCheckedRadioButtonId();
        		if(style == R.id.countdown_radio){
        			intent.putExtra(
        					MyAppConsts.INTENT_KEY_OF_COUNTSTYLE,
        					MyAppConsts.STYLE_COUNTDOWN);
        		}else if(style == R.id.countup_radio){
        			intent.putExtra(
        					MyAppConsts.INTENT_KEY_OF_COUNTSTYLE,
        					MyAppConsts.STYLE_COUNTUP);
        		}else{
        			intent.putExtra(
        					MyAppConsts.INTENT_KEY_OF_COUNTSTYLE,
        					getString(R.string.error_literal));
        		}

        		setResult(Activity.RESULT_OK, intent);
        		finish();
        	}
        });

        // キャンセルボタン
        Button cancelButton = (Button)findViewById(R.id.anniversary_cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		setResult(Activity.RESULT_CANCELED);
        		finish();
        	}
        });

        // 新規ならリスナーを設定しない。
        if(intent.getExtras() != null &&
        		MyAppConsts.INTENT_VALUE_OF_STATUS_UPDATE.equals(
        				intent.getExtras().getString(MyAppConsts.INTENT_KEY_OF_STATUS))){
            // 削除ボタン
            Button deleteButton = (Button)findViewById(R.id.anniversary_delete_button);
            deleteButton.setOnClickListener(new View.OnClickListener() {
            	public void onClick(View v) {
            		intent.putExtra(
            				MyAppConsts.INTENT_KEY_OF_STATUS,
            				MyAppConsts.INTENT_VALUE_OF_STATUS_DELETE);
            		setResult(Activity.RESULT_OK, intent);
            		finish();
            	}
            });
        }
    }

	/**
	 * 特に何もしない。DatePickerの初期化をしたいがため。
	 */
	public void onDateChanged(
			DatePicker view,
			int year,
			int monthOfYear,
			int dayOfMonth) {
		// 特に何もしない。
	}
}
