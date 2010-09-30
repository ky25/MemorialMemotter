package com.kyosuke25.MemorialMemotter;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.http.AccessToken;
import twitter4j.http.RequestToken;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.widget.Toast;

public class SettingsPreferenceActivity extends PreferenceActivity {

	String twitterStatus = "";

	SharedPreferences pref;

	Twitter twitter = null;
	RequestToken requestToken = null;

	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);

		// Twitter連携状態を取得。初回起動時はnull。
		pref = getSharedPreferences(
				MyAppConsts.TWITTER_PREFERENCE_NAME,
				MODE_PRIVATE);

		// Twitter連携の状態を取得。
		if(pref != null){
			twitterStatus  =
				pref.getString(
						MyAppConsts.PREF_KEY_OF_TWITTER_STATUS, "");
		}

		// Twitter設定メニューのタイトルとサマリーを設定する。
		setTwitterPreferenceTitle();
	}

	/**
	 * Twitter設定メニューのタイトルとサマリーを設定する。
	 */
	private void setTwitterPreferenceTitle(){
		PreferenceScreen twitterPreference =
			(PreferenceScreen)findPreference(
					MyAppConsts.PREFERENCE_KEY_OF_TWITTER_SETTING);

		// 連携状態によって説明文とサブ説明文を変える。
		if(MyAppUtil.isConnected(twitterStatus)){
			twitterPreference.setSummary(
					R.string.twitter_disconnect_setting_detail);
		}else{
			twitterPreference.setSummary(
					R.string.twitter_connect_setting_detail);
		}
	}

	/**
	 * Preferenceのいずれかが押されたときの挙動。
	 */
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {

		// Twitter設定が押されたときの挙動
		if(MyAppConsts.PREFERENCE_KEY_OF_TWITTER_SETTING.equals(preference.getKey())){

			// Twitter設定準備
			this.settingTwitter();

			if(!MyAppUtil.isConnected(twitterStatus)){
				// 未連携なのでアクティビティを起動。
				Intent intent = new Intent(this, TwitterLogin.class);
				intent.putExtra("auth_url", requestToken.getAuthorizationURL());
				this.startActivityForResult(
						intent,
						MyAppConsts.REQUEST_CODE);
			}else{
				// 連携済みなので解除
				disconnectTwitter();
			}
		}

		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	/**
	 * 認証後に呼ばれて、access_tokenを保存。
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

		if(resultCode == RESULT_OK){
			super.onActivityResult(requestCode, resultCode, intent);

			AccessToken accessToken = null;

			try {
				accessToken = twitter.getOAuthAccessToken(
						requestToken,
						intent.getExtras().getString("oauth_verifier"));

		        SharedPreferences.Editor editor=pref.edit();
		        editor.putString("oauth_token",accessToken.getToken());
		        editor.putString("oauth_token_secret",accessToken.getTokenSecret());
		        editor.putString("status","available");

		        editor.commit();

		        // 設定おしまい。
		        finish();
			} catch (TwitterException e) {
				Toast.makeText(
						this, R.string.auth_error_message,
						Toast.LENGTH_LONG).show();
			}
		}
	}

	/**
	 * Twitter連携解除
	 */
	private void disconnectTwitter(){
        // 連携状態とトークンの削除
        SharedPreferences.Editor editor=pref.edit();
        editor.remove("oauth_token");
        editor.remove("oauth_token_secret");
        editor.remove("status");

        editor.commit();

        // 設定おしまい。
        finish();
	}

	/**
	 * Twitter設定の準備
	 */
	private void settingTwitter(){

		TwitterFactory factory = new TwitterFactory();
		twitter = factory.getOAuthAuthorizedInstance(
				MyAppConsts.CONSUMER_KEY,
				MyAppConsts.CONSUMER_SECRET);

		try {
			// 認証用URLをインテントにセット。
			requestToken = twitter.getOAuthRequestToken(MyAppConsts.CALLBACK_URL);
		} catch (TwitterException e) {
			Toast.makeText(
					this, R.string.auth_error_message,
					Toast.LENGTH_LONG).show();
		}
	}
}
