package cn.a114.klassmaster.gui.data;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public record CopyToClipboardJsonDTO(@Expose @SerializedName("os_name") String os_name,
                                     @Expose @SerializedName("os_arch") String os_arch,
                                     @Expose @SerializedName("os_version") String os_version,
                                     @Expose @SerializedName("klassmaster_version") String klassmaster_version) {

    //    @Expose
//    @SerializedName("HWID")
//    public String HWID;

}
