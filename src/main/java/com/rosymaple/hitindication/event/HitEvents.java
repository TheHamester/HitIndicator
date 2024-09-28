package com.rosymaple.hitindication.event;

import com.rosymaple.hitindication.HitIndication;
import com.rosymaple.hitindication.latesthits.HitIndicatorType;
import com.rosymaple.hitindication.latesthits.HitMarkerType;
import com.rosymaple.hitindication.latesthits.PacketsHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = HitIndication.MODID)
public class HitEvents {
    @SubscribeEvent
    public static void onAttack(LivingDamageEvent event) {
        Entity attackerProjectile = event.getSource().getDirectEntity();
        Entity attacker = event.getSource().getEntity();
        Entity target = event.getEntity();
        if(attackerProjectile instanceof ThrownPotion)
            return;

        if(attacker instanceof ServerPlayer attackingPlayer)
            if(attackerProjectile instanceof Projectile && !attacker.getUUID().equals(target.getUUID()))
                PacketsHelper.addHitMarker(attackingPlayer, HitMarkerType.CRIT);

        if(!(target instanceof ServerPlayer targetPlayer))
            return;

        int damagePercent = (int)Math.floor((event.getAmount() / targetPlayer.getMaxHealth() * 100));
        if(!(attacker instanceof LivingEntity livingAttacker) || attacker.getUUID().equals(target.getUUID()))
            PacketsHelper.addHitIndicator(targetPlayer, null, HitIndicatorType.ND_HIT, damagePercent, false);
        else
            PacketsHelper.addHitIndicator(targetPlayer, livingAttacker, HitIndicatorType.HIT, damagePercent, false);
    }

    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event) {
        if(!(event.getPlayer() instanceof ServerPlayer player) || !event.isVanillaCritical())
            return;

        PacketsHelper.addHitMarker(player, HitMarkerType.CRIT);
    }

    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        Entity attacker = event.getSource().getEntity();
        Entity entity = event.getEntity();

        if(!(attacker instanceof ServerPlayer player))
            return;
        if(attacker.getUUID().equals(entity.getUUID()))
            return;

        PacketsHelper.addHitMarker(player, HitMarkerType.KILL);
    }

    @SubscribeEvent
    public static void onBlock(LivingAttackEvent event) {
        Entity attackerProjectile = event.getSource().getDirectEntity();
        Entity attacker = event.getSource().getEntity();
        LivingEntity target = event.getEntityLiving();
        if(attackerProjectile instanceof ThrownPotion)
            return;
        if(!(attacker instanceof LivingEntity livingAttacker))
            return;

        ItemStack itemInHand = livingAttacker.getMainHandItem();
        boolean targetIsBlocking = canBlockDamageSource(target, event.getSource());
        boolean shieldAboutToBreak = itemInHand.getItem().canDisableShield(itemInHand, target.getMainHandItem(), target, livingAttacker);
        if(targetIsBlocking) {
            if(target instanceof ServerPlayer targetPlayer)
                PacketsHelper.addHitIndicator(targetPlayer, livingAttacker, HitIndicatorType.BLOCK, shieldAboutToBreak ? 125 : 0, false);

            if(livingAttacker instanceof ServerPlayer attackingPlayer)
                PacketsHelper.addHitMarker(attackingPlayer, HitMarkerType.CRIT);
        }
    }

    @SubscribeEvent
    public static void onPotion(ProjectileImpactEvent event) {
        if(!(event.getProjectile().getOwner() instanceof LivingEntity source)
                ||!(event.getProjectile().getOwner().getLevel() instanceof ServerLevel)
                || !(event.getProjectile() instanceof ThrownPotion potion))
            return;

        AABB axisalignedbb = event.getProjectile().getBoundingBox().inflate(4.0D, 2.0D, 4.0D);
        List<ServerPlayer> list = event.getProjectile().getLevel().getEntitiesOfClass(ServerPlayer.class, axisalignedbb);

        boolean hasNegativeEffects = PotionUtils.getMobEffects(potion.getItem())
                .stream().anyMatch((x) -> !x.getEffect().isBeneficial());
        boolean damagingPotion = PotionUtils.getMobEffects(potion.getItem())
                .stream().anyMatch((x) -> x.getEffect() == MobEffects.POISON
                        || x.getEffect() == MobEffects.HARM
                        || x.getEffect() == MobEffects.WITHER);

        int damagePercent = 0;
        Optional<MobEffectInstance> instantDamage = PotionUtils.getMobEffects(potion.getItem())
                .stream().filter((x) -> x.getEffect() == MobEffects.HARM).findFirst();
        for(ServerPlayer player : list) {
            if(!player.isAffectedByPotions() || player.getUUID().equals(source.getUUID()))
                continue;

            if(damagingPotion || hasNegativeEffects) {
                if(instantDamage.isPresent()) {
                    damagePercent = (int)Math.floor(applyPotionDamageCalculations(player, DamageSource.MAGIC, 3*(2<<instantDamage.get().getAmplifier())) / player.getMaxHealth() * 100);
                }

                PacketsHelper.addHitIndicator(player, source, HitIndicatorType.HIT, damagePercent, hasNegativeEffects && !damagingPotion);
            }
        }
    }

    // LivingEntity.canBlockDamageSource
    private static boolean canBlockDamageSource(LivingEntity player, DamageSource pDamageSource) {
        Entity entity = pDamageSource.getDirectEntity();
        boolean flag = false;
        if (entity instanceof AbstractArrow) {
            AbstractArrow abstractarrow = (AbstractArrow)entity;
            if (abstractarrow.getPierceLevel() > 0) {
                flag = true;
            }
        }

        if (!pDamageSource.isBypassArmor() && player.isBlocking() && !flag) {
            Vec3 vec32 = pDamageSource.getSourcePosition();
            if (vec32 != null) {
                Vec3 vec3 = player.getViewVector(1.0F);
                Vec3 vec31 = vec32.vectorTo(player.position()).normalize();
                vec31 = new Vec3(vec31.x, 0.0D, vec31.z);
                if (vec31.dot(vec3) < 0.0D) {
                    return true;
                }
            }
        }

        return false;
    }

    // Player.applyPotionDamageCalculations
    private static float applyPotionDamageCalculations(ServerPlayer player, DamageSource pSource, float pDamage)
    {
        if (pSource.isBypassMagic()) {
            return pDamage;
        } else {
            if (player.hasEffect(MobEffects.DAMAGE_RESISTANCE) && pSource != DamageSource.OUT_OF_WORLD) {
                int i = (player.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() + 1) * 5;
                int j = 25 - i;
                float f = pDamage * (float)j;
                float f1 = pDamage;
                pDamage = Math.max(f / 25.0F, 0.0F);
                float f2 = f1 - pDamage;
                if (f2 > 0.0F && f2 < 3.4028235E37F) {
                    if (player instanceof ServerPlayer) {
                        ((ServerPlayer)player).awardStat(Stats.CUSTOM.get(Stats.DAMAGE_RESISTED), Math.round(f2 * 10.0F));
                    } else if (pSource.getEntity() instanceof ServerPlayer) {
                        ((ServerPlayer)pSource.getEntity()).awardStat(Stats.CUSTOM.get(Stats.DAMAGE_DEALT_RESISTED), Math.round(f2 * 10.0F));
                    }
                }
            }

            if (pDamage <= 0.0F) {
                return 0.0F;
            } else {
                int k = EnchantmentHelper.getDamageProtection(player.getArmorSlots(), pSource);
                if (k > 0) {
                    pDamage = CombatRules.getDamageAfterMagicAbsorb(pDamage, (float)k);
                }

                return pDamage;
            }
        }
    }
}
