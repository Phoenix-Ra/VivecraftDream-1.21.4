package org.vivecraft.client.render;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vivecraft.client.render.models.FeetModel;
import org.vivecraft.client.utils.ModelUtils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.common.network.FBTMode;
import org.vivecraft.common.utils.MathUtils;

public class VRPlayerModel_WithArmsLegs extends VRPlayerModel_WithArms implements FeetModel {
    public static final int LOWER_EXTENSION = 2;
    public static final int UPPER_EXTENSION = 2;

    // thighs use the vanilla leg parts
    public ModelPart leftFoot;
    public ModelPart rightFoot;
    public ModelPart leftFootPants;
    public ModelPart rightFootPants;

    private final Vector3f footDir = new Vector3f();
    private final Vector3f footOffset = new Vector3f();
    private final Vector3f kneeOffset = new Vector3f();

    private final Vector3f footPos = new Vector3f();
    private final Vector3f kneePosTemp = new Vector3f();
    private final Quaternionf footQuat = new Quaternionf();

    public VRPlayerModel_WithArmsLegs(ModelPart root, boolean isSlim) {
        super(root, isSlim);
        this.leftFoot = root.getChild("left_foot");
        this.rightFoot = root.getChild("right_foot");
        this.leftFootPants = this.leftFoot.getChild("left_foot_pants");
        this.rightFootPants = this.rightFoot.getChild("right_foot_pants");

        // copy textures
        ModelUtils.textureHackUpper(this.leftLeg, this.leftFoot);
        ModelUtils.textureHackUpper(this.rightLeg, this.rightFoot);
        ModelUtils.textureHackUpper(this.leftPants, this.rightFootPants);
        ModelUtils.textureHackUpper(this.rightPants, this.leftFootPants);
    }

