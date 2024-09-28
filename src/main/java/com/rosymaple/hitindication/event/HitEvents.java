package com.rosymaple.hitindication.event;

import com.rosymaple.hitindication.HitIndication;
import com.rosymaple.hitindication.latesthits.HitIndicatorType;
import com.rosymaple.hitindication.latesthits.HitMarkerType;
import com.rosymaple.hitindication.latesthits.PacketsHelper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = HitIndication.MODID)
public class HitEvents {
    @SubscribeEvent
    public static void onAttack(LivingDamageEvent event) {
        Entity attackerProjectile = event.getSource().getImmediateSource();
        Entity attacker = event.getSource().getTrueSource();
        Entity target = event.getEntity();
        if(attackerProjectile instanceof PotionEntity)
            return;

        if(attacker instanceof ServerPlayerEntity)
            if(attackerProjectile instanceof ProjectileEntity && !attacker.getUniqueID().equals(target.getUniqueID()))
                PacketsHelper.addHitMarker((ServerPlayerEntity)attacker, HitMarkerType.CRIT);

        if(!(target instanceof ServerPlayerEntity))
            return;

        ServerPlayerEntity targetPlayer = (ServerPlayerEntity)target;
        int damagePercent = (int)Math.floor((event.getAmount() / targetPlayer.getMaxHealth() * 100));
        if(!(attacker instanceof LivingEntity) || attacker.getUniqueID().equals(target.getUniqueID()))
            PacketsHelper.addHitIndicator(targetPlayer, null, HitIndicatorType.ND_HIT, damagePercent, false);
        else
            PacketsHelper.addHitIndicator(targetPlayer, (LivingEntity)attacker, HitIndicatorType.HIT, damagePercent, false);
    }

    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event) {
        if(!(event.getPlayer() instanceof ServerPlayerEntity) || !event.isVanillaCritical())
            return;

        ServerPlayerEntity player = (ServerPlayerEntity)event.getPlayer();
        PacketsHelper.addHitMarker(player, HitMarkerType.CRIT);
    }

    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        Entity attacker = event.getSource().getTrueSource();
        Entity target = event.getEntity();
        if(!(attacker instanceof ServerPlayerEntity))
            return;
        if(attacker.getUniqueID().equals(target.getUniqueID()))
            return;

        PacketsHelper.addHitMarker((ServerPlayerEntity)attacker, HitMarkerType.KILL);
    }

    @SubscribeEvent
    public static void onBlock(LivingAttackEvent event) {
        Entity attackerProjectile = event.getSource().getImmediateSource();
        Entity attacker = event.getSource().getTrueSource();
        LivingEntity target = event.getEntityLiving();
        if(attackerProjectile instanceof PotionEntity)
            return;
        if(!(attacker instanceof LivingEntity))
            return;

        LivingEntity livingAttacker = (LivingEntity)attacker;

        ItemStack itemInHand = livingAttacker.getHeldItemMainhand();
        boolean targetIsBlocking = canBlockDamageSource(target, event.getSource());
        boolean shieldAboutToBreak = itemInHand.getItem().canDisableShield(itemInHand, target.getActiveItemStack(), target, livingAttacker);
        if(targetIsBlocking) {
            if(target instanceof ServerPlayerEntity) {
                ServerPlayerEntity targetPlayer = (ServerPlayerEntity)target;
                PacketsHelper.addHitIndicator(targetPlayer, livingAttacker, HitIndicatorType.BLOCK, shieldAboutToBreak ? 125 : 0, false);
            }

            if(attacker instanceof ServerPlayerEntity && shieldAboutToBreak) {
                ServerPlayerEntity attackingPlayer = (ServerPlayerEntity)attacker;
                PacketsHelper.addHitMarker(attackingPlayer, HitMarkerType.CRIT);
            }
        }
    }

    @SubscribeEvent
    public static void onPotion(ProjectileImpactEvent.Throwable event) {
        if(!(event.getThrowable().getShooter() instanceof LivingEntity)
                ||!(event.getThrowable().getShooter().getEntityWorld() instanceof ServerWorld)
                || !(event.getThrowable() instanceof PotionEntity))
            return;

        AxisAlignedBB axisalignedbb = event.getThrowable().getBoundingBox().grow(4.0D, 2.0D, 4.0D);
        List<ServerPlayerEntity> list = event.getThrowable().getEntityWorld().getEntitiesWithinAABB(ServerPlayerEntity.class, axisalignedbb);

        LivingEntity source = (LivingEntity)event.getThrowable().getShooter();
        PotionEntity potion = (PotionEntity)event.getThrowable();

        boolean hasNegativeEffects = PotionUtils.getEffectsFromStack(potion.getItem())
                .stream().anyMatch((x) -> !x.getPotion().isBeneficial());
        boolean damagingPotion = PotionUtils.getEffectsFromStack(potion.getItem())
                .stream().anyMatch((x) -> x.getPotion() == Effects.POISON
                        || x.getPotion() == Effects.INSTANT_DAMAGE
                        || x.getPotion() == Effects.WITHER);

        int damagePercent = 0;
        Optional<EffectInstance> instantDamage = PotionUtils.getEffectsFromStack(potion.getItem())
                .stream().filter((x) -> x.getPotion() == Effects.INSTANT_DAMAGE).findFirst();
        for(ServerPlayerEntity player : list) {
            if(!player.canBeHitWithPotion() || player.getUniqueID().equals(source.getUniqueID()))
                continue;

            if(damagingPotion || hasNegativeEffects) {
                if(instantDamage.isPresent())
                    damagePercent = (int)Math.floor(applyPotionDamageCalculations(player, DamageSource.MAGIC, 3*(2<<instantDamage.get().getAmplifier())) / player.getMaxHealth() * 100);

                PacketsHelper.addHitIndicator(player, source, HitIndicatorType.HIT, damagePercent, hasNegativeEffects && !damagingPotion);
            }
        }
    }

    // LivingEntity.canBlockDamageSource
    private static boolean canBlockDamageSource(LivingEntity entity, DamageSource damageSourceIn) {
        if (!damageSourceIn.isUnblockable() && entity.isActiveItemStackBlocking())
        {
            Vector3d vec3d = damageSourceIn.getDamageLocation();

            if (vec3d != null)
            {
                Vector3d vec3d1 = entity.getLook(1.0F);
                Vector3d vec3d2 = vec3d.subtractReverse(new Vector3d(entity.getPosX(), entity.getPosY(), entity.getPosZ())).normalize();
                vec3d2 = new Vector3d(vec3d2.x, 0.0D, vec3d2.z);

                return vec3d2.dotProduct(vec3d1) < 0.0D;
            }
        }

        return false;
    }

    // Player.applyPotionDamageCalculations
    private static float applyPotionDamageCalculations(ServerPlayerEntity player, DamageSource source, float damage) {
        if (source.isDamageAbsolute())
            return damage;
        else {
            if (player.isPotionActive(Effects.RESISTANCE) && source != DamageSource.OUT_OF_WORLD) {
                int i = (player.getActivePotionEffect(Effects.RESISTANCE).getAmplifier() + 1) * 5;
                int j = 25 - i;
                float f = damage * (float)j;
                damage = f / 25.0F;
            }

            if (damage <= 0.0F)
                return 0.0F;
            else {
                int k = EnchantmentHelper.getEnchantmentModifierDamage(player.getArmorInventoryList(), source);
                if (k > 0)
                    damage = CombatRules.getDamageAfterMagicAbsorb(damage, (float)k);

                return damage;
            }
        }
    }
}
