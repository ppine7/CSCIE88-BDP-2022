package cscie88.week2;

import java.time.ZonedDateTime;

/**
 * model class for input logs in the fformat:
 * <uuid> <timestamp> <url> <userId> <country> <ua_browser><ua_os> <response_status> <TTFB>
 * example:
 * 20ac3311c7a64d7a8675e5c1c776857b,2018-09-12T00:03:53.280Z,http://example.com/?url=114,user-038,RS,Firefox,Android,413,0.2393
 * @author marinapopova
 *
 */
public class LogLine {
	String uuid;
	ZonedDateTime eventDateTime;
	String url;
	String userId;
	String country;
	String uaBrowser;
	String uaOs;
	int responseCode;
	float ttfb;
	
	public LogLine(String uuid, ZonedDateTime eventDateTime, String url, String userId, String country,
			String uaBrowser, String uaOs, int responseCode, float ttfb) {
		super();
		this.uuid = uuid;
		this.eventDateTime = eventDateTime;
		this.url = url;
		this.userId = userId;
		this.country = country;
		this.uaBrowser = uaBrowser;
		this.uaOs = uaOs;
		this.responseCode = responseCode;
		this.ttfb = ttfb;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public ZonedDateTime getEventDateTime() {
		return eventDateTime;
	}

	public void setEventDateTime(ZonedDateTime eventDateTime) {
		this.eventDateTime = eventDateTime;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getUaBrowser() {
		return uaBrowser;
	}

	public void setUaBrowser(String uaBrowser) {
		this.uaBrowser = uaBrowser;
	}

	public String getUaOs() {
		return uaOs;
	}

	public void setUaOs(String uaOs) {
		this.uaOs = uaOs;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	public float getTtfb() {
		return ttfb;
	}

	public void setTtfb(float ttfb) {
		this.ttfb = ttfb;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((country == null) ? 0 : country.hashCode());
		result = prime * result + ((eventDateTime == null) ? 0 : eventDateTime.hashCode());
		result = prime * result + responseCode;
		result = prime * result + Float.floatToIntBits(ttfb);
		result = prime * result + ((uaBrowser == null) ? 0 : uaBrowser.hashCode());
		result = prime * result + ((uaOs == null) ? 0 : uaOs.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
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
		LogLine other = (LogLine) obj;
		if (country == null) {
			if (other.country != null)
				return false;
		} else if (!country.equals(other.country))
			return false;
		if (eventDateTime == null) {
			if (other.eventDateTime != null)
				return false;
		} else if (!eventDateTime.equals(other.eventDateTime))
			return false;
		if (responseCode != other.responseCode)
			return false;
		if (Float.floatToIntBits(ttfb) != Float.floatToIntBits(other.ttfb))
			return false;
		if (uaBrowser == null) {
			if (other.uaBrowser != null)
				return false;
		} else if (!uaBrowser.equals(other.uaBrowser))
			return false;
		if (uaOs == null) {
			if (other.uaOs != null)
				return false;
		} else if (!uaOs.equals(other.uaOs))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "LogLine [uuid=" + uuid + ", eventDateTime=" + eventDateTime + ", url=" + url + ", userId=" + userId
				+ ", country=" + country + ", uaBrowser=" + uaBrowser + ", uaOs=" + uaOs + ", responseCode="
				+ responseCode + ", ttfb=" + ttfb + "]";
	}
	
		
}
