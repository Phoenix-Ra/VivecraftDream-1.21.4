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
    private boolean shouldRelease;

    private float worldYawLocked;
    private float offsetYawLocked;

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
        shouldRelease = false;
        worldYawLocked = 0;
    }

    @Override
    public void doProcess(LocalPlayer player) {
        KeyMapping keyBlockYaw = VivecraftVRMod.INSTANCE.keyBlockYaw;
        if(!yawBlocked && keyBlockYaw.consumeClick()){
            blockYaw();
        }
        if(yawBlocked && !keyBlockYaw.isDown()){
            shouldRelease = true;
        }
    }

    private void blockYaw(){
        if(yawBlocked) return;
        yawBlocked = true;
        shouldRelease = false;
        worldYawLocked = dh.vrPlayer.vrdata_world_pre.hmd.getYawRad();
        offsetYawLocked = dh.vrPlayer.vrdata_world_pre.rotation_radians;
    }


    public static void getYawLockFor(VRData data) {
        ClientDataHolderVR dh = ClientDataHolderVR.getInstance();
        YawBlockerTracker yawBlockerTracker = dh.yawBlockerTracker;
        if(!yawBlockerTracker.yawBlocked) return;

        dh.vrSettings.worldRotation = Mth.RAD_TO_DEG *
            (yawBlockerTracker.offsetYawLocked+(data.hmd.getYawRad() - yawBlockerTracker.worldYawLocked));
        dh.vrSettings.worldRotation %= 360.0F;
        if(yawBlockerTracker.shouldRelease){
            yawBlockerTracker.reset(null);
        }

    }
}
