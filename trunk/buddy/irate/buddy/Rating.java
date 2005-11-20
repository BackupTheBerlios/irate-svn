package irate.buddy;

import java.io.Serializable;

import com.sleepycat.bind.serial.ClassCatalog;
import com.sleepycat.bind.serial.SerialSerialBinding;

public class Rating implements Serializable {

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
		return key.userId;
	}

	public UniqueId getTrackId() {
		return key.trackId;
	}

	public float getRating() {
		return data.rating;
	}

	public static class RatingKey implements Serializable {
		private UniqueId userId;

		private UniqueId trackId;

		private RatingKey(UniqueId userId, UniqueId trackId) {
			this.userId = userId;
			this.trackId = trackId;
		}
	}

	public static class RatingData implements Serializable {
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
			return ((Rating) entity).key;
		}

		public Object objectToData(Object entity) {
			return ((Rating) entity).data;
		}

	}
}
