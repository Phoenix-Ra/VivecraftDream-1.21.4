package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client_vr.ClientDataHolderVR;

//PhoenixRa Yaw
public class YawBlockerTracker extends Tracker {
    private boolean yawBlocked;
    private float yawBeforeBlocked;
    public YawBlockerTracker(Minecraft mc, ClientDataHolderVR dh) {
        super(mc, dh);
    }

    @Override
    public boolean isActive(LocalPlayer player) {
        if (this.dh.vrSettings.seated) {
            return false;
        }
        return true;
    }

    @Override
    public void reset(LocalPlayer player) {
        yawBlocked = false;
        yawBeforeBlocked = 0;
    }

    @Override
    public void doProcess(LocalPlayer player) {
        KeyMapping keyBlockYaw = VivecraftVRMod.INSTANCE.keyBlockYaw;
        if(!yawBlocked && keyBlockYaw.consumeClick()){
            blockYaw();
        }
        if(yawBlocked && !keyBlockYaw.isDown()){
            releaseYaw();
        }
    }

    public void blockYaw(){
        if(yawBlocked) return;
        yawBlocked = true;
        yawBeforeBlocked = dh.vrPlayer.vrdata_world_pre.hmd.getYaw()
            - (Mth.RAD_TO_DEG * dh.vrPlayer.vrdata_world_pre.rotation_radians);
    }
    public void releaseYaw(){
        if(!yawBlocked) return;
        yawBlocked = false;
        dh.vrSettings.worldRotation = dh.vrPlayer.vrdata_world_pre.hmd.getYaw() - yawBeforeBlocked;
        dh.vrSettings.worldRotation %= 360.0F;
        yawBeforeBlocked = 0;
    }
}
