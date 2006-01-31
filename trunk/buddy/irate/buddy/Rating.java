package irate.buddy;

import java.io.Serializable;

import com.sleepycat.bind.serial.ClassCatalog;
import com.sleepycat.bind.serial.SerialSerialBinding;

public class Rating implements Serializable {

	static final long serialVersionUID = -7138214834474829683L;

	private RatingKey key;

	private RatingData data;

	public Rating(UniqueId userId, UniqueId trackId, float ratingValue) {
		this.key = new RatingKey(userId, trackId);
		this.data = new RatingData(ratingValue);
	}

	private Rating(RatingKey key, RatingData data) {
		this.key = key;
		this.data = data;
	}

	public RatingKey getKey() {
		return key;
	}

	public UniqueId getUserId() {
		return key.getUserId();
	}

	public UniqueId getTrackId() {
		return key.getTrackId();
	}

	public float getRating() {
		return data.rating;
	}

	public static class RatingKey implements Serializable {
		static final long serialVersionUID = 4435312860486931886L;

		private UniqueId userId;

		private UniqueId trackId;

		private RatingKey(UniqueId userId, UniqueId trackId) {
			this.userId = userId;
			this.trackId = trackId;
		}

		public UniqueId getUserId() {
			return userId;
		}

		public UniqueId getTrackId() {
			return trackId;
		}		
	}

	public static class RatingData implements Serializable {
		static final long serialVersionUID = 5724864233179155579L;

		private float rating;

		private RatingData(float rating) {
			this.rating = rating;
		}
	}

	public static class RatingBinding extends SerialSerialBinding {

		public RatingBinding(ClassCatalog classCatalogue) {
			super(classCatalogue, RatingKey.class, RatingData.class);
		}

		public Object entryToObject(Object key, Object data) {
			return new Rating((RatingKey) key, (RatingData) data);
		}

		public Object objectToKey(Object entity) {
			return (RatingKey)((Rating) entity).key;
		}

		public Object objectToData(Object entity) {
			return (RatingData)((Rating) entity).data;
		}
	}
}