    public static MeshDefinition createMesh(CubeDeformation cubeDeformation, boolean slim) {
        MeshDefinition meshDefinition = VRPlayerModel_WithArms.createMesh(cubeDeformation, slim);
        PartDefinition partDefinition = meshDefinition.getRoot();

        boolean connected = ClientDataHolderVR.getInstance().vrSettings.playerLimbsConnected;
        int upperExtension = connected ? UPPER_EXTENSION : 0;
        int lowerExtension = connected ? LOWER_EXTENSION : 0;
        float lowerShrinkage = connected ? -0.05F : 0F;

        // feet
        PartDefinition leftFoot = partDefinition.addOrReplaceChild("left_foot", CubeListBuilder.create()
                .texOffs(16, 55 - lowerExtension)
                .addBox(-2.0F, -5.0F - lowerExtension, -2.0F, 4.0F, 5.0F + lowerExtension, 4.0F,
                    cubeDeformation.extend(lowerShrinkage)),
            PartPose.offset(1.9F, 24.0F, 0.0F));
        leftFoot.addOrReplaceChild("left_foot_pants", CubeListBuilder.create()
                .texOffs(0, 55 - lowerExtension)
                .addBox(-2.0F, -5.0F - lowerExtension, -2.0F, 4.0F, 5.0F + lowerExtension, 4.0F,
                    cubeDeformation.extend(0.25F + lowerShrinkage)),
            PartPose.ZERO);
        PartDefinition rightFoot = partDefinition.addOrReplaceChild("right_foot", CubeListBuilder.create()
                .texOffs(0, 23 - lowerExtension)
                .addBox(-2.0F, -5.0F - lowerExtension, -2.0F, 4.0F, 5.0F + lowerExtension, 4.0F,
                    cubeDeformation.extend(lowerShrinkage)),
            PartPose.offset(-1.9F, 24.0F, 0.0F));
        rightFoot.addOrReplaceChild("right_foot_pants", CubeListBuilder.create()
                .texOffs(0, 39 - lowerExtension)
                .addBox(-2.0F, -5.0F - lowerExtension, -2.0F, 4.0F, 5.0F + lowerExtension, 4.0F,
                    cubeDeformation.extend(0.25F + lowerShrinkage)),
            PartPose.ZERO);

        // thighs
        PartDefinition leftThigh = partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(16, 48)
                .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F + upperExtension, 4.0F, cubeDeformation),
            PartPose.offset(1.9F, 12.0F, 0.0F));
        leftThigh.addOrReplaceChild("left_pants", CubeListBuilder.create()
                .texOffs(0, 48)
                .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F + upperExtension, 4.0F, cubeDeformation.extend(0.25f)),
            PartPose.ZERO);
        PartDefinition rightThigh = partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 16)
                .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F + upperExtension, 4.0F, cubeDeformation),
            PartPose.offset(-1.9F, 12.0F, 0.0F));
        rightThigh.addOrReplaceChild("right_pants", CubeListBuilder.create()
                .texOffs(0, 32)
                .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F + upperExtension, 4.0F, cubeDeformation.extend(0.25f)),
            PartPose.ZERO);
        return meshDefinition;
    }

    @Override
    public void setupAnim(PlayerRenderState renderState) {
        super.setupAnim(renderState);

        if (this.rotInfo == null) {
            return;
        }
        boolean noLegs = renderState.isPassenger ||
            (this.laying && (renderState.isInWater || this.rotInfo.fbtMode == FBTMode.ARMS_ONLY)) ||
            renderState.isFallFlying;
        if (!noLegs) {
            if (ClientDataHolderVR.getInstance().vrSettings.playerWalkAnim) {
                // vanilla walking animation on top
                // limbSwingAmount = 1;
                float limbRotation =
                    Mth.cos(renderState.walkAnimationPos * 0.6662F) * renderState.walkAnimationSpeed;
                this.footOffset.set(0, -0.5F, 0)
                    .rotateX(limbRotation)
                    .sub(0, -0.5F, 0)
                    .mul(1F, 0.75F, 1F)
                    .rotateY(-this.bodyYaw);
                this.kneeOffset
                    .set(0, -0.5F, 0)
                    .rotateX(Math.abs(limbRotation))
                    .sub(0, -0.5F, 0);
            } else {
                this.footOffset.zero();
                this.kneeOffset.zero();
            }

            // left leg
            Vector3fc kneePos;
            if (this.rotInfo.fbtMode == FBTMode.ARMS_ONLY) {
                this.footPos.set(this.leftLeg.x, 24 + Math.min(this.body.y, 0F), this.leftLeg.z);
                ModelUtils.modelToWorld(renderState, this.footPos, this.rotInfo, this.bodyYaw, true,
                    this.isMainPlayer, this.footPos);
                this.footQuat.identity().rotateY(Mth.PI - this.bodyYaw);
                if (renderState.isAutoSpinAttack) {
                    // player is offset 1 block during the spin
                    this.footPos.y -= 1F;
                }
            } else {
                this.footPos.set(this.rotInfo.leftFootPos);
                this.footQuat.set(this.rotInfo.leftFootQuat);
            }
            if (this.rotInfo.fbtMode == FBTMode.WITH_JOINTS) {
                this.kneePosTemp.set(this.rotInfo.leftKneePos);
                this.kneePosTemp.add(this.kneeOffset);
                kneePos = this.kneePosTemp;
            } else {
                kneePos = null;
            }

            this.footPos.add(this.footOffset);
            if (ClientDataHolderVR.getInstance().vrSettings.playerLimbsConnected) {
                positionConnectedLimb(renderState, this.leftLeg, this.leftFoot, this.footPos, this.footQuat, 0F,
                    kneePos, false, null);
            } else {
                this.footQuat.transform(MathUtils.BACK, this.footDir);
                positionSplitLimb(renderState, this.leftLeg, this.leftFoot, this.footPos, this.footQuat,
                    -Mth.HALF_PI, 0F, kneePos, false, null);
            }

            // right leg
            if (this.rotInfo.fbtMode == FBTMode.ARMS_ONLY) {
                this.footPos.set(this.rightLeg.x, 24 + Math.min(this.body.y, 0F), this.rightLeg.z);
                ModelUtils.modelToWorld(renderState, this.footPos, this.rotInfo, this.bodyYaw, true,
                    this.isMainPlayer, this.footPos);
                if (renderState.isAutoSpinAttack) {
                    // player is offset 1 block during the spin
                    this.footPos.y -= 1F;
                }
            } else {
                this.footPos.set(this.rotInfo.rightFootPos);
                this.footQuat.set(this.rotInfo.rightFootQuat);
            }

            if (this.rotInfo.fbtMode == FBTMode.WITH_JOINTS) {
                this.kneePosTemp.set(this.rotInfo.rightKneePos);
                this.kneePosTemp.add(this.kneeOffset);
                kneePos = this.kneePosTemp;
            } else {
                kneePos = null;
            }

            this.footPos.add(-this.footOffset.x, this.footOffset.y, -this.footOffset.z);
            if (ClientDataHolderVR.getInstance().vrSettings.playerLimbsConnected) {
                positionConnectedLimb(renderState, this.rightLeg, this.rightFoot, this.footPos, this.footQuat, 0F,
                    kneePos, false, null);
            } else {
                this.footQuat.transform(MathUtils.BACK, this.footDir);
                positionSplitLimb(renderState, this.rightLeg, this.rightFoot, this.footPos, this.footQuat,
                    -Mth.HALF_PI, 0F, kneePos, false, null);
            }
        }

        if (this.layAmount > 0F) {
            ModelUtils.applySwimRotationOffset(renderState, this.xRot, this.tempV, this.tempV2,
                this.leftLeg, this.rightLeg,
                this.leftFoot, this.rightFoot);
        }

        if (noLegs) {
            // align the feet with the legs
            this.footQuat.rotationZYX(this.leftLeg.zRot, this.leftLeg.yRot, this.leftLeg.xRot);
            this.footQuat.transform(0, 12, 0, this.tempV);
            this.leftFoot.setPos(
                this.leftLeg.x + this.tempV.x,
                this.leftLeg.y + this.tempV.y,
                this.leftLeg.z + this.tempV.z);
            this.rightFoot.setPos(
                this.rightLeg.x - this.tempV.x,
                this.rightLeg.y + this.tempV.y,
                this.rightLeg.z + (renderState.isPassenger ? this.tempV.z : -this.tempV.z));
            this.leftFoot.setRotation(this.leftLeg.xRot, this.leftLeg.yRot, this.leftLeg.zRot);
            this.rightFoot.setRotation(this.rightLeg.xRot, this.rightLeg.yRot, this.rightLeg.zRot);
        }

        this.leftFoot.xScale = this.leftFoot.zScale = this.rightFoot.xScale = this.rightFoot.zScale = this.legScale;

        if (renderState.isAutoSpinAttack) {
            spinOffset(this.leftLeg, this.rightLeg, this.leftFoot, this.rightFoot);
        }

        this.leftFootPants.visible = renderState.showLeftPants;
        this.rightFootPants.visible = renderState.showRightPants;
    }

    @Override
    public ModelPart getLeftFoot() {
        return this.leftFoot;
    }

    @Override
    public ModelPart getRightFoot() {
        return this.rightFoot;
    }

    @Override
    public void copyPropertiesTo(HumanoidModel model) {
        super.copyPropertiesTo(model);
        if (model instanceof FeetModel feetModel) {
            feetModel.getLeftFoot().copyFrom(this.leftFoot);
            feetModel.getRightFoot().copyFrom(this.rightFoot);
        }
    }

    @Override
    public void setAllVisible(boolean visible) {
        super.setAllVisible(visible);

        this.leftFoot.visible = visible;
        this.rightFoot.visible = visible;
        this.leftFootPants.visible = visible;
        this.rightFootPants.visible = visible;
    }
}
