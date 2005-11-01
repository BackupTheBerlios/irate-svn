package irate.buddy;

import irate.buddy.Rating.RatingKey;

import com.sleepycat.bind.serial.ClassCatalog;
import com.sleepycat.bind.serial.SerialSerialKeyCreator;

public class RatingByUserKeyCreator extends SerialSerialKeyCreator {

	public RatingByUserKeyCreator(ClassCatalog classCatalogue) {
		super(classCatalogue, RatingKey.class, Rating.class, UniqueId.class);
	}

	public Object createSecondaryKey(Object primaryKeyInput, Object valueInput) {
		Rating value = (Rating)valueInput;
		return value.getUserId();
	}
}
