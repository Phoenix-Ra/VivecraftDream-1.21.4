package org.vivecraft.mixin.client.renderer.entity.layers;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.extensions.EntityRenderStateExtension;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.mod_compat_vr.immersiveportals.ImmersivePortalsHelper;
import org.vivecraft.mod_compat_vr.shaders.ShadersHelper;

@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerMixin extends RenderLayer {

    public ItemInHandLayerMixin(RenderLayerParent renderer) {
        super(renderer);
    }

    @Inject(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ArmedModel;translateToHand(Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;)V", shift = At.Shift.AFTER))
    private void vivecraft$firstPersonItemScale(
        CallbackInfo ci, @Local(argsOnly = true) ArmedEntityRenderState renderState,
        @Local(argsOnly = true) PoseStack poseStack)
    {
        if (((EntityRenderStateExtension) renderState).vivecraft$isMainPlayer() &&
            ClientDataHolderVR.getInstance().vrSettings.shouldRenderSelf &&
            RenderPass.isFirstPerson(ClientDataHolderVR.getInstance().currentPass) &&
            !ShadersHelper.isRenderingShadows() &&
            !(ImmersivePortalsHelper.isLoaded() && ImmersivePortalsHelper.isRenderingPortal()))
        {
            // make the item scale equal in all directions
            poseStack.translate(0.0F, 0.65F, 0.0F);
            poseStack.scale(1F, ClientDataHolderVR.getInstance().vrSettings.playerModelArmsScale, 1f);
            poseStack.translate(0.0F, -0.65F, 0.0F);
        }
    }
}
