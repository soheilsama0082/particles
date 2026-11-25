package games.enchanted.eg_particle_interactions.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {
    @Shadow @Final private MinecraftClient client;

    /**
     * Adds a small placement burst after a successful block interaction.
     * The effect is intentionally client-side and does not change gameplay.
     */
    @Inject(method = "interactBlock", at = @At("RETURN"))
    private void particleInteractions$afterBlockUse(
            ClientPlayerEntity player,
            ClientWorld world,
            Hand hand,
            BlockHitResult hit,
            CallbackInfoReturnable<ActionResult> cir) {

        if (world == null || cir.getReturnValue() != ActionResult.SUCCESS) {
            return;
        }

        ItemStack stack = player.getStackInHand(hand);
        Vec3d hitPos = hit.getPos();

        if (stack.getItem() == Items.FLINT_AND_STEEL || stack.getItem() == Items.FIRE_CHARGE) {
            particleInteractions$burst(world, ParticleTypes.FLAME, hitPos, 5, 0.035);
            particleInteractions$burst(world, ParticleTypes.SMOKE, hitPos, 3, 0.02);
        } else if (stack.getItem() == Items.WATER_BUCKET) {
            particleInteractions$burst(world, ParticleTypes.SPLASH, hitPos, 8, 0.055);
        } else if (stack.getItem() == Items.LAVA_BUCKET) {
            particleInteractions$burst(world, ParticleTypes.FLAME, hitPos, 7, 0.045);
        } else if (stack.getItem() instanceof net.minecraft.item.BlockItem) {
            particleInteractions$burst(world, ParticleTypes.CLOUD, hitPos, 7, 0.025);
        }
    }

    /**
     * Adds a short burst when the player begins breaking a block.
     * This is a visual cue only; block breaking itself is untouched.
     */
    @Inject(method = "attackBlock", at = @At("RETURN"))
    private void particleInteractions$afterAttackBlock(
            BlockPos pos,
            Direction direction,
            CallbackInfoReturnable<Boolean> cir) {

        if (!cir.getReturnValueZ() || client == null || client.world == null) {
            return;
        }

        Vec3d center = new Vec3d(
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5
        );
        particleInteractions$burst(client.world, ParticleTypes.CRIT, center, 5, 0.045);
    }

    private static void particleInteractions$burst(
            ClientWorld world,
            net.minecraft.particle.ParticleEffect particle,
            Vec3d center,
            int count,
            double speed) {

        for (int i = 0; i < count; i++) {
            double vx = (Math.random() - 0.5) * speed * 2.0;
            double vy = Math.random() * speed;
            double vz = (Math.random() - 0.5) * speed * 2.0;

            world.addParticle(
                    particle,
                    center.x + (Math.random() - 0.5) * 0.35,
                    center.y + (Math.random() - 0.5) * 0.35,
                    center.z + (Math.random() - 0.5) * 0.35,
                    vx, vy, vz
            );
        }
    }
}
