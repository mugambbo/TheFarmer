package ng.com.apcl.thefarmer;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

/**
 * Created by MUGAMBBO on 3/31/2017.
 */

public class Farmer extends GenericJson {

    public static final String COLLECTION = "Farmer";

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    @Key("name")
    private String mName;

    @Key("phone_number")
    private String mPhoneNumber;

    @Key("farm_size")
    private float mFarmSize;

    @Key("coordinates")
    private String mCoordinates;

    @Key("image_id")
    private String mImageID;

    public Farmer(){};
    public Farmer (String name){
        this.mName = name;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getmPhoneNumber() {
        return mPhoneNumber;
    }

    public void setmPhoneNumber(String mPhoneNumber) {
        this.mPhoneNumber = mPhoneNumber;
    }

    public float getmFarmSize() {
        return mFarmSize;
    }

    public void setmFarmSize(float mFarmSize) {
        this.mFarmSize = mFarmSize;
    }

    public String getmCoordinates() {
        return mCoordinates;
    }

    public void setmCoordinates(String mCoordinates) {
        this.mCoordinates = mCoordinates;
    }

    public String getmImageID() {
        return mImageID;
    }

    public void setmImageID(String mImageID) {
        this.mImageID = mImageID;
    }

}
