package com.kyosuke25.MemorialMemotter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.http.AccessToken;
import twitter4j.http.OAuthAuthorization;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class MemorialMemotter extends Activity implements OnClickListener {

	List<AnniversaryItem> anniversaryList;

	SharedPreferences pref;
	SharedPreferences prefTwitter;

	int position = 0;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		// 追加ボタンをクリックできるようにする。
        ((ImageView)findViewById(
        		R.id.add_anniversary_menu_button)).setOnClickListener(this);

		// ツイートボタンをクリックできるようにする。
        ((ImageView)findViewById(
        		R.id.tweet_button)).setOnClickListener(this);

        // 表示データを設定ファイルから復元。
        // もし設定ファイルがなければ箱だけ生成。
		// 設定ファイルを呼び出し。
		pref = getSharedPreferences(
				MyAppConsts.PREFERENCE_NAME,
				MODE_PRIVATE);
		if(pref != null){
			// Listを復元。
			this.anniversaryList = MyAppUtil.makeAnniversarryList(this, pref);
		}else{
			// 取得したデータを入れていく箱を生成。
			this.anniversaryList = new ArrayList<AnniversaryItem>();

			// この先prefが無いと困るので作る。
			pref.edit().commit();
		}

		// Twitter設定ファイルを呼び出し。
		prefTwitter = getSharedPreferences(
				MyAppConsts.TWITTER_PREFERENCE_NAME,
				MODE_PRIVATE);
		// nullなら作成。
		if(prefTwitter == null){
			prefTwitter.edit().commit();
		}

		// 記念日リストを表示する。
		displayAnniversaryList();
    }


	/**
	 * 記念日リストを表示する。
	 */
	private void displayAnniversaryList(){

		// Adapterの生成。
		AnniversaryItemAdapter adapter =
			new AnniversaryItemAdapter(this, 0, anniversaryList);

		// 記念日リストに追加。
		ListView anniversaryListView =
			(ListView)findViewById(R.id.anniversary_list);
		anniversaryListView.setAdapter(adapter);

		// クリックできるようにする。
		anniversaryListView.setOnItemClickListener(
				new AdapterView.OnItemClickListener() {
			/**
			 * 明細行をクリックしたときの挙動。
			 */
			public void onItemClick(
					AdapterView<?> list,
					View view,
					int position,
					long id) {
				AnniversaryItem item =
					(AnniversaryItem)list.getItemAtPosition(position);

				// 選択された行数を保持する。
				setPosition(position);

				// AddAnniversaryに飛ばして編集させるためのデータ箱。
				Intent intent = new Intent(
						MemorialMemotter.this,
						AddAnniversary.class);

				// 表示内容をインテントにセット
				// ステータス
				intent.putExtra(
						MyAppConsts.INTENT_KEY_OF_STATUS,
						MyAppConsts.INTENT_VALUE_OF_STATUS_UPDATE);

				// 記念日内容
				intent.putExtra(
						MyAppConsts.INTENT_KEY_OF_ANNIVERSARY,
						item.getAnniversary());
				// 日付
				int[] dateInts = {
						Integer.parseInt(item.getAnniversaryYear()),
						Integer.parseInt(item.getAnniversaryMonth()),
						Integer.parseInt(item.getAnniversaryDay())
				};
				intent.putExtra(MyAppConsts.INTENT_KEY_OF_DATE, dateInts);
				// カウントスタイル
				intent.putExtra(
						MyAppConsts.INTENT_KEY_OF_COUNTSTYLE,
						item.getCountStyle());

				// AddAnniversaryに飛ばして編集させる。
				startAddAnniversaryActivity(intent);
			}
		});
	}

    /**
     * 追加アイコンをクリックしたら記念日入力Activityを呼ぶ。
     * TwitterアイコンをクリックしたらTwitterメソッドを呼ぶ。
     */
	public void onClick(View v) {
		if(R.id.add_anniversary_menu_button == v.getId()){
			startAddAnniversaryActivity();
		}else if(R.id.tweet_button == v.getId()){
			// Twitter連携済みならツイート
			if(MyAppUtil.isConnected(
					prefTwitter.getString(
							MyAppConsts.PREF_KEY_OF_TWITTER_STATUS, ""))){
				tweet();
			}
			// 未連携なら設定画面
			else{
				this.startSettingsActivity();
			}
		}
	}

	/**
	 * 記念日をツイートする。
	 */
	private void tweet(){

		Twitter twitter = this.settingTweet();

		try {
			// 記念日をまとめてツイート文字列を作って投げる。
			String tweet = createTweetMessage();

			// 空文字ならツイートせずに、トーストにどれかをチェックする旨の表示。
			if(!"".equals(tweet)){
				twitter.updateStatus(tweet);
				Toast.makeText(
						this,
						getString(R.string.tweet_complete_message),
						Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(
						this,
						getString(R.string.unchecked_message),
						Toast.LENGTH_LONG).show();
			}
		} catch (TwitterException e) {
			Toast.makeText(
					this, R.string.tweet_send_error,
					Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * ツイート文字列を作る。
	 *
	 * @return
	 */
	private String createTweetMessage(){

		StringBuilder builder = new StringBuilder();

		// チェックが入っている数
		int countCheck = 0;

		// 記念日リストを回す。
		for(Iterator<AnniversaryItem> i=anniversaryList.iterator(); i.hasNext();){
			AnniversaryItem anniversary = i.next();

			// チェックが入ってるのだけが対象。
			if(anniversary.isChecked()){

				countCheck++;

				StringBuilder temp = new StringBuilder();
				temp.append(anniversary.getAnniversary());
				temp.append(":");
				temp.append(anniversary.getCount());
				temp.append("/");

				// 元の文字列とさっき作った文字列とハッシュタグを足して140文字以下の時だけ、
				// ツイート文字列にする。
				if(builder.length() + temp.length() + getString(R.string.hashtag).length() + 1 <= 140){
					builder.append(temp);
				}else{
					// 140文字超えたらおしまい。
					break;
				}
			}
		}

		if(countCheck == 0){
			return "";
		}else{
			// 文字列の最後についた「/」を消す。
			builder.deleteCharAt(builder.length()-1);
			return builder.toString() + " " + getString(R.string.hashtag);
		}
	}

    /**
     * ツイートの準備をする。
     *
     * @author kyosuke25.com
     */
    private Twitter settingTweet(){

		TwitterFactory factory = new TwitterFactory();

		// oauth_tokenとoauth_token_secretを取得。
		String oauthToken  = prefTwitter.getString("oauth_token", "");
		String oauthTokenSecret = prefTwitter.getString("oauth_token_secret", "");

		ConfigurationBuilder builder = new ConfigurationBuilder();

		Configuration config = builder.build();
		factory = new TwitterFactory(config);
		Twitter twitter = factory.getInstance(
					new OAuthAuthorization(
							config,
							MyAppConsts.CONSUMER_KEY,
							MyAppConsts.CONSUMER_SECRET,
							new AccessToken(oauthToken, oauthTokenSecret)));

		return twitter;
    }

	/**
	 * 記念日入力Activityから返ってきたら、記念日リストに追加
	 * STATUSがDELETEなら該当のItemをListから消す。
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

		if(resultCode == Activity.RESULT_OK){
			// インテントからExtrasを取得。
			Bundle bundle = intent.getExtras();

			AnniversaryItem anniversary;
			boolean isDelete = false;

			// 更新時は前のデータを持ってくる。
			if(MyAppConsts.INTENT_VALUE_OF_STATUS_UPDATE.equals(
					bundle.getString(
							MyAppConsts.INTENT_KEY_OF_STATUS))){
				anniversary = anniversaryList.get(position);
			}
			// 削除時はデータを消す。
			else if(MyAppConsts.INTENT_VALUE_OF_STATUS_DELETE.equals(
					bundle.getString(MyAppConsts.INTENT_KEY_OF_STATUS))){
				anniversary = anniversaryList.remove(position);
				isDelete = true;
			}
			// 追加時はnewする。
			else{
				anniversary = new AnniversaryItem();
			}

			// 追加、更新時の時のみ
			if(!isDelete){
				// 入力内容をAnniversaryItemにセット
				// 記念日内容をセット
				anniversary.setAnniversary(
						bundle.getString(
								MyAppConsts.INTENT_KEY_OF_ANNIVERSARY));
				// 記念日日付をセット
				anniversary.setAnniversaryDate(
						MyAppUtil.createDateForm(
								bundle.getIntArray(
										MyAppConsts.INTENT_KEY_OF_DATE)));
				// 記念日年をセット
				anniversary.setAnniversaryYear(
						String.valueOf(
								bundle.getIntArray(
										MyAppConsts.INTENT_KEY_OF_DATE)[0]));
				// 記念日月をセット
				anniversary.setAnniversaryMonth(
						String.valueOf(
								bundle.getIntArray(
										MyAppConsts.INTENT_KEY_OF_DATE)[1]));
				// 記念日日をセット
				anniversary.setAnniversaryDay(
						String.valueOf(
								bundle.getIntArray(
										MyAppConsts.INTENT_KEY_OF_DATE)[2]));
				// あと○日とか○日目の文字列をセット
				anniversary.setCount(
						MyAppUtil.createCountForm(
								this,
								bundle.getString(
										MyAppConsts.INTENT_KEY_OF_COUNTSTYLE),
								bundle.getIntArray(
										MyAppConsts.INTENT_KEY_OF_DATE)));
				// あと○日とか○日目のスタイルIDをセット
				anniversary.setCountStyle(
						bundle.getString(
								MyAppConsts.INTENT_KEY_OF_COUNTSTYLE));

				// 更新時でなければ、記念日リストにセット
				if(!MyAppConsts.INTENT_VALUE_OF_STATUS_UPDATE.equals(
						intent.getExtras().getString(
								MyAppConsts.INTENT_KEY_OF_STATUS))){
					anniversaryList.add(anniversary);
				}
			}

			// インテントのステータスを削除
			intent.putExtra(MyAppConsts.INTENT_KEY_OF_STATUS, "");

			// 表示のたびに設定ファイルに書き込む。
			this.saveData(anniversaryList);

			// 記念日リストを表示する。
			displayAnniversaryList();

		}else{
			super.onActivityResult(requestCode, resultCode, intent);
		}
	}

	/**
	 * リストのデータを設定ファイルに書き込み。
	 *
	 * @param anniversaryList
	 */
	private void saveData(List<AnniversaryItem> anniversaryList){

		// 書き込み準備
		SharedPreferences.Editor editor=pref.edit();
		// 一旦全クリア
		editor.clear();

		// 総記念日数を書き込み
		int counts = anniversaryList.size();
		editor.putInt(
				MyAppConsts.PREF_KEY_OF_ANNIVERSARY_COUNT,
				counts);

		// リストを回して書き込み
		// チェックボックスは値の切り替えのタイミングで書き込んでいるので
		// ここでは書き込まない。
		for(int i=0; i<counts; i++){
			AnniversaryItem anniversary = anniversaryList.get(i);
			editor.putInt(
					MyAppConsts.PREF_KEY_OF_POSITION + i, i);
			editor.putString(
					MyAppConsts.PREF_KEY_OF_ANNIVERSARY + i,
					anniversary.getAnniversary());
			editor.putString(
					MyAppConsts.PREF_KEY_OF_YEAR + i,
					anniversary.getAnniversaryYear());
			editor.putString(
					MyAppConsts.PREF_KEY_OF_MONTH + i,
					anniversary.getAnniversaryMonth());
			editor.putString(
					MyAppConsts.PREF_KEY_OF_DAY + i,
					anniversary.getAnniversaryDay());
			editor.putString(
					MyAppConsts.PREF_KEY_OF_COUNT_STYLE + i,
					anniversary.getCountStyle());
		}

		// コミット
        editor.commit();
	}

	/**
	 * AddAnniversaryを呼ぶ。（初期起動用）
	 */
	private void startAddAnniversaryActivity(){
		Intent intent = new Intent(
				this, AddAnniversary.class);
		startAddAnniversaryActivity(intent);
	}

	/**
	 * AddAnniversaryを呼ぶ。（共通）
	 */
	private void startAddAnniversaryActivity(Intent intent){
		startActivityForResult(
				intent, MyAppConsts.REQUEST_CODE);
	}

	/**
	 * Settingsを呼ぶ。
	 */
	private void startSettingsActivity(){;
		startActivity(
				new Intent(
						this,
						SettingsPreferenceActivity.class));
	}

	/**
	 * リスト内のポジションをセットする。
	 *
	 * @param position
	 */
	private void setPosition(int position){
		this.position = position;
	}

    /**
     * MENUボタンを押したときのメニューの表示
     */
	public boolean onCreateOptionsMenu(Menu menu) {

		boolean bool = super.onCreateOptionsMenu(menu);

		// 設定ボタン
		MenuItem menuItemArea  =
			menu.add(
					0,
					Menu.FIRST,
					Menu.NONE,
					this.getString(
							R.string.menu_settings_title));
		menuItemArea.setIcon(android.R.drawable.ic_menu_manage);

		return bool;
	}

	/**
	 * メニューのいずれかのメニューが押されたときの挙動
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    	// 設定
	    	case Menu.FIRST:
	    		this.startSettingsActivity();
	    		break;
	    }
	    return true;
	}
}