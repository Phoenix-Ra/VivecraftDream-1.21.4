package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.provider.MCVR;

//PhoenixRa Yaw
public class YawBlockerTracker extends Tracker {
    private boolean yawBlocked;

    private float worldYawLocked;

    public YawBlockerTracker(Minecraft mc, ClientDataHolderVR dh) {
        super(mc, dh);
    }

    @Override
    public boolean isActive(LocalPlayer player) {
        if (this.dh.vrSettings.seated) {
            return false;
        }
        if(!MCVR.get().headIsTracking) {
            return false;
        }
        return true;
    }

    @Override
    public void reset(LocalPlayer player) {
        yawBlocked = false;
        worldYawLocked = 0;
    }

    @Override
    public void doProcess(LocalPlayer player) {
        KeyMapping keyBlockYaw = VivecraftVRMod.INSTANCE.keyBlockYaw;
        if(!yawBlocked && keyBlockYaw.consumeClick()){
            blockYaw();
        }
        if(yawBlocked && !keyBlockYaw.isDown()){
            reset(null);
        }
    }

    private void blockYaw(){
        if(yawBlocked) return;
        yawBlocked = true;

        worldYawLocked = dh.vrPlayer.vrdata_world_pre.hmd.getYaw();
    }


    public static float getYawLockFor(VRData data) {
        ClientDataHolderVR dh = ClientDataHolderVR.getInstance();
        YawBlockerTracker yawBlockerTracker = dh.yawBlockerTracker;
        if(!yawBlockerTracker.yawBlocked) return -1;

        return (yawBlockerTracker.worldYawLocked - data.hmd.getYaw());

    }
}
