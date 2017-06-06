package com.ludo3wifi.userInterface.peers;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by eduardogomes on 12/04/17.
 */

public class Buddy implements Parcelable, Comparable<Buddy> {
    public static int TYPE_OWNER = 0x1,
            TYPE_SELF = 0x2,
            TYPE_PEER = 0x4;
    public int peerID;
    public String Username;
    public String Status;
    public WifiP2pDevice device;
    public int type;

    public Buddy(String username, String status, WifiP2pDevice device, int type) {
        this.Username = username;
        this.Status = status;
        this.type = type;
        this.device = device;
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
        if (this.device.deviceAddress == null || !this.device.deviceAddress.equals(other.device.deviceAddress))
            return false;
        return true;
    }


    public int compareTo(Buddy usr) {
        return (this.type > usr.type) ? 1 : -1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.peerID);
        dest.writeString(this.Username);
        dest.writeString(this.Status);
        dest.writeParcelable(this.device, flags);
        dest.writeInt(this.type);
    }

    protected Buddy(Parcel in) {
        this.peerID = in.readInt();
        this.Username = in.readString();
        this.Status = in.readString();
        this.device = in.readParcelable(WifiP2pDevice.class.getClassLoader());
        this.type = in.readInt();
    }

    public static final Creator<Buddy> CREATOR = new Creator<Buddy>() {
        @Override
        public Buddy createFromParcel(Parcel source) {
            return new Buddy(source);
        }

        @Override
        public Buddy[] newArray(int size) {
            return new Buddy[size];
        }
    };
}
