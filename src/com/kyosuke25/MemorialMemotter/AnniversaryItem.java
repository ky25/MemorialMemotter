package com.kyosuke25.MemorialMemotter;

/**
 * 記念日の明細行データ
 *
 * @author kyosuke
 */
public class AnniversaryItem {

	private String anniversary;
	private String anniversaryYear;
	private String anniversaryMonth;
	private String anniversaryDay;
	private String anniversaryDate;
	private String count;
	private String countStyle;
	private boolean isChecked;

	public String getAnniversary() {
		return anniversary;
	}
	public void setAnniversary(String anniversary) {
		this.anniversary = anniversary;
	}
	public String getAnniversaryYear() {
		return anniversaryYear;
	}
	public void setAnniversaryYear(String anniversaryYear) {
		this.anniversaryYear = anniversaryYear;
	}
	public String getAnniversaryMonth() {
		return anniversaryMonth;
	}
	public void setAnniversaryMonth(String anniversaryMonth) {
		this.anniversaryMonth = anniversaryMonth;
	}
	public String getAnniversaryDay() {
		return anniversaryDay;
	}
	public void setAnniversaryDay(String anniversaryDay) {
		this.anniversaryDay = anniversaryDay;
	}
	public String getAnniversaryDate() {
		return anniversaryDate;
	}
	public void setAnniversaryDate(String anniversaryDate) {
		this.anniversaryDate = anniversaryDate;
	}
	public String getCount() {
		return count;
	}
	public void setCount(String count) {
		this.count = count;
	}
	public String getCountStyle() {
		return countStyle;
	}
	public void setCountStyle(String countStyle) {
		this.countStyle = countStyle;
	}
	public boolean isChecked() {
		return isChecked;
	}
	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}
}
