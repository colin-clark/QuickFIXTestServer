package com.cep.darkstar.events.twitter;

import java.util.Date;

//twitter word class

public class TwitterWord {
    String	timeZone;
    String 	location;
    String 	word;
    String 	screenName;
    int 	count;
    Date 	tS;
    // these should be boolean!
    String 	mention_YN = "N";
    String	hashTag_YN = "N";
    String	link_YN = "N";
    String  reTweet_YN = "N";
    String 	locUT_YN = "N";
    
    // event creation - constructor
    public TwitterWord(String aTimeZone, String aLocation, String aScreenName, String aWord, int c, long t) {
    	// set raw data fields
    	timeZone = aTimeZone;
    	location = aLocation;
    	screenName = aScreenName;
    	word = aWord;
        count = c;
        tS = new Date(t);
        // set indicator fields
        if (!aWord.isEmpty()) {
        	if (aWord.contains("#")) {hashTag_YN = "Y";}
        	if (aWord.length() > 4) {
        		if (aWord.contains("http")) {link_YN = "Y";}
        	}
        	if (aWord.contains("@")) {mention_YN="Y";}
        	// a tweet is retweeted, not a word
        	if (aWord.contains("RT ")) {reTweet_YN="Y";}
        	if (aWord.contains("�T:")) {locUT_YN="Y";}
        }
    }

	// get methods - fields accessible via query
    public String getTimeZone() {return timeZone;}
    public String getLocation() {return location;}
    public String getWord() {return word;}
    public String getScreenName() {return screenName;}
    public String getHashTag_YN() {return hashTag_YN;}
    public String getLink_YN() {return link_YN;}
    public String getMention_YN() {return mention_YN;}
    public String getReTweet_YN() {return reTweet_YN;}
    public int getcount() {return count;}
    public Date getTS() {return tS;}
    public String getLocUT_YN() {return locUT_YN;}

    @Override
    public String toString() {
        return "ScreenName:" + screenName + " TimeZone:" + timeZone + " Location:" + location + " TimeStamp:" + tS.toString();
    }
}
