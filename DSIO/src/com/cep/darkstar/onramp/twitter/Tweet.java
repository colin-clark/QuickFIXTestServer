package com.cep.darkstar.onramp.twitter;

public class Tweet {
	public String text;
	public String user;
	public Double sentiment;
	
	public Tweet() {
		//todo anything specific here?
	}
	
	public Tweet(String text, String user, Double sentiment) {
		super();
		this.text = text;
		this.user = user;
		this.sentiment = sentiment;
	}
	public String getText() {
		return this.text;
	}
	public String getUser() {
		return this.user;
	}
	public Double getSentiment() {
		return this.sentiment;
	}
	public void setText(String text) {
		this.text = text;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public void setSentiment(Double sentiment) {
		this.sentiment = sentiment;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.sentiment == null) ? 0 : this.sentiment.hashCode());
		result = prime * result
				+ ((this.text == null) ? 0 : this.text.hashCode());
		result = prime * result
				+ ((this.user == null) ? 0 : this.user.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tweet other = (Tweet) obj;
		if (this.sentiment == null) {
			if (other.sentiment != null)
				return false;
		} else if (!this.sentiment.equals(other.sentiment))
			return false;
		if (this.text == null) {
			if (other.text != null)
				return false;
		} else if (!this.text.equals(other.text))
			return false;
		if (this.user == null) {
			if (other.user != null)
				return false;
		} else if (!this.user.equals(other.user))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "Tweet [sentiment=" + this.sentiment + ", text=" + this.text
				+ ", user=" + this.user + "]";
	}
	
	
}
