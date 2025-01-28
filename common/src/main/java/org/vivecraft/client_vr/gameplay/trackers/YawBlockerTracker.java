package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.provider.MCVR;

//PhoenixRa Yaw
public class YawBlockerTracker extends Tracker {
    private boolean isYawLocked;
    private boolean shouldRelease;

    private float lockedHmdYaw;

    private float lockedWorldYaw;



    public YawBlockerTracker(Minecraft mc, ClientDataHolderVR dh) {
        super(mc, dh);
    }


    @Override
    public void doProcess(LocalPlayer player) {
        KeyMapping keyBlockYaw = VivecraftVRMod.INSTANCE.keyBlockYaw;
        if(!isYawLocked && keyBlockYaw.consumeClick()){
            lockYaw(); //freeze
        }
        if(!shouldRelease && isYawLocked && !keyBlockYaw.isDown()){
            shouldRelease = true; //unfreeze
        }
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
        isYawLocked = false;
        shouldRelease = false;
        lockedHmdYaw = 0;
    }

    private void lockYaw(){
        if(isYawLocked) return;
        isYawLocked = true;

        lockedHmdYaw = extractYawFromPose(dh.vr.hmdPose);
        lockedWorldYaw = dh.vrPlayer.vrdata_world_pre.hmd.getYaw()
            - (Mth.RAD_TO_DEG * dh.vrPlayer.vrdata_world_pre.rotation_radians);
    }

    private void releaseYaw(){
        //getting most relevant vr data
        VRData vrData = new VRData(
            dh.vrPlayer.roomOrigin,
            this.dh.vrSettings.walkMultiplier,
            dh.vrPlayer.worldScale,
            Mth.DEG_TO_RAD * this.dh.vrSettings.worldRotation
        );

        //apply yaw offset
        dh.vrSettings.worldRotation = vrData.hmd.getYaw() - lockedWorldYaw;
        dh.vrSettings.worldRotation %= 360.0f;

        //apply rotation changes
        dh.vrPlayer.vrdata_world_pre.rotation_radians
            = Mth.DEG_TO_RAD * this.dh.vrSettings.worldRotation;
        dh.vrPlayer.vrdata_world_post.rotation_radians
            = Mth.DEG_TO_RAD * this.dh.vrSettings.worldRotation;

        //finish
        reset(null);
    }

    private static float extractYawFromPose(Matrix4f matrix) {
        Vector3f forward = new Vector3f(matrix.m20(), matrix.m21(), matrix.m22());

        return (float) Math.atan2(forward.x, forward.z);
    }

    public static void unlockIfShould(){
        ClientDataHolderVR dh = ClientDataHolderVR.getInstance();
        if(dh.yawBlockerTracker == null
            || !dh.yawBlockerTracker.isYawLocked) {
            return;
        }
        if(dh.yawBlockerTracker.shouldRelease){
            dh.yawBlockerTracker.releaseYaw();
        }
    }

    public static Matrix4f lockYawForPose(Matrix4f poseMatrix) {
        ClientDataHolderVR dh = ClientDataHolderVR.getInstance();

        if (dh.yawBlockerTracker == null || !dh.yawBlockerTracker.isYawLocked) {
            return poseMatrix;
        }
        if (dh.yawBlockerTracker.shouldRelease) {
            return poseMatrix;
        }

        Quaternionf rotation = new Quaternionf();
        poseMatrix.getNormalizedRotation(rotation);

        float originalYaw = extractYawFromPose(poseMatrix);
        float yawDifference = dh.yawBlockerTracker.lockedHmdYaw - originalYaw;

        Quaternionf yawLocker = new Quaternionf().rotateY(yawDifference);
        rotation = yawLocker.mul(rotation);

        // Rebuilding pose matrix
        Matrix4f lockedMatrix = new Matrix4f().rotate(rotation);
        //translation
        lockedMatrix.m30(poseMatrix.m30());
        lockedMatrix.m31(poseMatrix.m31());
        lockedMatrix.m32(poseMatrix.m32());

        return lockedMatrix;
    }
}
