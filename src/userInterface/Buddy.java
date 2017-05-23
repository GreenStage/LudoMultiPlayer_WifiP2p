package rmsf.eduardodiogo.ludo3wifi.userInterface;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by eduardogomes on 12/04/17.
 */

public class Buddy implements Parcelable,Comparable<Buddy> {
    WifiP2pDevice deviceInfo;
    String Username;
    String Status;
    public int peerID;
    public static int  TYPE_OWNER = 0x1,
                TYPE_SELF = 0x2,
                TYPE_PEER = 0x4;
    int type;
    public Buddy(WifiP2pDevice deviceinfo,String username, String status, int type){
        this.deviceInfo = deviceinfo;
        this.Username = username;
        this.Status = status;
        this.type = type;
    }

    public void setDevice(WifiP2pDevice device){
        this.deviceInfo = device;
    }




    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Buddy))
            return false;
        Buddy other = (Buddy) obj;
        if (this.deviceInfo == null || !this.deviceInfo.equals(other.deviceInfo))
            return false;
        return true;
    }
    public void maskType(int type){
        this.type &=type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.deviceInfo, flags);
        dest.writeString(this.Username);
        dest.writeString(this.Status);
        dest.writeInt(peerID);
    }

    protected Buddy(Parcel in) {
        this.deviceInfo = in.readParcelable(WifiP2pDevice.class.getClassLoader());
        this.Username = in.readString();
        this.Status = in.readString();
        this.peerID = in.readInt();
    }

    public static final Parcelable.Creator<Buddy> CREATOR = new Parcelable.Creator<Buddy>() {
        @Override
        public Buddy createFromParcel(Parcel source) {
            return new Buddy(source);
        }

        @Override
        public Buddy[] newArray(int size) {
            return new Buddy[size];
        }
    };

    public int compareTo(Buddy usr) {
        return (this.type > usr.type) ? 1 : -1;
    }
}
